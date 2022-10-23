package org.spring.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.spring.entity.PropertyValue;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BeanDefinition {

    private Object bean;    // 实例化后的对象
    private Class<?> beanClass;
    private String beanClassName;
    private Boolean singleton;    // 是否是单例模式
    private List<PropertyValue> propertyValues;    // Bean的属性

    public boolean isSingleton() {
        return this.singleton;
    }

}