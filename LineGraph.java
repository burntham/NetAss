//CSC3002F
//Network Assignment 1
//Gerard Nothnagel|Jared Norman|Daniel Burnham-King
//NTHGER001|NRMJAR001|BRNDAN022
//Group 10
//Line Graph

/*Code adapted from JFreeChart Class Library - Developer Guide v1.0.13, 11 May 2009, Chapter  7, Pages 59-61
 */

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

public class LineGraph extends ApplicationFrame {

	private static final long serialVersionUID = 1L;
	private String xLabel;
	private String yLabel;
	private String heading;
	private boolean draw4;
	private boolean draw8;
	private boolean draw10;
	List<Data> data4;
	List<Data> data8;
	List<Data> data10;
	
	public LineGraph(String title, String heading, String xLabel, String yLabel, 
			boolean draw4, boolean draw8, boolean draw10, List<Data> data4,  List<Data> data8,  List<Data> data10) {
		
		super(title);
		
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		this.heading = heading;
		
		this.draw4 = draw4;
		this.draw8 = draw8;
		this.draw10 = draw10;
		
		this.data4 = data4;
		this.data8 = data8;
		this.data10 = data10;
		
		XYDataset dataset = createDataset();
		JFreeChart chart = createChart(dataset);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}

	private XYDataset createDataset() {
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		if(draw4){
			XYSeries series1 = new XYSeries("Group 4");
			fillSeries(series1, data4);
			dataset.addSeries(series1);
		}
		if(draw8){
			XYSeries series2 = new XYSeries("Group 8");
			fillSeries(series2, data8);
			dataset.addSeries(series2);
		}
		if(draw10){
			XYSeries series3 = new XYSeries("Group 10");
			fillSeries(series3, data10);
			dataset.addSeries(series3);
		}
		
		return dataset;
	}
	
	private void fillSeries(XYSeries series, List<Data> data){
		for(int i = 0; i < data.size(); ++i){
			double x = data.get(i).x;
			double y = data.get(i).y;
			series.add(x, y);
		}
	}
	
	private JFreeChart createChart(XYDataset dataset) {
		
		JFreeChart chart = ChartFactory.createXYLineChart(
		heading,
		xLabel, 
		yLabel, 
		dataset,
		PlotOrientation.VERTICAL,
		true, 
		true, 
		false 
		);
		
		chart.setBackgroundPaint(Color.white);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		
		
		return chart;
	}

	public JPanel createDemoPanel() {
		JFreeChart chart = createChart(createDataset());
		return new ChartPanel(chart);
	}

	public static void main(String[] args) {
		
		List<Data> data4 = new ArrayList<Data>();
		List<Data> data8 = new ArrayList<Data>();
		List<Data> data10 = new ArrayList<Data>();
		
		int n = -1;
		for(int i = 0; i < 100; ++i){
			n *= -1;
			Data d1 = new Data(i, i*n);
			data4.add(d1);
			Data d2 = new Data(i, i*2*n);
			data8.add(d2);
			Data d3 = new Data(i, i*3*n);
			data10.add(d3);
		}
		
		LineGraph chart = new LineGraph("Line Chart", "Heading", "X Label", "Y Label", 
				true, true, true, data4, data8, data10);
		chart.pack();
		RefineryUtilities.centerFrameOnScreen(chart);
		chart.setVisible(true);
	}
}