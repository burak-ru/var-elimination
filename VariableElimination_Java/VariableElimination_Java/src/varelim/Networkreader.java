package varelim;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class that reads in a network from a .bif file and puts the variables and probabilities at the right places.
 * 
 * @author Marcel de Korte, Moira Berens, Djamari Oetringer, Abdullahi Ali, Leonieke van den Bulk
 */

public class Networkreader {

	private ArrayList<Variable> vs = new ArrayList<Variable>();
	private ArrayList<Table> ps = new ArrayList<Table>();
	private ArrayList<ProbRow> probRows;
	private String varName;
	private String probName;
	private ArrayList<Variable> parents = new ArrayList<Variable>();
	private int nrOfRows;

	/**
	 * Constructor reads in the data file and adds the variables and its
	 * probabilities to the designated arrayLists.
	 * 
	 * @param file, the name of the .bif file that contains the network.
	 */
	public Networkreader(String file) {
		BufferedReader br = null;
		try {
			String cur; // Keeping track of current line observed by BufferedReader
			br = new BufferedReader(new FileReader(file)); 
			try {
				while ((cur = br.readLine()) != null) { 
					if (cur.contains("variable")) { 
						//Add variable to the list
						varName = cur.substring(9, cur.length() - 2);
						cur = br.readLine();
						ArrayList<String> possibleValues = searchForValues(cur);
						vs.add(new Variable(varName, possibleValues));
					}
					if (cur.contains("{")) { 
						parents = new ArrayList<Variable>();
					}
					if (cur.contains("probability")) { 
						// Conditional to check for parents of selected variable
						searchForParents(cur);
					}
					if (cur.contains("table")) { 
						//Conditional to find probabilities of 1 row and add Probabilities to
						// probability list
						ArrayList<ProbRow> currentProbRows = searchForProbs(cur);
						for(ProbRow p : currentProbRows) {
							probRows.add(p);
						}
						Table table = new Table(getByName(probName), probRows);
						ps.add(table);
					}
					if (cur.contains(")") && cur.contains("(") && !cur.contains("prob")) {
						// Conditional to find probabilities of more than 1 row;
						// add probabilities to probability list
						ArrayList<ProbRow> currentProbRows = searchForProbs(cur);
						for(ProbRow p : currentProbRows) {
							probRows.add(p);
						}
						if (probRows.size() == nrOfRows) {
							Table table = new Table(getByName(probName), probRows);
							ps.add(table);
						}
					}
				}
			} catch (IOException e) {
			}
		} catch (FileNotFoundException e) {
			System.out.println("This file does not exist.");
			System.exit(0);
		}
	}

	/**
	 * Searches for a row of probabilities in a string
	 * 
	 * @param a string s
	 * @return a ProbRow
	 */
	public ArrayList<ProbRow> searchForProbs(String s) {
		ArrayList<ProbRow> currentProbRows = new ArrayList<ProbRow>();
		int beginIndex = s.indexOf(')') + 2;
		if (s.contains("table")) {
			beginIndex = s.indexOf('e') + 2;
		}

		int endIndex = s.length() - 1;
		String subString = s.substring(beginIndex, endIndex);
		String[] probsString = subString.split(", ");
		double[] probs = new double[probsString.length];
		for (int i = 0; i < probsString.length; i++) {
			probs[i] = Double.parseDouble(probsString[i]);
		}

		if (!s.contains("table")) {
			ArrayList<String> parentsValues = new ArrayList<String>();
			ArrayList<String> nodeValues = new ArrayList<String>();
			beginIndex = s.indexOf('(') + 1;
			endIndex = s.indexOf(')');
			subString = s.substring(beginIndex, endIndex);
			String[] stringValues = subString.split(", ");
			for(String value : stringValues) {
				parentsValues.add(value);
			}
			for(Variable v : vs) {
				if(probName.equals(v.getName())) {
					nodeValues = v.getValues();
				}
			}
			for(int i=0; i<probs.length; i++) {
				parentsValues.add(0,(String) nodeValues.get(i));
				ArrayList<String> currentVal = new ArrayList<String>(parentsValues);
				currentProbRows.add(new ProbRow(currentVal, probs[i] ));
				parentsValues.remove(0);
			}
		}
		else {
			ArrayList<String> values = new ArrayList<String>();
			ArrayList<String> nodeValues = new ArrayList<String>();
			for(Variable v : vs) {
				if(probName.equals(v.getName())) {
					values = v.getValues();
				}
			}
			for(int i=0; i<probs.length; i++) {
				nodeValues.add(values.get(i));
				ArrayList<String> currentVal = new ArrayList<String>(nodeValues);
				ProbRow prob = new ProbRow(currentVal, probs[i]);
				currentProbRows.add(prob);
				nodeValues.clear();
			}
		}
		return currentProbRows;
	}

