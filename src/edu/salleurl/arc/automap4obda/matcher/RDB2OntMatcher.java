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

package edu.salleurl.arc.automap4obda.matcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cheatham.metric.Metric;
import org.cheatham.metric.PartOfSpeech;
import org.cheatham.metric.SoftTFIDFMetric;
import org.cheatham.metric.SoftTFIDFMetricProp;
import org.cheatham.metric.TFIDFMetricProp;
import org.cheatham.metriccomp.MetricSelection;
import org.cheatham.metriccomp.Settings;
import org.cheatham.preprocessing.Preprocessing;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.HermiT.datatypes.datetime.DateTime;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.salleurl.arc.automap4obda.ontology.OntologyTools;
import uk.ac.ox.krr.logmap2.LogMap2Core;
import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;


public class RDB2OntMatcher {
	
	private int numMatches = 0;
	private HashMap<String, ClassMatch> matches;
	
	private HashMap<String, List<ClassMatch>> domainmatches;
	
    Hashtable<String, HashMap<String, Integer>> htObjectProperties;

	private DatabaseStructure db;
	
	private String basePrefix = "";
	
	float mfThresholdClasses = 0.6f;
	float mfThresholdDataProperties = 0.6f;
	public boolean extendMappings = true;

	Metric mp,mr, mdp;
	OntologyTools putative;
	OntologyTools domain;
	
	//For properties
	private static HashMap<String, String> labelMap = new HashMap<>();
	private static HashMap<String, String> domainMap = new HashMap<>();
	private static HashMap<String, String> rangeMap = new HashMap<>();
	private HashMap<OWLEntity, OWLEntity> matched = new HashMap<>();
	private HashMap<OWLEntity, Double> matchedConf = new HashMap<>();
	TFIDFMetricProp propMetric;
	TFIDFMetricProp generalMetric;
	SoftTFIDFMetricProp softMetric;
	//
	private static HashMap<String, String> propMap = new HashMap<>();
	private static HashMap<String, String> classMap = new HashMap<>();
	
	
	public RDB2OntMatcher(String sbasePrefix)
	{
		basePrefix = sbasePrefix;
		
		db = new DatabaseStructure();
		
		matches = new HashMap<String, ClassMatch>();
		domainmatches = new HashMap<String, List<ClassMatch>>();
		
		htObjectProperties = new Hashtable<String, HashMap<String, Integer>> ();
	}
	
	
	/**
	 * @return Database structure
	 */
	public DatabaseStructure getDatabaseStructure()
	{
		return db;
	}

	public HashMap<String, Integer> getObjectProperties(String domainclass) {
		if(htObjectProperties.containsKey(domainclass))
			return htObjectProperties.get(domainclass);
		
		return new HashMap<String, Integer>();
	}
	/**
	 * @return list of match of classes
	 */
	public HashMap<String, ClassMatch> getMatches()
	{
		return matches;
	}

	/**
	 * @return list of match of classes
	 */
	public HashMap<String, List<ClassMatch>> getClassesMatch()
	{
		return domainmatches;
	}
	/**
	 * Method to extract database structure: tables, columns...
	 * @param conn Connection to read database strucure
	 * @param schema schema where the databases is stored in. This is particularly important in Postgres and Oracle. 
	 */
	public void extractDatabaseStructure(Connection conn, String schema) {
		db.extractDatabaseStructure(conn, schema);
		
		//db.print();
	}

	/**
	 * 
	 * @param match match to a class and their data properties
	 */
	public void addMatch(ClassMatch match) {
		if(!matches.containsKey(match.table+"."+match.column+"."+match.putativename)) {
			matches.put(match.table+"."+match.column+"."+match.putativename, match);
			
			numMatches++;
		}
	}
	
	
	private void removeMatch(ClassMatch match) {

		if(matches.containsKey(match.table+"."+match.column+"."+match.putativename)){
		
			if(domainmatches.containsKey(match.domainname)) {
				domainmatches.get(match.domainname).remove(match);
			}
			//remove the match
			matches.remove(match.table+"."+match.column+"."+match.putativename);
			
			numMatches--;
		}		
	}

