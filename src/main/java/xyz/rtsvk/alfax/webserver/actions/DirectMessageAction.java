package xyz.rtsvk.alfax.webserver.actions;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.webserver.Request;
import xyz.rtsvk.alfax.webserver.Response;

public class DirectMessageAction implements Action {
	@Override
	public ActionResult handle(GatewayDiscordClient client, Request request) {

		String key = request.getProperty("auth_key").toString();
		if (!Database.checkPermissionsByKey(key, Database.PERMISSION_API_DM))
			return new ActionResult(Response.RESP_403_FORBIDDEN, "You don't have permission to do that");

		String userID = request.getProperty("user_id").toString();
		String msg = request.getProperty("message").toString();
		if (msg.isEmpty()) msg = "Message not supplied";

		User user = client.getUserById(Snowflake.of(userID)).block();
		if (user == null)
			return new ActionResult(Response.RESP_404_NOT_FOUND, "User not found");

		PrivateChannel channel = user.getPrivateChannel().block();
		if (channel == null)
			return new ActionResult(Response.RESP_404_NOT_FOUND, "Unable to open DM channel");
		channel.createMessage(msg).block();

		return new ActionResult(Response.RESP_200_OK, "Message sent");
	}
}
