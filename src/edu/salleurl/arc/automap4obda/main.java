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

package edu.salleurl.arc.automap4obda;

import java.io.IOException;

public class main {

	public static void main(String[] args) {

		System.out.println("AUTOMAP4OBDA an automated system for R2RML mapping generation @ 2016");
		System.out.println("--------------------------------------------------------------------");
		System.out.println("");
		
		String sOntologyName = "IcaenOntology";		
		String sOutPutFile = "outputOWL.owl";
        String dbURL = "jdbc:mysql://localhost/icaen";
        String driverClass = "com.mysql.jdbc.Driver";
        String username = "root";
        String schema = null;
        String password = "";
        String sInputConfigFile = "";
        String sDomainOntology = "";
        boolean bAttributesAsClasses = true; 
        boolean bOntologyLearning = true; 
        boolean bOLClassTableOrColumn = true;
        boolean bOLClassNameAlone = true; 
        double dParamMaxEntropy = 0.4; 
        double dParamMinEntropy = 0.1; 
        boolean bExtendMappings = true;
        
		if(args.length >= 8) {
	        
			for(int i = 0; i < args.length; i++){
        	
	        	if(args[i].equals("-db"))
				{
	        		if(i+1 < args.length) {
	        			dbURL = args[1 + i++];
	        		}
				} 
	        	else if(args[i].equals("-driver"))
				{
	        		if(i+1 < args.length) {
	        			driverClass = args[1 + i++];
	        		}
				} 
	        	else if(args[i].equals("-schema"))
				{
	        		if(i+1 < args.length) {
	        			schema = args[1 + i++];
	        		}
				} 
				else if(args[i].equals("-u"))
				{
	        		if(i+1 < args.length) {
	        			username = args[1 + i++];
	        		}
				}
				else if(args[i].equals("-p"))
				{
	        		if(i+1 < args.length) {
	        			password = args[1 + i++];
	        		}
				}
				else if(args[i].equals("-n"))
				{
	        		if(i+1 < args.length) {
	        			sOntologyName = args[1 + i++];
	        		}
				}
				else if(args[i].equals("-d"))
				{
	        		if(i+1 < args.length) {
	        			sDomainOntology = args[1 + i++];
	        		}
				}
				else if(args[i].equals("-o"))
				{
	        		if(i+1 < args.length) {
	        			sOutPutFile = args[1 + i++];
	        		}
				}
	        	
				else if(args[i].equals("-attrasclass"))
				{
	        		if(i+1 < args.length) {
	        			bAttributesAsClasses = args[1 + i++].compareTo("1") == 0;
	        		}
				}
				else if(args[i].equals("-ol"))
				{
	        		if(i+1 < args.length) {
	        			bOntologyLearning = args[1 + i++].compareTo("1") == 0;
	        		}
				}
				else if(args[i].equals("-olclasstable"))
				{
	        		if(i+1 < args.length) {
	        			bOLClassTableOrColumn = args[1 + i++].compareTo("1") == 0;
	        		}
				}
				else if(args[i].equals("-olclassnamealone"))
				{
	        		if(i+1 < args.length) {
	        			bOLClassNameAlone = args[1 + i++].compareTo("1") == 0;
	        		}
				}
				else if(args[i].equals("-olmaxentropy"))
				{
	        		if(i+1 < args.length) {
	        			dParamMaxEntropy = Double.parseDouble(args[1 + i++]);
	        		}
				}
				else if(args[i].equals("-olminentropy"))
				{
	        		if(i+1 < args.length) {
	        			dParamMinEntropy = Double.parseDouble(args[1 + i++]);
	        		}
				}
				else if(args[i].equals("-extendedmappings"))
				{
	        		if(i+1 < args.length) {
	        			bExtendMappings = args[1 + i++].compareTo("1") == 0;
	        		}
				}
	        }
			
			System.out.println("Configuration:");
			System.out.println(" - Database URL: " + dbURL);
			System.out.println(" - Dabase driver: " + driverClass);
			System.out.println(" - Database schema: " + schema);
			System.out.println(" - Database username: " + username);
			System.out.println(" - Database password: " + password);
			System.out.println(" - Ontology name: " + sOntologyName);
			System.out.println(" - Domain ontology: " + sDomainOntology);			
			System.out.println(" - Output files: " + sOutPutFile + ".owl and " + sOutPutFile + ".n3");
			System.out.println("");
			System.out.println(" - Attributes as classes approach: " + bAttributesAsClasses);
			System.out.println(" - Ontology learning approach: " + bOntologyLearning);
			System.out.println(" - OL Classes as subclass of table (or column): " + bOLClassTableOrColumn);
			System.out.println(" - OL Class name alone (or concatenated to the table/column name): " + bOLClassNameAlone);
			System.out.println(" - R2RML Extended mappings: " + bExtendMappings);
			System.out.println("");
			
			try {
				System.out.println("---------------------------------------");
				System.out.println("");
				
				System.out.println("Starting to autogenerate...");
				AutoMap4OBDA oDBM = new AutoMap4OBDA(sDomainOntology);
				
				oDBM.setup(bAttributesAsClasses, bOntologyLearning, bOLClassTableOrColumn, 
			    	 bOLClassNameAlone, dParamMaxEntropy, dParamMinEntropy, bExtendMappings);			    		
			    		
				oDBM.Invoke(dbURL, driverClass, schema, username, password, sOntologyName, sOutPutFile);	
								
		
				System.out.println("");
				System.out.println("Generation completed");

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			printToolArgumentDescription();
		}	
	}

	
	public static void printToolArgumentDescription(){
		System.out.println("AUTOMAP4OBDA an automated system for R2RML mapping generation");
		System.out.println("--------------------------------------------------------------------");
		System.out.println("");
		System.out.println("automap4obda.jar -db <databaseURL> -schema <schemaname> -driver <databaseDriver> -u <username> - p \"<password>\" "
				+ "-n <ontologyname> -d <path-to-domainontology> -o <outputfiles> "
				+ "[-attrasclass <0/1>] [-ol <0/1>] [-olclasstable <0/1>] [-olclassnamealone <0/1>] [-extendedmappings <0/1>] ");
		System.out.println("");
		System.out.println("Example:");
		System.out.println("automap4obda.jar -db jdbc:postgresql:postgres -schema sigkdd_structured -driver org.postgresql.Driver -u postgres -p \"postgres\" -n sigkdd_structured_putative "
				+ "-d \"c:\\...\\sigkdd_structured.ttl\" -o sigkdd_structured_putative "
				+ "-attrasclass 1 -ol 1 -olclasstable 1 -olclassnamealone 1  -extendedmappings 1");

		System.out.println("");
		System.out.println("- Password can be empty:");
		System.out.println("- attrasclass: Attributes as classes option (default 1)");
		System.out.println("- ol: Ontology learning technique option (default 1)");
		System.out.println("- olclasstable: New classes in ontology learning technique belongs to the table class (1) or to the column class (0) (default 1)");
		System.out.println("- olclassnamealone: New classes in ontology learning technique is taken as it is in the DB (1) or the name of the table/column is attached (0) (default 1)");
		System.out.println("- extendedmappings: Short path technique option (default 1)");
		System.out.println("--------------------------------------------------------------------");
		System.out.println("");
	}
}
