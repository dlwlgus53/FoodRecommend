package MovieRec ;

import java.io.* ;
import java.util.* ;

import org.apache.commons.cli.* ;
import org.apache.commons.configuration.* ;
import org.apache.commons.csv.* ;

import org.apache.logging.log4j.Logger ; 
import org.apache.logging.log4j.LogManager ;

public 
class Main 
{
	static PropertiesConfiguration config ;
	static boolean isToShow = false ;
	static String configFilePath = "config.properties" ;
	static Logger logger = LogManager.getLogger(Main.class) ;
	static Logger informer = LogManager.getLogger("Info") ;

	public static 
	void main (String [] args) 
	{
		Options options = new Options() ;
		options.addOption("c", "config", true, "configuration file") ;
		options.addOption("d", "display", false, "show statistics") ;
		options.addOption("h", "help", false, "show help message") ;

		CommandLineParser parser = new DefaultParser() ;
		CommandLine cmd = null ;
		try {
			cmd = parser.parse(options, args) ;
			if (cmd.hasOption("d"))
				isToShow = false ;/*이거수정했음 원래는 ture*/
			if (cmd.hasOption("c"))
				configFilePath = cmd.getOptionValue("c") ;
			if (cmd.hasOption("h")) {
				HelpFormatter formater = new HelpFormatter() ;
				formater.printHelp("Usage", options) ;
				System.exit(0) ;
			}
		}
		catch (ParseException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}

		config(configFilePath) ;

		try {
			MovieRatingData data = new MovieRatingData(config) ;
			FileReader ftrain = new FileReader(config.getString("data.orgin")) ;
			//FileReader ftest =  new FileReader(config.getString("data.testing")) ;

			logger.debug("Data loading starts.") ;		
			data.load(ftrain) ;
			logger.debug("Data loading finishes.") ;
			/*
			if (isToShow)
				data.show() ;
			data.removeOutliers() ;
			*/

			Recommender rec = new Recommender(config) ;
			rec.train(data) ;
			/*
			test(ftest, rec) ;
			*/
		}
		catch (IOException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}
	}
	
	public static
	void config (String fpath) {
		try {
			config = new PropertiesConfiguration(fpath) ;
		}
		catch (ConfigurationException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}
	}

/*
	public static
	void test (FileReader ftest, Recommender rec) throws IOException
	{
		int [][] error = new int[2][2] ; // actual x predict -> # 	

		TreeMap<String, HashSet<String>> 
		users = new TreeMap<String, HashSet<String>>();
		//user x foodset

		TreeMap<String, HashSet<String>> 
		q_positive = new TreeMap<String, HashSet<String>>();

		TreeMap<String, HashSet<String>> 
		q_negative = new TreeMap<String, HashSet<String>>();

		for (CSVRecord r : CSVFormat.newFormat(',').parse(ftest)) {
			//한줄 읽어오기
			String user = r.get(0) ;
			String food = r.get(1) ;
			Double rating = Double.parseDouble(r.get(2)) ;
			//String type = r.get(3) ;

			if (users.containsKey(user) == false) {
				users.put(user, new HashSet<String>()) ;
				q_positive.put(user, new HashSet<String>()) ;
				q_negative.put(user, new HashSet<String>()) ;
			}

			if (rating >= config.getDouble("data.like_threshold"))
					users.get(user).add(food) ;
					//점수가 어느정도 높으면 넣기	
			
			if (type.equals("c")) {
				if (rating >= config.getDouble("data.like_threshold"))
					users.get(user).add(foodID) ;								
			}
			else {
				if (rating >= config.getDouble("data.like_threshold"))
					q_positive.get(user).add(movie) ;
				else
					q_negative.get(user).add(movie) ;
			}
			
		}

		for (String u : users.keySet()) {
			HashSet<String> u_movies = users.get(u) ;
			
			for (String q : q_positive.get(u))
				error[1][rec.predict(u_movies, q)] += 1 ;
	
			for (Integer q : q_negative.get(u))
				error[0][rec.predict(u_movies, q)] += 1 ;
		}

		if (error[0][1] + error[1][1] > 0)
			informer.info("Precision: " +
				String.format("%.3f", 
					(double)(error[1][1]) / (double)(error[0][1] + error[1][1]))) ;
		else
			informer.info("Precision: undefined.") ;

		if (error[1][0] + error[1][1] > 0)
			informer.info("Recall: " +
			  String.format("%.3f", 
				((double)(error[1][1]) / (double)(error[1][0] + error[1][1])))) ;
		else
			informer.info("Recall: undefined.") ;

		if (error[0][0] + error[1][1] > 0)
			informer.info("All case accuracy: " +
			  String.format("%.3f", 
				((double)(error[1][1] + error[0][0]) / 
				(double)(error[0][0] + error[0][1] + error[1][0] + error[1][1])))) ;
		else
			informer.info("All case accuracy: undefined.") ;

		informer.info("[[" + error[0][0] + ", " + error[0][1] + "], "  + 
			"[" + error[1][0] + ", " + error[1][1] + "]]") ;
	}
	*/
}
