package am.app;

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Model;

import am.AMException;
import am.app.mappingEngine.AbstractMatcher;
import am.app.mappingEngine.Alignment;
import am.app.mappingEngine.MatcherChangeEvent;
import am.app.mappingEngine.MatcherChangeListener;
import am.app.mappingEngine.MatchersRegistry;
import am.app.ontology.Node;
import am.app.ontology.Ontology;
import am.app.ontology.OntologyChangeEvent;
import am.app.ontology.OntologyChangeListener;
import am.userInterface.UI;

/**
 * SINGLETON JAVA PATTERN
 * There will be only one instance of this class in the whole application
 * All classes can access it without having any reference to it, just invoking the static method
 * Core.getIstance()
 * All model information of the system will be accessible from this class:
 * ontologies, matchings and so on.
 *
 */
public class Core {
	
	// Program wide DEBUG flag.
	public static final boolean DEBUG = false;
	
	
	/**List of matchers instances run by the user
	 * Data of the tableModel of the matcherTable is taken from this structure
	 * Can't be null;
	 */
	
	private ArrayList<AbstractMatcher> matcherInstances = new ArrayList<AbstractMatcher>();
	
	private int IDcounter = 0;  // used in generating IDs for the ontologies and matchers
	public static final int ID_NONE = -1;  // the ID for when no ID has been set
	
	private Ontology sourceOntology; //Null if it's not loaded yet
	private Ontology targetOntology; //Null if it's not loaded yet
	
	/**
	 * The ArrayList of ontologies that are currently loaded into the AgreementMaker.
	 * This adds support for more than two ontologies to be loaded into the AgreementMaker
	 * 
	 * Also, when an ontology is loaded, we need to let the GUI know, so we have an OntologyChangeListener interface.
	 * Interested parts of our code can register as an OntologyChangeListener of the core, by calling:
	 * 		- addChangeListener()
	 * 		- or removeChangeListener() to remove from the listener list
	 */
	private ArrayList<Ontology> loadedOntologies;
	private ArrayList<OntologyChangeListener> ontologyListeners;
	private ArrayList<MatcherChangeListener>  matcherListeners;
	
	
	/**A reference to the userinterface instance, canvas and table can be accessed anytime. It used often to invoke the method redisplayCanvas()*/
	private static UI ui;
	
	/**
	 * Singleton pattern: unique instance
	 */
	private static Core core  = new Core();
	/**
	 * 
	 * @return the unique instance of the core
	 */
	public static Core getInstance() {
		return core;
	}
	/**
	 * It's private because it's not possible to create new instances of this class
	 */
	private Core() {
		loadedOntologies = new ArrayList<Ontology>();  // initialize the arraylist of ontologies.
		ontologyListeners    = new ArrayList<OntologyChangeListener>();  // new list of listeners
		matcherListeners	= new ArrayList<MatcherChangeListener>(); // another list of listeners
	}
	
    @Deprecated
	public Ontology getSourceOntology() {  // deprecated by multiple-ontology interface
		return sourceOntology;
	}
    
    @Deprecated
	public void setSourceOntology(Ontology sourceOntology) {  // deprecated by multi-ontology array
    	this.sourceOntology = sourceOntology;
		addOntology(sourceOntology); // support for more than 2 ontologies
	}
    
    @Deprecated
	public Ontology getTargetOntology() {   // deprecated by multi-ontology interface
		return targetOntology;
	}
    
    @Deprecated
	public void setTargetOntology(Ontology targetOntology) {  // deprecated by multi-ontology interface
		this.targetOntology = targetOntology;
		addOntology(targetOntology); // support for more than 2 ontologies
	}
	
	
	@Deprecated
	public boolean sourceIsLoaded() {
		return sourceOntology != null;
	}
	
	@Deprecated
	public boolean targetIsLoaded() {
		return targetOntology != null;
	}
	
	@Deprecated
	public boolean ontologiesLoaded() {
		return sourceIsLoaded() &&  targetIsLoaded();
	}
	
	
	/**
	 * This function will return the first Matcher instance of type "matcher" in the AM (ordered by its index).
	 */
	public AbstractMatcher getMatcherInstance( MatchersRegistry matcher ) {
	
		for( int i = 0; i < matcherInstances.size(); i++ ) {
			String m1 = matcherInstances.get(i).getName().getMatcherClass();
			String m2 = matcher.getMatcherClass();
			System.out.println("m1: " + m1);
			System.out.println("m2: " + m2);
			System.out.println( m1.equals(m2) );
			System.out.println( m1 == m2 );
			
			if( m1.equals(m2)  ) {
				return matcherInstances.get(i);
			}
			
		}
		
		return null;
		
	}
	
