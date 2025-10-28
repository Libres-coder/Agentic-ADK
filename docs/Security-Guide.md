# Agentic ADK Security Guide

## Overview

Agentic ADK provides a comprehensive set of security capabilities to help developers build secure and compliant AI Agent applications. This guide introduces how to use these security features to protect your application from security threats and ensure user privacy data is properly protected.

## Security Capabilities

### 1. Sensitive Word / Blacklist Filtering

Automatically detect and filter sensitive words in text, including but not limited to:
- Political sensitive words
- Pornographic content
- Gambling related
- Prohibited items information
- Fraud related
- Other inappropriate content

**Features:**
- Support custom sensitive word library
- High-performance matching using DFA algorithm
- Multiple replacement strategies (asterisk, delete, custom)
- Detect-only mode (without modifying original text)

### 2. Data Masking (PII Protection)

Automatically identify and mask Personally Identifiable Information (PII) to protect user privacy:
- Chinese mainland phone numbers (11 digits)
- ID card numbers (18 digits)
- Email addresses
- Bank card numbers (13-19 digits)
- IP addresses
- Password fields

**Features:**
- Automatically identify multiple PII types
- Retain prefix and suffix for tracking
- Selectively enable/disable specific types
- Maintain data format integrity

### 3. Security Callback (Java)

Automatically perform security checks before and after Agent execution:
- Request phase security check
- Response phase security check
- Security event logging
- Configurable blocking strategy

## Quick Start

### Java Version

#### 1. Sensitive Word Filtering

```java
import com.alibaba.agentic.core.tools.security.SensitiveWordFilterTool;
import com.alibaba.agentic.core.executor.SystemContext;

// Create filter tool
SensitiveWordFilterTool filterTool = new SensitiveWordFilterTool();
SystemContext context = new SystemContext();

// Detect sensitive words
Map<String, Object> args = new HashMap<>();
args.put("text", "This text contains gambling and fraud");
args.put("strategy", "DETECT_ONLY");

Map<String, Object> result = filterTool.run(args, context).blockingFirst();
System.out.println("Has sensitive words: " + result.get("has_sensitive_words"));
System.out.println("Word count: " + result.get("detected_words_count"));

// Filter sensitive words (asterisk replacement)
args.put("strategy", "ASTERISK");
result = filterTool.run(args, context).blockingFirst();
System.out.println("Filtered: " + result.get("filtered_text"));
```

#### 2. Data Masking

```java
import com.alibaba.agentic.core.tools.security.DataMaskingTool;

// Create masking tool
DataMaskingTool maskingTool = new DataMaskingTool();

// Mask phone number
Map<String, Object> args = new HashMap<>();
args.put("text", "My phone is 13812345678");
args.put("types", Arrays.asList("phone"));

Map<String, Object> result = maskingTool.run(args, context).blockingFirst();
System.out.println("Masked: " + result.get("masked_text"));
// Output: My phone is 138****5678
```

#### 3. Security Callback

```java
import com.alibaba.agentic.core.executor.SecurityCallback;

// Create security callback
SecurityCallback securityCallback = new SecurityCallback()
    .enableSensitiveWordFilter(true)
    .enableDataMasking(true)
    .setBlockOnSensitiveWord(false)
    .setMaskLogs(true);

// Add custom sensitive word
securityCallback.addSensitiveWord("custom_word");

// Use in Agent execution chain
// securityCallback.execute(systemContext, request, result, chain);
```

### Python Version

#### 1. Sensitive Word Filtering

```python
from ali_agentic_adk_python.core.tool.security_tools import SensitiveWordFilterTool

# Create filter tool
filter_tool = SensitiveWordFilterTool()

# Detect sensitive words
result = filter_tool.run(
    text="This text contains gambling and fraud",
    strategy="detect_only"
)
print(f"Has sensitive words: {result['has_sensitive_words']}")
print(f"Word count: {result['detected_words_count']}")

# Filter sensitive words (asterisk replacement)
result = filter_tool.run(
    text="This text contains gambling and fraud",
    strategy="asterisk"
)
print(f"Filtered: {result['filtered_text']}")
```

