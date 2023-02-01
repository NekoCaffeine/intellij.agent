package com.intellij.openapi.util;

import java.util.List;

public abstract class RecursionGuard<Key> {
    
    public interface StackStamp {
        
        boolean mayCacheNow();
        
    }
    
    public <T> T doPreventingRecursion(final Key key, final boolean memoize, final Computable<T> computation) = computePreventingRecursion(key, memoize, computation::compute);
    
    public abstract <T, E extends Throwable> T computePreventingRecursion(final Key key, final boolean memoize, final ThrowableComputable<T, E> computation) throws E;
    
    public abstract List<? extends Key> currentStack();
    
    public abstract void prohibitResultCaching(Key since);
    
}
