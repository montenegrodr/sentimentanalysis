package com.aylien.word2vec;

public class Word2VecResponse {

	private String word;
	private String[] words;
	private float[] confidences;
	

	public Word2VecResponse(String word, String[] words, float[] confidences) {
		this.word = word;
		this.words = words;
		this.confidences = confidences;
	}


	public String getWord() {
		return word;
	}


	public void setWord(String word) {
		this.word = word;
	}


	public String[] getWords() {
		return words;
	}


	public void setWords(String[] words) {
		this.words = words;
	}


	public float[] getConfidences() {
		return confidences;
	}


	public void setConfidences(float[] confidences) {
		this.confidences = confidences;
	}

}
