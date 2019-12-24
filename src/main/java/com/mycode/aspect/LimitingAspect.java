package com.mycode.aspect;

import com.mycode.annotation.Limiter;
import com.mycode.exception.FangShuaException;
import com.mycode.util.WebUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 定义aspect
 */
@Aspect
@Component
public class LimitingAspect {

    private static final String LIMITING_KEY = "limiting:%s:%s";
    private static final String LIMITING_BEGINTIME = "beginTime";
    private static final String LIMITING_EXFREQUENCY = "exFrequency";

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 定义切入点
     * @param limiter
     */
    @Pointcut("@annotation(limiter)")
    public void pointcut(Limiter limiter){
    }

    @Around("pointcut(limiter)")
    public Object around(ProceedingJoinPoint pjp, Limiter limiter) throws Throwable{
        //获取请求的ip和方法
        String ipAddress = WebUtil.getIpAddress();
        String methodName = pjp.getSignature().getName();
        //获取方法的访问周期和频率
        long frequency = limiter.frequency();
        int cycle = limiter.cycle();
        Long currentTime = System.currentTimeMillis();
        //获取redis中周期内第一次访问方法的时间和执行的次数
        //Long beginTimeLong = Long.parseLong((String)redisTemplate.opsForHash().get(String.format(LIMITING_KEY, ipAddress, methodName), LIMITING_BEGINTIME));
        //Integer exFrequencyLong = Integer.parseInt((String)redisTemplate.opsForHash().get(String.format(LIMITING_KEY,ipAddress,methodName),LIMITING_EXFREQUENCY));

        String beginTimeString = (String)redisTemplate.opsForHash()
                .get(String.format(LIMITING_KEY, ipAddress, methodName), LIMITING_BEGINTIME);
        String exFrequencyLongString = (String)redisTemplate.opsForHash()
                .get(String.format(LIMITING_KEY,ipAddress,methodName),LIMITING_EXFREQUENCY);

        Long beginTime = beginTimeString==null?0L:Long.parseLong(beginTimeString);
        Integer exFrequency = exFrequencyLongString==null?0:Integer.parseInt(exFrequencyLongString);
//        long beginTime = beginTimeLong ;
//        int exFrequency = exFrequencyLong;

        //如果当前时间减去周期内第一次访问方法的时间大于周期时间，则正常访问
        //并将周期内第一次访问方法的时间和执行次数初始化
        if(currentTime-beginTime>cycle){
            redisTemplate.opsForHash().put(String.format(LIMITING_KEY, ipAddress, methodName),LIMITING_BEGINTIME,String.valueOf(currentTime));
            redisTemplate.opsForHash().put(String.format(LIMITING_KEY,ipAddress,methodName),LIMITING_EXFREQUENCY,"1");
            redisTemplate.expire(String.format(LIMITING_KEY, ipAddress, methodName), limiter.expireTime(), TimeUnit.SECONDS);
            System.out.println("第一次访问 "+beginTime);
            return pjp.proceed();
        }else{
            //如果在时间周期内，且访问次数小于frequency，则正常访问，并将执行次数加1
            if(exFrequency -frequency<0){
                redisTemplate.opsForHash().put(String.format(LIMITING_KEY,ipAddress,methodName),LIMITING_EXFREQUENCY,String.valueOf(exFrequency+1));
                //请求过于频繁后十秒内不能再刷新
                redisTemplate.expire(String.format(LIMITING_KEY, ipAddress, methodName), limiter.expireTime(), TimeUnit.SECONDS);
                System.out.println("第N次访问");
                return pjp.proceed();
            }else{
                throw new FangShuaException(limiter.message());
            }
        }

    }
}

