<%@ include file="/common/taglibs.jsp"%>
<div id="actions">
	<label>Resource Actions</label>
	<ul class="plain">							
		<li><a href="<s:url action="resource" namespace="/manage"><s:param name="resourceType" value="occ" /></s:url>">New Occurrence Resource</a></li>
		<li><a href="<s:url action="resource" namespace="/manage"><s:param name="resourceType" value="tax" /></s:url>">New Checklist Resource</a></li>
		<li><a href="<s:url action="resource" namespace="/manage"><s:param name="resourceType" value="meta" /></s:url>">New Metadata Resource</a></li>
	</ul>
</div>
