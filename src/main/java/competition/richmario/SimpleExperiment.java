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

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import competition.richmario.agents.*;
import competition.richmario.shapings.*;
import ch.idsia.agents.Agent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.MarioAIOptions;

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

    public static Integer usingSimilarities = 1 ; // 0 = no, 1 = full similarities, 2 = poop
    public static Integer simStage = 0;
    private Double avg = 0.0;

    public static AgentType activeAgentType;



    public static void main(String[] args) throws Exception {
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss").format(new java.util.Date());
        Logger logger = new Logger("logs/info__" + timeStamp + ".log");
        // logger.initiateLearningCurveDisplay(timeStamp);

        experimentMain(logger);
        /*
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
        */
    }

    public static double[] experimentMain(Logger logger) throws Exception {

        double[] resultsSum = null;
        int runs = 50;
        int episodesForRun = 20000;

        AgentType[] agentsToRun = new AgentType[] { AgentType.RewardShaping };

        for (AgentType agentType : agentsToRun) {
            runAgentExperiment(logger, agentType, runs, episodesForRun);
        }

        return resultsSum;
    }

    private static void runAgentExperiment(Logger logger, AgentType agentType, int runs, int episodesForRun) throws Exception {
        SimpleExperiment.activeAgentType = agentType;

        switch (agentType) {
            case BasicQLearning: {
                logger.setActiveSeries("Basic Q Learning");
                break;
            }
            case Abstraction: {
                logger.setActiveSeries("Abstraction");
                break;
            }
            case RewardShaping: {
                logger.setActiveSeries("Reward Shaping");
                break;
            }
            case Similarities: {
                logger.setActiveSeries("Similarities");
                break;
            }
            case AbstractionBasicQLearning: {
                logger.setActiveSeries("AbstractionBasicQLearning");
                break;
            }
            case SimilaritiesOnRewardShaping: {
                logger.setActiveSeries("SimilaritiesOnRewardShaping");
                break;
            }
        }

        experiment(logger, runs, episodesForRun);
    }

    private static double[] addResults(double[] resultsSum, double[] result) {

        for(int i = 0; i < result.length; ++i) {
            resultsSum[i] += result[i];
        }

        return resultsSum;
    }


    public static Integer testStepSize = 100;
    private static Integer testEpisodes = 250;


    /*public static Integer testStepSize = 1000;
    public static int episodes = 5000;
    private static Integer testEpisodes = 1000;*/


    public static void experiment(Logger logger, int runs, int episodesForRun) throws Exception {

        int evaluationEpisodes = 1000;
        int evaluationInterval = 1000;
        int maxSecondsForTrainSession = 40 * 60 * 1000;

        boolean visualize = false;

        double alpha = 0.01;
        double gamma = 0.9;
        double epsilon = 0.05;
        double lambda = 0.5;

        double[] trainMeanResults = new double[episodesForRun];
        double[] evaluationResults = new double[evaluationEpisodes];

        long startTime = System.currentTimeMillis();

        for (int run_id = 0 ; run_id < runs ; run_id++) {

//            logger.increaseRound();

            final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
            marioAIOptions.setVisualization(false);

            BasicMarioAIAgent agent;
            if (AgentType.Abstraction == SimpleExperiment.activeAgentType
                    || AgentType.AbstractionBasicQLearning == SimpleExperiment.activeAgentType) {

                agent =
                        new AbstractionLinearEnsembleAgent(
                                logger,
                                new AbstractionQLambdaAgent[]{
                                        new AbstractionQLambdaAgent(
                                                alpha,
                                                lambda,
                                                new ConstantInitialization(1.0, gamma, 0.0),
                                                gamma
                                        )
                                },
                                epsilon
                        );
            }
            else {
                agent =
                    new LinearEnsembleAgent(
                            logger,
                            new QLambdaAgent[]{
                                    new QLambdaAgent(
                                            alpha,
                                            lambda,
                                            new ConstantInitialization(1.0, gamma, 0.0),
                                            gamma
                                    )
                            },
                            epsilon
                    );
            }

            logger.info("=== Experiment #" + run_id + " ===");

            final BasicTask basicTask = new BasicTask(marioAIOptions);
            double rewardTmpSum = 0;
            long sessionCurrentTime = System.currentTimeMillis();
            double currentSessionDuration = 0;

            for (int ep = 0; ep < episodesForRun; ++ep) {

            /*if(i > 0 && i % testStepSize == 0) {
                Integer stepResultIndex = i / testStepSize;
                System.out.println("Test " + stepResultIndex + "  current episode:" + i);

                Double testResult = test(agent);
                results[stepResultIndex] = testResult;
                System.out.println("test result[" + stepResultIndex + "]:" + testResult);
            }*/

                marioAIOptions.setAgent(agent);
                marioAIOptions.setLevelDifficulty(0);
                marioAIOptions.setLevelRandSeed(RNG.randomInt(1000000));
                marioAIOptions.setMarioMode(RNG.randomInt(3));
                marioAIOptions.setGapsCount(false);
                marioAIOptions.setVisualization(visualize);
                basicTask.setOptionsAndReset(marioAIOptions);

                if (AgentType.Abstraction == SimpleExperiment.activeAgentType
                        || AgentType.AbstractionBasicQLearning == SimpleExperiment.activeAgentType) {
                    ((AbstractionEnsembleAgent)agent).newEpisode();
                }
                else {
                    ((EnsembleAgent)agent).newEpisode();
                }

                Double episodeResult = basicTask.runSingleEpisode(1, true);
                trainMeanResults[ep] = ((trainMeanResults[ep] * run_id) + episodeResult) / (run_id + 1.0);

                if ((ep+1) % logger.LOGGING_INTERVAL == 0) {
                    logger.info("ex" + run_id + "ep" + (ep+1) + " mean: " + rewardTmpSum / logger.LOGGING_INTERVAL);
                    rewardTmpSum = 0;
                }
                rewardTmpSum += episodeResult;
//                logger.addEpisodeResult(res);

                // 2. run evaluation session
                if ((ep+1) % evaluationInterval == 0) {

                    // hold session timer
                    currentSessionDuration += System.currentTimeMillis() - sessionCurrentTime;

                    logger.info("-- Evaluation --");

                    Double evaluationEpisodeResult = 0.0;
                    for (int evalEp = 0; evalEp < evaluationEpisodes; ++evalEp) {

                        marioAIOptions.setAgent(agent);
                        marioAIOptions.setLevelDifficulty(0);
                        marioAIOptions.setLevelRandSeed(RNG.randomInt(1000000));
                        marioAIOptions.setMarioMode(RNG.randomInt(3));
                        marioAIOptions.setGapsCount(false);
                        marioAIOptions.setVisualization(visualize);
                        basicTask.setOptionsAndReset(marioAIOptions);

                        if (AgentType.Abstraction == SimpleExperiment.activeAgentType
                                || AgentType.AbstractionBasicQLearning == SimpleExperiment.activeAgentType) {
                            ((AbstractionEnsembleAgent) agent).newEpisode();
                        } else {
                            ((EnsembleAgent) agent).newEpisode();
                        }

                        evaluationEpisodeResult = basicTask.runSingleEpisode(1, false);
                        evaluationResults[evalEp] = evaluationEpisodeResult;
                    }

                    logger.info("evaluation results: " + Arrays.toString(evaluationResults));
                    logger.info("mean result: " + mean(evaluationResults));

                    // continue session timer
                    sessionCurrentTime = System.currentTimeMillis();
                }

                if (currentSessionDuration+(System.currentTimeMillis()-sessionCurrentTime) > maxSecondsForTrainSession) {
                    logger.error("session timeout");
                    break;
                }
            }
        }

        double totalTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("total time: " + totalTime + " secs");
        logger.addSeriesTime(totalTime);

        logger.info("Train episodes mean: " + Arrays.toString(trainMeanResults));
        logger.info("Train experiments mean: " + SimpleExperiment.mean(trainMeanResults));
    }

    private static double mean(double[] stats) {
        double means = 0.0;
        for (int i = 0; i < stats.length; i++) {
            means += stats[i];
        }
        means = 1.0 * means / (stats.length);
        return means;
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
