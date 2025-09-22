package com.alibaba.langengine.alipay.model;

import lombok.Data;

@Data
public class TradeQueryResponse {
    private String code;
    private String msg;
    private String outTradeNo;
    private String tradeNo;
    private String tradeStatus;
    private String totalAmount;
    private String receiptAmount;
    private String buyerPayAmount;
    private String pointAmount;
    private String invoiceAmount;
    private String sendPayDate;
    private String alipayStoreId;
    private String storeId;
    private String terminalId;
    private String storeName;
    private String buyerUserId;
    private String buyerLogonId;
    private String fundBillList;
    private String discountGoodsDetail;
    private String industrySepcDetail;
    private String alipaySubMerchantId;
    private String extInfos;
}
