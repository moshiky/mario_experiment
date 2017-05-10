/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package competition.demonstration;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: Sep 1, 2009 Time: 3:12:07 PM Package: competition.cig.sergeykarakovskiy
 */
public class LfDAgent extends BasicMarioAIAgent implements Agent {
    
    protected int prevAction;
    
    private ConverterUtils.DataSource source;
    private Instances data;
    private J48 tree;
    
    public LfDAgent(String file) throws Exception {
        super("QLambda");
        
        prevAction = 0;
        
        source = new ConverterUtils.DataSource(file);
        data = source.getDataSet();
        if (data.classIndex() == -1)
          data.setClassIndex(data.numAttributes() - 1);
        
//        String[] options = new String[1];
//        options[0] = "-U";            // unpruned tree
        tree = new J48();         // new instance of tree
//        tree.setOptions(options);     // set the options
        tree.buildClassifier(data);   // build classifier
        
        reset();
    }
    
    public int getNumActions(){
        return 12;
    }
    
    public Integer[] sortEnemies(){
        Integer[] sorted = new Integer[enemiesFloatPos.length/3];
        for(int i=0; i<sorted.length; i++){
            sorted[i] = i*3;
        }
        Arrays.sort(sorted, new Comparator<Integer>(){
            @Override
            public int compare(Integer o1, Integer o2) {
                double dist1 = (enemiesFloatPos[o1+1]*enemiesFloatPos[o1+1] + enemiesFloatPos[o1+2]*enemiesFloatPos[o1+2]);
                double dist2 = (enemiesFloatPos[o2+1]*enemiesFloatPos[o2+1] + enemiesFloatPos[o2+2]*enemiesFloatPos[o2+2]);
                return ((Double)dist1).compareTo((Double)dist2);
            }
        });
        return sorted;
    }
    
    public int[] closestEnemy(){
        Integer[] sorted = sortEnemies();
        
        int[] enemy = new int[2];
        if(sorted.length == 0){
            enemy[0] = 1000;
            enemy[1] = 1000;
        } else {
            enemy[0] = (int)(enemiesFloatPos[sorted[0]+1]/13);
            enemy[1] = (int)(enemiesFloatPos[sorted[0]+2]/13);
        }
        return enemy;
    }
    
    private int getStateSize(){
        return 6;
    }
    
    public int[] getState(){
        int[] state = new int[6];
        
        // CLOSEST TWO ENEMIES
        int[] enemy = closestEnemy();
        state[0] = isMarioAbleToJump ? 0 : 1;
        state[1] = isMarioOnGround ? 0 : 1;
        state[2] = facing == -1 ? 0 : 1;
        state[3] = isMarioAbleToShoot ? 0 : 1;
        
        if(Math.abs(enemy[0]) < 11 && Math.abs(enemy[1]) < 11){
            state[4] = enemy[0]+10;
            state[5] = enemy[1]+10;
        } else {
            state[4] = 21;
            state[5] = 21;
        }
        
        return state; //2*2*2*2*13*13*12
    }

    public boolean[] getAction() {
        for (int i = 0; i < action.length; ++i) {
            action[i] = false;
        }
        switch(prevAction){
            case 0:
                break;
            case 1:
                action[Mario.KEY_LEFT] = true;
                break;
            case 2:
                action[Mario.KEY_RIGHT] = true;
                break;
            case 3:
                action[Mario.KEY_JUMP] = true;
                break;
            case 4:
                action[Mario.KEY_LEFT] = true;
                action[Mario.KEY_JUMP] = true;
                break;
            case 5:
                action[Mario.KEY_RIGHT] = true;
                action[Mario.KEY_JUMP] = true;
                break;
            case 6:
                action[Mario.KEY_SPEED] = true;
                break;
            case 7:
                action[Mario.KEY_LEFT] = true;
                action[Mario.KEY_SPEED] = true;
                break;
            case 8:
                action[Mario.KEY_RIGHT] = true;
                action[Mario.KEY_SPEED] = true;
                break;
            case 9:
                action[Mario.KEY_JUMP] = true;
                action[Mario.KEY_SPEED] = true;
                break;
            case 10:
                action[Mario.KEY_LEFT] = true;
                action[Mario.KEY_JUMP] = true;
                action[Mario.KEY_SPEED] = true;
                break;
            case 11:
                action[Mario.KEY_RIGHT] = true;
                action[Mario.KEY_JUMP] = true;
                action[Mario.KEY_SPEED] = true;
                break;
        }
        
        
//        action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
        return action;
    }

    @Override
    public void reset() {
        for (int i = 0; i < action.length; ++i) {
            action[i] = false;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String Name) {
        this.name = Name;
    }
    
    public void giveIntermediateReward(float reward) {
        prevAction = actionSelection(getState());
    }
    
    protected int actionSelection(int[] state){
        try {
            Instances unlabeled = source.getStructure();
            unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
//            unlabeled.add(new Instance(1.0, new double[]{0.0,0.0,1.0,0.0,0.0,0.0}));
            unlabeled.add(new Instance(1.0, normalize(state)));
            
            return (int)tree.classifyInstance(unlabeled.firstInstance());
        } catch (Exception ex) {
            Logger.getLogger(QValueReuseAgent.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
            return -1;
        }
    }
    
    @Override
    public void endEpisode() {
    }

    private double[] normalize(int[] state) {
        double[] newState = new double[6];
        newState[0] = state[0];
        newState[1] = state[1];
        newState[2] = state[2];
        newState[3] = state[3];
        newState[4] = (state[4])/21.0;
        newState[5] = (state[5])/21.0;
        return newState;
    }
    
}
