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
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.MarioAIOptions;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import util.RNG;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, sergey@idsia.ch Date: May
 * 7, 2009 Time: 4:38:23 PM Package: ch.idsia
 */
public class Custom {

    public static void main(String[] args) throws Exception {
        int experiments = 1;

//        Demonstration d = new Demonstration("demo/super_mario_0.arff");
//        System.out.println(d.size());
        
        double[][] results = new double[experiments][];
        for (int i = 0; i < experiments; i++) {
//            results[i] = experiment(args);
            results[i] = experiment(new String[]{"0","demo/rlexpert_mario_3113.arff"});
//        System.out.println(Arrays.toString(results[i]));
//            demonstrate();
        }
        System.out.println(Arrays.toString(means(results)));
        System.exit(0);
    }
    
    public static Demonstration demonstrate(){
        
        final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(true);
        HumanKeyboardAgent agent = new HumanKeyboardAgent();
        final BasicTask basicTask = new BasicTask(marioAIOptions);
        marioAIOptions.setAgent(agent);
        marioAIOptions.setLevelDifficulty(0);
        marioAIOptions.setLevelRandSeed(RNG.randomInt(1000000));
//        marioAIOptions.setLevelRandSeed(0);
        marioAIOptions.setGapsCount(false);
        //            marioAIOptions.setGameViewer(false);
        marioAIOptions.setMarioMode(RNG.randomInt(3));
        //            marioAIOptions.setGameViewerContinuousUpdates(false);
        basicTask.setOptionsAndReset(marioAIOptions);
        double result = basicTask.runSingleEpisode(1, true);
        System.out.println(result);
//        agent.getDemonstration().toFile("human_mario_" + result + ".arff");
        return agent.getDemonstration();
    }
    
    
    public static Demonstration demonstrateSimple(){
        
        final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);
        SimpleAgent agent = new SimpleAgent();
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
        agent.getDemonstration().toFile("simple_mario_" + result + ".arff");
        return agent.getDemonstration();
    }
    
    public static Demonstration demonstrateRL(){
        
        final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);
        QLambdaAgent agent = new QLambdaAgent(AgentType.NoShaping, new Demonstration(-1), new Demonstration(-1));
        final BasicTask basicTask = new BasicTask(marioAIOptions);
        double result = 0;
        for(int i=0; i<1000; i++){
            marioAIOptions.setAgent(agent);
            marioAIOptions.setLevelDifficulty(0);
            //            marioAIOptions.setLevelRandSeed(1000000);
            marioAIOptions.setLevelRandSeed(0);
            marioAIOptions.setGapsCount(false);
            //            marioAIOptions.setGameViewer(false);
            //            marioAIOptions.setMarioMode(1);
            //            marioAIOptions.setGameViewerContinuousUpdates(false);
            basicTask.setOptionsAndReset(marioAIOptions);
            result = basicTask.runSingleEpisode(1, true);
            System.out.println(result);
        }
//        agent.setEpsilon(0.0);
//        agent.setAlpha(0.0);
//        result = basicTask.runSingleEpisode(1, true);
//        System.out.println(result);
        agent.getDemonstration().toFile("rlexpert_mario_" + result + ".arff");
        return agent.getDemonstration();
    }

    public static double[] experiment(String[] args) throws Exception {

        int episodes = 1000;
        int level = 0;
        boolean visualize = false;

        final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);

        Agent agent;

        switch (new Integer(args[0])) {
            default:
            case 0:
                agent = new QLambdaAgent(AgentType.NoShaping, new Demonstration(-1), new Demonstration(-1));
                break;
            case 1:
                agent = new QLambdaAgent(AgentType.DemonstrationShaping, new Demonstration(-1), new Demonstration(args[1]));
                break;
            case 2:
                agent = new QValueReuseAgent(AgentType.NoShaping, new Demonstration(-1), args[1]);
                break;
            case 3:
                agent = new LfDAgent(args[1]);
        }

//    final Agent agent = new HumanKeyboardAgent();
        final BasicTask basicTask = new BasicTask(marioAIOptions);
        double[] results = new double[episodes];

        for (int i = 0; i < episodes; ++i) {
            marioAIOptions.setAgent(agent);
            marioAIOptions.setLevelDifficulty(0);
            //            marioAIOptions.setLevelRandSeed(1000000);
            marioAIOptions.setLevelRandSeed(level);
            marioAIOptions.setGapsCount(false);
            //            marioAIOptions.setGameViewer(false);
            //            marioAIOptions.setMarioMode(1);
            //            marioAIOptions.setGameViewerContinuousUpdates(false);
            basicTask.setOptionsAndReset(marioAIOptions);
            results[i] = basicTask.runSingleEpisode(1, true);
            System.out.println(results[i]);
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
