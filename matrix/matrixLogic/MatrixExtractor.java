package matrixLogic;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

import Tasks.Task;
import Tasks.TaskQueue;
import Tasks.TaskType;
import main.Main;
import main.TaskCoordinator;


public class MatrixExtractor implements Runnable {
    private String filename;  //apsolutname for reading from file
    private int maxFileChunk;
    private int start;
    private int end;
    private int matrixRows;   // rows that matrix have this don't changes
    private int matrixCols;   // columns that matrix have this don't change
    private String matrixName;
    private ForkJoinPool threadPool;
    private MatrixBrain matrixBrain;
    private TaskCoordinator taskCoordinator;

    public MatrixExtractor(MatrixBrain matrixBrain, TaskCoordinator taskCoordinator) {
        this.maxFileChunk = Integer.parseInt(Main.maximumFileChunkSize);
        this.threadPool = new ForkJoinPool();
        this.matrixBrain = matrixBrain;
        this.taskCoordinator = taskCoordinator;
    }

    @Override
    public void run() {
        while(true){
            Task t = taskCoordinator.getNextMatrix(true);
            if(t.isPoisonPill()){
                break;
            }
            try {
                extract(t.getFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        shutDown();
    }

    public void extract(File file) throws IOException {
        // for each file this changes
        this.filename = file.getAbsolutePath();
        //here I got matrixName, start,end, columns and rows

        //check if file can be read
        if(matrixInfo(file)){
            //A new task has been created for matrix multiplication, multiplying the same matrices A*A
            Future<BigInteger[][]> result = threadPool.submit(new MyMatrixExtractor(start,end,filename,matrixRows,matrixCols,matrixName));
            System.out.println("Created matrix: " +matrixName);
            //printResult(result);
            //Complicated but dont have time change it
            Task task = new Task(TaskType.MULTIPLY,result,result,matrixName,matrixName);
            TaskQueue.getInstance().addTask(task,file);

            Map <String, String> fileNameAndPath = new HashMap<>();
            fileNameAndPath.put(matrixName,filename);
            matrixBrain.addMatrix(result,fileNameAndPath);
            //for testing just
        }
    }


    //Error:
    // 1)file cant be open
    // 2) First row don't start with matrixName
    // 3) Columns or Rows are 0  or matrixName is empty
    public boolean matrixInfo(File file) {
        int currentChar =0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null ) {
                if (line.startsWith("matrix_name") &&  currentChar == 0) {
                    String[] parts = line.split(", ");
                    if (parts.length >= 3 && parts[0] != null && parts[1] != null && parts[2] != null) {
                        matrixName = parts[0].split("=")[1];
                        if (matrixName != null) {
                            matrixRows = Integer.parseInt(parts[1].split("=")[1]);
                            matrixCols = Integer.parseInt(parts[2].split("=")[1]);
                        }else{
                            System.out.println("Error: Unable to read file: "+file.getName() );
                            return false;}
                    }else{
                        System.out.println("Error: Unable to read file: "+file.getName() );
                        return false;}

                    if(matrixName == null || matrixName.trim().isEmpty() || matrixCols == 0 || matrixRows == 0){
                        System.out.println("Error: Unable to read file: "+file.getName() );
                        return false;
                    }
                }else if(!line.startsWith("matrix_name") &&  currentChar == 0){
                    System.out.println("Error: Unable to read file: "+file.getName());
                    return false;
                }
                currentChar+= line.length();
            }
        } catch (IOException e) {
            System.out.println("Error: Unable to open the file: " +file.getName());
            return false;
        }
        start = 0;
        end = currentChar;
        return true;
    }

    public void shutDown(){
        threadPool.shutdown();
    }


    private class MyMatrixExtractor extends RecursiveTask{
        private String filename;  //apsolutname for reading from file
        private BigInteger[][] matrices;
        private int start;
        private int end;
        private int matrixRows;   // rows that matrix have this don't changes
        private int matrixCols;   // columns that matrix have this don't change
        private String matrixName;
        public MyMatrixExtractor(int start, int end, String filename, int matrixRows, int matrixCols, String matrixName) {
            this.start = start;
            this.end = end;
            this.filename = filename;
            this.matrixName = matrixName;
            this.matrixCols = matrixCols;
            this. matrixRows = matrixRows;
        }

        @Override
        protected BigInteger[][] compute() {
            try {
                if(end - start <= maxFileChunk || calculateMid()==-1 ){
                    readMatricesFromFile();
                }
                else{
                    int mid = calculateMid();
                    MyMatrixExtractor left = new MyMatrixExtractor(start,mid,filename,matrixRows,matrixCols,matrixName);
                    MyMatrixExtractor right = new MyMatrixExtractor(mid,end,filename,matrixRows,matrixCols,matrixName);
                    left.fork();

                    BigInteger[][] rightResults = right.compute();
                    BigInteger[][] leftResults = (BigInteger[][])left.join();

                    BigInteger[][] mergedMatrix = mergeMatrices(rightResults,leftResults);
                    matrices = mergedMatrix;
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return matrices;
        }

        public void readMatricesFromFile() throws IOException {
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line;
                BigInteger[][] currentMatrix = new BigInteger[matrixRows][matrixCols];
                int currentChar =0;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("matrix_name")) {
                        //Toto tu misi stat zbog toho ze ak ze prvi raz zinde aj razom obrobi celi fajl tak budu mi nulls secko
                        //zato ze sa nemerguje bez tohoto
                        for (int i = 0; i < matrixRows; i++) {
                            for (int j = 0; j < matrixCols; j++) {
                                currentMatrix[i][j] = BigInteger.ZERO;
                            }
                        }

                    } else if(currentChar >= start && currentChar < end){
                        String[] parts = line.split(" = ");
                        String[] indices = parts[0].split(",");
                        int row = Integer.parseInt(indices[0]);
                        int col = Integer.parseInt(indices[1]);
                        BigInteger value = new BigInteger(parts[1].trim());
                        currentMatrix[row][col] = value;
                    }
                    currentChar+= line.length();
                }
                matrices = currentMatrix;

            }
        }

        public int calculateMid() throws IOException {
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line;
                int mid = (start + (end - start) / 2);
                int currentChar = 0;

                while ((line = br.readLine()) != null) {
                    if(currentChar >= start && currentChar < end){
                        if(currentChar+line.length() >= mid){
                            break;
                        }
                    }
                    currentChar+= line.length();
                }
                //if there is more characters in one row than maxChunk read whole line don't try to find mid
                if(currentChar == end || currentChar == start){
                    return -1;
                }
                return currentChar;
            }
        }


