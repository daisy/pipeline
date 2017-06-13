package org.daisy.pipeline.client.filestorage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.models.Argument;
import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.utils.XML;
import org.w3c.dom.Document;

public class JobStorage {

	private String id;
	private Job job;
	private File directory;
	private Map<String,File> contextFiles = new HashMap<String,File>();
	private boolean lazyLoaded = false;
	
	private static final Object lock = new Object();

	/**
	 * Create a JobStorage associated with the provided Job.
	 * 
	 * @param job The job
	 * @param jobStorage The job storage directory
	 * @param id The job Id
	 */
	public JobStorage(Job job, File jobStorage, String id) {
		job.setJobStorage(this);
		this.job = job;
		if (id == null) {
			this.id = job.getId();
			
		} else {
			this.id = id;
		}
		directory = new File(jobStorage, this.id);
	}
	
	/**
	 * Create a JobStorage associated with the provided Job, and copy the context from another job.
	 * 
	 * @param job The job
	 * @param jobStorage The job storage directory
	 * @param otherJobContext Copy context from this job storage
	 * @param id The job Id
	 */
	public JobStorage(Job job, File jobStorage, JobStorage otherJobContext, String id) {
		this(job, jobStorage, id);
		
		if (otherJobContext != null) {
			for (File f : otherJobContext.getContextDir().listFiles()) {
				addContextFile(f, null);
			}
		}
		save(false);
	}
	
	/**
	 * Load the job.
	 * 
	 * Will be triggered when the caller tries to read from a job.
	 */
	public void lazyLoad() {
		if (lazyLoaded) {
			return;
		}
		
		
		synchronized (lock) {
			
			if (directory != null && directory.exists()) {
				File jobFile = new File(directory, "job.xml");
				String jobString = null;
				Document jobDocument = null;
				if (jobFile.exists()) {
					try {
						byte[] encoded = Files.readAllBytes(Paths.get(jobFile.getPath()));
						jobString = new String(encoded, Charset.defaultCharset());
						jobDocument = XML.getXml(jobString);
	
					} catch (IOException e) {
						Pipeline2Logger.logger().error("Unable to load job.xml: "+jobFile.getAbsolutePath(), e);
					}
	
					if (jobDocument != null) {
						try {
							if (job == null) {
								job = new Job(jobDocument);
								
							} else {
								job.setJobXml(jobDocument);
							}
	
						} catch (Pipeline2Exception e) {
							Pipeline2Logger.logger().error("Failed to load job: "+jobFile.getAbsolutePath(), e);
						}
					}
				}
			}
			
		}

		lazyLoaded = true;
	}

	/**
	 * Save the job to the job storage.
	 *
	 * (i.e. stores the job XML)
	 * 
	 * By default, will move the files instead of copying them.
	 */
	public synchronized void save() {
		save(true);
	}
	
