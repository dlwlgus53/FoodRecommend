package MovieRec ;

import java.io.* ;
import java.util.* ;
import com.google.common.collect.* ;
import org.apache.commons.configuration.* ;
import java.lang.System.*;

public class 
Recommender
{
	TreeMap<String, Integer> 
	support1 = new TreeMap<String, Integer>() ; 
	/* support1 : FoodId -> Num */

	TreeMap<StringPair, Integer> 
	support2 = new TreeMap<StringPair, Integer>() ; 
	/* support2 : FoodId x FoodId -> Num */

	TreeMap<StringTriple, Integer> 
	support3 = new TreeMap<StringTriple, Integer>() ; 
	/* support3 : FoodId x FoodId x FoodId -> Num */

	PropertiesConfiguration config ;
	int min_supports ;
	int min_evidence_3 ;
	double threshold_2 ;
	double threshold_3 ;


	Recommender(PropertiesConfiguration config) {
		this.config = config ;
		this.min_supports = 
			config.getInt("training.min_supports") ;
		this.threshold_2 = 
			config.getDouble("prediction.threshold_2") ;
		this.threshold_3 = 
			config.getDouble("prediction.threshold_3") ;
		this.min_evidence_3 = 
			config.getInt("prediction.min_evidence_3") ;
	}

	public 
	void train(MovieRatingData data) {
		TreeMap<String, HashSet<String>> 
		Baskets = data.getBaskets() ;
		/* Baskets : UserID -> Set<FoodID> */

		for (String user : Baskets.keySet()) {
			HashSet<String> Basket = Baskets.get(user) ;

			updateSupport1(Basket) ;
			updateSupport2(Basket) ;
			updateSupport3(Basket) ;
		}
	}

	public
	int predict(HashSet<String> profile, String q) {
        //thing's to predict 
        int predict =0;
		if (predictPair(profile, q) == 1)
            predict = 1;
        if( predictTriple(profile, q)==1)
            predict =1;
		/*
        if(predict == 1){
            System.out.println("predict yes");
            System.out.println(profile.toString() + q);
        }*/
		return predict ;
	}


	private
	void updateSupport1(HashSet<String> Basket) {
		for (String item : Basket) {
			Integer c = support1.get(item) ;
			if (c == null)
				c = new Integer(1) ;
			else
				c = new Integer(c.intValue() + 1 ) ;
			support1.put(item, c) ;
		}
	}

	private
	void updateSupport2(HashSet<String> Basket) {
		if (Basket.size() >= 2) {
			for (Set<String> pair : Sets.combinations(Basket, 2)) {
				Integer c = support2.get(new StringPair(pair)) ;
				if (c == null) 
					c = new Integer(1) ;
				else
					c = new Integer(c.intValue() + 1) ;
				support2.put(new StringPair(pair), c) ;
			}
		}
	}

	private
	void updateSupport3(HashSet<String> Basket) {
		HashSet<String> 
		_Basket = new HashSet<String>() ;
		for (String elem : Basket) {
			if (support1.get(elem) >= min_supports)
				_Basket.add(elem) ;
		}
		Basket = _Basket ;

		if (Basket.size() >= 3) {
			for (Set<String> triple : Sets.combinations(Basket, 3)) {
				Integer c = support3.get(new StringTriple(triple));
				if (c == null) 
					c = new Integer(1) ;
				else
					c = new Integer(c.intValue() + 1) ;
				support3.put(new StringTriple(triple), c) ;
			}
		}
	}

	private
	int predictPair(HashSet<String> profile, String q) {
        /* TODO: implement this method */
        //profile => set of foodId
		if (profile.size() < 1)
            return 0 ;
            //구매 이력이 적음

        int evidence = 0;
        
		for (String s : profile){
			Integer den = support1.get(s);
			if (den == null)
				continue;

			StringPair item = new StringPair(s,q);
			Integer num = support2.get(item);
			if (num == null)
				continue;
			if (num.intValue() < min_supports)
				continue;
			if ((double)num / (double)den >= threshold_2)
				evidence++;
		}
		
		if (evidence != 0)
			return 1;//만족하는 경우가 하나라도 있다면
	
		return 0 ;
	}

	private
	int predictTriple(HashSet<String> profile, String q) {
		if (profile.size() < 2)
			return 0 ;

		int evidence = 0 ;
		for (Set<String> p : Sets.combinations(profile, 2)) {
			Integer den = support2.get(new StringPair(p)) ;
			if (den == null)
				continue ;

			TreeSet<String> t = new TreeSet<String>(p) ;
			t.add(q) ;
			StringTriple item = new StringTriple(t) ;			
			Integer num = support3.get(item) ;
			if (num == null)
				continue ;

			if (num.intValue() < min_supports)
				continue ;

			if ((double)num / (double)den >= threshold_3){ 
				System.out.println(t);
				evidence++ ;
			}
		}

		if (evidence >= min_evidence_3) 
			return 1 ;

		return 0 ;
	}	
}

class 
StringPair implements Comparable 
{
	String first ;
	String second ;

	public
	StringPair(String first, String second) {
		if (first.compareTo(second)<0) {
			this.first = first ;
			this.second = second ;
		}
		else {
			this.first = second ;
			this.second = first ;
		}
	}

	public
	StringPair(Set<String> s) {
		String [] elem = s.toArray(new String[2]) ;
		Arrays.sort(elem);
		/*if (elem[0].compareTo(elem[1])<0) {
			this.first = elem[0] ;
			this.second = elem[1] ;
		}
		else {
			this.first = elem[1] ;
			this.second = elem[0] ;
		}*/
	}

	public 
	int compareTo(Object obj) {
		/*
		Object[] t= (Object []) obj;
		
		if (this.first < t.first) 
			return -1 ;
		if (this.first > t.first)
			return 1 ;

		return (this.second - t.second) ;
		*/
		
		return 0;

	}
	
    
}

class 
StringTriple implements Comparable 
{
	String [] elem = new String[3];

	public
	StringTriple(Set<String> s) {
		/* TODO: implement this method */
		String [] tempelem = s.toArray(new String[3]);
		for (int i = 0; i < 3; i++){
			this.elem[i] = tempelem[i];
		}
		Arrays.sort(elem);
	}

	public 
	int compareTo(Object obj) {
		/*
		Object[] t= (Object []) obj;
				
		for (int i = 0; i < 3; i++){
			for(int j=0; j<elem[i])
			char t1 = elem[i][0];
			if(this.elem[i] < t[i])
				return -1;
			if(this.elem[i] > t.elem[i])
				return 1;
		}*/

		return 0;
    }
    
    
}
