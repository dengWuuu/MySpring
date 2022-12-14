package org.spring.context;

import org.spring.entity.BeanDefinition;
import org.spring.factory.AbstractBeanFactory;
import org.spring.factory.AutowiredCapableBeanFactory;
import org.spring.io.ResourceLoader;
import org.spring.reader.XmlBeanDefinitionReader;

import java.util.Map;

/**
 * 程序上下文类实现
 */
public class ClassPathXmlApplicationContext extends AbstractApplicationContext {

    private final Object startupShutdownMonitor = new Object();
    private final String location;

    public ClassPathXmlApplicationContext(String location) throws Exception {
        super();
        this.location = location;
        refresh();
    }

    public void refresh() throws Exception {
        synchronized (startupShutdownMonitor) {
            AbstractBeanFactory beanFactory = obtainBeanFactory();
            prepareBeanFactory(beanFactory);
            this.beanFactory = beanFactory;
        }
    }

    private void prepareBeanFactory(AbstractBeanFactory beanFactory) throws Exception {
        beanFactory.populateBeans();
    }

    /**
     * 创建Bean工厂并且通过Reader读取XML扫描Bean的信息
     *
     * @return
     * @throws Exception
     */
    private AbstractBeanFactory obtainBeanFactory() throws Exception {
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(new ResourceLoader());
        beanDefinitionReader.loadBeanDefinitions(location);
        AbstractBeanFactory beanFactory = new AutowiredCapableBeanFactory();
        //获取Reader的注册map读取注入的Bean
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionReader.getRegistry().entrySet()) {
            beanFactory.registerBeanDefinition(beanDefinitionEntry.getKey(), beanDefinitionEntry.getValue());
        }
        return beanFactory;
    }


    public void addNewBeanDefinition(String name, BeanDefinition definition) throws Exception {
        XmlBeanDefinitionReader.processAnnotationProperty(definition.getBeanClass(), definition);
        beanFactory.registerBeanDefinition(name, definition);

    }

    public void refreshBeanFactory() throws Exception {
        prepareBeanFactory((AbstractBeanFactory) beanFactory);
    }
}