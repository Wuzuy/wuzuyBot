package com.wuzuy.bot.transactions;

import com.google.gson.JsonObject;
import com.wuzuy.bot.database.DatabaseManager;
import com.wuzuy.bot.models.Transaction;
import com.wuzuy.bot.pix.TokenGenerator;
import com.wuzuy.bot.pix.PixApiClient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class TransactionChecker implements Runnable {

    private final JDA jda;

    public TransactionChecker(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void run() {
        try {
            List<Transaction> pendingTransactions = DatabaseManager.getPendingTransactions();

            for (Transaction transaction : pendingTransactions) {
                String txid = transaction.getIdCompra();
                String token = TokenGenerator.getAccessToken();

                JsonObject response = PixApiClient.consultarCobrancaPix(token, txid);

                String status = response.get("status").getAsString();

                if ("CONCLUIDA".equalsIgnoreCase(status)) {
                    // Atualiza o status da transação no banco de dados
                    DatabaseManager.updatePagamentoStatus(txid, "Concluída");

                    // Envia mensagem privada ao usuário
                    sendPrivateMessage(transaction.getIdUsuario());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPrivateMessage(String userId) {
        User user = jda.getUserById(userId);
        if (user != null) {
            user.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("Seu pagamento foi confirmado! \n " +
                        "Seu produto: https://discord.gg/wuzuy").queue();
            }, throwable -> {
                System.err.println("Erro ao abrir canal privado com o usuário: " + throwable.getMessage());
            });
        } else {
            System.err.println("Usuário com ID " + userId + " não encontrado.");
        }
    }
}
