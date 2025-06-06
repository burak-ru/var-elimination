package varelim;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class that handles the communication with the user.
 * 
 * @author Marcel de Korte, Moira Berens, Djamari Oetringer, Abdullahi Ali, Leonieke van den Bulk
 */
	
public class UserInterface {
	
	private ArrayList<Variable> vs;
	ArrayList<Table> ps;
	private Variable query = null;
	private ArrayList<Variable> obs = new ArrayList<Variable>();
	private String line;
	private String heuristic;
	private Scanner scan;

	/**
	 * Constructor of the user interface.
	 * @param vs, the list of variables.
	 * @param ps, the list of probability tables.
	 */
	public UserInterface(ArrayList<Variable> vs, ArrayList<Table> ps) {
		this.vs = vs;
		this.ps = ps;
	}
	
	/**
	 * Asks for a query from the user.
	 */
	public void askForQuery() {
		System.out.println("\nWhich variable(s) do you want to query? Please enter in the number of the variable.");
		for (int i = 0; i < vs.size(); i++) {
			System.out.println("Variable " + i + ": " + vs.get(i).getName());
		}
		scan = new Scanner(System.in);
		line = scan.nextLine();
		if (line.isEmpty()) {
			System.out.println("You have not chosen a query value. Please choose a query value.");
			askForQuery();
		}
		try {
			int queriedVar = Integer.parseInt(line);
			if (queriedVar >= 0 && queriedVar < vs.size()) {
				query = vs.get(queriedVar);
			} else {
				System.out.println("This is not a correct index. Please choose an index between " + 0 + " and "
						+ (vs.size() - 1) + ".");
				askForQuery();
			}
		} catch (NumberFormatException ex) {
			System.out.println("This is not a correct index. Please choose an index between " + 0 + " and "
					+ (vs.size() - 1) + ".");
			askForQuery();
		}
	}

	/**
	 * Ask the user for observed variables in the network.
	 */
	public void askForObservedVariables() {

		obs.clear();
		System.out.println("Which variable(s) do you want to observe? Please enter in the number of the variable, \n"
				+ "followed by a comma and the value of the observed variable. Do not use spaces. \n"
				+ "If you want to query multiple variables, delimit them with a ';' and no spaces.\n"
				+ "Example: '2,True;3,False'");
		for (int i = 0; i < vs.size(); i++) {
			String values = "";
			for (int j = 0; j < vs.get(i).getNrOfValues() - 1; j++) {
				values = values + vs.get(i).getValues().get(j) + ", ";
			}
			values = values + vs.get(i).getValues().get(vs.get(i).getNrOfValues() - 1);

			System.out.println("Variable " + i + ": " + vs.get(i).getName() + " - " + values);
		}
		scan = new Scanner(System.in);
		line = scan.nextLine();
		if (line.isEmpty()) {
		} else {
			if (!line.contains(",")) {
				System.out.println("You did not enter a comma between values. Please try again");
				askForObservedVariables();
				return;
			} else {
				while (line.contains(";")) { // Multiple observed variables
					try {
						int queriedVar = Integer.parseInt(line.substring(0, line.indexOf(",")));
						String bool = line.substring(line.indexOf(",") + 1, line.indexOf(";"));
						changeVariableToObserved(queriedVar, bool);
						line = line.substring(line.indexOf(";") + 1); // Continue
																		// with
																		// next
																		// observed
																		// variable.
					} catch (NumberFormatException ex) {
						System.out.println("This is not a correct input. Please choose an index between " + 0 + " and "
								+ (vs.size() - 1) + ".");
						askForObservedVariables();
						return;
					}
				}
				if (!line.contains(";")) { // Only one observed variable
					try {

						int queriedVar = Integer.parseInt(line.substring(0, line.indexOf(",")));
						String bool = line.substring(line.indexOf(",") + 1);
						changeVariableToObserved(queriedVar, bool);
					} catch (NumberFormatException ex) {
						System.out.println("This is not a correct input. Please choose an index between " + 0 + " and "
								+ (vs.size() - 1) + ".");
						askForObservedVariables();
					}
				}
			}
		}
	}

