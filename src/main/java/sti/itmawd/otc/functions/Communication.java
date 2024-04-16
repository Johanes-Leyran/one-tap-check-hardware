package sti.itmawd.otc.functions;

import com.fazecast.jSerialComm.SerialPort;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static sti.itmawd.otc.functions.Api.sendTap;

public class Communication {
    enum FromArduino{
        TAP, SETUP;

        public static void process(SerialPort port, String message){
            String[] command = message.split(" ");

            if (Objects.equals(command[0], FromArduino.SETUP.toString().toLowerCase())){
                ToArduino.sendToArduino(port, ToArduino.ACTIVATE, "true");
                sendTime(port);
                Tracking.setRoom(port, message.substring(6));
            }

            else if (Objects.equals(command[0], FromArduino.TAP.toString().toLowerCase())){
                //TODO tap nameHere/Failed to Tap./false || tap nameHere/Successful tap/true
                String roomUID = command[1];
                if (!roomUID.equals(Tracking.getRoom(port))) throw new RuntimeException(); //TODO
                String userUID = command[2];
                Api.Purpose purpose;

                if (!Tracking.isRoomUsed(port)) { //CREATE
                    purpose = Api.Purpose.CREATE_SESSION;

                    HttpResponse<String> response = sendTap(port, userUID, purpose);
                    if (response == null){
                        ToArduino.sendToArduino(port, ToArduino.TAP, " /Connection Issue/false");
                        return;
                    }

                    JSONObject jsonObject = new JSONObject(response.body());

                    if (jsonObject.has("error")){
                        ToArduino.sendToArduino(port, ToArduino.TAP, " /" + jsonObject.getString("error") + "/false");
                        return;
                    }

                    ToArduino.sendToArduino(port, ToArduino.TAP, jsonObject.getString("Teacher Name") + "/" + jsonObject.getString("Message") + "/true");
                    ToArduino.sendToArduino(port, ToArduino.LCD, "1 T. " + jsonObject.getString("Teacher Name"));
                    Tracking.setAttendance(port, jsonObject.getString("Attendance ID"));
                } else {
                    if (Tracking.getStaff(port).equals(userUID)) { //END SESSION
                        purpose = Api.Purpose.END_SESSION;

                        HttpResponse<String> response = sendTap(port, userUID, purpose);

                        if (response == null) {
                            ToArduino.sendToArduino(port, ToArduino.TAP, "tap nameTest/Connection Issue/false");
                            return;
                        }

                        JSONObject jsonObject = new JSONObject(response.body());

                        if (jsonObject.has("error")){
                            ToArduino.sendToArduino(port, ToArduino.TAP, " /" + jsonObject.getString("error") + "/false");
                            return;
                        }

                        ToArduino.sendToArduino(port, ToArduino.LCD, "1 Available");

                    } else { //ATTEND
                        purpose = Api.Purpose.ATTEND_SESSION;
                        HttpResponse<String> response = sendTap(port, userUID, purpose);

                        if (response == null) {
                            ToArduino.sendToArduino(port, ToArduino.TAP, "tap nameTest/Connection Issue/false");
                            return;
                        }

                        JSONObject jsonObject = new JSONObject(response.body());

                        if (jsonObject.has("error")){
                            ToArduino.sendToArduino(port, ToArduino.TAP, " /" + jsonObject.getString("error") + "/false");
                            return;
                        }

                        ToArduino.sendToArduino(port, ToArduino.TAP, jsonObject.getString("Last Name") + "/" + jsonObject.getString("Message") + "/true");
                    }
                }
            }

        }

    }

    enum ToArduino{
        LCD, ACTIVATE, TAP, SETUP, TIME;

        /**
         * Send a message to an Arduino
         * @param port port, null if all
         * @param type ToArduino type
         * @param arguments string arguments, null if none
         */
        public static void sendToArduino(@Nullable SerialPort port, @NotNull ToArduino type, @Nullable String arguments){
            sendMessage(port, type.toString().toLowerCase() + (arguments == null? "" : " " + arguments));
        }
    }

    /**
     * Sends a message to an Arduino
     * @param port the port, null if all ports
     * @param message the text
     */
    public static synchronized void sendMessage(@Nullable SerialPort port, @NotNull String message){
        System.out.println("Sent message: " + message);

        if (port == null) for (SerialPort serialPort : Tracking.getPortList()) serialPort.writeBytes(("[" + message + "]").getBytes(), message.length() + 2);
        else port.writeBytes(("[" + message + "]").getBytes(), message.length() + 2);
    }

    public static void sendTime(@Nullable SerialPort port){
        LocalDateTime date = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy, hh:mma");
        ToArduino.sendToArduino(port, ToArduino.TIME, formatter.format(date));
    }
}
