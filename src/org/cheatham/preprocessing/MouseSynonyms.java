package org.cheatham.preprocessing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class MouseSynonyms extends DefaultHandler {

	private static String uri;

	private static HashMap<String, String> values;
	private static HashMap<String, ArrayList<String>> references;

	private static String tempVal;
	private static String classURI;
	private static String synURI;
	private static ArrayList<String> refs;

	private static HashMap<URI, HashSet<String>> cache;
	private static File mouseSynonymFile = new File("mouse_synonyms");

	private static boolean initialized = false;

	public void init(String theURI) {

		values = new HashMap<String, String>();
		references = new HashMap<String, ArrayList<String>>();
		refs = new ArrayList<String>();

		if (cache == null) {

			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(mouseSynonymFile));
				cache = (HashMap<URI, HashSet<String>>) ois.readObject();
			} catch (Exception e) {
				e.printStackTrace();
				cache = new HashMap<URI, HashSet<String>>();
			}
		}


		uri = theURI;

		parseDocument();

		initialized = true;
	}


	public HashSet<String> getSynonyms(URI entity) {

		if (!initialized) {
			this.init("data/anatomy/mouse.owl");
		}

		HashSet<String> synonyms = new HashSet<String>();

		if (cache.containsKey(entity)) {
			synonyms = cache.get(entity);

		} else {

			if (!references.containsKey(entity.toString())) {
				return synonyms;
			}

			ArrayList<String> keys = references.get(entity.toString());

			for (String s: keys) {
				synonyms.add(values.get(s));
			}

			cache.put(entity, synonyms);
		}

		return synonyms;
	}


	private void parseDocument() {

		SAXParserFactory spf = SAXParserFactory.newInstance();

		try {
			SAXParser sp = spf.newSAXParser();
			sp.parse(uri, this);

		} catch(Exception se) {
			se.printStackTrace();
		}
	}


	//	private void printData() {
	//
	//		for (String key: references.keySet()) {
	//			System.out.println(key);
	//
	//			ArrayList<String> refVals = references.get(key);
	//			for (String refVal: refVals) {
	//				System.out.println("\t" + values.get(refVal));
	//			}
	//		}
	//	}


	public void startElement(String uri, String localName, String qName, 
			Attributes attributes) throws SAXException {

		if (qName.equalsIgnoreCase("owl:Class")) {
			classURI = attributes.getValue("rdf:about");

		} else if (qName.equalsIgnoreCase("oboInOwl:hasRelatedSynonym")) {
			refs.add(attributes.getValue("rdf:resource"));

		} else if (qName.equalsIgnoreCase("rdf:Description")) {
			synURI = attributes.getValue("rdf:about");

		} else if (qName.equalsIgnoreCase("rdfs:label")) {
			tempVal = "";
		}
	}


	public void characters(char[] ch, int start, int length) throws SAXException {
		tempVal += new String(ch, start, length);
	}

	public void endElement(String uri, String localName, String qName) 
			throws SAXException {

		if (qName.equalsIgnoreCase("rdfs:label")) {
			values.put(synURI, tempVal);
			tempVal = "";

		} else if (qName.equalsIgnoreCase("owl:Class")) {
			if (refs.size() > 0) {
				references.put(classURI, refs);
			}
			classURI = "";
			refs = new ArrayList<String>();

		} else if (qName.equalsIgnoreCase("rdf:Description")) {
			synURI = "";
		} 
	}

	public static void close() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(mouseSynonymFile));
			oos.writeObject(cache);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws URISyntaxException {

		MouseSynonyms p = new MouseSynonyms();

		HashSet<String> synonyms = p.getSynonyms(new URI("http://mouse.owl#MA_0000860"));
		for (String syn: synonyms) {
			System.out.println(syn);
		}
	}
}
