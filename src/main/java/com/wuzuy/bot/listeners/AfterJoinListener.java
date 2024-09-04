package com.wuzuy.bot.listeners;

import com.wuzuy.bot.embeds.EmbedGenerator;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class AfterJoinListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {

        EmbedGenerator embed = new EmbedGenerator();

        TextChannel channel = event.getGuild().getTextChannelById("1280891770967687279");
        TextChannel joinChannel = event.getGuild().getTextChannelById("1280891644626862183");

        assert channel != null;
        channel.sendMessage(event.getMember().getAsMention()).queue(message -> message.delete().queueAfter(2, TimeUnit.SECONDS));

        assert joinChannel != null;
        joinChannel.sendMessageEmbeds(embed.defaultEmbed(

                "%s Entrou ao servidor!".formatted(event.getMember().getEffectiveName()),

                Color.GREEN,

                """
                        Usuário: %s
                        ID: %s
                        Data de criação: %s
                        """.formatted(event.getMember().getAsMention(), event.getMember().getId(),
                        event.getUser().getTimeCreated().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),

                event.getMember().getEffectiveAvatarUrl())).queue();
    }
}
