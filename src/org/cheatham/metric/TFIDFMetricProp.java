package org.cheatham.metric;


import org.cheatham.preprocessing.Preprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import com.wcohen.ss.BasicStringWrapperIterator;
import com.wcohen.ss.TFIDF;
import com.wcohen.ss.api.StringWrapper;
import com.wcohen.ss.api.Tokenizer;
import com.wcohen.ss.tokens.SimpleTokenizer;

public class TFIDFMetricProp {
	
	private TFIDF distance;

	
	public TFIDFMetricProp() {
		super();
		
	    Tokenizer tokenizer = new SimpleTokenizer(true, false);
		distance = new TFIDF(tokenizer);
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
	
	
	public void initIndividuals(OWLOntology ontologyA, OWLOntology ontologyB) {
		
        List<StringWrapper> list = new ArrayList<StringWrapper>();

        Set<OWLNamedIndividual> setA = ontologyA.getIndividualsInSignature();
		for (OWLEntity e: setA) {
			String label = Preprocessing.preprocess(e, ontologyA);
			list.add(distance.prepare(label));
		}
		
        Set<OWLAxiom> axioms = ontologyA.getAxioms();
		for (OWLAxiom axiom: axioms) {
			if (axiom.toString().contains("DataPropertyAssertion")) {
				OWLDataPropertyAssertionAxiom it = (OWLDataPropertyAssertionAxiom) axiom;
				OWLLiteral object = it.getObject();
				String label = object.getLiteral();
				list.add(distance.prepare(label));
			}
		}

		Set<OWLNamedIndividual> setB = ontologyB.getIndividualsInSignature();
		for (OWLEntity e: setB) {
			String label = Preprocessing.preprocess(e, ontologyB);
			list.add(distance.prepare(label));
		}
		
        axioms = ontologyB.getAxioms();
		for (OWLAxiom axiom: axioms) {
			if (axiom.toString().contains("DataPropertyAssertion")) {
				OWLDataPropertyAssertionAxiom it = (OWLDataPropertyAssertionAxiom) axiom;
				OWLLiteral object = it.getObject();
				String label = object.getLiteral();
				list.add(distance.prepare(label));
			}
		}
        
        distance.train(new BasicStringWrapperIterator(list.iterator()));
	}
	

	public double compute(String a, String b) {
//		System.out.println(a + ", " + b + " = " + distance.score(a,b));
        return distance.score(a, b);
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
