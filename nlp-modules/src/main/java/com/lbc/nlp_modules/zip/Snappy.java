package com.lbc.nlp_modules.zip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.xerial.snappy.SnappyFramedInputStream;
import org.xerial.snappy.SnappyFramedOutputStream;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

/**
 * Snappy library tests
 */
public class Snappy extends Parent {

    @Benchmark
    public int snappyNormalOutput() throws IOException
    {
        return baseBenchmark(new StreamFactory() {
            @Override
            public OutputStream getStream(OutputStream underlyingStream) throws IOException {
                return new SnappyOutputStream( underlyingStream, 65536 );
            }
        });
    }

    @Benchmark
    public int snappyFramedOutput() throws IOException
    {
        return baseBenchmark(new StreamFactory() {
            @Override
            public OutputStream getStream(OutputStream underlyingStream) throws IOException {
                return new SnappyFramedOutputStream( underlyingStream );
            }
        });
    }
    
    @Benchmark
    public int snappyDecompress() throws IOException
    {
        return baseBenchmark(new InputStreamFactory() {
            @Override
            public InputStream getStream(InputStream underlyingStream) throws IOException {
            	return new SnappyInputStream( underlyingStream );
            }
        });
    }
    
    @Benchmark
    public int snappyFramedDecompress() throws IOException
    {
        return baseBenchmark(new InputStreamFactory() {
            @Override
            public InputStream getStream(InputStream underlyingStream) throws IOException {
            	return new SnappyFramedInputStream( underlyingStream );
            }
        });
    }
}
