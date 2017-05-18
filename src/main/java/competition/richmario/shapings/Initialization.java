/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.shapings;

import competition.richmario.StateAction;
import competition.richmario.experiment.ShapingManager;

/**
 *
 * @author timbrys
 */
public abstract class Initialization extends Shaping{

    private ShapingManager m_shapingManager;

    public Initialization(double scaling, double gamma){
        super(scaling, gamma);
        this.m_shapingManager = new ShapingManager();
    }
    
    @Override
    public double shape(StateAction sa1, StateAction sa2, double reward){
        return reward +
                this.m_shapingManager.getShapingReward(
                        sa1.getState(),
                        sa1.getExtraState(),
                        sa1.getAction(),
                        sa2.getState(),
                        sa2.getExtraState()
                );
    }
    
}
