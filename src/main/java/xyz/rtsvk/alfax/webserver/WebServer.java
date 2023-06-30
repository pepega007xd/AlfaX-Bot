package xyz.rtsvk.alfax.webserver;

import discord4j.core.GatewayDiscordClient;

import java.net.ServerSocket;

public class WebServer extends Thread {

	private int port;
	private GatewayDiscordClient client;

	public WebServer(int port, GatewayDiscordClient client) {
		this.port = port;
		this.client = client;
	}

	@Override
	public void run(){
		try {
			ServerSocket srv = new ServerSocket(this.port);
			while (true) {
				Thread handler = new Thread(new RequestHandler(srv.accept(), this.client));
				handler.start();
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
