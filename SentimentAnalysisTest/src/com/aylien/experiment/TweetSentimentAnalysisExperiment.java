package com.aylien.experiment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.ConditionalClassifierEvaluator;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.io.Reporter;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aylien.data.TrainMethod;
import com.aylien.pos.PosTagger;
import com.aylien.util.ProjectUtils;
import com.aylien.util.PropertiesManager;
import com.aylien.util.SentencesUtils;
import com.aylien.word2vec.Word2VecLib;

public class TweetSentimentAnalysisExperiment implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(TweetSentimentAnalysisExperiment.class);

	private File dataDir;
	private int numFolds;
	private String[] categories;
	private List<String[]> trainSentences;
	private List<String[]> testSentences;
	private String tokenRegex;
	private int minFeaturesCount;
	private boolean includeInterceptFeature;
	private boolean noninformativeIntercept;
	private double priorVariance;
	private double minImprovement;
	private int minEpochs;
	private int maxEpochs;
	private int rollingAvgSize;
	private double annealingRate;
	private double annealingBase;
	private double holdoutProp;
	private TrainMethod trainMethod;
	private File modelsDir;
	private boolean saveModels;
	private boolean loadModels;
	private String modelName;
	private int numberVariations;
	private String posChangeable;
	private Word2VecLib word2vec;
	private PosTagger posTagger;
	private SentencesUtils senUtils;
	private PropertiesManager props;
	
	LogisticRegressionClassifier<CharSequence> classifier;

	public TweetSentimentAnalysisExperiment(Word2VecLib word2vec, PosTagger posTagger, SentencesUtils senUtils, PropertiesManager props) {

		this.props = props; 
		this.word2vec = word2vec;
		this.posTagger = posTagger;
		this.senUtils = senUtils;
		this.dataDir = props.getTweetsDir();
		this.numFolds = props.getNumFolds();
		this.tokenRegex = props.getTokenRegex();
		this.minFeaturesCount = props.getMinFeaturesCount();
		this.includeInterceptFeature = props.getInterceptFeature();
		this.noninformativeIntercept = props.getNoninformativeIncercept();
		this.priorVariance = props.getPriorVariance();
		this.minEpochs = props.getMinEpochs();
		this.maxEpochs = props.getMaxEpochs();
		this.rollingAvgSize = props.getRollingAvgSize();
		this.annealingRate = props.getAnnealingRate();
		this.annealingBase = props.getAnnealingBase();
		this.holdoutProp = props.getHoldoutProp();
		this.trainMethod = props.getTrainMethod();
		this.modelsDir = props.getModelsDir();
		this.saveModels = props.getSaveModels();
		this.loadModels = props.getLoadModels();
		this.modelName = props.getModelName();
		this.numberVariations = props.getNumberVariations();
		this.posChangeable = props.getPosChangeable();
	}

	@Override
	public void run() {
		if (dataPrepare()) {
			train();
			test();
		}

	}

	// train a LogisticRegressionClassifier with the data in dataDir
	private void train() {

		if (classifier != null) {
			return;
		}
		LOG.info("train() started");
		LogisticRegressionClassifier<CharSequence> classifier = null;

		// Setup the classifier
		if ((this.classifier = buildClassifier()) != null) {
			
			if (saveModels) {
				try {
					ProjectUtils.SaveModel(classifier, this.categories, this.testSentences, modelsDir, modelName);
				} catch (IOException e) {
					LOG.error(e.getMessage());
				}
			}

		}
	}

	private LogisticRegressionClassifier<CharSequence> buildClassifier() {

		LOG.info("Classifier setup...");

		LogisticRegressionClassifier<CharSequence> classifier = null;
		LogisticRegressionClassifier<CharSequence> bestClassifier = null;
		XValidatingObjectCorpus<Classified<CharSequence>> experiment = null;
		Classified<CharSequence> classified = null;
		Classification category = null;
		TokenizerFactory tokenizerFactory = null;
		FeatureExtractor<CharSequence> featureExtractor = null;
		LogisticRegressionClassifier<CharSequence> hotStart = null;
		RegressionPrior prior = null;
		AnnealingSchedule annealingSchedule = null;
		ObjectHandler<LogisticRegressionClassifier<CharSequence>> classifierHandler = null;
		ConditionalClassifierEvaluator<CharSequence> evaluator = null;
		Reporter reporter = null;
		int blockSize;
		boolean storeInputs = false;
		double accuracy = Double.MIN_VALUE;
		int numFoldsIntern = 0;

		switch (trainMethod) {
		case CROSSVALIDATION:
			numFoldsIntern = this.numFolds;
			LOG.debug("Train method: Cross-Validation " + this.numFolds + "-fold");
			break;
		case HOLDOUT:
			numFoldsIntern = 0;
			LOG.debug("Train method: Holdout.");
			break;

		}

		experiment = new XValidatingObjectCorpus<Classified<CharSequence>>(numFoldsIntern);

		if (categories.length == 0) {
			return null;
		}

		for (int i = 0; i < categories.length; i++) {
			category = new Classification(categories[i]);
			for (String sentence : trainSentences.get(i)) {
				classified = new Classified<CharSequence>(sentence, category);
				experiment.handle(classified);
			}
		}

		// Suffle data
		experiment.permuteCorpus(new Random());

		LOG.debug("Tokenizer regEx: " + this.tokenRegex);
		tokenizerFactory = new RegExTokenizerFactory(tokenRegex);
		featureExtractor = new TokenFeatureExtractor(tokenizerFactory);
		LOG.debug("Simulated anneling rate: " + annealingRate + " base: " + annealingBase);
		LOG.debug("Simulated anneling base: " + annealingBase);
		annealingSchedule = AnnealingSchedule.exponential(annealingRate, annealingBase);
		prior = RegressionPrior.gaussian(priorVariance, noninformativeIntercept);
		LOG.debug("Min features count: " + minFeaturesCount);
		LOG.debug("Including intercept feature: " + includeInterceptFeature);

		blockSize = experiment.size();

		try {

			if (trainMethod.equals(TrainMethod.CROSSVALIDATION)) {
				LOG.info("Starting cross-validation:");
				for (int fold = 0; fold < this.numFolds; ++fold) {

					experiment.setFold(fold);
					LOG.info("Running train fold #" + (fold + 1));

					classifier = LogisticRegressionClassifier.<CharSequence> train(experiment, featureExtractor,
							minFeaturesCount, includeInterceptFeature, prior, blockSize, hotStart, annealingSchedule,
							minImprovement, rollingAvgSize, minEpochs, maxEpochs, classifierHandler, reporter);

					LOG.info("Running test fold #" + (fold + 1));

					evaluator = new ConditionalClassifierEvaluator<CharSequence>(classifier, categories, storeInputs);
					experiment.visitTest(evaluator);
					evaluator.confusionMatrix().totalAccuracy();
					LOG.info("Fold #" + (fold + 1) + " results:");
					LOG.info("\t Accuracy = " + evaluator.confusionMatrix().totalAccuracy() + "("
							+ evaluator.confusionMatrix().totalCorrect() + "/"
							+ evaluator.confusionMatrix().totalCount() + ")");

					// Pick the classifier with "best" accuracy
					if (accuracy < evaluator.confusionMatrix().totalAccuracy()) {
						accuracy = evaluator.confusionMatrix().totalAccuracy();
						bestClassifier = classifier;
					}
				}
			} else if (trainMethod.equals(TrainMethod.HOLDOUT)) {
				LOG.info("Building model....");

				classifier = LogisticRegressionClassifier.<CharSequence> train(experiment, featureExtractor,
						minFeaturesCount, includeInterceptFeature, prior, blockSize, hotStart, annealingSchedule,
						minImprovement, rollingAvgSize, minEpochs, maxEpochs, classifierHandler, reporter);

			}

		} catch (IOException e1) {
			LOG.error(e1.getMessage());
		}

		return classifier;
	}

	private boolean dataPrepare() {

		if (!loadModels) {
			return loadAllData();
		} else {
			return loadTestDataAndModel();
		}

	}

	private boolean loadTestDataAndModel() {

		LOG.info("Loading previous built model: " + modelName);

		boolean retval = false;

		testSentences = new ArrayList<String[]>();
		File testDir = new File(modelsDir, modelName + "/" + ProjectUtils.TEST_DIR_NAME);
		File modelFile = new File(modelsDir, modelName + "/" + ProjectUtils.MODEL_NAME);
		File[] dataFiles = null;
		String[] sentences = null;

		try {
			if(!testDir.exists()){
				throw new IOException(testDir.getAbsolutePath() + " does not exist, It must be trained first.");
			}
			
			dataFiles = testDir.listFiles();
			LOG.info(dataFiles.length + " categories found");

			if (dataFiles.length > 0) {
				categories = new String[dataFiles.length];

				for (int i = 0; i < dataFiles.length; i++) {
					categories[i] = dataFiles[i].getName();
					sentences = ProjectUtils.GetAllLines(dataFiles[i].getAbsoluteFile());
					testSentences.add(sentences);
				}
				retval = true;
				LOG.info("Data parsed");

			} else {
				throw new Exception("No train files found");
			}

			classifier = (LogisticRegressionClassifier<CharSequence>) AbstractExternalizable.readObject(modelFile);

			LOG.info("Model loaded");

		} catch (IOException e1) {
			LOG.error(e1.getMessage());
		} catch (Exception e2) {
			LOG.error(e2.getLocalizedMessage());
		}

		return retval;
	}

	private boolean loadAllData() {
		boolean retval = false;
		int from, to;
		double trainProp = 0, testProp = 0;
		File[] dataFiles = dataDir.listFiles();
		String[] sentences = null;
		trainSentences = new ArrayList<String[]>();
		testSentences = new ArrayList<String[]>();

		try {

			LOG.info(dataFiles.length + " categories found");

			if (trainMethod.equals(TrainMethod.HOLDOUT)) {
				trainProp = this.holdoutProp;
				testProp = 1 - this.holdoutProp;
				LOG.info("Loading data (holdout): train (" + trainProp + ")" + " tests " + "(" + (1 - testProp) + ")");
			} else {
				LOG.info("Loading data (cross-validation). ");
				trainProp = 1;
			}

			if (dataFiles.length > 0) {
				categories = new String[dataFiles.length];

				for (int i = 0; i < dataFiles.length; i++) {
					categories[i] = dataFiles[i].getName();
					sentences = ProjectUtils.GetAllLines(dataFiles[i].getAbsoluteFile());
					ProjectUtils.SuffleArray(sentences);

					from = 0;
					to = (int) (sentences.length * trainProp);
					trainSentences.add(Arrays.copyOfRange(sentences, from, to));

					from = to;
					to = sentences.length;
					testSentences.add(Arrays.copyOfRange(sentences, from, to));

					if (trainMethod.equals(TrainMethod.HOLDOUT)) {
						LOG.info("[" + categories[i] + "] category has " + sentences.length + " items (train = "
								+ trainSentences.get(i).length + ")" + "(test = " + testSentences.get(i).length + ")");
					} else {
						LOG.info("[" + categories[i] + "] category has " + sentences.length
								+ " items (cross-validation = " + trainSentences.get(i).length + ")");
					}
				}
				retval = true;
				LOG.info("Data parsed");
			} else {
				throw new Exception("No train files found");
			}
		} catch (IOException e1) {
			LOG.error(e1.getMessage());
		} catch (Exception e2) {
			LOG.error(e2.getMessage());
		}

		return retval;
	}

	private void test() {
		LOG.info("test() started");
		String[] sentences = null;
		String sentence = null;
		List<ConditionalClassification> ccResults = null;
		ConditionalClassification cc = null;
		double polarity = 0;
		String bestCategory = null;
		
		HashMap<String, HashMap<String, Integer>> confMat = new HashMap<String, HashMap<String, Integer>> ();
		
		for(int i = 0; i < categories.length; i++){
			confMat.put(categories[i], new HashMap<String, Integer>());
			for(int j = 0; j < categories.length; j++){
				confMat.get(categories[i]).put(categories[j], 0);
			}
		}
		
		int tp = 0, fp = 0;

		if (testSentences != null && classifier != null) {
			for (int i = 0; i < categories.length; i++) {
				for (int j = 0; j < testSentences.get(i).length; j++) {
					
					sentence = testSentences.get(i)[j];
					cc = classifier.classify(sentence);

					if (this.numberVariations > 0) {

						sentences = this.senUtils.GenerateSequencesByContext(sentence, this.numberVariations,
								this.posChangeable);
						if (sentences != null) {
							ccResults = new ArrayList<ConditionalClassification>();
							ccResults.add(cc);
							for (String sent : sentences) {
								ccResults.add(classifier.classify(sent));
							}
							polarity = ProjectUtils.calcPolarity(ccResults);
							bestCategory = ProjectUtils.getBestCategory(ccResults);

						}else{
							polarity = ProjectUtils.calcPolarity(cc);
							bestCategory = cc.bestCategory();
						}
					} else {
						polarity = ProjectUtils.calcPolarity(cc);
						bestCategory = cc.bestCategory();
					}
					LOG.debug("[" + polarity + "] " + sentence);

					if (bestCategory.equals(categories[i])) {
						tp++;
					} else {
						fp++;
					}
					
					confMat.get(categories[i]).put(bestCategory, confMat.get(categories[i]).get(bestCategory) + 1);
				}
			}
			int total = tp + fp;
			double tpPerc = (double) tp / total;
			double fpPerc = (double) fp / total;

			LOG.info("TP: " + tp + "(" + tpPerc + "%)");
			LOG.info("FP: " + fp + "(" + fpPerc + "%)");
			
			LOG.info("Confusion Matrix:");
			String line = "\t\t";
			for(int i = 0; i < categories.length; i++){
				line += categories[i] + "\t";
			}
			LOG.info(line);
			for(int i = 0; i < categories.length; i++){
				line = categories[i] + "\t";
				HashMap<String, Integer> curr = confMat.get(categories[i]);
				for(int j = 0; j < categories.length; j++){
					line += curr.get(categories[j]) + "\t\t";
				}
				LOG.info(line);
			}
		}

	}

}
