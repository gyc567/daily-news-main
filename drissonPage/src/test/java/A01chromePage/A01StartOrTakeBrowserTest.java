package A01chromePage;

import com.ll.drissonPage.page.ChromiumPage;
import org.junit.Test;

import java.nio.file.Path;

/**
 * 启动或接管浏览器
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class A01StartOrTakeBrowserTest {
    /**
     * 启动浏览器默认端口是9223
     */
    @Test
    public void start() {
        ChromiumPage page = ChromiumPage.getInstance();
    }

    /**
     * 指定端口或地址
     */
    @Test

    public void portOrAddress() {
//        ChromiumPage.getInstance(9223);
//        ChromiumPage.getInstance("127.0.0.1:9223");
        ChromiumPage.getInstance("localhost:9223");
    }

    /**
     * 指定ini文件创建
     */
    @Test
    public void ini() {
        ChromiumPage.getInstance(Path.of("./configs.ini"));
    }
}
