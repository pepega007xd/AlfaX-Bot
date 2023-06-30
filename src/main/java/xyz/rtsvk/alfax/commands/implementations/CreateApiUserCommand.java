package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Database;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class CreateApiUserCommand implements Command {

	@Override
	public void handle(User user, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) {
		try {
			String name = args.get(1);
			String hash = hash(name);
			boolean success = Database.addAPIUser(name, hash);
			if (success) channel.createMessage("Pouzivatel '" + name + "' vytvoreny. API kluc: " + hash).block();
			else channel.createMessage("Nepodarilo sa vygenerovat API kluc. Prosim, kontaktujte vyvojara.").block();
		}
		catch (Exception e) {
			e.printStackTrace();
			channel.createMessage("Nastala neocakavana chyba. Prosim, kontaktujte vyvojara.").block();
		}
	}

	private String hash(String input) throws NoSuchAlgorithmException {
		String result = input;
		if(input != null) {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(input.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			result = hash.toString(16);
			while(result.length() < 128) {
				result = "0" + result;
			}
		}
		return result;
	}

	@Override
	public String getDescription() {
		return null;
	}
}
