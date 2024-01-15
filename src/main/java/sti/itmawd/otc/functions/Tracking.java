package sti.itmawd.otc.functions;

import com.fazecast.jSerialComm.SerialPort;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Tracking {
    private static final HashMap<String, SerialPort> roomArduinos = new HashMap<>();
    private static final HashMap<String, Boolean> roomAvailability = new HashMap<>();

    /**
     * Adds a room to the list of rooms
     * @param uid The Arduino's uid
     * @param port The Arduino's port
     */
    public static void addRoom(String uid, SerialPort port){
        roomArduinos.put(uid, port);
        //roomAvailability.put(uid, false);
    }

    /**
     * Removes a room from the list of rooms.
     * Used in disconnection
     * @param uid The Arduino's uid
     */
    public static void removeRoom(String uid){
        roomArduinos.remove(uid);
    }

    //public static void makeRoomAvailable(String uid){
    //    if (roomArduinos.containsKey(uid)) return; //Add some error here idk
    //    roomAvailability.put(uid, true);
    //}

    //public static void makeRoomUnavailable(String uid){
    //    if (roomAvailability.containsKey(uid)) return; //Add some error here idk
    //    roomAvailability.put(uid, false);
    //}

    /**
     * Gets the UID of the port given
     * @param port the arduino port
     * @return the UID
     */
    public static String getRoomByPort(SerialPort port){
        for (Map.Entry<String, SerialPort> entry : roomArduinos.entrySet()){
            if (entry.getValue() == port){
                return entry.getKey();
            }
        }

        return null;
    }
}
