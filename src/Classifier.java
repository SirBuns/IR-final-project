import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class Classifier {
	
	static String indexDir = Constants.INDEX_CATAGORIES_PATH;
	String dataDir = Constants.CATAGORIES_PATH;
	CIndexer indexer;
	static Searcher searcher;
	String original_query;
	String query;
	Map<String, Map<String, Float>> m;
	String category;
	String boost_query;
	
	public void Classifier() throws IOException, ParseException {
		//no need of this function - we only apply this once to create the Index folder.
		//createIndex();
	}
	
	public void init_vars(String q) throws IOException, ParseException {
		original_query = q;
		q = handle_query(q);
		m = map_query(q);
		print_map_query(m);
		category = classifyQuery(m);
		boost_query = build_boosting_query(q, m, category);
	}
	//access functions.
	public String getQuery() {	return query; }
	public Map<String, Map<String, Float>> getQueryMap() {	return m; }
	public String getQueryCatagory() { return category; }
	public String getBoostQuery() { return boost_query; }
	
	// after we classify the query to a category and map all the terms to categories and scores now is the hart of this class
	// and the truly goal of making all this. we build new query like this:
	// original_query: how to build a successful IR model? -> handled_query -> (handled_query: h_q , category: c) ->
	// boosting_query: '<(score) first_term[category : c]>' + '^' + 'first term' + " " ...
	public String build_boosting_query(String q, Map<String, Map<String, Float>> m, String classify_catagory) {
		int p;
		   
		   Iterator<Map.Entry<String, Map<String, Float>>> it = m.entrySet().iterator();
		   while(it.hasNext()) {
			   Map.Entry<String, Map<String, Float>> pair = it.next();
			   String str = pair.getKey();
			   
			   Pattern pattern = Pattern.compile(str);
			   Matcher matcher = pattern.matcher(q);
			   
			   while(matcher.find()) {
		   			p = matcher.end();
		   			if(pair.getValue().get(classify_catagory) != null) {
		   				q = q.substring(0, p) + "^" + Float.toString(10*pair.getValue().get(classify_catagory)) + q.substring(p, q.length());
		   			}
		   		}
		   }
		   System.out.println("Final query:" + q);
		   return q;
	}
	
	public void print_map_query(Map<String, Map<String, Float>> m) {
		   Iterator<Map.Entry<String, Map<String, Float>>> it = m.entrySet().iterator();
		   while(it.hasNext()) {
			   Map.Entry<String, Map<String, Float>> pair = it.next();
			   System.out.println("[*]term: " + pair.getKey());
			   
			   Iterator<Map.Entry<String, Float>> it2 = pair.getValue().entrySet().iterator();
			   while(it2.hasNext()) {
				   Map.Entry<String, Float> pair2 = it2.next();
				   System.out.println("< catagory = " + pair2.getKey() + " , score = " + pair2.getValue() + " >");
			   }
		   }
	   }
	// map every term in query to list (Example): <Term: banana> -> <(Category: medic , Score: 5.2) , (Category:* , Score:*)...>    
	public Map<String, Map<String, Float>> map_query(String q) throws IOException, ParseException {   
		   
		   Map<String, Map<String, Float>> m  = new HashMap<String, Map<String, Float>>();
		   
		   String[] terms = q.split(" ");
		   
		   Searcher searcher = new Searcher(indexDir);
		   
		   for(String term : terms) {
			   
			   if(term == null) { continue; }
			   TopDocs hits = searcher.search(term);
			   
			   Map<String,  Float> tmp_map = new  HashMap<String, Float>();
			   
			   for(ScoreDoc scoreDoc : hits.scoreDocs) {			   
			   
				  Document doc = searcher.getDocument(scoreDoc);
				  tmp_map.put(doc.get(Constants.CATAGORY_2), scoreDoc.score);
				   
			   }
			   
			   m.put(term, tmp_map);
			   
		   }
		   
		   return m; 
	   }
	
	// classify the query by summing up all the categories score in each term and take the category with the highest score.
	// like adding to buckets - in these case the buckets are the categories and in each iteration (iteration for every term)
	// we add to all buckets the score of the cross category in term
	public static String classifyQuery(Map<String, Map<String, Float>> m){
		   
		   String ans = "";
		   Float bestScore = 0f;
		   
		   Map<String, Float> catagories_score = new HashMap<String, Float>();
		   
		   Iterator<Map.Entry<String, Map<String, Float>>> it = m.entrySet().iterator();
		   while(it.hasNext()) {
			   Map.Entry<String, Map<String, Float>> pair = it.next();
			   
			   Iterator<Map.Entry<String, Float>> it2 = pair.getValue().entrySet().iterator();
			   while(it2.hasNext()) {
				   Map.Entry<String, Float> pair2 = it2.next();
				   
				   String key = pair2.getKey();
				   Float val = pair2.getValue();
				   
				   if(catagories_score.get(key) == null) {
					   catagories_score.put(key, 0f);
				   }
				   val += catagories_score.get(key);
				   catagories_score.put(key, val);
			   }
		   }
		   
		   Iterator<Map.Entry<String, Float>> it3 = catagories_score.entrySet().iterator();
		   while(it3.hasNext()) {
			   Map.Entry<String, Float> pair3 = it3.next();
			   if(pair3.getValue() > bestScore) {
				   bestScore = pair3.getValue();
				   ans = pair3.getKey();
			   }
		   }
		   
		   return ans;
	   }
	
	// we make some changes in the original query to delete some bad chars
	public String handle_query(String q) throws IOException, ParseException {
		   
		   String[] bad_chrs = Constants.BAD_CHRS.split(Constants.SEPRATOR); 
		   for(String chr : bad_chrs) {
			   q = q.replaceAll(chr, "");
		   }
		   
		   return q;
	   }
	
	public void createIndex() throws IOException {
	      indexer = new CIndexer(indexDir);
	      int numIndexed;
	      long startTime = System.currentTimeMillis();	
	      numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
	      if(numIndexed < 0) {
	    	  System.out.println("[-] Error occur in createIndex(String dataDirPath)");
	    	  System.exit(1);
	      }
	      long endTime = System.currentTimeMillis();
	      indexer.close();
	      System.out.println(numIndexed+" Documents indexed, time taken: "
	         +(endTime-startTime)+" ms");		
	   }

	public void search(String searchQuery) throws IOException, ParseException {
	      searcher = new Searcher(indexDir);
	      long startTime = System.currentTimeMillis();
	      TopDocs hits = searcher.search(searchQuery);
	      long endTime = System.currentTimeMillis();
	   
	      System.out.println(hits.totalHits +
	         " answers found. Time :" + (endTime - startTime));
	      int i = 0;
	      for(ScoreDoc scoreDoc : hits.scoreDocs) {
	    	 i++;
	         Document doc = searcher.getDocument(scoreDoc);
	            System.out.println("[" + i + "] Answer: "
	            + doc.get(Constants.CATAGORY_2) + "\nScore: " + scoreDoc + '\n');
	      }
	      
	   }
}