package com.aylien.word2vec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aylien.util.ProjectUtils;
import com.aylien.util.PropertiesManager;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class Word2VecLib {
	
	private static Word2VecLib INSTANCE;
	private static IWord2Vec LIB;
	private static String word2VecModelPath;
	private static final Logger LOG = LoggerFactory.getLogger(Word2VecLib.class);
	

	public static Word2VecLib getInstance(String word2VecModelPath){
		LOG.info("Loading Word2VecLib...");
		PropertiesManager props = null;
		if(INSTANCE == null){
			props = new PropertiesManager();
			INSTANCE = new Word2VecLib();
			INSTANCE.word2VecModelPath = word2VecModelPath;
			LIB = (IWord2Vec) Native.loadLibrary(props.getWord2VecLibPath(), IWord2Vec.class);
			Init();
			LOG.info("Word2VecLib loaded.");
		}
		return INSTANCE;
	}
	
	public static int Init(){
		LOG.debug("Initializing Word2VecLib lib");
		int retval;
		retval = LIB.Init(word2VecModelPath);
		LOG.debug("Init() status = " + retval);
		return retval;
	}

	public static Word2VecResponse GetContextWords(String word){
		
		Pointer pWords = new Memory(Pointer.SIZE);
		IntByReference pWordsLen = new IntByReference();
		Pointer pConfidences = new Memory(Pointer.SIZE);
		IntByReference pNumberWords = new IntByReference();
		
		if(LIB.Word2VecLoad(pWords, pWordsLen, pConfidences, pNumberWords, word) == 0){
			
			String rawWords = pWords.getPointer(0).getString(0);
			String [] words = ProjectUtils.RawWordsToStringArray(rawWords);
			Pointer p = pConfidences.getPointer(0);
			float[] confidences = p.getFloatArray(0, pNumberWords.getValue());
			
			LIB.FreeMemoryString(pWords);
			LIB.FreeMemoryFloats(pConfidences);
			
			return new Word2VecResponse(word, words, confidences);
			
		}else{
			return null;
		}

	}
	
//	public static void main(String[] args) {
//		Word2VecLib lib = Word2VecLib.getInstance();
//		lib.Init();
//		int counter = 1;
////		while(true){
//		Word2VecResponse resp = lib.GetContextWords("lovely");
////		System.out.println(counter++);
////		}
//	}
}
