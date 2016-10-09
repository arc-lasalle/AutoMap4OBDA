package org.cheatham.metric;

import com.wcohen.ss.Levenstein;

public class LevensteinMetric extends Metric {
	
	public LevensteinMetric() {
		super();
	}
	
	public LevensteinMetric(double t1, double t2) {
		super(t1, t2);
	}

	private double distance(String a, String b) {
		return Math.abs(new Levenstein().score(a, b));
	}
	
	@Override
	public double compute(String a, String b, boolean order) {	
		return 1 - (distance(a, b) / (double) a.length());
	}

}
