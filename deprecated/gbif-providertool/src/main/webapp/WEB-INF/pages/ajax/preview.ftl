<table class="table">
	<thead>
		<tr>
			<#list previewHeader as h>
			<th>${h!}</th>
			</#list>
		</tr>
	</thead>
	<tbody>
		<#list preview as row>
		<tr>
			<#list row as cell>
				<td>${cell!"&lt;NULL&gt;"}</td>
			</#list>
		</tr>
		</#list>
	</tbody>
</table>