        // Method to merge matrices
        public BigInteger[][] mergeMatrices(BigInteger[][] matrix1,BigInteger[][] matrix2) {
            BigInteger[][] mergedMatrix = new BigInteger[matrixRows][matrixCols];


            // Initialize mergedMatrix with zeros
            for (int i = 0; i < matrixRows; i++) {
                for (int j = 0; j < matrixCols; j++) {
                    mergedMatrix[i][j] = BigInteger.ZERO;
                }
            }

            // Merge matrix1 into the mergedMatrix
            mergeMatrix(mergedMatrix, matrix1);

            // Merge matrix2 into the mergedMatrix
            mergeMatrix(mergedMatrix, matrix2);

            return mergedMatrix;

        }



        private void mergeMatrix(BigInteger[][] mergedMatrix, BigInteger[][] matrix) {

            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    if (matrix[i][j] != null && matrix[i][j] != BigInteger.ZERO) {
                        mergedMatrix[i][j] = matrix[i][j];
                    }
                }
            }
        }
    }

    public static void printResult(Future<BigInteger[][]> result) {
        try {
            BigInteger[][] values = result.get(); // Get the result from the Future (blocking until it's available)

            // Loop through each row and column to print the values
            for (int i = 0; i < values.length; i++) {
                for (int j = 0; j < values[i].length; j++) {
                    System.out.println(values[i][j]);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(); // Handle any exceptions that might occur during retrieval of the result
        }
    }
}
