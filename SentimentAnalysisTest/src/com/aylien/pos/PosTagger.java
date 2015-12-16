package com.aylien.pos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Streams;
import com.aylien.util.PropertiesManager;

public class PosTagger {

	private static final Logger LOG = LoggerFactory.getLogger(PosTagger.class);

	static TokenizerFactory TOKENIZER_FACTORY = new RegExTokenizerFactory("(-|'|\\d|\\p{L})+|\\S");
	private static PosTagger INSTANCE;
	private static HmmDecoder DECODER;

	public PosTagger(File posTaggerPath) throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(posTaggerPath);
		ObjectInputStream objIn = new ObjectInputStream(fileIn);
		HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
		Streams.closeQuietly(objIn);
		DECODER = new HmmDecoder(hmm);
	}

	public static TaggedSequence TagSequence(String sequence) {
		TaggedSequence tagSeq = null;

		char[] cs = sequence.toCharArray();
		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(cs, 0, cs.length);
		String[] tokens = tokenizer.tokenize();
		List<String> tokenList = Arrays.asList(tokens);
		Tagging<String> tagging = DECODER.tag(tokenList);
		
		int size = tagging.size();
		
		String [] words = new String[size];
		String [] tags = new String[size];
		
		for(int i = 0; i < size; i++){
			words[i]= tagging.token(i);
			tags[i] = tagging.tag(i);
		}

		return new TaggedSequence(words, tags);
	}

	public static PosTagger getInstance() {
		if (INSTANCE == null) {
			try {
				PropertiesManager props = new PropertiesManager();
				File posTaggerPath = props.getPosTaggerModel();
				INSTANCE = new PosTagger(posTaggerPath);
			} catch (FileNotFoundException e) {
				LOG.error(e.getMessage());
			} catch (ClassNotFoundException e) {
				LOG.error(e.getMessage());
			} catch (IOException e) {
				LOG.error(e.getMessage());
			}
		}
		return INSTANCE;
	}

	public static void main(String[] args) {
		PosTagger posTagger = PosTagger.getInstance();
		String seq = "You are lovely";
		TaggedSequence taggedSeq = posTagger.TagSequence(seq);
	}
}
