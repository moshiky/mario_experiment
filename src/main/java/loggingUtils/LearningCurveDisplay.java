package loggingUtils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.TextAnchor;
import util.RNG;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by moshecohen on 27/04/2017.
 */
public class LearningCurveDisplay {

    private double m_updateInterval;
    private JPanel panel;

    private JFreeChart scatterChart;
    private ChartPanel chartPanel;
    private DefaultXYDataset dataset;

    private Map<String, Double> m_tempResultSum;
    private Map<String, Integer> m_episodeCounter;
    private Map<String, Double[][]> m_seriesData;
    private String m_activeSeriesName;

    private int m_round;
    private String m_timeStamp;


    public LearningCurveDisplay(String timeStamp, int updateInterval) {

        this.m_updateInterval = updateInterval;

        this.m_tempResultSum = new HashMap<>();
        this.m_activeSeriesName = "default";

        this.m_episodeCounter = new HashMap<>();
        this.m_seriesData = new HashMap<>();

        this.m_round = 0;
        this.m_timeStamp = timeStamp;

        this.setActiveSeries(this.m_activeSeriesName);

        dataset = new DefaultXYDataset();
        scatterChart =
                ChartFactory.createXYLineChart(
                        "Learning Curve",
                        "episode (x" + this.m_updateInterval + ")",
                        "reward mean", dataset
                );

        chartPanel = new ChartPanel(scatterChart);

        panel.add(chartPanel);
        panel.validate();
    }

    public JPanel getPanel() { return panel; }

    public void addSample(double result) {
        double tempResultSum = this.m_tempResultSum.get(this.m_activeSeriesName);
        int episodeCounter = this.m_episodeCounter.get(this.m_activeSeriesName);

        if ((episodeCounter+1) % this.m_updateInterval == 0) {
            double x = (episodeCounter+1) / this.m_updateInterval;
            double y = tempResultSum / this.m_updateInterval;

            this.addPoint(x, y);
            tempResultSum = 0;
        }

        this.m_episodeCounter.put(this.m_activeSeriesName, episodeCounter+1);
        this.m_tempResultSum.put(this.m_activeSeriesName, tempResultSum+result);
    }

    private void addPoint(double x, double y) {
        Double[][] oldSeriesData = this.m_seriesData.get(this.m_activeSeriesName);
        Double[][] newSeriesData = null;

        if (this.m_round > 1) {
            Double oldValue = oldSeriesData[1][(int)x-1];
            oldSeriesData[1][(int)x-1] = ((this.m_round-1) * oldValue + y) / this.m_round;
            newSeriesData = oldSeriesData;
        }
        else {
            newSeriesData = new Double[oldSeriesData.length][oldSeriesData[0].length + 1];

            for (int i = 0; i < oldSeriesData.length; i++) {
                System.arraycopy(oldSeriesData[i], 0, newSeriesData[i], 0, oldSeriesData[0].length);
            }

            newSeriesData[0][newSeriesData[0].length - 1] = x;
            newSeriesData[1][newSeriesData[1].length - 1] = y;
        }

        double[][] seriesData = new double[newSeriesData.length][newSeriesData[0].length];
        for (int i = 0; i < newSeriesData.length; i++) {
            for (int j = 0; j < newSeriesData[0].length; j++) {
                seriesData[i][j] = newSeriesData[i][j];
            }
        }

        dataset.addSeries(this.m_activeSeriesName, seriesData);
        this.m_seriesData.put(this.m_activeSeriesName, newSeriesData);
    }

    public void setActiveSeries(String seriesName) {
        this.m_activeSeriesName = seriesName;
        this.m_round = 0;

        if (!this.m_episodeCounter.containsKey(seriesName)) {
            this.m_episodeCounter.put(seriesName, 0);
            this.m_tempResultSum.put(seriesName, 0.0);
            this.m_seriesData.put(seriesName, new Double[2][0]);
        }
    }

    public void resetRound() {
        this.m_episodeCounter.put(this.m_activeSeriesName, 0);
        this.m_tempResultSum.put(this.m_activeSeriesName, 0.0);
    }

    public void addSeriesTime(long timeInSecs) {
        Double[][] seriesData = this.m_seriesData.get(this.m_activeSeriesName);
        final XYPlot plot = scatterChart.getXYPlot();

        double xValue = seriesData[0][seriesData[0].length-1];
        double yValue = seriesData[1][seriesData[1].length-1];

        String label = yValue + ", \n" + timeInSecs + " secs";
        final XYPointerAnnotation pointer =
                new XYPointerAnnotation(label, xValue, yValue, (Math.PI / -2.0) - RNG.randomDouble() * (Math.PI / 4));

        pointer.setBaseRadius(35.0);
        pointer.setTipRadius(2.0);
        pointer.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pointer.setPaint(plot.getRenderer().getSeriesPaint(this.dataset.indexOf(this.m_activeSeriesName)));
        pointer.setTextAnchor(TextAnchor.HALF_ASCENT_RIGHT);
        plot.addAnnotation(pointer);
    }

    public void increaseRound() {
        this.m_round++;
    }

    public void saveChart() {
        // create graphs folder
        if (Files.notExists(FileSystems.getDefault().getPath("graphs"))) {
            new File("graphs").mkdirs();
        }

        try {
            ScreenImage.writeImage(
                    ScreenImage.createImage(this.chartPanel),
                    "graphs/graph__" + this.m_timeStamp + ".jpg"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
