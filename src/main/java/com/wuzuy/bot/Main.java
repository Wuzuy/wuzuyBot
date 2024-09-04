package com.wuzuy.bot;

import com.wuzuy.bot.commands.BroadcastCommand;
import com.wuzuy.bot.commands.RegisterCommand;
import com.wuzuy.bot.listeners.AfterJoinListener;
import com.wuzuy.bot.listeners.AfterLeaveListener;
import com.wuzuy.bot.listeners.RegisterButtonListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {

    public static JDA jda;

    public static void main(String[] args) throws InterruptedException {
        jda = JDABuilder.createDefault("YOUR_TOKEN")
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .build();

        jda.addEventListener(
                new RegisterCommand(),
                new BroadcastCommand(),


                new AfterLeaveListener(),
                new AfterJoinListener(),
                new RegisterButtonListener()
        );
        jda.awaitReady();
    }
}