package com.lbc.nlp_modules.zip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import org.openjdk.jmh.annotations.Benchmark;

/**
 * Java LZ4 implementations test
 */
public class Lz4 extends Parent {

    @Benchmark
    public int testFastNative64K() throws IOException {
        return lz4(LZ4Factory.nativeInstance().fastCompressor(), BLOCK_64K );
    }
    
    @Benchmark
    public int lz4Decompress() throws IOException {
        return lz4decompress(LZ4Factory.nativeInstance().fastDecompressor());
    }

    @Benchmark
    public int testFastNative128K() throws IOException {
        return lz4(LZ4Factory.nativeInstance().fastCompressor(), BLOCK_128K );
    }

    @Benchmark
    public int testFastNative32M() throws IOException {
        return lz4(LZ4Factory.nativeInstance().fastCompressor(), MAX_BLOCK_SIZE );
    }

    //Uncomment these tests if you want to see the performance of the less efficient implementations.
    //Keep in mind that you will not get extra benefits from using those. The only 2 options which affect
    //the output size are fast/high compressor and a compressor buffer size.
    //In terms of performance, you will be affected more by a change of a slower compressor rather than
    //by increasing a compressor buffer.

    @Benchmark
    public int testHighNative() throws IOException {
        return lz4(LZ4Factory.nativeInstance().highCompressor(), BLOCK_64K );
    }
    
    @Benchmark
    public int lz4HighDecompress() throws IOException {
        return lz4decompress(LZ4Factory.nativeInstance().fastDecompressor());
    }

//    @Benchmark
//    public int testFastUnsafe() throws IOException {
//        return lz4(LZ4Factory.unsafeInstance().fastCompressor(), BLOCK_64K );
//    }
//    @Benchmark
//    public int testHighUnsafe() throws IOException {
//        return lz4(LZ4Factory.unsafeInstance().highCompressor(), BLOCK_64K );
//    }
//
//    @Benchmark
//    public int testFastSafe() throws IOException {
//        return lz4(LZ4Factory.safeInstance().fastCompressor(), BLOCK_64K );
//    }
//    @Benchmark
//    public int testHighSafe() throws IOException {
//        return lz4(LZ4Factory.safeInstance().highCompressor(), BLOCK_64K );
//    }

    final int BLOCK_64K = 64 * 1024;
    final int BLOCK_128K = 128 * 1024;
    final int MAX_BLOCK_SIZE = 32 * 1024 * 1024;

    @Benchmark
    public int testFastNativeDouble64K() throws IOException {
        final LZ4Compressor compressor = LZ4Factory.nativeInstance().fastCompressor();
        return baseBenchmark(new StreamFactory() {
            @Override
            public OutputStream getStream(OutputStream underlyingStream) throws IOException {
                return new LZ4BlockOutputStream( new LZ4BlockOutputStream( underlyingStream, BLOCK_64K, compressor ), BLOCK_64K, compressor );
            }
        });
    }
    
    @Benchmark
    public int lz4Double64KDecompress() throws IOException {
        final LZ4FastDecompressor decompressor = LZ4Factory.nativeInstance().fastDecompressor();
        return baseBenchmark(new InputStreamFactory() {
            @Override
            public InputStream getStream(InputStream underlyingStream) throws IOException {
                return new LZ4BlockInputStream(underlyingStream, decompressor);
            }
        });
    }

    @Benchmark
    public int testFastNativeDouble32M() throws IOException {
        final LZ4Compressor compressor = LZ4Factory.nativeInstance().fastCompressor();
        return baseBenchmark(new StreamFactory() {
            @Override
            public OutputStream getStream(OutputStream underlyingStream) throws IOException {
                return new LZ4BlockOutputStream( new LZ4BlockOutputStream( underlyingStream, MAX_BLOCK_SIZE, compressor ), MAX_BLOCK_SIZE, compressor );
            }
        });
    }
    @Benchmark
    public int testFastNativeTriple32M() throws IOException {
        final LZ4Compressor compressor = LZ4Factory.nativeInstance().fastCompressor();
        return baseBenchmark(new StreamFactory() {
            @Override
            public OutputStream getStream(OutputStream underlyingStream) throws IOException {
                return new LZ4BlockOutputStream( new LZ4BlockOutputStream( new LZ4BlockOutputStream( underlyingStream, MAX_BLOCK_SIZE, compressor ), MAX_BLOCK_SIZE, compressor ), MAX_BLOCK_SIZE, compressor );
            }
        });
    }

    private int lz4( final LZ4Compressor compressor, final int blockSize ) throws IOException
    {
        return baseBenchmark(new StreamFactory() {
            @Override
            public OutputStream getStream(OutputStream underlyingStream) throws IOException {
                return new LZ4BlockOutputStream( underlyingStream, blockSize, compressor );
            }
        });
    }
    
    private int lz4decompress(final LZ4FastDecompressor deCompressor) throws IOException
    {
        return baseBenchmark(new InputStreamFactory() {
            @Override
            public InputStream getStream(InputStream underlyingStream) throws IOException {
            	return new LZ4BlockInputStream( underlyingStream, deCompressor );
            }
        });
    }
}
