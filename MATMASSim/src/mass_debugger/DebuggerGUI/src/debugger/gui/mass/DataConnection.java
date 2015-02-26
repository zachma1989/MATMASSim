package debugger.gui.mass;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import MASS.AgentDebugData;
import MASS.SinglePlaceAgentData;

public class DataConnection extends Thread{
	
	public static boolean stop = false;
	private int counter;
	private DataEventListener listener;
	private DebuggerGUI debuggerGUI;
	private Socket socket;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private int arraySize;
	//private Double[] places;
	private int guiCommand;
	private SinglePlaceAgentData currentSPAData;
	

	public DataConnection(DebuggerGUI debuggerGUI){
		counter = 1;
		arraySize = 0;
		socket = null;
		input = null;
		output = null;
		currentSPAData = null;
		this.debuggerGUI = debuggerGUI;
		guiCommand = Constants.GUI_CMD_RECV_DATA;
	}
	
	@Override
	public void run(){
		counter = 0;
		while(true){
			try{
				Thread.sleep(1000);
			} catch (Exception e){}
			
			synchronized(debuggerGUI){
				if(stop){
					stopDataConnection();
					continue;
				}
			}
			synchronized(this){
				//System.out.println("guiCommand:"+guiCommand);
				switch(guiCommand){
				case Constants.GUI_CMD_IDLE:
					try{
						//process the data that has already sent by MASS	
						if(input.available() > 0){
							System.out.println("idle and read data");
							receiveDataFromMASS();
						}
					}catch(Exception e){}
					break;
				case Constants.GUI_CMD_RECV_DATA:
					receiveDataFromMASS();
					break;
				case Constants.GUI_CMD_PAUSE:
					pauseComputation();
					break;
				case Constants.GUI_CMD_RESUME:
					resumeComputation();
					break;
				case Constants.GUI_CMD_INJECT_PLACE:
					injectPlaceData();
					break;
				default:
					break;
				}
			}
		}
	}
	
	private void pauseComputation(){
		try{
			output.writeObject(Constants.DATA_CMD_PAUSE);
			output.flush();
			System.out.println("send "+Constants.DATA_CMD_PAUSE);
		}catch(IOException e){
			System.err.println("send "+Constants.DATA_CMD_PAUSE+" failed");
		}
		guiCommand = Constants.GUI_CMD_IDLE;
	}
	
	private void resumeComputation(){
		
		try{
			output.writeObject(Constants.DATA_CMD_RESUME);
			output.flush();
			System.out.println("send "+Constants.DATA_CMD_RESUME);
		}catch(IOException e){
			System.err.println("send "+Constants.DATA_CMD_RESUME+" failed");
		}
		
		guiCommand = Constants.GUI_CMD_RECV_DATA;
	}
	
	private void injectPlaceData(){
		try{
			output.writeObject(Constants.DATA_CMD_INJECT_PLACE);
			System.out.println("x:"+currentSPAData.getX()+"y:"+currentSPAData.getY()+"v:"+currentSPAData.getPlace_val());
			//writeint and readint doesn't work?????
			output.writeObject(String.valueOf(currentSPAData.getX()));
			output.writeObject(String.valueOf(currentSPAData.getY()));
			output.writeObject(String.valueOf(currentSPAData.getPlace_val()));
			output.flush();
			System.out.println("send "+Constants.DATA_CMD_INJECT_PLACE);
		}catch(IOException e){
			e.printStackTrace();
			System.err.println("send "+Constants.DATA_CMD_INJECT_PLACE+" failed");
		}
		
		guiCommand = Constants.GUI_CMD_IDLE;
	}
	
	private void receiveDataFromMASS(){
		String cmd = "";
		//receive data from debugger server
		try{

			cmd = (String) input.readObject();
			System.out.println(cmd);
			if(cmd.equals(Constants.DATA_CMD_NEW_APPLICATION)){
				//receive application name
				final String appName = (String)input.readObject();
				//receive place size
				arraySize = input.readInt();
				final int size = arraySize;
				System.out.println(appName+"  "+arraySize);
				//receive application mode: place or place+agent
				DebuggerGUI.isPlaceAgentApp = input.readBoolean();
				//init places array
				DebuggerGUI.places = new Double[arraySize * arraySize];
				debuggerGUI.initGraphic(arraySize);
				SwingUtilities.invokeLater(new Runnable(){
					@Override
					public void run(){
						listener.initApplicationSpec(appName, size);
					} 
				});
			}else if(cmd.equals(Constants.DATA_CMD_PLACE_DATA)){
				int i = 0;
				for(i=0; i<arraySize*arraySize; i++){
					DebuggerGUI.places[i] = new Double(input.readDouble());
				}
				//System.out.println("times:" + counter++ +" total:"+i);
				//only place based application needs to update GUI at this point
				if(!DebuggerGUI.isPlaceAgentApp){
					SwingUtilities.invokeLater(new Runnable(){
						@Override
						public void run(){
							listener.onReceiveData(DebuggerGUI.places);
						} 
					});
				}
			}else if(cmd.equals(Constants.DATA_CMD_AGENT_DATA)){
				//read the number of agents
				int nAgents = Integer.parseInt((String)input.readObject());
				try{
					DebuggerGUI.agents.clear();
					for(int i=0; i<nAgents; i++){
						AgentDebugData agent = (AgentDebugData)input.readObject();
						int index = agent.x * arraySize + agent.y;
						System.out.println("x: "+agent.x+" y: "+agent.y);
						if(DebuggerGUI.agents.containsKey(index)){
							DebuggerGUI.agents.get(index).add(agent.val);
						}else{
							ArrayList<Integer> list = new ArrayList<Integer>();
							list.add(agent.val);
							DebuggerGUI.agents.put(index, list);
						}
					}
				}catch(ClassNotFoundException e){
					e.printStackTrace();
				}
				SwingUtilities.invokeLater(new Runnable(){
					@Override
					public void run(){
						listener.onReceiveData(DebuggerGUI.places);
					} 
				});
				
			}
		} catch (IOException e){
			System.out.println("IOERROR, read timeout!");
		} catch (ClassNotFoundException e){
			
		}
	}
	
	public void setPauseCommand(int cmd){
		synchronized(this){
			guiCommand = cmd;
		}
	}
	
	public void setResumeCommand(int cmd){
		synchronized(this){
			guiCommand = cmd;
		}
	}
	
	public void setInjectPlaceCommand(int cmd, SinglePlaceAgentData spaData){
		synchronized(this){
			guiCommand = cmd;
			currentSPAData = new SinglePlaceAgentData(spaData.getX(), 
					spaData.getY(), spaData.getPlace_val(), null);
		}
	}
	
	
	
	
	public void addListener(DataEventListener l){
		this.listener = l;
	}
	
	@SuppressWarnings("finally")
	public boolean setTCPSocket(Socket client){
		this.socket = client;
		try{
			socket.setSoTimeout(1000);
			System.out.println("before get output");
			output = new ObjectOutputStream(client.getOutputStream());
			System.out.println("after get output");
			output.flush();
			input = new ObjectInputStream(client.getInputStream());
			System.out.println("after get input");
		} catch(Exception e){
			e.printStackTrace();
			try{
				client.close();
				
			} catch (Exception exp){
				
			} finally{
				return false;
			}
		}
		
		return true;
	}
	
	private void stopDataConnection(){
		//close TCP connection
		try{
			input.close();
			output.close();
			socket.close();
		} catch (Exception e){
			
		} finally{
			try{
				input.close();
				output.close();
				socket.close();
			} catch(Exception e){}
			socket = null;
			input = null;
			output = null;
		}
	}
}
