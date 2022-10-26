package org.spring.factory.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PropertyValue {

    private final String name;
    private final Object value;

}