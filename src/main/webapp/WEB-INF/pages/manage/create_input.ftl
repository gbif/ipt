<#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
	<title><@s.text name="manage.resource.create.title"/></title>
 <#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">
<#include "/WEB-INF/pages/macros/forms-bootstrap.ftl"/>

<main class="container">
    <div class="my-3 p-3 bg-body rounded shadow-sm">
        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
            <@s.text name="manage.resource.create.title"/>
        </h5>
        <#include "inc/create_new_resource-bootstrap.ftl"/>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
