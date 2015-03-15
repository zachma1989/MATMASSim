package matmassim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.qsim.InternalInterface;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.DefaultQNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.LinkSpeedCalculator;
import org.matsim.core.mobsim.qsim.qnetsimengine.NetsimNetwork;
import org.matsim.core.mobsim.qsim.qnetsimengine.NetsimNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.QLanesNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.QLinkInternalI;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEngine;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetwork;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNode;
import org.matsim.core.mobsim.qsim.qnetsimengine.QLinkImpl;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QueueAgentSnapshotInfoBuilder;
import org.matsim.core.utils.misc.Time;

import edu.uw.bothell.css.dsl.MASS.*;


public class QSimEngine_MASS extends QNetsimEngine {

	private static final Logger log = Logger.getLogger(QNetsimEngine.class);

	private static final int INFO_PERIOD = 3600;

	/*package*/   QNetwork network;

	/** This is the collection of links that have to be moved in the simulation */
	/*package*/  List<QLinkInternalI> simLinksList = new ArrayList<QLinkInternalI>();

	/** This is the collection of nodes that have to be moved in the simulation */
	/*package*/  List<QNode> simNodesList = null;

	/** This is the collection of links that have to be activated in the current time step */
	/*package*/  ArrayList<QLinkInternalI> simActivateLinks = new ArrayList<QLinkInternalI>();

	/** This is the collection of nodes that have to be activated in the current time step */
	/*package*/  ArrayList<QNode> simActivateNodes = new ArrayList<QNode>();

//	private final Map<Id, QVehicle> vehicles = new HashMap<Id, QVehicle>();
//
//	private final QSim qsim;
//
//	private final double stucktimeCache;
//	private final DepartureHandler dpHandler;

	private double infoTime = 0;

//	private LinkSpeedCalculator linkSpeedCalculator = new DefaultLinkSpeedCalculator();

	// default constructor	
	public QSimEngine_MASS(QSim sim) {
		super(sim);
	}
	
	/*package*/ InternalInterface internalInterface = null ;
	@Override
	public void setInternalInterface( InternalInterface internalInterface) {
		this.internalInterface = internalInterface;
	}


	@Override
	public void onPrepareSim() {
		simNodesList = new ArrayList<QNode>();
		this.infoTime = 
				Math.floor(internalInterface.getMobsim().getSimTimer().getSimStartTime() / INFO_PERIOD) * INFO_PERIOD; 
		/*
		 * infoTime may be < simStartTime, this ensures to print out the
		 * info at the very first timestep already 
		 */
	}

	@Override
	public void afterSim() {
		/* Reset vehicles on ALL links. We cannot iterate only over the active links
		 * (this.simLinksArray), because there may be links that have vehicles only
		 * in the buffer (such links are *not* active, as the buffer gets emptied
		 * when handling the nodes.
		 */
		for (QLinkInternalI link : network.getNetsimLinks().values()) {
			link.clearVehicles();
		}
	}

	/**
	 * Implements one simulation step, called from simulation framework
	 * @param time The current time in the simulation.
	 */
	@Override
	public void doSimStep(final double time) {
		moveNodes(time);
		moveLinks(time);
		printSimLog(time);
	}

	private void moveNodes(final double time) {
		reactivateNodes();
		ListIterator<QNode> simNodes = this.simNodesList.listIterator();
		QNode node;

		while (simNodes.hasNext()) {
			node = simNodes.next();
			node.doSimStep(time);

			if (!node.isActive()) simNodes.remove();
		}
	}

	private void moveLinks(final double time) {
		
		reactivateLinks();
		ListIterator<QLinkInternalI> simLinks = this.simLinksList.listIterator();
		QLinkInternalI link;
		boolean isActive;

		while (simLinks.hasNext()) {
			link = simLinks.next();
			isActive = link.doSimStep(time);
			if (!isActive) {
				simLinks.remove();
			}
		}
	}

	// CHANGES: private --> protected
	protected void printSimLog(double time) {
		if (time >= this.infoTime) {
			this.infoTime += INFO_PERIOD;
			int nofActiveLinks = this.getNumberOfSimulatedLinks();
			int nofActiveNodes = this.getNumberOfSimulatedNodes();
			log.info("SIMULATION (QNetsimEngine) AT " + Time.writeTime(time)
					+ " : #links=" + nofActiveLinks
					+ " #nodes=" + nofActiveNodes);
		}
	}

	@Override
	protected void activateLink(final QLinkInternalI link) {
		this.simActivateLinks.add(link);
	}

	private void reactivateLinks() {
		if (!this.simActivateLinks.isEmpty()) {
			this.simLinksList.addAll(this.simActivateLinks);
			this.simActivateLinks.clear();
		}
	}

	@Override
	protected void activateNode(QNode node) {
		this.simActivateNodes.add(node);
	}

	private void reactivateNodes() {
		if (!this.simActivateNodes.isEmpty()) {
			this.simNodesList.addAll(this.simActivateNodes);
			this.simActivateNodes.clear();
		}
	}


}
