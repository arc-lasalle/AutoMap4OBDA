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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.validation.Schema;

import org.cheatham.metric.Metric;
import org.cheatham.metriccomp.MetricSelection;



import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.conf.ParamType;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.conf.StatementType;
import org.jooq.impl.DSL;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;








import com.hp.hpl.jena.sparql.function.library.e;

import edu.salleurl.arc.automap4obda.matcher.AllDirectedPaths;
import edu.salleurl.arc.automap4obda.matcher.ClassMatch;
import edu.salleurl.arc.automap4obda.matcher.ColumnStructure;
import edu.salleurl.arc.automap4obda.matcher.DataPropertyMatch;
import edu.salleurl.arc.automap4obda.matcher.DatabaseStructure;
import edu.salleurl.arc.automap4obda.matcher.RDB2OntMatcher;
import edu.salleurl.arc.automap4obda.matcher.TableStructure;
import edu.salleurl.arc.automap4obda.ontology.OntologyTools;
import edu.salleurl.arc.automap4obda.ontologylearning.OntologyLearning;
import edu.salleurl.arc.automap4obda.r2rml.R2rmlTools;

public class AutoMap4OBDA {

	/////////////////////////////////////
	// Parameters
	//
	// Schema of the database
	String msSchema = "";
	// OWL generation: apply attributes as classes approach
	boolean mbAttributesAsClasses = false;
	// OWL generation: apply ontology learning rules or not
	boolean mbOntologyLearning = false;
	// OWL generation: class from OL are subclass of the table class (true) or column class (false)
	boolean mbOLClassTableOrColumn = true;
	// OWL generation: the name of the class from OL are alone (as the category name is) or with the table/column name concatenated
	boolean mbOLClassNameAlone = true;	
	
	// OWL generation: to filter the attributes of tables to be categorized
	double mdParamMaxEntropy = 0.3;
	// OWL generation: to filter the attributes of tables to be categorized
	double mdParamMinEntropy = 0.8;
	
    // R2RML generation: to connect already mapped classes between them
	boolean mbExtendMappings = false;
	
	
	float mfThresholdClasses = 0.7f;
	float mfThresholdDataProperties = 0.6f;
	//
	/////////////////////////////////////
	
	// MRDB2Onto matcher
	RDB2OntMatcher matcher;
    // Putative ontology
	OntologyTools ontPutative = new OntologyTools();
	// Domain ontology
	OntologyTools ontDomain = new OntologyTools();
    // Domain ontology path
	String msOntologyPath = "";
	// Base uri for the putative ontology
	String baseUri = "http://www.example.net/ontologies/";
	
	public AutoMap4OBDA(String sDomainOntology) {
	
		msOntologyPath = sDomainOntology;
		
        System.out.println("- Loading the target ontology: " + msOntologyPath);

		ontDomain.load(msOntologyPath);
		
		System.out.println("- Enhancing the ontology: Saturating the properties...");
		ontDomain.enhanceOntology();
		
		System.out.println("   # class: "+ontDomain.getClasses().size());
		System.out.println("");
	}
	
    public void setup(boolean bAttributesAsClasses, boolean bOntologyLearning, boolean bOLClassTableOrColumn, 
    		boolean bOLClassNameAlone, double dParamMaxEntropy, double dParamMinEntropy, boolean bExtendMappings)
	{
    	mbAttributesAsClasses = bAttributesAsClasses;
    	mbOntologyLearning = bOntologyLearning;
    	mbOLClassTableOrColumn = bOLClassTableOrColumn; 
		mbOLClassNameAlone = bOLClassNameAlone; 
		mdParamMaxEntropy = dParamMaxEntropy;
		mdParamMinEntropy = dParamMinEntropy; 
		mbExtendMappings = bExtendMappings;
	}
	
