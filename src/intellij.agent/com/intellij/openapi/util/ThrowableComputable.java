package com.intellij.openapi.util;

public interface ThrowableComputable<T, E extends Throwable> {
    
    T compute() throws E;
    
}
