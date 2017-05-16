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
package competition.richmario.shapings;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.MarioAIOptions;
import competition.richmario.agents.ConfidenceEnsembleAgent;
import competition.richmario.demonstrations.Demonstration;
import competition.richmario.demonstrations.DemonstrationShaping;
import competition.richmario.agents.HumanKeyboardAgent;
import competition.richmario.agents.LinearEnsembleAgent;
import competition.richmario.QLHash;
import competition.richmario.agents.EnsembleAgent;
import competition.richmario.agents.QLambdaAgent;
import competition.richmario.agents.RankingEnsembleAgent;
import competition.richmario.agents.VotingEnsembleAgent;
import competition.transfer.AgentType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import util.RNG;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, sergey@idsia.ch Date: May
 * 7, 2009 Time: 4:38:23 PM Package: ch.idsia
 */
public class EnsembleOfEverythingExperiment {
    
    private static boolean DEBUG = false;

    public static void main(String[] args) throws Exception {
        int experiments = 1;
        
//        demonstrate();
        
        double[][] results = new double[experiments][];
        for (int i = 0; i < experiments; i++) {
            if(DEBUG){
                results[i] = experiment(new String[]{"4", "40"});
            } else {
                results[i] = experiment(args);
            }
        }
        System.out.println(Arrays.toString(means(results)));
        System.exit(0);
    }
    
    public static QLHash learnSourceTask(){
        QLambdaAgent learner = new QLambdaAgent(new HeuristicShaping(-1, 1.0, 0.9), 0.9);
        EnsembleAgent agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, 0.05);
        
        MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);
        final BasicTask basicTask = new BasicTask(marioAIOptions);

        double lastReward = -512;
        for (int i = 0; i < 100; ++i) {
            marioAIOptions.setAgent(agent);
            marioAIOptions.setLevelDifficulty(0);
            marioAIOptions.setMarioMode(RNG.randomInt(3));
            marioAIOptions.setLevelRandSeed(RNG.randomInt(1000000));
            marioAIOptions.setGapsCount(false);
            marioAIOptions.setEnemies("off");
            basicTask.setOptionsAndReset(marioAIOptions);
            agent.newEpisode();
            lastReward = basicTask.runSingleEpisode(1, true);
            System.out.println(i + ": " + lastReward);
        }
        return learner.getValues();
    }
    
    public static Demonstration demonstrate(){
        
        final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(true);
        HumanKeyboardAgent agent = new HumanKeyboardAgent();
        final BasicTask basicTask = new BasicTask(marioAIOptions);
        marioAIOptions.setAgent(agent);
        marioAIOptions.setLevelDifficulty(0);
        //            marioAIOptions.setLevelRandSeed(1000000);
        marioAIOptions.setLevelRandSeed(0);
        marioAIOptions.setGapsCount(false);
        //            marioAIOptions.setGameViewer(false);
        //            marioAIOptions.setMarioMode(1);
        //            marioAIOptions.setGameViewerContinuousUpdates(false);
        basicTask.setOptionsAndReset(marioAIOptions);
        double result = basicTask.runSingleEpisode(1, true);
        System.out.println(result);
        agent.getDemonstration().toFile("human_mario_" + result + ".arff");
        return agent.getDemonstration();
    }
    
    public static Shaping[] getShapings(int n, double gamma){
        Shaping[] shapes = new Shaping[n];
        for(int i=0; i<shapes.length; i++){
            shapes[i] = getShaping(8*(i/(n/5)) + (i%(n/5)), gamma);
        }
        return shapes;
    }
    
    public static Shaping getShaping(int i, double gamma){
        if(i == -1){
            return new HeuristicShaping(i, 1.0, gamma);
        }
        double[] scalings = new double[]{0.0001,0.001,0.01,0.1,1.0,10.0,100.0,1000.0};
        switch(i/scalings.length){
            default:
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                return new HeuristicShaping(i/scalings.length, scalings[i%scalings.length], gamma);
            case 5:
                return new DemonstrationShaping(scalings[i%scalings.length], gamma, new Demonstration(new String[]{"human_mario_343.arff", "human_mario_2335.arff", "human_mario_2337.arff", "human_mario_2593.arff", "human_mario_2817.arff"}));
//            case 6:
//                return new TransferShaping(scalings[i%scalings.length], gamma, learnSourceTask());
        }
//        return new TransferShaping(1.0, gamma, learnSourceTask());
    }

    public static double[] experiment(String[] args) throws Exception {
        int episodes = 5000;
        boolean visualize = false;
        
        double gamma = 0.9;

        final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);

        EnsembleAgent agent;
        
        Shaping[] shapes = new Shaping[new Integer(args[1])];
        QLambdaAgent[] ensemble = new QLambdaAgent[new Integer(args[1])];
        
        if(new Integer(args[0]) > 1){
            shapes = getShapings(new Integer(args[1]), gamma);
            for(int i=0; i<ensemble.length; i++){
                ensemble[i] = new QLambdaAgent(shapes[i], gamma);
            }
        }
                
        switch (new Integer(args[0])) {
            default:
            case 0:
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{new QLambdaAgent(getShaping(-1, gamma), gamma)}, 0.05);
                break;
            case 1:
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{new QLambdaAgent(getShaping(new Integer(args[1]), gamma), gamma)}, 0.05);
                break;
            case 2:
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{new QLambdaAgent(new CombiShaping(shapes, gamma), gamma)}, 0.05);
                break;
            case 3:
                agent = new LinearEnsembleAgent(null, ensemble, 0.05);
                break;
            case 4:
                agent = new RankingEnsembleAgent(ensemble, 0.05);
                break;
            case 5:
                agent = new ConfidenceEnsembleAgent(ensemble, 0.05);
                break;
            case 6:
                agent = new VotingEnsembleAgent(ensemble, 0.05);
                break;
        }

