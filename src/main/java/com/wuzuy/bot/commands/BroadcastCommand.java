package com.wuzuy.bot.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BroadcastCommand extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(BroadcastCommand.class);

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ", 2); // Divide em até 2 partes

        if (args.length < 2 || !args[0].equalsIgnoreCase("wz.broadcast")) {
            return; // Não é um comando válido
        }
        if (Objects.requireNonNull(event.getMember()).getId().equalsIgnoreCase("318822785063583744")) return;

        String message = args[1]; // Mensagem para enviar

        if (message.isEmpty()) {
            event.getChannel().sendMessage("Por favor, forneça uma mensagem para enviar.").queue();
            return;
        }

        event.getGuild().loadMembers().onSuccess(members -> {
            logger.info("Total de membros no servidor '{}': {}", event.getGuild().getName(), members.size());

            // Filtra membros com base na condição onlineOnly
            List<Member> filteredMembers = members.stream()
                    .filter(member -> !member.getUser().isBot())
                    .collect(Collectors.toList());

            logger.info("Membros filtrados: {}", filteredMembers.size());

            // Envia mensagens diretas em lotes para evitar rate limits
            sendDirectMessagesInBatches(filteredMembers, message);
        }).onError(throwable -> logger.error("Falha ao carregar membros", throwable));
    }

    private void sendDirectMessagesInBatches(List<Member> members, String message) {
        int batchSize = 10; // Número de mensagens por lote
        int delay = 2000; // Atraso entre os lotes em milissegundos

        for (int i = 0; i < members.size(); i += batchSize) {
            int end = Math.min(i + batchSize, members.size());
            List<Member> batch = members.subList(i, end);

            for (Member member : batch) {
                // Evita enviar mensagem para o próprio bot
                if (member.getUser().isBot()) {
                    logger.info("Ignorando bot: {}", member.getEffectiveName());
                    continue;
                }

                // Envia a mensagem direta
                member.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue(
                        success -> logger.info("Mensagem enviada com sucesso para {}", member.getEffectiveName()),
                        error -> logger.error("Falha ao enviar mensagem para {}: {}", member.getEffectiveName(), error.getMessage())
                ), error -> logger.error("Falha ao abrir canal privado para {}: {}", member.getEffectiveName(), error.getMessage()));
            }

            // Adiciona atraso entre os lotes
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                logger.error("Erro ao pausar entre lotes", e);
                Thread.currentThread().interrupt(); // Restores interrupted status
            }
        }
    }
}
