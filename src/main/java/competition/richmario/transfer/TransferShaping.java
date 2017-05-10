/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.transfer;

import competition.richmario.QLHash;
import competition.richmario.StateAction;
import competition.richmario.shapings.Shaping;
import competition.transfer.Util;

/**
 *
 * @author timbrys
 */
public class TransferShaping extends Shaping{

    protected QLHash source;
    
    public TransferShaping(double scaling, double gamma, QLHash source){
        super(scaling, gamma);
        this.source = source;
    }
    
    @Override
    protected double actualPotential(StateAction sa) {
        return (Util.amongstArgMax(sa.getAction(), getQs(sa)) ? 1.0 : 0);
    }
    
    protected double[] getQs(StateAction state){
        double[] Qs = new double[12];
        for(int i=0; i<Qs.length; i++){
            Qs[i] = source.getValue(new StateAction(mapState(state).getState(), i));
        }
        return Qs;
    }
    
    protected static StateAction mapState(StateAction sa){
        int[] mappedState = new int[sa.getState().length];
        System.arraycopy(sa.getState(), 0, mappedState, 0, 4);
        mappedState[4] = mappedState[5] = mappedState[6] = 0;
        mappedState[8] = mappedState[9] = 21;
        return new StateAction(mappedState, sa.getAction());
    }
}
