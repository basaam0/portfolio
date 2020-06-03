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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Servlet with a GET handler that returns a list of comments and a POST handler that adds a new comment to the list using form data.
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private List<Comment> comments = new ArrayList<>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Convert the list of comments to JSON.
    String json = convertToJson(comments);

    // Send the JSON as the response.
    response.setContentType("text/html;");
    response.getWriter().println(json);
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

    Comment comment;

    // Initialize the comment, using  "Anonymous" as the author if none is provided
    if (author == null || author.length() == 0) {
      comment = new Comment(commentText);
    }
    else {
      comment = new Comment(author, commentText);
    }

    comments.add(comment);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }
}
