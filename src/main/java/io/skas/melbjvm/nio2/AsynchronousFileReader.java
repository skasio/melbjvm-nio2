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
 * @author Szymon Szukalski [szymon.szukalski@gmail.com]
 */
public class AsynchronousFileReader implements CompletionHandler<Integer, ByteBuffer> {

    public static final Logger LOG = LoggerFactory.getLogger(AsynchronousFileReader.class);
    public static final int BYTES_IN_MEGABYTE = 1048576;


    private Long position;
    private Path path;
    private ByteBuffer buffer;
    private AsynchronousFileChannel asynchronousFileChannel;

    public AsynchronousFileReader(Path path) {
        this.position = 0L;
        this.path = path;
        this.buffer = ByteBuffer.allocate(BYTES_IN_MEGABYTE);

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

    private void closeChannel() {
        try {
            this.asynchronousFileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readChannel(long position) {
        asynchronousFileChannel.read(buffer, position, buffer, this);
    }

    @Override
    public void completed(Integer result, ByteBuffer buffer) {
        if (result < 0) {
            closeChannel();
            LOG.debug("read: {} megabytes", position / BYTES_IN_MEGABYTE);
        } else {
            position += result;
            if (buffer.hasRemaining()) {
                readChannel(position);
            } else {
                buffer.flip();
                // Do something with the content of the buffer
                buffer.clear();
                this.readChannel(position);
            }
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        LOG.error("read failed: {}", exc.getMessage());
        exc.printStackTrace();
    }
}
