package org.cheatham.preprocessing;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.cheatham.metriccomp.Settings;
import org.cheatham.utils.StringUtils;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.tartarus.snowball.ext.porterStemmer;


/*
public class Preprocessing {
	
	public static String preprocess(OWLEntity x, OWLOntology ontologyX) {
		String s = StringUtils.getString(x, ontologyX);
		
		s = s.replaceAll("wordnet_", "");
		s = s.replaceAll("wikicategory_", "");
		
		s = Preprocessing.stringTokenize(s, true);
		return s;
	}
	
	
	public static String preprocess(String label) {
		String s = StringUtils.getString(label);
		
		s = s.replaceAll("wordnet_", "");
		s = s.replaceAll("wikicategory_", "");
		
		s = Preprocessing.stringTokenize(s, true);
		return s;
	}
	

	public static ArrayList<String> tokenize(String s, boolean lowercase) {
		if (s == null) {
			return null;
		}

		ArrayList<String> strings = new ArrayList<String>();

		String current = "";
		Character prevC = 'x';

		for (Character c: s.toCharArray()) {

			if ((Character.isLowerCase(prevC) && Character.isUpperCase(c)) || 
					c == '_' || c == '-' || c == ' ' || c == '/' || c == '\\' || c == '>') {

				current = current.trim();

				if (current.length() > 0) {
					if (lowercase) 
						strings.add(current.toLowerCase());
					else
						strings.add(current);
				}

				current = "";
			}

			if (c != '_' && c != '-' && c != '/' && c != '\\' && c != '>') {
				current += c;
				prevC = c;
			}
		}

		current = current.trim();

		if (current.length() > 0) {
			// this check is to handle the id numbers in YAGO
			if (!(current.length() > 4 && Character.isDigit(current.charAt(0)) && 
					Character.isDigit(current.charAt(current.length()-1)))) {
				strings.add(current.toLowerCase());
			}
		}

		return strings;
	}

	public static String stringTokenize(String s, boolean lowercase) {
		String result = "";

		ArrayList<String> tokens = tokenize(s, lowercase);
		for (String token: tokens) {
			result += token + " ";
		}

		return result.trim();
	}
	
	public static String translate(String s, String from, String to) {
		return GoogleTranslate.translate(s, from, to);
	}
	
	public static HashSet<String> getSynonyms(String s, String type, URI uri) {
		HashSet<String> synonyms = new HashSet<String>();

		if (type.equals("wordnet")) {

			WordNet wordNet = WordNet.getInstance();
			synonyms = wordNet.getSynonyms(s);

		} else if (type.equals("human")) {

			HumanSynonyms human = new HumanSynonyms();
			try {
				synonyms = human.getSynonyms(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (type.equals("mouse")) {

			MouseSynonyms mouse = new MouseSynonyms();
			try {
				synonyms = mouse.getSynonyms(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return synonyms;
	}
	
	
}

*/
public class Preprocessing {
	
	public static String preprocess(OWLEntity x, OWLOntology ontologyX) {
		
		String s = StringUtils.getString(x, ontologyX);
		
		if (Settings.tokenization) {
			s = Preprocessing.stringTokenize(s, true);
		}
		
		if (Settings.stopWords) {
			//s = Preprocessing.removeStopwords(s);
		}
		
		if (Settings.stemming) {
			s = Preprocessing.stem(s);
		}
		
		if (Settings.normalization) {
			s = Preprocessing.normalize(s);
		}
		
		return s;
	}
	
	public static String preprocess(String s) {
		
		if (Settings.tokenization) {
			s = Preprocessing.stringTokenize(s, true);
		}
		
		if (Settings.stopWords) {
			s = Preprocessing.removeStopwords(s);
		}
		
		if (Settings.stemming) {
			s = Preprocessing.stem(s);
		}
		
		if (Settings.normalization) {
			s = Preprocessing.normalize(s);
		}
		
		return s;
	}

	public static ArrayList<String> tokenize(String s, boolean lowercase) {
		if (s == null) {
			return null;
		}

		ArrayList<String> strings = new ArrayList<String>();

		String current = "";
		Character prevC = 'x';

		for (Character c: s.toCharArray()) {

			if ((Character.isLowerCase(prevC) && Character.isUpperCase(c)) || 
					c == '_' || c == '-' || c == ' ' || c == '/' || c == '\\') {

				current = current.trim();

				if (current.length() > 0) {
					if (lowercase) 
						strings.add(current.toLowerCase());
					else
						strings.add(current);
				}

				current = "";
			}

			if (c != '_' && c != '-' && c != '/' && c != '\\') {
				current += c;
				prevC = c;
			}
		}

		current = current.trim();

		if (current.length() > 0) {
			strings.add(current.toLowerCase());
		}

		return strings;
	}

	public static String stringTokenize(String s, boolean lowercase) {
		String result = "";

		ArrayList<String> tokens = tokenize(s, lowercase);
		for (String token: tokens) {
			result += token + " ";
		}

		return result.trim();
	}


	public static String removeStopwords(String s) {
		return StopWords.removeStopwords(s);
	}

	public static HashSet<String> getSynonyms(String s, String type, URI uri) {
		HashSet<String> synonyms = new HashSet<String>();

		if (type.equals("wordnet")) {

			WordNet wordNet = WordNet.getInstance();
			synonyms = wordNet.getSynonyms(s);

		} else if (type.equals("human")) {

			HumanSynonyms human = new HumanSynonyms();
			try {
				synonyms = human.getSynonyms(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (type.equals("mouse")) {

			MouseSynonyms mouse = new MouseSynonyms();
			try {
				synonyms = mouse.getSynonyms(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return synonyms;
	}

	public static String translate(String s, String from, String to) {
		return GoogleTranslate.translate(s, from, to);
	}
	
	public static String transliterate(String s) {
		String first = "";

		for (Character c: s.toCharArray()) {
			first += CharacterMap.translateCharacter(c);
		}
		
		return first;
	}

	public static String normalize(String s) {

		// remove non-ascii characters
		String first = "";

		for (Character c: s.toCharArray()) {
			first += CharacterMap.translateCharacter(c);
		}
		
		// remove hyphens and punctuation
		String letters = "";
		
		for (Character c: first.toCharArray()) {
			if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
				letters += c;
			}
		}

		// alphabetize words
		ArrayList<String> words = Preprocessing.tokenize(letters, true);
		Collections.sort(words);

		String result = "";
		for (String temp: words) {
			result += temp + " ";
		}

		return result.trim();
	}

	public static String stem(String s) {

		porterStemmer stemmer = new porterStemmer();
		ArrayList<String> words = Preprocessing.tokenize(s, false);

		String result = "";
		for (String temp: words) {
			stemmer.setCurrent(temp);
			stemmer.stem();
			result += stemmer.getCurrent() + " ";
		}

		return result.trim();
	}

	public static void main(String[] args) {
		String input = "修改�?�投标";
		String normalized = Preprocessing.normalize(input);
		System.out.println("normalized: " + normalized);

		//		System.out.println("tokenized: " + Preprocessing.tokenize(normalized, true));
		//		System.out.println("stopwords removed: " + Preprocessing.removeStopwords(normalized));
		//		System.out.println("stemmed: " + Preprocessing.stem(normalized));
		//		
		//		GoogleTranslate.initialize();
		//		System.out.println(Preprocessing.translate("Bonjour le monde", Language.FRENCH, Language.ENGLISH));
		//		GoogleTranslate.close();
	}
}
