<table class="table">
	<thead>
		<tr>
			<#list viewColumnHeaders as h>
			<th>${h}</th>
			</#list>
		</tr>
	</thead>
	<tbody>
		<#list preview as row>
		<tr>
			<#list viewColumnHeaders as h2>
				<td>${row[h2_index]}</td>
			</#list>
		</tr>
		</#list>
	</tbody>
</table>
