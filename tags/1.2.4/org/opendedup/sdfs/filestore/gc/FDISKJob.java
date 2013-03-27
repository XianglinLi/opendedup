package org.opendedup.sdfs.filestore.gc;

import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.opendedup.sdfs.Main;
import org.opendedup.sdfs.notification.SDFSEvent;

import org.opendedup.logging.SDFSLogger;
import org.opendedup.mtools.FDisk;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class FDISKJob implements Job {
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		WriteLock l = GCMain.gclock.writeLock();
		l.lock();
			try {
				SDFSEvent evt = SDFSEvent.gcInfoEvent("Running GC on " + Main.volume.getName());
				new FDisk(evt);
			} catch (Exception e) {
				SDFSLogger.getLog().warn("unable to finish executing fdisk", e);
			} finally {
				l.unlock();
			}

	}

}
