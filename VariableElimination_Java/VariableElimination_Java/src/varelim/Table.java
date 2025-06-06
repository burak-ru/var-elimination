package varelim;

import java.util.ArrayList;

/**
 * Class to represent a probability table consisting of probability rows.
 * 
 * @author Marcel de Korte, Moira Berens, Djamari Oetringer, Abdullahi Ali, Leonieke van den Bulk
 */

public class Table {

	private Variable variable;
	private ArrayList<ProbRow> table;
	
	/**
	 * Constructor of the class.
	 * @param variable, variable belonging to the current probability table.
	 * @param table, table made out of probability rows (ProbRows).
	 */
	public Table(Variable variable, ArrayList<ProbRow> table) {
		this.variable = variable;
		this.table = table;
	}
	
	/**
	 * Returns the size of the Table (amount of probability rows).
	 * @return amount of rows in the table as an int.
	 */
	public int size() {
		return table.size();
	}
	
	/**
	 * Transform table to string.
	 */
	public String toString() {
		String tableString = variable.getName() + " | ";
		for (int i = 0; i < variable.getNrOfParents(); i++) {
			tableString = tableString + variable.getParents().get(i).getName();
			if(!(i == variable.getNrOfParents()-1)) {
				tableString = tableString + ", ";
			}
		}
		for(ProbRow row: table) {
			tableString = tableString + "\n" + row.toString();
		}
		return tableString;
	}

	/**
	  * Gets the i'th element from the ArrayList of ProbRows.
	  * @param i index as an int.
	  * @return i'th ProbRow in Table.
	  */
	public ProbRow get(int i) {
		return table.get(i);
	}
	
	/**
	 * Getter of the table made out of ProbRows
	 * @return table as an ArrayList of ProbRows.
	 */
	public ArrayList<ProbRow> getTable() {
		return table;
	}
	
	 /**
	 * Getter of the variable that belongs to the probability table.
	 * @return the variable.
	 */
	public Variable getVariable() {
		return variable;
	}

	/**
	 * Getter of the parents that belong to the node of the probability table.
	 * @return the parents as an ArrayList of Variables.
	 */
	public ArrayList<Variable> getParents() {
		return variable.getParents();
	}
}
