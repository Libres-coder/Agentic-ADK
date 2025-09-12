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


import hashlib
import time
import threading

class StringBasedLetterSnowflake:
    CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    CHAR_LENGTH = len(CHARS)

    sequence_bits = 12
    datacenter_id_bits = 10  # 增加到10位
    max_datacenter_id = -1 ^ (-1 << datacenter_id_bits)
    datacenter_id_shift = sequence_bits
    timestamp_left_shift = sequence_bits + datacenter_id_bits
    sequence_mask = -1 ^ (-1 << sequence_bits)

    def __init__(self, datacenter_id: str):
        self.datacenter_id = datacenter_id
        self.datacenter_id_hash = abs(self.hash_string(datacenter_id)) & self.max_datacenter_id
        self.sequence = 0
        self.last_timestamp = -1
        self.lock = threading.Lock()

    def hash_string(self, input_str: str) -> int:
        md5 = hashlib.md5()
        md5.update(input_str.encode('utf-8'))
        hash_bytes = md5.digest()
        return int.from_bytes(hash_bytes[:4], byteorder='big', signed=False)

    def next_id(self) -> str:
        with self.lock:
            timestamp = self.time_gen()
            if timestamp < self.last_timestamp:
                raise Exception("Clock moved backwards. Refusing to generate id")
            if self.last_timestamp == timestamp:
                self.sequence = (self.sequence + 1) & self.sequence_mask
                if self.sequence == 0:
                    timestamp = self.til_next_millis(self.last_timestamp)
            else:
                self.sequence = 0
            self.last_timestamp = timestamp
            id_val = ((timestamp - 1288834974657) << self.timestamp_left_shift) | \
                     (self.datacenter_id_hash << self.datacenter_id_shift) | \
                     self.sequence
            return self.convert_to_letters(id_val)

    def convert_to_letters(self, id_val: int) -> str:
        sb = []
        while id_val > 0:
            sb.append(self.CHARS[id_val % self.CHAR_LENGTH])
            id_val //= self.CHAR_LENGTH
        return ''.join(reversed(sb)) if sb else self.CHARS[0]

    def til_next_millis(self, last_timestamp: int) -> int:
        timestamp = self.time_gen()
        while timestamp <= last_timestamp:
            timestamp = self.time_gen()
        return timestamp

    def time_gen(self) -> int:
        return int(time.time() * 1000)

if __name__ == "__main__":
    id_worker = StringBasedLetterSnowflake("my-datacenter")
    for _ in range(10):
        print(id_worker.next_id())