package varelim;

import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Class for Variable Elimination Algorithm.
 */
public class VariableElimination {
    private final Variable query;
    private final ArrayList<Variable> variables;
    private final ArrayList<Table> probabilities;
    private final ArrayList<Variable> observed;
    private final String heuristic;
    /**
     * Constructor of Variable Elimination object.
     * @param query The query variable
     * @param observed Observed variables list
     * @param variables List of all variables
     * @param probabilities List of all tables
     * @param heuristic Heuristic name for ordering variables list
     */
    public VariableElimination(Variable query, ArrayList<Variable> variables, ArrayList<Table> probabilities, ArrayList<Variable> observed, String heuristic) {
        this.query = query;
        this.variables = variables;
        this.probabilities = probabilities;
        this.observed = observed;
        this.heuristic = heuristic;
    }

    /**
     * Algorithm to apply Variable Elimination to the network.
     */
    public void variableElimination() {
        // Setting up Logger
        Logger logger = Logger.getLogger("LogVE");
        try {
            FileHandler fileHandler = new FileHandler("LogVE.log", false); // To clear log file in each run
            fileHandler.close();
            fileHandler = new FileHandler("LogVE.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.info("Starting Variable Elimination...\n");

        } catch (Exception e) {
            logger.info("Error occurred.");
            e.printStackTrace();
        }
        // Reducing the variables/tables and setting up heuristics
        ArrayList<Factor> factorsForHeuristic = new ArrayList<>(); // Only for contained in the fewest factors first heuristic
        for (Table table : probabilities) {
            factorsForHeuristic.add(new Factor(table));
        }
        variables.remove(query);
        ArrayList<Variable> reducedVariables = necessaryVariables(variables, observed); // Reducing variables list to needed ones only
        Heuristic h = new Heuristic(heuristic, reducedVariables, query, factorsForHeuristic);
        h.applyHeuristic();
        ArrayList<Variable> eliminationOrder = h.getVariables(); // Elimination order by heuristic
        Factor finalFactor = null; // Factor that will be the result of VE
        ArrayList<Table> reducedProbabilities = necessaryTables(eliminationOrder, probabilities); // Reducing tables list to needed ones only
        ArrayList<Factor> factors = new ArrayList<>();
        logger.info("\nQuery variable: " + query);
        logger.info("Observed variables: " + observed);
        logger.info("Elimination order: " + eliminationOrder);
        // Algorithm starts
        // Identify factors and reduce observed variables
        logger.info("Identifying factors and reducing observed variables.\n");
        for (Table table : reducedProbabilities) {
            Factor tempFactor = new Factor(table);
            for (Variable obs : observed) {
                if (tempFactor.getVariables().contains(obs)) {
                    logger.info("Factor:\n" + tempFactor.getTable().toString() + "\nwith observed variable " + obs.getName() + " reduced to:\n");
                    tempFactor = Factor.reduction(tempFactor, obs);
                    logger.info("\n" + tempFactor.getTable().toString() + "\n");
                }
            }
            factors.add(tempFactor);
            logger.info("Factor added:\n" + tempFactor.getTable().toString() + "\n");
        }
        // For every variable Z in this ordering
        long startTime = System.nanoTime(); // Time starts here because ordering of variables to eliminate has role in the following part of the algorithm
        for (Variable Z : eliminationOrder) {
            logger.info("Eliminating variable " + Z.getName() + ".\n");
            ArrayList<Factor> factorsContainZ = new ArrayList<>();
            for (Factor f : factors) {
                if (f.getVariables().contains(Z)) {
                    factorsContainZ.add(f);
                    logger.info("Factor below contains variable " + Z.getName() + ":\n" + f.getTable().toString() + "\n");
                }
            }
            Factor factorZ;
            // Multiply factors containing Z
            if (factorsContainZ.size() > 1) {
                // Remove th factors that will be multiplied/marginalized from the list
                for (Factor f : factorsContainZ) {
                    factors.remove(f);
                }
                factorZ = Factor.productAll(factorsContainZ, Z);
                logger.info("Factor after multiplying factors containing " + Z.getName() + ":\n" + factorZ.getTable().toString() + "\n");
                // Sum out Z to obtain new factorZ
                factorZ = Factor.marginalization(factorZ, Z);
            }
            else {
                Factor f = factorsContainZ.get(0);
                factors.remove(f);
                factorZ = Factor.marginalization(f, Z);
            }
            logger.info("Factor after marginalizing using " + Z.getName() + ":\n" + factorZ.getTable().toString() + "\n");
            // Add factor Z to the list with update checking
            /*
             * Note that we already removed factors used in marginalization. This is for updates.
             * For example, Factor Z might be a new factor of Burglary after marginalizing other factor(s).
             * So we need to remove, in this case old version of Burglary.
             * We will add Factor Z no matter what after update checking.
             */
            for (Factor f : factors) {
                if (f.getVariables() == factorZ.getVariables()) {
                    factors.remove(f); // Removing old version
                }
            }
            factors.add(factorZ);
            logger.info("Updated factor list:\n");
            for (Factor f : factors) {
                logger.info(f.getTable().toString() + "\n");
            }
        }
        for (Factor f : factors) {
            if (f.getVariables().contains(query)) {
                finalFactor = f;
            }
        }
        assert finalFactor != null;
        finalFactor = Factor.normalize(finalFactor);
        logger.info("Normalized result:\n" + finalFactor.getTable().toString());
        long endTime = System.nanoTime();
        logger.info("Variable Elimination took " + (endTime-startTime) + " nanoseconds.");
    }

    /*
     * ======================================
     * Helper Functions
     * ======================================
     */

    /**
     * Get necessary variables.
     * @param variables Variables list that will be cut to only necessary ones
     * @param observed List of observed variables
     * @return result List of variables that are necessary for variable elimination
     */
    private ArrayList<Variable> necessaryVariables(ArrayList<Variable> variables, ArrayList<Variable> observed) {
        ArrayList<Variable> relevant = new ArrayList<>();
        ArrayList<Variable> result = new ArrayList<>();
        ArrayList<Variable> queue = new ArrayList<>();
        queue.add(query);
        queue.addAll(observed);
        while (!queue.isEmpty()) {
            Variable tempVar = queue.remove(0);
            relevant.add(tempVar);
            if (tempVar.hasParents()) {
                ArrayList<Variable> parents = tempVar.getParents();
                for (Variable var : parents) {
                    if (!relevant.contains(var)) {
                        queue.add(var);
                    }
                }
            }
        }
        for (Variable var : variables) {
            if (relevant.contains(var)) {
                result.add(var);
            }
        }
        return result;
    }

    /**
     * Get necessary tables.
     * @param variables Necessary variables list to check which tables should be included
     * @param probabilities List of all tables
     * @return result List of tables that belong to query or variables from observed variables
     */
    private ArrayList<Table> necessaryTables(ArrayList<Variable> variables, ArrayList<Table> probabilities) {
        ArrayList<Table> result = new ArrayList<>();
        for (Table table : probabilities) {
            if (table.getVariable() == query || variables.contains(table.getVariable())) {
                if (!result.contains(table)) {
                    result.add(table);
                }
            }
        }
        return result;
    }
}
