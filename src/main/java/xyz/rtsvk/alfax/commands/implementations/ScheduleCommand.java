package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class ScheduleCommand implements Command {

	@Override
	public void handle(User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) {
		if (!user.getTag().equals("Jastrobaron#0262")) {
			chat.sendMessage("**Dobry pokus zmrde. Toto ty nemozes :)**");
		}


	}

	@Override
	public String getName() {
		return "schedule_obsolete";
	}

	@Override
	public String getDescription() {
		return "Add a scheduled command.";
	}

	@Override
	public String getUsage() {
		return "schedule <command> <time>";
	}

	@Override
	public List<String> getAliases() {
		return List.of();
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
