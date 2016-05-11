/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  

package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.LinkedList;

public interface Index {

    /* Index types */
    public static final int HASHED_INDEX = 0;

    /* Query types */
    public static final int INTERSECTION_QUERY = 0;
    public static final int PHRASE_QUERY = 1;
    public static final int RANKED_QUERY = 2;
	
    /* Ranking types */
    public static final int TF_IDF = 0; 
    public static final int PAGERANK = 1; 
    public static final int COMBINATION = 2; 

    /* Structure types */
    public static final int UNIGRAM = 0; 
    public static final int BIGRAM = 1; 
    public static final int SUBPHRASE = 2; 
	
    /* Searching types */
    public static final int ALLFRAMES = 0; 
    public static final int CLOSEFRAMES = 1; 

    public HashMap<String, String> docIDs = new HashMap<String,String>();
    public HashMap<String,Integer> docLengths = new HashMap<String,Integer>();
    public HashMap<String,Integer> docLengthsExtended = new HashMap<String,Integer>();
    public HashMap<String,Double> docTimeFrame = new HashMap<String,Double>();
    public LinkedList<Double> pageRank = new LinkedList<Double>();

    public void insert( String token, int docID, int frame);
    public void insert(String token, PostingsList postings);
    public Iterator<String> getDictionary();
    public TreeMap<String,PostingsList> order();
    public PostingsList getPostings( String token );
    public boolean containsToken (String token);
    public int getSize();
    public PostingsList search( Query query, int queryType, int rankingType, int frameType, double weightPopularity, double[] popularityScores, int distanceFrames, boolean optimization, double idf_threshold, boolean addition, double sweight_addition);
    public void cleanup();

}
		    
