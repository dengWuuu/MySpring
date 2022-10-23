package org.spring.entity;

import lombok.Data;

/**
 * 如果bean注入了另一个对象，需要一个引用记录
 * @author Wu
 */
@Data
public class BeanReference {
    private String name;
    private Object bean;
}