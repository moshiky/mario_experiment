/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.pca;

import competition.richmario.StateAction;

/**
 *
 * @author timbrys
 */
public class PCAStateAction extends StateAction{
    
    private double[] PCAState;
    private int n;
    
    public PCAStateAction(int n, int[] state, int action, double[] PCAState){
        super(state, action);
        this.PCAState = PCAState;
        this.n = n;
    }

    public PCAStateAction(int n, int[] state, float[] extraState, int action, double[] PCAState){
        super(state, extraState, action);
        this.PCAState = PCAState;
        this.n = n;
    }
    
    public double[] getPCAState(){
        return PCAState;
    }
    
    public long key(){
        return key(n);
    }
    
    public long key(int m) {
        if(m == -1 || m == state.length){
            return super.key();
        } else {
            long hash = action;
            for(int i=0; i<m; i++){
//                if(((PCAState[i]+1)/2.0) < 0 || ((PCAState[i]+1)/2.0) > 1){
//                    System.out.println(PCAState[i]);
//                    System.out.println(((int)(((PCAState[i]+1)/2.0)*100)));
//                }
                double s = ((PCAState[i]+1.1)/2.2);
                if(s < 0.0) {
                    s = 0.0;
                }
                if(s > 1.0) {
                    s = 1.0;
                }
                hash += 12 * Math.pow(100, i-1) * ((int)(s*100));
            }
            return hash;
        }
    }
}
