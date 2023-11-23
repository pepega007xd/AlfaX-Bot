package xyz.rtsvk.alfax.commands;

import java.util.*;

public class CommandProcessor {

	private Map<String, Command> cmds = new HashMap<>();
	private Map<String, String> aliases = new HashMap<>();

	public Command getCommandExecutor(String command) {
		String orig = ""; // there might be a chain of aliases, so let's get to the root command
		while ((orig = aliases.get(command)) != null) command = orig;
		return cmds.get(command);
	}

	public void registerCommand(String commandName, Command callback) {
		cmds.put(commandName, callback);
	}

	public void registerCommandAlias(String alias, String originalCommand) {
		aliases.put(alias, originalCommand);
	}

	public Map<String, Command> getCommands() {
		return cmds;
	}
}
