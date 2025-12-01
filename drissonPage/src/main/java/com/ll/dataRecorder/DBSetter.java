package com.ll.dataRecorder;

import java.nio.file.Path;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class DBSetter extends BaseSetter<DBRecorder> {
    public DBSetter(DBRecorder recorder) {
        super(recorder);
    }

    @Override
    public void path(String path) {
        this.path(path, null);
    }

    @Override
    public void path(Path path) {
        this.path(path, null);
    }

    /**
     * 重写父类方法
     *
     * @param path  文件路径
     * @param table 数据表名称
     */
    public void path(Path path, String table) {
        this.path(path.toAbsolutePath().toString(), table);
    }

    /**
     * 重写父类方法
     *
     * @param path  文件路径
     * @param table 数据表名称
     */
    public void path(String path, String table) {

        this.recorder.lock.lock();
        try {
            super.path(path);
            if (this.recorder.conn != null) {
            }
        } finally {
            this.recorder.lock.unlock();
        }
    }


}
