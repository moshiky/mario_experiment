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

import competition.richmario.shapings.Shaping;
import competition.richmario.tableImplementations.AvlTreeBasedQTable;
import competition.richmario.tableImplementations.IQTable;


/**
 * @author moshec
 */
public class AbstractionQLambdaAgent {

    protected IQTable qTable;

    protected double alpha;
    protected double gamma;
    protected double lambda;

    protected Shaping shape;

    public AbstractionQLambdaAgent(Shaping shape, double gamma) {
        this(0.01, 0.5, shape, shape, gamma);
    }

    public AbstractionQLambdaAgent(Shaping initialization, Shaping shape, double gamma) {
        this(0.01, 0.5, initialization, shape, gamma);
    }

    public AbstractionQLambdaAgent(double alpha, double lambda, Shaping shape, double gamma){
        this(alpha, lambda, shape, shape, gamma);
    }

    public AbstractionQLambdaAgent(double alpha, double lambda, Shaping initialization, Shaping shape, double gamma){
        this.alpha = alpha;
        this.gamma = gamma;
        this.lambda = lambda;
        
        this.shape = shape;
        
        this.qTable = new AvlTreeBasedQTable(this.getNumActions());
    }
    
    public int getNumActions(){
        return 12;
    }
    
    public double getQ(double[] state, int action){
        return qTable.getKeyValue(state, action);
    }

    public void update(double[] previousState, int previousAction, double actionReward, double[] currentState,
                       int nextAction) {

        // find best next q value
        double[] nextQValues = new double[getNumActions()];
        double bestNextQValue = -Double.MAX_VALUE;
        int bestAction = 0;

        for (int i = 0 ; i < getNumActions() ; i++) {
            nextQValues[i] = getQ(currentState, i);
            if (nextQValues[i] > bestNextQValue){
                bestNextQValue = nextQValues[i];
                bestAction = i;
            }
        }

        // get previous state q value
        double previousStateQValue = this.getQ(previousState, previousAction);

        // calculate delta
        double delta = actionReward + (this.gamma * bestNextQValue) - previousStateQValue;

        // update previous state q value
        this.qTable.setKeyTrace(previousState, previousAction, 1.0);
        this.qTable.updateByTraces(alpha, delta);

        // decay or reset
        if (nextAction == bestAction) {
            this.qTable.decayTraces(this.gamma * this.lambda);
        }
        else {
            this.qTable.resetTraces();
        }
    }
}
