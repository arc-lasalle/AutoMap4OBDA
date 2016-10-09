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

package edu.salleurl.arc.automap4obda.ontology;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.lf5.viewer.configure.MRUFileManager;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.CollectionFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import edu.salleurl.arc.automap4obda.matcher.AllDirectedPaths;
import edu.salleurl.arc.automap4obda.matcher.ClassMatch;
import edu.salleurl.arc.automap4obda.matcher.ColumnStructure;
import edu.salleurl.arc.automap4obda.matcher.TableStructure;
import edu.stanford.nlp.util.Sets;
import uk.ac.manchester.cs.jfact.JFactFactory;

import org.semanticweb.owlapi.search.EntitySearcher;


public class OntologyTools {

	OWLOntologyManager oMan;
	OWLOntology oOntology;
	OWLReasoner oReasoner;
	OWLDataFactory mFactory = null;

	String msBbasePrefix = "";
	DirectedGraph<String, DefaultEdge> g;
	List<GraphPath<String, DefaultEdge>> allPaths;
	HashMap<String, List<GraphPath<String, DefaultEdge>>> pathsOfVertex;
	int nMaxPathLength = 10;
	
    public OntologyTools() {  
    }
    
    public void load (String sOntology) {
    		
		OWLDataFactory factory;
		oMan = OWLManager.createOWLOntologyManager();
        factory = oMan.getOWLDataFactory();

        File f = new File(sOntology);        
        
		try {
			oOntology = oMan.loadOntologyFromOntologyDocument(f);

			Configuration configuration = new Configuration();
            configuration.ignoreUnsupportedDatatypes = true;
			
			oReasoner = new Reasoner(configuration, oOntology);
			
			oReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			
			mFactory = oMan.getOWLDataFactory();	    	
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
    }

    
    public void initSearch() {
		g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		
		for(OWLClass cl: oOntology.getClassesInSignature()) {
			g.addVertex(cl.getIRI().toString());
		}
				
		for(OWLObjectProperty op : oOntology.getObjectPropertiesInSignature())
		{
		    Collection<OWLClassExpression> superD = EntitySearcher.getDomains(op, oOntology);
		    Collection<OWLClassExpression> superR = EntitySearcher.getRanges(op, oOntology);

			//domains
     	    for (OWLClassExpression owlClassDom : superD) {

     	    	
     	    	if(owlClassDom.isOWLThing()) continue;
     	    	if(owlClassDom.isOWLNothing()) continue;
     	    	if(owlClassDom.isAnonymous()) continue;
     	    	
     	    	for (OWLClassExpression owlClassRang : superR) {
     	    	
         	    	if(owlClassRang.isOWLThing()) continue;
         	    	if(owlClassRang.isOWLNothing()) continue;
         	    	if(owlClassRang.isAnonymous()) continue;
         	    	
        			g.addVertex(op.getIRI().toString());

         	    	g.addEdge(owlClassDom.asOWLClass().getIRI().toString(), op.getIRI().toString());
         	    	g.addEdge(op.getIRI().toString(), owlClassRang.asOWLClass().getIRI().toString());
     	    	}
     	    }
		}
		
		
		
	}
    //////////////////////////////////////
    // Method to find the path between two concepts
    //
	
    public List<GraphPath<String, DefaultEdge>>  findPathBetween(String source, String target, int iMaxPathLength) {

    	List<GraphPath<String, DefaultEdge>> path = new ArrayList<GraphPath<String, DefaultEdge>>();
	
    	Set<String> sources = new HashSet<>();
		Set<String> targets = new HashSet<>();

		sources.add(source);
		targets.add(target);
		
		if(!g.containsVertex(source) || !g.containsVertex(target)) return path;
		
        DijkstraShortestPath<String, DefaultEdge> fastpath = new DijkstraShortestPath<String, DefaultEdge>(
                g,
                source,
                target,
                Double.POSITIVE_INFINITY);

        if(fastpath == null) return path;
        if(fastpath.getPathEdgeList() == null) return path;
        
        if(iMaxPathLength > fastpath.getPathEdgeList().size())
        	iMaxPathLength = fastpath.getPathEdgeList().size();
        
		AllDirectedPaths<String, DefaultEdge> pathFindingAlg = new AllDirectedPaths<String, DefaultEdge>(g);

		allPaths = pathFindingAlg.getAllPaths(sources, targets, true, iMaxPathLength);	
		
		Iterator<GraphPath<String, DefaultEdge>> iter = allPaths.iterator();
		int iMinSizes = 9999;
		
		while(iter.hasNext())
		{
			GraphPath<String, DefaultEdge> gpath = iter.next();

			if(gpath.getEdgeList().size() < iMinSizes) {
				iMinSizes = gpath.getEdgeList().size();
			}			
		}
		
		iter = allPaths.iterator();
		
		while(iter.hasNext())
		{
			GraphPath<String, DefaultEdge> gpath = iter.next();
			
			if(gpath.getEdgeList().size() == iMinSizes) {
				path.add(gpath);
			}
		}

		return path;
	}


    public int maxClassNameLength()
    {
    	int ret = 0;
    	int l ;
    	
    	for(OWLClass c : oOntology.getClassesInSignature())
    	{
    		l = c.getIRI().getFragment().length();
    		
    		if(ret < l) ret = l;
    	}
    	
    	return ret;
    }


    public int calculateMaxSubClasses()
    {
    	String sMaxClass = "";
    	int iMax = 0;
    	int iTotal = 0;
    	int iCount = 0;
    	
    	int iMaxDeep = getMaxDeepSubClasses();
    	
    	iMaxDeep = (int) (iMaxDeep*0.1);
    	
    	if(iMaxDeep < 0) iMaxDeep = 1;
    	
		for (OWLClass cls : oOntology.getClassesInSignature())
		{
			if(!cls.isOWLThing() ){
							
				int size = getDirectSubclassesSize(cls, iMaxDeep);
				
				if(size > iMax) {
					iMax = size;
					sMaxClass = cls.getIRI().getFragment();
				}
				
				iTotal += size;
				iCount++;
			}
		}
		
		return iMax;
    }

    private int getDirectSubclassesSize(OWLClass oClass, int iLevel){
    	
    	NodeSet<OWLClass> subC =  oReasoner.getSubClasses(oClass, true);
		 	
    	int size = 0;
 
		for (OWLClass owlClassSu : subC.getFlattened()) {

			if(!owlClassSu.isOWLNothing()) {
				size += 1;
				
				if(iLevel > 1)
					size += getDirectSubclassesSize(owlClassSu, iLevel-1);
			}
		}
    	
    	return size;
    }

    private int getMaxDeepSubClasses()
    {
    	int iDeep = 1;
    	
    	String sMaxClass = "";
    	int iTotal = 0;
    	int iCount = 0;
    	HashMap<String, Integer>  hmClassesVisited = new HashMap<String, Integer>();
    	
		for (OWLClass cls : oOntology.getClassesInSignature())
		{
			if(!cls.isOWLThing() /*&& cls.getIRI().getFragment().compareTo("TemporalRegion") == 0 */){
							
				int size = getDirectSubclassesDeep(cls, hmClassesVisited);
				
				if(size > iDeep) {
					iDeep = size;
					sMaxClass = cls.getIRI().getFragment();
				}
				
				iTotal += size;
				iCount++;
			}
		}
    	
    	return iDeep;
    }

    private int getDirectSubclassesDeep(OWLClass oClass, HashMap<String, Integer>  hmClassesVisited){
    	
    	if(hmClassesVisited.containsKey(oClass.getIRI().toString())) return 0;
    	
    	hmClassesVisited.put(oClass.getIRI().toString(), 1);
    	
    	NodeSet<OWLClass> subC =  oReasoner.getSubClasses(oClass, true);
 	
    	int size = 0;
 
		for (OWLClass owlClassSu : subC.getFlattened()) {

			if(!owlClassSu.isOWLNothing()) {
				
				if(size < 1)
					size = 1;
				
				
					int tmp = getDirectSubclassesDeep(owlClassSu, hmClassesVisited) +1;
					
					if(size < tmp) size = tmp;
				
			}
		}
    	
		return size;
    }
    
    public double calculateEntropy()
    {    	
    	String sMaxClass = "";
    	double dMax = 0;
    	double dMin = 10000;
    	double dTotal = 0;
    	int iCount = 0;
    	
    	int iMaxDeep = getMaxDeepSubClasses();
    	
    	iMaxDeep = (int) (iMaxDeep*0.1);
    	
    	if(iMaxDeep < 0) iMaxDeep = 1;
    	
		for (OWLClass cls : oOntology.getClassesInSignature())
		{
			if(!cls.isOWLThing() ){
				
				HashMap<String, Integer> hmPatterns = new HashMap<String, Integer>();
				
				int size = getDirectSubclasses(cls, iMaxDeep, hmPatterns);
				
				double dEntropy = 0;
				
				for(String sPat : hmPatterns.keySet()){
					double dP = hmPatterns.get(sPat) / (double)size;
					dEntropy += dP * Math.log(dP);
				}
				
				dEntropy *= -1;
				
				if(dEntropy > dMax) {
					dMax = dEntropy;
					sMaxClass = cls.getIRI().getFragment();
				}
				if(dEntropy < dMin && size > 1) {
					dMin = dEntropy;
				}
				
				dTotal += dEntropy;
				iCount++;
			}
		}
		/*
		System.out.println("Total subclasss: " +dTotal + " num classes:" + iCount);
		System.out.println("Average subclasss: " +(float)dTotal/(float)iCount);
		System.out.println("MAX subclasseS: " +sMaxClass+ " > "+ dMax);
		System.out.println("MIN subclasseS: " +sMaxClass+ " > "+ dMin);
		*/
		return dMax;
    }

    private int getDirectSubclasses(OWLClass oClass, int iLevel, HashMap<String, Integer> hmClasses){
    	        	
    	NodeSet<OWLClass> subC =  oReasoner.getSubClasses(oClass, true);
		  	
    	int size = 0;
 
		for (OWLClass owlClassSu : subC.getFlattened()) {

			if(!owlClassSu.isOWLNothing()) {
				size += 1;
				
				String sPattern = owlClassSu.getIRI().getFragment().replaceAll("\\S", "A");
				
				if(!hmClasses.containsKey(sPattern ))
					hmClasses.put(sPattern, 1);
				else
					hmClasses.put(sPattern, 1 + hmClasses.get(sPattern));
					
				if(iLevel > 1)
					size += getDirectSubclassesSize(owlClassSu, iLevel-1);
			}
		}
    	
    	return size;
    }
    
    public Boolean isAnOntologyLearningClass(String sClass){
    	PrefixManager pm = new DefaultPrefixManager(msBbasePrefix);
    	OWLClass oClass = mFactory.getOWLClass(sClass, pm);
    	OWLAnnotationProperty oProp = mFactory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());

    			
    	for( OWLAnnotation anno : oClass.getAnnotations(oOntology, oProp))
    	{
    		String sValue = anno.getValue().toString();
    		
    		if(sValue.startsWith("\"<Ontology learning>: "))
    			return true;
    	}
    	
    	return false;
    }
    
