package matmassim;

import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.utils.collections.IdentifiableArrayMap;

import MASS.*;

public class Node_MASS extends Place {

	private String type = null;
	private String origid = null;

	protected transient  Map<Id, Link> inlinks  = new IdentifiableArrayMap<Link>();
	protected transient  Map<Id, Link> outlinks = new IdentifiableArrayMap<Link>();

	protected Coord coord;
	protected final Id id = null;

	private final static Logger log = Logger.getLogger(NodeImpl.class);



	//////////////////////////////////////////////////////////////////////
	// add / set methods
	//////////////////////////////////////////////////////////////////////

	public final void setOrigId(final String id) {
		this.origid = id;
	}

	public final void setType(final String type) {
		this.type = type == null ? null : type.intern();
	}

	private static int cnt2 = 0 ;
	public final boolean exchangeNetworkParameter() {
		
		Link inlink;
		Id linkid = inlink.getId();
		if (this.inlinks.containsKey(linkid)) {
			throw new IllegalArgumentException(this + "[inlink_id=" + inlink.getId() + " already exists]");
		}
		if (this.outlinks.containsKey(linkid) && (cnt2 < 1)) {
			cnt2++ ;
			log.warn(this + "[inlink_id=" + inlink.getId() + " is now in- and out-link]");
			log.warn(Gbl.ONLYONCE) ;
		}
		this.inlinks.put(linkid, inlink);
		return true; // yy should return true only if collection changed as result of call
	}

	private static int cnt = 0 ;
	public final boolean addOutLink(Link outlink) {
		Id linkid = outlink.getId();
		if (this.outlinks.containsKey(linkid)) {
			throw new IllegalArgumentException(this + "[outlink_id=" + outlink.getId() + " already exists]");
		}
		if (this.inlinks.containsKey(linkid) && (cnt < 1)) {
			cnt++ ;
			log.warn(this.toString() + "[outlink_id=" + outlink + " is now in- and out-link]");
			log.warn(Gbl.ONLYONCE) ;
		}
		this.outlinks.put(linkid, outlink);
		return true ; // yy should return true only if collection changed as result of call
	}

	public void setCoord(final Coord coord){
		this.coord = coord;
	}
	
	public void setNeighbours( Vector<int[]> neighbours ){
    	
    	// Go through the list    	
    	// Store neighbours for each node and link to their corresponding Place class 
		int length = 10;
    	for ( int i = 0; i < neighbours.size( ); i++ ) {
    			    
    		// for each neighbor
    		int[] offset = neighbours.get(i);
    		int[] neighborCoord = new int[length];
    			    
		    
		    neighbours.add( neighborCoord );
    	}
    	
    }
	

}