    public void Invoke(String dbURL, String driverClass, String schema, String username, String password, String sOntologyName, String sOutputFile) throws IOException {

		Connection conn = null;

		matcher = new RDB2OntMatcher( baseUri + sOntologyName + ".owl#");
		matcher.extendMappings = mbExtendMappings;
		
		String sTimesLog = "times.txt";
		long start_time = System.nanoTime();
		
		///////////
		//
        try {

            Class.forName(driverClass);

            conn = DriverManager.getConnection(dbURL + "?useServerPrepStmts=false", username, password);
            msSchema = schema;

            //0. Read database structure
            System.out.println("- Extracting database structure...");
            matcher.extractDatabaseStructure(conn, schema);
            
            //1. to generate the putative OWL ontology
			GeneratePutativeOntology(sOntologyName, sOutputFile, matcher);
		
			//2. Apply OL approach to mapped classes
			ApplyOL(conn, sOutputFile + "_ol_automap4obda.owl", matcher);
			
			//3. To invoke the matcher to generate alignments of the final ontology
			System.out.println("3. Invoking matcher...");

			//matcher.runLOGMAP(msOntologyPath, sOutputFile + "_automap4obda.owl", sOutputFile + "alignment_automap4obda.rdf", ontPutative, ontDomain);
			//matcher.runAML(msOntologyPath, sOutputFile + "_ol_automap4obda.owl", sOutputFile + "alignment_automap4obda.rdf", ontPutative, ontDomain);
			matcher.run(ontPutative, ontDomain);
			
			matcher.printMatchesToFile(sOutputFile + "_ol_automap4obda.text");

			//printMatcherResults(matcher, matcher2);
			
			//4. To generate the R2RML according to the alignment.
			GenerateR2RMLmappings(conn, ontDomain, sOutputFile + "_automap4obda.r2rml");
			
			long end_time = System.nanoTime();
			double difference = (end_time - start_time) / 1000000000.0;

			printMatchesToFile(sTimesLog, sOutputFile+"\t" + difference+"\t"+matcher.getDatabaseStructure().getTables().size()+"\t"+ontPutative.getClasses().size()+"\t"+ontDomain.getClasses().size());
			
	    } catch (ClassNotFoundException ex) {
	        System.err.println("Failed to load mysql driver");
	        System.err.println(ex);
	    } catch (SQLException ex) {
	        System.out.println("SQLException: " + ex.getMessage()); 
	        System.out.println("SQLState: " + ex.getSQLState()); 
	        System.out.println("VendorError: " + ex.getErrorCode()); 
	    } finally {
	        if (conn != null) {
	            try {
	                conn.close();
	            } catch (SQLException ex) { /* ignore */ }
	            conn = null;
	        }
	    }
	}


