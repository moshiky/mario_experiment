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

        int maskOptions[] = {0,0x1,0x11,0x01,0x111,0x101,0x011,0x1111,0x1011,0x1101,0x1001,0x0011,0x11111,0x10111,0x11011,0x11101,0x10011,0x11001,0x10001,
                0x101111,0x110111,0x111011,0x111011,0x111101,0x100111,0x110011,0x111001,0x100011,0x110001,0x100001,0x1111111,0x1011111,0x1101111,0x1110111,0x1111011,0x1111101,0x1001111,
                0x1100111,0x1110011,0x1111001,0x1000111,0x1100011,0x1110001,0x1010101,0x1001101,0x1011001};

        if (state[4] == 0)//&&(state[5] == 0))
        {
            for (int i=0;i<maskOptions.length;i++) {
                int[] newState = state.clone();
                newState[6] = maskOptions[i];
                similarityRecords.add(new Pair<StateAction, Double>(new StateAction(newState, action), 0.8));
            }

            if ((state[8] > 10) && (state[9] > 10))
            {
                int[] newState = state.clone();
                if (state[2]==1)
                    newState[2]=0;
                else
                    newState[2]=1;

                similarityRecords.add(new Pair<StateAction, Double>(new StateAction(newState, action), 1.0));
            }
        }




        // *** END OF YOUR CODE ********************************************************************

        return similarityRecords;
    }

    // *** YOUR CODE HERE **********************************************************************
    // Here you can add custom STATIC help functions, if needed

    // *** END OF YOUR CODE ********************************************************************
}
