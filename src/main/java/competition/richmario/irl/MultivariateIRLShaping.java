/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.irl;

import competition.richmario.StateAction;
import competition.richmario.shapings.ConstantInitialization;
import competition.richmario.shapings.DynamicShaping;
import competition.richmario.shapings.Shaping;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import util.MyMath;

/**
 *
 * @author timbrys
 */
public class MultivariateIRLShaping extends DynamicShaping{
    protected double[][] weights;
    protected MultivariateNormalDistribution[] distributions;
    protected double[] normalizations;
    
    public MultivariateIRLShaping(double scaling, double gamma, String weightsFile, String meansFile, String varsFile){
        super(scaling, gamma, new ConstantInitialization(1.0, gamma, 0.0));
        
        double[][] means;
        double[][][] covariances;
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";
        int nrGaussians = 0;
        int nrDimensions = 0;
        try {

            //count number of gaussians
            br = new BufferedReader(new FileReader(weightsFile));
            while ((line = br.readLine()) != null) {
                nrGaussians++;
            }
            br.close();
            
            weights = new double[nrGaussians][];

            //read weights
            br = new BufferedReader(new FileReader(weightsFile));
            for(int gaussian=0; (line = br.readLine()) != null; gaussian++) {
                String[] strWeights = line.split(cvsSplitBy);
                weights[gaussian] = new double[strWeights.length];
                for(int i=0; i<strWeights.length; i++){
                    weights[gaussian][i] = new Double(strWeights[i]);
                }
            }
            br.close();
            
            means = new double[nrGaussians][];
            
            //read means
            br = new BufferedReader(new FileReader(meansFile));
            for(int gaussian=0; (line = br.readLine()) != null; gaussian++) {
                String[] strWeights = line.split(cvsSplitBy);
                means[gaussian] = new double[strWeights.length];
                for(int i=0; i<strWeights.length; i++){
                    means[gaussian][i] = new Double(strWeights[i]);
                }
            }
            br.close();
            
            nrDimensions = weights[0].length;
            covariances = new double[nrGaussians][nrDimensions][nrDimensions];
            
            //read covariances
            br = new BufferedReader(new FileReader(varsFile));
            for(int row=0; (line = br.readLine()) != null; row++) {
                String[] strWeights = line.split(cvsSplitBy);
                for(int i=0; i<strWeights.length; i++){
//                    covariances[i/nrDimensions][row][i%nrDimensions] = new Double(strWeights[i]);
                    covariances[row/nrDimensions][row%nrDimensions][i] = new Double(strWeights[i]);
                }
            }
            br.close();
            
            distributions = new MultivariateNormalDistribution[nrGaussians];
            normalizations = new double[nrGaussians];
            for(int i=0; i<distributions.length; i++){
                try{
                    distributions[i] = new MultivariateNormalDistribution(means[i], covariances[i]);
                } catch(SingularMatrixException ex){
                    System.err.println("Covariance matrix " + i + " is not invertible");
                    distributions[i] = null;
                }
                RealMatrix m = new Array2DRowRealMatrix(covariances[i]);
                normalizations[i] = (Math.sqrt(Math.pow(2*Math.PI, nrDimensions)*(new LUDecomposition(m).getDeterminant())));
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
    
//    @Override
//    protected double actualPotential(StateAction sa) {
//        double product = 0.0;
//        int counter = 0;
//        for(int i=0; i<distributions.length; i++){
//            if(distributions[i] != null){
//                if(distributions[i].density(sa.rewardFeatures())*normalizations[i] > 1){
//                    System.out.println(distributions[i].density(sa.rewardFeatures())*normalizations[i]);
//                }
//                product += (distributions[i].density(sa.rewardFeatures())*normalizations[i]) * 
//                        MyMath.vectorMultiplication(weights[i], sa.rewardFeatures());
//                counter++;
//            }
//        }
//        return product;
//    }
    
    
    
    protected double rp(StateAction sa1, StateAction sa2){
        double product = 0.0;
        for(int i=0; i<distributions.length; i++){
            if(distributions[i] != null){
                product += (distributions[i].density(sa1.rewardFeatures())*normalizations[i]) * 
                        MyMath.vectorMultiplication(weights[i], sa2.rewardFeatures());
            }
        }
        return product;
    }
    
    
    @Override
    protected float reward(StateAction sa1, double reward, StateAction sa2){
        return (float)(rp(sa1,sa2)-rp(sa1,sa1));
    }
    
}
