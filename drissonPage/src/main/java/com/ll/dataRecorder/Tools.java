package com.ll.dataRecorder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Tools {


    /**
     * 检查文件或文件夹是否有重名，并返回可以使用的路径
     *
     * @param path 文件或文件夹路径
     * @return 可用的路径，Path对象
     */
    public static Path getUsablePath(String path) {
        return getUsablePath(path, true, true);
    }

    /**
     * 检查文件或文件夹是否有重名，并返回可以使用的路径
     *
     * @param path          文件或文件夹路径
     * @param isFile        目标是文件还是文件夹
     * @param createParents 是否创建目标路径
     * @return 可用的路径，Path对象
     */
    public static Path getUsablePath(String path, boolean isFile, boolean createParents) {
        Path filePath = Paths.get(path).toAbsolutePath();
        Path parent = filePath.getParent();
        if (createParents) parent.toFile().mkdirs();

        File pathFile = new File(parent.toFile(), makeValidName(filePath.getFileName().toString()));
        String wholeName = new File(parent.toFile(), makeValidName(filePath.getFileName().toString())).getName();

        String name = pathFile.isFile() ? wholeName.substring(0, wholeName.lastIndexOf('.')) : wholeName;
        String ext = pathFile.isFile() ? wholeName.substring(wholeName.lastIndexOf('.')) : "";


        int num;
        String srcName;
        boolean firstTime = true;

        while (pathFile.exists() && pathFile.isFile() == isFile) {
            int i = name.lastIndexOf('_');
//            Matcher matcher = Pattern.compile("(.*)_(\\d+)$").matcher(name);
            if (i < 0 || firstTime) {
                srcName = name;
                num = 1;
            } else {
                srcName = name.substring(0, i);
                try {
                    num = Integer.parseInt(name.substring(i + 1)) + 1;
                } catch (NumberFormatException e) {
                    throw new NumberFormatException(parent.toString() + "前缀最后分隔符是_的后面不是数字");
                }
            }
            name = srcName + "_" + num;
            pathFile = new File(parent.toFile(), name + ext);
            firstTime = false;
        }

        return pathFile.toPath();
    }

    /**
     * 获取有效的文件名
     *
     * @param fullName 文件名
     * @return 可用的文件名
     */
    public static String makeValidName(String fullName) {
        //   ----------------去除前后空格----------------
        fullName = fullName.trim();

        String name;
        String ext;
        int extLong;
        //----------------使总长度不大于255个字符（一个汉字是2个字符）----------------
        Matcher matcher = Pattern.compile("(.*)(\\.[^.]+$)").matcher(fullName);
        if (matcher.find()) {
            name = matcher.group(1);
            ext = matcher.group(2);
            extLong = ext.length();
        } else {
            name = fullName;
            ext = "";
            extLong = 0;
        }

        while (getLong(name) > 255 - extLong) {
            name = name.substring(0, name.length() - 1);
        }

        fullName = name + ext;

        //--------------去除不允许存在的字符----------------
        return fullName.replaceAll("[<>/\\\\|:*?\\n]", "");
    }

    /**
     * 返回字符串中字符个数（一个汉字是2个字符）
     *
     * @param txt 字符串
     * @return 字符个数
     */
    public static int getLong(String txt) {
        int txtLen = txt.length();
        return (txt.getBytes().length - txtLen) / 2 + txtLen;
    }

    public static List<Map<Object, Object>> dataToMap(BaseRecorder<?> recorder, Object data) {
//        if (data == null) {
//            data = new ArrayList<>();
//        } else if (!(data instanceof List || data instanceof Map)) {
//            List<Object> list = new ArrayList<>();
//            list.add(data);
//            data = list;
//        }
//
//        if (recorder.before() == null && recorder.after == null) {
//            return data;
//        }
//
//        if (data instanceof List) {
//            List<Collection<Object>> dataList = new ArrayList<>();
//            for (Object o : recorder.before) {
//                if (o == null) {
//                    continue;
//                }
//                if (o instanceof Map) {
//                    dataList.add(((Map) o).values());
//                } else if (o instanceof Collection) {
//                    dataList.add(((Collection) o));
//                } else if (o instanceof String[] || o instanceof Integer[] || o instanceof Double[] || o instanceof Float[] || o instanceof Long[] || o instanceof Byte[] || o instanceof Character[] || o instanceof Boolean[]) {
//                    dataList.add(List.of(o));
//                } else {
//                    dataList.add(Collections.singletonList(o.toString()));
//                }
//            }
//            return dataList;
//        } else if (data instanceof Map) {
//            Map<Object, Object> dataMap = (Map<Object, Object>) data;
//            return dataMap;
//        }

        return new ArrayList<>();
    }

}
