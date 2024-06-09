package main;

import Tasks.TaskQueue;
import matrixLogic.MatrixBrain;
import matrixLogic.MatrixExtractor;
import matrixLogic.MatrixMultiplier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Scanner;


public class Main {
    public static String sysExplorerSleepTime;
    public static String maximumFileChunkSize;
    public static String maximumRowsSize;
    public static MatrixBrain matrixBrain;




    public static void main(String[] args) {
        // instanciranje propertija
        appProperties();
        //kreiranje systemExplorera kao novi thread
        String pathToInitialFolder = Paths.get("src","resources","initialFolder").toString();//RELATIVNA PUTANJA

        //thread za systemEplorer
        SystemExplorer systemExplorer = new SystemExplorer(pathToInitialFolder);
        Thread systemThread = new Thread(systemExplorer);

        //need this to ha reference to MatrixBrain
        matrixBrain = new MatrixBrain();

        //Thread za TaskCoordinator
        TaskCoordinator taskCoordinator = new TaskCoordinator();
        Thread cordinatorThread = new Thread(taskCoordinator);

        //Thread za Matrix Extractor
        MatrixExtractor matrixExtractor = new MatrixExtractor(matrixBrain,taskCoordinator);
        Thread extractorThread = new Thread(matrixExtractor);

        //Thread za Matrix Multiplier
        MatrixMultiplier matrixMultiplier = new MatrixMultiplier(matrixBrain,taskCoordinator);
        Thread multiplierThread = new Thread(matrixMultiplier);

        //starting threads
        systemThread.start();
        cordinatorThread.start();
        extractorThread.start();
        multiplierThread.start();

        cliInterakcija(systemExplorer);

    }

    public static void cliInterakcija(SystemExplorer systemExplorer){
        // Kreiranje objekta za unos sa tastature
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine().trim();
            // Provera unete komande
            if (command.equalsIgnoreCase("stop")) {
                // Zatvaranje skenera i izlaz iz petlje
                System.out.println("Stopping ...");
                scanner.close();
                systemExplorer.stop();
                break;
            }//THIS IS FOR INFO WHAT IS THERE
            else if (command.toLowerCase().startsWith("info")) {
                String[] commandParts = command.split("\\s+");
                if (commandParts.length == 1) {
                    System.out.println("Nevalidna komanda info. primer koristenja: info -all");
                } else {
                    String firstPart = commandParts[0]; // Should be "info"
                    String secondPart = commandParts[1]; // Can be matrix_name, -all, -dec, -asc, -s N, or -e N


                    // Checking possible options for the second part of the command
//                    if (secondPart.equalsIgnoreCase("-all") || secondPart.equalsIgnoreCase("-dec") ||
//                            secondPart.equalsIgnoreCase("-asc") || secondPart.matches("-s\\s\\d+") ||
//                            secondPart.matches("-e\\s\\d+") || secondPart.matches("[a-zA-Z0-9_]+")) {
                    if(secondPart.equalsIgnoreCase("-all")){
                        // Processing the command
                        matrixBrain.printMatrices();
                        // Here you can call the method that handles information based on command parts (firstPart and secondPart)
                    }
                    else if(secondPart.equalsIgnoreCase("-dec")){
                        matrixBrain.sortDecreasing();
                    }else if(secondPart.equalsIgnoreCase("-asc")){
                        matrixBrain.sortAsceding();
                    }else if (secondPart.matches("-s")){
                        try{
                            String thirdPart = commandParts[2];
                            matrixBrain.printFirstNMatrices(Integer.parseInt(thirdPart));
                        }catch (Exception e){
                            System.out.println("invalid command tipe likw: info -s 10");
                        }

                    }else if(secondPart.matches("-e")){
                        try{
                            String thirdPart = commandParts[2];
                            matrixBrain.printLastNMatrices(Integer.parseInt(thirdPart));
                        }catch (Exception e){
                            System.out.println("invalid command tipe likw: info -e 10");
                        }
                    }
                    else if(!secondPart.isEmpty()){
                        matrixBrain.checkIfFutureIsFinished(secondPart);
                    }
                    else {
                        System.out.println("Nepostojeci parametri info. Pogledaj -h za pomoc");
                    }
                }
            }else if (command.toLowerCase().startsWith("multiply")){
                String[] parts = command.split(" ");
                if (parts.length < 4) {
                    System.out.println("Nevažeća komanda");
                }else if(parts.length > 4 && parts[4].equals("-async")){
                    matrixBrain.multiplyASync(parts[1],parts[3]);
                } else if (parts.length == 4){
                    matrixBrain.multiplySync(parts[1],parts[3]);
                }else{
                    System.out.println("Try like this: multiply mat1 , mat2 -async");
                }
            }else if(command.toLowerCase().startsWith("save")){
                String[] parts = command.split(" ");
                if(parts.length == 5){
                    if(parts[1].equals("-name") && parts[3].equals("-file")){
                        matrixBrain.saveToFile(parts[4],parts[2]);
                    }else{
                        System.out.println("Incorrect command: save -name mat_name -file file_name");
                    }

                }else{
                    System.out.println("Incorrect command: save -name mat_name -file file_name");
                }
            }else if(command.toLowerCase().startsWith("dir")){
                String[] parts = command.split(" ");
                if(parts.length>1){
                    Path folderPath = Paths.get("src","resources",parts[1]);//RELATIVNA PUTANJA
                    if(Files.isDirectory(folderPath)){
                        systemExplorer.setFolderPaths(folderPath.toString());
                    }else{
                        System.out.println("dir not exist");
                    }
                }else {
                    System.out.println("Not valid command need to be like: dir dir_name");
                }

            }else if(command.toLowerCase().startsWith("clear")){
                String[] parts = command.split(" ");
                if(parts.length != 2){
                    System.out.println("Not valid command: clear mat_name / clear filename");
                }else if (parts[1].contains(".rix")){
                    matrixBrain.deleteMatName(parts[1]);
                }else{
                    matrixBrain.deleteAllMatrices(parts[1]);
                }
            }
            else {
                System.out.println("Nepoznata komanda. Molimo pokušajte ponovo.");
            }
        }

    }
    public static void appProperties(){
        Properties prop = new Properties();
        InputStream input = null;
        try {
            String path = Paths.get("src", "app.properties").toString();
            input = new FileInputStream(path);
            System.out.println("Adding initial dir  "+ path);
            prop.load(input);

            // Sada možete dobiti vrednosti parametara
            sysExplorerSleepTime = prop.getProperty("sys_explorer_sleep_time");
            maximumFileChunkSize = prop.getProperty("maximum_file_chink_size");
            maximumRowsSize = prop.getProperty("maximum_rows_size");

            // Koristite vrednosti parametara u vašem kodu
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}