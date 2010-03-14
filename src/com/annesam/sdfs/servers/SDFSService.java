package com.annesam.sdfs.servers;

import java.util.logging.Logger;

import com.annesam.sdfs.Config;
import com.annesam.sdfs.Main;
import com.annesam.sdfs.filestore.DedupFileStore;
import com.annesam.sdfs.filestore.FileChunkStore;
import com.annesam.sdfs.filestore.MemoryHashStore;
import com.annesam.sdfs.filestore.MetaFileStore;

public class SDFSService {
	String configFile;
	String routingFile;
	private static Logger log = Logger.getLogger("sdfs");

	public SDFSService(String configFile, String routingFile) {

		this.configFile = configFile;
		this.routingFile = routingFile;
		log.info("reading config file = " + this.configFile);
		log.info("reading routing file = " + this.routingFile);
	}

	public void start() throws Exception {
		Config.parseSDFSConfigFile(this.configFile);
		Config.parserRoutingFile(this.routingFile);
	}

	public void stop() {
		System.out.println("Shutting Down SDFS");
		System.out.println("Flushing and Closing Write Caches");
		DedupFileStore.close();
		System.out.println("Write Caches Flushed and Closed");
		System.out.println("Committing open Files");
		MetaFileStore.close();
		System.out.println("Open File Committed");
		System.out.println("Writing Config File");
		try {
			Config.writeSDFSConfigFile(configFile);
		} catch (Exception e) {
			
		}
		if (Main.chunkStoreLocal) {
			System.out
					.println("Shutting down ChunkStore");
			FileChunkStore.closeAll();
			System.out
					.println("Shutting down HashStore");
			MemoryHashStore.closeAll();
		}
		System.out.println("SDFS is Shut Down");
	}

}
