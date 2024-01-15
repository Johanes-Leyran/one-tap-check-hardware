package sti.itmawd.otc.functions;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Connection implements SerialPortDataListener {
    private static final List<SerialPort> currentPorts = Collections.synchronizedList(new ArrayList<>());
    private static final List<StringBuilder> buffers = Collections.synchronizedList(new ArrayList<>());

    /**
     * Establishes connection to the Arduino
     */
    public synchronized void connectArduino() {
        List<SerialPort> newPorts = Collections.synchronizedList(new ArrayList<>());

        synchronized (currentPorts) {
            //Gets new ports (that isn't contained in currentPorts) and puts them in newPorts
            for (SerialPort port : SerialPort.getCommPorts()) {
                if (currentPorts.stream().anyMatch(existingPort -> existingPort.getSystemPortName().equals(port.getSystemPortName()))) continue;

                if (port.toString().contains("Arduino")) {
                    System.out.println("Detected " + port);

                    synchronized (newPorts) {
                        newPorts.add(port);
                    }
                    synchronized (buffers) {
                        buffers.add(new StringBuilder());
                    }
                }
            }
        }

        //Initializes the newly detected ports
        synchronized (newPorts) {
            if (!newPorts.isEmpty()) {
                for (SerialPort port : newPorts) {
                    port.setComPortParameters(9600, 8, 1, 0);
                    port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

                    if (!port.openPort()) {
                        System.err.println(port + " can't open!");
                        continue;
                    }

                    port.addDataListener(this);

                    synchronized (currentPorts) {
                        currentPorts.add(port);
                    }


                    System.out.println("Connected to " + port);
                }
            }
        }
    }

    /**
     * Processes the data from the Arduino and sends it to Communication.processReceivedMessage
     */
    @Override
    public synchronized void serialEvent(SerialPortEvent e) {
        SerialPort port = e.getSerialPort();
        if (e.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
            buffers.remove(currentPorts.indexOf(port));
            currentPorts.remove(port);
            port.closePort();
            Tracking.removeRoom(Tracking.getRoomByPort(port));
            System.err.println(port + " has been disconnected.");
        }

        else if (e.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;

        if (port.bytesAvailable() < 0) return;

        int index;
        synchronized (currentPorts) {
            index = currentPorts.indexOf(port);
        }

        StringBuilder buffer;
        synchronized (buffers) {
            buffer = buffers.get(index);
        }

        byte[] readBuffer = new byte[port.bytesAvailable()];
        int numRead = port.readBytes(readBuffer, readBuffer.length);
        buffer.append(new String(readBuffer, 0, numRead));

        if (buffer.length() > 128) {
            System.out.println("Buffer overflow. Clearing buffer.");
            buffer.setLength(0);  // Clear the buffer
            return;
        }

        int startIndex = buffer.indexOf("[");
        int endIndex = buffer.indexOf("]");

        // If the buffer is growing large without a valid message, clean it up.
        if (buffer.length() > 100 && (startIndex == -1 || endIndex == -1)) {
            System.out.println("Buffer misalignment detected. Cleaning up buffer.");
            buffer.setLength(0);  // Clear the buffer
            return;
        }

        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) return;

        String message = buffer.substring(startIndex + 1, endIndex).trim();
        buffer.delete(0, endIndex + 1);

        try {
            Communication.processArduinoMessage(port, message);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * The SerialPort events you want to keep track off
     * @return the events
     */
    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
    }

    /**
     * Get ports currently connected to
     * @return List of ports
     */
    public static List<SerialPort> getCurrentPorts() {
        return currentPorts;
    }
}