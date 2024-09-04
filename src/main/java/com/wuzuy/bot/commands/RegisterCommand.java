package com.wuzuy.bot.commands;

import com.wuzuy.bot.embeds.EmbedGenerator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RegisterCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if(event.getMessage().getContentRaw().equalsIgnoreCase("wz.registro")) {

            if(!event.getAuthor().isBot() &&
                    Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR)) {

                EmbedGenerator EmbedGenerator = new EmbedGenerator();

                event.getChannel().sendMessageEmbeds(EmbedGenerator.criarEmbedRegistro())
                        .setActionRow(EmbedGenerator.criarButtonsRegistro())
                        .queue();
            }
        }
    }
}
