package org.spring.factory;


import lombok.extern.slf4j.Slf4j;
import org.spring.annotation.Component;
import org.spring.entity.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工厂实现
 *
 * @author Wu
 */
@Slf4j
public abstract class AbstractBeanFactory implements BeanFactory {
    //个人只能想到要用map去存
    ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    //TODO 将getBean里面只调用一个写好的doGetBean模仿Spring里面实现
    @Override
    public Object getBean(String name) throws Exception {
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        if (beanDefinition == null) {
            log.error("找不到这个Bean:{}", name);
            return null;
        }
        //不需要单例则创建bean
        if (!beanDefinition.isSingleton() || beanDefinition.getBean() == null) {
            return doCreateBean(beanDefinition);
        } else {
            return beanDefinition.getBean();
        }
    }

    @Override
    public Object getBean(Class clazz) throws Exception {
        BeanDefinition beanDefinition = null;
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            Class<?> tmpClass = entry.getValue().getBeanClass();
            if (tmpClass == clazz || clazz.isAssignableFrom(tmpClass)) {
                beanDefinition = entry.getValue();
            }
        }
        if (beanDefinition == null) {
            log.error("找不到这个Bean:{}", clazz.toGenericString());
            return null;
        }
        if (!beanDefinition.isSingleton() || beanDefinition.getBean() == null) {
            return doCreateBean(beanDefinition);
        } else {
            return beanDefinition.getBean();
        }
    }

    @Override
    public void registerBeanDefinition(String name, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(name, beanDefinition);
    }

    /**
     * 创建Bean实例
     *
     * @param beanDefinition Bean定义对象
     * @return Bean实例对象
     * @throws Exception 可能出现的异常
     */
    abstract Object doCreateBean(BeanDefinition beanDefinition) throws Exception;

    /**
     * 批量创建bean
     * @throws Exception
     */
    public void populateBeans() throws Exception {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            doCreateBean(entry.getValue());
        }
    }
}