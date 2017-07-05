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
        int newAction = 0;
        int[] newState = state.clone();
        if (action == 1 && state[0] == 0) {
            newAction = 4;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 1.0));
        if (action == 1 && state[0] == 0) {
            newAction = 10;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.8));
        if (action == 2 && state[0] == 0) {
            newAction = 5;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 1.0));
        if (action == 2 && state[0] == 0) {
            newAction = 11;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.8));


        if (action == 4 && state[0] == 0) {
            newAction = 1;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 1.0));
        if (action == 10 && state[0] == 0) {
            newAction = 1;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.8));
        if (action == 5 && state[0] == 0) {
            newAction = 2;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 1.0));
        if (action == 11 && state[0] == 0) {
            newAction = 2;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.8));

/*
        if (state[8] > 50) {
            for (int i = 50; i < 200; i++) {
                if (i!=state[8]) {
                    newState = state.clone();
                    newState[8] = i;
                    similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.9));
                }
            }
        }*/
        /*newState = state.clone();
        if (state[2] == 0 && action == 6) {
            similarityRecords.add(new Pair<>(new StateAction(newState, 0), 1.0));
        }
        if (state[2] == 0 && action == 0) {
            similarityRecords.add(new Pair<>(new StateAction(newState, 6), 1.0));
        }*/

        newState = state.clone();
        if (action == 1) {
            newAction = 4;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.6));
        if (action == 1) {
            newAction = 7;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.6));
        if (action == 1) {
            newAction = 10;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.5));


        if (action == 4) {
            newAction = 1;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.6));
        if (action == 7) {
            newAction = 1;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.6));
        if (action == 10) {
            newAction = 1;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.5));


        if (action == 2) {
            newAction = 5;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.6));
        if (action == 2) {
            newAction = 8;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.6));
        if (action == 2) {
            newAction = 11;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.5));

        if (action == 5) {
            newAction = 2;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.6));
        if (action == 8) {
            newAction = 2;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.6));
        if (action == 11) {
            newAction = 2;
        }
        similarityRecords.add(new Pair<>(new StateAction(newState, newAction), 0.5));

        // *** END OF YOUR CODE ********************************************************************

        return similarityRecords;
    }

    // *** YOUR CODE HERE **********************************************************************
    // Here you can add custom STATIC help functions, if needed

    // *** END OF YOUR CODE ********************************************************************
}
