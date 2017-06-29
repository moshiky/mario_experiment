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

        // If there are far enemies, it's not that important where they are
        for (int[] newState : getAllStatesOfGivenColumn(state, 6)) {
            similarityRecords.add(new Pair<>(new StateAction(newState, action), 0.7));
        }

        // Trying to make all the states where mario cannot jump similar
        /*if (state[0] == 0 && contains(new int[] {3, 4, 5, 9, 10, 11}, action)) {
            for (int[] newState : getAllStatesOfGivenColumn(state, 4)) {
                similarityRecords.add(new Pair<>(new StateAction(newState, action), 0.7));
            }
            for (int[] newState : getAllStatesOfGivenColumn(state, 5)) {
                similarityRecords.add(new Pair<>(new StateAction(newState, action), 0.7));
            }
            for (int[] newState : getAllStatesOfGivenColumn(state, 6)) {
                similarityRecords.add(new Pair<>(new StateAction(newState, action), 0.7));
            }
            for (int[] newState : getAllStatesOfGivenColumn(state, 7)) {
                similarityRecords.add(new Pair<>(new StateAction(newState, action), 0.7));
            }
            for (int[] newState : getAllStatesOfGivenColumn(state, 8)) {
                similarityRecords.add(new Pair<>(new StateAction(newState, action), 0.7));
            }
            for (int[] newState : getAllStatesOfGivenColumn(state, 9)) {
                similarityRecords.add(new Pair<>(new StateAction(newState, action), 0.7));
            }
        }*/



        // *** END OF YOUR CODE ********************************************************************

        return similarityRecords;
    }

    private static boolean contains(int[] arr, int num) {
        for (int n : arr) {
            if (n == num) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return all of the states with the same values in columns which are not the given column
     * @param originalState The state
     * @param column        The column in the state to change
     */
    private static List<int[]> getAllStatesOfGivenColumn(int[] originalState, int column) {
        List<int[]> list = new ArrayList<>();
        int range = 0;
        switch (column) {
            case 0:
            case 1:
            case 2:
                range = 1;
                break;
            case 3:
                range = 8;
                break;
            case 4:
            case 5:
            case 6:
                range = 255;
                break;
            case 7:
                range = 15;
            case 8:
            case 9:
                range = 21;
                break;
        }

        for (int i = 0; i < range; i++) {
            if (i == originalState[column]) {
                continue;
            }

            int[] newState = originalState.clone();
            newState[column] = i;
            list.add(newState);
        }

        return list;
    }

    // *** YOUR CODE HERE **********************************************************************
    // Here you can add custom STATIC help functions, if needed

    // *** END OF YOUR CODE ********************************************************************
}
