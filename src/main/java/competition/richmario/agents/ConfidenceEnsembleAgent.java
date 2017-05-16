/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.agents;

import competition.richmario.StateAction;
import java.util.ArrayList;
import util.RNG;
import util.Util;

/**
 *
 * @author timbrys
 */
public class ConfidenceEnsembleAgent extends EnsembleAgent {

    public ConfidenceEnsembleAgent(QLambdaAgent[] agents, double epsilon) {
        super(null, agents, epsilon);
    }
    
    @Override
    protected int greedyActionSelection(StateAction sa) {
//        ArrayList<Integer> bestEnsembles = new ArrayList<Integer>();
//        double confidence = -1.0;
//        double p;
//        
        double[] Qs = new double[getNumActions()];
//        for(int e=0; e<agents.length; e++){
//            p = agents[e].confidence(sa);
//            if(p >= confidence){
//                if(p > confidence){
//                    bestEnsembles.clear();
//                }
//                confidence = p;
//                bestEnsembles.add(e);
//            }
//        }
        
        int bestEnsemble = 0;//bestEnsembles.get(RNG.randomInt(bestEnsembles.size()));
        for(int i=0; i<Qs.length; i++){
            sa.setAction(i);
            Qs[i] += agents[bestEnsemble].getQ(sa);
        }
        
//        int x = (int)(100*(((MountainCar)prob).getPosition() - MountainCar.minPosition)/(MountainCar.maxPosition-MountainCar.minPosition));
//        int y = (int)(100*(((MountainCar)prob).getVelocity() - MountainCar.minVelocity)/(MountainCar.maxVelocity-MountainCar.minVelocity));
//        objectivesSelected[bestEnsemble][x][y]++;
//        objectivesSelected[3][x][y]++;
        
        return Util.argMax(Qs);
    }

    @Override
    protected double getPreference(int i, StateAction sa) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
