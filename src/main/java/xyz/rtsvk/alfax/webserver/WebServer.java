package xyz.rtsvk.alfax.webserver;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.mqtt.Mqtt;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.webserver.actions.*;
import xyz.rtsvk.alfax.webserver.contentparsing.Content;
import xyz.rtsvk.alfax.webserver.contentparsing.CsvContent;
import xyz.rtsvk.alfax.webserver.contentparsing.JsonContent;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebServer extends Thread {

	private final GatewayDiscordClient client;
	private final Config cfg;
	private final Mqtt mqtt;
	private final Map<String, Content> supportedContentTypes;
	private final List<Action> actions;
	private boolean running;

	public WebServer(Config cfg, GatewayDiscordClient client) {
		this.setName("WebServer");
		this.cfg = cfg;
		this.client = client;

		this.supportedContentTypes = new HashMap<>();
		this.supportedContentTypes.put("application/json", new JsonContent());
		this.supportedContentTypes.put("application/x-www-form-urlencoded", new CsvContent());

		this.actions = new ArrayList<>();
		this.actions.add(new ChannelMessageAction());
		this.actions.add(new DirectMessageAction());
		this.actions.add(new GetFileAction());
		this.actions.add(new EditMessageAction());

		if (this.cfg.getBoolean("mqtt-enabled")) {
			this.mqtt = new Mqtt(cfg, client);
			this.mqtt.setClientId("Alfa-X Bot WebServer");
			this.actions.add(new MqttPublishAction(this.mqtt));
		}
		else {
			this.mqtt = null;
		}
	}

	@Override
	public void run() {
		int timeout = this.cfg.getInt("webserver-timeout-ms");
		try (ServerSocket srv = new ServerSocket(this.cfg.getInt("webserver-port"))) {
			while (this.running) {
				Socket httpClient = srv.accept();
				Thread handler = new Thread(new RequestHandler(this, httpClient, timeout));
				handler.start();
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void start() {
		this.running = true;
		super.start();
	}

	public void stopWebServer() {
		this.running = false;
		this.interrupt();
	}

	public GatewayDiscordClient getDiscordClient() {
		return this.client;
	}

	public Config getConfig() {
		return this.cfg;
	}

	public Mqtt getMqtt() {
		return this.mqtt;
	}

	public Map<String, Content> getSupportedContentTypes() {
		return this.supportedContentTypes;
	}

	public List<Action> getActions() {
		return this.actions;
	}
}
