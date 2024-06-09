package main;

import Tasks.Task;
import Tasks.TaskQueue;
import Tasks.TaskType;
import matrixLogic.MatrixBrain;
import matrixLogic.MatrixExtractor;
import matrixLogic.MatrixMultiplier;

import java.io.*;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.*;


public class TaskCoordinator implements Runnable{
    private BlockingQueue<Task> matrixExtractor;
    private BlockingQueue<Task> matrixMultiplier;


    public TaskCoordinator() {
        matrixMultiplier = new LinkedBlockingQueue<>();
        matrixExtractor = new LinkedBlockingQueue<>();
    }
    @Override
    public void run() {
        while (true) {
            Task t = TaskQueue.getInstance().getNextTask();
            if(t.isPoisonPill()){
                try {
                    matrixExtractor.put(t);
                    matrixMultiplier.put(t);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            addTask(t);
        }
    }

    //if p is true take from extractor
    //if p is false take from multiply
    public Task getNextMatrix(boolean p) {
        try {
            if(p){
                return matrixExtractor.take(); // Uzimamo sledeći zadatak iz reda (čeka ako je red prazan)
            }else{
                return matrixMultiplier.take();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Ponovo postavljamo prekidnu oznaku ako je prekinuta nit
            return null;
        }
    }

    public void addTask(Task task) {
        try {
            if(task.getTaskType().equals(TaskType.CREATE)){
                matrixExtractor.put(task);
            }else if(task.getTaskType().equals(TaskType.MULTIPLY)){

                matrixMultiplier.put(task);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Ponovo postavljamo prekidnu oznaku ako je prekinuta nit
        }
    }
}
