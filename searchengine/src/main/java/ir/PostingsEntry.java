/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.LinkedList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public double score;
    /** The postings in a document as a linked list. */
    private LinkedList<Integer> pos;


    public PostingsEntry(int docID) {
    	this.docID = docID;
    	this.score = 1;
    	this.pos = new LinkedList<Integer>();
    }
    
    public PostingsEntry(int docID,  LinkedList<Integer> pos) {
    	this.docID = docID;
    	this.pos = pos;
    	this.score = 1;
    }
    
    public PostingsEntry(int docID, int posting) {
    	this.docID = docID;
    	LinkedList<Integer> liste = new LinkedList<Integer>();
    	liste.add(posting);
    	this.pos = liste;
    	this.score = 1;
    }
    
    public PostingsEntry(int docID,  LinkedList<Integer> list, double score) {
    	this.docID = docID;
    	this.pos = list;
    	this.score = score;
    }
    
    public PostingsEntry(int docID, int posting, double score) {
    	this.docID = docID;
    	LinkedList<Integer> liste = new LinkedList<Integer>();
    	liste.add(posting);
    	this.pos = liste;
    	this.score = score;
    }
    

    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
	return Double.compare( other.score, score );
    }

    public int getDocID() {
    	return docID;
    }

    public double getScore() {
    	return score;
    }
    
    public LinkedList<Integer> getPos() {
    	return pos;
    }

    public int getPos(int i) {
    	return pos.get(i);
    }

    public int getFirstPos() {
    	return pos.getFirst();
    }

    public int getSizePos() {
    	return pos.size();
    }

    public void changePos(LinkedList<Integer> liste) {
    	pos = liste;
    }

    public void addPos(int position) {
    	pos.add(position);
    }
    
    public void deleteFirstPos() {
    	pos.removeFirst();
    }

    public void changeScore(double score) {
    	this.score = score;
    }
}

    
