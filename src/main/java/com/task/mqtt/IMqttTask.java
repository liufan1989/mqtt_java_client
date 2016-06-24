/**
 * 
 */
package com.task.mqtt;

/**
 * @author liufan
 *
 */
public interface IMqttTask {
		
		public void connect();
		
		public void disconnect();
		
		public void publish(String topic,String Message,int qos,boolean retain)throws TaskMqttException;
		
		public void publish(String topic,String Message)throws TaskMqttException;
		
		public void subscribe(String topic,int Qos) throws TaskMqttException;
		
		public void unsubscribe(String topic)throws TaskMqttException;
		
}
