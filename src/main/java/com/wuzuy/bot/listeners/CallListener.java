package com.wuzuy.bot.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class CallListener extends ListenerAdapter {

    private static final long LOG_CHANNEL_ID = 1291196366642483314L;

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        VoiceChannel oldChannel = (VoiceChannel) event.getOldValue();
        VoiceChannel newChannel = (VoiceChannel) event.getNewValue();

        Member member = event.getMember();
        Guild guild = event.getGuild();

        TextChannel logChannel = guild.getTextChannelById(LOG_CHANNEL_ID);

        if (logChannel == null) {
            System.out.println("Canal de log não encontrado!");
            return;
        }

        if (oldChannel == null && newChannel != null) {
            // Entrou em uma chamada
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.GREEN);
            embed.setTitle("Usuário Entrou na Chamada");
            embed.setDescription(member.getEffectiveName() + " entrou em " + newChannel.getName());
            embed.setTimestamp(Instant.now());

            logChannel.sendMessageEmbeds(embed.build()).queue();

        } else if (oldChannel != null && newChannel == null) {
            // Saiu da chamada
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.RED);
            embed.setTitle("Usuário Saiu da Chamada");
            embed.setDescription(member.getEffectiveName() + " saiu de " + oldChannel.getName());
            embed.setTimestamp(Instant.now());

            logChannel.sendMessageEmbeds(embed.build()).queue();

        } else if (oldChannel != null && newChannel != null && !oldChannel.equals(newChannel)) {
            // Trocou ou foi movido de chamada
            guild.retrieveAuditLogs().type(ActionType.MEMBER_VOICE_MOVE).limit(1).queue(auditLogs -> {
                AuditLogEntry moveEntry = null;
                for (AuditLogEntry entry : auditLogs) {
                    if (entry.getTargetIdLong() == member.getIdLong()) {
                        moveEntry = entry;
                        break;
                    }
                }

                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.YELLOW);
                embed.setTimestamp(Instant.now());

                if (moveEntry != null) {
                    // Foi movido por um moderador
                    User moderator = moveEntry.getUser();
                    embed.setTitle("Usuário Movido de Chamada");
                    embed.setDescription(member.getEffectiveName() + " foi movido de " + oldChannel.getName() + " para " + newChannel.getName() + " por moderador " + moderator.getName());
                } else {
                    // Trocou de chamada por conta própria
                    embed.setTitle("Usuário Trocou de Chamada");
                    embed.setDescription(member.getEffectiveName() + " trocou de " + oldChannel.getName() + " para " + newChannel.getName());
                }

                logChannel.sendMessageEmbeds(embed.build()).queue();
            });
        }
    }
}