import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher {
	
	   IndexSearcher indexSearcher;
	   Directory indexDirectory;
	   Analyzer analyzer;
	   QueryParser queryParser;
	   Query query;
	   DirectoryReader reader;
	   
	   public Searcher(String indexDirectoryPath) throws IOException {
		   
		   indexDirectory = newDirectory(indexDirectoryPath);
		   
		   analyzer = newAnalyzer();
		   
		   reader = DirectoryReader.open(indexDirectory);
		   
		   queryParser = new QueryParser(Constants.CONTENTS, analyzer);
		   
		   Similarity sim = new BM25Similarity();
		   
		   indexSearcher = new IndexSearcher(reader);
		   
		   indexSearcher.setSimilarity(sim);
	   }
	   
	   public TopDocs search( String searchQuery) throws IOException, ParseException {
		  // we use the escape method to fix some parsing problems.
		  query = queryParser.parse(queryParser.escape(searchQuery));

          System.out.println();
          
	      return indexSearcher.search(query, Constants.MAX_SEARCH);
	   }

	   public Document getDocument(ScoreDoc scoreDoc) 
	      throws CorruptIndexException, IOException {
	      return indexSearcher.doc(scoreDoc.doc);	
	   }
	   
	   private static Analyzer newAnalyzer() {
		   MyAnalyzer bestAnalyzer = new MyAnalyzer(Constants.BAD_WORDS, Constants.SEPRATOR);
		   // we use this MyAnalyzer class to make some changes in the standard EnAnalyzer - remove some words.
		   return bestAnalyzer.getMyAnalyzer(); 
	   }
	    
	   private static Directory newDirectory(String indexDirectoryPath) throws IOException {
	        // return new RAMDirectory();
	        return FSDirectory.open(new File(indexDirectoryPath).toPath());
	    }
	   
}