package xyz.rtsvk.alfax.webserver.actions;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.webserver.Request;
import xyz.rtsvk.alfax.webserver.Response;

public class SendMessageAction implements Action {
	@Override
	public ActionResult handle(GatewayDiscordClient client, Request request) {

		String channelID = request.getProperty("channel_id").toString();
		String msg = request.getProperty("message").toString();

		try {
			client.rest().getChannelById(Snowflake.of(channelID)).createMessage(msg).subscribe();
		} catch (Exception e) {
			return new ActionResult(Response.RESP_500_ERROR, e.getMessage());
		}

		return new ActionResult(Response.RESP_200_OK, "Message sent");
	}
}
