<%@page import="de.mhus.test.bridgews.client.liferay.SetJwsUrlAction"%>
<%@include file="init.jsp" %>

<portlet:renderURL portletMode="view" var="cancelURL" />
<portlet:actionURL name="setJwsUrl" var="setJwsUrl"/>

<aui:form name="setJwsAction" action="<%=setJwsUrl.toString() %>" method="post">

	<c:set var="labelSubmit"><liferay-ui:message key="edit.submit"/></c:set>
	<c:set var="labelCancel"><liferay-ui:message key="edit.cancel"/></c:set>
	<c:set var="labelUrl"><liferay-ui:message key="edit.url"/></c:set>
	
	<liferay-ui:message key="edit.default"/>&nbsp;<%=SetJwsUrlAction.JWS_URL_DEFAULT %><br/>
	<aui:input name="<%=SetJwsUrlAction.URL_INPUT_ID%>" inlineLabel="left" label="${labelUrl}" value="<%=SetJwsUrlAction.getCurrentJwsUrl(renderRequest)%>" style="width:90%"/>
	<aui:button-row>
		<aui:button type="submit" value="${labelSubmit}"/>
		<aui:button type="cancel" value="${labelCancel}" onClick="<%=cancelURL.toString() %>"/>
	</aui:button-row>
</aui:form>