/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.agents;

import competition.richmario.StateAction;
import loggingUtils.Logger;
import util.Util;

/**
 *
 * @author moshec
 */
public class AbstractionLinearEnsembleAgent extends AbstractionEnsembleAgent {

    public AbstractionLinearEnsembleAgent(Logger logger, AbstractionQLambdaAgent[] agents, double epsilon) {
        super(logger, agents, epsilon, false);
    }

    public AbstractionLinearEnsembleAgent(Logger logger, AbstractionQLambdaAgent[] agents, double epsilon, boolean record) {
        super(logger, agents, epsilon, record);
    }
    
    @Override
    protected int greedyActionSelection(double[] state){
        double[] nextQValues = new double[getNumActions()];
        for (AbstractionQLambdaAgent agent : agents) {
            for (int i = 0 ; i < nextQValues.length ; i++) {
                nextQValues[i] += agent.getQ(state, i);
            }
        }

        return Util.argMax(nextQValues);
    }
}

