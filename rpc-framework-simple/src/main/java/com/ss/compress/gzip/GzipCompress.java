package com.ss.compress.gzip;

import com.ss.compress.Compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompress implements Compress {
    private static final int BUFFER_SIZE=1024*4; // 缓冲区大小
    @Override
    public byte[] compress(byte[] bytes) {
        if (bytes==null){
            throw new NullPointerException("bytes is null");
        }
        try(ByteArrayOutputStream out=new ByteArrayOutputStream(); GZIPOutputStream gzip=new GZIPOutputStream(out)) {
            gzip.write(bytes);
            gzip.flush(); //清空缓冲区
            gzip.finish();
            return out.toByteArray();
        }catch (IOException e){
            throw new RuntimeException("compress bytes error",e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if (bytes==null){
            throw new NullPointerException("bytes is null");
        }
        try (ByteArrayOutputStream out= new ByteArrayOutputStream(); GZIPInputStream gunzip=new GZIPInputStream(new ByteArrayInputStream(bytes))){
            byte[] buffer=new byte[BUFFER_SIZE];
            int n;
            while ((n= gunzip.read(buffer))>-1){  // 每次读取buffer长度的数据，分段写入
                out.write(buffer,0,n);
            }
            return out.toByteArray();
        }catch (IOException e){
            throw new RuntimeException("decompress  bytes error",e);
        }
    }
}
