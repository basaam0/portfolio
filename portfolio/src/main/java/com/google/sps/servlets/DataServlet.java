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

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private List<String> messages;

  @Override
  public void init() {
    messages = new ArrayList<>();
    messages.add("Hello, this is the first message.");
    messages.add("This is the second message.");
    messages.add("This is the third and final message.");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Convert the list of messages to JSON
    String json = convertToJson(messages);

    // Send the JSON as the response
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
}
