package xyz.rtsvk.alfax.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.chat.impl.DiscordChat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.*;

public class CommandProcessor {
	public static final Character SEPARATOR = ' ';
	public static final List<Character> QUOTES = List.of('"', '\'');
	private final List<Command> commands = new ArrayList<>();
	private Command fallback = null;

	public Command getCommandExecutor(String command) {
		return this.getCommandExecutor(command, this.fallback);
	}

	public Command getCommandExecutor(String command, Command fallback) {
		return this.commands.stream()
				.filter(cmd -> cmd.getName().equals(command) || cmd.getAliases().contains(command))
				.findFirst().orElse(fallback);
	}

	public void executeCommand(String command, User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) {
		Command cmd = this.getCommandExecutor(command);
		MessageManager language = Database.getUserLanguage(user.getId(), "legacy");
		if (cmd == null) {
			cmd = this.fallback;
		}
		try {
			cmd.handle(user, new DiscordChat(channel, messageId), args, guildId, bot, language);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String> splitCommandString(String input) {
		List<String> output = new LinkedList<>();
		StringBuilder token = new StringBuilder();
		int state = 0;

		int index = 0;
		while (index < input.length()) {
			char currentChar = input.charAt(index++);
			switch (state) {
				case 0:
					if (currentChar == SEPARATOR) state = 1;
					else token.append(currentChar);
					break;

				case 1:
					if (!token.isEmpty()) {
						output.add(token.toString());
						token.setLength(0);
					}
					if (QUOTES.contains(currentChar)) {
						state = 2;
					}
					else {
						token.append(currentChar);
						state = 0;
					}
					break;

				case 2:
					if (currentChar == '\\') state = 3;
					else if (QUOTES.contains(currentChar)) {
						if (!token.isEmpty()) {
							output.add(token.toString());
							token.setLength(0);
						}
						state = 0;
					} else token.append(currentChar);
					break;

				case 3:
					token.append(currentChar);
					state = 2;
					break;
			}
		}

		if (!token.isEmpty())
			output.add(token.toString());

		return output.stream().map(String::trim).toList();
	}

	public void registerCommand(Command command) {
		this.commands.add(command);
	}

	public void setFallback(Command command) {
		this.fallback = command;
	}

	public List<Command> getCommands() {
		return commands;
	}
	public Command getFallback() {
		return this.fallback;
	}
}
