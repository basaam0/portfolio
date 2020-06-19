// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * Wrapper class representing a span of time that contains overlapping time
 * ranges.
 */
public final class OverlappingTimeRange {
  private ImmutableList<TimeRange> overlappingTimes;
  private TimeRange block;

  public OverlappingTimeRange(List<TimeRange> overlappingTimes, int start, int end) {
    this.overlappingTimes = ImmutableList.copyOf(overlappingTimes);
    block = TimeRange.fromStartEnd(start, end, false);
  }

  public int getNumberOfOverlappingTimes() {
    return overlappingTimes.size();
  }

  public int start() {
    return block.start();
  }

  public int end() {
    return block.end();
  }
}