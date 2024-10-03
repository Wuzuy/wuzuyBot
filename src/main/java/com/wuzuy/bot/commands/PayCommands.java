// PayCommands.java
package com.wuzuy.bot.commands;

import com.google.gson.JsonObject;
import com.wuzuy.bot.database.DatabaseManager;
import com.wuzuy.bot.integrations.QRCodeGenerator;
import com.wuzuy.bot.models.ConversationState;
import com.wuzuy.bot.models.PaymentData;
import com.wuzuy.bot.pix.PixApiClient;
import com.wuzuy.bot.pix.TokenGenerator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PayCommands extends ListenerAdapter {

    private static final String PAGAR_BUTTON_ID = "pagar_button";

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String messageContent = event.getMessage().getContentRaw();
        String userId = event.getAuthor().getId();

        if (messageContent.equalsIgnoreCase("$pay")) {

            if(!Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR)) {
                event.getMessage()
                        .reply("Você não tem permissão o suficiente para executar esse comando!")
                        .queue();
            }

            ConversationState existingConversation = DatabaseManager.getConversation(userId);
            if (existingConversation != null) {
                event.getChannel().sendMessage("Você já iniciou um pagamento. Por favor, complete-o primeiro.").queue();
                return;
            }

            ConversationState state = new ConversationState();
            state.setUserId(userId);
            state.setCurrentStep(ConversationState.Step.AWAITING_TITLE);
            DatabaseManager.upsertConversation(state);

            event.getChannel().sendMessage("Por favor, informe o **título** do pagamento:").queue(message -> {
                state.setBotMessageId(message.getId());
                DatabaseManager.upsertConversation(state);
            });
        } else {
            ConversationState state = DatabaseManager.getConversation(userId);
            if (state == null) {
                return;
            }

            switch (state.getCurrentStep()) {
                case AWAITING_TITLE:
                    state.setTitulo(messageContent);
                    state.setCurrentStep(ConversationState.Step.AWAITING_DESCRIPTION);
                    DatabaseManager.upsertConversation(state);

                    event.getChannel().deleteMessageById(event.getMessageId()).queue();
                    if (state.getBotMessageId() != null) {
                        event.getChannel().deleteMessageById(state.getBotMessageId()).queue();
                    }

                    event.getChannel().sendMessage("Por favor, informe a **descrição** do pagamento:").queue(message -> {
                        state.setBotMessageId(message.getId());
                        DatabaseManager.upsertConversation(state);
                    });
                    break;

                case AWAITING_DESCRIPTION:
                    state.setDescricao(messageContent);
                    state.setCurrentStep(ConversationState.Step.AWAITING_VALUE);
                    DatabaseManager.upsertConversation(state);

                    event.getChannel().deleteMessageById(event.getMessageId()).queue();
                    if (state.getBotMessageId() != null) {
                        event.getChannel().deleteMessageById(state.getBotMessageId()).queue();
                    }

                    event.getChannel().sendMessage("Por favor, informe o **valor** do pagamento:").queue(message -> {
                        state.setBotMessageId(message.getId());
                        DatabaseManager.upsertConversation(state);
                    });
                    break;

                case AWAITING_VALUE:
                    
                    String valorOriginal = messageContent.trim();
                    String valorFormatado = validarEFormatarValor(valorOriginal, event);

                    if (valorFormatado == null) {
                        // Valor inválido, solicitar novamente
                        event.getChannel().sendMessage("Valor inválido. Por favor, informe o valor no formato \"$,$$\" (exemplo: \"12,34\").").queue();
                        return;
                    }
                    
                    state.setValor(messageContent);
                    state.setCurrentStep(ConversationState.Step.COMPLETED);
                    DatabaseManager.upsertConversation(state);

                    event.getChannel().deleteMessageById(event.getMessageId()).queue();
                    if (state.getBotMessageId() != null) {
                        event.getChannel().deleteMessageById(state.getBotMessageId()).queue();
                    }

                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle(state.getTitulo())
                            .setDescription(state.getDescricao())
                            .addField("Valor", "R$ " + state.getValor(), false)
                            .setColor(Color.GREEN)
                            .setFooter("Clique no botão abaixo para pagar via Pix!");

                    event.getChannel().sendMessageEmbeds(embed.build())
                            .addActionRow(Button.primary(PAGAR_BUTTON_ID + "_" + UUID.randomUUID(), "Me pague!"))
                            .queue();

                    DatabaseManager.removeConversation(userId);
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Valida e formata o valor fornecido pelo usuário.
     * Se o valor estiver no formato "$" ou "$,$$", converte para "$.$$".
     * Se estiver no formato correto "$,$$", retorna o valor.
     * Caso contrário, retorna null indicando valor inválido.
     *
     * @param valorOriginal Valor original fornecido pelo usuário.
     * @param event         Evento para enviar mensagens de erro, se necessário.
     * @return Valor formatado ou null se inválido.
     */
    private String validarEFormatarValor(String valorOriginal, MessageReceivedEvent event) {
        // Define o padrão esperado: um ou mais dígitos, seguido de vírgula e exatamente dois dígitos
        Pattern pattern = Pattern.compile("^\\d+(,\\d{1,2})?$");
        Matcher matcher = pattern.matcher(valorOriginal);

        if (matcher.matches()) {
            // Se corresponder a apenas um dígito decimal, adiciona um zero
            if (valorOriginal.matches("^\\d+,\\d$")) {
                return valorOriginal + "0";
            }
            // Se não houver dígitos decimais, adiciona ",00"
            if (valorOriginal.matches("^\\d+$")) {
                return valorOriginal + ",00";
            }
            // Está no formato correto
            return valorOriginal;
        } else {
            // Valor inválido
            return null;
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String customId = event.getComponentId();

        if (customId.startsWith(PAGAR_BUTTON_ID)) {
            String userId = event.getUser().getId();

            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            String titulo = embed.getTitle();
            String descricao = embed.getDescription();
            String valor = embed.getFields().get(0).getValue();

            // Gera um ID único para o pagamento
            String uniquePaymentId = UUID.randomUUID().toString();

            // Insere o pagamento no banco de dados
            DatabaseManager.insertPagamento(uniquePaymentId, valor, titulo, descricao, userId, "Pendente");

            // Processa o pagamento
            processPayment(event, uniquePaymentId);

        } else if (customId.startsWith("show_qrcode_")) {
            String uniqueQrCodeId = customId.substring("show_qrcode_".length());

            // Exibe o QR Code
            showQrCode(event, uniqueQrCodeId);
        }
    }

    private void processPayment(ButtonInteractionEvent event, String uniquePaymentId) {
        event.deferReply(true).queue();

        CompletableFuture.runAsync(() -> {
            try {
                // Obtém os dados do pagamento
                PaymentData paymentData = DatabaseManager.getPagamento(uniquePaymentId);

                // Verifica se o pagamento já foi processado
                if (!paymentData.getStatus().equalsIgnoreCase("Pendente")) {
                    event.getHook().sendMessage("Este pagamento já foi processado.").setEphemeral(true).queue();
                    return;
                }

                // Atualiza o status do pagamento para "Processando"
                DatabaseManager.updatePagamentoStatus(uniquePaymentId, "Processando");

                // Parâmetros para a criação da cobrança Pix
                int expiracao = 3600; // 1 hora
                String token = TokenGenerator.getAccessToken();
                String valor = paymentData.getValor();
                String infoPagador = paymentData.getDescricao();
                String chave = "61b555ee-2975-433a-af8c-785f7608f2a2"; // Sua chave Pix

                JsonObject response = PixApiClient.criarCobrancaPix(token, expiracao, valor, infoPagador, chave);

                String servidorId = Objects.requireNonNull(event.getGuild()).getId();
                String idCompra = response.get("txid").getAsString();
                String idUsuario = event.getUser().getId();
                String status = "Pendente";

                // Insere a transação no banco de dados
                DatabaseManager.insertTransaction(servidorId, idCompra, idUsuario, valor, status);

                // Atualiza o pagamento com o ID da compra
                DatabaseManager.updatePagamentoDetails(uniquePaymentId, idCompra, "Criado");

                if (response.has("pixCopiaECola")) {
                    String pixCopia = response.get("pixCopiaECola").getAsString();

                    byte[] qrCodeImage = QRCodeGenerator.generateQRCodeImage(pixCopia, 300, 300);

                    // Gera um ID único para o QR Code
                    String uniqueQrCodeId = UUID.randomUUID().toString();

                    // Insere o QR Code no banco de dados
                    DatabaseManager.insertQRCode(uniqueQrCodeId, pixCopia);

                    Button showQrCodeButton = Button.primary("show_qrcode_" + uniqueQrCodeId, "Mostrar QR Code");

                    event.getHook().sendMessage("Cobrança Pix criada com sucesso. Pix:")
                            .addFiles(FileUpload.fromData(qrCodeImage, "qrcode.png"))
                            .addActionRow(showQrCodeButton)
                            .queue();
                } else {
                    event.getHook().sendMessage("Campo 'pixCopiaECola' não encontrado na resposta.")
                            .setEphemeral(true)
                            .queue();
                }
            } catch (Exception e) {
                event.getHook().sendMessage("Erro ao criar a cobrança Pix: " + e.getMessage())
                        .setEphemeral(true)
                        .queue();
                e.printStackTrace();
            }
        });
    }

    private void showQrCode(ButtonInteractionEvent event, String uniqueQrCodeId) {
        String pixCopiaECola = DatabaseManager.getQRCode(uniqueQrCodeId);

        if (pixCopiaECola != null) {
            event.reply("Aqui está o QR Code em texto:\n```\n" + pixCopiaECola + "\n```")
                    .setEphemeral(true)
                    .queue();

            // Remove o QR Code do banco de dados após exibição
            DatabaseManager.removeQRCode(uniqueQrCodeId);
        } else {
            event.reply("QR Code não encontrado ou já foi exibido.")
                    .setEphemeral(true)
                    .queue();
        }
    }
}
