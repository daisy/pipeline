/*
 * Braille Utils (C) 2010-2011 Daisy Consortium 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.daisy.braille.api.paper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Provides a custom paper collection that lets a user
 * add and remove papers. The collection is stored as
 * a file in the users home directory.
 */
public class CustomPaperCollection {
	private static CustomPaperCollection collection;
	private final File f;
	private ArrayList<Paper> papers;
	private Integer index;
	private Date sync;
	
	private CustomPaperCollection() {
		this.papers = new ArrayList<>();
		this.index = 0;
		this.sync = null;
		File tmp = null;
		try {
			tmp = new File(System.getProperty("user.home"), CustomPaperCollection.class.getCanonicalName() + ".obj");
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

	public synchronized static CustomPaperCollection getInstance() {
		if (collection==null) {
			collection = new CustomPaperCollection();
		}
		return collection;
	}

	public synchronized Collection<Paper> list() {
		return papers;
	}

	public synchronized SheetPaper addNewSheetPaper(String name, String desc, Length width, Length height) throws IOException {
		return (SheetPaper)add(new SheetPaper(name, desc, nextIdentifier(), width, height));
	}

	public synchronized TractorPaper addNewTractorPaper(String name, String desc, Length across, Length along) throws IOException {
		return (TractorPaper)add(new TractorPaper(name, desc, nextIdentifier(), across,	along));
	}

	public synchronized RollPaper addNewRollPaper(String name, String desc, Length across) throws IOException  {
		return (RollPaper)add(new RollPaper(name, desc, nextIdentifier(), across));
	}

	private Paper add(Paper p) throws IOException {
		syncWithFile();
		papers.add(p);
		updateFile();
		return p;
	}

	public synchronized void remove(Paper p) throws IOException {
		syncWithFile();
		papers.remove(p);
		updateFile();
	}

	private String nextIdentifier() {
		index++;
		return CustomPaperCollection.class.getCanonicalName()+"_"+index;
	}

	@SuppressWarnings("unchecked")
	private void syncWithFile() throws IOException {
		if (f==null) {
			throw new FileNotFoundException();
		}
		if (sync == null || new Date(f.lastModified()).after(sync)) {
			if (f.exists()) {
				ObjectInputStream ois = null;
				//FileLock lock = null;
				try {
					FileInputStream is = new FileInputStream(f);
					//lock = is.getChannel().tryLock();
					ois = new ObjectInputStream(is);
					index = (Integer)ois.readObject();
					papers = (ArrayList<Paper>)ois.readObject();
					sync = new Date(f.lastModified());
				} catch (IOException e) {
					throw e;
				} catch (Exception e) {
					e.printStackTrace();
					if (!f.delete()) {
						f.deleteOnExit();
					}
					sync = null;
				} finally {
					if (ois!=null) {
						try {
							ois.close();
						} catch (IOException e) { }
					}
				}
			}
		}
	}

	private void updateFile() throws IOException {
		if (f==null) {
			throw new FileNotFoundException();
		}
		ObjectOutputStream oos = null;
		FileOutputStream os;
		try {
			os = new FileOutputStream(f);
			oos = new ObjectOutputStream(os);
			oos.writeObject(index);
			oos.writeObject(papers);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) { }
			}
			sync = new Date(f.lastModified());
		}
	}
}