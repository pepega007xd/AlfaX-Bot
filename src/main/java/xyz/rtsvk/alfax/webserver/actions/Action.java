package xyz.rtsvk.alfax.webserver.actions;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.webserver.Request;

public interface Action {
	String handle(GatewayDiscordClient client, Request request);
}
