package MASS;

import java.io.Serializable;
import java.util.ArrayList;

public class SinglePlaceAgentData implements Serializable{
	private int x;
	private int y;
	private int place_val;
	private ArrayList<Integer> agents;
	public SinglePlaceAgentData(int x, int y, int val, ArrayList<Integer> agents){
		this.x = x;
		this.y = y;
		this.place_val = val;
		this.agents = new ArrayList<Integer>();
		if(agents != null){
			this.agents = (ArrayList<Integer>)(agents.clone());
		}
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getPlace_val() {
		return place_val;
	}
	public void setPlace_val(int place_val) {
		this.place_val = place_val;
	}
	public ArrayList<Integer> getAgents() {
		return agents;
	}
	public void setAgents(ArrayList<Integer> agents) {
		this.agents = agents;
	}
}
