/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.demonstrations;

import competition.richmario.StateAction;
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
    
    //normalized reward features 
    protected List<DemonstratedSample> trajectory;
    
    //normalized original state-space
    protected List<List<DemonstratedSample>> demonstratedSamples;
    
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
            System.out.println(file);
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
        demonstratedSamples = new ArrayList<List<DemonstratedSample>>();
        for(int i=0; i<12; i++){
            demonstratedSamples.add(new LinkedList<DemonstratedSample>());
        }
        trajectory = new ArrayList<DemonstratedSample>();
    }
    
    public void readline(double[] state_action){
        double[] state = new double[state_action.length-1];
        int action = (int)state_action[state_action.length-1];
        System.arraycopy(state_action, 0, state, 0, state.length);
        DemonstratedSample sample = new DemonstratedSample(state, action);
        trajectory.add(new DemonstratedSample(StateAction.unnormalize(state, action).rewardFeatures(), action));
        if(!contains(demonstratedSamples.get(action), state)){
            demonstratedSamples.get(action).add(sample);
        }
    }
    
    protected boolean contains(List<DemonstratedSample> states, double[] state){
        for(Iterator<DemonstratedSample> it = states.iterator(); it.hasNext();){
            if(Arrays.equals(it.next().getState(), state)){
                return true;
            }
        }
        return false;
    }
    
    public int size(){
        return trajectory.size();
    }
    
    public void record(StateAction state){
        if(size < cap || cap < 0){
            size++;
            trajectory.add(new DemonstratedSample(state.rewardFeatures(), state.getAction()));
            DemonstratedSample sample = new DemonstratedSample(StateAction.normalize(state.getState()), state.getAction());
            demonstratedSamples.get(state.getAction()).add(sample);
        }
    }
    
    public double getDistanceClosest(int[] state, int action){
        double[] normalizedState = StateAction.normalize(state);
        double distance, lowestDistance = Double.MAX_VALUE;
        List<DemonstratedSample> states = demonstratedSamples.get(action);
        for(Iterator<DemonstratedSample> it = states.iterator(); it.hasNext();){
            distance = distance(normalizedState, it.next().getState());
            if(distance < lowestDistance){
                lowestDistance = distance;
            }
        }
        return Math.sqrt(lowestDistance);
    }
    
    protected double distance(double[] state1, double[] state2){
        double distance = 0.0;
        for(int i=0; i<state1.length; i++){
            distance += Math.pow(state1[i]-state2[i], 2.0); 
        }
        return distance;
    }
    
    public void toFile(String file){
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                  new FileOutputStream(file), "utf-8"));
            writer.write(toString());
        } catch (IOException ex) {
           System.out.println("Couldn't write demonstration to file: " + ex.getMessage());
        } finally {
           try {writer.close();} catch (Exception ex) {}
        }
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                  new FileOutputStream("reward_features_"+file), "utf-8"));
            writer.write(toStringTrajectory());
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
                + "   @ATTRIBUTE shoot   {0.0, 1.0}\n"
                + "   @ATTRIBUTE current_dir   NUMERIC\n"
                + "   @ATTRIBUTE close_enemies   NUMERIC\n"
                + "   @ATTRIBUTE mid_enemies   NUMERIC\n"
                + "   @ATTRIBUTE far_enemies   NUMERIC\n"
                + "   @ATTRIBUTE obstacles   NUMERIC\n"
                + "   @ATTRIBUTE closest_enemy_x   NUMERIC\n"
                + "   @ATTRIBUTE closest_enemy_y   NUMERIC\n"
//                + "   @ATTRIBUTE gap   NUMERIC\n"
                + "   @ATTRIBUTE class        {0,1,2,3,4,5,6,7,8,9,10,11}\n"
                + "\n"
                + "@DATA\n";
        for (List<DemonstratedSample> demonstratedSample : demonstratedSamples) {
//            s += i;
            for (DemonstratedSample state : demonstratedSample) {
                for(int j=0; j<state.getState().length; j++){
                    s += state.getState()[j] + ",";
                }
                s += state.getAction() + "\r\n";
            }
        }
        return s;
    }
    
    public String toStringTrajectory(){        
        String s = "@RELATION mario\n"
                + "\n"
                + "   @ATTRIBUTE jump  {0.0, 1.0}\n"
                + "   @ATTRIBUTE ground   {0.0, 1.0}\n"
                + "   @ATTRIBUTE shoot   {0.0, 1.0}\n"
                + "   @ATTRIBUTE x_dir   NUMERIC\n"
                + "   @ATTRIBUTE y_dir   NUMERIC\n"
                + "   @ATTRIBUTE close_enemies_1   {0.0, 1.0}\n"
                + "   @ATTRIBUTE close_enemies_2   {0.0, 1.0}\n"
                + "   @ATTRIBUTE close_enemies_3   {0.0, 1.0}\n"
                + "   @ATTRIBUTE close_enemies_4   {0.0, 1.0}\n"
                + "   @ATTRIBUTE close_enemies_5   {0.0, 1.0}\n"
                + "   @ATTRIBUTE close_enemies_6   {0.0, 1.0}\n"
                + "   @ATTRIBUTE close_enemies_7   {0.0, 1.0}\n"
                + "   @ATTRIBUTE close_enemies_8   {0.0, 1.0}\n"
                + "   @ATTRIBUTE mid_enemies_1   {0.0, 1.0}\n"
                + "   @ATTRIBUTE mid_enemies_2   {0.0, 1.0}\n"
                + "   @ATTRIBUTE mid_enemies_3   {0.0, 1.0}\n"
                + "   @ATTRIBUTE mid_enemies_4   {0.0, 1.0}\n"
                + "   @ATTRIBUTE mid_enemies_5   {0.0, 1.0}\n"
                + "   @ATTRIBUTE mid_enemies_6   {0.0, 1.0}\n"
                + "   @ATTRIBUTE mid_enemies_7   {0.0, 1.0}\n"
                + "   @ATTRIBUTE mid_enemies_8   {0.0, 1.0}\n"
                + "   @ATTRIBUTE obstacles_1   {0.0, 1.0}\n"
                + "   @ATTRIBUTE obstacles_2   {0.0, 1.0}\n"
                + "   @ATTRIBUTE obstacles_3   {0.0, 1.0}\n"
                + "   @ATTRIBUTE obstacles_4   {0.0, 1.0}\n"
                + "   @ATTRIBUTE closest_enemy_x   NUMERIC\n"
                + "   @ATTRIBUTE closest_enemy_y   NUMERIC\n"
//                + "   @ATTRIBUTE gap_1   NUMERIC\n"
//                + "   @ATTRIBUTE gap_2   NUMERIC\n"
//                + "   @ATTRIBUTE gap_3   NUMERIC\n"
                + "   @ATTRIBUTE class        {0,1,2,3,4,5,6,7,8,9,10,11}\n"
                + "\n"
                + "@DATA\n";
        for (DemonstratedSample state : trajectory) {
            for(int j=0; j<state.getState().length; j++){
                s += state.getState()[j] + ",";
            }
            s += state.getAction() + "\r\n";
        }
//        for(int i=0; i<demonstratedSamples.size(); i++){
////            s += i;
//            for(Iterator<DemonstratedSample> it=demonstratedSamples.get(i).iterator(); it.hasNext();){
//                DemonstratedSample state = it.next();
//                for(int j=0; j<state.getState().length; j++){
//                    s += state.getState()[j] + ",";
//                }
//                s += state.getAction() + "\r\n";
//            }
//        }
        return s;
    }
}
