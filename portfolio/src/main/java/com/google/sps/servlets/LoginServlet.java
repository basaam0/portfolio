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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Servlet with a GET handler that returns a URL for the user to login with their Google account.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    String urlToRedirectToAfterUserLogsIn = "/";
    String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
  
    // Convert the login URL to JSON.
    String json = convertToJson(loginUrl);
    
    // Send the JSON as the response.
    response.setContentType("text/html;");
    response.getWriter().println(json);
  }

  /**
   * Converts a String value into a JSON string using the Gson library.
   */
  private String convertToJson(String value) {
    Gson gson = new Gson();
    String json = gson.toJson(value);
    return json;
  }
}
