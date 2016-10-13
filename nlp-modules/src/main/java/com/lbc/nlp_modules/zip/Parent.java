package com.lbc.nlp_modules.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lbc.nlp_modules.utils.Encodes;

/**
 * Just to set all annotations in one place
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@BenchmarkMode(Mode.SingleShotTime)
public class Parent {
    //uncomment the following line to run file size tests
    protected Path m_inputFile = new File("F://words.txt").toPath();
    
    String data;
    
    protected byte[] compressData;

    @Setup
    public void setup()
    {
        m_inputFile = new File("F://words.txt").toPath();
    }

    interface StreamFactory
    {
        public OutputStream getStream( final OutputStream underlyingStream) throws IOException;
    }
    
    interface InputStreamFactory {
    	public InputStream getStream(final InputStream underlyingStream ) throws IOException;
    }

    public int baseBenchmark( final StreamFactory factory ) throws IOException
    {
        
        String inputStr = "这个是用来测试压缩的句子。这个是用来测试压缩的句子。这个是用来测试压缩的句子。这个是用来测试压缩的句子。";
        byte[] inputdata = inputStr.getBytes();
    	ByteArrayOutputStream bos = new ByteArrayOutputStream(inputStr.length());
      try ( OutputStream os = factory.getStream( bos ) )
      {
    	  os.write(inputdata);
      }
    	
//    	ByteArrayOutputStream bos = new ByteArrayOutputStream((int) m_inputFile.toFile().length());
//        try ( OutputStream os = factory.getStream( bos ) )
//        {
//            Files.copy(m_inputFile, os);
//        }
        
      data = Encodes.encodeUrlSafeBase64(bos.toByteArray());
//        data = JSON.toJSONString(bos.toByteArray());
        return bos.size();
    }
    
    public int baseBenchmark( final InputStreamFactory factory ) throws IOException {
//    	byte[] dataByte = JSON.parseObject(data, new TypeReference<byte[]>(){});
    	byte[] dataByte = Encodes.decodeBase64(data);
    	ByteArrayInputStream bais = new ByteArrayInputStream(dataByte);  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        
        InputStream is = factory.getStream(bais);
        
        int count;  
        byte data[] = new byte[512];  
        while ((count = is.read(data, 0, 512)) != -1) {  
        	baos.write(data, 0, count);  
        } 
        System.out.println("decompress size: " + baos.size() + "; decompress sentence:" + baos.toString());
        return baos.size();
    }
    
    public static void main(String[] args) throws IOException {
//    	String testString = "5Z3NaltXFIVfxSjT4HPP2fv8eZqRC9GoHZf7o+vaWJGRZUgJGbXz0tBJSqHTPoOhb9M4fYxuxUotxXUSlxYWy5JAP0h3n73OEffb+1xYL7w/eDHpL85Xi/lseTg/mhxMvlmtzg6cO54f+WZfUtNdfGuv9/vF3K1m5yt3nprnqfn6ZDx3Ky/Ox+SiVC8piguluF5akTh0LuY8i11qp7E0UUqs+2fPjiaPJ+Oync+eLE4XS4v2aNTBHvbx6eJocd/wyfmmuhJDTtGeq/duHPuxCZ1uwscwTVVmMqS0f3K2Dr9cdIvV4TA58P79m3vnHYoLyYL7bAe3sK6kHN0QpElB/fvQdTrrchmz7ISeWvYWTsQ+WC2ets8u2tOvlqc3Izg5nh/7/ZNhHXry8rGI3Jqjvb9vn9Rz56vzL/8p4LYoFm1HlZ2fb4//i8Onh1evL//87verXy7fvP7tj8vLuxLau3r16u3P37/54ce3P/26/WKdnab7r8DSPC+bmRB1qTjxolFsOUjxwXkJfa6trctccu7UlkDsU6lRNivwc5fabhyxQ5s8NTRxvbo0udAGW1u2At8Famf9NI5DHFsZNoFuZNV0v8W2G9r+Z6FWX+zuctRsf7Km68dZv0nRd9NYdei7vtuO/JGpWiufH4byGU/58jCUL3jK14ehfIVTPjYfU14/GFY/P3fXZ9pawzvG8NmbHHbGzbdlH1o7806HMOtDCMOdst8dpOj6hB6dCedLahp7e0vy6yChGdt27PwtyWNzl+RyZ9icLYTxgi0nC1xTSbf1vg47y/1QRO6jtyfX24PpHcj1DmB6C7neAqa3kuutYHpHcr0jmN6JXO8Epncm1zuD6V3I9S5geldyvSuW3om8vkxg9WUiry8TWH2ZyOvLBFZfJvL6MoHVl4mcBxMYDyZyHkxgPJjIeTCB8WAm58EMxoOZnAczGA9mcj7JYHySyfvfGaz/ncn73xms/53J+98ZrP+dyXkwg/FgIefBAsaDhZwHCxgPFvL+YAHrDxZy/i5g/F3I+buA8Xch5+8Cxt+FnL8LGH8X8v2GArbfUMj3GwrYfkMlr3cqWL1TyeudClbvVHL+rmD8Xcn5u4LxdyXn7wrG35WcvysYf1dy/q5g/F3J+bui8Tf5/k7F2t/Rhpu/LT8wvbn3Gyw/ML256x3LD0xv7nrH8gPTm7vesfzA9Oaudyw/ML25+dvyA9Obm78tPyy9Pfd+g+UHpjd5vePB6h1Pzt8ejL89OQ96MB705DzowXjQc/e/LT8wvcn524Pxtyfnbw/G34GcvwMYfwdy/g5g/B3I+TuA8Xcg738HsP53IOfvAMbfgZy/Axh/B3L+DmD8Hcj5O4Dxt5Dzt4Dxt5BffyJg158IOX8LGH8LOX8LGH8L+X6DgO03CDl/Cxh/Czl/Cxh/KzkPKhgPKnk/VsH6sUrOgwrGg0rOJwrGJ0rej1WwfqyS86CC8aCS86Ci8SB5P1bB+rHkfqMK5jeq5H6jCuY3quR+owrmN6rk/pcK5n+p5H6MCubHqOR+jArmx6jkfowK5seo5P6ACuYPqOR+dQrmV6fpNp/s2e1/1Hx9eBvbuLRxPVmcLpYW8tGogz1upuOzx/DvpmQzhq1p2cWYD8L/11OzHf4T00Pevk1g7VtyO0EFsxNUcjtBBbMTVHJ7OwWzt1NyezsFs7fTTI6PGQwfye0EFcxOUMntBBXMTlDJ7QQVzE5Qye0EFcxOUDN5vZPB6p1MXu9ksHqnkF8+UcAunyC3j1Ew+xglt49RMPsYreT1ZYWqL1/+BQ==";
//    	String test1 = JSON.toJSONString(testString);
    	long start = System.currentTimeMillis();
    	GZip gZip = new GZip();
        System.out.print( "GZIP:" + gZip.gzip() );
        System.out.print(", UNGZIP:" + gZip.ungzip());
        long end1 = System.currentTimeMillis();
        System.out.println(", time consumer: " + (end1 - start));
        Snappy snappy = new Snappy();
        System.out.print( "Snappy (normal):" + snappy.snappyNormalOutput() );
        System.out.print(", snappy (normal) decompress:" + snappy.snappyDecompress());
        long end2 = System.currentTimeMillis();
        System.out.println("time consumer: " + (end2 - end1));
        snappy = new Snappy();
        System.out.print( "Snappy (framed);" + snappy.snappyFramedOutput() );
        System.out.print( ", Snappy (framed) decompress;" + snappy.snappyFramedDecompress() );
        long end3 = System.currentTimeMillis();
        System.out.println(", time consumer: " + (end3 - end2));
        Lz4 lz4 = new Lz4();
        System.out.print( "LZ4 (fast 64K);" + lz4.testFastNative64K() );
        System.out.print( ", LZ4 (fast 64K);" + lz4.lz4Decompress() );
        long end4 = System.currentTimeMillis();
        System.out.println(", time consumer: " + (end4 - end3));
        lz4 = new Lz4();
        System.out.print( "LZ4 (fast 128K);" + lz4.testFastNative128K() );
        System.out.print( ", LZ4 (fast 64K);" + lz4.lz4Decompress() );
        long end5 = System.currentTimeMillis();
        System.out.println(", time consumer: " + (end5 - end4));
        lz4 = new Lz4();
        System.out.print( "LZ4 (fast 32M);" + lz4.testFastNative32M() );
        System.out.print( ", LZ4 (fast 64K);" + lz4.lz4Decompress() );
        long end6 = System.currentTimeMillis();
        System.out.println(", time consumer: " + (end6 - end5));
//        lz4 = new Lz4();
//        System.out.println( "LZ4 (fast double 64K);" + lz4.testFastNativeDouble64K() );
//        System.out.println( "LZ4 (fast 64K);" + lz4.lz4Double64KDecompress() );
//        long end7 = System.currentTimeMillis();
//        System.out.println("time consumer: " + (end7 - end6));
//        lz4 = new Lz4();
//        System.out.println( "LZ4 (fast double 32M);" + lz4.testFastNativeDouble32M() );
//        System.out.println( "LZ4 (fast 64K);" + lz4.lz4Double64KDecompress() );
//        long end8 = System.currentTimeMillis();
//        System.out.println("time consumer: " + (end8 - end7));
//        lz4 = new Lz4();
//        System.out.println( "LZ4 (fast triple 32M);" + lz4.testFastNativeTriple32M() );
//        System.out.println( "LZ4 (fast 64K);" + lz4.lz4Double64KDecompress() );
//        long end9 = System.currentTimeMillis();
//        System.out.println("time consumer: " + (end9 - end8));
        lz4 = new Lz4();
        System.out.print( "LZ4 (high);" + lz4.testHighNative() );
        System.out.print( ", LZ4 (fast 64K);" + lz4.lz4HighDecompress() );
        long end10 = System.currentTimeMillis();
        System.out.println(", time consumer: " + (end10 - end6));
        for ( int i = 1; i <= 9; ++i ) {
        	long begin = System.currentTimeMillis();
            JdkDeflate test = new JdkDeflate();
            test.m_lvl = i;
            System.out.print("Deflate (lvl=" + i + ");" + test.deflate() );
            System.out.print(", Inflate (lvl=" + i + ");" + test.inflate() );
            long last = System.currentTimeMillis();
            System.out.println(", time consumer: " + (last - begin));
        }
    }


}
