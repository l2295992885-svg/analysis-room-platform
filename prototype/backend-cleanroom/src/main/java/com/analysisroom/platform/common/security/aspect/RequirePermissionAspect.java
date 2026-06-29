package com.analysisroom.platform.common.security.aspect;

import com.analysisroom.platform.common.security.PermissionService;
import com.analysisroom.platform.common.security.annotation.RequirePermission;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequirePermissionAspect {

    private final PermissionService permissionService;

    public RequirePermissionAspect(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Around("@within(com.analysisroom.platform.common.security.annotation.RequirePermission) || "
        + "@annotation(com.analysisroom.platform.common.security.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        RequirePermission requirePermission = resolveAnnotation(joinPoint);
        permissionService.requirePermission(requirePermission.value(), requirePermission.requireSuperAdmin());
        return joinPoint.proceed();
    }

    private RequirePermission resolveAnnotation(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequirePermission methodAnnotation = AnnotationUtils.findAnnotation(method, RequirePermission.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        RequirePermission typeAnnotation = AnnotationUtils.findAnnotation(
            joinPoint.getTarget().getClass(),
            RequirePermission.class
        );
        if (typeAnnotation == null) {
            throw new IllegalStateException("@RequirePermission annotation not found");
        }
        return typeAnnotation;
    }
}
