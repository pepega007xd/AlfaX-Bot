package xyz.rtsvk.alfax.commands.sets;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.ICommandHandler;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.chat.impl.DiscordChat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandSet {
	private String name;
	private Map<String, ICommandHandler> commands;
	private final List<Thread> executors;

	/**
	 * Creates a new command set with the given name
	 * @param name the name of the set
	 */
	public CommandSet(String name) {
		this.name = name;
		this.commands = new HashMap<>();
		this.executors = new ArrayList<>();
	}

	/**
	 * Adds a command to the set
	 * @param name the name of the command
	 * @param executor command executor
	 */
	protected void addCommand(String name, ICommandHandler executor) {
		this.commands.put(name, executor);
	}

	/**
	 * Returns the executor for the command with the given name
	 * @param name the name of the command
	 * @return the executor
	 */
	public ICommandHandler getExecutor(String name) {
		return this.commands.get(name);
	}

	/**
	 * Executes the command
	 * @param commandName   command name
	 * @param user          user that queried the command execution
	 * @param messageId     ID of the message containing the query
	 * @param channel       channel the conversation is taking place in
	 * @param args          command arguments
	 * @param guildId       ID of the guild the conversation is taking place in
	 * @param bot           bot reference to use if you need to access the Discord API
	 */
	public void executeCommand(String commandName, User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) {
		Thread executor = new Thread(() -> {
			try {
				MessageManager language = Database.getUserLanguage(user.getId(), "legacy");
				getExecutor(commandName).handle(user, new DiscordChat(channel, messageId, null), args, guildId, bot, language);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		executor.start();
		this.executors.add(executor);
	}
}
