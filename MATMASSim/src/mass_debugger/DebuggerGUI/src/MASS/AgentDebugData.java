package MASS;

import java.io.Serializable;

public class AgentDebugData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int x;
	public int y;
	public int val;
	public AgentDebugData(int x, int y, int v){
		this.x = x;
		this.y = y;
		this.val = v;
	}
}
