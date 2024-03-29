package org.spring.factory;


import lombok.extern.slf4j.Slf4j;
import org.spring.entity.BeanDefinition;
import org.spring.entity.BeanReference;
import org.spring.entity.PropertyValue;

import java.lang.reflect.Field;

/**
 * 工厂实现类
 */
@Slf4j
public class AutowiredCapableBeanFactory extends AbstractBeanFactory {

    /**
     * 创建Bean
     *
     * @param beanDefinition Bean定义对象
     * @return beanDefinition
     * @throws Exception 异常
     */
    @Override
    Object doCreateBean(BeanDefinition beanDefinition) throws Exception {
        //单例且存在直接返回
        if (beanDefinition.isSingleton() && beanDefinition.getBean() != null) {
            return beanDefinition.getBean();
        }
        Object bean = beanDefinition.getBeanClass().newInstance();
        if (beanDefinition.isSingleton()) {
            beanDefinition.setBean(bean);
        }
        applyPropertyValues(bean, beanDefinition);
        return bean;
    }

    /**
     * 为新创建了bean注入属性
     *
     * @param bean           待注入属性的bean
     * @param beanDefinition bean的定义
     * @throws Exception 反射异常
     */
    void applyPropertyValues(Object bean, BeanDefinition beanDefinition) throws Exception {
        for (PropertyValue propertyValue : beanDefinition.getPropertyValues()) {
            Field field = bean.getClass().getDeclaredField(propertyValue.getName());
            Object value = propertyValue.getValue();
            log.debug("注入属性:{}", value);
            if (value instanceof BeanReference) {
                BeanReference beanReference = (BeanReference) propertyValue.getValue();
                BeanDefinition refDefinition = beanDefinitionMap.get(beanReference.getName());
                if (refDefinition.getBean() == null) {
                    value = doCreateBean(refDefinition);
                }
            }
            field.setAccessible(true);
            field.set(bean, value);
        }
    }
}