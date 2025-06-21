package com.lonelyash.service;

import com.lonelyash.dto.EmployeeDTO;
import com.lonelyash.dto.EmployeeLoginDTO;
import com.lonelyash.dto.EmployeePageQueryDTO;
import com.lonelyash.entity.Employee;
import com.lonelyash.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    void updateStatus(Integer status, long id);

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    Employee getById(long id);

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    void update(EmployeeDTO employeeDTO);
}
