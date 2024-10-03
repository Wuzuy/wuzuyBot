package com.wuzuy.bot.translations;

import com.wuzuy.bot.functional.Webhook;

import com.darkprograms.speech.translator.GoogleTranslate;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TranslationBR extends ListenerAdapter {

    private final Webhook webhook = new Webhook();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        TextChannel chat = event.getGuild().getTextChannelById("1285395070823956564");


        if (chat == null || event.getMember() == null || event.getMember().getUser().isBot() || event.isWebhookMessage()) {
            return;
        }

        if (event.getChannel().getId().equalsIgnoreCase(chat.getId())) {
            String mensagemEnviada = event.getMessage().getContentRaw();
            String userId = event.getMember().getId();

            try {
                String linguagem = GoogleTranslate.detectLanguage(mensagemEnviada);
                System.out.println("Linguagem detectada: " + linguagem);
                String mensagemTraducao = GoogleTranslate.translate("pt", mensagemEnviada);

                event.getMessage().delete().queue();

                String webhookUrl = webhook.getWebhookUrl(userId, event.getGuild(), chat, event.getMember().getEffectiveName(), event.getMember().getUser().getEffectiveAvatarUrl());

                if (!linguagem.equalsIgnoreCase("pt")) {
                    webhook.enviarMensagemWebhook(webhookUrl, mensagemTraducao + "\n-# Original " +linguagem + ": " +
                            mensagemEnviada + "\n", event.getMember().getEffectiveName(), event.getMember().getUser().getEffectiveAvatarUrl());
                } else {
                    webhook.enviarMensagemWebhook(webhookUrl, mensagemEnviada, event.getMember().getEffectiveName(), event.getMember().getUser().getEffectiveAvatarUrl());
                }

            } catch (IOException e) {
                throw new RuntimeException("Erro ao detectar linguagem ou enviar mensagem via Webhook", e);
            }
        }
    }
}
