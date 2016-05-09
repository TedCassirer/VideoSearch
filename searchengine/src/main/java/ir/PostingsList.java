/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Third version. Laura Jacquemod, 2015
 */  

package ir;

import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Collections;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.lang.Math;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
	/** The postings list as a linked list. */
	private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();

	/**  Number of postings in this list  */
	public int size() {
		return list.size();
	}

	/**  Returns the ith posting */
	public PostingsEntry get( int i ) {
		return list.get( i );
	}

	/** Returns postingsentry with given docID or null if it doesn't exist **/ 
	public PostingsEntry getFromID(int docID) {
		int pos = 0;
		while (pos < this.size()) {
			if (this.get(pos).docID == docID) {
				return this.get(pos);
			}
			pos++;
		}
		return null;
	}
	/**  Returns the entire list of postings */
	public LinkedList<PostingsEntry> getList() {
		return list;
	}

	/** Copy the postingslist*/
	public PostingsList copy() {
		PostingsList postingsCopy = new PostingsList();
		postingsCopy.list = (LinkedList<PostingsEntry>) list.clone();
		return postingsCopy;
	}

	/** Checks if postingslist has a special docID**/
	public boolean hasID(int docID) {
		boolean result = false;
		int pos = 0;
		while (!result && pos < this.size()) {
			if (this.get(pos).docID == docID) {
				result = true;
			}
			pos++;
		}
		return result;
	}
	
	/**  Adds docID at the end of the list
	 * It is at the end since the documents are indexed
	 * recursively */
	public void add( int docID, int frame) {
		PostingsEntry ent = new PostingsEntry(docID,frame);
		list.addLast(ent);
	}

	/**  Adds docID at the end of the list
	 * It is at the end since the documents are indexed
	 * recursively */
	public void add( int docID, int frame, double score ) {
		PostingsEntry ent = new PostingsEntry(docID,frame, score);
		list.addLast(ent);
	}

	/**  Adds docID at the end of the list with no specific offset */    
	public void addAnswer( int docID, double score ) {
		PostingsEntry ent = new PostingsEntry(docID, 0, score);
		list.addLast(ent);
	} 

	/**  Adds postingsentry to the postingslist */    
	public void addEntry(PostingsEntry entry ) {
		list.addLast(entry);
	} 

	/**  Adds position at the end of the list */
	public void addPosting(int docID, int frame) {
		if (list.getLast().docID != docID) {
			//In case the docID is not the last one, should be
			System.err.println("Problem of indexing");
		} else {
			//Remove Postings entry in order to add to it the new offset
			PostingsEntry position = list.removeLast();
			position.getPos().add(frame);
			list.addLast(position);
		}
	}

	/**  Add a list of postings to an existing PostingsList */
	public void mergePostings(PostingsList postlist) {
		for (int i =0; i< postlist.size(); i++) {
			boolean result = false;
			int pos = 0;
			while (!result && pos < this.size()) {
				if (this.get(pos).docID == postlist.get(i).docID) {
					result = true;
				} else {
					pos++;
				}
			}
			
			if (result) {
				//DocID is already there
				for (int k=0; k < postlist.get(i).getSizePos();k++) {
					if (!this.get(pos).hasPos(postlist.get(i).getPos(k))){
						this.get(pos).addPos(postlist.get(i).getPos(k));
					}
				}
			} else {
				this.list.add(pos, postlist.get(i));
			}
		}
	}

	/**
	 *  Add a list of postings to an existing PostingsList with a special scale
	 *
	 */
	public PostingsList mergeScale(PostingsList postlist, double scale, int nbDocs) {
		for(int i=0;i<this.size();i++){
			this.get(i).changeScore(this.get(i).getScore()*scale);
		}

		for (int i =0; i< postlist.size(); i++) {
			list.addLast(postlist.get(i));
		}

		this.sortScore();

		PostingsList result = new PostingsList();
		for (int k = 0; k < nbDocs ; k++) {
			result.addEntry(this.get(k));
		}

		return result;
	}

	/* Computation of the cosine score for a certain term*/
	public HashMap<String,Double> addIdfScore(HashMap<String,Double> scores, PostingsList post, String term, double weight, int nbDocs, boolean optimization, double idf_threshold, boolean addition, double weight_addition){    	   
		//Deleting unrelevant information if additional information is not used
		if (!addition) {
			post.deleteAddition();
		}
		
		//Calculation of idft
		int dft=post.size();
		double termIDF=Math.log((double)nbDocs/(double)dft);  
		termIDF=termIDF*weight;

		//Implementation of optimization 
		if(optimization){
			//If IDF is less than the threshold, skip word
			//We skip by not adding scores to the hashmap
			if(termIDF < idf_threshold){
				return scores;	
			}
		}

		this.mergePostings(post);
		
		// For each document, add score
		for (int i=0; i < post.size(); i++) {
			PostingsEntry entry = post.get(i);
			double tf=(double)entry.getSizePos();
			double tf_idf=tf*termIDF;
			Double curScore=scores.get(""+entry.getDocID());
			
			//If addition information is taken into account
			if (addition) {
				int nbAdditional = 0;
				int pos = 0;	// Looking at all values of positions to see how many are additional
				while (entry.getPos(pos) == -1 && pos < entry.getSizePos()) {
					//If it was in additional info
					nbAdditional++;
					pos++;
				}
				//Updating the counting
				tf_idf *= (1 - (1-weight_addition)*nbAdditional/tf);
			}
			
			if(curScore==null){
				curScore=tf_idf;	
			}
			else{
				curScore=curScore+tf_idf;	
			}
			scores.put(""+entry.getDocID(),curScore);
		}
		return scores;
	}

	/** Sorting the postings list **/
	public void sortScore(){
		Collections.sort(list);
	}


	/** Enters the popularity scores for all documents required **/
	public HashMap<String,Double> addPopularity(PostingsList post, double[] popularityScores, HashMap<String,String> docIDs) {
		this.mergePostings(post);
		HashMap<String,Double> scores=new HashMap<String,Double>();
		for (int i=0; i < list.size(); i++) {
			//Getting the file with metadata
			String filename = docIDs.get(""+list.get(i).getDocID());
			double popularity = getPopularity(filename, popularityScores,docIDs);
			scores.put(""+list.get(i).getDocID(),popularity);	
		}
		return scores;
	}

	/** Calculating the popularity of a file **/
	public double getPopularity(String filename, double[] popularityScores, HashMap<String,String> docIDs) {
		int nameFile = filename.lastIndexOf("\\");
		int posFolder = filename.substring(0,nameFile).lastIndexOf("\\");
		int position = filename.lastIndexOf("d");
		String f = filename.substring(0,posFolder)+ "\\Metadata" +filename.substring(nameFile,position) +'m'+filename.substring(position+1);

		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader(f));
			JSONObject jsonObject = (JSONObject) obj;

			JSONArray items = (JSONArray) jsonObject.get("items");

			JSONObject item = (JSONObject) items.get(0);
			JSONObject stat = (JSONObject) item.get("statistics");

			//Calculating each part of the formula for popularity
			double firstPart = (10*Double.parseDouble((String) stat.get("favoriteCount")) + Double.parseDouble((String) stat.get("likeCount")) + Double.parseDouble((String) stat.get("dislikeCount")))/10.0;
			double secondPart = Double.parseDouble((String) stat.get("viewCount"));
			double thirdPart = Double.parseDouble((String) stat.get("commentCount"))/10.0;

			//Adding popularity
			double popularity = popularityScores[0] * Math.sqrt(firstPart) + popularityScores[1]* Math.cbrt(secondPart) + popularityScores[2] * Math.sqrt(thirdPart);
			return popularity;
		} catch (IOException | ParseException e) {
			return 0.0;
		}
	}

	/**
	 * Add combination score from a postingslist where idf score is already calculated
	 */
	public void addCombinationScore(double alpha, double[] popularityScores, HashMap<String,String> docIDs) {
		for (int i=0; i < list.size(); i++) {		
			double scoreIDF = list.get(i).getScore();
			String filename = docIDs.get(""+ list.get(i).getDocID());
			double popularity = getPopularity(filename, popularityScores,docIDs);

			double newScore = alpha*scoreIDF + (1-alpha) * popularity / 1000;
			list.get(i).changeScore(newScore);
		}
	}

	/**
	 * Finding the intersection of query terms
	 */
	public PostingsList intersection(PostingsList otherList, int distanceFrames,HashMap<String,Double> docTimeFrame){
		if (list.size() == 0) {
			return this;
		}

		//Initialization of the answer as a Postingslist
		PostingsList answer = new PostingsList();

		//Getting and cloning the two postingslist to intersect
		LinkedList<PostingsEntry> temp = new LinkedList<PostingsEntry>();
		temp = (LinkedList<PostingsEntry>) list.clone();
		LinkedList<PostingsEntry> other = new LinkedList<PostingsEntry>();
		other = (LinkedList<PostingsEntry>) otherList.getList().clone();
		
		//Getting the first postings entry
		PostingsEntry entry1 = temp.remove();
		PostingsEntry entry2 = other.remove();
		
		while (temp.size() != 0 && other.size() != 0){
			if (entry1.docID == entry2.docID) {
				if (distanceFrames == 0) {
					//Same document -> add to answer and remove both entries
					//Different document -> delete the smallest entry
					entry1.mergePos(entry2.getPos());
					PostingsEntry toAdd = new PostingsEntry(entry1.docID,entry1.getPos(),entry1.score);
					answer.addEntry(toAdd);
					entry1 = temp.remove();
					entry2 = other.remove();
				} else  {
					//Checking that it is in close frames
					LinkedList<Integer> position1 = entry1.getPos();
					LinkedList<Integer> position2 = entry2.getPos();
					boolean first = true;
					
					double timeFrame = docTimeFrame.get("" + entry1.docID);
					//Find if the second token is after the first one
					//for each repetition of the first token
					while (position1.size() > 0 ){
						while (position2.size() > 0) {
							if (Math.abs(position1.get(0) - position2.get(0)) < distanceFrames * timeFrame) {
								if (first) {
									answer.add(entry1.docID, position2.get(0), entry1.score);
									first = false;
								} else {
									answer.addPosting(entry1.docID, position2.get(0));
								}
								//Taking the smallest away, otherwise infinite loop
								if (position1.get(0) < position2.get(0)) {
									position1.removeFirst();
								} else { position2.removeFirst();}
								
							//If the times are too far, deleting the smallest one
							} else if (position1.get(0) - position2.get(0) > distanceFrames * timeFrame) {
								position2.removeFirst();
							} else if (position1.get(0) - position2.get(0) < distanceFrames * timeFrame) {
								position1.removeFirst();
							}
							
						}
					}
					entry1 = temp.remove();
					entry2 = other.remove();
				}
			//If documents are different, deleting the one with smallest ID
			} else if (entry1.docID < entry2.docID) {
				entry1 = temp.remove();
			} else {
				entry2 = other.remove();
			}
		}

		//One postingslist's size is equal to 0 only other entries to look at
		if (temp.size() == 0 || other.size() == 0) {
			if (entry1.docID == entry2.docID) {
				if (distanceFrames == 0) {
					entry1.mergePos(entry2.getPos());
					PostingsEntry toAdd = new PostingsEntry(entry1.docID,entry1.getPos(),entry1.score);
					answer.addEntry(toAdd);
				} else  {
					//Checking that it is in close frames
					LinkedList<Integer> position1 = entry1.getPos();
					LinkedList<Integer> position2 = entry2.getPos();
					boolean first = true;
					
					double timeFrame = docTimeFrame.get("" + entry1.docID);
					//Find if the second token is after the first one
					//for each repetition of the first token
					while (position1.size() > 0 ){
						while (position2.size() > 0) {
							if (Math.abs(position1.get(0) - position2.get(0)) < distanceFrames * timeFrame) {
								if (first) {
									answer.add(entry1.docID, position2.get(0), entry1.score);
									first = false;
								} else {
									answer.addPosting(entry1.docID, position2.get(0));
								}
								//Taking the smallest away, otherwise infinite loop
								if (position1.get(0) < position2.get(0)) {
									position1.removeFirst();
								} else { position2.removeFirst();}
								
							//If the times are too far, deleting the smallest one
							} else if (position1.get(0) - position2.get(0) > distanceFrames * timeFrame) {
								position2.removeFirst();
							} else if (position1.get(0) - position2.get(0) < distanceFrames * timeFrame) {
								position1.removeFirst();
							}
							
						}
					}
				}	
			}
			else if (entry1.docID < entry2.docID) {
				while (temp.size() != 0) {
					entry1 = temp.remove();
					if (entry1.docID == entry2.docID) {
						if (distanceFrames == 0) {
							//Same document -> add to answer and remove both entries
							//Different document -> delete the smallest entry
							entry1.mergePos(entry2.getPos());
							PostingsEntry toAdd = new PostingsEntry(entry1.docID,entry1.getPos(),entry1.score);
							answer.addEntry(toAdd);
						} else  {
							//Checking that it is in close frames
							LinkedList<Integer> position1 = entry1.getPos();
							LinkedList<Integer> position2 = entry2.getPos();
							boolean first = true;
							
							double timeFrame = docTimeFrame.get("" + entry1.docID);
							//Find if the second token is after the first one
							//for each repetition of the first token
							while (position1.size() > 0 ){
								while (position2.size() > 0) {
									if (Math.abs(position1.get(0) - position2.get(0)) < distanceFrames * timeFrame) {
										if (first) {
											answer.add(entry1.docID, position2.get(0), entry1.score);
											first = false;
										} else {
											answer.addPosting(entry1.docID, position2.get(0));
										}
										//Taking the smallest away, otherwise infinite loop
										if (position1.get(0) < position2.get(0)) {
											position1.removeFirst();
										} else { position2.removeFirst();}
										
									//If the times are too far, deleting the smallest one
									} else if (position1.get(0) - position2.get(0) > distanceFrames * timeFrame) {
										position2.removeFirst();
									} else if (position1.get(0) - position2.get(0) < distanceFrames * timeFrame) {
										position1.removeFirst();
									}
								}
							}
						}
						break;
						//end the other will be bigger then
					}
				}
			} else {
				while (other.size() != 0) {
					entry2 = other.remove();
					if (entry1.docID == entry2.docID) {
						if (distanceFrames == 0) {
							//Same document -> add to answer and remove both entries
							//Different document -> delete the smallest entry
							entry1.mergePos(entry2.getPos());
							PostingsEntry toAdd = new PostingsEntry(entry1.docID,entry1.getPos(),entry1.score);
							answer.addEntry(toAdd);
						} else  {
							//Checking that it is in close frames
							LinkedList<Integer> position1 = entry1.getPos();
							LinkedList<Integer> position2 = entry2.getPos();
							boolean first = true;
							
							double timeFrame = docTimeFrame.get("" + entry1.docID);
							//Find if the second token is after the first one
							//for each repetition of the first token
							while (position1.size() > 0 ){
								while (position2.size() > 0) {
									if (Math.abs(position1.get(0) - position2.get(0)) < distanceFrames * timeFrame) {
										if (first) {
											answer.add(entry1.docID, position2.get(0), entry1.score);
											first = false;
										} else {
											answer.addPosting(entry1.docID, position2.get(0));
										}
										//Taking the smallest away, otherwise infinite loop
										if (position1.get(0) < position2.get(0)) {
											position1.removeFirst();
										} else { position2.removeFirst();}
										
									//If the times are too far, deleting the smallest one
									} else if (position1.get(0) - position2.get(0) > distanceFrames * timeFrame) {
										position2.removeFirst();
									} else if (position1.get(0) - position2.get(0) < distanceFrames * timeFrame) {
										position1.removeFirst();
									}		
								}
							}
						}
						break;
						//end the other will be bigger then
					}
				}
			}
		}
		return answer;
	}

	/**
	 * Finding the phrase intersection of query terms
	 */
	public PostingsList phraseIntersect(PostingsList otherList){
		if (list.size() == 0) {
			return this;
		}

		//Initialization of the answer as a Postingslist
		PostingsList answer = new PostingsList();

		//Getting and cloning the two postingslist to intersect
		LinkedList<PostingsEntry> temp = new LinkedList<PostingsEntry>();
		temp = (LinkedList<PostingsEntry>) list.clone();
		LinkedList<PostingsEntry> other = new LinkedList<PostingsEntry>();
		other = (LinkedList<PostingsEntry>) otherList.getList().clone();

		//Getting the first postings entry
		PostingsEntry entry1 = temp.remove();
		PostingsEntry entry2 = other.remove();

		while (temp.size() != 0 && other.size() != 0){
			//Same docID -> Search if it is following words or not
			if (entry1.docID == entry2.docID) {
				//Getting the positions for each entry
				LinkedList<Integer> position1 = entry1.getPos();
				LinkedList<Integer> position2 = entry2.getPos();
				boolean first = true;

				//Looking at the shortest list of positions
				if (position1.size() <= position2.size()) {
					//Find if the second token is after the first one
					//for each repetition of the first token
					for (int i = 0; i < position1.size(); i++) {
						if (position2.contains(position1.get(i))) {
							if (first) {
								answer.add(entry1.docID, position1.get(i), entry1.score);
								first = false;
							} else {
								answer.addPosting(entry1.docID, position1.get(i));
							}
						}
					}
				} else {
					//Same thing but position2 is shorter
					for (int i = 0; i < position2.size(); i++) {
						//Find if the first token is before the second one
						//for each repetition of the second token
						if (position1.contains(position2.get(i))) {
							if (first) {
								answer.add(entry1.docID, position2.get(i), entry1.score);
								first = false;
							} else {
								answer.addPosting(entry1.docID, position2.get(i));
							}
						}
					}
				}
				entry1 = temp.remove();
				entry2 = other.remove();

				//Otherwise, remove the entry with the smallest docID
			} else if (entry1.docID < entry2.docID) {
				entry1 = temp.remove();
			} else {
				entry2 = other.remove();
			}
		}

		if (entry1.docID == entry2.docID) {
			//Same thing, to make sure the last entries are treated
			LinkedList<Integer> position1 = entry1.getPos();
			LinkedList<Integer> position2 = entry2.getPos();
			boolean first = true;
			if (position1.size() <= position2.size()) {
				for (int i = 0; i < position1.size(); i++) {
					if (position2.contains(position1.get(i))) {
						if (first) {
							answer.add(entry1.docID, position1.get(i), entry1.score);    						first = false;
						} else {
							answer.addPosting(entry1.docID, position1.get(i));    						
						}
					}
				}
			} else {
				for (int i = 0; i < position2.size(); i++) {
					if (position1.contains(position2.get(i))) {
						if (first) {
							answer.add(entry1.docID, position2.get(i), entry1.score);
							first = false;
						} else {
							answer.addPosting(entry1.docID, position2.get(i));
						}
					}
				}
			}
		}

		return answer;
	}
	
	/** Delete all the information that was in the additional information **/
	public void deleteAddition() {
		LinkedList<Integer> toDelete = new LinkedList<Integer>();
		for (int i =0; i< this.size(); i++) {
			//Look at all the documents
			PostingsEntry entry = this.get(i);
			int pos = entry.getFirstPos();
			while (pos == -1) {
				//If it was in additional info
				entry.deleteFirstPos();
				if (entry.getSizePos() > 0) {
					pos = entry.getFirstPos();
				} else {
					pos = 0;
				}
			}
			//If word was only in the additional part
			//PostingsEntry to delete
			if (entry.getSizePos() == 0) {
				toDelete.add(i);
			}
		}
		for (int i = toDelete.size()-1; i >=0 ; i--) {
			int delete = toDelete.get(i);
			this.list.remove(delete);
		}
	}
}



