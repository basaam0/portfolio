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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Servlet with a POST handler that updates the name associated with a logged-in user in comments.
 */
@WebServlet("/name")
public class NameServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String name = request.getParameter("name");
    upsertUserInfo(name);
    response.sendRedirect("/index.html");
  }
  

  /**
   * Inserts a new user info entity with the given name in datastore or updates the name in an existing entity
   * based on the logged-in user's ID.
   */
  public static void upsertUserInfo(String name) {
    UserService userService = UserServiceFactory.getUserService();
    String id = userService.getCurrentUser().getUserId();

    // Create an entity with a kind of UserInfo that has the logged-in user's ID as an identifier.
    Entity entity = new Entity("UserInfo", id);
    entity.setProperty("id", id);
    entity.setProperty("name", name);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // The put() function upserts the entity based on ID.
    datastore.put(entity);
  }
}
