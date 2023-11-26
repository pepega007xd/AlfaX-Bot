package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.commands.CommandProcessor;

import java.util.List;
import java.util.Map;

public class HelpCommand implements Command {

	private final CommandProcessor processor;

	public HelpCommand(CommandProcessor processor) {
		this.processor = processor;
	}

	@Override
	public void handle(User user, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) {
		StringBuilder sb = new StringBuilder();

		if (args.isEmpty()) {
			sb.append("Poznam tieto prikazy:```\n");
			final Map<String, Command> cmds = this.processor.getCommands();
			cmds.forEach((key, value) -> {
				String desc = value.getDescription();
				if (desc == null) return;                   // if the command does not have a description, hide it
				sb.append(key).append(" - ").append(desc).append('\n');
			});
			sb.append("```");
			channel.createMessage(sb.toString()).block();
		}
		else {
			Command command = this.processor.getCommandExecutor(args.get(0));
			if (command == null) {
				channel.createMessage("Prikaz neexistuje").block();
				return;
			}
			sb.append("```\nPouzitie: ").append(command.getUsage()).append('\n');
			sb.append("Popis: ").append(command.getDescription()).append("```");
			channel.createMessage(sb.toString()).block();
		}
	}

	@Override
	public String getDescription() {
		return "Vypise zoznam prikazov";
	}

	@Override
	public String getUsage() {
		return "help [prikaz]";
	}
}
