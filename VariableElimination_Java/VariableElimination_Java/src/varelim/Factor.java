package varelim;

import java.util.ArrayList;

/**
 * Class that represents a factor.
 */
public class Factor {
    private final Table table;
    private final ArrayList<Variable> variables;
    /**
     * Constructor of the Factor.
     * @param table Table of factor
     */
    public Factor(Table table) {
        this.table = table;
        ArrayList<Variable> tempVars = new ArrayList<>();
        tempVars.add(table.getVariable());
        if (table.getVariable().hasParents()) {
            tempVars.addAll(table.getParents());
        }
        this.variables = tempVars;
    }

    /**
     * Constructor of the Factor.
     * @param variables Variables for factor
     * @param rows Rows that consist values for variables
     */
    public Factor(ArrayList<Variable> variables, ArrayList<ProbRow> rows) {
        this.variables = variables;
        Variable firstVar = variables.get(0);
        ArrayList<Variable> parents = new ArrayList<>();
        for (int i = 1; i < variables.size(); i++) {
            parents.add(variables.get(i));
        }
        firstVar.setParents(parents);
        this.table = new Table(firstVar, rows);
    }

    /*
     * =============================
     * Factor Calculation Functions
     * =============================
     */

    /**
     * Reduction method for a factor.
     * @param factor Factor that will be reduced
     * @param var Variable to reduce
     * @return resultFactor Factor resulted by reduction of input variable from input factor
     */
    public static Factor reduction(Factor factor, Variable var) {
        ArrayList<Variable> vars = factor.getVariables();
        ArrayList<ProbRow> table = factor.getTable().getTable();
        ArrayList<ProbRow> resultTable = new ArrayList<>();
        Factor resultFactor;
        int varIndex = vars.indexOf(var);
        for (ProbRow row : table) {
            if (row.getValues().get(varIndex).equals(var.getObservedValue())) {
                resultTable.add(row);
            }
        }
        resultFactor = new Factor(vars, resultTable);
        return resultFactor;
    }

    /**
     * Product method for two factors.
     * @param factorOne First factor to use for production
     * @param factorTwo Second factor to use for production
     * @return resultFactor Factor result of the production for factorOne and factorTwo
     */
    public static Factor product(Factor factorOne, Factor factorTwo, Variable var) {
        Factor resultFactor;
        ArrayList<Variable> tempVars = getTotalVariables(factorOne, factorTwo);
        ArrayList<ProbRow> resultTable;
        resultTable = productOneCommonVariable(factorOne, factorTwo, var);
        resultFactor = new Factor(tempVars, resultTable);
        return resultFactor;
    }

    /**
     * Marginalization method for a factor.
     * @param factor Factor to marginalize
     * @param var Variable to marginalize on
     * @return resultFactor
     */
    public static Factor marginalization(Factor factor, Variable var) {
        if (factor.getVariables().size() == 1 && factor.getVariables().get(0) == var) {
            return factor;
        }
        Factor resultFactor;
        ArrayList<ProbRow> resultTable = new ArrayList<>();
        ArrayList<Variable> vars = new ArrayList<>(factor.getVariables());
        ArrayList<ProbRow> tempTable = factor.getTable().getTable();
        int varIndex = factor.getVariables().indexOf(var);
        while (!tempTable.isEmpty()) {
            ProbRow tempRowOne = tempTable.remove(0);
            ArrayList<ProbRow> toMargRows = new ArrayList<>();
            for (ProbRow row : tempTable) {
                if (summable(tempRowOne, row, varIndex)) {
                    toMargRows.add(row);
                }
            }
            if (!toMargRows.isEmpty()) {
                double sumProb = tempRowOne.getProb();
                for (ProbRow row : toMargRows) {
                    sumProb += row.getProb();
                    tempTable.remove(row);
                }
                ArrayList<String> marginalizedValues = new ArrayList<>(tempRowOne.getValues());
                marginalizedValues.remove(varIndex);
                ProbRow marginalizedRow = new ProbRow(marginalizedValues, sumProb);
                resultTable.add(marginalizedRow);
            }
            else {
                ArrayList<String> tempValues = new ArrayList<>(tempRowOne.getValues());
                tempValues.remove(varIndex);
                ProbRow tempRow = new ProbRow(tempValues, tempRowOne.getProb());
                resultTable.add(tempRow);
            }
        }
        vars.remove(var);
        resultFactor = new Factor(vars, resultTable);
        return resultFactor;
    }

    /*
     * =========================
     * Helper Functions Product
     * =========================
     */

