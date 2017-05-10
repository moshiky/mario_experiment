/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.richmario.shapings;

import competition.richmario.StateAction;

/**
 *
 * @author timbrys
 */
public class HeuristicShaping extends Shaping {

    protected int id;
    
    public HeuristicShaping(int id, double scaling, double gamma){
        super(scaling, gamma);
        this.id = id;
    }
    
    @Override
    protected double actualPotential(StateAction sa) {
        double potential;
        switch(id){
            default:
                potential = 0.0;
                break;
            case 0:
//                potential = 0.0001*sa.getExtraState()[0];
                potential = 1.0*sa.getExtraState()[0];
                break;
            case 1:
//                potential = 0.01*(250-sa.getExtraState()[1]);
                potential = 1.0*(250-sa.getExtraState()[1]);
                break;
            case 2:
                if(sa.getAction() == 2 || sa.getAction() == 5 || sa.getAction() == 8 || sa.getAction() == 11){
//                    potential = 0.0001;
                    potential = 1.0;
                } else {
                    potential = 0.0;
                }
                break;
            case 3:
                if(sa.getAction() == 3 || sa.getAction() == 4 || sa.getAction() == 5 || sa.getAction() == 9 || sa.getAction() == 10 || sa.getAction() == 11){
//                    potential = 0.0001;
                    potential = 1.0;
                } else {
                    potential = 0.0;
                }
                break;
            case 4:
                if(sa.getAction() == 6 || sa.getAction() == 7 || sa.getAction() == 8 || sa.getAction() == 9 || sa.getAction() == 10 || sa.getAction() == 11){
                    potential = 1.0;//1.0;
                } else {
                    potential = 0.0;
                }
                break;
            case 5:
                if(sa.getAction() == 2 || sa.getAction() == 5 || sa.getAction() == 11){
                    potential = 1.0;//1.0;
                } else {
                    potential = 0.0;
                }
                break;
        }
        return potential;
    }
    
}
