/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.advice;

import java.util.Arrays;

/**
 *
 * @author timbrys
 */
public class SARAd {
    private int[] state;
    private int action;
    private float reward;
    private boolean advice;
    
    public SARAd(int[] state, int action, float reward, boolean advice){
        this.state = state.clone();
        this.action = action;
        this.reward = reward;
        this.advice = advice;
    }

    public int[] getState() {
        return state;
    }

    public int getAction() {
        return action;
    }

    public float getReward() {
        return reward;
    }

    public boolean getAdvice() {
        return advice;
    }
    
    public String toString(){
        return Arrays.toString(state) + "," + action + "," + reward + "," + advice;
    }
}