	public ArrayList<AbstractMatcher> getMatcherInstances() {
		return matcherInstances;
	}
	
	public AbstractMatcher getMatcherByID( int mID ) {
		Iterator<AbstractMatcher> matchIter = matcherInstances.iterator();
		while( matchIter.hasNext() ) {
			AbstractMatcher a = matchIter.next();
			if( a.getID() == mID ) { return a; }
		}
		return null;
	}
	
	// this method adds a matcher to the end of the matchers list.
	public void addMatcherInstance(AbstractMatcher a) {
		a.setIndex( matcherInstances.size() );
		matcherInstances.add(a);
		fireEvent( new MatcherChangeEvent(a, MatcherChangeEvent.EventType.MATCHER_ADDED, a.getID() ));
	}
	
	public void removeMatcher(AbstractMatcher a) {
		MatcherChangeEvent evt = new MatcherChangeEvent(this, MatcherChangeEvent.EventType.MATCHER_REMOVED, a.getID());
		int myIndex = a.getIndex();
		matcherInstances.remove(myIndex);
		//All indexes must be decreased by one;
		//For this reason whenever you have to delete more then one matcher, it's good to start from the last in the order
		// TODO: The above behavior should be fixed - cosmin
		AbstractMatcher next;
		for(int i = myIndex; i<matcherInstances.size(); i++) {
			next = matcherInstances.get(i);
			next.setIndex(i);
		}
		fireEvent(evt);
		
	}
	
	public static UI   getUI()      { return ui;    }
	public static void setUI(UI ui) { Core.ui = ui; }
	
	/**
	 * Some selection parameters or some information in the alignMatrix of the matcher a are changed,
	 * so it's needed to invoke a.select() and also to update all other matchers with a as inputMatcher
	 * matchers are kept in chronological order in the list , so any matcher with a as input must be in a next entry of the list
	 * at the beginning there will be only one matcher in the modifiedList that is a
	 * anytime we find another matcher a2 with a as input we have to invoke a2.match() and we have to add a2 to the modifiedmatchings
	 * so that all other matchers with a and/or a2 as input will be updated to and so on.
	 * @param a the matcher that has been modified and generates a chain reaction on other matchings
	 * @throws AMException 
	 */
	public void selectAndUpdateMatchers(AbstractMatcher a) throws Exception{
		a.select();
		MatcherChangeEvent evt = new MatcherChangeEvent(this, MatcherChangeEvent.EventType.MATCHER_ALIGNMENTSET_UPDATED, a.getID() );
		fireEvent(evt);
		updateMatchers(a);

	}
	
	public void matchAndUpdateMatchers(AbstractMatcher a) throws Exception{
		a.match();
		MatcherChangeEvent evt = new MatcherChangeEvent(this, MatcherChangeEvent.EventType.MATCHER_ALIGNMENTSET_UPDATED, a.getID() );
		fireEvent(evt);
		updateMatchers(a);
	}
	
	private void updateMatchers(AbstractMatcher a) throws Exception {
		//Chain update of all matchers after a
		int startingIndex =  a.getIndex()+1;
		ArrayList<AbstractMatcher> modifiedMatchers = new ArrayList<AbstractMatcher>();
		modifiedMatchers.add(a);
		AbstractMatcher current;
		AbstractMatcher modified;
		for(int i = startingIndex; i < matcherInstances.size(); i++) {
			//for each matcher after a: current, we have to check if it contains any modified matchers as input
			current = matcherInstances.get(i);
			if(current.getMaxInputMatchers() > 0) {
				for(int j = 0; j < modifiedMatchers.size(); j++) { //scan modified matchers at the beginning is only a
					modified = modifiedMatchers.get(j);
					if(current.getInputMatchers().contains(modified)) {//if current contains any of the modified matchers as input matcher
						current.match(); //reinvoke() current and add it to the modified list
						MatcherChangeEvent evt = new MatcherChangeEvent(this, MatcherChangeEvent.EventType.MATCHER_ALIGNMENTSET_UPDATED, current.getID() );
						fireEvent(evt);
						modifiedMatchers.add(current);
						break;
					}
				}
			}
		}
	}
	/**
	 * add or update the alignments selected by the user in all the matchers selected in the table
	 * @param alignments selected by the user
	 */
	public void performUserMatching(int index, ArrayList<Alignment> alignments) {
		AbstractMatcher matcher = matcherInstances.get(index);
		matcher.addManualAlignments(alignments);
	}
	
	
	
	
	
