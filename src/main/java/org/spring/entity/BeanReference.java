package org.spring.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 如果bean注入了另一个对象，需要一个引用记录
 *
 * @author Wu
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
public class BeanReference {
    public BeanReference(String name) {
        this.name = name;
    }

    private String name;
    private Object bean;
}