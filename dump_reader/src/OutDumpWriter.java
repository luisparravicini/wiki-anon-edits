import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OutDumpWriter implements DumpWriter {
	private static final String DATA_DIR = "data";

	public void writeAnonymousEdit(String ip, String date) throws IOException {
		byte[] address = ntoh(ip);
		if (address == null)
			System.err.println(String.format("invalid address: (%s)", ip));
		else {
			File x = new File(DATA_DIR, date.replace('-', File.separatorChar));
			if (!x.exists())
				x.getParentFile().mkdirs();
			OutputStream out = new FileOutputStream(x, true);
			out.write(address);
			out.close();
		}
	}

	private byte[] ntoh(String ip) {
		if (ip == null || ip.length() > 15 || ip.length() < 7)
			return null;

		String[] parts = ip.split("\\.");
		if (parts.length != 4)
			return null;

		try {
			byte[] result = new byte[4];
			for (int i = 0; i < result.length; i++)
				result[i] = (byte) Integer.parseInt(parts[i]);
			return result;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public void close() {
	}
}
