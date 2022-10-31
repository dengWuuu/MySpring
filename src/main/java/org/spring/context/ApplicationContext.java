package org.spring.context;

/**
 * 应用程序上下文
 *
 * @author Wu
 * @date 2022年10月23日 16:51
 */
public interface ApplicationContext {
    /**
     * 通过类来获取
     *
     * @param clazz
     * @return
     * @throws Exception
     */
    Object getBean(Class<?> clazz) throws Exception;

    /**
     * 通过名字来获取
     *
     * @param beanName
     * @return
     * @throws Exception
     */
    Object getBean(String beanName) throws Exception;
}