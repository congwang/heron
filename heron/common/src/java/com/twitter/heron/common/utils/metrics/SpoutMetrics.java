// Copyright 2016 Twitter. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.twitter.heron.common.utils.metrics;

import com.twitter.heron.api.metric.CountMetric;
import com.twitter.heron.api.metric.MeanReducer;
import com.twitter.heron.api.metric.MeanReducerState;
import com.twitter.heron.api.metric.ReducedMetric;
import com.twitter.heron.common.basics.SingletonRegistry;
import com.twitter.heron.common.config.SystemConfig;
import com.twitter.heron.common.utils.misc.PhysicalPlanHelper;
import com.twitter.heron.common.utils.topology.TopologyContextImpl;


/**
 * Spout's metrics to be collect
 * We need to:
 * 1. Define the metrics to be collected
 * 2. New them in the constructor
 * 3. Register them in registerMetrics(...) by using MetricsCollector's registerMetric(...)
 * 4. Expose methods which could be called externally to change the value of metrics
 */

public class SpoutMetrics {
  private final CountMetric ackCount;
  private final ReducedMetric<MeanReducerState, Number, Double> completeLatency;
  private final ReducedMetric<MeanReducerState, Number, Double> failLatency;
  private final CountMetric failCount;
  private final CountMetric timeoutCount;
  private final CountMetric emitCount;
  private final ReducedMetric<MeanReducerState, Number, Double> nextTupleLatency;
  private final CountMetric nextTupleCount;

  // The # of times back-pressure happens on outStreamQueue so instance could not
  // produce more tuples
  private final CountMetric outQueueFullCount;

  // The mean # of pending-to-be-acked tuples in spout if acking is enabled
  private final ReducedMetric<MeanReducerState, Number, Double> pendingTuplesCount;

  public SpoutMetrics() {
    ackCount = new CountMetric();
    completeLatency = new ReducedMetric<>(new MeanReducer());
    failLatency = new ReducedMetric<>(new MeanReducer());
    failCount = new CountMetric();
    timeoutCount = new CountMetric();
    emitCount = new CountMetric();
    nextTupleLatency = new ReducedMetric<>(new MeanReducer());
    nextTupleCount = new CountMetric();
    outQueueFullCount = new CountMetric();
    pendingTuplesCount = new ReducedMetric<>(new MeanReducer());
  }

  public void registerMetrics(TopologyContextImpl topologyContext) {
    SystemConfig systemConfig =
        (SystemConfig) SingletonRegistry.INSTANCE.getSingleton(SystemConfig.HERON_SYSTEM_CONFIG);

    int interval = systemConfig.getHeronMetricsExportIntervalSec();

    topologyContext.registerMetric("__ack-count", ackCount, interval);
    topologyContext.registerMetric("__complete-latency", completeLatency, interval);
    topologyContext.registerMetric("__fail-latency", failLatency, interval);
    topologyContext.registerMetric("__fail-count", failCount, interval);
    topologyContext.registerMetric("__timeout-count", timeoutCount, interval);
    topologyContext.registerMetric("__emit-count", emitCount, interval);
    topologyContext.registerMetric("__next-tuple-latency", nextTupleLatency, interval);
    topologyContext.registerMetric("__next-tuple-count", nextTupleCount, interval);
    topologyContext.registerMetric("__out-queue-full-count", outQueueFullCount, interval);
    topologyContext.registerMetric("__pending-acked-count", pendingTuplesCount, interval);
  }

  // For MultiCountMetrics, we need to set the default value for all streams.
  // Otherwise, it is possible one metric for a particular stream is null.
  // For instance, the fail-count on a particular stream could be undefined
  // causing metrics not be exported.
  // However, it will not set the Multi Reduced/Assignable Metrics,
  // since we could not have default values for them
  public void initMultiCountMetrics(PhysicalPlanHelper helper) {
    // For spout, we would consider the output stream
  }

  public void ackedTuple(String streamId, long latency) {
    ackCount.incr();
    completeLatency.update(latency);
  }

  public void failedTuple(String streamId, long latency) {
    failCount.incr();
    failLatency.update(latency);
  }

  public void timeoutTuple(String streamId) {
    timeoutCount.incr();
  }

  public void emittedTuple(String streamId) {
    emitCount.incr();
  }

  public void nextTuple(long latency) {
    nextTupleLatency.update(latency);
    nextTupleCount.incr();
  }

  public void updateOutQueueFullCount() {
    outQueueFullCount.incr();
  }

  public void updatePendingTuplesCount(long count) {
    pendingTuplesCount.update(count);
  }

  public void serializeDataTuple(String streamId, long latency) {
  }
}
