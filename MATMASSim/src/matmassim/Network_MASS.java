package matmassim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.uw.bothell.css.dsl.MASS.*;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.network.Network;

public class Network_MASS implements Network {
	
	private String name = null;
	
	private double capperiod = 3600.0 ;
	
	private static final double DEFAULT_EFFECTIVE_CELL_SIZE = 7.5;

	private double effectiveCellSize = DEFAULT_EFFECTIVE_CELL_SIZE;

	private double effectiveLaneWidth = 3.75;
	
	private final Map<Id, Node> nodes = new LinkedHashMap<Id, Node>();

	private Map<Id, Link> links = new LinkedHashMap<Id, Link>();
	
	// Places object holds the entire network
	Places network = null;
	
	private NetworkFactory_MASS factory;
	
	// Create the adjacency list holds all the relationship between nodes and links
	private Map<Id, List<List<Id>> > adjacencyList;
	
	
	public Network_MASS(int[] size) {
		
		this.adjacencyList = new HashMap<Id, List<List<Id>>>();	
		
		this.network = new Places(1, "Element_MASS", null, size);
		
	}
	
	public String getName() {
		return this.name;
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
	
	public NetworkFactory_MASS getFactory() {
		return this.factory;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCapacityPeriod(double capperiod) {
		this.capperiod = capperiod;
	}

	public void setEffectiveCellSize(double effectiveCellSize) {
		this.effectiveCellSize = effectiveCellSize;
	}

	public void setEffectiveLaneWidth(double effectiveLaneWidth) {
		this.effectiveLaneWidth = effectiveLaneWidth;
	}

	@Override
	public Map<Id, Node> getNodes() {
		return this.nodes;
	}
	
	public void addNode(Node node) {
		this.nodes.put(node.getId(), node);
	}

	public void addLink(Link link) {
		this.links.put(link.getId(), link);
	}




	// Reading XML file using NetworkReader class
	public void readNetwork() {
		
		List<Id> sources = null;
		List<Id> destinations = null;
		List<List<Id>> listItem = null;
		
		NetworkReader reader = new NetworkReader();
		
		Iterator it = nodes.entrySet().iterator();
		Node nodeIt = null;
		while (it.hasNext()) {
			nodeIt = (Node) it.next();
			
			sources = new ArrayList<Id>();
			destinations = new ArrayList<Id>();
			
			Iterator linkIt = links.entrySet().iterator();
			Link linkTmp = null;
			while (linkIt.hasNext()) {
				linkTmp = (Link) linkIt.next();
				
				if (linkTmp.getFromNode().getId().equals(nodeIt.getId())) {
					sources.add(linkTmp.getId());
				}
				
				if (linkTmp.getToNode().getId().equals(nodeIt.getId())) {
					destinations.add(linkTmp.getId());
				}
				
			}
			
			listItem = new ArrayList<List<Id>>();
			listItem.add(sources);
			listItem.add(destinations);
			
			this.adjacencyList.put(nodeIt.getId(), listItem);
		}
		
		//Assign different neighbors to all place
		this.network.callAll( Element_MASS.setNeighbours, adjacencyList);
		
	}

	
	// unused methods
		@Override
	public Map<Id, ? extends Link> getLinks() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Node removeNode(Id nodeId) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Link removeLink(Id linkId) {
			// TODO Auto-generated method stub
			return null;
		}

}
