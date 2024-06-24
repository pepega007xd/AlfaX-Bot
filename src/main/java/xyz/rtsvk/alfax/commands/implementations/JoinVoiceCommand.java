package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class JoinVoiceCommand implements ICommand {
	@Override
	public void handle(User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) throws Exception {
		Member member = user.asMember(guildId).block();
		if (member == null) {
			chat.sendMessage("Neexistujes bratu :skull:");
			return;
		}

		VoiceState state = member.getVoiceState().block();
		if (state == null) {
			chat.sendMessage("Nie si vo voice roomke!");
			return;
		}


	}

	@Override
	public String getName() {
		return "join";
	}

	@Override
	public String getDescription() {
		return "Joins the voice channel you are currently in";
	}

	@Override
	public String getUsage() {
		return "join";
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
