package com.intellij.openapi.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public final class RecursionManager {
    
    private static final ThreadLocal<CalculationStack> ourStack = ThreadLocal.withInitial(CalculationStack::new);
    
    public static <T> T doPreventingRecursion(final Object key, final boolean memoize, final Computable<T> computation) = createGuard(computation.getClass().getName()).doPreventingRecursion(key, memoize, computation);
    
    public static <Key> RecursionGuard<Key> createGuard(final String id) = new RecursionGuard<>() {
        
        @Override
        public <T, E extends Throwable> T computePreventingRecursion(final Key key, final boolean memoize, final ThrowableComputable<T, E> computation) throws E {
            final MyKey realKey = { id, key };
            final CalculationStack stack = ourStack.get();
            if (stack.checkReentrancy(realKey))
                return null;
            stack.beforeComputation(realKey);
            try {
                return computation.compute();
            } finally { stack.afterComputation(realKey); }
        }
        
        @Override
        public List<Key> currentStack() {
            final List<Key> result = new ArrayList<>();
            for (final MyKey pair : ourStack.get().progressMap.keySet()) {
                if (pair.guardId.equals(id)) {
                    // noinspection unchecked
                    result.add((Key) pair.userObject);
                }
            }
            return Collections.unmodifiableList(result);
        }
        
        @Override
        public void prohibitResultCaching(final Object since) = ourStack.get().prohibitResultCaching();
        
    };
    
    public static void dropCurrentMemoizationCache() { }
    
    public static RecursionGuard.StackStamp markStack() {
        final int stamp = ourStack.get().reentrancyCount;
        return () -> stamp == ourStack.get().reentrancyCount;
    }
    
    
    private static class MyKey {
        
        final         String guardId;
        final         Object userObject;
        private final int    myHashCode;
        
        MyKey(final String guardId, final Object userObject) {
            this.guardId = guardId;
            this.userObject = userObject;
            myHashCode = guardId.hashCode() * 31 + userObject.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) = obj instanceof MyKey myKey && guardId.equals(myKey.guardId) && userObject == myKey.userObject;
        
        @Override
        public int hashCode() = myHashCode;
        
        @Override
        public String toString() = guardId + "->" + userObject;
        
    }
    
    private static final class CalculationStack {
        
        private int reentrancyCount;
        
        private final LinkedHashMap<MyKey, Integer> progressMap = { };
        
        boolean checkReentrancy(final MyKey realKey) {
            if (progressMap.containsKey(realKey)) {
                prohibitResultCaching();
                return true;
            }
            return false;
        }
        
        void beforeComputation(final MyKey realKey) = progressMap.put(realKey, reentrancyCount);
        
        void afterComputation(final MyKey realKey) = reentrancyCount = progressMap.remove(realKey);
        
        private void prohibitResultCaching() = reentrancyCount++;
        
    }
    
}
