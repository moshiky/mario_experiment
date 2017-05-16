package competition.richmario.experiment;

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
     * @param previousState represents the state:
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
     * @param previousAction represent the action:
     *               <to be completed>
     * @return
     */
    public static List<Pair<StateAction, Double>> getSimilarityRecords(int[] previousState, int previousAction) {
        ArrayList<Pair<StateAction, Double>> similarityRecords = new ArrayList<>();

        // *** YOUR CODE HERE **********************************************************************

        StateAction sa = new StateAction(previousState, previousAction);

        similarityRecords.add(new Pair<>(sa.clone(), 1.0));


        if(true)/*simStage != 0*/{
            int[] state = sa.getState();
            int action = sa.getAction();
            // !(3,4,5 9,10,11)
            // is able to jump -> not able to jump
            if (action % 6 < 3) {
                state[state_jump] = state[state_jump] == 0 ? 1 : 0;
            }


            StateAction ssa = new StateAction(state, sa.getExtraState(), action);

            Pair<StateAction, Double> pair = new Pair<StateAction, Double>(ssa, strongSimFactor);
            similarityRecords.add(pair);
        }

        // 3
        Integer size = 0;

        if(true)/*simStage != 1*/{
            size = similarityRecords.size();
            for (int i = 0; i < size; ++i) {
                Pair<StateAction, Double> tstatePair = similarityRecords.get(i);
                Double tsim = tstatePair.getSecond();
                StateAction tstateAction = tstatePair.getFirst().clone();
                int taction = tstateAction.getAction();
                int[] tstate = tstateAction.getState();

                if (taction <= 6) {
                    tstate[state_shoot] = tstate[state_shoot] == 0 ? 1 : 0;
                    StateAction tstateAction2 = new StateAction(tstate, sa.getExtraState(), taction);
                    similarityRecords.add(new Pair<>(tstateAction2, strongSimFactor * tsim));
                }
            }
        }


        if(true)/*simStage != 2*/{
            // MIRROR CLOSE ENEMIES
            size = similarityRecords.size();
            for (int j = 0; j < size; ++j) {
                Pair<StateAction, Double> tstatePair = similarityRecords.get(j);
                Double tsim = tstatePair.getSecond();
                StateAction tstateAction = tstatePair.getFirst().clone();
                int taction = tstateAction.getAction();
                int[] tstate = tstateAction.getState();

                Integer state4 = tstate[4];


                short[] state4bits = new short[8];
                for (int i = 0; i < 8; ++i) {
                    state4bits[i] = (short) (state4 % 2);
                    state4 >>= 1;
                }


                swap(state4bits, 0, 1);
                swap(state4bits, 3, 4);
                swap(state4bits, 6, 7);

                int newstate4 = 0;
                for (int i = 0; i < 8; ++i) {
                    newstate4 += Math.pow(2, i) * state4bits[i];
                }

                tstate[4] = newstate4;

                int newtaction = 0;
                switch (taction) {
                    case 1:
                        newtaction = 2;
                        break;
                    case 4:
                        newtaction = 5;
                        break;
                    case 7:
                        newtaction = 8;
                        break;
                    case 10:
                        newtaction = 11;
                        break;
                    default:
                        newtaction = taction;
                }


                similarityRecords.add(new Pair<>(new StateAction(tstate, sa.getExtraState(), newtaction), weakSimFactor * tsim));
            }
        }

        if(true)/*simStage != 3*/{

            // MIRROR MID ENEMIES
            size = similarityRecords.size();
            for (int j = 0; j < size; ++j) {
                Pair<StateAction, Double> tstatePair = similarityRecords.get(j);
                Double tsim = tstatePair.getSecond();
                StateAction tstateAction = tstatePair.getFirst().clone();
                int taction = tstateAction.getAction();
                int[] tstate = tstateAction.getState();

                Integer state5 = tstate[5];


                short[] state5bits = new short[8];
                for (int i = 0; i < 8; ++i) {
                    state5bits[i] = (short) (state5 % 2);
                    state5 >>= 1;
                }


                swap(state5bits, 0, 1);
                swap(state5bits, 3, 4);
                swap(state5bits, 6, 7);

                int newstate5 = 0;
                for (int i = 0; i < 8; ++i) {
                    newstate5 += Math.pow(2, i) * state5bits[i];
                }

                tstate[5] = newstate5;

                similarityRecords.add(new Pair<>(new StateAction(tstate, sa.getExtraState(), taction), weakSimFactor * tsim));
            }
        }


        if(true)/*simStage != 4*/{
            int[] state = sa.getState();

            // Generate mid-range if there is close-range enemy
            if (state[8] >= 9 && state[8] <= 11 &&
                    state[9] >= 9 && state[9] <= 11) {
                size = similarityRecords.size();
                for (int j = 0; j < size; ++j) {
                    for (int si = 0; si < 15; si++) {
                        Pair<StateAction, Double> tstatePair = similarityRecords.get(j);
                        Double tsim = tstatePair.getSecond();
                        StateAction tstateAction = tstatePair.getFirst().clone();
                        int taction = tstateAction.getAction();
                        int[] tstate = tstateAction.getState();
                        tstate[5] = si;
                        similarityRecords.add(new Pair<>(new StateAction(tstate, sa.getExtraState(), taction), weakSimFactor * tsim));
                    }
                }
            }
        }

        if(true)/*simStage != 5*/{
            // Running similarity
            size = similarityRecords.size();
            for (int i = 0; i < size; ++i) {
                Pair<StateAction, Double> tstatePair = similarityRecords.get(i);
                Double tsim = tstatePair.getSecond();
                StateAction tstateAction = tstatePair.getFirst().clone();
                int taction = tstateAction.getAction();
                int[] tstate = tstateAction.getState();

                taction = (taction + 6) % 12;

                similarityRecords.add(new Pair<>(new StateAction(tstate, sa.getExtraState(), taction), strongSimFactor * tsim));
            }
        }

        // *** END OF YOUR CODE ********************************************************************

        return similarityRecords;
    }

    private static void swap(short[] state4bits, int i, int i1) {
        short t = state4bits[i];
        state4bits[i] = state4bits[i1];
        state4bits[i1] = t;
    }
}
