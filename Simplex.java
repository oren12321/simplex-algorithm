import java.util.ArrayList;
import java.util.HashMap;



public class Simplex {

	enum ProblemType {
		MINIMUM,
		MAXIMUM
	}

	enum SolutionType {
		SINGLE,
		INFINIT,
		UNBOUND,
		NONE,
		UNKNOWN
	}

	private static String[] solutionTypesStrings = 
		{
		"The problem ended with single solution",
		"The problem ended with infinit solution",
		"The problem ended with unbound solution",
		"The problem ended with no solution"
		};

	private static HashMap<Integer, String> variablesAliases;
	public static HashMap<Integer, String> getAliases() {
		return Simplex.variablesAliases;
	}
	private static Boolean constraintsProcessed;
	public static Boolean getConstraintsProcessed() {
		return Simplex.constraintsProcessed;
	}

	/*
	 * The constraints and objective function of the linear programming problem.
	 */
	private ArrayList<Constraint> constraints;
	private ObjectiveFunction objectiveFunction;

	/*
	 * The matrix of the simplex calculation.
	 */
	private Rational[][] matrix;

	/*
	 * The linear programming problem type - minimum or maximum.
	 */
	private ProblemType problameType;

	/*
	 * Total number of variables in the problem (after addition of slake/artificial variables).
	 */
	private Integer totalNumberOfVariables;

	/*
	 * The current indices of the variables in the basis.
	 */
	private ArrayList<Integer> currentBasis;

	/*
	 * Indication for artificial variables in the problem.
	 */
	private Boolean artificialVariablesExists;

	/*
	 * Indications for :
	 *  1. An in to base candidate has been found.
	 *  2. An out of base cadidate has been found.
	 */
	private Boolean dominantAtCpjFound;
	private Boolean relevantDivisionFound;

	/*
	 * Initialize simplex solver.
	 */
	public Simplex(ArrayList<Constraint> constraints, ObjectiveFunction objectiveFunction) {
		this.constraints = constraints;
		this.objectiveFunction = objectiveFunction;
		this.matrix = null;
		this.problameType = null;
		this.artificialVariablesExists = false;
		this.dominantAtCpjFound = this.relevantDivisionFound = false;

		Simplex.constraintsProcessed = false;
		Simplex.variablesAliases = new HashMap<Integer, String>();
	}

	private Integer bColumnIndex() {
		return this.totalNumberOfVariables;
	}

	private Integer matrixRowSize() {
		return this.totalNumberOfVariables + 1;
	}

	private Integer cpjRealRowIndex() {
		return ((this.artificialVariablesExists) ? (this.matrix.length - 2) : (this.matrix.length - 1));
	}

	private Integer cpjArtificialRowIndex() {
		return (this.matrix.length - 1);
	}

	/*
	 * Main procedure which performs all the steps of simplex solving.
	 */
	public void solve() {

		/*
		 * 1. Complete the constraints with the appropriate slake/artificial variables.
		 */
		this.addSlakeArtificialVariables();

		/*
		 * 2. Move the linear programming problem to its suitable simplex matrix.
		 */
		this.matrixTransformation();

		/*
		 * 3. Perform on the matrix simplex steps until basis change can't be achieved. 
		 */
		while(this.performSimplexStep());

		/*
		 * 4. Catalog the solution type according to the last state of the simplex matrix.
		 */
		SimLog.writeln(Simplex.solutionTypesStrings[this.classifySolution().ordinal()]);

		/*
		 * 5. Output stream off the final solutions 
		 */
		SimLog.writeln(this.toSolutionsString());
	}

	/*
	 * Get solutions string of the current step solutions.
	 */
	private String toSolutionsString() {
		StringBuilder sb = new StringBuilder();
		for(Integer rI = 0; rI < this.currentBasis.size(); rI++) {
			sb.append("X" + this.currentBasis.get(rI) + " = " + this.matrix[rI][this.bColumnIndex()] + "; ");
		}
		if(this.artificialVariablesExists) {
			Rational z = this.matrix[this.cpjRealRowIndex()][this.bColumnIndex()];
			Rational mz = this.matrix[this.cpjArtificialRowIndex()][this.bColumnIndex()];
			sb.append("Z = ");
			if(!mz.equals(new Rational(0))) {
				sb.append(mz + "M ");
			}
			sb.append(z);
		}
		else {
			Rational z = this.matrix[this.cpjRealRowIndex()][this.bColumnIndex()];
			sb.append("Z = " + z);
		}
		return sb.toString();
	}

