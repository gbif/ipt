<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="manage.resource.create.title"/></title>
 <#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/popover.ftl">

<main class="container">
    <div class="my-3 p-3 border rounded shadow-sm">
        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header fw-400 text-center">
            <@s.text name="manage.resource.create.title"/>
        </h5>
        <#include "inc/create_new_resource.ftl"/>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
