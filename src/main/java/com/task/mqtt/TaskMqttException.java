package com.task.mqtt;

public class TaskMqttException extends Exception {
	/**
	 * @author liufan
	 */
	private static final long serialVersionUID = 1L;
	private String reason = "BIAODA MQTT ERROR";

	public TaskMqttException(){
		super();
	}
	
	public TaskMqttException(String reason) {
		super();
		this.reason = reason;
	}
	
	public String toString() {
		return reason;
	}	

}
