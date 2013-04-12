//CSC3002F
//Network Assignment 1
//Gerard Nothnagel|Jared Norman|Daniel Burnham-King
//NTHGER001|NRMJAR001|BRNDAN022
//Group 10
//BarChart Class


/*
Code adapted from JFreeChart Class Library - Developer Guide v1.0.13, 11 May 2009, Chapter 6 pg 48-49
*/


import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
/**
* A simple demonstration application showing how to create a bar chart.
* Used by the Client class to display bar charts
*/
public class BarChart extends ApplicationFrame{


	private static final long serialVersionUID = 1L;
	public BarChart(String title, String categoryLabel, String valueLabel, List<Data> data) {
		super(title);
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		for(int i = 0; i < data.size(); i++){
			double value = data.get(i).value;
			String id = data.get(i).id;
			dataset.addValue(value, id, "");
		}
		
		JFreeChart chart = ChartFactory.createBarChart(
				title, // chart title
				categoryLabel, // domain axis label
				valueLabel, // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);
		ChartPanel chartPanel = new ChartPanel(chart, false);
		chartPanel.setPreferredSize(new Dimension(700, 350));
		setContentPane(chartPanel);
	}
/**
* Starting point for the demonstration application.
*
* @param args ignored.
*/
	public static void main(String[] args) {
		String title = "Bar Chart Example";
		String category = "Category";
		String value = "Value";
		List<Data> data = new ArrayList<Data>(30);
		
		for(int i = 0; i < 4; ++i){
			Data d = new Data("Group " + i, i+1);
			data.add(d);
		}
		
		BarChart demo = new BarChart(title, category, value, data);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}
}