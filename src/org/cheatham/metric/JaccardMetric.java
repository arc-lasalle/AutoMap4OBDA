package org.cheatham.metric;

import com.wcohen.ss.Jaccard;

public class JaccardMetric extends Metric {
	
	public JaccardMetric() {
		super();
	}

	public JaccardMetric(double t1, double t2) {
		super(t1, t2);
	}

	// Jaccard(A, B) = |A intersect B| / |A union B| 
	@Override
	public double compute(String a, String b, boolean order) {
		return new Jaccard().score(a, b);
	}
}
