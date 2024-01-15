package sti.itmawd.otc;

import sti.itmawd.otc.functions.Connection;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static ScheduledExecutorService scheduler;
    //TODO: Fix OneTapCheckArduino
    //TODO: Also make tags contain UID instead of primitive shit lmao
    public static void main(String[] args) throws IOException, InterruptedException {
        scheduler = Executors.newScheduledThreadPool(1);
        Connection arduinoCon = new Connection();

        scheduler.scheduleAtFixedRate(() -> {
            arduinoCon.connectArduino();
            try { Thread.sleep(2000); } catch (InterruptedException e) { throw new RuntimeException(e); }
        }, 0, 3, TimeUnit.SECONDS);

        System.out.println("Started data receiving");
    }
}
