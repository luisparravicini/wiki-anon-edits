/*
 * Based on MediaWiki import/export processing tools
 * Copyright 2005 by Brion Vibber
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * $Id$
 */

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlDumpReader extends DefaultHandler {
	InputStream input;

	DumpWriter writer;

	private char[] buffer;

	private int len;

	boolean abortFlag;

	private String date;

	private Stats stats;

	/**
	 * Initialize a processor for a MediaWiki XML dump stream. Events are sent
	 * to a single DumpWriter output sink, but you can chain multiple output
	 * processors with a MultiWriter.
	 * 
	 * @param inputStream
	 *            Stream to read XML from.
	 * @param writer
	 *            Output sink to send processed events to.
	 * @throws IOException
	 */
	public XmlDumpReader(InputStream inputStream, DumpWriter writer)
			throws IOException {
		input = inputStream;
		this.writer = writer;
		buffer = new char[4096];
		len = 0;
		stats = new Stats();
	}

	/**
	 * Reads through the entire XML dump on the input stream, sending events to
	 * the DumpWriter as it goes. May throw exceptions on invalid input or due
	 * to problems with the output.
	 * 
	 * @throws IOException
	 */
	public void readDump() throws IOException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();

			parser.parse(input, this);
		} catch (ParserConfigurationException e) {
			throw (IOException) new IOException(e.getMessage()).initCause(e);
		} catch (SAXException e) {
			throw (IOException) new IOException(e.getMessage()).initCause(e);
		}
		writer.close();
	}

	/**
	 * Request that the dump processing be aborted. At the next element, an
	 * exception will be thrown to stop the XML parser.
	 * 
	 * @fixme Is setting a bool thread-safe? It should be atomic...
	 */
	public void abort() {
		abortFlag = true;
	}

	public void startElement(String uri, String localname, String qName,
			Attributes attributes) throws SAXException {
		// Clear the buffer for character data; we'll initialize it
		// if and when character data arrives -- at that point we
		// have a length.
		len = 0;
		stats.incCounters(qName);
		if (abortFlag)
			throw new SAXException("XmlDumpReader set abort flag.");
	}

	public void characters(char[] ch, int start, int length) {
		if (buffer.length < len + length) {
			int maxlen = buffer.length * 2;
			if (maxlen < len + length)
				maxlen = len + length;
			char[] tmp = new char[maxlen];
			System.arraycopy(buffer, 0, tmp, 0, len);
			buffer = tmp;
		}
		System.arraycopy(ch, start, buffer, len, length);
		len += length;
	}

	public void endElement(String uri, String localname, String qName)
			throws SAXException {
		try {
			if (qName == "timestamp")
				readTimestamp();
			else if (qName == "ip")
				readIp();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String bufferContents() {
		return len == 0 ? "" : new String(buffer, 0, len);
	}

	void readTimestamp() {
		date = bufferContents().substring(0, 10);
	}

	void readIp() throws IOException {
		writer.writeAnonymousEdit(bufferContents(), date);

		stats.next();

		date = null;
	}

	public static void main(String[] args) throws IOException {
		XmlDumpReader dumper = new XmlDumpReader(System.in, new OutDumpWriter());
		dumper.readDump();
	}
}
