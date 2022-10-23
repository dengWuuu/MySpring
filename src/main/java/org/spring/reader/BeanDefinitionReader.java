package org.spring.reader;

/**
 * Bean定义读取
 *
 * @author Wu
 */
public interface BeanDefinitionReader {

    /**
     * 从某个位置读取Bean的配置
     * @param location 位置
     * @throws Exception
     */
    void loadBeanDefinitions(String location) throws Exception;

}