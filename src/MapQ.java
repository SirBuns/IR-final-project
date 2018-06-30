import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MapQ {
    public Map<String, String> Q2toMap() throws Exception {

        int p=0,s=0;
        Map<String, String> map = new HashMap<String,String>();
        BufferedReader in = new BufferedReader(new FileReader("Q.txt"));
        String line = "";

        while ((line = in.readLine()) != null) {

            Pattern pattern = Pattern.compile("[\t]");
			Matcher matcher = pattern.matcher(line);

			  while(matcher.find()) {
				  p = matcher.end();
				  s = matcher.start();
			  }
            map.put(line.substring(0, p), line.substring(s, line.length()));
        }

        in.close();

        return map;
    }
}