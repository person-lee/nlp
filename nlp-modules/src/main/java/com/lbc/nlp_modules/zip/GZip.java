package com.lbc.nlp_modules.zip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.openjdk.jmh.annotations.Benchmark;

/**
 * JDK GZIP test
 */
public class GZip extends Parent {
    @Benchmark
    public int gzip() throws IOException
    {
        return baseBenchmark(new StreamFactory() {
            @Override
            public OutputStream getStream(OutputStream underlyingStream) throws IOException {
            	return new GZIPOutputStream( underlyingStream, 65536 );
            }
        });
    }
    
    @Benchmark
    public int ungzip() throws IOException
    {
        return baseBenchmark(new InputStreamFactory() {
            @Override
            public InputStream getStream(InputStream underlyingStream) throws IOException {
            	return new GZIPInputStream( underlyingStream, 65536 );
            }
        });
    }
}
