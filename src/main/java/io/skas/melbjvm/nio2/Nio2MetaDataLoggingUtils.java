package io.skas.melbjvm.nio2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.util.Set;

/**
 * @author Szymon Szukalski [szymon.szukalski@gmail.com]
 */
public class Nio2MetaDataLoggingUtils {

    public static final Logger LOG = LoggerFactory.getLogger(Nio2MetaDataLoggingUtils.class);

    public static final long BYTES_IN_MEGABYTE = 1048576L;

    public static final String BASIC_VIEW = "basic";
    public static final String OWNER_VIEW = "owner";
    public static final String DOS_VIEW = "dos";
    public static final String POSIX_VIEW = "posix";

    public static void logBasicFileAttributes(Path path) throws IOException {
        final FileStore fileStore = Files.getFileStore(path);

        if (fileStore.supportsFileAttributeView(BASIC_VIEW)) {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);

            LOG.info("Basic file attributes for path [{}]", path);
            LOG.info("  isDirectory?     {}", basicFileAttributes.isDirectory());
            LOG.info("  isOther?         {}", basicFileAttributes.isOther());
            LOG.info("  isRegularFile?   {}", basicFileAttributes.isRegularFile());
            LOG.info("  isSymbolicLink?  {}", basicFileAttributes.isSymbolicLink());
        } else {
            LOG.info("the file store in which [{}] resides doesn't support the [{}] file attribute view", path, BASIC_VIEW);
        }
    }

    public static void logDosFileAttributes(Path path) throws IOException {
        final FileStore fileStore = Files.getFileStore(path);

        if (fileStore.supportsFileAttributeView(DOS_VIEW)) {
            DosFileAttributes dosFileAttributes = Files.readAttributes(path, DosFileAttributes.class);

            LOG.info("Dos file attributes for path [{}]", path);

            LOG.info("  isArchive?   {}", dosFileAttributes.isArchive());
            LOG.info("  isHidden?    {}", dosFileAttributes.isHidden());
            LOG.info("  isReadOnly?  {}", dosFileAttributes.isReadOnly());
            LOG.info("  isSystem?    {}", dosFileAttributes.isSystem());
        } else {
            LOG.info("the file store in which [{}] resides doesn't support the [{}] file attribute view", path, DOS_VIEW);
        }
    }

    public static void logPosixFileAttributes(Path path) throws IOException {
        final FileStore fileStore = Files.getFileStore(path);

        if (fileStore.supportsFileAttributeView(POSIX_VIEW)) {
            PosixFileAttributes posixFileAttributes = Files.readAttributes(path, PosixFileAttributes.class);

            final Set<PosixFilePermission> permissions = posixFileAttributes.permissions();

            LOG.info("Posix file attributes for path [{}]", path);

            LOG.info("  owner?               {}", posixFileAttributes.owner());
            LOG.info("  group?               {}", posixFileAttributes.group());
            LOG.info("  owner permissions?   {}",
                    (permissions.contains(PosixFilePermission.OWNER_READ) ? "r" : "-") +
                            (permissions.contains(PosixFilePermission.OWNER_WRITE) ? "w" : "-") +
                            (permissions.contains(PosixFilePermission.OWNER_EXECUTE) ? "x" : "-"));
            LOG.info("  group permissions?   {}",
                    (permissions.contains(PosixFilePermission.GROUP_READ) ? "r" : "-") +
                            (permissions.contains(PosixFilePermission.GROUP_WRITE) ? "w" : "-") +
                            (permissions.contains(PosixFilePermission.GROUP_EXECUTE) ? "x" : "-"));
            LOG.info("  others permissions?  {}",
                    (permissions.contains(PosixFilePermission.OTHERS_READ) ? "r" : "-") +
                            (permissions.contains(PosixFilePermission.OTHERS_WRITE) ? "w" : "-") +
                            (permissions.contains(PosixFilePermission.OTHERS_EXECUTE) ? "x" : "-"));
        } else {
            LOG.info("the file store in which [{}] resides doesn't support the [{}] file attribute view", path, POSIX_VIEW);
        }
    }

    public static void logFileOwnerAttributes(Path path) throws IOException {
        final FileStore fileStore = Files.getFileStore(path);
        if (fileStore.supportsFileAttributeView(OWNER_VIEW)) {
            final FileOwnerAttributeView fileAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
            LOG.info("Owner file attributes for path [{}]", path);
            LOG.info("  principal:  {}", fileAttributeView.getOwner());

        } else {
            LOG.info("the file store in which [{}] resides doesn't support the [{}] file attribute view", path, OWNER_VIEW);
        }
    }

    public static void logFileStoreAttributes(Path path) throws IOException {
        final FileStore fileStore = Files.getFileStore(path);

        LOG.info("File store attributes for path [{}]", path);

        LOG.info("  name:               {}", fileStore.name());
        LOG.info("  type:               {}", fileStore.type());
        LOG.info("  total space:        {}", fileStore.getTotalSpace() / BYTES_IN_MEGABYTE);
        LOG.info("  total unallocated:  {}", fileStore.getUnallocatedSpace() / BYTES_IN_MEGABYTE);
        LOG.info("  total space:        {}", fileStore.getUsableSpace() / BYTES_IN_MEGABYTE);
    }

}
