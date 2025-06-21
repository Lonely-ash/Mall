package com.lonelyash.service;

import com.lonelyash.vo.OrderReportVO;
import com.lonelyash.vo.SalesTop10ReportVO;
import com.lonelyash.vo.TurnoverReportVO;
import com.lonelyash.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {

    TurnoverReportVO getTurnoverStatistics(LocalDate startDate, LocalDate endDate);

    UserReportVO getUserStatistics(LocalDate startDate, LocalDate endDate);

    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

    /**
     * 导出运营数据报表
     * @param response
     */
    void reportBusinessData(HttpServletResponse response);
}
