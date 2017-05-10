/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.demonstration;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 *
 * @author timbrys
 */
public class Demonstration {
    
    protected List<List<StateActionPair>> trajectory;
    
    protected int size;
    protected int cap;
    
    public Demonstration(String file){
        clear();
        addFile(file);
    }
    
    public Demonstration(String[] files){
        clear();
        for(int i=0; i<files.length; i++){
            addFile(files[i]);
        }
    }
    
    protected void addFile(String file){
        try {
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(file);
            Instances data = source.getDataSet();
            for(int i=0; i<data.numInstances(); i++){
                Instance datum = data.instance(i);
                readline(datum.toDoubleArray());
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            Logger.getLogger(Demonstration.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }
    
    public Demonstration(int cap){
        this.cap = cap;
        clear();
    }
    
    public void clear(){
        size = 0;
        trajectory = new ArrayList<List<StateActionPair>>();
        for(int i=0; i<12; i++){
            trajectory.add(new LinkedList<StateActionPair>());
        }
    }
    
    public void readline(double[] state_action){
        double[] state = new double[6];
        int action = (int)state_action[6];
        for(int i=0; i<6; i++){
            state[i] = state_action[i];
        }
        if(!contains(trajectory.get(action), state)){
            trajectory.get(action).add(new StateActionPair(state, action));
        }
    }
    
    protected boolean contains(List<StateActionPair> states, double[] state){
        for(Iterator<StateActionPair> it = states.iterator(); it.hasNext();){
            if(Arrays.equals(it.next().getState(), state)){
                return true;
            }
        }
        return false;
    }
    
    public int size(){
        int s = 0;
        for(int i=0; i<trajectory.size(); i++){
            s += trajectory.get(i).size();
        }
        return s;
    }
    
    public void record(int[] state, int action){
        if(size < cap || cap < 0){
            size++;
            trajectory.get(action).add(new StateActionPair(normalize(state), action));
        }
    }
    
    public double getDistanceClosest(int[] state, int action){
        double[] normalizedState = normalize(state);
        double distance, lowestDistance = Double.MAX_VALUE;
        List<StateActionPair> states = trajectory.get(action);
        for(Iterator<StateActionPair> it = states.iterator(); it.hasNext();){
            distance = distance(normalizedState, it.next().getState());
            if(distance < lowestDistance){
                lowestDistance = distance;
            }
        }
        return lowestDistance;
    }
    
    protected double[] normalize(int[] state){
        double[] newState = new double[6];
        newState[0] = state[0];
        newState[1] = state[1];
        newState[2] = state[2];
        newState[3] = state[3];
        newState[4] = (state[4])/21.0;
        newState[5] = (state[5])/21.0;
        return newState;
    }
    
    protected double distance(double[] state1, double[] state2){
        double distance = 0.0;
        for(int i=0; i<state1.length; i++){
            distance += Math.pow(state1[i]-state2[i], 2.0); 
        }
        return Math.sqrt(distance);
    }
    
    public void toFile(String file){
        String content = toString();
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                  new FileOutputStream(file), "utf-8"));
            writer.write(content);
        } catch (IOException ex) {
           System.out.println("Couldn't write demonstration to file: " + ex.getMessage());
        } finally {
           try {writer.close();} catch (Exception ex) {}
        }
    }
    
    public String toString(){        
        String s = "@RELATION mario\n"
                + "\n"
                + "   @ATTRIBUTE jump  {0.0, 1.0}\n"
                + "   @ATTRIBUTE ground   {0.0, 1.0}\n"
                + "   @ATTRIBUTE facing  {0.0, 1.0}\n"
                + "   @ATTRIBUTE shoot   {0.0, 1.0}\n"
                + "   @ATTRIBUTE enemyx   NUMERIC\n"
                + "   @ATTRIBUTE enemyy   NUMERIC\n"
                + "   @ATTRIBUTE class        {0,1,2,3,4,5,6,7,8,9,10,11}\n"
                + "\n"
                + "@DATA\n";
        for(int i=0; i<trajectory.size(); i++){
//            s += i;
            for(Iterator<StateActionPair> it=trajectory.get(i).iterator(); it.hasNext();){
                StateActionPair state = it.next();
                for(int j=0; j<state.getState().length; j++){
                    s += state.getState()[j] + ",";
                }
                s += state.getAction() + "\r\n";
            }
        }
        return s;
    }
    
}
