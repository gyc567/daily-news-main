package com.ll.drissonPage.page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ll.drissonPage.base.Browser;
import com.ll.drissonPage.config.ChromiumOptions;
import com.ll.drissonPage.config.PortFinder;
import com.ll.drissonPage.error.extend.BrowserConnectError;
import com.ll.drissonPage.functions.BrowserUtils;
import com.ll.drissonPage.functions.HttpUrlUtils;
import com.ll.drissonPage.units.setter.ChromiumPageSetter;
import com.ll.drissonPage.units.waiter.PageWaiter;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 用于管理浏览器的类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class ChromiumPage extends ChromiumBase {
    private static final Map<String, ChromiumPage> PAGES = new ConcurrentHashMap<>();
    private final String browserId;
    private final boolean isExist;
    @Getter
    private final ChromiumOptions chromiumOptions;
    private final Lock lock;
    /**
     * 返回所控制的浏览器版本号
     */
    private String browserVersion;


    private ChromiumPage(String address, String tabId, Double timeout, Object[] objects) {
        this(handleOptions(address), tabId, timeout, objects);
    }

    private ChromiumPage(Integer address, String tabId, Double timeout, Object[] objects) {
        this("127.0.0.1:" + String.format("%04d", Math.max(address, 1000)), tabId, timeout, objects);
    }

    private ChromiumPage(ChromiumOptions options, String tabId, Double timeout, Object[] objects) {
        this.chromiumOptions = handleOptions(options);
        this.isExist = Boolean.parseBoolean(objects[0].toString());
        this.browserId = objects[1].toString();
        this.setTimeout(timeout);
        this.page = this;
        this.lock = new ReentrantLock();
        this.runBrowser();
        super.init(options.getAddress(), tabId, timeout);
        this.setType("ChromiumPage");
        this.set().timeouts(timeout);
        this.pageInit();
    }

    /**
     * 单例模式
     */
    public static ChromiumPage getInstance() {
        return getInstance("");
    }

    /**
     * 单例模式
     *
     * @param address 浏览器地址
     */
    public static ChromiumPage getInstance(String address) {
        return getInstance("".equals(address) ? null : address, null);
    }

    /**
     * 单例模式
     *
     * @param options 浏览器配置
     */
    public static ChromiumPage getInstance(ChromiumOptions options) {
        return getInstance(options, null);
    }

    /**
     * 单例模式
     *
     * @param address 浏览器地址
     * @param timeout 超时时间（秒）
     */
    public static ChromiumPage getInstance(String address, Double timeout) {
        return getInstance(address, null, timeout);
    }

    /**
     * 单例模式
     *
     * @param port 端口
     */
    public static ChromiumPage getInstance(Integer port) {
        return getInstance(port, null);
    }

    /**
     * 单例模式
     *
     * @param options 浏览器配置
     * @param timeout 超时时间（秒）
     */
    public static ChromiumPage getInstance(ChromiumOptions options, Double timeout) {
        return getInstance(options, null, timeout);
    }

    /**
     * 单例模式
     *
     * @param port    端口
     * @param timeout 超时时间（秒）
     */
    public static ChromiumPage getInstance(Integer port, Double timeout) {
        return getInstance(port, null, timeout);
    }

    /**
     * 单例模式
     *
     * @param address 浏览器地址
     * @param tabId   要控制的标签页id，不指定默认为激活的
     * @param timeout 超时时间（秒）
     */
    public static ChromiumPage getInstance(String address, String tabId, Double timeout) {
        return getInstance(handleOptions(address), tabId, timeout);
    }

    /**
     * 单例模式
     *
     * @param path configs.ini文件路径
     */
    public static ChromiumPage getInstance(Path path) {
        return getInstance(path, null);
    }

    /**
     * 单例模式
     *
     * @param path  configs.ini文件路径
     * @param tabId 要控制的标签页id，不指定默认为激活的
     */
    public static ChromiumPage getInstance(Path path, String tabId) {
        return getInstance(path, tabId, null);
    }

    /**
     * 单例模式
     *
     * @param path    configs.ini文件路径
     * @param tabId   要控制的标签页id，不指定默认为激活的
     * @param timeout 超时时间（秒）
     */
    public static ChromiumPage getInstance(Path path, String tabId, Double timeout) {
        return getInstance(handleOptions(path), tabId, timeout);
    }

    /**
     * 单例模式
     *
     * @param port    端口
     * @param tabId   要控制的标签页id，不指定默认为激活的
     * @param timeout 超时时间（秒）
     */
    public static ChromiumPage getInstance(Integer port, String tabId, Double timeout) {
        Object[] objects = runBrowser(handleOptions(port));
        return PAGES.computeIfAbsent(objects[1].toString(), key -> new ChromiumPage(port, tabId, timeout, objects));
    }

    /**
     * 单例模式
     *
     * @param options 浏览器配置
     * @param tabId   要控制的标签页id，不指定默认为激活的
     * @param timeout 超时时间（秒）
     */
    public static ChromiumPage getInstance(ChromiumOptions options, String tabId, Double timeout) {
        Object[] objects = runBrowser(handleOptions(options));
        return PAGES.computeIfAbsent(objects[1].toString(), key -> new ChromiumPage(options, tabId, timeout, objects));
    }

    /**
     * 设置浏览器启动属性
     *
     * @param port 'port'
     */
    public static ChromiumOptions handleOptions(int port) {
        return new ChromiumOptions().setLocalPort(port);

    }

    /**
     * 设置浏览器启动属性
     *
     * @param options 'ChromiumOptions'
     */
    public static ChromiumOptions handleOptions(ChromiumOptions options) {
        if (options == null) options = new ChromiumOptions();
        else {
            if (options.isAutoPort()) {
                PortFinder.PortInfo address = new PortFinder(options.getTmpPath()).getPort();
                options = options.setAddress("127.0.0.1:" + address.getPort()).setUserDataPath(address.getPath()).autoPort();
            }
        }
        return options;

    }

    /**
     * 设置浏览器启动属性
     *
     * @param path configs.ini路径
     */
    public static ChromiumOptions handleOptions(Path path) {
        return new ChromiumOptions(true, path.toString());

    }

    /**
     * 设置浏览器启动属性
     *
     * @param address 'ip:port'
     */
    public static ChromiumOptions handleOptions(String address) {
        ChromiumOptions options;
        if (address == null || address.isEmpty()) {
            options = new ChromiumOptions();
        } else {
            options = new ChromiumOptions();
            options.setAddress(address);
        }
        return options;

    }

    //----------挂件----------

    public static Object[] runBrowser(ChromiumOptions options) {
        boolean isExist = BrowserUtils.connectBrowser(options);
        String browserId;
        try {
            String ws = HttpUrlUtils.get("http://" + options.getAddress() + "/json/version", Map.of("Connection", "close"));
            if (ws.isEmpty())
                throw new BrowserConnectError("\n浏览器连接失败，如使用全局代理，须设置不代理127.0.0.1地址。");
            String[] split = JSON.parseObject(ws).get("webSocketDebuggerUrl").toString().split("/");
            browserId = split[split.length - 1];
        } catch (NullPointerException e) {
            throw new BrowserConnectError("浏览器版本太旧，请升级。");
        } catch (Exception e) {
            throw new BrowserConnectError("\n浏览器连接失败，如使用全局代理，须设置不代理127.0.0.1地址。");
        }
        return new Object[]{isExist, browserId};
    }

    private void runBrowser() {
        this.setBrowser(Browser.getInstance(this.chromiumOptions.getAddress(), this.browserId, this));
        String r = this.browser().runCdp("Browser.getVersion");
        this.browserVersion = JSON.parseObject(r).getString("product");
        if (this.isExist && !this.chromiumOptions.isHeadless() && JSON.parseObject(this.getBrowser().runCdp("Browser.getVersion")).getString("userAgent").toLowerCase().contains("headless")) {
            this.getBrowser().quit(3);
            BrowserUtils.connectBrowser(this.chromiumOptions);
            JSONObject obj;
            try {
                obj = JSON.parseObject(HttpUrlUtils.get("http://" + this.chromiumOptions.getAddress() + "/json/version", Map.of("Connection", "close")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String[] split = obj.get("webSocketDebuggerUrl").toString().split("/");
            String ws = split[split.length - 1];
            this.setBrowser(Browser.getInstance(this.chromiumOptions.getAddress(), ws, this));
        }
    }


    //----------挂件----------

    @Override
    protected void dSetRuntimeSettings() {
        super.timeouts = new Timeout(this, this.chromiumOptions.getTimeouts().get("base"), this.chromiumOptions.getTimeouts().get("pageLoad"), this.chromiumOptions.getTimeouts().get("script"));
        Double base = this.chromiumOptions.getTimeouts().get("base");
        if (base != null) this.setTimeout(base);
        super.setLoadMode(this.chromiumOptions.getLoadMode());
        this.setDownloadPath(this.chromiumOptions.getDownloadPath() == null ? null : Paths.get(this.chromiumOptions.getDownloadPath()).toFile().getAbsolutePath());

        super.setRetryTimes(this.chromiumOptions.getRetryTimes());
        super.setRetryInterval((double) this.chromiumOptions.getRetryInterval());

    }

    /**
     * 浏览器相关设置
     */
    private void pageInit() {
        this.getBrowser().connectToPage();
    }

    /**
     * @return 返回用于设置的对象
     */
    @Override
    public ChromiumPageSetter set() {
        if (super.set == null) {
            super.set = new ChromiumPageSetter(this);
        }
        return (ChromiumPageSetter) super.set;
    }

    /**
     * @return 返回用于等待的对象
     */
    public PageWaiter waits() {
        if (super.wait == null) this.wait = new PageWaiter(this);
        return (PageWaiter) super.wait;
    }

    /**
     * @return 返回用于控制浏览器cdp的driver
     */
    public Browser browser() {
        return this.getBrowser();
    }

    /**
     * @return 返回标签页数量
     */
    public int tabsCount() {
        return this.getBrowser().tabsCount();
    }

    /**
     * @return 返回所有标签页id组成的列表
     */
    public List<String> tabIds() {
        return this.getBrowser().tabs();
    }

    /**
     * @return 返回最新的标签页对象，最新标签页指最后创建或最后被激活的
     */
    public ChromiumTab latestTab() {
        return this.getTab(this.tabIds().get(0));
    }

    /**
     * @return 返回浏览器进程id
     */

    public Integer processId() {
        return this.getBrowser().getProcessId();
    }

    /**
     * @return 返回所控制的浏览器版本号
     */
    public String browserVersion() {
        return this.browserVersion;
    }

    /**
     * 把当前页面保存为文件，如果path和name参数都为null，只返回文本
     *
     * @param path 保存路径，为null且name不为null时保存在当前路径
     * @param name 文件名，为null且path不为null时用title属性值
     * @return asPdf为True时返回bytes，否则返回文件文本
     */

    public Object save(String path, String name) {
        return save(path, name, false);
    }

    /**
     * 把当前页面保存为文件，如果path和name参数都为null，只返回文本
     *
     * @param path  保存路径，为null且name不为null时保存在当前路径
     * @param name  文件名，为null且path不为null时用title属性值
     * @param asPdf 为Ture保存为pdf，否则为mhtml且忽略params参数
     * @return asPdf为True时返回bytes，否则返回文件文本
     */

    public Object save(String path, String name, boolean asPdf) {
        return save(path, name, asPdf, new HashMap<>());
    }

    /**
     * 把当前页面保存为文件，如果path和name参数都为null，只返回文本
     *
     * @param path   保存路径，为null且name不为null时保存在当前路径
     * @param name   文件名，为null且path不为null时用title属性值
     * @param asPdf  为Ture保存为pdf，否则为mhtml且忽略params参数
     * @param params pdf生成参数
     * @return asPdf为True时返回bytes，否则返回文件文本
     */

    public Object save(String path, String name, boolean asPdf, Map<String, Object> params) {
        return asPdf ? ChromiumBase.getPdf(this, path, name, params) : ChromiumBase.getMHtml(this, path, name);
    }

    /**
     * 获取一个标签页对象，id_or_num不为None时，后面几个参数无效
     *
     * @return {@link  ChromiumTab}对象
     */
    public ChromiumTab getTab() {
        return getTab(null);
    }

    /**
     * 获取一个标签页对象，id_or_num不为None时，后面几个参数无效
     *
     * @param id 要获取的标签页id或序号，序号从1开始，可传入负数获取倒数第几个，不是视觉排列顺序，而是激活顺序
     * @return {@link  ChromiumTab}对象
     */
    public ChromiumTab getTab(String id) {
        return getTab(id, null);
    }

    /**
     * 获取一个标签页对象，id_or_num不为None时，后面几个参数无效
     *
     * @param id    要获取的标签页id或序号，序号从1开始，可传入负数获取倒数第几个，不是视觉排列顺序，而是激活顺序
     * @param title 要匹配title的文本，模糊匹配，为None则匹配所有
     * @return {@link  ChromiumTab}对象
     */
    public ChromiumTab getTab(String id, String title) {
        return getTab(id, title, null);
    }

    /**
     * 获取一个标签页对象，id_or_num不为None时，后面几个参数无效
     *
     * @param id    要获取的标签页id或序号，序号从1开始，可传入负数获取倒数第几个，不是视觉排列顺序，而是激活顺序
     * @param title 要匹配title的文本，模糊匹配，为None则匹配所有
     * @param url   要匹配url的文本，模糊匹配，为None则匹配所有
     * @return {@link  ChromiumTab}对象
     */
    public ChromiumTab getTab(String id, String title, String url) {
        return getTab(id, title, url, null);
    }

    /**
     * 获取一个标签页对象，id_or_num不为None时，后面几个参数无效
     *
     * @param id      要获取的标签页id或序号，序号从1开始，可传入负数获取倒数第几个，不是视觉排列顺序，而是激活顺序
     * @param title   要匹配title的文本，模糊匹配，为None则匹配所有
     * @param url     要匹配url的文本，模糊匹配，为None则匹配所有
     * @param tabType tab类型，可用列表输入多个，如 'page', 'iframe' 等，为None则匹配所有
     * @return {@link  ChromiumTab}对象
     */
    public ChromiumTab getTab(String id, String title, String url, List<String> tabType) {
        return getTab(id, title, url, tabType, true);
    }

    /**
     * 获取一个标签页对象，id_or_num不为None时，后面几个参数无效
     *
     * @param num 要获取的标签页id或序号，序号从1开始，可传入负数获取倒数第几个，不是视觉排列顺序，而是激活顺序
     * @return {@link  ChromiumTab}对象
     */
    public ChromiumTab getTab(int num) {
        return getTab(num, null);
    }

    /**
     * 获取一个标签页对象，id_or_num不为None时，后面几个参数无效
     *
     * @param num   要获取的标签页id或序号，序号从1开始，可传入负数获取倒数第几个，不是视觉排列顺序，而是激活顺序
     * @param title 要匹配title的文本，模糊匹配，为None则匹配所有
     * @return {@link  ChromiumTab}对象
     */
    public ChromiumTab getTab(int num, String title) {
        return getTab(num, title, null);
    }

    /**
     * 获取一个标签页对象，id_or_num不为None时，后面几个参数无效
     *
     * @param num   要获取的标签页id或序号，序号从1开始，可传入负数获取倒数第几个，不是视觉排列顺序，而是激活顺序
     * @param title 要匹配title的文本，模糊匹配，为None则匹配所有
     * @param url   要匹配url的文本，模糊匹配，为None则匹配所有
     * @return {@link  ChromiumTab}对象
     */
    public ChromiumTab getTab(int num, String title, String url) {
        return getTab(num, title, url, null);
    }

    /**
     * 获取一个标签页对象，id_or_num不为None时，后面几个参数无效
     *
     * @param num     要获取的标签页id或序号，序号从1开始，可传入负数获取倒数第几个，不是视觉排列顺序，而是激活顺序
     * @param title   要匹配title的文本，模糊匹配，为None则匹配所有
     * @param url     要匹配url的文本，模糊匹配，为None则匹配所有
     * @param tabType tab类型，可用列表输入多个，如 'page', 'iframe' 等，为None则匹配所有
     * @return {@link  ChromiumTab}对象
     */
    public ChromiumTab getTab(int num, String title, String url, List<String> tabType) {
        return getTab(num, title, url, tabType, false);
    }

    /**
     * 获取一个标签页对象，id_or_num不为None时，后面几个参数无效
     *
     * @param idOrNum 要获取的标签页id或序号，序号从1开始，可传入负数获取倒数第几个，不是视觉排列顺序，而是激活顺序
     * @param title   要匹配title的文本，模糊匹配，为None则匹配所有
     * @param url     要匹配url的文本，模糊匹配，为None则匹配所有
     * @param tabType tab类型，可用列表输入多个，如 'page', 'iframe' 等，为None则匹配所有
     * @return {@link  ChromiumTab}对象
     */
    public ChromiumTab getTab(Object idOrNum, String title, String url, List<String> tabType, boolean ignored) {
        String tabId = null;
        if (idOrNum != null) {
            if (idOrNum instanceof String) {
                tabId = (String) idOrNum;
            } else if (idOrNum instanceof Integer) {
                tabId = this.tabIds().get((Integer) idOrNum > 0 ? (Integer) idOrNum - 1 : (Integer) idOrNum);
            } else if (idOrNum instanceof ChromiumTab) {
                return (ChromiumTab) idOrNum;
            }
        } else if (title == null && url == null && (tabType == null || tabType.isEmpty())) {
            tabId = this.tabId();
        } else {
            List<Map<String, Object>> tabs = this.browser().findTabs(title, url, tabType);
            if (tabs != null) tabId = tabs.get(0).get("id").toString();
            else return null;
        }
        this.lock.lock();
        try {
            return ChromiumTab.getInstance(this, tabId);
        } finally {
            this.lock.unlock();
        }

    }


    /**
     * 查找符合条件的tab，返回它们的id组成的列表
     *
     * @return tab id或tab列表
     */
    public List<Map<String, Object>> getTabsStr() {
        return getTabsStr(null);
    }

    /**
     * 查找符合条件的tab，返回它们的id组成的列表
     *
     * @param title 要匹配title的文本
     * @return tab id或tab列表
     */
    public List<Map<String, Object>> getTabsStr(String title) {
        return getTabsStr(title, null);
    }

    /**
     * 查找符合条件的tab，返回它们的id组成的列表
     *
     * @param title 要匹配title的文本
     * @param url   要匹配url的文本
     * @return tab id或tab列表
     */
    public List<Map<String, Object>> getTabsStr(String title, String url) {
        return getTabsStr(title, url, null);
    }

    /**
     * 查找符合条件的tab，返回它们的id组成的列表
     *
     * @param title   要匹配title的文本
     * @param url     要匹配url的文本
     * @param tabType tab类型，可用列表输入多个
     * @return tab id或tab列表
     */
    public List<Map<String, Object>> getTabsStr(String title, String url, List<String> tabType) {
        return this.getBrowser().findTabs(title, url, tabType);
    }


    /**
     * 查找符合条件的tab，返回它们的id组成的列表
     *
     * @return tab id或tab列表
     */
    public List<ChromiumTab> getTabsTab() {
        return getTabsTab(null);
    }

    /**
     * 查找符合条件的tab，返回它们的id组成的列表
     *
     * @param title 要匹配title的文本
     * @return tab id或tab列表
     */
    public List<ChromiumTab> getTabsTab(String title) {
        return getTabsTab(title, null);
    }

    /**
     * 查找符合条件的tab，返回它们的id组成的列表
     *
     * @param title 要匹配title的文本
     * @param url   要匹配url的文本
     * @return tab id或tab列表
     */
    public List<ChromiumTab> getTabsTab(String title, String url) {
        return getTabsTab(title, url, null);
    }

    /**
     * 查找符合条件的tab，返回它们的id组成的列表
     *
     * @param title   要匹配title的文本
     * @param url     要匹配url的文本
     * @param tabType tab类型，可用列表输入多个
     * @return tab id或tab列表
     */
    public List<ChromiumTab> getTabsTab(String title, String url, List<String> tabType) {
        this.lock.lock();
        try {
            return this.getBrowser().findTabs(title, url, tabType).stream().map(tab -> new ChromiumTab(this, tab.get("id").toString())).collect(Collectors.toList());
        } finally {
            this.lock.unlock();
        }

    }

    /**
     * 新建一个标签页
     *
     * @param url 新标签页跳转到的网址
     * @return 新标签页对象
     */
    public ChromiumTab newTab(String url) {
        return newTab(url, false);
    }

    /**
     * 新建一个标签页
     *
     * @param url       新标签页跳转到的网址
     * @param newWindow 是否在新窗口打开标签页
     * @return 新标签页对象
     */
    public ChromiumTab newTab(String url, boolean newWindow) {
        return newTab(url, newWindow, false);
    }

    /**
     * 新建一个标签页
     *
     * @param url        新标签页跳转到的网址
     * @param newWindow  是否在新窗口打开标签页
     * @param background 是否不激活新标签页，如new_window为True则无效
     * @return 新标签页对象
     */
    public ChromiumTab newTab(String url, boolean newWindow, boolean background) {
        return newTab(url, newWindow, background, false);
    }

    /**
     * 新建一个标签页
     *
     * @param url        新标签页跳转到的网址
     * @param newWindow  是否在新窗口打开标签页
     * @param background 是否不激活新标签页，如new_window为True则无效
     * @param newContext 是否创建新的上下文
     * @return 新标签页对象
     */
    public ChromiumTab newTab(String url, boolean newWindow, boolean background, boolean newContext) {
        ChromiumTab chromiumTab = ChromiumTab.getInstance(this, this._newTab(newWindow, background, newContext));
        if (url != null && !url.isEmpty()) chromiumTab.get(url);
        return chromiumTab;
    }

    /**
     * 新建一个标签页
     *
     * @param newWindow  是否在新窗口打开标签页
     * @param background 是否不激活新标签页，如new_window为True则无效
     * @param newContext 是否创建新的上下文
     * @return 新标签页对象
     */
    protected String _newTab(boolean newWindow, boolean background, boolean newContext) {
        Object bid = null;
        if (newContext)
            bid = JSON.parseObject(this.getBrowser().runCdp("Target.createBrowserContext")).get("browserContextId");
        Map<String, Object> params = new HashMap<>();
        params.put("url", "");
        if (newWindow) params.put("newWindow", true);
        if (background) params.put("background", true);
        if (bid != null) params.put("browserContextId", bid);
        return JSON.parseObject(this.getBrowser().runCdp("Target.createTarget", params)).get("targetId").toString();
    }

    /**
     * 关闭Page管理的标签页
     */
    public void close() {
        this.quit();
    }

    /**
     * 关闭传入的标签页，默认关闭当前页。可传入多个
     */
    public void closeTabs() {
        closeTabs(new String[]{});
    }

    /**
     * 关闭传入的标签页，默认关闭当前页。可传入多个
     *
     * @param ids 要关闭的标签页对象或id，可传入列表或元组，为None时关闭当前页
     */
    public void closeTabs(String[] ids) {
        closeTabs(ids, false);
    }

    /**
     * 关闭传入的标签页，默认关闭当前页。可传入多个
     *
     * @param ids 要关闭的标签页对象或id，可传入列表或元组，为None时关闭当前页
     */
    public void closeTabs(String ids) {
        closeTabs(ids, false);
    }

    /**
     * 关闭传入的标签页，默认关闭当前页。可传入多个
     *
     * @param ids    要关闭的标签页对象或id，可传入列表或元组，为None时关闭当前页
     * @param others 是否关闭指定标签页之外的
     */
    public void closeTabs(String[] ids, boolean others) {
        if (ids.length == 0) ids = new String[]{this.tabId()};
        List<String> tabs = Arrays.asList(ids);
        closeTabs(others, tabs);

    }

    /**
     * 关闭传入的标签页，默认关闭当前页。可传入多个
     *
     * @param ids    要关闭的标签页对象或id，可传入列表或元组，为None时关闭当前页
     * @param others 是否关闭指定标签页之外的
     */
    public void closeTabs(String ids, boolean others) {
        if (ids == null) ids = this.tabId();
        List<String> tabs = Collections.singletonList(ids);
        closeTabs(others, tabs);
    }

    /**
     * 关闭传入的标签页，默认关闭当前页。可传入多个
     *
     * @param chromiumTabs 要关闭的标签页对象或id，可传入列表或元组，为None时关闭当前页
     */
    public void closeTabs(ChromiumTab[] chromiumTabs) {
        closeTabs(chromiumTabs, false);

    }

    /**
     * 关闭传入的标签页，默认关闭当前页。可传入多个
     *
     * @param chromiumTab 要关闭的标签页对象或id，可传入列表或元组，为None时关闭当前页
     */
    public void closeTabs(ChromiumTab chromiumTab) {
        closeTabs(chromiumTab, false);
    }

    /**
     * 关闭传入的标签页，默认关闭当前页。可传入多个
     *
     * @param chromiumTabs 要关闭的标签页对象或id，可传入列表或元组，为None时关闭当前页
     * @param others       是否关闭指定标签页之外的
     */
    public void closeTabs(ChromiumTab[] chromiumTabs, boolean others) {
        List<String> tabs = new ArrayList<>();
        if (chromiumTabs.length == 0) tabs.add(this.tabId());
        for (ChromiumTab chromiumTab : chromiumTabs) tabs.add(chromiumTab.tabId());

        closeTabs(others, tabs);
    }

    /**
     * 关闭传入的标签页，默认关闭当前页。可传入多个
     *
     * @param chromiumTab 要关闭的标签页对象或id，可传入列表或元组，为None时关闭当前页
     * @param others      是否关闭指定标签页之外的
     */
    public void closeTabs(ChromiumTab chromiumTab, boolean others) {
        List<String> tabs = new ArrayList<>();
        tabs.add((chromiumTab == null ? this : chromiumTab).tabId());
        closeTabs(others, tabs);

    }

    /**
     * 关闭传入的标签页，默认关闭当前页。可传入多个
     *
     * @param others 要关闭的标签页对象或id，可传入列表或元组，为None时关闭当前页
     * @param tabs   是否关闭指定标签页之外的
     */
    protected void closeTabs(boolean others, List<String> tabs) {
        List<String> allTabs = this.tabIds();
        int allSize = allTabs.size();
        if (others) {
            allTabs.removeAll(tabs);
            tabs = allTabs;
        }


        int endLen = allSize - tabs.size();
        if (endLen <= 0) {
            if (this.listener != null) this.listener.stop();
            this.quit();
            return;
        }
        for (String id : tabs) {
            this.getBrowser().closeTab(id);
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        long endTime = System.currentTimeMillis() + 3000;
        while (this.tabsCount() != endLen && endTime > System.currentTimeMillis()) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 关闭浏览器
     */
    public void quit() {
        this.quit(5.0);
    }

    /**
     * 关闭浏览器
     *
     * @param timeout 等待浏览器关闭超时时间（秒）
     */
    public void quit(double timeout) {
        this.quit(timeout, true);
    }

    /**
     * 关闭浏览器
     *
     * @param timeout 等待浏览器关闭超时时间（秒）
     * @param force   关闭超时是否强制终止进程
     */
    public void quit(double timeout, boolean force) {
        this.getBrowser().quit(timeout, force);
    }


    /**
     * 克隆新的浏览器
     *
     * @param cloneNumber 克隆数量
     * @return 集合
     */
    public List<ChromiumPage> copy(int cloneNumber) {
        return IntStream.range(0, cloneNumber < 0 ? 1 : cloneNumber).mapToObj(i -> copy()).collect(Collectors.toList());
    }

    /**
     * 克隆新的浏览器
     *
     * @return 单个
     */
    public ChromiumPage copy() {
        ChromiumOptions chromiumOptions1 = this.chromiumOptions.copy();
        chromiumOptions1.autoPort(true, chromiumOptions1.getTmpPath() + UUID.randomUUID().toString().substring(0, 5));
        ChromiumPage instance = ChromiumPage.getInstance(chromiumOptions1);
        String url1 = this.url();
        if (url1 != null) instance.get(url1);
        return instance;
    }

    @Override
    public void onDisconnect() {
        ChromiumPage.PAGES.remove(this.browserId);
    }

    @Override
    public String toString() {
        return "<ChromiumPage browser_id=" + this.getBrowser().getId() + "tab_id=" + this.tabId() + '>';
    }


}
