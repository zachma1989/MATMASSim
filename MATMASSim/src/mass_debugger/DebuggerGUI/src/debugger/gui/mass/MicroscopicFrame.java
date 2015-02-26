package debugger.gui.mass;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import MASS.SinglePlaceAgentData;


/***********UI***********
 *      x:___
 *      y:___ 
 *  Value:___
 * Agents:[     ] ___ 
 * Submit      Cancel
 * */

/**
 * 
 * show the data of one place and the agents informations in this place.
 * user can modify the data of place and agent and then send the change to 
 * back-end computation node
 * */

public class MicroscopicFrame extends JFrame{
	
	private MicroscopicPanel mPanel;
	private SinglePlaceAgentData combineData;
	private static MicroscopicFrame instance = null;
	private DataEventListener listener;
	
	private MicroscopicFrame(){
		mPanel = new MicroscopicPanel();
		add(mPanel, BorderLayout.CENTER);
		setTitle("Detail Information");
		setSize(300, 220);
	}
	
	public static MicroscopicFrame getMicroscopicFrame(){
		if(instance == null){
			instance = new MicroscopicFrame();
		}
		return instance;
	}
	
	public void initData(SinglePlaceAgentData combineData, DataEventListener listener){
		this.combineData = combineData;
		mPanel.assignValueToComponent(combineData);
		this.listener = listener;
	}
	
	public SinglePlaceAgentData getData(){
		int x = Integer.parseInt(mPanel.label_x_val.getText());
		int y = Integer.parseInt(mPanel.label_y_val.getText());
		int place_v = Integer.parseInt(mPanel.textField_place_val.getText());
		SinglePlaceAgentData spaData = new SinglePlaceAgentData(x, y, place_v, null);
		return spaData;
	}
	
	
	private class MicroscopicPanel extends JPanel{
		private JLabel label_x;
		private JLabel label_y;
		private JLabel label_place;
		private JLabel label_agent;
		
		private JLabel label_x_val;
		private JLabel label_y_val;
		private JTextField textField_place_val;
		private JTextField textField_agent_val;
		
		private JComboBox<Integer> combox_agents;
		
		private JButton button_submit;
		private JButton button_cancel;
		
		private MicroscopicPanel(){
			initComponents();
			addActionsToButton();
		}
		
		private void initComponents(){
			label_x = new JLabel("X:");
			label_y = new JLabel("Y:");
			label_x_val = new JLabel();
			label_y_val = new JLabel();
			label_place = new JLabel("Place");
			label_agent = new JLabel("Agents");
			textField_place_val = new JTextField();
			textField_agent_val = new JTextField();
			combox_agents = new JComboBox<Integer>();
			button_submit = new JButton("Submit");
			button_cancel = new JButton("Cancel");
			textField_agent_val.setSize(5, 5);
			
			GroupLayout layout = new GroupLayout(this);
	        this.setLayout(layout);
	        layout.setAutoCreateGaps(true);
	        layout.setAutoCreateContainerGaps(true);
	        layout.setHorizontalGroup(layout.createSequentialGroup()
	        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	        				.addComponent(label_x)
	        				.addComponent(label_y)
	        				.addComponent(label_place)
	        				.addComponent(label_agent))
	        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	        				.addComponent(label_x_val)
	        				.addComponent(label_y_val)
	        				.addComponent(textField_place_val)
	        				.addComponent(textField_agent_val)
	        				.addComponent(button_submit))
	        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	        				.addComponent(combox_agents)
	        				.addComponent(button_cancel))
	        );
	        layout.setVerticalGroup(layout.createSequentialGroup()
	        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	        				.addComponent(label_x)
	        				.addComponent(label_x_val))
	        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	    	        		.addComponent(label_y)
	    	        		.addComponent(label_y_val))
	    	        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	    	    	   		.addComponent(label_place)
	    	    	   		.addComponent(textField_place_val))
	    	    	.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	    	    	   		.addComponent(label_agent)
	    	    	   		.addComponent(textField_agent_val)
	    	    	   		.addComponent(combox_agents))
	    	    	.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	    	    			.addComponent(button_submit)
	    	    			.addComponent(button_cancel))
	        );
		}
		
		private void addActionsToButton(){
			button_cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e){
					instance.setVisible(false);
				}
			});
			
			button_submit.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e){
					
					SinglePlaceAgentData spaData = instance.getData();
					listener.sendInjectData(spaData);
					instance.setVisible(false);
				}
			});
		}
		
		private void assignValueToComponent(SinglePlaceAgentData cd){
			this.label_x_val.setText(String.valueOf(cd.getX()));
			this.label_y_val.setText(String.valueOf(cd.getY()));
			this.textField_place_val.setText(String.valueOf(cd.getPlace_val()));
			for(int i=0; i<cd.getAgents().size(); i++){
				this.combox_agents.addItem(cd.getAgents().get(i));
			}
			this.combox_agents.setSelectedIndex(-1);
		}
	}
}
