package com.aylien.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aylien.data.TrainMethod;

public class PropertiesManager {

	private static final Logger LOG = LoggerFactory.getLogger(PropertiesManager.class);

	
	private static Properties props;

	private static String PROPERTY_FILE = "/project.properties";
	private static String TWEETS_DIR = "tweets.dir";
	private static String NUM_FOLDS = "num.crossvalidation.folds";
	private static String TOKEN_REGEX = "token.regex";
	private static String MIN_FEATURES_COUNT = "min.features.count";
	private static String INTERCEPT_FEATURE = "intercept.feature";
	private static String NONINFORMATIVE_INTERCEPT = "noninformative.intercept";
	private static String PRIOR_VARIANCE = "prior.variance";
	private static String MIN_IMPROVEMENT = "min.improvement";
	private static String MIN_EPOCHS = "min.epochs";
	private static String MAX_EPOCHS = "max.epochs";
	private static String ROLLING_AVG_SIZE = "rolling.avg.size";
	private static String ANNEALING_RATE = "annealing.rate";
	private static String ANNEALING_BASE = "annealing.base";
	private static String HOLDOUT = "holdout";
	private static String TRAIN_METHOD = "train.method";
	private static String MODELS_DIR = "models.dir";
	private static String SAVE_MODELS = "save.models";
	private static String MODEL_NAME = "model.name";
	private static String LOAD_MODELS = "load.models";
	private static String WORD2VEC_PATH = "word2vec.path";
	private static String WORD2VEC_MODEL_PATH = "word2vec.model.path";
	private static String POS_TAGGER_MODEL_PATH = "post.tagger.mode.path";
	private static String NUMBER_VARIATIONS = "number.variations";
	private static String POS_CHANGEABLE = "pos.changeable";
	private static Boolean FORCE_LOAD_MODEL = null;
	
	public PropertiesManager() {
		super();
	}

	public String getProperty(String propertyName) {

		// Get the properties file from the classpath
		if (props == null) {
			try {
				LOG.debug("First use.");
				props = getPropertiesFromClasspath(PROPERTY_FILE);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return props.getProperty(propertyName);

	}

	private Properties getPropertiesFromClasspath(String propFileName) throws IOException {
		Properties props = new Properties();
		
		InputStream inputStream = null;
		String externPropFile =  "." + propFileName;
		try{
			LOG.debug("Loading extern properties file: " + externPropFile);
			inputStream = new FileInputStream("." + propFileName);
		}catch(Exception e){
			LOG.debug("Extern file not found. Loading resource file: " + propFileName);
			inputStream = PropertiesManager.class.getResourceAsStream(propFileName);
		}
		
		props.load(inputStream);

		return props;
	}

	private File returnAsResourceFile(String prop) {
		URL url = PropertiesManager.class.getResource("/" + prop);
		return returnAsFile(url.getFile());
	}

	private File returnAsFile(String prop) {
		return new File(prop);
	}

	private int returnAsInt(String prop) {
		return Integer.parseInt(prop);
	}

	private boolean returnAsBoolean(String prop) {
		return prop.contains("1");
	}
	
	private double returnAsDouble(String prop) {
		return Double.parseDouble(prop);
	}
	
	public int getNumFolds() {
		String prop = getProperty(NUM_FOLDS);
		return returnAsInt(prop);
	}

	public File getTweetsDir() {
		String prop = getProperty(TWEETS_DIR);
		return returnAsResourceFile(prop);
	}

	public String getTokenRegex() {
		String prop = getProperty(TOKEN_REGEX);
		return prop;
	}

	public int getMinFeaturesCount() {
		String prop = getProperty(MIN_FEATURES_COUNT);
		return returnAsInt(prop);
	}
	
	public boolean getInterceptFeature(){
		String prop = getProperty(INTERCEPT_FEATURE);
		return returnAsBoolean(prop);
	}

	public boolean getNoninformativeIncercept(){
		String prop = getProperty(NONINFORMATIVE_INTERCEPT);
		return returnAsBoolean(prop);
	}
	
	public double getPriorVariance() {
		String prop = getProperty(PRIOR_VARIANCE);
		return returnAsDouble(prop);
	}

	public double getMinImprovement() {
		String prop = getProperty(MIN_IMPROVEMENT);
		return returnAsDouble(prop);
	}

	public int getMinEpochs() {
		String prop = getProperty(MIN_EPOCHS);
		return returnAsInt(prop);
	}
	
	public int getMaxEpochs() {
		String prop = getProperty(MAX_EPOCHS);
		return returnAsInt(prop);
	}	

	public int getRollingAvgSize() {
		String prop = getProperty(ROLLING_AVG_SIZE);
		return returnAsInt(prop);
	}

	public double getAnnealingRate() {
		String prop = getProperty(ANNEALING_RATE);
		return returnAsDouble(prop);
	}
	
	public double getAnnealingBase() {
		String prop = getProperty(ANNEALING_BASE);
		return returnAsDouble(prop);
	}

	public double getHoldoutProp() {
		String prop = getProperty(HOLDOUT);
		return returnAsDouble(prop);
	}
	
	public TrainMethod getTrainMethod() {
		String prop = getProperty(TRAIN_METHOD);
		return ProjectUtils.returnAsTrainMethod(prop);
	}
	
	public File getModelsDir(){
		String prop = getProperty(MODELS_DIR);
		return returnAsResourceFile(prop);
	}
	
	public boolean getSaveModels() {
		String prop = getProperty(SAVE_MODELS);
		return returnAsBoolean(prop);
	}
	
	public boolean getLoadModels() {
		if(FORCE_LOAD_MODEL != null)
		{
			return FORCE_LOAD_MODEL;
		}
		String prop = getProperty(LOAD_MODELS);
		return returnAsBoolean(prop);
	}
	
	public void setLoadModels(boolean b) {
		FORCE_LOAD_MODEL = b;
	}
	
	public String getModelName(){
		String prop = getProperty(MODEL_NAME);
		return prop;
	}

	public String getWord2VecLibPath() {
		String prop = getProperty(WORD2VEC_PATH);
		return prop;
	}

	public String getWord2VecModelPath() {
		String prop = getProperty(WORD2VEC_MODEL_PATH);
		return prop;
	}

	public File getPosTaggerModel() {
		String prop = getProperty(POS_TAGGER_MODEL_PATH);
		return returnAsResourceFile(prop);
	}
	
	public int getNumberVariations(){
		String prop = getProperty(NUMBER_VARIATIONS);
		return returnAsInt(prop);
	}
	
	public String getPosChangeable(){
		String prop = getProperty(POS_CHANGEABLE);
		return prop;
	}




}