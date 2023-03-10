import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;


public class CustomRecordReader extends RecordReader<Text,IntWritable> {

	protected LineRecordReader reader;
	protected Text key;
	protected IntWritable value;
	protected ArrayList<String> stopWords;
	protected int corpus;


	CustomRecordReader() {
		reader = new LineRecordReader();
		key = null;
		value = null;
		stopWords = new ArrayList<>();
		corpus = 0;
	}
	private void createStopWords(){
		try {
			InputStream stream = getClass().getClassLoader().getResourceAsStream("stopwords.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] words = line.split(" ");
				stopWords.addAll(Arrays.asList(words));
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
		reader.initialize(split, context);
		createStopWords();
	}


	@Override
	public void close() throws IOException {
		reader.close();		
	}
	
	@Override
	public boolean nextKeyValue() throws IOException{
		while (true) {
			if (reader.nextKeyValue()) {
				key = reader.getCurrentValue();
				value = new IntWritable(corpus);
				corpus = (corpus + 1) % 2;
				if (isKeyValid()) {
					transformKey();
					return true;
				}
			} else {
				key = null;
				value = null;
				return false;
			}
		}
	}

	private boolean isKeyValid() {
		String []splitKey = key.toString().split("\\t");
		if (splitKey.length == 0){
			return false;
		}
		String[] keys = splitKey[0].split(" ");
		if(keys.length != 3){
			return false;
		}
		for (String strKey : keys){
			if (stopWords.contains(strKey) || !isValidWord(strKey)){
				return false;
			}
		}
		return true;
	}

	private boolean isValidWord(String strKey) {
		if(strKey.length() == 0)
			return false;
		for(int i=0; i<strKey.length(); i=i+1 ){
			if(strKey.charAt(i) < 1488 || strKey.charAt(i) > 1514)
				return false;
		}
		return true;
	}

	private void transformKey() {
		//int lengthWithoutLastBracket = key.getLength() - 1;
		//String keyWithoutBrackets = key.toString().substring(1,lengthWithoutLastBracket);
		String newKey = key.toString().replaceAll(" ", ",");
		//newKey = newKey.replaceAll("\\[|\\]", "");
		String[] keys;
		keys = newKey.split("\\t");
		newKey = keys[0];
		key.set(newKey);
	}

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		return key;
	}
	
	@Override
	public IntWritable getCurrentValue() throws IOException, InterruptedException {
		return value;
	}	


	@Override
	public float getProgress() throws IOException, InterruptedException {
		return reader.getProgress();
	}

}
