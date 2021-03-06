/*
 * Copyright 2016 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot;

import java.awt.Color;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.AboutCommand;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.jagrosh.jmusicbot.commands.admin.SetdjCmd;
import com.jagrosh.jmusicbot.commands.admin.SettcCmd;
import com.jagrosh.jmusicbot.commands.admin.SetvcCmd;
import com.jagrosh.jmusicbot.commands.dj.ForceskipCmd;
import com.jagrosh.jmusicbot.commands.dj.PauseCmd;
import com.jagrosh.jmusicbot.commands.dj.Repeat;
import com.jagrosh.jmusicbot.commands.dj.SkiptoCmd;
import com.jagrosh.jmusicbot.commands.dj.StopCmd;
import com.jagrosh.jmusicbot.commands.dj.VolumeCmd;
import com.jagrosh.jmusicbot.commands.general.SettingsCmd;
import com.jagrosh.jmusicbot.commands.music.LyricsCmd;
import com.jagrosh.jmusicbot.commands.music.NowplayingCmd;
import com.jagrosh.jmusicbot.commands.music.PlayCmd;
import com.jagrosh.jmusicbot.commands.music.PlaylistsCmd;
import com.jagrosh.jmusicbot.commands.music.QueueCmd;
import com.jagrosh.jmusicbot.commands.music.RemoveCmd;
import com.jagrosh.jmusicbot.commands.music.RemoveLastCmd;
import com.jagrosh.jmusicbot.commands.music.SCSearchCmd;
import com.jagrosh.jmusicbot.commands.music.ShuffleCmd;
import com.jagrosh.jmusicbot.commands.music.SkipCmd;
import com.jagrosh.jmusicbot.commands.owner.AutoplaylistCmd;
import com.jagrosh.jmusicbot.commands.owner.EvalCmd;
import com.jagrosh.jmusicbot.commands.owner.ExportCmd;
import com.jagrosh.jmusicbot.commands.owner.PlaylistCmd;
import com.jagrosh.jmusicbot.commands.owner.RepeatAllCmd;
import com.jagrosh.jmusicbot.commands.owner.SetavatarCmd;
import com.jagrosh.jmusicbot.commands.owner.SetgameCmd;
import com.jagrosh.jmusicbot.commands.owner.SetnameCmd;
import com.jagrosh.jmusicbot.commands.owner.SetstatusCmd;
import com.jagrosh.jmusicbot.commands.owner.ShutdownCmd;
import com.jagrosh.jmusicbot.entities.Prompt;
import com.jagrosh.jmusicbot.gui.GUI;
import com.jagrosh.jmusicbot.settings.SettingsManager;
import com.jagrosh.jmusicbot.utils.OtherUtil;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

/**
 * @author John Grosh (jagrosh)
 */
public class JMusicBot {
    public static final String PLAY_EMOJI = "\u25B6"; // ▶
    public static final String PAUSE_EMOJI = "\u23F8"; // ⏸
    public static final String STOP_EMOJI = "\u23F9"; // ⏹
    static final Permission[] RECOMMENDED_PERMS = new Permission[] { Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION,
        Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_MANAGE, Permission.MESSAGE_EXT_EMOJI,
        Permission.MANAGE_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.NICKNAME_CHANGE };

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // startup log
        Logger log = LoggerFactory.getLogger("Startup");

        // create prompt to handle startup
        Prompt prompt = new Prompt("JMusicBot", "Switching to nogui mode. You can manually start in nogui mode by including the -Dnogui=true flag.",
            "true".equalsIgnoreCase(System.getProperty("nogui", "false")));

        // check deprecated nogui mode (new way of setting it is -Dnogui=true)
        for (String arg : args)
            if ("-nogui".equalsIgnoreCase(arg)) {
                prompt.alert(Prompt.Level.WARNING, "GUI", "The -nogui flag has been deprecated. "
                    + "Please use the -Dnogui=true flag before the name of the jar. Example: java -jar -Dnogui=true JMusicBot.jar");
                break;
            }

        // get and check latest version
        String version = OtherUtil.checkVersion(prompt);

        // load config
        BotConfig config = new BotConfig(prompt);
        config.load();
        if (!config.isValid())
            return;

