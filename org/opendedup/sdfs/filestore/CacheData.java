package org.opendedup.sdfs.filestore;

import java.io.IOException;

/**
 * 
 * @author sam silverberg Chunk block meta data is as follows: [mark of deletion
 *         (1 byte)|hash lenth(2 bytes)|hash(32 bytes)|date added (8 bytes)|date
 *         last claimed (8 bytes)| number of times claimed (8 bytes)|chunk len
 *         (4 bytes)|chunk position (8 bytes)]
 * 
 */

public class CacheData {

	private final long cPos;
	private final byte[] chunk;
	
	
	public CacheData(long cPos,byte [] data) { 
		this.chunk = data;
		this.cPos = cPos;
	}

	public long getcPos() {
		return cPos;
	}
	
	public byte[] getData() throws IOException {
			return chunk;
	}
}
