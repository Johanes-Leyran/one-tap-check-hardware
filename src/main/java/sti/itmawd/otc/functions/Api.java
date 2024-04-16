package sti.itmawd.otc.functions;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Api {
    public enum Purpose {
        CREATE_SESSION, ATTEND_SESSION, END_SESSION;
    }



    public static HttpResponse<String> sendTap(String room, String user, Purpose purpose) {
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("room_uuid", room);
            sendJson.put("user_uuid", user);
            sendJson.put("purpose", purpose.toString());

            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            //todo fix
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://group2research.pythonanywhere.com/rfid-api/tap-in/"))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(sendJson.toString()))
                    .build();

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e){
            return null;
        }
    }
}
