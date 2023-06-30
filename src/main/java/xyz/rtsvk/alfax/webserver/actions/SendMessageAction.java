package xyz.rtsvk.alfax.webserver.actions;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.webserver.Request;
import xyz.rtsvk.alfax.webserver.Response;

public class SendMessageAction implements Action {
	@Override
	public String handle(GatewayDiscordClient client, Request request) {

		String channelID = request.getProperty("channel_id").toString();
		String msg = request.getProperty("message").toString();
		if (msg.length() == 0) msg = "Message not supplied";
		client.rest().getChannelById(Snowflake.of(channelID)).createMessage(msg).subscribe();

		return Response.RESP_200_OK;
	}
}
