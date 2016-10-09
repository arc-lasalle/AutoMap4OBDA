package org.cheatham.metric;

import com.wcohen.ss.JaroWinkler;

public class JaroWinklerMetric extends Metric {
	
	public JaroWinklerMetric() {
		super();
	}
	
	public JaroWinklerMetric(double t1, double t2) {
		super(t1, t2);
	}

	@Override
	public double compute(String a, String b, boolean order) {	
		return new JaroWinkler().score(a, b);
	}
}
