package sti.itmawd.otc.functions;

import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

public class Api {
    public enum Purpose {
        CREATE_SESSION, ATTEND_SESSION, END_SESSION;
    }



    public static HttpResponse<String> sendTap(SerialPort port, String user, Purpose purpose) {
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("scanner_id", Tracking.getRoom(port));
            sendJson.put("tag_id", user);
            sendJson.put("time", Instant.now().getEpochSecond());
            sendJson.put("purpose", purpose.toString());
            if (purpose.equals(Purpose.ATTEND_SESSION)){
                sendJson.put("attendance_id", Tracking.getAttendance(port));
            }

            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();


            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://leyranmoment.pythonanywhere.com/one_tap_api/attendance/create"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(sendJson.toString()))
                    .build();

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e){
            return null;
        }
    }
}
