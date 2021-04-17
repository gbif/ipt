<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="login.title"/></title>
<#assign currentMenu = "about"/>
<#--Custom styles only for about page-->
<style>
    h1 {
        font-size: 20px !important;
        text-align: center !important;
        padding-top: .5rem !important;
        padding-bottom: .5rem !important;
        margin-bottom: .5rem !important;
        color: #4e565f !important;
        border-bottom: 1px solid #dee2e6 !important;
    }

    #not-registered {
        text-align: center !important;
    }
</style>
<#include "/WEB-INF/pages/inc/menu.ftl">

<main class="container">
    <div class="my-3 p-3 bg-body rounded shadow-sm" id="summary">
        <div class="mx-md-4 mx-2 text-muted">
            <@content?interpret />
        </div>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
