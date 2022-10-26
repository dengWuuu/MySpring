package org.spring.reader;

import lombok.AllArgsConstructor;
import org.spring.factory.entity.BeanDefinition;
import org.spring.io.ResourceLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * BeanDefinitionReader实现的抽象类
 *
 * @author Wu
 */
@AllArgsConstructor
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader {

    /**
     * 存储Bean的名称和BeanDefinition的映射关系，供工厂使用
     */
    private final Map<String, BeanDefinition> registry;

    private final ResourceLoader resourceLoader;

    public AbstractBeanDefinitionReader(ResourceLoader resourceLoader) {
        this.registry = new HashMap<>();
        this.resourceLoader = resourceLoader;
    }

    public Map<String, BeanDefinition> getRegistry() {
        return registry;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }
}