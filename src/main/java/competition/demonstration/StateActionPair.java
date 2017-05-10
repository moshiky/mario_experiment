/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.demonstration;

/**
 *
 * @author timbrys
 */
public class StateActionPair {
    
    protected double [] state;
    protected int action;
    
    public StateActionPair(double[] state, int action){
        this.state = state.clone();
        this.action = action;
    }

    public double[] getState() {
        return state;
    }

    public int getAction() {
        return action;
    }
    
}
