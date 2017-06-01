package competition.richmario.tableImplementations;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Dev on 04/05/2017.
 */
public class AvlTreeBasedQTable implements IQTable {

    private AvlTree m_tree;
    private int m_numberOfActions;
    private HashMap<Pair<double[], Integer>, Double> m_traces;
    private final double MINIMAL_TRACE_VALUE = 0.001;

    public AvlTreeBasedQTable(int numberOfActions) {
        this.m_tree = new AvlTree();
        this.m_numberOfActions = numberOfActions;
        this.m_traces = new HashMap<>();
    }

    public double getKeyValue(double[] state, int action) {
        return this.getStateActionValue(state, action);
    }

    public void setKeyValue(double[] state, int action, double value) {
        this.setStateActionValue(state, action, value);
    }

    private AvlNode getStateNode(double[] state) {
        AvlTree currentTree = this.m_tree;
        AvlNode currentNode = null;

        for (int i = 0 ; i < state.length ; i++) {
            currentNode = this.getNodeAtValue(currentTree, state[i]);

            if (null == currentNode.nextTree && i < state.length - 1) {
                currentNode.nextTree = new AvlTree();
            }

            currentTree = currentNode.nextTree;
        }

        return currentNode;
    }

    public double getStateActionValue(double[] state, int action) {
        return this.getActionValue(this.getStateNode(state), action);
    }

    public void setStateActionValue(double[] state, int action, double value) {
        this.setActionValue(this.getStateNode(state), action, value);
    }

    private AvlNode getNodeAtValue(AvlTree tree, double value) {
        AvlNode valueNode = tree.findNode(value);

        if (null == valueNode) {
            tree.insert(value);
            valueNode = tree.getLastInsertedNode();
        }

        return valueNode;
    }

    private void verifyActionArrayInitialized(AvlNode node) {
        if (node.actionValues == null) {
            node.actionValues = new double[m_numberOfActions];
            Arrays.fill(node.actionValues, 0);
        }
    }

    private double getActionValue(AvlNode node, int action) {
        this.verifyActionArrayInitialized(node);
        return node.actionValues[action];
    }

    private void setActionValue(AvlNode node, int action, double value) {
        this.verifyActionArrayInitialized(node);
        node.actionValues[action] = value;
    }

    public void setKeyTrace(double[] state, int action, Double traceValue) {
        Pair<double[], Integer> stateActionKey = null;

        for (Pair<double[], Integer> traceRecord : this.m_traces.keySet()) {
            if (this.isSameState(traceRecord.getKey(), state) && traceRecord.getValue() == action) {
                stateActionKey = traceRecord;
                break;
            }
        }

        double oldValue = 0;
        if (stateActionKey != null) {
            oldValue = this.m_traces.get(stateActionKey);
        }
        else {
            stateActionKey = new Pair<>(state.clone(), action);
        }

        this.m_traces.put(stateActionKey, Math.max(traceValue, oldValue));
    }

    private boolean isSameState(double[] first, double[] second) {
        for (int i = 0 ; i < first.length ; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }
        return true;
    }

    public void decayTraces(double gamma_lambda) {

        ArrayList<Pair<double[], Integer>> keysToRemove = new ArrayList<>();
        for (Pair<double[], Integer> traceRecord : this.m_traces.keySet()) {
            double oldValue = this.m_traces.get(traceRecord);
            double newValue = oldValue * gamma_lambda;

            if (newValue > this.MINIMAL_TRACE_VALUE) {
                this.m_traces.put(traceRecord, newValue);
            }
            else {
                keysToRemove.add(traceRecord);
            }
        }

        for (Pair<double[], Integer> traceRecord : keysToRemove) {
            this.m_traces.remove(traceRecord);
        }
    }

    public void resetTraces() {
        this.m_traces.clear();
    }

    public void updateByTraces(double alpha, double delta) {
        for (Pair<double[], Integer> traceRecord : this.m_traces.keySet()) {
            double traceFactor = this.m_traces.get(traceRecord);
            double oldQValue = this.getKeyValue(traceRecord.getKey(), traceRecord.getValue());
            this.setKeyValue(
                    traceRecord.getKey(),
                    traceRecord.getValue(),
                    oldQValue + alpha * delta * traceFactor
            );
        }
    }
}
