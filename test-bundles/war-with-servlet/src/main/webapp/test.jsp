<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" import="java.util.*, java.net.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<%
  int i = 0;
  Enumeration<URL> e = this.getClass().getClassLoader().getResources("test.txt");
  while (e.hasMoreElements()) {
    out.println(e.nextElement());
    i++;
  }
  out.println("Found resources " + i);
%>
</body>
</html>