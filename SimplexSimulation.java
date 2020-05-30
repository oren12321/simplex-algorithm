import java.util.ArrayList;
import java.util.Scanner;



public class SimplexSimulation {
	
	/*
	 * Simplex generator instance.
	 */
	private static Simplex simplex = null;
	
	/*
	 * User input stream.
	 */
	private static Scanner scanner = new Scanner(System.in);
	
	/*
	 * Main function that responsible for the simplex simulation.
	 */
	public static void simulate() {
		
		System.out.print("Choose output method ( 1 - Console, 2 - File ) : ");
		
		Boolean outputMethodGood = false;
		Integer outputMethodCode = 1;
		while(!outputMethodGood) {
			try {
				outputMethodCode = Integer.parseInt(SimplexSimulation.scanner.nextLine());
				outputMethodGood = true;
			}
			catch(IllegalArgumentException e) {
				System.out.print("Output method is illegal, enter it again : ");
			}
		}
		
		if(outputMethodCode.equals(2)) {
			SimLog.setStreamType(SimLog.LogStreamType.FILE_STREAM);
		}
		else {
			SimLog.setStreamType(SimLog.LogStreamType.SYS_OUT_STREAM);
		}
		
		SimLog.writeln("Starting linear programming problem data reception :");
		
		SimplexSimulation.constraintsInput();
		
		SimLog.writeln("Problem reception ended.");
		
		SimLog.writeln("The problem is:\n" + SimplexSimulation.simplex);
		
		SimLog.writeln("Starting simplex solution :");
		
		SimplexSimulation.solveSimplex();
		
		SimLog.writeln("Simplex solved !");
		
		SimLog.close();
		
		System.out.println("Done!");
	}
	
	/*
	 * Perform simplex method on given problem.
	 */
	private static void solveSimplex() {
		SimplexSimulation.simplex.solve();
	}
	
	/*
	 * Get from the user its linear programming problem (objective function + constrains).
	 */
	private static void constraintsInput() {
		
		System.out.println("At any time enter exit to stop program.");
		System.out.println("Enter your objective function :");
		System.out.println("( pattern : (min|max)z=(num1)x1+(num2)x2+...+(numN)xN )");
		
		Boolean done = false;
		Boolean simplexCanBeBuilt = false;
		
		ObjectiveFunction objFunc = null;
		
		while(!done) {
			String input = SimplexSimulation.scanner.nextLine();
			if(input.equals("exit")) {
				SimLog.writeln("Process stoped!");
				done = true;
				SimLog.close();
				System.exit(0);
			}
			else {
				objFunc = new ObjectiveFunction(input);
				if(!objFunc.getValid()) {
					System.out.println("Invalid objective function, try again :");
				}
				else {
					done = true;
				}
			}
		}
		
		ArrayList<Constraint> constraints = new ArrayList<Constraint>();
		
		done = false;
		
		System.out.println("Enter your constraints , and press ENTER to finish :");
		System.out.println("( pattern : (num1)x1+(num2)x2+...+(numN)xN(>|>=|<|<=|=)(num) )");
		
		Integer constraintsCounter = 0;
		
		while(!done) {
			String input = SimplexSimulation.scanner.nextLine();
			if(input.equals("exit")) {
				SimLog.writeln("Process stoped!");
				done = true;
				SimLog.close();
				System.exit(0);
			}
			else if(input.equals("") && constraintsCounter > 0) {
				done = true;
				simplexCanBeBuilt = true;
			}
			else {
				Constraint constraint = new Constraint(input);
				if(!constraint.getValid()) {
					System.out.println("Invalid constraint, try again :");
				}
				else {
					constraintsCounter++;
					constraints.add(constraint);
				}
			}
		}
		
		if(simplexCanBeBuilt) {
			SimplexSimulation.simplex = new Simplex(constraints, objFunc);
		}
		
	}
	
}
