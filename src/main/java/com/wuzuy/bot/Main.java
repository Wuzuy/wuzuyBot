package com.wuzuy.bot;

import com.wuzuy.bot.commands.BroadcastCommand;
import com.wuzuy.bot.commands.RegisterCommand;
import com.wuzuy.bot.listeners.AfterJoinListener;
import com.wuzuy.bot.listeners.AfterLeaveListener;
import com.wuzuy.bot.listeners.translations.TranslationBR;
import com.wuzuy.bot.listeners.RegisterButtonListener;
import com.wuzuy.bot.listeners.translations.TranslationEN;
import com.wuzuy.bot.listeners.translations.TranslationES;
import com.wuzuy.bot.listeners.translations.TranslationJA;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {

    public static JDA jda;

    public static void main(String[] args) throws InterruptedException {
        jda = JDABuilder.createDefault("TOKEN_BOT")
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .build();

        jda.addEventListener(
                new RegisterCommand(),
                new BroadcastCommand(),


                new AfterLeaveListener(),
                new AfterJoinListener(),
                new RegisterButtonListener(),

                new TranslationBR(),
                new TranslationEN(),
                new TranslationES(),
                new TranslationJA()
        );
        jda.awaitReady();
    }
}