<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
<title><@s.text name="login.title"/></title>
<#assign currentMenu = "account"/>
<#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">

<main class="container pt-5" style="max-width: 400px;">
    <div class="my-3 p-3 bg-body rounded shadow-sm">

        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">
            <@s.text name="login.title"/>
        </h5>

        <p class="text-muted mx-md-4 mx-2">
            <@s.text name="login.intro">
                <@s.param>${admin.email}</@s.param>
            </@s.text>
        </p>

        <form class="needs-validation" action="${baseURL}/login.do" method="post" novalidate>
            <input type="hidden" name="csrfToken" value="${newCsrfToken!}">

            <div class="row g-3 mx-md-4 mx-2 mt-0 mb-2">
                <div class="col-12">
                    <label for="email" class="form-label"><@s.text name="user.email"/></label>
                    <input type="email" class="form-control" id="email" name="email" value="${email!}">
<#--                    <div class="invalid-feedback">-->
<#--                        <@s.text name="validation.email.required"/>-->
<#--                    </div>-->
<#--                    <@s.fielderror id="field-error-email" cssClass="invalid-feedback list-unstyled field-error" fieldName="user.email"/>-->
                </div>

                <div class="col-12">
                    <label for="password" class="form-label"><@s.text name="user.password"/></label>
                    <input type="password" class="form-control" id="password" name="password" value="${password!}">
<#--                    <div class="invalid-feedback">-->
<#--                        <@s.text name="validation.email.required"/>-->
<#--                    </div>-->
<#--                    <@s.fielderror id="field-error-email" cssClass="invalid-feedback list-unstyled field-error" fieldName="user.email"/>-->
                </div>

                <#if email?has_content>
                    <p>
                        <@s.text name="login.forgottenpassword"><@s.param>${admin.email}</@s.param></@s.text>
                    </p>
                </#if>

                <div class="col-12">
                    <@s.submit cssClass="btn btn-outline-success" name="login" key="portal.login"/>
                </div>
            </div>
        </form>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
