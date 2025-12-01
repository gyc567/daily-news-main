import com.ll.dataRecorder.Recorder;
import com.ll.drissonPage.config.ChromiumOptions;
import com.ll.drissonPage.element.ChromiumElement;
import com.ll.drissonPage.page.ChromiumPage;
import com.ll.drissonPage.units.listener.DataPacket;
import org.junit.Test;

import java.util.List;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class MostTest {

    /**
     * 登录gitee
     */
    @Test
    public void giteeLogin() {
        int ok = 0;
        int error = 0;
        for (int i = 0; i < 500; i++) {
            System.out.println("运行次数为" + i);
            System.out.println("存活线程数量为" + Thread.activeCount());
            ChromiumPage page = ChromiumPage.getInstance();
            try {
                //跳转到登录页面
                page.get("https://www.baidu.com");
                //定位到账号文本框，获取文本框元素
                ChromiumElement ele = page.ele("#kw");
                //输入对文本框输入账号
                ele.input("123456");
                page.ele("#su").click().click();
                ok++;
            } catch (Exception e) {
                error++;
            } finally {
                page.close();
            }

        }

        System.out.println("ok = " + ok);
        System.out.println("error = " + error);


        ChromiumPage page = ChromiumPage.getInstance();
        page.listen().start();
        page.get("httsp://xxxx");

        page.listen().stop();
        List<DataPacket> waits = page.listen().waits();


        //创建页面对象，并启动或接管浏览器

    }

    @Test
    public void test1() {
        byte[] bytes = new byte[15];
        for (byte aByte : bytes) {
            System.out.println(aByte);
        }
        System.out.println(bytes.length);
        ChromiumPage page = ChromiumPage.getInstance(new ChromiumOptions().setPaths("C:\\Users\\11509\\AppData\\Local\\VirtualBrowser\\Application\\VirtualBrowser.exe"));
        //打开创建
        page.ele("x://*[@id=\"app\"]/div/div[2]/section/div/div[1]/div[1]/button[1]/span").click().click(true);
        //设置配置
        System.out.println(page.html());
        new Recorder("");
    }

    @Test
    public void test2() {
//        ChromiumPage page = ChromiumPage.getInstance();
//        page.get("https://mp.weixin.qq.com/s?__biz=MzU4NTA0NzMyOQ==&mid=2247842833&idx=1&sn=50d763e51a4a7115a8aeee57cd38eb53&chksm=fd9f705fcae8f949813d4a810b7f2f17989e82c9f548698adfd308ef5eef9f592a1d476e79b6#rd");
//        Integer y = page.rect().viewportSize().getY();
//        Integer y1 = ;
//        while (page.rect().size().getY()-100>){
//
//        }
//        page.actions().move(new Coordinate(0,50));
    }
}
