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
package competition.richmario;

import competition.richmario.shapings.*;
import ch.idsia.agents.Agent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.MarioAIOptions;
import competition.richmario.agents.EnsembleAgent;
import competition.richmario.agents.LinearEnsembleAgent;
import competition.richmario.agents.QLambdaAgent;
import competition.richmario.agents.RankingEnsembleAgent;
import competition.richmario.agents.VotingEnsembleAgent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import loggingUtils.Logger;
import sun.rmi.runtime.Log;
import util.RNG;
import weka.core.Utils;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, sergey@idsia.ch Date: May
 * 7, 2009 Time: 4:38:23 PM Package: ch.idsia
 */
public class SimpleExperiment {

    private static final boolean DEBUG = true;
    public static Integer episode = 0;
    public static Integer usingSimilarities = 1 ; // 0 = no, 1 = full similarities, 2 = poop
    public static Integer simStage = 0;
    private Double avg = 0.0;

    public static boolean isBasicQLearning;



    public static void main(String[] args) throws Exception {
        long currentTime = System.currentTimeMillis();
        Logger logger = new Logger("logs/info__" + currentTime + ".log");
        logger.initiateLearningCurveDisplay(currentTime);

        //for(simStage = 0; simStage <= 5; simStage++) {
            logger.info("Stage: " + simStage);
            for (double sim = 0.3; sim <= 0.3; sim += 0.3) {

                EnsembleAgent.strongSimFactor = sim;
                EnsembleAgent.weakSimFactor = sim / 2.0;
                logger.info("Starting with weight: " + sim);

                double[] avgs = experimentMain(logger);

                //System.out.println(Arrays.toString(means(results)));
                logger.info("FINAL weight " + sim);
                logger.info(Arrays.toString(avgs));
            }
        //}
        System.exit(0);
    }

    public static double[] experimentMain(Logger logger) throws Exception {

        double[] resultsSum = null;
        int runs = 10;

        for(int i = 0; i < runs; ++i) {
            double[] result = experiment(logger);
            logger.info(Arrays.toString(result));
            if(resultsSum == null) {
                resultsSum = result;
            } else {
                resultsSum = addResults(resultsSum, result);
            }
        }

        for(int i = 0; i < resultsSum.length; ++i) {
            resultsSum[i] = resultsSum[i] / runs;
        }

        return resultsSum;
    }

    private static double[] addResults(double[] resultsSum, double[] result) {

        for(int i = 0; i < result.length; ++i) {
            resultsSum[i] += result[i];
        }

        return resultsSum;
    }


    public static Integer testStepSize = 100;
    public static int episodes = 20000;
    private static Integer testEpisodes = 250;


    /*public static Integer testStepSize = 1000;
    public static int episodes = 5000;
    private static Integer testEpisodes = 1000;*/


    static Date start = null;
    static Date end = null;

    public static double[] experiment(Logger logger) throws Exception {

        boolean visualize = false;

        double alpha = 0.01;
        double gamma = 0.9;
        double epsilon = 0.05;
        double lambda = 0.5;

        final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);

        EnsembleAgent agent;

        agent = new LinearEnsembleAgent(logger, new QLambdaAgent[]{new QLambdaAgent(alpha, lambda, new ConstantInitialization(1.0, gamma, 0.0), gamma)}, epsilon);

        final BasicTask basicTask = new BasicTask(marioAIOptions);
        double[] results = new double[episodes / testStepSize + 1];
        double rewardTmpSum = 0;

        for (int i = 0; i <= episodes; ++i) {

            if(i > 0 && i % testStepSize == 0) {
                Integer stepResultIndex = i / testStepSize;
                //System.out.println("Test " + stepResultIndex + "  current episode:" + i);

                //Double testResult = test(agent);
                //results[stepResultIndex] = testResult;
                //System.out.println("test result[" + stepResultIndex + "]:" + testResult);
            }

            episode = i;
            marioAIOptions.setAgent(agent);
            marioAIOptions.setLevelDifficulty(0);
            marioAIOptions.setLevelRandSeed(RNG.randomInt(1000000));
            marioAIOptions.setMarioMode(RNG.randomInt(3));
            marioAIOptions.setGapsCount(false);
            marioAIOptions.setVisualization(visualize);
            basicTask.setOptionsAndReset(marioAIOptions);
            agent.newEpisode();

            Double res = basicTask.runSingleEpisode(1, true);

            if(i % logger.LOGGING_INTERVAL == 0) {
                logger.info("step #" + i + " res=" + rewardTmpSum/logger.LOGGING_INTERVAL);
                rewardTmpSum = 0;
            }
            rewardTmpSum += res;
            logger.addEpisodeResult(res);

        }


        return results;
    }


    private static Double test(EnsembleAgent agent) {
        Integer episodes = testEpisodes;
        double alpha = 0.01;
        double gamma = 0.9;
        double epsilon = 0.05;
        double lambda = 0.5;

        final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);

        final BasicTask basicTask = new BasicTask(marioAIOptions);
        Double sum = 0.0;

        Integer prevSim = SimpleExperiment.usingSimilarities;
        SimpleExperiment.usingSimilarities = 2;

        for (int i = 0; i <= episodes; ++i) {

            marioAIOptions.setAgent(agent);
            marioAIOptions.setLevelDifficulty(0);
            marioAIOptions.setLevelRandSeed(RNG.randomInt(1000000));
            marioAIOptions.setMarioMode(RNG.randomInt(3));
            marioAIOptions.setGapsCount(false);
            marioAIOptions.setVisualization(false);
            basicTask.setOptionsAndReset(marioAIOptions);
            agent.newEpisode();

            sum += basicTask.runSingleEpisode(1, true);

        }

        SimpleExperiment.usingSimilarities = prevSim;

        return sum / episodes;
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
