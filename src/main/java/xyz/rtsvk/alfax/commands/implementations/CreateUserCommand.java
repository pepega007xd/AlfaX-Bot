package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;
import xyz.rtsvk.alfax.util.text.TextUtils;

import java.util.List;

public class CreateUserCommand implements Command {
	@Override
	public void handle(User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) throws Exception {
		String userId = user.getId().asString();
		if (Database.userExists(userId)) {
			chat.sendMessage(language.getMessage("command.register.already-registered"));
			return;
		}

		PrivateChannel dm = user.getPrivateChannel().block();
		if (dm != null) {
			String hash = TextUtils.hash(userId + System.currentTimeMillis() + Math.random());
			if (Database.addUser(userId, hash, Database.PERMISSION_NONE)) {
				chat.sendMessage(language.getMessage("command.register.success"));
				dm.createMessage(language.getFormattedString("command.register.success-dm").addParam("token", hash).build()).block();
			}
			else {
				chat.sendMessage(language.getMessage("command.register.error"));
			}
		}
		else {
			chat.sendMessage(language.getMessage("command.register.dm-error"));
		}
	}

	@Override
	public String getName() {
		return "register";
	}

	@Override
	public String getDescription() {
		return "command.register.description";
	}

	@Override
	public String getUsage() {
		return "register";
	}

	@Override
	public List<String> getAliases() {
		return List.of("reg");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
