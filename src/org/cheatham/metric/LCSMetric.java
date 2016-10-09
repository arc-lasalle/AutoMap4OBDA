package org.cheatham.metric;

public class LCSMetric extends Metric {
	
	public LCSMetric() {
		super();
	}

	public LCSMetric(double t1, double t2) {
		super(t1, t2);
	}


	public static String findLCS(String a, String b) {
		
		// figure out which string is shorter
		String shorter = a;
		String longer = b;
		
		if (a.length() > b.length()) {
			shorter = b;
			longer = a;
		}
				
		String lcs = "";
		
		// for each character in the shorter string
		for (int i=0; i<shorter.length(); i++) {
			
			char c = shorter.charAt(i);
			int location = -1;
			String current = "";
			
			do {
				
				// check to see if the character is in the longer string
				location = longer.indexOf(c, location+1);	
				int temp = 0;
								
				// if it is, start from there and count the number of identical characters
				for (int j=location; j<longer.length(); j++) {
					
					if ((i+temp) >= shorter.length() || location == -1) {
						break;
					}
					
					if (shorter.charAt(i+temp) == longer.charAt(j)) {
						current += shorter.charAt(i+temp);
					} else {
						break;
					}
					
					temp++;
				}
				
				if (current.length() > lcs.length()) {
					lcs = current;
				}
				current = "";
				
			} while (location != -1);
		}
		
		return lcs;
	}
	
	
	@Override
	public double compute(String a, String b, boolean order) {
		return LCSMetric.findLCS(a, b).length() / (double) a.length();
	}
	
	public static void main(String[] args) {
		System.out.println(new LCSMetric(0, .7).compute("Academ", "Artle", true));
	}
}
