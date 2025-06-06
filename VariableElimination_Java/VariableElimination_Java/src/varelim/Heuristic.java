package varelim;

import java.util.ArrayList;
import java.util.Comparator;

import static java.util.Collections.shuffle;

public class Heuristic {
    private final String heuristic;
    private ArrayList<Variable> variables;
    private final Variable query;
    private final ArrayList<Factor> factors;

    public Heuristic(String heuristic, ArrayList<Variable> variables, Variable query, ArrayList<Factor> factors) {
        this.heuristic = heuristic;
        this.variables = variables;
        this.query = query;
        this.factors = factors;
    }

    public void applyHeuristic() {
        ArrayList<Variable> orderedVars;
        if (heuristic.equals("least-incoming")) {
            orderedVars = new ArrayList<>(variables);
            orderedVars.sort(Comparator.comparingInt(Variable::getNrOfParents));
        }
        else if (heuristic.equals("fewest-factors")) {
            orderedVars = new ArrayList<>(variables);
            orderedVars.sort(Comparator.comparingInt(v -> {
                int factorCount = 0;
                for (Factor factor : factors) {
                    if (factor.getVariables().contains(v)) {
                        factorCount++;
                    }
                }
                return factorCount;
            }));
        }
        else {
            orderedVars = new ArrayList<>(variables);
            shuffle(orderedVars);
        }
        orderedVars.remove(query);
        variables = orderedVars;
    }

    public ArrayList<Variable> getVariables() {
        return variables;
    }
}
