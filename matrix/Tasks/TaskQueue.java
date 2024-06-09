package Tasks;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskQueue{
    private static TaskQueue instance;
    private BlockingQueue<Task> queue;
//    private main.TaskCoordinator taskCoordinator;

    private TaskQueue() {
        // initialization queue and thread
        queue = new LinkedBlockingQueue<>();
//        taskCoordinator = new main.TaskCoordinator();
//        Thread thread = new Thread(taskCoordinator);
//        thread.start();
    }

    // Metoda za dobijanje instance Singleton-a
    public static synchronized TaskQueue getInstance() {
        if (instance == null) {
            instance = new TaskQueue();
        }
        return instance;
    }

    // Metoda za dodavanje zadatka u red
    public void addTask(Task task, File file) {
        try {
            queue.put(task);
//            System.out.println(task.getTaskType());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Ponovo postavljamo prekidnu oznaku ako je prekinuta nit
        }
    }

    // Metoda za preuzimanje sledećeg zadatka iz reda
    public Task getNextTask() {
        try {
            return queue.take(); // Uzimamo sledeći zadatak iz reda (čeka ako je red prazan)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Ponovo postavljamo prekidnu oznaku ako je prekinuta nit
            return null;
        }
    }



}
