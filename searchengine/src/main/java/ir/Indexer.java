/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Third version: Laura Jacquemod, 2015
 */  


package ir;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.io.IOException;

//Reading JSON files
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 *   Processes a directory structure and indexes all PDF and text files.
 */
public class Indexer {
	/*
    //For bigram and therefore subphrase, too much memore to index all texts
    //Choosing which texts (get ID) to index
    private int startDoc = 0;
    private int stopDoc = 18000;

    public int nbDocsReturned=20;
    public double subphraseScale=4;*/

	/** The index to be built up by this indexer. */
	public Index index;
	public Index longIndex;

	/** The next docID to be generated. */
	private int lastDocID = 0;
	
	/* ----------------------------------------------- */


	/** Generates a new document identifier as an integer. */
	private int generateDocID() {
		return lastDocID++;
	}


	/* ----------------------------------------------- */


	/**
	 *  Initializes the index as a HashedIndex.
	 */
	public Indexer() {
		index = new HashedIndex();
		longIndex = new HashedIndex();
	}


	/* ----------------------------------------------- */


	/**
	 *  Tokenizes and indexes the file @code{f}. If @code{f} is a directory,
	 *  all its files and subdirectories are recursively processed.
	 */
	public void processFiles( File f, double thresholdProbability ) {
		// do not try to index fs that cannot be read
		if ( f.canRead() ) {
			if ( f.isDirectory() ) {
				String[] fs = f.list();
				// an IO error could occur
				if ( fs != null ) {
					for ( int i=0; i<fs.length; i++ ) {
						processFiles( new File( f, fs[i] ),thresholdProbability);
					}
				}
			} else {
				//System.err.println( "Indexing " + f.getPath() );

				// First register the document and get a docID
				int docID = generateDocID();
				index.docIDs.put( "" + docID, f.getPath() );
				try {
					int length = 0 , extendedLength = 0;
					
					/***************************************************
					 *      GETTING INFORMATION FROM JSON FILE         *
					 *              (file with metadata)               *
					 **************************************************/

					//Getting the name of the file
					String file = "Metadata/" + f.getName().substring(0, f.getName().length() - 6) + "_m.txt";

					JSONParser parser = new JSONParser();
					
					Object obj = parser.parse(new FileReader(file));
					JSONObject jsonObject = (JSONObject) obj;

					JSONArray items = (JSONArray) jsonObject.get("items");

					JSONObject item = (JSONObject) items.get(0);
					JSONObject information = (JSONObject) item.get("snippet");
					
					String title = (String) information.get("title");
					title = title + " " + (String) information.get("description");
					
					Reader reader = new StringReader(title);
					
					SimpleTokenizer tok = new SimpleTokenizer( reader );
					int tokens = 0;
					while ( tok.hasMoreTokens() ) {
						String token = tok.nextToken();
						//Adding additional information to frame "-1"
						insertIntoIndex(index, docID, token, -1);
						insertIntoIndex(longIndex, docID, token, -1);
						tokens++;
					}
					length += tokens;
					reader.close();
					
					
					/***************************************************
					 *      GETTING INFORMATION FROM JSON FILE         *
					 *            (file with description)              *
					 **************************************************/
					
					obj = parser.parse(new FileReader(f));
					jsonObject = (JSONObject) obj;

					JSONArray listFrames = (JSONArray) jsonObject.get("imgblobs");
					
					for (int i = 0; i < listFrames.size(); ++i) {
						//Getting the information concerning the frame
						JSONObject frame = (JSONObject) listFrames.get(i);
						
						JSONObject candidate = (JSONObject) frame.get("candidate");
						double probability = (double) candidate.get("logprob");
						
						if (i == 1) {
							//Taking the amount of time between two frames
							//At i=0, t =0, so we have to look at second frame
							Double time = (Double) frame.get("time [s]");
							int timeFrame = time.intValue();
							index.docTimeFrame.put( "" + docID, timeFrame);
						}
						
						if (probability > thresholdProbability) {
							//If it is sure enough, we had info to index
							String description = (String) candidate.get("text");
							reader = new StringReader(description);
							Double time = (Double) frame.get("time [s]");
							int Nbframe = time.intValue();
							
							//Normal description
							tok = new SimpleTokenizer( reader );
							tokens = 0;
							while ( tok.hasMoreTokens() ) {
								String token = tok.nextToken();
								insertIntoIndex(index, docID, token, Nbframe);
								tokens++;
							}
							
							length += tokens;
							reader.close();
							
							//Extended description
							String extended = (String) frame.get("extended");
							Reader readerLong = new StringReader(extended);
							tok = new SimpleTokenizer( readerLong );
							tokens = 0;
							while ( tok.hasMoreTokens() ) {
								String token = tok.nextToken();
								insertIntoIndex(longIndex, docID, token, Nbframe);
								tokens++;
							}
							extendedLength += tokens;
							reader.close();
						}
					}
					index.docLengths.put( "" + docID, length );
					index.docLengthsExtended.put( "" + docID, extendedLength);
				}
				catch ( IOException e ) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 *  Tokenizes and gets the text of the file @code{f}.
	 * @throws ParseException 
	 **/
	public static SimpleTokenizer getFileText( File f, double thresholdProbability, boolean extended) {
		if ( f.canRead() ) {
			try {
				JSONParser parser = new JSONParser();
				Object obj = parser.parse(new FileReader(f));

				JSONObject jsonObject = (JSONObject) obj;

				JSONArray listFrames = (JSONArray) jsonObject.get("imgblobs");
				
				String description = "";
				for (int i = 0; i < listFrames.size(); ++i) {
					//Getting the information concerning the frame
					JSONObject frame = (JSONObject) listFrames.get(i);
					
					JSONObject candidate = (JSONObject) frame.get("candidate");
					double probability = (double) candidate.get("logprob");
					
					if (probability > thresholdProbability) {
						//If it is sure enough, we get info
						
						if (extended) {//Extended description
							description = description + " " + (String) frame.get("extended");
							}
						else {
							description = description + " " + (String) candidate.get("text");
						}
					}
				}
				Reader reader = new StringReader(description);
				SimpleTokenizer tok = new SimpleTokenizer( reader );
				return tok;
			}
			catch ( IOException e ) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} 
		return null;
	}



	/* ----------------------------------------------- */


	/**
	 *  Indexes one token.
	 */
	public void insertIntoIndex(Index index, int docID, String token, int offset ) {
		index.insert( token, docID, offset );
		//biwordIndex.insert( token, docID, offset );
	}

}

