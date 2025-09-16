package com.alibaba.langengine.volcenginetranslate.tools;

import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.volcenginetranslate.VolcengineTranslateConfiguration;
import com.alibaba.langengine.volcenginetranslate.model.VolcengineTranslateRequest;
import com.alibaba.langengine.volcenginetranslate.model.VolcengineTranslateResponse;
import com.alibaba.langengine.volcenginetranslate.service.VolcengineTranslateService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.Arrays;

import static com.alibaba.langengine.volcenginetranslate.VolcengineTranslateConfiguration.*;

/**
 * 火山翻译工具
 *
 * @author Makoto
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class VolcengineTranslateTool extends DefaultTool {
    
    private static final String DEFAULT_SOURCE_LANG = "auto";
    private static final String DEFAULT_TARGET_LANG = "zh";
    
    @Setter
    private String sourceLang = DEFAULT_SOURCE_LANG;
    @Setter
    private String targetLang = DEFAULT_TARGET_LANG;
    
    private String accessKey;
    private String secretKey;
    private String region;
    private String service;
    private String version;
    private String action;
    private int timeout;
    private VolcengineTranslateService volcengineTranslateService;
    
    public VolcengineTranslateTool() {
        this.accessKey = getAccessKey();
        this.secretKey = getSecretKey();
        this.region = getRegion();
        this.service = getService();
        this.version = getVersion();
        this.action = getAction();
        this.timeout = getTimeout();
        init();
    }
    
    public VolcengineTranslateTool(String accessKey, String secretKey, String region, String service, 
                                  String version, String action, int timeout) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.service = service;
        this.version = version;
        this.action = action;
        this.timeout = timeout;
        init();
    }
    
    private void init() {
        setName("VolcengineTranslateTool");
        setDescription("火山引擎翻译工具，字节跳动旗下服务，支持批量翻译和自定义翻译模型");
        
        this.volcengineTranslateService = new VolcengineTranslateService(
            getServerUrl(),
            Duration.ofSeconds(timeout),
            accessKey,
            secretKey,
            region,
            service,
            version,
            action
        );
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("火山翻译输入: {}", toolInput);
        
        if (StringUtils.isBlank(toolInput)) {
            return ToolExecuteResult.builder()
                    .success(false)
                    .error("翻译文本不能为空")
                    .build();
        }
        
        try {
            VolcengineTranslateRequest request = new VolcengineTranslateRequest();
            request.setTextList(Arrays.asList(toolInput));
            request.setSourceLanguage(sourceLang);
            request.setTargetLanguage(targetLang);
            
            VolcengineTranslateResponse response = volcengineTranslateService.translate(request);
            
            if (response.getResult() == null) {
                return ToolExecuteResult.builder()
                        .success(false)
                        .error("翻译失败: 无响应结果")
                        .build();
            }
            
            if (response.getResponseMetadata() != null && response.getResponseMetadata().getError() != null) {
                return ToolExecuteResult.builder()
                        .success(false)
                        .error("翻译失败: " + response.getResponseMetadata().getError().getMessage())
                        .build();
            }
            
            if (response.getResult().getTranslationList() == null || response.getResult().getTranslationList().isEmpty()) {
                return ToolExecuteResult.builder()
                        .success(false)
                        .error("翻译失败: 无翻译结果")
                        .build();
            }
            
            String translatedText = response.getResult().getTranslationList().get(0).getTranslation();
            String detectedLang = response.getResult().getDetectedLanguage();
            
            log.info("火山翻译结果: {}", translatedText);
            log.info("检测到的源语言: {}", detectedLang);
            
            return ToolExecuteResult.builder()
                    .success(true)
                    .result(translatedText)
                    .build();
                    
        } catch (Exception e) {
            log.error("火山翻译异常", e);
            return ToolExecuteResult.builder()
                    .success(false)
                    .error("翻译异常: " + e.getMessage())
                    .build();
        }
    }
}
