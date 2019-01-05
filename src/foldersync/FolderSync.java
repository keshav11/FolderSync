package foldersync;

import java.io.IOException;

public class FolderSync {

    public static void main(String[] args) throws IOException, InterruptedException {

        Monitor monitor = new Monitor();
        if(args.length < 2) {
            System.out.println("At least two folder paths are required.");
        }
        monitor.registerDir(args);
        monitor.startMonitoring();

    }
}
