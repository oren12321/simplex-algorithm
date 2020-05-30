import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ObjectiveFunction {

	enum ObjectiveFunctionParseSteps {
		CHECK_VALID_CHARACTERS,
		SPLIT_AROUND_EQUALITY_SYMBOL,
		CHECK_LEFT_SIDE_VALID_CHARACTERS,
		CHECK_RIGHT_SIDE_VALID_CHARACTERS,
		PARSE_LEFT_SIDE,
		PARSE_RIGHT_SIDE,
		FINISH_STEPS
	}

	private String[] splittedStringsAroundEqualitySign;


	private String objectiveFunctionStr;

	private ArrayList<Rational> rightSideCoefficients;

	private String minMaxStr;

	private Boolean validAtCurrentTime;

	public ObjectiveFunction(String objectiveFunctionStr) {

		this.validAtCurrentTime = this.stateMachineParser(objectiveFunctionStr);

	}

	public ArrayList<Rational> getCoefficients() {
		return this.rightSideCoefficients;
	}

	public String getMinMax() {
		return this.minMaxStr;
	}
	
	public void setMinMax(String minMaxStr) {
		this.minMaxStr = minMaxStr;
	}

	public Boolean getValid() {
		return this.validAtCurrentTime;
	}

	@Override
	public boolean equals(Object obj) {
		Boolean result = false;
		if(obj instanceof ObjectiveFunction) {
			result = ((ObjectiveFunction)obj).objectiveFunctionStr.equals(this.objectiveFunctionStr);
		}
		return result;
	}

	private Boolean stateMachineParser(String objFuncStr) {

		SimLog.writeln("Start parsing " + objFuncStr + "... ");

		this.objectiveFunctionStr = objFuncStr;

		ObjectiveFunctionParseSteps currentStep = ObjectiveFunctionParseSteps.CHECK_VALID_CHARACTERS;
		Boolean done = false;
		Boolean result = false;

		while(!done) {

			switch(currentStep) {
			case CHECK_LEFT_SIDE_VALID_CHARACTERS:
				result = this.checkLeftSideValidCharacters();
				currentStep = (result) ? (ObjectiveFunctionParseSteps.CHECK_RIGHT_SIDE_VALID_CHARACTERS) : (ObjectiveFunctionParseSteps.FINISH_STEPS);
				break;
			case CHECK_RIGHT_SIDE_VALID_CHARACTERS:
				result = this.checkRightSideValidCharacters();
				currentStep = (result) ? (ObjectiveFunctionParseSteps.PARSE_LEFT_SIDE) : (ObjectiveFunctionParseSteps.FINISH_STEPS);
				break;
			case CHECK_VALID_CHARACTERS:
				result = this.checkValidCharacters();
				currentStep = (result) ? (ObjectiveFunctionParseSteps.SPLIT_AROUND_EQUALITY_SYMBOL) : (ObjectiveFunctionParseSteps.FINISH_STEPS);
				break;
			case PARSE_LEFT_SIDE:
				result = this.checkPatternAndParseLeftSide();
				currentStep = (result) ? (ObjectiveFunctionParseSteps.PARSE_RIGHT_SIDE) : (ObjectiveFunctionParseSteps.FINISH_STEPS);
				break;
			case PARSE_RIGHT_SIDE:
				result = this.checkPatternAndParseRightSide();
				currentStep = ObjectiveFunctionParseSteps.FINISH_STEPS;
				break;
			case SPLIT_AROUND_EQUALITY_SYMBOL:
				result = this.splitAroundEqualitySymbol();
				currentStep = (result) ? (ObjectiveFunctionParseSteps.CHECK_LEFT_SIDE_VALID_CHARACTERS) : (ObjectiveFunctionParseSteps.FINISH_STEPS);
				break;
			case FINISH_STEPS:
				done = true;
				break;
			}

		}
		this.splittedStringsAroundEqualitySign = null;

		SimLog.writeln("Done parsing " + objFuncStr + "... " + ((result) ? ("pass") : ("fail")) + ".");

		return result;
	}



	private Boolean checkValidCharacters() {
		Boolean result = false;
		SimLog.write("Check if the objective function contains only the valid characters... ");
		Matcher matcher = Pattern.compile("[^x+-><=\\d/zmaxin]").matcher(this.objectiveFunctionStr);
		if(!matcher.find()) {
			result = true;
			SimLog.writeln("pass.");
		}
		else {
			SimLog.writeln("fail.");
		}
		return result;
	}

	private Boolean splitAroundEqualitySymbol() {
		Boolean result = false;
		SimLog.write("Splitting the objective function around the equality symbol... ");
		this.splittedStringsAroundEqualitySign = this.objectiveFunctionStr.split("=");
		if(this.splittedStringsAroundEqualitySign.length == 2) {
			result = true;
			SimLog.writeln(" pass.");
			SimLog.write("Splitted parts are : ");
			for(String s : this.splittedStringsAroundEqualitySign) {
				SimLog.write(s + " ");
			}
			SimLog.writeln(" .");
		}
		else {
			SimLog.writeln("fail.");
		}
		return result;
	}

	private Boolean checkLeftSideValidCharacters() {
		Boolean result = false;
		SimLog.write("Check characters validity of left side... ");
		Matcher matcher = Pattern.compile("[^maxinz]").matcher(this.splittedStringsAroundEqualitySign[0]);
		if(!matcher.find()) {
			result = true;
			SimLog.writeln("pass.");
		}
		else {
			SimLog.writeln("fail.");
		}
		return result;
	}

	private Boolean checkRightSideValidCharacters() {
		Boolean result = false;
		SimLog.write("Check characters validity of right side... ");
		Matcher matcher = Pattern.compile("[^x+-><=\\d/]").matcher(this.splittedStringsAroundEqualitySign[1]);
		if(!matcher.find()) {
			result = true;
			SimLog.writeln("pass.");
		}
		else {
			SimLog.writeln("fail.");
		}
		return result;
	}

	private Boolean checkPatternAndParseLeftSide() {
		Boolean result = false;
		SimLog.write("Check pattern of the objective function left side... ");
		Matcher matcher = Pattern.compile("(max|min)z").matcher(this.splittedStringsAroundEqualitySign[0]);
		if(matcher.find()) {
			result = true;
			SimLog.writeln("pass.");
			SimLog.write("Parsing the objective function left side... ");
			if(this.splittedStringsAroundEqualitySign[0].contains("max")) {
				this.minMaxStr = "max";
			}
			else if(this.splittedStringsAroundEqualitySign[0].contains("min")) {
				this.minMaxStr = "min";
			}
			SimLog.writeln("done.");
		}
		else {
			SimLog.writeln("fail.");
		}
		return result;
	}

	private Boolean checkPatternAndParseRightSide() {
		Boolean result = false;
		SimLog.write("Check pattern of the objective function right side... ");
		Matcher matcher = Pattern.compile("([+-]?\\d*?/?\\d*?x\\d+)*").matcher(this.splittedStringsAroundEqualitySign[1]);
		if(matcher.find()) {
			result = true;
			SimLog.writeln("pass.");

			SimLog.writeln("Parsing each one of the right side components... ");
			matcher = Pattern.compile("[+-]?\\d*?/?\\d*?x\\d+").matcher(this.splittedStringsAroundEqualitySign[1]);
			this.rightSideCoefficients = new ArrayList<Rational>();
			this.rightSideCoefficients.add(new Rational(0));
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

				Integer spare = compIndex - this.rightSideCoefficients.size() + 1;
				for(Integer i = 0; i < spare; i++) {
					this.rightSideCoefficients.add(new Rational(0));
				}
				this.rightSideCoefficients.set(this.rightSideCoefficients.size() - 1, coeff);

				SimLog.writeln("done.");
			}
			SimLog.writeln("Parsing right side components pass.");
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
			sb.append("Z = ");
			Integer counter = 1;
			for(Rational d : this.rightSideCoefficients) {
				if(d.equals(Rational.infinity())) {
					sb.append(" +M" + Simplex.getAliases().get(counter));
				}
				else if(d.equals(Rational.infinity().negative())) {
					sb.append(" -M" + Simplex.getAliases().get(counter));
				}
				else {
					if(!d.equals(new Rational(0))) {
						sb.append(" " + d + Simplex.getAliases().get(counter));
					}
				}
				counter++;
			}
			return sb.toString();
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Z = ");
			Integer counter = 1;
			for(Rational d : this.rightSideCoefficients) {
				if(d.equals(Rational.infinity())) {
					sb.append(" +MX" + counter);
				}
				else if(d.equals(Rational.infinity().negative())) {
					sb.append(" -MX" + counter);
				}
				else {
					if(!d.equals(new Rational(0))) {
						sb.append(" " + d + "X" + counter);
					}
				}
				counter++;
			}
			return sb.toString();
		}
	}
}
