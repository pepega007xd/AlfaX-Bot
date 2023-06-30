package xyz.rtsvk.alfax.mqtt;

import discord4j.core.GatewayDiscordClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Mqtt extends Thread {

	private GatewayDiscordClient gateway;
	private MqttClient client;

	private final String uri;
	private String uname;
	private char[] pwd;

	public Mqtt(String uri, GatewayDiscordClient gateway) {
		this.uri = uri;
		this.gateway = gateway;
	}

	public void subscribe(String topic, int qos) throws MqttException {
		this.client.subscribe(topic, qos);
	}

	public void publish(String topic, MqttMessage message) throws MqttException {
		this.client.publish(topic, message);
	}

	@Override
	public void run() {
		try {
			this.client = new MqttClient(this.uri, "AlfaX-Bot-Subscriber", new MemoryPersistence());

			MqttConnectOptions ops = new MqttConnectOptions();
			ops.setUserName(this.uname);
			ops.setPassword(this.pwd);
			ops.setCleanSession(true);

			this.client.connect(ops);
			this.client.setCallback(new MqttHandler(gateway));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setUsername(String uname) {
		this.uname = uname;
	}

	public void setPassword(char[] pwd) {
		this.pwd = pwd;
	}
}
