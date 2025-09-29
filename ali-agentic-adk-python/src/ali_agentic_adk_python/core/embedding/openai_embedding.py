# Copyright (C) 2025 AIDC-AI
# This project incorporates components from the Open Source Software below.
# The original copyright notices and the licenses under which we received such components are set forth below for informational purposes.
#
# Open Source Software Licensed under the MIT License:
# --------------------------------------------------------------------
# 1. vscode-extension-updater-gitlab 3.0.1 https://www.npmjs.com/package/vscode-extension-updater-gitlab
# Copyright (c) Microsoft Corporation. All rights reserved.
# Copyright (c) 2015 David Owens II
# Copyright (c) Microsoft Corporation.
# Terms of the MIT:
# --------------------------------------------------------------------
# MIT License
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


from __future__ import annotations

import logging
from typing import Any, Dict, List, Sequence

from openai import OpenAI

from ..common.exceptions import EmbeddingProviderError
from .basic_embedding import BasicEmbedding

logger = logging.getLogger(__name__)


class OpenAIEmbedding(BasicEmbedding):
    """Embedding provider that delegates to the OpenAI embeddings API."""

    def __init__(
        self,
        api_key: str,
        model: str = "text-embedding-3-small",
        *,
        base_url: str | None = None,
        dimensions: int | None = None,
        user: str | None = None,
        client_options: Dict[str, Any] | None = None,
        request_options: Dict[str, Any] | None = None,
    ) -> None:
        super().__init__(model=model)
        options: Dict[str, Any] = {"api_key": api_key}
        if base_url:
            options["base_url"] = base_url
        if client_options:
            options.update(client_options)
        self._client = OpenAI(**options)
        self._dimensions = dimensions
        self._user = user
        self._request_options = request_options.copy() if request_options else {}

    def embed_documents(self, texts: Sequence[str]) -> List[List[float]]:
        normalized_inputs = self._normalize_inputs(texts)
        if not normalized_inputs:
            return []

        payload: Dict[str, Any] = {
            "model": self.model,
            "input": normalized_inputs,
        }
        if self._dimensions is not None:
            payload["dimensions"] = self._dimensions
        if self._user is not None:
            payload["user"] = self._user
        payload.update(self._request_options)

        try:
            response = self._client.embeddings.create(**payload)
        except Exception as exc:  # pragma: no cover - propagated for callers
            message = "Failed to retrieve embeddings from OpenAI provider"
            logger.exception(message)
            raise EmbeddingProviderError(message, original_exception=exc) from exc

        embeddings: List[List[float]] = []
        for item in response.data:
            vector = list(getattr(item, "embedding", []) or [])
            if not vector:
                raise EmbeddingProviderError("OpenAI response did not contain embedding vectors")
            embeddings.append(vector)

        return embeddings


__all__ = ["OpenAIEmbedding"]
