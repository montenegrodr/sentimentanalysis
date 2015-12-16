package com.aylien.word2vec;

import java.io.UnsupportedEncodingException;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class Word2VectorDLLTest {

	public static void main(String[] args) throws UnsupportedEncodingException {

		IWord2Vec dll = (IWord2Vec) Native.loadLibrary("Word2VecLoader", IWord2Vec.class);
		int x = dll.Init("E:\\bases\\sentimentanalysis\\word2vec_twitter_model\\word2vec_twitter_model.bin");

		Pointer pWords = new Memory(Pointer.SIZE);
		IntByReference pWordsLen = new IntByReference();
		Pointer pConfidences = new Memory(Pointer.SIZE);
		IntByReference pNumberWords = new IntByReference();
		
		int count = 1;
		while(true){
			String word = "coke";
			
			dll.Word2VecLoad(pWords, pWordsLen, pConfidences, pNumberWords, word);
			String decoded = pWords.getPointer(0).getString(0);
			
			Pointer p = pConfidences.getPointer(0);
			float[] b = p.getFloatArray(0, pNumberWords.getValue());
			
			dll.FreeMemoryString(pWords);
			dll.FreeMemoryFloats(pConfidences);
			
			System.out.println(decoded + " " + count++);
		}

	}
	

	

}
