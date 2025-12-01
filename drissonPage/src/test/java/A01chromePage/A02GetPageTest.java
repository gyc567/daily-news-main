package A01chromePage;

import com.ll.drissonPage.config.ChromiumOptions;
import com.ll.drissonPage.element.ChromiumElement;
import com.ll.drissonPage.page.ChromiumPage;
import com.ll.drissonPage.units.Coordinate;
import com.ll.drissonPage.units.listener.DataPacket;
import org.junit.Test;

import java.util.List;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class A02GetPageTest {
    /**
     * 跳转
     */
    @Test
    public void get() {
        ChromiumPage.getInstance().get("https://www.baidu.com");
    }

    /**
     * 设置超时和重试
     */
    @Test
    public void waits() {
        ChromiumPage instance = ChromiumPage.getInstance();
        instance.set().loadMode().eager(); //设置为eager模式
        instance.get("https://www.baidu.com");
    }


    /**
     * 模式设置
     */
    @Test
    public void mode() {
        //运行前设置
        ChromiumPage.getInstance(new ChromiumOptions().setLoadMode("none"));
        //运行时设置
        ChromiumPage.getInstance().set().loadMode().none();
    }

    /**
     * none模式技巧
     */
    @Test
    public void none1() {
        for (int i = 0; i < 1; i++) {
            ChromiumPage page = ChromiumPage.getInstance();
//            page.set().loadMode().none();
            page.listen().start("ajax/statuses/mentions");
            page.get("https://weibo.com/at/weibo");  // 访问网站
            List<DataPacket> waits = page.listen().waits();
            System.out.println("===ok===");
            waits.forEach((w) -> {
                System.out.println(w.url());
                System.out.println(w.request().headers());
                System.out.println(w.response().body());
            });//打印正文
            page.stopLoading();  // 主动停止加载
//            page.close();
        }
    }


    /**
     * none模式技巧
     */
    @Test
    public void none2() {
        ChromiumPage page = ChromiumPage.getInstance();
        page.set().loadMode().none();
        page.get("http://www.hao123.com/");  // 访问网站
        ChromiumElement ele = page.ele("中国日报");//查找text包含“中国日报”的元素
        page.stopLoading();//主动停止加载
        System.out.println(ele.text());
        System.out.println(ele.rawText());
    }


    /**
     * none模式技巧
     */
    @Test
    public void none3() {
        ChromiumPage page = ChromiumPage.getInstance();
        page.set().loadMode().none();
        page.get("http://www.hao123.com/");  // 访问网站
        page.waits().titleChange("hao123");
        page.stopLoading();
    }

    @Test
    public void test3() {
        ChromiumPage page = ChromiumPage.getInstance();
        page.set().loadMode().eager();
        page.get("https://www.jd.com/");
        page.scroll().toBottom();
        page.actions().scroll(new Coordinate(0, 100));
        page.scroll().toBottom();
    }
}
