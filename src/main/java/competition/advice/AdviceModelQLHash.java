/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package competition.advice;

/**
 *
 * @author timbrys
 */
public class AdviceModelQLHash extends QLHash{
    public AdviceModelQLHash(int size, double alpha, double gamma, double lambda, double init){
        super(size, alpha, gamma, lambda, init);
    }
    
    public void update(double delta){
        for(int i=0; i<weights.length; i++){
            weights[i] += alpha * delta;
        }
    }
    
    public void decay(){
        for(int i=0; i<weights.length; i++){
            weights[i] *= lambda;
        }
    }
}
