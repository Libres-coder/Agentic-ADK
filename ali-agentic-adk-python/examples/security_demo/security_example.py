# Copyright (C) 2025 AIDC-AI
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

"""
Security Features Example - 安全能力示例

Demonstrates how to use security tools to protect Agent applications.
演示如何使用安全工具保护Agent应用。
"""

from ali_agentic_adk_python.core.tool.security_tools import (
    SensitiveWordFilterTool,
    DataMaskingTool,
    ReplaceStrategy
)


def demo_sensitive_word_filter():
    """演示敏感词过滤工具"""
    print("\n" + "="*60)
    print("示例1: 敏感词过滤工具")
    print("="*60)

    filter_tool = SensitiveWordFilterTool()

    print("\n[检测模式]")
    result1 = filter_tool.run(
        text="这是一段包含赌博和诈骗的文本内容",
        strategy="detect_only"
    )
    print(f"原始文本: {result1['original_text']}")
    print(f"检测到敏感词: {result1['has_sensitive_words']}")
    print(f"敏感词数量: {result1['detected_words_count']}")
    print(f"敏感词列表: {result1['detected_words']}")

    print("\n[星号替换模式]")
    result2 = filter_tool.run(
        text="我想要赌博赚快钱，有人教我诈骗",
        strategy="asterisk"
    )
    print(f"原始文本: {result2['original_text']}")
    print(f"过滤后文本: {result2['filtered_text']}")

    print("\n[自定义替换模式]")
    result3 = filter_tool.run(
        text="非法内容和暴力内容不应该出现",
        strategy="custom",
        custom_replace="[已屏蔽]"
    )
    print(f"原始文本: {result3['original_text']}")
    print(f"过滤后文本: {result3['filtered_text']}")

    print("\n[删除模式]")
    result4 = filter_tool.run(
        text="这里有黄色和色情内容",
        strategy="delete"
    )
    print(f"原始文本: {result4['original_text']}")
    print(f"过滤后文本: {result4['filtered_text']}")

    print("\n[自定义词库]")
    custom_filter = SensitiveWordFilterTool({"测试敏感词", "自定义词汇"})
    result5 = custom_filter.run(
        text="这包含测试敏感词和自定义词汇",
        strategy="asterisk"
    )
    print(f"原始文本: {result5['original_text']}")
    print(f"过滤后文本: {result5['filtered_text']}")


def demo_data_masking():
    """演示数据脱敏工具"""
    print("\n" + "="*60)
    print("示例2: 数据脱敏工具")
    print("="*60)

    masking_tool = DataMaskingTool()

    print("\n[手机号脱敏]")
    result1 = masking_tool.run(
        text="我的手机号是13812345678，联系我吧",
        types=["phone"]
    )
    print(f"原始文本: {result1['original_text']}")
    print(f"脱敏后文本: {result1['masked_text']}")
    print(f"检测到的PII: {result1['detected_pii']}")

    print("\n[身份证号脱敏]")
    result2 = masking_tool.run(
        text="我的身份证号是110101199001011234",
        types=["id_card"]
    )
    print(f"原始文本: {result2['original_text']}")
    print(f"脱敏后文本: {result2['masked_text']}")

    print("\n[邮箱脱敏]")
    result3 = masking_tool.run(
        text="联系邮箱: user@example.com",
        types=["email"]
    )
    print(f"原始文本: {result3['original_text']}")
    print(f"脱敏后文本: {result3['masked_text']}")

    print("\n[银行卡号脱敏]")
    result4 = masking_tool.run(
        text="银行卡号: 6222021234567890123",
        types=["bank_card"]
    )
    print(f"原始文本: {result4['original_text']}")
    print(f"脱敏后文本: {result4['masked_text']}")

    print("\n[IP地址脱敏]")
    result5 = masking_tool.run(
        text="服务器IP是192.168.1.100",
        types=["ip_address"]
    )
    print(f"原始文本: {result5['original_text']}")
    print(f"脱敏后文本: {result5['masked_text']}")

    print("\n[综合测试 - 所有PII类型]")
    result6 = masking_tool.run(
        text="用户信息: 手机13812345678，邮箱user@test.com，身份证110101199001011234，IP地址192.168.1.1",
        types=["all"]
    )
    print(f"原始文本: {result6['original_text']}")
    print(f"脱敏后文本: {result6['masked_text']}")
    print(f"PII数量: {result6['pii_count']}")
    print(f"检测到的PII: {result6['detected_pii']}")


def demo_secure_logging():
    """演示安全日志记录"""
    print("\n" + "="*60)
    print("示例3: 安全日志记录")
    print("="*60)

    masking_tool = DataMaskingTool()

    log_messages = [
        "用户登录: username=zhangsan, phone=13812345678",
        "订单创建: order_id=ORD123, email=customer@example.com, amount=999",
        "支付成功: card_number=6222021234567890, amount=500",
        "错误日志: IP 192.168.1.100 访问被拒绝",
    ]

    print("\n[原始日志 vs 脱敏日志]")
    for i, log_msg in enumerate(log_messages, 1):
        result = masking_tool.run(text=log_msg, types=["all"])
        print(f"\n日志{i}:")
        print(f"  原始: {log_msg}")
        print(f"  脱敏: {result['masked_text']}")
        if result['has_pii']:
            print(f"  ⚠️  检测到 {result['pii_count']} 个PII")


