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

public class QSimEngineRunner_MASS {

	private double time = 0.0;

	private volatile boolean simulationRunning = true;


	private final List<QNode> nodesList = new ArrayList<QNode>();
	private final List<QLinkInternalI> linksList = new ArrayList<QLinkInternalI>();
	
	private final Map<Id, QNode> nodesToActivate = new ConcurrentHashMap<Id, QNode>();
	
	private Network_MASS network;
	
	//MASS Debugger
//	private Places debugger;
//	private int nProcesses = 4;
//	private Object[] debugData;
	
	
	/** This is the collection of links that have to be activated in the current time step */
	private final ArrayList<QLinkInternalI> linksToActivate = new ArrayList<QLinkInternalI>();

	
	
	public QSimEngineRunner_MASS(String[] arguments, int nProc, int nThr) {
		
		this.network = new Network_MASS();
		
		MASS.init( arguments, nProc, nThr );
		
//		debugger = new Places(99, "Debugger", (Object)(1), 8);
//		debugger.callAll( Debugger.init_);
//		debugData = new Object[8];
		
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
		while( this.simulationRunning ) {
			try {
				
				//At the beginning of each iteration
				this.network.network.callAll( Element_MASS.collectParameter );

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
				
				this.network.network.exchangeAll(1, Element_MASS.exchangeParameter);
				
				//Mass Debugger
//			    Object[] debugParam = new Integer[nProcesses];
//			    debugData = (Object[])debugger.callAll(Debugger.fetchDebugData_, debugParam);
//			    //here we should let debugger send data to GUI
//			    Debugger.sendDataToGUI();


			} catch (Exception e) {}
		}
		
		MASS.finish();
	}


}
