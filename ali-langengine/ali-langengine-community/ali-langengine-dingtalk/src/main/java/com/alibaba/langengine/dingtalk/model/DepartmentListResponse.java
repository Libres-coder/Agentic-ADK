package com.alibaba.langengine.dingtalk.model;

import lombok.Data;
import java.util.List;

@Data
public class DepartmentListResponse {
    private int errcode;
    private String errmsg;
    private List<Department> result;
    
    @Data
    public static class Department {
        private Long deptId;
        private String name;
        private Long parentId;
        private String sourceIdentifier;
        private Long order;
        private boolean createDeptGroup;
        private boolean autoAddUser;
        private String deptManagerUseridList;
        private String outerDept;
        private String outerPermitDepts;
        private String outerPermitUsers;
        private String hideDept;
        private String userPermits;
        private String deptPermits;
        private String outerDepartment;
        private String outerUser;
        private String deptGroupChatId;
        private String groupContainSubDept;
        private String orgDeptOwner;
        private String deptGroupContainOuterDept;
        private String deptGroupContainHiddenDept;
        private String deptGroupContainSubDept;
        private String deptGroupContainAutoAddUser;
        private String deptGroupContainOuterUser;
        private String deptGroupContainOuterPermitDepts;
        private String deptGroupContainOuterPermitUsers;
        private String deptGroupContainHideDept;
        private String deptGroupContainUserPermits;
        private String deptGroupContainDeptPermits;
        private String deptGroupContainOuterDepartment;
        private String deptGroupContainOuterUser;
    }
}
