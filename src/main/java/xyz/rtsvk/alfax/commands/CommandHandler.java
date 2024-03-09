package xyz.rtsvk.alfax.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public interface CommandHandler {
	/**
	 * Command executor method prototype
	 *
	 * @param user     user that queried the command execution
	 * @param chat     ID of the message containing the command execution query
	 * @param args     command arguments
	 * @param guildId  ID of the guild the conversation is taking place
	 * @param bot      bot reference, just to interact with the Discord API if needed
	 * @param language language manager to get localized messages
	 * @throws Exception if something goes wrong during command execution
	 */
	void handle(User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) throws Exception;
}
