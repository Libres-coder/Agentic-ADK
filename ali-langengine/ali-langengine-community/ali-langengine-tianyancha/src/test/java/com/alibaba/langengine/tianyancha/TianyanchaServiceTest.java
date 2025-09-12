package com.alibaba.langengine.tianyancha;

import com.alibaba.langengine.tianyancha.model.CompanyBasicInfoResponse;
import com.alibaba.langengine.tianyancha.model.CompanyHolderResponse;
import com.alibaba.langengine.tianyancha.model.CompanyVerifyResponse;
import com.alibaba.langengine.tianyancha.model.SpecialCompanyInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TianyanchaServiceTest {
    
    private TianyanchaService tianyanchaService;
    private TianyanchaService customTokenService;
    private static final String TEST_TOKEN = "your_test_token_here";
    
    @BeforeEach
    public void setUp() {
        // 使用默认配置构造
        tianyanchaService = new TianyanchaService();
        
        // 使用自定义token构造
        customTokenService = new TianyanchaService(TEST_TOKEN);
    }
    
    @Test
    public void testGetCompanyBasicInfo() {
        try {
            CompanyBasicInfoResponse response = tianyanchaService.getCompanyBasicInfo("中航重机股份有限公司");
            
            assertNotNull(response);
            System.out.println("企业基本信息查询响应: " + response);
            
            if (response.getErrorCode() == 0) {
                assertNotNull(response.getResult());
                CompanyBasicInfoResponse.CompanyBasicInfo basicInfo = response.getResult();
                
                assertNotNull(basicInfo.getName());
                System.out.println("企业名称: " + basicInfo.getName());
                System.out.println("法定代表人: " + basicInfo.getLegalPersonName());
                System.out.println("注册资本: " + basicInfo.getRegCapital());
                System.out.println("经营状态: " + basicInfo.getRegStatus());
                System.out.println("统一社会信用代码: " + basicInfo.getCreditCode());
            } else {
                System.out.println("API错误: " + response.getReason());
            }
        } catch (Exception e) {
            System.out.println("测试异常: " + e.getMessage());
        }
    }
    
    @Test
    public void testGetSpecialCompanyInfo() {
        try {
            SpecialCompanyInfoResponse response = tianyanchaService.getSpecialCompanyInfo("百度（香港）有限公司");
            
            assertNotNull(response);
            System.out.println("特殊企业信息查询响应: " + response);
            
            if (response.getErrorCode() == 0) {
                assertNotNull(response.getResult());
                SpecialCompanyInfoResponse.SpecialCompanyInfo specialInfo = response.getResult();
                
                assertNotNull(specialInfo.getName());
                System.out.println("企业名称: " + specialInfo.getName());
                System.out.println("企业类型: " + specialInfo.getEntityType());
                System.out.println("经营状态: " + specialInfo.getRegStatus());
                System.out.println("注册地址: " + specialInfo.getRegLocation());
            } else {
                System.out.println("API错误: " + response.getReason());
            }
        } catch (Exception e) {
            System.out.println("测试异常: " + e.getMessage());
        }
    }
    
    @Test
    public void testGetCompanyHoldersWithDefaults() {
        try {
            CompanyHolderResponse response = tianyanchaService.getCompanyHolders("北京百度网讯科技有限公司");
            
            assertNotNull(response);
            System.out.println("企业股东信息查询响应（默认参数）: " + response);
            
            if (response.getErrorCode() == 0) {
                assertNotNull(response.getResult());
                CompanyHolderResponse.CompanyHolderResult holderResult = response.getResult();
                
                System.out.println("股东总数: " + holderResult.getTotal());
                
                if (holderResult.getItems() != null && !holderResult.getItems().isEmpty()) {
                    CompanyHolderResponse.CompanyHolder firstHolder = holderResult.getItems().get(0);
                    System.out.println("第一大股东: " + firstHolder.getName());
                    System.out.println("股东类型: " + firstHolder.getType());
                }
            } else {
                System.out.println("API错误: " + response.getReason());
            }
        } catch (Exception e) {
            System.out.println("测试异常: " + e.getMessage());
        }
    }
    
    @Test
    public void testGetCompanyHoldersWithCustomParams() {
        try {
            CompanyHolderResponse response = tianyanchaService.getCompanyHolders("北京百度网讯科技有限公司", 1, 10);
            
            assertNotNull(response);
            System.out.println("企业股东信息查询响应（自定义参数）: " + response);
            
            if (response.getErrorCode() == 0) {
                assertNotNull(response.getResult());
                CompanyHolderResponse.CompanyHolderResult holderResult = response.getResult();
                
                System.out.println("股东总数: " + holderResult.getTotal());
                
                if (holderResult.getItems() != null) {
                    System.out.println("返回股东数量: " + holderResult.getItems().size());
                    assertTrue(holderResult.getItems().size() <= 10);
                }
            } else {
                System.out.println("API错误: " + response.getReason());
            }
        } catch (Exception e) {
            System.out.println("测试异常: " + e.getMessage());
        }
    }
    
    @Test
    public void testVerifyCompanyInfo() {
        try {
            CompanyVerifyResponse response = tianyanchaService.verifyCompanyInfo(
                "91110000802100433B", 
                "北京百度网讯科技有限公司", 
                "梁志祥"
            );
            
            assertNotNull(response);
            System.out.println("企业三要素验证响应: " + response);
            
            if (response.getErrorCode() == 0) {
                assertNotNull(response.getResult());
                CompanyVerifyResponse.CompanyVerifyResult verifyResult = response.getResult();
                
                assertNotNull(verifyResult.getResult());
                System.out.println("验证结果: " + verifyResult.getResult());
                System.out.println("验证说明: " + verifyResult.getRemark());
                
                // 验证结果：1-验证成功，0-验证失败，2-输入企业名疑似曾用名
                assertTrue(verifyResult.getResult() >= 0 && verifyResult.getResult() <= 2);
            } else {
                System.out.println("API错误: " + response.getReason());
            }
        } catch (Exception e) {
            System.out.println("测试异常: " + e.getMessage());
        }
    }
    
    @Test
    public void testServiceInitialization() {
        assertNotNull(tianyanchaService);
        assertNotNull(customTokenService);
        
        // 测试不同构造方法
        TianyanchaService anotherService = new TianyanchaService("another_token");
        assertNotNull(anotherService);
        
        TianyanchaService defaultService = new TianyanchaService();
        assertNotNull(defaultService);
    }
    
    @Test
    public void testCustomTokenService() {
        try {
            CompanyBasicInfoResponse response = customTokenService.getCompanyBasicInfo("阿里巴巴集团控股有限公司");
            
            assertNotNull(response);
            System.out.println("自定义Token服务响应: " + response);
            
            if (response.getErrorCode() == 0) {
                assertNotNull(response.getResult());
                System.out.println("使用自定义Token查询成功");
            }
        } catch (Exception e) {
            System.out.println("自定义Token测试异常: " + e.getMessage());
        }
    }
}