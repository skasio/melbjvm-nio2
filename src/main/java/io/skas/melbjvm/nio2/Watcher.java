package io.skas.melbjvm.nio2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

/**
 * @author Szymon Szukalski [szymon.szukalski@gmail.com]
 */
public class Watcher {

    private static final Logger LOG = LoggerFactory.getLogger(Watcher.class);

    public void watchDirectory(Path watchedPath) throws IOException, InterruptedException {

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

            final WatchKey key = watchedPath.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);

            while (true) {

                watchService.take();

                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    final WatchEvent.Kind<?> kind = watchEvent.kind();

                    // handle OVERFLOW event
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
                    Path newFile = watchedPath.resolve(watchEventPath.context());

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        new AsynchronousFileReader(newFile);
                    }
                }

                // reset the key
                boolean valid = key.reset();

                // exit loop if the key is not valid (if the directory was deleted, for example)
                if (!valid) {
                    break;
                }
            }
        }
    }
}
