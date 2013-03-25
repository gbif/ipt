<%@ taglib prefix="s" uri="/struts-tags"%>
<table border="1">
	<thead>
		<tr>
			<th><s:text name="job.name"/></th>
			<th><s:text name="job.description"/></th>
			<th><s:text name="job.jobGroup"/></th>
			<th><s:text name="job.runningGroup"/></th>
			<th><s:text name="job.jobClassName"/></th>
			<th><s:text name="job.created"/></th>
			<th><s:text name="job.nextFireTime"/></th>
			<th><s:text name="job.started"/></th>
			<th><s:text name="job.instanceId"/></th>
			<th><s:text name="job.repeatInDays"/></th>			
		</tr>
	</thead>
	<tbody>	
		<s:iterator value="jobs">
			<tr>
				<td><s:property value="name"/></td>
				<td><s:property value="description"/></td>
				<td><s:property value="jobGroup"/></td>
				<td><s:property value="runningGroup"/></td>
				<td><s:property value="jobClassName"/></td>
				<td><s:property value="created"/></td>
				<td><s:property value="nextFireTime"/></td>
				<td><s:property value="started"/></td>
				<td><s:property value="instanceId"/></td>
				<td><s:property value="repeatInDays"/></td>
			</tr>
		</s:iterator>
	</tbody>
</table>