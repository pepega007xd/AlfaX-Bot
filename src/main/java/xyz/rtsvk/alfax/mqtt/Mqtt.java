package xyz.rtsvk.alfax.mqtt;

import discord4j.core.GatewayDiscordClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import xyz.rtsvk.alfax.mqtt.actions.Action;
import xyz.rtsvk.alfax.mqtt.actions.SendAlertAction;
import xyz.rtsvk.alfax.mqtt.actions.SensorDataReceivedAction;
import xyz.rtsvk.alfax.util.Config;

import java.util.HashMap;
import java.util.Map;

public class Mqtt extends Thread {

	private GatewayDiscordClient gateway;
	private MqttClient client;

	private final String uri;
	private final String clientid;
	private String uname;
	private char[] pwd;
	private boolean doSubscribe = true;

	public Mqtt(Config cfg, String clientid, GatewayDiscordClient gateway) {

		this.uri = cfg.getStringOrDefault("mqtt-uri", null);
		this.uname = cfg.getStringOrDefault("mqtt-username", null);
		this.pwd = cfg.getStringOrDefault("mqtt-password", null).toCharArray();

		this.gateway = gateway;
		this.clientid = clientid;
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
			this.client = new MqttClient(this.uri, this.clientid, new MemoryPersistence());

			MqttConnectOptions ops = new MqttConnectOptions();
			ops.setCleanSession(true);
			if (this.uname != null && this.pwd != null){
				ops.setUserName(this.uname);
				ops.setPassword(this.pwd);
			}

			this.client.connect(ops);

			if (this.doSubscribe) {
				Map<String, Action> actionMap = new HashMap<>();

				actionMap.put("alert", new SendAlertAction());
				actionMap.put("sensor", new SensorDataReceivedAction());

				this.client.setCallback(new MqttHandler(this.gateway, actionMap));
				for (String topic : actionMap.keySet())
					this.client.subscribe(topic, 0);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setDoSubscribe(boolean doSubscribe) {
		this.doSubscribe = doSubscribe;
	}
}
