package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;

import java.util.List;

public class CreateApiUserCommand implements Command {

	@Override
	public void handle(User user, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) {
		channel.createMessage("Tato funkcia je zastarana.").block();
		/*try {
			String id = user.getId().asString();
			String hash = Database.hash(id);
			boolean success = Database.addAPIUser(id, hash);
			if (success) channel.createMessage("Pouzivatel vytvoreny. API kluc: " + hash).block();
			else channel.createMessage("Nepodarilo sa vygenerovat API kluc. Prosim, kontaktujte vyvojara.").block();
		}
		catch (Exception e) {
			e.printStackTrace();
			channel.createMessage("Nastala neocakavana chyba. Prosim, kontaktujte vyvojara.").block();
		}*/
	}

	@Override
	public String getDescription() {
		return null;
	}
}
