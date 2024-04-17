package sti.itmawd.otc.functions;

import com.fazecast.jSerialComm.SerialPort;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Api {
    public enum Purpose {
        CREATE_SESSION, ATTEND_SESSION, END_SESSION;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+08:00'");

    /**
     * Sends an HttpRequest to OneTapCheck main
     * @param port the Arduino
     * @param user the user ID
     * @param purpose the purpose of the request
     * @return HttpResponse, null if connection failed, "error" key if something went wrong
     */
    @Nullable
    public static HttpResponse<String> sendTap(SerialPort port, String user, Purpose purpose) {
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("device_id", Tracking.getRoom(port));
            sendJson.put("tag_id", user);
            sendJson.put("time_in", FORMATTER.format(LocalDateTime.now())); //TODO Change
            sendJson.put("purpose", purpose.toString());
            if (!purpose.equals(Purpose.CREATE_SESSION)) sendJson.put("attendance_id", Tracking.getAttendance(port));

            System.out.println(sendJson.get("device_id"));
            System.out.println(sendJson.get("tag_id"));
            System.out.println(sendJson.get("time_in"));
            System.out.println(sendJson.get("purpose"));

            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://leyranmoment.pythonanywhere.com/one_tap_api/attendance/" + purpose.toString().toLowerCase().replace("_session", "") + "/"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(sendJson.toString()))
                    .build();

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e){
            return null;
        }
    }
}
