/*
 *  Copyright (C) 2016 ARC La Salle Barcelona Campus, Ramon Llull University.
 *
 *  for comments please contact Alvaro Sicilia (ascilia@salleurl.edu)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package edu.salleurl.arc.automap4obda.ontologylearning;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.IRI;

public class OntologyLearning {

	Connection mConn = null;
	int iMaxLengthClassName = 50;
	String mSchema = "";
	
	public OntologyLearning(Connection conn, String schema) {
		mConn = conn;
		mSchema = schema;
	}

	public ArrayList<String> CategorizingAttributes (String sTable, String sAttribute) {
		ArrayList<String> arrCategories = new  ArrayList<String>();
		Statement stmt;

		Pattern regex = Pattern.compile("[$&+%,:;=?@#|]");

		try {
	
			stmt = mConn.createStatement();
			
			String query = "SELECT \""+sAttribute+"\", count(*) AS num FROM "+mSchema+".\""+sTable + "\" GROUP BY \"" + sAttribute + "\" ORDER BY num DESC";
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()) {
				String sAttr = rs.getString(sAttribute);
				double dNum = rs.getDouble("num");
			
				if(sAttr != null) {
					
					if( 	
						(sAttr.length() < iMaxLengthClassName) &&	//Only text with less characters than the max class name of the domain ontology
						!sAttr.matches("[-+]?\\d*\\.?\\d+") && 	 	//Is not numeric
						!regex.matcher(sAttr).find() &&				//DO not have special characters.
						!sAttr.toLowerCase().contains("http://") &&	//no url
						!sAttr.contains("--")						//no two - 
						)
					{
		//					System.out.println(sAttr +  " : " + sAttr.toLowerCase().contains("http://"));

						arrCategories.add(sAttr);
					}
				}				
			}

			
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 
		return arrCategories;
	}

	public double getEntropy(String sTable, String sAttribute) {
		Statement stmt;
		double dEntropy = 0;
		
		try {
			stmt = mConn.createStatement();
			
			//Getting number of rows
			ResultSet rs = stmt.executeQuery("SELECT count(*) AS numrows FROM "+sTable);

			rs.next();
			double dNumRows = rs.getDouble("numrows");
			//System.out.println("Num of rows for "+sTable+"."+sAttribute+": " +dNumRows);

			rs.close();
			
			rs = stmt.executeQuery("SELECT "+sAttribute+", count(*) AS num FROM "+sTable + " GROUP BY " + sAttribute);
			
			while (rs.next()) {
				String sAttr = rs.getString(sAttribute);
				double dNum = rs.getDouble("num");

				
				double dP = dNum / dNumRows;
				
				if(sAttr != null) {
					dEntropy += dP * Math.log(dP);
					
					//System.out.println("     "  +sAttr+ ": " + dNum + "P(v): " + dP);
					
					//IRI iri = IRI.create(sAttr);
					
					//System.out.println("IRIs: " + iri.toString());
				}				
			}

			dEntropy *= -1;
			
			//System.out.println("Entropy: " + dEntropy);
			
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return dEntropy;
	}

	public void setMaxLengthClassName(int l) {
		
		iMaxLengthClassName = l +10;
		
	}

	public double getEntropyFromPattern(String sTable, String sAttribute) {

		Statement stmt;
		double dEntropy = 0;
		
		try {
			stmt = mConn.createStatement();
			
			//Getting number of rows
			ResultSet rs = stmt.executeQuery("SELECT count(*) AS numrows FROM "+mSchema+".\""+sTable+"\"");

			rs.next();
			double dNumRows = rs.getDouble("numrows");
			//System.out.println("Num of rows for "+sTable+"."+sAttribute+": " +dNumRows);

			rs.close();
			
			rs = stmt.executeQuery("SELECT \""+sAttribute+"\", count(*) AS num FROM "+mSchema+".\""+sTable + "\" GROUP BY \"" + sAttribute+"\"");
			
			HashMap<String, Double> hmPatterns = new HashMap<String, Double>();
				
			while (rs.next()) {
				String sAttr = rs.getString(sAttribute);
				double dNum = rs.getDouble("num");
				if(sAttr != null) {
					String sPattern = sAttr.replaceAll("\\S", "A");
					
					if(hmPatterns.containsKey(sPattern))
						hmPatterns.put(sPattern, hmPatterns.get(sPattern) + dNum);
					else 
						hmPatterns.put(sPattern, dNum);
					
					//System.out.println("     "  +sAttr+ ": " + sAttr.replaceAll("\\S", "A") + " num: " +dNum);
				}
				//double dP = dNum / dNumRows;
				
				//if(sAttr != null) {
					//dEntropy += dP * Math.log(dP);
					
					//System.out.println("     "  +sAttr+ ": " + dNum + "P(v): " + dP);
					
					//IRI iri = IRI.create(sAttr);
					
					//System.out.println("IRIs: " + iri.toString());
				//}				
			}

			for(String sPat : hmPatterns.keySet()){
				//System.out.println("     "  +sPat+ ": " + hmPatterns.get(sPat));
				double dP = hmPatterns.get(sPat) / dNumRows;
				dEntropy += dP * Math.log(dP);
			}
			
			dEntropy *= -1;
			
			//System.out.println("Entropy: " + dEntropy);
			
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return dEntropy;
	}
}
