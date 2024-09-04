package com.wuzuy.bot.listeners;

import com.wuzuy.bot.embeds.EmbedGenerator;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RegisterButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        EmbedGenerator embed = new EmbedGenerator();

        if (Objects.equals(event.getButton().getId(), "registro")) {

            event.replyEmbeds(embed.criarEmbedGenero())
                    .addActionRow(embed.criarButtonsGeneros())
                    .setEphemeral(true)
                    .queue(message -> message.deleteOriginal().queueAfter(7, TimeUnit.SECONDS));

        } else if (Objects.equals(event.getButton().getId(), "masculino")) {

            Objects.requireNonNull(event.getGuild())
                    .addRoleToMember(event.getUser(),
                            Objects.requireNonNull(event.getGuild().getRoleById("1280943206363693147"))).queue();

            event.replyEmbeds(embed.criarEmbedIdade())
                    .addActionRow(embed.criarButtonsIdades())
                    .setEphemeral(true)
                    .queue(message -> message.deleteOriginal().queueAfter(7, TimeUnit.SECONDS));

        } else if (Objects.equals(event.getButton().getId(), "feminino")) {

            Objects.requireNonNull(event.getGuild())
                    .addRoleToMember(event.getUser(),
                            Objects.requireNonNull(event.getGuild().getRoleById("1280943239997816832"))).queue();

            event.replyEmbeds(embed.criarEmbedIdade())
                    .addActionRow(embed.criarButtonsIdades())
                    .setEphemeral(true)
                    .queue(message -> message.deleteOriginal().queueAfter(7, TimeUnit.SECONDS));

        } else if (Objects.equals(event.getButton().getId(), "+18")) {

            Objects.requireNonNull(event.getGuild())
                    .addRoleToMember(event.getUser(),
                            Objects.requireNonNull(event.getGuild().getRoleById("1280943424962302024"))).queue();

            event.reply(Objects.requireNonNull(event.getMember()).getAsMention() +
                            " Seu registro foi concluido com sucesso!")
                    .queue(message -> {
                        message.deleteOriginal().queueAfter(7, TimeUnit.SECONDS);

                        Objects.requireNonNull(event.getGuild())
                                .addRoleToMember(event.getUser(),
                                        Objects.requireNonNull(event.getGuild().getRoleById("1280943329445675169"))).queue();
                    });

        } else if (Objects.equals(event.getButton().getId(), "-18")) {

            Objects.requireNonNull(event.getGuild())
                    .addRoleToMember(event.getUser(),
                            Objects.requireNonNull(event.getGuild().getRoleById("1280943453278179458"))).queue();

            event.reply(Objects.requireNonNull(event.getMember()).getAsMention() +
                            " Seu registro foi concluido com sucesso!")
                    .queue(message -> {
                        message.deleteOriginal().queueAfter(7, TimeUnit.SECONDS);

                        Objects.requireNonNull(event.getGuild())
                                .addRoleToMember(event.getUser(),
                                        Objects.requireNonNull(event.getGuild().getRoleById("1280943329445675169"))).queue();
                    });
        }
    }
}
