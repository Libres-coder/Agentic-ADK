package com.alibaba.langengine.core.config;

import com.alibaba.langengine.core.caches.BaseCache;
import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.util.NullAwareBeanUtilsBean;
import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * 可传递的上下文，封装配置对象。提供向后兼容的默认构造。
 */
public class LangEngineContext {

	private final LangEngineConfig config;

	public LangEngineContext(LangEngineConfig config) {
		this.config = config;
	}

	public LangEngineConfig getConfig() {
		return config;
	}

	/**
	 * 向后兼容的默认上下文：尽量从旧的静态配置桥接，避免一次性大改。
	 */
	public static LangEngineContext defaultContext() {
		BaseCache cache = LangEngineConfiguration.CurrentCache;
		BaseCallbackManager cm = LangEngineConfiguration.CALLBACK_MANAGER != null
				? LangEngineConfiguration.CALLBACK_MANAGER
				: new CallbackManager();
		NullAwareBeanUtilsBean beanUtils = LangEngineConfiguration.NULL_AWARE_BEAN_UTILS_BEAN != null
				? LangEngineConfiguration.NULL_AWARE_BEAN_UTILS_BEAN
				: new NullAwareBeanUtilsBean();
		Integer recommend = null;
		try {
			recommend = Integer.parseInt(
					WorkPropertiesUtils.get("retrieval_qa_recommend_count", "2"));
		} catch (Throwable ignore) {
		}

		LangEngineConfig cfg = LangEngineConfig.builder()
				.cache(cache)
				.callbackManager(cm)
				.beanUtils(beanUtils)
				.retrievalQaRecommendCount(recommend)
				.build();
		return new LangEngineContext(cfg);
	}
}
