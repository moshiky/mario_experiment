/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.transfer;

import competition.richmario.StateAction;
import competition.richmario.agents.EnsembleAgent;
import competition.richmario.agents.LinearEnsembleAgent;
import competition.richmario.agents.QLambdaAgent;
import util.RNG;
import util.Util;

/**
 *
 * @author timbrys
 */
public class ProbabilisticPolicyReuseAgent extends LinearEnsembleAgent {

    protected double phi;
    protected QLambdaAgent source;
    
    public ProbabilisticPolicyReuseAgent(QLambdaAgent agent, QLambdaAgent source, double epsilon){
        this(agent, source, epsilon, false);
    }
    
    public ProbabilisticPolicyReuseAgent(QLambdaAgent agent, QLambdaAgent source, double epsilon, boolean recording){
        super(null, new QLambdaAgent[]{agent}, epsilon, recording);
        this.source = source;
        this.phi = 1.0;
    }
    
    @Override
    public int egreedyActionSelection(StateAction sa){
        if(RNG.randomDouble() < phi){
            return policyReuse(sa);
        } else if(RNG.randomDouble() < epsilon){
            return RNG.randomInt(getNumActions());
        } else {
            return greedyActionSelection(sa);
        }
    }
    
    protected int policyReuse(StateAction sa){
        double[] Qs = new double[getNumActions()];
        for (int i = 0; i<Qs.length; i++) {
            sa.setAction(i);
            Qs[i] += source.getQ(TransferShaping.mapState(sa));
        }
        return Util.argMax(Qs);
    }

    @Override
    public void endEpisode() {
        super.endEpisode();
        phi*=0.95;
    }
    
    
}
