package org.spring.entity;

import lombok.Data;

@Data
public class BeanReference {
    private String name;
    private Object bean;
}