package org.cheatham.metric;

import java.util.HashSet;
import java.util.Set;


public class SoftJaccardMetric extends Metric {

	private double levThreshold = 0.8;
	
	public SoftJaccardMetric() {
		super();
	}
	
	public SoftJaccardMetric(double t1, double t2) {
		super(t1, t2);
	}
	
	public SoftJaccardMetric(double t1, double t2, double t3) {
		super(t1, t2);
		levThreshold = t3;
	}

	@Override
	public double compute(String a, String b, boolean order) {
		
		String[] aStrings = a.split("[ ]");
		String[] bStrings = b.split("[ ]");
		
		Set<String> aSet = new HashSet<String>();
		
		for (int i=0; i<aStrings.length; i++) {
			
			boolean addThisOne = true;
			
			for (String alreadyIn: aSet) {
				if (new LevensteinMetric(0, 0).compute(aStrings[i], alreadyIn, true) >= levThreshold) {
					addThisOne = false;
				}
			}
			
			if (addThisOne) {
				aSet.add(aStrings[i]);
			}
		}
//		System.out.println(aSet);
		
		Set<String> bSet = new HashSet<String>();
		
		for (int i=0; i<bStrings.length; i++) {
			
			boolean addThisOne = true;
			
			for (String alreadyIn: bSet) {
				if (new LevensteinMetric(0, 0).compute(bStrings[i], alreadyIn, true) >= levThreshold) {
					addThisOne = false;
				}
			}
			
			if (addThisOne) {
				bSet.add(bStrings[i]);
			}
		}
//		System.out.println(bSet);
		
		Set<String> intersection = new HashSet<String>();
		intersection.addAll(aSet);
		Set<String> toRemove = new HashSet<String>();
		
		for (String i: intersection) {
			
			boolean removeThisOne = true;
			
			for (String s: bSet) {
				if (new LevensteinMetric(0, 0).compute(i, s, true) >= levThreshold) {
					removeThisOne = false;
				}
			}
			
			if (removeThisOne) {
				toRemove.add(i);
			}
		}
		
		intersection.removeAll(toRemove);
//		System.out.println(intersection);
		
		String[] unionArray = new String[aSet.size() + bSet.size()];
		int count = 0;
		for (String s: aSet) {
			unionArray[count++] = s;
		}
		for (String s: bSet) {
			unionArray[count++] = s;
		}
		
		Set<String> union = new HashSet<String>();
		
		for (int i=0; i<count; i++) {
			
			boolean addThisOne = true;
			
			for (String alreadyIn: union) {
				if (new LevensteinMetric(0, 0).compute(unionArray[i], alreadyIn, true) >= levThreshold) {
					addThisOne = false;
				}
			}
			
			if (addThisOne) {
				union.add(unionArray[i]);
			}
		}
//		System.out.println(union);
		
		return intersection.size() / (double) (union.size());
	}

	public static void main(String[] args) {
		String one = "One fish two wish";
		String two = "Red fsih blue wsih";
		
		System.out.println(new SoftJaccardMetric(1, 1).compute(one, two, false));
	}
}
