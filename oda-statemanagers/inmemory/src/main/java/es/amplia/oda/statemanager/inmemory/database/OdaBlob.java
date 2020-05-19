package es.amplia.oda.statemanager.inmemory.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;

public class OdaBlob implements Blob {
	private static final Logger LOGGER = LoggerFactory.getLogger(OdaBlob.class);

	byte[] databytes;
	InputStream inputStream = null;
	OutputStream outputStream = null;

	public OdaBlob(byte[] databytes) {
		this.databytes = databytes;
	}

	@Override
	public long length() throws SQLException {
		return databytes.length;
	}

	@Override
	public byte[] getBytes(long pos, int length) throws SQLException {
		byte[] specificBytes = new byte[length];
		if(pos + length <= this.length()) {
			for (long i = 0; i < length; i++) {
				specificBytes[(int) i] = databytes[(int) (pos + i)];
			}
		}
		return specificBytes;
	}

	@Override
	public InputStream getBinaryStream() throws SQLException {
		return this.getBinaryStream(0, this.databytes.length);
	}

	@Override
	public long position(byte[] pattern, long start) throws SQLException {
		for (long i = start; pattern.length + i <= this.length(); i++) {
			if (pattern == getBytes(i, pattern.length)) {
				return i;
			}
		}
		return 0;
	}

	@Override
	public long position(Blob pattern, long start) throws SQLException {
		return position(pattern.getBytes(0, (int) pattern.length()), start);
	}

	@Override
	public int setBytes(long pos, byte[] bytes) throws SQLException {
		return setBytes(pos, bytes, 0, bytes.length);
	}

	@Override
	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
		int count = 0;
		for (int i = offset; i < len; i++) {
			this.databytes[(int)pos + count] = bytes[i];
		}
		return count;
	}

	@Override
	public OutputStream setBinaryStream(long pos) throws SQLException {
		if(outputStream == null) {
			try {
				if(pos < this.databytes.length) {
					outputStream = new ByteArrayOutputStream(this.databytes.length);
					outputStream.write(this.databytes, (int) pos, this.databytes.length - (int) pos);
					return outputStream;
				}
				throw new SQLException("Error setting the binary stream. Position is out of the array.");
			} catch (IOException e) {
				throw new SQLException("Error setting the binary stream. Exception was caught trying to create it");
			}
		}
		return this.outputStream;
	}

	@Override
	public void truncate(long len) throws SQLException {
		byte[] truncatedDatabytes = new byte[(int)len];
		if (len >= 0) System.arraycopy(this.databytes, 0, truncatedDatabytes, 0, (int)len);
		this.databytes = truncatedDatabytes;
	}

	@Override
	public void free() throws SQLException {
		try {
			this.databytes = null;
			this.inputStream.close();
			this.outputStream.close();
		} catch (IOException e) {
			throw new SQLException("Error trying to free the resources. Exception was caught trying to freeing it");
		}
	}

	@Override
	public InputStream getBinaryStream(long pos, long length) throws SQLException {
		if(inputStream == null) {
			this.inputStream = new ByteArrayInputStream(this.databytes, (int)pos, (int)length);
			return this.inputStream;
		}
		return this.inputStream;
	}
}
