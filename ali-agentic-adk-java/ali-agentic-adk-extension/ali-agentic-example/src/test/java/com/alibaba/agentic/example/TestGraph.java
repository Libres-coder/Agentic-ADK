package com.alibaba.agentic.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/8/4 10:34
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@ActiveProfiles("testing")
public class TestGraph {


    @Test
    public void graph() {

    }
}
