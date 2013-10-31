<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.FetchOptions" %>
<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
<%@ page import="com.google.appengine.api.datastore.Query" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="com.gm.server.model.DAO" %>
<%@ page import="com.gm.server.model.*" %>
<%@ page import="com.gm.server.*" %>

<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>

<%
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>

<html>
<head>
<meta charset="UTF-8">
<title>Push to user</title>
</head>
<body>

    <form action="<%= blobstoreService.createUploadUrl("/social/upload_image") %>" method="post" enctype="multipart/form-data">
        <input type="file" name="image">
        <select name="key" multiple>
<%
    DAO dao = DAO.get();
    List<User> users = dao.query(User.class).prepare().asList();
	
    if (!users.isEmpty()) {
   	 	for (User user : users) {
   	 	 
   	 		if(user.getEntityKey()!=null){
       			         
%>
		<option value= <%= KeyFactory.keyToString(user.getEntityKey())%>><%= user.getUserID()%></option>
<%
			} 
		}
	}
%>
</select >

        <input type="submit" value="Submit">
    </form>

<form action="/util/push" method="post">
Please choose users by ID:<br>
<select name="friend_id" multiple>
<%
 
 
	
    if (!users.isEmpty()) {
   	 	for (User user : users) {
   	 	 
   	 		if(user.getDeviceID()!=null){
       			         
%>
		<option value= <%= user.getUserID()%>><%= user.getUserID()%></option>
<%
			} 
		}
	}
%>
</select >
Total user number is     <%= users.size() %>
<input type="submit">
</form>

<p><b>Note:</b> The datalist tag is not supported in Internet Explorer 9 and earlier versions, or in Safari.</p>


</body>
</html>