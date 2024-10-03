package com.wuzuy.bot;

import com.wuzuy.bot.commands.BroadcastCommand;
import com.wuzuy.bot.commands.RegisterCommand;
import com.wuzuy.bot.listeners.AfterJoinListener;
import com.wuzuy.bot.listeners.AfterLeaveListener;
import com.wuzuy.bot.listeners.CallListener;
import com.wuzuy.bot.translations.TranslationBR;
import com.wuzuy.bot.listeners.RegisterButtonListener;
import com.wuzuy.bot.translations.TranslationEN;
import com.wuzuy.bot.translations.TranslationES;
import com.wuzuy.bot.translations.TranslationJA;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {

    public static JDA jda;

    public static void main(String[] args) throws InterruptedException {
        jda = JDABuilder.createDefault("TOKEN_BOT")

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .build();

        jda.addEventListener(
                new RegisterCommand(),
                new BroadcastCommand(),


                new AfterLeaveListener(),
                new AfterJoinListener(),
                new RegisterButtonListener(),
                new CallListener(),

                new TranslationBR(),
                new TranslationEN(),
                new TranslationES(),
                new TranslationJA()
        );
        jda.awaitReady();

        /*
        com.wuzuy.bot.tasks.PaymentChecker paymentChecker = new com.wuzuy.bot.tasks.PaymentChecker(scheduler, jda);
        paymentChecker.start();
        */
    }
}