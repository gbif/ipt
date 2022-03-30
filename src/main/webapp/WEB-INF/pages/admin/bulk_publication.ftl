<#include "/WEB-INF/pages/inc/header.ftl">
<script src="${baseURL}/js/jconfirmation.jquery.js"></script>
<script>
    $(document).ready(function(){
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

<@s.form cssClass="topForm mx-md-4 mx-2" action="publishAll.do" method="post" namespace="" includeContext="false">
    <div class="container-fluid bg-body border-bottom">
        <div class="container">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">
        </div>

        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="text-uppercase fw-bold fs-smaller-2">
                    <span><@s.text name="menu.admin"/></span>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="admin.home.bulkPublication"/>
                </h1>

                <div class="mt-2">
                    <@s.submit cssClass="btn btn-sm btn-outline-gbif-primary top-button confirmPublishAll" name="publishAll" key="admin.config.publishResources"/>
                    <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.cancel"/>
                </div>
            </div>
        </div>
    </div>

    <main class="container">
        <div class="my-3 p-3">
            <p class="mx-md-4 mx-2">
                <@s.text name="admin.config.publishResources.details"/>
            </p>
        </div>
    </main>
</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
