package xyz.rtsvk.alfax.webserver;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.webserver.actions.Action;
import xyz.rtsvk.alfax.webserver.actions.DirectMessageAction;
import xyz.rtsvk.alfax.webserver.actions.SendMessageAction;
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

	private Map<String, Content> supportedContentTypes;
	private Map<String, Action> actions;

	public RequestHandler(Socket s, GatewayDiscordClient client) throws IOException {
		this.skt = s;
		this.client = client;

		this.in = new BufferedInputStream(s.getInputStream());
		this.out = new PrintWriter(s.getOutputStream());

		this.supportedContentTypes = new HashMap<>();
		this.supportedContentTypes.put("application/json", new JsonContent());
		this.supportedContentTypes.put("application/x-www-form-urlencoded", new FormContent());

		this.actions = new HashMap<>();
		this.actions.put("channel_message", new SendMessageAction());
		this.actions.put("direct_message", new DirectMessageAction());
	}

	@Override
	public void run() {
		try {
			System.out.println("Incoming request from " + this.skt.getRemoteSocketAddress());
			this.skt.setSoTimeout(5000);

			String data = readStream(this.in);
			Request request = Request.parse(data, this.supportedContentTypes);

			if (request == null)
				this.out.println("HTTP/1.1 " + Response.RESP_500_ERROR);

			else if (!request.getRequestMethod().equals("POST"))
				this.out.println(request.getProtocolVersion() + " " + Response.RESP_501_NOT_IMPLEMENTED);

			else {

				this.out.print(request.getProtocolVersion() + " ");
				if (!Database.authorizeAPIUser(request.getProperty("auth_key")))
					this.out.println(Response.RESP_401_FORBIDDEN);

				else {
					Action action = this.actions.get(request.getPath().substring(1));

					if (action == null)
						this.out.println(Response.RESP_404_NOT_FOUND);
					else this.out.println(action.handle(this.client, request));
				}
			}

			StringBuilder message = new StringBuilder("Lorem ipsum dolor sit amet.\n");
			if (request != null) request.getProperties().forEach((k,v) -> message.append(k + " = " + v + "\n"));

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

	private String readStream(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();

		while (in.available() == 0);
		while (in.available() > 0) {
			byte[] buf = new byte[64];
			int read = in.read(buf);
			sb.append(new String(buf, 0, read));
		}

		return sb.toString();
	}
}
