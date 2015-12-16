package com.aylien.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aylien.pos.PosTagger;
import com.aylien.pos.TaggedSequence;
import com.aylien.word2vec.Word2VecLib;
import com.aylien.word2vec.Word2VecResponse;

public class SentencesUtils {

	private static final Logger LOG = LoggerFactory.getLogger(SentencesUtils.class);

	private Word2VecLib word2vec;
	private PosTagger posTagger;

	public SentencesUtils(Word2VecLib word2vec, PosTagger posTagger) {
		this.word2vec = word2vec;
		this.posTagger = posTagger;
	}

	public String[] GenerateSequencesByContext(String seq, int n, String pos) {

		String[] retval = null;
		TaggedSequence tagSeq = PosTagger.getInstance().TagSequence(seq);
		String[] wordsTagSeq = null;
		String[] tagTagSeq = null;
		int wordIndex = -1;
		String word = null;
		Word2VecResponse resp = null;

		if (tagSeq != null) {

			wordsTagSeq = tagSeq.getWords();
			tagTagSeq = tagSeq.getTags();

			for (int i = 0; i < tagTagSeq.length; i++) {
				if (tagTagSeq[i].equals(pos)) {
					wordIndex = i;
					word = wordsTagSeq[i];
				}
			}

			if (wordIndex != -1) {
				if ((resp = word2vec.GetContextWords(word)) != null) {
					retval = new String[n];
					String[] newWords = new String[n];
					int newc = 0;
					for (int j = 0; j < resp.getWords().length; j++) {
						if (!resp.getWords()[j].toLowerCase().equals(word.toLowerCase())) {
							newWords[newc++] = resp.getWords()[j];
						}
						if (newc >= n)
							break;
					}

					if (newc < n)
						n = newc;
					for (int j = 0; j < n; j++) {
						retval[j] = "";
					}
					for (int i = 0; i < wordsTagSeq.length; i++) {
						if (i != wordIndex) {
							for (int j = 0; j < n; j++) {
								retval[j] = retval[j] + wordsTagSeq[i] + " ";
							}
						} else {
							for (int j = 0; j < n; j++) {
								retval[j] = retval[j] + newWords[j] + " ";
							}
						}
					}
				}
			}
		}
		return retval;
	}

	public String GenerateSequenceByContext(String seq, String pos) {
		String retSeq = null;
		String[] words = null;
		String[] tags = null;
		String[] wordsTagSeq = null;
		String[] tagTagSeq = null;
		boolean fail = true;

		TaggedSequence tagSeq = PosTagger.getInstance().TagSequence(seq);

		if (tagSeq != null) {

			wordsTagSeq = tagSeq.getWords();
			tagTagSeq = tagSeq.getTags();

			words = new String[wordsTagSeq.length];
			tags = new String[wordsTagSeq.length];

			for (int i = 0; i < tagTagSeq.length; i++) {
				fail = true;
				if (tagTagSeq[i].equals(pos)) {
					Word2VecResponse resp = word2vec.GetContextWords(wordsTagSeq[i]);
					if (resp != null) {
						for (String word : resp.getWords()) {
							if (word.toLowerCase().equals(wordsTagSeq[i].toLowerCase()))
								continue;
							else {
								words[i] = word;
								tags[i] = tagTagSeq[i];
								fail = false;
								break;
							}
						}
					}
				}
				if (fail) {
					words[i] = wordsTagSeq[i];
					tags[i] = tagTagSeq[i];
				}
			}

			TaggedSequence retTagSeq = new TaggedSequence(words, tags);
			retSeq = retTagSeq.toString();
		}

		return retSeq;
	}

	// public static void main(String[] args) {
	// Word2VecLib word2vec = Word2VecLib.getInstance();
	//
	// PosTagger posTagger = PosTagger.getInstance();
	//
	// SentencesUtils senUtils = new SentencesUtils(word2vec, posTagger);
	// String[] otherSentences = senUtils.GenerateSequencesByContext("You are
	// sensational", 5, "jj");
	// for (String s : otherSentences) {
	// System.out.println(s);
	// }
	// }
}
