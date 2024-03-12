package com.merqury.agpu.general;

public class Foundation {
    public static void async(Runnable task){
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
