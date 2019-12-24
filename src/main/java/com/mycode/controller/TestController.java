package com.mycode.controller;

import com.mycode.VO.ResultVO;
import com.mycode.annotation.Limiter;
import com.mycode.exception.FangShuaException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.transform.Result;

@RestController
public class TestController {

    @GetMapping("/hello")
    @Limiter(frequency = 3,cycle = 3000,expireTime = 10)
    public ResultVO hello(){
        try{
            return new ResultVO(200,"页面防刷");
        }catch(FangShuaException e){
            return new ResultVO(201,"访问过于频繁",e.getMessage());
        }

    }
}
