/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.agents;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import competition.richmario.AgentType;
import competition.richmario.SimpleExperiment;
import competition.richmario.demonstrations.Demonstration;
import loggingUtils.Logger;
import util.RNG;

import java.util.*;

/**
 *
 * @author moshec
 */
abstract public class AbstractionEnsembleAgent extends BasicMarioAIAgent implements Agent {

    protected double epsilon;
    protected double[] previousState;
    protected int previousAction;
    protected float[] prevMarioPos;
    private float lastWorldReward;

    protected AbstractionQLambdaAgent[] agents;

    protected Demonstration record;
    protected boolean recording;

    protected Logger logger;

    public AbstractionEnsembleAgent(Logger logger, AbstractionQLambdaAgent[] agents, double epsilon) {
        this(logger, agents, epsilon, false);
    }

    public AbstractionEnsembleAgent(Logger logger, AbstractionQLambdaAgent[] agents, double epsilon, boolean recording){
        super("AbstractionEnsemble");

        if (AgentType.Abstraction != SimpleExperiment.activeAgentType) {
            this.stateLength = 10;
        }

        this.previousState = this.getInitialState();
        this.previousAction = 0;
        this.prevMarioPos = new float[]{32.0f,32.0f};
        this.lastWorldReward = -1;
        this.record = new Demonstration(-1);

        this.epsilon = epsilon;
        this.agents = agents;
        this.recording = recording;
        this.logger = logger;

        // reset all action flags
        this.reset();
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
    public double[] getState(){
        if (AgentType.Abstraction == SimpleExperiment.activeAgentType) {
            // Abstraction agent type
            return this.getCustomState();
        }
        else {
            // System.out.println("E[" + marioEgoPos[0] + "," + marioEgoPos[1] + "] F[" + marioFloatPos[0] + "," + marioFloatPos[1] + "]");

            // other agent type
            double[] state = new double[10];

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

            //state[10] = marioFloatPos[0];
            //state[11] = marioFloatPos[1];

            return state;
        }
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
                double dist1 =
                        (enemiesFloatPos[o1+1] * enemiesFloatPos[o1+1]
                                + enemiesFloatPos[o1+2] * enemiesFloatPos[o1+2]);
                double dist2 = (enemiesFloatPos[o2+1] * enemiesFloatPos[o2+1]
                        + enemiesFloatPos[o2+2] * enemiesFloatPos[o2+2]);
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

    @Override
    public boolean[] getAction() {
        for (int i = 0; i < action.length; ++i) {
            action[i] = false;
        }
        switch(this.previousAction){
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

    public void giveIntermediateReward(float worldRewardUntilNow) {
        double[] currentState = getState();

        double actionReward = worldRewardUntilNow - lastWorldReward;
        this.lastWorldReward = worldRewardUntilNow;

        int nextAction = egreedyActionSelection(currentState);
        for (AbstractionQLambdaAgent agent : agents) {
            agent.update(this.previousState, this.previousAction, actionReward, currentState, nextAction);
        }

        runs++;

        this.previousState = currentState;
        this.previousAction = nextAction;
        this.prevMarioPos = this.marioFloatPos.clone();
    }

    public int egreedyActionSelection(double[] state) {
        if(RNG.randomDouble() < epsilon) {
            return RNG.randomInt(getNumActions());
        } else {
            return greedyActionSelection(state);
        }
    }

    public AbstractionQLambdaAgent getAgent(int n){
        return agents[n];
    }

    abstract protected int greedyActionSelection(double[] state);

    public void newEpisode() {
        previousState = this.getInitialState();
        previousAction = 0;
        prevMarioPos = new float[] {32.0f,32.0f};
        lastWorldReward = -1;
    }

    @Override
    public void endEpisode() { }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }




    private double[] getInitialState() {
        double[] initialState = new double[this.stateLength];
        Arrays.fill(initialState, 0);
        return initialState;
    }

    /**
     *  You have access to:
     *
     *  >>  this.marionState:   represents current mario state
     *      [0]     Mario Status    [Mario.STATUS_DEAD, Mario.STATUS_WIN, Mario.STATUS_RUNNING]
     *      [1]     Mario Mode      [0= small, 1= large, 2= large+able to shoot fireballs]
     *      [2]     Is mario on ground  [0= no, 1= yes]
     *      [3]     Is mario able to jump   [0= no, 1= yes]
     *      [4]     Is mario able to shoot  [0= no, 1= yes]
     *      [5]     Is mario carried on top of something (e.g. a Bullet or other creature)  [0= no, 1= yes]
     *      [6]     Killed creatures total so far    [int >= 0]
     *      [7]     Killed creatures by Mario's fireballs total so far   [int >= 0]
     *      [8]     Killed creatures by Mario stomping them total so far    [int >= 0]
     *      [9]     Killed creatures by shell hits total so far    [int >= 0]
     *      [10]    Left time for current episode
     *
     *  >>  this.marioFloatPos:  represents mario current position
     *      [0]     position x value
     *      [1]     position y value
     *
     *  >>  this.prevMarioPos:  represents mario previous position
     *      [0]     position x value
     *      [1]     position y value
     *
     *  >>  this.levelScene:    represents all the objects around mario, in a 19x19 grid (mario is at the center- [9,9])
     *      each cell can get one of the following values:
     *      -60 =   something that mario can't go through
     *      0   =   nothing
     *      2   =   coin
     *      5   =   princess
     *      1   =   something else (usually that mario can't pass, like ground)
     *
     *  >>  this.enemiesFloatPos:   represents the types and locations of all enemies that are currently on the screen
     *      structure- enemy i represented using cells [i, i+1, i+2] in the following order:
     *      [i] =   enemy i type. possible values:
     *                Sprite.GOOMBA
     *                Sprite.BULLET_BILL
     *                Sprite.ENEMY_FLOWER
     *                Sprite.GOOMBA_WINGED
     *                Sprite.GREEN_KOOPA
     *                Sprite.GREEN_KOOPA_WINGED
     *                Sprite.RED_KOOPA
     *                Sprite.RED_KOOPA_WINGED
     *                Sprite.SPIKY
     *                Sprite.SPIKY_WINGED
     *                Sprite.SHELL
     *      [i+1] =  (enemy_i.x - mario.x)
     *      [i+2] =  (enemy_i.x - mario.x)
     *
     *  >>  method     int enemies(int out, int in)
     *      returns one or sum of few of the following numbers:
     *          1   64  8
     *          4   M   32
     *          2   128 16
     *
     *      result can also be analyzed as byte:
     *          LSB     [0]     up-left
     *                  [1]     down-left
     *                  [2]     left
     *                  [3]     up-right
     *                  [4]     down-right
     *                  [5]     right
     *                  [6]     up
     *          MSB     [7]     down
     *
     *      for instance, in case there is an enemy on the right of mario, 32 will be returned.
     *      in case there is an enemy both at the left of mario and above him, 68 will be returned (the
     *      sum of 64 and 4).
     *      zero will be returned in case there are no enemies in mario immediate surrounding.
     *
     *  >>  method     int obstacle()   -   how far is the closest obstacle (lower = closer)
     *      returns one or sum of few of the following numbers:
     *          0   -   no obstacles in mario immediate surrounding
     *          1   -   mario touching an obstacle (standing on it not considered 'touching')
     *          2   -   obstacle in the very close surrounding of mario (below him not considered)
     *          4   -   obstacle in the close surrounding of mario (below him not considered)
     *          8   -   obstacle in the surrounding of mario, but not very close (below him not considered)
     *
     *          8 8 8 8 8 8 8 8 8 8 8 8 8 8 8
     *          8 8 8 8 8 8 8 8 8 8 8 8 8 8 8
     *          8 8 8 8 8 8 8 8 8 8 8 8 8 8 8
     *          8 8 8 4 4 4 4 4 4 4 4 4 8 8 8
     *          8 8 8 4 4 4 4 4 4 4 4 4 8 8 8
     *          8 8 8 4 4 2 2 2 2 2 4 4 8 8 8
     *          8 8 8 4 4 2 1 1 1 2 4 4 8 8 8
     *          8 8 8 4 4 2 1   1 2 4 4 8 8 8
     *          #############################
     *
     *      for instance, in case there is an obstacle very close to mario 2 will be returned.
     *      in case both mario touching an obstacle and there is another one not very close to him 9
     *      will be returned (the sum of 1 and 8).
     *
     *  >>  method     int[] closestEnemy()
     *      returns [xDist, yDist] where:
     *          xDist   =   {closest_enemy}.x - mario.x
     *          yDist   =   {closest_enemy}.y - mario.y
     */
    private int stateLength = 10;   // NOTICE: remember to change this to your state length!
    private double[] getCustomState() {
        double[] state = new double[this.stateLength];

        // *** YOUR CODE HERE **********************************************************************

        // *** END OF YOUR CODE ********************************************************************

        return state;
    }
}