package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Database;

import java.util.List;

public class SetAnnouncementChannelCommand implements Command {

	@Override
	public void handle(User user, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {

		if (!Database.checkPermissions(user.getId().asString(), Database.PERMISSION_ADMIN)) {
			channel.createMessage("You don't have permissions to do that").block();
			return;
		}

		// syntax: ac <channel id>
		if (args.size() < 2) {
			channel.createMessage("Syntax: setannouncementchannel <channel id>").block();
			return;
		}

		String channelID = args.get(1);
		if (channelID.isEmpty()) {
			channel.createMessage("Channel ID is empty").block();
			return;
		}

		Database.setAnnouncementChannel(guildId, Snowflake.of(channelID));
		channel.createMessage("Kanal pre oznamenia bol nastaveny na <#" + channelID + ">").block();
	}

	@Override
	public String getDescription() {
		return "Set the channel where the bot will send announcements.";
	}
}