	/*
	 * Check if there is a variable that it currently out of base and its coefficient is zero.
	 */
	private Boolean notInBaseCoefficientZero() {
		Boolean result = false;

		Rational[] cpjRealRow = this.matrix[this.cpjRealRowIndex()];
		Rational[] cpjArtificialRow = null;
		if(this.artificialVariablesExists) {
			cpjArtificialRow = this.matrix[this.cpjArtificialRowIndex()];
		}

		for(Integer cI = 0; cI < this.matrixRowSize() - 1; cI++) {
			if(!this.currentBasis.contains(cI + 1)) {
				if(this.artificialVariablesExists) {

					if(cpjArtificialRow[cI].equals(new Rational(0)) && cpjRealRow[cI].equals(new Rational(0))) {
						result = true;
					}

				}
				else if(cpjRealRow[cI].equals(new Rational(0))) {
					result = true;
				}
			}
		}
		return result;
	}

	/*
	 * Classify the simplex solution according to the current matrix.
	 */
	private SolutionType classifySolution() {

		SolutionType solutionType = SolutionType.UNKNOWN;

		/*
		 * Check if the problem with artificial variables.
		 */
		if(this.artificialVariablesExists) {

			/*
			 * No one to insert to basis.
			 */
			if(!this.dominantAtCpjFound) {
				/*
				 * Big M still inside the solution.
				 */
				if(!this.matrix[this.cpjArtificialRowIndex()][this.matrix[this.cpjArtificialRowIndex()].length - 1].equals(new Rational(0))) {
					solutionType = SolutionType.NONE;
				}
				else {
				solutionType = SolutionType.SINGLE;
				}
			}
			/*
			 * There is a candidate to insert to the basis.
			 */
			else {
				/*
				 * But no one to put out from it.
				 */
				if(!this.relevantDivisionFound) {
					solutionType = SolutionType.UNBOUND;
				}
			}
		}
		else {
			if(!this.dominantAtCpjFound) {
				if(this.notInBaseCoefficientZero()) {
					solutionType = SolutionType.INFINIT;
				}
				else {
					solutionType = SolutionType.SINGLE;
				}
			}
			else {
				if(!this.relevantDivisionFound) {
					solutionType = SolutionType.UNBOUND;
				}
			}
		}

		return solutionType;

	}

	/*
	 * Problem to matrix transformation for all problems types.
	 */
	private void matrixTransformation() {
		if(this.artificialVariablesExists) {
			this.matrixTransformationWithArtificialVariables();
		}
		else {
			this.matrixTransformationWithoutArtificialVariables();
		}
	}

