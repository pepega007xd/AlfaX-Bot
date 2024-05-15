package xyz.rtsvk.alfax.webserver;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.mqtt.Mqtt;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.webserver.endpoints.*;
import xyz.rtsvk.alfax.webserver.contentparsing.IContent;
import xyz.rtsvk.alfax.webserver.contentparsing.CsvContent;
import xyz.rtsvk.alfax.webserver.contentparsing.JsonContent;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebServer extends Thread {

	/**
	 * Discord client
	 */
	private final GatewayDiscordClient client;

	/**
	 * Startup config
	 */
	private final Config cfg;

	/**
	 * MQTT client
	 */
	private final Mqtt mqtt;

	/**
	 * Map containing content types
	 */
	private final Map<String, IContent> supportedContentTypes;

	/**
	 * List of endpoints
	 */
	private final List<IEndpoint> endpoints;
	private boolean running;

	public WebServer(Config cfg, GatewayDiscordClient client) {
		this.setName("WebServer");
		this.cfg = cfg;
		this.client = client;

		this.supportedContentTypes = new HashMap<>();
		this.supportedContentTypes.put("application/json", new JsonContent());
		this.supportedContentTypes.put("application/x-www-form-urlencoded", new CsvContent());

		this.endpoints = new ArrayList<>();
		this.endpoints.add(new ChannelMessageEndpoint());
		this.endpoints.add(new DirectMessageEndpoint());
		this.endpoints.add(new GetFileEndpoint());
		this.endpoints.add(new EditMessageEndpoint());

		if (this.cfg.getBoolean("mqtt-enabled")) {
			this.mqtt = new Mqtt(cfg, client);
			this.mqtt.setClientId(cfg.getString("mqtt-web-client-id"));
			this.endpoints.add(new MqttPublishEndpoint(this.mqtt));
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

	public Map<String, IContent> getSupportedContentTypes() {
		return this.supportedContentTypes;
	}

	public List<IEndpoint> getEndpoints() {
		return this.endpoints;
	}
}
