package org.cheatham.metric;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;


public class PartOfSpeech {
	
	public static MaxentTagger tagger = new MaxentTagger("english-left3words-distsim.tagger");
	
	public static String getCoreConcept(String phrase) {
		
		String core = null;
		String aNoun = "";
        String tagged = tagger.tagString(phrase);
        
//        System.out.println(phrase + " -> " + tagged);
        
        boolean stopNoun = false;

        String[] tokens = tagged.split("\\s+");
        for (String token: tokens) {
        	if (token.contains("V") && token.length() > 7) {
        		token = token.substring(0, token.lastIndexOf("_"));
        		core = token;
        		aNoun = "";
        		break;
        	}
        	
           	if ((token.contains("N") || token.contains("J")) && !stopNoun) {
           		if (token.contains("IN")) {
           			stopNoun = true;
           		} else {
           			token = token.substring(0, token.lastIndexOf("_"));
           			aNoun += " " + token;
           		}
        	}
        }
        
        if (core == null) {
        	core = aNoun.trim();
        }
        
//        System.out.println("\t" + core);
        
        return core;
	}
	
	
	public static void main(String[] args) {
		System.out.println(getCoreConcept("is conference attendee"));
	}
}
