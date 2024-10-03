// PaymentChecker.java
package com.wuzuy.bot.tasks;

import com.wuzuy.bot.database.DatabaseManager;
import com.wuzuy.bot.models.PaymentData;
import com.wuzuy.bot.pix.PixApiClient;
import com.wuzuy.bot.pix.TokenGenerator;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PaymentChecker {

    private final ScheduledExecutorService scheduler;
    private final JDA jda;

    public PaymentChecker(ScheduledExecutorService scheduler, JDA jda) {
        this.scheduler = scheduler;
        this.jda = jda;
    }

    /**
     * Inicia a tarefa agendada para verificar pagamentos pendentes a cada 1 minuto.
     */
    public void start() {
        scheduler.scheduleAtFixedRate(this::checkPayments, 0, 4500, TimeUnit.MILLISECONDS);
    }

    /**
     * Verifica o status dos pagamentos pendentes e envia mensagens aos usuários se necessário.
     */
    private void checkPayments() {
        try {
            // Obter pagamentos que estão com status 'Criado' e mensagem ainda não enviada
            List<PaymentData> pendingPayments = DatabaseManager.getPaymentsToCheck();

            if (pendingPayments.isEmpty()) {
                return;
            }

            String token = TokenGenerator.getAccessToken();

            for (PaymentData payment : pendingPayments) {
                // Verificar o status do pagamento via API Pix
                JsonObject response = PixApiClient.consultarCobrancaPix(token, payment.getIdCompra());

                if (response != null && response.has("status")) {
                    String status = response.get("status").getAsString();

                    if (status.equalsIgnoreCase("CONCLUIDA")) {
                        // Atualizar o status do pagamento no banco de dados
                        DatabaseManager.updatePagamentoStatus(payment.getUniqueId(), "Concluída");

                        // Enviar a mensagem com o produto se ainda não foi enviada
                        if (payment.getMessageSent() == 0) {
                            sendProductMessage(payment);
                            // Marcar que a mensagem foi enviada
                            DatabaseManager.updateMessageSentStatus(payment.getUniqueId(), 1);
                        }
                    } else if (status.equalsIgnoreCase("REMOVIDA_PELO_USUARIO_RECEBEDOR") ||
                            status.equalsIgnoreCase("EXPIRADA")) {
                        // Atualizar o status do pagamento no banco de dados
                        DatabaseManager.updatePagamentoStatus(payment.getUniqueId(), status);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Envia uma mensagem privada ao usuário com o produto após a confirmação do pagamento.
     *
     * @param payment Objeto PaymentData contendo os detalhes do pagamento.
     */
    private void sendProductMessage(PaymentData payment) {
        try {
            // Obter o usuário pelo ID
            User user = jda.getUserById(payment.getUserId());
            if (user != null) {
                // Enviar a mensagem privada com o produto
                user.openPrivateChannel().queue(channel -> {
                    channel.sendMessage("Obrigado pelo seu pagamento! Aqui está o seu produto: **[Detalhes do Produto]**").queue();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
