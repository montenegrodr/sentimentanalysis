package com.aylien.pos;

import com.aylien.util.ProjectUtils;

public class TaggedSequence {
	
	private String[] words;
	private String[] tags;
	
	public TaggedSequence(String[] words, String[] tags) {
		this.words = words;
		this.tags = tags;
	}

	public String[] getWords() {
		return words;
	}
	public void setWords(String[] words) {
		this.words = words;
	}
	public String[] getTags() {
		return tags;
	}
	public void setTags(String[] tags) {
		this.tags = tags;
	}

	public String toString(){
		return ProjectUtils.strJoin(words, " ");
	}
	

}
