/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario;

import competition.richmario.shapings.Shaping;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author timbrys
 */
public class QLHash {
    protected double alpha;
    protected double gamma;
    protected double lambda;
    
    protected Shaping init;
    
    protected HashMap<Long, Double> weights;
    protected HashMap<Long, Double> es;
    
    public QLHash(double alpha, double gamma, double lambda, Shaping init){
        this.weights = new HashMap<Long, Double>();
        this.es = new HashMap<Long, Double>();
        
        this.alpha = alpha;
        this.gamma = gamma;
        this.lambda = lambda;
        
        this.init = init;
    }
    
    public void reset(){
        resetEs();
        resetWeights();
    }
    
    public void resetWeights(){
        this.weights = new HashMap<Long, Double>();
    }
    
    public void resetEs(){
        this.es = new HashMap<Long, Double>();
    }
    
    public double getValue(StateAction features){
        if(!weights.containsKey(features.key())){
            weights.put(features.key(), init.potential(features));
        }
        return weights.get(features.key());
    }
    
    public double getValueNoUpdate(StateAction features){
        if(!weights.containsKey(features.key())){
            return init.potential(features);
        } else {
            return weights.get(features.key());
        }
    }
    
    public void setValue(StateAction features, double value){
        weights.put(features.key(), value);
    }
    
    public boolean hasEntry(StateAction features){
        return weights.containsKey(features.key());
    }
    
    public void update(double delta){
        Long f;
        for(Iterator<Long> it = es.keySet().iterator(); it.hasNext(); ){
            f = it.next();
            weights.put(f, weights.get(f) + (alpha * delta * es.get(f)));
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

    public void setTracesSimilarity(StateAction features, Double similarity){
        Double ess = es.get(features.key());
        if(ess == null) {
            es.put(features.key(), similarity);
        } else if(ess != null && ess < similarity) {
            es.put(features.key(), similarity);
        }
    }
    
    public int getSize(){
        return weights.size();
    }
}
