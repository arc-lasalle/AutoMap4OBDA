package org.cheatham.utils;

import java.util.*;

/**
 * An implementation of the stable marriage algorithm from
 * Chapter 1-2 in "Algorithm Design" by Kleinberg and Tardos.
 *
 * @author Stefan Nilsson
 * @version 2008.10.23
 * 
 * Modified by @author Michelle Cheatham
 * @version 2012.3.31
 */
public class StableMarriage
{
	// Number of men
	private int n;
	
	// Number of women
	private int n1;

	// Preference tables (size nxn)
	private double[][] manPref;
	private double[][] womanPref;
	
	private boolean[][] proposedTo;

	private static final boolean DEBUGGING = false;

	/**
	 * Creates and solves a random stable marriage problem of
	 * size n men and n1 women, where these values are hardcoded
	 */
	public static void main(String[] args) {
		int n = 3;
		int n1 = 2;
		StableMarriage sm = new StableMarriage(n, n1);
		if (n <= 10)
			sm.printPrefTables();
		int[] marriage = sm.stable();
		if (n <= 10)
			sm.printMarriage(marriage);
	}

	/**
	 * Creates a marriage problem of size n men and n1 women
	 *  with random preferences.
	 */
	public StableMarriage(int n, int n1) {
		this.n = n;
		this.n1 = n1;
		manPref = new double[n][];
		womanPref = new double[n1][];
		for (int i = 0; i < n; i++) {
			manPref[i] = new double[n1];
			createRandomPrefs(manPref[i]);
		}
		for (int i = 0; i < n1; i++) {
			womanPref[i] = new double[n];
			createRandomPrefs(womanPref[i]);
		}
		proposedTo = new boolean[n][n1];
		for (int i=0; i<n; i++)
			for (int j=0; j<n1; j++) 
				proposedTo[i][j] = false;
	}

	/**
	 * Creates a marriage problem with given preferences.
	 */
	public StableMarriage(double[][] manPref, double[][] womanPref) {
		this.n = manPref.length;
		this.n1 = womanPref.length;
		this.manPref = manPref;
		this.womanPref = womanPref;
		proposedTo = new boolean[n][n1];
		for (int i=0; i<n; i++)
			for (int j=0; j<n1; j++) 
			proposedTo[i][j] = false;
	}
	
	
	/**
	 * Fills the array with random values (0 through 1)
	 * Values are doubles to two decimal places.
	 */
	private void createRandomPrefs(double[] v) {
		for (int i = 0; i < v.length; i++) {
			double value = Math.random();
			value *= 100;
			value = Math.floor(value);
			value /= 100;
			v[i] = value;
		}
	}

	/**
	 * Returns a stable marriage in the form an int array v,
	 * where v[i] is the man married to woman i.
	 */
	public int[] stable() {
		// Indicates that woman i is currently engaged to
		// the man v[i].
		int[] current = new int[n1];
		final int NOT_ENGAGED = -1;
		for (int i = 0; i < current.length; i++)
			current[i] = NOT_ENGAGED;

		// List of men that are not currently engaged.
		LinkedList<Integer> freeMen = new LinkedList<Integer>();
		for (int i = 0; i < n; i++)
			freeMen.add(i);

		//computeRanking();
		while (!freeMen.isEmpty()) {
			int m = freeMen.remove();
			int w = getNextPreferred(m);
			printDebug("m=" + m + " w=" + w);
			
			if (w > -1) {
				if (current[w] == NOT_ENGAGED) {
					current[w] = m;
				} else {
					int m1 = current[w];
					if (prefers(w, m, m1)) {
						current[w] = m;
						freeMen.add(m1);
					} else {
						freeMen.add(m);
					}
				}	
			}
		}
		return current;	
	}
	
	/**
	 * Returns the most-preferred woman not already proposed to by this man
	 */
	private int getNextPreferred(int m) {
		double[] prefs = manPref[m];
		
		int bestW = -1;
		double bestPref = -1;
		
		for (int i=0; i<prefs.length; i++) {
			if (prefs[i] > bestPref && !proposedTo[m][i]) {
				bestPref = prefs[i];
				bestW = i;
			}
		}
		
		if (bestW > -1)
			proposedTo[m][bestW] = true;
		
		return bestW;
	}
	
	/**
	 * Returns true iff w prefers x to y.
	 */
	private boolean prefers(int w, int x, int y) {
		return womanPref[w][x] > womanPref[w][y];
	}

	public void printPrefTables() {
		System.out.println("manPref:");
		printMatrix(manPref);
		System.out.println("womanPref:");
		printMatrix(womanPref);
	}

	private void printMarriage(int[] marraiges) {
		System.out.println("Married couples (man + woman): ");
		for (int i = 0; i < marraiges.length; i++)
			System.out.println(marraiges[i] + " + " + i);
	}

	private void printDebug(String s) {
		if (DEBUGGING) {
			System.out.println(s);
		}
	}

	/**
	 * Prints the matrix v.
	 */
	private void printMatrix(double[][] v) {
		if (v == null) {
			System.out.println("<null>");
			return;
		}
		for (int i = 0; i < v.length; i++) {
			for (int j = 0; j < v[i].length; j++)
				System.out.print(v[i][j] + " ");
			System.out.println();
		}
	}
}