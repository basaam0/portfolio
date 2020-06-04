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

package com.google.sps.data;

/**
 * A user's comment.
 */
public final class Comment {

  public static final String DEFAULT_AUTHOR = "Anonymous";

  private final long id;
  private final String author;
  private final String commentText;
  private final String formattedDate;
  private final long timestamp;

  public Comment(long id, String author, String commentText, String formattedDate, long timestamp) {
    this.id = id;
    this.author = author;
    this.commentText = commentText;
    this.formattedDate = formattedDate;
    this.timestamp = timestamp;
  }
}