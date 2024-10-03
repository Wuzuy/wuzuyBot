package com.wuzuy.bot.models;

public class Transaction {
    private String servidorId;
    private String idCompra;
    private String idUsuario;
    private String valorCobranca;
    private String status;

    public Transaction(String servidorId, String idCompra, String idUsuario, String valorCobranca, String status) {
        this.servidorId = servidorId;
        this.idCompra = idCompra;
        this.idUsuario = idUsuario;
        this.valorCobranca = valorCobranca;
        this.status = status;
    }

    // Getters e setters
    public String getServidorId() {
        return servidorId;
    }

    public void setServidorId(String servidorId) {
        this.servidorId = servidorId;
    }

    public String getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(String idCompra) {
        this.idCompra = idCompra;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getValorCobranca() {
        return valorCobranca;
    }

    public void setValorCobranca(String valorCobranca) {
        this.valorCobranca = valorCobranca;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
