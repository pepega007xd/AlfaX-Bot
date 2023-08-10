package xyz.rtsvk.alfax.webserver;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.mqtt.Mqtt;
import xyz.rtsvk.alfax.util.Config;

import java.net.ServerSocket;

public class WebServer extends Thread {

	private GatewayDiscordClient client;
	private Config cfg;

	public WebServer(Config cfg, GatewayDiscordClient client) {
		this.cfg = cfg;
		this.client = client;
	}

	@Override
	public void run() {
		try {
			Mqtt mqtt = new Mqtt(cfg, "Alfa-X Bot WebServer", client);
			ServerSocket srv = new ServerSocket(this.cfg.getInt("webserver-port"));
			while (true) {
				Thread handler = new Thread(new RequestHandler(this.cfg, mqtt, srv.accept(), this.client));
				handler.start();
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
