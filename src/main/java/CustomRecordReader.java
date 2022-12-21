import java.io.IOException;

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

	CustomRecordReader() {
		reader = new LineRecordReader();
		key = null;
		value = null;
	}
	
	@Override
	public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
		reader.initialize(split, context);
	}


	@Override
	public void close() throws IOException {
		reader.close();		
	}
	
	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if (reader.nextKeyValue()) {
			key = reader.getCurrentValue();
			value = new IntWritable((int)reader.getCurrentKey().get()%2);
			return true;
		} else {
			key = null;
			value = null;
			return false;
		}
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
