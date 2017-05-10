/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario;

import java.io.Serializable;

/**
 *
 * @author timbrys
 */
public class StateAction implements Serializable{
    protected int[] state;
    protected float[] extraState;
    protected int action;
    
    protected long key;
    
    public static StateAction initState(){
        return new StateAction(new int[10], new float[]{0,0}, 0);
    }
    
    public StateAction(int[] state, int action){
        this(state, new float[0], action);
    }

    public StateAction(int[] state, float[] extraState, int action){
        this.state = state.clone();
        this.extraState = extraState.clone();
        this.action = action;
        generateKey();
    }

    public int[] getState() {
        return state;
    }
    
    public float[] getExtraState(){
        return extraState;
    }

    public int getAction() {
        return action;
    }
    
    public void setAction(int action){
        this.action = action;
        generateKey();
    }
    
    public static double[] normalize(int[] state){
        double[] newState = new double[state.length];
        newState[0] = 1.0*state[0];
        newState[1] = 1.0*state[1];
        newState[2] = 1.0*state[2];
        newState[3] = (state[3])/8.0;
        newState[4] = (state[4])/255.0;
        newState[5] = (state[5])/255.0;
        newState[6] = (state[6])/255.0;
        newState[7] = (state[7])/15.0;
        newState[8] = (state[8])/21.0;
        newState[9] = (state[9])/21.0;
//        newState[10] = (state[10])/7.0;
        return newState;
    }
    
    public static StateAction unnormalize(double[] state, int action){
        int[] newState = new int[state.length];
        newState[0] = (int)state[0];
        newState[1] = (int)state[1];
        newState[2] = (int)state[2];
        newState[3] = (int)(state[3]*8);
        newState[4] = (int)(state[4]*255);
        newState[5] = (int)(state[5]*255);
        newState[6] = (int)(state[6]*255);
        newState[7] = (int)(state[7]*15);
        newState[8] = (int)(state[8]*21);
        newState[9] = (int)(state[9]*21);
//        newState[10] = (int)(state[10]*7);
        return new StateAction(newState, action);
    }
    
    public double[] rewardFeatures(){
        double[] newState = new double[27];
        newState[0] = getState()[0];
        newState[1] = getState()[1];
        newState[2] = getState()[2];
        newState[3] = (getState()[3]%3)/2.0;
        newState[4] = (getState()[3]/3)/2.0;
        for(int i=0; i<8; i++){
            newState[5+i] = (int)((getState()[4]/(Math.pow(2, i))))%2;
            newState[13+i] = (int)(getState()[5]/(Math.pow(2, i)))%2;
        }
        for(int i=0; i<4; i++){
            newState[21+i] = (int)(getState()[7]/(Math.pow(2, i)))%2;
        }
        newState[25] = getState()[8]/21.0;
        newState[26] = getState()[9]/21.0;
//        newState[27] = getState()[10]%2;
//        newState[28] = (getState()[10]/2)%2;
//        newState[29] = (getState()[10]/4)%2;
        return newState;
    }
    
//    public static double[] normalize(int[] state){
//        double[] newState = new double[state.length];
//        newState[0] = 1.0*state[0];
//        newState[1] = 1.0*state[1];
//        newState[2] = 1.0*state[2];
//        newState[3] = (state[3])/2.0;
//        newState[4] = (state[4])/2.0;
//        for(int i=0; i<20; i++){
//            newState[i+5] = 1.0*(state[i+5]);
//        }
//        newState[25] = (state[25])/21.0;
//        newState[26] = (state[26])/21.0;
//        return newState;
//    }
    
    public void generateKey() {
        long hash = action;
        hash += 12 * state[0];
        hash += 12*2 * state[1];
        hash += 12*2*2 * state[2];
        hash += 12*2*2*2 * state[3];
        hash += 12*2*2*2*9 * state[4];
        hash += 12*2*2*2*9*256 * state[5];
        hash += 12*2*2*2*9*256*256 * state[7];
        hash += 12*2*2*2*9*256*256*16 * state[8];
        hash += 12*2*2*2*9*256*256*16*22 * state[9];
//        hash += 12*2*2*2*9*256*256*16*22*22 * state[10];
        this.key = hash;
    }
    
//    public void generateKey() {
//        long hash = action;
//        hash += 12 * state[0];
//        hash += 12*2 * state[1];
//        hash += 12*2*2 * state[2];
//        hash += 12*2*2*2 * state[3];
//        hash += 12*2*2*2*3 * state[4];
//        for(int i=0; i<20; i++){
//            hash += 12*2*2*2*3*3 * Math.pow(2, i) * state[i+5];
//        }
//        hash += 12*2*2*2*3*3 * Math.pow(2, 20) * state[25];
//        hash += 12*2*2*2*3*3 * Math.pow(2, 20) * 22 * state[26];
//        this.key = hash;
//    }
    
    public long key(){
        return this.key;
    }
    
    @Override
    public StateAction clone(){
        return new StateAction(state.clone(), extraState.clone(), action);
    }
}
