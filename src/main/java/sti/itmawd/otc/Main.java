package sti.itmawd.otc;

import sti.itmawd.otc.arduino.ArduinoCommunication;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        if (!ArduinoCommunication.connectArduino()) return;

        // Testing
        ArduinoCommunication.sendMessage("open led");
        Thread.sleep(2000);
        ArduinoCommunication.sendMessage("close led");
        Thread.sleep(1000);


        System.out.println("Started data receiving");

        // Receiving data from Arduino
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            String data = ArduinoCommunication.receiveData();
            if(data == null) return;

            // Put Code below
            System.out.println("Received data: " + data);


        }, 0, 100, TimeUnit.MILLISECONDS);

        // Receiving text from user input
        Thread userInputThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("Enter input ('0' to exit): ");
                String input = scanner.nextLine();

                // Exit Code (0)
                if ("0".equalsIgnoreCase(input)) {
                    System.out.println("Shutting down...");

                    scheduler.shutdown();
                    try {
                        if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                            scheduler.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        scheduler.shutdownNow();
                    }

                    ArduinoCommunication.closeArduinoPort();
                    Thread.currentThread().interrupt();
                    scanner.close();

                    System.out.println("Exiting...");
                    System.exit(0);
                } else {
                    System.out.println("You entered: " + input);
                }

            }
            scanner.close();
        }); userInputThread.start();

        // You can add more code below
    }
}