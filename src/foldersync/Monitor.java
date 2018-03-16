package foldersync;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class Monitor {

    Map<WatchKey, Path> watchKeys = new HashMap<>();
    WatchService folderWatcher;

    public Monitor() throws IOException {
        folderWatcher = FileSystems.getDefault().newWatchService();
    }

    boolean copy(Path modFile,  Path modPath) throws IOException {
        for(Path dest: watchKeys.values()) {
            Path destPath = Paths.get(dest.toString(), modFile.toString());
            System.out.println("Source: " + modPath.toString());
            System.out.println("Destination: " + destPath.toString());
            if(modPath.compareTo(destPath) != 0)
                Files.copy(modPath, destPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        }

        return true;
    }

    boolean syncDirs(WatchEvent event, WatchKey eventKey) throws IOException {
        Path modFile = (Path) event.context();
        Path modDir = watchKeys.get(eventKey);
        Path modPath = Paths.get(modDir.toString(), modFile.toString());

        System.out.println(event.kind() + " in directory " + modDir);
        System.out.println(event.kind() + " for file " + modFile);
        if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
            copy(modFile, modPath);
        }
        else if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
            copy(modFile, modPath);
        }
        else if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
            for(Path dest: watchKeys.values()) {
                Path destPath = Paths.get(dest.toString(), modFile.toString());
                System.out.println("Deleting: " + destPath.toString());
                if(modPath.compareTo(destPath) != 0) {
                    try {
                        Files.delete(destPath);
                    } catch (NoSuchFileException ex) {
                        System.out.println(ex.toString());
                    }

                }
            }
        }

        return true;
    }

    void registerDir(String dir) throws IOException{
        Path path = Paths.get(dir).toRealPath();

        WatchKey folderKey = path.register(folderWatcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.OVERFLOW);

        watchKeys.put(folderKey, path);

    }

    void startMonitoring() throws InterruptedException, IOException {
        boolean complete = false;
        while (!complete) {
            WatchKey key = folderWatcher.take();
            for (WatchEvent event : key.pollEvents()) {
                this.syncDirs(event, key);
            }
            complete = !key.reset();
        }
    }
}
