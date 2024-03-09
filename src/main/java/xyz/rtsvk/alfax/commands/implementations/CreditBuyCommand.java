package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class CreditBuyCommand implements Command {

	@Override
	public void handle(User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) throws Exception {
		long amount = !args.isEmpty() ? Long.parseLong(args.get(0)) : 100;
		boolean success = Database.addUserCredits(user.getId(), amount);
		if (success) {
			chat.sendMessage("Tokeny boli pridane!");
		} else {
			chat.sendMessage("Nepodarilo sa pridat tokeny!");
		}
	}

	@Override
	public String getName() {
		return "credit_buy";
	}

	@Override
	public String getDescription() {
		return "Nakup kreditov (zdarma for now)";
	}

	@Override
	public String getUsage() {
		return "credit_buy [amount]";
	}

	@Override
	public List<String> getAliases() {
		return List.of("cb", "buy");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