	/**
	 * Checks whether a number and value represent a valid observed value or not and if so, adds it to the
	 * observed list. If not, asks again for new input.
	 */
	public void changeVariableToObserved(int queriedVar, String value) {
		Variable ob;
		if (queriedVar >= 0 && queriedVar < vs.size()) {
			ob = vs.get(queriedVar);
			if (ob.isValueOf(value)) {
				ob.setObservedValue(value);
				ob.setObserved(true);
			}

			else {
				System.out.println("Apparently you did not fill in the value correctly. You typed: \"" + value
						+ "\"Please try again");
				askForObservedVariables();
				return;
			}
			obs.add(ob); // Adding observed variable and it's value to list.
		} else {
			System.out.println("You have chosen an incorrect index. Please choose an index between " + 0 + " and "
					+ (vs.size() - 1));
			askForObservedVariables();
		}
	}

	/**
	 * Print the network that was read-in (by printing the variables, parents and probabilities).
	 * 
	 * @param The list of variables.
	 * @param The list of probabilities.
	 */
	public void printNetwork() {
		System.out.println("The variables:");
		for (int i = 0; i < vs.size(); i++) {
			String values = "";
			for (int j = 0; j < vs.get(i).getNrOfValues() - 1; j++) {
				values = values + vs.get(i).getValues().get(j) + ", ";
			}
			values = values + vs.get(i).getValues().get(vs.get(i).getNrOfValues() - 1);
			System.out.println((i + 1) + ") " + vs.get(i).getName() + " - " + values); // Printing
																						// the
																						// variables.
		}
		System.out.println("\nThe probabilities:");
		for (int i = 0; i < ps.size(); i++) {
			if (vs.get(i).getNrOfParents() == 1) {
				System.out.println(ps.get(i).getVariable().getName() + " has parent "
						+ vs.get(i).getParents().get(0).getName());
			} else if (vs.get(i).getNrOfParents() > 1) {
				String parentsList = "";
				for (int j = 0; j < vs.get(i).getParents().size(); j++) {
					parentsList = parentsList + vs.get(i).getParents().get(j).getName();
					if (!(j == vs.get(i).getParents().size()-1)) {
						parentsList = parentsList + " and ";
					}
				}
				System.out.println(ps.get(i).getVariable().getName() + " has parents "
						+ parentsList);
			} else {
				System.out.println(ps.get(i).getVariable().getName() + " has no parents.");
			} 

			Table probs = ps.get(i);
			for (int l = 0; l < probs.size(); l++) {
				System.out.println(probs.get(l)); 			// Printing
			}												// the
			System.out.println();							// probabilities.
		}
	}

	/**
	 * Prints the query and observed variables given in by the user.
	 * 
	 * @param The query variable(s).
	 * @param The observed variable(s).
	 */
	public void printQueryAndObserved(Variable query, ArrayList<Variable> Obs) {
		System.out.println("\nThe queried variable(s) is/are: "); // Printing
																	// the
																	// queried
																	// variables.
		System.out.println(query.getName());
		if (!Obs.isEmpty()) {
			System.out.println("The observed variable(s) is/are: "); // Printing
																		// the
																		// observed
																		// variables.
			for (int m = 0; m < Obs.size(); m++) {
				System.out.println(Obs.get(m).getName());
				System.out.println("This variable has the value: " + Obs.get(m).getObservedValue());
			}
		}
	}
	
	/**
	 * Asks for a heuristic.
	 */
	public void askForHeuristic() {
		System.out.println("Supply a heuristic. Input 1 for least-incoming, 2 for fewest-factors and enter for random");
		scan = new Scanner(System.in);
		line = scan.nextLine();
		if (line.isEmpty()) {
			heuristic = "empty";
			System.out.println("You have chosen for the random heuristic");
		} else if (line.equals("1")) {
			heuristic = "least-incoming";
			System.out.println("You have chosen for the least-incoming heuristic");
		} else if (line.equals("2")) {
			heuristic = "fewest-factors";
			System.out.println("You have chosen for the fewest-factors heuristic");
		} else {
			System.out.println(line + " is not an option. Please try again");
			askForHeuristic();
		}
		scan.close();
	}

	/**
	 * Getter of the observed variables.
	 * 
	 * @return a list of observed variables given by the user.
	 */
	public ArrayList<Variable> getObservedVariables() {
		return obs;
	}

	/**
	 * Getter of the queried variables.
	 * 
	 * @return the variable the user wants to query.
	 */
	public Variable getQueriedVariable() {
		return query;
	}
	
	/**
	 * Getter of the heuristic.
	 * 
	 * @return the name of the heuristic.
	 */
	public String getHeuristic() {
		return heuristic;
	}
}
