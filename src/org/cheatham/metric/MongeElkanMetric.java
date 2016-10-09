package org.cheatham.metric;

import com.wcohen.ss.MongeElkan;

public class MongeElkanMetric extends Metric {
	
	public MongeElkanMetric() {
		super();
	}

	public MongeElkanMetric(double t1, double t2) {
		super(t1, t2);
	}

	@Override
	public double compute(String a, String b, boolean order) {	
		return new MongeElkan().score(a, b);
	}
	
	public static void main(String[] args) {
		System.out.println(new MongeElkanMetric(1.0, 1.0).compute("hello", "help", false));
	}

}
