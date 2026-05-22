package br.com.POO_CRUD.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ViaCepService {
    private static final String BASE_URL = "https://viacep.com.br/ws/%s/json/";
    private final HttpClient client = HttpClient.newHttpClient();

    public Endereco buscarEnderecoPorCep(String cep) {
        try {
            String url = String.format(BASE_URL, cep);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            if (jsonObject.has("erro") && jsonObject.get("erro").getAsBoolean()) {
                return null;
            }

            String rua = getAsString(jsonObject, "logradouro");
            String cidade = getAsString(jsonObject, "localidade");

            if (rua.isBlank() || cidade.isBlank()) {
                return null;
            }

            return new Endereco(rua, cidade);
        } catch (Exception e) {
            System.err.println("Erro ao consultar CEP no ViaCEP: " + e.getMessage());
            return null;
        }
    }

    private String getAsString(JsonObject jsonObject, String key) {
        return jsonObject.has(key) && !jsonObject.get(key).isJsonNull()
                ? jsonObject.get(key).getAsString()
                : "";
    }

    public static class Endereco {
        private final String rua;
        private final String cidade;

        public Endereco(String rua, String cidade) {
            this.rua = rua;
            this.cidade = cidade;
        }

        public String getRua() {
            return rua;
        }

        public String getCidade() {
            return cidade;
        }
    }
}
