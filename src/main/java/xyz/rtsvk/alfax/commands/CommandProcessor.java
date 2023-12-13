package xyz.rtsvk.alfax.commands;

import java.util.*;

public class CommandProcessor {

	private final List<Command> cmds = new ArrayList<>();
	private Command fallback = null;

	public Command getCommandExecutor(String command) {
		return this.cmds.stream()
				.filter(cmd -> cmd.getName().equals(command) || cmd.getAliases().contains(command))
				.findFirst()
				.orElse(this.fallback);
	}

	public void registerCommand(Command command) {
		this.cmds.add(command);
	}

	public void setFallback(Command command) {
		this.fallback = command;
	}

	public List<Command> getCommands() {
		return cmds;
	}
}
