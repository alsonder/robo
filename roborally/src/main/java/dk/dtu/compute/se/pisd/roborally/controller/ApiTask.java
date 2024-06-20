package dk.dtu.compute.se.pisd.roborally.controller;


public class ApiTask implements Runnable {
    private volatile boolean shouldStop = false;

    public void getApi() {
        // Your API call logic here
        System.out.println("Calling API...");
        // Implement the actual API call logic
    }

    @Override
    public void run() {
        while (!shouldStop) {
            getApi();
            try {
                Thread.sleep(3000); // Sleep for 3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("ApiTask interrupted");
            }
        }
    }
    public void stopApiTask() {
        shouldStop = true;
    }
}