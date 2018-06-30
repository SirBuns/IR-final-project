import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Indexer {
	
	private IndexWriter writer;
	private JSONParser j_parser = new JSONParser();
	Directory indexDirectory;
	Analyzer analyzer;
	
	
	public Indexer(String indexDirectoryPath) throws IOException {
	      
		indexDirectory = newDirectory(indexDirectoryPath); 
	      
		analyzer = newAnalyzer();
		
		writer = new IndexWriter(indexDirectory, newIndexWriterConfig(analyzer));
		
		JSONParser j_parser = new JSONParser();
		
	    	  
	}
	
	private Document getDocument(String context, String catagory) throws IOException {
	      Document document = new Document();

	      document.add(new StringField(Constants.CATAGORY_2, catagory, Store.YES));
	      document.add(new TextField(Constants.CONTENTS, context, Store.YES));

	      return document;
	}
	
	// translate every answer to a document and add to index. 
	public int createIndex(String dataDirPath) {

		try {
			JSONArray json_objects = (JSONArray) j_parser.parse(new FileReader((Constants.JSON_FILE_PATH))); 
			for (Object tmp_object : json_objects) {
				
				JSONObject aq_node = (JSONObject) tmp_object;
			
				JSONArray ans_node = (JSONArray) aq_node.get(Constants.CATAGORY);
			
				for (Object ans : ans_node) {
					
					Document doc = getDocument((String) ans, (String) aq_node.get(Constants.CATAGORY_2));
					
					writer.addDocument(doc);
					
				}
				
			}
			
			return writer.numDocs();
			
		} catch (FileNotFoundException eNotFound) {
        eNotFound.printStackTrace();
		} catch (IOException eIO) {
        eIO.printStackTrace();
		} catch (ParseException eParse) {
        eParse.printStackTrace();
		}
		
		return -1;
	}
	
	private static Analyzer newAnalyzer() {
		MyAnalyzer bestAnalyzer = new MyAnalyzer(Constants.BAD_WORDS, Constants.SEPRATOR);
		return bestAnalyzer.getMyAnalyzer();
    }

    private static IndexWriterConfig newIndexWriterConfig(Analyzer analyzer) {
    	Similarity sim = new BM25Similarity();
    	return new IndexWriterConfig(analyzer)
                .setOpenMode(OpenMode.CREATE)
                .setCodec(new SimpleTextCodec())
                .setCommitOnClose(true)
                .setSimilarity(sim);
    }
    
    static Directory newDirectory(String indexDirectoryPath) throws IOException {
        return FSDirectory.open(new File(indexDirectoryPath).toPath());
    }
    
    public void close() throws CorruptIndexException, IOException {
        writer.close();
     }

}