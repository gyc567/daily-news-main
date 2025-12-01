package com.ll.drissonPage.units.waiter;

import com.ll.drissonPage.error.extend.WaitTimeoutError;
import com.ll.drissonPage.functions.Settings;
import com.ll.drissonPage.page.ChromiumPage;
import com.ll.drissonPage.page.ChromiumTab;
import com.ll.drissonPage.units.downloader.DownloadMission;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class PageWaiter extends TabWaiter {
    public PageWaiter(ChromiumPage page) {
        super(page);
    }

    /**
     * @return 等到新标签页返回其标签，否则返回null
     */
    public ChromiumTab newTab() {
        return newTab(null);
    }

    /**
     * @param timeout 等待超时时间，为Null则使用页面对象timeout属性
     * @return 等到新标签页返回其标签，否则返回null
     */
    public ChromiumTab newTab(Double timeout) {
        return newTab(timeout, null);
    }

    /**
     * @param timeout  等待超时时间，为Null则使用页面对象timeout属性
     * @param raiseErr 等待失败时是否报错，为Null时根据Settings设置
     * @return 等到新标签页返回其标签，否则返回null
     */
    public ChromiumTab newTab(Double timeout, Boolean raiseErr) {
        timeout = timeout == null ? this.driver.timeout() : timeout;
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        while (System.currentTimeMillis() < endTime) {
            ChromiumTab s = ((ChromiumPage) (this.driver)).latestTab();
            if (!Objects.equals(this.driver.tabId(), s.tabId())) return s;
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (raiseErr != null && raiseErr || Settings.raiseWhenWaitFailed)
            throw new WaitTimeoutError("等待新标签页失败（等待" + timeout + "秒");
        return null;
    }

    /**
     * 等待所有浏览器下载任务结束
     *
     * @return 是否等待成功
     */
    public boolean allDownloadsDone() {
        return allDownloadsDone(null);
    }

    /**
     * 等待所有浏览器下载任务结束
     *
     * @param timeout 超时时间，为null时无限等待
     * @return 是否等待成功
     */
    public boolean allDownloadsDone(Double timeout) {
        return allDownloadsDone(timeout, true);
    }

    /**
     * 等待所有浏览器下载任务结束
     *
     * @param timeout         超时时间，为null时无限等待
     * @param cancelIfTimeout 超时时是否取消剩余任务
     * @return 是否等待成功
     */
    public boolean allDownloadsDone(Double timeout, boolean cancelIfTimeout) {
        if (timeout == null) {
            while (!this.driver.browser().getDlMgr().getMissions().isEmpty()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        } else {
            long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
            while (System.currentTimeMillis() < endTime) {
                if (this.driver.browser().getDlMgr().getMissions().isEmpty()) {
                    return true;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (!this.driver.browser().getDlMgr().getMissions().isEmpty()) {
                if (cancelIfTimeout)
                    this.driver.browser().getDlMgr().getMissions().values().forEach(DownloadMission::cancel);
                return false;
            } else {
                return true;
            }
        }
    }

}
