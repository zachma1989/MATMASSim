package matmassim;

import org.matsim.api.core.v01.Id;

public class ElementFactory_MASS {

	public Element_MASS createLink(Id id, Element_MASS from, Element_MASS to, Network_MASS network, double length, double freespeed,
			double capacity, double nOfLanes) {
		return new Element_MASS(id, from, to, network, length, freespeed, capacity, nOfLanes, "Link");
	}

}
