/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.retrofit;

import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import java.io.IOException;
import java.lang.annotation.Annotation;
import retrofit.Call;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;

public final class DeserializeErrorBody {
  interface Service {
    @GET("/user") Call<User> getUser();
  }

  static class User {
    // normal fields...
  }

  static class Error {
    String message;
  }

  public static void main(String... args) throws IOException {
    // Create a local web server which response with a 404 and JSON body.
    MockWebServer server = new MockWebServer();
    server.start();
    server.enqueue(new MockResponse()
        .setResponseCode(404)
        .setBody("{\"message\":\"Unable to locate resource\"}"));

    // Create our Service instance with a Retrofit pointing at the local web server and Gson.
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(server.url("/"))
        .addConverterFactory(GsonConverterFactory.create())
        .build();
    Service service = retrofit.create(Service.class);

    Response<User> response = service.getUser().execute();

    // Normally you would check response.isSuccess() here before doing the following, but we know
    // this call will always fail. You could also use response.code() to determine whether to
    // convert the error body and/or which type to use for conversion.

    // Look up a converter for the Error type on the Retrofit instance.
    Converter<ResponseBody, Error> errorConverter =
        retrofit.responseConverter(Error.class, new Annotation[0]);
    // Convert the error body into our Error type.
    Error error = errorConverter.convert(response.errorBody());
    System.out.println("ERROR: " + error.message);
  }
}
