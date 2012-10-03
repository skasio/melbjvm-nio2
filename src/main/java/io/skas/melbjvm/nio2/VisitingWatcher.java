package io.skas.melbjvm.nio2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * VisitingWatcher is a watch service that watches a directory tree using the
 * SimpleFileVisitor to register directory trees created within the watched
 * directory.
 *
 * @author Szymon Szukalski [szymon.szukalski@gmail.com]
 */
public class VisitingWatcher {

    private static final Logger LOG = LoggerFactory.getLogger(VisitingWatcher.class);

    private WatchService watchService;
    private final Map<WatchKey, Path> watchedPaths = new HashMap<WatchKey, Path>();

    private void registerDirectory(Path directory) throws IOException {

        WatchKey key = directory.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);

        watchedPaths.put(key, directory);

    }

    private void registerTree(Path startingDirectory) throws IOException {

        Files.walkFileTree(startingDirectory, new SimpleFileVisitor<Path>() {
            /**
             * Invoked for a directory before entries in the directory are visited.
             * <p/>
             * <p> Unless overridden, this method returns {@link java.nio.file.FileVisitResult#CONTINUE
             * CONTINUE}.
             */
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                LOG.debug("registering: {}", dir);
                registerDirectory(dir);
                return FileVisitResult.CONTINUE;
            }
        });

    }

    public void watchDirectory(Path directory) throws IOException, InterruptedException {

        watchService = FileSystems.getDefault().newWatchService();
        registerTree(directory);

        while (true) {

            // retrieve and remove the next watch key (waits)
            final WatchKey key = watchService.take();

            // get list of events for the key
            for (WatchEvent<?> watchEvent : key.pollEvents()) {

                // get the event kind
                final WatchEvent.Kind<?> kind = watchEvent.kind();

                // get the filename for the event
                final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
                final Path filename = watchEventPath.context();

                LOG.debug("event: {} filename: {}", kind, filename);

                // handle OVERFLOW event
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                // handle CREATE event
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    final Path directoryPath = watchedPaths.get(key);
                    final Path child = directoryPath.resolve(filename);

                    if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                        registerTree(child);
                    }
                }
            }

            // reset the key
            boolean valid = key.reset();

            // remove the key from watched paths if the key is not valid
            // (if the directory was deleted, for example)
            if (!valid) {
                watchedPaths.remove(key);

                if (watchedPaths.isEmpty()) {
                    break;
                }
            }
        }

        watchService.close();
    }
}