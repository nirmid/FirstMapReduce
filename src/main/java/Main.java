import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
public class Main {
        public static class TokenizerMapper
                extends Mapper<Text, IntWritable, Text, IntWritable[]>{
            private final static IntWritable[] countAll = {new IntWritable(1)};
            private final static IntWritable[] addToCorpus0 = {new IntWritable(1),new IntWritable(0)};
            private final static IntWritable[] addToCorpus1 = {new IntWritable(0),new IntWritable(1)};
            private Text word = new Text();
            private final Text asterisk = new Text("*");

            public void map(Text key, IntWritable value, Context context
            ) throws IOException, InterruptedException {
                StringTokenizer itr = new StringTokenizer(value.toString());
                while (itr.hasMoreTokens()) {
                    word.set(itr.nextToken());
                    context.write(asterisk, countAll);
                    if (value.get() == 1) {
                        context.write(word, addToCorpus1);
                    }
                    else{
                        context.write(word,addToCorpus0);
                    }
                }
            }
        }

        public static class IntSumReducer
                extends Reducer<Text,IntWritable[],Text,Text> {

            public void reduce(Text key, Iterable<IntWritable[]> values,
                               Context context
            ) throws IOException, InterruptedException {
                int sum0 = 0;
                int sum1 = 0;
                if (key.toString().equals("*")) {
                    for (IntWritable[] val : values) {
                        sum0 += val[0].get();
                    }
                    context.write(key,new Text(String.valueOf(sum0)));
                } else {
                    for (IntWritable[] val : values) {
                        sum0 += val[0].get();
                        sum1 += val[1].get();
                    }
                    Text result = new Text();
                    result.set(sum0 + "," + sum1);
                    context.write(key, result);
                }
            }
        }

        public static void main(String[] args) throws Exception {
            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf, "word count");
            job.setJarByClass(Main.class);
            job.setMapperClass(TokenizerMapper.class);
            job.setCombinerClass(IntSumReducer.class);
            job.setReducerClass(IntSumReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(IntWritable.class);
            FileInputFormat.addInputPath(job, new Path(args[1]));
            FileOutputFormat.setOutputPath(job, new Path(args[2]));
            job.setInputFormatClass(CustomInputFormat.class);
            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }
    }

