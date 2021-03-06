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
package competition.transfer;

import competition.transfer.*;
import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import java.awt.event.KeyEvent;
import java.util.*;
import util.RNG;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: Sep 1, 2009 Time: 3:12:07 PM Package: competition.cig.sergeykarakovskiy
 */
public class DSPolicyReuseQLambdaAgent extends BasicMarioAIAgent implements Agent {

    private QLHash cmac;
    private competition.fa.QLambdaAgent transferred;
    protected int prevAction;
    protected int[] prevState;
    protected double[] prevStateSource;
    
    protected double prevPotential;
    protected double potential;
    protected boolean adviceGiven;
    
    private int maxNrTiles;
    
    private double alpha;
    private double gamma;
    private double lambda;
    private double epsilon;
    private double phi;
    private double phiDecay;
    
    private double lastReward;
    private AgentType type;
    
    public static int nrAdvices = 0;
    public static int totalSteps = 0;
    
    public DSPolicyReuseQLambdaAgent(AgentType type, competition.fa.QLambdaAgent transferred) {
        super("QLambda");
        
        this.type = type;
        
        maxNrTiles = 120000;
        
        alpha = 0.01;
        gamma = 0.9;
        lambda = 0.5;
        epsilon = 0.05;
        phi = 1.0;
        phiDecay = 0.95;
        
        cmac = new QLHash(maxNrTiles, alpha, gamma, lambda, 0.0);
        this.transferred = transferred;
        
        prevAction = 0;
        prevState = new int[]{0,0,0,0,24,24};
        
        prevPotential = 0.0;
        potential = 0.0;
        
        lastReward = -1;
        
        adviceGiven = false;
        
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
            state[4] = enemy[0]+11;
            state[5] = enemy[1]+11;
        } else {
            state[4] = 24;
            state[5] = 24;
        }
        
        return state; //2*2*2*2*13*13*12
    }
    
    public int calcState(int[] state, int action){
        int hashedState = state[0];
        hashedState += 2*state[1];
        hashedState += 2*2*state[2];
        hashedState += 2*2*2*state[3];
        hashedState += 2*2*2*2*state[4];
        hashedState += 2*2*2*2*25*state[5];
        hashedState += 2*2*2*2*25*25*action;
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
    }

    public String getName() {
        return name;
    }

    public void setName(String Name) {
        this.name = Name;
    }
    
    protected int[] mapState(int[] state){
        int[] mappedState = new int[state.length];
        for(int i=0; i<4; i++){
            mappedState[i] = state[i];
        }
        mappedState[4] = mappedState[5] = 24;
        return mappedState;
    }
    
    public void giveIntermediateReward(float reward) {
        
        int[] state = getState();
        
        double deltaR = reward - lastReward;
        deltaR -= cmac.getValue(calcState(prevState, prevAction));
        
        lastReward = reward;
        
        double QsR[] = new double[getNumActions()];
        double QsS[] = new double[getNumActions()];
        double bestR = -Double.MAX_VALUE;
        int bestRi = -1;
        for(int i=0; i<getNumActions(); i++){
            QsR[i] = cmac.getValue(calcState(state, i));
            if(QsR[i] > bestR){
                bestR = QsR[i];
                bestRi = i;
            }
        }
        
        int a;
        if(RNG.randomDouble() < phi){
            QsR = new double[getNumActions()];
            double[] sourceState = calcSourceState();

            for(int i=0; i<getNumActions(); i++){
                QsR[i] = transferred.getValue(0, sourceState, i);
            }
            a = actionSelection(QsR);
        } else if (RNG.randomDouble() > epsilon) {
            QsR = new double[getNumActions()];

            for(int i=0; i<getNumActions(); i++){
                QsR[i] = cmac.getValue(calcState(state, i));
            }
            a = actionSelection(QsR);
        } else {
            a = RNG.randomInt(getNumActions());
        }

        cmac.update(deltaR + gamma*bestR);
        
        if(a == bestRi){
            cmac.decay();
        } else {
            cmac.resetEs();
        }
        
        prevState = state;
        prevAction = a;
        
        cmac.setTraces(calcState(state, a));
        
        if(adviceGiven) {
            nrAdvices++;
        }
        totalSteps++;
        
        adviceGiven = false;
    }
    
    protected double[] calcSourceState(){
        
        double[] state = new double[4];
        // CLOSEST TWO ENEMIES
        double[] enemy = closestEnemy2();
        state[0] = enemy[0] == 0 ? 0 : Math.signum(enemy[0])*Math.log(Math.abs(enemy[0]));
        state[1] = enemy[1] == 0 ? 0 : Math.signum(enemy[1])*Math.log(Math.abs(enemy[1]));
        //HEIGHT OF WALL IN FRONT
        state[2] = getObstacleHeight()/3.0;
        //TIME
        state[3] = Math.log(time);
        
        return state;
    }
    
    public double[] closestEnemy2(){
        Integer[] sorted = sortEnemies();
        
        double[] enemy = new double[2];
        if(sorted.length == 0){
            enemy[0] = 200;
            enemy[1] = 0;
        } else {
            enemy[0] = enemiesFloatPos[sorted[0]+1];
            enemy[1] = enemiesFloatPos[sorted[0]+2];
        }
        return enemy;
    }
    
    public int getObstacleHeight(){
//        System.out.println(levelScene[marioEgoPos[0]][marioEgoPos[1]+1]);
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
    
    private void offpolicyAction(int i){
        cmac.resetEs();
    }
    
    @Override
    public void endEpisode() {
        
        int[] state = getState();
        
        double deltaR = 0.0;
        deltaR -= cmac.getValue(calcState(prevState, prevAction));
        
        cmac.update(deltaR);
        
        cmac.resetEs();
        phi *= phiDecay;
    }
    
//    protected int[] tileCoding(double[] state, int action) {
//        int[] extra = new int[]{action, isMarioAbleToShoot ? 0 : 1, facing+1, isMarioOnGround ? 0 : 1};
//        return TileCoding.GetTiles(nrTilings, state, maxNrTiles, extra);
//    }

    @Override
    public void keyPressed(KeyEvent e) {
        potential = (potential+1)/gamma;
        adviceGiven = true;
//        System.out.println(potential);
    }

}