        // set up the listener
        EventWaiter waiter = new EventWaiter();
        SettingsManager settings = new SettingsManager();
        Bot bot = new Bot(waiter, config, settings);

        AboutCommand aboutCommand = new AboutCommand(Color.BLUE.brighter(),
            "a music bot that is [easy to host yourself!](https://github.com/jagrosh/MusicBot) (v" + version + ")",
            new String[] { "High-quality music playback", "FairQueue™ Technology", "Easy to host yourself" },
            RECOMMENDED_PERMS);
        aboutCommand.setIsAuthor(false);
        aboutCommand.setReplacementCharacter("\uD83C\uDFB6"); // 🎶

        // set up the command client
        CommandClientBuilder cb = new CommandClientBuilder()
            .setPrefix(config.getPrefix())
            .setAlternativePrefix(config.getAltPrefix())
            .setOwnerId(Long.toString(config.getOwnerId()))
            .setEmojis(config.getSuccess(), config.getWarning(), config.getError())
            .setHelpWord(config.getHelp())
            .setLinkedCacheSize(200)
            .setGuildSettingsManager(settings)
            .addCommands(
                new PingCommand(),
                new SettingsCmd(),

                //MUSIC
                new LyricsCmd(bot),
                new NowplayingCmd(bot),
                new PlayCmd(bot, config.getLoading(), config.getSearching()),
                new PlaylistsCmd(bot),
                new QueueCmd(bot),
                new SCSearchCmd(bot, config.getSearching()),
                new SkipCmd(bot),
                new SkiptoCmd(bot),
                new StopCmd(bot),
                new VolumeCmd(bot),
                new RemoveCmd(bot),
                new RemoveLastCmd(bot),

                //DJ
                new ForceskipCmd(bot),
                new PauseCmd(bot),
                new Repeat(bot),

                //Admin
                new SetdjCmd(),
                new SettcCmd(),
                new SetvcCmd(),

                //Owner
                new AutoplaylistCmd(bot),
                new PlaylistCmd(bot),
                new RepeatAllCmd(),
                new SetavatarCmd(),
                new SetgameCmd(),
                new SetnameCmd(),
                new SetstatusCmd(),
                new ShuffleCmd(bot),
                new ShutdownCmd(bot),
                new ExportCmd(bot)
            );
        if (config.useEval())
            cb.addCommand(new EvalCmd(bot));
        boolean nogame = false;
        if (config.getStatus() != OnlineStatus.UNKNOWN)
            cb.setStatus(config.getStatus());
        if (config.getGame() == null)
            cb.useDefaultGame();
        else if (config.getGame().getName().equalsIgnoreCase("none")) {
            cb.setActivity(null);
            nogame = true;
        } else
            cb.setActivity(config.getGame());
        CommandClient client = cb.build();

        if (!prompt.isNoGUI()) {
            try {
                GUI gui = new GUI(bot);
                bot.setGUI(gui);
                gui.init();
            } catch (Exception e) {
                log.error("Could not start GUI. If you are "
                    + "running on a server or in a location where you cannot display a "
                    + "window, please run in nogui mode using the -Dnogui=true flag.");
            }
        }

        log.info("Loaded config from " + config.getConfigLocation());

        // attempt to log in and start
        try {
            JDA jda = JDABuilder.create(config.getToken(), GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(nogame ? null : Activity.playing("loading..."))
                .setStatus(config.getStatus() == OnlineStatus.INVISIBLE || config.getStatus() == OnlineStatus.OFFLINE ? OnlineStatus.INVISIBLE : OnlineStatus.DO_NOT_DISTURB)
                .addEventListeners(client, waiter, new Listener(bot))
                .setBulkDeleteSplittingEnabled(true)
                .build();
            bot.setJDA(jda);
        } catch (LoginException ex) {
            log.error(ex + "\nPlease make sure you are "
                + "editing the correct config.txt file, and that you have used the "
                + "correct token (not the 'secret'!)");
            System.exit(1);
        } catch (IllegalArgumentException ex) {
            log.error("Some aspect of the configuration is invalid: " + ex);
            System.exit(1);
        }
    }
}
