package A01chromePage;

import com.ll.drissonPage.config.ChromiumOptions;
import com.ll.drissonPage.page.ChromiumPage;
import com.ll.drissonPage.page.WebMode;
import com.ll.drissonPage.page.WebPage;
import com.ll.drissonPage.units.Coordinate;
import okhttp3.Cookie;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 页面交互
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */

public class A04PageInteractionTest {

    /**
     * 跳转
     */
    @Test
    public void jump() {
        ChromiumPage page = ChromiumPage.getInstance();
        //页面跳转
        page.get("https://www.baidu.com");
        page.get("http://www.hao123.com/");
        //后退
        page.back();
        //前进
        page.forward();
        //刷新
        System.out.println(page.refresh());
        //停止加载
        page.stopLoading();
    }

    /**
     * 缓存
     */
    @Test
    public void cookie() {
        ChromiumPage page = ChromiumPage.getInstance();
        page.get("https://www.baidu.com");
        //清空
        page.set().cookies().clear();
        //添加
        page.set().cookies().add(new Cookie.Builder().name("666").value("666").domain("aaa").build());
        //清空
        page.cookies(true, true).forEach(System.out::println);
        ///添加
//        page.set().cookies().add(new Cookie.Builder().name("666").value("666").domain("aaa").build());
//        page.cookies(true,true).forEach(System.out::println);
        //删除一个
        System.out.println("-----");
        page.set().cookies().remove("666", null, "aaa");
        page.cookies(true, true).forEach(System.out::println);

        page.set().sessionStorage("aa", 123);
        System.out.println("page.sessionStorage() = " + page.sessionStorage());
        page.set().sessionStorage("aa", null);

        System.out.println("page.sessionStorage() = " + page.sessionStorage());
        page.set().sessionStorage("aa", 12313);

        page.clearCache(true, false);
        System.out.println("page.sessionStorage() = " + page.sessionStorage());
    }

    /**
     * 运行参数设置
     */
    @Test
    public void runParamsTest() {
        ChromiumPage page = ChromiumPage.getInstance();
        page.get("https://www.baidu.com");
        //此方法用于设置连接失败时重连次数。
        page.set().retryTimes(1);
        //此方法用于设置连接失败时重连次数。
        page.set().retryInterval(1.0);
        //此方法用于设置三种超时时间，单位为秒。可单独设置，为None表示不改变原来设置。
        page.set().timeouts(10.0, 20.0, 30.0);
        //此属性用于设置页面加载策略，调用其方法选择某种策略。
        page.set().loadMode().normal();
        page.set().loadMode().eager();
        page.set().loadMode().none();
        //设置ua
        page.set().userAgent("xxx", "windows 11");
        page.set().headers(Map.of("a", "b"));
        page.set().headers("a: b\nc: d");
        System.out.println(page.ua());
    }

    /**
     * 窗口管理
     */
    @Test
    public void window() throws InterruptedException {
        ChromiumOptions options = new ChromiumOptions();
        options.setProxy(ChromiumOptions.ProxyType.http, "127.0.0.1:8888");
        ChromiumPage page = ChromiumPage.getInstance(options);
        WebPage page1 = new WebPage(WebMode.s, new ChromiumOptions());
        page1.close();
        //此方法用于使窗口最大化。
        page.set().window().max();
        //此方法用于使窗口最小化。
        page.set().window().min();
        //此方法用于使窗口切换到全屏模式。
        page.set().window().full();
        //此方法用于使窗口切换到普通模式。
        page.set().window().normal();
        //隐藏
        page.set().window().hide();
        //显示
        page.set().window().show();
        TimeUnit.SECONDS.sleep(1);

    }

    /**
     * 页面滚动
     */
    @Test
    public void pageScroll() throws InterruptedException {
        ChromiumPage page = ChromiumPage.getInstance();
        page.set().scroll().smooth(false);

        page.get("https://www.hao123.com/");
        page.scroll().toBottom();
        TimeUnit.SECONDS.sleep(1);
        page.scroll().toTop();
        TimeUnit.SECONDS.sleep(1);
        page.scroll().toHalf();
        page.set().autoHandleAlert();
        page.scroll().toRightmost();
        page.scroll().toLeftmost();
        page.actions().scroll(new Coordinate(60, 0));
    }
}
