package xyz.rtsvk.alfax.webserver.actions;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.webserver.Request;
import xyz.rtsvk.alfax.webserver.Response;

public class SendMessageAction implements Action {
	@Override
	public ActionResult handle(GatewayDiscordClient client, Request request) {

		String key = request.getProperty("auth_key").toString();
		if (!Database.checkPermissionsByKey(key, Database.PERMISSION_API_CHANNEL))
			return new ActionResult(Response.RESP_403_FORBIDDEN, "You don't have permissions to do that");

		String channelID = request.getProperty("channel_id").toString();
		String msg = request.getProperty("message").toString();
		if (msg.length() == 0)
			return new ActionResult(Response.RESP_400_BAD_REQUEST, "Message is empty");

		client.rest().getChannelById(Snowflake.of(channelID)).createMessage(msg).subscribe();

		return new ActionResult(Response.RESP_200_OK, "Message sent");
	}
}
