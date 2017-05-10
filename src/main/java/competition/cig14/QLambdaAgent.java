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
package competition.cig14;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import competition.cig14.cmac.CMAC;
import competition.cig14.cmac.TileCoding;
import java.util.*;
import util.RNG;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: Sep 1, 2009 Time: 3:12:07 PM Package: competition.cig.sergeykarakovskiy
 */
public class QLambdaAgent extends BasicMarioAIAgent implements Agent {

    private CMAC cmac;
    protected int prevAction;
    protected int[] prevFa;
    
    private int nrTilings;
    private int maxNrTiles;
    
    private double alpha;
    private double gamma;
    private double lambda;
    private double epsilon;
    
    private double lastReward;
    private float prevX;
    private float prevY;
    private int prevMarioMode;
    
    public QLambdaAgent() {
        super("QLambda");
        
        nrTilings = 32;
        maxNrTiles = 100000;
        
        alpha = 0.01/nrTilings;
        gamma = 1.0;
        lambda = 0.9;
        epsilon = 0.1;
        
        cmac = new CMAC(maxNrTiles, alpha, gamma, lambda, 0.0);
        
        prevAction = 0;
        prevFa = tileCoding(new double[getStateSize()], prevAction);
        prevX = 0f;
        prevY = 0f;
        prevMarioMode = 0;
        
        lastReward = -1;
        
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
    
    public double[] twoClosestEnemies(){
        Integer[] sorted = sortEnemies();
        
        double[] enemies = new double[4];
        if(sorted.length == 0){
            enemies[0] = 200;
            enemies[1] = 0;
            enemies[2] = 200;
            enemies[3] = 0;
        } else if(sorted.length == 1){
            enemies[0] = enemiesFloatPos[sorted[0]+1];
            enemies[1] = enemiesFloatPos[sorted[0]+2];
            enemies[2] = 200;
            enemies[3] = 0;
        } else{
            enemies[0] = enemiesFloatPos[sorted[0]+1];
            enemies[1] = enemiesFloatPos[sorted[0]+2];
            enemies[2] = enemiesFloatPos[sorted[1]+1];
            enemies[3] = enemiesFloatPos[sorted[1]+2];
        }
        return enemies;
    }
    
    public double[] closestEnemy(){
        Integer[] sorted = sortEnemies();
        
        double[] enemy = new double[2];
        if(sorted.length == 0){
            enemy[0] = -200;
            enemy[1] = 0;
        } else {
            enemy[0] = enemiesFloatPos[sorted[0]+1];
            enemy[1] = enemiesFloatPos[sorted[0]+2];
        }
        return enemy;
    }
    
    public double[] closestCoin(){
        int cx = 200;
        int cy = 200;
        double[] coin = new double[2];
        double distance = Double.MAX_VALUE;
        for(int x=0; x<levelScene.length; x++){
            for(int y=0; y<levelScene[x].length; y++){
                if(levelScene[x][y] == GeneralizerLevelScene.COIN_ANIM){
                    if(marioEgoPos[0] - x + marioEgoPos[1] - y < distance){
                        distance = marioEgoPos[0] - x + marioEgoPos[1] - y;
                        cx = marioEgoPos[0] - x;
                        cy = marioEgoPos[1] - y;
                    }
                }
            }
        }
        
        if(cx == -1){
            coin[0] = 200;
            coin[1] = 0;
        } else {
            coin[0] = cx;
            coin[1] = cy;
        }
        return coin;
    }
    
    public int getObstacleHeight(){
        int height = 0;
        for(int x=marioEgoPos[0]; x>0; x--){
            if(!(levelScene[x][marioEgoPos[1]+1] == GeneralizerLevelScene.FLOWER_POT_OR_CANNON
                    || levelScene[x][marioEgoPos[1]+1] == GeneralizerLevelScene.BORDER_CANNOT_PASS_THROUGH 
                    || levelScene[x][marioEgoPos[1]+1] == GeneralizerLevelScene.BRICK
                    || levelScene[x][marioEgoPos[1]+1] == 1)){
                break;
            }
            height++;
        }
        return height;
    }
    
    private int getStateSize(){
        return 4;
    }
    
    public double[] getState(){
        double[] state = new double[6];
        
        // CLOSEST TWO ENEMIES
        double[] enemy = closestEnemy();
        state[0] = enemy[0] == 0 ? 0 : Math.signum(enemy[0])*Math.log(Math.abs(enemy[0]));
        state[1] = enemy[1] == 0 ? 0 : Math.signum(enemy[1])*Math.log(Math.abs(enemy[1]));
//        state[2] = enemy[2] == 0 ? 0 : Math.signum(enemy[2])*Math.log(Math.abs(enemy[2]));
//        state[3] = enemy[3] == 0 ? 0 : Math.signum(enemy[3])*Math.log(Math.abs(enemy[3]));
        
        //HEIGHT OF WALL IN FRONT
        state[2] = getObstacleHeight()/3.0;
        
        state[3] = Math.log(time);
        
        return state;
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
    }

    public String getName() {
        return name;
    }

    public void setName(String Name) {
        this.name = Name;
    }

    public static int type = 3;
    
    public void giveIntermediateReward(float reward) {
        
        double[] state = getState();
        
        double[] enemy = closestEnemy();
        
        double delta = reward - lastReward;
        if(type == 1 || type == 3){
            delta += 100*(gamma * marioFloatPos[0] - prevX);
//            System.out.println(marioFloatPos[0] - prevX);
        }// + 100*(gamma * -marioFloatPos[1] - prevY);//  + 100*(gamma*-time - prevTime) 
        if(type == 2 || type == 3){
            delta += 100*(gamma * -marioFloatPos[1] - prevY);
//            delta += 0.1*(gamma * (Math.pow(enemy[0], 2.0) + Math.pow(enemy[1], 2.0)) - prevDistToEnemy);
//            delta += 10*(gamma * Math.abs(enemy[1]) - prevDistToEnemy);
//            System.out.println(Math.abs(enemy[1]) - prevDistToEnemy);
        }
//        delta += 100*(gamma*marioMode - prevMarioMode);
        if(type == 4){
            double a = (reward - lastReward + 100*(gamma * marioFloatPos[0] - prevX));
            double b = (reward - lastReward + 100*(gamma * -marioFloatPos[1] - prevY));
            delta =  Math.abs(a-(-100))*Math.abs(b-(-100));
        }
//        System.out.println();
        delta -= cmac.getValue(prevFa);
        
        lastReward = reward;
        prevX = marioFloatPos[0];
        prevY = -marioFloatPos[1];
        prevMarioMode = marioMode;
        
        int[][] Fas = new int[getNumActions()][];
        for(int i=0; i<getNumActions(); i++){
            Fas[i] = tileCoding(state, i);
        }
        double Qs[] = new double[getNumActions()];
        double best = -Double.MAX_VALUE;
        
        for(int i=0; i<getNumActions(); i++){
            Qs[i] = cmac.getValue(Fas[i]);
            if(Qs[i] > best){
                best = Qs[i];
            }
        }
        
        cmac.update(delta + gamma*best);
        
        int a = 0;
        if (RNG.randomDouble() > epsilon) {
            Qs = new double[getNumActions()];

            //each tile separately
            for(int i=0; i<getNumActions(); i++){
                Qs[i] = cmac.getValue(Fas[i]);
            }
            a = actionSelection(Qs);
            
            cmac.decay();
        } else {
            a = RNG.randomInt(getNumActions());
            cmac.resetEs();
        }
        
        prevFa = tileCoding(state, a);
        prevAction = a;
        
        cmac.setTraces(prevFa);
    }
    
    protected int actionSelection(double[] Qs){
        double best = -Double.MAX_VALUE;
        ArrayList<Integer> ibest = new ArrayList <Integer>();
        
        for(int i=0; i<Qs.length; i++){
            if(Qs[i] > 1e4){
//                System.out.println(Qs[0][i]);
            }
            if(Qs[i] >= best){
                if(Qs[i] > best){
                    ibest.clear();
                }
                ibest.add(i);
                best = Qs[i];
            }
        }
        
        int b = ibest.get(RNG.randomInt(ibest.size()));
        return b;
    }
    

    @Override
    public void endEpisode() {
        cmac.resetEs();
    }
    
    protected int[] tileCoding(double[] state, int action) {
        int[] extra = new int[]{action, isMarioAbleToShoot ? 0 : 1, facing+1, isMarioOnGround ? 0 : 1};
        return TileCoding.GetTiles(nrTilings, state, maxNrTiles, extra);
    }
}
