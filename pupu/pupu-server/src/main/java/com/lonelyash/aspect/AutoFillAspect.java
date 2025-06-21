package com.lonelyash.aspect;

import com.lonelyash.annotation.AutoFill;
import com.lonelyash.constant.AutoFillConstant;
import com.lonelyash.context.BaseContext;
import com.lonelyash.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static com.lonelyash.constant.AutoFillConstant.SET_CREATE_TIME;

@Aspect
@Component
@Slf4j


public class AutoFillAspect {
    /**
     * 切入点
     */
    @Pointcut("execution(* com.lonelyash.mapper.*.*(..)) && @annotation(com.lonelyash.annotation.AutoFill)")
    public void autoFillPointcut() {}


    @Before("autoFillPointcut()")
    public void before(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info ("公共字段自动填充");
        //反射
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType = autoFill.value();//获得数据库操作类型

        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];

        LocalDateTime now = LocalDateTime.now();
        Long currentid = BaseContext.getCurrentId();

        if(operationType == OperationType.INSERT) {
            Method setCreatTime = entity.getClass().getDeclaredMethod(SET_CREATE_TIME, LocalDateTime.class);
            Method setCreatUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            //反射赋值
            setCreatTime.invoke(entity,now);
            setCreatUser.invoke(entity,currentid);
            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentid);
        }
        if(operationType == OperationType.UPDATE) {
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentid);
        }
    }
}
