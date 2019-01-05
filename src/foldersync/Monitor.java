package foldersync;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Monitor {

    private Map<WatchKey, Path> watchKeys;
    private Map<String, List<String>> backupFoldersMap;
    private WatchService folderWatcher;

    Monitor() throws IOException {
        watchKeys = new HashMap<>();
        backupFoldersMap = new HashMap<>();
        folderWatcher = FileSystems.getDefault().newWatchService();
    }

    private void copy(Path modFile, Path modDir) throws IOException {
        Path modPathSrc = Paths.get(modDir.toString(), modFile.toString());
        for(String destDir: backupFoldersMap.get(modDir.toString())) {
            Path modPathDest = Paths.get(destDir, modFile.toString());
            System.out.println("[Copy]: Source " + modPathSrc.toString());
            System.out.println("[Copy]: Destination " + modPathDest.toString());
            Files.copy(modPathSrc, modPathDest,
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.COPY_ATTRIBUTES);
        }
    }

    private void syncEventCallback(WatchEvent event, WatchKey eventKey) throws IOException {
        Path modFile = (Path) event.context();
        Path modDir = watchKeys.get(eventKey);

        System.out.println("[Event]: " + event.kind() + " in directory " + modDir + " for file " + modFile);

        if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
            copy(modFile, modDir);
        }
        else if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
            copy(modFile, modDir);
        }
        else if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
            for(String backupDir: backupFoldersMap.get(modDir.toString())) {
                Path toDelPath = Paths.get(backupDir, modFile.toString());
                System.out.println("[Delete]: " + toDelPath.toString());
                try {
                    Files.delete(toDelPath);
                } catch (NoSuchFileException ex) {
                    System.out.println(ex.toString());
                }
            }
        }
    }

    public void registerDir(String[] dirs) throws IOException{
        String master = dirs[0];
        Path path = Paths.get(master).toRealPath();
            WatchKey folderKey = path.register(folderWatcher,
                                StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_DELETE,
                                StandardWatchEventKinds.ENTRY_MODIFY,
                                StandardWatchEventKinds.OVERFLOW);
        
        watchKeys.put(folderKey, path);
        List<String> backupList = new ArrayList<>();
        for (int i = 1; i < dirs.length; i++) {
            backupList.add(dirs[i]);
        }
        
        backupFoldersMap.put(path.toString(), backupList);
    }

    public void startMonitoring() throws InterruptedException, IOException {
        boolean complete = false;
        while (!complete) {
            WatchKey key = folderWatcher.take();
            for (WatchEvent event : key.pollEvents()) {
                this.syncEventCallback(event, key);
            }

            complete = !key.reset();
        }
    }
}
