package com.alibaba.langengine.wecom.constant;


public class WeComConstant {
    
    /**
     * API基础URL
     */
    public static final String API_BASE_URL = "https://qyapi.weixin.qq.com";
    
    /**
     * API接口
     */
    public static final String API_GET_TOKEN = "/cgi-bin/gettoken";
    public static final String API_SEND_MESSAGE = "/cgi-bin/message/send";
    public static final String API_GET_USER = "/cgi-bin/user/get";
    
    /**
     * 消息类型常量
     */
    public static final String MSG_TYPE_TEXT = "text";
    public static final String MSG_TYPE_MARKDOWN = "markdown";
    public static final String MSG_TYPE_IMAGE = "image";
    public static final String MSG_TYPE_TEXTCARD = "textcard";
    
    /**
     * 错误码常量
     */
    public static final int ERROR_INVALID_USERID = 60111;

    /**
     * 消息类型
     */
    public static class MessageType {
        public static final String TEXT = "text";
        public static final String MARKDOWN = "markdown";
        public static final String IMAGE = "image";
        public static final String VOICE = "voice";
        public static final String VIDEO = "video";
        public static final String FILE = "file";
        public static final String TEXTCARD = "textcard";
        public static final String NEWS = "news";
        public static final String MPNEWS = "mpnews";
        public static final String TASKCARD = "taskcard";
        public static final String MINIPROGRAM_NOTICE = "miniprogram_notice";
    }

    /**
     * API接口路径
     */
    public static class ApiPath {
        public static final String GET_TOKEN = "/cgi-bin/gettoken";
        public static final String SEND_MESSAGE = "/cgi-bin/message/send";
        public static final String GET_USER = "/cgi-bin/user/get";
        public static final String CREATE_USER = "/cgi-bin/user/create";
        public static final String UPDATE_USER = "/cgi-bin/user/update";
        public static final String DELETE_USER = "/cgi-bin/user/delete";
        public static final String BATCH_DELETE_USER = "/cgi-bin/user/batchdelete";
        public static final String GET_USER_LIST = "/cgi-bin/user/simplelist";
        public static final String GET_USER_DETAIL_LIST = "/cgi-bin/user/list";
        public static final String GET_DEPARTMENT_LIST = "/cgi-bin/department/list";
        public static final String CREATE_DEPARTMENT = "/cgi-bin/department/create";
        public static final String UPDATE_DEPARTMENT = "/cgi-bin/department/update";
        public static final String DELETE_DEPARTMENT = "/cgi-bin/department/delete";
    }

    /**
     * 响应状态码
     */
    public static class ResponseCode {
        public static final int SUCCESS = 0;
        public static final int INVALID_CREDENTIAL = 40014;
        public static final int ACCESS_TOKEN_EXPIRED = 42001;
        public static final int INVALID_AGENT_ID = 40013;
        public static final int USER_NOT_EXIST = 60111;
        public static final int DEPARTMENT_NOT_EXIST = 60003;
    }

    /**
     * 用户状态
     */
    public static class UserStatus {
        public static final int ACTIVATED = 1;
        public static final int DEACTIVATED = 2;
        public static final int UNACTIVATED = 4;
    }

    /**
     * 性别
     */
    public static class Gender {
        public static final int UNKNOWN = 0;
        public static final int MALE = 1;
        public static final int FEMALE = 2;
    }

    /**
     * 默认值
     */
    public static class Default {
        public static final String TO_ALL_USER = "@all";
        public static final String SAFE_VALUE = "0";
        public static final String ENABLE_ID_TRANS = "0";
        public static final String ENABLE_DUPLICATE_CHECK = "0";
        public static final int DUPLICATE_CHECK_INTERVAL = 1800;
    }
}
