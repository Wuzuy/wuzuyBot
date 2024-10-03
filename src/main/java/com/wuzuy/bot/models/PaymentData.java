// PaymentData.java
package com.wuzuy.bot.models;

public class PaymentData {
    private final String valor;
    private final String titulo;
    private final String descricao;
    private final String userId;
    private String status;
    private final String uniqueId;
    private String idCompra; // ID da compra após criação da cobrança Pix

    public PaymentData(String valor, String titulo, String descricao, String userId, String status, String uniqueId) {
        this.valor = valor;
        this.titulo = titulo;
        this.descricao = descricao;
        this.userId = userId;
        this.status = status;
        this.uniqueId = uniqueId;
    }

    // Getters e setters
    public String getValor() {
        return valor;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(String idCompra) {
        this.idCompra = idCompra;
    }

    private int messageSent;

    public int getMessageSent() {
        return messageSent;
    }

    public void setMessageSent(int messageSent) {
        this.messageSent = messageSent;
    }

}
