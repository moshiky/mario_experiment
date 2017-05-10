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
package competition.richmario.agents;

import competition.richmario.demonstrations.Demonstration;
import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import competition.richmario.StateAction;
import java.awt.event.KeyEvent;
import java.util.*;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: Sep 1, 2009 Time: 3:12:07 PM Package: competition.cig.sergeykarakovskiy
 */
public class HumanKeyboardAgent extends BasicMarioAIAgent implements Agent {
    
    protected double prevPotential;
    protected double potential;
    
    protected float[] prevMarioPos;
    
    private Demonstration demonstration;
    
    public HumanKeyboardAgent() {
        super("QLambda");
        
        this.demonstration = new Demonstration(-1);
        prevMarioPos = new float[]{32.0f,32.0f};
        
        reset();
    }
    
    public int getNumActions(){
        return 12;
    }
    
    /*
     * State
     * 0 can jump? 0-1
     * 1 on ground? 0-1
     * 2 able to shoot? 0-1
     * 3 current direction 0-8
     * 4 close enemies yes or no in 8 directions 0-255
     * 5 midrange enemies yes or no in 8 directions 0-255
     * 6 far enemies yes or no in 8 directions 0-255
     * 7 obstacles in front 0-15
     * 8 closest enemy x 0-21
     * 9 closest enemy y 0-21
     * 
     */
    public StateAction getState(){
        int[] state = new int[10];
        
        // CLOSEST TWO ENEMIES
        state[0] = isMarioAbleToJump ? 0 : 1;
        state[1] = isMarioOnGround ? 0 : 1;
        state[2] = isMarioAbleToShoot ? 0 : 1;//marioMode;//
        float xdiff = marioFloatPos[0] - prevMarioPos[0];
        float ydiff = marioFloatPos[1] - prevMarioPos[1];
        state[3] = xdiff < 0 ? 0 : (xdiff == 0 ? 1 : 2);
        state[3] += 3*(ydiff < 0 ? 0 : (ydiff == 0 ? 1 : 2));
        
        state[4] = enemies(1, 0);
        state[5] = enemies(3, 1);
        state[6] = 0;//enemies(5, 3);
        
        state[7] = obstacle();
        
        int[] enemy = closestEnemy();
        if(Math.abs(enemy[0]) < 11 && Math.abs(enemy[1]) < 11){
            state[8] = enemy[0]+10;
            state[9] = enemy[1]+10;
        } else {
            state[8] = 21;
            state[9] = 21;
        }
        
//        state[10] = gap();
        
//        System.out.println(isMarioAbleToShoot);
//        state[10] = isMarioAbleToShoot ? 0 : 1;
        
        float[] extraState = new float[2];
        extraState[0] = marioFloatPos[0];
        extraState[1] = marioFloatPos[1];
        
        return new StateAction(state, extraState, -1); //2*2*2*2*13*13*12
    }
    
    protected boolean hasGap(int y){
        for(int x=marioEgoPos[0]; x<levelScene.length; x++){
            if(levelScene[x][y] == GeneralizerLevelScene.FLOWER_POT_OR_CANNON
                    || levelScene[x][y] == GeneralizerLevelScene.BORDER_CANNOT_PASS_THROUGH 
                    || levelScene[x][y] == GeneralizerLevelScene.BRICK
                    || levelScene[x][y] == 1){
                return false;
            }
        }
        return true;
    }
    
    protected int gap(){
        int gap = hasGap(marioEgoPos[1]) ? 1 : 0;
        gap += hasGap(marioEgoPos[1]+1) ? 2 : 0;
        return gap;
    }
    
    protected int obstacle(){
        int obstacle = 0;
        int obst;
        for(int i=0; i<4; i++){
            obst = getReceptiveFieldCellValue(marioEgoPos[0]-i, marioEgoPos[1]+1);
            if(obst < 0){
                obstacle += Math.pow(2, i);
            }
        }
        return obstacle;
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
    
    protected int enemies(int out, int in){
        boolean[] ens = new boolean[8];
        int total = 0;
        int dir;
        int enemy;
        int extra = 0;
        if(marioMode > 0){
            extra = -1;
        }
        for(int i=-out+extra; i<out+1; i++){
            for(int j=-out; j<out+1; j++){
                if(!(i >= -in && i <= in && j >= -in && j <= in)){
                    enemy = getEnemiesCellValue(marioEgoPos[0]+i, marioEgoPos[1]+j);
                    if((enemy >= 80 && enemy < 100) || enemy == 13){
                        dir = i < 0 ? 0 : (i == 0 ? 2 : 1);
                        dir += 3*(j < 0 ? 0 : (j == 0 ? 2 : 1));
                        ens[dir] = true;
                    }
                }
            }
        }
        for(int i=0; i<ens.length; i++){
            if(ens[i]){
                total += Math.pow(2, i);
            }
        }
        return total;
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
        
        StateAction state = getState();
        state.setAction(reverseEngineerAction());
        demonstration.record(state);
        prevMarioPos = marioFloatPos.clone();
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
