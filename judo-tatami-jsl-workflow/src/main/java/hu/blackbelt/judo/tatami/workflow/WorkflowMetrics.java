package hu.blackbelt.judo.tatami.workflow;

import hu.blackbelt.judo.tatami.core.workflow.work.MetricsCollector;

import java.util.Map;

public interface WorkflowMetrics extends MetricsCollector {

    Map<String, Integer> getInvocationCounts();

    Map<String, Integer> getCompletedCounts();

    Map<String, Integer> getFailedCounts();

    Map<String, Long> getExecutionTimes();
}