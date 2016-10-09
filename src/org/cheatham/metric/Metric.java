package org.cheatham.metric;

import org.semanticweb.owlapi.model.OWLOntology;

public abstract class Metric {
	
	private double threshold1;
	private double threshold2;
	
	public Metric() { }
	
	public Metric(double t1, double t2) {
		threshold1 = t1;
		threshold2 = t2;
	}
	
	public void setThresholds(double t1, double t2) {
		threshold1 = t1;
		threshold2 = t2;
	}
	
	public double getThreshold1() { return threshold1; }
	public double getThreshold2() { return threshold2; }
	
	public void init(OWLOntology ontologyA, OWLOntology ontologyB) {
		// subclasses can override this if necessary -- likely for set metrics
	}
	
	public abstract double compute(String a, String b, boolean order);
}
