/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 *   Second version: Laura Jacquemod, 2015
 */  


package ir;

import java.io.File;
import java.util.LinkedList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *   A graphical interface to the information retrieval system.
 */
public class SearchGUI extends JFrame {
	//VARIABLES TO CHOOSE:
	double weightPopularity = 0.5;
	double alpha = 1;
	double beta = 0.5;
	double thresholdProbability = -10;
	double[] popularityScores ={0.3, 0.3, 0.3};
	int distanceFrames = 3;
	//For ranked retrieval
	boolean optimization = false;    
	double idf_threshold = 0.0001;
	//For additional info (title, description,...)
	boolean addition = false;
	double weight_addition = 0.2;
	
    /**  The indexer creating the search index. */
    Indexer indexer = new Indexer();

    /**  The query posed by the user, used in search() and relevanceFeedbackSearch() */
    private Query query; 
	
    /**  The returned documents, used in search() and relevanceFeedbackSearch() */
    private PostingsList results; 
	
    /**  Directories that should be indexed. */
    LinkedList<String> dirNames = new LinkedList<String>();

    /**  The query type (either intersection, phrase, or ranked). */
    int queryType = Index.INTERSECTION_QUERY;

    /**  The ranking type (either tf-idf, pagerank, or combination). */
    int rankingType = Index.TF_IDF;
		
    /**  The word structure type (either unigram, bigram, or subphrase). */
    int structureType = Index.UNIGRAM;
	
    /**  The searching structure type (either all frames, or close frames.). */
    int frameType = Index.ALLFRAMES;
    
    /**  Lock to prevent simultaneous access to the index. */
    Object indexLock = new Object();

    /*
     *   The nice logotype
     *   Generated at http://neswork.com/logo-generator/google-font
     */
    static final String LOGOPIC = "Videoquery.png";
    static final String BLANKPIC = "blank.jpg";
	

    /*  
     *   Common GUI resources
     */
    public JTextField queryWindow = new JTextField( "", 28 );
    public JTextArea resultWindow = new JTextArea( "", 23, 28 );
    private JScrollPane resultPane = new JScrollPane( resultWindow );
    private Font queryFont = new Font( "Arial", Font.BOLD, 24 );
    private Font resultFont = new Font( "Arial", Font.BOLD, 16 );
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu( "File" );
    JMenu optionsMenu = new JMenu( "Search options" );
    JMenu rankingMenu = new JMenu( "Ranking score" ); 
    JMenu structureMenu = new JMenu( "Text structure" ); 
    JMenu frameMenu = new JMenu( "Type of search" ); 
    JMenuItem quitItem = new JMenuItem( "Quit" );
    JRadioButtonMenuItem intersectionItem = new JRadioButtonMenuItem( "Intersection query" );
    JRadioButtonMenuItem phraseItem = new JRadioButtonMenuItem( "Phrase query" );
    JRadioButtonMenuItem rankedItem = new JRadioButtonMenuItem( "Ranked retrieval" );
    JRadioButtonMenuItem tfidfItem = new JRadioButtonMenuItem( "tf-idf" );
    JRadioButtonMenuItem pagerankItem = new JRadioButtonMenuItem( "Popularity" );
    JRadioButtonMenuItem combinationItem = new JRadioButtonMenuItem( "Combination" );
    JRadioButtonMenuItem closeFrameItem = new JRadioButtonMenuItem( "Close Frames" );
    JRadioButtonMenuItem allFramesItem = new JRadioButtonMenuItem( "All Frames" );
    JRadioButtonMenuItem unigramItem = new JRadioButtonMenuItem( "Short Descriptor" );
    JRadioButtonMenuItem bigramItem = new JRadioButtonMenuItem( "Extended Descriptor" );
    //JRadioButtonMenuItem subphraseItem = new JRadioButtonMenuItem( "Subphrase" );
    ButtonGroup queries = new ButtonGroup();
    ButtonGroup ranking = new ButtonGroup(); 
    ButtonGroup structure = new ButtonGroup();
    ButtonGroup frame = new ButtonGroup();
    public JPanel feedbackBar = new JPanel(); 
    JCheckBox[] feedbackButton = new JCheckBox[10];
    JToggleButton feedbackExecutor = new JToggleButton("New search"); 


