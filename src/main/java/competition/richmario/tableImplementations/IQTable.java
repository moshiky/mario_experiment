package competition.richmario.tableImplementations;

/**
 * Created by Dev on 05/05/2017.
 */
public interface IQTable {

    double getKeyValue(double[] state, int action);

    void setKeyValue(double[] state, int action, double value);

    void setKeyTrace(double[] state, int action, Double traceValue);

    void decayTraces(double gamma_lambda);

    void resetTraces();

    void updateByTraces(double alpha, double delta);
}
