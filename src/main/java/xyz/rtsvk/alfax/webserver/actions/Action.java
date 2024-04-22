package xyz.rtsvk.alfax.webserver.actions;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.webserver.Request;

import java.util.List;

public interface Action {
	ActionResult handle(GatewayDiscordClient client, Request request);
	byte getRequiredPermissions();
	List<String> getRequiredArgs();
	String getEndpointName();
	List<Request.Method> getAllowedRequestMethods();
}
