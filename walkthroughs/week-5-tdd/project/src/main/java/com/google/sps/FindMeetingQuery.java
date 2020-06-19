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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implements the "find a meeting" feature which returns the possible times when
 * a meeting could be scheduled given information about the meeting and a list
 * of all the events already scheduled.
 */
public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // There are no options for a meeting longer than a day.
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    Collection<String> mandatoryAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    // Get the list of time ranges where the mandatory attendees have scheduled events.
    List<TimeRange> conflicts = findTimeConflicts(events, mandatoryAttendees);
    Collection<TimeRange> meetings = getPossibleMeetingTimes(conflicts, request);

    if (!optionalAttendees.isEmpty()) {
      // Get the list of time ranges where all attendees have scheduled events.
      List<TimeRange> optionalAttendeeConflicts = findTimeConflicts(events, optionalAttendees);
      conflicts = Stream.concat(conflicts.stream(), optionalAttendeeConflicts.stream())
                      .sorted(TimeRange.ORDER_BY_START)
                      .collect(Collectors.toList());

      Collection<TimeRange> meetingsWithOptionalAttendees =
          getPossibleMeetingTimes(conflicts, request);

      // If time slots exist so that both mandatory and optional attendees can attend, return those
      // time slots.
      if (!meetingsWithOptionalAttendees.isEmpty() || mandatoryAttendees.isEmpty()) {
        return meetingsWithOptionalAttendees;
      }
    }

    return meetings;
  }

  /**
   * Extracts a list of time ranges where any of the given people have events
   * scheduled. Returns the list sorted by start time in ascending order.
   */
  private ImmutableList<TimeRange> findTimeConflicts(
      Collection<Event> events, Collection<String> people) {
    return events.stream()
        .filter(event -> hasCommonAttendees(event, people))
        .map(Event::getWhen)
        .sorted(TimeRange.ORDER_BY_START)
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Returns true if any of the people are attending the event.
   */
  private boolean hasCommonAttendees(Event event, Collection<String> people) {
    Collection<String> attendees = event.getAttendees();
    return !Collections.disjoint(attendees, people);
  }

  /**
   * Gets the list of time ranges when all attendees are free to have
   * the requested meeting given a list of conflicting time ranges
   * that is sorted by start time.
   */
  private ImmutableList<TimeRange> getPossibleMeetingTimes(
      List<TimeRange> conflicts, MeetingRequest request) {
    long meetingDuration = request.getDuration();
    ImmutableList.Builder<TimeRange> possibleTimes = ImmutableList.<TimeRange>builder();
    int freeStart = TimeRange.START_OF_DAY;

    // Add time ranges that are in the gaps between conflicting events.
    int conflictIndex = 0;
    while (conflictIndex < conflicts.size()) {
      OverlappingTimeRange conflict =
          combineOverlappingTimes(conflicts.subList(conflictIndex, conflicts.size()));

      // Move the index to after the chunk of overlapping times.
      conflictIndex += conflict.getNumberOfOverlappingTimes();

      // Add the free time in between conflicts if it is long enough for the meeting.
      TimeRange freeTime = TimeRange.fromStartEnd(freeStart, conflict.start(), false);
      if (freeTime.duration() >= meetingDuration) {
        possibleTimes.add(freeTime);
      }

      // The start of the next free time range is the end of the latest conflict.
      freeStart = conflict.end();
    }

    // Add the free time between the end of the last conflict and end of day.
    TimeRange freeTime = TimeRange.fromStartEnd(freeStart, TimeRange.END_OF_DAY, true);
    if (freeTime.duration() >= meetingDuration) {
      possibleTimes.add(freeTime);
    }

    return possibleTimes.build();
  }

  // Creates an overlapping time range containing a list of the time ranges that overlap
  // starting at the first one. If no time ranges overlap with the first, the list
  // will be of size one. The given list of times must be sorted by start time.
  private OverlappingTimeRange combineOverlappingTimes(List<TimeRange> times) {
    int currentIndex = 0;
    TimeRange overlappingTime = times.get(currentIndex);
    int start = overlappingTime.start();
    int end = overlappingTime.end();

    List<TimeRange> overlappingTimes = new ArrayList<>();
    overlappingTimes.add(overlappingTime);

    // Add all the time ranges that overlap with the current one and get the latest end time.
    while (
        currentIndex < times.size() - 1 && overlappingTime.overlaps(times.get(currentIndex + 1))) {
      currentIndex++;
      TimeRange nextOverlappingTime = times.get(currentIndex);
      overlappingTimes.add(nextOverlappingTime);

      // The remaining time ranges that overlap with the current should be compared to the one
      // with the latest end time. Otherwise, in the case that a time range contains two
      // non-overlapping time ranges, the loop would exit before all three time ranges are
      // looked at because the third range does not overlap with the second.
      if (nextOverlappingTime.end() > end) {
        overlappingTime = nextOverlappingTime;
        end = nextOverlappingTime.end();
      }
    }

    return new OverlappingTimeRange(overlappingTimes, start, end);
  }
}
