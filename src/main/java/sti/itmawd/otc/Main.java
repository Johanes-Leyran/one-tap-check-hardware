package sti.itmawd.otc;

import com.fazecast.jSerialComm.SerialPort;
import sti.itmawd.otc.arduino.ArduinoCommunication;

import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static ScheduledExecutorService scheduler;
    private static Scanner scanner;

    public static void main(String[] args) throws InterruptedException {
        if (!ArduinoCommunication.connectArduino()) return;

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Initiating shutdown sequence...");

            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    System.err.println("Scheduler did not terminate in the expected time. Forcing shutdown...");
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("Error waiting for scheduler termination: " + e.getMessage());
            }

            if(scanner != null) scanner.close();

            // Close the Arduino ports
            ArduinoCommunication.closeArduinoPort();

            System.out.println("Shutdown sequence completed. Exiting...");
        }));

        // Testing
        ArduinoCommunication.sendMessage("open led");
        Thread.sleep(2000);
        ArduinoCommunication.sendMessage("close led");
        Thread.sleep(1000);

        System.out.println("Started data receiving");

        // Receiving data from Arduino
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            for (SerialPort arduinoPort : ArduinoCommunication.arduinoPorts) {
                String data = ArduinoCommunication.receiveData(arduinoPort);
                if (data != null) {
                    // Put Code below
                    System.out.println(arduinoPort + ": " + data);
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        // Receiving text from user input
        scanner = new Scanner(System.in);
        Thread userInputThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("Enter input ('0' to exit): ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("0")) {
                    System.exit(0);
                } else if (input.startsWith("send")) {
                    ArduinoCommunication.sendMessage(input.replace("send ", ""));
                } else {
                    System.out.println("You entered: " + input);
                }
            }
        });
        userInputThread.start();
    }
}
