/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.io.IOException;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;

public class Query {
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();

    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
	StringTokenizer tok = new StringTokenizer( queryString );
	while ( tok.hasMoreTokens() ) {
	    terms.add( tok.nextToken() );
	    weights.add( new Double(1) );
	}    
    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {
	return terms.size();
    }
    
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
	Query queryCopy = new Query();
	queryCopy.terms = (LinkedList<String>) terms.clone();
	queryCopy.weights = (LinkedList<Double>) weights.clone();
	return queryCopy;
    }
    
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Index index, double alpha, double beta, double thresholdProbability, boolean extended) {
	//Initialization of the variables
	HashMap<String,Double> termScores=new HashMap<String,Double>();

	//Calculate the number of relevant documents
	int nbRelevantDocs=0;
	int loops = 0;
	for(int i=0;i<docIsRelevant.length;i++){
		if(docIsRelevant[i]){
			nbRelevantDocs++;
			loops++;
		}
	}
	
	int nbDocs=index.docIDs.size();
	
	int i =0;
	while (loops>0){
		if(docIsRelevant[i]){
			loops--;
			//Get file
			int docID=results.get(i).docID;
			String docPath=index.docIDs.get(Integer.toString(docID));

			//Read the file
			File f=new File(docPath);
			SimpleTokenizer tok = Indexer.getFileText(f, thresholdProbability, extended);

			LinkedList<String> termsText = new LinkedList<String>();

			try {
				while ( tok.hasMoreTokens() ) {
					//Look at all the terms of the text
					String term=tok.nextToken();

					if (!termsText.contains(term)) {
					termsText.add(term);
					PostingsList postingsList=index.getPostings(term);
					double termIDF=Math.log((double)nbDocs/(double)postingsList.size());
						
					for (int j =0; j< postingsList.size(); j++) {
						if(postingsList.get(j).docID==docID){
							//Calculate the tf_idf score for the term
							int tf = postingsList.get(j).getSizePos();
							double tf_idf=tf*termIDF;
							int docLength=index.docLengths.get(Integer.toString(docID));
							double finalScore=tf_idf/(double)docLength;

							//Multiply with beta and divide by number of relevant
							finalScore=(finalScore*beta)/(double)nbRelevantDocs;
							termScores.put(term,finalScore);
							break;
						}
					}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		i++;
	}

	//For all terms that were in previous query, add their weigth
	for (int j =0;j<terms.size();j++) {
		double newScore =0;
		if (termScores.get(terms.get(j)) == null) {
			newScore=weights.get(j)*alpha;
		}
		else {
			newScore=weights.get(j)*alpha+termScores.get(terms.get(j));
		}
		termScores.put(terms.get(j),newScore);
	}

	//Clear the old term and weight lists
	terms.clear();
	weights.clear();

	//Then add the new terms and their weights
	Set<String> newTerms=termScores.keySet();
	int nbTerms=termScores.keySet().size();
	for(String newTerm:newTerms){
		double termScore=termScores.get(newTerm);
		//Normalize it
		termScore=termScore/(double)nbTerms;
		//Add term and weight
		terms.add(newTerm);
		weights.add(termScore);
	}

    }
}

    
