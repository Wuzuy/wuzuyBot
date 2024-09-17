package com.wuzuy.bot.functional;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Webhook {

    private static final OkHttpClient httpClient = new OkHttpClient();
    private final Map<String, String> webhookCache = new HashMap<>();

    public String criarWebhook(Guild ignoredGuild, TextChannel channel, String webhookName, String avatarUrl) throws IOException {
        String url = "https://discord.com/api/v10/channels/" + channel.getId() + "/webhooks";

        JSONObject json = new JSONObject();
        json.put("name", webhookName);
        if (avatarUrl != null) {
            json.put("avatar", avatarUrl);
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "TOKEN")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erro ao criar Webhook: " + Objects.requireNonNull(response.body()).string());
            }
            JSONObject responseJson = new JSONObject(Objects.requireNonNull(response.body()).string());
            return responseJson.getString("url");
        }
    }

    public void enviarMensagemWebhook(String webhookUrl, String mensagem, String username, String avatarUrl) throws IOException {
        JSONObject json = new JSONObject();
        json.put("content", mensagem);
        json.put("username", username);
        json.put("avatar_url", avatarUrl);

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erro ao enviar mensagem via Webhook: " + Objects.requireNonNull(response.body()).string());
            }
        }
    }

    public String getWebhookUrl(String userId, Guild guild, TextChannel channel, String username, String avatarUrl) throws IOException {

        String webhookUrl = webhookCache.get(userId);
        if (webhookUrl == null) {

            webhookUrl = criarWebhook(guild, channel, username, avatarUrl);
            webhookCache.put(userId, webhookUrl);
        }
        return webhookUrl;
    }
}
