package com.intellij.openapi.util;

import java.util.function.Supplier;

public interface Computable<T> extends Supplier<T> {
    
    final class PredefinedValueComputable<T> implements Computable<T> {
        
        private final T myValue;
        
        public PredefinedValueComputable(final T value) = myValue = value;
        
        @Override
        public T compute() = myValue;
        
        @Override
        public String toString() = "PredefinedValueComputable{" + myValue + "}";
        
    }
    
    T compute();
    
    @Override
    default T get() = compute();
    
}
