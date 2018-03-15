package foldersync;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.*;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        WatchService folderWatcher = FileSystems.getDefault().newWatchService();

        Path path = Paths.get("./test/");

        path.register(folderWatcher, StandardWatchEventKinds.ENTRY_CREATE);
        path.register(folderWatcher, StandardWatchEventKinds.ENTRY_DELETE);
        path.register(folderWatcher, StandardWatchEventKinds.ENTRY_MODIFY);

        boolean complete = false;
        while (!complete) {

            WatchKey key = folderWatcher.take();
            for (WatchEvent event : key.pollEvents()) {
                System.out.println(event.kind());
            }
            complete = !key.reset();
        }
    }
}
