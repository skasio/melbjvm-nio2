package io.skas.melbjvm.nio2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {

    public static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws IOException {

        final Path path = Paths.get("/tmp/doit");

        Watcher watcher = new Watcher();

        try {
            Nio2MetaDataLoggingUtils.logFileStoreAttributes(path);
            Nio2MetaDataLoggingUtils.logBasicFileAttributes(path);
            Nio2MetaDataLoggingUtils.logDosFileAttributes(path);
            Nio2MetaDataLoggingUtils.logPosixFileAttributes(path);
            Nio2MetaDataLoggingUtils.logFileOwnerAttributes(path);

            watcher.watchDirectory(path);

        } catch (IOException | InterruptedException ex) {
            LOG.error(ex.getMessage());
        }
    }

}
