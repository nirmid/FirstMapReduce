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
                extends Mapper<Text, IntWritable, Text, Text>{
            private final static Text one = new Text("1");
            private final static Text addToCorpus0 = new Text("1,0");
            private final static Text addToCorpus1 = new Text("0,1");
            private Text word = new Text();
            private final Text asterisk = new Text();

            public void map(Text key, IntWritable value, Context context
            ) throws IOException, InterruptedException {
                StringTokenizer itr = new StringTokenizer(key.toString());
                asterisk.set("*");
                while (itr.hasMoreTokens()) {
                    word.set(itr.nextToken());
                    context.write(asterisk, one);
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
                extends Reducer<Text,Text,Text,Text> {
            public void reduce(Text key, Iterable<Text> values,
                               Context context
            ) throws IOException, InterruptedException {
                int sum0 = 0;
                int sum1 = 0;
                if (key.toString().equals("*")) {
                    for (Text val : values) {
                        int[] pairVal = splitTextCommasToIntArray(val);
                        sum0 += pairVal[0];
                    }
                    context.write(key,new Text(String.valueOf(sum0)));
                } else {
                    for (Text val : values) {
                        int[] pairVal = splitTextCommasToIntArray(val);
                        sum0 += pairVal[0];
                        sum1 += pairVal[1];
                    }
                    Text result = new Text();
                    result.set(sum0 + "," + sum1);
                    context.write(key, result);
                }
            }

            private int[] splitTextCommasToIntArray(Text val) {
                String input = val.toString();
                String[] parts = input.split(",");
                int[] intParts = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    intParts[i] = Integer.parseInt(parts[i]);
                }
                return intParts;
            }
        }

        public static void main(String[] args) throws Exception {
            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf, "EMR1");
            job.setJarByClass(Main.class);
            job.setMapperClass(TokenizerMapper.class);
            job.setCombinerClass(IntSumReducer.class);
            job.setReducerClass(IntSumReducer.class);
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            //TODO
            //FileInputFormat.addInputPath(job, new Path(args[1]));
            //FileOutputFormat.setOutputPath(job, new Path(args[2]));
            FileInputFormat.addInputPath(job, new Path(args[0]));
            FileOutputFormat.setOutputPath(job, new Path(args[1]));
            job.setInputFormatClass(CustomInputFormat.class);
            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }
    }

