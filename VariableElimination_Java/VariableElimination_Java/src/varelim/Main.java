package varelim;

import java.util.ArrayList;

/**
 * Main class to read in a network, add queries and observed variables, and run variable elimination.
 * 
 * @author Marcel de Korte, Moira Berens, Djamari Oetringer, Abdullahi Ali, Leonieke van den Bulk
 */

public class Main {
	private final static String networkName = "earthquake.bif"; // The network to be read in (format and other networks can be found on http://www.bnlearn.com/bnrepository/)

	public static void main(String[] args) {
		// Read in the network
		Networkreader reader = new Networkreader(networkName);

		// Get the variables and probabilities of the network
		ArrayList<Variable> vs = reader.getVs();
		ArrayList<Table> ps = reader.getPs();

		// Make user interface
		UserInterface ui = new UserInterface(vs, ps);

		// Print variables and probabilities
		ui.printNetwork();

		// Ask user for query
		ui.askForQuery();
		Variable query = ui.getQueriedVariable();

		// Ask user for observed variables
		ui.askForObservedVariables();
		ArrayList<Variable> observed = ui.getObservedVariables();

		// Turn this on if you want to experiment with different heuristics for bonus points (you need to implement the heuristics yourself)
		ui.askForHeuristic();
		String heuristic = ui.getHeuristic();

		// Print the query and observed variables
		ui.printQueryAndObserved(query, observed);

		//PUT YOUR CALL TO THE VARIABLE ELIMINATION ALGORITHM HERE
		VariableElimination ve = new VariableElimination(query, vs, ps, observed, heuristic);
		ve.variableElimination();
	}
}