	/*
	 * Perform one simplex step on the matrix.
	 */
	private Boolean performSimplexStep() {

		Integer[] basisReplacement = null;
		if(this.artificialVariablesExists) {
			basisReplacement = this.findBasisReplacementWithArtificialVariables();
		}
		else {
			basisReplacement = this.findBasisReplacementWithoutArtificialVariables();
		}
		Boolean anyActionDone = false;

		if(basisReplacement != null) {

			anyActionDone = true;

			Rational[][] afterMatrix = new Rational[this.matrix.length][];

			for(Integer rI = 0; rI < afterMatrix.length; rI++) {

				afterMatrix[rI] = new Rational[this.matrix[rI].length];
				System.arraycopy(this.matrix[rI], 0, afterMatrix[rI], 0, afterMatrix[rI].length);
			}


			Integer inToBasis = basisReplacement[0];
			Integer outFromBasis = basisReplacement[1];
			Integer pivotR = this.currentBasis.indexOf(outFromBasis);
			Integer pivotC = inToBasis - 1;
			Rational pivot = this.matrix[pivotR][pivotC];

//			SimLog.writeln("Basis replacement : in = " + inToBasis + ", out = " + outFromBasis + ", pivot = " + pivot);
			SimLog.writeln("Basis replacement : in -> " + Simplex.variablesAliases.get(inToBasis) + ", out -> " + Simplex.variablesAliases.get(outFromBasis) + ", pivot = " + pivot);

			for(Integer cI = 0; cI < afterMatrix[pivotR].length; cI++) {
				afterMatrix[pivotR][cI] =  afterMatrix[pivotR][cI].divide(pivot);
			}

			for(Integer rI = 0; rI < pivotR; rI++) {
				for(Integer cI = 0; cI < afterMatrix[rI].length; cI++) {
					afterMatrix[rI][cI] = this.matrix[rI][cI].subtract((this.matrix[pivotR][cI].multiply(this.matrix[rI][pivotC])).divide(pivot));
				}
			}

			for(Integer rI = pivotR + 1; rI < afterMatrix.length; rI++) {

				for(Integer cI = 0; cI < afterMatrix[rI].length; cI++) {
					afterMatrix[rI][cI] = this.matrix[rI][cI].subtract((this.matrix[pivotR][cI].multiply(this.matrix[rI][pivotC])).divide(pivot));
				}
			}

			this.currentBasis.set(pivotR, inToBasis);


			this.matrix = afterMatrix;
			SimLog.writeln("Performing simplex step. Suitable matrix is now :");
			SimLog.write(this.toMatrixString());
		}

		return anyActionDone;
	}

	/*
	 * Find basis replacement for problem with artificial variables (negative b's).
	 */
	private Integer[] findBasisReplacementWithArtificialVariables() {
		Integer[] basisReplacement = new Integer[2];

		ArrayList<Integer> maxArtColIndices = new ArrayList<Integer>();

		Boolean dominantAtCpjFound = false;
		Integer inToBasis = 0;

		// find dominant artificial weight
		Rational dominantArtificialWeight = new Rational(0);
		Rational[] artificialRow = this.matrix[this.cpjArtificialRowIndex()];
		for(Integer cI = 0; cI < artificialRow.length - 1; cI++) {
			if(this.problameType == ProblemType.MAXIMUM) {
				if(artificialRow[cI].compareTo(new Rational(0)) < 0 && dominantArtificialWeight.compareTo(artificialRow[cI]) > 0) {
					dominantArtificialWeight = artificialRow[cI];
					inToBasis = cI;
					dominantAtCpjFound = true;
				}
			}
			else if(this.problameType == ProblemType.MINIMUM) {
				if(artificialRow[cI].compareTo(new Rational(0)) > 0 && dominantArtificialWeight.compareTo(artificialRow[cI]) < 0) {
					dominantArtificialWeight = artificialRow[cI];
					inToBasis = cI;
					dominantAtCpjFound = true;
				}
			}
		}
		// find suitable artificial indices
		for(Integer cI = 0; cI < artificialRow.length - 1; cI++) {
			if(artificialRow[cI] == dominantArtificialWeight) {
				maxArtColIndices.add(cI);
			}
		}
		// find dominant weight around the artificial indices
		Rational[] dominantsRow = this.matrix[this.cpjRealRowIndex()];
		Rational dominant = new Rational(0);
		if(dominantAtCpjFound) {
			for(Integer cI = 0; cI < maxArtColIndices.size(); cI++) {
				if(this.problameType == ProblemType.MAXIMUM) {
					if(dominantsRow[maxArtColIndices.get(cI)].compareTo(new Rational(0)) < 0 && dominant.compareTo(dominantsRow[maxArtColIndices.get(cI)]) > 0) {
						dominant = dominantsRow[maxArtColIndices.get(cI)];
						inToBasis = maxArtColIndices.get(cI);
					}
				}
				else if(this.problameType == ProblemType.MINIMUM) {
					if(dominantsRow[maxArtColIndices.get(cI)].compareTo(new Rational(0)) > 0 && dominant.compareTo(dominantsRow[maxArtColIndices.get(cI)]) < 0) {
						dominant = dominantsRow[maxArtColIndices.get(cI)];
						inToBasis = maxArtColIndices.get(cI);
					}
				}
			}
		}
		else {
			for(Integer cI = 0; cI < dominantsRow.length - 1; cI++) {
				if(this.problameType == ProblemType.MAXIMUM) {
					if(dominantsRow[cI].compareTo(new Rational(0)) < 0 && dominant.compareTo(dominantsRow[cI]) > 0) {
						if(artificialRow[cI].compareTo(new Rational(0)) == 0) {
							dominant = dominantsRow[cI];
							inToBasis = cI;
							dominantAtCpjFound = true;
						}
					}
				}
				else if(this.problameType == ProblemType.MINIMUM) {
					if(dominantsRow[cI].compareTo(new Rational(0)) > 0 && dominant.compareTo(dominantsRow[cI]) < 0) {
						if(artificialRow[cI].compareTo(new Rational(0)) == 0) {
							dominant = dominantsRow[cI];
							inToBasis = cI;
							dominantAtCpjFound = true;
						}
					}
				}
			}
		}
		basisReplacement[0] = inToBasis + 1;

		// find division
		Rational division = Rational.infinity();
		Integer outFromBasis = 0;
		Boolean divisionFound = false;
		for(Integer rI = 0; rI < this.currentBasis.size(); rI++) {
			if(this.matrix[rI][inToBasis].compareTo(new Rational(0)) > 0 && this.matrix[rI][this.matrix[rI].length - 1].compareTo(new Rational(0)) >= 0) {
				if(this.matrix[rI][this.matrix[rI].length - 1].divide(this.matrix[rI][inToBasis]).compareTo(division) < 0) {
					division = this.matrix[rI][this.matrix[rI].length - 1].divide(this.matrix[rI][inToBasis]);
					outFromBasis = this.currentBasis.get(rI);
					divisionFound = true;
				}
			}
		}
		basisReplacement[1] = outFromBasis;

		if(!(dominantAtCpjFound && divisionFound)) {
			basisReplacement = null;
		}

		this.dominantAtCpjFound = dominantAtCpjFound;
		this.relevantDivisionFound = divisionFound;

		return basisReplacement;
	}

