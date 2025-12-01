package A04FindEle;

import com.ll.drissonPage.element.SessionElement;
import com.ll.drissonPage.page.SessionPage;
import org.junit.Test;

import java.util.List;

/**
 * 概述
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class A01FindEleTest {
    /**
     * 在页面中查找
     */
    @Test
    public void findEle01() {
        SessionPage sessionPage = new SessionPage();
        sessionPage.get("https://www.baidu.com");
        SessionElement ele = sessionPage.ele("#kw");
        System.out.println(ele.tag());
    }

    /**
     * 在元素中查找 +链式
     */
    @Test
    public void findEle02() {
        SessionPage page = new SessionPage();
        List<SessionElement> children = page.ele("#xxxx").ele("#xxx").child("tag:div").children("tag:div");
    }

    /**
     * 实际示例
     */
    @Test
    public void findEle03() {
        SessionPage page = new SessionPage();
        page.get("https://gitee.com/explore");
        SessionElement ele = page.ele("tag:ul@@text():全部推荐项目");
        List<SessionElement> sessionElements = ele.eles("tag:a");
        sessionElements.stream().map(SessionElement::text).forEach(System.out::println);
    }
}
