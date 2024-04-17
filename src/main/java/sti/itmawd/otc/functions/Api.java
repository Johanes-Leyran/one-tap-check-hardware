package sti.itmawd.otc.functions;

import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Api {
    public enum Purpose {
        CREATE_SESSION, ATTEND_SESSION, END_SESSION;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+08:00");

    /**
     * Sends an HttpRequest to OneTapCheck main
     * @param port the Arduino
     * @param user the user ID
     * @param purpose the purpose of the request
     * @return HttpResponse, null if connection failed, "error" key if something went wrong
     */
    public static HttpResponse<String> sendTap(SerialPort port, String user, Purpose purpose) {
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("device_id", Tracking.getRoom(port));
            sendJson.put("tag_id", user);
            sendJson.put("time_in", FORMATTER.format(LocalDateTime.now())); //TODO Change
            sendJson.put("purpose", purpose.toString());
            if (purpose.equals(Purpose.ATTEND_SESSION)) sendJson.put("attendance_id", Tracking.getAttendance(port));

            HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

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
