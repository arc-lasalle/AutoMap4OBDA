package org.cheatham.metric;

import java.util.ArrayList;
import java.util.HashSet;

public class NGramMetric extends Metric {
	
	public NGramMetric() {
		super();
	}

	public NGramMetric(double t1, double t2) {
		super(t1, t2);
	}

	private static ArrayList<String> ngrams(String s, int n) {
		ArrayList<String> ngrams = new ArrayList<String>();

		int numNgrams = s.length() + (n-1) - 1;

		for (int i=0; i<=numNgrams; i++) {

			if (i < n-1) {
				String ngram = "";

				// add the prefix characters
				int prefixes = Math.max(n-i-1, 0);
				for (int j=0; j<prefixes; j++) {
					ngram += "#";
				}

				// add the characters to fill out the prefix
				int prefixCharacters = 0;
				if (prefixes > 0) {
					prefixCharacters = n-prefixes;
					for (int j=0; j<prefixCharacters; j++) {
						if (j < s.length()) {
							ngram += s.charAt(j);
						}
					}
				}

				// fill out the suffix if needed
				while (ngram.length() < n) {
					ngram += "%";
				}

				ngrams.add(ngram);

			} else if (i >= n-1 && i < s.length()) {
				String ngram = "";

				for (int j=i+1-n; j<i+1; j++) {
					ngram += s.charAt(j);
				}

				ngrams.add(ngram);

			} else {
				String ngram = "";

				// figure out the number of suffix characters
				int suffixes = i - s.length() + 1;

				// add the characters to fill out the suffix
				for (int j=i+1-n; j<s.length(); j++) {
					ngram += s.charAt(j);
				}

				// add the suffix characters
				for (int j=0; j<suffixes; j++) {
					ngram += "%";
				}

				ngrams.add(ngram);
			}
		}

		return ngrams;
	}

	@Override
	public double compute(String a, String b, boolean order) {

		ArrayList<String> ngrams1 = NGramMetric.ngrams(a, 3);
		ArrayList<String> ngrams2 = NGramMetric.ngrams(b, 3);

		HashSet<String> visited = new HashSet<String>();
		int intersection = 0;

		for (String ngram1: ngrams1) {

			if (visited.contains(ngram1)) {
				continue;
			}

			// handle multiple occurrences of a substring
			int count = 0;
			int matched = 0;
			for (String temp: ngrams1) {
				if (temp.equals(ngram1)) {
					count++;
				}
			}

			for (String ngram2: ngrams2) {
				if (ngram1.equals(ngram2)) {
					matched++;
				}
			}

			intersection += Math.min(count, matched);
			visited.add(ngram1);
		}

		return intersection / (double) ngrams1.size();
	}

	public static void main(String[] args) {
		System.out.println(new NGramMetric(0, .8).compute("hellohello", "hello", true));
	}

}
