package com.google.sps.data;

/**
 * A user's comment.
 */
public final class Comment {

  public static final String DEFAULT_AUTHOR = "Anonymous";

  private final long id;
  private final String author;
  private final String commentText;
  private final long timestamp;

  public Comment(long id, String author, String commentText, long timestamp) {
    this.id = id;
    this.author = author;
    this.commentText = commentText;
    this.timestamp = timestamp;
  }
}