package debugger.gui.mass;

public class Constants {
	//after receiver this command: application name, dimension, size will be received
	public final static String DATA_CMD_NEW_APPLICATION = "cmdNewApplication"; 
	//after receive this command: all place data will be received
	public final static String DATA_CMD_PLACE_DATA = "cmdPlaceData";
	//after receive this command: all place data will be received
	public final static String DATA_CMD_AGENT_DATA = "cmdAgentData";
	public final static String DATA_CMD_PAUSE = "cmdPause";
	public final static String DATA_CMD_RESUME = "cmdResume";
	public final static String DATA_CMD_INJECT_PLACE = "cmdInjectPlace";
	
	
	
	
	public final static int GUI_CMD_IDLE = 1;
	public final static int GUI_CMD_RECV_DATA = 2;
	public final static int GUI_CMD_PAUSE = 3;
	public final static int GUI_CMD_RESUME = 4;
	public final static int GUI_CMD_INJECT_PLACE = 5;
}
