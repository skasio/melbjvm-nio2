package io.skas.melbjvm.nio2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author Szymon Szukalski [szymon.szukalski@portlandrisk.com]
 */
public class FileReader implements CompletionHandler<Integer, ByteBuffer> {

    public static final Logger LOG = LoggerFactory.getLogger(FileReader.class);

    private Long fileSize;
    private Path path;
    private ByteBuffer buffer;
    private AsynchronousFileChannel asynchronousFileChannel;

    public FileReader(Path path) {
        this.fileSize = 0L;
        this.path = path;
        this.buffer = ByteBuffer.allocate(1048576);
        LOG.info("reading {}...", path);

        this.openChannel();
        this.readChannel(0);
    }

    private void openChannel() {
        try {
            this.asynchronousFileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readChannel(long position) {
        asynchronousFileChannel.read(buffer, position, buffer, this);
    }

    private void closeChannel() {
        try {
            this.asynchronousFileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        if (result != -1) {
            this.fileSize += result;
            attachment.flip();
            attachment.clear();
            this.readChannel(this.fileSize);
        } else {

            LOG.info("... read in: {} bytes from {}", fileSize, path);
            attachment.flip();
            attachment.clear();
            this.closeChannel();
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        LOG.error("read failed: {}", exc.getMessage());
        exc.printStackTrace();
    }
}
