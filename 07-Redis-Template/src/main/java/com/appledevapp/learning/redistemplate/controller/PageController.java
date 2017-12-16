package com.appledevapp.learning.redistemplate.controller;

import com.appledevapp.learning.redistemplate.entity.MonthPayMent;
import com.appledevapp.learning.redistemplate.exception.ParameterInvalidException;
import com.appledevapp.learning.redistemplate.service.MonthPaymentService;
import com.appledevapp.learning.redistemplate.service.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;

/**
 * MVC模型控制器 定义路由以及响应
 */
@Controller
public class PageController {

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private MonthPaymentService monthPaymentService;

    /**
     * 获取数据库里列表并展示
     *
     * @return
     */
    @RequestMapping("/")
    public ModelAndView webIndex() throws Exception {

        //缓存10秒，然后获取失败则从数据库获取后，重新进入缓存
        List<MonthPayMent> recordList = redisCacheService.getCacheValue("indexCache");

        if (recordList == null) {
            //获取数据
            recordList = monthPaymentService.findPaymentList();

            redisCacheService.setCacheValueForTime("indexCache", recordList, 10);
        }

        //指定对应的视图模版名称，以及需要填充的数据
        ModelAndView modelView = new ModelAndView("index");
        modelView.addObject("payMentList", recordList);
        return modelView;
    }

    /**
     * 新增记录并返回记录id
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/add")
    public int addPayment() throws Exception {
        MonthPayMent payMent = new MonthPayMent();
        payMent.setName("RandomName" + System.currentTimeMillis());
        payMent.setSalary(BigDecimal.valueOf(12.34));
        int id = monthPaymentService.add(payMent);
        return id;
    }

    /**
     * 根据id去查找记录
     *
     * @param id
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/search/{id}")
    public MonthPayMent searchPayment(@PathVariable("id") int id) throws Exception {
        return monthPaymentService.findPaymentById(id);
    }

    /**
     * 更新数据
     *
     * @param id
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/update/{id}")
    public MonthPayMent updatePayment(@PathVariable("id") int id) throws Exception {
        MonthPayMent oldRecord = monthPaymentService.findPaymentById(id);
        if (oldRecord == null) {
            throw new ParameterInvalidException("记录不存在");
        }

        oldRecord.setName(String.valueOf(System.currentTimeMillis()));
        monthPaymentService.update(oldRecord);

        return monthPaymentService.findPaymentById(id);
    }

    /**
     * 更新数据
     *
     * @param id
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/updateflush/{id}")
    public MonthPayMent updatePaymentWithFlush(@PathVariable("id") int id) throws Exception {
        MonthPayMent oldRecord = monthPaymentService.findPaymentById(id);
        if (oldRecord == null) {
            throw new ParameterInvalidException("记录不存在");
        }

        oldRecord.setName(String.valueOf(System.currentTimeMillis()));
        monthPaymentService.updateUsingIndex(oldRecord);

        return monthPaymentService.findPaymentById(id);
    }

    /**
     * 更新数据
     *
     * @param id
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/updatenative/{id}")
    public MonthPayMent updatePaymentWithNative(@PathVariable("id") int id) throws Exception {
        MonthPayMent oldRecord = monthPaymentService.findPaymentById(id);
        if (oldRecord == null) {
            throw new ParameterInvalidException("记录不存在");
        }

        oldRecord.setName(String.valueOf(System.currentTimeMillis()));
        return monthPaymentService.updateUsingNative(oldRecord);
    }

}
