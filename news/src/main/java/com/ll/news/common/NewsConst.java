package com.ll.news.common;

public interface NewsConst {

    interface Status {
        // 新入库
        int NEW = 0;
        // 已发布
        int PUBLISHED = 1;
    }

    enum Source {
        /**
         * 金十全球财经早餐
         */
        jin10_global("金十全球财经早餐", "jin10_global", "https://xnews.jin10.com/topic/343"),
        /**
         * 金十 美联储
         */
        jin10_fed("金十美联储", "jin10_fed", "https://xnews.jin10.com/topic/20"),
        /**
         * ForesightNews快讯
         */
        foresightNews_quick_news("ForesightNews快讯", "foresightNews_quick_news", "https://rsshub.app/foresightnews/news?format=json"),
        /**
         * Binance New (每日早报)
         */
        binance_new("Binance New (每日早报)", "binance_new", "https://www.binance.com/zh-CN/square/profile/binance_news"),
        /**
         * Binance Delisting (下架讯息)
         */
        binance_delisting("Binance Delisting", "binance_delisting", "https://www.binance.com/zh-CN/support/announcement/%E4%B8%8B%E6%9E%B6%E8%AE%AF%E6%81%AF?c=161&navId=161&hl=zh-CN"),

        /**
         * OKX Delisting (下架讯息)
         */
        okx_delisting("OKX Delisting", "okx_delisting", "https://www.okx.com/zh-hans/help/section/announcements-delistings")
        ;

        private final String desc;
        private final String source;
        private final String link;

        Source(String desc, String source, String link) {
            this.desc = desc;
            this.source = source;
            this.link = link;
        }

        public String source() {
            return source;
        }

        public String link() {
            return link;
        }

        public String desc() {
            return desc;
        }

        public static Source getBySource(String source) {
            for (Source s : Source.values()) {
                if (s.source.equals(source)) {
                    return s;
                }
            }
            return null;
        }
    }


}
