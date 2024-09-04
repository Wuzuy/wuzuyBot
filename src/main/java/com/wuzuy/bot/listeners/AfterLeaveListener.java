package com.wuzuy.bot.listeners;

import com.wuzuy.bot.embeds.EmbedGenerator;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.format.DateTimeFormatter;

public class AfterLeaveListener extends ListenerAdapter {
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {

        EmbedGenerator embed = new EmbedGenerator();
        TextChannel joinChannel = event.getGuild().getTextChannelById("1280891653883953205");

        assert joinChannel != null;
        joinChannel.sendMessageEmbeds(embed.defaultEmbed(
                "%s Saiu do servidor!".formatted(event.getUser().getEffectiveName()),
                Color.RED,
                """
                        Usuário: %s
                        ID: %s
                        Data de criação: %s
                        """.formatted(event.getUser().getAsMention(),
                        event.getUser().getId(),
                        event.getUser().getTimeCreated().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
                event.getUser().getEffectiveAvatarUrl()
        )).queue();
    }
}
