package com.alibaba.langengine.wework.model;

import lombok.Data;
import java.util.List;

@Data
public class DepartmentListResponse {
    private int errcode;
    private String errmsg;
    private List<Department> department;
    
    @Data
    public static class Department {
        private int id;
        private String name;
        private int parentid;
        private int order;
        private String name_en;
        private String department_leader;
    }
}
