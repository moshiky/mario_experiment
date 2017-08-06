/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.agents;

import ch.idsia.evolution.ea.ES;
import competition.richmario.SimpleExperiment;
import competition.richmario.demonstrations.Demonstration;
import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import competition.richmario.StateAction;
import competition.richmario.experiment.SimilarityManager;
import loggingUtils.Logger;
import org.apache.commons.math3.util.Pair;
import util.*;

import java.math.BigInteger;
import java.util.*;

import static competition.richmario.SimpleExperiment.simStage;

/**
 *
 * @author timbrys
 */
abstract public class EnsembleAgent extends BasicMarioAIAgent implements Agent {

        protected double epsilon;
    
    protected StateAction prevSA;
    
    protected float[] prevMarioPos;
    
    private float lastReward;
    
    protected QLambdaAgent[] agents;
    
    protected Demonstration record;
    protected boolean recording;

    protected Logger logger;
    
    public EnsembleAgent(Logger logger, QLambdaAgent[] agents, double epsilon){
        this(logger, agents, epsilon, false);
    }
    
    public EnsembleAgent(Logger logger, QLambdaAgent[] agents, double epsilon, boolean recording){
        super("Ensemble");
        
        this.epsilon = epsilon;
        
        prevSA = StateAction.initState();
        
        prevMarioPos = new float[]{32.0f,32.0f};
        
        lastReward = -1;
        
        this.agents = agents;
     
        this.recording = recording;
        this.record = new Demonstration(-1);

        this.logger = logger;
        
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
        state[0] = isMarioAbleToJump ? 1 : 0;
        state[1] = isMarioOnGround ? 1 : 0;
        state[2] = isMarioAbleToShoot ? 1 : 0;//marioMode;//
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
        
        float[] extraState = new float[2];
        extraState[0] = marioFloatPos[0];
        extraState[1] = marioFloatPos[1];
        
        return new StateAction(state, extraState, -1); //2*2*2*2*13*13*12
    }
    
//    /*
//     * State
//     * 0 can jump? 0-1
//     * 1 on ground? 0-1
//     * 2 able to shoot? 0-1
//     * 3 current direction x 0-2
//     * 4 current direction x 0-2
//     * 4 close enemies yes or no in 8 directions 0-255
//     * 5 midrange enemies yes or no in 8 directions 0-255
//     * 6 far enemies yes or no in 8 directions 0-255
//     * 7 obstacles in front 0-15
//     * 8 closest enemy x 0-21
//     * 9 closest enemy y 0-21
//     * 
//     */
//    public StateAction getState(){
//        int[] state = new int[27];
//        
//        // CLOSEST TWO ENEMIES
//        state[0] = isMarioAbleToJump ? 0 : 1;
//        state[1] = isMarioOnGround ? 0 : 1;
//        state[2] = isMarioAbleToShoot ? 0 : 1;//marioMode;//
//        
//        float xdiff = marioFloatPos[0] - prevMarioPos[0];
//        float ydiff = marioFloatPos[1] - prevMarioPos[1];
//        state[3] = xdiff < 0 ? 0 : (xdiff == 0 ? 1 : 2);
//        state[4] = ydiff < 0 ? 0 : (ydiff == 0 ? 1 : 2);
//        
//        int[] nearbyEnemies = enemies(1,0);
//        System.arraycopy(nearbyEnemies, 0, state, 5, 8);
//        
//        nearbyEnemies = enemies(3,1);
//        System.arraycopy(nearbyEnemies, 0, state, 13, 8);
//        
//        int[] obstacles = obstacle();
//        System.arraycopy(obstacles, 0, state, 21, 4);
//        
//        int[] enemy = closestEnemy();
//        if(Math.abs(enemy[0]) < 11 && Math.abs(enemy[1]) < 11){
//            state[25] = enemy[0]+10;
//            state[26] = enemy[1]+10;
//        } else {
//            state[25] = 21;
//            state[26] = 21;
//        }
//        
////        System.out.println(isMarioAbleToShoot);
////        state[10] = isMarioAbleToShoot ? 0 : 1;
//        
//        float[] extraState = new float[2];
//        extraState[0] = marioFloatPos[0];
//        extraState[1] = marioFloatPos[1];
//        
//        return new StateAction(state, extraState, -1); 
//    }
    
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
        gap += hasGap(marioEgoPos[1]+2) ? 4 : 0;
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
//    protected int[] obstacle(){
//        int[] obstacle = new int[4];
//        int obst;
//        for(int i=0; i<4; i++){
//            obst = getReceptiveFieldCellValue(marioEgoPos[0]-i, marioEgoPos[1]+1);
//            if(obst < 0){
//                obstacle[i] = 1;
//            }
//        }
//        return obstacle;
//    }
    
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
    
//    protected int[] enemies(int out, int in){
//        int[] ens = new int[8];
//        int dir, enemy, extra = 0;
//        if(marioMode > 0){
//            extra = -1;
//        }
//        for(int i=-out+extra; i<out+1; i++){
//            for(int j=-out; j<out+1; j++){
//                if(!(i >= -in && i <= in && j >= -in && j <= in)){
//                    enemy = getEnemiesCellValue(marioEgoPos[0]+i, marioEgoPos[1]+j);
//                    if((enemy >= 80 && enemy < 100) || enemy == 13){
//                        dir = i < 0 ? 0 : (i == 0 ? 2 : 1);
//                        dir += 3*(j < 0 ? 0 : (j == 0 ? 2 : 1));
//                        ens[dir] = 1;
//                    }
//                }
//            }
//        }
//        return ens;
//    }
    
