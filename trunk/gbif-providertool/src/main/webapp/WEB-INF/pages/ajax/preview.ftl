<table class="table">
	<thead>
		<tr>
			<#list previewHeader as h>
			<th>${h}</th>
			</#list>
		</tr>
	</thead>
	<tbody>
		<#list preview as row>
		<tr>
			<#list row as cell>
				<td>${cell}</td>
			</#list>
		</tr>
		</#list>
	</tbody>
</table>
