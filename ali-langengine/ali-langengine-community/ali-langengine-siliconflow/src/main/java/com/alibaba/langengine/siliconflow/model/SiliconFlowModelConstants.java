package com.alibaba.langengine.siliconflow.model;

/**
 * SiliconFlow model constants
 */
public class SiliconFlowModelConstants {

    // Default recommended model
    public static final String DEFAULT_MODEL = "deepseek-ai/DeepSeek-V2.5";

    // ================= DeepSeek Series =================
    public static final String DEEPSEEK_V2_5 = "deepseek-ai/DeepSeek-V2.5";
    public static final String DEEPSEEK_V3 = "deepseek-ai/DeepSeek-V3";
    public static final String DEEPSEEK_V3_1 = "deepseek-ai/DeepSeek-V3.1";
    public static final String DEEPSEEK_R1 = "deepseek-ai/DeepSeek-R1";
    public static final String DEEPSEEK_R1_DISTILL_7B = "deepseek-ai/DeepSeek-R1-Distill-Qwen-7B";
    public static final String DEEPSEEK_R1_DISTILL_14B = "deepseek-ai/DeepSeek-R1-Distill-Qwen-14B";
    public static final String DEEPSEEK_R1_DISTILL_32B = "deepseek-ai/DeepSeek-R1-Distill-Qwen-32B";
    public static final String DEEPSEEK_R1_0528_QWEN3_8B = "deepseek-ai/DeepSeek-R1-0528-Qwen3-8B";
    
    // DeepSeek Pro versions - Only available with recharged balance
    public static final String PRO_DEEPSEEK_V3 = "Pro/deepseek-ai/DeepSeek-V3";
    public static final String PRO_DEEPSEEK_V3_1 = "Pro/deepseek-ai/DeepSeek-V3.1";
    public static final String PRO_DEEPSEEK_R1 = "Pro/deepseek-ai/DeepSeek-R1";
    public static final String PRO_DEEPSEEK_R1_DISTILL_7B = "Pro/deepseek-ai/DeepSeek-R1-Distill-Qwen-7B";

    // ================= Qwen Series =================
    // Qwen2 Series
    public static final String QWEN2_7B_INSTRUCT = "Qwen/Qwen2-7B-Instruct";
    
    // Qwen2.5 Series
    public static final String QWEN2_5_7B_INSTRUCT = "Qwen/Qwen2.5-7B-Instruct";
    public static final String QWEN2_5_14B_INSTRUCT = "Qwen/Qwen2.5-14B-Instruct";
    public static final String QWEN2_5_32B_INSTRUCT = "Qwen/Qwen2.5-32B-Instruct";
    public static final String QWEN2_5_72B_INSTRUCT = "Qwen/Qwen2.5-72B-Instruct";
    public static final String QWEN2_5_72B_INSTRUCT_128K = "Qwen/Qwen2.5-72B-Instruct-128K";
    
    // Qwen2.5 Coder Series
    public static final String QWEN2_5_CODER_7B_INSTRUCT = "Qwen/Qwen2.5-Coder-7B-Instruct";
    public static final String QWEN2_5_CODER_32B_INSTRUCT = "Qwen/Qwen2.5-Coder-32B-Instruct";
    
    // Qwen3 Series
    public static final String QWEN3_8B = "Qwen/Qwen3-8B";
    public static final String QWEN3_14B = "Qwen/Qwen3-14B";
    public static final String QWEN3_32B = "Qwen/Qwen3-32B";
    public static final String QWEN3_30B_A3B = "Qwen/Qwen3-30B-A3B";
    public static final String QWEN3_235B_A22B = "Qwen/Qwen3-235B-A22B";
    public static final String QWEN3_235B_A22B_INSTRUCT_2507 = "Qwen/Qwen3-235B-A22B-Instruct-2507";
    public static final String QWEN3_235B_A22B_THINKING_2507 = "Qwen/Qwen3-235B-A22B-Thinking-2507";
    public static final String QWEN3_30B_A3B_INSTRUCT_2507 = "Qwen/Qwen3-30B-A3B-Instruct-2507";
    public static final String QWEN3_30B_A3B_THINKING_2507 = "Qwen/Qwen3-30B-A3B-Thinking-2507";
    
