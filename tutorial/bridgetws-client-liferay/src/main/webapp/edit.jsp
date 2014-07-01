<%@page import="de.mhus.test.bridgews.client.liferay.SetJwsUrlAction"%>
<%@include file="init.jsp" %>

<portlet:renderURL var="cancelURL">
	<portlet:param name="jspPage" value="view.jsp"/>
</portlet:renderURL>
<portlet:actionURL name="setJwsUrl" var="setJwsUrl"/>

<aui:form name="setJwsAction" action="<%=setJwsUrl.toString() %>" method="post">
	<%
		String jwsUrl = SetJwsUrlAction.getCurrentJwsUrl(renderRequest);
	%>
	Default: <%=SetJwsUrlAction.JWS_URL_DEFAULT %><br/>
	<aui:input name="<%=SetJwsUrlAction.URL_INPUT_ID%>" title="URL" value="<%=jwsUrl%>" size="80"/>
	<aui:button-row>
		<aui:button type="submit"/>
		<aui:button type="cancel" value="Cancel" onClick="<%=cancelURL.toString() %>"/>
	</aui:button-row>
</aui:form>