<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<c:if test="${not empty user}">
		<meta name="isLogin" content="true"/>
	</c:if>
	<c:if test="${empty user}">
		<meta name="isLogin" content="false"/>
	</c:if>
  	<title>${tag }</title>
	<link rel="stylesheet" type="text/css" href="${ctx}/static/css/bootstrap2.css">
    <link rel="stylesheet" type="text/css" href="${ctx}/static/css/navbar.css">
    <link rel="stylesheet" type="text/css" href="${ctx}/static/css/semantic.css">
	<link rel="stylesheet" type="text/css" href="${ctx}/static/css/style.css">
  	<script type="text/javascript" src="${ctx}/static/js/jquery.js"></script>
  	<script type="text/javascript" src="${ctx}/static/js/jquery.infinitescroll.js"></script>
  	<script type="text/javascript" src="${ctx}/static/js/semantic.js"></script>
  	<script type="text/javascript" src="${ctx}/static/js/basic.js"></script>
  	<script type="text/javascript" src="${ctx}/static/js/code.js"></script>
  	<script type="text/javascript" src="${ctx}/static/js/interest.js"></script>
  	<script type="text/javascript" src="${ctx}/static/js/login.js"></script>
  	<script type="text/javascript" src="${ctx}/static/js/feed.js"></script>
  	<script type="text/javascript" src="${ctx}/static/js/like.js"></script>
</head>
<body>
	<%@ include file="../topbar.jsp" %>
	<%@ include file="../login_modal.jsp" %>
	<div class="container">
    <div class="row">  
          <div class="span8">             
                  <div class="ui feed" id="feeds">
 					<%@ include file="../nextpage.jsp" %>                                     
                  </div>  <!--end feed -->
                  <a id="next" href="<c:url value="/${id}/page/2" />"></a>
            </div> <!-- end span8  -->
          <div class="span4">
          	<div id="rightside">
				<div class="tagheader">
				  	<div class="content">
				  		${tag }
				  	</div>
				  	<c:if test="${isInterest }">
				  		<div class="ui mini basic button interest" tag_id="${id }">
				  			 已关注
				  		</div>
				  	</c:if>
				  	<c:if test="${!isInterest }">
				  		<div class="ui mini inverted yellow button interest" tag_id="${id }">
				  			+关注
				  		</div>
				  	</c:if>
				</div>          		
			
				<jsp:include page="/sidebar"></jsp:include>		
            </div>           
          </div>
        </div>
      </div>


    </div>

  </div>

</body>
</html>