package xyz.rtsvk.alfax;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.*;
import xyz.rtsvk.alfax.commands.implementations.*;
import xyz.rtsvk.alfax.mqtt.Mqtt;
import xyz.rtsvk.alfax.scheduler.CommandExecutionScheduler;
import xyz.rtsvk.alfax.commands.CommandProcessor;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.webserver.WebServer;

import java.util.*;

public class Main {
	public static void main(String[] args) throws Exception {

		final Config config = Config.from(args);
		final Logger logger = new Logger(Main.class);
		Logger.setLogFile(config.getStringOrDefault("log-file", "latest.log"));

		// to generate the default config, run the bot as `java -jar jarfile.jar --default-config`
		if (config.containsKey("default-config")) {
			String filename = config.getStringOrDefault("default-config", "config.properties");
			Config defaultConfig = Config.createDefaultConfig(filename);
			config.putAll(defaultConfig);
			logger.info("Created default configuration file '" + filename + "'!");
			return;
		}

		// initialize database wrapper
		Database.init(
				config.getString("db-host"),
				config.getString("db-user"),
				config.getString("db-password"),
				config.getString("db-name")
		);

		// set up discord gateway
		final String prefix = config.getString("prefix");
		logger.info("Bot's prefix is " + prefix + " (length=" + prefix.length() + ")");
		final DiscordClient client = DiscordClient.create(config.getString("token"));
		final GatewayDiscordClient gateway = client.login().block();

		// register all commands
		CommandProcessor.registerCommand("help", new HelpCommand());
		CommandProcessor.registerCommand("test", new TestCommand());
		CommandProcessor.registerCommand("8ball", new FortuneTeller());
		CommandProcessor.registerCommand("pick", new PickCommand());
		CommandProcessor.registerCommand("today", new TodayCommand());
		CommandProcessor.registerCommand("weather", new WeatherCommand());
		CommandProcessor.registerCommand("createapiuser", new CreateApiUserCommand());
		CommandProcessor.registerCommand("bigtext", new BigTextCommand());
		CommandProcessor.registerCommand("gpt", new ChatGPTCommand());

		// register command aliases
		CommandProcessor.registerCommandAlias("fortune", "8ball");

		// scheduler
		if (config.getBooleanOrDefault("scheduler-enabled", false)) {
			Thread scheduler = new Thread(new CommandExecutionScheduler(gateway));
			scheduler.start();
		}

		// webhook server
		if (config.containsKey("webhook-port")) {
			Thread webserver = new WebServer(config.getInt("webhook-port"), gateway);
			webserver.start();
		}

		// MQTT Client
		if (config.getBoolean("mqtt-enabled")) {
			Thread mqtt = new Mqtt(config.getString("mqtt-uri"), gateway);
			mqtt.start();
		}

		gateway.on(MessageCreateEvent.class).subscribe(event -> {
			try {
				final Message message = event.getMessage();
				final User user = message.getAuthor().orElseThrow(Exception::new);
				final Snowflake guildId = message.getGuildId().orElseThrow(Exception::new);
				final MessageChannel channel = message.getChannel().block();
				final String msg = message.getContent().trim();
				final String mention = gateway.getSelf().block().getMention();

				if (user.isBot()) return;
				if (msg.startsWith(prefix)) {
					Thread cmdThread = new Thread(() -> {
						try {
							String cStr = message.getContent().substring(prefix.length());
							final List<String> commandArgs = new ArrayList<>(Arrays.asList(cStr.split(" ")));
							Command cmd = CommandProcessor.getCommandExecutor(commandArgs.get(0));

							if (cmd == null)
								channel.createMessage("**:question: Bracho, netusim co odomna chces. Napis '" + prefix + "help' pre zoznam prikazov. :thinking:**").block();
							else cmd.handle(user, channel, commandArgs, guildId, gateway);
						}
						catch (Exception e) {
							e.printStackTrace();
							channel.createMessage("**:x: Nastala neocakavana chyba. Prosim, skontrolujte standardny vystup pre viac informacii.**").block();
						}
					});
					cmdThread.start();
				}

				else if (msg.startsWith(mention)) {
					Thread cmd = new Thread(() -> {
						try {
							Command c = CommandProcessor.getCommandExecutor("gpt");
							c.handle(user, channel, Arrays.asList(msg.split(" ")), guildId, gateway);
						} catch (Exception e) {
							e.printStackTrace();
							channel.createMessage("**:x: Nastala neocakavana chyba. Prosim, skontrolujte standardny vystup pre viac informacii.**").block();
						}
					});
					cmd.start();
				}
			}
			catch (Exception e) {
				e.printStackTrace(System.out);
			}
		});

		gateway.onDisconnect().block();
	}
}