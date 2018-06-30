import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class IR_Project_Tester{
	
	String indexDir = Constants.INDEX_DIR_PATH; 
	String dataDir = Constants.JSON_FILE_PATH;
	String answersPath = Constants.ANSWERS_FILE_PATH;
	Indexer indexer;
	Searcher searcher;
	
	public static void main(String[] args) throws Exception {
	      IR_Project_Tester tester;
	      
	      try {
	         tester = new IR_Project_Tester();
	         // as in classifier this is a one-time function that we need to apply only once to create the index directory.
	         tester.createIndex();
	         
	         //create a classifier.
	         Classifier cls = new Classifier();
	         String q;
	         
	         // creating the answers file.
	         String file_name = Constants.ANSWERS_FILE_PATH;
	         File f = new File(file_name);
	         f.createNewFile();
	         FileWriter fw = new FileWriter(file_name, true);
	         
	         Map<String, String> tmp_map = new HashMap<String, String>();
	         MapQ mapQ = new MapQ();
	         tmp_map = mapQ.Q2toMap();
	         Iterator<Map.Entry<String, String>> it = tmp_map.entrySet().iterator();
	         
	         BufferedReader in = new BufferedReader(new FileReader(Constants.QUESTIONS_FILE_PATH));
	         String line = "";
	         
	         // for every line in the questions file do:
	         while((line = in.readLine()) != null) {
	        	 
	        	 String i = line.replaceAll("[\\D]", "");
	        	 
	        	 System.out.println("id: " + i);
	        	 // build the boosting query
	        	 q = line;
	        	 System.out.println("handle_query: " + cls.handle_query(q));
	        	 if(q == null) { continue; }
	        	 cls.init_vars(q);
	        	 System.out.println("Query category: " + cls.getQueryCatagory());
	        	 q = cls.getBoostQuery();
	        	 if(q == null) { continue; }
	        	 // search the new query with the right boosting in every term and save them in the answers file 
	        	 tester.search_and_save(q,i,fw);
	         }
	         
	      } catch (IOException e) {
	         e.printStackTrace();
	      } catch (ParseException e) {
	    	  e.printStackTrace();
	      }
	      
	   }

	private void createIndex() throws IOException {
	      indexer = new Indexer(indexDir);
	      int numIndexed;
	      long startTime = System.currentTimeMillis();	
	      numIndexed = indexer.createIndex(dataDir);
	      if(numIndexed < 0) {
	    	  System.out.println("[-] Error occur in createIndex(String dataDirPath)");
	    	  System.exit(1);
	      }
	      long endTime = System.currentTimeMillis();
	      indexer.close();
	      System.out.println(numIndexed+" Documents indexed, time taken: "
	         +(endTime-startTime)+" ms");		
	   }
	
	// In short, create json object for every question (because every question is iteration and every iteration we call this function.)
	// the json is build like this: {id, top-5-answers}
	// Maybe worth to notice that we search for 19 best answers and we do this because of the implement of the classifier.
	private void search_and_save(String searchQuery, String id, FileWriter file) throws IOException, ParseException {
	      searcher = new Searcher(indexDir);
	      long startTime = System.currentTimeMillis();
	      TopDocs hits = searcher.search(searchQuery);
	      long endTime = System.currentTimeMillis();
	   
	      System.out.println(hits.totalHits +
	         " answers found. Time :" + (endTime - startTime));
	      
	      JSONObject obj = new JSONObject();
	      obj.put("id", id);
	      JSONArray answers = new JSONArray();
	      
	      int i = 0;
	      for(ScoreDoc scoreDoc : hits.scoreDocs) {
		     i++;
	    	 if(i > 5) { break; }
	         Document doc = searcher.getDocument(scoreDoc);
	            System.out.println("[" + i + "] Answer: "
	            + doc.get(Constants.CONTENTS) + "\nScore: " + scoreDoc + "\nCatagory: " + doc.get(Constants.CATAGORY_2) + '\n');
	        
	         JSONObject ans = new JSONObject();
	         ans.put("answer", doc.get(Constants.CONTENTS));
	         ans.put("score", scoreDoc.score);
	         answers.add(ans);
	      }
	      
	      obj.put("answers", answers);
	      
	      writeJSONtoFile(obj,file);
	   }
	
	private void writeJSONtoFile(JSONObject obj, FileWriter file) throws IOException {
			file.write(obj.toJSONString() + '\n');
			System.out.println("Successfully Copied JSON Object to File...");
			System.out.println("\nJSON Object: " + obj);
			System.out.println("toJSONString : " + obj.toJSONString());
	}
}
