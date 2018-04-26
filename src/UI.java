import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class UI extends JFrame implements ActionListener{
	
	private MqttClient mqtt;
    
    private JLabel serverIP = new JLabel("Host主机");
    private JTextField edtServerIP = new JTextField(20);
    
    private JLabel port = new JLabel("Port端口");
    private JTextField edtPort = new JTextField(10);
    
    private JButton connect = new JButton("连接Connect");
    
    private JLabel username = new JLabel("Username用户名");
    private JTextField edtUsername = new JTextField(10);
    
    private JLabel password = new JLabel("Password密码");
    private JTextField edtPassword = new JTextField(10);
    
    private JLabel clientID = new JLabel("Client ID");
    private JTextField edtClientID = new JTextField(10);
    
    private JLabel keepAlive = new JLabel("Keep-Alive保活");
    private JTextField edtKeepAlive = new JTextField(10);   
    
    private JLabel timeout = new JLabel("Timeout");
    private JTextField edtTimeout = new JTextField(10);  
    
    private JLabel cleanSession = new JLabel("Clean Session");
    private JCheckBox ckCleanSession = new JCheckBox();
    
    private JLabel subtopic = new JLabel("SubTopic订阅主题");
    private JTextField edtSubtopic = new JTextField(10);
    private JLabel subQoS = new JLabel("QoS服务质量");
    private JComboBox cbSubQoS = new JComboBox();
    private JButton subscribe = new JButton("Subscribe订阅");
    private JButton unsubscribe = new JButton("Unsubscribe取消订阅");
    
    private JLabel pubtopbic = new JLabel("PubTopic发布主题");
    private JTextField edtPubtopic = new JTextField(10);
    private JLabel retain = new JLabel("Retain");
    private JCheckBox ckRetain = new JCheckBox();
    private JLabel pubQoS = new JLabel("QoS服务质量");
    private JComboBox cbPubQoS = new JComboBox();
    private JLabel message = new JLabel("Message 消息");
    private JTextField edtMessage = new JTextField(10);
    private JButton publish = new JButton("发布");
    
   
    
    private static JTextArea result = new JTextArea(15,30);
    
    private JLabel welcome = new JLabel("欢迎使用MQTT客户端");
    private JLabel status = new JLabel("当前状态:未连接");
    
    UI(){
    	this.setTitle("MQTTClient");
    	JPanel southPane = new JPanel();
    	JPanel northPane = new JPanel();
    	JPanel centerPane = new JPanel();
    	JPanel eastPane = new JPanel();
    	JScrollPane scrollPane = new JScrollPane();
    	
    	this.getContentPane().add(southPane, BorderLayout.SOUTH);
    	this.getContentPane().add(northPane, BorderLayout.NORTH);
    	this.getContentPane().add(centerPane, BorderLayout.CENTER);
    	this.getContentPane().add(eastPane, BorderLayout.EAST);
    	this.getContentPane().add(scrollPane, BorderLayout.WEST);
     

    	southPane.add(status);
    	northPane.add(welcome);
    	
        final FlowLayout flowLayout = new FlowLayout();  
        flowLayout.setHgap(10);  
        flowLayout.setVgap(10);
        centerPane.setLayout(flowLayout);
    	JPanel pane1 = new JPanel();
    	pane1.setLayout(new GridLayout(0,2));
    	pane1.add(serverIP);
    	pane1.add(edtServerIP);
    	pane1.add(port);
    	pane1.add(edtPort);
    	pane1.add(username);
    	pane1.add(edtUsername);
    	pane1.add(password);
    	pane1.add(edtPassword);
    	pane1.add(clientID);
    	pane1.add(edtClientID);
    	pane1.add(keepAlive);
    	pane1.add(edtKeepAlive);
    	pane1.add(timeout);
    	pane1.add(edtTimeout);
    	pane1.add(cleanSession);
    	pane1.add(ckCleanSession);
    	pane1.add(connect);
    	connect.addActionListener(this);
    	pane1.add(new JLabel());
    	centerPane.add(pane1);
    
    	
        final FlowLayout flowLayout2 = new FlowLayout();  
        flowLayout2.setHgap(10);  
        flowLayout2.setVgap(10);
    	eastPane.setLayout(flowLayout2);
    	JPanel pane2 = new JPanel();
    	pane2.setLayout(new GridLayout(0,2));
    	pane2.add(subtopic);
    	pane2.add(edtSubtopic);
    	pane2.add(subQoS);
    	cbSubQoS.addItem("0 最多一次的传输");
    	cbSubQoS.addItem("1 至少一次的传输");
    	cbSubQoS.addItem("2 只有一次的传输");
    	pane2.add(cbSubQoS);
    	pane2.add(subscribe);
    	subscribe.addActionListener(this);
    	pane2.add(unsubscribe);
    	unsubscribe.addActionListener(this);
    	
    	pane2.add(pubtopbic);
    	pane2.add(edtPubtopic);
    	pane2.add(pubQoS);
    	cbPubQoS.addItem("0 最多一次的传输");
    	cbPubQoS.addItem("1 至少一次的传输");
    	cbPubQoS.addItem("2 只有一次的传输");
    	pane2.add(cbPubQoS);
    	pane2.add(retain);
    	pane2.add(ckRetain);
    	pane2.add(message);
    	pane2.add(edtMessage);
    	pane2.add(publish);
    	publish.addActionListener(this);
        eastPane.add(pane2);
    	
    	    
    	result.setTabSize(4);    
    	result.setLineWrap(true);// 激活自动换行功能  
    	result.setWrapStyleWord(true);
    	result.setBackground(Color.white);
    	scrollPane.setViewportView(result);
    	this.setVisible(true);
    }
    
    
    
    public static void main(String[] argS) {
    	UI ui = new UI();
        ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       // ui.setExtendedState(Frame.MAXIMIZED_BOTH);
        ui.setSize(1150, 400);
        ui.setLocationRelativeTo(null);
    }



	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == this.connect) {
			if (this.connect.getText().toString().equals("连接Connect")) {
				mqtt = new MqttClient(this.edtServerIP.getText().toString(), Integer.valueOf(this.edtPort.getText()));
		        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		        mqttConnectOptions.setClientID(this.edtClientID.getText().toString());
		        mqttConnectOptions.setUsername(this.edtUsername.getText().toString());
		        mqttConnectOptions.setPassword(this.edtPassword.getText().toString());
		        mqttConnectOptions.setCleanSession(this.ckCleanSession.isSelected());
		        mqttConnectOptions.setKeepalive(Short.valueOf(this.edtKeepAlive.getText()));
		        mqtt.setMqttConnectOptions(mqttConnectOptions);
		//		int timeout = Integer.valueOf(this.edtTimeout.getText());
				mqtt.setMqttCallback(new MqttCallback() {

	                @Override
	                public void connectionLost(Throwable cause) {
	                  //  System.out.println("connectionLost-----------");
	                	UI.result.append(new java.util.Date()+":");
	                	UI.result.append("connectionLost-----------\n");
	                }

	                @Override
	                public void deliveryComplete(String message) {
	                 //   System.out.println("deliveryComplete---------"
	                 //           + token.isComplete());
	                	UI.result.append(new java.util.Date()+":");
	                	UI.result.append(message);
	                }

	                @Override
	                public void messageArrived(String message) {
	                  //  System.out.println("messageArrived----------");
	                  //  System.out.println(arg1.toString());
	                	UI.result.append(new java.util.Date()+":");
	                	UI.result.append("messageArrived:"+"message: "+message.toString()+"\n");
	                }
	            });
				//this.edtResult.setText(mqtt.isConnect()+"");
			//	System.out.println(mqtt.isConnect());
				mqtt.connect();
				this.status.setText("当前状态:已连接");
				this.connect.setText("断开连接Disconnect");
			}
			else {
				mqtt.disconnect();
				this.result.append(new java.util.Date()+":");
				this.result.append("Disconnect\n");
				this.status.setText("当前状态:未连接");
				this.connect.setText("连接Connect");
			}
		}
		else if(e.getSource() == this.publish) {
			mqtt.publish(this.edtPubtopic.getText(), this.cbPubQoS.getSelectedIndex(), this.edtMessage.getText(), this.ckRetain.isSelected());
		}
		else if(e.getSource() == this.subscribe){
			mqtt.subscribe(this.edtSubtopic.getText(), this.cbSubQoS.getSelectedIndex());
		}
		else if(e.getSource() == this.unsubscribe){
			mqtt.unsubscribe(this.edtSubtopic.getText());
		}
	}
}
