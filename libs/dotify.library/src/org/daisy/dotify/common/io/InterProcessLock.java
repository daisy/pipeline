package org.daisy.dotify.common.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an inter-process locking signal that can be used to negotiate access across JVMs.
 * Whoever holds the lock has it until it's released by the owner. A lock should therefore be
 * released as soon as the lock is no longer needed. In most cases, the following pattern
 * should be used:
 *
 * <pre> {@code
 * InterProcessLock lock = ...;
 * lock.lock();
 * try {
 *   // do work
 * } finally {
 *   lock.unlock();
 * }}</pre>
 * <p>
 * Locks remaining when the JVM exits will be released automatically.
 *
 * @author Joel Håkansson
 */
public final class InterProcessLock {
    private static final Logger logger = Logger.getLogger(InterProcessLock.class.getCanonicalName());
    private static Set<InterProcessLock> activeLocks = new HashSet<InterProcessLock>();
    private final File file;
    private LockDetails details = null;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(InterProcessLock::unlockAll));
    }

    private static void unlockAll() {
        Set<InterProcessLock> theLocks;

        synchronized (InterProcessLock.class) {
            theLocks = activeLocks;
            activeLocks = null;
        }

        for (InterProcessLock lock : theLocks) {
            lock.unlock();
        }
    }

    private static class LockDetails {
        private final FileLock lock;
        private final FileChannel fileChannel;

        private LockDetails(FileLock lock, FileChannel fileChannel) {
            this.lock = lock;
            this.fileChannel = fileChannel;
        }

        static Optional<LockDetails> lock(File file) throws IOException {
            // Create file if it doesn't exist
            file.createNewFile();
            @SuppressWarnings("resource")
            FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
            FileLock lock = null;
            try {
                lock = channel.tryLock();
            } catch (OverlappingFileLockException e) {
            }
            if (lock == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Failed to acquire lock..");
                }
                channel.close();
                return Optional.empty();
            }
            return Optional.of(new LockDetails(lock, channel));
        }

        void release() throws IOException {
            if (lock != null) {
                lock.release();
            }
            fileChannel.close();
        }

    }

    /**
     * Creates a new lock with the specified id.
     *
     * @param id an identifier
     */
    public InterProcessLock(String id) {
        this(new File(System.getProperty("java.io.tmpdir"), "lock-" + Objects.requireNonNull(id) + ".tmp"));
    }

    /**
     * Creates a new lock using the specified file.
     *
     * @param file the file to use as a lock
     */
    public InterProcessLock(File file) {
        File path = file.getAbsoluteFile().getParentFile();
        if (!path.isDirectory()) {
            throw new IllegalArgumentException("Not an existing directory: " + path);
        }
        this.file = file;
    }

    /**
     * Acquire the lock.
     *
     * @return returns true if a lock was acquired, false otherwise
     * @throws LockException if an error occurs when acquiring the lock
     */
    public synchronized boolean lock() throws LockException {
        if (details != null) {
            return false;
        }
        Optional<LockDetails> ld;
        try {
            // attempt to acquire lock
            ld = LockDetails.lock(file);
        } catch (IOException e) {
            throw new LockException(e);
        }

        if (ld.isPresent()) {
            if (activeLocks != null) {
                activeLocks.add(this);
            }
            details = ld.get();
            return true;
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Failed to acquire lock..");
            }
            return false;
        }
    }

    /**
     * Releases a previously acquired lock.
     */
    public synchronized void unlock() {
        try {
            if (details != null) {
                details.release();
                details = null;
            }
            file.delete();
            if (activeLocks != null) {
                activeLocks.remove(this);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to release lock.", e);
        }
    }

}
