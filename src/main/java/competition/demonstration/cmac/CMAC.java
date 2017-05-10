/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.demonstration.cmac;

/**
 *
 * @author timbrys
 */
public class CMAC {
    private double alpha;
    private double gamma;
    private double lambda;
    
    private double[] weights;
    private double[] es;
    
    public CMAC(int size, double alpha, double gamma, double lambda, double init){
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
    
    public double getValue(int[] features){
        double Q = 0.0;
        for (int i = 0; i < features.length; i++) {
            Q += weights[features[i]];
        }
        return Q;
    }
    
    public double[] getValues(int[] features){
        double[] Q = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            Q[i] = weights[features[i]];
        }
        return Q;
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
    
    public void setTraces(int[] features){
        for(int i=0; i<features.length; i++){
            es[features[i]] = 1;
        }
    }
}
