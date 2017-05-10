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
public class ConstantInitialization extends Initialization{

    protected double constant;
    
    public ConstantInitialization(double scaling, double gamma, double constant){
        super(scaling, gamma);
        this.constant = constant;
    }
    
    @Override
    protected double actualPotential(StateAction sa) {
        return constant;
    }
    
}
