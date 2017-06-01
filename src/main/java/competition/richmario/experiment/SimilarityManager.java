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
     *      [5] mid-range enemies yes or no in 8 directions : 0-255
     *      [6] far enemies yes or no in 8 directions   : 0-255
     *      [7] obstacles in front  : 0-15
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
        int[] newState = null;

        // can jump - similar by 0.7
        newState = state.clone();
        newState[0] = 1 - newState[0];
        similarityRecords.add(new Pair<>(new StateAction(newState, action), 0.7));

        // on ground - similar by 0.5
        newState = state.clone();
        newState[1] = 1 - newState[1];
        similarityRecords.add(new Pair<>(new StateAction(newState, action), 0.5));

        // able to shoot - similar by 0.6
        newState = state.clone();
        newState[2] = 1 - newState[2];
        similarityRecords.add(new Pair<>(new StateAction(newState, action), 0.6));

        // directions - similar by 0.2
        for (int i = 0 ; i < 9 ; i++) {
            if (i != newState[3]) {
                newState = state.clone();
                newState[3] = i;
                similarityRecords.add(new Pair<>(new StateAction(newState, action), 0.2));
            }
        }

        // *** END OF YOUR CODE ********************************************************************

        return similarityRecords;
    }

    // *** YOUR CODE HERE **********************************************************************
    // Here you can add custom STATIC help functions, if needed

    // *** END OF YOUR CODE ********************************************************************
}
