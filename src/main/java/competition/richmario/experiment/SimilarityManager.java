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


        //all states in which closest enemy is within range of 1 are very similar (0.9)
        //within 2 are 0.8
        //within 3 are 0.7

        double[][] basicScores = {{1, 0.9},{2, 0.8},{3, 0.7}};

        for (int i=0; i<3; i++) {

            if (state[8] - basicScores[i][0] >= 0)
            {
                //minus in x
                int[] s = copyState(state);
                s[8] -= basicScores[i][0];
                int a = action;
                double sim = basicScores[i][1];

                addtolist(similarityRecords, s, a, sim);

            }

            if (state[8] + basicScores[i][0] <= 21)
            {
                //plus in x
                int[] s = copyState(state);
                s[8] += basicScores[i][0];
                int a = action;
                double sim = basicScores[i][1];

                addtolist(similarityRecords, s, a, sim);
            }

            if (state[9] - basicScores[i][0] >= 0)
            {
                //minus in y
                int[] s = copyState(state);
                s[9] -= basicScores[i][0];
                int a = action;
                double sim = basicScores[i][1];

                addtolist(similarityRecords, s, a, sim);
            }

            if (state[9] + basicScores[i][0] <= 21)
            {
                //plus in y
                int[] s = copyState(state);
                s[9] += basicScores[i][0];
                int a = action;
                double sim = basicScores[i][1];

                addtolist(similarityRecords, s, a, sim);
            }
        }

        //look at all states in which Mario touches an obstacle: in this case, the similarity is 0.9
        int[] masks = {0x01, 0x3, 0x5, 0x9, 0xB, 0xD};
        for (int i=0; i<6; i++)
        {
            if ((state[7]&masks[i]) != state[7])    //don't do this for the identical state
            {
                //plus in y
                int[] s = copyState(state);
                s[7] &= masks[i];
                int a = action;
                double sim = 0.9;

                addtolist(similarityRecords, s, a, sim);
            }
        }

        {
            //add the identical state in which Mario can or can't shoot
            int[] s = copyState(state);
            int a = action;
            double sim = 0.9;
            if (s[2] == 0)
                s[2] = 1;
            else
                s[2] = 0;

            addtolist(similarityRecords, s, a, sim);
        }

        

        // *** END OF YOUR CODE ********************************************************************

        return similarityRecords;
    }

    // *** YOUR CODE HERE **********************************************************************
    // Here you can add custom STATIC help functions, if needed
    static int[] copyState(int[] state)
    {
        int[] newstate = new int[10];
        for (int i=0; i<10; i++)
        {
            newstate[i] = state[i];
        }

        return newstate;
    }

    static void addtolist(ArrayList<Pair<StateAction, Double>> similarityRecords, int[] s, int a, double sim)
    {
        StateAction sa = new StateAction(s, a);
        Double simrec = new Double(sim);
        Pair p = new Pair<StateAction, Double>(sa, simrec);
        similarityRecords.add(p);
    }

    // *** END OF YOUR CODE ********************************************************************
}
