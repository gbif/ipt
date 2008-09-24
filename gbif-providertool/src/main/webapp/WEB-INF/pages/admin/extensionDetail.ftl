<head>
    <title>${extension.name} Extension/></title>
    <meta name="heading" content="${extension.name} Extension"/>
</head>

<@s.form action="extensions">
	<@s.label key="extension.name"/>
	<li class="wwgrp">
	    <div class="wwlbl">
			<label class="desc"><@s.text name='extension.link'/></label>
        </div> 
		<div class="wwctrl">
			<label><a href="${extension.link}" target="_blank">${extension.link}</a></label>
		</div>
	</li>
	<@s.label key="extension.properties" value="" name=""/>
	<ul class="subform">
	<#list extension.properties as p>
	  <div class="subentry">
		<a href="${p.link}" target="_blank">${p.name}</a>
		 <label>Namespace:</label>${p.namespace}<label>Qualname:</label>${p.qualname}
	  </div>
	    <#if terms??>
			<div class="terms subform">
				<@s.text name='extension.property.terms'/>:
				<#list terms as t>
					<@s.label name="${t}"/>
				</#list>
			</div>
		</#if>
	</#list>
	</ul>
	<br/>
	<br/>
    <@s.submit cssClass="button" key="button.done" theme="simple"/>
</@s.form>

<br/>
