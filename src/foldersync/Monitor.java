package foldersync;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private void copy(Path copyFile, Path copyToDir) throws IOException {
        Path copySrc = Paths.get(copyToDir.toString(), copyFile.getFileName().toString());
        for (String destDir : backupFoldersMap.get(copyToDir.toString())) {
            Path copyDest = Paths.get(destDir, copyFile.toString());
            System.out.println("[Copy]: Source " + copySrc.toString());
            System.out.println("[Copy]: Destination " + copyDest.toString());
            Files.copy(copySrc, copyDest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        }
    }

    private void syncEventCallback(WatchEvent event, WatchKey eventKey) throws IOException {
        Path modFile = (Path) event.context();
        Path modDir = watchKeys.get(eventKey);

        System.out.println("[Event]: " + event.kind() + " in directory " + modDir + " for file " + modFile);

        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
            copy(modFile, modDir);
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
            copy(modFile, modDir);
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
            for (String backupDir : backupFoldersMap.get(modDir.toString())) {
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

    private boolean validateDirs(String[] dirs) {
        if (dirs.length < 2) {
            System.out.println("At least two folder paths are required.");
            return false;
        }

        for (String dir : dirs) {
            File dirFil = new File(dir);
            if (!dirFil.exists()) {
                System.out.println(String.format("Path %s does not exists", dir));
                return false;
            }

            if (!dirFil.isDirectory()) {
                System.out.println(String.format("Path %s is not a directory", dir));
                return false;
            }
        }

        return true;
    }

    private void copyDirFiles(String fromDir, String toDir) throws IOException {
        File fromDirFile = new File(fromDir);
        for (File file : fromDirFile.listFiles()) {
            Path filePath = Paths.get(file.getPath()).toRealPath();

            Path copySrc = Paths.get(fromDirFile.toString(), filePath.getFileName().toString());
            Path copyDest = Paths.get(toDir, filePath.getFileName().toString());
            Files.copy(copySrc, copyDest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        }
    }

    public void registerDir(String[] dirs) throws IOException {
        if (!validateDirs(dirs))
            return;

        String master = dirs[0];
        Path path = Paths.get(master).toRealPath();
        WatchKey folderKey = path.register(folderWatcher, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.OVERFLOW);

        watchKeys.put(folderKey, path);
        List<String> backupList = new ArrayList<>();
        for (int i = 1; i < dirs.length; i++) {
            backupList.add(dirs[i]);
            copyDirFiles(master, dirs[i]);
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
