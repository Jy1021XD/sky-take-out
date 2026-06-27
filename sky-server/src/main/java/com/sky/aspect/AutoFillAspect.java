package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面：实现公共字段自动填充处理
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect
{
    // 定义切点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut()
    {
    }


    // 定义通知
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint)
    {
        log.info("自动填充公共字段");
        // 获取被通知方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 获取方法上的注解
        AutoFill autoFill = method.getAnnotation(AutoFill.class);
        // 获取注解中的操作类型
        OperationType operationType = autoFill.value();

        // 获取方法的参数（对方法参数进行数据的添加）
        Object[] args = joinPoint.getArgs();

        if (args == null || args.length == 0)
        {
            return;
        }
        // 获取Employee类型的参数
        Object entity = args[0];

        // 准备公共字段数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 根据操作类型进行不同的处理
        if (operationType == OperationType.UPDATE)
        {
            // 更新操作
            try
            {
                // 根据传入的形参，获取类后，获取setter方法
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                // 调用setter方法，进行赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        } else if (operationType == OperationType.INSERT)
        {
            // 新增操作
            try{
                // 获取setter方法
                Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod("setCreateUser", Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                // 调用setter方法，进行赋值
                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }
}
