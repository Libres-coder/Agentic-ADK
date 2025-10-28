# Agentic ADK 安全能力指南

## 概述

Agentic ADK 提供了一套完整的安全能力，帮助开发者构建安全、合规的AI Agent应用。本指南介绍如何使用这些安全功能来保护您的应用免受安全威胁，并确保用户隐私数据得到妥善保护。

## 安全能力

### 1. 敏感词/黑词过滤

自动检测和过滤文本中的敏感词，包括但不限于：
- 政治敏感词
- 色情内容
- 赌博相关
- 违禁品信息
- 诈骗相关
- 其他不当内容

**特性：**
- 支持自定义敏感词库
- 使用DFA算法实现高性能匹配
- 多种替换策略（星号、删除、自定义）
- 仅检测模式（不修改原文）

### 2. 数据脱敏（PII保护）

自动识别和脱敏个人可识别信息（PII），保护用户隐私：
- 中国大陆手机号（11位）
- 身份证号（18位）
- 邮箱地址
- 银行卡号（13-19位）
- IP地址
- 密码字段

**特性：**
- 自动识别多种PII类型
- 保留前缀和后缀便于追踪
- 可选择性启用/禁用特定类型
- 保持数据格式完整性

### 3. 安全回调（Java - 可选的高级特性）

**注意**: 这是一个可选的高级特性。如果你不熟悉Callback机制，建议直接使用上述工具类进行手动安全检查。

在Agent执行前后自动进行安全检查：
- 请求阶段安全检查
- 响应阶段安全检查
- 安全事件记录
- 可配置的阻断策略

## 快速开始

### Java 版本

#### 1. 敏感词过滤

```java
import com.alibaba.agentic.core.tools.security.SensitiveWordFilterTool;
import com.alibaba.agentic.core.executor.SystemContext;

// 创建过滤工具
SensitiveWordFilterTool filterTool = new SensitiveWordFilterTool();
SystemContext context = new SystemContext();

// 检测敏感词
Map<String, Object> args = new HashMap<>();
args.put("text", "这是一段包含赌博和诈骗的文本");
args.put("strategy", "DETECT_ONLY");

Map<String, Object> result = filterTool.run(args, context).blockingFirst();
System.out.println("检测到敏感词: " + result.get("has_sensitive_words"));
System.out.println("敏感词数量: " + result.get("detected_words_count"));

// 过滤敏感词（星号替换）
args.put("strategy", "ASTERISK");
result = filterTool.run(args, context).blockingFirst();
System.out.println("过滤后: " + result.get("filtered_text"));
```

#### 2. 数据脱敏

```java
import com.alibaba.agentic.core.tools.security.DataMaskingTool;

// 创建脱敏工具
DataMaskingTool maskingTool = new DataMaskingTool();

// 脱敏手机号
Map<String, Object> args = new HashMap<>();
args.put("text", "我的手机号是13812345678");
args.put("types", Arrays.asList("phone"));

Map<String, Object> result = maskingTool.run(args, context).blockingFirst();
System.out.println("脱敏后: " + result.get("masked_text"));
// 输出: 我的手机号是138****5678
```

#### 3. 安全回调

```java
import com.alibaba.agentic.core.executor.SecurityCallback;

// 创建安全回调
SecurityCallback securityCallback = new SecurityCallback()
    .enableSensitiveWordFilter(true)
    .enableDataMasking(true)
    .setBlockOnSensitiveWord(false)
    .setMaskLogs(true);

// 添加自定义敏感词
securityCallback.addSensitiveWord("自定义敏感词");

// 在Agent执行链中使用
// securityCallback.execute(systemContext, request, result, chain);
```

### Python 版本

#### 1. 敏感词过滤

```python
from ali_agentic_adk_python.core.tool.security_tools import SensitiveWordFilterTool

# 创建过滤工具
filter_tool = SensitiveWordFilterTool()

# 检测敏感词
result = filter_tool.run(
    text="这是一段包含赌博和诈骗的文本",
    strategy="detect_only"
)
print(f"检测到敏感词: {result['has_sensitive_words']}")
print(f"敏感词数量: {result['detected_words_count']}")

# 过滤敏感词（星号替换）
result = filter_tool.run(
    text="这是一段包含赌博和诈骗的文本",
    strategy="asterisk"
)
print(f"过滤后: {result['filtered_text']}")
```

#### 2. 数据脱敏

```python
from ali_agentic_adk_python.core.tool.security_tools import DataMaskingTool

# 创建脱敏工具
masking_tool = DataMaskingTool()

# 脱敏手机号
result = masking_tool.run(
    text="我的手机号是13812345678",
    types=["phone"]
)
print(f"脱敏后: {result['masked_text']}")
# 输出: 我的手机号是138****5678

# 脱敏所有PII类型
result = masking_tool.run(
    text="联系方式：手机13812345678，邮箱user@example.com",
    types=["all"]
)
print(f"脱敏后: {result['masked_text']}")
```