	//
	// java -jar logmap2_standalone.jar MATCHER file:C://eclipse//workspace//doc//AutoMap4OBDA//test//sigkdd2cmt_putative_ol_automap4obda.owl file:C://Users//alvaro//Desktop//DOC//RODI//data//cmt_structured//ontology.ttll /C://eclipse//workspace//doc//AutoMap4OBDA//test// true
	public void runLOGMAP(String sOntologyPath, String sOutputFile, String sOutAlignments, OntologyTools ontPutative, OntologyTools ontDomain)
	{
		LogMap2_Matcher logmap2 = new LogMap2_Matcher(ontPutative.getOWLOntology(), ontDomain.getOWLOntology());
		
		//Set of mappings computed my LogMap
		Set<MappingObjectStr> logmap2_mappings = logmap2.getLogmap2_Mappings();
		
		System.out.println("Number of mappings computed by LogMap: " + logmap2_mappings.size());

		for (MappingObjectStr mapping: logmap2_mappings){
			System.out.println("Mapping: ");
			System.out.println("\t"+ mapping.getIRIStrEnt1());
			System.out.println("\t"+ mapping.getIRIStrEnt2());
			System.out.println("\t"+ mapping.getConfidence());
			
			//MappingObjectStr.EQ or MappingObjectStr.SUB or MappingObjectStr.SUP
			System.out.println("\t"+ mapping.getMappingDirection()); //Utilities.EQ;
			
			//MappingObjectStr.CLASSES or MappingObjectStr.OBJECTPROPERTIES or MappingObjectStr.DATAPROPERTIES or MappingObjectStr.INSTANCES
			System.out.println("\t"+ mapping.getTypeOfMapping());
			
			for(ClassMatch cl : matches.values()) {
            	
            	
            	if(mapping.getTypeOfMapping() == MappingObjectStr.CLASSES){
            	//classs
            	
            		if(mapping.getIRIStrEnt1().endsWith("#"+cl.putativename)) {
            			cl.isMatched = true;
            			cl.confindence = mapping.getConfidence();
            			cl.domainname = mapping.getIRIStrEnt2();
            			
                    	if(!domainmatches.containsKey(cl.domainname))
        					domainmatches.put(cl.domainname, new ArrayList<ClassMatch>());
        				
        				domainmatches.get(cl.domainname).add(cl);
            		}
            	}
            	//properties
            	else if(mapping.getTypeOfMapping() == MappingObjectStr.DATAPROPERTIES){
            		            		
    				for(DataPropertyMatch dp : cl.dataproperties.values()) {
    					if(mapping.getIRIStrEnt1().endsWith("#"+dp.putativename)) {
    						dp.isMatched = true;
    						dp.domainname = mapping.getIRIStrEnt2();
	                	}	
    				}
        		} else {
        			System.out.println(" ### non class or dp " + mapping.toString() + " ###");

        		}
            	

            }			
		}
				
		printMatches();
			
		System.out.println("--- extending mappings ---");
		
		//extendMappings(ontPutative, false);
		//extendMappings(ontDomain, true);
		
		printMatches();
		
		//repairMappings(ontPutative);
		//repairMappings(ontDomain);
		
		printMatches();
		System.out.println("--- end ---");
	}
	
	
	//
	//
	//
	public void runAML(String sOntologyPath, String sOutputFile, String sOutAlignments, OntologyTools ontPutative, OntologyTools ontDomain)
	{
		
		//String currentDir = System.getProperty("user.dir");
		String currentDir = "C:\\eclipse\\workspace\\doc";
		
		
		
		ProcessBuilder pb = new ProcessBuilder(
				"java", 
				"-Xms512M",
				"-Xmx1024M",
				"-jar", 
				currentDir + "\\aml\\AML.jar",
				"-m", 
				"-s",
				"\""+currentDir+"\\AutoMap4OBDA\\"+sOutputFile + "\"",
				"-t",
				"\""+sOntologyPath+"\"",
				"-o",
				"\""+currentDir+"\\AutoMap4OBDA\\"+sOutAlignments+"\""
				);
		
        pb.directory(new File(currentDir + "\\aml"));
        pb.redirectError(new File(currentDir + "\\error-aling.txt"));
        
        System.out.println("Pb: ");
        
        List<String> res = pb.command();
        Iterator<String> ite = res.iterator();
        
        while (ite.hasNext()) {
        	System.out.println("Pb: " + ite.next());
        }
        
        try {
            Process p = pb.start();
            
            LogStreamReader lsr = new LogStreamReader(p.getInputStream());
            Thread thread = new Thread(lsr, "LogStreamReader");
            thread.start();
            
            p.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        
        
        /////////////////////7
        // parsing the mappings
        
        File alignmentsFile = new File( currentDir+"\\AutoMap4OBDA\\"+sOutAlignments);

		System.out.println("R2RML generation...");
	
		////////////////////////////////////////////////////////////////////
		// OWL generation
		
		try {
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			try {
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(alignmentsFile);
				
				NodeList nList = doc.getElementsByTagName("Cell");
	
		         for (int temp = 0; temp < nList.getLength(); temp++) {
		            Node nNode = nList.item(temp);
	
		            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		                Element eElement = (Element) nNode;
		                
		                Element eEntity1 = (Element)eElement.getElementsByTagName("entity1").item(0);
		                Element eEntity2 = (Element)eElement.getElementsByTagName("entity2").item(0);
		                Element eType = (Element)eElement.getElementsByTagName("type").item(0);
		               	String source = ontPutative.getClassFragment(eEntity1.getAttribute("rdf:resource"));
		   		                
		                for(ClassMatch cl : matches.values()) {
		                	OWLClass oc = ontDomain.getOWLClass(eEntity2.getAttribute("rdf:resource"));
		                	
		                	if(ontDomain.getOWLClass(eEntity2.getAttribute("rdf:resource")) != null){
		                	//classs
		                	
		                		if(cl.putativename.compareTo(source)==0) {
		                			cl.isMatched = true;
		                			cl.confindence = 1.0;
		                			cl.domainname = eEntity2.getAttribute("rdf:resource");
		                		}
		                	}
		                	//properties
		                	else {
		                		if(ontDomain.getOWLDataProperty(eEntity2.getAttribute("rdf:resource")) != null){
		                		
			        				for(DataPropertyMatch dp : cl.dataproperties.values()) {
				    					if(dp.putativename.compareTo(source)==0) {
				    						dp.isMatched = true;
				    						dp.domainname = eEntity2.getAttribute("rdf:resource");
					                	}	
				    				}
		                		}
		                	}
		                }
		            }
		         }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("");
		
		printMatches();
		
		
		
		System.out.println("--- extending mappings ---");
		
		extendMappings(ontDomain, true);
		
		printMatches();
		
		repairMappings(ontDomain);
		
		printMatches();
		
		System.out.println("--- end ---");
	}

	/**
	 * Run the matcher 
	 * @param match
	 */
	public void run(OntologyTools ontPutative, OntologyTools ontDomain) {
		putative = ontPutative;
		domain = ontDomain;
		
		Settings.tokenization = true;
		Settings.stopWords = true;
		Settings.stemming = true;
		Settings.normalization = true;
		Settings.synonymsAll = false;
		Settings.synonymsBest = false;

		Settings.classes = true;
		Settings.properties = true;
		
		mp = MetricSelection.chooseMetric(putative.getOWLOntology(), domain.getOWLOntology(), "precision", false);
		mr = MetricSelection.chooseMetric(putative.getOWLOntology(), domain.getOWLOntology(), "recall", false);
		mdp = new SoftTFIDFMetric();
		
		mp.init(putative.getOWLOntology(), domain.getOWLOntology());
		mr.init(putative.getOWLOntology(), domain.getOWLOntology());
		mdp.init(putative.getOWLOntology(), domain.getOWLOntology());		

		initPropertiesMatcher(ontPutative, ontDomain);

		
		Set<OWLClass> domainClasses = ontDomain.getClasses();
		// EYE! This list of properties can be filtered by only those whose domain is owl:thing or the domain class.
		Set<OWLDataProperty> domainDataproperties = ontDomain.getDataProperties();
		

		for(ClassMatch cl : matches.values()) {
			
			matchClass(cl, domainClasses);
			
			if(cl.isMatched) {
				for(DataPropertyMatch dp : cl.dataproperties.values()) {
					matchDataProperty(dp, domainDataproperties);
				}
				
				if(!domainmatches.containsKey(cl.domainname))
					domainmatches.put(cl.domainname, new ArrayList<ClassMatch>());
				
				domainmatches.get(cl.domainname).add(cl);
			}
		}
		
		System.out.println("   # matches: "+numMatches);
		
		//printMatches();
		
		System.out.println("3.1 Extending mappings...");
		
		extendMappings(ontDomain, true);
		
		System.out.println("   # matches: "+numMatches);

		//printMatches();
		
		System.out.println("3.2. Repairing mappings...");

		repairMappings(ontDomain);

		System.out.println("   # matches: "+numMatches);
		System.out.println("");

		//printMatches();
	}




	/**
	 * @param cl class to be matched
	 * @param domainClasses list of domain classes
	 */
	private void matchClass(ClassMatch cl, Set<OWLClass> domainClasses) {
			
		HashMap<String, Double> cmatches = new HashMap<String, Double>();

		for (OWLClass cls : domainClasses)
		{
			if(!cls.isOWLThing() ){
				String a = "";
				String b = "";
				
				if(classMap.containsKey(cl.putativename)) 			
					a = classMap.get(cl.putativename);
				else {
					a = Preprocessing.preprocess(putative.getOWLClass(basePrefix+cl.putativename), putative.getOWLOntology());
					classMap.put(cl.putativename, a);
				}
				
				if(classMap.containsKey(cls.getIRI().getFragment())) 			
					b = classMap.get(cls.getIRI().getFragment());
				else {
					b = Preprocessing.preprocess(cls, domain.getOWLOntology());
					classMap.put(cls.getIRI().getFragment(), b);
				}
		
				double valP = mp.compute(a, b, false);
				double valR = mr.compute(a, b, false);
				
				/////////////////////////
				// 20160406: try to restrict the mathcing thresholds of OL matchs
				boolean ok = false;
				
				if(cl.isOL || cl.isAttributesAsClasses) {
					if (Math.min(valP, valR) >= mfThresholdClasses && 
						Math.max(valP, valR) >= mfThresholdClasses) {
						ok = true;
					}
				} else 	if(valP >= mp.getThreshold1() || valR >= mr.getThreshold1()){
					ok = true;
				}
				
				if(ok){

					double rp = mp.compute(cl.putativename, cls.getIRI().getFragment(), false);
					double rr = mr.compute(cl.putativename, cls.getIRI().getFragment(), false);

					double r = (rp+rr) / 2;
					
					cmatches.put(cls.getIRI().toString(), r);
				}
			}
		}
		
		/////////////////////////////////
		// we select the class with the highest confidence
		
		double nMax = 0;
		String sMatch = "";
		
		for (String cls : cmatches.keySet())
		{
			if(cmatches.get(cls) > nMax) {
				sMatch = cls;
				nMax = cmatches.get(cls);
			}
		}
		
		if(nMax > 0) {
			//We found the match
			
			cl.isMatched = true;
			cl.domainname = sMatch;
			cl.confindence = nMax;
		}
	}
	

	/**
	 * @param dp data property to be matched
	 * @param domainDataproperties list of domain data properties
	 */
	private void matchDataProperty(DataPropertyMatch dp,
			Set<OWLDataProperty> domainDataproperties) {
		
		// This method still requires a proper metric
		// also, it should check if the type of the property is comparable with the type of the column.

		HashMap<String, Float> dpmatches = new HashMap<String, Float>();
		
		for (OWLDataProperty dps : domainDataproperties)
		{
			if(!dps.isAnonymous() ){

				String a = "";
				String b = "";
				
				if(propMap.containsKey(dp.putativename)) 			
					a = propMap.get(dp.putativename);
				else {
					a = Preprocessing.preprocess(putative.getOWLDataProperty(basePrefix+dp.putativename), putative.getOWLOntology());
					if(a.isEmpty()) a = dp.putativename;
					propMap.put(dp.putativename, a);
				}
					
				if(propMap.containsKey(dps.getIRI().getFragment()))
					b = propMap.get(dps.getIRI().getFragment());
				else {
					b = Preprocessing.preprocess(dps, domain.getOWLOntology());
					if(b.isEmpty()) b = dps.getIRI().getFragment();

					propMap.put(dps.getIRI().getFragment(), b);
				}				
				
				double valP = mdp.compute(a, b, false);
				double valR = mdp.compute(b, a, false);
						
				if (Math.min(valP, valR) >= mfThresholdDataProperties && 
						Math.max(valP, valR) >= mfThresholdDataProperties) {
					
					float confidence = (float) Math.min(valP, valR);
						/*
						 * 
						 * WHy tHIs is NEEDED?
						 * 
						 */
					
					if(!dpmatches.containsKey(dps.getIRI().toString()))
						dpmatches.put(dps.getIRI().toString(), (float) confidence);
					else if(dpmatches.get(dps.getIRI().toString()) < confidence) {
						dpmatches.put(dps.getIRI().toString(), (float) confidence);
					}
				} 
				
				
				OWLDataProperty entityA = putative.getOWLDataProperty(basePrefix+dp.putativename);  
				String stringA;
				if (labelMap.containsKey(dp.putativename)) {
					stringA = labelMap.get(dp.putativename);
				} else {
					stringA = Preprocessing.preprocess(entityA, putative.getOWLOntology());
					//stringA = PartOfSpeech.getCoreConcept(Preprocessing.preprocess(entityA, putative.getOWLOntology()));
					if(stringA.isEmpty()) stringA = dp.putativename;
					labelMap.put(dp.putativename, stringA);
				}
				
				String exactLabelA = stringA; // Preprocessing.preprocess(entityA, putative.getOWLOntology());
				
				String domainA = getDomainString(entityA, putative.getOWLOntology());
				String rangeA = getRangeString(entityA, putative.getOWLOntology());
				
				OWLDataProperty entityB = dps;

				String stringB;
				if (labelMap.containsKey(dps.getIRI().getFragment())) {
					stringB = labelMap.get(dps.getIRI().getFragment());
				} else {
					stringB = Preprocessing.preprocess(entityB, domain.getOWLOntology());
					if(stringB.isEmpty()) stringB = dps.getIRI().getFragment();

					labelMap.put(dps.getIRI().getFragment(), stringB);
				}
				
				String exactLabelB = stringB;//Preprocessing.preprocess(entityB, domain.getOWLOntology());
				
				String domainB = getDomainString(entityB, domain.getOWLOntology());
				String rangeB = getRangeString(entityB, domain.getOWLOntology());

				double labelConfA = softMetric.compute(stringA, stringB);
				double labelConfB = softMetric.compute(stringB, stringA);

				double domainConfA = generalMetric.compute(domainA, domainB);
				double domainConfB = generalMetric.compute(domainB, domainA);

				double rangeConfA = generalMetric.compute(rangeA, rangeB);
				double rangeConfB = generalMetric.compute(rangeB, rangeA);

				if (domainA.equals("none") && domainB.equals("none")) {
					domainConfA = 0.0;
					domainConfB = 0.0;
				}
				
				if (rangeA.equals("none") && rangeB.equals("none")) {
					rangeConfA = 0.0;
					rangeConfB = 0.0;
				}
				
				double labelConfidence = (labelConfA + labelConfB) / 2.0;
				double domainConfidence = (domainConfA + domainConfB) / 2.0;
				double rangeConfidence = (rangeConfA + rangeConfB) / 2.0;
				
				double exactConfA = softMetric.compute(exactLabelA, exactLabelB);
				double exactConfB = softMetric.compute(exactLabelB, exactLabelA);
				double exactConfidence = (exactConfA + exactConfB) / 2.0;
				
								
				if (labelConfidence >= mfThresholdDataProperties && 
					rangeConfidence >= mfThresholdDataProperties) {
					
					double confidence = (exactConfidence + domainConfidence + 
							rangeConfidence) / 3.0;
				
					boolean replace = true;
					
					if (matchedConf.containsKey(entityA) && 
							matchedConf.get(entityA) > confidence) {
						replace = false;
					}

					if (matchedConf.containsKey(entityB) && 
							matchedConf.get(entityB) > confidence) {
						replace = false;
					}

					if (replace) {
						matched.put(entityA, entityB);
						matched.put(entityB, entityA);	
						matchedConf.put(entityA, confidence);
						matchedConf.put(entityB, confidence);
					}
									
					if(!dpmatches.containsKey(dps.getIRI().toString()))
						dpmatches.put(dps.getIRI().toString(), (float) confidence);
					else if(dpmatches.get(dps.getIRI().toString()) < confidence) {
						dpmatches.put(dps.getIRI().toString(), (float) confidence);
					}
				}
				
				////////////////
				// if there are not matches then
				if (dpmatches.keySet().size() == 0) {
					
					float n = howManyTargetInDomain(dp.putativename, b);
					
					if(n >= 0.6) {
						if(!dpmatches.containsKey(dps.getIRI().toString()))
							dpmatches.put(dps.getIRI().toString(), (float) n);
						else if(dpmatches.get(dps.getIRI().toString()) < n) {
							dpmatches.put(dps.getIRI().toString(), (float) n);
						}
					}
				}
			}
		}
		
		float nMax = 0;
		String sMatch = "";
		// we select the class with the most matches
		for (String cls : dpmatches.keySet())
		{
			if(dpmatches.get(cls) > nMax) {
				sMatch = cls;
				nMax = dpmatches.get(cls);
			}
		}
		
		if(nMax > 0) {
			//We found the match
			dp.isMatched = true;
			dp.domainname = sMatch;
		}
	}



	private float howManyTargetInDomain(String a, String b) {

		float ret = 0;
				
		a = a.toLowerCase();
		
		String[] tokens = b.split(" ");

		if(tokens.length == 0) return 0.0f;
		
		int lengthNotFound = 0;
		
		for(String t : tokens){
			if(a.contains(t)) {
				ret += 1;
				//b = b.replace(t, "");
				a = a.replace(t, " "+t+" ");
			} else {
				lengthNotFound += t.length();
			}
			
			
		}
		
		a = a.replace("  ", " ");
		ret /= tokens.length;

		double r = mdp.compute(a, b, false);
		
		return (float)r;
	}


	private void printMatches() {
		System.out.println("");
		System.out.println("--- Printing matches ---");
		System.out.println("");
		int iCl = 0;
		int iDp = 0;
		
		for(ClassMatch cl : matches.values()) {

			if(cl.isMatched) {
				iCl++;
			}
		
			if(cl.putativename.isEmpty())
				System.out.println( " EXTENDED { "+cl.table+"."+cl.column+"} - " + cl.domainname + " | " + cl.confindence);
			else 
				System.out.println((cl.isAttributesAsClasses ? "AC " : "") + (cl.isOL ? "OL " : "")+ cl.putativename + "{ "+cl.table+"."+cl.column+"} - " + cl.domainname + " | " + cl.confindence);
			
			for(DataPropertyMatch dp : cl.dataproperties.values()) {
				
				System.out.println("   " + dp.putativename + "{ "+dp.table+"."+dp.column+"} - " + dp.domainname);
				
				if(dp.isMatched) {
				
					iDp++;
				}
			}
			
		
			System.out.println("");
		//}
		}
		
		System.out.println("");
		System.out.println("Total classes matched: " + iCl);
		System.out.println("Total data proprties matched: " + iDp);
	}


	public void printMatchesToFile(String file) {
		FileWriter outConfigFile;
		
		try {
			outConfigFile = new FileWriter(file);
		
			PrintWriter out = new PrintWriter(outConfigFile);
		
			out.println("--- matcher "+new Date().toString()+" ----------");
		
			out.println("");
			int iCl = 0;
			int iDp = 0;
			
			for(ClassMatch cl : matches.values()) {

				if(cl.isMatched) {
					iCl++;
				}
				if(cl.putativename.isEmpty())
					out.println("CLS: EXTENDED { "+cl.table+"."+cl.column+"} - " + cl.domainname + " | " + cl.confindence);
				else 
					out.println("CLS: "+(cl.isAttributesAsClasses ? "AC " : "") + (cl.isOL ? "OL " : "")+ cl.putativename + "{ "+cl.table+"."+cl.column+"} - " + cl.domainname + " | " + cl.confindence);
				for(DataPropertyMatch dp : cl.dataproperties.values()) {

					out.println("   DP: " + dp.putativename + "{ "+dp.table+"."+dp.column+"} - " + dp.domainname);
					
					if(dp.isMatched) {
					
						iDp++;
					}
				}
			
			}
			
			out.println("");
			out.println("Total classes matched: " + iCl);
			out.println("Total data proprties matched: " + iDp);
			
			out.close();
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	

	/**
	 * This method extends the correspondences according to the domain ontology and the database structure.
	 * 
	 * @param ontDomain domain ontology
	 */
	private void extendMappings(OntologyTools ont, boolean bDomainPutative) {
	
		db.initSearch();
		ont.initSearch();
		
		HashMap<String, String> hmConnectedClasses = new HashMap<String, String>();
		HashMap<String, List<ClassMatch>> hmNewMatches = new HashMap<String, List<ClassMatch>>();
		
		for(ClassMatch source : matches.values())
		{
			if(!source.isMatched) continue;
			
			for(ClassMatch target : matches.values())
			{
				if(!target.isMatched) continue;
				
				long start_time = System.nanoTime();

				List<GraphPath<String, DefaultEdge>> dbpaths = db.findPathBetween(source.table, target.table);
				long end_time = System.nanoTime();
				double difference = (end_time - start_time)/1e6;

				if(dbpaths.size() <= 0) continue;
				
				// If we are here means that there is a path between the tables of the source and the target
				
				int iMaxPathLength = 30;
								
				start_time = System.nanoTime();

				List<GraphPath<String, DefaultEdge>> ontpaths;
				
				if(bDomainPutative)
					ontpaths = ont.findPathBetween(source.domainname, target.domainname, iMaxPathLength);
				else 
					ontpaths = ont.findPathBetween(source.putativename, target.putativename, iMaxPathLength);
				
				end_time = System.nanoTime();
				difference = (end_time - start_time)/1e6;

				if(ontpaths.size() <= 0) continue;
				
				processPaths(source, target, dbpaths, ontpaths, hmConnectedClasses, hmNewMatches);

			}
		}
		
		
		if(extendMappings) {
			///////////////////////////////
			//We add the new matches found!
			for(List<ClassMatch> cllist : hmNewMatches.values()) {
				for(ClassMatch newCl : cllist) {
				
					addMatch(newCl);
				}
			}
		}
	}
	
	private void processPaths(ClassMatch source, ClassMatch target,	
			List<GraphPath<String, DefaultEdge>> dbpaths, List<GraphPath<String, DefaultEdge>> ontpaths, 
			HashMap<String, String> hmConnectedClasses,
			HashMap<String, List<ClassMatch>> hmNewMatches) {

		for(GraphPath<String, DefaultEdge> ontpath : ontpaths){
			
			for(int i = 0; i < ontpath.getEdgeList().size(); i += 2) {
				String sFrom = ontpath.getGraph().getEdgeSource(ontpath.getEdgeList().get(i));
				String sObjectProperty = ontpath.getGraph().getEdgeTarget(ontpath.getEdgeList().get(i));
				String sTo = ontpath.getGraph().getEdgeTarget(ontpath.getEdgeList().get(i+1));

				//We check if we already have processed this object property
				if(hmConnectedClasses.containsKey(sFrom+"."+sObjectProperty+"."+sTo)) continue;
				
				hmConnectedClasses.put(sFrom+"."+sObjectProperty+"."+sTo, "");
								
				//We check if the sTo is already mapped or no, if not the we create the mapping
				if(domainmatches.containsKey(sFrom) && !domainmatches.containsKey(sTo) && !hmNewMatches.containsKey(sTo)) {
					
					for(ClassMatch c : domainmatches.get(sFrom)) {
						ClassMatch cl = new ClassMatch("", c.table, c.column);
						cl.isMatched = true;
						cl.domainname = sTo;

						if(!hmNewMatches.containsKey(cl.domainname))
							hmNewMatches.put(cl.domainname, new ArrayList<ClassMatch>());
						
						hmNewMatches.get(cl.domainname).add(cl);	
					}
				}
				
				//We check if the sTo is already mapped or no, if not the we create the mapping
				if(hmNewMatches.containsKey(sFrom) && !domainmatches.containsKey(sTo) && !hmNewMatches.containsKey(sTo)) {
					for(ClassMatch c : hmNewMatches.get(sFrom)) {
						ClassMatch cl = new ClassMatch("", c.table, c.column);
						cl.isMatched = true;
						cl.domainname = sTo;

						if(!hmNewMatches.containsKey(cl.domainname))
							hmNewMatches.put(cl.domainname, new ArrayList<ClassMatch>());
						
						hmNewMatches.get(cl.domainname).add(cl);
					}
				}
				
				if(!htObjectProperties.containsKey(sFrom))
					htObjectProperties.put(sFrom, new HashMap<String, Integer>());
				
				htObjectProperties.get(sFrom).put(sObjectProperty + ">>>" + sTo, 1);
			}
		}		
	}



	/**
	 * This method iterates over the domain matches and tries to find inconsistencies
	 *  - a match has a column that is not primary key but another match has it.
	 *  
	 * @param ontDomain
	 */
	private void repairMappings(OntologyTools ontDomain) {

		List<ClassMatch> listToRemove = new ArrayList<ClassMatch>();
		
		for(String domainclass : domainmatches.keySet()){
			
			boolean bExistsMatchWithPrimaryKey = false;
			String sFound = "";
			
			//1. Search if the matches of the domain class has a primary key. Usually yes
			for(ClassMatch cl : domainmatches.get(domainclass)){
				
				if(db.getTable(cl.table).getColumn(cl.column).isPrimarykey) {
					bExistsMatchWithPrimaryKey = true;
					sFound = cl.table+"."+cl.column+"."+cl.putativename;
					break;
				}
			}
			
			//The mappings need to have a primary key at least
			if(!bExistsMatchWithPrimaryKey) continue;
				
			//2. Search if the matches of the domain class has not a primary key
			for(ClassMatch cl : domainmatches.get(domainclass)){
				
				if(!db.getTable(cl.table).getColumn(cl.column).isPrimarykey && 
						!db.getTable(cl.table).getColumn(cl.column).isForeignkey) {
					// We found an inconsitency
					listToRemove.add(cl);
				}
				
			}
		}
		
		///////////////////////////////////
		// remove the bad matches 
		for(ClassMatch cl : listToRemove){
			removeMatch(cl);
		}
		
		listToRemove.clear();
		
		/*
		 * commented for further work
		//
		///////////////////////////////////
		
		for(String domainclass : domainmatches.keySet()){

			//3. Check if all the primary keys of the matches of a domain class are connected through foreign keys.
			List<ClassMatch> matches = domainmatches.get(domainclass);
			System.out.println("CHECKING INCONSISTENCY: " + domainclass);

			//this is only valid if there are more than 1 match
			if(matches.size() <= 1) continue;
			
			ClassMatch clFrom = matches.get(0);
			boolean bFromModified = false;
			
			for(int j = 1; j < matches.size(); j++) {
				ClassMatch clTo = matches.get(j);
				
				if(areConsistent(clFrom, clTo, bFromModified)){
				} else {
					System.out.println("   NO COnsISTENT");
					
					// remove the matching that are attributes as classes or OL
					if((clFrom.isAttributesAsClasses || clFrom.isOL) && !db.getTable(clFrom.table).getColumn(clFrom.column).isForeignkey)
						listToRemove.add(clFrom);
					if((clTo.isAttributesAsClasses || clTo.isOL) && !db.getTable(clTo.table).getColumn(clTo.column).isForeignkey)
						listToRemove.add(clTo);
				}
			}
		}
		
		///////////////////////////////////
		// remove the bad matches 
		for(ClassMatch cl : listToRemove){
			removeMatch(cl);
		}
		
		listToRemove.clear();
		//
		///////////////////////////////////
		
		for(String domainclass : domainmatches.keySet()){

			List<ClassMatch> matches = domainmatches.get(domainclass);
			System.out.println("CHECKING NON needed MAtches AC/OL: " + domainclass);

			//this is only valid if there are more than 1 match
			if(matches.size() <= 1) continue;
						
			boolean bExitsNonACOL = false;
			for(int j = 0; j < matches.size(); j++) {
				ClassMatch clTo = matches.get(j);
				
				if( !clTo.isAttributesAsClasses && !clTo.isOL) {
					bExitsNonACOL = true;
					break;
				}
			}
			
			if(!bExitsNonACOL) continue;
			
			for(int j = 0; j < matches.size(); j++) {
				ClassMatch clTo = matches.get(j);

				if( clTo.isAttributesAsClasses || clTo.isOL) {
					listToRemove.add(clTo);
					
				}
			}
		}
		///////////////////////////////////
		// remove the bad matches 
		for(ClassMatch cl : listToRemove){
			removeMatch(cl);
		}
		
		listToRemove.clear();
		//
		///////////////////////////////////
		*/
	}
	


	/**
	 * This method checks if both matchings are consistens in terms that those primary keys are connected.
	 * @param clFrom
	 * @param clTo
	 * @return
	 */
	private boolean areConsistent(ClassMatch clFrom, ClassMatch clTo, boolean bFromModified) {
			
		//3.1 obtain all primary and foreign keys
		LinkedHashSet<String> keysFrom = new LinkedHashSet<>();
		LinkedHashSet<String> keysTo = new LinkedHashSet<>();

		keysFrom.add(clFrom.column);
		keysFrom.addAll(db.getTable(clFrom.table).getPrimaryKeys());
		keysTo.add(clTo.column);
		keysTo.addAll(db.getTable(clTo.table).getPrimaryKeys());
		
		for(String keyFrom : keysFrom) {
			String sfrom = clFrom.table+"."+keyFrom;
			
			for(String keyTo : keysTo) {
				String sto = clTo.table+"."+keyTo;
				
				if(sfrom.compareTo(sto) != 0) {
					
					//it can be that the from is connected to the sTo or other way around
					if(db.getTable(clFrom.table).getColumn(keyFrom).isForeignkey){
						String sForeignFrom = db.getTable(clFrom.table).getColumn(keyFrom).foreigntable+"."+db.getTable(clFrom.table).getColumn(keyFrom).foreignkey;
						if(sForeignFrom.compareTo(sto) != 0) {

						} else {
							clFrom.column = keyFrom;
							clTo.column = keyTo;							
							
							return true;
						}
					}
					if(db.getTable(clTo.table).getColumn(keyTo).isForeignkey){
						String sForeignTo = db.getTable(clTo.table).getColumn(keyTo).foreigntable+"."+db.getTable(clTo.table).getColumn(keyTo).foreignkey;
						if(sForeignTo.compareTo(sfrom) != 0) {

						} else {		
							clFrom.column = keyFrom;
							clTo.column = keyTo;
							
							return true;
						}
					}
				} else {
					// the columns are equal so they are consistent
					return true;
				}
			}
		}
		
		return false;
	}


	private void initPropertiesMatcher(OntologyTools ontPutative, OntologyTools ontDomain){
		// Train a TFIDF metric for properties
		OWLOntology ontA = ontPutative.getOWLOntology();
		OWLOntology ontB = ontDomain.getOWLOntology();
		
		Set<OWLObjectProperty> aObjectProps = ontA.getObjectPropertiesInSignature();
		Set<OWLDataProperty> aDataProps = ontA.getDataPropertiesInSignature();
		Set<OWLAnnotationProperty> aAnnotationProps = ontA.getAnnotationPropertiesInSignature();
		
		Set<OWLEntity> aEntities = new HashSet<>();
		aEntities.addAll(aObjectProps);
		aEntities.addAll(aDataProps);
		aEntities.addAll(aAnnotationProps);
		
		Set<OWLObjectProperty> bObjectProps = ontB.getObjectPropertiesInSignature();
		Set<OWLDataProperty> bDataProps = ontB.getDataPropertiesInSignature();
		Set<OWLAnnotationProperty> bAnnotationProps = ontB.getAnnotationPropertiesInSignature();
		
		Set<OWLEntity> bEntities = new HashSet<>();
		bEntities.addAll(bObjectProps);
		bEntities.addAll(bDataProps);
		bEntities.addAll(bAnnotationProps);
		
		propMetric = new TFIDFMetricProp();
		propMetric.init(aEntities, ontA, bEntities, ontB);
		
		generalMetric = new TFIDFMetricProp();
		generalMetric.init(ontA.getSignature(), ontA, ontB.getSignature(), ontB);

		softMetric = new SoftTFIDFMetricProp(0.9);
		softMetric.init(aEntities, ontA, bEntities, ontB);
	}
	
	public static String getDomainString(OWLEntity e, OWLOntology ont) {
		
		if (domainMap.containsKey(e.getIRI().getFragment())) return domainMap.get(e.getIRI().getFragment());
		
		String s = "";
		
		Set<OWLEntity> neighbors = getDomain(e, ont);
		for (OWLEntity neighbor: neighbors) {
			s += " " + Preprocessing.preprocess(neighbor, ont);
		}
		
		s = s.trim();
		if (s.length() == 0) {
			s = "none";
		}
		
		domainMap.put(e.getIRI().getFragment(), s);
		
		return s;
	}
	
	
	public static String getRangeString(OWLEntity e, OWLOntology ont) {
		
		if (rangeMap.containsKey(e.getIRI().getFragment())) return rangeMap.get(e.getIRI().getFragment());
		
		String s = "";
		
		Set<OWLEntity> neighbors = getRange(e, ont);
		for (OWLEntity neighbor: neighbors) {
			s += " " + Preprocessing.preprocess(neighbor, ont);
		}
		
		if (e.isOWLDataProperty()) {
			s += " literal";
		}
		
		s = s.trim();
		if (s.length() == 0) {
			s = "none";
		}
		
		rangeMap.put(e.getIRI().getFragment(),  s);
		
		return s;
	}
	

	private static Set<OWLEntity> getDomain(OWLEntity e, OWLOntology ont) {
		
		Set<OWLEntity> neighbors = new HashSet<>();
		
		if (e.isOWLObjectProperty()) {
			Set<OWLClassExpression> temp = e.asOWLObjectProperty().getDomains(ont);
			for (OWLClassExpression t: temp) {
				if (t.isAnonymous()) {
					Set<OWLClass> components = t.getClassesInSignature();
					for (OWLClass component: components) {
						neighbors.add(component);
					}
				} else {
					neighbors.add(t.asOWLClass());
				}
			}
		}
		
		if (e.isOWLDataProperty()) {
			Set<OWLClassExpression> temp = e.asOWLDataProperty().getDomains(ont);
			for (OWLClassExpression t: temp) {
				if (t.isAnonymous()) {
					Set<OWLClass> components = t.getClassesInSignature();
					for (OWLClass component: components) {
						neighbors.add(component);
					}
				} else {
					neighbors.add(t.asOWLClass());
				}
			}
		}
		
		if (e.isOWLAnnotationProperty()) {
			
			Set<OWLAxiom> axioms = ont.getAxioms();
			for (OWLAxiom axiom: axioms) {
				
				String axiomS = axiom.toString();
				if (axiomS.contains(e.toString()) && axiomS.contains("Domain")) {
					
					if (axiomS.contains("<") && axiomS.contains(">")) {
						String domain = axiomS.substring(axiomS.lastIndexOf("<") + 1, 
								axiomS.lastIndexOf(">"));
						neighbors.addAll(ont.getEntitiesInSignature(IRI.create(domain)));
					}
				}
			}
		}
		
		return neighbors;
	}
	
	
	private static Set<OWLEntity> getRange(OWLEntity e, OWLOntology ont) {
		
		Set<OWLEntity> neighbors = new HashSet<>();
		
		if (e.isOWLObjectProperty()) {
			Set<OWLClassExpression> temp = e.asOWLObjectProperty().getRanges(ont);
			for (OWLClassExpression t: temp) {
				if (t.isAnonymous()) {
					Set<OWLClass> components = t.getClassesInSignature();
					for (OWLClass component: components) {
						neighbors.add(component);
					}
				} else {
					neighbors.add(t.asOWLClass());
				}
			}
		}
		
		///////////
		// PropString do not take into account the ranges of the datatypes... to check it. 
		// An option, to include them and and try to harmonize the types to compatible ones.
		// https://www.w3.org/2001/sw/rdb2rdf/r2rml/
		// https://www.w3.org/2001/sw/rdb2rdf/wiki/Mapping_SQL_datatypes_to_XML_Schema_datatypes
		
		
		return neighbors;
	}


}


class LogStreamReader implements Runnable {

    private BufferedReader reader;

    public LogStreamReader(InputStream is) {
        this.reader = new BufferedReader(new InputStreamReader(is));
    }

    public void run() {
        try {
            String line = reader.readLine();
            while (line != null) {
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}