package com.wuzuy.bot.pix;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;

public class PixApiClient {
    private static OkHttpClient httpClient;

    static {
        try {
            httpClient = createHttpClientWithCertificate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static OkHttpClient createHttpClientWithCertificate() throws Exception {
        // Carrega o certificado
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream("src/main/resources/homologacao.p12")) {
            keyStore.load(fis, null); // Sem senha
        }

        // Configura o SSLContext com o certificado
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, null); // Sem senha

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

        // Configura o TrustManager para validar os certificados
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);

        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        // Cria o cliente OkHttp com o SSLContext
        return new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0])
                .build();
    }

    public static JsonObject criarCobrancaPix(String token, int expiracao, String cobranca, String infoPagador, String chave) throws IOException {
        JsonObject body = new JsonObject();

        JsonObject calendario = new JsonObject();
        calendario.addProperty("expiracao", expiracao);
        body.add("calendario", calendario);

        JsonObject valor = new JsonObject();
        valor.addProperty("original", cobranca);
        body.add("valor", valor);

        body.addProperty("chave", chave);
        body.addProperty("solicitacaoPagador", infoPagador);

        String jsonBody = new Gson().toJson(body);

        Request request = new Request.Builder()
                .url("https://pix-h.api.efipay.com.br/v2/cob")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (response.isSuccessful()) {
                return JsonParser.parseString(responseBody).getAsJsonObject();
            } else {
                throw new IOException("Falha ao criar a cobrança Pix: " + responseBody);
            }
        }
    }


    public static JsonObject consultarCobrancaPix(String token, String txid) throws IOException {
        Request request = new Request.Builder()
                .url("https://pix-h.api.efipay.com.br/v2/cob/" + txid)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (response.isSuccessful()) {
                return JsonParser.parseString(responseBody).getAsJsonObject();
            } else {
                throw new IOException("Falha ao consultar a cobrança Pix: " + responseBody);
            }
        }
    }
}
