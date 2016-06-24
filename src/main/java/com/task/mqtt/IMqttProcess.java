/**
 * 
 */
package com.task.mqtt;

import com.task.mqtt.IMqttProcess.MsgType;

/**
 * @author liufan
 *
 */

public interface IMqttProcess {
	public static enum MsgType {
		
	    onMessage, onPublish, onDisconnect
	}
	public void onMessage(IMqttTask client,String topic,String message);
	
	public void onPublish(IMqttTask client);
	
	public void onDisconnect(IMqttTask client);

}

class IWrapper implements Runnable{
	private String topic = "";
	private String message = "";
	private MsgType type = MsgType.onMessage;
	private IMqttProcess Instance = null;
	private IMqttTask client = null;

	public IWrapper(IMqttProcess instance,IMqttTask client,String topic,String message,MsgType t){
		this.Instance = instance;
		this.client = client;
		this.message = message;
		this.topic = topic;
		this.type = t;
	}
	public void run() {
		// TODO Auto-generated method stub
		synchronized(Instance)
		{
			switch(this.type)
			{
				case onMessage:
					Instance.onMessage(client, topic, message);
					break;
				case onPublish:
					Instance.onPublish(client);
					break;
				case onDisconnect:
					Instance.onDisconnect(client);
					break;
				default:
					System.out.println("IWrapper run error");
			}
		}
	}
	
}