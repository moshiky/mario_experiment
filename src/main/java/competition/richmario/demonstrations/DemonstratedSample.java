/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.demonstrations;

/**
 *
 * @author timbrys
 */
public class DemonstratedSample {
    
    protected double [] state;
    protected int action;
    
    public DemonstratedSample(double[] state, int action){
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
