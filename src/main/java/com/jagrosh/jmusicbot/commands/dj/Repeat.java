package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.settings.Settings;

public class Repeat extends DJCommand {
    public Repeat(Bot bot) {
        super(bot);
        this.name = "repeat";
        this.help = "re-adds the current playing song to the queue when finished";
        this.arguments = "[on|off]";
        this.guildOnly = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        boolean value;
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().isEmpty()) {
            value = !settings.getRepeatModeSingle();
        } else if (event.getArgs().equalsIgnoreCase("true") || event.getArgs().equalsIgnoreCase("on")) {
            value = true;
        } else if (event.getArgs().equalsIgnoreCase("false") || event.getArgs().equalsIgnoreCase("off")) {
            value = false;
        } else {
            event.replyError("Valid options are `on` or `off` (or leave empty to toggle)");
            return;
        }
        settings.setRepeatModeSingle(value);
        event.replySuccess("Repeat mode is now `" + (value ? "ON" : "OFF") + "`");
    }
}

