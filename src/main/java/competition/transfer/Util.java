package competition.transfer;

import java.util.ArrayList;
import util.RNG;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author timbrys
 */
public class Util {
    public static int argMax(double[] v){
        ArrayList<Integer> ibest = new ArrayList<Integer>();
        ibest.add(0);
        double best = v[0];
        for(int i=1; i<v.length; i++){
            if(v[i] > best){
                best = v[i];
                ibest.clear();
                ibest.add(i);
            } else if (v[i] == best){
                ibest.add(i);
            }
        }
        return ibest.get(RNG.randomInt(ibest.size()));
    }
    
    public static int argMax(int[] v){
        ArrayList<Integer> ibest = new ArrayList<Integer>();
        ibest.add(0);
        int best = v[0];
        for(int i=0; i<v.length; i++){
            if(v[i] > best){
                best = v[i];
                ibest.clear();
                ibest.add(i);
            } else if (v[i] == best){
                ibest.add(i);
            }
        }
        return ibest.get(RNG.randomInt(ibest.size()));
    }
    
    public static boolean amongstArgMax(int arg, double[] v){
        ArrayList<Integer> ibest = new ArrayList<Integer>();
        ibest.add(0);
        double best = v[0];
        for(int i=0; i<v.length; i++){
            if(v[i] > best){
                best = v[i];
                ibest.clear();
                ibest.add(i);
            } else if (v[i] == best){
                ibest.add(i);
            }
        }
        return ibest.contains(arg);
    }
    
    public static double closestToZero(double arg0, double arg1){
        if(Math.abs(arg0) < Math.abs(arg1)){
            return arg0;
        } else {
            return arg1;
        }
    }
    
    public static boolean contains(int[] array, int element){
        for(int i=0; i<array.length; i++){
            if(array[i] == element) {
                return true;
            }
        }
        return false;
    }
}
