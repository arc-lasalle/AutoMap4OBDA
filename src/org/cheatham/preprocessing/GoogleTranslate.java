package org.cheatham.preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Set;

import org.cheatham.metriccomp.Settings;
import org.cheatham.utils.StringUtils;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;


public class GoogleTranslate {

	private static HashMap<String, HashMap<String, String>> translations; // source string, language, target string
	private static HashMap<String, String> languages; // source string, language
	private static File translationFile = new File("data/translations");
	private static File languagesFile = new File("data/languages");

	protected GoogleTranslate() {
		// Exists only to defeat instantiation
	}

	@SuppressWarnings("unchecked")
	public static void initialize() {

		if (translations == null) {

			if (translationFile.exists()) {
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new FileInputStream(translationFile));
					translations = (HashMap<String, HashMap<String, String>>) ois.readObject();

				} catch (Exception e) {
					e.printStackTrace();
					translations = new HashMap<String, HashMap<String, String>>();
				}
			} else {
				translations = new HashMap<String, HashMap<String, String>>();
			}
		}

		if (languages == null) {

			if (languagesFile.exists()) {
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new FileInputStream(languagesFile));
					languages = (HashMap<String, String>) ois.readObject();

				} catch (Exception e) {
					e.printStackTrace();
					languages = new HashMap<String, String>();
				}
			} else {
				languages = new HashMap<String, String>();
			}
		}
	}
	
	
	public static String getLanguage(OWLOntology ont) {
		
		String s = "";
		int count = 0;
		
		Set<OWLEntity> entities = ont.getSignature();
		for (OWLEntity e: entities) {
			
			if (!e.isOWLClass()) {
				continue;
			}
			
			s += " " + StringUtils.getString(e, ont);
			if (++count == 10) {
				break;
			}
		}
		
		return getLanguage(s);
	}


	public static String getLanguage(String s) {
		GoogleTranslate.initialize();
		
		String result = "en";
		languages.put(s, result);
		
		return result;
		/*
		

		if (s == null) {
			return null;
		}

		if (languages.containsKey(s)) {
			return languages.get(s);

		} else {

			try {

				String string = "https://www.googleapis.com/language/translate/v2?key=" + 
				Settings.googleAPIKey + "&q=" + URLEncoder.encode(s, "utf-8") + "&target=en";

				URL query = new URL(string);

//				System.out.println("Query is " + string);

				BufferedReader in = new BufferedReader(new InputStreamReader(
						query.openStream(), "utf-8"));

				String result = "";
				String line = in.readLine();

				while (line != null) {
					if (line.contains("detectedSourceLanguage")) {
						result = line.substring(line.indexOf(":")+1);
						result = result.replaceAll("\"", "");
						result = result.replaceAll(",", "");
						result = result.trim();

						if (result.contains("-")) {
							result = result.substring(result.indexOf("-") + 1).
							trim().toLowerCase();
						}

						break;
					}
					line = in.readLine();
				}

				if (result.equals("cn")) {
					result = "zh";
				}

//				System.out.println("The result is " + result);

				languages.put(s, result);
				return result;

			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
				return null;
			}
		}
		*/
	}


	public static String translate(String word, String from, String to) {
		
		if (word == null || from == null || to == null) { return word; }

		GoogleTranslate.initialize();

		String translation = word;

		if (translations.containsKey(word)) { // we have translations for this word in some language(s)

			HashMap<String, String> thisTranslations = translations.get(word);

			if (thisTranslations.containsKey(to)) { // we have translation of this word for the target language

				translation = thisTranslations.get(to);

			} else {  // the target language isn't one of the translations we have for this word

//				System.out.println("1 hitting google translate for " + 
//						word + " from " + from + " to " + to);
				//				System.exit(0);

				translation = execTranslation(word, from, to);	
				thisTranslations.put(to, translation);
			}

		} else { // we don't have any translations for this word

//			System.out.println("2 hitting google translate for " + 
//					word + " from " + from + " to " + to);
			//	    	System.exit(0);

			translation = execTranslation(word, from, to);

			HashMap<String, String> thisTranslations = new HashMap<String, String>();
			thisTranslations.put(to, translation);
			translations.put(word, thisTranslations);
		}
		
		return translation;
	}


	private static String execTranslation(String s, String from, String to) {

		int tries = 0;
		String string = null;
		String result = "";

		while (++tries < 5) {
			try {
				string = "https://www.googleapis.com/language/translate/v2?key=" + 
				Settings.googleAPIKey + "&q=" + URLEncoder.encode(s, "utf-8") + 
				"&source=" + from + "&target=" + to;

				URL query = new URL(string);

//				System.out.println("Query is " + string);

				BufferedReader in = new BufferedReader(new InputStreamReader(
						query.openStream(), "utf-8"));

				result = "";
				String line = in.readLine();

				while (line != null) {

					if (line.contains("translatedText\":")) {
						result = line.substring(line.indexOf(":") + 1);
						result = result.replaceAll("\"", "");
						result = result.trim();
						break;
					}

					line = in.readLine();
				}

				Thread.sleep(Settings.googleThrottle);
				
				return result;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Query is " + string);
				System.out.println("The result is " + result);
				System.out.println("retrying...");
			}
		}

		close();
//		System.out.println("failed on " + s + " from " + from + " to " + to);
		System.exit(0);

		return null;
	}

	public static void close() {
		
//		System.out.println("Closing cache");
		
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(translationFile));
			oos.writeObject(translations);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(languagesFile));
			oos.writeObject(languages);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
