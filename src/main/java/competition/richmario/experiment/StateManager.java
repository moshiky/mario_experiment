package competition.richmario.experiment;

import ch.idsia.benchmark.mario.engine.sprites.Mario;

/**
 * Created by user on 17/05/2017.
 */
public class StateManager {
    public static final int STATE_LENGTH = 1;

    /**
     * @param marionState   represents current mario state:
     *
     *      [0]     Mario Status    [Mario.STATUS_DEAD, Mario.STATUS_WIN, Mario.STATUS_RUNNING]
     *      [1]     Mario Mode      [0= small, 1= large, 2= large+able to shoot fireballs]
     *      [2]     Is mario on ground  [0= no, 1= yes]
     *      [3]     Is mario able to jump   [0= no, 1= yes]
     *      [4]     Is mario able to shoot  [0= no, 1= yes]
     *      [5]     Is mario carried on top of something (e.g. a Bullet or other creature)  [0= no, 1= yes]
     *      [6]     Killed creatures total so far    [int >= 0]
     *      [7]     Killed creatures by Mario's fireballs total so far   [int >= 0]
     *      [8]     Killed creatures by Mario stomping them total so far    [int >= 0]
     *      [9]     Killed creatures by shell hits total so far    [int >= 0]
     *      [10]    Left time for current episode
     *
     * @return
     */
    public static double[] getStateRepresentation(int[] marionState) {
        double[] state = null;

        // *** YOUR CODE HERE **********************************************************************

        // *** END OF YOUR CODE ********************************************************************

        return state;

    }

    public static double[] getInitialState() {
        return new double[StateManager.STATE_LENGTH];
    }
}
