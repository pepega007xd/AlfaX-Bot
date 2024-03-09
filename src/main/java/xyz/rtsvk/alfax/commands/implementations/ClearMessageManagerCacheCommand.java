package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class ClearMessageManagerCacheCommand implements Command {

	private Logger logger = new Logger(ClearMessageManagerCacheCommand.class);

	@Override
	public void handle(User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) throws Exception {
		if (!Database.checkPermissions(user.getId(), Database.PERMISSION_ADMIN)) {
			chat.sendMessage("You don't have permission to use this command!");
			return;
		}
		MessageManager.clearCache();
		chat.sendMessage("Cache cleared!");
		this.logger.info("Cleared message manager cache...");
	}

	@Override
	public String getName() {
		return "clear-message-manager-cache";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getUsage() {
		return "clear-message-manager-cache";
	}

	@Override
	public List<String> getAliases() {
		return List.of("clear-msg-cache");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