	public Node getNode(String localname, boolean fromSource, boolean fromClasses) {
		Ontology o = sourceOntology;
		Node result = null;;
		if(!fromSource) {
			o = targetOntology;
		}
		ArrayList<Node> list = o.getClassesList();
		if(!fromClasses) {
			list = o.getPropertiesList();
		}
		Iterator<Node> it = list.iterator();
		while(it.hasNext()) {
			Node n = it.next();
			String ln = n.getLocalName();
			if(ln.equalsIgnoreCase(localname)) {
				result = n;
				break;
			}
		}
		return result;
	}
	
	
	
	public int getNextMatcherID() { return IDcounter++; }
	
	
	/***********************************************************************************************
	 * ************ Multi-ontology Support. ****************** - Cosmin, Nov 11, 2009.
	 */
	
	// this function gives a unique ID to every ontology that's loaded
	// this function is called in the TreeBuilder class, when the ID is set.
	public int getNextOntologyID() { return IDcounter++; }
	
	
	
	public int numOntologies() {
		return loadedOntologies.size();
	}
	
	public Ontology getOntologyByIndex( int index ) { // TODO: should check for and throw an IndexOutOfBounds exception
		return loadedOntologies.get(index);
	}
	
	// Returns an ontology by its ID, null if there is no ontology by that ID
	public Ontology getOntologyByID( int id ) {
		Iterator<Ontology> ontIter = loadedOntologies.iterator();
		while( ontIter.hasNext() ) {
			Ontology ont = ontIter.next();
			if( ont.getID() == id ) return ont;
		}
		return null;
	}
	
	// Return the ID of the ontology that has a certain model.
	public int getOntologyIDbyModel(Model model) {
		
		Iterator<Ontology> ontIter = loadedOntologies.iterator();
		while( ontIter.hasNext() ) {
			Ontology ont = ontIter.next();
			if( ont.getModel().equals(model) )
				return ont.getID();
		}
		
		// no ontology exists with this ID.
		return Ontology.ID_NONE;
	}
	
	public Iterator<Ontology> getOntologiesIterator() {
		return loadedOntologies.iterator();
	}
	
	/**
	 * Adds an ontology to the core.
	 */
	public void addOntology( Ontology ont ) {
		if( !loadedOntologies.contains(ont) ) {
				loadedOntologies.add(ont); 
				// We must notify all the listeners that an ontology was loaded into the system.
				OntologyChangeEvent e = new OntologyChangeEvent(this, OntologyChangeEvent.EventType.ONTOLOGY_ADDED, ont.getID() );
				fireEvent(e);
		}
	}
	
	/** 
	 * Removes an ontology from the core.
	 */
	public void removeOntology( Ontology ont ) {
		if( loadedOntologies.contains(ont) ) {
			loadedOntologies.remove(ont);
			if( sourceOntology == ont ) sourceOntology = null;
			if( targetOntology == ont ) targetOntology = null;
			// Notify all the listeners that an ontology was removed from the system
			OntologyChangeEvent e = new OntologyChangeEvent(this, OntologyChangeEvent.EventType.ONTOLOGY_REMOVED, ont.getID() );
			fireEvent(e);
		}
	}
	
	
	/********************************************************************************************
	 * Ontology Change Events
	 * fireEvent will send the event to all the listeners.
	 * The listeners get updated whenever a new ontology is loaded to the core, or an ontology is removed from the core.
	 */
	public void addOntologyChangeListener( OntologyChangeListener l )    { ontologyListeners.add(l); }
	public void removeOntologyChangeListener( OntologyChangeListener l ) { ontologyListeners.remove(l); }
	
	public void fireEvent( OntologyChangeEvent event ) {
		for( int i = ontologyListeners.size()-1; i >= 0; i-- ) {  // count DOWN from max (for a very good reason, http://book.javanb.com/swing-hacks/swinghacks-chp-12-sect-8.html )
			ontologyListeners.get(i).ontologyChanged(event);
		}
	}
	
	public void addMatcherChangeListener( MatcherChangeListener l )  { matcherListeners.add(l); }
	public void removeMatcherChangeListener( MatcherChangeListener l ) { matcherListeners.remove(l); }
	
	public void fireEvent( MatcherChangeEvent event ) {
		for( int i = matcherListeners.size()-1; i >= 0; i-- ) {  // count DOWN from max (for a very good reason, http://book.javanb.com/swing-hacks/swinghacks-chp-12-sect-8.html )
			matcherListeners.get(i).matcherChanged(event);
		}
	}
	
	
}
