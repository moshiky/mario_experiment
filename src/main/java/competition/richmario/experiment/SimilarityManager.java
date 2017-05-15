package competition.richmario.experiment;

import org.apache.commons.math3.util.Pair;
import java.util.ArrayList;
import java.util.List;

import competition.richmario.StateAction;

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
     *               <to be completed>
     * @return
     */
    public static List<Pair<StateAction, Double>> getSimilarityRecords(int[] state, int action) {
        ArrayList<Pair<StateAction, Double>> similarityRecords = new ArrayList<>();

        // *** YOUR CODE HERE **********************************************************************

        // *** END OF YOUR CODE ********************************************************************

        return similarityRecords;
    }
}
