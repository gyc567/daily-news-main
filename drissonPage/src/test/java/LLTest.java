import com.ll.drissonPage.page.ChromiumPage;
import org.junit.Test;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class LLTest {
    @Test
    public void test01() {
        for (int i = 0; i < 100; i++) {
            try (ChromiumPage chromiumPage = ChromiumPage.getInstance()) {
                chromiumPage.get("https://www.baidu.com/");
                chromiumPage.ele("#kw").input(6);
                chromiumPage.ele("x:#su").click().click();
            }

        }

    }
}
