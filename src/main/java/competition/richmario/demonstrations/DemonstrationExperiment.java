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
package competition.richmario.demonstrations;

import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.MarioAIOptions;
import competition.richmario.agents.EnsembleAgent;
import competition.richmario.agents.HumanKeyboardAgent;
import competition.richmario.agents.LinearEnsembleAgent;
import competition.richmario.agents.QLambdaAgent;
import competition.richmario.agents.VotingEnsembleAgent;
import competition.richmario.shapings.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import util.RNG;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, sergey@idsia.ch Date: May
 * 7, 2009 Time: 4:38:23 PM Package: ch.idsia
 */
public class DemonstrationExperiment {
    
    private static final boolean DEBUG = true;

    public static void main(String[] args) throws Exception {
        int experiments = 1;
        
        mergeDemonstrations();
        
//        for(int i=0; i<20; i++){
//            demonstrateHuman();
//        }
        
        double[][] results = new double[experiments][];
        for (int i = 0; i < experiments; i++) {
            if(DEBUG){
                results[i] = experiment(new String[]{"10", "richdemos/super_human_mario.arff"});
            } else {
                results[i] = experiment(args);
            }
        }
        System.out.println(Arrays.toString(means(results)));
        System.exit(0);
    }
    
    public static void mergeDemonstrations(){
        String[] files = {"human_mario_-137.arff", "human_mario_-181.arff", "human_mario_-227.arff", "human_mario_-257.arff", "human_mario_-271.arff", "human_mario_-321.arff", "human_mario_-387.arff", "human_mario_-427.arff", "human_mario_-449.arff", "human_mario_-455.arff", "human_mario_-485.arff", "human_mario_-521.arff", "human_mario_-555.arff", "human_mario_-57.arff", "human_mario_-577.arff", "human_mario_105.arff", "human_mario_149.arff", "human_mario_1875.arff", "human_mario_1915.arff", "human_mario_1969.arff", "human_mario_2263.arff", "human_mario_2357.arff", "human_mario_475.arff", "human_mario_583.arff"};
        Demonstration d = new Demonstration(files);
        d.toFile("super_human_mario_gap.arff");
    }
    
    public static Demonstration demonstrateSimple(){
        
        final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);
        EnsembleAgent agent = new SimpleDemonstratorAgent();
        final BasicTask basicTask = new BasicTask(marioAIOptions);
        marioAIOptions.setAgent(agent);
        marioAIOptions.setLevelDifficulty(0);
        marioAIOptions.setLevelRandSeed(RNG.randomInt(1000000));
        marioAIOptions.setMarioMode(RNG.randomInt(3));
        marioAIOptions.setGapsCount(false);
        basicTask.setOptionsAndReset(marioAIOptions);
        double result = basicTask.runSingleEpisode(1, true);
        System.out.println(result);
        agent.getTrajectory().toFile("simple_mario_" + result + ".arff");
        return agent.getTrajectory();
    }
    
    public static Demonstration demonstrateHuman(){
        
        final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(true);
        HumanKeyboardAgent agent = new HumanKeyboardAgent();
        final BasicTask basicTask = new BasicTask(marioAIOptions);
        marioAIOptions.setAgent(agent);
        marioAIOptions.setLevelDifficulty(0);
        marioAIOptions.setLevelRandSeed(RNG.randomInt(1000000));
        marioAIOptions.setMarioMode(RNG.randomInt(3));
        marioAIOptions.setGapsCount(false);
        basicTask.setOptionsAndReset(marioAIOptions);
        double result = basicTask.runSingleEpisode(1, true);
        System.out.println(result);
        agent.getDemonstration().toFile("human_mario_" + result + ".arff");
        return agent.getDemonstration();
    }
    
    public static Demonstration demonstrateOptimalRL(){
        
        final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);
        double gamma = 0.9;
        EnsembleAgent agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{new QLambdaAgent(new ConstantInitialization(1.0, gamma, 0.0), new HeuristicShaping(-1, 1.0, gamma), gamma)}, 0.05);
        final BasicTask basicTask = new BasicTask(marioAIOptions);
        for (int i = 0; i < 1000; ++i) {
            marioAIOptions.setAgent(agent);
            marioAIOptions.setLevelDifficulty(0);
            marioAIOptions.setLevelRandSeed(RNG.randomInt(1000000));
            marioAIOptions.setMarioMode(RNG.randomInt(3));
            marioAIOptions.setGapsCount(false);
            marioAIOptions.setVisualization(false);
            basicTask.setOptionsAndReset(marioAIOptions);
            System.out.println(i + ": " + basicTask.runSingleEpisode(1, true));
        }
        agent.setRecording(true);
        marioAIOptions.setLevelRandSeed(RNG.randomInt(1000000));
        marioAIOptions.setMarioMode(RNG.randomInt(3));
        basicTask.setOptionsAndReset(marioAIOptions);
        double result = basicTask.runSingleEpisode(1, true);
        System.out.println("Demonstration: " + result);
        agent.getTrajectory().toFile("suboptrl_mario_" + result + ".arff");
        return agent.getTrajectory();
    }

    public static double[] experiment(String[] args) throws Exception {
        int episodes = 1000;
        boolean visualize = false;
        
        double gamma = 0.9;

        final MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);

        EnsembleAgent agent;
        QLambdaAgent learner;
        QLambdaAgent[] learners;
                
