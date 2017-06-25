package competition.richmario.experiment;

import competition.richmario.AgentType;
import competition.richmario.SimpleExperiment;
import org.apache.commons.math3.util.Pair;
import java.util.ArrayList;
import java.util.List;

import competition.richmario.StateAction;

import static competition.richmario.agents.EnsembleAgent.*;

/**
 * Created by user on 15/05/2017.
 */
public class SimilarityManager {

    /**
     *
     * @param state represents the state:
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
     * @param action represent the action:
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
     * @return list of (<state, action>, similarity_factor) pairs
     */
    public static List<Pair<StateAction, Double>> getSimilarityRecords(int[] state, int action) {
        ArrayList<Pair<StateAction, Double>> similarityRecords = new ArrayList<>();
        if (AgentType.Similarities != SimpleExperiment.activeAgentType) {
            return similarityRecords;
        }

        // *** YOUR CODE HERE **********************************************************************
        // if user is already in a jump
        if(state[0] == 0)
        {
            // if user pressed jump(3) == it's like do nothing(0)
            if(action == 3)
            {
                similarityRecords.add(new Pair<StateAction, Double>(new StateAction(state, 0), 1.0));
            }
            // if user pressed nothing(0) == it's like do jump(3)
            else if(action == 0)
            {
                similarityRecords.add(new Pair<StateAction, Double>(new StateAction(state, 3), 1.0));
            }


            // if user pressed left(1) == it's like left+jump(4)
            else if(action == 1)
            {
                similarityRecords.add(new Pair<StateAction, Double>(new StateAction(state, 4), 1.0));
            }
            // if user pressed left+jump(4) == it's like left(1)
            else if(action == 4)
            {
                similarityRecords.add(new Pair<StateAction, Double>(new StateAction(state, 1), 1.0));
            }


            // if user pressed right(2) == it's like right+jump(5)
            else if(action == 2)
            {
                similarityRecords.add(new Pair<StateAction, Double>(new StateAction(state, 5), 1.0));
            }
            // if user pressed right+jump(5) == it's like left(2)
            else if(action == 5)
            {
                similarityRecords.add(new Pair<StateAction, Double>(new StateAction(state, 2), 1.0));
            }

            // if user pressed run(6) == it's like run+jump(9)
            else if(action == 6)
            {
                similarityRecords.add(new Pair<StateAction, Double>(new StateAction(state, 9), 1.0));
            }
            // if user pressed run+jump(9) == it's like run(6)
            else if(action == 9)
            {
                similarityRecords.add(new Pair<StateAction, Double>(new StateAction(state, 6), 1.0));
            }

            // if user pressed left+run(7) == it's like left+run+jump(10)
            else if(action == 7)
            {
                similarityRecords.add(new Pair<StateAction, Double>(new StateAction(state, 10), 1.0));
            }
            // if user pressed left+run+jump(10) == it's like left+run(7)
            else if(action == 10)
            {
                similarityRecords.add(new Pair<StateAction, Double>(new StateAction(state, 7), 1.0));
            }


            // if user pressed right+run(7) == it's like right+run+jump(10)
            else if(action == 8)
            {
                similarityRecords.add(new Pair<StateAction, Double>(new StateAction(state, 11), 1.0));
            }
            // if user pressed right+run+jump(10) == it's like right+run(7)
            else if(action == 11)
            {
                similarityRecords.add(new Pair<StateAction, Double>(new StateAction(state, 8), 1.0));
            }
        }

        //similarityRecords.add(new Pair<StateAction, Double>(new StateAction(newState, newAction), factor));

        // *** END OF YOUR CODE ********************************************************************

        return similarityRecords;
    }

    // *** YOUR CODE HERE **********************************************************************
    // Here you can add custom STATIC help functions, if needed

    // *** END OF YOUR CODE ********************************************************************
}
