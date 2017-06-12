package competition.richmario.experiment;

import competition.richmario.AgentType;
import competition.richmario.SimpleExperiment;
import competition.richmario.StateAction;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by user on 15/05/2017.
 */
public class SimilarityManager {

    private static final int SHORT_TERM_ENEMIES = 4;
    private static final int MEDIUM_TERM_ENEMIES = 5;
    private static final int LONG_TERM_ENEMIES = 6;

    /**
     * @param state  represents the state:
     *               [0] can jump?   : 0-1
     *               [1] on ground?  : 0-1
     *               [2] able to shoot?  : 0-1
     *               [3] current direction   : 0-8
     *               direction codes:    [4 = stays in place]
     *               <p>
     *               6   7   8
     *               <p>
     *               3   4   5
     *               <p>
     *               0   1   2
     *               <p>
     *               [4] close enemies yes or no in 8 directions : 0-255
     *               returns one or sum of few of the following numbers:
     *               1   64  8
     *               4   M   32
     *               2   128 16
     *               <p>
     *               result can also be analyzed as byte:
     *               LSB     [0]     up-left
     *               [1]     down-left
     *               [2]     left
     *               [3]     up-right
     *               [4]     down-right
     *               [5]     right
     *               [6]     up
     *               MSB     [7]     down
     *               <p>
     *               [5] mid-range enemies yes or no in 8 directions : 0-255
     *               <same encoding as [4]]>
     *               <p>
     *               [6] far enemies yes or no in 8 directions   : 0-255
     *               <same encoding as [4]]>
     *               <p>
     *               [7] obstacles in front  : 0-15
     *               the sum of one or few of the following numbers:
     *               0   -   no obstacles in mario immediate surrounding
     *               1   -   mario touching an obstacle (standing on it not considered 'touching')
     *               2   -   obstacle in the very close surrounding of mario (below him not considered)
     *               4   -   obstacle in the close surrounding of mario (below him not considered)
     *               8   -   obstacle in the surrounding of mario, but not very close (below him not considered)
     *               <p>
     *               8 8 8 8 8 8 8 8 8 8 8 8 8 8 8
     *               8 8 8 8 8 8 8 8 8 8 8 8 8 8 8
     *               8 8 8 8 8 8 8 8 8 8 8 8 8 8 8
     *               8 8 8 4 4 4 4 4 4 4 4 4 8 8 8
     *               8 8 8 4 4 4 4 4 4 4 4 4 8 8 8
     *               8 8 8 4 4 2 2 2 2 2 4 4 8 8 8
     *               8 8 8 4 4 2 1 1 1 2 4 4 8 8 8
     *               8 8 8 4 4 2 1   1 2 4 4 8 8 8
     *               #############################
     *               <p>
     *               for instance, in case there is an obstacle very close to Mario, the value will be 2.
     *               in case both Mario touching an obstacle and there is another one not very close to him
     *               the value will be 9 (the sum of 1 and 8).
     *               <p>
     *               [8] closest enemy x : 0-21
     *               [9] closest enemy y : 0-21
     * @param action represent the action:
     *               ** NOTICE: the 'run' key makes mario to shoot fireballs as well, if he in the right mode (white clothes)
     *               <p>
     *               0   -   do nothing
     *               1   -   press key:  'go left'
     *               2   -   press key:  'go right'
     *               3   -   press key:  'jump'
     *               4   -   press keys: 'go left' + 'jump'
     *               5   -   press keys: 'go right' + 'jump'
     *               6   -   press key:  'run'
     *               7   -   press keys: 'go left' + 'run'
     *               8   -   press keys: 'go right'  + 'run'
     *               9   -   press keys: 'jump' + 'run'
     *               10  -   press keys: 'go left' + 'jump' + 'run'
     *               11  -   press keys: 'go right' + 'jump' + 'run'
     * @return list of (<state, action>, similarity_factor) pairs
     */
    public static List<Pair<StateAction, Double>> getSimilarityRecords(int[] state, int action) {
        ArrayList<Pair<StateAction, Double>> similarityRecords = new ArrayList<>();
        if (AgentType.Similarities != SimpleExperiment.activeAgentType) {
            return similarityRecords;
        }

        // *** YOUR CODE HERE **********************************************************************

//        similarityRecords.addAll(ignoreOnAir(state, action));
//        whenShootingEnemiesDontCount(similarityRecords, state, action);
        duplicateForEnemiesInMyOppositeDirection(similarityRecords, state, action);
//        similarityRecords.addAll(duplicateForRunOrDoNothing(state, action));
//        similarityRecords.addAll(duplicateJumpWithSomeSimilarityForRunOrDoNothing(state, action));


        // *** END OF YOUR CODE ********************************************************************

        return similarityRecords;
    }

//    private static void whenShootingEnemiesDontCount(ArrayList<Pair<StateAction, Double>> similarityRecords, int[] state, int action) {
//        Actions actionInstance = Actions.findAction(action);
//        if(action.isShooting()) {
//            duplicateEnemiesFor(SHORT_TERM_ENEMIES, LONG_TERM_ENEMIES);
//            duplicateEnemiesFor(MEDIUM_TERM_ENEMIES, LONG_TERM_ENEMIES);
//            duplicateEnemiesFor(SHORT_TERM_ENEMIES, LONG_TERM_ENEMIES);
//        }
//    }

    private static void duplicateForEnemiesInMyOppositeDirection(ArrayList<Pair<StateAction, Double>> similarityRecords, int[] state, int action) {
        Actions actionInstance = Actions.findAction(action);
            if (actionInstance.isRight()) {
                duplicateBySingle(similarityRecords, state, action, new int[]{1, 4, 2, 64, 3, 5, 6, 7}, SHORT_TERM_ENEMIES);
                duplicateBySingle(similarityRecords, state, action, new int[]{1, 4, 2, 64, 3, 5, 6, 7}, MEDIUM_TERM_ENEMIES);
                duplicateBySingle(similarityRecords, state, action, new int[]{1, 4, 2, 64, 3, 5, 6, 7}, LONG_TERM_ENEMIES);
            } else if (actionInstance.isLeft()) {
                duplicateBySingle(similarityRecords, state, action, new int[]{1, 4, 2, 64, 3, 5, 6, 7}, SHORT_TERM_ENEMIES);
                duplicateBySingle(similarityRecords, state, action, new int[]{1, 4, 2, 64, 3, 5, 6, 7}, MEDIUM_TERM_ENEMIES);
                duplicateBySingle(similarityRecords, state, action, new int[]{1, 4, 2, 64, 3, 5, 6, 7}, LONG_TERM_ENEMIES);
            }
    }

    private static void duplicateBySingle(ArrayList<Pair<StateAction, Double>> similarityRecords, int[] state, int action, int[] values, int position) {
        for (int value : values) {
            int[] newState = Arrays.copyOf(state, state.length);
            newState[position] = value;
            similarityRecords.add(new Pair<StateAction, Double>(new StateAction(newState, action), 1d));
        }
    }

    // *** YOUR CODE HERE **********************************************************************
    // Here you can add custom STATIC help functions, if needed

    // *** END OF YOUR CODE ********************************************************************
}
