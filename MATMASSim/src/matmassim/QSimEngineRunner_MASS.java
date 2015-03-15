package matmassim;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.matsim.api.core.v01.Id;
import org.matsim.core.mobsim.qsim.qnetsimengine.QLinkInternalI;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNode;

import edu.uw.bothell.css.dsl.MASS.MASS;
import edu.uw.bothell.css.dsl.MASS.Places;
import mass_debugger.Wave2D_Java.Debugger;

/**
 * These are the "threads" of the {@link ParallelQNetsimEngine}. The "run()" method is implicitly called by starting the thread.  
 * 
 * @author (of this documentation) nagel
 *
 */
public class QSimEngineRunner_MASS {

	private double time = 0.0;

	private volatile boolean simulationRunning = true;


	private final List<QNode> nodesList = new ArrayList<QNode>();
	private final List<QLinkInternalI> linksList = new ArrayList<QLinkInternalI>();

	/** 
	 * This is the collection of nodes that have to be activated in the current time step.
	 * This needs to be thread-safe since it is not guaranteed that each incoming link is handled
	 * by the same thread as a node itself.
	 * A node could be activated multiple times concurrently from different incoming links within 
	 * a time step. To avoid this,
	 * a) 	the activateNode() method in the QNode class could be synchronized or 
	 * b) 	a map could be used instead of a list. By doing so, no multiple entries are possible.
	 * 		However, still multiple "put" operations will be performed for the same node.
	 */
	private final Map<Id, QNode> nodesToActivate = new ConcurrentHashMap<Id, QNode>();
	
	private Places networkPlaces;
	
	private int exchangNetworkParameters = 0;
	private int exchangNodeParameters = 1;
	private int exchangLinkParameters = 2;
	
	//MASS Debugger
	private Places debugger;
	private int nProcesses = 4;
	private Object[] debugData;
	
	
	/** This is the collection of links that have to be activated in the current time step */
	private final ArrayList<QLinkInternalI> linksToActivate = new ArrayList<QLinkInternalI>();

	
	
	public QSimEngineRunner_MASS(String[] arguments, int nProc, int nThr) {
		
		MASS.init( arguments, nProc, nThr );
		
		debugger = new Places(99, "Debugger", (Object)(1), 8);
		debugger.callAll( Debugger.init_);
		debugData = new Object[8];
		
	}

	/*package*/ void setTime(final double t) {
		time = t;
	}

	public void afterSim() {
		this.simulationRunning = false;
	}

	public void run() {
		/*
		 * The method is ended when the simulationRunning Flag is
		 * set to false.
		 */
		while(true) {
			try {

				/*
				 * Move Nodes
				 */
				ListIterator<QNode> simNodes = this.nodesList.listIterator();
				QNode node;
				
				while (simNodes.hasNext()) {
					node = simNodes.next();
					node.doSimStep(time);
					if (!node.isActive()) simNodes.remove();
				}

				//networkPlaces.exchangeAll(1, exchangNodeParameters);
				
				
				/*
				 * Move Links
				 */
				ListIterator<QLinkInternalI> simLinks = this.linksList.listIterator();
				QLinkInternalI link;
				boolean isActive;

				while (simLinks.hasNext()) {
					link = simLinks.next();

					isActive = link.doSimStep(time);

					if (!isActive) {
						simLinks.remove();
					}
				}
				
				//networkPlaces.exchangeAll(1, exchangLinkParameters);
				
				networkPlaces.exchangeAll(1, exchangNetworkParameters);
				
				//Mass Debugger
			    Object[] debugParam = new Integer[nProcesses];
			    debugData = (Object[])debugger.callAll(Debugger.fetchDebugData_, debugParam);
			    //here we should let debugger send data to GUI
			    Debugger.sendDataToGUI();


			} catch (Exception e) {}
		}
	}


}
