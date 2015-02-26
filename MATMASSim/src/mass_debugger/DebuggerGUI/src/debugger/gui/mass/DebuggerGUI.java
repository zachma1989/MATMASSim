package debugger.gui.mass;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;

import MASS.SinglePlaceAgentData;

public class DebuggerGUI implements DataEventListener{
	
	private static int GRAPHIC_PANEL_SIZE = 720;
	private static int PORT = 40863;
	private static int CTRL_PANEL_WIDTH = 220;
	private static int CTRL_PANEL_HEIGHT = 100;
	private static boolean DATACONNECTION_STARTED = false;
	private DebuggerFrame gFrame;
	private MicroscopicFrame microFrame;
	private GraphicPanel graphicPanel;
	private ControlPanel ctlPanel;
	private DataConnection dataConnection;
	private DebuggerGUI debugerGuiIinstance;
	private InetAddress serverAddress;
	private double[][] matrix;
	private MASSApplication massApp;
	//store agents, key: place index, value: list of agents
	protected static HashMap<Integer, ArrayList<Integer>> agents;
	protected static Double[] places;
	//true: place and agent app; false: only place based app.
	protected static boolean isPlaceAgentApp;

	public static void main(String[] args) {
		DebuggerGUI debuggerGui = new DebuggerGUI();
		debuggerGui.initDataConnection();//cannot start this thread in even dispatch thread??? so here
	}
	
	private DebuggerGUI() {
		try{
			serverAddress = InetAddress.getByName("localhost");
		} catch (Exception e){}
		agents = new HashMap<Integer, ArrayList<Integer>>();
		debugerGuiIinstance = this;
		gFrame = new DebuggerFrame();
	}
	
	public void initDataConnection(){
		dataConnection = new DataConnection(this);
		dataConnection.addListener(this);//(graphicPanel);
	}
	
	public void initGraphic(int size){
		graphicPanel.initGraphic(size);
	}

	@Override
	public void onReceiveData(Object[] objects) {
		//Double[] places = (Double[])objects;
		int r = 0, c = 0;
		for(int i=0; i<places.length; i++){
			r = i/massApp.getSize();
			c = i%massApp.getSize();
			matrix[r][c] = DebuggerGUI.places[i].doubleValue();
		}
		graphicPanel.drawGraphics();
	}

	@Override
	public void initApplicationSpec(String name, int size) {
		massApp = new MASSApplication(name, size);
		ctlPanel.updateAppSpecToGUI(name, size);
		
	}
	
	@Override
	public void sendInjectData(SinglePlaceAgentData spaData){
		dataConnection.setInjectPlaceCommand(Constants.GUI_CMD_INJECT_PLACE, spaData);
	}
	
	
	
	/** *************************************************************
	 * DebuggerFrame
	 * *************************************************************/
	class DebuggerFrame extends JFrame{
		public DebuggerFrame() {
			setTitle("MASS Debugger");
			setResizable(false);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			//start graphic panel
			graphicPanel = new GraphicPanel();
			graphicPanel.setBackground(Color.white);
			graphicPanel.setPreferredSize(new Dimension(GRAPHIC_PANEL_SIZE, GRAPHIC_PANEL_SIZE));
			add(graphicPanel, BorderLayout.CENTER);
			
			//start control panel
			ctlPanel = new ControlPanel();
			ctlPanel.setBackground(Color.WHITE);
			ctlPanel.setPreferredSize(new Dimension(CTRL_PANEL_WIDTH, CTRL_PANEL_HEIGHT));
			add(ctlPanel, BorderLayout.EAST);
			
			pack();
			setVisible(true);
		}
	}
	
