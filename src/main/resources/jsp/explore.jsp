<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache">
<meta http-equiv="Expires" content="0">

<c:if test="${not empty user}">
	<meta name="isLogin" content="true"/>
</c:if>
<c:if test="${empty user}">
	<meta name="isLogin" content="false"/>
</c:if>
<title>探索OSF</title>
  <link rel="stylesheet" type="text/css" href="${ctx}/static/css/bootstrap2.css">
  <link rel="stylesheet" type="text/css" href="${ctx}/static/css/navbar.css">
  <link rel="stylesheet" type="text/css" href="${ctx}/static/css/semantic.css">
  <link rel="stylesheet" type="text/css" href="${ctx}/static/css/style.css">
  <script src="${ctx}/static/js/jquery.js"></script>
  <script src="${ctx}/static/js/jquery.row-grid.js"></script>
  <script src="${ctx}/static/js/semantic.js"></script>
  <script src="${ctx}/static/js/basic.js"></script>
  <script src="${ctx}/static/js/code.js"></script>
  <script src="${ctx}/static/js/explore.js"></script>
  <script src="${ctx}/static/js/follow.js"></script>
  <script src="${ctx}/static/js/login.js"></script>
  
</head>
<body>
 	<%@ include file="topbar.jsp" %>
	<%@ include file="login_modal.jsp" %>
	<div class="explore">
		<div class="topbar">
			<div class="container">
				<div class="header">
					<div>探索</div>
					<div>标签</div>
					<div>用户</div>
				</div>
			</div>
			<div class="active"></div>
		</div>

		<div class="main">
			<div class="gallery" >
				<div class="box first-item"></div>
				<c:forEach items="${events }" var="event">
					<div class="box">
						<c:if test="${event.object_type eq dic.object_type_post }">
							<a href="<c:url value="/post/${event.object_id }" />">
								<img src="<c:url value="${img_base_url }${event.content }?imageView2/2/h/200" />" alt="" />
							</a>
						</c:if>
						<c:if test="${event.object_type eq dic.object_type_album }">
							<a href="<c:url value="/album/${event.object_id }/photos" />">
								<img src="<c:url value="${img_base_url }${event.title }?imageView2/2/h/200" />" alt="" />
							</a>
						</c:if>
						<div class="meta">
							<a href="<c:url value="/user/${event.user_id }" />">
								<img class="ui avatar image" src="${img_base_url }${event.userAvatar}?imageView2/1/w/48/h/48">
								<span>${event.userName}</span>
							</a>
						</div>
					</div>
				</c:forEach>
			</div>	
			<div class="tags" style="display: none">
				<div class="container">
					<div class="row">
						<div>
							<c:forEach items="${tags }" var="tag" begin="0" end="9">
								<div class="tagbox">
									<div>
										<img class="visible" src="<c:url value="${img_base_url }${tag.cover }?imageView2/1/w/200/h/200" />" alt="" />
										<span class="desc">#${tag.tag }</span>
									</div>
									<c:if test="${!isInterests[tag.id] }">
										<div class="hidden">
											<a href="#" id="${tag.id }" action="interest">加关注</a>
										</div>									
									</c:if>
									<c:if test="${isInterests[tag.id] }">
										<div class="interested">
											<a href="#" id="${tag.id }" action="undointerest">已关注</a>
										</div>											
									</c:if>

								</div>								
							</c:forEach>

						</div>
					</div>
				</div>
			</div>
			<div class="users" style="display: none">
				<div class="container">
					<div class="row">
						
						<c:forEach items="${feeds }" var="feed">
							<div class="userbox">
								<div class="header">
									<img class="avatar" src="${img_base_url }${feed.key.user_avatar }" alt="" />
									<div class="desc">${feed.key.userName }</div>
									<c:if test="${isFollowings[feed.key.id] }">
										<div class="ui tiny basic button follow" following="${feed.key.id }">已关注</div>
									</c:if>
									<c:if test="${!isFollowings[feed.key.id] }">
										<div class="ui inverted yellow tiny  button follow" following="${feed.key.id }">+关注</div>
									</c:if>
								</div>
								<div class="content">	
									<c:forEach items="${feed.value }" var="f">
										<c:if test="${f.object_type eq dic.object_type_post }">
										   <a class="box" href="<c:url value="/post/${f.object_id }" />" href="<c:url value="/post/${f.object_id }" />">
												<img src="${img_base_url }${f.content }${album_thumbnail}" alt="" />
												<div class="cover">
													${f.title }
												</div>
											</a>
								
										</c:if>							
										<c:if test="${f.object_type eq dic.object_type_album }">
											<a class="box" href="<c:url value="/album/${f.object_id }/photos" />" href="<c:url value="/album/${f.object_id }/photos" />">
												<img src="${img_base_url }${f.title }${album_thumbnail}" alt="" />
												<div class="cover">
													${f.summary }
												</div>		
											</a>							
										</c:if>	
										<c:if test="${f.object_type eq dic.object_type_shortpost }">
											<div class="box" >
												<i class="disabled large quote left icon"></i>
												${f.summary }
												<i class="disabled large quote right icon"></i>
											</div>											
										</c:if>				
									</c:forEach>
								</div>
							</div>
						</c:forEach>
					
	
					</div>
					<!-- end a row -->

				</div>
				
			</div>
		</div>
	</div>
  <script type="text/javascript">
	$(function(){		
		var options = {minMargin: 5, maxMargin: 10, itemSelector: ".box", firstItemClass: "first-item"};
		$(".gallery").rowGrid(options);	
		
		
		$(".topbar .header>div").click(function(){
			var index=$(this).index();
			var explore=$('.gallery:first');
			var tags=$('.tags:first');
			var users = $('.users:first');
			var active_tip=$('.topbar .active');
			if(index == 0){	
				$(explore).fadeIn(300);
				$(tags).fadeOut(200);
				$(users).fadeOut(200);
				$(active_tip).css('left', '19.5%');
			} else if(index == 1 ){							
				$(tags).fadeIn(300);
				$(explore).fadeOut(200);
				$(users).fadeOut(200);
				$(active_tip).css('left', '44%');
			} else{
				$(explore).fadeOut(300);
				$(tags).fadeOut(200);
				$(users).fadeIn(200);
				$(active_tip).css('left', '69%');
			}
		});
		
	});
  </script>
</body>
</html>