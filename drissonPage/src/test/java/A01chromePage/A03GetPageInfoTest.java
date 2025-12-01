package A01chromePage;

import com.ll.drissonPage.page.ChromiumPage;
import org.junit.Test;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class A03GetPageInfoTest {

    /**
     * 页面信息
     */
    @Test
    public void pageInfo() {
        ChromiumPage page = ChromiumPage.getInstance();
        page.get("https://www.baidu.com");
        //页面信息
        System.out.println(page.html());
        //解析成json
        System.out.println(page.json());
        //标题
        System.out.println(page.title());
        //userAgent
        System.out.println(page.userAgent());
        System.out.println(page.ua());
        //保存
        page.save(".", "text.html");
        // 运行状态信息
    }

    /**
     * 运行状态
     */
    @Test
    public void runState() {
        ChromiumPage page = ChromiumPage.getInstance();
        //  此属性返回当前访问的 url。
        System.out.println(page.url());
        //地址+端口
        System.out.println(page.getAddress());
        //标签id
        System.out.println(page.tabId());
        //进程id
        System.out.println(page.processId());
        //此属性返回页面是否正在加载状态。
        System.out.println("page.states().isLoading() = " + page.states().isLoading());
        //此属性返回页面是否仍然可用，标签页已关闭则返回False。
        System.out.println("page.states().isAlive() = " + page.states().isAlive());
        //页面当前加载状态
        System.out.println("page.states().readyState() = " + page.states().readyState());
        //此属性以布尔值返回当前链接是否可用。
        System.out.println("page.urlAvailable() = " + page.urlAvailable());
        //此属性以布尔值返回页面是否存在弹出框。
        System.out.println("page.states().hasAlert() = " + page.states().hasAlert());
    }

    /**
     * 窗口信息
     */
    @Test
    public void windowsInfo() {
        ChromiumPage page = ChromiumPage.getInstance();
        //返回页面大小
        System.out.println("page.rect().size() = " + page.rect().size());
        //窗口大小
        System.out.println("page.rect().windowSize() = " + page.rect().windowSize());
        //此属性以返回窗口当前状态，有'normal'、'fullscreen'、'maximized'、 'minimized'几种。
        System.out.println("page.rect().windowState() = " + page.rect().windowState());
        //此属性以tuple返回视口大小，不含滚动条，格式：(宽, 高)。
        System.out.println("page.rect().viewportSize() = " + page.rect().viewportSize());
        //此属性以tuple返回浏览器窗口大小，含滚动条，格式：(宽, 高)。
        System.out.println("page.rect().viewportSizeWithScrollbar() = " + page.rect().viewportSizeWithScrollbar());
        //此属性以tuple返回页面左上角在屏幕中坐标，左上角为(0, 0)。
        System.out.println("page.rect().pageLocation() = " + page.rect().pageLocation());
        //此属性以tuple返回视口在屏幕中坐标，左上角为(0, 0)。
        System.out.println("page.rect().viewportLocation() = " + page.rect().viewportLocation());
    }

    /**
     * 配置参数信息
     */
    @Test
    public void optionInfo() {
        ChromiumPage page = ChromiumPage.getInstance();
        //此属性为整体默认超时时间，包括元素查找、点击、处理提示框、列表选择等需要用到超时设置的地方，都以这个数据为默认值。
        //默认为 10，可对其赋值。
        page.timeout = 10.0;
        //此属性以字典方式返回三种超时时间。
        //
        //'base'：与timeout属性是同一个值
        //'page_load'：用于等待页面加载
        //'script'：用于等待脚本执行
        System.out.println("page.getTimeouts() = " + page.getTimeouts());
        //此属性为网络连接失败时的重试次数。默认为 3，可对其赋值。
        page.retryTimes = 5;
        //此属性为网络连接失败时的重试等待间隔秒数。默认为 2，可对其赋值。
        page.retryInterval = 1.5;
        //此属性返回页面加载策略，有 3 种：
        //
        //'normal'：等待页面所有资源完成加载
        //'eager'：DOM 加载完成即停止
        //'none'：页面完成连接即停止
        System.out.println("page.loadMode() = " + page.loadMode());
    }

    /**
     *  cookies 和缓存信息
     */
    @Test
    public void cookie() {
        ChromiumPage page = ChromiumPage.getInstance();
        page.get("https://www.baidu.com");
        //此方法返回 cookies 信息。
        System.out.println("page.cookies() = " + page.cookies());
        //此方法用于获取 sessionStorage 信息，可获取全部或单个项。
        System.out.println("page.sessionStorage() = " + page.sessionStorage());
        //此方法用于获取 localStorage 信息，可获取全部或单个项。
        System.out.println("page.localStorage() = " + page.localStorage());
    }
}
