/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.onlineinit;

import competition.richmario.StateAction;
import competition.richmario.agents.QLambdaAgent;
import competition.richmario.shapings.Initialization;

/**
 *
 * @author timbrys
 */
public class OnlineInitialization extends Initialization{

    protected QLambdaAgent agent;
    
    public OnlineInitialization(double scaling, double gamma){
        super(scaling, gamma);
        this.agent = null;
    }
    
    public OnlineInitialization(double scaling, double gamma, QLambdaAgent agent){
        super(scaling, gamma);
        this.agent = agent;
    }
    
    public void setAgent(QLambdaAgent agent){
        this.agent = agent;
    }
    
    @Override
    protected double actualPotential(StateAction sa) {
        int counter = 0;
        double Q = 0.0;
        
        for(int i=0; i<sa.getState().length; i++){
            StateAction[] sam = neighbours(sa, i);
            for (StateAction sam1 : sam) {
                if(agent.getValues().hasEntry(sam1)){
                    Q += agent.getValues().getValue(sam1);
                    counter++;
                }
            }
        }
        
        return Q/counter;
    }
    
    protected StateAction[] neighbours(StateAction sa, int dim){
        switch(dim){
            default: 
                return new StateAction[0];
            case 0:
            case 1:
            case 2:
                int[] state = sa.getState().clone();
                state[dim] = ((state[dim] == 0) ? 1 : 0);
                return new StateAction[]{new StateAction(state, sa.getAction())};
        }
    }
    
}
