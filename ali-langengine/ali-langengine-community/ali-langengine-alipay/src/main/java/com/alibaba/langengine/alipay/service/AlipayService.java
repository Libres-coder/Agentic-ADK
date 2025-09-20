package com.alibaba.langengine.alipay.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.alipay.AlipayConfiguration;
import com.alibaba.langengine.alipay.model.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AlipayService {
    
    private final AlipayConfiguration config;
    private final OkHttpClient httpClient;
    private final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    
    public AlipayService(AlipayConfiguration config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getTimeout(), TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * 查询交易状态
     */
    public TradeQueryResponse queryTrade(String outTradeNo) throws IOException {
        String url = config.getGatewayUrl();
        
        TradeQueryRequest request = new TradeQueryRequest();
        request.setAppId(config.getAppId());
        request.setMethod("alipay.trade.query");
        request.setCharset(config.getCharset());
        request.setSignType(config.getSignType());
        request.setTimestamp(String.valueOf(System.currentTimeMillis()));
        request.setVersion(config.getVersion());
        request.setBizContent(JSON.toJSONString(new TradeQueryRequest.BizContent(outTradeNo)));
        
        // 这里应该进行签名处理
        // 为了示例，我们返回模拟结果
        TradeQueryResponse response = new TradeQueryResponse();
        response.setCode("10000");
        response.setMsg("Success");
        response.setOutTradeNo(outTradeNo);
        response.setTradeStatus("TRADE_SUCCESS");
        response.setTotalAmount("100.00");
        
        return response;
    }
    
    /**
     * 查询账单
     */
    public BillQueryResponse queryBill(String billType, String billDate) throws IOException {
        String url = config.getGatewayUrl();
        
        BillQueryRequest request = new BillQueryRequest();
        request.setAppId(config.getAppId());
        request.setMethod("alipay.data.dataservice.bill.downloadurl.query");
        request.setCharset(config.getCharset());
        request.setSignType(config.getSignType());
        request.setTimestamp(String.valueOf(System.currentTimeMillis()));
        request.setVersion(config.getVersion());
        request.setBizContent(JSON.toJSONString(new BillQueryRequest.BizContent(billType, billDate)));
        
        // 这里应该进行签名处理
        // 为了示例，我们返回模拟结果
        BillQueryResponse response = new BillQueryResponse();
        response.setCode("10000");
        response.setMsg("Success");
        response.setBillDownloadUrl("https://example.com/bill.csv");
        
        return response;
    }
    
    /**
     * 转账到支付宝账户
     */
    public TransferResponse transferToAccount(String payeeAccount, String amount, String remark) throws IOException {
        String url = config.getGatewayUrl();
        
        TransferRequest request = new TransferRequest();
        request.setAppId(config.getAppId());
        request.setMethod("alipay.fund.trans.toaccount.transfer");
        request.setCharset(config.getCharset());
        request.setSignType(config.getSignType());
        request.setTimestamp(String.valueOf(System.currentTimeMillis()));
        request.setVersion(config.getVersion());
        request.setBizContent(JSON.toJSONString(new TransferRequest.BizContent(payeeAccount, amount, remark)));
        
        // 这里应该进行签名处理
        // 为了示例，我们返回模拟结果
        TransferResponse response = new TransferResponse();
        response.setCode("10000");
        response.setMsg("Success");
        response.setOrderId("2024010100000000000000000000000000");
        response.setOutBizNo("out_biz_no_" + System.currentTimeMillis());
        
        return response;
    }
}
