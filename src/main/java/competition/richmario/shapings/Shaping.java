/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.shapings;

import competition.richmario.StateAction;
import java.util.HashMap;

/**
 *
 * @author timbrys
 */
public abstract class Shaping {
    
    protected double gamma;
    protected double prevPotential;
    protected double initialPotential;
    
    protected double scaling;
    
    protected boolean memoize;
    protected HashMap<Long, Double> memoization;
    
    public Shaping(double scaling, double gamma){
        this.gamma = gamma;
        this.prevPotential = Double.MAX_VALUE;
        this.scaling = scaling;
        this.memoize = false;
        this.memoization = null;
    }
    
    public Shaping(double scaling, double gamma, boolean memoize){
        this.gamma = gamma;
        this.prevPotential = Double.MAX_VALUE;
        this.scaling = scaling;
        this.memoize = memoize;
        this.memoization = new HashMap<Long, Double>();
    }
    
    public void reset(){
        prevPotential = Double.MAX_VALUE;
    }
    
    public double shape(StateAction sa1, StateAction sa2, double reward){
        if(dummyState(sa2)){
            return dummyShape(sa1, reward);
        }
        double phi;
        if(prevPotential == Double.MAX_VALUE){
            phi = potential(sa1);
            initialPotential = phi;
        } else {
            phi = prevPotential;
        }
        double nextPhi = potential(sa2);
        prevPotential = nextPhi;
        
        return reward + gamma * nextPhi - phi;
    }
    
    //move to dummy state with initial potential
    public double dummyShape(StateAction sa, double reward){
        double phi;
        if(prevPotential == Double.MAX_VALUE){
            phi = potential(sa);
            initialPotential = phi;
        } else {
            phi = prevPotential;
        }
        double nextPhi = initialPotential;
        
        return reward + gamma * nextPhi - phi;
    }
    
    protected boolean dummyState(StateAction sa){
        return sa.getAction() == -1;
    }
    
    public double potential(StateAction sa){
        if(memoize){
            return scaling*memoizedPotential(sa);
        } else {
            return scaling*actualPotential(sa);
        }
    }
    
    protected double memoizedPotential(StateAction sa){
        if(memoization.containsKey(sa.key())){
            return memoization.get(sa.key());
        } else {
            double potential = actualPotential(sa);
            memoization.put(sa.key(), potential);
            return potential;
        }
    }
    
    protected abstract double actualPotential(StateAction sa);
    
    public void endEpisode(){
        this.prevPotential = Double.MAX_VALUE;
    }
}
