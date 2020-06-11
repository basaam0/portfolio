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
import com.google.sps.servlets.NameServlet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.common.collect.Streams;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Servlet with a GET handler that loads a list of comments from Datastore, checks
 * whether the user is logged in, and sends back either the user's email, name, and
 * a link to logout if they are logged in or a link to login if they are not; and  
 * a POST handler that stores a new comment in Datastore.
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  // Logs to System.err by default.
  private static final Logger LOGGER = Logger.getLogger(DataServlet.class.getName());
  private static final int DEFAULT_MAX_COMMENTS = 10;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    JsonObject json = new JsonObject();
    UserService userService = UserServiceFactory.getUserService();

    if (userService.isUserLoggedIn()) {
      String name = getUserName(userService.getCurrentUser().getUserId());

      // If the user has logged in for the first time, set their name as their Google account nickname.
      if (name == null) {
        name = userService.getCurrentUser().getNickname();
        NameServlet.upsertUserInfo(name);
      }

      String urlToRedirectToAfterUserLogsOut = "/";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
      String userEmail = userService.getCurrentUser().getEmail();

      json.addProperty("logoutUrl", logoutUrl);
      json.addProperty("email", userEmail);
      json.addProperty("name", name);
    } else {
      // Add a login link to the response if the user is not logged in.
      String urlToRedirectToAfterUserLogsIn = "/";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      json.addProperty("loginUrl", loginUrl);
    }
    
    // Query up to maxComments comment entities from Datastore with the user's specified sorting option.
    int maxComments = getMaxCommentsToReturn(request);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = createCommentQuery(request);
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(maxComments));

    // Check if there are comments to translate.
    if (results.isEmpty()) {
      json.add("comments", new JsonArray());
    } else {
      // Construct a stream of comment texts from the queried entities.
      Stream<String> commentTexts = results.stream().map((entity) -> {
        String commentText = (String) entity.getProperty("commentText");
        return commentText;
      });

      String languageCode = request.getParameter("language-code");
      Stream<String> translatedCommentTexts = translateComments(commentTexts, languageCode);

      // Construct a list of comments from the queried entities and translated comment texts.
      List<Comment> comments = 
          Streams.zip(results.stream(), translatedCommentTexts, (entity, translatedCommentText) -> {
            long id = entity.getKey().getId();
            String author = (String) entity.getProperty("author");
            long timestamp = (long) entity.getProperty("timestamp");

            return new Comment(id, author, translatedCommentText, timestamp);
          }).collect(Collectors.toList());

      // Convert the list of comments to a JsonElement.
      JsonElement commentsJsonElement = convertToJsonElement(comments);
      json.add("comments", commentsJsonElement);
    }

    // Send the JSON as the response.
    response.setContentType("application/json; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(json.toString());
  }

  /** 
   * Returns the name of the user with id, or null if a name has not been set. 
   */
  private String getUserName(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return null;
    }

    String name = (String) entity.getProperty("name");
    return name;
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
      return DEFAULT_MAX_COMMENTS;
    }

    // Check that the input is positive.
    if (maxComments < 1) {
      LOGGER.warning("Choice for maximum number of comments is out of range: " + maxCommentsString);
      return DEFAULT_MAX_COMMENTS;
    }

    return maxComments;
  }

  /**
   * Creates and returns a query for comments with the sorting option
   * specified by the user applied.
   */
  private Query createCommentQuery(HttpServletRequest request) {
    Query query = new Query("Comment");
    String sortOption = request.getParameter("sort-option");
    
    if (sortOption.equals("newest")) {
      return query.addSort("timestamp", SortDirection.DESCENDING);
    } else if (sortOption.equals("oldest")) {
      return query.addSort("timestamp", SortDirection.ASCENDING);
    } else if (sortOption.equals("name")) {
      return query.addSort("author", SortDirection.ASCENDING);
    }

    return query;
  }

  /**
   * Translates a stream of comment texts to the specified language code and returns a
   * stream of the translated comment texts.
   */
  private Stream<String> translateComments(Stream<String> comments, String languageCode) {
    // Convert the stream of comment texts into a list since the translation API takes in lists.
    List<String> commentTexts = comments.collect(Collectors.toList());

    // Translate the list of comment texts.
    Translate translate = TranslateOptions.getDefaultInstance().getService();
    List<Translation> translations =
        translate.translate(commentTexts, Translate.TranslateOption.targetLanguage(languageCode));

    // Map the translations to a stream of strings.
    return translations.stream().map(Translation::getTranslatedText);
  }

  /**
   * Converts a List into a JsonElement using the Gson library.
   */
  private JsonElement convertToJsonElement(List list) {
    Gson gson = new Gson();
    JsonElement jsonElement = gson.toJsonTree(list);
    return jsonElement;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String author = getUserName(userService.getCurrentUser().getUserId());
    String commentText = request.getParameter("comment");
    long timestamp = System.currentTimeMillis();
    
    // Create an entity with a kind of Comment.
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("author", author);
    commentEntity.setProperty("commentText", commentText);
    commentEntity.setProperty("timestamp", timestamp);

    // Store the comment entity in Datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
  }
}
