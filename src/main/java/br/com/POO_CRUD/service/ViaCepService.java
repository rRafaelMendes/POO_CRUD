package br.com.POO_CRUD.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class ViaCepService {
    private static final String BASE_URL = "https://viacep.com.br/ws/%s/json/";
    private final HttpClient client = HttpClient.newHttpClient();

    public Optional<Endereco> buscarEnderecoPorCep(String cep) {
        if (cep == null || cep.isBlank()) {
            return Optional.empty();
        }

        try {
            String url = String.format(BASE_URL, cep);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            if (jsonObject.has("erro") && jsonObject.get("erro").getAsBoolean()) {
                return Optional.empty();
            }

            Optional<String> rua = getAsString(jsonObject, "logradouro");
            Optional<String> cidade = getAsString(jsonObject, "localidade");

            if (rua.isEmpty() || cidade.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new Endereco(rua.get(), cidade.get()));
        } catch (Exception e) {
            System.err.println("Erro ao consultar CEP no ViaCEP: " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> getAsString(JsonObject jsonObject, String key) {
        if (!jsonObject.has(key) || jsonObject.get(key).isJsonNull()) {
            return Optional.empty();
        }

        String value = jsonObject.get(key).getAsString().trim();
        return value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    public record Endereco(String rua, String cidade) {}
}
