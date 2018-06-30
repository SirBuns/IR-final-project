import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
// Index the categories files.
public class CIndexer {
	
	private IndexWriter writer;
	private JSONParser j_parser = new JSONParser();
	Directory indexDirectory;
	Analyzer analyzer;
	
	public CIndexer(String indexDirectoryPath) throws IOException {
		
		indexDirectory = newDirectory(indexDirectoryPath); 
	      
		analyzer = newAnalyzer();
		
		writer = new IndexWriter(indexDirectory, newIndexWriterConfig(analyzer));
		
		JSONParser j_parser = new JSONParser();
		
	}
	
	public int createIndex(String dataDirPath, FileFilter filter) 
		      throws IOException {
		      //get all files in the data directory
		      File[] files = new File(dataDirPath).listFiles();

		      for (File file : files) {
		         if(!file.isDirectory()
		            && !file.isHidden()
		            && file.exists()
		            && file.canRead()
		            && filter.accept(file)
		         ){
		            indexFile(file);
		         }
		      }
		      return writer.numDocs();
	}
	
	private void indexFile(File file) throws IOException {
	      System.out.println("Indexing "+file.getCanonicalPath());
	      Document document = getDocument(file);
	      writer.addDocument(document);
	}
	
	private Document getDocument(File file) throws IOException {
	      Document document = new Document();
	      // two fields - category: , text: ;
	      document.add(new StringField(Constants.CATAGORY_2, file.getName().replaceAll(".txt", ""), Store.YES));
	      document.add(new TextField(Constants.CONTENTS, new FileReader(file)));

	      return document;
	}
	
	// one time function. this function create the category files from the Yahoo - QA file.
	public void create_catagory_files_json(String path) {
	
		try {
			
			Map<String, String> docs_strings = new HashMap<String, String>();
			
			JSONArray json_objects = (JSONArray) j_parser.parse(new FileReader(path));
			String[] catagories = Constants.CATAGORIES.split(Constants.SEPRATOR);
			
			for(String tag : catagories) {
				
				long startTime  = System.currentTimeMillis();
				
				System.out.println("[+] tag : " + tag);
				
				String f_name = Constants.CATAGORIES_PATH + "\\" + tag + ".txt";
				
				File f = new File(f_name);
				f.createNewFile();
				
				FileWriter fw = new FileWriter(f_name, true);
				
				System.out.println("In tag: { " + tag + " }");
				
				for(Object tmp_obj : json_objects) {
					
					JSONObject obj = (JSONObject) tmp_obj;
					
					String str_obj_cat = (String) obj.get(Constants.CATAGORY_2);
					
					if(str_obj_cat.equals(tag)) {
						
						JSONArray ans_arr = (JSONArray) obj.get(Constants.CATAGORY);
						
						String context = new String();
						
						for(Object ans : ans_arr) {
							
							context += (String) ans;
							
						}
						
						docs_strings.put(tag, context);
						
						fw.write(context + "\n");
					}
				}
				
				fw.close();
				
				long finishTime = System.currentTimeMillis() - startTime;
				
				System.out.println("Finish tag: { " + tag + " }\nTime takes: " + finishTime);
				
			}
			
			System.out.println("Finish!!!");
			
			
		} catch (FileNotFoundException eNotFound) {
	        eNotFound.printStackTrace();
			} catch (IOException eIO) {
	        eIO.printStackTrace();
			} catch (ParseException eParse) {
	        eParse.printStackTrace();
			}
		
	}
	
	private static Analyzer newAnalyzer() {
		MyAnalyzer bestAnalyzer = new MyAnalyzer(Constants.BAD_WORDS, Constants.SEPRATOR);
		return bestAnalyzer.getMyAnalyzer();
    }

    private static IndexWriterConfig newIndexWriterConfig(Analyzer analyzer) {
    	// The configure of IndexWriter is very clear - we use BM25Similarity and EnglishAnalyzer
    	Similarity sim = new BM25Similarity();
    	return new IndexWriterConfig(analyzer)
                .setOpenMode(OpenMode.CREATE)
                .setCodec(new SimpleTextCodec())
                .setCommitOnClose(true)
                .setSimilarity(sim);
    }
    
    static Directory newDirectory(String indexDirectoryPath) throws IOException {
        // return new RAMDirectory();
        return FSDirectory.open(new File(indexDirectoryPath).toPath());
    }
    
    public void close() throws CorruptIndexException, IOException {
        writer.close();
    }
    
}