package sti.itmawd.otc.arduino;

import com.fazecast.jSerialComm.SerialPort;

public class ArduinoCommunication {
    private static SerialPort arduinoPort; // The actual port
    private static final StringBuilder buffer = new StringBuilder(); // For data receiving

    /**
     * Establishes connection to the Arduino
     * @return true if connection was established, false if not
     * @throws InterruptedException for Thread.sleep()
     */
    public static boolean connectArduino() throws InterruptedException {
        for (SerialPort port : SerialPort.getCommPorts()){
            if (port.toString().contains("Arduino")){
                arduinoPort = port;
            }
        }

        if (arduinoPort == null){
            System.out.println("Arduino not detected! Cancelling operation!");
            return false;
        }

        arduinoPort.setComPortParameters(9600, 8, 1, 0);
        arduinoPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

        if (!arduinoPort.openPort()){
            System.out.println("Port is not open! Cancelling operation!");
            return false;
        }

        System.out.println("Connected to " + arduinoPort.toString());
        Thread.sleep(2000); //Buffer so that arduino can initialize
        return true;
    }

    /**
     * Closes the arduino port, that's pretty much it
     */
    public static void closeArduinoPort(){
        arduinoPort.closePort();
        System.out.println("Closing port...");
    }

    /**
     * Sends a message to the arduino
     * @param message the text
     */
    public static void sendMessage(String message){
        System.out.println("Sent message: " + message);
        arduinoPort.writeBytes(("[" + message + "]").getBytes(), message.length() + 2);
    }

    /**
     * Processes the data from the arduino and returns the data
     * Null-check this because it returns null when it doesn't receive data
     * @return the data if it's available, null if there aren't any
     */
    public static String receiveData() {
        if (arduinoPort.bytesAvailable() < 0) return null;

        byte[] readBuffer = new byte[arduinoPort.bytesAvailable()];
        int numRead = arduinoPort.readBytes(readBuffer, readBuffer.length);
        buffer.append(new String(readBuffer, 0, numRead));

        if (buffer.length() > 128){
            System.out.println("Something went wrong! Buffer exceeded 128, check Arduino delay!");
            buffer.delete(0, buffer.length() - 128);
        }

        int startIndex = buffer.indexOf("[");
        int endIndex = buffer.indexOf("]");

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String message = buffer.substring(startIndex + 1, endIndex).trim();
            buffer.delete(0, endIndex + 1);
            return message;
        }

        return null;
    }


}