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
package competition.aamas15;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import competition.aamas15.cmac.CMAC;
import competition.aamas15.cmac.TileCoding;
import java.util.*;
import org.apache.commons.math3.stat.inference.TTest;
import util.RNG;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: Sep 1, 2009 Time: 3:12:07 PM Package: competition.cig.sergeykarakovskiy
 */
public class QLambdaAgent2 extends BasicMarioAIAgent implements Agent {

    private QLHash[] cmac;
    protected int prevAction;
    protected int prevState;
    
    private int maxNrTiles;
    
    private double alpha;
    private double beta;
    private double gamma;
    private double lambda;
    private double epsilon;
    
    private double lastReward;
    private float prevX;
    private float prevY;
    private int prevFacing;
    private int prevMarioMode;
    private int prevNrEnemies;
    private int prevTime;
    private int prevMarioOnGround;
    private double prevDistanceToEnemy;
    
    private int[] shapings;
    private AgentType type;
    
    public QLambdaAgent2(int[] shapings, AgentType type) {
        super("QLambda");
        
        this.shapings = shapings;
        this.type = type;
        
        maxNrTiles = 120000;
        
        alpha = 0.05;
        beta = alpha;
        gamma = 1.0;
        lambda = 0.9;
        epsilon = 0.1;
        
        cmac = new QLHash[shapings.length];
        for(int i=0; i<cmac.length; i++){
            cmac[i] = new QLHash(maxNrTiles, alpha, beta, gamma, lambda, 0.0);
        }
        
        prevAction = 0;
        prevState = 0;
        prevX = 0f;
        prevY = 0f;
        prevFacing = 0;
        prevNrEnemies = 0;
        prevTime = 0;
        prevMarioOnGround = 0;
        prevDistanceToEnemy = 0;
        
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
    
    protected double shaping(int i){
        switch(i){
            case 1: return 100*(gamma * marioFloatPos[0] - prevX);
            case 2: return 100*(gamma * -marioFloatPos[1] - prevY);
            case 3: return 100*(gamma * -marioFloatPos[0] + prevX);
            case 4: return 100*(gamma * marioFloatPos[1] + prevY);
            case 5: return (gamma*facing - prevFacing);
            case 6: return (gamma*marioMode - prevMarioMode);
//            case 7: return (gamma* - );
            case 7: return (gamma*-enemiesFloatPos.length/3 - prevNrEnemies);
        }
        return 0.0;
    }
    
    public void giveIntermediateReward(float reward) {
        
        int[] state = getState();
        
        double[] delta = new double[cmac.length];
        for(int i=0; i<delta.length; i++){
            if(type == AgentType.Supershaping){
                delta[i] = reward - lastReward;
                double shape = 0.0;
                for(int j=0; j<shapings.length; j++){
                    shape += shaping(shapings[j]);
                }
                delta[i] += shape/shapings.length;
            } else {
                delta[i] = reward - lastReward + shaping(shapings[i]);
            }
            delta[i] -= cmac[i].getValue(prevState);
        }
        
        lastReward = reward;
        prevX = marioFloatPos[0];
        prevY = -marioFloatPos[1];
        prevFacing = facing;
        prevMarioMode = marioMode;
        prevNrEnemies = -enemiesFloatPos.length/3;
        prevTime = time;
        prevMarioOnGround = isMarioOnGround ? 0 : 1;
        int[] enemy = closestEnemy();
        prevDistanceToEnemy = -(Math.sqrt(Math.pow(enemy[0],2.0)+Math.pow(enemy[1], 2.0)));
        
        for(int j=0; j<cmac.length; j++){
            double Qs[] = new double[getNumActions()];
            double best = -Double.MAX_VALUE;
            for(int i=0; i<getNumActions(); i++){
                Qs[i] = cmac[j].getValue(calcState(state, i));
                if(Qs[i] > best){
                    best = Qs[i];
                }
            }

            cmac[j].update(delta[j] + gamma*best);
            cmac[j].updateVariance(prevState, delta[j] + gamma*best);
        }
        
        int a = 0;
        if (RNG.randomDouble() > epsilon) {
            double[][] Qs = new double[cmac.length][getNumActions()];
            double[][] variance = new double[cmac.length][getNumActions()];

            for(int j=0; j<cmac.length; j++){
                //each tile separately
                for(int i=0; i<getNumActions(); i++){
                    Qs[j][i] = cmac[j].getValue(calcState(state, i));
                    variance[j][i] = cmac[j].getVariance(calcState(state, i));
                }
            }
            switch(type){
                case NoShaping: 
                case SingleShaping:
                case Supershaping: a = actionSelection(Qs[0]);
                    break;
                case Linear: a = linearActionSelection(Qs);
                    break;
                case Ranking: a = rankingActionSelection(Qs);
                    break;
                case AOS: a = adaptiveObjectiveSelection(Qs, variance);
                    break;
            }
            
            for(int i=0; i<cmac.length; i++){
                cmac[i].decay();
            }
        } else {
            a = RNG.randomInt(getNumActions());
            for(int i=0; i<cmac.length; i++){
                cmac[i].resetEs();
            }
        }
        
        prevState = calcState(state, a);
        prevAction = a;
        
        for(int i=0; i<cmac.length; i++){
            cmac[i].setTraces(prevState);
        }
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
    
    protected int linearActionSelection(double[][] Qs){
        double best = -Double.MAX_VALUE;
        double[] bests = new double[Qs.length];
        for(int i=0; i<bests.length; i++){
            bests[i] = -Double.MAX_VALUE;
        }
        
        ArrayList<Integer> ibest = new ArrayList <Integer>();
        
        for(int i=0; i<Qs[0].length; i++){
            double tmp = 0.0;
            for(int j=0; j<Qs.length; j++){
                tmp += Qs[j][i];
                if(Qs[j][i] > bests[j]){
                    bests[j] = Qs[j][i];
                }
            }
            
            if(tmp >= best){
                if(tmp > best){
                    ibest.clear();
                }
                ibest.add(i);
                best = tmp;
            }
        }
        
        int b = ibest.get(RNG.randomInt(ibest.size()));
        
        for(int o=0; o<cmac.length; o++){
            if(Qs[o][b] != bests[o]){
                offpolicyAction(o);
            }
        }
        
        return b;
    }
    
    //Returns the greedy action wrt the objective that yields the highest 
    //confidence in the current state, given Qs[objective][action] and 
    //weights[objective][action][tile]. 
    //(Basically Qs[objective][action] = \SUM_{tile} weights[objective][action][tile])
    protected int adaptiveObjectiveSelection(double[][] Qs, double[][] variance){
        
        //Will store best and worst Q-value per objective
        double[] best = new double[cmac.length];
        double[] worst = new double[cmac.length];
        
        //Will store best and worst actions per objective (multiple actions can have same Q-value)
        ArrayList<ArrayList<Integer>> ibest = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> iworst = new ArrayList<ArrayList<Integer>>();
        
        //initialize
        for(int o=0; o<cmac.length; o++){
            best[o] = -Double.MAX_VALUE;
            worst[o] = Double.MAX_VALUE;
            ibest.add(new ArrayList<Integer>());
            iworst.add(new ArrayList<Integer>());
        }
        
        //For every action
        for(int i=0; i<Qs[0].length; i++){
            //For every objective
            for(int o=0; o<cmac.length; o++){
                //If better than current best ....
                if(Qs[o][i] >= best[o]){
                    if(Qs[o][i] > best[o]){
                        ibest.get(o).clear();
                    }
                    ibest.get(o).add(i);
                    best[o] = Qs[o][i];
                }
                //If worse than current worst ....
                if(Qs[o][i] <= worst[o]){
                    if(Qs[o][i] < worst[o]){
                        iworst.get(o).clear();
                    }
                    iworst.get(o).add(i);
                    worst[o] = Qs[o][i];
                }
            }
        }
        
        //For each objective, randomly select a best and worst representative 
        //among the equally good best and worst actions
        int[] b = new int[cmac.length];
        int[] w = new int[cmac.length];
        for(int o=0; o<cmac.length; o++){
            b[o] = ibest.get(o).get(RNG.randomInt(ibest.get(o).size()));
            w[o] = iworst.get(o).get(RNG.randomInt(iworst.get(o).size()));
        }
        
        //Store a p-value for each objective, indicating confidence 
        //in that objective (lower p = higher confidence)
        double[] p = new double[cmac.length];
        double bestp = Double.MAX_VALUE;
        ArrayList<Integer> ibestp = new ArrayList<Integer>();

        //For each objective
        for(int o=0; o<cmac.length; o++){
            //Test confidence in that objective by applying a paired t-test
            //to the sets of weights of the best and worst action according
            //to that objective
//            p[o] = test.pairedTTest(weights[o][b[o]], weights[o][w[o]]);
            p[o] = bhattacharyya(Qs[o][b[o]], variance[o][b[o]], Qs[o][w[o]], variance[o][w[o]]);

            //When weights are completely the same, the test returns null,
            //catch that and set to 1 (lowest confidence)
            if(Double.isNaN(p[o])){
                p[o] = 1;
            }

            //Keep track of objective with highest confidence (lowest p)
            if(p[o] <= bestp){
                if(p[o] < bestp){
                    ibestp.clear();
                }
                bestp = p[o];
                ibestp.add(o);
            }
        }

        //Select a random objective among those with lowest p
        int tmp = ibestp.get(RNG.randomInt(ibestp.size()));

        //If necessary (in case of Q(lambda) e.g.) indicate that an off-policy
        //action will be taken wrt to an objective. Eligibility traces 
        //should be reset
        for(int o=0; o<cmac.length; o++){
            boolean included = false;
            for(int i=0; i<ibest.get(o).size(); i++){
                if(b[tmp] == ibest.get(o).get(i).intValue()){
                    included = true;
                }
            }
            if(!included){
                offpolicyAction(o);
            }
        }

        //Return the best action according to the most confident objective
        return b[tmp];
    }

    protected double bhattacharyya(double Qs0, double var0, double Qs1, double var1){
        double p = 0.25*Math.log(0.25*((var0/var1) + (var1/var0) + 2))
            + 0.25*Math.pow(Qs0-Qs1, 2.0)/(var0+var1);
        return 1-Math.pow(Math.E, -p);
    }
    
    protected int rankingActionSelection(double[][] Qs){
        ArrayList<TreeMap<Double, Integer>> maps = new ArrayList<TreeMap<Double, Integer>>();
        
        double[] best = new double[cmac.length];
        
        for(int o=0; o<cmac.length; o++){
            best[o] = -Double.MAX_VALUE;
            
            maps.add(new TreeMap<Double, Integer>());
        }
        
        for(int i=0; i<Qs[0].length; i++){
            for(int o=0; o<cmac.length; o++){
                maps.get(o).put(Qs[o][i], i);
                
//                if(Qs[o][i] > 1e4 || Double.isInfinite(Qs[o][i]) || Double.isNaN(Qs[o][i])){
//                    System.out.println(Qs[o][i]);
//                }
                
                if(Qs[o][i] >= best[o]){
                    best[o] = Qs[o][i];
                }
            }
        }
        
//        double[] ps = condifence(Qs, Qss);
        double[] ranking = new double[Qs[0].length];
        for(int o=0; o<cmac.length; o++){
            Collection<Integer> values = maps.get(o).values();
            Iterator<Integer> it = values.iterator();
//            it.next();
            int i=0;
            while(it.hasNext()){
//                ranking[it.next()] += (1.0-ps[o])*i/(ranking.length-1);
                ranking[it.next()] += (1.0)*i/(ranking.length-1);
                i++;
            }
        }
        
        
        double bestv = -Double.MAX_VALUE;
        ArrayList<Integer> ibest = new ArrayList<Integer>();
        for(int a=0; a<ranking.length; a++){
            if(ranking[a] >= bestv){
                if(ranking[a] > bestv){
                    ibest.clear();
                }
                ibest.add(a);
                bestv = ranking[a];
            }
        }
        
        int tmp = ibest.get(RNG.randomInt(ibest.size()));
        for(int o=0; o<cmac.length; o++){
            if(Qs[o][tmp] != best[o]){
                offpolicyAction(o);
            }
        }
        //System.out.println(Arrays.toString(ranking));
        return tmp;
    }
    
    
    private void offpolicyAction(int i){
        cmac[i].resetEs();
    }
    
    @Override
    public void endEpisode() {
        for(int i=0; i<cmac.length; i++){
            cmac[i].resetEs();
        }
    }
    
//    protected int[] tileCoding(double[] state, int action) {
//        int[] extra = new int[]{action, isMarioAbleToShoot ? 0 : 1, facing+1, isMarioOnGround ? 0 : 1};
//        return TileCoding.GetTiles(nrTilings, state, maxNrTiles, extra);
//    }
}