	/*
	 * Find basis replacement for problem without artificial variables (no negative b's).
	 */
	private Integer[] findBasisReplacementWithoutArtificialVariables() {
		Integer[] basisReplacement = new Integer[2];

		Rational dominant = new Rational(0);
		Integer inToBasis = 0;
		Boolean dominantFound = false;

		Rational[] dominantsRow = this.matrix[this.cpjRealRowIndex()];

		for(Integer cI = 0; cI < dominantsRow.length - 1; cI++) {
			if(this.problameType == ProblemType.MAXIMUM) {
				if(dominantsRow[cI].compareTo(new Rational(0)) < 0 && dominant.compareTo(dominantsRow[cI]) > 0) {
					dominant = dominantsRow[cI];
					inToBasis = cI;
					dominantFound = true;
				}
			}
			else if(this.problameType == ProblemType.MINIMUM) {
				if(dominantsRow[cI].compareTo(new Rational(0)) > 0 && dominant.compareTo(dominantsRow[cI]) < 0) {
					dominant = dominantsRow[cI];
					inToBasis = cI;
					dominantFound = true;
				}
			}
		}
		basisReplacement[0] = inToBasis + 1;

		Rational division = Rational.infinity();
		Integer outFromBasis = 0;
		Boolean divisionFound = false;
		for(Integer rI = 0; rI < this.currentBasis.size(); rI++) {
			if(this.matrix[rI][inToBasis].compareTo(new Rational(0)) > 0 && this.matrix[rI][this.matrix[rI].length - 1].compareTo(new Rational(0)) >= 0) {
				if(this.matrix[rI][this.matrix[rI].length - 1].divide(this.matrix[rI][inToBasis]).compareTo(division) < 0) {
					division = this.matrix[rI][this.matrix[rI].length - 1].divide(this.matrix[rI][inToBasis]);
					outFromBasis = this.currentBasis.get(rI);
					divisionFound = true;
				}
			}
		}
		basisReplacement[1] = outFromBasis;


		if(!(dominantFound && divisionFound)) {
			basisReplacement = null;
		}

		this.dominantAtCpjFound = dominantFound;
		this.relevantDivisionFound = divisionFound;

		return basisReplacement;
	}