    /**
     * Get all variables from two factors.
     * @param factorOne First factor to get variables from
     * @param factorTwo Second factor to get variables from
     * @return totalVars Union of all variables
     */
    public static ArrayList<Variable> getTotalVariables(Factor factorOne, Factor factorTwo) {
        ArrayList<Variable> varListOne = factorOne.getVariables();
        ArrayList<Variable> varListTwo = factorTwo.getVariables();
        ArrayList<Variable> totalVars = new ArrayList<>(varListOne);
        for (Variable var : varListTwo) {
            if (!varListOne.contains(var)) {
                totalVars.add(var);
            }
        }
        return totalVars;
    }

    /**
     * Product method for factors over one common variable
     * @param factorOne First factor to use for production
     * @param factorTwo Second factor to use for production
     * @return resultTable Row list of two input factors
     */
    public static ArrayList<ProbRow> productOneCommonVariable(Factor factorOne, Factor factorTwo, Variable commonVar) {
        ArrayList<ProbRow> resultTable = new ArrayList<>();
        int commonIndexFactorOne = factorOne.getVariables().indexOf(commonVar);
        int commonIndexFactorTwo = factorTwo.getVariables().indexOf(commonVar);
        for (int i = 0; i < factorOne.getTable().size(); i++) {
            ProbRow rowOne = factorOne.getTable().get(i);
            for (int j = 0; j < factorTwo.getTable().size(); j++) {
                ProbRow rowTwo = factorTwo.getTable().get(j);
                if (rowOne.getValues().get(commonIndexFactorOne).equals(rowTwo.getValues().get(commonIndexFactorTwo))) {
                    ProbRow rowProduct;
                    ArrayList<String> resultValues = new ArrayList<>(rowOne.getValues());
                    for (int k = 0; k < rowTwo.getValues().size(); k++) {
                        if (k != commonIndexFactorTwo) {
                            resultValues.add(rowTwo.getValues().get(k));
                        }
                    }
                    rowProduct = new ProbRow(resultValues, (rowOne.getProb() * rowTwo.getProb()));
                    resultTable.add(rowProduct);
                }
            }
        }
        return resultTable;
    }

    /**
     * Apply product method for a factor list.
     * @param factors Factor list to use for production
     * @return resultFactor Factor result of the production from each factor in list
     */
    public static Factor productAll(ArrayList<Factor> factors, Variable var) {
        Factor resultFactor;
        while (factors.size() != 1) {
            Factor factorOne = factors.get(0);
            Factor factorTwo = factors.get(1);
            Factor tempFactor = product(factorOne, factorTwo, var);
            factors.remove(factorOne);
            factors.remove(factorTwo);
            factors.add(tempFactor);
        }
        resultFactor = factors.get(0);
        return resultFactor;
    }

    /*
     * ================================
     * Helper Functions Marginalization
     * ================================
     */

    /**
     * Boolean function for checking if two rows are summable according to rules of marginalization
     * @param rowOne First row to check
     * @param rowTwo Second row to check
     * @param index Index of variable to exclude when summing
     * @return true or false depending on if rows are summable
     */
    public static Boolean summable(ProbRow rowOne, ProbRow rowTwo, int index) {
        ArrayList<String> valuesOne = rowOne.getValues();
        ArrayList<String> valuesTwo = rowTwo.getValues();
        for (int i = 0; i < valuesOne.size(); i++) {
            if (!valuesOne.get(i).equals(valuesTwo.get(i)) && i != index) {
                return false;
            }
        }
        return true;
    }

    /*
     * ================================
     * Normalizing Function
     * ================================
     */

    /**
     * Normalize final factor
     * @param factor Factor to normalize
     * @return resultFactor Normalized factor
     */
    public static Factor normalize(Factor factor) {
        Factor resultFactor;
        ArrayList<ProbRow> finalTable = new ArrayList<>();
        double finalProb = 0.0;
        for (ProbRow row : factor.getTable().getTable()) {
            finalProb += row.getProb();
        }
        for (ProbRow row : factor.getTable().getTable()) {
            ProbRow tempRow = new ProbRow(row.getValues(), (row.getProb() / finalProb));
            finalTable.add(tempRow);
        }
        resultFactor = new Factor(factor.getVariables(), finalTable);
        return resultFactor;
    }

    /*
     * =================
     * Getter Functions
     * =================
     */

    /**
     * Getter for table that belongs to a table
     * @return table
     */
    public Table getTable() {
        return table;
    }

    /**
     * Getter for variables that belongs to a table
     * @return variables
     */
    public ArrayList<Variable> getVariables() {
        return variables;
    }
}
