package org.spring.factory;


import org.spring.entity.BeanDefinition;

/**
 * Bean工厂接口
 *
 * @author Wu
 */
public interface BeanFactory {

    Object getBean(String name) throws Exception;
    Object getBean(Class<?> clazz) throws Exception;
    void registerBeanDefinition(String name, BeanDefinition beanDefinition) throws Exception;

}