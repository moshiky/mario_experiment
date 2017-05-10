/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package competition.demonstration;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import java.awt.event.KeyEvent;
import java.util.*;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: Sep 1, 2009 Time: 3:12:07 PM Package: competition.cig.sergeykarakovskiy
 */
public class SimpleAgent extends BasicMarioAIAgent implements Agent {
    
    protected double prevPotential;
    protected double potential;
    
    private AgentType type;
    private Demonstration demonstration;
    
    protected int prevAction = 0;
    
    public SimpleAgent() {
        super("QLambda");
        
        this.demonstration = new Demonstration(-1);
        
        reset();
    }
    
    public int getNumActions(){
        return 12;
    }
    
    public Integer[] sortEnemies(){
        Integer[] sorted = new Integer[enemiesFloatPos.length/3];
        for(int i=0; i<sorted.length; i++){
            sorted[i] = i*3;
        }
        Arrays.sort(sorted, new Comparator<Integer>(){
            @Override
            public int compare(Integer o1, Integer o2) {
                double dist1 = (enemiesFloatPos[o1+1]*enemiesFloatPos[o1+1] + enemiesFloatPos[o1+2]*enemiesFloatPos[o1+2]);
                double dist2 = (enemiesFloatPos[o2+1]*enemiesFloatPos[o2+1] + enemiesFloatPos[o2+2]*enemiesFloatPos[o2+2]);
                return ((Double)dist1).compareTo((Double)dist2);
            }
        });
        return sorted;
    }
    
    public int[] closestEnemy(){
        Integer[] sorted = sortEnemies();
        
        int[] enemy = new int[2];
        if(sorted.length == 0){
            enemy[0] = 1000;
            enemy[1] = 1000;
        } else {
            enemy[0] = (int)(enemiesFloatPos[sorted[0]+1]/13);
            enemy[1] = (int)(enemiesFloatPos[sorted[0]+2]/13);
        }
        return enemy;
    }
    
    private int getStateSize(){
        return 6;
    }
    
    public int[] getState(){
        int[] state = new int[6];
        
        // CLOSEST TWO ENEMIES
        int[] enemy = closestEnemy();
        state[0] = isMarioAbleToJump ? 0 : 1;
        state[1] = isMarioOnGround ? 0 : 1;
        state[2] = facing == -1 ? 0 : 1;
        state[3] = isMarioAbleToShoot ? 0 : 1;
        
        if(Math.abs(enemy[0]) < 11 && Math.abs(enemy[1]) < 11){
            state[4] = enemy[0]+10;
            state[5] = enemy[1]+10;
        } else {
            state[4] = 21;
            state[5] = 21;
        }
        
        return state; //2*2*2*2*13*13*12
    }
    
    public int calcState(int[] state, int action){
        int hashedState = state[0];
        hashedState += 2*state[1];
        hashedState += 2*2*state[2];
        hashedState += 2*2*2*state[3];
        hashedState += 2*2*2*2*state[4];
        hashedState += 2*2*2*2*22*state[5];
        hashedState += 2*2*2*2*22*22*action;
        return hashedState;
    }
    
    public boolean[] getAction() {
        for (int i = 0; i < action.length; ++i) {
            action[i] = false;
        }
        switch(prevAction){
            case 0:
                break;
            case 1:
                action[Mario.KEY_LEFT] = true;
                break;
            case 2:
                action[Mario.KEY_RIGHT] = true;
                break;
            case 3:
                action[Mario.KEY_JUMP] = true;
                break;
            case 4:
                action[Mario.KEY_LEFT] = true;
                action[Mario.KEY_JUMP] = true;
                break;
            case 5:
                action[Mario.KEY_RIGHT] = true;
                action[Mario.KEY_JUMP] = true;
                break;
            case 6:
                action[Mario.KEY_SPEED] = true;
                break;
            case 7:
                action[Mario.KEY_LEFT] = true;
                action[Mario.KEY_SPEED] = true;
                break;
            case 8:
                action[Mario.KEY_RIGHT] = true;
                action[Mario.KEY_SPEED] = true;
                break;
            case 9:
                action[Mario.KEY_JUMP] = true;
                action[Mario.KEY_SPEED] = true;
                break;
            case 10:
                action[Mario.KEY_LEFT] = true;
                action[Mario.KEY_JUMP] = true;
                action[Mario.KEY_SPEED] = true;
                break;
            case 11:
                action[Mario.KEY_RIGHT] = true;
                action[Mario.KEY_JUMP] = true;
                action[Mario.KEY_SPEED] = true;
                break;
        }
        
        
//        action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
        return action;
    }

