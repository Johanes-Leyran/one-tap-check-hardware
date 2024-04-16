package sti.itmawd.otc.functions;

import com.fazecast.jSerialComm.SerialPort;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tracking {
    /**
     * Room Map - For Room UID
     * Attendance Map - For Attendance UID
     * Staff Map - For Staff UID
     * Builder Map - For StringBuilder (Serial connection)
     */
    private static final HashMap<@NotNull SerialPort, String> roomMap = new HashMap<>();
    private static final HashMap<@NotNull SerialPort, String> attendanceMap = new HashMap<>();
    private static final HashMap<@NotNull SerialPort, String> staffMap = new HashMap<>();
    private static final HashMap<@NotNull SerialPort, StringBuilder> builderMap = new HashMap<>();

    /**
     * Adds an Arduino to tracking
     * @param port the Arduino
     */
    public static synchronized void addArduino(SerialPort port){
        roomMap.put(port, "");
        attendanceMap.put(port, "");
        staffMap.put(port, "");
        builderMap.put(port, new StringBuilder());
    }

    /**
     * Removes an Arduino from tracking
     * @param port the Arduino
     */
    public static synchronized void removeArduino(SerialPort port){
        roomMap.remove(port);
        attendanceMap.remove(port);
        staffMap.remove(port);
        builderMap.remove(port);
    }

    /**
     * Gets the list of Arduino
     * @return List of Arduinos
     */
    public static synchronized List<SerialPort> getPortList(){
        return new ArrayList<>(roomMap.keySet());
    }

    /**
     * Gets the UID of an Arduino
     * @param port the Arduino
     * @return the UID
     */
    public static synchronized String getRoom(SerialPort port){
        return roomMap.get(port);
    }

    /**
     * Sets an Arduino's UID
     * @param port the Arduino
     * @param uid the UID
     */
    public static synchronized void setRoom(SerialPort port, String uid){
        roomMap.replace(port, uid);
    }

    /**
     * Gets the Attendance UID of an Arduino
     * @param port the Arduino
     * @return the Attendance UID
     */
    public static synchronized String getAttendance(SerialPort port){
        return attendanceMap.get(port);
    }

    /**
     * Sets the Attendance UID of an Arduino
     * Use clearAttendance if you want to clear. Don't bother with ""
     * @param port the Arduino
     * @param uid the UID
     */
    public static synchronized void setAttendance(SerialPort port, String uid){
        attendanceMap.replace(port, uid);
    }

    /**
     * Clears the Attendance UID of an Arduino
     * @param port the Arduino
     */
    public static synchronized void clearAttendance(SerialPort port){
        attendanceMap.replace(port, "");
    }

    /**
     * Gets the UID of the staff currently using the Arduino
     * @param port the Arduino
     * @return the staff's UID
     */

    public static synchronized String getStaff(SerialPort port){
        return staffMap.get(port);
    }

    /**
     * Sets the staff that will use an Arduino
     * Use clearStaff if you want to clear. Don't bother with ""
     * @param port the Arduino
     * @param uid the staff's UID
     */
    public static synchronized void setStaff(SerialPort port, String uid){
        staffMap.replace(port, uid);
    }

    /**
     * Clears the UID of the staff using the Arduino
     * @param port the Arduino
     */
    public static synchronized void clearStaff(SerialPort port){
        staffMap.replace(port, "");
    }

    /**
     * Checks if there is a staff currently using the Arduino
     * @param port the Arduino
     * @return true if there is, false if there isn't
     */
    public static boolean isRoomUsed(SerialPort port){
        return getStaff(port).isEmpty();
    }

    /**
     * Gets the StringBuilder of an Arduino (For Serial connection purposes)
     * @param port the Arduino
     * @return the mutable StringBuilder
     */
    public static synchronized StringBuilder getBuilder(SerialPort port){
        return builderMap.get(port);
    }
}