package com.ll.dataRecorder;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class ByteRecorder extends OriginalRecorder<List<ByteRecorder.ByteData>> {
    private static final byte[] END = {0, 2};

    /**
     * 用于记录字节数据的工具
     */
    public ByteRecorder() {
        this("");
    }

    /**
     * 用于记录字节数据的工具
     *
     * @param path 保存的文件路径
     */
    public ByteRecorder(Path path) {
        this(path, null);
    }

    /**
     * 用于记录字节数据的工具
     *
     * @param cacheSize 每接收多少条记录写入文件，0为不自动写入
     */
    public ByteRecorder(Integer cacheSize) {
        this("", null);
    }

    /**
     * 用于记录字节数据的工具
     *
     * @param path      保存的文件路径
     * @param cacheSize 每接收多少条记录写入文件，0为不自动写入
     */
    public ByteRecorder(Path path, Integer cacheSize) {
        super(path == null ? null : path.toAbsolutePath().toString(), cacheSize);
    }

    /**
     * 用于记录字节数据的工具
     *
     * @param path 保存的文件路径
     */
    public ByteRecorder(String path) {
        super(path, null);
    }

    /**
     * 用于记录字节数据的工具
     *
     * @param path      保存的文件路径
     * @param cacheSize 每接收多少条记录写入文件，0为不自动写入
     */
    public ByteRecorder(String path, Integer cacheSize) {
        super("".equals(path) ? null : path, cacheSize);
    }

    /**
     * @param data 类型只能为byte[]
     */
    @Override
    public void addData(Object data) {
        if (data instanceof byte[]) addData((byte[]) data, null);
        else throw new IllegalArgumentException("data类型只能为byte[]为了兼容");
    }


    /**
     * 添加一段二进制数据
     *
     * @param data bytes类型数据
     * @param seek 在文件中的位置，None表示最后
     */
    public void addData(byte[] data, Long seek) {
        while (this.pauseAdd) {  //等待其它线程写入结束
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (seek != null && seek < 0) throw new IllegalArgumentException("seek参数只能接受null或大于等于0的整数。");
        this.data().add(new ByteData(data, seek));
        this.dataCount++;
        if (0 < this.cacheSize() && this.cacheSize() <= this.dataCount) this.record();

    }

    /**
     * @return 返回当前保存在缓存的数据
     */
    public List<ByteData> data() {
        return this.data;
    }

    /**
     * 记录数据到文件
     */
    protected void _record() {
        try {
            if (!Files.exists(Paths.get(path))) {
                Files.createFile(Paths.get(path));
            }
            try (RandomAccessFile file = new RandomAccessFile(path, "rw")) {
                byte[] previous = null;
                for (ByteData data : data) {
                    byte[] loc = (data.seek == null) ? END : data.data;
                    if (!Arrays.equals(previous, loc) && !Arrays.equals(loc, END)) {
                        file.seek(loc[1]);
                        previous = loc;
                    }
                    file.write(data.data);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ByteData {
        private byte[] data;
        private Long seek;
    }
}
