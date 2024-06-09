package matrixLogic;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

import Tasks.Task;
import main.Main;
import main.TaskCoordinator;

public class MatrixMultiplier implements Runnable {
    private Future<BigInteger[][]> matrix1;
    private Future<BigInteger[][]> matrix2;
    private int start;
    private int end;
    private MatrixBrain matrixBrain;

    private final int maxRows;
    private ForkJoinPool threadPool;
    private TaskCoordinator taskCoordinator;


    public MatrixMultiplier(MatrixBrain matrixBrain, TaskCoordinator taskCoordinator){
        this.maxRows = Integer.parseInt(Main.maximumRowsSize);
        threadPool = new ForkJoinPool();
        this.matrixBrain = matrixBrain;
        this.taskCoordinator = taskCoordinator;
    }

    @Override
    public void run() {
        while(true){
            Task t = taskCoordinator.getNextMatrix(false);
            if(t.isPoisonPill()){
                break;
            }
            try {
                multiply(t);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        shutDown();
    }
    public void multiply(Task t) throws ExecutionException, InterruptedException {

        BigInteger[][] allRows = t.getMatrix1().get();
        this.end = allRows.length;
        matrix1 = t.getMatrix1();
        matrix2 = t.getMatrix2();

        if(canMultiply(matrix1.get(),matrix2.get())){
            String matrixName = (t.getName1()+t.getName2());
            Future<BigInteger[][]> result = threadPool.submit(new MyMatrixMultiplier(start,end,matrix1.get(),matrix2.get(),matrixName));
            //printMatrix(result.get().entrySet().iterator().next().getValue());

            Map <String, String> fileNameAndPath = new HashMap<>();
            fileNameAndPath.put(matrixName,"in memory");
            matrixBrain.addMatrix(result,fileNameAndPath);
            System.out.println("Multiply "+t.getName1() +" x "+t.getName2());
        }else{
            System.out.println("Error :  Matrix cant be multiply: " +t.getName1() +" x "+ t.getName2());
            matrixBrain.setSync(false);
        }
    }

    public boolean canMultiply(BigInteger[][] matrix1, BigInteger[][] matrix2) {
        // Check if matrix1 and matrix2 are not null
        if (matrix1 == null || matrix2 == null) {
            return false;
        }

        // Check if the number of columns in matrix1 is equal to the number of rows in matrix2
        int colsMatrix1 = matrix1[0].length;
        int rowsMatrix2 = matrix2.length;

        return colsMatrix1 == rowsMatrix2;
    }
    public void shutDown(){
        threadPool.shutdown();
    }


    private class MyMatrixMultiplier extends RecursiveTask{
        private BigInteger[][] matrix1;
        private BigInteger[][] matrix2;
        private int start;
        private int end;
        private String matrxName;
        public MyMatrixMultiplier(int start, int end,BigInteger[][] matrix1,BigInteger[][] matrix2,String matrxName) {
            this.start = start;
            this.end = end;
            this.matrix1 = matrix1;
            this.matrix2 = matrix2;
            this.matrxName = matrxName;
        }
        @Override
        protected BigInteger[][] compute() {
            BigInteger[][] result = null;

            if((end - start)<= maxRows || end <= start){
                result = multiply(matrix1, matrix2);

            }else{
                //zbog try catch....
                int mid = findMid();

                MyMatrixMultiplier left = new MyMatrixMultiplier(start,mid,matrix1,matrix2,matrxName);
                MyMatrixMultiplier right = new MyMatrixMultiplier(mid,end,matrix1,matrix2,matrxName);

                left.fork();
                BigInteger[][] rightResults =  right.compute();
                BigInteger[][] leftResults = (BigInteger[][]) left.join();

                result = mergeMatrix(rightResults,leftResults);

            }


            return result;
        }

//        private BigInteger[][] extractMatrix(Map<String, BigInteger[][]> resultMap) {
//            if (resultMap.isEmpty()) {
//                System.out.println("Map is empty.");
//                return null;
//            }
//            // Dobivanje prvog elementa iz mape
//            return resultMap.entrySet().iterator().next().getValue();
//        }



        private BigInteger[][] multiply(BigInteger[][] matrix1, BigInteger[][] matrix2) {
            int m1Rows = matrix1.length;
            int m1Cols = matrix1[0].length;
            int m2Cols = matrix2[0].length;

//        System.out.println(m1Cols);
//        System.out.println(matrix2.length);

            if (m1Cols != matrix2.length) {
                System.out.println("Matrices cannot be multiplied: Invalid dimensions.");
                return null;
            }

            BigInteger[][] result = new BigInteger[m1Rows][m2Cols];
            // Fill result matrix with zeros
            for (int i = 0; i < m1Rows; i++) {
                for (int j = 0; j < m2Cols; j++) {
                    result[i][j] = BigInteger.ZERO;
                }
            }

            for (int i = start; i < end; i++) {
                for (int j = 0; j < m2Cols; j++) {
//                result[i][j] = BigInteger.ZERO;
                    for (int k = 0; k < m1Cols; k++) {
                        result[i][j] = result[i][j].add(matrix1[i][k].multiply(matrix2[k][j]));
                    }
                }
            }
//        printMatrix(result);
            return result;
        }

        private int findMid() {

            int numRows =  (end - start);
            // Check if numRows is even or odd
            int midRow;
            if (numRows % 2 == 0) {
                // If even, choose the lower middle row
                midRow = start + numRows / 2 - 1;
            } else {
                // If odd, directly get the middle row
                midRow = start +numRows / 2;
            }
//        System.out.println(midRow);
            return midRow;

            // Print the middle row
//        System.out.println("Middle row:");
//        for (int j = 0; j < matrix[midRow].length; j++) {
//            System.out.print(matrix[midRow][j] + " ");
//        }
//        System.out.println();
        }

        private BigInteger[][] mergeMatrix (BigInteger[][] matrix1, BigInteger[][] matrix2){
            if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
                throw new IllegalArgumentException("Matrices must have the same dimensions.");
            }

            int rows = matrix1.length;
            int cols = matrix1[0].length;

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (!matrix2[i][j].equals(BigInteger.ZERO)) {
                        matrix1[i][j] = matrix2[i][j];
                    }
                }
            }

            return matrix1;
        }


    }

    private void printMatrix(BigInteger[][] matrix) {
        for (BigInteger[] row : matrix) {
            for (BigInteger element : row) {
                System.out.print(element + " ");
            }
            System.out.println();
        }
    }

}
