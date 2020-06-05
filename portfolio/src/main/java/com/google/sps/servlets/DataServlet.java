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

package com.google.sps.servlets;

import com.google.sps.data.Comment;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Servlet with a GET handler that loads a list of comments from Datastore and 
 * a POST handler that stores a new comment in Datastore.
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(DataServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int maxComments = getMaxCommentsToReturn(request);

    // Query up to maxComments comment entities from Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(maxComments));

    // Construct a list of comments from the queried entities.
    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results) {
      long id = entity.getKey().getId();
      String author = (String) entity.getProperty("author");
      String commentText = (String) entity.getProperty("commentText");
      long timestamp = (long) entity.getProperty("timestamp");

      Comment comment = new Comment(id, author, commentText, timestamp);
      comments.add(comment);
    }

    // Convert the list of comments to JSON.
    String json = convertToJson(comments);

    // Send the JSON as the response.
    response.setContentType("text/html;");
    response.getWriter().println(json);
  }

  /** 
   * Returns the maximum number of comments selected by the user, or the default of 10 if the number was invalid.
   */
  private int getMaxCommentsToReturn(HttpServletRequest request) {
    // Get the input from the form.
    String maxCommentsString = request.getParameter("max-comments");

    // Convert the input to an int.
    int maxComments;
    try {
      maxComments = Integer.parseInt(maxCommentsString);
    } catch (NumberFormatException e) {
      LOGGER.warning("Could not convert to int: " + maxCommentsString);
      return 10;
    }

    // Check that the input is positive.
    if (maxComments < 1) {
      LOGGER.warning("Choice for maximum number of comments is out of range: " + maxCommentsString);
      return 10;
    }

    return maxComments;
  }

  /**
   * Converts a List into a JSON string using the Gson library.
   */
  private String convertToJson(List list) {
    Gson gson = new Gson();
    String json = gson.toJson(list);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String author = request.getParameter("author");
    String commentText = request.getParameter("comment");
    
    // Use the default author "Anonymous" if none is provided.
    if (author.isEmpty()) {
      author = Comment.DEFAULT_AUTHOR;
    }

    long timestamp = System.currentTimeMillis();
    
    // Create an entity with a kind of Comment.
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("author", author);
    commentEntity.setProperty("commentText", commentText);
    commentEntity.setProperty("timestamp", timestamp);

    // Store the comment entity in Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }
}
