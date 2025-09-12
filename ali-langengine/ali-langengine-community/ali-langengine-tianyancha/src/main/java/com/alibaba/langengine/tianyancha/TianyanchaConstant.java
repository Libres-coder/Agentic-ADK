package com.alibaba.langengine.tianyancha;

public interface TianyanchaConstant {
    
    /**
     * The base URL for the Tianyancha API.
     */
    String BASE_URL = "http://open.api.tianyancha.com/services/open";
    
    /**
     * The endpoint for the basic info API.
     */
    String BASIC_INFO_ENDPOINT = "/ic/baseinfo/normal";
    
    /**
     * The endpoint for the special info API.
     */
    String SPECIAL_INFO_ENDPOINT = "/ic/baseinfo/special";
    
    /**
     * The endpoint for the holder info API.
     */
    String HOLDER_ENDPOINT = "/ic/holder/2.0";
    
    /**
     * The endpoint for the verify info API.
     */
    String VERIFY_ENDPOINT = "/ic/verify/2.0";
    /**
     * The default timeout in seconds for API requests.
     */
    int DEFAULT_TIMEOUT = 30;
    
    /**
     * The default page size for API requests.
     */
    int DEFAULT_PAGE_SIZE = 20;
    /**
     * The default page number for API requests.
     */
    int DEFAULT_PAGE_NUM = 1;
}