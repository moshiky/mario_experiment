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

package competition.fa;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.ForwardAgent;
import ch.idsia.agents.controllers.human.HumanKeyboardAgent;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.MarioAIOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey@idsia.ch
 * Date: May 7, 2009
 * Time: 4:38:23 PM
 * Package: ch.idsia
 */

public class Custom
{
public static void main(String[] args)
{
    int experiments = 1;
    
    double[][] results = new double[experiments][];
    for(int i=0; i<experiments; i++){
        results[i] = experiment(args);
    }
    System.out.println(Arrays.toString(means(results)));
    System.exit(0);
}

public static double[] experiment(String[] args){

    int episodes = 1000;
    int level = 0;
    boolean visualize = true;
    
    int nrShapings = 2;//new Integer(args[1]);
    int[] selectedShapings = new int[0];

//    if(new Integer(args[0]) > 1){
        selectedShapings = randomShapings(nrShapings);
//    } 
        
    final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
    marioAIOptions.setVisualization(false);
    
    Agent agent;
    
    switch(0){//new Integer(args[0])){
            case 1: agent = new InitQLambdaAgent(new int[]{nrShapings}, AgentType.SingleShaping);
                break;
            case 2: agent = new InitQLambdaAgent(selectedShapings, AgentType.Supershaping);
                break;
            case 3: agent = new InitQLambdaAgent(selectedShapings, AgentType.Linear);
                break;
            case 4: agent = new InitQLambdaAgent(selectedShapings, AgentType.AOS);
                break;
            case 5: agent = new InitQLambdaAgent(selectedShapings, AgentType.Ranking);
                break;
            case 0: 
            default: agent = new InitQLambdaAgent(new int[]{0}, AgentType.NoShaping);
        }
    
//    final Agent agent = new HumanKeyboardAgent();
    final BasicTask basicTask = new BasicTask(marioAIOptions);
    
    double[] results = new double[episodes];
    for (int i = 0; i < episodes; ++i)
    {
//        do
//        {
            marioAIOptions.setAgent(agent);
            marioAIOptions.setLevelDifficulty(0);
//            marioAIOptions.setLevelRandSeed(1000000);
            marioAIOptions.setLevelRandSeed(level);
            marioAIOptions.setGapsCount(false);
//            marioAIOptions.setGameViewer(false);
//            marioAIOptions.setMarioMode(1);
            if(visualize && i == -1){
                marioAIOptions.setVisualization(true);
            }
//            marioAIOptions.setGameViewerContinuousUpdates(false);
            basicTask.setOptionsAndReset(marioAIOptions);
            results[i] = basicTask.runSingleEpisode(1, true);
            if(visualize) {
//                System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
                System.out.println(results[i]);
            }
//        } while (basicTask.getEnvironment().getEvaluationInfo().marioStatus != Environment.MARIO_STATUS_WIN);
    }
    return results;
//    Runtime rt = Runtime.getRuntime();
//    try
//    {
////            Process proc = rt.exec("/usr/local/bin/mate " + marioTraceFileName);
//        Process proc = rt.exec("python hello.py");
//    } catch (IOException e)
//    {
//        e.printStackTrace();
//    }
//    System.exit(0);

}


    
    public static double[] means(double[][] stats){
        double[] means = new double[stats[0].length];
        for(int j=0; j<stats[0].length; j++){
            for(int i=0; i<stats.length; i++){
                means[j] += stats[i][j];
            }
            means[j] = 1.0*means[j]/(stats.length);
        }
        return means;
    }
    
    public static double[] xs(double[][] stats, int every){
        double[] x = new double[stats[0].length/every];
        for(int i=0; i<stats[0].length; i+=every){
            x[i/every] = i;
        }
        return x;
    }
    
    public static double[] ys(double[] stats, int every){
        double[] x = new double[stats.length/every];
        for(int i=0; i<stats.length; i+=every){
            x[i/every] = stats[i];
        }
        return x;
    }
    
    public static double[][] stds(double[][] stats, int every){
        double[] mean = means(stats);
        
        double[] stdL = new double[stats[0].length/every];
        double[] stdU = new double[stats[0].length/every];
        int counterL = 0;
        int counterU = 0;
        
        for(int j=0; j<stats[0].length; j+=every){
            for(int i=0; i<stats.length; i++){
                if(stats[i][j] < mean[j]){
                    stdL[j/every] += Math.pow(stats[i][j] - mean[j], 2.0);
                    counterL++;
                } else {
                    stdU[j/every] += Math.pow(stats[i][j] - mean[j], 2.0);
                    counterU++;
                }
            }
            if(counterL > 0){
                stdL[j/every] = Math.sqrt(stdL[j/every]/counterL);
            }
            if(counterU > 0){
                stdU[j/every] = Math.sqrt(stdU[j/every]/counterU);
            }
            
//            stdL[j/every] = stdL[j/every];
//            stdU[j/every] = stdU[j/every];
        }
        
        return new double[][]{stdL, stdU};
    }
    
    
    public static int[] randomShapings(int nrShapings){
        ArrayList<Integer> shapings = new ArrayList<Integer>();
        for(int i=1; i<3;i++){
            shapings.add(i);
        }
        Collections.shuffle(shapings);
        shapings.subList(0, nrShapings);
        
        int[] selectedShapings = new int[nrShapings];
//        int[] selectedShapings = new int[nrShapings+1];
        for(int i=0; i<nrShapings; i++){
            selectedShapings[i] = shapings.get(i);
        }
//        selectedShapings[nrShapings] = 0;
        return selectedShapings;
    }
    
}
