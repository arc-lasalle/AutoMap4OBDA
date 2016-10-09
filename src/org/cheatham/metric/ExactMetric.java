package org.cheatham.metric;

public class ExactMetric extends Metric {
	
	public ExactMetric() {
		super();
	}

	public ExactMetric(double t1, double t2) {
		super(t1, t2);
	}

	@Override
	public double compute(String a, String b, boolean order) {
		if (b.startsWith(a)) {
			return 1;
		}
		return 0;
	}
}
