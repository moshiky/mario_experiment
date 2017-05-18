package competition.richmario.tableImplementations;

/**
 * Created by Dev on 05/05/2017.
 */
public interface IQTable {

    double getKeyValue(double[] state, int action);

    void setKeyValue(double[] state, int action, double value);
}
