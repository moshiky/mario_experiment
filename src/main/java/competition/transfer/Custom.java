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
package competition.transfer;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.MarioAIOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, sergey@idsia.ch Date: May
 * 7, 2009 Time: 4:38:23 PM Package: ch.idsia
 */
public class Custom {

    public static void main(String[] args) {
        int experiments = 1;

        double[][] results = new double[experiments][];
        for (int i = 0; i < experiments; i++) {
            results[i] = experiment(args);
//        System.out.println(Arrays.toString(results[i]));
        }
        System.out.println(Arrays.toString(means(results)));
        System.exit(0);
    }
    
    public static competition.fa.QLambdaAgent learnSourceTaskOtherState(int level){
        competition.fa.QLambdaAgent agent = new competition.fa.QLambdaAgent(new int[]{1,2}, competition.fa.AgentType.Supershaping);
        
        MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);
        final BasicTask basicTask = new BasicTask(marioAIOptions);

        double lastReward = -512;
        for (int i = 0; i < 100; ++i) {
            marioAIOptions.setAgent(agent);
            marioAIOptions.setLevelDifficulty(0);
            marioAIOptions.setLevelRandSeed(level);
            marioAIOptions.setGapsCount(false);
            basicTask.setOptionsAndReset(marioAIOptions);
            lastReward = basicTask.runSingleEpisode(1, true);
            System.out.println(i + ": " + lastReward);
        }
        return agent;
    }
    
    public static QLambdaAgent learnSourceTask(int level){
        QLambdaAgent agent = new QLambdaAgent(AgentType.NoShaping);
        
        MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);
        final BasicTask basicTask = new BasicTask(marioAIOptions);

        double lastReward = -512;
        for (int i = 0; i < 100; ++i) {
            marioAIOptions.setAgent(agent);
            marioAIOptions.setLevelDifficulty(0);
            marioAIOptions.setLevelRandSeed(level);
            marioAIOptions.setGapsCount(false);
            marioAIOptions.setEnemies("off");
            basicTask.setOptionsAndReset(marioAIOptions);
            lastReward = basicTask.runSingleEpisode(1, true);
            System.out.println(i + ": " + lastReward);
        }
        return agent;
    }

    public static double[] experiment(String[] args) {

        int episodes = 100;
        int level = 0;

        MarioAIOptions marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);

        QLambdaAgent agent1 = learnSourceTask(0);
//        QLambdaAgent agent2 = learnSourceTask(0);
//        QLambdaAgent agent3 = learnSourceTask(0);
        
        BasicTask basicTask = new BasicTask(marioAIOptions);
        BasicMarioAIAgent targetAgent;
        switch(0){//new Integer(args[0])) {
            default:
            case 0:
                targetAgent = new QLambdaAgent(AgentType.NoShaping);
                break;
            case 1:
                targetAgent = new QLambdaAgentTransferred(AgentType.NoShaping, agent1.getQs());
                break;
            case 2:
                targetAgent = new QValueReuseAgent(AgentType.NoShaping, agent1.getQs());
                break;
            case 3:
                targetAgent = new QLambdaPolicyTransferAgent(AgentType.NoShaping, agent1.getQs());
                break;
            case 4:
                targetAgent = new PolicyReuseQLambdaAgent(AgentType.NoShaping, agent1.getQs());
                break;
            case 5:
                targetAgent = new QLambdaReuseWShapingTransferAgent(AgentType.NoShaping, agent1.getQs());
                break;
            case 6:
                competition.fa.QLambdaAgent otherAgent = learnSourceTaskOtherState(0);
                targetAgent = new DSQLambdaPolicyTransferAgent(AgentType.NoShaping, otherAgent);
                break;
            case 7:
                competition.fa.QLambdaAgent otherAgent2 = learnSourceTaskOtherState(0);
                targetAgent = new DSPolicyReuseQLambdaAgent(AgentType.NoShaping, otherAgent2);
                break;
            case 8:
                competition.fa.QLambdaAgent otherAgent3 = learnSourceTaskOtherState(0);
                targetAgent = new DSLinearQLambdaPolicyTransferAgent(AgentType.NoShaping, new QLHash[]{agent1.getQs()}, new competition.fa.QLambdaAgent[]{otherAgent3});
                break;
            case 9:
                targetAgent = new competition.fa.QLambdaAgent(new int[]{1,2}, competition.fa.AgentType.Supershaping);
                break;
        }

        marioAIOptions = new MarioAIOptions(new String[]{});
        marioAIOptions.setVisualization(false);
        double[] results = new double[episodes * 10];
        for (int i = 0; i < episodes * 10; ++i) {

            marioAIOptions.setAgent(targetAgent);
            marioAIOptions.setLevelDifficulty(0);
            marioAIOptions.setLevelRandSeed(level);
            marioAIOptions.setGapsCount(false);
            basicTask.setOptionsAndReset(marioAIOptions);
            results[i] = basicTask.runSingleEpisode(1, true);
            System.out.println(i + ": " + results[i]);
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
