package org.cheatham.preprocessing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

public class WordNet {

	private static WordNet instance = null;
	private static Dictionary dictionary;
	
	private static HashMap<String, HashSet<String>> cache;
	private static File synonymFile = new File("data/synonyms");

	protected WordNet() {
		// Exists only to defeat instantiation
	}

	public static WordNet getInstance() {

		if (instance == null) {
			instance = new WordNet();

			try {
				JWNL.initialize(new FileInputStream(new File("data/wordnet_properties.xml")));
			} catch (Exception e) {
				e.printStackTrace();
			}

			dictionary = Dictionary.getInstance();
			
			if (synonymFile.exists()) {
				try {
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(synonymFile));
					cache = (HashMap<String, HashSet<String>>) ois.readObject();
				} catch (Exception e) {
					e.printStackTrace();
					cache = new HashMap<String, HashSet<String>>();
				}
			} else {
				cache = new HashMap<String, HashSet<String>>();
			}
		}

		return instance;
	}

	public HashSet<String> getSynonyms(String word) {
		HashSet<String> synonyms = new HashSet<String>();
		
		if (cache.containsKey(word)) {
			synonyms = cache.get(word);
			
		} else {

			StringTokenizer strTok = new StringTokenizer(word, " ");
			while (strTok.hasMoreTokens()) {

				String wd = strTok.nextToken();

				try {
					IndexWordSet indexWordSet = dictionary.lookupAllIndexWords(wd);
					IndexWord[] indexWords = indexWordSet.getIndexWordArray();
					for (IndexWord indexWord: indexWords) {
						Synset[] synSets = indexWord.getSenses();
						for (Synset synSet: synSets) {
							Word[] words = synSet.getWords();
							for (Word w: words) {
								synonyms.add(w.getLemma());
							}
						}
					}

				} catch (JWNLException e) {
					e.printStackTrace();
				}
			}

			cache.put(word, synonyms);
		}

		return synonyms;
	}
	
	public static void close() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(synonymFile));
			oos.writeObject(cache);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		WordNet wn = WordNet.getInstance();
		System.out.println(wn.getSynonyms("baseball bat"));
	}
}
