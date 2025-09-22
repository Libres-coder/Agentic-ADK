package com.alibaba.langengine.tencenttranslate.tools;

import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.tencenttranslate.TencentTranslateConfiguration;
import com.alibaba.langengine.tencenttranslate.model.TencentTranslateRequest;
import com.alibaba.langengine.tencenttranslate.model.TencentTranslateResponse;
import com.alibaba.langengine.tencenttranslate.service.TencentTranslateService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

import static com.alibaba.langengine.tencenttranslate.TencentTranslateConfiguration.*;

/**
 * 腾讯翻译工具
 *
 * @author Makoto
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class TencentTranslateTool extends DefaultTool {
    
    private static final String DEFAULT_SOURCE_LANG = "auto";
    private static final String DEFAULT_TARGET_LANG = "zh";
    
    @Setter
    private String sourceLang = DEFAULT_SOURCE_LANG;
    @Setter
    private String targetLang = DEFAULT_TARGET_LANG;
    
    private String secretId;
    private String secretKey;
    private String region;
    private String service;
    private String version;
    private String action;
    private int timeout;
    private TencentTranslateService tencentTranslateService;
    
    public TencentTranslateTool() {
        this.secretId = getSecretId();
        this.secretKey = getSecretKey();
        this.region = getRegion();
        this.service = getService();
        this.version = getVersion();
        this.action = getAction();
        this.timeout = getTimeout();
        init();
    }
    
    public TencentTranslateTool(String secretId, String secretKey, String region, String service, 
                               String version, String action, int timeout) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.region = region;
        this.service = service;
        this.version = version;
        this.action = action;
        this.timeout = timeout;
        init();
    }
    
    private void init() {
        setName("TencentTranslateTool");
        setDescription("腾讯云翻译工具，支持多种翻译模式，集成图片和语音翻译，提供翻译质量检查");
        
        this.tencentTranslateService = new TencentTranslateService(
            getServerUrl(),
            Duration.ofSeconds(timeout),
            secretId,
            secretKey,
            region,
            service,
            version,
            action
        );
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("腾讯翻译输入: {}", toolInput);
        
        if (StringUtils.isBlank(toolInput)) {
            return ToolExecuteResult.builder()
                    .success(false)
                    .error("翻译文本不能为空")
                    .build();
        }
        
        try {
            TencentTranslateRequest request = new TencentTranslateRequest();
            request.setSourceText(toolInput);
            request.setSource(sourceLang);
            request.setTarget(targetLang);
            
            TencentTranslateResponse response = tencentTranslateService.translate(request);
            
            if (response.getResponse() == null) {
                return ToolExecuteResult.builder()
                        .success(false)
                        .error("翻译失败: 无响应结果")
                        .build();
            }
            
            if (response.getResponse().getError() != null) {
                return ToolExecuteResult.builder()
                        .success(false)
                        .error("翻译失败: " + response.getResponse().getError().getMessage())
                        .build();
            }
            
            String translatedText = response.getResponse().getTargetText();
            
            log.info("腾讯翻译结果: {}", translatedText);
            
            return ToolExecuteResult.builder()
                    .success(true)
                    .result(translatedText)
                    .build();
                    
        } catch (Exception e) {
            log.error("腾讯翻译异常", e);
            return ToolExecuteResult.builder()
                    .success(false)
                    .error("翻译异常: " + e.getMessage())
                    .build();
        }
    }
}
