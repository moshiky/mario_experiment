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
        //sim 0 when in air
        int[] s = state.clone();
        int a = action;
        StateAction sa;
        Pair p;
//        if(s[0]==0){
//            s[0]=1;
//            sa = new StateAction(s,a);
//            p = new Pair<StateAction,Double>(sa,new Double(0.01));
//            similarityRecords.add(p);
//        }

        Double sim_decay=0.05;
        //sim of distances from enemy x axis
        if(3<=state[8]) {
            int buffer = 10;

            for (int i = state[8]; i < 20-state[8] ; i++) {
                s = state.clone();
                a = action;
                s[8] = i;
                sa = new StateAction(s, a);
                Double similarity = (1.0-(sim_decay*i)) ;
                p = new Pair(sa, new Double(similarity));
                similarityRecords.add(p);
            }
        }

        //sim of distances from enemy y axis
        //assuming action interval similarity in y axis is 1-5,6-10 other irrelevant
        if((1<=state[9])&&(state[9]<=5)){
            Double similarity;
            for(int i=1;i<5;i++){
                s = state.clone();
                a = action;
                s[9] = i;
                sa = new StateAction(s, a);
                //becoming more similar
                if(i<state[9]){
                    similarity = (0.5-(sim_decay*i));
                    if(1.0<similarity){
                        similarity=1.0;
                    }
                }else{
                    similarity = (1.0-(sim_decay*i));
                    if(similarity<0.0){
                        similarity=0.0;
                    }
                }

                p = new Pair(sa, new Double(similarity));
                similarityRecords.add(p);
            }
        } else if((6<=state[9])&&(state[9]<=10)){
            Double similarity;
            for(int i=6;i<10;i++){
                s = state.clone();
                a = action;
                s[9] = i;
                sa = new StateAction(s, a);
                //becoming more similar
                if(i<state[9]){
                    similarity = (0.5-(sim_decay*i));
                    if(1.0<similarity){
                        similarity=1.0;
                    }
                }else{
                    similarity = (1.0-(sim_decay*i));
                    if(similarity<0.0){
                        similarity=0.0;
                    }
                }

                p = new Pair(sa, new Double(similarity));
                similarityRecords.add(p);
            }
        }

        // *** END OF YOUR CODE ********************************************************************

        return similarityRecords;
    }

    // *** YOUR CODE HERE **********************************************************************
    // Here you can add custom STATIC help functions, if needed

    // *** END OF YOUR CODE ********************************************************************
}
