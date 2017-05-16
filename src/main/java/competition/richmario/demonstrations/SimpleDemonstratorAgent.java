/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.demonstrations;

import competition.richmario.StateAction;
import competition.richmario.agents.EnsembleAgent;
import competition.richmario.agents.QLambdaAgent;
import util.RNG;

/**
 *
 * @author timbrys
 */
public class SimpleDemonstratorAgent extends EnsembleAgent{

    public SimpleDemonstratorAgent() {
        super(null, new QLambdaAgent[0], 0.0, true);
    }
    
    @Override
    public int egreedyActionSelection(StateAction sa) {
        double rand = RNG.randomDouble();
        if(rand < 1.0/3.0){
            return 2;
        } else if(rand < 2.0/3.0){
            return 5;
        } else {
            return 11;
        }
    }

    @Override
    protected double getPreference(int i, StateAction sa) {
        return 0.0;
    }

    @Override
    protected int greedyActionSelection(StateAction sa) {
        return egreedyActionSelection(sa);
    }
    
}