//    final Agent agent = new HumanKeyboardAgent();
        final BasicTask basicTask = new BasicTask(marioAIOptions);
        double[] results = new double[episodes];

        for (int i = 0; i < episodes; ++i) {
//            System.out.println(i);
            marioAIOptions.setAgent(agent);
            marioAIOptions.setLevelDifficulty(0);
            //            marioAIOptions.setLevelRandSeed(1000000);
            int level = RNG.randomInt(1000000);
            marioAIOptions.setMarioMode(RNG.randomInt(3));
            marioAIOptions.setLevelRandSeed(level);
            marioAIOptions.setGapsCount(false);
            marioAIOptions.setVisualization(false);
//            if(DEBUG && i > 1000){
//                marioAIOptions.setVisualization(true);
//            }
//                        marioAIOptions.setGameViewer(true);
            //            marioAIOptions.setMarioMode(1);
            //            marioAIOptions.setGameViewerContinuousUpdates(false);
            basicTask.setOptionsAndReset(marioAIOptions);
            agent.newEpisode();
            results[i] = basicTask.runSingleEpisode(1, true);
            if(DEBUG){
                System.out.println(results[i]);
            }
            if (visualize) {
                System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
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

    public static double[] means(double[][] stats) {
        double[] means = new double[stats[0].length];
        for (int j = 0; j < stats[0].length; j++) {
            for (int i = 0; i < stats.length; i++) {
                means[j] += stats[i][j];
            }
            means[j] = 1.0 * means[j] / (stats.length);
        }
        return means;
    }

    public static double[] xs(double[][] stats, int every) {
        double[] x = new double[stats[0].length / every];
        for (int i = 0; i < stats[0].length; i += every) {
            x[i / every] = i;
        }
        return x;
    }

    public static double[] ys(double[] stats, int every) {
        double[] x = new double[stats.length / every];
        for (int i = 0; i < stats.length; i += every) {
            x[i / every] = stats[i];
        }
        return x;
    }

    public static double[][] stds(double[][] stats, int every) {
        double[] mean = means(stats);

        double[] stdL = new double[stats[0].length / every];
        double[] stdU = new double[stats[0].length / every];
        int counterL = 0;
        int counterU = 0;

        for (int j = 0; j < stats[0].length; j += every) {
            for (int i = 0; i < stats.length; i++) {
                if (stats[i][j] < mean[j]) {
                    stdL[j / every] += Math.pow(stats[i][j] - mean[j], 2.0);
                    counterL++;
                } else {
                    stdU[j / every] += Math.pow(stats[i][j] - mean[j], 2.0);
                    counterU++;
                }
            }
            if (counterL > 0) {
                stdL[j / every] = Math.sqrt(stdL[j / every] / counterL);
            }
            if (counterU > 0) {
                stdU[j / every] = Math.sqrt(stdU[j / every] / counterU);
            }

//            stdL[j/every] = stdL[j/every];
//            stdU[j/every] = stdU[j/every];
        }

        return new double[][]{stdL, stdU};
    }

    public static int[] randomShapings(int nrShapings) {
        ArrayList<Integer> shapings = new ArrayList<Integer>();
        for (int i = 5; i < 8; i++) {
            shapings.add(i);
        }
        Collections.shuffle(shapings);
        shapings.subList(0, nrShapings);

        int[] selectedShapings = new int[nrShapings];
//        int[] selectedShapings = new int[nrShapings+1];
        for (int i = 0; i < nrShapings; i++) {
            selectedShapings[i] = shapings.get(i);
        }
//        selectedShapings[nrShapings] = 0;
        return selectedShapings;
    }
}
