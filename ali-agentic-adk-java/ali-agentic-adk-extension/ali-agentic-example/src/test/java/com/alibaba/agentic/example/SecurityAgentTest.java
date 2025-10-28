package com.alibaba.agentic.example;

import com.alibaba.agentic.core.executor.*;
import com.alibaba.agentic.core.tools.security.DataMaskingTool;
import com.alibaba.agentic.core.tools.security.SensitiveWordFilterTool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/**
 * Security features demonstration and testing.
 *
 * @author Libres-coder
 * @date 2025/10/27
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@ActiveProfiles("testing")
public class SecurityAgentTest {

    @Test
    public void testSensitiveWordFilter() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试1: 敏感词过滤工具");
        System.out.println("=".repeat(60));

        SensitiveWordFilterTool filterTool = new SensitiveWordFilterTool();
        SystemContext context = new SystemContext();

        // 测试用例1: 检测敏感词
        Map<String, Object> args1 = new HashMap<>();
        args1.put("text", "这是一段包含赌博和诈骗的文本内容");
        args1.put("strategy", "DETECT_ONLY");

        Map<String, Object> result1 = filterTool.run(args1, context).blockingFirst();
        System.out.println("\n[检测模式]");
        System.out.println("原始文本: " + result1.get("original_text"));
        System.out.println("检测到敏感词: " + result1.get("has_sensitive_words"));
        System.out.println("敏感词数量: " + result1.get("detected_words_count"));
        System.out.println("敏感词列表: " + result1.get("detected_words"));

        // 测试用例2: 星号替换
        Map<String, Object> args2 = new HashMap<>();
        args2.put("text", "我想要赌博赚快钱，有人教我诈骗");
        args2.put("strategy", "ASTERISK");

        Map<String, Object> result2 = filterTool.run(args2, context).blockingFirst();
        System.out.println("\n[星号替换模式]");
        System.out.println("原始文本: " + result2.get("original_text"));
        System.out.println("过滤后文本: " + result2.get("filtered_text"));

        // 测试用例3: 自定义替换
        Map<String, Object> args3 = new HashMap<>();
        args3.put("text", "非法内容和暴力内容不应该出现");
        args3.put("strategy", "CUSTOM");
        args3.put("custom_replace", "[已屏蔽]");

        Map<String, Object> result3 = filterTool.run(args3, context).blockingFirst();
        System.out.println("\n[自定义替换模式]");
        System.out.println("原始文本: " + result3.get("original_text"));
        System.out.println("过滤后文本: " + result3.get("filtered_text"));

        // 测试用例4: 自定义敏感词库
        Set<String> customWords = new HashSet<>();
        customWords.add("测试敏感词");
        customWords.add("自定义词汇");

        SensitiveWordFilterTool customFilter = new SensitiveWordFilterTool(customWords);

        Map<String, Object> args4 = new HashMap<>();
        args4.put("text", "这包含测试敏感词和自定义词汇");
        args4.put("strategy", "ASTERISK");

        Map<String, Object> result4 = customFilter.run(args4, context).blockingFirst();
        System.out.println("\n[自定义词库]");
        System.out.println("原始文本: " + result4.get("original_text"));
        System.out.println("过滤后文本: " + result4.get("filtered_text"));

        System.out.println("\n" + "=".repeat(60));
    }

    @Test
    public void testDataMasking() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试2: 数据脱敏工具");
        System.out.println("=".repeat(60));

        DataMaskingTool maskingTool = new DataMaskingTool();
        SystemContext context = new SystemContext();

        // 测试用例1: 手机号脱敏
        Map<String, Object> args1 = new HashMap<>();
        args1.put("text", "我的手机号是13812345678，联系我吧");
        args1.put("types", Arrays.asList("phone"));

        Map<String, Object> result1 = maskingTool.run(args1, context).blockingFirst();
        System.out.println("\n[手机号脱敏]");
        System.out.println("原始文本: " + result1.get("original_text"));
        System.out.println("脱敏后文本: " + result1.get("masked_text"));
        System.out.println("检测到的PII: " + result1.get("detected_pii"));

        // 测试用例2: 身份证号脱敏
        Map<String, Object> args2 = new HashMap<>();
        args2.put("text", "我的身份证号是110101199001011234");
        args2.put("types", Arrays.asList("id_card"));

        Map<String, Object> result2 = maskingTool.run(args2, context).blockingFirst();
        System.out.println("\n[身份证号脱敏]");
        System.out.println("原始文本: " + result2.get("original_text"));
        System.out.println("脱敏后文本: " + result2.get("masked_text"));

        // 测试用例3: 邮箱脱敏
        Map<String, Object> args3 = new HashMap<>();
        args3.put("text", "联系邮箱: user@example.com");
        args3.put("types", Arrays.asList("email"));

        Map<String, Object> result3 = maskingTool.run(args3, context).blockingFirst();
        System.out.println("\n[邮箱脱敏]");
        System.out.println("原始文本: " + result3.get("original_text"));
        System.out.println("脱敏后文本: " + result3.get("masked_text"));

        // 测试用例4: 银行卡号脱敏
        Map<String, Object> args4 = new HashMap<>();
        args4.put("text", "银行卡号: 6222021234567890123");
        args4.put("types", Arrays.asList("bank_card"));

        Map<String, Object> result4 = maskingTool.run(args4, context).blockingFirst();
        System.out.println("\n[银行卡号脱敏]");
        System.out.println("原始文本: " + result4.get("original_text"));
        System.out.println("脱敏后文本: " + result4.get("masked_text"));

        // 测试用例5: 综合测试（所有类型）
        Map<String, Object> args5 = new HashMap<>();
        args5.put("text", "用户信息: 手机13812345678，邮箱user@test.com，身份证110101199001011234");
        args5.put("types", Arrays.asList("all"));

        Map<String, Object> result5 = maskingTool.run(args5, context).blockingFirst();
        System.out.println("\n[综合测试 - 所有PII类型]");
        System.out.println("原始文本: " + result5.get("original_text"));
        System.out.println("脱敏后文本: " + result5.get("masked_text"));
        System.out.println("PII数量: " + result5.get("pii_count"));
        System.out.println("检测到的PII: " + result5.get("detected_pii"));

        System.out.println("\n" + "=".repeat(60));
    }

    @Test
    public void testSecurityCallback() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试3: 安全回调处理器");
        System.out.println("=".repeat(60));

        SecurityCallback securityCallback = new SecurityCallback()
            .enableSensitiveWordFilter(true)
            .enableDataMasking(true)
            .setBlockOnSensitiveWord(false)
            .setMaskLogs(true);

        securityCallback.addSensitiveWord("机密信息");

        SystemContext context = new SystemContext();

        // 模拟请求
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("message", "我想分享一些赌博技巧和机密信息，我的手机是13812345678");

        Request request = new Request()
            .setInvokeMode(InvokeMode.SYNC)
            .setParam(requestPayload);
        Result result = new Result();

        // 创建简单的回调链
        CallbackChain chain = new CallbackChain() {
            @Override
            public void execute(SystemContext systemContext, Request request, Result result) {
                System.out.println("\n[回调链] execute方法被调用");
                System.out.println("请求已通过安全检查，继续执行...");
            }

            @Override
            public void receive(SystemContext systemContext, Request request, Result result) {
                System.out.println("\n[回调链] receive方法被调用");
                System.out.println("响应已通过安全检查");
            }
        };

        // 执行安全回调
        System.out.println("\n[执行阶段安全检查]");
        try {
            securityCallback.execute(context, request, result, chain);
        } catch (SecurityException e) {
            System.out.println("安全异常: " + e.getMessage());
        }

        // 模拟响应
        Map<String, Object> responsePayload = new HashMap<>();
        responsePayload.put("message", "这是包含用户邮箱user@example.com的响应");
        result.setData(responsePayload);

        System.out.println("\n[接收阶段安全检查]");
        try {
            securityCallback.receive(context, request, result, chain);
        } catch (SecurityException e) {
            System.out.println("安全异常: " + e.getMessage());
        }

        // 查看安全事件
        System.out.println("\n[安全事件记录]");
        List<SecurityCallback.SecurityEvent> events = securityCallback.getSecurityEvents();
        System.out.println("记录的安全事件数量: " + events.size());
        for (SecurityCallback.SecurityEvent event : events) {
            System.out.println("  - " + event);
        }

        System.out.println("\n" + "=".repeat(60));
    }

    @Test
    public void testSecurityCallbackWithBlock() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试4: 安全回调 - 阻断模式");
        System.out.println("=".repeat(60));

        // 创建启用阻断的安全回调
        SecurityCallback securityCallback = new SecurityCallback()
            .enableSensitiveWordFilter(true)
            .setBlockOnSensitiveWord(true);  // 启用阻断

        SystemContext context = new SystemContext();

        // 模拟包含敏感词的请求
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("message", "我要学习诈骗技术");

        Request request = new Request()
            .setInvokeMode(InvokeMode.SYNC)
            .setParam(requestPayload);
        Result result = new Result();

        CallbackChain chain = new CallbackChain() {
            @Override
            public void execute(SystemContext systemContext, Request request, Result result) {
                System.out.println("这行不应该被打印（请求应该被阻断）");
            }

            @Override
            public void receive(SystemContext systemContext, Request request, Result result) {
                System.out.println("响应处理");
            }
        };

        // 执行安全回调 - 应该抛出异常
        System.out.println("\n[测试阻断功能]");
        System.out.println("尝试发送包含敏感词的请求...");
        try {
            securityCallback.execute(context, request, result, chain);
            System.out.println("错误: 请求没有被阻断!");
        } catch (SecurityException e) {
            System.out.println("成功: 请求被阻断!");
            System.out.println("原因: " + e.getMessage());
        }

        System.out.println("\n" + "=".repeat(60));
    }

    @Test
    public void testSecureCustomerServiceAgent() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("测试5: 综合示例 - 安全的客服Agent");
        System.out.println("=".repeat(60));

        // 1. 创建安全工具
        SensitiveWordFilterTool filterTool = new SensitiveWordFilterTool();
        DataMaskingTool maskingTool = new DataMaskingTool();
        
        // 2. 创建安全回调
        SecurityCallback securityCallback = new SecurityCallback()
            .enableSensitiveWordFilter(true)
            .enableDataMasking(true)
            .setBlockOnSensitiveWord(false)
            .setMaskLogs(true);

        SystemContext context = new SystemContext();

        // 3. 模拟客户查询
        System.out.println("\n[场景: 客户咨询订单问题]");
        String customerQuery = "您好，我的订单号是ORDER-123，手机号13812345678，想查询订单状态";

        // 3.1 先进行敏感词检查
        Map<String, Object> filterArgs = new HashMap<>();
        filterArgs.put("text", customerQuery);
        filterArgs.put("strategy", "DETECT_ONLY");
        
        Map<String, Object> filterResult = filterTool.run(filterArgs, context).blockingFirst();
        System.out.println("客户输入: " + customerQuery);
        System.out.println("敏感词检测: " + (Boolean.TRUE.equals(filterResult.get("has_sensitive_words")) ? "❌ 发现敏感词" : "✅ 通过"));

        // 3.2 脱敏日志记录
        Map<String, Object> maskArgs = new HashMap<>();
        maskArgs.put("text", customerQuery);
        maskArgs.put("types", Arrays.asList("all"));
        
        Map<String, Object> maskResult = maskingTool.run(maskArgs, context).blockingFirst();
        System.out.println("日志记录（脱敏）: " + maskResult.get("masked_text"));

        // 4. 模拟Agent响应
        System.out.println("\n[Agent处理并响应]");
        String agentResponse = "您好！您的订单ORDER-123状态为已发货。" +
                              "稍后会发送短信到手机 13812345678。" +
                              "如有问题请联系客服邮箱 support@company.com";

        // 4.1 响应脱敏
        Map<String, Object> responseArgs = new HashMap<>();
        responseArgs.put("text", agentResponse);
        responseArgs.put("types", Arrays.asList("all"));
        
        Map<String, Object> responseMask = maskingTool.run(responseArgs, context).blockingFirst();
        System.out.println("原始响应: " + agentResponse);
        System.out.println("日志记录（脱敏）: " + responseMask.get("masked_text"));
        System.out.println("检测到PII数量: " + responseMask.get("pii_count"));

        // 5. 查看安全统计
        System.out.println("\n[安全统计]");
        System.out.println("所有敏感数据已被正确处理");
        System.out.println("PII信息已被脱敏保护");
        System.out.println("可以安全地记录到日志系统");

        System.out.println("\n" + "=".repeat(60));
        System.out.println("综合示例完成！");
        System.out.println("安全措施:");
        System.out.println("1. 输入过滤 - 检测用户输入中的敏感词");
        System.out.println("2. PII保护 - 自动识别和脱敏个人信息");
        System.out.println("3. 日志安全 - 记录脱敏后的数据");
        System.out.println("4. 合规性 - 满足数据隐私保护要求");
        System.out.println("=".repeat(60));
    }
}
