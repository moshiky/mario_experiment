/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.aamas15;

import competition.aamas15.cmac.*;

/**
 *
 * @author timbrys
 */
public class QLHash {
    private double alpha;
    private double beta;
    private double gamma;
    private double lambda;
    
    private double[] weights;
    private double[] variances;
    private double[] es;
    
    public QLHash(int size, double alpha, double beta, double gamma, double lambda, double init){
        this.weights = new double[size];
        this.variances = new double[size];
        for(int i=0; i<size; i++){
            weights[i] = init;
            variances[i] = 0.0;
        }
        this.es = new double[size];
        
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.lambda = lambda;
    }
    
    public void reset(){
        resetEs();
        resetWeights();
    }
    
    public void resetWeights(){
        this.weights = new double[weights.length];
        this.variances = new double[variances.length];
    }
    
    public void resetEs(){
        this.es = new double[es.length];
    }
    
    public double getValue(int state){
        return weights[state];
    }
    
    public double getVariance(int state){
        return variances[state];
    }
    
    public void update(double delta){
        for(int i=0; i<weights.length; i++){
            weights[i] += alpha * delta * es[i];
        }
    }
    
    public void updateVariance(int state, double delta){
        variances[state] = (1-beta)*variances[state] + beta * delta;
    }
    
    public void decay(){
        for(int i=0; i<weights.length; i++){
            es[i] *= gamma*lambda;
        }
    }
    
    public void setTraces(int state){
        es[state] = 1;
    }
}
