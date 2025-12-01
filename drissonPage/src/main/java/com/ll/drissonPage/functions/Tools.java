package com.ll.drissonPage.functions;

import com.ll.drissonPage.config.OptionsManager;
import com.ll.drissonPage.error.extend.*;
import com.ll.drissonPage.page.ChromiumPage;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 工具
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */

//wait_until
public class Tools {
    /**
     * 检查端口是否被占用
     *
     * @param ip   浏览器地址
     * @param port 浏览器端口
     * @return 是否占用
     */
    public static boolean portIsUsing(String ip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), 100);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void cleanFolder(String folderPath) {
        cleanFolder(folderPath, new String[]{});
    }

    /**
     * 清空一个文件夹，除了 ignore 里的文件和文件夹
     *
     * @param folderPath 要清空的文件夹路径
     * @param ignore     忽略列表
     */
    public static void cleanFolder(String folderPath, String[] ignore) {
        Path path = Paths.get(folderPath);
        try {
            Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!contains(ignore, file.getFileName().toString())) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!contains(ignore, dir.getFileName().toString())) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 执行显示或隐藏浏览器窗口
     *
     * @param page ChromePage对象
     */
    public static void showOrHideBrowser(ChromiumPage page) {
        showOrHideBrowser(page, true);
    }


    public static void showOrHideBrowser(ChromiumPage page, boolean hide) {
        if (!page.getAddress().startsWith("127.0.0.1") && !page.getAddress().startsWith("localhost")) {
            return;
        }

        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            throw new UnsupportedOperationException("该方法只能在Windows系统使用。");
        }

        Integer pids = page.getPage().processId();

        String title = page.title();
        int sw = hide ? User32.SW_HIDE : User32.SW_SHOW;
        List<Long> hwnds = getHwndsFromPid(pids, title);
        for (long hwnd : hwnds) {
            User32.INSTANCE.ShowWindow(new WinDef.HWND(Pointer.createConstant(hwnd)), sw);
        }

    }


    public static Long getBrowserProcessId(String address) {
        return getBrowserProcessId(null, address);
    }

    /**
     * 获取浏览器进程id
     *
     * @param process 已知的进程对象，没有时传入null
     * @param address 浏览器管理地址，含端口
     * @return 进程id
     */
    public static Long getBrowserProcessId(Process process, String address) {
        if (process != null) {
            return process.pid();
        }

        String port = address.split(":")[1];
        String command = "netstat -nao | findstr :" + port;

        try {
            Process netstatProcess = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(netstatProcess.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("LISTENING")) {
                    String[] parts = line.split(" ");
                    return Long.parseLong(parts[parts.length - 1]);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 查询指定程序
     *
     * @param pid   端口
     * @param title 标题
     * @return 程序
     */
    public static List<Long> getHwndsFromPid(int pid, String title) {
        List<Long> hwnds = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, lParam) -> {
            char[] buffer = new char[512];
            User32.INSTANCE.GetWindowText(hWnd, buffer, buffer.length);
            String windowTitle = Native.toString(buffer);

            if (User32.INSTANCE.IsWindow(hWnd) && windowTitle.contains(title)) {
                IntByReference processId = new IntByReference();
                User32.INSTANCE.GetWindowThreadProcessId(hWnd, processId);
                if (processId.getValue() == pid) {
                    hwnds.add(Pointer.nativeValue(hWnd.getPointer()));
                }
            }
            return true;
        }, null);
        return hwnds;
    }

    /**
     * 强制关闭某个端口内的进程
     *
     * @param port 端口号
     */
    public static void stopProcessOnPort(int port) {

        try {
            ProcessHandle.allProcesses().filter(process -> process.info().command().isPresent()).filter(process -> process.info().command().map(s -> s.contains(Integer.toString(port))).orElse(false)).forEach(process -> {
                if (process.isAlive()) process.destroy();
            });
        } catch (SecurityException ignored) {
        }
    }

    private static boolean contains(String[] array, String target) {
        for (String s : array) {
            if (s.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public static void raiseError(Map<String, Object> result, Object ignore) {
        Object o = result.get("error");
        if (o == null) return;
        String error = o.toString();
        if ("Cannot find context with specified id".equals(error) || "Inspected target navigated or closed".equals(error) || "No frame with given id found".equals(error)) {
            throw new ContextLostError();
        } else if ("Could not find node with given id".equals(error) || "Could not find object with given id".equals(error) || "No node with given id found".equals(error) || "Node with given id does not belong to the document".equals(error) || "No node found for given backend id".equals(error)) {
            throw new ElementLostError();
        } else if ("connection disconnected".equals(error) || "No target with given id found".equals(error)) {
            throw new PageDisconnectedError();
        } else if ("alert exists.".equals(error)) {
            throw new AlertExistsError();
        } else if ("Node does not have a layout object".equals(error) || "Could not compute box model.".equals(error)) {
            throw new NoRectError();
        } else if ("Cannot navigate to invalid URL".equals(error)) {
            throw new WrongURLError("无效的url：" + result.get("args.url") + "。也许要加上\"http://\"？");
        } else if ("Frame corresponds to an opaque origin and its storage key cannot be serialized".equals(error)) {
            throw new StorageError();
        } else if ("Sanitizing cookie failed".equals(error)) {
            throw new CookieFormatError("cookie格式不正确：" + result.get("args"));
        } else if ("Given expression does not evaluate to a function".equals(error)) {
            throw new JavaScriptError("传入的js无法解析成函数：\n" + result.get("args.functionDeclaration"));
        } else if ("call_method_error".equals(result.get("type")) || "timeout".equals(result.get("type"))) {
            long processTime = System.currentTimeMillis(); // 这里可能需要替换成其他计时方式
            String txt = "\n错误：" + error + "\nmethod：" + result.get("method") + "\nargs：" + result.get("args") + "\n运行时间：" + processTime + "\n出现这个错误可能意味着程序有bug，请把错误信息和重现方法" + "告知作者，谢谢。\n报告网站：https://gitee.com/g1879/DrissionPage/issues";
            if ("timeout".equals(result.get("type"))) {
                throw new WaitTimeoutError(txt);
            } else {
                throw new CDPError(txt);
            }
        }

        if (ignore == null) {
            // 此处可能需要替换成适当的异常类
            throw new RuntimeException(result.toString());
        }
    }

    /**
     * 将默认的ini文件复制到当前目录
     *
     * @param saveName 指定文件名，为null则命名为'dp_configs.ini'
     * @throws IOException 如果复制过程中发生IO异常
     */
    public static void copyConfigsToCurrentDirectory(String saveName) throws IOException {
        OptionsManager optionsManager = new OptionsManager("default");
        saveName = (saveName != null) ? saveName + ".ini" : "dp_configs.ini";
        optionsManager.save(saveName);
    }

    /**
     * 删除文件夹以及其子目录
     *
     * @param path     删除文件地址
     * @param fileName 删除文件名也可以是文件夹
     * @throws IOException 删除异常
     */
    public static void deleteDirectory(String path, String fileName) throws IOException {
        deleteDirectory(Paths.get(path + "/" + fileName));
    }

    /**
     * 删除文件夹以及其子目录
     *
     * @param path 删除文件地址
     * @throws IOException 删除异常
     */
    public static void deleteDirectory(String path) throws IOException {
        deleteDirectory(Paths.get(path));
    }

    /**
     * 删除文件夹以及其子目录
     *
     * @param path 删除文件地址
     * @throws IOException 删除异常
     */
    public static void deleteDirectory(File path) throws IOException {
        deleteDirectory(Paths.get(path.getAbsolutePath()));
    }

    /**
     * 删除文件夹以及其子目录
     *
     * @param directoryPath 要删除的文件夹路径
     * @throws IOException 删除异常
     */
    public static void deleteDirectory(Path directoryPath) throws IOException {
        if (Files.exists(directoryPath)) try (Stream<Path> walk = Files.walk(directoryPath)) {
            walk.sorted(java.util.Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

        }
    }

  /*  public interface User32 extends Library {
        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean ShowWindow(WinDef.HWND hWnd, int nCmdShow);

        WinDef.HWND FindWindow(String lpClassName, String lpWindowName);

        void GetWindowText(WinDef.HWND hWnd, char[] lpString, int i);

        void GetWindowThreadProcessId(WinDef.HWND hWnd,int[] id);

        boolean EnumWindows(WNDENUMPROC wndenumproc, WinDef.HWND hWnd);

        interface WNDENUMPROC {
            boolean callback(WinDef.HWND hWnd, WinDef.LPARAM lParam);
        }
    }*/


}
