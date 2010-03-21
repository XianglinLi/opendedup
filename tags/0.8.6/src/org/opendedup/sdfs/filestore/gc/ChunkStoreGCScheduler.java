package org.opendedup.sdfs.filestore.gc;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;

public class ChunkStoreGCScheduler {

	private static Logger log = Logger.getLogger("sdfs");

	Scheduler sched = null;

	public ChunkStoreGCScheduler() {
		try {
			log.info("Scheduling Garbage Collection Jobs");
			SchedulerFactory schedFact = new StdSchedulerFactory();
			sched = schedFact.getScheduler();
			sched.start();
			JobDetail ccjobDetail = new JobDetail("claimChunks", null, ChunkClaimJob.class);
			Trigger cctrigger = TriggerUtils.makeDailyTrigger(22, 00); // fire every hour
			cctrigger.setStartTime(TriggerUtils.getEvenMinuteDate(new Date())); 
			cctrigger.setName("claimChunksTrigger");
			sched.scheduleJob(ccjobDetail, cctrigger);
			
			JobDetail rcjobDetail = new JobDetail("removeChunks", null, RemoveChunksJob.class);
			Trigger rctrigger = TriggerUtils.makeDailyTrigger(23, 00); // fire every two minutes
			rctrigger.setStartTime(TriggerUtils.getEvenMinuteDate(new Date())); 
			rctrigger.setName("removeChunksTrigger");
			
			sched.scheduleJob(rcjobDetail, rctrigger);
			log.info("Garbage Collection Jobs Scheduled");
		} catch (Exception e) {
			log.log(Level.SEVERE, "Unable to schedule Garbage Collection", e);
		}
	}

	public void stopSchedules() {
		try {
			sched.unscheduleJob("removeChunks", "removeChunksTrigger");
			sched.unscheduleJob("claimChunks", "claimChunksTrigger");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

}
