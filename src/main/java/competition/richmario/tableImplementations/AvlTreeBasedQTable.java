package competition.richmario.tableImplementations;

import java.util.Arrays;

/**
 * Created by Dev on 04/05/2017.
 */
public class AvlTreeBasedQTable implements IQTable {

    private AvlTree m_tree;
    private int m_numberOfActions;

    public AvlTreeBasedQTable(int numberOfActions) {
        this.m_tree = new AvlTree();
        this.m_numberOfActions = numberOfActions;
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
}