    @Override
    public void reset() {
        for (int i = 0; i < action.length; ++i) {
            action[i] = false;
        }
        demonstration.clear();
    }

    public String getName() {
        return name;
    }

    public void setName(String Name) {
        this.name = Name;
    }
    
    public Demonstration getDemonstration(){
        return demonstration;
    }
    
    public void giveIntermediateReward(float reward) {
        
        int[] state = getState();
//        prevAction = new int[]{2,5,11}[RNG.randomInt(3)];
        
        
//(enemyy <= 0.47619) => class=5 (16.0/5.0)
//(enemyx >= 0.666667) and (enemyx <= 0.761905) and (enemyy <= 0.571429) => class=5 (38.0/18.0)
// => class=2 (801.0/505.0)
        
//        if((state[5]/21.0 <= 0.47619) || ((state[4]/21.0 >= 0.666667) && (state[4]/21.0 <= 0.761905) && (state[5]/21.0 <= 0.571429))){
//            prevAction = 5;
//        } else {
//            prevAction = 2;
//        }
        
//        if((state[5]/21.0 <= 0.47619)){
//            prevAction = 5;
//        } else if(((state[4]/21.0 >= 0.809524) && (state[1] == 1) && (state[4]/21.0 <= 0.904762)) || ((state[4]/21.0 >= 1) && (state[5]/21.0 <= 0.571429))){
//            prevAction = 2;
//        } else {
//            prevAction = 11;
//        }
//        (enemyy <= 0.47619) => class=5 (31.0/13.0)
//(enemyx >= 0.809524) and (ground = 1.0) and (enemyx <= 0.904762) => class=2 (20.0/9.0)
//(enemyx >= 1) and (enemyy <= 0.571429) => class=2 (9.0/2.0)
// => class=11 (508.0/335.0)
        
        state = state.clone();
        state[4] /= 21.0;
        state[5] /= 21.0;
        
//        if((state[4] <= 0.619048) && (state[4] >= 0.619048) && (state[5] >= 0.714286) && (state[5] <= 0.714286) && (state[2] == 0.0)){
//            prevAction = 7;
//        } else if((state[4] <= 0.619048) && (state[3] == 1.0) && (state[4] >= 0.619048) && (state[2] == 1.0) && (state[5] >= 0.666667) && (state[5] <= 0.666667) && (state[1] == 1.0)){
//            prevAction = 7;
//        } else if((state[4] <= 0.428571) && (state[3] == 1.0) && (state[5] >= 0.666667) && (state[5] <= 0.666667) && (state[4] >= 0.428571)){
//            prevAction = 7;
//        } else if((state[5] <= 0.571429) && (state[1] == 0.0) && (state[0] == 1.0) && (state[3] == 1.0) && (state[4] <= 0.238095) && (state[4] >= 0.190476)){
//            prevAction = 7;
//        } else if((state[5] <= 0.761905) && (state[2] == 0.0) && (state[5] >= 0.619048) && (state[3] == 0.0) && (state[0] == 0.0) && (state[4] >= 0.666667) && (state[4] <= 0.761905)){
//            prevAction = 7;
//        } else if((state[4] <= 0.619048) && (state[3] == 1.0) && (state[4] <= 0.238095) && (state[4] >= 0.238095) && (state[5] >= 0.666667) && (state[2] == 1.0)){
//            prevAction = 7;
//        } else if((state[4] <= 0.952381) && (state[4] >= 0.571429) && (state[4] <= 0.619048) && (state[5] >= 0.761905) && (state[2] == 1.0) && (state[5] <= 0.809524) && (state[4] >= 0.619048)){
//            prevAction = 10;
//        } else if((state[5] <= 0.619048) && (state[4] >= 0.571429) && (state[5] <= 0.47619) && (state[1] == 1.0) && (state[4] <= 0.761905) && (state[3] == 1.0)){
//            prevAction = 10;
//        } else if((state[4] <= 0.952381) && (state[4] >= 0.619048) && (state[4] <= 0.619048) && (state[2] == 1.0) && (state[5] >= 0.619048) && (state[5] <= 0.619048) && (state[1] == 1.0)){
//            prevAction = 10;
//        } else if((state[2] == 0.0) && (state[0] == 0.0) && (state[4] >= 0.761905) && (state[4] <= 0.857143)){
//            prevAction = 10;
//        } else if((state[4] <= 0.952381) && (state[4] >= 0.952381) && (state[3] == 0.0) && (state[1] == 1.0) && (state[5] >= 0.571429) && (state[5] <= 0.619048)){
//            prevAction = 9;
//        } else if((state[4] <= 0.47619) && (state[4] >= 0.285714) && (state[4] <= 0.380952) && (state[5] >= 0.714286) && (state[2] == 1.0) && (state[4] <= 0.333333)){
//            prevAction = 9;
//        } else if((state[4] <= 0.47619) && (state[4] >= 0.285714) && (state[4] <= 0.380952) && (state[4] <= 0.285714) && (state[5] <= 0.52381) && (state[3] == 1.0) && (state[1] == 1.0)){
//            prevAction = 9;
//        } else if((state[5] <= 0.571429) && (state[5] >= 0.571429) && (state[2] == 1.0) && (state[3] == 1.0) && (state[4] >= 0.47619) && (state[4] <= 0.47619)){
//            prevAction = 9;
//        } else if((state[0] == 0.0) && (state[5] >= 0.857143) && (state[3] == 1.0)){
//            prevAction = 4;
//        } else if((state[5] <= 0.761905) && (state[4] >= 0.666667) && (state[4] <= 0.666667) && (state[5] >= 0.761905) && (state[3] == 0.0)){
//            prevAction = 5;
//        } else if((state[5] <= 0.714286) && (state[4] >= 0.809524) && (state[3] == 1.0) && (state[5] <= 0.52381) && (state[5] >= 0.52381) && (state[2] == 1.0) && (state[1] == 1.0) && (state[4] <= 0.809524)){
//            prevAction = 5;
//        } else if((state[5] <= 0.714286) && (state[4] >= 0.809524) && (state[3] == 1.0) && (state[4] >= 0.857143) && (state[4] <= 0.904762) && (state[2] == 1.0) && (state[4] >= 0.904762)){
//            prevAction = 5;
//        } else if((state[4] <= 0.428571) && (state[2] == 1.0) && (state[4] >= 0.428571) && (state[5] >= 0.571429) && (state[3] == 0.0) && (state[5] <= 0.571429) && (state[0] == 1.0)){
//            prevAction = 5;
//        } else if((state[5] <= 0.761905) && (state[5] >= 0.619048) && (state[4] <= 0.333333) && (state[4] >= 0.238095) && (state[3] == 0.0) && (state[2] == 1.0)){
//            prevAction = 5;
//        } else if((state[5] <= 0.714286) && (state[4] >= 0.666667) && (state[5] >= 0.619048) && (state[4] >= 0.809524) && (state[4] <= 0.809524) && (state[3] == 0.0)){
//            prevAction = 5;
//        } else if((state[5] <= 0.714286) && (state[3] == 1.0) && (state[5] >= 0.714286) && (state[4] <= 0.238095)){
//            prevAction = 5;
//        } else if((state[5] <= 0.714286) && (state[3] == 1.0) && (state[4] >= 0.857143) && (state[4] >= 0.952381) && (state[2] == 0.0)){
//            prevAction = 5;
//        } else if((state[2] == 1.0) && (state[1] == 0.0) && (state[3] == 0.0) && (state[4] >= 0.761905) && (state[0] == 1.0) && (state[5] >= 0.761905)){
//            prevAction = 6;
//        } else if((state[4] <= 0.47619) && (state[4] >= 0.285714) && (state[5] >= 0.571429) && (state[4] >= 0.47619) && (state[5] <= 0.571429) && (state[2] == 1.0) && (state[1] == 1.0)){
//            prevAction = 6;
//        } else if((state[4] <= 0.47619) && (state[5] >= 0.666667) && (state[4] >= 0.428571) && (state[3] == 0.0) && (state[5] <= 0.666667)){
//            prevAction = 6;
//        } else if((state[4] <= 0.428571) && (state[4] >= 0.285714) && (state[5] >= 0.714286) && (state[5] <= 0.714286)){
//            prevAction = 6;
//        } else if((state[2] == 1.0) && (state[3] == 1.0) && (state[4] <= 0.285714) && (state[4] >= 0.285714) && (state[5] <= 0.571429) && (state[1] == 1.0)){
//            prevAction = 6;
//        } else if((state[4] >= 0.571429) && (state[5] <= 0.666667) && (state[5] >= 0.619048) && (state[1] == 1.0) && (state[3] == 0.0) && (state[5] <= 0.619048) && (state[4] <= 0.571429)){
//            prevAction = 6;
//        } else if((state[5] >= 0.666667) && (state[3] == 1.0) && (state[1] == 1.0) && (state[4] >= 0.714286) && (state[5] <= 0.666667) && (state[4] <= 0.714286)){
//            prevAction = 6;
//        } else if((state[4] <= 0.238095) && (state[5] <= 0.571429) && (state[4] >= 0.238095) && (state[2] == 1.0) && (state[1] == 0.0) && (state[0] == 1.0)){
//            prevAction = 3;
//        } else if((state[4] <= 0.571429) && (state[4] <= 0.142857) && (state[2] == 0.0) && (state[4] >= 0.095238) && (state[4] <= 0.095238) && (state[1] == 0.0)){
//            prevAction = 3;
//        } else if((state[3] == 1.0) && (state[1] == 1.0) && (state[5] <= 0.619048) && (state[4] <= 0.666667) && (state[2] == 1.0) && (state[5] >= 0.571429) && (state[5] <= 0.571429) && (state[4] >= 0.619048)){
//            prevAction = 3;
//        } else if((state[3] == 1.0) && (state[4] <= 0.52381) && (state[1] == 1.0) && (state[5] <= 0.619048) && (state[4] >= 0.47619) && (state[5] >= 0.571429)){
//            prevAction = 3;
//        } else if((state[3] == 1.0) && (state[1] == 1.0) && (state[4] <= 0.380952) && (state[5] <= 0.571429) && (state[2] == 1.0) && (state[4] >= 0.238095)){
//            prevAction = 3;
//        } else if((state[2] == 0.0) && (state[3] == 1.0) && (state[4] >= 0.761905) && (state[5] >= 0.809524)){
//            prevAction = 3;
//        } else if((state[5] >= 0.619048) && (state[4] <= 0.904762) && (state[0] == 0.0) && (state[2] == 0.0) && (state[3] == 0.0)){
//            prevAction = 3;
//        } else if((state[4] <= 0.190476) && (state[1] == 1.0) && (state[4] >= 0.142857) && (state[4] <= 0.142857) && (state[5] >= 0.666667) && (state[5] <= 0.666667)){
//            prevAction = 3;
//        } else if((state[4] <= 0.142857) && (state[1] == 1.0) && (state[4] >= 0.142857) && (state[2] == 0.0) && (state[5] <= 0.571429)){
//            prevAction = 3;
//        } else if((state[5] <= 0.761905) && (state[5] >= 0.714286) && (state[4] >= 0.714286) && (state[4] <= 0.714286)){
//            prevAction = 2;
//        } else if((state[5] <= 0.904762) && (state[5] >= 0.714286) && (state[2] == 0.0) && (state[4] >= 0.52381) && (state[4] <= 0.619048) && (state[5] >= 0.809524) && (state[5] <= 0.809524)){
//            prevAction = 2;
//        } else if((state[5] <= 0.761905) && (state[5] >= 0.714286) && (state[4] >= 0.857143) && (state[5] >= 0.761905)){
//            prevAction = 2;
//        } else if((state[5] <= 0.761905) && (state[4] >= 0.47619) && (state[4] <= 0.571429) && (state[3] == 0.0) && (state[4] <= 0.47619) && (state[5] <= 0.52381) && (state[5] >= 0.52381) && (state[0] == 1.0) && (state[2] == 1.0)){
//            prevAction = 2;
//        } else if((state[5] <= 0.904762) && (state[5] >= 0.666667) && (state[3] == 1.0) && (state[4] >= 0.52381) && (state[4] <= 0.714286) && (state[5] <= 0.761905) && (state[4] >= 0.666667) && (state[5] <= 0.666667) && (state[0] == 1.0)){
//            prevAction = 2;
//        } else if((state[1] == 0.0) && (state[4] >= 1) && (state[3] == 1.0)){
//            prevAction = 2;
//        } else if((state[5] <= 0.904762) && (state[4] >= 0.52381) && (state[4] <= 0.571429) && (state[4] >= 0.571429) && (state[3] == 0.0) && (state[2] == 1.0) && (state[5] <= 0.666667) && (state[1] == 1.0)){
//            prevAction = 2;
//        } else if((state[5] <= 0.904762) && (state[5] >= 0.714286) && (state[3] == 1.0) && (state[5] <= 0.714286) && (state[4] <= 0.52381)){
//            prevAction = 2;
//        } else if((state[5] <= 0.904762) && (state[2] == 0.0) && (state[4] >= 0.714286) && (state[5] >= 0.857143) && (state[4] >= 0.761905)){
//            prevAction = 2;
//        } else if((state[5] <= 0.761905) && (state[5] >= 0.714286) && (state[3] == 1.0) && (state[4] >= 0.952381)){
//            prevAction = 2;
//        } else if((state[2] == 0.0) && (state[4] >= 1) && (state[1] == 1.0)){
//            prevAction = 11;
//        } else if((state[5] <= 0.761905) && (state[4] >= 0.571429) && (state[3] == 0.0) && (state[4] >= 0.761905) && (state[1] == 1.0) && (state[5] >= 0.666667) && (state[4] >= 0.904762)){
//            prevAction = 11;
//        } else if((state[5] <= 0.666667) && (state[4] >= 0.52381) && (state[0] == 0.0) && (state[4] >= 0.761905) && (state[4] <= 0.857143) && (state[4] >= 0.809524)){
//            prevAction = 11;
//        } else if((state[5] <= 0.857143) && (state[2] == 0.0) && (state[5] >= 0.571429) && (state[5] <= 0.619048) && (state[1] == 0.0) && (state[4] <= 0.238095) && (state[3] == 0.0)){
//            prevAction = 11;
//        } else if((state[5] <= 0.857143) && (state[4] >= 0.52381) && (state[4] <= 0.619048) && (state[2] == 0.0) && (state[1] == 1.0) && (state[5] >= 0.619048) && (state[5] <= 0.619048)){
//            prevAction = 11;
//        } else if((state[5] <= 0.857143) && (state[4] >= 0.52381) && (state[4] <= 0.619048) && (state[2] == 0.0) && (state[1] == 1.0) && (state[4] >= 0.571429) && (state[5] >= 0.666667) && (state[3] == 1.0) && (state[5] <= 0.714286)){
//            prevAction = 11;
//        } else if((state[5] <= 0.857143) && (state[5] <= 0.52381) && (state[4] >= 0.52381) && (state[3] == 0.0) && (state[4] >= 0.761905) && (state[1] == 1.0) && (state[5] >= 0.52381) && (state[4] <= 0.809524)){
//            prevAction = 11;
//        } else {
//            prevAction = 8;
//        }
        
        if((state[3] == 1.0) && (state[4] >= 0.47619) && (state[4] <= 0.619048) && (state[5] >= 0.809524) && (state[4] <= 0.47619)){
            prevAction = 9;
        } else if((state[3] == 1.0) && (state[4] <= 0.428571)){
            prevAction = 11;
        } else {
            prevAction = 8;
        }

        
        demonstration.record(state, prevAction);
    }
    
    
    @Override
    public void endEpisode() {
        
    }
    
//    protected int[] tileCoding(double[] state, int action) {
//        int[] extra = new int[]{action, isMarioAbleToShoot ? 0 : 1, facing+1, isMarioOnGround ? 0 : 1};
//        return TileCoding.GetTiles(nrTilings, state, maxNrTiles, extra);
//    }

    
}
