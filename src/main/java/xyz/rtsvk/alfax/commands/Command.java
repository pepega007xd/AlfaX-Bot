package xyz.rtsvk.alfax.commands;

import java.util.List;

public interface Command extends CommandHandler {
	/**
	 * @return the command name
	 */
	String getName();
	/**
	 * @return the command description
	 */
	String getDescription();
	/**
	 * @return the command usage
	 */
	String getUsage();
	/**
	 * @return the command aliases
	 */
	List<String> getAliases();
	/**
	 * @return the command cooldown
	 */
	int getCooldown();
}
