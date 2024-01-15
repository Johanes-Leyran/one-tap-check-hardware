package sti.itmawd.otc.functions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public class ApiJson {
    public static HttpResponse<String> sendRequest(String room, String user) throws IOException, InterruptedException{
        JSONObject sendJson = new JSONObject();
        sendJson.put("room_uuid", room);
        sendJson.put("user_uuid", user);

        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://group2research.pythonanywhere.com/rfid-api/tap-in/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(sendJson.toString()))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
