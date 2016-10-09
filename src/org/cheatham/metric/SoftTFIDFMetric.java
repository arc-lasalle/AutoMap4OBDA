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
import com.wcohen.ss.JaroWinkler;
import com.wcohen.ss.SoftTFIDF;
import com.wcohen.ss.api.StringWrapper;
import com.wcohen.ss.api.Tokenizer;
import com.wcohen.ss.tokens.SimpleTokenizer;

public class SoftTFIDFMetric extends Metric {

	private SoftTFIDF distance;
	private double jwThreshold = 0.9;
	
	private ArrayList<ArrayList<String>> synSets;
	
	
	public SoftTFIDFMetric() {
		super();
		
	    Tokenizer tokenizer = new SimpleTokenizer(true, false);
		distance = new SoftTFIDF(tokenizer, new JaroWinkler(), jwThreshold);
		
		synSets = new ArrayList<ArrayList<String>>();
	}
	
	
	public SoftTFIDFMetric(double t1, double t2) {
		super(t1, t2);
		
	    Tokenizer tokenizer = new SimpleTokenizer(true, false);
		distance = new SoftTFIDF(tokenizer, new JaroWinkler(), jwThreshold);
		
		synSets = new ArrayList<ArrayList<String>>();
	}
	
	public SoftTFIDFMetric(double t1, double t2, double t3) {
		super(t1, t2);
		jwThreshold = t3;
		
	    Tokenizer tokenizer = new SimpleTokenizer(true, false);
		distance = new SoftTFIDF(tokenizer, new JaroWinkler(), jwThreshold);
		
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
}
