
import MASS.*;
import  MASS.Debugger_base; 
import java.util.*;

public class Debugger extends Debugger_base{
    public final static int init_ = 0;
    public final static int fetchDebugData_ = 1;
    public final static int injectDebugData_ = 2;

    public Debugger(Object argument){
        super(argument);
    }

    public Object callMethod(int functionId, Object argument) {
        switch (functionId) {
        case init_: return super.init(argument);
        case fetchDebugData_: return super.fetchDebugData(argument);
        case injectDebugData_: return super.InjectDebugData(argument);
        default: break;
        }
        return null;
    }
    
    public static void sendDataToGUI(){
	Debugger_base.updateDataConnectionThread(Debugger_base.STATUS_SEND_PLACE_DATA);
    }

}
