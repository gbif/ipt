<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.source.title'/></title>
	<script type="text/javascript" src="${baseURL}/js/jconfirmaction.jquery.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	initHelp();
	$('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});
});   
</script>
	<style>
	.actions select{
		width: 200px;
	}
	div.definition div.title{
		width: 30%;
	}
	div.title input[type="submit"]{
		float: right;
		margin-right: 5px;
	}
	div.definition div.body{
		width: 68%;
	}
	div.details{
		padding-top: 20px;
	}
	</style>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.mapping.title'/>: <em>${mapping.extension.title}</em></h1>
<p>${mapping.extension.description}</p>
<#if mapping.extension.link?has_content>
<p><@s.text name="basic.link"/>: <a href="${mapping.extension.link}">${mapping.extension.link}</a></p>
</#if>

<h1><@s.text name='manage.mapping.properties'/></h1>
<p><@s.text name='manage.mapping.intro'/></p>

<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="mapping.do" method="post">
  	<input type="hidden" name="id" value="${id}" />

	<@selectList name="source" options=ms.config.sources objValue="name" objTitle="name" value="${mapping.source.name!}" disabled=mapping.source?exists/>
	<@select name="mapping.idColumn" options=columns value="${mapping.idColumn!}" />

  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<#if mapping.source?exists>
 	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
 	</#if>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>

  
<#if mapping.source?exists>

	<#assign group=""/>
	<#list mapping.extension.properties as p>
	
	<#if (p.group!"")!=group>
		<#assign group=p.group/>
		<h2>${p.group}</h2>
	</#if>
	<div class="definition">	
	  <div class="title">
	  	<div class="head">
			${p.name}
			<#if p.required>[required]</#if>
	  	</div>
	  </div>
	  <div class="body">
	      	<div>
	      		<em>The Mapping</em>:
	      	</div>
	      	<div>
	      		<em>Example data from source</em>:
	      	</div>
	      	<div>
	      		<em><@s.text name="basic.description"/></em>:
	      		<span>
				${p.description}
				<#if p.description?has_content><br/></#if>              	
				<#if p.link?has_content><@s.text name="basic.seealso"/> <a href="${p.link}">${p.link}</a></#if>
				<em><@s.text name="basic.examples"/></em>:
				${p.examples}
				</span>              	
	      	</div>
	      	<div>
	      	</div>
	      	<#if p.vocabulary?exists>
	      	<div>
		      	<em><@s.text name="extension.vocabulary"/></em>: 
		      	<a href="vocabulary.do?id=${p.vocabulary.uri}">${p.vocabulary.title}</a>
	      	</div>
	      	</#if>
	  </div>
	</div>
	</#list>

  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<#if id?exists>
 	<@s.submit cssClass="confirm" name="delete" key="button.delete"/>
 	</#if>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>
</#if>

</form>


<#include "/WEB-INF/pages/inc/footer.ftl">
