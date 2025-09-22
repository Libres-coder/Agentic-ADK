package com.alibaba.langengine.alipay.model;

import lombok.Data;

@Data
public class BillQueryResponse {
    private String code;
    private String msg;
    private String billDownloadUrl;
    private String billType;
    private String billDate;
    private String billSize;
}
