import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public class TrecDocIterator implements Iterator<Document> {

	protected BufferedReader rdr;
	protected boolean at_eof = false;

	public TrecDocIterator(File file) throws FileNotFoundException {
		rdr = new BufferedReader(new FileReader(file));
		System.out.println("Reading " + file.toString());
	}

	@Override
	public boolean hasNext() {
		return !at_eof;
	}

	@Override
	public Document next() {
		Document doc = new Document();
		StringBuffer sb = new StringBuffer();
		StringBuffer rtsf = new StringBuffer();
		try {
			String line;
			Pattern cc_tag = Pattern
					.compile("<chief_complaint>\\s*(\\S+)\\s*<");
			Pattern chks_tag = Pattern.compile("<checksum>\\s*(\\S+)\\s*<");
			Pattern rt_tag = Pattern.compile("<report_text>(.*)");
			boolean in_doc = false;
			boolean rt_flag = false;
			// System.out.println("outside while");
			while (true) {
				// System.out.println("Inside while\n");
				line = rdr.readLine();
				if (line == null) {
					at_eof = true;
					break;
				}
				if (!in_doc) {
					if (line.startsWith("<checksum>")
							|| line.startsWith("<chief_complaint>")
							|| line.startsWith("<report_text>")) {
						System.out.println("Initial Match is" + line);
						in_doc = true;
					} else
						continue;
				}
				if (line.contains("</checksum>")
						|| line.contains("</chief_complaint>")
						|| line.startsWith("</report_text>")) {
					in_doc = false;
					sb.append(line);
					break;
				}

				Matcher m = cc_tag.matcher(line);
				if (m.find()) {
					String cc = m.group(1);
					System.out.println("Chief Complaint content is" + cc);
					doc.add(new StringField("chief_complaint", cc,
							Field.Store.YES));
				}

				Matcher m1 = chks_tag.matcher(line);
				if (m1.find()) {
					String chks = m1.group(1);
					System.out.println("Checksum content is" + chks);
					doc.add(new StringField("checksum", chks, Field.Store.YES));
				}

				System.out.println("in_doc " + in_doc);
				if (in_doc) {
					if (!rt_flag) {
						System.out.println("Inside rt loop\n");
						Matcher m2 = rt_tag.matcher(line);
						if (m2.find()) {
							rt_flag = true;
							String rt = m2.group(1);
							System.out.println("First line" + rt);
							rtsf.append(rt);
							// System.out.println("Report Type is" + rt);
							// doc.add(new StringField("report_type", rt,
							// Field.Store.YES));
						}
					} else {
						rtsf.append(line);
					}
				}

				// System.out.println("Line is " + line);
				sb.append(line);
			}
			System.out.println("Our output" + rtsf.toString());
			// if ( rt.length > 0) {
			doc.add(new StringField("report_type", rtsf.toString(),
					Field.Store.YES));

			if (sb.length() > 0)
				doc.add(new TextField("contents", sb.toString(), Field.Store.NO));

		} catch (IOException e) {
			doc = null;
		}
		return doc;
	}

	@Override
	public void remove() {
		// Do nothing, but don't complain
	}

}
