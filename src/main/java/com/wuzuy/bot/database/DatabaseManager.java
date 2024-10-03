// DatabaseManager.java
package com.wuzuy.bot.database;


import com.wuzuy.bot.models.ConversationState;
import com.wuzuy.bot.models.PaymentData;
import com.wuzuy.bot.models.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:status.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("Driver SQLite JDBC carregado com sucesso.");
        } catch (ClassNotFoundException e) {
            System.err.println("Erro ao carregar o driver SQLite JDBC: " + e.getMessage());
        }
    }

    /**
     * Inicializa o banco de dados criando as tabelas necessárias se elas não existirem.
     */
    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                Statement stmt = conn.createStatement();

                // Tabela de transações
                String sqlTransacoes = "CREATE TABLE IF NOT EXISTS transacoes (\n"
                        + "    servidor_id TEXT NOT NULL,\n"
                        + "    id_compra TEXT NOT NULL,\n"
                        + "    id_usuario TEXT NOT NULL,\n"
                        + "    valor_cobranca TEXT NOT NULL,\n"
                        + "    status TEXT NOT NULL\n"
                        + ");";
                stmt.execute(sqlTransacoes);

                // Tabela de conversas
                String sqlConversas = "CREATE TABLE IF NOT EXISTS conversas (\n"
                        + "    user_id TEXT PRIMARY KEY,\n"
                        + "    current_step TEXT NOT NULL,\n"
                        + "    titulo TEXT,\n"
                        + "    descricao TEXT,\n"
                        + "    valor TEXT,\n"
                        + "    bot_message_id TEXT\n"
                        + ");";
                stmt.execute(sqlConversas);

                // Tabela de pagamentos
                String sqlPagamentos = "CREATE TABLE IF NOT EXISTS pagamentos (\n"
                        + "    unique_id TEXT PRIMARY KEY,\n"
                        + "    valor TEXT NOT NULL,\n"
                        + "    titulo TEXT NOT NULL,\n"
                        + "    descricao TEXT,\n"
                        + "    user_id TEXT NOT NULL,\n"
                        + "    status TEXT NOT NULL,\n"
                        + "    id_compra TEXT,\n"
                        + "    message_sent INTEGER DEFAULT 0\n" // Campo para rastrear se a mensagem foi enviada
                        + ");";
                stmt.execute(sqlPagamentos);

                // Tabela de QR Codes
                String sqlQRCodes = "CREATE TABLE IF NOT EXISTS qrcodes (\n"
                        + "    unique_id TEXT PRIMARY KEY,\n"
                        + "    pix_copia_colar TEXT NOT NULL\n"
                        + ");";
                stmt.execute(sqlQRCodes);

                System.out.println("Banco de dados inicializado com sucesso.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao inicializar o banco de dados: " + e.getMessage());
        }
    }

    // ======================== Métodos para Conversas ========================

    /**
     * Insere ou atualiza o estado da conversa com o usuário.
     *
     * @param state Objeto ConversationState contendo o estado atual da conversa.
     */
    public static void upsertConversation(ConversationState state) {
        String sql = "INSERT INTO conversas(user_id, current_step, titulo, descricao, valor, bot_message_id) VALUES(?,?,?,?,?,?) "
                + "ON CONFLICT(user_id) DO UPDATE SET current_step = excluded.current_step, "
                + "titulo = excluded.titulo, descricao = excluded.descricao, valor = excluded.valor, bot_message_id = excluded.bot_message_id";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, state.getUserId());
            pstmt.setString(2, state.getCurrentStep().name());
            pstmt.setString(3, state.getTitulo());
            pstmt.setString(4, state.getDescricao());
            pstmt.setString(5, state.getValor());
            pstmt.setString(6, state.getBotMessageId());
            pstmt.executeUpdate();
            // Remover prints excessivos para evitar logs desnecessários
        } catch (SQLException e) {
            System.out.println("Erro ao upsertar conversa: " + e.getMessage());
        }
    }

    /**
     * Obtém o estado atual da conversa do usuário.
     *
     * @param userId ID do usuário.
     * @return Objeto ConversationState ou null se não existir.
     */
    public static ConversationState getConversation(String userId) {
        String sql = "SELECT * FROM conversas WHERE user_id = ?";
        ConversationState conversation = null;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                conversation = new ConversationState();
                conversation.setUserId(rs.getString("user_id"));
                conversation.setCurrentStep(ConversationState.Step.valueOf(rs.getString("current_step")));
                conversation.setTitulo(rs.getString("titulo"));
                conversation.setDescricao(rs.getString("descricao"));
                conversation.setValor(rs.getString("valor"));
                conversation.setBotMessageId(rs.getString("bot_message_id"));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter conversa: " + e.getMessage());
        }

        return conversation;
    }

    /**
     * Remove a conversa do usuário após a conclusão.
     *
     * @param userId ID do usuário.
     */
    public static void removeConversation(String userId) {
        String sql = "DELETE FROM conversas WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
            // Remover prints excessivos para evitar logs desnecessários
        } catch (SQLException e) {
            System.out.println("Erro ao remover conversa: " + e.getMessage());
        }
    }

    // ======================== Métodos para Pagamentos ========================

    /**
     * Insere um novo pagamento no banco de dados.
     *
     * @param uniqueId   ID único do pagamento.
     * @param valor      Valor do pagamento.
     * @param titulo     Título do pagamento.
     * @param descricao  Descrição do pagamento.
     * @param userId     ID do usuário que fez o pagamento.
     * @param status     Status inicial do pagamento (e.g., "Pendente").
     */
    public static void insertPagamento(String uniqueId, String valor, String titulo, String descricao, String userId, String status) {
        String sql = "INSERT INTO pagamentos(unique_id, valor, titulo, descricao, user_id, status) VALUES(?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uniqueId);
            pstmt.setString(2, valor);
            pstmt.setString(3, titulo);
            pstmt.setString(4, descricao);
            pstmt.setString(5, userId);
            pstmt.setString(6, status);
            pstmt.executeUpdate();
            // Remover prints excessivos para evitar logs desnecessários
        } catch (SQLException e) {
            System.out.println("Erro ao inserir pagamento: " + e.getMessage());
        }
    }

    /**
     * Obtém os detalhes de um pagamento específico.
     *
     * @param uniqueId ID único do pagamento.
     * @return Objeto PaymentData ou null se não encontrado.
     */
    public static PaymentData getPagamento(String uniqueId) {
        String sql = "SELECT * FROM pagamentos WHERE unique_id = ?";
        PaymentData payment = null;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uniqueId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                payment = new PaymentData(
                        rs.getString("valor"),
                        rs.getString("titulo"),
                        rs.getString("descricao"),
                        rs.getString("user_id"),
                        rs.getString("status"),
                        rs.getString("unique_id")
                );
                payment.setIdCompra(rs.getString("id_compra"));
                payment.setMessageSent(rs.getInt("message_sent"));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter pagamento: " + e.getMessage());
        }

        return payment;
    }

    /**
     * Obtém a lista de pagamentos que precisam ser verificados (status 'Criado' e mensagem não enviada).
     *
     * @return Lista de objetos PaymentData.
     */
    public static List<PaymentData> getPaymentsToCheck() {
        String sql = "SELECT * FROM pagamentos WHERE status = 'Criado' AND message_sent = 0";
        List<PaymentData> payments = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PaymentData payment = new PaymentData(
                        rs.getString("valor"),
                        rs.getString("titulo"),
                        rs.getString("descricao"),
                        rs.getString("user_id"),
                        rs.getString("status"),
                        rs.getString("unique_id")
                );
                payment.setIdCompra(rs.getString("id_compra"));
                payment.setMessageSent(rs.getInt("message_sent"));
                payments.add(payment);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter pagamentos para verificar: " + e.getMessage());
        }

        return payments;
    }

    /**
     * Atualiza o status de um pagamento.
     *
     * @param uniqueId ID único do pagamento.
     * @param newStatus Novo status do pagamento.
     */
    public static void updatePagamentoStatus(String uniqueId, String newStatus) {
        String sql = "UPDATE pagamentos SET status = ? WHERE unique_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, uniqueId);
            pstmt.executeUpdate();
            // Remover prints excessivos para evitar logs desnecessários
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar status do pagamento: " + e.getMessage());
        }
    }

    /**
     * Atualiza os detalhes de um pagamento após a criação da cobrança Pix.
     *
     * @param uniqueId ID único do pagamento.
     * @param idCompra ID da compra retornado pela API Pix.
     * @param status   Novo status do pagamento.
     */
    public static void updatePagamentoDetails(String uniqueId, String idCompra, String status) {
        String sql = "UPDATE pagamentos SET id_compra = ?, status = ? WHERE unique_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idCompra);
            pstmt.setString(2, status);
            pstmt.setString(3, uniqueId);
            pstmt.executeUpdate();
            // Remover prints excessivos para evitar logs desnecessários
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar detalhes do pagamento: " + e.getMessage());
        }
    }

    /**
     * Atualiza o campo message_sent para indicar se a mensagem com o produto foi enviada.
     *
     * @param uniqueId     ID único do pagamento.
     * @param messageSent  Valor inteiro (0 ou 1) indicando se a mensagem foi enviada.
     */
    public static void updateMessageSentStatus(String uniqueId, int messageSent) {
        String sql = "UPDATE pagamentos SET message_sent = ? WHERE unique_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, messageSent);
            pstmt.setString(2, uniqueId);
            pstmt.executeUpdate();
            // Remover prints excessivos para evitar logs desnecessários
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar status da mensagem enviada: " + e.getMessage());
        }
    }

    // ======================== Métodos para Transações ========================

    /**
     * Insere uma nova transação no banco de dados.
     *
     * @param servidorId     ID do servidor Discord.
     * @param idCompra       ID da compra retornado pela API Pix.
     * @param idUsuario      ID do usuário que fez o pagamento.
     * @param valorCobranca  Valor da cobrança.
     * @param status         Status da transação (e.g., "Pendente").
     */
    public static void insertTransaction(String servidorId, String idCompra, String idUsuario, String valorCobranca, String status) {
        String sql = "INSERT INTO transacoes(servidor_id, id_compra, id_usuario, valor_cobranca, status) VALUES(?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, servidorId);
            pstmt.setString(2, idCompra);
            pstmt.setString(3, idUsuario);
            pstmt.setString(4, valorCobranca);
            pstmt.setString(5, status);
            pstmt.executeUpdate();
            // Remover prints excessivos para evitar logs desnecessários
        } catch (SQLException e) {
            System.out.println("Erro ao inserir transação: " + e.getMessage());
        }
    }

    /**
     * Obtém a lista de transações pendentes.
     *
     * @return Lista de objetos Transaction.
     */
    public static List<Transaction> getPendingTransactions() {
        String sql = "SELECT * FROM transacoes WHERE status = 'Pendente'";
        List<Transaction> pendingTransactions = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Transaction transaction = new Transaction(
                        rs.getString("servidor_id"),
                        rs.getString("id_compra"),
                        rs.getString("id_usuario"),
                        rs.getString("valor_cobranca"),
                        rs.getString("status")
                );
                pendingTransactions.add(transaction);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter transações pendentes: " + e.getMessage());
        }
        return pendingTransactions;
    }

    // ======================== Métodos para QR Codes ========================

    /**
     * Insere um QR Code no banco de dados.
     *
     * @param uniqueId       ID único para o QR Code.
     * @param pixCopiaCola   Código Pix no formato Copia e Cola.
     */
    public static void insertQRCode(String uniqueId, String pixCopiaCola) {
        String sql = "INSERT INTO qrcodes(unique_id, pix_copia_colar) VALUES(?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uniqueId);
            pstmt.setString(2, pixCopiaCola);
            pstmt.executeUpdate();
            // Remover prints excessivos para evitar logs desnecessários
        } catch (SQLException e) {
            System.out.println("Erro ao inserir QR Code: " + e.getMessage());
        }
    }

    /**
     * Obtém o código Pix Copia e Cola de um QR Code específico.
     *
     * @param uniqueId ID único do QR Code.
     * @return String contendo o código Pix Copia e Cola ou null se não encontrado.
     */
    public static String getQRCode(String uniqueId) {
        String sql = "SELECT pix_copia_colar FROM qrcodes WHERE unique_id = ?";
        String pixCopiaCola = null;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uniqueId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                pixCopiaCola = rs.getString("pix_copia_colar");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter QR Code: " + e.getMessage());
        }

        return pixCopiaCola;
    }

    /**
     * Remove um QR Code do banco de dados após a exibição.
     *
     * @param uniqueId ID único do QR Code.
     */
    public static void removeQRCode(String uniqueId) {
        String sql = "DELETE FROM qrcodes WHERE unique_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uniqueId);
            pstmt.executeUpdate();
            // Remover prints excessivos para evitar logs desnecessários
        } catch (SQLException e) {
            System.out.println("Erro ao remover QR Code: " + e.getMessage());
        }
    }
}