//        String[] files = new String[args.length-1];
//        for(int i=0; i<files.length; i++){
//            files[i] = args[i+1];
//        }
        String[] files = new String[]{"richdemos/human_mario_2161.arff", "richdemos/human_mario_-145.arff", "richdemos/human_mario_2297.arff", "richdemos/human_mario_-535.arff", "richdemos/human_mario_2565.arff", "richdemos/human_mario_2601.arff", "richdemos/human_mario_145.arff", "richdemos/human_mario_163.arff", "richdemos/human_mario_2211.arff", "richdemos/human_mario_2549.arff", "richdemos/human_mario_-67.arff"};
//        String[] files = new String[]{"richdemos/human_mario_2565.arff", "richdemos/human_mario_2581.arff", "richdemos/human_mario_2587.arff", "richdemos/human_mario_2601.arff", "richdemos/human_mario_2891.arff"};
                
        switch (new Integer(args[0])) {
            default:
            case 0:
                learner = new QLambdaAgent(new ConstantInitialization(1.0, gamma, 0.0), 
                        new HeuristicShaping(-1, 1.0, gamma), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, 0.05);
                break;
            case 1:
                learner = new QLambdaAgent(new ConstantInitialization(1.0, gamma, 1.0), 
                        new HeuristicShaping(-1, 1.0, gamma), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, 0.05);
                break;
            case 2:
                learner = new QLambdaAgent(new DemonstrationShaping(1.0, gamma, new Demonstration(args[1])), 
                        new HeuristicShaping(-1, 1.0, gamma), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, 0.05);
                break;
            case 3:
                learner = new QLambdaAgent(new ConstantInitialization(1.0, gamma, 0.0), 
                        new DemonstrationShaping(1.0, gamma, new Demonstration(args[1])), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, 0.05);
                break;
            case 4:
                learner = new QLambdaAgent(new DemonstrationShaping(1.0, gamma, new Demonstration(args[1])), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, 0.05);
                break;
            case 5:
                learner = new QLambdaAgent(new ConstantInitialization(1.0, gamma, 0.0),
                        new DynamicShaping(1.0, gamma, new DemonstrationShaping(1.0, gamma, new Demonstration(args[1]))), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, 0.05);
                break;
            case 6:
                learner = new QLambdaAgent(new HATShaping(1.0, gamma, args[1]),
                        new HeuristicShaping(-1, 1.0, gamma), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, 0.05);
                break;
            case 7:
                agent = new LfDAgent(args[1]);
                break;
            case 8:
                learner = new QLambdaAgent(new ConstantInitialization(1.0, gamma, 0.0),
                        new DynamicShaping(1.0, gamma, new DemonstrationShaping(1.0, gamma, new Demonstration(files))), gamma);
                agent = new LinearEnsembleAgent(null, new QLambdaAgent[]{learner}, 0.05);
                break;
            case 9:
                learners = new QLambdaAgent[files.length];
                for(int i=0; i<learners.length; i++){
                    learners[i] = new QLambdaAgent(new DemonstrationShaping(1.0, gamma, new Demonstration(files)), 
                            new HeuristicShaping(-1, 1.0, gamma), gamma);
                }
                agent = new VotingEnsembleAgent(learners, 0.05);
                break;
            case 10:
                learners = new QLambdaAgent[files.length];
                for(int i=0; i<learners.length; i++){
                    learners[i] = new QLambdaAgent(new ConstantInitialization(1.0, gamma, 0.0), 
                            new DynamicShaping(1.0, gamma, new DemonstrationShaping(1.0, gamma, new Demonstration(files[i]))), gamma);
                }
                agent = new VotingEnsembleAgent(learners, 0.05);
                break;
        }

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
