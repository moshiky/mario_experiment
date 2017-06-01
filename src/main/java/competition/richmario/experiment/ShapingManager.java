package competition.richmario.experiment;

import competition.richmario.AgentType;
import competition.richmario.SimpleExperiment;

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

        boolean rightKeyPressed =
                (previousAction == 2) || (previousAction == 5) || (previousAction == 8) || (previousAction == 11);
        boolean leftKeyPressed =
                (previousAction == 1) || (previousAction == 4) || (previousAction == 7) || (previousAction == 10);
        boolean jumpKeyPressed =
                (previousAction == 3) || (previousAction == 4) || (previousAction == 5) || (previousAction == 9)
                        || (previousAction == 10) || (previousAction == 11);
        boolean runKeyPressed =
                (previousAction == 6) || (previousAction == 7) || (previousAction == 8) || (previousAction == 9)
                        || (previousAction == 10) || (previousAction == 11);
/*
        // wrong right up move
        if ((previousState[3] == 8) && ((previousState[4] & 8) == 8) && rightKeyPressed) {
            rewardShaping -= 5;
        }

        // wrong right move
        else if ((previousState[3] == 5) && ((previousState[4] & 32) == 32) && rightKeyPressed && !jumpKeyPressed) {
            rewardShaping -= 5;
        }

        // prefer moving right
        else if (rightKeyPressed) {
            rewardShaping += 5;
        }

        // wrong left up move
        if ((previousState[3] == 6) && ((previousState[4] & 1) == 1) && leftKeyPressed) {
            rewardShaping -= 5;
        }

        // wrong left move
        if ((previousState[3] == 3) && ((previousState[4] & 4) == 4) && leftKeyPressed && !jumpKeyPressed) {
            rewardShaping -= 5;
        }
*/
        // always prefer to shoot
        if ((previousState[2] == 1) && runKeyPressed && (previousState[4] != 0)) {
            rewardShaping += 0.5;
        }

        // *** END OF YOUR CODE ********************************************************************

        return rewardShaping;
    }

    // *** YOUR CODE HERE **********************************************************************
    // Here you can add custom help functions, if needed

    // *** END OF YOUR CODE ********************************************************************

}
