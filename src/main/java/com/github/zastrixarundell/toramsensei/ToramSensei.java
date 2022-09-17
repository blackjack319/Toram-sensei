/*
 *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *                     Version 2, December 2004
 *
 * Copyright (C) 2019, Zastrix Arundell, https://github.com/ZastrixArundell
 *
 *  Everyone is permitted to copy and distribute verbatim or modified
 *  copies of this license document, and changing it is allowed as long
 *  as the name is changed.
 *
 *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *    TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *   0. You just DO WHAT THE FUCK YOU WANT TO.
 *
 *
 */

package com.github.zastrixarundell.toramsensei;

import com.github.zastrixarundell.toramsensei.commands.crafting.CookingCommand;
import com.github.zastrixarundell.toramsensei.commands.crafting.MatsCommand;
import com.github.zastrixarundell.toramsensei.commands.crafting.ProficiencyCommand;
import com.github.zastrixarundell.toramsensei.commands.gameinfo.*;
import com.github.zastrixarundell.toramsensei.commands.player.LevelCommand;
import com.github.zastrixarundell.toramsensei.commands.player.PointsCommand;
import com.github.zastrixarundell.toramsensei.commands.search.items.DiscordItemCommand;
import com.github.zastrixarundell.toramsensei.commands.search.items.UpgradeCommand;
import com.github.zastrixarundell.toramsensei.commands.search.monsters.MonsterSearchCommand;
import com.github.zastrixarundell.toramsensei.commands.torambot.*;
import com.github.zastrixarundell.toramsensei.objects.tasks.MessageTask;
import com.github.zastrixarundell.toramsensei.objects.tasks.MonthlyDyesTask;
import com.github.zastrixarundell.toramsensei.objects.tasks.UpdateDisplayer;
import com.github.zastrixarundell.toramsensei.objects.toram.items.Item;
import com.github.zastrixarundell.toramsensei.objects.toram.items.ProficiencyItem;
import com.github.zastrixarundell.toramsensei.objects.toram.monsters.Monster;
import org.discordbots.api.client.DiscordBotListAPI;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class ToramSensei
{

    public static void main(String[] args)
    {
        String token = System.getenv("MTAxOTA3MDg5NDY4MjkzNTQyNg.GIaiPw.jOAFVQYzt02sUrXQV75wL3MEe0IMz0VQxOcaA0");
        String prefix = System.getenv(">");

        Values.setPrefix(prefix);
        System.out.println("Prefix set to: " + Values.getPrefix());

        DiscordApi bot;
        try
        {
            bot = new DiscordApiBuilder().setToken(token).login().join();
        }
        catch (Exception e)
        {
            System.out.println("Error! Is the token correct?");
            System.exit(0);
            return;
        }

        Values.getMavenVersion();

        bot.updateActivity("Starting up! Please wait!");

        addCommands(bot);
        setupDiscordBotListApi(bot);

        System.out.println("Started! Type in \"stop\" to stop the bot!");

        String input;
        Scanner scanner = new Scanner(System.in);

        Timer activity = updateActivity(bot);
        Timer dyeImage = updateDyesImage(bot);
        Timer updates = updateUpdates(bot);

        try
        {
            while (true)
            {
                System.out.print("User input: ");
                input = scanner.nextLine();
                if (input.equalsIgnoreCase("stop"))
                {
                    bot.disconnect();
                    activity.cancel();
                    dyeImage.cancel();
                    updates.cancel();
                    System.exit(0);
                }
            }
        }
        catch(Exception ignore)
        {

        }

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            try
            {
                bot.disconnect();
                activity.cancel();
                dyeImage.cancel();
                updates.cancel();
                Values.closePool();
            }
            catch (Exception ignore)
            {

            }
        }));
    }

    private static Timer updateActivity(DiscordApi bot)
    {
        Timer timer = new Timer();
        TimerTask task = new MessageTask(bot);
        timer.schedule(task, 0, 1000*60);
        return timer;
    }

    private static Timer updateDyesImage(DiscordApi bot)
    {
        Timer timer = new Timer();
        TimerTask task = new MonthlyDyesTask(bot);
        timer.schedule(task,0, 1000*60*5);
        return timer;
    }

    private static Timer updateUpdates(DiscordApi bot)
    {
        Timer timer = new Timer();
        TimerTask task = new UpdateDisplayer(bot);
        timer.schedule(task,0, 1000*60*5);
        return timer;
    }

    private static void addCommands(DiscordApi bot)
    {
        //Crafting
        for (ProficiencyItem.ProficiencyType type : ProficiencyItem.ProficiencyType.values())
            bot.addListener(new ProficiencyCommand(type));

        bot.addListener(new CookingCommand());
        bot.addListener(new MatsCommand());

        //items
        for (Item.ItemType type : Item.ItemType.values())
            bot.addListener(new DiscordItemCommand(type));

        bot.addListener(new UpgradeCommand());

        //monsters
        for (Monster.MonsterType type : Monster.MonsterType.values())
            bot.addListener(new MonsterSearchCommand(type));


        //player
        bot.addListener(new LevelCommand());
        bot.addListener(new PointsCommand());

        //torambot
        bot.addListener(new HelpCommand());
        bot.addListener(new InviteCommand());
        bot.addListener(new DonateCommand());
        bot.addListener(new SupportCommand());
        bot.addListener(new VoteCommand());

        //gameinfo
        bot.addListener(new NewsCommand());
        bot.addListener(new LatestCommand());
        bot.addListener(new MaintenanceCommand());
        bot.addListener(new EventsCommand());
        bot.addListener(new DyeCommand());
        bot.addListener(new MonthlyCommand());
    }

    private static void setupDiscordBotListApi(DiscordApi bot)
    {
        try
        {
            String token = System.getenv("DISCORD_BOT_LIST_API");

            DiscordBotListAPI api = new DiscordBotListAPI.Builder()
                    .token(token)
                    .botId("600302983305101323")
                    .build();

            Values.setApi(api);

            api.setStats(bot.getServers().size());


        }
        catch (Exception ignore)
        {

        }
    }
}
