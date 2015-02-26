package debugger.gui.mass;

import MASS.SinglePlaceAgentData;

public interface DataEventListener {
	
	void onReceiveData(Object[] objects);
	void initApplicationSpec(String appName, int size);
	void sendInjectData(SinglePlaceAgentData spaData);
}

