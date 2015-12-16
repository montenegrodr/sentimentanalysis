package com.aylien.word2vec;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public interface IWord2Vec extends Library{
	
	int Init(String word2vecFilePath);
	
	int Word2VecLoad(Pointer pWords, IntByReference pWordsLen, Pointer pConfidences, IntByReference pNumberWords, String word);
	
	void FreeMemoryString(Pointer pData);
	
	void FreeMemoryFloats(Pointer pData);

}
