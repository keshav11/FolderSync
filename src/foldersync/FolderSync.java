package foldersync;

import java.io.IOException;
import java.nio.file.*;

public class FolderSync {


    public static void main(String[] args) throws IOException, InterruptedException {

        Monitor monitor = new Monitor();

        for(String dir: args)
            monitor.registerDir(dir);

        monitor.startMonitoring();

    }
}
