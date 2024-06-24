package xyz.rtsvk.alfax.commands;

import discord4j.discordjson.json.ApplicationCommandRequest;

public interface IApplicationCommand extends ICommand {
	ApplicationCommandRequest getCommandCreateRequest();
}
