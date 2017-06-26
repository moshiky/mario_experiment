package competition.richmario.experiment;

import competition.richmario.AgentType;
import competition.richmario.SimpleExperiment;

import java.util.Arrays;
import java.util.List;

/**
 * Created by user on 15/05/2017.
 */
public class ShapingManager {
    private static final int JUMP_RUNNING_RIGHT = 11;
    private static final int RUN_RIGHT = 8;
    private static final int GO_RIGHT = 2;
    private static final int JUMP_RIGHT = 5;
    private static final int DO_NOTHING = 0;
    private static final int JUMP = 3;

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

        rewardShaping += rewardForGoingForward(previousAction);
        rewardShaping += punishForStayingInSameSpot(previousMarionPosition, currentMarionPosition);
        rewardShaping += punishForDoingNothing(previousAction);
        rewardShaping += rewardForJumpingIfThereIsSomethingAbove();
        rewardShaping += rewardForJumpingIfThereIsACloseBlock();
        rewardShaping += rewardForJumpingIfThereIsACloseEnemy(previousState, previousAction);
        rewardShaping += rewardForRunJumpingIfThereIsAMediumLengthEnemy(previousState, previousAction);
        rewardShaping += rewardJumpToObsticle(previousState, previousAction);
        //punishForDeath(); // Unneeded. This is automatic in the game.
        // *** END OF YOUR CODE ********************************************************************

        return rewardShaping;
    }

    private double rewardJumpToObsticle(int[] previousState, int previousAction) {
        if(previousState[7] == 4 || previousState[7] == 8)
        {
            return 0.5;
        }

        return 0;
    }

    private double rewardForJumpingIfThereIsACloseEnemy(int[] previousState, int previousAction) {
//        int closeEnemies = previousState[4];
//
//        if(closeEnemies == 32 && (previousAction == JUMP_RIGHT || previousAction == JUMP)) {
//            return 0.5;
//        }

        return 0;
    }

    private double rewardForRunJumpingIfThereIsAMediumLengthEnemy(int[] previousState, int previousAction) {
        int notSoCloseEnemies = previousState[5];

        if(notSoCloseEnemies == 32 && (previousAction == JUMP_RIGHT || previousAction == JUMP_RUNNING_RIGHT)) {
            return 0.2;
        }

        return 0;
    }

    private double punishForDoingNothing(int previousAction) {
        return previousAction == DO_NOTHING ? -0.5 : 0;
//        return 0;
    }

    private double rewardForJumpingIfThereIsACloseBlock() {
        return 0;
    }

    private double rewardForJumpingIfThereIsSomethingAbove() {
        return 0;
    }

    private double punishForStayingInSameSpot(float[] previousMarionPosition, float[] currentMarionPosition) {
//        if(Arrays.equals(previousMarionPosition, currentMarionPosition))
//        {
//            return -0.3;
//        }
//
        return 0;
    }

    private double rewardForGoingForward(int previousAction) {
        List<Integer> goingForwardActions = Arrays.asList(GO_RIGHT, JUMP_RIGHT, RUN_RIGHT, JUMP_RUNNING_RIGHT);
        List<Integer> involveRunning = Arrays.asList(RUN_RIGHT, JUMP_RUNNING_RIGHT);

        if(goingForwardActions.contains(previousAction))
        {
            return involveRunning.contains(previousAction) ? 0.9 : 0.5;
        }

        return 0;
    }

    // *** YOUR CODE HERE **********************************************************************
    // Here you can add custom help functions, if needed

    // *** END OF YOUR CODE ********************************************************************

}
