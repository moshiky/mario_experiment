/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.shapings;

import competition.richmario.StateAction;

/**
 *
 * @author timbrys
 */
public class CombiShaping extends Shaping {
    
    protected Shaping[] shapings;
    
//    static private double[] scalings = new double[]{0.0001, 0.001, 0.01, 0.1, 1.0, 10.0, 100.0, 1000.0};
    
    public CombiShaping(Shaping[] shapings, double gamma){
        super(1.0, gamma);
        this.shapings = shapings;
    }
    
    @Override
    protected double actualPotential(StateAction sa){
        double shape = 0.0;
        for(int i=0; i<shapings.length; i++){
            shape += shapings[i].potential(sa);
        }
        if(shapings.length == 0){
            return 0.0;
        } else {
            return shape/shapings.length;
        }
    }
}
