package edu.salleurl.arc.automap4obda.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class Readrodiresults {
	
	static String rodireportspath = "..\\RODIox\\reports\\";
			
	public static void main(String[] args) {

		
		File folder = new File(rodireportspath);

		List<String> files = new ArrayList<String>();
		HashMap<String, Double> reportAll = new HashMap<String, Double>();
		HashMap<String, Double> reportPrecision = new HashMap<String, Double>();
		HashMap<String, Double> reportRecall = new HashMap<String, Double>();
		HashMap<String, Double> reportClass = new HashMap<String, Double>();
		HashMap<String, Double> reportAttr = new HashMap<String, Double>();
		HashMap<String, Double> reportLink = new HashMap<String, Double>();
		
		for (final File fileEntry : folder.listFiles()) {
	        
			
			if(/*fileEntry.getName().startsWith("tabular_") && */
					fileEntry.getName().endsWith("_reasoning_structural.txt")){
			
				files.add(fileEntry.getName());
			}
	    }
		
		Collections.sort(files);
		
		FileWriter outConfigFile;
		
		try {
			java.util.Date date = new java.util.Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
			System.out.println(sdf.format(date));
			
			outConfigFile = new FileWriter(rodireportspath+sdf.format(date)+"_automap_report.txt");
			PrintWriter out = new PrintWriter(outConfigFile);
			out.println("Scenario	Fmeasure	Precision	Recall	class	attrib	link");
			
			for(String fileEntry : files) {
				String name = fileEntry.replace("tabular_", "").replace("_reasoning_structural.txt","");
							
				try {
					String content = readFile(rodireportspath+fileEntry, Charset.defaultCharset());
					
					reportAll.put(name,  searchFor("[ Evaluation report 'All (AVG)': score = ", content));
					reportPrecision.put(name,  searchForPrecision("[ Evaluation report 'All (AVG)': score = ", content));
					reportRecall.put(name,  searchForRecall("[ Evaluation report 'All (AVG)': score = ", content));
					
					reportClass.put(name,  searchFor("[ Evaluation report 'class (AVG)': score = ", content));
					reportAttr.put(name,  searchFor("[ Evaluation report 'attrib (AVG)': score = ", content));
					reportLink.put(name,  searchFor("[ Evaluation report 'link (AVG)': score = ", content));
					
					out.println(name+"	"+reportAll.get(name).toString().replace(".", ",") +"	"+
							reportPrecision.get(name).toString().replace(".", ",") +"	"+
							reportRecall.get(name).toString().replace(".", ",") +"	"+
							reportClass.get(name).toString().replace(".", ",") + "	"+
							reportAttr.get(name).toString().replace(".", ",") + "	"+
							reportLink.get(name).toString().replace(".", ",")
							
							);	
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		
			
			out.close();
		
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
	}
	
	private static double searchFor(String str, String content) {

		int inx = content.indexOf(str);
		
		if(inx < 0) return -1;
		
		int inx2= content.indexOf(";", inx);
		
		String val = content.substring(inx+str.length(), inx2);
		
		return Double.parseDouble(val);
	}

	private static double searchForPrecision(String str, String content) {

		int inx = content.indexOf(str);
		
		if(inx < 0) return -1;
		
		inx= content.indexOf("; precision = ", inx) + "; precision = ".length();
		int inx2= content.indexOf(", ", inx);
		
		String val = content.substring(inx, inx2);
		
		return Double.parseDouble(val);
	}

	private static double searchForRecall(String str, String content) {

		int inx = content.indexOf(str);
		
		if(inx < 0) return -1;
		
		inx= content.indexOf(", recall = ", inx) + ", recall = ".length();
		int inx2= content.indexOf("]", inx);
		
		String val = content.substring(inx, inx2);
		
		return Double.parseDouble(val);
	}

	static String readFile(String path, Charset encoding) 
			  throws IOException 
	{
	  byte[] encoded = Files.readAllBytes(Paths.get(path));
	  return new String(encoded, encoding);
	}

}
