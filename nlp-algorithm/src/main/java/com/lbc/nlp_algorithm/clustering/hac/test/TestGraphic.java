//package com.jd.brain.xnlp.clusering.test;
//
//import java.awt.Color;
//
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.annotations.XYPointerAnnotation;
//import org.jfree.chart.axis.NumberAxis;
//import org.jfree.chart.block.BlockContainer;
//import org.jfree.chart.block.BorderArrangement;
//import org.jfree.chart.block.EmptyBlock;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.chart.plot.XYPlot;
//import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
//import org.jfree.chart.title.CompositeTitle;
//import org.jfree.chart.title.LegendTitle;
//import org.jfree.data.xy.XYDataset;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;
//import org.jfree.ui.ApplicationFrame;
//import org.jfree.ui.RectangleEdge;
//import org.jfree.ui.RefineryUtilities;
//import org.jfree.ui.TextAnchor;
//
//public class TestGraphic extends ApplicationFrame {
//	private static final long serialVersionUID = 4540402465397663673L;
//
//	public TestGraphic(String s) {
//		super(s);
//		setContentPane(createDemoPanel());
//	}
//
//	private static XYDataset createDataset1() {
//		XYSeries xyseries = new XYSeries("Random Data 1");
//		XYSeriesCollection xyseriescollection = new XYSeriesCollection();
//		xyseriescollection.addSeries(xyseries);
//		return xyseriescollection;
//	}
//
//	private static XYDataset createDataset2() {
//		XYSeries xyseries = new XYSeries("Random Data 2");
//		xyseries.add(1.0D, 429.60000000000002D);
//		xyseries.add(2D, 323.19999999999999D);
//		xyseries.add(3D, 417.19999999999999D);
//		xyseries.add(4D, 624.10000000000002D);
//		xyseries.add(5D, 422.60000000000002D);
//		xyseries.add(6D, 619.20000000000005D);
//		xyseries.add(7D, 416.5D);
//		xyseries.add(8D, 512.70000000000005D);
//		xyseries.add(9D, 501.5D);
//		xyseries.add(10D, 306.10000000000002D);
//		xyseries.add(11D, 410.30000000000001D);
//		xyseries.add(12D, 511.69999999999999D);
//		xyseries.add(13D, 611D);
//		xyseries.add(14D, 709.60000000000002D);
//		xyseries.add(15D, 613.20000000000005D);
//		xyseries.add(16D, 711.60000000000002D);
//		xyseries.add(17D, 708.79999999999995D);
//		xyseries.add(18D, 501.60000000000002D);
//		XYSeriesCollection xyseriescollection = new XYSeriesCollection();
//		xyseriescollection.addSeries(xyseries);
//		return xyseriescollection;
//	}
//
//	private static JFreeChart createChart() {
//		XYDataset xydataset = createDataset1();
//		/*
//		JFreeChart jfreechart = ChartFactory.createXYLineChart(
//				"Annotation Demo 2", "Date", "Price Per Unit", xydataset,
//				PlotOrientation.VERTICAL, false, true, false);
//				*/
//		JFreeChart jfreechart = ChartFactory.createXYLineChart("demo", "x", "y", xydataset);
//		XYPlot xyplot = (XYPlot) jfreechart.getPlot();
//		NumberAxis numberaxis = (NumberAxis) xyplot.getRangeAxis();
//		numberaxis.setAutoRangeIncludesZero(false);
//		NumberAxis numberaxis1 = new NumberAxis("Secondary");
//		numberaxis1.setAutoRangeIncludesZero(false);
//		xyplot.setRangeAxis(1, numberaxis1);
//		xyplot.setDataset(1, createDataset2());
//		xyplot.mapDatasetToRangeAxis(1, 1);
//
//		XYLineAndShapeRenderer xylineandshaperenderer1 = new XYLineAndShapeRenderer(
//				true, true);
//		xylineandshaperenderer1.setSeriesPaint(0, Color.black);
//		xylineandshaperenderer1.setBaseLinesVisible(false);
//		xyplot.setRenderer(1, xylineandshaperenderer1);
//		return jfreechart;
//	}
//
//	public static JPanel createDemoPanel() {
//		JFreeChart jfreechart = createChart();
//		return new ChartPanel(jfreechart);
//	}
//
//	public void draw(XYDataset xyDataset) {
//		XYDataset xydataset = createDataset1();
//		JFreeChart jfreechart = ChartFactory.createXYLineChart("chart", "density", "delta", xydataset);
//		XYPlot xyplot = (XYPlot) jfreechart.getPlot();
//		NumberAxis numberaxis = (NumberAxis) xyplot.getRangeAxis();
//		numberaxis.setAutoRangeIncludesZero(false);
//		NumberAxis numberaxis1 = new NumberAxis("Secondary");
//		numberaxis1.setAutoRangeIncludesZero(false);
//		xyplot.setRangeAxis(1, numberaxis1);
//		xyplot.setDataset(1, xyDataset);
//		xyplot.mapDatasetToRangeAxis(1, 1);
//
//		XYLineAndShapeRenderer xylineandshaperenderer1 = new XYLineAndShapeRenderer(
//				true, true);
//		xylineandshaperenderer1.setSeriesPaint(0, Color.black);
//		xylineandshaperenderer1.setBaseLinesVisible(false);
//		xyplot.setRenderer(1, xylineandshaperenderer1);
//
//		ChartPanel chartPanel = new ChartPanel(jfreechart);
//		setContentPane(chartPanel);
//		this.pack();
//		RefineryUtilities.centerFrameOnScreen(this);
//		this.setVisible(true);
//	}
//
//	public static void main(String args[]) {
//		TestGraphic testGraphic = new TestGraphic("");
//		XYDataset xyDataset = TestGraphic.createDataset2();
//		testGraphic.draw(xyDataset);
//		/*
//		JFrame.setDefaultLookAndFeelDecorated(true);
//		TestGraphic annotationdemo2 = new TestGraphic(
//				"Annotation Demo 2");
//		annotationdemo2.pack();
//		RefineryUtilities.centerFrameOnScreen(annotationdemo2);
//		annotationdemo2.setVisible(true);
//		*/
//	}
//
//}
package com.lbc.nlp_algorithm.clustering.hac.test;

public class TestGraphic {

}
