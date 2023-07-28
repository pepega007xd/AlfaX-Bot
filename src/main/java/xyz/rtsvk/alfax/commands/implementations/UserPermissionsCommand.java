package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Database;

import java.util.List;

public class UserPermissionsCommand implements Command {

	private Config cfg;

	public UserPermissionsCommand(Config cfg) {
		this.cfg = cfg;
	}

	@Override
	public void handle(User user, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {

		if (!Database.checkPermissions(user.getId().asString(), Database.PERMISSION_ADMIN)) {
			channel.createMessage("Nemas opravnenie na vykonanie tohto prikazu.").block();
			return;
		}

		if (args.size() < 3) {
			channel.createMessage("Pouzitie: " + this.cfg.getString("prefix") + "usermod <id> <opravnenia>").block();
			return;
		}

		String id = args.get(1);
		int permissions = Integer.parseInt(args.get(2));
		boolean success = Database.updateUserPermissions(id, permissions);
		if (success) {
			channel.createMessage("Uzivatel " + id + " ma teraz opravnenia " + permissions).block();
		} else {
			channel.createMessage("Nepodarilo sa upravit opravnenia uzivatela " + id).block();
		}
	}

	@Override
	public String getDescription() {
		return "Edit user permissions. Admin only.";
	}
}
