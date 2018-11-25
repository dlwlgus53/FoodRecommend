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
			FileReader ftrain = new FileReader(config.getString("data.training")) ;
			FileReader ftest =  new FileReader(config.getString("data.testing")) ;

			logger.debug("Data loading starts.") ;		
			data.load(ftrain) ;
			logger.debug("Data loading finishes.") ;
			/*
			if (isToShow)
				data.show() ;
			data.removeOutliers() ;
			*/

			Recommender rec = new Recommender(config) ;
			rec.train(data);
			test(ftest, rec) ;
			
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


	public static
	void test (FileReader ftest, Recommender rec) throws IOException
	{	logger.debug("Doing test") ;

		int [][] error = new int[2][2] ; // actual x predict -> # 	

		TreeMap<String, HashSet<String>> 
		users = new TreeMap<String, HashSet<String>>();
		//users  =  user x foodset

		TreeMap<String, HashSet<String>> 
		q_positive = new TreeMap<String, HashSet<String>>();

		TreeMap<String, HashSet<String>> 
		q_negative = new TreeMap<String, HashSet<String>>();
		logger.debug("Reading csv") ;
		for (CSVRecord r : CSVFormat.newFormat(',').parse(ftest)) {
			
			//한줄 읽어오기
			String user = r.get(0) ;
			String food = r.get(1) ;
			Double rating = Double.parseDouble(r.get(2)) ;
			String type = r.get(3) ;

			if (users.containsKey(user) == false) {
				users.put(user, new HashSet<String>()) ;
				q_positive.put(user, new HashSet<String>()) ;
				q_negative.put(user, new HashSet<String>()) ;
			}

			if (type.equals("c")) {
				if (rating >= config.getDouble("data.like_threshold"))
					users.get(user).add(food) ;								
					//일반항목 -> 좋아하면 user가 좋아하는 음식 리스트 중 하나로 넣기
			}
			else {
				if (rating >= config.getDouble("data.like_threshold"))
					q_positive.get(user).add(food) ;
					//question일때, 유저가 좋아하는 음식이라면 질문 리스트에 넣기

				else
					q_negative.get(user).add(food) ;
					//반대경우
			}
			
		}
		logger.debug("Reading csv finish" ) ;

		for (String u : users.keySet()) {
			HashSet<String> u_foods = users.get(u) ;
			
			for (String q : q_positive.get(u))
				error[1][rec.predict(u_foods, q)] += 1 ;
	
			for (String q : q_negative.get(u))
				error[0][rec.predict(u_foods, q)] += 1 ;
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

		logger.debug("[[" + error[0][0] + ", " + error[0][1] + "], "  + 
			"[" + error[1][0] + ", " + error[1][1] + "]]") ;
		logger.debug("test finish") ;
		
	}
	
	
}
