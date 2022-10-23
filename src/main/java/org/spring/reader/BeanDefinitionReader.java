package org.spring.reader;

/**
 * Bean定义读取
 *
 * @author Wu
 */
public interface BeanDefinitionReader {

    void loadBeanDefinitions(String location) throws Exception;

}