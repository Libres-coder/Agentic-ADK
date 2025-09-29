package com.alibaba.langengine.rockset.vectorstore.service;

import lombok.Data;


@Data
public class RocksetDeleteResponse {
    private int deletedCount;
    private String status;
}
