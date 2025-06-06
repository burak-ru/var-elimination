package varelim;

import java.util.ArrayList;

/**
 * Class to represent a row of a probability table by its values and a probability.
 * 
 * @author Marcel de Korte, Moira Berens, Djamari Oetringer, Abdullahi Ali, Leonieke van den Bulk
 */
public class
ProbRow {
	private ArrayList<String> values;
	private double prob;

	/**
	 * Constructor of the class.
	 * @param values, values of the variables (main variable+parents) in the row, of which the value of the main variable itself is always first.
	 * @param prob, probability belonging to this row of values.
	 */
	public ProbRow(ArrayList<String> values, double prob) {
		this.prob = prob;
		this.values = values;
	}

	/**
	 * Transform probabilities to string.
	 */
	public String toString() {
		String valuesString = "";
		for(int i = 0; i < values.size()-1; i++){
			valuesString = valuesString + values.get(i) + ", ";
		}
		valuesString = valuesString + values.get(values.size()-1);
		return valuesString + " | " + Double.toString(prob);
	}
	
	/**
	 * Getter of the values of this probability row.
	 * @return ArrayList<String> of values
	 */
	public ArrayList<String> getValues() {
		return values;
	}
	
	/**
	 * Getter of the probability of this probability row
	 * @return the probability as a double.
	 */
	public double getProb() {
		return prob;
	}
}