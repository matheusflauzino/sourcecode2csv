/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
package org.apache.abdera.protocol.server.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.AbderaServer;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.RequestHandler;
import org.apache.abdera.protocol.server.RequestHandlerManager;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.exceptions.AbderaServerException;
import org.apache.abdera.protocol.server.util.ServerConstants;

public class AbderaServlet 
  extends HttpServlet 
  implements ServerConstants {

  private static final long serialVersionUID = -4273782501412352619L;

  private Abdera abdera;
  private AbderaServer abderaServer;
  
  @Override
  public void init() throws ServletException {
    ServletContext context = getServletContext();
    if (context.getAttribute("abdera") == null) {
      synchronized(context) {
        ServletConfig config = getServletConfig();
        this.abdera = new Abdera();
        this.abderaServer = 
          new AbderaServer(
            abdera, 
            config.getInitParameter(TARGET_RESOLVER), 
            config.getInitParameter(HANDLER_MANAGER), 
            config.getInitParameter(SUBJECT_RESOLVER), 
            config.getInitParameter(PROVIDER_MANAGER));
        context.setAttribute("abdera", abdera);
        context.setAttribute("server", abderaServer);
      }
    }
  }

  /**
   * The RequestContext will either be set on the HttpServletRequest by 
   * some filter or servlet earlier in the invocation chain or will need
   * to be created and set on the request 
   */
  private RequestContext getRequestContext(HttpServletRequest request) {
    return new ServletRequestContext(abderaServer,request);
  }
  
  private RequestHandlerManager getRequestHandlerManager() {
    return abderaServer.getRequestHandlerManager();
  }
  
  @Override
  protected void service(
    HttpServletRequest request, 
    HttpServletResponse response) 
      throws ServletException, IOException {
    RequestContext requestContext = getRequestContext(request);
    ResponseContext responseContext = null;
    RequestHandler handler = null;
    RequestHandlerManager manager = null;
    try {
      manager = getRequestHandlerManager();
      handler = (manager != null) ? 
        manager.newRequestHandler(abderaServer) : null;
      responseContext = (handler != null) ? 
        handler.invoke(requestContext) :
        new AbderaServerException(AbderaServerException.Code.NOTFOUND);
    } catch (AbderaServerException exception) {  
      responseContext = exception;
    } catch (Throwable t) {
      responseContext = new AbderaServerException(t);
    } finally {
      if (manager != null)
        manager.releaseRequestHandler(handler);
    }
    doOutput(response, responseContext); 
  }

  private void doOutput(
    HttpServletResponse response, 
    ResponseContext context) 
      throws IOException, ServletException {
    if (context != null) {
      response.setStatus(context.getStatus());
      long cl = context.getContentLength();
      String cc = context.getCacheControl();
      if (cl > -1) response.setHeader("Content-Length", Long.toString(cl));
      if (cc != null) response.setHeader("Cache-Control",cc);
      Map<String, List<Object>> headers = context.getHeaders();
      if (headers != null) {
        for (Map.Entry<String, List<Object>> entry : headers.entrySet()) {
          List<Object> values = entry.getValue();
          if (values == null) 
            continue;          
          for (Object value : values) {
            if (value instanceof Date)
              response.setDateHeader(entry.getKey(), ((Date)value).getTime());
            else
              response.setHeader(entry.getKey(), value.toString());
          }
        }
      }  
      if (context.hasEntity())
        context.writeTo(response.getOutputStream());
    } else {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
  
}