package MovieRec ;

import java.io.* ;
import java.util.* ;
import java.awt.* ;
import javax.swing.* ;

import org.apache.commons.csv.* ;
import org.apache.commons.configuration.* ;

import org.jfree.chart.* ;
import org.jfree.chart.plot.* ;
import org.jfree.chart.renderer.xy.XYDotRenderer ;
import org.jfree.data.* ;
import org.jfree.data.statistics.* ;
import org.jfree.data.xy.XYDataset ;
import org.jfree.ui.ApplicationFrame ;

public 
class MovieRatingData 
{
	TreeMap<String, HashSet<String>>
	Baskets = new TreeMap<String, HashSet<String>>() ;
	//userxfoodID set

	TreeMap<String, Integer>
	numRatingsOfMovies = new TreeMap<String, Integer>() ;
	//foodIDx rating 사람 수
	TreeMap<String, Double>
	accRatingsOfMovies = new TreeMap<String, Double>() ;
	//foodIDx rating합
	PropertiesConfiguration config ;
	double like_threshold ;
	int outlier_threshold ;

	public
	MovieRatingData (PropertiesConfiguration config) {
		this.config = config ;
		this.like_threshold = config.getDouble("data.like_threshold") ;
		this.outlier_threshold = config.getInt("data.outlier_threshold") ;
	}

	public 
	void load (FileReader f) throws IOException {
		int i = 0;

		for (CSVRecord r : CSVFormat.newFormat(',').parse(f)) {
			String user = r.get(0) ;
			String food = r.get(1) ;
			Double rating = Double.parseDouble(r.get(2)) ;

			if (numRatingsOfMovies.containsKey(food) == false) {
				numRatingsOfMovies.put(food, 1) ;
				accRatingsOfMovies.put(food, rating) ;
			}
			else {
				numRatingsOfMovies.put(food, numRatingsOfMovies.get(food) + 1) ;
				accRatingsOfMovies.put(food, accRatingsOfMovies.get(food) + rating) ;
			}

			if (rating >= like_threshold) {
				HashSet<String> basket = Baskets.get(user) ;
				//basket : food basket
				if (basket == null) {
					basket = new HashSet<String>() ;
					Baskets.put(user, basket) ;
				}
				basket.add(food) ;
			}
		}
	}
/*
	public
	void removeOutliers() {
		HashSet<Integer> outliers = new HashSet<Integer>() ;
		for (Integer userId : Baskets.keySet()) {
			HashSet<Integer> basket = Baskets.get(userId) ;
			if (basket.size() > outlier_threshold) 
				outliers.add(userId) ;
		}
		for (Integer userId : outliers) 
			Baskets.remove(userId) ;
	}
*/
	public 
	TreeMap<String, HashSet<String>>
	getBaskets() {
		return Baskets ;
	}
/*
	public
	void show() {
		showMovieStat() ;
		showUserStat() ;
		showRatingStat() ;
	}



	private
	void showMovieStat() {
		ApplicationFrame frame = new ApplicationFrame("Movie Stat.") ;

		XYDataset dataset = getNumAvgRatingDataset() ;
		JFreeChart chart = ChartFactory.createScatterPlot("Num vs. Avg Rating", "Num", "Avg Rating", 
			dataset, PlotOrientation.VERTICAL, true, true, false) ;
		XYPlot plot = (XYPlot) chart.getPlot() ;
		XYDotRenderer renderer = new XYDotRenderer() ;
		renderer.setDotWidth(2) ;
		renderer.setDotHeight(2) ;
		plot.setRenderer(renderer) ;
		JPanel panel = new ChartPanel(chart) ;
		panel.setPreferredSize(new java.awt.Dimension(500, 270)) ;

		frame.setContentPane(panel) ;
		frame.pack() ;
		frame.setVisible(true) ;
	}

	private
	XYDataset getNumAvgRatingDataset() {
		return (XYDataset) new NumAvgDataset(numRatingsOfMovies, accRatingsOfMovies) ;
	}

	private
	void showUserStat() {
		ApplicationFrame frame = new ApplicationFrame("User Stat.") ;

		double [] ratings = new double[Baskets.keySet().size()] ;

		int i = 0 ;
		for (Integer user : Baskets.keySet()) {
			ratings[i] = (double) Baskets.get(user).size() ;
			i++ ;
		}

		HistogramDataset dataset = new HistogramDataset() ;
		dataset.setType(HistogramType.RELATIVE_FREQUENCY) ;
		dataset.addSeries("Histogram", ratings, 20) ;
		JFreeChart chart = ChartFactory.createHistogram("Num. Ratings by Users",
			"Num", "value", dataset, PlotOrientation.VERTICAL, false, false, false) ;
		JPanel panel = new ChartPanel(chart) ;
		frame.setContentPane(panel) ;
		frame.pack() ;
		frame.setVisible(true) ;
	}

	private
	void showRatingStat() {
		
		ApplicationFrame frame = new ApplicationFrame("Movie Rating Stat.") ;

		double [] ratings = new double[accRatingsOfMovies.keySet().size()] ;

		int i = 0 ;
		for (Integer id : accRatingsOfMovies.keySet()) {
			ratings[i] = accRatingsOfMovies.get(id).doubleValue() / (double)numRatingsOfMovies.get(id).intValue();
			i++ ;
		}

		HistogramDataset dataset = new HistogramDataset() ;
		dataset.setType(HistogramType.RELATIVE_FREQUENCY) ;
		dataset.addSeries("Histogram", ratings, 50) ;
		JFreeChart chart = ChartFactory.createHistogram("Movie Ratings",
			"Rate", "Num. Rate", dataset, PlotOrientation.VERTICAL, false, false, false) ;
		JPanel panel = new ChartPanel(chart) ;
		frame.setContentPane(panel) ;
		frame.pack() ;
		frame.setVisible(true);
	}
	*/
}
