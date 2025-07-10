package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 统计指定时间内的营业额
     * @param beginDate
     * @param endDate
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate beginDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>(); // 存放从 begin 到 end 每一天的日期

        dates.add(beginDate);

        while (!beginDate.equals(endDate)) {
            beginDate = beginDate.plusDays(1);
            dates.add(beginDate);
        }

        List<Double> turnoverList = new ArrayList<>();

        dates.forEach(date->{
            // 查询date日期对应的营业额数据
            LocalDateTime begin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
            // select sum(amount) from orders where order_time > begin and order_time < start and status = 5
            Map map = new HashMap();
            map.put("begin", begin);
            map.put("end", end);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = (turnover == null ? 0 : turnover);
            turnoverList.add(turnover);
        });

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dates, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate beginDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>(); // 存放从 begin 到 end 每一天的日期

        dates.add(beginDate);

        while (!beginDate.equals(endDate)) {
            beginDate = beginDate.plusDays(1);
            dates.add(beginDate);
        }

        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        dates.forEach(date->{
            // 查询date日期对应的营业额数据
            LocalDateTime begin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", end);
            totalUserList.add(userMapper.countByMap(map));
            map.put("begin", begin);
            newUserList.add(userMapper.countByMap(map));
        });

        return UserReportVO.builder()
                .dateList(StringUtils.join(dates, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate beginDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        dates.add(beginDate);
        while (!beginDate.equals(endDate)) {
            beginDate = beginDate.plusDays(1);
            dates.add(beginDate);
        }

        List<Integer> totalOrderList = new ArrayList<>();
        List<Integer> validOrderList = new ArrayList<>();

        dates.forEach(date->{
            LocalDateTime begin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
            Integer orderCount = getOrderCount(begin, end, null);
            Integer validOrderCount = getOrderCount(begin, end, Orders.COMPLETED);

            totalOrderList.add(orderCount);
            validOrderList.add(validOrderCount);
        });

        Integer totalOrder = totalOrderList.stream().reduce(Integer::sum).get();
        Integer validOrder = validOrderList.stream().reduce(Integer::sum).get();
        Double route = 0.0;
        if (totalOrder != 0) {
            route = validOrder.doubleValue() / totalOrder;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dates, ","))
                .validOrderCountList(StringUtils.join(validOrderList, ","))
                .validOrderCount(validOrder)
                .orderCountList(StringUtils.join(totalOrderList, ","))
                .totalOrderCount(totalOrder)
                .orderCompletionRate(route)
                .build();
    }

    @Override
    public SalesTop10ReportVO getTop10Statistics(LocalDate beginDate, LocalDate endDate) {
        LocalDateTime begin = LocalDateTime.of(beginDate, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOS = orderMapper.getSalesTop10(begin, end);
        List<String> names = goodsSalesDTOS.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> nums = goodsSalesDTOS.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");
        String numList = StringUtils.join(nums, ",");
        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numList)
                .build();
    }

    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }

}
