package hu.blackbelt.judo.tatami.jsl.workflow;

/*-
 * #%L
 * JUDO Tatami JSL parent
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
