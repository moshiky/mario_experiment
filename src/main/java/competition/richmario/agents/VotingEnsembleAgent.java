/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.agents;

import competition.richmario.StateAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import util.Util;


/**
 *
 * @author timbrys
 */
public class VotingEnsembleAgent extends EnsembleAgent{
    
    public VotingEnsembleAgent(QLambdaAgent[] agents, double epsilon) {
        super(null, agents, epsilon);
    }
    
    @Override
    protected int greedyActionSelection(StateAction sa){
        int[] votes = new int[getNumActions()];
        for(int e=0; e<agents.length; e++){
            double[] Qs = new double[getNumActions()];
            for(int i=0; i<Qs.length; i++){
                sa.setAction(i);
                Qs[i] += agents[e].getQ(sa);
            }
            votes[Util.argMax(Qs)]++;
        }
        return Util.argMax(votes);
    }

    @Override
    protected double getPreference(int i, StateAction sa) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
