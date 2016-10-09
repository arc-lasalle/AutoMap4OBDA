package org.cheatham.metric;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cheatham.metriccomp.Settings;
import org.cheatham.preprocessing.Preprocessing;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import com.wcohen.ss.BasicStringWrapperIterator;
import com.wcohen.ss.TFIDF;
import com.wcohen.ss.api.StringWrapper;
import com.wcohen.ss.api.Tokenizer;
import com.wcohen.ss.tokens.SimpleTokenizer;

public class TFIDFMetric extends Metric {
	
	private TFIDF distance;
	private ArrayList<ArrayList<String>> synSets;

	
	public TFIDFMetric() {
		super();
		
	    Tokenizer tokenizer = new SimpleTokenizer(true, false);
		distance = new TFIDF(tokenizer);
		
		synSets = new ArrayList<ArrayList<String>>();
	}
	
	public TFIDFMetric(double t1, double t2) {
		super(t1, t2);
		
	    Tokenizer tokenizer = new SimpleTokenizer(true, false);
		distance = new TFIDF(tokenizer);
		
		synSets = new ArrayList<ArrayList<String>>();
	}
	
	
	@Override
	public void init(OWLOntology ontologyA, OWLOntology ontologyB) {
		
        List<StringWrapper> list = new ArrayList<StringWrapper>();
        
		Set<OWLEntity> set = ontologyA.getSignature();

		for (OWLEntity e: set) {
			String label = Preprocessing.preprocess(e, ontologyA);
			
			if (Settings.synonymsAll) {
				String[] tokens = label.split("[ ]");
				for (String token: tokens) {
					list.add(distance.prepare(checkSynSets(token)));
				}
			} else {
				if (Settings.translations) {
					label = Preprocessing.translate(label, Settings.from, Settings.to);
				}

				list.add(distance.prepare(label));
			}
		}
		
		set = ontologyB.getSignature();

		for (OWLEntity e: set) {
			String label = Preprocessing.preprocess(e, ontologyB);
			
			if (Settings.synonymsAll) {
				String[] tokens = label.split("[ ]");
				for (String token: tokens) {
					list.add(distance.prepare(checkSynSets(token)));
				}
			} else {
				if (Settings.translations) {
					label = Preprocessing.translate(label, Settings.to, Settings.from);
				}

				list.add(distance.prepare(label));
			}
		}
        
        distance.train(new BasicStringWrapperIterator(list.iterator()));
	}
	

	@Override
	public double compute(String a, String b, boolean order) {
//		System.out.println(a + ", " + b + " = " + distance.score(a,b));
        return distance.score(a, b);
	}
	
	
	private String checkSynSets(String s) {
		
		for (ArrayList<String> synSet: synSets) {
			
			if (synSet.contains(s)) {
				
				HashSet<String> synonyms = Preprocessing.getSynonyms(s, "wordnet", null);
				synSet.addAll(synonyms);
				
				return synSet.get(0);
			}
		}
		
		ArrayList<String> newSynSet = new ArrayList<String>();
		newSynSet.add(s);
		
		HashSet<String> synonyms = Preprocessing.getSynonyms(s, "wordnet", null);
		newSynSet.addAll(synonyms);
		
		synSets.add(newSynSet);
		
		return s;
	}

	/*
	 * 
	 * Treats an entity as a document, as in document indexing.
	 * 
	 * The term frequency (TF) is the number of times a word appears in a document.
	 * 
	 * The inverse document frequency (IDF) is the logarithm of the number of 
	 * documents divided by the number of documents that contain the word in question.
	 * 
	 * The term frequency is then multiplied by the inverse document frequency.  
	 * This is done for each word in the document, creating a vector.
	 * 
	 * The cosine similarity of the vectors created from potentially matching 
	 * vectors is then computed and compared to a threshold.
	 * 
	 * set metric 
	 */
}
