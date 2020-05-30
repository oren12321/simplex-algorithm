import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Constraint {

	enum ConstraintParseSteps {
		CHECK_CONSTRAINT_VALID_CHARACTERS,
		SPLIT_CONSTRAINT_AROUND_EQUALITY_SYMBOL_AND_PARSE_IT,
		CHECK_CONSTRAINT_LEFT_SIDE_VALID_CHARACTERS,
		CHECK_CONSTRAINT_RIGHT_SIDE_VALID_CHARACTERS,
		CHECK_PATTERN_AND_PARSE_RIGHT_SIDE,
		CHECK_PATTERN_AND_PARSE_LEFT_SIDE,
		FINISH_STEPS
	}

	private String[] splittedStringsAroundEqualitySign;


	private String constraintStr;

	private Integer currentEqualityType;
	private Integer previousEqualityType;

	private Rational rightSideBound;

	private ArrayList<Rational> leftSideCoefficients;

	private Boolean validAtCurrentTime;

	public Constraint(String constraintStr) {

		this.validAtCurrentTime = this.stateMachineParser(constraintStr);
		this.normalizeEquation();
		this.previousEqualityType = 0;

	}

	private void normalizeEquation() {
		if(this.rightSideBound.compareTo(new Rational(0)) < 0) {
			this.currentEqualityType *= (-1);
			this.rightSideBound = this.rightSideBound.negative();
			for(Integer cI = 0; cI < this.leftSideCoefficients.size(); cI++) {
				this.leftSideCoefficients.set(cI, this.leftSideCoefficients.get(cI).negative());
			}
		}
	}

	public ArrayList<Rational> getCoefficients() {
		return this.leftSideCoefficients;
	}

	public Rational getBound() {
		return this.rightSideBound;
	}

	public Integer getCurrentEqualityType() {
		return this.currentEqualityType;
	}

	public Integer getPreviousEqualityType() {
		return this.previousEqualityType;
	}

	public void setEquality(Integer equalityType) {
		this.previousEqualityType = this.currentEqualityType;
		this.currentEqualityType = equalityType;
	}

	public Boolean getValid() {
		return this.validAtCurrentTime;
	}

	@Override
	public boolean equals(Object obj) {
		Boolean result = false;
		if(obj instanceof Constraint) {
			result = ((Constraint)obj).constraintStr.equals(this.constraintStr);
		}
		return result;
	}

	private Boolean stateMachineParser(String constraintStr) {

		SimLog.writeln("Start parsing " + constraintStr + "... ");

		this.constraintStr = constraintStr;

		ConstraintParseSteps currentStep = ConstraintParseSteps.CHECK_CONSTRAINT_VALID_CHARACTERS;
		Boolean done = false;
		Boolean result = false;

		while(!done) {

			switch(currentStep) {
			case CHECK_CONSTRAINT_LEFT_SIDE_VALID_CHARACTERS:
				result = this.checkConstraintLeftSideValidCharacters();
				currentStep = (result) ? (ConstraintParseSteps.CHECK_PATTERN_AND_PARSE_RIGHT_SIDE) : (ConstraintParseSteps.FINISH_STEPS);
				break;
			case CHECK_CONSTRAINT_RIGHT_SIDE_VALID_CHARACTERS:
				result = this.checkConstraintRightSideValidCharacters();
				currentStep = (result) ? (ConstraintParseSteps.CHECK_CONSTRAINT_LEFT_SIDE_VALID_CHARACTERS) : (ConstraintParseSteps.FINISH_STEPS);
				break;
			case CHECK_CONSTRAINT_VALID_CHARACTERS:
				result = this.checkConstraintValidCharacters();
				currentStep = (result) ? (ConstraintParseSteps.SPLIT_CONSTRAINT_AROUND_EQUALITY_SYMBOL_AND_PARSE_IT) : (ConstraintParseSteps.FINISH_STEPS);
				break;
			case CHECK_PATTERN_AND_PARSE_LEFT_SIDE:
				result = this.checkPatternAndParseLeftSide();
				currentStep = ConstraintParseSteps.FINISH_STEPS;
				break;
			case CHECK_PATTERN_AND_PARSE_RIGHT_SIDE:
				result = this.checkPatternAndParseRightSide();
				currentStep = (result) ? (ConstraintParseSteps.CHECK_PATTERN_AND_PARSE_LEFT_SIDE) : (ConstraintParseSteps.FINISH_STEPS);
				break;
			case SPLIT_CONSTRAINT_AROUND_EQUALITY_SYMBOL_AND_PARSE_IT:
				result = this.splitConstraintAroundEqualitySymbolAndParseIt();
				currentStep = (result) ? (ConstraintParseSteps.CHECK_CONSTRAINT_RIGHT_SIDE_VALID_CHARACTERS) : (ConstraintParseSteps.FINISH_STEPS);
				break;
			case FINISH_STEPS:
				done = true;
				break;

			}

		}
		this.splittedStringsAroundEqualitySign = null;

		SimLog.writeln("Done parsing " + constraintStr + "... " + ((result) ? ("pass") : ("fail")) + ".");

		return result;
	}



	private Boolean checkConstraintValidCharacters() {
		Boolean result = false;
		SimLog.write("Check if the constraint contains only the valid characters... ");
		Matcher matcher = Pattern.compile("[^x+-><=\\d/]").matcher(this.constraintStr);
		if(!matcher.find()) {
			result = true;
			SimLog.writeln("pass.");
		}
		else {
			SimLog.writeln("fail.");
		}
		return result;
	}

	private Boolean splitConstraintAroundEqualitySymbolAndParseIt() {
		Boolean result = false;
		SimLog.write("Splitting the constraint around the equality symbol... ");
		this.splittedStringsAroundEqualitySign = this.constraintStr.split(">=|<=|>|<|=");
		if(this.splittedStringsAroundEqualitySign.length == 2) {
			result = true;
			SimLog.writeln(" pass.");
			SimLog.write("Splitted parts are : ");
			for(String s : this.splittedStringsAroundEqualitySign) {
				SimLog.write(s + " ");
			}
			SimLog.writeln(" .");
			SimLog.write("Parsing equality symbol... ");
			if(this.constraintStr.contains(">") || this.constraintStr.contains(">=")) {
				this.currentEqualityType = 1;
			}
			else if(this.constraintStr.contains("<") || this.constraintStr.contains("<=")) {
				this.currentEqualityType = -1;
			}
			else if(this.constraintStr.contains("=")) {
				this.currentEqualityType = 0;
			}
			SimLog.writeln(" done.");
		}
		else {
			SimLog.writeln("fail.");
		}
		return result;
	}

	private Boolean checkConstraintLeftSideValidCharacters() {
		Boolean result = false;
		SimLog.write("Check characters validity of left side... ");
		Matcher matcher = Pattern.compile("[^x+-><=\\d/]").matcher(this.splittedStringsAroundEqualitySign[0]);
		if(!matcher.find()) {
			result = true;
			SimLog.writeln("pass.");
		}
		else {
			SimLog.writeln("fail.");
		}
		return result;
	}

	private Boolean checkConstraintRightSideValidCharacters() {
		Boolean result = false;
		SimLog.write("Check characters validity of right side... ");
		Matcher matcher = Pattern.compile("[^-\\d/]").matcher(this.splittedStringsAroundEqualitySign[1]);
		if(!matcher.find()) {
			result = true;
			SimLog.writeln("pass.");
		}
		else {
			SimLog.writeln("fail.");
		}
		return result;
	}

	private Boolean checkPatternAndParseRightSide() {
		Boolean result = false;
		SimLog.write("Check pattern of the constraint right side... ");
		Matcher matcher = Pattern.compile("\\d+/?\\d?").matcher(this.splittedStringsAroundEqualitySign[1]);
		if(matcher.find()) {
			result = true;
			SimLog.writeln("pass.");
			SimLog.write("Parsing the constraint right side... ");
			this.rightSideBound = Rational.parseRational(this.splittedStringsAroundEqualitySign[1]);
			SimLog.writeln("done.");
		}
		else {
			SimLog.writeln("fail.");
		}
		return result;
	}

	private Boolean checkPatternAndParseLeftSide() {
		Boolean result = false;
		SimLog.write("Check pattern of the constraint left side... ");
		Matcher matcher = Pattern.compile("([+-]?\\d*?/?\\d*?x\\d+)*").matcher(this.splittedStringsAroundEqualitySign[0]);
		if(matcher.find()) {
			result = true;
			SimLog.writeln("pass.");

			SimLog.writeln("Parsing each one of the left side components... ");
			matcher = Pattern.compile("[+-]?\\d*?/?\\d*?x\\d+").matcher(this.splittedStringsAroundEqualitySign[0]);
			this.leftSideCoefficients = new ArrayList<Rational>();
			this.leftSideCoefficients.add(new Rational(0));
			while(matcher.find()) {
				String comp = matcher.group();
				SimLog.write("Parsing " + comp + " ... ");
				Rational coeff = null;
				if(comp.charAt(0) == 'x' || comp.substring(0, 2).equals("+x")) {
					coeff = new Rational(1);
				}
				else if(comp.substring(0, 2).equals("-x")) {
					coeff = new Rational(-1);
				}
				else {
					coeff = Rational.parseRational(comp.substring(0, comp.indexOf('x')));
				}
				Integer compIndex = Integer.parseInt(comp.substring(comp.indexOf('x') + 1)) - 1;

				Integer spare = compIndex - this.leftSideCoefficients.size() + 1;
				for(Integer i = 0; i < spare; i++) {
					this.leftSideCoefficients.add(new Rational(0));
				}
				this.leftSideCoefficients.set(this.leftSideCoefficients.size() - 1, coeff);

				SimLog.writeln("done.");
			}
			SimLog.writeln("Parsing left side components pass.");
		}
		else {
			SimLog.writeln("fail.");
		}
		return result;
	}

	@Override
	public String toString() {
		if(Simplex.getConstraintsProcessed()) {
			StringBuilder sb = new StringBuilder();
			Integer counter = 1;
			for(Rational d : this.leftSideCoefficients) {
				if(!d.equals(new Rational(0))) {
					sb.append(d + Simplex.getAliases().get(counter) + " ");
				}
				counter++;
			}
			if(this.currentEqualityType == 0) {
				sb.append("= ");
			}
			else if(this.currentEqualityType == -1) {
				sb.append("<= ");
			}
			else if(this.currentEqualityType == 1) {
				sb.append(">= ");
			}
			sb.append(this.rightSideBound);
			return sb.toString();
		}
		else {
			StringBuilder sb = new StringBuilder();
			Integer counter = 1;
			for(Rational d : this.leftSideCoefficients) {
				if(!d.equals(new Rational(0))) {
					sb.append(d + "X" + counter + " ");
				}
				counter++;
			}
			if(this.currentEqualityType == 0) {
				sb.append("= ");
			}
			else if(this.currentEqualityType == -1) {
				sb.append("<= ");
			}
			else if(this.currentEqualityType == 1) {
				sb.append(">= ");
			}
			sb.append(this.rightSideBound);
			return sb.toString();
		}
	}
}
