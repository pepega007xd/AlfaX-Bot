package xyz.rtsvk.alfax.mqtt.actions;

import discord4j.core.GatewayDiscordClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface Action {
	void handle(String topic, MqttMessage message, GatewayDiscordClient client);
}
