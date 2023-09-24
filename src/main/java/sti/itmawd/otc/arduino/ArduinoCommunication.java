package sti.itmawd.otc.arduino;

import com.fazecast.jSerialComm.SerialPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArduinoCommunication {
    public static final List<SerialPort> arduinoPorts = Collections.synchronizedList(new ArrayList<>());
    private static final List<StringBuilder> buffers = Collections.synchronizedList(new ArrayList<>());

    /**
     * Establishes connection to the Arduino
     * @return true if connection was established, false if not
     * @throws InterruptedException for Thread.sleep()
     */
    public static synchronized boolean connectArduino() throws InterruptedException {
        synchronized (arduinoPorts) {
            arduinoPorts.clear();
        }

        synchronized (buffers) {
            buffers.clear();
        }

        for (SerialPort port : SerialPort.getCommPorts()) {
            if (port.toString().contains("Arduino")) {
                synchronized (arduinoPorts) {
                    arduinoPorts.add(port);
                }
                synchronized (buffers) {
                    buffers.add(new StringBuilder());
                }
            }
        }

        synchronized (arduinoPorts) {
            if (arduinoPorts.isEmpty()) {
                System.out.println("No Arduinos detected! Cancelling operation!");
                return false;
            }

            for (SerialPort arduinoPort : arduinoPorts) {
                arduinoPort.setComPortParameters(9600, 8, 1, 0);
                arduinoPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

                if (!arduinoPort.openPort()) {
                    System.out.println(arduinoPort + " is not open! Cancelling operation!");
                    return false;
                }

                System.out.println("Connected to " + arduinoPort);
            }

            System.out.println("Connected to " + arduinoPorts.size() + " device(s)");
        }

        Thread.sleep(2000); // Buffer so that Arduino can initialize
        return true;
    }

    /**
     * Closes the arduino port, that's pretty much it
     */
    public static synchronized void closeArduinoPort(){
        for (SerialPort arduinoPort : arduinoPorts) {
            System.out.println("Closing " + arduinoPort);
            arduinoPort.closePort();
        }
    }

    /**
     * Sends a message to the arduino
     * @param message the text
     */
    public static synchronized void sendMessage(String message){
        System.out.println("Sent message: " + message);
        for (SerialPort arduinoPort : arduinoPorts) arduinoPort.writeBytes(("[" + message + "]").getBytes(), message.length() + 2);
    }

    /**
     * Processes the data from the arduino and returns the data
     * Null-check this because it returns null when it doesn't receive data
     * @return the data if it's available, null if there aren't any
     */

    public static synchronized String receiveData(SerialPort arduinoPort) {
        int index;
        synchronized (arduinoPorts) {
            index = arduinoPorts.indexOf(arduinoPort);
        }

        StringBuilder buffer;
        synchronized (buffers) {
            buffer = buffers.get(index);
        }

        if (arduinoPort.bytesAvailable() < 0) return null;

        byte[] readBuffer = new byte[arduinoPort.bytesAvailable()];
        int numRead = arduinoPort.readBytes(readBuffer, readBuffer.length);
        buffer.append(new String(readBuffer, 0, numRead));

        if (buffer.length() > 128) {
            System.out.println("Buffer overflow. Clearing buffer.");
            buffer.setLength(0);  // Clear the buffer
            return null;
        }

        int startIndex = buffer.indexOf("[");
        int endIndex = buffer.indexOf("]");

        // If the buffer is growing large without a valid message, clean it up.
        if (buffer.length() > 100 && (startIndex == -1 || endIndex == -1)) {
            System.out.println("Buffer misalignment detected. Cleaning up buffer.");
            buffer.setLength(0);  // Clear the buffer
            return null;
        }

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String message = buffer.substring(startIndex + 1, endIndex).trim();
            buffer.delete(0, endIndex + 1);
            return message;
        }

        return null;
    }

}