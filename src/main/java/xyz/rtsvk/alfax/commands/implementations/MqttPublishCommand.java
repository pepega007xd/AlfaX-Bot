package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.mqtt.Mqtt;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MqttPublishCommand implements Command {

	private Mqtt client;
	private final Logger logger;
	private final String prefix;

	public MqttPublishCommand(Config cfg) {
		this.logger = new Logger(this.getClass());
		this.prefix = cfg.getString("prefix");

		if (!cfg.containsKey("mqtt-enabled")) {
			logger.error("MQTT is not enabled in config, skipping MQTT initialization");
			return;
		}

		this.client = new Mqtt(cfg, "MQTT-Publish-Command", null);
		this.client.setDoSubscribe(false);
		this.client.start();
	}

	public Mqtt getClient() {
		return client;
	}

	public void handle(User user, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {
		if (!Database.checkPermissions(user.getId().asString(), Database.PERMISSION_MQTT)) {
			channel.createMessage("Nemas opravnenie na pouzitie tohto prikazu.").block();
			return;
		}

		if (args.size() < 2) {
			channel.createMessage("Usage: " + this.prefix + "mqtt <topic> <message>").block();
			return;
		}

		String topic = args.get(0);
		String message = String.join(" ", args.subList(1, args.size()));
		MqttMessage msg = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
		this.client.publish(topic, msg);
		this.logger.info("Published message to topic '" + topic + "': " + message);
	}

	@Override
	public String getName() {
		return "mqtt-publish";
	}

	@Override
	public String getDescription() {
		return "Mqtt publish command";
	}

	@Override
	public String getUsage() {
		return "mqtt <topic> <message>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("mqtt-pub", "pub");
	}
}
