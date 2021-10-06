<#include "/WEB-INF/pages/inc/header.ftl">
<script src="${baseURL}/js/jconfirmation.jquery.js"></script>
<script>
    $(document).ready(function(){
        initHelp();

        $('.confirmPublishAll').jConfirmAction({
            titleQuestion : "<@s.text name="basic.confirm"/>",
            yesAnswer : "<@s.text name='basic.yes'/>",
            cancelAnswer : "<@s.text name='basic.no'/>",
            buttonType: "primary"
        });
    });
</script>

<title><@s.text name="title"/></title>
<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/macros/forms.ftl">
<#include "/WEB-INF/pages/inc/menu.ftl">

<main class="container">
    <div class="my-3 p-3 bg-body rounded shadow-sm">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">

        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
            <@s.text name="admin.home.bulkPublication"/>
        </h5>

        <p class="mx-md-4 mx-2">
            <@s.text name="admin.config.publishResources.details"/>
        </p>

        <@s.form cssClass="topForm mx-md-4 mx-2" action="publishAll.do" method="post" namespace="" includeContext="false">
            <@s.submit cssClass="btn btn-outline-gbif-primary confirmPublishAll" name="publishAll" key="admin.config.publishResources"/>
        </@s.form>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
