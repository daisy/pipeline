package org.daisy.dotify.api.paper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Provides a custom paper collection that lets a user
 * add and remove papers. The collection is stored as
 * a file in the users home directory.
 */
enum UserPapersCollection {
    INSTANCE;
    private static final String ID_PREFIX = "org.daisy.dotify.api.paper.UserPapersCollection";
    private final File f;
    private Map<String, Paper> papers;
    private Integer index;
    private Date sync;

    private static final Logger logger = Logger.getLogger(UserPapersCollection.class.getCanonicalName());

    private UserPapersCollection() {
        this.papers = new LinkedHashMap<>();
        this.index = 0;
        this.sync = null;
        File tmp = null;
        try {
            tmp = new File(System.getProperty("user.home"), ID_PREFIX + ".obj");
        } catch (Exception e) {
            // silently fail here
        }
        this.f = tmp;
        try {
            syncWithFile();
        } catch (IOException e) {
            // silently fail here
        }
    }

    /**
     * Gets the instance.
     *
     * @return returns the instance
     */
    static synchronized UserPapersCollection getInstance() {
        return INSTANCE;
    }

    synchronized Paper get(String identifier) {
        return papers.get(identifier);
    }

    /**
     * Lists the papers in the collection.
     *
     * @return returns a collection of papers
     */
    synchronized Collection<Paper> list() {
        return papers.values();
    }

    synchronized Map<String, Paper> getMap() {
        return Collections.unmodifiableMap(papers);
    }

    /**
     * Adds a new sheet paper to the collection.
     *
     * @param name   the name
     * @param desc   the description
     * @param width  the width
     * @param height the height
     * @return returns the new sheet paper
     * @throws IOException if an I/O error occurs
     */
    synchronized SheetPaper addNewSheetPaper(String name, String desc, Length width, Length height) throws IOException {
        return (SheetPaper) add(new SheetPaper(name, desc, nextIdentifier(), width, height));
    }

    /**
     * Adds a new tractor paper to the collection.
     *
     * @param name   the name
     * @param desc   the description
     * @param across the length across the feed
     * @param along  the length along the feed
     * @return returns the new tractor paper
     * @throws IOException if an I/O error occurs
     */
    synchronized TractorPaper addNewTractorPaper(
        String name,
        String desc,
        Length across,
        Length along
    ) throws IOException {
        return (TractorPaper) add(new TractorPaper(name, desc, nextIdentifier(), across, along));
    }

    /**
     * Adds a new roll paper to the collection.
     *
     * @param name   the name
     * @param desc   the description
     * @param across the length across the feed
     * @return returns the new roll paper
     * @throws IOException if an I/O error occurs
     */
    synchronized RollPaper addNewRollPaper(String name, String desc, Length across) throws IOException {
        return (RollPaper) add(new RollPaper(name, desc, nextIdentifier(), across));
    }

    private Paper add(Paper p) throws IOException {
        syncWithFile();
        papers.put(p.getIdentifier(), p);
        updateFile();
        return p;
    }

    /**
     * Removes the specified paper from the collection.
     *
     * @param p the paper to remove
     * @throws IOException if an I/O error occurs
     */
    synchronized void remove(Paper p) throws IOException {
        syncWithFile();
        papers.remove(p.getIdentifier());
        updateFile();
    }

    private String nextIdentifier() {
        index++;
        return ID_PREFIX + "_" + index;
    }

    private void syncWithFile() throws IOException {
        if (f == null) {
            throw new FileNotFoundException();
        }
        if ((sync == null || new Date(f.lastModified()).after(sync)) && f.exists()) {
            ObjectInputStream ois = null;
            //FileLock lock = null;
            try {
                FileInputStream is = new FileInputStream(f);
                //lock = is.getChannel().tryLock();
                ois = new ObjectInputStream(is);
                index = (Integer) ois.readObject();
                papers = new LinkedHashMap<>(((ArrayList<Paper>) ois.readObject()).stream()
                        .collect(Collectors.toMap(p -> p.getIdentifier(), p -> p, (p1, p2) -> p2)));
                sync = new Date(f.lastModified());
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                logger.throwing(UserPapersCollection.class.getCanonicalName(), "syncWithFile", e);
                if (!f.delete()) {
                    f.deleteOnExit();
                }
                sync = null;
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    private void updateFile() throws IOException {
        if (f == null) {
            throw new FileNotFoundException();
        }
        ObjectOutputStream oos = null;
        FileOutputStream os;
        try {
            os = new FileOutputStream(f);
            oos = new ObjectOutputStream(os);
            oos.writeObject(index);
            oos.writeObject(new ArrayList<>(papers.values()));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            logger.throwing(UserPapersCollection.class.getCanonicalName(), "updateFile", e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                }
            }
            sync = new Date(f.lastModified());
        }
    }
}
