package org.cheatham.metriccomp;

public class Settings {
	
	public static String googleAPIKey = "Your Key Here";
	public static long googleThrottle = 500;
	
	public static String theMetric = "org.cheatham.metric.LevensteinMetric";
	
	public static boolean classes = true;
	public static boolean properties = true;
	
	public static boolean tokenization = true;
	public static boolean stopWords = false;
	public static boolean stemming = false;
	public static boolean normalization = false;
	public static boolean synonymsAll = false;  // use with tokenization for global set metrics
	public static boolean synonymsBest = false;
	public static boolean translations = false;  // use in conjunction with tokenization
	
	public static boolean ensemble = false;
	
	public static boolean verbose = false;
	
	public static String outputFilename = "results.txt";
	
	public static String from;
	public static String to;
	
	public static String getString() {
		String s = "";
		
		s += "preprocessing\n";
		s += "\ttokenization: " + tokenization + "\n";
		s += "\tstopwords: " + stopWords + "\n";
		s += "\tstemming: " + stemming + "\n";
		s += "\tnormalization: " + normalization + "\n";
		s += "\tsynonyms-all: " + synonymsAll + "\n";
		s += "\tsynonyms-best: " + synonymsBest + "\n";
		s += "\ttranslations: " + translations + "\n";
		
		if (ensemble) {
			s += "ensemble\n";
		} else {
			s += "series\n";
		}
		
		return s;
	}
}
