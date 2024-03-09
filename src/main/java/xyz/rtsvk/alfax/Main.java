package xyz.rtsvk.alfax;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.commands.CommandProcessor;
import xyz.rtsvk.alfax.commands.implementations.*;
import xyz.rtsvk.alfax.mqtt.Mqtt;
import xyz.rtsvk.alfax.scheduler.CommandExecutionScheduler;
import xyz.rtsvk.alfax.tasks.TaskTimer;
import xyz.rtsvk.alfax.util.*;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.chat.impl.DiscordChat;
import xyz.rtsvk.alfax.util.text.MessageManager;
import xyz.rtsvk.alfax.util.text.TextUtils;
import xyz.rtsvk.alfax.webserver.WebServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
	public static void main(String[] args) throws Exception {

		final Config config = Config.from(args);
		final Logger logger = new Logger(Main.class);
		FileManager.init();
		Logger.setLogFile(config.getStringOrDefault("log-file", "latest.log"));

		// if the bot is run with --copy-default-config, it will generate a default config file and exit
		if (config.containsKey("copy-default-config")) {
			String filename = config.getStringOrDefault("copy-default-config", "default-config.properties");
			Config.copyDefaultConfig(filename);
			logger.info("Default configuration saved to file '" + filename + "'!");
			return;
		}

		Config.defaultConfig().forEach(config::putIfAbsent);    // fill in missing values with defaults
		if (config.containsKey("save-config")) {
			String filename = config.getStringOrDefault("save-config", "saved-config_" + System.currentTimeMillis() + ".properties");
			config.remove("save-config");
			config.write(filename);
			logger.info("Current configuration saved to file '" + filename + "'!");
		}

		// initialize database wrapper
		Database.init(config);

		int adminCount = Database.getAdminCount();
		if (adminCount == 0) {
			String token = config.getStringOrDefault("admin-token", TextUtils.getRandomString(128));
			logger.info("No admin users found! Admin token: " + token);
			config.putIfAbsent("admin-token", token);
		}
		else if (adminCount == -1) {
			logger.error("Failed to check admin users! Please check your database connection.");
			return;
		}

		// set up discord gateway
		final String prefix = config.getString("prefix");
		logger.info("Bot's prefix is " + prefix + " (length=" + prefix.length() + ")");
		final DiscordClient client = DiscordClient.create(config.getString("token"));
		final GatewayDiscordClient gateway = client.login().blockOptional().orElseThrow(Exception::new);
		final User self = gateway.getSelf().blockOptional().orElseThrow(Exception::new);
		final String botMention = self.getMention();
		final String defaultLanguage = config.getString("default-language");
		final boolean forceDefaultLanguage = config.getBoolean("force-default-language");

		CommandProcessor proc = new CommandProcessor();
		proc.setFallback(new HelpCommand(proc) {
			@Override
			public void handle(User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) {
				chat.sendMessage("Neznamy prikaz!");
				super.handle(user, chat, args, guildId, bot, language);
			}
		});

		// register all commands
		proc.registerCommand(new HelpCommand(proc));
		proc.registerCommand(new TestCommand());
		proc.registerCommand(new FortuneTellerCommand());
		proc.registerCommand(new PickCommand());
		proc.registerCommand(new TodayCommand());
		proc.registerCommand(new WeatherCommand(config));
		proc.registerCommand(new BigTextCommand());
		proc.registerCommand(new ChatGPTCommand(config));
		proc.registerCommand(new MqttPublishCommand(config));
		proc.registerCommand(new RegisterSensorCommand(prefix));
		proc.registerCommand(new CreateUserCommand());
		proc.registerCommand(new UserPermissionsCommand(config));
		proc.registerCommand(new RedeemAdminPermissionCommand(config));
		proc.registerCommand(new CreditsCommand(config));
		proc.registerCommand(new SetAnnouncementChannelCommand());
		proc.registerCommand(new ScheduleEventCommand());
		proc.registerCommand(new MathExpressionCommand());
		proc.registerCommand(new CatCommand());
		proc.registerCommand(new TextToSpeechCommand(config));
		proc.registerCommand(new GenerateImageCommand(config));
		proc.registerCommand(new RollDiceCommand());
		proc.registerCommand(new CreditBuyCommand());
		proc.registerCommand(new MeCommand());
		proc.registerCommand(new PollCreateCommand());
		proc.registerCommand(new PollEndCommand());
		proc.registerCommand(new SetLanguageCommand());
		proc.registerCommand(new ClearMessageManagerCacheCommand());
		proc.registerCommand(new ServiceInfoCommand(() -> Thread.getAllStackTraces().keySet()));
		//proc.registerCommand(new PlayCommand(playerManager, player, provider, trackScheduler));

		ServiceWatcher watcher = new ServiceWatcher();
		Thread scheduler = new CommandExecutionScheduler(gateway, proc);
		Thread webserver = new WebServer(config, gateway);
		Mqtt mqtt = new Mqtt(config, gateway);

		// scheduler
		if (config.getBoolean("scheduler-enabled")) {
			scheduler.setUncaughtExceptionHandler(watcher);
			scheduler.start();
		}

		// webhook server
		if (config.getBoolean("webserver-enabled")) {
			webserver.setUncaughtExceptionHandler(watcher);
			webserver.start();
		}

		// MQTT Subscribe Client
		if (config.getBoolean("mqtt-enabled")) {
			mqtt.setUncaughtExceptionHandler(watcher);
			mqtt.start();
		}

		// task timer
		TaskTimer timer = new TaskTimer(gateway, 1000);
		timer.setEnabled(true);

		final String commandOnTag = config.getString("command-on-tag");
		logger.info("Command on tag: " + commandOnTag);
		List<Thread> runningCommandExecutors = new ArrayList<>();
		gateway.on(MessageCreateEvent.class).subscribe(event -> {
			try {
				final Message message = event.getMessage();
				final User user = message.getAuthor().orElseThrow(Exception::new);
				if (user.isBot()) return;

				final Snowflake guildId = message.getGuildId().orElse(null);
				final MessageChannel channel = message.getChannel().block();
				assert channel != null;

				final String msg = message.getContent().trim();
				if (!msg.startsWith(botMention) && !msg.startsWith(prefix)) return;
				final List<String> tokenList = CommandProcessor.splitCommandString(msg);
				final String first = tokenList.get(0);

				MessageManager language = forceDefaultLanguage
						? MessageManager.getMessages(defaultLanguage)
						: Database.getUserLanguage(user.getId(), defaultLanguage);

				if (language == null) {
					logger.error("Failed to load language for user " + user.getId().asString());
					channel.createMessage("A fatal error has occured. Please contact the administrator.")
							.withMessageReference(message.getId()).block();
					return;
				}

				String cmdName = first.startsWith(prefix) ? first.substring(prefix.length()) : commandOnTag;
				Thread cmdThread = new Thread(() -> {
					try {
						Command cmd = proc.getCommandExecutor(cmdName);
						Snowflake messageId = message.getId();
						if (cmd == null) {
							channel.createMessage(language.formatMessage("command.not-found", prefix))
									.withMessageReference(messageId).block();
						}
						else {
							cmd.handle(user, new DiscordChat(channel, messageId), tokenList.subList(1, tokenList.size()), guildId, gateway, language);
						}

					} catch (Exception e) {
						e.printStackTrace(System.out);
						channel.createMessage("**:x: " + e.getMessage() + "**").withMessageReference(message.getId()).block();
					}
				});
				cmdThread.start();
				cmdThread.setName("CommandExecutor-" + cmdName + "-" + System.currentTimeMillis());
				runningCommandExecutors.add(cmdThread);


			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		});

		AtomicBoolean shutdownRequested = new AtomicBoolean(false);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			shutdownRequested.set(true);
			if (scheduler.isAlive()) {
				scheduler.interrupt();
			}
			if (webserver.isAlive()) {
				webserver.interrupt();
			}
			if (mqtt.isAlive()) {
				mqtt.interrupt();
			}
			timer.setEnabled(false);
			if (config.getBoolean("force-shutdown-on-exit")) {
				runningCommandExecutors.forEach(Thread::interrupt);
			}
			else { // wait for all command executors to finish
				while (runningCommandExecutors.stream().anyMatch(Thread::isAlive));
			}
			Database.close();
			FileManager.close();
			gateway.logout().block();
			logger.info("Goodbye!");
		}));

		gateway.onDisconnect().subscribe(event -> {
			if (shutdownRequested.get()) return;
			logger.error("Disconnected from Discord! Shutting down...");
			Runtime.getRuntime().exit(1);
		});
	}
}