package br.com.POO_CRUD.service;

import io.github.cdimascio.dotenv.Dotenv;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AlphaVantageService {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    private static final String API_KEY = getApiKey();
    private static final String BASE_URL = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s";

    public void exibirTop4AcoesB3() {
        if (API_KEY == null || API_KEY.isBlank()) {
            System.out.println("\nAPI_KEY_ALPHA nao configurada. Consulta de cotacoes ignorada.");
            return;
        }

        System.out.println("\nConsultando as principais acoes da B3 no momento...");
        String[] acoes = {"PETR4.SAO", "VALE3.SAO", "ITUB4.SAO", "BBAS3.SAO"};

        HttpClient client = HttpClient.newHttpClient();

        for (String acao : acoes) {
            try {
                String url = String.format(BASE_URL, acao, API_KEY);
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
                if(jsonObject.has("Global Quote") && !jsonObject.getAsJsonObject("Global Quote").isEmpty()) {
                    JsonObject quote = jsonObject.getAsJsonObject("Global Quote");
                    String preco = quote.get("05. price").getAsString();
                    System.out.println("Ativo: " + acao + " | Preco atual: R$ " + preco);
                } else {
                    System.out.println("Limite da API excedido ou ativo indisponivel: " + acao);
                }

                Thread.sleep(1500); // Pausa para respeitar o limite gratuito da API.

            } catch (Exception e) {
                System.err.println("Erro ao buscar cotacao de " + acao + ": " + e.getMessage());
            }
        }
    }

    private static String getApiKey() {
        String key = dotenv.get("API_KEY_ALPHA");
        return key == null || key.isBlank() ? System.getenv("API_KEY_ALPHA") : key;
    }
}
