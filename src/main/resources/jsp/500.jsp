<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>   
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>500</title>
  <link rel="shortcut icon" href="${ctx}/static/img/favicon.ico" />
  <link rel="stylesheet" type="text/css" href="${ctx}/static/css/bootstrap2.css">
  <link rel="stylesheet" type="text/css" href="${ctx}/static/css/navbar.css">
  <link rel="stylesheet" type="text/css" href="${ctx}/static/css/semantic.css">
  <link rel="stylesheet" type="text/css" href="${ctx}/static/css/style.css">
  <style>
  body{
  	background-color: white;
  }
  
  </style>
</head>
<body>
  <%@ include file="topbar.jsp" %>
	
	<div class="container">
		<div class="row">
			<div class="span9 offset1">
				<div class="error page img">
					<img src="<c:url value="/img/500.png" />" alt="" />
				</div>
				<div class="error page code">
					500
				</div>
			</div>
		</div>
	</div>

</body>
</html>