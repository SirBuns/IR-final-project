import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;

public class MyAnalyzer {
	
	public Analyzer myAnalyzer;
	public IndexWriterConfig config;
	
	public MyAnalyzer(String bbad_words, String sseperator) {
		
		CharArraySet stopSet = CharArraySet.copy( EnglishAnalyzer.getDefaultStopSet());
		
		// add all bad words (separated by ;) to the stopSet
		String[] bad_words = bbad_words.split(sseperator);
		
		for(String word : bad_words) {
			stopSet.add(word);
		}
		
		myAnalyzer = new EnglishAnalyzer(stopSet);
		
		config = new IndexWriterConfig(myAnalyzer);
		
	}
	
	public Analyzer getMyAnalyzer() {
		return this.myAnalyzer;
	}
}