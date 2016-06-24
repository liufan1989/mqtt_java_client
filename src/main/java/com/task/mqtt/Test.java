/**
 * 
 */
package com.task.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

/**
 * @author liufan
 *
 */
public class Test implements IMqttProcess {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
//		TaskMqtt a = new TaskMqtt("liufan_client","192.168.1.253",1883,true,60,10);
//		System.out.println(a.toString());
//		Test b = new Test();
//		a.registerCallback(b);
////		for (int i = 0;i < 1000;i++){
////			Test b = new Test();
////			a.registerCallback(b);
////		}
//		a.connect();
//		try {
//			a.subscribe("biaoda", 0);
//			//a.unsubscribe("biaoda");
//		} catch (TaskMqttException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		Test b = new Test();
		for (int i=0;i< 1000;i++){
			TaskMqtt a = new TaskMqtt("biaoda_client_" + String.valueOf(i),"192.168.1.241",1883, true, 600, 10);
			a.registerCallback(b);
			a.connect();
			try {
				a.subscribe("biaoda", 2);
			} catch (TaskMqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void onMessage(IMqttTask client,String topic, String message) {
		// TODO Auto-generated method stub
		if(message.equals("liufan")){
			System.out.println("receive liufan message subscribe topic:liufan!");
			try {
				client.subscribe("liufan", 0);
			} catch (TaskMqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(client.toString());
		System.out.println("onMessage:" + topic+ ":" + message);
	}

	public void onPublish(IMqttTask client) {
		// TODO Auto-generated method stub
		System.out.println("onPublish");
	}

	public void onDisconnect(IMqttTask client) {
		// TODO Auto-generated method stub
		System.out.println("onDisconnect");
	}

}
