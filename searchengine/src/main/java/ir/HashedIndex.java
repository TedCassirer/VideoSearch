/*  
 *   This file is part of the computer assignment for the 
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 *   Third version: Laura Jacquemod, 2015
 */  


package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

	/** The index as a hashtable. */
	public HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();

	/**
	 *  Inserts this token in the index.
	 */
	public void insert( String token, int docID, int frame) {
		//If word already indexed
		if (index.containsKey(token)) {
			PostingsList postlist = index.get(token);
			//If the word was already found in the document
			if (postlist.get(postlist.size()-1).docID == docID){
				postlist.addPosting(docID,frame);
			} else {
				//First occurence of the word in the document
				postlist.add(docID, frame);
			} 
			//Overwrites over the previous mapping -> previous postings
			index.put(token,postlist);
			//If new word
		} else {
			PostingsList postlist = new PostingsList();
			postlist.add(docID, frame);
			//Overwrites over the previous mapping -> previous postings
			index.put(token,postlist);
		}

	}

	/**
	 *  Inserts this token with its postingslist in the index.
	 */
	public void insert(String token, PostingsList postings) {
		//If word already indexed
		if (index.containsKey(token)) {
			PostingsList postlist = index.get(token);
			postlist.mergePostings(postings);
			//Overwrites over the previous mapping -> previous postings
			index.put(token,postlist);
			//If new word
		} else {
			index.put(token,postings);
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
	 *  Returns the postings for a specific term, or null
	 *  if the term is not in the index.
	 */
	public PostingsList getPostings( String token) {
		if (index.containsKey(token)) {
			return index.get(token);
		} else {
			return null;
		}
	}

	/**
	 *  Returns if the token is in the index or not yet.
	 */
	public boolean containsToken( String token) {
		return index.containsKey(token);
	}

	/**
	 *  Searches the index for postings matching the query.
	 */
	public PostingsList search(Query query, int queryType, int rankingType, int frameType, double weightPopularity, double[] popularityScores, int distanceFrames, boolean optimization, double idf_threshold, boolean addition, double weight_addition) {
		Query q = query.copy();
		LinkedList<String> t = q.terms;
		LinkedList<Double> w = q.weights;

		if (t.size() == 0) {
			return null;
		}

		//Intersection query
		if (queryType == Index.INTERSECTION_QUERY) {
			PostingsList intersect = getPostings(t.remove());
			
			if (intersect == null ) {intersect = new PostingsList();}
			while (t.size() != 0){
				PostingsList posts = getPostings(t.remove());
				if (posts == null) {
					return null;
				} else {
					if (frameType == Index.ALLFRAMES) {
						intersect  = intersect.intersection(posts,0,docTimeFrame);
					} else {
						intersect = intersect.intersection(posts,distanceFrames, docTimeFrame);
					}					
				}
			}
			if (! addition) {
				intersect.deleteAddition();
			}
			return intersect;

			//Phrase query
		} else if (queryType == Index.PHRASE_QUERY) {
			PostingsList phrase_answer = getPostings(t.remove());
			if (!addition) {
				phrase_answer.deleteAddition();
			}
			if (phrase_answer == null) {phrase_answer = new PostingsList();}
			while (t.size() != 0){
				PostingsList posts = getPostings(t.remove());
				if (posts == null) { 
					return null;
				} else { 
					if (!addition) {
						posts.deleteAddition();
					}
					phrase_answer = phrase_answer.phraseIntersect(posts);
				}
			}
			return phrase_answer;

			//Ranked query
		} else if (queryType == Index.RANKED_QUERY) {
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
					double docLengthSqrt= Math.sqrt(docLengths.get(docIDStr));
					Double curScore=scores.get(docIDStr);
					double finalScore=curScore/docLengthSqrt;
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
					double docLengthSqrt= Math.sqrt(docLengths.get(docIDStr));
					Double curScore=scores.get(docIDStr);
					double finalScore=curScore/docLengthSqrt;
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
			System.err.println("Unrecognized query type");
			return null;
		}
		return null;
	}

	/**
	 *  No need for cleanup in a HashedIndex.
	 */
	public void cleanup() {
		index.clear();
	}
}
