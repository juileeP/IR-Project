import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.BufferedReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class indexing {
	private String dirname = "/Index";
	private String corpusdir = "/Corpus/";
	private String queryDir = "/Queries";
	private String topFile = "/Lucene.top";
	BufferedReader br = null;
	FileWriter fw = null;
	BufferedWriter bw = null;
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public void indexer() {
		try {
			IndexWriter index;
			@SuppressWarnings("deprecation")
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
			@SuppressWarnings("deprecation")
			IndexWriterConfig config = new IndexWriterConfig(
					Version.LUCENE_CURRENT, analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

			Directory dir;

			dir = FSDirectory.open(new File(dirname));
			index = new IndexWriter(dir, config);

			File file = new File(corpusdir);
			// Reading directory contents
			File[] files = file.listFiles();

			Date indSrtTime = new Date();
			for (int i = 0; i < files.length; i++) {
				Date date = new Date();
				System.out.println("Indexing Start time for" + files[i] + " "
						+ dateFormat.format(date));
				TrecDocIterator docs = new TrecDocIterator(files[i]);
				Document document;

				while (docs.hasNext()) {
					System.out.println("Has Next...\n");
					document = docs.next();
					index.addDocument(document);
				}
				Date endDate = new Date();
				System.out.println("Indexing End time for" + files[i] + " "
						+ dateFormat.format(endDate));
			}
			Date indEndTime = new Date();
			double diffInTime = (double) (((indEndTime.getTime()) - (indSrtTime
					.getTime())));
			System.out.println("Time taken for Indexing is " + diffInTime
					/ (1000 * 60) + " mins");
			index.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void searchDocs() {
		try {
			String Querynum = "";
			Directory dir = FSDirectory.open(new File(dirname));
			@SuppressWarnings("deprecation")
			IndexReader index = IndexReader.open(dir);
			IndexSearcher searchIndex = new IndexSearcher(index);

			/* BM25 */
			Similarity simfm = new BM25Similarity();
			searchIndex.setSimilarity(simfm);

			@SuppressWarnings("deprecation")
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
			@SuppressWarnings("deprecation")
			QueryParser qp = new QueryParser(Version.LUCENE_CURRENT,
					"contents", analyzer);

			File resFile = new File(topFile);
			fw = new FileWriter(resFile.getAbsoluteFile());
			bw = new BufferedWriter(fw);

			File file = new File(queryDir);
			// Reading directory contents
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				String currLine;
				br = new BufferedReader(new FileReader(files[i]));
				while ((currLine = br.readLine()) != null) {
					if (currLine.startsWith("<num>")) {
						Querynum = currLine
								.substring(currLine.indexOf(':') + 1);

					}
					if (currLine.startsWith("<desc>")) {
						currLine = currLine
								.substring(currLine.indexOf(' ') + 1);
						Query q = qp.parse(currLine);
						Date startTime = new Date();
						TopDocs results = searchIndex.search(q, 1000);
						for (int j = 0; j < Math.min(1000, results.totalHits); j++) {
							float score = results.scoreDocs[j].score;
							Document doc = index
									.document(results.scoreDocs[j].doc);
							String docno = doc.getField("checksum")
									.stringValue();
							bw.write(Querynum + " Q0 " + " " + docno + " " + j
									+ " " + score + " patankar " + "\n");
							// System.out.println (Querynum + " Q0 " + " " +
							// docno + " " + j + " " + score +
							// " patankar");
						}
						Date endTime = new Date();
						double diffInTime = (double) (((endTime.getTime()) - (startTime
								.getTime())));
						System.out
								.println("Time taken for documents retrieval for Query '"
										+ currLine
										+ "' is "
										+ diffInTime
										/ 1000 + " secs");
					}
				}
				bw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		indexing idocs = new indexing();
		idocs.indexer();
		// idocs.searchDocs();
		// System.out.println("Done");
	}

}
