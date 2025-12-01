package A04FindEle;

import com.ll.drissonPage.element.ChromiumElement;
import com.ll.drissonPage.element.SessionElement;
import com.ll.drissonPage.page.ChromiumPage;
import com.ll.drissonPage.page.SessionPage;
import org.junit.Test;

import java.io.IOException;

/**
 * 基本用法
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class A02BaseTest {
    /**
     * 查找元素方法
     */
    @Test
    public void test01() {
        SessionPage page = new SessionPage();
        //页面内查找元素
        SessionElement ele1 = page.ele("//one");
        //在元素内查找后代元素
        SessionElement ele2 = ele1.ele("第二行");

    }

    @Test
    public void test02() throws IOException {
//        ChromiumOptions options = new ChromiumOptions();
//        options.addExtension("D:\\idea_java\\DrissonPage\\插件\\SwitchyOmega.crx");
        ChromiumPage page = ChromiumPage.getInstance();
        page.set();
        // 以s模式创建页面对象
        page.get("https://book.douban.com/tag/小说?start=0&type=T");

        for (int i = 0; i < 4; i++) {
            for (ChromiumElement ele : page.eles(".subject-item")) {
                long start = System.currentTimeMillis();
                ChromiumElement eles = ele.ele("t:img");
                eles.save("./imgs");
                long end = System.currentTimeMillis();
                System.out.println(end - start);
            }
            page.ele(".next").click().click();
        }


        // 用相对定位获取当前div元素后一个兄弟元素，并获取其文本


        // 在div元素的style属性中提取图片网址并进行拼接


    }
}
