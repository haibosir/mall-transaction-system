package com.mall.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mall.dto.ApiResponse;


/**
 * 健康检查控制器
 *
 * @author mall
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ApiResponse<?> health() {
        return ApiResponse.success("商城交易系统运行正常",null);
    }

}
