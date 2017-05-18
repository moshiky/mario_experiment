package competition.richmario.tableImplementations;

/**
 * Created by Dev on 04/05/2017.
 */
public class AvlTreeBasedQTable implements IQTable {

    private AvlTree m_tree;

    public AvlTreeBasedQTable() {
        this.m_tree = new AvlTree();
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
            node.actionValues = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
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

    public void decay() {
        // traces not supported
    }

    public void resetEs() {
        // traces not supported
    }

    public void setTraces(double[] state, int action) {
        // traces not supported
    }

    public void update(double delta) {
        // update code
    }

    public int getSize() { return 0; }
}