	/**
	 * Searches for values in a string
	 * 
	 * @param a string s
	 * @return a list of values
	 */
	public ArrayList<String> searchForValues(String s) {
		int beginIndex = s.indexOf('{') + 2;
		int endIndex = s.length() - 3;
		String subString = s.substring(beginIndex, endIndex);
		String[] valueArray = subString.split(", ");
		return new ArrayList<String>(Arrays.asList(valueArray));
	}

	/**
	 * Method to check parents of chosen variable.
	 * 
	 * @param cur, which gives the current line.
	 */
	public void searchForParents(String cur) {
		if (cur.contains("|")) { // Variable has parents
			extractParents(cur);
		} else { // Variable has no parents
			probName = cur.substring(14, cur.length() - 4);
			for(Variable v : vs) {
				if(probName.equals(v.getName())) {
					nrOfRows = v.getNrOfValues();
				}
			}
		}
		probRows = new ArrayList<ProbRow>();
	}

	/**
	 * Gets a variable from variable Vs when the name is given
	 * 
	 * @param name
	 * @return variable with name as name
	 */
	private Variable getByName(String name) {
		Variable var = null;
		for (int i = 0; i < vs.size(); i++) {
			if (vs.get(i).getName().equals(name))
				var = vs.get(i);
		}
		return var;
	}

	/**
	 * Extracts parents and puts them in a list of parents of that node.
	 * 
	 * @param cur, a string to extract from
	 */
	public void extractParents(String cur) {
		probName = cur.substring(14, cur.indexOf("|") - 1);
		Variable var = getByName(probName);
		String sub = cur.substring(cur.indexOf('|') + 2, cur.indexOf(')') - 1);
		while (sub.contains(",")) { // Variable has more parents
			String current = sub.substring(0, sub.indexOf(','));
			sub = sub.substring(sub.indexOf(',') + 2);
			for (int i = 0; i < vs.size(); i++) {
				if (vs.get(i).getName().equals(current)) {
					parents.add(vs.get(i)); // Add parent to list
				}
			}
		}
		if (!sub.contains(",")) { // Variable has no more parents
			for (int i = 0; i < vs.size(); i++) {
				if (vs.get(i).getName().equals(sub)) {
					parents.add(vs.get(i)); //
				}
			}
		}

		var.setParents(parents);
		nrOfRows = computeNrOfRows(probName);
	}

	/**
	 * Computes the number of rows needed given the current parents
	 * 
	 * @return the number of rows
	 */
	private int computeNrOfRows(String probName) {
		int fac = 1;
		for (int i = 0; i < parents.size(); i++) {
			fac = fac * parents.get(i).getNrOfValues();
		}
		for(Variable v : vs) {
			if(probName.equals(v.getName())) {
				fac = fac * v.getNrOfValues();
			}
		}
		return fac;
	}

	/**
	 * Getter of the variables in the network.
	 * 
	 * @return the list of variables in the network.
	 */
	public ArrayList<Variable> getVs() {
		return vs;
	}

	/**
	 * Getter of the probabilities in the network.
	 * 
	 * @return the list of probabilities in the network.
	 */
	public ArrayList<Table> getPs() {
		return ps;
	}

}