/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.pca;

import competition.richmario.QLHash;
import competition.richmario.StateAction;
import competition.richmario.shapings.Initialization;

/**
 *
 * @author timbrys
 */
public class IncrementalHashInitialization extends Initialization{

    protected int n;
    protected QLHash previous;
    
    public IncrementalHashInitialization(double scaling, double gamma, QLHash previous, int dimension){
        super(scaling, gamma);
        this.previous = previous;
        this.n = dimension;
    }
    
    @Override
    protected double actualPotential(StateAction sa) {
        if(sa instanceof PCAStateAction){
            PCAStateAction sa2 = (PCAStateAction)sa;
            return previous.getValueNoUpdate(new PCAStateAction(n, sa2.getState(), sa2.getExtraState(), sa2.getAction(), sa2.getPCAState()));
        } else {
            return 0.0;
        }
    }
    
}
