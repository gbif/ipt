<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><#if "${newUser!}"=="no"><@s.text name="admin.user.title.edit"/><#else><@s.text name="admin.user.title.new"/></#if></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
        $(document).ready(function(){
            $('.userConfirmDeletion').jConfirmAction({
                titleQuestion: "<@s.text name="basic.confirm"/>",
                question: "<@s.text name="admin.user.delete.confirmation.message"/>",
                yesAnswer: "<@s.text name="basic.yes"/>",
                cancelAnswer: "<@s.text name="basic.no"/>",
                buttonType: "danger"
            });

            $('.confirmPasswordReset').jConfirmAction({
                titleQuestion: "<@s.text name="basic.confirm"/>",
                question: "<@s.text name="admin.user.resetPassword.confirmation.message"/>",
                yesAnswer: "<@s.text name="basic.yes"/>",
                cancelAnswer: "<@s.text name="basic.no"/>",
                buttonType: "danger"
            });
        });
    </script>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl">

    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">
        </div>

        <div class="container my-3 p-3">
            <div class="text-center text-uppercase fw-bold fs-smaller-2">
                <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                    <ol class="breadcrumb justify-content-center mb-0">
                        <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                        <li class="breadcrumb-item"><a href="${baseURL}/admin/users.do"><@s.text name="breadcrumb.admin.users"/></a></li>
                        <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.admin.users.user"/></li>
                    </ol>
                </nav>
            </div>

            <div class="text-center">
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <#if "${newUser!}"=="no">
                        ${user.firstname!} ${user.lastname!}
                    <#else>
                        <@s.text name="admin.user.title.new"/>
                    </#if>
                </h1>

                <#if user.email?has_content>
                    <div class="text-smaller">
                        <a href="mailto:${user.email!}">${user.email!}</a>
                    </div>
                </#if>

                <div class="mt-2">
                    <@s.submit tabindex=7 cssClass="button btn btn-sm btn-outline-gbif-primary top-button" form="newuser" name="save" key="button.save"/>
                    <#if "${newUser!}"=="no">
                        <@s.submit tabindex=8 cssClass="userConfirmDeletion btn btn-sm btn-outline-gbif-danger top-button" form="newuser" name="delete" key="button.delete"/>
                        <@s.submit tabindex=9 cssClass="confirmPasswordReset button btn btn-sm btn-outline-gbif-danger top-button" form="newuser" name="resetPassword" key="button.resetPassword" />
                    </#if>
                    <@s.submit tabindex=10 cssClass="button btn btn-sm btn-outline-secondary top-button" form="newuser" name="cancel" key="button.cancel"/>
                </div>
            </div>
        </div>
    </div>

    <main class="container">
        <div class="my-3 p-3">
            <p class="mb-0">
                <@s.text name="admin.user.intro"/>
            </p>
            <p class="mb-0">
                <@s.text name="admin.user.intro2"/>
            </p>

            <form id="newuser" class="needs-validation" action="user.do" method="post">
                <div class="row g-3 mt-2">
                    <@s.hidden name="id" value="${(user.email)!}" required="true"/>

                    <div class="col-md-6">
                        <@input tabindex=1 name="user.firstname" />
                    </div>

                    <div class="col-md-6">
                        <@input tabindex=2 name="user.lastname" />
                    </div>

                    <div class="col-md-6">
                        <@input tabindex=3 name="user.email" disabled=id?has_content/>
                    </div>

                    <div class="col-md-6">
                        <@select name="user.role" tabindex=4 value=(user.role)! javaGetter=false options={"User":"user.roles.user", "Manager":"user.roles.manager", "Publisher":"user.roles.publisher", "Admin":"user.roles.admin"}/>
                    </div>

                    <#if "${newUser!}"!="no">
                        <div class="col-md-6">
                            <@input name="user.password" type="password" tabindex=5 />
                        </div>
                        <div class="col-md-6">
                            <@input name="password2" i18nkey="user.password2" type="password" tabindex=6/>
                        </div>
                    </#if>
                </div>
            </form>
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
