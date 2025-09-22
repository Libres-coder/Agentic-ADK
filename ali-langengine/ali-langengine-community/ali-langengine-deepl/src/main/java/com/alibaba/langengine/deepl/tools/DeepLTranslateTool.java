package com.alibaba.langengine.deepl.tools;

import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.deepl.DeepLConfiguration;
import com.alibaba.langengine.deepl.model.DeepLTranslateRequest;
import com.alibaba.langengine.deepl.model.DeepLTranslateResponse;
import com.alibaba.langengine.deepl.service.DeepLService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.alibaba.langengine.deepl.DeepLConfiguration.*;

/**
 * DeepL 翻译工具
 *
 * @author Makoto
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class DeepLTranslateTool extends DefaultTool {
    
    private static final String DEFAULT_SOURCE_LANG = "auto";
    private static final String DEFAULT_TARGET_LANG = "ZH";
    
    @Setter
    private String sourceLang = DEFAULT_SOURCE_LANG;
    @Setter
    private String targetLang = DEFAULT_TARGET_LANG;
    
    private String apiKey;
    private boolean isPro;
    private int timeout;
    private DeepLService deepLService;
    
    public DeepLTranslateTool() {
        this.apiKey = getApiKey();
        this.isPro = isPro();
        this.timeout = getTimeout();
        init();
    }
    
    public DeepLTranslateTool(String apiKey, boolean isPro, int timeout) {
        this.apiKey = apiKey;
        this.isPro = isPro;
        this.timeout = timeout;
        init();
    }
    
    private void init() {
        setName("DeepLTranslateTool");
        setDescription("DeepL 高质量翻译工具，支持多种语言之间的互译，特别适合欧洲语言翻译");
        
        this.deepLService = new DeepLService(
            getServerUrl(),
            Duration.ofSeconds(timeout),
            apiKey,
            isPro
        );
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("DeepL 翻译输入: {}", toolInput);
        
        if (StringUtils.isBlank(toolInput)) {
            return ToolExecuteResult.builder()
                    .success(false)
                    .error("翻译文本不能为空")
                    .build();
        }
        
        try {
            DeepLTranslateRequest request = new DeepLTranslateRequest();
            request.setText(Arrays.asList(toolInput));
            request.setSourceLang(sourceLang);
            request.setTargetLang(targetLang);
            
            DeepLTranslateResponse response = deepLService.translate(request);
            
            if (response.getTranslations() == null || response.getTranslations().isEmpty()) {
                return ToolExecuteResult.builder()
                        .success(false)
                        .error("翻译失败: 无翻译结果")
                        .build();
            }
            
            String translatedText = response.getTranslations().get(0).getText();
            String detectedLang = response.getTranslations().get(0).getDetectedSourceLanguage();
            
            log.info("DeepL 翻译结果: {}", translatedText);
            log.info("检测到的源语言: {}", detectedLang);
            
            return ToolExecuteResult.builder()
                    .success(true)
                    .result(translatedText)
                    .build();
                    
        } catch (Exception e) {
            log.error("DeepL 翻译异常", e);
            return ToolExecuteResult.builder()
                    .success(false)
                    .error("翻译异常: " + e.getMessage())
                    .build();
        }
    }
}
