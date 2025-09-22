package com.alibaba.langengine.alipay.model;

import lombok.Data;

@Data
public class TransferResponse {
    private String code;
    private String msg;
    private String orderId;
    private String outBizNo;
    private String payDate;
    private String status;
}
