package xyz.rtsvk.alfax.mqtt;

import discord4j.core.GatewayDiscordClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import xyz.rtsvk.alfax.util.Logger;

public class MqttHandler implements MqttCallback {

	private final Logger logger;
	private final GatewayDiscordClient gateway;

	public MqttHandler(GatewayDiscordClient gateway) {
		this.logger = new Logger(this.getClass());
		this.gateway = gateway;
	}

	@Override
	public void connectionLost(Throwable cause) {
		this.logger.error("Connection lost! " + cause.getLocalizedMessage());
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {

	}
}
