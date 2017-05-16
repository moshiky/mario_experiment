/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.agents;

import competition.richmario.StateAction;
import loggingUtils.Logger;
import sun.rmi.runtime.Log;
import util.Util;

/**
 *
 * @author timbrys
 */
public class LinearEnsembleAgent extends EnsembleAgent {

    public LinearEnsembleAgent(Logger logger, QLambdaAgent[] agents, double epsilon) {
        super(logger, agents, epsilon, false);
    }
    
    public LinearEnsembleAgent(Logger logger, QLambdaAgent[] agents, double epsilon, boolean record) {
        super(logger, agents, epsilon, record);
    }
    
    @Override
    protected int greedyActionSelection(StateAction sa){
        double[] Qs = new double[getNumActions()];
        for (QLambdaAgent agent : agents) {
            for (int i = 0; i<Qs.length; i++) {
                sa.setAction(i);
                Qs[i] += agent.getQ(sa);
            }
        }
        return Util.argMax(Qs);
    }

    @Override
    protected double getPreference(int i, StateAction sa) {
        double value = 0.0;
        for (QLambdaAgent agent : agents) {
            sa.setAction(i);
            value += agent.getQ(sa);
        }
        return value;
    }
}
