package main;

import Tasks.Task;
import Tasks.TaskQueue;
import Tasks.TaskType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemExplorer implements Runnable{
    public List<String> folderPaths = new ArrayList<>();
    private Map<String, Long> lastModifiedMap = new HashMap<>();
    private boolean running = true;

    public SystemExplorer(String folderPaths) {
        this.folderPaths.add(folderPaths);
    }


    @Override
    public void run() {
        while (running) {
            exploreAllFolders();
            try {
                Thread.sleep(Integer.parseInt(Main.sysExplorerSleepTime)); // Spava 10 sec
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void exploreAllFolders(){
        for (String folder : folderPaths){
            exploreFolder(folder);
        }
    }

    private void exploreFolder(String folderPath) {
        File folder = new File(folderPath);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if(file.isDirectory()){
                        //System.out.println("Exploring Folder "+folderPath);
                        exploreFolder(file.getAbsolutePath());
                    } else if (file.getName().endsWith(".rix")) {
                        long lastModified = file.lastModified();
                        if (lastModified != lastModifiedMap.getOrDefault(file.getAbsolutePath(), 0L)) {
                            System.out.println("Found " +file.getName());
                            lastModifiedMap.put(file.getAbsolutePath(), lastModified);
                            //processFile(file);
                            addFileToTaskQueue(file);
                        }
                    }
                }
            }
        } else {
            System.out.println("Putanja nije direktorijum. " );
        }
    }

//    private void processFile(File file) {
//        try {
//            // Čitanje prve linije iz datoteke
//            List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
//            if (!lines.isEmpty()) {
//                System.out.println("Prva linija datoteke " + file.getName() + ": " + lines.get(0));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public List<String> getFolderPaths() {
        return folderPaths;
    }

    public void setFolderPaths(String folderPath) {
        this.folderPaths.add(folderPath);
    }


    public void addFileToTaskQueue(File file){
        Task task = new Task(TaskType.CREATE,file);
        TaskQueue.getInstance().addTask(task,file);
    }

    public void stop() {
        Task task = new Task();
        TaskQueue.getInstance().addTask(task,null);
        running = false;
    }
}
