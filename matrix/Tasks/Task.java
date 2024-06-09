package Tasks;

import java.io.File;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.Future;

public class Task {
    // Varijable članice klase
    private TaskType taskType;
    private File file;
    String name1;
    String name2;
    private Future<BigInteger[][]> matrix1;
    private Future<BigInteger[][]> matrix2;
    private boolean poisonPill;

    // Konstruktor for create matrix
    public Task(TaskType taskType, File file) {
        this.taskType = taskType;
        this.file = file;
        this.poisonPill = false;
    }

    //constructor for multiply matrix
    public Task(TaskType taskType,Future<BigInteger[][]> futureTask1,Future<BigInteger[][]> futureTask2,String name1, String name2) {
        this.taskType = taskType;
        this.matrix1 = futureTask1;
        this.matrix2 = futureTask2;
        this.poisonPill = false;
        this.name1 = name1;
        this.name2 = name2;
    }

    //constructor for just poisonPill
    public Task(){
        poisonPill = true;
    }

    public boolean isPoisonPill() {
        return poisonPill;
    }

    public TaskType getTaskType() {
        return taskType;
    }
    public  File getFile(){return file;}

    public  Future<BigInteger[][]> getMatrix1() {
        return matrix1;
    }

    public Future<BigInteger[][]> getMatrix2() {
        return matrix2;
    }

    public String getName1() {
        return name1;
    }

    public String getName2() {
        return name2;
    }
    //    // Metod za inicijalizaciju
//    public Future<M_Matrix> initiate() {
//        // Ovde započnite posao i vratite Future objekat
//        // koji će sadržati rezultate tog posla
//        return future;
//    }

}
