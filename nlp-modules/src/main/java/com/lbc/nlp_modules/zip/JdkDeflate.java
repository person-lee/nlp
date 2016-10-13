package com.lbc.nlp_modules.zip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;

/**
 * JDK java.util.zip Deflater/Inflater test
 */
public class JdkDeflate extends Parent {
    @Param({"1", "2", "3", "4", "5", "6", "7", "8", "9"})
    public int m_lvl;

    @Benchmark
    public int deflate() throws IOException
    {
        return baseBenchmark(new StreamFactory() {
            @Override
            public OutputStream getStream(OutputStream underlyingStream) throws IOException {
                return new DeflaterOutputStream( underlyingStream, new Deflater( m_lvl, true ), 512 );
            }
        });
    }
    
    @Benchmark
    public int inflate() throws IOException
    {
        return baseBenchmark(new InputStreamFactory() {
            @Override
            public InputStream getStream(InputStream underlyingStream) throws IOException {
            	return new InflaterInputStream( underlyingStream, new Inflater(true), 65536 );
            }
        });
    }
}
