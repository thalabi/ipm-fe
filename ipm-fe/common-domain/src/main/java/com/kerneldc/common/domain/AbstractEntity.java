package com.kerneldc.common.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class AbstractEntity {
	
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
}
