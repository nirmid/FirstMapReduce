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
			File file = new File("/src/main/stopWords/stopwords.txt");
			BufferedReader reader = new BufferedReader(new FileReader(file));
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
				transformKey();
				value = new IntWritable(corpus);
				corpus = (corpus + 1) % 2;
				if (isKeyValid()) {
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
		String[] keys = key.toString().split(",");
		for (String strKey : keys){
			if (stopWords.contains(strKey)){
				return false;
			}

		}
		return true;
	}

	private void transformKey() {
		//int lengthWithoutLastBracket = key.getLength() - 1;
		//String keyWithoutBrackets = key.toString().substring(1,lengthWithoutLastBracket);
		String newKey = key.toString().replaceAll(" ", ",");
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
