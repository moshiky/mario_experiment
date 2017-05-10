/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.demonstration;

import java.util.Random;
import weka.classifiers.Evaluation;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
/**
 *
 * @author timbrys
 */
public class WekaTest {
    public static void main(String[] args) throws Exception{
        DataSource source = new DataSource("rlexpert_mario_3069.arff");
        Instances data = source.getDataSet();
        // setting class attribute if the data format does not provide this information
        // For example, the XRFF format saves the class attribute information as well
        if (data.classIndex() == -1)
          data.setClassIndex(data.numAttributes() - 1);
        
//        String[] options = new String[1];
//        options[0] = "-U";            // unpruned tree
        J48 tree = new J48();         // new instance of tree
//        tree.setOptions(options);     // set the options
        tree.buildClassifier(data);   // build classifier
        
        Instances unlabeled = source.getStructure();
        unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
        unlabeled.add(new Instance(1.0, new double[]{0.0,0.0,1.0,0.0,0.0,0.0}));
        
        System.out.println(tree.classifyInstance(unlabeled.firstInstance()));
        
//         Evaluation eval = new Evaluation(data);
//         eval.crossValidateModel(tree, data, 10, new Random(1));
//         System.out.println(eval.toSummaryString("\nResults\n======\n", false));
    }
}