def demo_secure_customer_service():
    """演示安全的客服Agent"""
    print("\n" + "="*60)
    print("示例4: 安全的客服Agent")
    print("="*60)

    filter_tool = SensitiveWordFilterTool()
    masking_tool = DataMaskingTool()

    print("\n[场景: 客户咨询订单问题]")
    customer_query = "您好，我的订单号是ORDER-123，手机号13812345678，想查询订单状态"

    filter_result = filter_tool.run(
        text=customer_query,
        strategy="detect_only"
    )
    print(f"客户输入: {customer_query}")
    print(f"敏感词检测: {'发现敏感词' if filter_result['has_sensitive_words'] else '✅ 通过'}")

    mask_result = masking_tool.run(
        text=customer_query,
        types=["all"]
    )
    print(f"日志记录（脱敏）: {mask_result['masked_text']}")

    print("\n[Agent处理并响应]")
    agent_response = (
        "您好！您的订单ORDER-123状态为已发货。"
        "稍后会发送短信到手机 13812345678。"
        "如有问题请联系客服邮箱 support@company.com"
    )

    response_mask = masking_tool.run(
        text=agent_response,
        types=["all"]
    )
    print(f"原始响应: {agent_response}")
    print(f"日志记录（脱敏）: {response_mask['masked_text']}")
    print(f"检测到PII数量: {response_mask['pii_count']}")

    print("\n[安全统计]")
    print("所有敏感数据已被正确处理")
    print("PII信息已被脱敏保护")
    print("可以安全地记录到日志系统")


def demo_content_moderation():
    """演示内容审核场景"""
    print("\n" + "="*60)
    print("示例5: 内容审核场景")
    print("="*60)

    filter_tool = SensitiveWordFilterTool()

    user_contents = [
        "这是一条正常的评论",
        "这个产品真不错，值得推荐！",
        "有人想一起赌博吗？",
        "我知道诈骗的方法，私信我",
        "非法内容，包含暴力和色情",
    ]

    print("\n[内容审核结果]")
    approved_count = 0
    rejected_count = 0

    for i, content in enumerate(user_contents, 1):
        result = filter_tool.run(
            text=content,
            strategy="detect_only"
        )

        if result['has_sensitive_words']:
            status = "拒绝"
            rejected_count += 1
            details = f"(发现 {result['detected_words_count']} 个敏感词)"
        else:
            status = "通过"
            approved_count += 1
            details = ""

        print(f"\n内容{i}: {content}")
        print(f"  状态: {status} {details}")

    print(f"\n[统计]")
    print(f"通过: {approved_count}, 拒绝: {rejected_count}")


def demo_performance():
    """演示性能测试"""
    print("\n" + "="*60)
    print("示例6: 性能测试")
    print("="*60)

    import time

    filter_tool = SensitiveWordFilterTool()
    masking_tool = DataMaskingTool()

    test_text = "这是一段测试文本，包含手机号13812345678，邮箱test@example.com，以及一些赌博诈骗等敏感词汇。" * 10

    print("\n[敏感词过滤性能]")
    start_time = time.time()
    iterations = 100
    for _ in range(iterations):
        filter_tool.run(text=test_text, strategy="detect_only")
    end_time = time.time()
    print(f"执行 {iterations} 次，耗时: {(end_time - start_time):.3f} 秒")
    print(f"平均每次: {((end_time - start_time) / iterations * 1000):.2f} 毫秒")

    print("\n[数据脱敏性能]")
    start_time = time.time()
    for _ in range(iterations):
        masking_tool.run(text=test_text, types=["all"])
    end_time = time.time()
    print(f"执行 {iterations} 次，耗时: {(end_time - start_time):.3f} 秒")
    print(f"平均每次: {((end_time - start_time) / iterations * 1000):.2f} 毫秒")


def main():
    """运行所有示例"""
    print("\n" + "="*60)
    print("安全能力示例 - Ali Agentic ADK Python")
    print("="*60)

    demo_sensitive_word_filter()
    demo_data_masking()
    demo_secure_logging()
    demo_secure_customer_service()
    demo_content_moderation()
    demo_performance()

    print("\n" + "="*60)
    print("所有示例完成！")
    print("\n安全措施总结:")
    print("1. 敏感词过滤 - 保护应用免受不当内容影响")
    print("2. 数据脱敏 - 保护用户隐私信息")
    print("3. 安全日志 - 合规的日志记录方式")
    print("4. 内容审核 - 自动化的内容安全检查")
    print("="*60 + "\n")


if __name__ == "__main__":
    main()