    // Qwen3 Coder Series
    public static final String QWEN3_CODER_480B_A35B_INSTRUCT = "Qwen/Qwen3-Coder-480B-A35B-Instruct";
    public static final String QWEN3_CODER_30B_A3B_INSTRUCT = "Qwen/Qwen3-Coder-30B-A3B-Instruct";
    
    // QwQ & QVQ Series
    public static final String QWQ_32B = "Qwen/QwQ-32B";
    public static final String QVQ_72B_PREVIEW = "Qwen/QVQ-72B-Preview";
    
    // QwenLong Series
    public static final String QWENLONG_L1_32B = "Tongyi-Zhiwen/QwenLong-L1-32B";

    // Qwen Pro versions - Only available with recharged balance
    public static final String PRO_QWEN2_7B_INSTRUCT = "Pro/Qwen/Qwen2-7B-Instruct";
    public static final String PRO_QWEN2_5_7B_INSTRUCT = "Pro/Qwen/Qwen2.5-7B-Instruct";
    public static final String PRO_QWEN2_5_CODER_7B_INSTRUCT = "Pro/Qwen/Qwen2.5-Coder-7B-Instruct";
    public static final String PRO_QWEN2_5_VL_7B_INSTRUCT = "Pro/Qwen/Qwen2.5-VL-7B-Instruct";

    // Qwen LoRA versions
    public static final String LORA_QWEN2_5_7B_INSTRUCT = "LoRA/Qwen/Qwen2.5-7B-Instruct";
    public static final String LORA_QWEN2_5_14B_INSTRUCT = "LoRA/Qwen/Qwen2.5-14B-Instruct";
    public static final String LORA_QWEN2_5_32B_INSTRUCT = "LoRA/Qwen/Qwen2.5-32B-Instruct";
    public static final String LORA_QWEN2_5_72B_INSTRUCT = "LoRA/Qwen/Qwen2.5-72B-Instruct";

    // ================= GLM Series =================
    public static final String GLM_4_9B_CHAT = "THUDM/glm-4-9b-chat";
    public static final String GLM_4_32B_0414 = "THUDM/GLM-4-32B-0414";
    public static final String GLM_4_9B_0414 = "THUDM/GLM-4-9B-0414";
    public static final String GLM_Z1_9B_0414 = "THUDM/GLM-Z1-9B-0414";
    public static final String GLM_Z1_32B_0414 = "THUDM/GLM-Z1-32B-0414";
    public static final String GLM_Z1_RUMINATION_32B_0414 = "THUDM/GLM-Z1-Rumination-32B-0414";
    public static final String GLM_4_1V_9B_THINKING = "THUDM/GLM-4.1V-9B-Thinking";
    
    // GLM Pro versions - Only available with recharged balance
    public static final String PRO_GLM_4_9B_CHAT = "Pro/THUDM/glm-4-9b-chat";
    public static final String PRO_GLM_4_1V_9B_THINKING = "Pro/THUDM/GLM-4.1V-9B-Thinking";
    
    // GLM ZAI versions
    public static final String GLM_4_5 = "zai-org/GLM-4.5";
    public static final String GLM_4_5_AIR = "zai-org/GLM-4.5-Air";
    public static final String GLM_4_5V = "zai-org/GLM-4.5V";

    // ================= InternLM Series =================
    public static final String INTERNLM2_5_7B_CHAT = "internlm/internlm2_5-7b-chat";

    // ================= MiniMax Series =================
    public static final String MINIMAX_M1_80K = "MiniMaxAI/MiniMax-M1-80k";

