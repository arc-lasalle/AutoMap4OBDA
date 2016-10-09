package org.cheatham.metriccomp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.cheatham.metric.JaroWinklerMetric;
import org.cheatham.metric.Metric;
import org.cheatham.metric.SoftJaccardMetric;
import org.cheatham.metric.SoftTFIDFMetric;
import org.cheatham.metric.TFIDFMetric;
import org.cheatham.preprocessing.GoogleTranslate;
import org.cheatham.preprocessing.Preprocessing;
import org.cheatham.utils.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class MetricSelection {

	public static Metric chooseMetric(OWLOntology ontA, OWLOntology ontB, 
			String optimize, boolean synonyms) {
		
		Settings.from = getLanguage(ontA);
		Settings.to = getLanguage(ontB);
		
		if (!Settings.from.equals(Settings.to)) {
			Settings.translations = true;
		} else {
			Settings.translations = false;
		}
		
		double words1 = wordsPerLabel(ontA, Settings.from);
		double words2 = wordsPerLabel(ontB, Settings.to);
		
		if (optimize.equals("precision")) { 
			
			if (words1 < 2 && words2 < 2) {
				System.out.println("   Using Jaro-Winkler(1, 1)");
				return new JaroWinklerMetric(1, 1);
				
			} else {
				
				if (synonyms) {
					System.out.println("   Using Soft Jaccard(.2, .5, .9)");
					return new SoftJaccardMetric(.2, .5, .9);
				} else if (!Settings.from.equals(Settings.to)) {
					System.out.println("   Using Soft Jaccard(1, 1, .8)");
					return new SoftJaccardMetric(1, 1, .8);
				} else {
					System.out.println("   Using Soft Jaccard(1, 1, .8)");
					return new SoftJaccardMetric(1, 1, .8);
				}
			}
			
		} else {
				
			if (words1 < 2 && words2 < 2) {
				System.out.println("   Using TF-IDF(.8, .8)");
				return new TFIDFMetric(.8, .8);
				
			} else {
				if (synonyms) {
					System.out.println("   Using Soft TF-IDF(.5, .8, .8)");
					return new SoftTFIDFMetric(.5, .8, .8);
				} else if (!Settings.from.equals(Settings.to)){
					System.out.println("   Using Soft TF-IDF(0, .7, .9)");
					return new SoftTFIDFMetric(0, .7, .9);
				} else {
					System.out.println("   Using Soft TF-IDF(.8, .8, .8)");
					return new SoftTFIDFMetric(.8, .8, .8);
				}
			}
		}
	}
	
	// average number of words per entity label after tokenization
	public static double wordsPerLabel(OWLOntology ontology, String lang) {

		Set<OWLEntity> entities = ontology.getSignature();
		int words = 0;
		int labels = 0;

		for (OWLEntity entity: entities) {
			
			if (!entity.isOWLClass()) continue;

			String label = Preprocessing.stringTokenize(
					StringUtils.getString(entity, ontology), true);

			if (!lang.equals("en")) {
				label = GoogleTranslate.translate(label, lang, "en");
			}
			
			labels++;
			words += label.split(" ").length;
		}

		return words / ((double) labels);
	}


	// average number of characters per word
	public static double charactersPerLabel(OWLOntology ontology, String lang) {

		Set<OWLEntity> entities = ontology.getSignature();
		int characters = 0;
		int labels = 0;

		for (OWLEntity entity: entities) {
			
			if (!entity.isOWLClass()) continue;

			String label = StringUtils.getString(entity, ontology);
			
			if (!lang.equals("en")) {
				label = GoogleTranslate.translate(label, lang, "en");
			}
			
			labels++;
			characters += label.length();
		}

		return characters / ((double) labels);
	}


	// ratio of the number of unique words to the total number of words in the
	// ontology after tokenization
	public static double uniqueToTotalWords(OWLOntology ontology, String lang) {

		Set<OWLEntity> entities = ontology.getSignature();
		int uniqueWords = 0;
		int totalWords = 0;

		for (OWLEntity entity: entities) {
			
			if (!entity.isOWLClass()) continue;
			
			String label = Preprocessing.stringTokenize(
					StringUtils.getString(entity, ontology), true);

			if (!lang.equals("en")) {
				label = GoogleTranslate.translate(label, lang, "en");
			}
			
			String[] words = label.split(" ");
			
			HashSet<String> unique = new HashSet<String>();
			for (String w: words) {
				unique.add(w);
			}

			uniqueWords += unique.size();
			totalWords += words.length;
		}

		return uniqueWords / ((double) totalWords);
	}


	public static String getLanguage(OWLOntology ontology) {

		Set<OWLEntity> entities = ontology.getSignature();
		String testString = "";

		int count = 0;

		for (OWLEntity entity: entities) {

			if (++count == 10) {
				break;
			}

			ArrayList<String> label = Preprocessing.tokenize(
					StringUtils.getString(entity, ontology), true);

			for (String s: label) {
				testString += s + " ";	
			}
		}

		return GoogleTranslate.getLanguage(testString);
	}


	public static boolean containsSynonyms(File file) {

		try {
			
			Scanner input = new Scanner(file);

			while (input.hasNext()) {

				if (input.nextLine().toLowerCase().contains("synonym")) {
					return true;
				}
			}

			return false;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
