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
import util.*;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: Sep 1, 2009 Time: 3:12:07 PM Package: competition.cig.sergeykarakovskiy
 */
public class QLambdaAgent extends BasicMarioAIAgent implements Agent {
    
    private QLHash cmac;
    
    protected int prevAction;
    protected int[] prevFullState;
    protected int prevState;
    
    protected double prevPotential;
    protected double potential;
    
    private int maxNrTiles;
    
    private double alpha;
    private double gamma;
    private double lambda;
    private double epsilon;
    
    private static double scaling = 1.0;
    private static double sigma = 0.5;
    
    private double lastReward;
    
    private AgentType type;
    private Demonstration demonstration;
    private Demonstration recording;
    
    public QLambdaAgent(AgentType type, Demonstration recording, Demonstration demonstration) {
        super("QLambda");
        
        maxNrTiles = 93000;
        
        alpha = 0.1;
        gamma = 0.9;
        lambda = 0.5;
        epsilon = 0.05;
        
        this.type = type;
        
        cmac = new QLHash(maxNrTiles, alpha, gamma, lambda, 0.0);
        
        prevAction = 0;
        prevFullState = new int[]{0,0,0,0,11,11};
        prevState = 0;
        
        prevPotential = 0.0;
        potential = 0.0;
        
        lastReward = -1;
        this.demonstration = demonstration;
        this.recording = recording;
        reset();
        initializeQs();
    }
    
    private void initializeQs(){
        
        for(int i=0; i<2*2*2*2*22*22*12; i++){
            int[] s = new int[6];
            s[0] = i%2;
            s[1] = (i/2)%2;
            s[2] = (i/(2*2)%2);
            s[3] = (i/(2*2*2)%2);
            s[4] = (i/(2*2*2*2)%22);
            s[5] = (i/(2*2*2*2*22)%22);
            int a = (i/(2*2*2*2*22*22)%12);
            cmac.setValue(i, scaling*Math.pow(Math.E, -Math.pow(demonstration.getDistanceClosest(s, a), 2.0)/(2*sigma*sigma)));
        }
    }
    
    public void setEpsilon(double eps){
        this.epsilon = eps;
    }
    public void setAlpha(double eps){
        this.alpha = eps;
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
        recording.clear();
    }

    public String getName() {
        return name;
    }

    public void setName(String Name) {
        this.name = Name;
    }
    
//    protected double shaping(int i, int[] s1, int a1, int[] s2, int a2){
//        if(a2 == -1){
//            potential = 0.0;
//        } else {
//            potential = scaling*Math.pow(Math.E, -Math.pow(demonstration.getDistanceClosest(s2, a2), 2.0)/(2*sigma*sigma));
//        }
//        double diff = gamma*potential-prevPotential;
//        prevPotential = potential;
//        return diff;
//    }
    
    public void giveIntermediateReward(float reward) {
        
        int[] state = getState();
        
        double deltaR = reward - lastReward;
        deltaR -= cmac.getValue(prevState);
        
        lastReward = reward;
        
        double QsR[] = new double[getNumActions()];
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
        if (RNG.randomDouble() > epsilon) {
            QsR = new double[getNumActions()];

            for(int i=0; i<getNumActions(); i++){
                QsR[i] = cmac.getValue(calcState(state, i));
            }
            a = actionSelection(QsR);
        } else {
            a = RNG.randomInt(getNumActions());
        }

        recording.record(state, a);
//        cmac.update(deltaR + shaping(1, prevFullState, prevAction, state, a) + gamma*bestR);
        cmac.update(deltaR + gamma*bestR);
        
        if(a == bestRi){
            cmac.decay();
        } else {
            cmac.resetEs();
        }
        
        prevFullState = state.clone();
        prevState = calcState(state, a);
        prevAction = a;
        
        cmac.setTraces(prevState);
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
    
    public Demonstration getDemonstration(){
        return recording;
    }
    
    @Override
    public void endEpisode() {
        
        int[] state = getState();
        
        double deltaR = 0.0;
        deltaR -= cmac.getValue(prevState);
        
//        cmac.update(deltaR + shaping(1, prevFullState, prevAction, state, -1));
        cmac.update(deltaR);
        
        cmac.resetEs();
    }
    
//    protected int[] tileCoding(double[] state, int action) {
//        int[] extra = new int[]{action, isMarioAbleToShoot ? 0 : 1, facing+1, isMarioOnGround ? 0 : 1};
//        return TileCoding.GetTiles(nrTilings, state, maxNrTiles, extra);
//    }

}
