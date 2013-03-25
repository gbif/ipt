package org.gbif.provider.util;

import java.io.IOException;

import org.apache.log4j.RollingFileAppender;


public class IptFileAppender extends RollingFileAppender {
	public static String LOGDIR = "";

	@Override
	public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize) throws IOException {
		// modify fileName if relative
		if (!fileName.startsWith("/")){
			fileName = LOGDIR + "/" + fileName;
		}
		super.setFile(fileName, append, bufferedIO, bufferSize);
	}

}
