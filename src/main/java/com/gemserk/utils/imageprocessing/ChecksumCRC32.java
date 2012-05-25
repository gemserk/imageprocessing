package com.gemserk.utils.imageprocessing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class ChecksumCRC32 {

	public static long checksum(File file) throws IOException {

		CheckedInputStream cis = null;
		try {
			// Computer CRC32 checksum
			cis = new CheckedInputStream(new FileInputStream(file), new CRC32());

			byte[] buf = new byte[1024];
			while (cis.read(buf) >= 0) {
			}

			long checksum = cis.getChecksum().getValue();

			return checksum;

		} finally {
			if (cis != null)
				cis.close();
		}
	}
}