#### 3. 自定义敏感词库

```python
# 使用自定义敏感词库
custom_words = {"自定义敏感词", "特殊词汇", "品牌名称"}
filter_tool = SensitiveWordFilterTool(custom_words)

result = filter_tool.run(
    text="这包含自定义敏感词",
    strategy="asterisk"
)
print(f"过滤后: {result['filtered_text']}")
```

## 使用场景

### 场景1: 客服Agent

保护客服系统中的用户隐私数据：

```python
# 客户查询
customer_query = "我的订单号是ORDER-123，手机号13812345678"

# 1. 检查敏感词
filter_result = filter_tool.run(text=customer_query, strategy="detect_only")
if filter_result['has_sensitive_words']:
    print("⚠️ 警告：检测到敏感词")

# 2. 脱敏后记录日志
mask_result = masking_tool.run(text=customer_query, types=["all"])
logger.info(f"客户查询: {mask_result['masked_text']}")
# 日志: 客户查询: 我的订单号是ORDER-123，手机号138****5678
```

### 场景2: 内容审核

审核用户生成内容（UGC）：

```python
# 用户评论
user_comment = "这个产品真不错！"

# 检测敏感词
result = filter_tool.run(text=user_comment, strategy="detect_only")

if result['has_sensitive_words']:
    print("❌ 评论被拒绝：包含敏感词")
    # 记录违规内容
    logger.warning(f"违规评论: {result['detected_words']}")
else:
    print("✅ 评论通过审核")
    # 发布评论
```

### 场景3: 日志脱敏

确保日志系统中不包含敏感信息：

```python
import logging

# 配置脱敏日志处理器
masking_tool = DataMaskingTool()

def log_with_masking(message: str):
    """脱敏后记录日志"""
    result = masking_tool.run(text=message, types=["all"])
    logging.info(result['masked_text'])

# 使用
log_with_masking("用户登录: phone=13812345678, email=user@example.com")
# 实际记录: 用户登录: phone=138****5678, email=us***@example.com
```

## 配置选项

### 敏感词过滤配置

**替换策略：**
- `ASTERISK` / `asterisk`: 替换为星号（默认）
- `DELETE` / `delete`: 删除敏感词
- `CUSTOM` / `custom`: 替换为自定义文本
- `DETECT_ONLY` / `detect_only`: 仅检测不替换

**Java示例：**
```java
args.put("strategy", "CUSTOM");
args.put("custom_replace", "[已屏蔽]");
```

**Python示例：**
```python
result = filter_tool.run(
    text="...",
    strategy="custom",
    custom_replace="[已屏蔽]"
)
```

### 数据脱敏配置

**PII类型：**
- `phone`: 手机号
- `id_card`: 身份证号
- `email`: 邮箱地址
- `bank_card`: 银行卡号
- `ip_address`: IP地址
- `password`: 密码字段
- `all`: 所有类型（默认）

**脱敏字符：**
- 默认使用 `*` 作为脱敏字符
- 可自定义脱敏字符

**Java示例：**
```java
args.put("types", Arrays.asList("phone", "email"));
args.put("mask_char", "#");
```

**Python示例：**
```python
result = masking_tool.run(
    text="...",
    types=["phone", "email"],
    mask_char="#"
)
```

### 安全回调配置（Java）

```java
SecurityCallback securityCallback = new SecurityCallback()
    // 启用敏感词过滤
    .enableSensitiveWordFilter(true)
    // 启用数据脱敏
    .enableDataMasking(true)
    // 检测到敏感词时是否阻断请求
    .setBlockOnSensitiveWord(false)
    // 是否脱敏日志内容
    .setMaskLogs(true);

// 添加自定义敏感词
securityCallback.addSensitiveWord("特殊敏感词");
securityCallback.addSensitiveWords(customWordSet);

// 获取安全事件记录
List<SecurityCallback.SecurityEvent> events = securityCallback.getSecurityEvents();
```

## 性能优化

### 敏感词过滤性能

- 使用 **DFA（Deterministic Finite Automaton）算法**
- 时间复杂度：O(n)，n为文本长度
- 空间复杂度：O(m)，m为敏感词总字符数
- 适合处理大量文本和大型词库

**性能基准（参考）：**
- 检测10KB文本：< 5ms
- 1000个敏感词词库加载：< 50ms
- 适用于实时处理场景

### 数据脱敏性能

- 使用正则表达式匹配
- 时间复杂度：O(n)，n为文本长度
- 支持并发处理

**性能基准（参考）：**
- 脱敏10KB文本（所有PII类型）：< 10ms
- 适用于高并发场景

## 最佳实践

### 1. 分层防护

