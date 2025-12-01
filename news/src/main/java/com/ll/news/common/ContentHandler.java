package com.ll.news.common;

import org.springframework.stereotype.Component;

@Component
public class ContentHandler {

    public boolean passTag(NewsConst.Source source, String content) {
        return true;
    }

    public boolean passTitle(NewsConst.Source source, String content) {
        return true;
    }

    public boolean passContent(NewsConst.Source source, String content) {
        return true;
    }

    public String AiRes(String content) {
        return content;
    }

}
