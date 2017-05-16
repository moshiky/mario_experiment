/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.demonstrations;

import competition.demonstration.QValueReuseAgent;
import competition.richmario.StateAction;
import competition.richmario.agents.EnsembleAgent;
import competition.richmario.agents.QLambdaAgent;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 *
 * @author timbrys
 */
public class LfDAgent extends EnsembleAgent{

    private final ConverterUtils.DataSource source;
    private final Instances data;
    private final J48 tree;

    public LfDAgent(String file) throws Exception {
        super(null, new QLambdaAgent[0], 0.0);
        
        source = new ConverterUtils.DataSource(file);
        data = source.getDataSet();
        if (data.classIndex() == -1)
          data.setClassIndex(data.numAttributes() - 1);
        
        tree = new J48();         // new instance of tree
        tree.buildClassifier(data);   // build classifier
    }
    
    @Override
    public int egreedyActionSelection(StateAction sa) {
        try {
            Instances unlabeled = source.getStructure();
            unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
            unlabeled.add(new Instance(1.0, StateAction.normalize(sa.getState())));
            
            return (int)tree.classifyInstance(unlabeled.firstInstance());
        } catch (Exception ex) {
            Logger.getLogger(QValueReuseAgent.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
            return -1;
        }
    }

    @Override
    protected double getPreference(int i, StateAction sa) {
        return 0.0;
    }

    @Override
    protected int greedyActionSelection(StateAction sa) {
        return egreedyActionSelection(sa);
    }

    
}
