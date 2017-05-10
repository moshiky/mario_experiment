/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author timbrys
 */
public class VariancesHash {
    protected double beta;
    protected double gamma;
    protected double lambda;
    
    protected HashMap<Long, Double> variances;
    protected HashMap<Long, Double> es;
    
    public VariancesHash(double beta, double gamma, double lambda){
        this.variances = new HashMap<Long, Double>();
        this.es = new HashMap<Long, Double>();
        this.beta = beta;
        this.gamma = gamma;
        this.lambda = lambda;
    }
    
    public void reset(){
        resetEs();
        resetWeights();
    }
    
    public void resetWeights(){
        this.variances = new HashMap<Long, Double>();
    }
    
    public void resetEs(){
        this.es = new HashMap<Long, Double>();
    }
    
    public double getValue(StateAction features){
        if(!variances.containsKey(features.key())){
            variances.put(features.key(), 0.0);
            es.put(features.key(), 0.0);
        }
        return variances.get(features.key());
    }
    
    public void setValue(StateAction features, double value){
        variances.put(features.key(), value);
    }
    
    public void update(double delta){
        Long f;
        for(Iterator<Long> it = es.keySet().iterator(); it.hasNext(); ){
            f = it.next();
            
            if(!variances.containsKey(f)){
                variances.put(f, 0.0);
            }
            variances.put(f, (1-beta)*variances.get(f) + (beta * delta * es.get(f)));
        }
    }
    
    public void decay(){
        Map.Entry<Long, Double> f;
        double e;
        for(Iterator<Map.Entry<Long, Double>> it = es.entrySet().iterator(); it.hasNext(); ){
            f = it.next();
            e = f.getValue()*gamma*lambda;
            if(e < 0.001){
                it.remove();
            } else {
                es.put(f.getKey(), e);
            }
        }
    }
    
    public void setTraces(StateAction features){
        es.put(features.key(), 1.0);
    }
    
    public int getSize(){
        return variances.size();
    }
}
