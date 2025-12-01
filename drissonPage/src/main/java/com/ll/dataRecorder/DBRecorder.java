package com.ll.dataRecorder;

import lombok.NonNull;
import org.sqlite.jdbc4.JDBC4Connection;

import javax.naming.NoPermissionException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 用于存储数据到sqlite的工具
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class DBRecorder extends BaseRecorder<Map<String, List<Map<Object, Object>>>> {

    protected Connection conn;
    private Statement stmt;
    protected String cur;

    public DBRecorder() {
        this("");
    }

    public DBRecorder(Integer cacheSize) {
        this("", cacheSize);
    }

    public DBRecorder(Path path) {
        this(path, null);
    }

    public DBRecorder(String path) {
        this(path, null);
    }

    public DBRecorder(Path path, Integer cacheSize) {
        this(path, cacheSize, null);
    }

    public DBRecorder(String path, Integer cacheSize) {

        this(path, cacheSize, null);
    }

    public DBRecorder(Path path, Integer cacheSize, String table) {
        this(path.toAbsolutePath().toString(), cacheSize, table);
    }

    public DBRecorder(String path, Integer cacheSize, String table) {
        super(path, cacheSize);
        this.table = table;
    }

    @Override
    protected void init() {
        super.init();
        this.type = "db";
        if (this.path != null) {
            this.set().path(path, table);
        }
    }

    /**
     * @return 返回用于设置属性的对象
     */
    public DBSetter set() {
        if (this.setter == null) this.setter = new DBSetter(this);
        return (DBSetter) this.setter;
    }

    @Override
    public void addData(Object data) {
        addData(data, null);
    }

    public void addData(Object data, String table) {
        while (pauseAdd) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        table = (table != null) ? table : this.table;
        if (table == null) throw new NullPointerException("未指定数据库表名。");

        //将数组转换成集合
        if (data instanceof Object[][]) {
            Object[][] objects = (Object[][]) data;
            for (int i = 0; i < objects.length; i++) {
                Object[] object = objects[i];
                object[i] = List.of(object);
            }
            data = List.of(data);
        }
        if (data instanceof Object[]) data = List.of(data);

        if (data == null) {
            List<Map<Object, Object>> list = this.data.computeIfAbsent(table, s -> new ArrayList<>());
            list.addAll(new ArrayList<>());
            this.dataCount++;
        } else if (data instanceof Map || data instanceof List && !(((List<?>) data).get(0) instanceof List) && !(((List<?>) data).get(0) instanceof Map)) {
            List<Map<Object, Object>> list = this.data.computeIfAbsent(table, k -> new ArrayList<>());
            list.addAll(Tools.dataToMap(this, data));
            this.dataCount++;
        } else {

        }

//        List<Object> dataList = new ArrayList<>();
//        if (data instanceof Map || (data instanceof List && !((List<?>) data).isEmpty()
//                                    && !(((List<?>) data).get(0) instanceof List)
//                                    && !(((List<?>) data).get(0) instanceof Map))) {
//            dataList.add(Tools.dataToListOrDict(this, data));
//        } else if (data instanceof List) {
//            for (Object d : (List<?>) data) {
//                dataList.add(Tools.dataToListOrDict(this, d));
//            }
//        } else {
//            dataList.add(new ArrayList<>());
//        }
//        this.data.computeIfAbsent(table, k -> new ArrayList<>()).addAll(dataList);
//        this.dataCount += dataList.size();
//        if (cacheSize > 0 && cacheSize <= dataCount) {
//            record();
//        }
    }

    /**
     * 执行sql语句并返回结果
     * @param sql sql语句
     * @param commit 是否提交到数据库
     * @return 查找到的结果
     * @throws SQLException sql异常
     */
    public ResultSet runSQL(@NonNull String sql, boolean commit) throws SQLException {
        this.stmt.execute(sql);
        ResultSet resultSet = this.stmt.getResultSet();
        if (commit) this.conn.commit();
        return resultSet;
    }

    /**
     * 连接数据库
     */
    private void connect() throws SQLException {
        File file = Paths.get(this.path).getParent().toFile();
        if (!file.exists()) file.mkdirs();
        this.conn = new JDBC4Connection(file.getAbsolutePath(), this.table, new Properties());
        this.stmt = this.conn.createStatement();
    }

    /**
     * 关闭数据库
     */
    public void close() {
        super.close();
        if (this.conn != null) {
            try {
                this.stmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            try {
                this.conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    protected void _record() throws NoPermissionException {
        try {
            this.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            this.stmt.execute("select  name from sqlite_master where type='table'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Map<Object, Object> tables = new HashMap<>();
        ResultSet resultSet = null;
        try {
            resultSet = this.stmt.getResultSet();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            try {
                if (!resultSet.next()) break;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
