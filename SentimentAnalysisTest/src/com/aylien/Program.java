package com.aylien;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aylien.experiment.TweetSentimentAnalysisExperiment;
import com.aylien.pos.PosTagger;
import com.aylien.util.PropertiesManager;
import com.aylien.util.SentencesUtils;
import com.aylien.word2vec.Word2VecLib;

public class Program {

	private static final Logger LOG = LoggerFactory.getLogger(Program.class);
	
	public static void main(String[] args) {
		
		// Parsing command line arguments
		List<String> argsList = new ArrayList<String>();  
		HashMap<String, String> optsList = new HashMap<String, String>();
	    List<String> doubleOptsList = new ArrayList<String>();

	    for (int i = 0; i < args.length; i++) {
	        switch (args[i].charAt(0)) {
	        case '-':
	            if (args[i].length() < 2)
	                throw new IllegalArgumentException("Not a valid argument: "+args[i]);
	            if (args[i].charAt(1) == '-') {
	                if (args[i].length() < 3)
	                    throw new IllegalArgumentException("Not a valid argument: "+args[i]);
	                doubleOptsList.add(args[i].substring(2, args[i].length()));
	            } else {
	                if (args.length-1 == i)
	                    throw new IllegalArgumentException("Expected arg after: "+args[i]);
	                optsList.put(args[i], args[i+1]);
	                i++;
	            }
	            break;
	        default:
	            argsList.add(args[i]);
	            break;
	        }
	    }
	    
	    if(optsList.isEmpty()){
	    	throw new IllegalArgumentException("Incorrect usage, none arguments found.");
	    }
	    
	    LOG.info("Load properties...");
	    PropertiesManager props = new PropertiesManager();
	    
	    String word2VecModelPath = null;
	    if(optsList.containsKey("-w")){
	    	word2VecModelPath = optsList.get("-w");
	    }else{
	    	word2VecModelPath = props.getWord2VecModelPath();
	    }
	    
	    	    
	    LOG.info("Loading libs...");
		Word2VecLib word2vec = Word2VecLib.getInstance(word2VecModelPath);
		PosTagger posTagger = PosTagger.getInstance();
		SentencesUtils senUtils = new SentencesUtils(word2vec, posTagger);
	    
	    // Handle arguments
	    if(optsList.containsKey("-s"))
	    {
	    	String preset = optsList.get("-s");
	    		    	
	    	switch(preset)
	    	{
	    		case "ALL": (new TweetSentimentAnalysisExperiment(word2vec, posTagger, senUtils, props)).run(); break;
	    		case "TEST": props.setLoadModels(true); (new TweetSentimentAnalysisExperiment(word2vec, posTagger, senUtils, props)).run(); break;
	    		default: LOG.error("Unkown preset configuration."); 
	    	}
	    }	    
	}

}
