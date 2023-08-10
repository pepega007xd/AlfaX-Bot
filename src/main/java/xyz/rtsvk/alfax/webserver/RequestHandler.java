package xyz.rtsvk.alfax.webserver;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.mqtt.Mqtt;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.webserver.actions.*;
import xyz.rtsvk.alfax.webserver.contentparsing.Content;
import xyz.rtsvk.alfax.webserver.contentparsing.FormContent;
import xyz.rtsvk.alfax.webserver.contentparsing.JsonContent;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler implements Runnable {

	private Socket skt;

	private BufferedInputStream in;
	private PrintWriter out;
	private GatewayDiscordClient client;
	private Logger logger;

	private Map<String, Content> supportedContentTypes;
	private Map<String, Action> actions;
	private Config cfg;

	public RequestHandler(Config cfg, Mqtt mqtt, Socket s, GatewayDiscordClient client) throws IOException {
		this.cfg = cfg;
		this.skt = s;
		this.client = client;
		this.logger = new Logger(this.getClass());

		this.in = new BufferedInputStream(s.getInputStream());
		this.out = new PrintWriter(s.getOutputStream());

		this.supportedContentTypes = new HashMap<>();
		this.supportedContentTypes.put("application/json", new JsonContent());
		this.supportedContentTypes.put("application/x-www-form-urlencoded", new FormContent());

		this.actions = new HashMap<>();
		this.actions.put("channel_message", new SendMessageAction());
		this.actions.put("direct_message", new DirectMessageAction());
		if (cfg.getBooleanOrDefault("mqtt-enabled", false))
			this.actions.put("mqtt_publish", new MqttPublishAction(mqtt));
	}

	@Override
	public void run() {
		try {
			this.logger.info("Incoming request from " + this.skt.getRemoteSocketAddress());
			this.skt.setSoTimeout(10000);

			Request request = Request.parseRequest(this.in, this.supportedContentTypes);
			String message = "Server responeded with an error";

			if (request == null)
				this.out.println(Response.RESP_500_ERROR);

			else if (!request.getRequestMethod().equals("POST"))
				this.out.println(request.getProtocolVersion() + " " + Response.RESP_501_NOT_IMPLEMENTED);

			else {

				this.out.print(request.getProtocolVersion() + " ");
				Action action = this.actions.get(request.getPath().substring(1));

				if (action == null)
					this.out.println(Response.RESP_404_NOT_FOUND);
				else {
					ActionResult result = action.handle(this.client, request);
					this.out.println(result.getStatus());
					message = result.getMessage();
				}
			}

			/*StringBuilder message = new StringBuilder("Lorem ipsum dolor sit amet.\n");
			if (request != null) request.getProperties().forEach((k,v) -> message.append(k + " = " + v + "\n"));*/

			this.out.println("Content-type: text/plain");
			this.out.println("Content-length: " + message.length());
			this.out.println();
			this.out.println(message);
			this.out.println();

			this.out.close();
			this.in.close();
			this.skt.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