	/** ***************************************************************
	 * GraphicPanel
	 * ***************************************************************/
	private class GraphicPanel extends JPanel implements MouseListener,
			MouseMotionListener{
		private int cellWidth;
		private int N;
		private Color color[]; // wave color
		private Insets insets;
		
		private Graphics g;
		private int offset;
		
		public GraphicPanel(){
			this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			insets = this.getInsets();
			color = new Color[21];
			color[0] = new Color(0x0000FF);
			color[1] = new Color(0x0033FF);
			color[2] = new Color(0x0066FF);
			color[3] = new Color(0x0099FF);
			color[4] = new Color(0x00CCFF);
			color[5] = new Color(0x00FFFF);
			color[6] = new Color(0x00FFCC);
			color[7] = new Color(0x00FF99);
			color[8] = new Color(0x00FF66);
			color[9] = new Color(0x00FF33);
			color[10] = new Color(0x00FF00);
			color[11] = new Color(0x33FF00);
			color[12] = new Color(0x66FF00);
			color[13] = new Color(0x99FF00);
			color[14] = new Color(0xCCFF00);
			color[15] = new Color(0xFFFF00);
			color[16] = new Color(0xFFCC00);
			color[17] = new Color(0xFF9900);
			color[18] = new Color(0xFF6600);
			color[19] = new Color(0xFF3300);
			color[20] = new Color(0xFF0000);
			
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		
		public void initGraphic(int N){
			this.N = N;
			matrix = new double[N][N];
			//cellWidth = defaultCellWidth / (N / defaultN);
			cellWidth = 720/N;
			cellWidth = cellWidth == 0 ? 1 : cellWidth;
			setSize(N * cellWidth + insets.left + insets.right, N
					* cellWidth + insets.top + insets.bottom);
			offset = (720-cellWidth*N)/2;
		}
		
		private void drawGraphics() {
			g = this.getGraphics();
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					// convert a wave height to a color index ( 0 through to 20 )
					int index = (int) (((Double)matrix[i][j]).doubleValue()/2+10);
					index = (index > 20) ? 20 : ((index < 0) ? 0 : index);
					
					g.setColor(color[index]);
					g.fill3DRect(offset+insets.left + i * cellWidth, offset+insets.top + j
							* cellWidth, cellWidth, cellWidth, false);
					//draw a agent
					if(agents.containsKey(i*N+j)){
						g.setColor(new Color(0x000000));//black
						g.fillOval(offset+insets.left + i * cellWidth, offset+insets.top + j
								* cellWidth, cellWidth, cellWidth);
					}
				}
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {

			
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			//show detail information frame
			int x = e.getX();
			int y = e.getY();
			if(x > N*cellWidth+offset || y > N*cellWidth+offset 
					|| x < offset || y < offset){
				return;
			}
			
			x = (x-offset)/cellWidth;
			y = (y-offset)/cellWidth;
			
			
			if(x>=0 && x<matrix.length && y>=0 && y<matrix[0].length){
				System.out.println("will start microscopic window...");
				SinglePlaceAgentData cd = new SinglePlaceAgentData(x, y, (int)matrix[x][y], null);
				microFrame = microFrame == null ? MicroscopicFrame.getMicroscopicFrame() : microFrame;
				microFrame.initData(cd, debugerGuiIinstance);
				microFrame.setVisible(true);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			
			int x = e.getX();
			int y = e.getY();
			if(x > N*cellWidth+offset || y > N*cellWidth+offset 
					|| x < offset || y < offset){
				return;
			}
			
			x = (x-offset)/cellWidth;
			y = (y-offset)/cellWidth;
			
			if(x>=0 && x<matrix.length && y>=0 && y<matrix[0].length){
				ctlPanel.updateXYUI(x, y);
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {

		}
		
	}
	
	/** *************************************************************
	 * control panel
	 * *************************************************************/
	private class ControlPanel extends JPanel {
		private JLabel lb_appName;
		private JLabel lb_appName_val;
		private JLabel lb_appMode;
		private JLabel lb_appMode_val;
		
		private JLabel lb_size;
		private JLabel lb_size_val;
		
		private JLabel lb_x;
		private JLabel lb_y;
		private JLabel lb_server_ip;
		
		private JTextField tf_x;
		private JTextField tf_y;
		private JTextField tf_server_ip;
		
		private JButton bt_detail;
		private JButton bt_pause;
		private JButton bt_connect;

		public ControlPanel() {
			this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			addComponents();
			addActionsToButton();
		}
		
		private void addComponents(){
			lb_appName = new JLabel("App:");
			lb_appName_val = new JLabel();
			
			lb_appMode = new JLabel("Mode:");
			lb_appMode_val = new JLabel();
			
			lb_size = new JLabel("Size:");
			lb_size_val = new JLabel();
			
			lb_x = new JLabel("X:");
			lb_y = new JLabel("Y:");
			tf_x = new JTextField();
			tf_y = new JTextField();
			
			lb_server_ip = new JLabel("Server:");
			tf_server_ip = new JTextField("uw1-320-00.uwb.edu");
			
			bt_detail = new JButton("Detail");
			bt_pause = new JButton("Pause"); 
			bt_connect = new JButton("Connect");
			bt_detail.setEnabled(false);
			bt_pause.setEnabled(false);
			
			GroupLayout layout = new GroupLayout(this);
	        this.setLayout(layout);
	        layout.setAutoCreateGaps(true);
	        layout.setAutoCreateContainerGaps(true);
	        layout.setHorizontalGroup(layout.createSequentialGroup()
	        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	        				.addComponent(lb_appName)
	        				.addComponent(lb_appMode)
	        				.addComponent(lb_size)
	        				.addComponent(lb_x)
	        				.addComponent(lb_y)
	        				.addComponent(lb_server_ip))
	        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	        				.addComponent(lb_appName_val)
	        				.addComponent(lb_appMode_val)
	        				.addComponent(lb_size_val)
	        				.addComponent(tf_x)
	        				.addComponent(tf_y)
	        				.addComponent(bt_detail)
	        				.addComponent(tf_server_ip)
	        				.addComponent(bt_connect)
	        				.addComponent(bt_pause))
	        );
	        layout.linkSize(SwingConstants.HORIZONTAL, bt_detail, bt_connect, bt_pause);
	        layout.setVerticalGroup(layout.createSequentialGroup()
	        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	        				.addComponent(lb_appName)
	        				.addComponent(lb_appName_val))
	        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	        				.addComponent(lb_appMode)
	        				.addComponent(lb_appMode_val))
	        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	    	        		.addComponent(lb_size)
	    	        		.addComponent(lb_size_val))
	        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	    	        		.addComponent(lb_x)
	    	        		.addComponent(tf_x))
	    	        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	    	    	   		.addComponent(lb_y)
	    	    	   		.addComponent(tf_y))
	    	    	.addComponent(bt_detail)
	    	    	.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	    	    	   		.addComponent(lb_server_ip)
	    	    	   		.addComponent(tf_server_ip))
	    	    	.addComponent(bt_connect)
	    	    	.addComponent(bt_pause)
	        );
		}
		
		private void addActionsToButton(){

			bt_detail.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					int x = Integer.parseInt(tf_x.getText());
					int y = Integer.parseInt(tf_y.getText());
					//open microscopic window
					
					SinglePlaceAgentData cd = new SinglePlaceAgentData(x, y, (int)matrix[x][y], 
							agents.get(x*matrix.length+y));
					microFrame = microFrame == null ? MicroscopicFrame.getMicroscopicFrame() : microFrame;
					microFrame.initData(cd, debugerGuiIinstance);
					microFrame.setVisible(true);
				}
			});
			
			bt_pause.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(e.getActionCommand().equals("Pause")){
						dataConnection.setPauseCommand(Constants.GUI_CMD_PAUSE);
						bt_pause.setText("Resume");
					}else if(e.getActionCommand().equals("Resume")){
						dataConnection.setResumeCommand(Constants.GUI_CMD_RESUME);
						bt_pause.setText("Pause");
					}
				}
			});
			
			bt_connect.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					
					String cmd = ((JButton)e.getSource()).getText();
					if(cmd.equals("Connect")){
						//start a task to create TCP socket to connect server
						try{
							serverAddress = InetAddress.getByName(tf_server_ip.getText());
						} catch (Exception exp){
							exp.printStackTrace();
							return;
						}
						bt_connect.setEnabled(false);
						TCPClientConnection tcpConnection =  new TCPClientConnection();
						tcpConnection.execute();
					}else if(cmd.equals("Disconnect")){
						synchronized(debugerGuiIinstance){
							DataConnection.stop = true;
						}
						bt_connect.setText("Connect");
						tf_server_ip.setEditable(true);
						bt_detail.setEnabled(false);
						bt_pause.setEnabled(false);
					}
				}
			});
		}
		
		private void updateXYUI(int x, int y){
			tf_x.setText(String.valueOf(x));
			tf_y.setText(String.valueOf(y));
		}
		
		private void updateAppSpecToGUI(String name, int size){
			this.lb_appName_val.setText(name);
			this.lb_size_val.setText(String.valueOf(size));
			if(DebuggerGUI.isPlaceAgentApp){
				this.lb_appMode_val.setText("Place & Agent");
			}else{
				this.lb_appMode_val.setText("Place Only");
			}
		}
	}
	
	
	/** *****************************************************
	 * TCPClientConnection
	 * ******************************************************/
	private class TCPClientConnection extends SwingWorker<Boolean, Void>{
		private Socket socket;
		protected TCPClientConnection(){
			
		}
		
		@Override
		protected Boolean doInBackground(){
			socket = null;
			int counter = 50;
			while(counter >= 0){
				try{
					counter--;
					socket = new Socket(serverAddress, PORT);
				} catch (Exception e){
					System.err.println("No server");
				}
				if(socket != null){
					return true;
				}
			}
			return false;
		}
		
		@Override
		protected void done(){
			boolean result = false;
			try{
				result = get();
				if(result){
					//start dataConnection
					System.out.println("connected to server: "+socket.getRemoteSocketAddress());
					System.out.println("local socket address: "+socket.getLocalSocketAddress());
					
					if(dataConnection.setTCPSocket(socket)){
						DataConnection.stop = false;
						if(!DATACONNECTION_STARTED){
							dataConnection.start();
							DATACONNECTION_STARTED = true;
						}
						//update bt_connect button
						ctlPanel.bt_connect.setEnabled(true);
						ctlPanel.bt_connect.setText("Disconnect");
						ctlPanel.tf_server_ip.setEditable(false);
						ctlPanel.bt_detail.setEnabled(true);
						ctlPanel.bt_pause.setEnabled(true);
					}
				}else{
					System.out.println("set tcp socket failed!!");
					ctlPanel.bt_connect.setEnabled(true);
				}
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}


