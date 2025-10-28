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
Security Tools - 安全工具集

Provides sensitive word filtering and data masking capabilities.
提供敏感词过滤和数据脱敏能力。
"""

from typing import Dict, List, Set, Tuple, Any, Optional
from enum import Enum
import re
from dataclasses import dataclass


class ReplaceStrategy(Enum):
    """Replace strategy for sensitive words."""
    ASTERISK = "asterisk"
    DELETE = "delete"
    CUSTOM = "custom"
    DETECT_ONLY = "detect_only"


class PIIType(Enum):
    """PII types for data masking."""
    PHONE = ("phone", r"1[3-9]\d{9}", 3, 4)
    ID_CARD = ("id_card", r"[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]", 6, 4)
    EMAIL = ("email", r"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}", 2, -1)
    BANK_CARD = ("bank_card", r"\d{13,19}", 4, 4)
    IP_ADDRESS = ("ip_address", r"\b(?:\d{1,3}\.){3}\d{1,3}\b", -1, -1)
    PASSWORD = ("password", r"(?i)(password|passwd|pwd)[\"']?\s*[:=]\s*[\"']?([^\"'\s,}]+)", -1, -1)

    def __init__(self, type_name: str, pattern: str, prefix_keep: int, suffix_keep: int):
        self.type_name = type_name
        self.pattern = re.compile(pattern)
        self.prefix_keep = prefix_keep
        self.suffix_keep = suffix_keep


@dataclass
class SensitiveWordResult:
    word: str
    start_index: int
    end_index: int


@dataclass
class PIIDetection:
    pii_type: str
    original_value: str
    masked_value: str
    start_index: int
    end_index: int


class SensitiveWordFilterTool:
    """Sensitive word filter tool using DFA algorithm."""

    def __init__(self, sensitive_words: Optional[Set[str]] = None):
        self.sensitive_word_map = {}
        words = sensitive_words if sensitive_words else self._get_default_sensitive_words()
        self._init_sensitive_word_map(words)

    def _init_sensitive_word_map(self, sensitive_words: Set[str]):
        if not sensitive_words:
            return

        for word in sensitive_words:
            if not word or not word.strip():
                continue

            current_map = self.sensitive_word_map
            for i, char in enumerate(word):
                if char not in current_map:
                    current_map[char] = {}

                if i == len(word) - 1:
                    current_map[char]["END"] = word

                current_map = current_map[char]

    def run(self, text: str, strategy: str = "asterisk", 
            custom_replace: str = "[已屏蔽]") -> Dict[str, Any]:
        if not text:
            return self._create_result(text, [], text, False)

        try:
            replace_strategy = ReplaceStrategy(strategy.lower())
        except ValueError:
            replace_strategy = ReplaceStrategy.ASTERISK

        detected_words = self._detect_sensitive_words(text)
        filtered_text = self._filter_text(text, detected_words, replace_strategy, custom_replace)

        return self._create_result(text, detected_words, filtered_text, len(detected_words) > 0)

    def _detect_sensitive_words(self, text: str) -> List[SensitiveWordResult]:
        results = []
        i = 0

        while i < len(text):
            length = self._check_sensitive_word(text, i)
            if length > 0:
                word = text[i:i + length]
                results.append(SensitiveWordResult(word, i, i + length))
                i += length
            else:
                i += 1

        return results

    def _check_sensitive_word(self, text: str, start_index: int) -> int:
        match_length = 0
        current_map = self.sensitive_word_map

        for i in range(start_index, len(text)):
            char = text[i]

            if char not in current_map:
                break

            match_length += 1

            if "END" in current_map[char]:
                return match_length  # 找到完整敏感词

            current_map = current_map[char]

        return 0

    def _filter_text(self, text: str, detected_words: List[SensitiveWordResult],
                     strategy: ReplaceStrategy, custom_replace: str) -> str:
        if not detected_words or strategy == ReplaceStrategy.DETECT_ONLY:
            return text

        result = list(text)
        offset = 0

        for word in detected_words:
            start = word.start_index + offset
            end = word.end_index + offset

            if strategy == ReplaceStrategy.ASTERISK:
                replacement = "*" * len(word.word)
            elif strategy == ReplaceStrategy.DELETE:
                replacement = ""
            elif strategy == ReplaceStrategy.CUSTOM:
                replacement = custom_replace
            else:
                replacement = word.word

            result[start:end] = replacement
            offset += len(replacement) - len(word.word)

        return "".join(result)

    def _create_result(self, original_text: str, detected_words: List[SensitiveWordResult],
                       filtered_text: str, has_sensitive_words: bool) -> Dict[str, Any]:
        return {
            "original_text": original_text,
            "filtered_text": filtered_text,
            "has_sensitive_words": has_sensitive_words,
            "detected_words_count": len(detected_words),
            "detected_words": [
                {
                    "word": word.word,
                    "start_index": word.start_index,
                    "end_index": word.end_index
                }
                for word in detected_words
            ]
        }

    def add_sensitive_word(self, word: str):
        self._init_sensitive_word_map({word})

    def add_sensitive_words(self, words: Set[str]):
        self._init_sensitive_word_map(words)

    @staticmethod
    def _get_default_sensitive_words() -> Set[str]:
        return {
            "政治敏感", "反动", "暴力",
            "色情", "黄色", "裸体",
            "赌博", "赌场", "赌钱",
            "毒品", "枪支", "爆炸物",
            "诈骗", "骗钱", "传销",
            "恐怖主义", "邪教", "非法",
        }


class DataMaskingTool:
    """Data masking tool for PII protection."""

    def run(self, text: str, types: List[str] = None, 
            mask_char: str = "*") -> Dict[str, Any]:
        if not text:
            return self._create_result(text, text, [])

        if types is None:
            types = ["all"]

        types_to_check = self._determine_types(types)

        masked_text, detected_pii = self._mask_text(text, types_to_check, mask_char)

        return self._create_result(text, masked_text, detected_pii)

    def _determine_types(self, enabled_types: List[str]) -> List[PIIType]:
        if "all" in enabled_types:
            return list(PIIType)

        types = []
        for type_str in enabled_types:
            for pii_type in PIIType:
                if pii_type.type_name == type_str.lower():
                    types.append(pii_type)
                    break

        return types

    def _mask_text(self, text: str, types: List[PIIType], 
                   mask_char: str) -> Tuple[str, List[PIIDetection]]:
        masked_text = text
        detected_pii = []

        for pii_type in types:
            matches = list(pii_type.pattern.finditer(masked_text))

            for match in matches:
                original = match.group()
                start, end = match.span()

                if pii_type == PIIType.PASSWORD and match.lastindex >= 2:
                    original = match.group(2)
                    masked = match.group(1) + re.sub(r'[^"\':=\s]', mask_char, 
                                                     match.group(0)[len(match.group(1)):])
                else:
                    masked = self._mask_string(original, pii_type.prefix_keep, 
                                               pii_type.suffix_keep, mask_char)

                detection = PIIDetection(
                    pii_type=pii_type.type_name,
                    original_value=original,
                    masked_value=masked,
                    start_index=start,
                    end_index=end
                )
                detected_pii.append(detection)

                masked_text = masked_text[:start] + masked + masked_text[end:]

        return masked_text, detected_pii

    def _mask_string(self, text: str, prefix_keep: int, suffix_keep: int, 
                     mask_char: str) -> str:
        if not text:
            return text

        length = len(text)

        if suffix_keep == -1 and "@" in text:
            parts = text.split("@")
            if len(parts) == 2:
                local_part = parts[0]
                keep_length = min(prefix_keep, len(local_part) // 2)
                masked_local = local_part[:keep_length] + mask_char * max(len(local_part) - keep_length, 3)
                return masked_local + "@" + parts[1]

        if prefix_keep == -1 and suffix_keep == -1:
            return mask_char * min(length, 10)

        if length <= prefix_keep + suffix_keep:
            return mask_char * length

        prefix = text[:prefix_keep]
        suffix = text[-suffix_keep:]
        mask_length = length - prefix_keep - suffix_keep

        return prefix + mask_char * mask_length + suffix

    def _create_result(self, original_text: str, masked_text: str,
                       detected_pii: List[PIIDetection]) -> Dict[str, Any]:
        return {
            "original_text": original_text,
            "masked_text": masked_text,
            "has_pii": len(detected_pii) > 0,
            "pii_count": len(detected_pii),
            "detected_pii": [
                {
                    "type": pii.pii_type,
                    "original_value": pii.original_value,
                    "masked_value": pii.masked_value,
                    "start_index": pii.start_index,
                    "end_index": pii.end_index
                }
                for pii in detected_pii
            ]
        }