	/*
	 * Problem transformation to matrix for regular problem.
	 */
	private void matrixTransformationWithoutArtificialVariables() {

		SimLog.writeln("1 objective function + " + this.constraints.size() + " constraints => " + (this.constraints.size() + 1) + " rows in matrix.");
		this.currentBasis = new ArrayList<Integer>();

		this.matrix = new Rational[this.constraints.size() + 1][];

		for(Integer rI = 0; rI < this.matrix.length - 1; rI++) {

			this.matrix[rI] = new Rational[this.matrixRowSize()];
			ArrayList<Rational> coeffs = this.constraints.get(rI).getCoefficients();
			for(Integer cI = 0; cI < this.matrix[rI].length; cI++) {
				if(cI < coeffs.size()) {
					this.matrix[rI][cI] = coeffs.get(cI);
				}
				else {
					this.matrix[rI][cI] = new Rational(0);
				}
			}
			this.matrix[rI][this.matrix[rI].length - 1] = this.constraints.get(rI).getBound();
			this.currentBasis.add(coeffs.size());
		}

		this.matrix[this.cpjRealRowIndex()] = new Rational[this.matrixRowSize()];

		ArrayList<Rational> coeffs = this.objectiveFunction.getCoefficients();
		for(Integer cI = 0; cI < this.totalNumberOfVariables; cI++) {
			if(cI < coeffs.size()) {

				if(!coeffs.get(cI).equals(Rational.infinity())) {
					this.matrix[this.cpjRealRowIndex()][cI] = coeffs.get(cI).negative();
				}
				else {
					this.matrix[this.cpjRealRowIndex()][cI] = new Rational(1);
				}
			}
			else {

				this.matrix[this.cpjRealRowIndex()][cI] = new Rational(0);
			}
		}

		this.matrix[this.cpjRealRowIndex()][this.bColumnIndex()] = new Rational(0);

		SimLog.writeln("Suitable matrix :");
		SimLog.write(this.toMatrixString());
	}


	/*
	 * Problem to matrix transformation for problem with artificial variables.
	 */
	private void matrixTransformationWithArtificialVariables() {

		SimLog.writeln("1 objective function + " + this.constraints.size() + " constraints + artificial variables => " + (this.constraints.size() + 1 + 1) + " rows in matrix.");

		this.currentBasis = new ArrayList<Integer>();

		this.matrix = new Rational[this.constraints.size() + 2][];

		for(Integer rI = 0; rI < this.matrix.length - 2; rI++) {

			this.matrix[rI] = new Rational[this.matrixRowSize()];
			ArrayList<Rational> coeffs = this.constraints.get(rI).getCoefficients();
			for(Integer cI = 0; cI < this.matrix[rI].length; cI++) {
				if(cI < coeffs.size()) {
					this.matrix[rI][cI] = coeffs.get(cI);
				}
				else {
					this.matrix[rI][cI] = new Rational(0);
				}
			}
			this.matrix[rI][this.matrix[rI].length - 1] = this.constraints.get(rI).getBound();
			this.currentBasis.add(coeffs.size());
		}

		this.matrix[this.cpjRealRowIndex()] = new Rational[this.matrixRowSize()];

		ArrayList<Rational> coeffs = this.objectiveFunction.getCoefficients();
		for(Integer cI = 0; cI < this.totalNumberOfVariables; cI++) {
			if(!coeffs.get(cI).equals(Rational.infinity()) && !coeffs.get(cI).equals(Rational.infinity().negative())) {
				this.matrix[this.cpjRealRowIndex()][cI] = coeffs.get(cI).negative();
			}
			else {
				this.matrix[this.cpjRealRowIndex()][cI] = new Rational(0);
			}

		}
		this.matrix[this.cpjRealRowIndex()][this.bColumnIndex()] = new Rational(0);


		// Artificial variables row
		this.matrix[this.cpjArtificialRowIndex()] = new Rational[this.matrixRowSize()];
		for(Integer cI = 0; cI < this.totalNumberOfVariables; cI++) {
			Rational infWeight = new Rational(0);
			for(Integer rI = 0; rI < this.constraints.size(); rI++) {
				Constraint currentConstraint = this.constraints.get(rI);
				if(currentConstraint.getPreviousEqualityType() == 1 || currentConstraint.getPreviousEqualityType() == 0) {
					infWeight = infWeight.add(this.matrix[rI][cI]);
				}
			}

			if(this.problameType == ProblemType.MAXIMUM) {
				this.matrix[this.cpjArtificialRowIndex()][cI] = infWeight.negative();
			}
			else if(this.problameType == ProblemType.MINIMUM) {
				this.matrix[this.cpjArtificialRowIndex()][cI] = infWeight;
			}

			if(this.objectiveFunction.getCoefficients().get(cI).equals(Rational.infinity()) || this.objectiveFunction.getCoefficients().get(cI).equals(Rational.infinity().negative())) {
				this.matrix[this.cpjArtificialRowIndex()][cI] = new Rational(0);
			}
		}

		Rational infWeight = new Rational(0);
		for(Integer rI = 0; rI < this.constraints.size(); rI++) {
			Constraint currentConstraint = this.constraints.get(rI);
			if(currentConstraint.getPreviousEqualityType() == 1) {
				infWeight = infWeight.add(this.matrix[rI][this.bColumnIndex()]);
			}
		}
		if(this.problameType == ProblemType.MAXIMUM) {
			this.matrix[this.cpjArtificialRowIndex()][this.bColumnIndex()] = infWeight.negative();
		}
		else if(this.problameType == ProblemType.MINIMUM) {
			this.matrix[this.cpjArtificialRowIndex()][this.bColumnIndex()] = infWeight;
		}


		SimLog.writeln("Suitable matrix :");
		SimLog.write(this.toMatrixString());
	}





