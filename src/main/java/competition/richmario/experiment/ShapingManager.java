package competition.richmario.experiment;

import competition.richmario.AgentType;
import competition.richmario.SimpleExperiment;

import java.util.LinkedList;

/**
 * Created by user on 15/05/2017.
 */
public class ShapingManager {

    // *** YOUR CODE HERE **********************************************************************
    // Here you can add custom members, if needed

    // *** END OF YOUR CODE ********************************************************************


    public ShapingManager () {

        // *** YOUR CODE HERE **********************************************************************
        // Here you can add custom members initialization, if needed

        // *** END OF YOUR CODE ********************************************************************
    }

    /**
     *
     * @param previousState and currentState represents states:
     *      [0] can jump?   : 0-1
     *      [1] on ground?  : 0-1
     *      [2] able to shoot?  : 0-1
     *      [3] current direction   : 0-8
     *              direction codes:    [4 = stays in place]
     *
     *                  6   7   8
     *
     *                  3   4   5
     *
     *                  0   1   2
     *
     *      [4] close enemies yes or no in 8 directions : 0-255
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
     *      [5] mid-range enemies yes or no in 8 directions : 0-255
     *          <same encoding as [4]]>
     *
     *      [6] far enemies yes or no in 8 directions   : 0-255
     *          <same encoding as [4]]>
     *
     *      [7] obstacles in front  : 0-15
     *          the sum of one or few of the following numbers:
     *          0   -   no obstacles in mario immediate surrounding
     *          1   -   mario touching an obstacle (standing on it not considered 'touching')
     *          2   -   obstacle in the very close surrounding of mario (below him not considered)
     *          4   -   obstacle in the close surrounding of mario (below him not considered)
     *          8   -   obstacle in the surrounding of mario, but not very close (below him not considered)
     *
     *                    8
     *                    4
     *                    2
     *                  M 1
     *
     *          for instance, in case there is an obstacle very close to Mario, the value will be 2.
     *          in case both Mario touching an obstacle and there is another one not very close to him
     *          the value will be 9 (the sum of 1 and 8).
     *
     *      [8] closest enemy x : 0-21
     *      [9] closest enemy y : 0-21
     *
     * @param previousMarionPosition and currentMarionPosition represent Mario's position in the world:
     *      [0] Mario's x coordinate
     *      [1] Mario's y coordinate
     *
     * @param previousAction represent the action taken:
     *      ** NOTICE: the 'run' key makes mario to shoot fireballs as well, if he in the right mode (white clothes)
     *
     *      0   -   do nothing
     *      1   -   press key:  'go left'
     *      2   -   press key:  'go right'
     *      3   -   press key:  'jump'
     *      4   -   press keys: 'go left' + 'jump'
     *      5   -   press keys: 'go right' + 'jump'
     *      6   -   press key:  'run'
     *      7   -   press keys: 'go left' + 'run'
     *      8   -   press keys: 'go right'  + 'run'
     *      9   -   press keys: 'jump' + 'run'
     *      10  -   press keys: 'go left' + 'jump' + 'run'
     *      11  -   press keys: 'go right' + 'jump' + 'run'
     *
     * @return the value of F(s, a, s'), such that R'(s, a, s') = R(s, a, s') + F(s, a, s')
     */
    public double getShapingReward(int[] previousState, float[] previousMarionPosition, int previousAction,
                                   int[] currentState, float[] currentMarionPosition) {
        double rewardShaping = 0.0;
        if (AgentType.RewardShaping != SimpleExperiment.activeAgentType) {
            return rewardShaping;
        }

        // *** YOUR CODE HERE **********************************************************************

        LinkedList<Integer> enemyAbove = new LinkedList<Integer>();
        enemyAbove.add(1);
        enemyAbove.add(9);
        enemyAbove.add(65);
        enemyAbove.add(72);
        enemyAbove.add(73);

        LinkedList<Integer> enemyBelow = new LinkedList<Integer>();
        enemyBelow.add(2);
        enemyBelow.add(18);
        enemyBelow.add(128);
        enemyBelow.add(130);
        enemyBelow.add(156);

     
        //bad to jump into enemy
        //good to jump over enemy
        //involves a jump


        if((previousAction==3)||(previousAction==5)||(previousAction==11)){

            if((currentState[9]-previousState[9])<=0)
            //far to medium
            if((enemyAbove.contains(currentState[5]))&&(enemyAbove.contains(previousState[6]))){
                rewardShaping-=0.2;
            }
            //medium to close
            if((enemyAbove.contains(currentState[4]))&&(enemyAbove.contains(previousState[5]))){
                rewardShaping-=0.7;
            }
            else {
                //close to medium
                if ((enemyBelow.contains(currentState[5])) && (enemyBelow.contains(previousState[4]))) {
                    rewardShaping += 0.2;
                }
                //medium to far
                if ((enemyAbove.contains(currentState[6])) && (enemyAbove.contains(previousState[5]))) {
                    rewardShaping += 0.7;
                }
            }
        }


        // *** END OF YOUR CODE ********************************************************************

        return rewardShaping;
    }

    // *** YOUR CODE HERE **********************************************************************
    // Here you can add custom help functions, if needed

    // *** END OF YOUR CODE ********************************************************************

}
