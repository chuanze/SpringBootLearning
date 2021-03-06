package com.idcut.mosmqtt.util;

import lombok.Builder;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public final class HttpUtil {

    public final static String GET = "GET";
    public final static String POST = "POST";

    private final static String UTF8 = "UTF-8";

    private final static String DEFAULT_CHARSET = UTF8;
    private final static String DEFAULT_METHOD = GET;
    private final static String DEFAULT_MEDIA_TYPE = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
    private final static boolean DEFAULT_LOG = false;

    private final static OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES))
            .readTimeout(20, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS).build();

    /**
     * GET请求
     *
     * @param url URL地址
     * @return
     */
    public static String get(String url) {
        return execute(OkHttp.builder().url(url).build());
    }

    /**
     * GET请求
     *
     * @param url URL地址
     * @return
     */
    public static String get(String url, String charset) {
        return execute(OkHttp.builder().url(url).responseCharset(charset).build());
    }

    /**
     * 带查询参数的GET查询
     *
     * @param url      URL地址
     * @param queryMap 查询参数
     * @return
     */
    public static String get(String url, Map<String, String> queryMap) {
        return execute(OkHttp.builder().url(url).queryMap(queryMap).build());
    }

    /**
     * 带查询参数的GET查询
     *
     * @param url      URL地址
     * @param queryMap 查询参数
     * @return
     */
    public static String get(String url, Map<String, String> queryMap, String charset) {
        return execute(OkHttp.builder().url(url).queryMap(queryMap).responseCharset(charset).build());
    }

    /**
     * POST
     * application/json
     *
     * @param url
     * @param obj
     * @return
     */
    public static String postJson(String url, Object obj) {
        return execute(OkHttp.builder().url(url).method(POST).data(JsonUtil.toJson(obj)).mediaType(DEFAULT_MEDIA_TYPE).build());
    }

    /**
     * POST
     * application/x-www-form-urlencoded
     *
     * @param url
     * @param formMap
     * @return
     */
    public static String postForm(String url, Map<String, String> formMap) {
        String data = "";
        if (formMap != null) {
            data = formMap.entrySet().stream().map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue())).collect(Collectors
                    .joining("&"));
        }
        return execute(OkHttp.builder().url(url).method(POST).data(data).mediaType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE).build());
    }

    private static String post(String url, String data, String mediaType, String charset) {
        return execute(OkHttp.builder().url(url).method(POST).data(data).mediaType(mediaType).responseCharset(charset).build());
    }

    /**
     * 通用执行方法
     */
    private static String execute(OkHttp okHttp) {
        if (!StringUtils.hasText(okHttp.requestCharset)) {
            okHttp.requestCharset = DEFAULT_CHARSET;
        }
        if (!StringUtils.hasText(okHttp.responseCharset)) {
            okHttp.responseCharset = DEFAULT_CHARSET;
        }
        if (!StringUtils.hasText(okHttp.method)) {
            okHttp.method = DEFAULT_METHOD;
        }
        if (!StringUtils.hasText(okHttp.mediaType)) {
            okHttp.mediaType = DEFAULT_MEDIA_TYPE;
        }
        if (okHttp.requestLog) {//记录请求日志
            log.info(okHttp.toString());
        }

        String url = okHttp.url;

        Request.Builder builder = new Request.Builder();

        if (okHttp.queryMap != null) {
            String queryParams = okHttp.queryMap.entrySet().stream()
                    .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("&"));
            url = String.format("%s%s%s", url, url.contains("?") ? "&" : "?", queryParams);
        }
        builder.url(url);

        if (okHttp.headerMap != null) {
            okHttp.headerMap.forEach(builder::addHeader);
        }

        String method = okHttp.method.toUpperCase();
        String mediaType = String.format("%s;charset=%s", okHttp.mediaType, okHttp.requestCharset);

        if (method.equals(GET)) {
            builder.get();
        } else if (method.equals(POST)) {
            RequestBody requestBody = RequestBody.create(MediaType.parse(mediaType), okHttp.data);
            builder.method(method, requestBody);
        }

        String result = null;
        try {
            Response response = client.newCall(builder.build()).execute();
            result = response.body().string();
            if (okHttp.responseLog) {//记录返回日志
                log.info(String.format("Got response->%s", result));
            }
        } catch (Exception e) {
            log.error(okHttp.toString(), e);
        }
        return result;
    }

    @Builder
    @ToString(exclude = {"requestCharset", "responseCharset", "requestLog", "responseLog"})
    static class OkHttp {
        private String url;
        private String method = DEFAULT_METHOD;
        private String data;
        private String mediaType = DEFAULT_MEDIA_TYPE;
        private Map<String, String> queryMap;
        private Map<String, String> headerMap;
        private String requestCharset = DEFAULT_CHARSET;
        private boolean requestLog = DEFAULT_LOG;

        private String responseCharset = DEFAULT_CHARSET;
        private boolean responseLog = DEFAULT_LOG;
    }
}