	/*
	 * Add slake/artificial variables (coefficients in fact) to the constraints/objective function.
	 */
	private void addSlakeArtificialVariables() {



		if(this.objectiveFunction.getMinMax().equals("max")) {
			this.problameType = ProblemType.MAXIMUM;
		}
		else if(this.objectiveFunction.getMinMax().equals("min")) {
			this.problameType = ProblemType.MINIMUM;
		}

		// find number of variables in problem
		Integer currentNumberOfVariables = 0;
		for(Constraint c : this.constraints) {
			if(c.getCoefficients().size() > currentNumberOfVariables) {
				currentNumberOfVariables = c.getCoefficients().size();
			}

			// register aliases of original variables
			Simplex.constraintsProcessed = true;
			Simplex.variablesAliases.clear();
			for(Integer cI = 0; cI < c.getCoefficients().size(); cI++) {
				if(!Simplex.variablesAliases.containsKey(cI + 1)) {
					Simplex.variablesAliases.put(cI + 1, "X" + (cI + 1));
				}
			}
		}

		// register aliases of original variables
		Integer slakesCounter = currentNumberOfVariables;
		Integer artificialsCounter = 0;
		
		// complete the constraints variables
		for(Constraint c : this.constraints) {
			if(c.getCurrentEqualityType() == 1) {
				while(c.getCoefficients().size() < currentNumberOfVariables) {
					c.getCoefficients().add(new Rational(0));
				}
				c.getCoefficients().add(new Rational(-1));
				c.getCoefficients().add(new Rational(1));
				c.setEquality(0);

				this.objectiveFunction.getCoefficients().add(new Rational(0));
				
				if(this.problameType == ProblemType.MAXIMUM) {
					this.objectiveFunction.getCoefficients().add(Rational.infinity().negative());
				}
				else if(this.problameType == ProblemType.MINIMUM) {
					this.objectiveFunction.getCoefficients().add(Rational.infinity());
				}
				

				this.artificialVariablesExists = true;

				currentNumberOfVariables = c.getCoefficients().size();
				
				// register aliases of original variables
				Simplex.variablesAliases.put(currentNumberOfVariables - 1, "X" + (++slakesCounter));
				Simplex.variablesAliases.put(currentNumberOfVariables, "Y" + (++artificialsCounter));
			}
			else if(c.getCurrentEqualityType() == -1) {
				while(c.getCoefficients().size() < currentNumberOfVariables) {
					c.getCoefficients().add(new Rational(0));
				}
				c.getCoefficients().add(new Rational(1));
				c.setEquality(0);

				this.objectiveFunction.getCoefficients().add(new Rational(0));

				currentNumberOfVariables = c.getCoefficients().size();
				
				// register aliases of original variables
				Simplex.variablesAliases.put(currentNumberOfVariables, "X" + (++slakesCounter));
			}
			else if(c.getCurrentEqualityType() == 0) {
				while(c.getCoefficients().size() < currentNumberOfVariables) {
					c.getCoefficients().add(new Rational(0));
				}
				c.getCoefficients().add(new Rational(1));
				c.setEquality(0);

				if(this.problameType == ProblemType.MAXIMUM) {
					this.objectiveFunction.getCoefficients().add(Rational.infinity().negative());
				}
				else if(this.problameType == ProblemType.MINIMUM) {
					this.objectiveFunction.getCoefficients().add(Rational.infinity());
				}

				this.artificialVariablesExists = true;

				currentNumberOfVariables = c.getCoefficients().size();
				
				// register aliases of original variables
				Simplex.variablesAliases.put(currentNumberOfVariables, "X" + (++slakesCounter));
			}
		}

		this.totalNumberOfVariables = currentNumberOfVariables;

		SimLog.writeln("Problem after normalization with slake/artificial variables :");
		SimLog.write(this.toString());

	}

