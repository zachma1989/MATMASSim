package matmassim;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkFactoryImpl;


public class NetworkFactory_MASS implements NetworkFactory {

	private final static Logger log = Logger.getLogger(NetworkFactoryImpl.class);

	private ElementFactory_MASS elementFactory = null;
	
	private final Network_MASS network;

	public NetworkFactory_MASS(final Network_MASS network) {
		this.network = network;
		this.elementFactory = new ElementFactory_MASS();
	}

	@Override
	public Element_MASS createNode(final Id id, final Coord coord) {
		Element_MASS node = new Element_MASS(id);
		node.setCoord(coord) ;
		return node ;
	}

	public Element_MASS createLink(Id id, Element_MASS fromNode, Element_MASS toNode) {
		return this.elementFactory.createLink(id, fromNode, toNode, this.network, 1.0, 1.0, 1.0, 1.0);
	}

	public Element_MASS createLink(final Id id, final Node from, final Node to,
			final Network_MASS network, final double length, final double freespeedTT, final double capacity,
			final double lanes) {
		return this.elementFactory.createLink(id, (Element_MASS)from, (Element_MASS)to, network, length, freespeedTT, capacity, lanes);
	}

	
	public void setLinkFactory(final ElementFactory_MASS factory) {
		this.elementFactory = factory;
	}

	// Unused Method
	@Override
	public Link createLink(Id id, Node fromNode, Node toNode) {
		// TODO Auto-generated method stub
		return null;
	}

	// Deprecated
	@Override
	public Link createLink(Id id, Id fromNodeId, Id toNodeId) {
		// TODO Auto-generated method stub
		return null;
	}

}
