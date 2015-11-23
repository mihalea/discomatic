package ro.mihalea.discomatic;

/**
 * Created by Mircea on 23-Nov-15.
 */
public class Timer {
    private String task;
    private long start;

    public void start(String task) {
        this.task = task;
        start = System.nanoTime();
    }

    public void stop() {
        long elapsed = (System.nanoTime() - start) / 1000000; //nano to milli
        if(elapsed < 1000)
            System.out.println("Task [" + task + "] finished in " + elapsed + " ms");
        else
            System.out.println("Task [" + task + "] finished in " + elapsed/1000 + " s");
    }
}
