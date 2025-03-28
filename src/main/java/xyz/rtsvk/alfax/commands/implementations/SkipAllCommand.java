package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.util.lavaplayer.TrackScheduler;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class SkipAllCommand implements ICommand {

    @Override
    public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
        if (chat.isPrivate()) {
            chat.sendMessage(language.getMessage("general.command.dm-not-supported"));
            return;
        }

        TrackScheduler scheduler = guildState.getTrackScheduler();
        scheduler.getTrackQueue().clear();
        scheduler.skipCurrentTrack();
	      chat.sendMessage(language.getMessage("command.skipall.queue-cleared"));
    }

    @Override
    public String getName() {
        return "skipall";
    }

    @Override
    public String getDescription() {
        return "command.skipall.description";
    }

    @Override
    public String getUsage() {
        return "skipall";
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
