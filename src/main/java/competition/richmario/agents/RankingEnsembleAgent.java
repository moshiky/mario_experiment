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
public class RankingEnsembleAgent extends EnsembleAgent{
    
    public RankingEnsembleAgent(QLambdaAgent[] agents, double epsilon) {
        super(null, agents, epsilon);
    }
    
    @Override
    protected int greedyActionSelection(StateAction sa){
        
        ArrayList<TreeMap<Double, Integer>> maps = new ArrayList<TreeMap<Double, Integer>>();
        
        for(int o=0; o<agents.length; o++){
            maps.add(new TreeMap<Double, Integer>());
        }
        
        for(int i=0; i<getNumActions(); i++){
            for(int o=0; o<agents.length; o++){
                sa.setAction(i);
                maps.get(o).put(agents[o].getQ(sa), i);
            }
        }
        
        double[] ranking = new double[getNumActions()];
        for(int o=0; o<agents.length; o++){
            Collection<Integer> values = maps.get(o).values();
            Iterator<Integer> it = values.iterator();
//            it.next();
            int i=0;
            while(it.hasNext()){
                ranking[it.next()] += (1.0)*i/(ranking.length-1);
                i++;
            }
        }
        return Util.argMax(ranking);
    }

    @Override
    protected double getPreference(int i, StateAction sa) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
