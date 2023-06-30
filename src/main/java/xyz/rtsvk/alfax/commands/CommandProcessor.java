package xyz.rtsvk.alfax.commands;

import java.util.*;

public class CommandProcessor {

	private static Map<String, Command> cmds = new HashMap<>();
	private static Map<String, String> aliases = new HashMap<>();

	public static Command getCommandExecutor(String command) {
		String orig = ""; // there might be a chain of aliases, so let's get to the root command
		while ((orig = aliases.get(command)) != null) command = orig;
		return cmds.get(command);
	}

	public static void registerCommand(String commandName, Command callback) {
		cmds.put(commandName, callback);
	}

	public static void registerCommandAlias(String alias, String originalCommand) {
		aliases.put(alias, originalCommand);
	}

	public static Map<String, Command> getCommands() {
		return cmds;
	}
}
