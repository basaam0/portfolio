package com.google.sps.data;

/**
 * Class representing a user's comment.
 */
public final class Comment {

  private String author;
  private String commentText;

  public Comment(String author, String commentText) {
    this.author = author;
    this.commentText = commentText;
  }

  /**
   * Sets the author to "Anonymous" if none is specified.
   */
  public Comment(String commentText) {
    this("Anonymous", commentText);
  }
}