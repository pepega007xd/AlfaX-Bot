package xyz.rtsvk.alfax.commands;

import java.util.List;

public interface Command extends CommandHandler {
	String getName();
	String getDescription();
	String getUsage();
	List<String> getAliases();
	int getCooldown();
}
