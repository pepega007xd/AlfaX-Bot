package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;
import xyz.rtsvk.alfax.util.text.TextUtils;

import java.util.List;

public class CreateUserCommand implements Command {
	@Override
	public void handle(User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) throws Exception {
		String userId = user.getId().asString();
		if (Database.userExists(userId)) {
			chat.sendMessage("Uz si zaregistrovany!");
			return;
		}

		String hash = TextUtils.hash(userId + System.currentTimeMillis() + Math.random());
		Database.addUser(userId, hash, Database.PERMISSION_NONE);
		PrivateChannel dm = user.getPrivateChannel().block();
		if (dm != null) {
			chat.sendMessage("Tvoj API token bol vytvoreny, pozri sa do DMs.");
			dm.createMessage("Tvoj API token bol vytvoreny. Mas prava na pouzivanie ChatGPT, TTS a generovanie obrazkov.\n" +
					"Tvoj token je:" + hash).block();
		}
		else {
			chat.sendMessage("Nastala chyba pri generovani tokenu. Kontaktujte prosim vyvojara.");
		}
	}

	@Override
	public String getName() {
		return "register";
	}

	@Override
	public String getDescription() {
		return "Pouzivatel, ktory napise tento prikaz, si vyziada pristup k API.";
	}

	@Override
	public String getUsage() {
		return "register";
	}

	@Override
	public List<String> getAliases() {
		return List.of("reg");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
