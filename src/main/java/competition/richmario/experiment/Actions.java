package competition.richmario.experiment;

import java.util.HashMap;

import static competition.richmario.experiment.ActionsConst.*;

/*

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
 */
public enum Actions {
    GO_RIGHT(2, MOVEMENT, RIGHT),
    RUN_RIGHT(8, MOVEMENT, RIGHT),
    RUN_LEFT(7, MOVEMENT, LEFT),
    JUMP_RIGHT(5, MOVEMENT, RIGHT),
    JUMP_LEFT(4, MOVEMENT, LEFT),
    JUMP_RUNNING_RIGHT(11, MOVEMENT, RIGHT),
    JUMP_RUNNING_LEFT(10, MOVEMENT, LEFT),
    GO_LEFT(1, MOVEMENT, LEFT),
    DO_NOTHING(0, NON_MOVEMENT, NO_MOVEMENT),
    JUMP(3, NON_MOVEMENT, NO_MOVEMENT),
    RUN(6, NON_MOVEMENT, NO_MOVEMENT),
    JUMP_RUNNING(9, NON_MOVEMENT, NO_MOVEMENT);

    private static HashMap<Integer, Actions> actionIdToAction = new HashMap<>();

    private boolean movement;
    private final int value;
    private int direction;

    static {
        for (Actions action : Actions.values()) {
            actionIdToAction.put(action.value, action);
        }
    }

    Actions(int value, boolean movement, int direction) {
        this.value = value;
        this.movement = movement;
        this.direction = direction;
    }

//    public static List<Actions> moveActions() {
//        return asList(GO_LEFT, GO_RIGHT, JUMP_LEFT, JUMP_RIGHT, RUN_LEFT, RUN_RIGHT, JUMP_RUNNING_LEFT, JUMP_RUNNING_RIGHT);
//    }

    public static Actions findAction(int action) {
        return actionIdToAction.get(action);
    }

    public boolean isMovement() {
        return movement;
    }

    public boolean isRight() {
        return direction == RIGHT;
    }

    public boolean isLeft() {
        return direction == LEFT;
    }
}
