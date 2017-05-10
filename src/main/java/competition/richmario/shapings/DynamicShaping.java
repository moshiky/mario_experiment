/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.shapings;

import competition.richmario.StateAction;
import competition.richmario.agents.QLambdaAgent;
import competition.richmario.agents.SarsaAgent;

/**
 *
 * @author timbrys
 */
public class DynamicShaping extends Shaping{

    protected QLambdaAgent agent;
    protected Shaping shaping;
    
    public DynamicShaping(double scaling, double gamma, Shaping shaping){
        super(scaling, gamma);
        this.agent = new SarsaAgent(0.5, 0.5, new ConstantInitialization(1.0, gamma, 0.0), gamma);
        this.shaping = shaping;
    }
    
    @Override
    public double shape(StateAction sa1, StateAction sa2, double reward){
        if(dummyState(sa2)){
            return dummyShape(sa1, reward);
        }
        agent.update(sa1, -reward(sa1, reward, sa2), sa2);
        double phi;
        if(prevPotential == Double.MAX_VALUE){
            phi = potential(sa1);
            initialPotential = phi;
        } else {
            phi = prevPotential;
        }
        double nextPhi = potential(sa2);
        prevPotential = nextPhi;
        
        return reward + gamma * nextPhi - phi;
    }
    
    @Override
    protected double actualPotential(StateAction sa) {
        return agent.getQ(sa);
    }
    
    protected float reward(StateAction sa1, double reward, StateAction sa2){
        return (float)shaping.potential(sa1);
    }
}
