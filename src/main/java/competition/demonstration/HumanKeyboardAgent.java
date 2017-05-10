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
public class HumanKeyboardAgent extends BasicMarioAIAgent implements Agent {
    
    protected double prevPotential;
    protected double potential;
    
    private AgentType type;
    private Demonstration demonstration;
    
    public HumanKeyboardAgent() {
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
    
    private int reverseEngineerAction(){
        if(action[Mario.KEY_LEFT]){
            //left key
            if(action[Mario.KEY_JUMP]){
                //left key and jump
                if(action[Mario.KEY_SPEED]){
                    //left key, jump and speed
                    return 10;
                } else {
                    //left key and jump
                    return 4;
                }
            } else {
                //left key and no jump
                if(action[Mario.KEY_SPEED]){
                    //left key and speed
                    return 7;
                } else {
                    //left key
                    return 1;
                }
            }
        } else {
            if(action[Mario.KEY_RIGHT]){
                // right key
                if(action[Mario.KEY_JUMP]){
                    //right key and jump
                    if(action[Mario.KEY_SPEED]){
                        //right key, jump and speed
                        return 11;
                    } else {
                        //right key and jump
                        return 5;
                    }
                } else {
                    //right key and no jump
                    if(action[Mario.KEY_SPEED]){
                        //right key and speed
                        return 8;
                    } else {
                        //right key
                        return 2;
                    }
                }
            } else {
                // no direction
                if(action[Mario.KEY_JUMP]){
                    //jump
                    if(action[Mario.KEY_SPEED]){
                        //jump and speed
                        return 9;
                    } else {
                        //jump
                        return 3;
                    }
                } else {
                    //no direction and no jump
                    if(action[Mario.KEY_SPEED]){
                        //speed
                        return 6;
                    } else {
                        //nothing
                        return 0;
                    }
                }
            }
        }
    }
    
    public Demonstration getDemonstration(){
        return demonstration;
    }
    
    public void giveIntermediateReward(float reward) {
        
        int[] state = getState();
        int a = reverseEngineerAction();
        demonstration.record(state, a);
    }
    
    
    @Override
    public void endEpisode() {
        
    }
    
//    protected int[] tileCoding(double[] state, int action) {
//        int[] extra = new int[]{action, isMarioAbleToShoot ? 0 : 1, facing+1, isMarioOnGround ? 0 : 1};
//        return TileCoding.GetTiles(nrTilings, state, maxNrTiles, extra);
//    }

    
public void keyPressed(KeyEvent e)
{
    toggleKey(e.getKeyCode(), true);
}

public void keyReleased(KeyEvent e)
{
    toggleKey(e.getKeyCode(), false);
}


private void toggleKey(int keyCode, boolean isPressed)
{
    switch (keyCode)
    {
        case KeyEvent.VK_LEFT:
            action[Mario.KEY_LEFT] = isPressed;
            break;
        case KeyEvent.VK_RIGHT:
            action[Mario.KEY_RIGHT] = isPressed;
            break;
        case KeyEvent.VK_DOWN:
            action[Mario.KEY_DOWN] = isPressed;
            break;
        case KeyEvent.VK_UP:
            action[Mario.KEY_UP] = isPressed;
            break;

        case KeyEvent.VK_S:
            action[Mario.KEY_JUMP] = isPressed;
            break;
        case KeyEvent.VK_A:
            action[Mario.KEY_SPEED] = isPressed;
            break;
    }
}
    
}
