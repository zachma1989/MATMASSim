package matmassim;

import org.matsim.core.config.*;
import org.matsim.core.controler.*;

public class CentralControler {

	public static void main(String[] args) {
		
		Config config = ConfigUtils.loadConfig("input/config.xml");
		Controler controler = new Controler(config);
		// controler.setOverwriteFiles(true);
		controler.run();
		

	}

}