    // ================= Moonshot (Kimi) Series =================
    public static final String KIMI_DEV_72B = "moonshotai/Kimi-Dev-72B";
    public static final String KIMI_K2_INSTRUCT = "moonshotai/Kimi-K2-Instruct";
    // Only available with recharged balance
    public static final String PRO_KIMI_K2_INSTRUCT = "Pro/moonshotai/Kimi-K2-Instruct";

    // ================= Tencent Hunyuan Series =================
    public static final String HUNYUAN_A13B_INSTRUCT = "tencent/Hunyuan-A13B-Instruct";

    // ================= Baidu ERNIE Series =================
    public static final String ERNIE_4_5_300B_A47B = "baidu/ERNIE-4.5-300B-A47B";

    // ================= Ascend PanGu Series =================
    public static final String PANGU_PRO_MOE = "ascend-tribe/pangu-pro-moe";

    // ================= StepFun Series =================
    public static final String STEP3 = "stepfun-ai/step3";

    // ================= Seed Series =================
    public static final String SEED_RICE_7B = "SeedLLM/Seed-Rice-7B";
    public static final String SEED_OSS_36B_INSTRUCT = "ByteDance-Seed/Seed-OSS-36B-Instruct";

    // ================= Other Pro Models - Only available with recharged balance =================
    public static final String PRO_BAAI_BGE_M3 = "Pro/BAAI/bge-m3";
    public static final String PRO_BAAI_BGE_RERANKER_V2_M3 = "Pro/BAAI/bge-reranker-v2-m3";

    // ================= Common Model Categories =================
    
    /**
     * Get all available text generation models
     */
    public static final String[] TEXT_GENERATION_MODELS = {
        // DeepSeek Series
        DEEPSEEK_V2_5, DEEPSEEK_V3, DEEPSEEK_V3_1, DEEPSEEK_R1,
        
        // Qwen Series  
        QWEN2_5_7B_INSTRUCT, QWEN2_5_14B_INSTRUCT, QWEN2_5_32B_INSTRUCT, QWEN2_5_72B_INSTRUCT,
        QWEN2_5_CODER_7B_INSTRUCT, QWEN2_5_CODER_32B_INSTRUCT,
        QWEN3_8B, QWEN3_14B, QWEN3_32B,
        
        // GLM Series
        GLM_4_9B_CHAT, GLM_4_32B_0414, GLM_4_5, GLM_4_5_AIR,
        
        // Others
        INTERNLM2_5_7B_CHAT, MINIMAX_M1_80K, KIMI_K2_INSTRUCT, HUNYUAN_A13B_INSTRUCT
    };

    /**
     * Get coding specialized models
     */
    public static final String[] CODING_MODELS = {
        QWEN2_5_CODER_7B_INSTRUCT,
        QWEN2_5_CODER_32B_INSTRUCT,
        QWEN3_CODER_480B_A35B_INSTRUCT,
        QWEN3_CODER_30B_A3B_INSTRUCT
    };

    /**
     * Get reasoning specialized models
     */
    public static final String[] REASONING_MODELS = {
        DEEPSEEK_R1,
        DEEPSEEK_R1_DISTILL_7B,
        DEEPSEEK_R1_DISTILL_14B,
        DEEPSEEK_R1_DISTILL_32B,
        QWQ_32B,
        QVQ_72B_PREVIEW,
        QWEN3_235B_A22B_THINKING_2507,
        QWEN3_30B_A3B_THINKING_2507,
        GLM_4_1V_9B_THINKING,
        GLM_Z1_RUMINATION_32B_0414
    };

    /**
     * Get free tier models (commonly available)
     */
    public static final String[] FREE_TIER_MODELS = {
        QWEN2_7B_INSTRUCT,
        QWEN2_5_7B_INSTRUCT,
        QWEN2_5_CODER_7B_INSTRUCT,
        GLM_4_9B_CHAT,
        INTERNLM2_5_7B_CHAT,
        DEEPSEEK_R1_DISTILL_7B
    };
}