/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.irl;

import competition.richmario.StateAction;
import competition.richmario.shapings.Shaping;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import util.MyMath;

/**
 *
 * @author timbrys
 */
public class IRLShaping extends Shaping {

//    protected double[] weights = {-0.3110,-0.2989, 0.4213, 0.3382, 0.6228, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0567, 0.0001, 0.0113, 0.1502, 0.0001, 0.0001, 0.0001, -0.0237, -0.0192, -0.0153, 0.0001, -0.2250, -0.2378};
    protected double[] weights;

    public IRLShaping(double scaling, double gamma, String weightsFile) {
        super(scaling, gamma);
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(weightsFile));
            line = br.readLine();

            // use comma as separator
            String[] strWeights = line.split(cvsSplitBy);
            weights = new double[strWeights.length];
            for(int i=0; i<strWeights.length; i++){
                weights[i] = new Double(strWeights[i]);
            }
            

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    protected double actualPotential(StateAction sa) {
        return MyMath.vectorMultiplication(weights, sa.rewardFeatures());
    }

}
