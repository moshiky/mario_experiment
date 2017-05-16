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

import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.MarioAIOptions;
import competition.richmario.agents.EnsembleAgent;
import competition.richmario.agents.LinearEnsembleAgent;
import competition.richmario.agents.QLambdaAgent;
import competition.richmario.irl.MultivariateIRLShaping;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import util.RNG;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, sergey@idsia.ch Date: May
 * 7, 2009 Time: 4:38:23 PM Package: ch.idsia
 */
public class InitExperiment {
    
    private static final boolean DEBUG = false;

    public static void main(String[] args) throws Exception {
        int experiments = 1;
        
        double[][] results = new double[experiments][];
        for (int i = 0; i < experiments; i++) {
            if(DEBUG){
                results[i] = experiment(new String[]{"1"});
            } else {
                results[i] = experiment(args);
            }
        }
        System.out.println(Arrays.toString(means(results)));
        System.exit(0);
//        String[] files = new String[]{"richdemos/human_mario_163.arff", "richdemos/human_mario_863.arff", "richdemos/human_mario_2581.arff", "richdemos/human_mario_901.arff", "richdemos/human_mario_2891.arff", "richdemos/human_mario_2211.arff", "richdemos/human_mario_2565.arff", "richdemos/human_mario_2549.arff", "richdemos/human_mario_2297.arff", "richdemos/human_mario_2601.arff"};
//        
//        Demonstration d = new Demonstration(files);
//        d.toFile("richdemos/bener_human_mario.arff");
    }
    
    public static double[] experiment(String[] args) throws Exception {
        int episodes = 50000;
        boolean visualize = false;
        
        double gamma = 0.9;
        double epsilon = 0.05;

        final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);

        EnsembleAgent agent;
        QLambdaAgent learner;
        
        switch (new Integer(args[0])) {
            default:
            case 0:
                learner = new QLambdaAgent(new ConstantInitialization(1.0, gamma, 0.0), 
                        new HeuristicShaping(-1, 1.0, gamma), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, epsilon);
                break;
            case 1:
                learner = new QLambdaAgent(new ConstantInitialization(1.0, gamma, 1.0), 
                        new HeuristicShaping(-1, 1.0, gamma), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, epsilon);
                break;
            case 2:
                learner = new QLambdaAgent(new ConstantInitialization(1.0, gamma, -1.0), 
                        new HeuristicShaping(-1, 1.0, gamma), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, epsilon);
                break;
            case 3:
                learner = new QLambdaAgent(new ConstantInitialization(1.0, gamma, 10.0), 
                        new HeuristicShaping(-1, 1.0, gamma), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, epsilon);
                break;
            case 4:
                learner = new QLambdaAgent(new ConstantInitialization(1.0, gamma, -10.0), 
                        new HeuristicShaping(-1, 1.0, gamma), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, epsilon);
                break;
            case 5:
                learner = new QLambdaAgent(new ConstantInitialization(1.0, gamma, 0.0),
                        new DynamicShaping(1.0, gamma, new MultivariateIRLShaping(10.0, gamma, 
                        "IRL/multi_reward_mario_data_09282015/alg2_mario_reward_weights.csv",
                        "IRL/multi_reward_mario_data_09282015/alg2_mario_component_means.csv",
                        "IRL/multi_reward_mario_data_09282015/alg2_mario_component_covariance_matrices.csv")), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, epsilon);
                break;
            case 6:
                learner = new QLambdaAgent(new ConstantInitialization(1.0, gamma, 1.0),
                        new DynamicShaping(1.0, gamma, new MultivariateIRLShaping(1.0, gamma, 
                        "IRL/multi_reward_mario_data_09282015/alg2_mario_reward_weights.csv",
                        "IRL/multi_reward_mario_data_09282015/alg2_mario_component_means.csv",
                        "IRL/multi_reward_mario_data_09282015/alg2_mario_component_covariance_matrices.csv")), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, epsilon);
                break;
        }

//    final Agent agent = new HumanKeyboardAgent();
        final BasicTask basicTask = new BasicTask(marioAIOptions);
        double[] results = new double[episodes];

        for (int i = 0; i < episodes; ++i) {
            marioAIOptions.setAgent(agent);
            marioAIOptions.setLevelDifficulty(0);
            marioAIOptions.setLevelRandSeed(RNG.randomInt(1000000));
            marioAIOptions.setMarioMode(RNG.randomInt(3));
            marioAIOptions.setGapsCount(false);
            marioAIOptions.setVisualization(false);
//            if(DEBUG && i > 10000){
//                marioAIOptions.setVisualization(true);
//            }
            basicTask.setOptionsAndReset(marioAIOptions);
            agent.newEpisode();
            results[i] = basicTask.runSingleEpisode(1, true);
            if(DEBUG){
                System.out.println(i + ": " + results[i]);
            }
            if (visualize) {
                System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
            }
        }
        return results;
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
