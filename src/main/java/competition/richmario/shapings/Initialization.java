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
public abstract class Initialization extends Shaping{

    public Initialization(double scaling, double gamma){
        super(scaling, gamma);
    }
    
    @Override
    public double shape(StateAction sa1, StateAction sa2, double reward){
        return reward;
    }
    
}