    public void printMatchesToFile(String file, String Text) {
		FileWriter outConfigFile;
		
		try {
			outConfigFile = new FileWriter(file, true);
		
			PrintWriter out = new PrintWriter(outConfigFile);
		
			
			out.println(Text);

			
			out.close();
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
    
	private void printMatcherResults(RDB2OntMatcher m1,
			RDB2OntMatcher m2) {
		
		HashMap<String, String> classMatches = new HashMap<>();
		HashMap<String, String> classDifferences = new HashMap<>();
		HashMap<String, String> dpMatches = new HashMap<>();
		HashMap<String, String> dpDifferences = new HashMap<>();
		
		
		int i = 0;
		for(ClassMatch cl : m1.getMatches().values())
		{
			classMatches.put(cl.putativename+"{"+cl.table+"."+cl.column+"}", (cl.isMatched ? "* ":"- ") + cl.domainname);
			
			for(DataPropertyMatch dp : cl.dataproperties.values()) {
				dpMatches.put(cl.putativename+"->"+dp.putativename+"{"+dp.table+"."+dp.column+"}", (dp.isMatched ? "* ":"- ") + dp.domainname);
			}
		}
		
		for(ClassMatch cl : m2.getMatches().values())
		{
			if(classMatches.containsKey(cl.putativename+"{"+cl.table+"."+cl.column+"}")) {
				String prev = classMatches.get(cl.putativename+"{"+cl.table+"."+cl.column+"}");
				String next = (cl.isMatched ? "* ":"- ") + cl.domainname;
				classMatches.put(cl.putativename+"{"+cl.table+"."+cl.column+"}", prev+"\t"+ next);
				
				if(prev.compareTo(next)!= 0) {
					classDifferences.put(cl.putativename+"{"+cl.table+"."+cl.column+"}", prev+"\t"+ next);
				}
				
				for(DataPropertyMatch dp : cl.dataproperties.values()) {
					prev = dpMatches.get(cl.putativename+"->"+dp.putativename+"{"+dp.table+"."+dp.column+"}");
					next = (dp.isMatched ? "* ":"- ") + dp.domainname;
					dpMatches.put(cl.putativename+"->"+dp.putativename+"{"+dp.table+"."+dp.column+"}", prev+"\t"+ next);
					
					if(prev.compareTo(next)!= 0) {
						dpDifferences.put(cl.putativename+"->"+dp.putativename+"{"+dp.table+"."+dp.column+"}", prev+"\t"+ next);
					}
				}
			}
			
		}
		System.out.println("---------------- classses -----------------");
		System.out.println("ALL");
	
		for(String putative : classMatches.keySet()){
			System.out.println(putative + "\t" + classMatches.get(putative));
		}
		System.out.println("");
		System.out.println("DIFERENCES");
		
		for(String putative : classDifferences.keySet()){
			System.out.println(putative + "\t" + classDifferences.get(putative));
		}
		System.out.println("");
		System.out.println("");
		System.out.println("---------------- classses -----------------");
		System.out.println("ALL");
	
		for(String putative : dpMatches.keySet()){
			System.out.println(putative + "\t" + dpMatches.get(putative));
		}
		System.out.println("");
		System.out.println("DIFERENCES");
		
		for(String putative : dpDifferences.keySet()){
			System.out.println(putative + "\t" + dpDifferences.get(putative));
		}	
	}

	/**
	 * Method for generating a putative ontology from a databasestructure
	 * @param sOntologyName Ontology name
	 * @param sOutputFile Path to the output file of the putative ontology
	 */
	private void GeneratePutativeOntology(String sOntologyName,
			String sOutputFile, RDB2OntMatcher mat) {
			
		String basePrefix = baseUri + sOntologyName + ".owl#";	
    	
		System.out.println("1: Creating putative ontology: "+basePrefix);

		ontPutative = new OntologyTools();
		ontPutative.createOntology(basePrefix, basePrefix);

		DatabaseStructure db = mat.getDatabaseStructure();
		
		for(TableStructure table : db.getTables())
		{
			ClassMatch match = new ClassMatch(table.getNormalizedName(), table.name, table.getOnePrimaryKey());
					
			ontPutative.addClass(table.getNormalizedName(), table.name);

			for(ColumnStructure column : table.getColumns())
			{
				// Each column is a datatype
				ontPutative.addDatatype(	
								table.getNormalizedName(), 
								column.getNormalizedName(),  
								column.type);
				
				match.addDataPropertyMatch(new DataPropertyMatch(column.getNormalizedName(), table.name, column.name, column.type));

				// Each foreign key is an object property
				if(column.isForeignkey) {
					ontPutative.addObjectproperty(	
								"has"+column.getNormalizedName(),  
								table.getNormalizedName(),
								column.getNormalizedForeigntable());
					
					// Checking if the columns of the foreign table are a subset of the columns of the table
					if(db.CheckAttributeSubset(table.name, column.foreigntable)) {
						ontPutative.addClass(
								table.getNormalizedName(), 
								column.getNormalizedForeigntable(), 
								table.name);
					}
				}

				//If attributes as classes is enabled, each column is a class
				if(mbAttributesAsClasses){
					ontPutative.addClass(
							 column.getNormalizedName(), 
							 "From_"+table.name+"-"+column.getNormalizedName());
					ontPutative.addObjectproperty(
							 "has"+column.getNormalizedName(), 
							 table.getNormalizedName(), 
							 column.getNormalizedName());
					ontPutative.addDatatype(
							 column.getNormalizedName(), 
							 column.getNormalizedName(), 
							 column.type);
					
					ClassMatch matchAttr = new ClassMatch(column.getNormalizedName(), table.name, column.name);

					matchAttr.isAttributesAsClasses = true;
					
					matchAttr.addDataPropertyMatch(new DataPropertyMatch(column.getNormalizedName(), table.name, column.name, column.type));
					// We add the match to the matcher's lists
					mat.addMatch(matchAttr);
				}
			}
			
			// We add the match to the matcher's lists
			mat.addMatch(match);
		}
		
		ontPutative.saveOntology(sOutputFile + "_automap4obda.owl");
		
		System.out.println("   # class: "+ontPutative.getClasses().size());
		System.out.println("");
	}

	/**
	 * @param conn
	 * @param sOutputFile
	 * @throws IOException
	 */
	private void ApplyOL(Connection conn, String sOutputFile, RDB2OntMatcher mat) throws IOException {

		if(!mbOntologyLearning) return;

		System.out.println("2: Applying Ontology learning technique...");

		int l = ontDomain.maxClassNameLength();
		int iMaxCategories = ontDomain.calculateMaxSubClasses();
		
		double dOntEntropy = ontDomain.calculateEntropy();
		
		dOntEntropy = 8;//////// TO FIX IT
		
		OntologyLearning ol = new OntologyLearning(conn, msSchema);
		ol.setMaxLengthClassName(l);
			
		DatabaseStructure db = mat.getDatabaseStructure();
		
	    for(TableStructure table : db.getTables()) {
        
			Hashtable<String, Double> columnsEntropy = new Hashtable<String, Double>();
			
			for(ColumnStructure col : table.getColumns())
			{		   
				double ep = ol.getEntropyFromPattern(table.name, col.name);
				
				ArrayList<String> listCat = ol.CategorizingAttributes(table.name, col.name);
				   
				if(ep > dOntEntropy) {
					continue;
				}
				   
				if(listCat.size() > iMaxCategories) {
				   continue;
				}
				
				for(String cat: listCat)
				{
					cat = cat.replace(" ", "_");
					String sOLSubClass = mbOLClassTableOrColumn ? table.getNormalizedName() : col.getNormalizedName();
					String sOLclass = mbOLClassNameAlone  ? cat : cat +"_"+ sOLSubClass;
					
					if(!ontPutative.isAnOntologyLearningClass(sOLclass)) {
					
						ontPutative.addClass(sOLclass, sOLSubClass, "<Ontology learning>: "+table.name+"."+col.name+" = \'"+cat+"\'");
						
						ClassMatch cm = new ClassMatch(sOLclass, table.name, matcher.getDatabaseStructure().getTable(table.name).getOnePrimaryKey());
						 
						cm.isOL = true;
						cm.where = "\\\""+table.name+"\\\".\\\""+col.name + "\\\" = \\\'"+cat+"\\\'";
						
						mat.addMatch(cm);
					}
				}
			}
		}
			        
	    ontPutative.saveOntology(sOutputFile);

	    System.out.println("   # class: "+ontPutative.getClasses().size());
		System.out.println("");
	}
    
	/**
	 * @param conn 
	 * @param sR2RMLFile path to th output file of the r2rml file
	 */
	private void GenerateR2RMLmappings(Connection conn, OntologyTools ont, String sR2RMLFile) {

		System.out.println("4. Generating R2RML mappings...");
	
		FileWriter outConfigFile;
		int nMappings = 0;
		
		try {
			outConfigFile = new FileWriter(sR2RMLFile);
		
			PrintWriter outConfig = new PrintWriter(outConfigFile);
			
			R2rmlTools r2rml = new R2rmlTools(ont, matcher, msSchema); //mOntology, hmClassesTable, hmClassesColumn, hmClassesMapping, hmTablesMapping, hmClassesWhere, bbddTables);
			
			r2rml.WriteR2RMLHeader(outConfig);
			
			for(ClassMatch cl : matcher.getMatches().values())
			{
				if(!cl.isMatched) continue;
				
				////////////////////////////
				//check boolean columns
				
				if(matcher.getDatabaseStructure().getTable(cl.table).getColumn(cl.column).type.compareTo("boolean") == 0)
				{
					cl.where = "\\\""+cl.table+"\\\".\\\""+cl.column + "\\\" = true";
					//since it is a boolean column, we will use the primary key of the table to generate the triples
					cl.column = matcher.getDatabaseStructure().getTable(cl.table).getOnePrimaryKey();
				}
				//
				///////
				
				//writing the main mappings for the match
				r2rml.WriteR2RMLTripleMap(outConfig, cl, null, "", conn); nMappings++;
				
				//writing the one mappings for object property of the match
				for(String objectproperty : matcher.getObjectProperties(cl.domainname).keySet()){
					HashMap<String, Integer> objs = new HashMap<String, Integer>();
					objs.put(objectproperty, 1);
					
					String [] splitted = objectproperty.split(">>>");
					
					if(splitted.length == 2 ) 
					{
						String range = splitted[1];
						
						if(matcher.getClassesMatch().containsKey(range)) {
							for(ClassMatch cOld : matcher.getClassesMatch().get(range)) {
								if(cOld.isMatched) {
									//Write triple map
									
									r2rml.WriteR2RMLTripleMap(outConfig, cl, cOld, splitted[0], conn); nMappings++;
								}
							}
						}
					}
 					
				}
			}
		    //
			////////////////////////////////////////////////////////////////////
			
			outConfig.close();
			
			System.out.println("   # mappings: " + nMappings);

			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	 
}
