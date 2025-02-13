package com.stars.datachange.module;

import com.stars.datachange.annotation.ChangeModel;
import com.stars.datachange.annotation.ChangeResult;
import com.stars.datachange.exception.ChangeResultException;
import com.stars.datachange.utils.DataChangeUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 基于注解的数据转换模块
 * @author Hao.
 * @version 1.0
 * @since 2025/2/13 15:43
 */
@Aspect
@Component
public class DataChangeAspect {

	@Pointcut("@annotation(com.stars.datachange.annotation.ChangeResult)")
	public void pointcut() {}

	@AfterReturning(value = "pointcut() && @annotation(changeResult)", returning = "result")
	public void changeResult(Object result, ChangeResult changeResult) {
		final Class<?> type = result.getClass();
		final boolean rollback = changeResult.rollback();
		// 数组
		if (type.isArray()) {
			DataChangeUtils.dataChangeToBean((Object[]) result, rollback);
			return;
		}
		// 单列集合
		if (Collection.class.isAssignableFrom(type)) {
			DataChangeUtils.dataChangeToBean((Collection<?>) result, rollback);
			return;
		}
		// 对象
		if (type.isAnnotationPresent(ChangeModel.class)) {
			DataChangeUtils.dataChangeToBean(result, rollback);
			return;
		}
		// 其他数据类型
		throw new ChangeResultException(String.format("the data change is not possible, mark the @ChangeModel on the %s data model or use the @ChangeModel labeled data model.", type));
	}

}
