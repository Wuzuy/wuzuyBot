package com.wuzuy.bot.pix;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.util.Base64;

public class TokenGenerator {


    public static void main(String[] args) throws Exception {
        System.out.println(getAccessToken());
    }

    public static String getAccessToken() throws Exception {
        // Load the keystore from the resource folder
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream keyStoreStream = new FileInputStream("src/main/resources/homologacao.p12")) {
            keyStore.load(keyStoreStream, "".toCharArray()); // Provide the keystore password
        }

        // Initialize KeyManagerFactory with the keystore
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "".toCharArray()); // Provide the keystore password

        // Initialize SSLContext with the key managers from the keystore
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        // Set up the connection
        URL url = new URL("https://pix-h.api.efipay.com.br/oauth/token");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setSSLSocketFactory(sslContext.getSocketFactory());
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        // Prepare Basic Auth header
        String basicAuth = Base64.getEncoder().encodeToString((PUBLIC_KEY + ':' + SECRET_KEY).getBytes());
        conn.setRequestProperty("Authorization", "Basic " + basicAuth);

        // Send the POST data
        String input = "{\"grant_type\": \"client_credentials\"}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(input.getBytes());
        }

        // Read the response
        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }
        }

        // Parse and return the access token
        JsonObject jsonResponse = JsonParser.parseString(responseBuilder.toString()).getAsJsonObject();
        return jsonResponse.get("access_token").getAsString();
    }
}
