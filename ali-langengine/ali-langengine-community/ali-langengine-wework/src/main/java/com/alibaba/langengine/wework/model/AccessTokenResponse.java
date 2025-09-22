package com.alibaba.langengine.wework.model;

import lombok.Data;

@Data
public class AccessTokenResponse {
    private int errcode;
    private String errmsg;
    private String access_token;
    private int expires_in;
}