    public OWLClass getOWLClass(String iri){
        if(oOntology.containsClassInSignature(IRI.create(iri)))
        	return mFactory.getOWLClass(IRI.create(iri));
        
        return null;
    }

	public OWLDataProperty getOWLDataProperty(String iri) {
		
		if(oOntology.containsDataPropertyInSignature(IRI.create(iri)))
			return mFactory.getOWLDataProperty(IRI.create(iri));
		
		return null;
	}
	
    public String[] getTableAndColumnFromClass(String sClass){
    
    	OWLClass oClass = mFactory.getOWLClass(IRI.create(sClass));
    	OWLAnnotationProperty oProp = mFactory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());

    			
    	for( OWLAnnotation anno : oClass.getAnnotations(oOntology, oProp))
    	{
    		String sValue = anno.getValue().toString();
    		
    		if(sValue.startsWith("\"<Ontology learning>: "))
    		{
    			sValue = sValue.replace("\"<Ontology learning>: ", "").replace("\"^^xsd:string", "");
    			  			
    			String [] split = sValue.split(" = ");
    			
    			if(split.length == 2)
    			{	
    				String [] tableCol = split[0].split("\\.");
    				if(tableCol.length == 2)
        			{
    					String[] ret = new String[3];
    					ret[0] = tableCol[0];
        				ret[1] = tableCol[1];
        				ret[2] = sValue;
        				
        				return ret;	
        			}
    				 
    			}
    			
    			return null;
    		}
    	}
    	
