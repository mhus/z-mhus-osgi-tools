<%@include file="init.jsp" %>

<c:if test="${requestScope.reason == 'noaccess'}">
No Access !
</c:if>
<c:if test="${requestScope.reason == 'login'}">
Please login
</c:if>
<c:if test="${requestScope.reason == 'error'}">
Internal error
</c:if>
