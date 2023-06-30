package xyz.rtsvk.alfax.webserver.actions;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.webserver.Request;
import xyz.rtsvk.alfax.webserver.Response;

public class DirectMessageAction implements Action {
	@Override
	public String handle(GatewayDiscordClient client, Request request) {
		return Response.RESP_404_NOT_FOUND;
	}
}
