package org.cheatham.metric;

import org.cheatham.preprocessing.Preprocessing;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import com.wcohen.ss.BasicStringWrapperIterator;
import com.wcohen.ss.JaroWinkler;
import com.wcohen.ss.SoftTFIDF;
import com.wcohen.ss.api.StringWrapper;
import com.wcohen.ss.api.Tokenizer;
import com.wcohen.ss.tokens.SimpleTokenizer;

public class SoftTFIDFMetricProp {

	private SoftTFIDF distance;
	private double jwThreshold = 0.9;
	
	
	public SoftTFIDFMetricProp() {
		super();
		
	    Tokenizer tokenizer = new SimpleTokenizer(true, false);
		distance = new SoftTFIDF(tokenizer, new JaroWinkler(), jwThreshold);
	}
	
	
	public SoftTFIDFMetricProp(double threshold) {
		super();
		
		jwThreshold = threshold;
	    Tokenizer tokenizer = new SimpleTokenizer(true, false);
		distance = new SoftTFIDF(tokenizer, new JaroWinkler(), jwThreshold);
	}
	
	
	public void init(OWLOntology ontologyA, OWLOntology ontologyB) {
		
        List<StringWrapper> list = new ArrayList<StringWrapper>();
        
		Set<OWLEntity> set = ontologyA.getSignature();

		for (OWLEntity e: set) {
			String label = Preprocessing.preprocess(e, ontologyA);
			list.add(distance.prepare(label));
		}
		
		set = ontologyB.getSignature();

		for (OWLEntity e: set) {
			String label = Preprocessing.preprocess(e, ontologyB);
			list.add(distance.prepare(label));
		}
        
        distance.train(new BasicStringWrapperIterator(list.iterator()));
	}
	
	
	public void init(Set<OWLEntity> setA, OWLOntology ontologyA, 
			Set<OWLEntity> setB, OWLOntology ontologyB) {
		
        List<StringWrapper> list = new ArrayList<StringWrapper>();

		for (OWLEntity e: setA) {
			String label = Preprocessing.preprocess(e, ontologyA);
			list.add(distance.prepare(label));
		}

		for (OWLEntity e: setB) {
			String label = Preprocessing.preprocess(e, ontologyB);
			list.add(distance.prepare(label));
		}
        
        distance.train(new BasicStringWrapperIterator(list.iterator()));
	}
	

	public double compute(String a, String b) {
        return distance.score(a, b);
	}
}
