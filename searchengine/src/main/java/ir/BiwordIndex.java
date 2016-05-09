/*  
 *   This file is part of the computer assignment for the 
 *   Information Retrieval course at KTH.
 * 
 *   First version: Laura Jacquemod, 2016
 */

package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 *   Implements an inverted index as a Hashtable from Biwords to PostingsLists.
 */
public class BiwordIndex implements Index {

    /** The biword index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();

    private String lastToken=null;
    private int lastTime=-1;
    private int lastDocID=-1;
    
    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int frame) {
    	//If new document
    	if(docID!=lastDocID){
    		lastToken=null;
    		lastTime=0;
    		lastDocID=docID;
    	}
    	else {
		String biword=lastToken+" "+token;
    	int biwordOffset=lastTime;
    		
		PostingsList postlist = index.get(biword);
		//If word already indexed
	    	if (index.containsKey(biword)) {
	    		
	    		//If the word was already found in the document
	    		if (postlist.get(postlist.size()-1).docID == docID){
	    			postlist.addPosting(docID,frame);
	    		} else {
	    		//First occurence of the word in the document
	    			postlist.add(docID, frame);
	    		} 
			//Overwrites over the previous mapping -> previous postings
			index.put(biword,postlist);
	    	//If new word
	    	} else {
	    		postlist = new PostingsList();
	    		postlist.add(docID, biwordOffset);
			index.put(biword,postlist);
	    	}		
	}
	lastToken=token;
	lastTime=frame;
    }
    
    /**
     *  Inserts this Biwordtoken with its postingslist in the index.
     */
    public void insert(String biword, PostingsList postings) {
	//If word already indexed
    	if (index.containsKey(biword)) {
    		PostingsList postlist = index.get(biword);
		postlist.mergePostings(postings);
		//Overwrites over the previous mapping -> previous postings
		index.put(biword,postlist);
    	//If new word
    	} else {
		index.put(biword,postings);
    	}
    }


    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
	Iterator<String> itr = index.keySet().iterator();
		return itr;
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
	PostingsList list=index.get(token);
	return list;
    }

    /**
     *  Returns the size of the index.
     */
    public int getSize() {
	return index.size();
    }

    /**
     *  Returns the index in a TreeMap to have it in order.
     */
    public TreeMap<String,PostingsList> order() {
	TreeMap<String,PostingsList> map = new TreeMap<String,PostingsList>(index);
	return map;
    }

    /**
     *  Returns if the BIWORD is in the index or not yet.
     */
    public boolean containsToken( String biword) {
    	return index.containsKey(biword);
    }

    /**
     *  Searches the index for postings matching the query using biwords.
     */
    public PostingsList search(Query query, int queryType, int rankingType, int frameType, double weightPopularity, double[] popularityScores, int distanceFrames, boolean optimization, double idf_threshold, boolean addition, double weight_addition) {
	System.out.println("size:"+query.terms.size());
	Query q = query.copy();
	if (q.terms.size() < 2) {
		return null;
	} 

	//Processing the query to have it in bigram
	LinkedList<String> t = new LinkedList<String>();
	LinkedList<Double> w = new LinkedList<Double>();
	for (int i =0; i<q.terms.size()-1;i++) {
		t.add(q.terms.get(i) + " " + q.terms.get(i+1));
		w.add((q.weights.get(i) + q.weights.get(i+1))/2);
	} 
    	
	for (int i =0; i<w.size()-1;i++) {
		System.out.println("query:"+ w.get(i));
	} 
    	
	//Ranked query
    	if (queryType == Index.RANKED_QUERY) {
		//Initialization of variables
		PostingsList answer=new PostingsList();
		HashMap<String,Double> scores=new HashMap<String,Double>();
		int nbDoc = docLengths.size();
	
    		if (rankingType == Index.TF_IDF) {
			//Get time to see how long program takes
			long initTime=System.nanoTime();	

			//Add tf-idf score for each document
	    		while (t.size() != 0){
				String term = t.remove();
				double weight = w.remove();
	    			PostingsList posts = getPostings(term);
				if (posts != null && posts.size() !=0) {
	    				scores = posts.addIdfScore(scores, term, weight, nbDoc, optimization, idf_threshold, addition, weight_addition);
				}
	    		}
	    	    	//Divide scores by docLength
			Iterator<String> keys=scores.keySet().iterator();
	    	    	while(keys.hasNext()){
	    	    		String docIDStr =keys.next();
	    	    		Integer docLength= docLengths.get(docIDStr);
	    	    	    	Double curScore=scores.get(docIDStr);
	    	    	    	double finalScore=curScore/(double)docLength;
	    	    	    	answer.addAnswer(Integer.parseInt(docIDStr),finalScore);
    	    		}
			answer.sortScore();

			//Print time program took
			long timeTaken=System.nanoTime()-initTime;
			System.out.println("Time taken:"+timeTaken+"ns");
	    		return answer;

    		} else if (rankingType == Index.PAGERANK) {
			//Add pageRank for each document
			while (t.size() != 0){
				String term = t.remove();
	    			PostingsList posts = getPostings(term);
				if (posts != null) {
	    				scores = posts.addPopularity(popularityScores, docIDs);
				}
	    		}

			Iterator<String> keys=scores.keySet().iterator();
		    	while(keys.hasNext()){
		    		String docIDStr=keys.next();
		    	    	Double pageRank=scores.get(docIDStr);
		    	    	answer.addAnswer(Integer.parseInt(docIDStr),pageRank);
		    	}
  			answer.sortScore();
			return answer;

    		} else if (rankingType == Index.COMBINATION) {
			//First calculate the tf-idf score
			while (t.size() != 0){
				String term = t.remove();
				double weight = w.remove();
	    			PostingsList posts = getPostings(term);
				if (posts != null) {
	    				scores = posts.addIdfScore(scores, term, weight, nbDoc, optimization, idf_threshold, addition, weight_addition);
				}
	    		}
			Iterator<String> keys=scores.keySet().iterator();
	    	    	while(keys.hasNext()){
	    	    		String docIDStr =keys.next();
	    	    		Integer docLength= docLengths.get(docIDStr);
	    	    	    	Double curScore=scores.get(docIDStr);
	    	    	    	double finalScore=curScore/(double)docLength;
	    	    	    	answer.addAnswer(Integer.parseInt(docIDStr),finalScore);
    	    		}
			answer.sortScore();

			//Then add combination with pageRank
			answer.addCombinationScore(weightPopularity, popularityScores, docIDs);
			answer.sortScore();
			return answer;

    		} else {
    			System.err.println("Unrecognized ranking type");
    		}

	//Problem of request
    	} else {
    		System.err.println("Query type not correct for bigram");
    		return null;
    	}
	return null;
    }

    /**
     *  No need for cleanup in a BiwordIndex.
     */
    public void cleanup() {
	index.clear();
    }
}
