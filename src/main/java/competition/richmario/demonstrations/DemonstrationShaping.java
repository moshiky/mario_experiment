/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.demonstrations;

import competition.richmario.shapings.Shaping;
import competition.richmario.StateAction;

/**
 *
 * @author timbrys
 */
public class DemonstrationShaping extends Shaping{
    
    protected Demonstration demonstration;
    
    public DemonstrationShaping(double scaling, double gamma, Demonstration demonstration){
        super(scaling, gamma, true);
        this.demonstration = demonstration;
    }
    
    @Override
    protected double actualPotential(StateAction sa){
        return Math.pow(Math.E, -Math.pow(demonstration.getDistanceClosest(sa.getState(), sa.getAction()), 2.0)/(2*0.2*0.2));
    }
    
}
