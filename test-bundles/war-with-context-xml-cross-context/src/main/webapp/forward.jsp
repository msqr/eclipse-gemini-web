<%
    ServletContext targetContext = config.getServletContext().getContext("/war-with-context-xml-custom-classloader");
    RequestDispatcher dispatcher = targetContext.getRequestDispatcher("/index.html");
    dispatcher.forward(request, response);
%>