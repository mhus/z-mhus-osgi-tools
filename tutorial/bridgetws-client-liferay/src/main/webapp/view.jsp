<%--
/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>
<%@include file="init.jsp" %>

<portlet:resourceURL var="ajaxServiceURL" id="service" />

<div class="panel panel-info">
	<div class="panel-heading">Add Entry</div>
	<div class="panel-body">
		<table class="info">
			<tr>
				<td>Name:</td>
				<td><input data-bind="value: f_name"/></td>
			</tr>
			<tr>
				<td></td>
				<td><button class="btn btn-success btn-mini" data-bind="click:doAdd"><span class="glyphicon glyphicon-plus"/></button></button></td>
		</table>
	</div>
</div>

<div class="panel panel-info">
	<div class="panel-heading"><button class="btn btn-warning btn-mini"><span class="glyphicon glyphicon-refresh" data-bind="click:loadEntryList"/></button>&nbsp;&nbsp;&nbsp;List Of Entries</div>
	<div class="panel-body">
		<table class="info">
			<tbody data-bind="foreach: f_entries">
				<td><button type="button" class="btn btn-mini btn-danger" data-bind="click: $parent.doRemove"><span class="glyphicon glyphicon-remove"></button>&nbsp;&nbsp;&nbsp;</span><span data-bind="text: name"></span></td>
				</tr>
			</tbody>
		</table>
	</div>
</div>

<script type="text/javascript">
var viewModel = {
		f_entries : ko.observableArray(),
		f_name: ko.observable(),
		
		loadEntryList: function() {
	        var url = '<%=ajaxServiceURL%>&action=list';
	        showSpinner(true);
	        $.getJSON(url, function (data2) {
	    		showAjaxResult(data2);
	    		if (data2.success == 1){
	    			viewModel.f_entries.removeAll();
					for (var nr in data2.result) {
						var row = data2.result[nr];
						viewModel.f_entries.push(row);
					}
	    		}
			});
		},
		
		doRemove: function(data) {
			//if (!confirm("Really remove " + data['name'])) return;
			
	        var url = '<%=ajaxServiceURL%>&action=remove&name=' + data['name'];
	        showSpinner(true);
	        $.getJSON(url, function (data2) {
	    		showAjaxResult(data2);
	    		if (data2.success == 1) {
	    			viewModel.loadEntryList();
	    		}
	    	});
		},
		
		doAdd: function() {
	        var url = '<%=ajaxServiceURL%>&action=add&name=' + viewModel.f_name();
	        showSpinner(true);
	        $.getJSON(url, function (data2) {
	    		showAjaxResult(data2);
	    		if (data2.success == 1) {
	    			//alert("Added!");
	    			viewModel.loadEntryList();
	    			viewModel.f_name("");
	    		}
	    	});
			
		}
};
ko.applyBindings(viewModel);
viewModel.loadEntryList();
</script>

