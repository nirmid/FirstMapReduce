import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;

public class CustomInputFormat extends FileInputFormat<Text, IntWritable> {
 
 
	  @Override
	  public RecordReader<Text, IntWritable> createRecordReader(InputSplit split, TaskAttemptContext context) {
	    return new CustomRecordReader();
	  }
	  
	  @Override
	  protected boolean isSplitable(JobContext context, Path file) {
	    CompressionCodec codec =
	      new CompressionCodecFactory(context.getConfiguration()).getCodec(file);
	    return codec == null;

	  }
 
}
