package sti.itmawd.otc;

import sti.itmawd.otc.functions.Communication;
import sti.itmawd.otc.functions.Connection;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static ScheduledExecutorService scheduler;
    //TODO: Also make tags contain UUID instead of primitive shit lmao
    //TODO: api
    //TODO: move api to connection
    public static void main(String[] args) {
        scheduler = Executors.newScheduledThreadPool(2);
        Connection arduinoCon = new Connection();

        scheduler.scheduleAtFixedRate(arduinoCon::connectArduino, 2, 3, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> { Communication.sendTime(null); }, 0, 1, TimeUnit.MINUTES);

        System.out.println("Started!");
    }
}
