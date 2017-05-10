/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.pca;

import competition.richmario.QLHash;
import competition.richmario.shapings.Shaping;
import competition.richmario.StateAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.attributeSelection.PrincipalComponents;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 *
 * @author timbrys
 */
public class MultiPCAQLHash extends QLHash {
    
    protected weka.attributeSelection.PrincipalComponents pca;
    protected ConverterUtils.DataSource source;
    
    protected QLHash[] hashes;
    protected static int[] dimensions = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 10};
    
    public MultiPCAQLHash(double alpha, double gamma, double lambda, Shaping init, String demo) throws Exception{
        super(alpha, gamma, lambda, init);
        
        source = new ConverterUtils.DataSource(demo);
        Instances data = source.getDataSet();
        data.deleteAttributeAt(data.numAttributes()-1);
        source = new ConverterUtils.DataSource(data);
        this.pca = new PrincipalComponents();
        this.pca.setCenterData(true);
        this.pca.setVarianceCovered(1.0);
        this.pca.setMaximumAttributeNames(-1);
        this.pca.buildEvaluator(data);
        
        this.hashes = new QLHash[dimensions.length];
        for(int i=0; i<this.hashes.length; i++){
            this.hashes[i] = new QLHash(alpha, gamma, lambda, init);
        }
    }
    
    
    public void reset(){
        for(int i=0; i<hashes.length; i++){
            hashes[i].reset();
        }
    }
    
    public void resetWeights(){
        for(int i=0; i<hashes.length; i++){
            hashes[i].resetWeights();
        }
    }
    
    public void resetEs(){
        for(int i=0; i<hashes.length; i++){
            hashes[i].resetEs();
        }
    }
    
    public double getValue(StateAction features){
        double Q = 0.0;
        for(int i=0; i<hashes.length; i++){
            Q += hashes[i].getValue(transform(features, dimensions[i]));
        }
        return Q;
    }
    
    public double getValueNoUpdate(StateAction features){
        System.out.println("Don't call this please");
        System.exit(-1);
        return 0.0;
    }
    
    public void setValue(StateAction features, double value){
        for(int i=0; i<hashes.length; i++){
            hashes[i].setValue(transform(features, dimensions[i]), value);
        }
    }
    
    public void update(double delta){
        for(int i=0; i<hashes.length; i++){
            hashes[i].update(delta);
        }
    }
    
    public void decay(){
        for(int i=0; i<hashes.length; i++){
            hashes[i].decay();
        }
    }
    
    public void setTraces(StateAction features){
        for(int i=0; i<hashes.length; i++){
            hashes[i].setTraces(transform(features, dimensions[i]));
        }
    }
    
    public int getSize(){
        return -1;
    }
    
    private StateAction transform(StateAction stateaction, int dims){
        try {
            Instances unlabeled = source.getStructure();
            unlabeled.add(new Instance(1.0, StateAction.normalize(stateaction.getState())));
            double[] PCAState = pca.convertInstance(unlabeled.firstInstance()).toDoubleArray();
            return new PCAStateAction(dims, stateaction.getState(), stateaction.getExtraState(), stateaction.getAction(), PCAState);
        } catch (Exception ex) {
            Logger.getLogger(IncrementalPCAQAgent.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
            return new StateAction(new int[]{}, 0);
        }
    }
}
