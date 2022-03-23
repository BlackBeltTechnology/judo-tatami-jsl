package hu.blackbelt.judo.tatami.workflow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class DefaultWorkflowMetricsCollector implements WorkflowMetrics {

    private final Map<String, AtomicInteger> invocationCountMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> completedCountMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> failedCountMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> executionTimeMap = new ConcurrentHashMap<>();

    @Override
    public void invokedTransformation(final String transformationName) {
        invocationCountMap.putIfAbsent(transformationName, new AtomicInteger());
        invocationCountMap.get(transformationName).incrementAndGet();
    }

    @Override
    public void stoppedTransformation(final String transformationName, final long executionTime, final boolean failed) {
        failedCountMap.putIfAbsent(transformationName, new AtomicInteger());
        completedCountMap.putIfAbsent(transformationName, new AtomicInteger());
        executionTimeMap.putIfAbsent(transformationName, new AtomicLong());
        if (failed) {
            failedCountMap.putIfAbsent(transformationName, new AtomicInteger());
        } else {
            completedCountMap.putIfAbsent(transformationName, new AtomicInteger());
        }
        executionTimeMap.get(transformationName).addAndGet(executionTime / 1000000);
    }

    @Override
    public Map<String, Integer> getInvocationCounts() {
        return invocationCountMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().get()));
    }

    @Override
    public Map<String, Integer> getCompletedCounts() {
        return completedCountMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().get()));
    }

    @Override
    public Map<String, Integer> getFailedCounts() {
        return completedCountMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().get()));
    }

    @Override
    public Map<String, Long> getExecutionTimes() {
        return executionTimeMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().get()));
    }
}