	/**
	 * Save the job to the job storage.
	 * 
	 * @param moveFiles if set to false, will make copies of the context files instead of moving them.
	 */
	public synchronized void save(boolean moveFiles) {
		lazyLoad();
		
		if (directory == null) {
			return;
		}
		
		if (!directory.exists()) {
			directory.mkdirs();
		}

		synchronized (lock) {

			File jobFile = new File(directory, "job.xml");
			Document jobDocument = job.toXml();

			try {
				String jobRequestString = XML.toString(jobDocument);
				Files.write(jobFile.toPath(), jobRequestString.getBytes());

			} catch (IOException e) {
				Pipeline2Logger.logger().error("Unable to store XML for job", e);
			}

		}

		for (Argument arg : job.getInputs()) {
			if ("anyFileURI".equals(arg.getType()) || "anyURI".equals(arg.getType())) {
				List<String> values = arg.getAsList();
				if (values != null) {
					for (String value : values) {
						getContextFile(value); // forces loading of Files into contextFiles map
					}
				}
			}
		}

		if (!contextFiles.isEmpty()) {
			File contextDir = new File(directory, "context");
			for (String contextPath : contextFiles.keySet()) {
				File file = contextFiles.get(contextPath);

				if (!file.exists()) {
					Pipeline2Logger.logger().error("File or directory does not exist and can not be added to context: '"+file.getAbsolutePath()+"'");
					continue;
				}

				File contextFile = new File(contextDir, contextPath);
				contextFiles.put(contextPath, contextFile);
				try {
					assert contextFile.getCanonicalPath().startsWith(contextDir.getCanonicalPath() + File.separator); // contextFile is inside contextDir

					if (!file.getCanonicalPath().equals(contextFile.getCanonicalPath())) {
						contextFile.toPath().getParent().toFile().mkdirs();
						if (moveFiles) {
							Files.move(file.toPath(), contextFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
							
						} else {
							Files.copy(file.toPath(), contextFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.COPY_ATTRIBUTES);
						}
					}

				} catch (IOException e) {
					Pipeline2Logger.logger().error("Unable to copy from '"+file.getAbsolutePath()+"' to '"+contextFile.getAbsolutePath(), e);
				}
			}
		}

	}

	/**
	 * List all job IDs that are stored in the given job storage folder.
	 * 
	 * @param jobStorage the directory where jobs are stored
	 * @return a list of all the job IDs in the job storage
	 */
	public static synchronized List<String> listJobs(File jobStorage) {
		List<String> jobs = new ArrayList<String>();
		if (jobStorage.isDirectory()) {
			for (File directory : jobStorage.listFiles()) {
				if (directory.isDirectory()) {
					String jobId = directory.getName();
					jobs.add(jobId);
				}
			}
		}
		Collections.sort(jobs);
		return jobs;
	}
	
	/**
	 * Load a job from the job storage folder.
	 * 
	 * @param storageId The jobs Id in the storage
	 * @param jobStorageDir The job storage directory
	 * @return The job
	 */
	public static synchronized Job loadJob(String storageId, File jobStorageDir) {
		Job job = new Job();
		new JobStorage(job, jobStorageDir, storageId);
		return job;
	}
	
	/**
	 * Add the file to the context.
	 * 
	 * @param file The file
	 * @param contextPath the path to the file in the context
	 */
	public synchronized void addContextFile(File file, String contextPath) {
		if (contextPath == null) {
			contextPath = file.getName();
		}
		
		if (file.isFile()) {
			contextFiles.put(contextPath, file);
			
		} else if (file.isDirectory()) {
			if (!contextPath.endsWith("/")) {
				contextPath += "/";
			}
			for (File f : file.listFiles()) {
				addContextFile(f, contextPath + f.getName());
			}
			
		} 
	}

	/**
	 * Remove the file from the context.
	 * 
	 * The file will also be removed from all attached arguments. 
	 * 
	 * @param contextPath the path to the file in the context
	 */
	public synchronized void removeContextFile(String contextPath) {
		contextFiles.remove(contextPath);
	}

	/**
	 * Get the file associated with the given context path.
	 * 
	 * @param file the file
	 * @return the context path to the file as a string
	 */
	public synchronized String getContextFilePath(File file) {
		for (String contextPath : contextFiles.keySet()) {
			if (contextFiles.get(contextPath).equals(file)) {
				return contextPath;
			}
		}
		return null;
	}

	/**
	 * Get the path in context associated with the File.
	 * 
	 * @param contextPath the path to the file in the context
	 * @return the file
	 */
	public synchronized File getContextFile(String contextPath) {
		if (!contextFiles.containsKey(contextPath)) {
			File contextFile = new File(getContextDir(), contextPath);
			if (contextFile.isFile()) {
				contextFiles.put(contextPath, contextFile);
				return contextFile;
			}
		}
		return contextFiles.get(contextPath);
	}
	
	/**
	 * Returns the root directory for the context files.
	 * 
	 * @return the root directory for the context files
	 */
	public synchronized File getContextDir() {
		return new File(directory, "context");
	}
	
	/**
	 * Bundles all context files up as a ZIP archive and returns it.
	 * 
	 * @return the zip file
	 */
	public synchronized File makeContextZip() {
		File zip;
		try {
			zip = Files.createTempFile("dp2client", ".zip").toFile();
			org.daisy.pipeline.client.utils.Files.zip(getContextDir(), zip);
			return zip;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Test whether the given path exists in the context.
	 * 
	 * The path must be relative and can refer to either a file or a directory. 
	 * 
	 * @param contextPath the path to the file in the context
	 * @return true if the path exists as either a file or directory in the context.
	 */
	
	public synchronized boolean existsInContext(String contextPath) {
		return contextFiles.containsKey(contextPath);
	}

	/**
	 * Test whether the given path exists as a file in the context.
	 * 
	 * @param contextPath the path to the file in the context
	 * @return true if the path exists as a file in the context.
	 */
	public synchronized boolean isFileInContext(String contextPath) {
		File file = getContextFile(contextPath);
		return file != null && file.isFile();
	}

	/**
	 * Test whether the given path exists as a directory in the context.
	 * 
	 * @param contextPath the path to the file in the context
	 * @return true if the path exists as a directory in the context.
	 */
	public synchronized boolean isDirectoryInContext(String contextPath) {
		if (contextPath == null) {
			return false;
		}
		String dirPath = contextPath + (contextPath.endsWith("/") ? "" : "/");
		for (String path : contextFiles.keySet()) {
			if (path.startsWith(dirPath)) {
				return true;
			}
		}
		return false;
	}
	
	/** Deletes the job including all its files from the job storage. */
	public synchronized void delete() {
		if (directory != null && directory.exists()) {
			try {
				deleteRecursively(directory);

			} catch (IOException e) {
				Pipeline2Logger.logger().error("Unable to delete job: "+directory.getAbsolutePath(), e);
			}
		}
	}

	private synchronized void deleteRecursively(File directory) throws IOException {
		if (directory.isDirectory()) {
			for (File file : directory.listFiles()) {
				deleteRecursively(file);
			}
		}
		Files.delete(directory.toPath());
	}
	
	public synchronized String getStorageId() {
		return id;
	}

}
