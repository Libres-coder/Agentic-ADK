/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.baidu.sdk;

import okhttp3.HttpUrl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.alibaba.langengine.baidu.BaiduConfiguration.BAIDU_USER_AGENT;
import static com.alibaba.langengine.baidu.sdk.BaiduConstant.*;

public class BaiduClient {

    private final String userAgent;

    public BaiduClient() {
        this.userAgent = BAIDU_USER_AGENT;
    }

    public BaiduClient(String userAgent) {
        this.userAgent = (userAgent == null || userAgent.trim().isEmpty()) ? BAIDU_USER_AGENT : userAgent;
    }

    public SearchResponse search(SearchRequest request) throws BaiduException {
        if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new BaiduException("query must not be blank");
        }

        try {
            int limit = request.getCount() == null ? 10 : Math.max(1, request.getCount());

            String wd = URLEncoder.encode(request.getQuery(), StandardCharsets.UTF_8);
            HttpUrl url = HttpUrl.parse(BASE_URL).newBuilder()
                .addQueryParameter("wd", wd)
                .build();

            Document doc = Jsoup.connect(url.toString())
                .userAgent(userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .timeout(DEFAULT_TIMEOUT_SECONDS * 1000)
                .followRedirects(true)
                .get();

            List<SearchResult> list = new ArrayList<>();
            Elements items = doc.select("#content_left > div.result, #content_left > div.c-container, #content_left > div[class*=result], #content_left > div[class*=c-container]");
            for (Element item : items) {
                Element link = item.selectFirst("h3 a, h3 > a, .t > a");
                if (link == null) {
                    continue;
                }
                String title = link.text();
                String href = link.attr("abs:href");
                if (href == null || href.isEmpty()) {
                    href = link.attr("href");
                }
                String desc = "";
                Element snippetEl = item.selectFirst(".c-abstract, .c-gap-top-small, .c-line-clamp3, .op_general_card_content, .op_general_card");
                if (snippetEl != null) {
                    desc = snippetEl.text();
                }

                if (title != null && !title.trim().isEmpty() && href != null && !href.trim().isEmpty()) {
                    SearchResult sr = new SearchResult();
                    sr.setTitle(title);
                    sr.setUrl(href);
                    sr.setDescription(desc);
                    list.add(sr);
                }
                if (list.size() >= limit) {
                    break;
                }
            }

            SearchResponse resp = new SearchResponse();
            resp.setQuery(request.getQuery());
            resp.setResults(list);
            return resp;
        } catch (Exception e) {
            throw new BaiduException("Baidu search failed: " + e.getMessage(), e);
        }
    }

    public SearchResponse search(String query) throws BaiduException {
        SearchRequest req = new SearchRequest();
        req.setQuery(query);
        return search(req);
    }

    public SearchResponse search(String query, int count) throws BaiduException {
        SearchRequest req = new SearchRequest();
        req.setQuery(query);
        req.setCount(count);
        return search(req);
    }
}

