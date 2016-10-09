package edu.salleurl.arc.automap4obda.tests;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;

public class runAllTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		//2016.04.02
		// modified data properties match system. 
		// Removed core concept use. 
		// Removed StopWords utilisation (they were removed but not know)
		// 
		//2106.04.06
		// OL AAC classes are matched with a more restrictive threshold
		// OL classes are generated with a "_". 
		// Next tests olclassnamealone = 0;
		//
		//2016.04.07 
		// to make the OL classes linked to a primary key of the table and not their column.
		//
		//2016.04.08
		// added a new metric for matching DP when the others fail. howManyTargetInDomain
		//

		String path = "..\\RODI\\data\\";
		
		String [] testsName = new String[] {
				/*
				"conference_structured", 
				"conference_renamed" /*, 
				
				"conference_nofks", 
				"conference_mixed", 
				"conference2cmt", 
				"conference2sigkdd", 

				"sigkdd_mixed" , 
				"sigkdd_renamed",
				"sigkdd_structured", 
				"sigkdd2cmt", 
				"sigkdd2conference", 

				"cmt_mixed", 
				"cmt_renamed", 
				"cmt_structured", 
				"cmt_denormalized", 
				"cmt2conference", 
				"cmt2sigkdd", 
				
				"mondial_rel", 
				"npd_atomic_tests",*/ 
				"npd_user_tests"
				};
		
		

		for(String test : testsName) {
			
			for(int i = 0; i < 10; i++) {
			// DONE all parameters on 
				runTest(path, test, 1, 1, 1, 0, 1);	
			}
			// DONE olClassTable = 0
			//runTest(path, test, 1, 1, 0, 1, 1);
			
			// DONE olClassNameAlone = 0
			//runTest(path, test, 1, 1, 1, 0, 1);

			// DONE olClassTable = 0
			// DONE olClassNameAlone = 0
			//runTest(path, test, 1, 1, 0, 0, 1);
			
			// DONE oOL = 0
			//runTest(path, test, 1, 0, 1, 1, 1);

			// DONE oOL = 0, oAttrAsClass = 0
			//runTest(path, test, 1, 1, 0, 0, 1);
			

		}

			
	
	}

	private static void runTest(String path, String testname, int attrasclass, int ol, int olclasstable, int olclassnamealone, int extendedmapping) {

		String [] arguments = new String[26];
		
		arguments[0] = "-db";
		arguments[1] = "jdbc:postgresql:postgres";
		arguments[2] = "-driver";
		arguments[3] = "com.mysql.jdbc.Driver";
		arguments[4] = "-u";
		arguments[5] = "postgres";
		arguments[6] = "-p";
		arguments[7] = "postgres";
		arguments[8] = "-schema";
		arguments[9] = testname;
		arguments[10] = "-n";
		arguments[11] = testname;
		arguments[12] = "-d";
		arguments[13] = path+testname+"\\ontology.ttl";
		arguments[14] = "-o";
		arguments[15] = "test/"+testname+"_putative";
		arguments[16] = "-attrasclass";
		arguments[17] = ""+attrasclass;
		arguments[18] = "-ol";
		arguments[19] = ""+ol;
		arguments[20] = "-olclasstable";
		arguments[21] = ""+olclasstable;
		arguments[22] = "-olclassnamealone";
		arguments[23] = ""+olclassnamealone;
		arguments[24] = "-extendedmapping";
		arguments[25] = ""+extendedmapping;
		
		edu.salleurl.arc.automap4obda.main m = new edu.salleurl.arc.automap4obda.main();

		m.main(arguments);
		
	}

}
