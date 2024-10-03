// ConversationState.java
package com.wuzuy.bot.models;

public class ConversationState {
    public enum Step {
        AWAITING_TITLE,
        AWAITING_DESCRIPTION,
        AWAITING_VALUE,
        COMPLETED
    }

    private String userId;
    private Step currentStep;
    private String titulo;
    private String descricao;
    private String valor;
    private String botMessageId; // Para armazenar o ID da mensagem do bot

    public ConversationState() {
        currentStep = Step.AWAITING_TITLE;
    }

    // Getters e setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Step getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Step currentStep) {
        this.currentStep = currentStep;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getBotMessageId() {
        return botMessageId;
    }

    public void setBotMessageId(String botMessageId) {
        this.botMessageId = botMessageId;
    }
}
