/**
 * 
 */
package com.task.mqtt;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.task.mqtt.IMqttProcess.MsgType;

/**
 * @author liufan
 *
 */
public class TaskMqtt extends TimerTask implements IMqttTask,MqttCallback{
	
	private  boolean cleanSessionFlag = true;
	private  int keepAliveInterval = 60;
	private  int connectionTimeout = 10;
	private  int connectCheckTimes = 0;

	private String clientid = "";
	private String mqttIP = "";
	private int mqttPort = 1883;
	private String mqtturl = "";
	
	protected MqttConnectOptions options = null;
	protected MqttClient client = null;
	protected ArrayList<IMqttProcess> mqttcallbacklist = null;
	protected ArrayList<String> topiclist = null;
	protected Timer connectTimeCheck = null;
	protected ExecutorService callbackPool = null;
	
	private String generateURL(String ip,int port){
		String url = "tcp://" + ip + ":";
		url += String.valueOf(port);
		return url;
	} 
	
	public TaskMqtt(String id,String ip,int port){
		this(id, ip, port, false, 60, 10);
	}
	
	public TaskMqtt(String id,String ip,int port,boolean cs,int kp,int ct){
		if (id == null || id.equals("")){
			this.clientid = MqttClient.generateClientId();
			cs = true;
		}
		else{
			this.clientid = id;
		}
		this.setMqttIP(ip);
		this.setMqttPort(port);
		this.mqtturl = generateURL(ip,port);
		this.cleanSessionFlag = cs;
		this.keepAliveInterval = kp;
		this.connectionTimeout = ct;
		this.options = new MqttConnectOptions();
		this.options.setCleanSession(cleanSessionFlag);
		this.options.setKeepAliveInterval(keepAliveInterval);
		this.options.setConnectionTimeout(connectionTimeout);
		mqttcallbacklist = new ArrayList<IMqttProcess>();
		topiclist = new ArrayList<String>();
		connectTimeCheck = new Timer();
		callbackPool = Executors.newCachedThreadPool();
	}

	public void registerCallback(IMqttProcess imp){
		mqttcallbacklist.add(imp);
	}
	
	public void unregisterCallback(IMqttProcess imp){
		mqttcallbacklist.remove(imp);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (!client.isConnected())
		{
			System.out.println(this.clientid + " disconnected to broker!!!");
			//resource realase
			try {
				client.connect(options);
				if(cleanSessionFlag){
					for (int i = 0; i < topiclist.size();i++){
						client.subscribe(topiclist.get(i));
					}
				}
			} catch (Exception e) {
				System.out.println(this.clientid + " reconnected to broker error!");
				connectCheckTimes++;
			}
			if(connectCheckTimes == 50){
				System.gc();
				connectCheckTimes = 0;
			}
		}else{
			System.out.println(this.clientid + " connected to broker normally!");
		}
	}

	public void connect() {
		// TODO Auto-generated method stub
		int connectfail = 0;
		while (true)
		{
			try {
				client = new MqttClient(mqtturl,clientid,new MemoryPersistence());
				client.setCallback(this);
				client.connect(options);
				System.out.println(clientid + " connect to broker OK!");
				connectTimeCheck.scheduleAtFixedRate(this, 1, 10000);
				System.out.println(clientid + " start checking connection thread!");
				break;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//resource realase
				e.printStackTrace();
			} 			
			System.out.println("The No." + String.valueOf(connectfail) + " try to connect to broker!!!");
			try {
				client = null;
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(connectfail == 50){
				System.gc();
				connectfail = 0;
			}
			connectfail++;
		}
	}

	public void disconnect() {
		// TODO Auto-generated method stub
		try {
				client.disconnect();
				connectTimeCheck.cancel();
				topiclist.clear();
				mqttcallbacklist.clear();
				callbackPool.shutdown();
				client = null;
		} catch (MqttException e) {
				// TODO Auto-generated catch block
		}
	}
	
	public void publish(String topic, String Message) throws TaskMqttException{
		publish(topic,Message,2,true);
	}
	
	public void publish(String topic, String Message,int qos,boolean retain) throws TaskMqttException {
		// TODO Auto-generated method stub
		synchronized(this)
		{
			try {
				client.publish(topic, Message.getBytes(), qos, retain);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new TaskMqttException("broker is not connected !");
			} 
		}
	}
	
	public void subscribe(String topic, int qos) throws TaskMqttException{
		// TODO Auto-generated method stub
		synchronized(this)
		{
			try {
				System.out.println(client.getClientId().toString() + ":subscribe:" + topic);
				client.subscribe(topic, qos);
				topiclist.add(topic);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new TaskMqttException("broker is not connected");
			}
		}
	}

	public void unsubscribe(String topic) throws TaskMqttException {
		// TODO Auto-generated method stub
		synchronized(this)
		{
			try {
				client.unsubscribe(topic);
				topiclist.remove(topic);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new TaskMqttException("broker is not connected!");
			}
		}
	}

	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		for (int i = 0;i < mqttcallbacklist.size();i++){
			IWrapper task = new IWrapper(mqttcallbacklist.get(i),this,null,null,MsgType.onDisconnect);
			callbackPool.execute(task);
		}
	}

	public void messageArrived(String topic, MqttMessage message){
		String msg = new String(message.getPayload());
		System.out.println(client.getClientId().toString() + "messageArrived:" + topic + ":" + msg);
		// TODO Auto-generated method stub
		for (int i = 0;i < mqttcallbacklist.size();i++){
			IWrapper task = new IWrapper(mqttcallbacklist.get(i),this,topic,msg,MsgType.onMessage);
			callbackPool.execute(task);
		}
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println(token.getClient().getClientId().toString() + "publish ok!");
		// TODO Auto-generated method stub
		for (int i = 0;i < mqttcallbacklist.size();i++){
			IWrapper task = new IWrapper(mqttcallbacklist.get(i),this,null,null,MsgType.onPublish);
			callbackPool.execute(task);
		}
	}

	public String getMqttIP() {
		return mqttIP;
	}

	public void setMqttIP(String mqttIP) {
		this.mqttIP = mqttIP;
	}

	public int getMqttPort() {
		return mqttPort;
	}

	public void setMqttPort(int mqttPort) {
		this.mqttPort = mqttPort;
	}

}
