package matmassim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.uw.bothell.css.dsl.MASS.*;

import org.matsim.api.core.v01.BasicLocation;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.TimeVariantLinkImpl;

public class Network_MASS {
	
	// Places object holds the entire network
	Places network = null;
	
	// Create the adjacency list holds all the relationship between nodes and links
	List< List<BasicLocation> > adjacencyList = new List< List<BasicLocation> >();
	
	public Network_MASS() {
		
		String[] arguments = new String[4];
		
		arguments[1] = "zacma";
		arguments[2] = "Mm951126~";
		arguments[3] = "machinefile.txt";
		arguments[4] = "61774";
		
		
		MASS.init( null, 1, 1);
		
		this.network = new Places(1, "Link_MASS", null, 3000);
		
		this.network.callAll( 0 );
		
	}
	
	// ////////////////////////////////////////////////////////////////////
		// set methods
		// ////////////////////////////////////////////////////////////////////

		/**
		 * @param capPeriod the capacity-period in seconds
		 */
		public void setCapacityPeriod(final double capPeriod) {
			this.capperiod = (int) capPeriod;
		}

		public void setEffectiveCellSize(final double effectiveCellSize) {
			if (this.effectiveCellSize != effectiveCellSize) {
				if (effectiveCellSize != DEFAULT_EFFECTIVE_CELL_SIZE) {
					log.warn("Setting effectiveCellSize to a non-default value of " + effectiveCellSize);
				} else {
					log.info("Setting effectiveCellSize to " + effectiveCellSize);
				}
				this.effectiveCellSize = effectiveCellSize;
			}
		}

		public void setEffectiveLaneWidth(final double effectiveLaneWidth) {
			if (!Double.isNaN(this.effectiveLaneWidth) && this.effectiveLaneWidth != effectiveLaneWidth) {
				log.warn(this + "[effectiveLaneWidth=" + this.effectiveLaneWidth + " already set. Will be overwritten with " + effectiveLaneWidth + "]");
			}
			this.effectiveLaneWidth = effectiveLaneWidth;
		}

		/**
		 * Sets the network change events and replaces existing events. Before
		 * events are applied to their corresponding links, all links are reset to
		 * their initial state. Pass an empty event list to reset the complete network.
		 *
		 * @param events a list of events.
		 */
		public void setNetworkChangeEvents(final List<NetworkChangeEvent> events) {
			if (!this.factory.isTimeVariant()) {
				throw new RuntimeException(
						"Trying to set NetworkChangeEvents but NetworkFactory is not time variant");
			}

			for(Link link : getLinks().values()) {
				((TimeVariantLinkImpl)link).clearEvents();
			}

			this.networkChangeEvents = events;
			for (NetworkChangeEvent event : events) {
				for (Link link : event.getLinks()) {
					((TimeVariantLinkImpl)link).applyEvent(event);
				}
			}
		}

		/**
		 * Adds a single network change event and applies it to the corresponding
		 * links.
		 *
		 * @param event: a network change event.
		 */
		public void addNetworkChangeEvent(final NetworkChangeEvent event) {
			if (!this.factory.isTimeVariant()) {
				throw new RuntimeException(
						"Trying to set NetworkChangeEvents but NetworkFactory is not time variant");
			}

			if (this.networkChangeEvents == null) {
				this.networkChangeEvents = new ArrayList<NetworkChangeEvent>();
			}

			this.networkChangeEvents.add(event);
			for (Link link : event.getLinks()) {
				((TimeVariantLinkImpl)link).applyEvent(event);
			}
		}

		@Override
		public double getCapacityPeriod() {
			return this.capperiod;
		}

		public double getEffectiveCellSize() {
			return this.effectiveCellSize;
		}

		@Override
		public double getEffectiveLaneWidth() {
			return this.effectiveLaneWidth;
		}

		@Override
		public Map<Id, Node> getNodes() {
			return this.nodes;
		}

		/**
		 * Finds the (approx.) nearest link to a given point on the map.<br />
		 * It searches first for the nearest node, and then for the nearest link
		 * originating or ending at that node.
		 *
		 * @param coord
		 *          the coordinate for which the closest link should be found
		 * @return the link found closest to coord
		 */
//		public Link getNearestLink(final Coord coord) {
//			Link nearestLink = null;
//			Node nearestNode = null;
//			if (this.nodeQuadTree == null) { buildQuadTree(); }
//			nearestNode = this.nodeQuadTree.get(coord.getX(), coord.getY());
//			if ( nearestNode == null ) {
//				log.warn("[nearestNode not found.  Will probably crash eventually ...  Maybe run NetworkCleaner?]" + this ) ;
//				return null ;
//			}
//
//			if ( nearestNode.getInLinks().isEmpty() && nearestNode.getOutLinks().isEmpty() ) {
//				log.warn(this + "[found nearest node that has no incident links.  Will probably crash eventually ...  Maybe run NetworkCleaner?]" ) ;
//			}
//
//			// now find nearest link from the nearest node
//			// [balmermi] it checks now ALL incident links, not only the outgoing ones.
//			// TODO [balmermi] Now it finds the first of the typically two nearest links (same nodes, other direction)
//			// It would be nicer to find the nearest link on the "right" side of the coordinate.
//			// (For Great Britain it would be the "left" side. Could be a global config param...)
//			double shortestDistance = Double.MAX_VALUE;
//			for (Link link : NetworkUtils.getIncidentLinks(nearestNode).values()) {
//				double dist = ((LinkImpl) link).calcDistance(coord);
//				if (dist < shortestDistance) {
//					shortestDistance = dist;
//					nearestLink = link;
//				}
//			}
//			if ( nearestLink == null ) {
//				log.warn(this + "[nearestLink not found.  Will probably crash eventually ...  Maybe run NetworkCleaner?]" ) ;
//			}
//			return nearestLink;
//		}
		
//		public Link getNearestLinkExactly(final Coord coord) {
//			if (this.linkQuadTree == null) {
//				buildLinkQuadTree();
//			}
//			return this.linkQuadTree.getNearest(coord.getX(), coord.getY());
//		}

		

		/**
		 * finds the node nearest to <code>coord</code>
		 *
		 * @param coord the coordinate to which the closest node should be found
		 * @return the closest node found, null if none
		 */
//		public Node getNearestNode(final Coord coord) {
//			if (this.nodeQuadTree == null) { buildQuadTree(); }
//			return this.nodeQuadTree.get(coord.getX(), coord.getY());
//		}

	public void performParameterExchange() {
		
		this.network.exchangeAll( 1, 1 );
		
	}

	// Reading XML file using NetworkReader class
	public void readNetwork() {
		
		NetworkReaderMatsimV1 reader = new NetworkReaderMatsimV1();
		
	}
	
}
