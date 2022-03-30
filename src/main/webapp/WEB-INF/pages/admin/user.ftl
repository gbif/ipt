<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><#if "${newUser!}"=="no"><@s.text name="admin.user.title.edit"/><#else><@s.text name="admin.user.title.new"/></#if></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
        $(document).ready(function(){
            $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>", buttonType: "danger"});
        });
    </script>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl">

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
                    <#if "${newUser!}"=="no"><@s.text name="admin.user.title.edit"/><#else><@s.text name="admin.user.title.new"/></#if>
                </h1>

                <div class="mt-2">
                    <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" form="newuser" name="save" key="button.save"/>
                    <#if "${newUser!}"=="no">
                        <@s.submit cssClass="confirm btn btn-sm btn-outline-gbif-danger top-button" form="newuser" name="delete" key="button.delete"/>
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-danger top-button" form="newuser" name="resetPassword" key="button.resetPassword" />
                    </#if>
                    <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" form="newuser" name="cancel" key="button.cancel"/>
                </div>
            </div>
        </div>
    </div>

    <main class="container">
        <div class="my-3 p-3">
            <p class="mx-md-4 mx-2 mb-0">
                <@s.text name="admin.user.intro"/>
            </p>
            <p class="mx-md-4 mx-2 mb-0">
                <@s.text name="admin.user.intro2"/>
            </p>

            <form id="newuser" class="needs-validation" action="user.do" method="post">
                <div class="row g-3 mx-md-3 mx-1 mt-2">
                    <@s.hidden name="id" value="${user.email!}" required="true"/>

                    <div class="col-md-6">
                        <@input name="user.firstname" />
                    </div>

                    <div class="col-md-6">
                        <@input name="user.lastname" />
                    </div>

                    <div class="col-md-6">
                        <@input name="user.email" disabled=id?has_content/>
                    </div>

                    <div class="col-md-6">
                        <@select name="user.role" value=user.role javaGetter=false options={"User":"user.roles.user", "Manager":"user.roles.manager", "Publisher":"user.roles.publisher", "Admin":"user.roles.admin"}/>
                    </div>

                    <#if "${newUser!}"!="no">
                        <div class="col-md-6">
                            <@input name="user.password" type="password" />
                        </div>
                        <div class="col-md-6">
                            <@input name="password2" i18nkey="user.password2" type="password"/>
                        </div>
                    </#if>
                </div>
            </form>
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
