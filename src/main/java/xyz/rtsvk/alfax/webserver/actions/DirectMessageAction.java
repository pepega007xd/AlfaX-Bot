package xyz.rtsvk.alfax.webserver.actions;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import xyz.rtsvk.alfax.webserver.Request;
import xyz.rtsvk.alfax.webserver.Response;

public class DirectMessageAction implements Action {
	@Override
	public String handle(GatewayDiscordClient client, Request request) {

		String userID = request.getProperty("user_id").toString();
		String msg = request.getProperty("message").toString();
		if (msg.length() == 0) msg = "Message not supplied";

		User user = client.getUserById(Snowflake.of(userID)).block();
		if (user == null) return Response.RESP_404_NOT_FOUND;

		PrivateChannel channel = user.getPrivateChannel().block();
		if (channel == null) return Response.RESP_404_NOT_FOUND;
		channel.createMessage(msg).block();

		return Response.RESP_200_OK;
	}
}