#### 2. Data Masking

```python
from ali_agentic_adk_python.core.tool.security_tools import DataMaskingTool

# Create masking tool
masking_tool = DataMaskingTool()

# Mask phone number
result = masking_tool.run(
    text="My phone is 13812345678",
    types=["phone"]
)
print(f"Masked: {result['masked_text']}")
# Output: My phone is 138****5678

# Mask all PII types
result = masking_tool.run(
    text="Contact: phone 13812345678, email user@example.com",
    types=["all"]
)
print(f"Masked: {result['masked_text']}")
```

## Use Cases

### Case 1: Customer Service Agent

Protect user privacy data in customer service systems:

```python
# Customer query
customer_query = "My order is ORDER-123, phone 13812345678"

# 1. Check sensitive words
filter_result = filter_tool.run(text=customer_query, strategy="detect_only")
if filter_result['has_sensitive_words']:
    print("⚠️ Warning: Sensitive words detected")

# 2. Log with masking
mask_result = masking_tool.run(text=customer_query, types=["all"])
logger.info(f"Customer query: {mask_result['masked_text']}")
# Log: Customer query: My order is ORDER-123, phone 138****5678
```

### Case 2: Content Moderation

Review user-generated content (UGC):

```python
# User comment
user_comment = "This product is great!"

# Detect sensitive words
result = filter_tool.run(text=user_comment, strategy="detect_only")

if result['has_sensitive_words']:
    print("❌ Comment rejected: Contains sensitive words")
    logger.warning(f"Violation: {result['detected_words']}")
else:
    print("✅ Comment approved")
```

### Case 3: Log Masking

Ensure logs don't contain sensitive information:

```python
import logging

# Configure masking logger
masking_tool = DataMaskingTool()

def log_with_masking(message: str):
    """Log with masking"""
    result = masking_tool.run(text=message, types=["all"])
    logging.info(result['masked_text'])

# Usage
log_with_masking("User login: phone=13812345678, email=user@example.com")
# Actual log: User login: phone=138****5678, email=us***@example.com
```

## Best Practices

### 1. Layered Protection

```java
// Layer 1: Input check
Map<String, Object> inputCheck = filterTool.run(userInput, context).blockingFirst();
if ((Boolean) inputCheck.get("has_sensitive_words")) {
    throw new SecurityException("Input contains sensitive words");
}

// Layer 2: Business processing (with security callback)
// ... Agent execution ...

// Layer 3: Output masking
Map<String, Object> outputMask = maskingTool.run(response, context).blockingFirst();
String safeResponse = (String) outputMask.get("masked_text");
```

### 2. Secure Logging

```python
class SecureLogger:
    """Secure logger"""
    def __init__(self):
        self.masking_tool = DataMaskingTool()
    
    def log(self, message: str, level: str = "info"):
        """Log with masking"""
        result = self.masking_tool.run(text=message, types=["all"])
        masked_message = result['masked_text']
        
        if level == "info":
            logging.info(masked_message)
        elif level == "warning":
            logging.warning(masked_message)
```

## Example Code

For complete examples, please refer to:

**Java:**
- [SecurityAgentTest.java](../ali-agentic-adk-java/ali-agentic-adk-extension/ali-agentic-example/src/test/java/com/alibaba/agentic/example/SecurityAgentTest.java)

**Python:**
- [security_example.py](../ali-agentic-adk-python/examples/security_demo/security_example.py)

## Technical Support

For questions or suggestions:
- Submit Issue: [GitHub Issues](https://github.com/AIDC-AI/Agentic-ADK/issues)
- Documentation: [Project Documentation](../README.md)

## Changelog

### v1.0.0 (2025-10-27)
- ✅ Initial release
- ✅ Sensitive word filtering
- ✅ Data masking
- ✅ Security callback mechanism (Java)
- ✅ Complete examples and documentation

