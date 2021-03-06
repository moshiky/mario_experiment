/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.transfer;

import competition.advice.*;
import competition.aamas15.*;
import competition.aamas15.cmac.*;

/**
 *
 * @author timbrys
 */
public class QLHash {
    protected double alpha;
    protected double gamma;
    protected double lambda;
    
    protected double[] weights;
    private double[] es;
    
    public QLHash(int size, double alpha, double gamma, double lambda, double init){
        this.weights = new double[size];
        for(int i=0; i<size; i++){
            weights[i] = init;
        }
        this.es = new double[size];
        
        this.alpha = alpha;
        this.gamma = gamma;
        this.lambda = lambda;
    }
    
    public void reset(){
        resetEs();
        resetWeights();
    }
    
    public void resetWeights(){
        this.weights = new double[weights.length];
    }
    
    public void resetEs(){
        this.es = new double[es.length];
    }
    
    public double getValue(int state){
        return weights[state];
    }
    
    public void setValue(int state, double value){
        weights[state] = value;
    }
    
    public double getValue(int[] state){
        double q = 0.0;
        for(int i=0; i<state.length; i++){
            q += weights[state[i]];
        }
        return q;
    }
    
    public void update(double delta){
        for(int i=0; i<weights.length; i++){
            weights[i] += alpha * delta * es[i];
        }
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
