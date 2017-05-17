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
     *      [5] mid-range enemies yes or no in 8 directions : 0-255
     *      [6] far enemies yes or no in 8 directions   : 0-255
     *      [7] obstacles in front  : 0-15
     *      [8] closest enemy x : 0-21
     *      [9] closest enemy y : 0-21
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
    public double getShapingReward(int[] previousState, int previousAction, int[] currentState) {
        double rewardShaping = 0.0;
        if (AgentType.RewardShaping != SimpleExperiment.activeAgentType) {
            return rewardShaping;
        }

        // *** YOUR CODE HERE **********************************************************************

        // *** END OF YOUR CODE ********************************************************************

        return rewardShaping;
    }

    // *** YOUR CODE HERE **********************************************************************
    // Here you can add custom help functions, if needed

    // *** END OF YOUR CODE ********************************************************************

}
