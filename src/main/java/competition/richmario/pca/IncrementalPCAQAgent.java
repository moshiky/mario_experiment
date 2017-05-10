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
package competition.richmario.pca;

import competition.richmario.shapings.HeuristicShaping;
import competition.richmario.agents.QLambdaAgent;
import competition.richmario.*;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.attributeSelection.PrincipalComponents;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: Sep 1, 2009 Time: 3:12:07 PM Package: competition.cig.sergeykarakovskiy
 */
public class IncrementalPCAQAgent extends QLambdaAgent{
    
    protected weka.attributeSelection.PrincipalComponents pca;
    protected ConverterUtils.DataSource source;
    protected int dimensions;
    
    protected int stepCounter;
    protected double unchanged;
    protected int unchangedits;
    protected boolean firstEpisode;
    
    public IncrementalPCAQAgent(double gamma, String demo, int dimensions) throws Exception{
        this(0.01, 0.5, gamma, demo, dimensions);
    }
    
    public IncrementalPCAQAgent(double alpha, double lambda, double gamma, String demo, int dimensions) throws Exception{
        super(alpha, lambda, new HeuristicShaping(-1, 1.0, gamma), gamma);
        
        source = new ConverterUtils.DataSource(demo);
        Instances data = source.getDataSet();
        data.deleteAttributeAt(data.numAttributes()-1);
        source = new ConverterUtils.DataSource(data);
        this.pca = new PrincipalComponents();
        this.pca.setCenterData(true);
        this.pca.setVarianceCovered(1.0);
        this.pca.setMaximumAttributeNames(-1);
        this.pca.buildEvaluator(data);
        
        this.dimensions = dimensions;
        
        this.stepCounter = 0;
        this.unchanged = 0.0;
        this.unchangedits = 0;
        this.firstEpisode = true;
    }
    
    private StateAction transform(StateAction stateaction){
        try {
            Instances unlabeled = source.getStructure();
            unlabeled.add(new Instance(1.0, StateAction.normalize(stateaction.getState())));
            double[] PCAState = pca.convertInstance(unlabeled.firstInstance()).toDoubleArray();
            return new PCAStateAction(dimensions, stateaction.getState(), stateaction.getExtraState(), stateaction.getAction(), PCAState);
        } catch (Exception ex) {
            Logger.getLogger(IncrementalPCAQAgent.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
            return new StateAction(new int[]{}, 0);
        }
    }
    
    public double getQ(StateAction stateaction){
        return cmac.getValue(transform(stateaction));
    }
    
    protected void upgrade(){
        if(DemonstrationExperiment.DEBUG){
            System.out.println("UPGRADING--------------------------");
        }
        cmac = new QLHash(alpha, gamma, lambda, new IncrementalHashInitialization(1.0, gamma, cmac, dimensions));
        dimensions++;
    }
    
    public void update(StateAction p, float reward, StateAction n) {
        stepCounter++;
//        int threshold = 100000*dimensions*(dimensions+1)/2;
//        if(stepCounter % threshold == 0 && dimensions < p.getState().length){
//            upgrade();
//        }
        
        StateAction previous = transform(p);
        StateAction next = transform(n);
        
        int[] state = next.getState();
        
        double deltaR = reward;
        deltaR -= cmac.getValue(previous);
        
        double QsR[] = new double[getNumActions()];
        double bestR = -Double.MAX_VALUE;
        for(int i=0; i<getNumActions(); i++){
            QsR[i] = cmac.getValue(transform(new StateAction(state, next.getExtraState(), i)));
            if(QsR[i] > bestR){
                bestR = QsR[i];
            }
        }

//        double[] preWeights = currentWeights(state, next.getExtraState());
        TreeSet<Integer> preBestActions = bestActions(state, next.getExtraState());
        
        cmac.update(deltaR + gamma*bestR);
        
//        double[] postWeights = currentWeights(state, next.getExtraState());
        TreeSet<Integer> postBestActions = bestActions(state, next.getExtraState());
//        preBestActions.retainAll(postBestActions);
//        if(preBestActions.size() > 0 && !Arrays.equals(preWeights, postWeights)){
        if(preBestActions.equals(postBestActions)){
            unchanged += (1.0/stepCounter)*(1.0-unchanged);
        } else {
            unchanged += (1.0/stepCounter)*(0.0-unchanged);
//            System.out.println(preBestActions);
        }
//        preBestActions.retainAll(postBestActions);
//        System.out.println(preBestActions);
        
        if(QsR[next.getAction()] == bestR){
            cmac.decay();
        } else {
            cmac.resetEs();
        }
        
        cmac.setTraces(next);
    }
    
    protected double[] currentWeights(int[] state, float[] extraState){
        double QsR[] = new double[getNumActions()];
        for(int i=0; i<getNumActions(); i++){
            QsR[i] = cmac.getValue(transform(new StateAction(state, extraState, i)));
        }
        return QsR;
    }
    
    protected TreeSet bestActions(int[] state, float[] extraState){
        TreeSet<Integer> li = new TreeSet<Integer>();
        double best = -9999999, Q;
        for(int i=0; i<getNumActions(); i++){
            Q = cmac.getValue(transform(new StateAction(state, extraState, i)));
            if(Q > best){
                best = Q;
                li.clear();
                li.add(i);
            } else if(Q == best){
                li.add(i);
            }
        }
        return li;
    }
    
    @Override
    public void endEpisode(StateAction previous, float reward, StateAction next) {
        double deltaR = 0.0;
        deltaR -= cmac.getValue(transform(previous));
        
        cmac.update(deltaR);
        
        shape.endEpisode();
        
//        if(DemonstrationExperiment.DEBUG){
//            System.out.println(unchanged);
//        }
//        System.out.println(stepCounter);
//        System.out.println();
        if(unchanged > 0.95){
            unchangedits++;
        } else {
            unchangedits = 0;
        }
        if(unchangedits == 5 && dimensions < previous.getState().length){
            upgrade();
            unchangedits = 0;
        }
        firstEpisode = false;
        unchanged = 0.0;
        stepCounter = 0;
        
        cmac.resetEs();
    }
    
//    protected int[] tileCoding(double[] state, int action) {
//        int[] extra = new int[]{action, isMarioAbleToShoot ? 0 : 1, facing+1, isMarioOnGround ? 0 : 1};
//        return TileCoding.GetTiles(nrTilings, state, maxNrTiles, extra);
//    }

}
