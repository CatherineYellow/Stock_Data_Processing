package mapper;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class MultipleInputMapper1 extends Mapper<LongWritable, Text, Text, Text> {
    Text k = new Text();
    Text v = new Text();
    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS000");
    
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String input = value.toString();
        String[] split = input.split("\t");
            
        long transactTime = Long.parseLong(split[12]);
        // 判断为平安银行且时间为连续竞价
        if(split[8].equals("000001") &&
//                (transactTime >= 20190102093000000L && transactTime <= 20190102093030000L)
                ((transactTime >= 20190102093000000L && transactTime < 20190102113000000L) ||
                 (transactTime >= 20190102130000000L && transactTime < 20190102145700000L))
        ){
            // 将k设置成AppSeqNum，表示委托索引
            k.set(split[7]);
            // 转化价格
            float price = Float.parseFloat(split[10]);
            Date date = null;
            try {
                // 转化时间
                date = inputFormat.parse(split[12]);
                String time = outputFormat.format(date);
                // 将v设置成O，TIMESTAMP，PRICE，BUY_SELL_FLAG,ORDER_TYPE
                // 表示order表，委托时间，价格，委托数量，买卖方，委托单类型
//                 v.set("O," + time + "," + price + "," + split[11] + "," + split[13] + "," + split[14]);

                StringBuilder vBuilder = new StringBuilder("O,");
                vBuilder.append(time).append(",").append(price).append(",").append(split[11]).append(",").append(split[13]).append(",").append(split[14]);
                v.set(vBuilder.toString());
                
                context.write(k, v);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