	/*
	 * Table row separator.
	 */
	private String separator() {
		StringBuilder sb = new StringBuilder();
		for(Integer cI = 0; cI < this.totalNumberOfVariables + 2; cI++) {
			sb.append("----------|");
		}
		sb.append("\n");
		return sb.toString();
	}

	private String toMatrixString() {
		StringBuilder sb = new StringBuilder();

		// first row
		sb.append(String.format("%10s|", ""));
		for(Integer cI = 0; cI < this.totalNumberOfVariables; cI++) {
//			sb.append(String.format("%10s|", "X" + (cI + 1)));
			sb.append(String.format("%10s|", Simplex.variablesAliases.get(cI + 1)));
		}
		sb.append(String.format("%10s", "B"));
		sb.append("\n");

		sb.append(this.separator());

		// constraints rows
		for(Integer rI = 0; rI < this.cpjRealRowIndex(); rI++) {

//			sb.append(String.format("%10s|", "X" + this.currentBasis.get(rI)));
			sb.append(String.format("%10s|", Simplex.variablesAliases.get(this.currentBasis.get(rI))));
			for(Integer cI = 0; cI < this.matrix[rI].length; cI++) {
				String output = this.matrix[rI][cI].toString();
				if(output.length() > 10) {
					output = output.substring(0, 10);
				}
				sb.append(String.format("%10s|", output));
			}
			sb.append("\n");
			sb.append(this.separator());
		}

		sb.append(String.format("%10s|", "C'j"));

		for(Integer cI = 0; cI < this.matrix[this.cpjRealRowIndex()].length - 1; cI++) {

			String output = this.matrix[this.cpjRealRowIndex()][cI].toString();

			if(output.length() > 10) {
				output = output.substring(0, 10);
			}
			sb.append(String.format("%10s|", output));
		}
		sb.append(String.format("%10s", "Z = " + this.matrix[this.cpjRealRowIndex()][this.matrix[this.cpjRealRowIndex()].length - 1]) + "|");

		sb.append("\n");
		sb.append(this.separator());

		if(this.artificialVariablesExists) {

			sb.append(String.format("%10s|", "IW"));
			for(Integer cI = 0; cI < this.matrix[this.cpjArtificialRowIndex()].length - 1; cI++) {

				String output = this.matrix[this.cpjArtificialRowIndex()][cI].toString();

				if(output.length() > 10) {
					output = output.substring(0, 10);
				}
				sb.append(String.format("%10s|", output));
			}
			sb.append(String.format("%10s", "Z = " + this.matrix[this.cpjArtificialRowIndex()][this.matrix[this.cpjArtificialRowIndex()].length - 1]) + "|");

			sb.append("\n");
			sb.append(this.separator());
		}

		return sb.toString();
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(((this.objectiveFunction.getMinMax().equals("max")) ? ("Maximum") : ("Minimum")) + " problem :\n");
		sb.append("\t" + this.objectiveFunction + "\n");
		sb.append("Subject to :\n");
		for(Constraint c : this.constraints) {
			sb.append("\t" + c + "\n");
		}
		return sb.toString();
	}
	
}
