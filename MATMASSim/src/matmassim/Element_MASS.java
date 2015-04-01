package matmassim;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.misc.Time;

import edu.uw.bothell.css.dsl.MASS.Place;

public class Element_MASS extends Place
	implements Link, Node {

	private Id id;

	protected Node from = null;
	protected Node to = null;
	
	protected Coord coord;

	protected double length = Double.NaN;
	protected double freespeed = Double.NaN;
	protected double capacity = Double.NaN;
	protected double nofLanes = Double.NaN;

	private double flowCapacity;

	protected String type = null;

	protected String origid = null;

	protected double euklideanDist;

	private Network_MASS network;

	private static int fsWarnCnt = 0 ;
	private static int cpWarnCnt = 0 ;
	private static int plWarnCnt = 0 ;
	private static int lengthWarnCnt = 0;
	private static int loopWarnCnt = 0 ;
	private static final int maxFsWarnCnt = 1;
	private static final int maxCpWarnCnt = 1;
	private static final int maxPlWarnCnt = 1;
	private static final int maxLengthWarnCnt = 1;
	private static final int maxLoopWarnCnt = 1;
	
	private Vector < int[] > neighbours;
	
	final private static Logger log = Logger.getLogger(Element_MASS.class);

	public Element_MASS( Id id ) {
		this.id = id;
	}

	public Element_MASS(Id id, Element_MASS from, Element_MASS to, Network_MASS network,
			double length, double freespeed, double capacity,
			double nOfLanes, String type) {
		this.id = id;
		
		this.type = type;
		this.setOutMessage( type );
		
		this.network = network;
		this.from = from;
		this.to = to;
		//this.allowedModes = DEFAULT_ALLOWED_MODES;
		this.setLength(length);
		this.freespeed = freespeed;
		this.checkFreespeedSemantics();
		this.capacity = capacity;
		this.calculateFlowCapacity();
		this.checkCapacitiySemantics();
		this.nofLanes = nOfLanes;
		this.checkNumberOfLanesSemantics();
		this.euklideanDist = CoordUtils.calcDistance(this.from.getCoord(), this.to.getCoord());
		if (this.from.equals(this.to) && (loopWarnCnt < maxLoopWarnCnt)) {
			loopWarnCnt++ ;
			log.warn("[from=to=" + this.to + " link is a loop]");
			if ( loopWarnCnt == maxLoopWarnCnt )
				log.warn(Gbl.FUTURE_SUPPRESSED ) ;
		}
	}

	private void calculateFlowCapacity() {
		this.flowCapacity = this.capacity / getCapacityPeriod();
		this.checkCapacitiySemantics();
	}

	protected double getCapacityPeriod() {
		return network.getCapacityPeriod();
	}

	private void checkCapacitiySemantics() {
		
		if ((this.capacity <= 0.0) && (cpWarnCnt < maxCpWarnCnt) ) {
			cpWarnCnt++ ;
		}
	}

	private void checkFreespeedSemantics() {
		if ((this.freespeed <= 0.0) && (fsWarnCnt < maxFsWarnCnt) ) {
			fsWarnCnt++ ;
			if ( fsWarnCnt == maxFsWarnCnt )
				System.out.print( Gbl.FUTURE_SUPPRESSED) ;
		}
	}
	
	private void checkNumberOfLanesSemantics(){
		if ((this.nofLanes < 1) && (plWarnCnt < maxPlWarnCnt) ) {
			plWarnCnt++ ;
			log.warn("[permlanes=" + this.nofLanes + " of link id " + this.getId() +" may cause problems]");
			if ( plWarnCnt == maxPlWarnCnt )
				log.warn( Gbl.FUTURE_SUPPRESSED ) ;
		}
	}

	private void checkLengthSemantics(){
		if ((this.getLength() <= 0.0) && (lengthWarnCnt < maxLengthWarnCnt)) {
			lengthWarnCnt++;
			log.warn("[length=" + this.length + " of link id " + this.getId() + " may cause problems]");
			if ( lengthWarnCnt == maxLengthWarnCnt )
				log.warn(Gbl.FUTURE_SUPPRESSED) ;
		}
	}

	/**
	 * Given that this calculates a scalar product, this may indeed calculate the orthogonal projection.  
	 * But it does not say so, and I have no time to go through the exact calculation in detail.  Maybe somebody
	 * else can figure it out and document it here.  kai, mar'11
	 */
	public final double calcDistance(final Coord coord) {
		// yyyy should, in my view, call the generalized utils method. kai, jul09
		Coord fc = this.from.getCoord();
		Coord tc =  this.to.getCoord();
		double tx = tc.getX();    double ty = tc.getY();
		double fx = fc.getX();    double fy = fc.getY();
		double zx = coord.getX(); double zy = coord.getY();
		double ax = tx-fx;        double ay = ty-fy;
		double bx = zx-fx;        double by = zy-fy;
		double la2 = ax*ax + ay*ay;
		double lb2 = bx*bx + by*by;
		if (la2 == 0.0) {  // from == to
			return Math.sqrt(lb2);
		}
		double xla = ax*bx+ay*by; // scalar product
		if (xla <= 0.0) {
			return Math.sqrt(lb2);
		}
		if (xla >= la2) {
			double cx = zx-tx;
			double cy = zy-ty;
			return Math.sqrt(cx*cx+cy*cy);
		}
		// lb2-xla*xla/la2 = lb*lb-x*x
		double tmp = xla*xla;
		tmp = tmp/la2;
		tmp = lb2 - tmp;
		// tmp can be slightly negativ, likely due to rounding errors (coord lies on the link!). Therefore, use at least 0.0
		tmp = Math.max(0.0, tmp);
		return Math.sqrt(tmp);
	}

	//////////////////////////////////////////////////////////////////////
	// get methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public Node getFromNode() {
		return this.from;
	}

	@Override
	public final boolean setFromNode(final Node node) {
		this.from = node;
		return true;
	}

	@Override
	public Node getToNode() {
		return this.to;
	}

	@Override
	public final boolean setToNode(final Node node) {
		this.to = node;
		return true;
	}

	public double getFreespeedTravelTime() {
		return getFreespeedTravelTime(Time.UNDEFINED_TIME);
	}

	public double getFreespeedTravelTime(final double time) {
		return this.length / this.freespeed;
	}

	public double getFlowCapacity() {
		return getFlowCapacity(Time.UNDEFINED_TIME);
	}

	public double getFlowCapacity(final double time) {
		return this.flowCapacity;
	}

	public final String getOrigId() {
		return this.origid;
	}

	public final String getType() {
		return this.type;
	}

	public final double getEuklideanDistance() {
		return this.euklideanDist;
	}

	@Override
	public double getCapacity() {
		return getCapacity(Time.UNDEFINED_TIME);
	}

	@Override
	public double getCapacity(final double time) { // not final since needed in TimeVariantLinkImpl
		return this.capacity;
	}

	@Override
	public void setCapacity(double capacityPerNetworkCapcityPeriod){
		this.capacity = capacityPerNetworkCapcityPeriod;
		this.calculateFlowCapacity();
	}

	@Override
	public double getFreespeed() {
		return getFreespeed(Time.UNDEFINED_TIME);
	}

	@Override
	public double getFreespeed(final double time) { // not final since needed in TimeVariantLinkImpl
		return this.freespeed;
	}

	@Override
	public void setFreespeed(double freespeed) {
		this.freespeed = freespeed;
		this.checkFreespeedSemantics();
	}

	@Override
	public double getLength() {
		return this.length;
	}

	@Override
	public final void setLength(double length) {
		this.length = length;
		this.checkLengthSemantics();
	}

	@Override
	public double getNumberOfLanes() {
		return getNumberOfLanes(Time.UNDEFINED_TIME);
	}

	@Override
	public double getNumberOfLanes(final double time) { // not final since needed in TimeVariantLinkImpl
		return this.nofLanes;
	}

	@Override
	public void setNumberOfLanes(double lanes) {
		this.nofLanes = lanes;
		this.checkNumberOfLanesSemantics();
	}
	
	//For Link
	@Override
	public void setAllowedModes(Set<String> modes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getAllowedModes() {
		// TODO Auto-generated method stub
		return null;
	}


	public final void setOrigId(final String id) {
		this.origid = id;
	}

	public void setType(final String type) {
		this.type = type;
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		this.from.addOutLink(this);
		this.to.addInLink(this);
	}

	@Override
	public String toString() {
		return super.toString() +
		"[id=" + this.getId() + "]" +
		"[from_id=" + this.from.getId() + "]" +
		"[to_id=" + this.to.getId() + "]" +
		"[length=" + this.length + "]" +
		"[freespeed=" + this.freespeed + "]" +
		"[capacity=" + this.capacity + "]" +
		"[permlanes=" + this.nofLanes + "]" +
		"[origid=" + this.origid + "]" +
		"[type=" + this.type + "]";
	}
	
	public void setCoord(Coord coord) {
		this.coord = coord;
	}
	
	@Override
	public Coord getCoord() {
		return null;
	}

	@Override
	public Id getId() {
		return id;
	}
	
	public Network getNetwork() {
		return (Network)this.network;
	}
	
	// For Node (Unused methods)
	@Override
	public boolean addInLink(Link link) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addOutLink(Link link) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<Id, ? extends Link> getInLinks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Id, ? extends Link> getOutLinks() {
		// TODO Auto-generated method stub
		return null;
	}

	// MASS portion
	
	public static final int setNeighbours = 0;
	public static final int exchangeParameter = 1;
	public static final int collectParameter = 2;
	
	@ Override
	public Object callMethod( int functionId, Object argument ) {
		
		switch ( functionId ) {
		
			case setNeighbours:
				setNeighbours( argument );
		
			case exchangeParameter:
				exchangeParameter( argument );
			
		}
    	return null;
    }
	
	public void setNeighbours( Object argument ) {
		
		Object[] neighbourList = (Object[])argument;
		
		if ( neighbourList != null ) {
			for (int i = 0; i < neighbourList.length; i++) {
				
				int[] indices = (int[])neighbourList[i];
				
				this.neighbours.add( indices );
			}
		}
		
		this.setNeighbours( neighbours );
		
	}
	
	public Object exchangeParameter( Object argument ) {
		
		String type = (String) argument;
		
		Object[] parameters = (Object[]) new Object();
		
		if ( parameters.length != 0 ) {

			if ( type.equals("Node") ){
				
				parameters[1] = this.freespeed;
				parameters[2] = this.capacity;
			}
			else if ( type.equals( "Link" ) ) {
				parameters[1] = this.freespeed;
				parameters[2] = this.capacity;
				parameters[3] = this.flowCapacity;
				parameters[4] = this.euklideanDist;
			}
			
			
		}

		return parameters;
		
	}
	
	public void collectParameter() {
		
		Object[] parameters = this.getInMessages();
		
		if ( parameters.length != 0 ) {

			if ( type.equals("Node") ){
				
				this.freespeed = (double) parameters[1];
				this.capacity = (double) parameters[2];
			}
			else if ( type.equals( "Link" ) ) {
				this.freespeed = (double) parameters[1];
				this.capacity = (double) parameters[2];
				this.flowCapacity = (double) parameters[3];
				this.euklideanDist = (double) parameters[4];
			}
			
			
		}
		
	}

	
}