```java
// 第1层：输入检查
Map<String, Object> inputCheck = filterTool.run(userInput, context).blockingFirst();
if ((Boolean) inputCheck.get("has_sensitive_words")) {
    throw new SecurityException("输入包含敏感词");
}

// 第2层：业务处理（使用安全回调）
// ... Agent执行 ...

// 第3层：输出脱敏
Map<String, Object> outputMask = maskingTool.run(response, context).blockingFirst();
String safeResponse = (String) outputMask.get("masked_text");
```

### 2. 敏感词库管理

```java
// 定期更新敏感词库
public class SensitiveWordManager {
    private SensitiveWordFilterTool filterTool;
    
    public void updateWordList(Set<String> newWords) {
        filterTool.addSensitiveWords(newWords);
        logger.info("敏感词库已更新，新增 {} 个词", newWords.size());
    }
    
    // 从配置中心加载
    public void loadFromConfig() {
        Set<String> words = configService.getSensitiveWords();
        filterTool = new SensitiveWordFilterTool(words);
    }
}
```

### 3. 日志安全

```python
class SecureLogger:
    """安全的日志记录器"""
    def __init__(self):
        self.masking_tool = DataMaskingTool()
    
    def log(self, message: str, level: str = "info"):
        """记录脱敏后的日志"""
        result = self.masking_tool.run(text=message, types=["all"])
        masked_message = result['masked_text']
        
        if level == "info":
            logging.info(masked_message)
        elif level == "warning":
            logging.warning(masked_message)
        elif level == "error":
            logging.error(masked_message)
```

### 4. 性能监控

```java
// 监控安全检查性能
public class SecurityMetrics {
    private final Meter filterMeter = registry.meter("security.filter");
    private final Timer filterTimer = registry.timer("security.filter.time");
    
    public Map<String, Object> filterWithMetrics(String text) {
        filterMeter.mark();
        return filterTimer.time(() -> {
            return filterTool.run(createArgs(text), context).blockingFirst();
        });
    }
}
```

## 合规性说明

### 数据保护法规

本安全能力帮助您的应用符合以下法规要求：

- **GDPR（欧盟通用数据保护条例）**：数据脱敏保护用户隐私
- **CCPA（加州消费者隐私法案）**：PII识别和保护
- **中国网络安全法**：敏感信息保护
- **个人信息保护法**：个人信息处理规范

### 审计和合规

```java
// 记录安全事件用于审计
List<SecurityCallback.SecurityEvent> events = securityCallback.getSecurityEvents();
for (SecurityCallback.SecurityEvent event : events) {
    auditLogger.log(event.getType(), event.getStage(), event.getMessage());
}
```

## 示例代码

完整示例请参考：

**Java：**
- [SecurityAgentTest.java](../ali-agentic-adk-java/ali-agentic-adk-extension/ali-agentic-example/src/test/java/com/alibaba/agentic/example/SecurityAgentTest.java)

**Python：**
- [security_example.py](../ali-agentic-adk-python/examples/security_demo/security_example.py)

## 常见问题

### Q1: 如何添加自定义敏感词？

**Java:**
```java
// 单个添加
filterTool.addSensitiveWord("自定义词");

// 批量添加
Set<String> words = new HashSet<>(Arrays.asList("词1", "词2", "词3"));
filterTool.addSensitiveWords(words);
```

**Python:**
```python
# 单个添加
filter_tool.add_sensitive_word("自定义词")

# 批量添加
filter_tool.add_sensitive_words({"词1", "词2", "词3"})
```

### Q2: 如何只脱敏特定类型的PII？

```python
# 只脱敏手机号和邮箱
result = masking_tool.run(
    text="...",
    types=["phone", "email"]
)
```

### Q3: 敏感词过滤会影响性能吗？

不会显著影响性能。使用DFA算法，即使是千级别的敏感词库，检测性能也在毫秒级别。建议：
- 合理控制词库大小（< 10000词）
- 定期清理无效敏感词
- 对超大文本可以分块处理

### Q4: 脱敏后的数据还能还原吗？

不能。脱敏是单向操作，原始数据无法从脱敏结果还原。如果需要还原，请：
- 在脱敏前保存原始数据
- 或使用加密而非脱敏

### Q5: 如何处理多语言敏感词？

工具支持Unicode字符，可以添加任何语言的敏感词：

```java
Set<String> multiLangWords = new HashSet<>(Arrays.asList(
    "English word",
    "中文敏感词",
    "日本語の単語",
    "한국어 단어"
));
SensitiveWordFilterTool filter = new SensitiveWordFilterTool(multiLangWords);
```

## 技术支持

如有问题或建议，请：
- 提交Issue: [GitHub Issues](https://github.com/AIDC-AI/Agentic-ADK/issues)
- 查看文档: [项目文档](../README_CN.md)

## 更新日志

### v1.0.0 (2025-10-27)
- ✅ 初始版本发布
- ✅ 敏感词过滤功能
- ✅ 数据脱敏功能
- ✅ 安全回调机制（Java）
- ✅ 完整示例和文档

