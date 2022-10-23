package org.spring.reader;

import lombok.AllArgsConstructor;
import org.spring.entity.BeanDefinition;
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