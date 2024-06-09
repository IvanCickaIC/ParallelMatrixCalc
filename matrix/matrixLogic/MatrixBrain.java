package matrixLogic;

import Tasks.Task;
import Tasks.TaskQueue;
import Tasks.TaskType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class MatrixBrain {
    Map<String, Future<BigInteger[][]>> matrice;
    Map<String, String>fileName;
    private boolean sync;



    public MatrixBrain(){
        matrice = new HashMap<>();
        this.fileName = new HashMap<>();
    }


    public void addMatrix(Future<BigInteger[][]> matrix, Map<String, String> fileName ){
        String key = fileName.entrySet().iterator().next().getKey();
        String value = fileName.entrySet().iterator().next().getValue();

        if(this.fileName.containsValue(value) && !Objects.equals(value, "in memory")) {
            String existingKey = null;
            for (Map.Entry<String, String> entry : this.fileName.entrySet()) {
                if (entry.getValue().equals(value)) {
                    existingKey = entry.getKey();
                    break;
                }
            }
            this.fileName.remove(existingKey);
            this.matrice.remove(existingKey);
        }
        this.matrice.put(fileName.entrySet().iterator().next().getKey(), matrix);
        this.fileName.put(fileName.entrySet().iterator().next().getKey(), fileName.entrySet().iterator().next().getValue());
    }

    public void printMatrices() {


        // Iterate over each entry in the map synchronously
        for (Map.Entry<String, Future<BigInteger[][]>> entry : matrice.entrySet()) {
            try {
                // Retrieve the future for the matrix
                Future<BigInteger[][]> futureMatrix = entry.getValue();

                // Get the matrix from the future, waiting at most 5 seconds for the result
                BigInteger[][] matrix = futureMatrix.get();

                // Print the key and matrix information
                String key = entry.getKey();
                int rows = matrix.length;
                int columns = matrix[0].length;
                String location = fileName.get(key);
                System.out.println(key + " | rows = " + rows + ", columns = " + columns + " | " + location);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                // Handle any exceptions here
            }
        }
    }

    public void deleteMatName(String fileNameToDelete) {
        boolean found = false;
         String keyToRemove = null;

        for (Map.Entry<String, String> entry : this.fileName.entrySet()) {
            if (entry.getValue().contains(fileNameToDelete)) {
                found = true;
                File fileToDelete = new File(entry.getValue());
                if (fileToDelete.exists()) {
                    fileToDelete.delete();
                    System.out.println("File " + entry.getKey() + " deleted successfully.");
                    keyToRemove = entry.getKey();
                } else {
                    System.out.println("File " + entry.getKey() + " does not exist.");
                }
            }
        }
        if(keyToRemove != null){
            this.fileName.remove(keyToRemove);
            matrice.remove(keyToRemove);
        }
        if (!found) {
            System.out.println("No file matching '" + fileNameToDelete + "' found in the mapping.");
        }
    }

    public void deleteAllMatrices(String folderName){
        boolean found = false;
        List <String> keyToRemove = new ArrayList<>();

        for (Map.Entry<String, String> entry : this.fileName.entrySet()) {
            if (entry.getValue().contains(folderName)) {
                found = true;
                File fileToDelete = new File(entry.getValue());
                if (fileToDelete.exists()) {
                    fileToDelete.delete();
                    System.out.println("File " + entry.getKey() + " deleted successfully.");
                    keyToRemove.add(entry.getKey());
                } else {
                    System.out.println("File " + entry.getKey() + " does not exist.");
                }
            }
        }

        for(String key : keyToRemove){
            this.fileName.remove(key);
            this.matrice.remove(key);
        }

        if (!found) {
            System.out.println("No Folder '" + folderName + "' found in the mapping.");
        }
    }

    public void printFirstNMatrices(int firstN) {

        int counter = 0;
        // Iterate over each entry in the map synchronously
        for (Map.Entry<String, Future<BigInteger[][]>> entry : matrice.entrySet()) {
            try {

                // Retrieve the future for the matrix
                Future<BigInteger[][]> futureMatrix = entry.getValue();

                // Get the matrix from the future, waiting at most 5 seconds for the result
                BigInteger[][] matrix = futureMatrix.get();
                if(counter < firstN) {
                    // Print the key and matrix information
                    String key = entry.getKey();
                    int rows = matrix.length;
                    int columns = matrix[0].length;
                    String location = fileName.get(key);
                    System.out.println(key + " | rows = " + rows + ", columns = " + columns + " | " + location);
                }
                counter++;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                // Handle any exceptions here
            }
        }
    }

    public void printLastNMatrices(int lastN) {
        int totalMatrices = matrice.size();
        int counter = 0;
        // Iterate over each entry in the map synchronously
        for (Map.Entry<String, Future<BigInteger[][]>> entry : matrice.entrySet()) {
            try {

                // Retrieve the future for the matrix
                Future<BigInteger[][]> futureMatrix = entry.getValue();

                // Get the matrix from the future, waiting at most 5 seconds for the result
                BigInteger[][] matrix = futureMatrix.get();
                if(counter + lastN >= totalMatrices) {
                    // Print the key and matrix information
                    String key = entry.getKey();
                    int rows = matrix.length;
                    int columns = matrix[0].length;
                    String location = fileName.get(key);
                    System.out.println(key + " | rows = " + rows + ", columns = " + columns + " | " + location);
                }
                counter ++;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                // Handle any exceptions here
            }
        }
    }

    public void multiplySync(String mat1, String mat2){
        if(matrice.get(mat1) == null || matrice.get(mat2) == null){
            System.out.println("matrice:"+ mat1+" , "+ mat2+" not exist");
        }else{
            Task task = new Task(TaskType.MULTIPLY,matrice.get(mat1), matrice.get(mat2),mat1,mat2);
            TaskQueue.getInstance().addTask(task,null);
            sync = true;
            while(sync){
                if(matrice.get(mat1+mat2) != null){
                    break;
                }
            }
            try{
                if(matrice.get(mat1+mat2) != null){
                matrice.get(mat1+mat2).get();
                }
            }catch (Exception e){
                System.out.println("Matrix not exist");
            }
        }
    }

    public void multiplyASync(String mat1, String mat2){
        if(matrice.get(mat1) == null || matrice.get(mat2) == null){
            System.out.println("matrice:"+ mat1+" , "+ mat2+" not exist");
        }else{
            Task task = new Task(TaskType.MULTIPLY,matrice.get(mat1), matrice.get(mat2),mat1,mat2);
            TaskQueue.getInstance().addTask(task,null);
        }
    }


    public void checkIfFutureIsFinished(String key){
        // Find the entry for the specified key
        Future<BigInteger[][]> future = matrice.get(key);
        if (future != null) {
            if(future.isDone()){
                printMatrixInfo(key);
            } else {
                System.out.println("Matrix: " + key + " is still being processed...");
            }
        } else {
            System.out.println("No future object found for key: " + key);
        }

    }

    public void printMatrixInfo(String key){
        // Find the entry for the specified key
        Future<BigInteger[][]> future = matrice.get(key);
        if (future != null) {
            CompletableFuture.supplyAsync(() -> {
                try {
                    // Get the matrix from the future, waiting at most 5 seconds for the result
                    BigInteger[][] matrix = future.get();

                    // Print the key and matrix information
                    int rows = matrix.length;
                    int columns = matrix[0].length;
                    String location = fileName.get(key);
                    System.out.println(key + " | rows = " + rows + ", columns = " + columns + " | " + location);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    // Handle any exceptions here
                }
                return null; // CompletableFuture requires a return value
            });
        } else {
            System.out.println("Matrix not found: " + key);
        }
    }

    public  void sortDecreasing(){
        // Pretvaranje mape u listu za sortiranje
        List<Map.Entry<String, Future<BigInteger[][]>>> entries = new ArrayList<>(matrice.entrySet());

        // Sortiranje
        entries.sort(Comparator.comparingInt((Map.Entry<String, Future<BigInteger[][]>> e) -> {
            try {
                return e.getValue().get().length; // Sortiranje po broju redova
            } catch (Exception ex) {
                ex.printStackTrace();
                return 0;
            }
        }).thenComparingInt(e -> {
            try {
                return e.getValue().get()[0].length; // Sortiranje po broju kolona
            } catch (Exception ex) {
                ex.printStackTrace();
                return 0;
            }
        }).reversed());

        // Konvertovanje liste nazad u mapu
        Map<String, Future<BigInteger[][]>> sortedMatrice = new LinkedHashMap<>();
        for (Map.Entry<String, Future<BigInteger[][]>> entry : entries) {
            sortedMatrice.put(entry.getKey(), entry.getValue());
        }

        // Ažuriranje originalne mape
        matrice = sortedMatrice;
    }

    public void sortAsceding(){
        // Pretvaranje mape u listu za sortiranje
        List<Map.Entry<String, Future<BigInteger[][]>>> entries = new ArrayList<>(matrice.entrySet());

        // Sortiranje
        entries.sort(Comparator.comparingInt((Map.Entry<String, Future<BigInteger[][]>> e) -> {
            try {
                return e.getValue().get().length; // Sortiranje po broju redova
            } catch (Exception ex) {
                ex.printStackTrace();
                return 0;
            }
        }).thenComparingInt(e -> {
            try {
                return e.getValue().get()[0].length; // Sortiranje po broju kolona
            } catch (Exception ex) {
                ex.printStackTrace();
                return 0;
            }
        }));


        // Konvertovanje liste nazad u mapu
        Map<String, Future<BigInteger[][]>> sortedMatrice = new LinkedHashMap<>();
        for (Map.Entry<String, Future<BigInteger[][]>> entry : entries) {
            sortedMatrice.put(entry.getKey(), entry.getValue());
        }

        // Ažuriranje originalne mape
        matrice = sortedMatrice;
    }

    public void saveToFile(String fileName, String matrixName){
        // Putanja do fajla u koji želite da sačuvate podatke


        String filePath = Paths.get("src","resources","SavedResults",fileName).toString();//RELATIVNA PUTANJA

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            Future<BigInteger[][]> futureMatrix = matrice.get(matrixName);
            if (futureMatrix != null) {
                BigInteger[][] matrix = futureMatrix.get();
                int rows = matrix.length;
                int cols = matrix[0].length;

                // Prvi red u fajlu
                writer.write("matrix_name=" + matrixName + ", rows=" + rows + ", cols=" + cols);
                writer.newLine();

                // Ostali redovi u fajlu
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        if(!matrix[i][j].equals(BigInteger.ZERO)){
                            writer.write(i + "," + j + " = " + matrix[i][j]);
                            writer.newLine();
                        }
                    }
                }
                System.out.println("Saved successfully");
                this.fileName.put(matrixName,Paths.get(filePath).toAbsolutePath().toString());
            } else {
                System.err.println("Matrix with name " + matrixName + " not found.");
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }
}