    @Override
    public boolean[] getAction() {
        for (int i = 0; i < action.length; ++i) {
            action[i] = false;
        }
        switch(prevSA.getAction()){
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

    @Override
    public void setName(String Name) {
        this.name = Name;
    }


    static int runs = 0;
    static int sizeTotal = 0;
    static double rewardTmpSum = 0;

    public void giveIntermediateReward(float reward, boolean update) {
        StateAction sa = getState();

        float thisreward = reward - lastReward;
        /*if (runs % this.logger.LOGGING_INTERVAL == 0) {
            this.logger.info("interval reward mean = " + (rewardTmpSum/this.logger.LOGGING_INTERVAL));
            rewardTmpSum = 0;
        }
        rewardTmpSum += thisreward;*/


        if(reward != lastReward) {
            int bp = 3;
        }

        lastReward = reward;

        // send update as isTrainMode because we update iff this is train mode
        int action = egreedyActionSelection(sa, update);
        sa.setAction(action);

        if((SimpleExperiment.usingSimilarities != 2) && update) {

            for (QLambdaAgent agent : agents) {
                //double deltaR = agent.getDeltaR(prevSA, thisreward, sa);
                //agent.update(prevSA, thisreward, sa);

                agent.update(prevSA, thisreward, sa);

                agent.setTraces(sa);

                List<Pair<StateAction, Double>> similarities = getSimilarities(prevSA);
                //Map<Long, Pair<StateAction, Double>> distinctSimilarities = getDistinctSimilarities(similarities);
                int size = similarities.size();
                sizeTotal += size;
                runs++;

                for (Pair<StateAction, Double> similarity : similarities) {
                    StateAction ssa = similarity.getFirst();
                    ssa.setAction(action);

                    agent.setTracesSimilarity(ssa, similarity.getSecond());

                }
            }
        }

/*
        if(recording){
            record.record(sa);
        }
*/
        prevSA = sa;
        prevMarioPos = marioFloatPos.clone();
    }

    private Map<Long, Pair<StateAction, Double>> getDistinctSimilarities(List<Pair<StateAction, Double>> similarities) {
        Map<Long, Pair<StateAction, Double>> sims = new HashMap<>();

        for(Pair<StateAction, Double> sim : similarities) {
            long key = sim.getFirst().key();
            Pair<StateAction, Double> existingSim = sims.get(key);
            if(existingSim != null) {
                if(existingSim.getSecond() < sim.getSecond()) {
                    sims.put(key, sim);
                }
            } else {
                sims.put(key, sim);
            }
        }

        return sims;
    }


    private void foo(int[] s) {
        s[3] = 5;
    }

    private List<Pair<StateAction, Double>> getSimilarities(StateAction prevSA) {

        switch(SimpleExperiment.usingSimilarities) {
            case 0:
                return new ArrayList<Pair<StateAction, Double>>();
            case 1:
                return getFullSimilarities(prevSA);
        }

        return null;
    }


    public static Integer state_jump = 0;
    public static Integer state_shoot = 2;

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
    public static double strongSimFactor = 0.8;
    public static double weakSimFactor = strongSimFactor / 2;

    private List<Pair<StateAction, Double>> getFullSimilarities(StateAction prevSA) {
        return SimilarityManager.getSimilarityRecords(prevSA.getState(), prevSA.getAction());
    }

    protected void swap(short[] state4bits, int i, int i1) {
        short t = state4bits[i];
        state4bits[i] = state4bits[i1];
        state4bits[i1] = t;
    }


    public int egreedyActionSelection(StateAction sa, boolean isTrainMode){
        if((RNG.randomDouble() < epsilon) && isTrainMode){
            return RNG.randomInt(getNumActions());
        } else {
            return greedyActionSelection(sa);
        }
//        double tau = 0.01;
//
//        double sum = 0.0;
//        double[] values = new double[getNumActions()];
//        double highest = -Double.MAX_VALUE;
//        for(int i=0; i<getNumActions(); i++){
//            values[i] = getPreference(i, sa)/tau;
//            if(values[i] > highest){
//                highest = values[i];
//            }
//        }
//        
//        for(int i=0; i<getNumActions(); i++){
//            values[i] = Math.exp(values[i]-highest);
//            sum += values[i];
//        }
//        
//        double threshold = RNG.randomDouble();
//        double total = 0.0;
//        for(int i=0; i<getNumActions(); i++){
//            total += values[i]/sum;
////            System.out.println(values[i]/sum);
//            if(total > threshold){
//                return i;
//            }
//        }
//        return 11;
    }
    
    protected abstract double getPreference(int i, StateAction sa);
    
    public QLambdaAgent getAgent(int n){
        return agents[n];
    }
    
    public Demonstration getTrajectory(){
        return record;
    }
    
    abstract protected int greedyActionSelection(StateAction sa);
    
    public void newEpisode(){
        prevSA = StateAction.initState();
        prevMarioPos = new float[]{32.0f,32.0f};
        lastReward = -1;
    }
    
    @Override
    public void endEpisode() {
//        System.out.println(agents[0].getSize());
        StateAction sa = getState();
        for (QLambdaAgent agent : agents) {
            agent.endEpisode(prevSA, 0.0f, sa);
        }
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }
}