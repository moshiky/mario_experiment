/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package competition.richmario.agents;

import competition.richmario.QLHash;
import competition.richmario.shapings.Shaping;
import competition.richmario.StateAction;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: Sep 1, 2009 Time: 3:12:07 PM Package: competition.cig.sergeykarakovskiy
 */
public class QLambdaAgent{
    
    protected QLHash cmac;

    protected double alpha;
    protected double gamma;
    protected double lambda;
    
    protected Shaping shape;
    
    public QLambdaAgent(Shaping shape, double gamma) {
        this(0.01, 0.5, shape, shape, gamma);
    }
    
    public QLambdaAgent(Shaping initialization, Shaping shape, double gamma) {
        this(0.01, 0.5, initialization, shape, gamma);
    }
    
    public QLambdaAgent(double alpha, double lambda, Shaping shape, double gamma){
        this(alpha, lambda, shape, shape, gamma);
    }
    
    public QLambdaAgent(double alpha, double lambda, Shaping initialization, Shaping shape, double gamma){
        this.alpha = alpha;
        this.gamma = gamma;
        this.lambda = lambda;
        
        this.shape = shape;
        
        cmac = new QLHash(alpha, gamma, lambda, initialization);
    }
    
    public int getNumActions(){
        return 12;
    }
    
    public double getQ(StateAction stateaction){
        return cmac.getValue(stateaction);
    }

    public void update(StateAction previous, float reward, StateAction next, Double deltaR, Double similarity) {

        int[] state = next.getState();

        double QsR[] = new double[getNumActions()];
        double bestR = -Double.MAX_VALUE;
        for(int i=0; i<getNumActions(); i++){
            QsR[i] = getQ(new StateAction(state, next.getExtraState(), i));
            if(QsR[i] > bestR){
                bestR = QsR[i];
            }
        }

        cmac.update(similarity * (deltaR + gamma*bestR));

        if(QsR[next.getAction()] == bestR){
            cmac.decay();
        } else {
            cmac.resetEs();
        }

        cmac.setTraces(next);
    }

    public void update(StateAction previous, float reward, StateAction next) {
        
        int[] state = next.getState();

        double deltaR = getDeltaR(previous, reward, next);
        
        double QsR[] = new double[getNumActions()];
        double bestR = -Double.MAX_VALUE;
        for(int i=0; i<getNumActions(); i++){
            QsR[i] = getQ(new StateAction(state, next.getExtraState(), i));
            if(QsR[i] > bestR){
                bestR = QsR[i];
            }
        }

        cmac.update(deltaR + gamma*bestR);

        if(QsR[next.getAction()] == bestR){
            cmac.decay();
        } else {
            cmac.resetEs();
        }


    }

    public void setTraces(StateAction action) { cmac.resetEs(); cmac.setTraces(action); }

    public void setTracesSimilarity(StateAction action, Double similarity) {
        double q = getQ(action);

        cmac.setTracesSimilarity(action, similarity);
    }


    public double getDeltaR(StateAction previous, float reward, StateAction next) {
        double deltaR = reward; //shape.shape(previous, next, reward);
        deltaR -= getQ(previous);
        return deltaR;
    }

    public void endEpisode(StateAction previous, float reward, StateAction next) {
        double deltaR = shape.shape(previous, next, 0.0);
        deltaR -= getQ(previous);
        
        cmac.update(deltaR);
        
        shape.endEpisode();
        
        cmac.resetEs();
    }
    
    public int getSize(){
        return cmac.getSize();
    }
    
    public QLHash getValues(){
        return cmac;
    }


}
