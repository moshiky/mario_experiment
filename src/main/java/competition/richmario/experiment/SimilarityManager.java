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

        int[] state_0 = {0,1};
        int[] state_1 = {0,1};
        int[] state_2 = {0,1};
        int[] state_3 = {0,1,2,3,4,5,6,7,8};
        int[] state_4 = {1,2,4,8,16,32,64,128};
        int[] state_5 = {1,2,4,8,16,32,64,128};
        int[] state_6 = {1,2,4,8,16,32,64,128};
        int[] state_7 = {1,2,4,8};
        int[] action_8 = {0,1,2,3,4,5,6,7,8,9,10,11};

        //int[] newState = state.clone();
        //newState[4] = 10;

        for(int i_0=0; i_0<state_0.length;i_0++){
            for(int i_1=0; i_1<state_1.length;i_1++){
                for(int i_2=0; i_2<state_2.length;i_2++){
                    for(int i_3=0; i_3<state_3.length;i_3++){
                        for(int i_4=0; i_4<state_4.length;i_4++){
                            for(int i_5=0; i_5<state_5.length;i_5++){
                                for(int i_6=0; i_6<state_6.length;i_6++){
                                    for(int i_7=0; i_7<state_7.length;i_7++){
                                        for(int i_8=0; i_8<action_8.length;i_8++){

                                            int[] newState = state.clone();
                                            int newAction;
                                            int count=0;
                                            double factor=0.0;

                                            newState[0] = state_0[i_0];
                                            newState[1] = state_1[i_1];
                                            newState[2] = state_2[i_2];
                                            newState[3] = state_3[i_3];
                                            newState[4] = state_4[i_4];
                                            newState[5] = state_5[i_5];
                                            newState[6] = state_6[i_6];
                                            newState[7] = state_7[i_7];
                                            newAction = action_8[i_8];

                                            for(int i=0;i<8;i++){
                                                if(state[i] == newState[i]){count+=1;}
                                            }
                                            if(action == newAction){count+=1;}

                                            factor = count/9;

                                            similarityRecords.add(new Pair<StateAction, Double>(new StateAction(newState, newAction), factor));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
