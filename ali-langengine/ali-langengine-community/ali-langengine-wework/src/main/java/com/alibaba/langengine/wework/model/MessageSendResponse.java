package com.alibaba.langengine.wework.model;

import lombok.Data;

@Data
public class MessageSendResponse {
    private int errcode;
    private String errmsg;
    private String invaliduser;
    private String invalidparty;
    private String invalidtag;
    private String msgid;
}
