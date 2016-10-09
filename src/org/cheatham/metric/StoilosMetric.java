package org.cheatham.metric;


public class StoilosMetric extends Metric {
	
	public StoilosMetric() {
		super();
	}

	public StoilosMetric(double t1, double t2) {
		super(t1, t2);
	}

	private String unmatchedA = "";
	private String unmatchedB = "";
	
	/*
	 * SMOA (Stoilos metric)
		• Sim(a,b) = Comm(a,b) - Diff(a,b) + Winkler(a,b)
		• Comm(a,b) finds the longest common substring, then removes it from both strings and finds the next longest
		substring, and so on until none remain. Then their lengths are summed and divided by the sum of the original
		string lengths.
		• Comm(a,b) = (2 * Summation{maxComSubString}) / (a.length + b.length)
		• Diff(a,b) = (uLenA * uLenB) / (p + (1-p) * (uLenA + uLenb - uLenA * uLenB))
		• uLenX is the length of the unmatched part of the string from the first step divided by the length of the
		corresponding original string.
		• p is the importance of the difference factor. The authors experimentally found 0.6 to be a good choice.
		• “Winkler” is the same as JaroWinkler, defined above
		• The metric ranges from -1 to +1
	 */
	
	private double comm(String a, String b) {
		StringBuffer aString = new StringBuffer(a);
		StringBuffer bString = new StringBuffer(b);
		
		String common = "";
		int sum = 0;
		
		do {
			common = LCSMetric.findLCS(aString.toString(), bString.toString());
			sum += common.length();
			
			aString.replace(aString.indexOf(common), aString.indexOf(common) + common.length(), "");
			bString.replace(bString.indexOf(common), bString.indexOf(common) + common.length(), "");
			
		} while (!common.equals(""));
		
		unmatchedA = aString.toString();
		unmatchedB = bString.toString();
		
		return (2 * sum) / (double) (a.length() + b.length());
	}
	
	private double diff(String a, String b) {
		double uLenA = unmatchedA.length() / (double) a.length();
		double uLenB = unmatchedB.length() / (double) b.length();
		double p = .6;
		
		return (uLenA * uLenB) / (p + (1-p) * (uLenA + uLenB - uLenA * uLenB));
	}
	
	@Override
	public double compute(String a, String b, boolean order) {
//		return comm(a,b) - diff(a, b) + new JaroWinkler().score(a, b);
		return comm(a,b) - diff(a, b);
	}

	public static void main(String[] args) {
		System.out.println(new StoilosMetric(0, .6).compute("bbbaaaaa", "bbbaaaaa", true));
	}
}