    	return null;
    }
    /////////////////////////////////////////////////////////////////////////7
    // Methods for creating the ontology
    //
    
    public void createOntology(String sIRI, String sBbasePrefix){
    	IRI ontologyIRI = IRI.create(sIRI);
    	oMan = OWLManager.createOWLOntologyManager();
    	
    	try {
    		oOntology = oMan.createOntology(ontologyIRI);
	    	
	    	msBbasePrefix = sBbasePrefix;
	    	
			mFactory = oMan.getOWLDataFactory();
			
			Configuration configuration = new Configuration();
            configuration.ignoreUnsupportedDatatypes = true;
			
			oReasoner = new Reasoner(configuration, oOntology);
			
			oReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);			
			
    	} catch (OWLOntologyCreationException e) {
    		e.printStackTrace();
    	}
    }
    
    public void addClass(String sName, String sDescription) {
    	
    	OWLDataFactory factory = oMan.getOWLDataFactory();
    	PrefixManager pm = new DefaultPrefixManager(msBbasePrefix);
    	
    	
    	OWLClass sClass = factory.getOWLClass(sName, pm);
    	
    	OWLDeclarationAxiom declaration = factory.getOWLDeclarationAxiom(sClass);
    	    	
    	//Add class
       	oMan.addAxiom(oOntology, declaration);
    	
    	//Add label
       	OWLAnnotation sClassLabel = factory.getOWLAnnotation( 
       			factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), 
       			factory.getOWLLiteral(sName));
       	
       	oMan.applyChange(new AddAxiom(oOntology, factory.getOWLAnnotationAssertionAxiom(sClass.getIRI(), sClassLabel)));

    	//Add description
       	OWLAnnotation sClassDescription = factory.getOWLAnnotation( 
       			factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()), 
       			factory.getOWLLiteral(sDescription));
       	
       	oMan.applyChange(new AddAxiom(oOntology, factory.getOWLAnnotationAssertionAxiom(sClass.getIRI(), sClassDescription)));
    }
    
    public void addClass(String sName, String sSuperName, String sDescription) {
    	
    	OWLDataFactory factory = oMan.getOWLDataFactory();
    	PrefixManager pm = new DefaultPrefixManager(msBbasePrefix);
    	
    	
    	OWLClass sClass = factory.getOWLClass(sName, pm);
    	
    	OWLDeclarationAxiom declaration = factory.getOWLDeclarationAxiom(sClass);
    	    	
    	//Add class
       	oMan.addAxiom(oOntology, declaration);
    	
       	// Super class
       	OWLClass sSuperClass = factory.getOWLClass(sSuperName, pm);
       	
       	OWLDeclarationAxiom declaration2 = factory.getOWLDeclarationAxiom(sClass);
    	
    	//Add class
       	//oMan.addAxiom(oOntology, declaration2);
       	
       	OWLAxiom axiom = factory.getOWLSubClassOfAxiom(sClass, sSuperClass);
	    // add the axiom to the ontology.
	    AddAxiom addAxiom = new AddAxiom(oOntology, axiom);
	    // We now use the manager to apply the change
	    oMan.applyChange(addAxiom);
       	
       	
    	//Add label
       	OWLAnnotation sClassLabel = factory.getOWLAnnotation( 
       			factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), 
       			factory.getOWLLiteral(sName));
       	
       	oMan.applyChange(new AddAxiom(oOntology, factory.getOWLAnnotationAssertionAxiom(sClass.getIRI(), sClassLabel)));

    	//Add description
       	OWLAnnotation sClassDescription = factory.getOWLAnnotation( 
       			factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()), 
       			factory.getOWLLiteral(sDescription));
       	
       	oMan.applyChange(new AddAxiom(oOntology, factory.getOWLAnnotationAssertionAxiom(sClass.getIRI(), sClassDescription)));
    }
    
    public void addObjectproperty(String sObjectproperty, String sDomain, String sRange) {
  		OWLDataFactory factory = oMan.getOWLDataFactory();
       	
    	PrefixManager pm = new DefaultPrefixManager(msBbasePrefix);

  		OWLObjectProperty objproperty = factory.getOWLObjectProperty(sObjectproperty, pm);
  		
  		OWLDeclarationAxiom declaration = factory.getOWLDeclarationAxiom(objproperty);
    	
    	//Add object property
       	oMan.addAxiom(oOntology, declaration);

       	////////////////////////////////////
       	// add domain
        OWLClass domainClass= factory.getOWLClass(sDomain, pm);
       	//Add domain with global restrictions
        OWLObjectPropertyDomainAxiom domain=factory.getOWLObjectPropertyDomainAxiom((OWLObjectPropertyExpression)objproperty, domainClass);   	
        oMan.addAxiom(oOntology, domain);
        
       	//Add domain with local restrictions
         
        oMan.addAxiom(oOntology, factory.getOWLSubClassOfAxiom( 
							factory.getOWLObjectSomeValuesFrom(objproperty, factory.getOWLThing()), domainClass)); 
        
        ////////////////////////////////////
        // add range
        OWLClass rangeClass= factory.getOWLClass(sRange, pm);
       	//Add domain with global restrictions
        OWLObjectPropertyRangeAxiom range=factory.getOWLObjectPropertyRangeAxiom((OWLObjectPropertyExpression)objproperty, rangeClass);   	
        oMan.addAxiom(oOntology, range);
        
        //Add range with local restrictions
        
        factory.getOWLInverseFunctionalObjectPropertyAxiom(objproperty);
        
        
        oMan.addAxiom(oOntology, factory.getOWLSubClassOfAxiom( 
							factory.getOWLObjectSomeValuesFrom(objproperty.getInverseProperty(), factory.getOWLThing()), rangeClass)); 
   	}  
    
  	public void addDatatype(String sClassName, String sDatatype, String sType) {
  		OWLDataFactory factory = oMan.getOWLDataFactory();
    	PrefixManager pm = new DefaultPrefixManager(msBbasePrefix);
    	

  		OWLDataProperty datatype = factory.getOWLDataProperty(sDatatype, pm);
  		
  		OWLDeclarationAxiom declaration = factory.getOWLDeclarationAxiom(datatype);
    	
    	//Add datatype
       	oMan.addAxiom(oOntology, declaration);
       //	oMan.addAxiom(oOntology, factory.getOWLFunctionalDataPropertyAxiom(datatype));


        OWLDataPropertyExpression man= factory.getOWLDataProperty(sDatatype, pm);
        OWLClass car= factory.getOWLClass(sClassName, pm);
        
        OWLDataPropertyDomainAxiom domain=factory.getOWLDataPropertyDomainAxiom(man, car);
    	//Add domain
        oMan.addAxiom(oOntology, domain);

        if(sType.length() > 0) {
        	OWLDatatype dt = factory.getOWLDatatype("xsd:"+sType,pm);
        	
            OWLDataPropertyRangeAxiom range= factory.getOWLDataPropertyRangeAxiom(man, dt);

            //Add range
        	oMan.addAxiom(oOntology, range);	
        }
	}  
  	
    public void saveOntology(String sPath){
    	OutputStream os;
		
    	try {
			os = new FileOutputStream(new File(sPath));
			
			oMan.saveOntology(oOntology, new RDFXMLOntologyFormat(), os);
			
			os.close();
			
		} catch (OWLOntologyStorageException | IOException e) {
			
			e.printStackTrace();
		}
    	
    	
    }

	public Set<OWLClass> getClasses() {
		
		return oOntology.getClassesInSignature();
	}
	
	public Set<OWLDataProperty> getDataProperties() {
		
		Set<OWLDataProperty> domainDataproperties = oOntology.getDataPropertiesInSignature();
		
		OWLDataFactory factory = oMan.getOWLDataFactory();

		domainDataproperties.add(factory.getOWLDataProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()));
		
		domainDataproperties.add(factory.getOWLDataProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));

		return domainDataproperties;
	}

	public Set<OWLDataProperty> getDataPropertiesFromDomain(String sClass) {

	    OWLClass cnode = mFactory.getOWLClass(IRI.create(sClass));

	    Set<OWLDataProperty> dplist = CollectionFactory.createSet();
	    
		for(OWLDataProperty dp : oOntology.getDataPropertiesInSignature())
		{
			NodeSet<OWLClass> result = oReasoner.getDataPropertyDomains(dp, false);
       	    
     	    for (OWLClass owlClass : result.getFlattened()) {

   	    		if(owlClass.isOWLThing() || owlClass.compareTo(cnode) == 0) {
   	    			dplist.add(dp);
     	    	}
     	    }
		}


		return dplist;
	}

	public String getClassFragment(String sClass) {
	    OWLClass cnode = mFactory.getOWLClass(IRI.create(sClass));

	    return cnode.getIRI().getFragment();		
	}

	public OWLOntology getOWLOntology() {

		return oOntology;
	}

	//Checks if the source and the target are related with an inheritance relation (super class or sub class).
	private boolean checkInheritance(String fromNode, String toNode) {

	    OWLClass from = mFactory.getOWLClass(IRI.create(fromNode));
	    OWLClass to = mFactory.getOWLClass(IRI.create(toNode));

	    //If the source of the target are owl:thing then there is no inheritance relation
	    if(from.isOWLThing() || to.isOWLThing())
	    	return false;
	
	    
	    
	   // Collection<OWLClassExpression> superC2 = EntitySearcher.getSuperClasses(from, oOntology);
	    
		 //1. We look for super classes
		NodeSet<OWLClass> superC = oReasoner.getSuperClasses(from, true);
		
		for (OWLClass owlClassS : superC.getFlattened()) {
 	    	if(owlClassS.getIRI().toString().compareTo(toNode) == 0)
 	    		return true;
		}
		
		NodeSet<OWLClass> subC = oReasoner.getSubClasses(from, false);
		
		for (OWLClass owlClassSu : subC.getFlattened()) {
			if(owlClassSu.getIRI().toString().compareTo(toNode) == 0)
 	    		return true;
		}
		
		return false;
	}

	/**
	 * This method add more domain and ranges to the object/data properties according to inferences.
	 * Also adds rdfs properties such as label and comment 
	 */
	public void enhanceOntology(){
		
		OWLDataFactory factory = oMan.getOWLDataFactory();
				
		for(OWLDataProperty dp : oOntology.getDataPropertiesInSignature())
		{
			NodeSet<OWLClass> result = oReasoner.getDataPropertyDomains(dp, true);

     	    for (OWLClass owlClass : result.getFlattened()) {

     	    	if(owlClass.isOWLThing()) continue;
     	    	      	    
     	    	for (OWLClass owlSubClass : oReasoner.getSubClasses(owlClass, false).getFlattened()) {
     	    		
     	    		if(!owlSubClass.isOWLNothing()){	     	    			
     	    	        OWLDataPropertyDomainAxiom domain=factory.getOWLDataPropertyDomainAxiom(dp.asOWLDataProperty(), owlSubClass);
     	    	    	//Add domain
     	    	        oMan.addAxiom(oOntology, domain);
     	    		}
         	    }
     	    }
		}


		for(OWLObjectProperty op : oOntology.getObjectPropertiesInSignature())
		{
			//domains
			NodeSet<OWLClass> result = oReasoner.getObjectPropertyDomains(op, true);

     	    for (OWLClass owlClass : result.getFlattened()) {

     	    	if(owlClass.isOWLThing()) continue;
     	    	
     	    	for (OWLClass owlSubClass : oReasoner.getSubClasses(owlClass, false).getFlattened()) {
     	    		
     	    		if(!owlSubClass.isOWLNothing()){
     	    	        OWLObjectPropertyDomainAxiom domain=factory.getOWLObjectPropertyDomainAxiom(op.asOWLObjectProperty(), owlSubClass);
     	    	    	//Add domain
     	    	        oMan.addAxiom(oOntology, domain);
     	    		}
         	    }
     	    }
     	    //ranges
			result = oReasoner.getObjectPropertyRanges(op, true);

     	    for (OWLClass owlClass : result.getFlattened()) {

     	    	if(owlClass.isOWLThing()) continue;
     	    	
     	    	for (OWLClass owlSubClass : oReasoner.getSubClasses(owlClass, false).getFlattened()) {
     	    		
     	    		if(!owlSubClass.isOWLNothing()){
     	    	        OWLObjectPropertyRangeAxiom domain=factory.getOWLObjectPropertyRangeAxiom(op.asOWLObjectProperty(), owlSubClass);
     	    	    	//Add domain
     	    	        oMan.addAxiom(oOntology, domain);
     	    		}
         	    }
     	    }
		}
		
		//We commit the changes
        oReasoner.flush();
	}
}
