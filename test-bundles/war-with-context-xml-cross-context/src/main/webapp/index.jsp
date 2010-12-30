<%@ page import="javax.naming.*,javax.mail.*"%>

<%
    Context ctx = new InitialContext();
    if(ctx == null ) throw new Exception("No Context");
    
    Object mail = ctx.lookup("java:comp/env/mail/Session");
    if (mail != null) out.println("mail: Got JavaMail Session " + mail.toString());

    mail = ctx.lookup("java:comp/env/mail/Session1");
    if (mail != null) out.println("mail1: Got JavaMail Session " + mail.toString());
%>