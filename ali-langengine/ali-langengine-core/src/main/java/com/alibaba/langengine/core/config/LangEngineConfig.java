package com.alibaba.langengine.core.config;

import com.alibaba.langengine.core.caches.BaseCache;
import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.util.NullAwareBeanUtilsBean;

/**
 * 不可变的引擎配置，支持通过构建器注入依赖。
 */
public final class LangEngineConfig {

	private final BaseCache cache;
	private final BaseCallbackManager callbackManager;
	private final NullAwareBeanUtilsBean beanUtils;
	private final Integer retrievalQaRecommendCount;

	private LangEngineConfig(Builder builder) {
		this.cache = builder.cache;
		this.callbackManager = builder.callbackManager;
		this.beanUtils = builder.beanUtils;
		this.retrievalQaRecommendCount = builder.retrievalQaRecommendCount;
	}

	public BaseCache getCache() {
		return cache;
	}

	public BaseCallbackManager getCallbackManager() {
		return callbackManager;
	}

	public NullAwareBeanUtilsBean getBeanUtils() {
		return beanUtils;
	}

	public Integer getRetrievalQaRecommendCount() {
		return retrievalQaRecommendCount;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private BaseCache cache;
		private BaseCallbackManager callbackManager;
		private NullAwareBeanUtilsBean beanUtils;
		private Integer retrievalQaRecommendCount;

		public Builder cache(BaseCache cache) {
			this.cache = cache;
			return this;
		}

		public Builder callbackManager(BaseCallbackManager callbackManager) {
			this.callbackManager = callbackManager;
			return this;
		}

		public Builder beanUtils(NullAwareBeanUtilsBean beanUtils) {
			this.beanUtils = beanUtils;
			return this;
		}

		public Builder retrievalQaRecommendCount(Integer count) {
			this.retrievalQaRecommendCount = count;
			return this;
		}

		public LangEngineConfig build() {
			return new LangEngineConfig(this);
		}
	}
}
