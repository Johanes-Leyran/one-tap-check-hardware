package sti.itmawd.otc.functions;

import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONObject;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

public class Communication {
    //TODO: Make it send at the same time as connect arduino, every 20 seconds
    //TODO: Add a func for sending time to Arduinos separately
    private static LocalDateTime time = LocalDateTime.now();
    public static synchronized void sendTimeAll(){
        LocalDateTime now = LocalDateTime.now();
        if (time.getMinute() >= LocalDateTime.now().getMinute()) return;

    }

    //TODO: Send API the room uid and the user's uid, get back
    //TODO: the user level
    //TODO: the last name
    //TODO: the status (if success or not)
    //TODO: the status reason (like no schedule, no user existed, etc)
    //TODO: additional info (like class name, section, etc)
    public static void processArduinoMessage(SerialPort port, String message) throws IOException, InterruptedException {
        System.out.println(port + ": " +message);

        if (message.startsWith("setup ")){
            Tracking.addRoom(message.substring(6), port);
            sendMessage(port, "activate true");
        }

        if (message.startsWith("tap ")){
            String[] uids = message.substring(4).split(" ");
            String roomUID = uids[0];
            String userUID = uids[1];

            HttpResponse<String> response = ApiJson.sendRequest(roomUID, userUID);

            JSONObject obj = new JSONObject(response.body());

            boolean status;
            status = response.statusCode() < 400;

            String code;
            if(status) code = "success";
            else code = "error";

            sendMessage(port, "tap "
                    + status + " "
                    + response.statusCode() + " "
                    + obj.getString(code).replace(" ", "_").substring(0, 19)
                    + " hello");
        }
    }

    /**
     * Sends a message to all Arduinos
     * @param message the text
     */
    public static synchronized void sendMessage(String message){
        System.out.println("Sent message: " + message);
        for (SerialPort port : Connection.getCurrentPorts()) port.writeBytes(("[" + message + "]").getBytes(), message.length() + 2);
    }

    /**
     * Sends a message to an Arduino
     * @param message the text
     */
    public static synchronized void sendMessage(SerialPort port, String message){
        System.out.println("Sent message: " + message);
        port.writeBytes(("[" + message + "]").getBytes(), message.length() + 2);
    }



}