    /* ----------------------------------------------- */


    /*
     *   Create the GUI.
     */
    private void createGUI() {
	// GUI definition
	setSize( 600, 650 );
	setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	JPanel p = new JPanel();
	p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
	getContentPane().add(p, BorderLayout.CENTER);
	// Top menu
	menuBar.add( fileMenu );
	menuBar.add( optionsMenu );
	menuBar.add( rankingMenu );
	menuBar.add( structureMenu );
	menuBar.add(frameMenu);
	fileMenu.add( quitItem );
	optionsMenu.add( intersectionItem );
	optionsMenu.add( phraseItem );
	optionsMenu.add( rankedItem );
	rankingMenu.add( tfidfItem ); 
	rankingMenu.add( pagerankItem ); 
	rankingMenu.add( combinationItem ); 
	structureMenu.add( unigramItem ); 
	structureMenu.add( bigramItem );
	frameMenu.add(closeFrameItem);
	frameMenu.add(allFramesItem);
	//structureMenu.add( subphraseItem ); 
	queries.add( intersectionItem );
	queries.add( phraseItem );
	queries.add( rankedItem );
	ranking.add( tfidfItem ); 
	ranking.add( pagerankItem );
	ranking.add( combinationItem ); 
	structure.add( unigramItem ); 
	structure.add( bigramItem ); 
	frame.add( closeFrameItem);
	frame.add(allFramesItem);
	//structure.add( subphraseItem ); 
	intersectionItem.setSelected( true );
	tfidfItem.setSelected( true );
	unigramItem.setSelected( true );
	allFramesItem.setSelected(true);
	p.add( menuBar );
	// Logo
	JPanel p1 = new JPanel();
	p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
	p1.add( new JLabel( new ImageIcon( LOGOPIC )));
	p.add( p1 );
	
	JPanel p3 = new JPanel();
	// Search box
	p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
	p3.add( new JLabel( new ImageIcon( BLANKPIC )));
	p3.add( queryWindow );
	queryWindow.setFont( queryFont );
	p3.add( new JLabel( new ImageIcon( BLANKPIC )));
	p.add( p3 );
	
	// Display area for search results
	p.add( resultPane );
	resultWindow.setFont( resultFont );
	// Relevance feedback
	for ( int i = 0; i<10; i++ ) {
	    feedbackButton[i] = new JCheckBox( i+"" );
	    feedbackBar.add( feedbackButton[i] ); 
	}
	feedbackBar.add( feedbackExecutor );
	p.add( feedbackBar ); 
	// Show the interface
	setVisible( true );
		
	Action search = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
		    // Normalize the search string and turn it into a Query
		    String queryString = SimpleTokenizer.normalize( queryWindow.getText() );
		    query = new Query( queryString );
		    // Search and print results. Access to the index is synchronized since
		    // we don't want to search at the same time we're indexing new files
		    // (this might corrupt the index).
		    synchronized ( indexLock ) {
			if (structureType == Index.UNIGRAM) {
			    	results = indexer.index.search(query, queryType, rankingType, frameType, weightPopularity, popularityScores, distanceFrames, optimization, idf_threshold, addition, weight_addition);
			} else if (structureType == Index.BIGRAM) {
				results = indexer.longIndex.search(query, queryType, rankingType, frameType, weightPopularity, popularityScores, distanceFrames, optimization, idf_threshold, addition, weight_addition);
		        } /*else if (structureType == Index.SUBPHRASE) {
				results = indexer.biwordIndex.search(query, queryType, rankingType, structureType,alphaWindow.getText());
				
			//Nothing to do if the list is long enough
				if (results.size() < indexer.nbDocsReturned) {
					PostingsList resultsUnigram = indexer.index.search(query, queryType, rankingType, Index.UNIGRAM,alphaWindow.getText());
					results = results.mergeScale(resultsUnigram, indexer.subphraseScale, indexer.nbDocsReturned);
				}
			}*/
		    }
		    StringBuffer buf = new StringBuffer();
		    if ( results != null ) {
			buf.append( "\nFound " + results.size() + " matching document(s)\n\n" );
			for ( int i=0; i<results.size(); i++ ) {
			    buf.append( " " + i + ". " );
			    String filename = indexer.index.docIDs.get( "" + results.get(i).docID );
			    if ( filename == null ) {
				buf.append( "" + results.get(i).docID );
			    }
			    else {
				buf.append( filename );
			    }
			    if ( queryType == Index.RANKED_QUERY ) {
				buf.append( "   " + String.format( "%.5f", results.get(i).score )); 
			    }
			 // Convert time in minutes + seconds & add it to the string
			    buf.append( "\n\tAt: " );
			    for(int offs: results.get(i).getPos()) {
			    	int minutes = offs / 60;
			    	int seconds = offs % 60;
			    	buf.append(String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + ", ");
			    }
			    buf.setLength(buf.length() - 2); // Remove the last extra comma
			    buf.append( "\n\n" );
			    
			    buf.append( "\n" );
			}
		    }
		    else {
			buf.append( "\nFound 0 matching document(s)\n\n" );
		    }
		    resultWindow.setText( buf.toString() );
		    resultWindow.setCaretPosition( 0 );
		}
	    };
	queryWindow.registerKeyboardAction( search,
					    "",
					    KeyStroke.getKeyStroke( "ENTER" ),
					    JComponent.WHEN_FOCUSED );
	
	Action relevanceFeedbackSearch = new AbstractAction() { 
		public void actionPerformed( ActionEvent e ) {
		    // Check that a ranked search has been made prior to the relevance feedback
		    StringBuffer buf = new StringBuffer();
		    if (( results != null ) && ( queryType == Index.RANKED_QUERY )) {
			// Read user relevance feedback selections
			boolean[] docIsRelevant = { false, false, false, false, false, false, false, false, false, false }; 
			for ( int i = 0; i<10; i++ ) {
			    docIsRelevant[i] = feedbackButton[i].isSelected(); 
			}
			// Expand the current search query with the documents marked as relevant 
			if (structureType == Index.UNIGRAM) {
				query.relevanceFeedback( results, docIsRelevant, indexer.index, alpha, beta, thresholdProbability, false );
		    } else if (structureType == Index.BIGRAM) {
		    	query.relevanceFeedback( results, docIsRelevant, indexer.longIndex, alpha, beta, thresholdProbability, true );
		    }
			
			
			// Perform a new search with the weighted and expanded query. Access to the index is 
			// synchronized since we don't want to search at the same time we're indexing new files
			// (this might corrupt the index).
			synchronized ( indexLock ) {
			    //If index stored in disk, delete the index in working memory if too
			    //large and anyway add the tokens and their postingslist to the index
			    //in the working memory
			    if (structureType == Index.UNIGRAM) {
			    	results = indexer.index.search(query, queryType, rankingType, frameType, weightPopularity, popularityScores, distanceFrames, optimization, idf_threshold, addition, weight_addition);
			    } else if (structureType == Index.BIGRAM) {
			    	results = indexer.longIndex.search(query, queryType, rankingType, frameType, weightPopularity, popularityScores, distanceFrames, optimization, idf_threshold, addition, weight_addition);
			    }
			}
			buf.append( "\nSearch after relevance feedback:\n" );
			buf.append( "\nFound " + results.size() + " matching document(s)\n\n" );
			for ( int i=0; i<results.size(); i++ ) {
			    buf.append( " " + i + ". " );
			    String filename = indexer.index.docIDs.get( "" + results.get(i).docID );
			    if ( filename == null ) {
				buf.append( "" + results.get(i).docID );
			    }
			    else {
				buf.append( filename );
			    }
			    buf.append( "   " + String.format( "%.5f", results.get(i).score ) + "\n" );
			}
		    }
		    else {
			buf.append( "\nThere was no returned ranked list to give feedback on.\n\n" );
		    }
		    resultWindow.setText( buf.toString() );
		    resultWindow.setCaretPosition( 0 );
		}
	    };
	feedbackExecutor.addActionListener( relevanceFeedbackSearch ); 	
	
	
	Action quit = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
		    System.exit( 0 );
		}
	    };
	quitItem.addActionListener( quit );

	
	Action setIntersectionQuery = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
		    queryType = Index.INTERSECTION_QUERY;
		}
	    };
	intersectionItem.addActionListener( setIntersectionQuery );
		
	Action setPhraseQuery = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
		    queryType = Index.PHRASE_QUERY;
		}
	    };
	phraseItem.addActionListener( setPhraseQuery );
		
	Action setRankedQuery = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
		    queryType = Index.RANKED_QUERY;
		}
	    };
	rankedItem.addActionListener( setRankedQuery );

	Action setTfidfRanking = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
			rankingType = Index.TF_IDF;
		}
		};
	tfidfItem.addActionListener( setTfidfRanking );
		
	Action setPagerankRanking = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
			rankingType = Index.PAGERANK;
		}
		};
	pagerankItem.addActionListener( setPagerankRanking );
		
	Action setCombinationRanking = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
			rankingType = Index.COMBINATION;
		}
		};
	combinationItem.addActionListener( setCombinationRanking );

	Action setUnigramStructure = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
			structureType = Index.UNIGRAM;
		}
		};
	unigramItem.addActionListener( setUnigramStructure );
		
	Action setBigramStructure = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
			structureType = Index.BIGRAM;
		}
		};
	bigramItem.addActionListener( setBigramStructure );
		
	/*
	Action setSubphraseStructure = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
			structureType = Index.SUBPHRASE;
		}
		};
	subphraseItem.addActionListener( setSubphraseStructure );*/	
	
	Action setCloseFrames = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
			frameType = Index.CLOSEFRAMES;
		}
		};
	closeFrameItem.addActionListener( setCloseFrames );
	
	Action setAllFrames = new AbstractAction() {
		public void actionPerformed( ActionEvent e ) {
			frameType = Index.ALLFRAMES;
		}
		};
	allFramesItem.addActionListener( setAllFrames );
	}


    /**
     *   Calls the indexer to index the chosen directory structure.
     *   Access to the index is synchronized since we don't want to 
     *   search at the same time we're indexing new files (this might 
     *   corrupt the index).
     */
    private void index() {
	synchronized ( indexLock ) {
	    resultWindow.setText( "\n  Indexing, please wait..." );
	    for (int i =0; i<dirNames.size(); i++) {
		File dokDir = new File( dirNames.get( i ));
		indexer.processFiles(dokDir, thresholdProbability);
	    }
 	    resultWindow.setText( "\n  Done!" );
	}
    }



    /* ----------------------------------------------- */


    /**
     *   Decodes the command line arguments.
     */
    private void decodeArgs( String[] args ) {
	int i=0;
	while ( i < args.length ) {
	    if ( "-d".equals( args[i] )) {
		i++;
		if ( i < args.length ) {
		    dirNames.add( args[i++] );
		}
	    }
	    else {
		System.err.println( "Unknown option: " + args[i] );
		break;
	    }
	}
    }				    

    /* ----------------------------------------------- */


    public static void main( String[] args ) {
		SearchGUI s = new SearchGUI();
		s.createGUI();
		s.decodeArgs(args);
		s.index();
    }

}
