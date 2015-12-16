package com.aylien.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.io.FileDeleteStrategy;

import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aylien.data.TrainMethod;

public class ProjectUtils {

	public static String CHARSET = "UTF-8";
	public static String BREAKLINE = "\n";
	public static String MODEL_NAME = "model.dat";
	public static String TEST_DIR_NAME = "tests";
	public static String SEP = "#";
	
	public static String[] GetAllLines(File textFile) throws IOException {
		return GetAllLines(Files.readFromFile(textFile, CHARSET));
	}

	private static String[] GetAllLines(String textFile) {
		return textFile.split(BREAKLINE);
	}

	public static void SuffleArray(String[] arr) {

		Random rnd = ThreadLocalRandom.current();
		for (int i = arr.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			// Simple swap
			String a = arr[index];
			arr[index] = arr[i];
			arr[i] = a;
		}
	}

	public static TrainMethod returnAsTrainMethod(String prop) {

		switch (prop) {
		case "holdout":
			return TrainMethod.HOLDOUT;
		case "crossvalidation":
			return TrainMethod.CROSSVALIDATION;
		default:
			return TrainMethod.HOLDOUT;
		}
	}

	public static void SaveModel(LogisticRegressionClassifier<CharSequence> classifier, String[] categories,
			List<String[]> testSentences, File modelsDir, String modelName) throws IOException {

		File categoryFile = null;
		File classifierDestFile = null;
		File testsDir = null;
		File modelDir = new File(modelsDir, modelName);

		if (!modelDir.exists()) {
			modelDir.mkdirs();
		} else if (modelDir.list().length > 0) {
			FileDeleteStrategy.FORCE.delete(modelDir);
			// modelDir.delete();
			modelDir.mkdir();
		}

		classifierDestFile = new File(modelDir, MODEL_NAME);
		AbstractExternalizable.compileTo(classifier, classifierDestFile);

		testsDir = new File(modelDir, TEST_DIR_NAME);
		testsDir.mkdirs();

		for (int i = 0; i < categories.length; i++) {
			categoryFile = new File(testsDir, categories[i]);
			InsertLines(testSentences.get(i), categoryFile);
		}

	}

	private static void InsertLines(String[] strings, File categoryFile) throws IOException {
		List<String> lines = Arrays.asList(strings);
		java.nio.file.Files.write(Paths.get(categoryFile.getAbsolutePath()), lines, StandardCharsets.UTF_8);
	}

	public static double calcPolarity(ConditionalClassification cc) {

		double negativeWeight = 0, neutralWeight = 0, positiveWeight = 0;

		for (int i = 0; i < 3; i++) {
			switch (cc.category(i)) {
			case "negative-all":
				negativeWeight = cc.conditionalProbability(i);
				break;
			case "positive-all":
				positiveWeight = cc.conditionalProbability(i);
				break;
			case "neutral-all":
				neutralWeight = cc.conditionalProbability(i);
				break;
			}
		}

		return (positiveWeight - negativeWeight) * (1 - neutralWeight);
	}
	
	public static double calcPolarity(List<ConditionalClassification> ccResults) {
		
		double meanPolarity = 0;
		
		for(ConditionalClassification cc : ccResults){
			meanPolarity += calcPolarity(cc);
		}
		
		return (double)meanPolarity/ccResults.size();
	}
	
	public static String getBestCategory(List<ConditionalClassification> ccResults) {
		double bestScore = Double.MIN_VALUE;
		String bestCategory = null;
		for(ConditionalClassification cc : ccResults){
			if(cc.conditionalProbability(0) > bestScore)
			{
				bestCategory = cc.bestCategory();
				bestScore = cc.conditionalProbability(0);
			}
		}
		
		return bestCategory;
	}

	public static String[] RawWordsToStringArray(String rawWords) {
		String aux = rawWords.substring(1,  rawWords.length());
		return aux.split(SEP);
	}

	public static String strJoin(String[] aArr, String sSep) {
	    StringBuilder sbStr = new StringBuilder();
	    for (int i = 0, il = aArr.length; i < il; i++) {
	        if (i > 0)
	            sbStr.append(sSep);
	        sbStr.append(aArr[i]);
	    }
	    return sbStr.toString();
	}


}
