<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.UserAccountsAction" -->
    <#assign editUserPage = (newUser! == "no")/>
    <#assign newUserPage = !editUserPage/>

    <#include "/WEB-INF/pages/inc/header.ftl">

    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/users.css">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <title><#if editUserPage><@s.text name="admin.user.title.edit"/><#else><@s.text name="admin.user.title.new"/></#if></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>

    <#if editUserPage>
        <#assign displayName = user.name!user.email/>
        <#assign actionUrl>${baseURL}/admin/user.do?id=${user.email?url}</#assign>
    </#if>

    <script>
        $(document).ready(function(){
            <#if editUserPage>
            // the ⋯ menu links post (data-post) once confirmed; they change only what they say, unsaved edits are dropped
            $('.us-delete').jConfirmAction({
                titleQuestion: "<@s.text name="dialog.user.delete.title"/>",
                question: "<@s.text name="dialog.user.delete.text"/>",
                yesAnswer: "<@s.text name="button.delete"/>",
                cancelAnswer: "<@s.text name="button.cancel"/>",
                buttonType: "danger"
            });
            $('.us-deactivate').jConfirmAction({
                titleQuestion: "<@s.text name="dialog.user.deactivate.title"/>",
                question: "<@s.text name="dialog.user.deactivate.text"/>",
                yesAnswer: "<@s.text name="button.deactivate"/>",
                cancelAnswer: "<@s.text name="button.cancel"/>",
                buttonType: "primary", tone: "warn", icon: "lock"
            });
            $('.us-reset').jConfirmAction({
                titleQuestion: "<@s.text name="dialog.user.resetPassword.title"/>",
                question: "<@s.text name="dialog.user.resetPassword.text"/>",
                yesAnswer: "<@s.text name="button.resetPassword"/>",
                cancelAnswer: "<@s.text name="button.cancel"/>",
                buttonType: "primary", tone: "warn", icon: "key"
            });
            </#if>

            $("#save").on("click", displayProcessing);

            $("#us-show-all").on("click", function () {
                $(".us-res tr.us-more").prop("hidden", false);
                $(this).remove();
            });
        });
    </script>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl">

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container-fluid bg-body border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3 ov-head ov-head-left">
                <div class="ov-grow">
                    <div class="fs-smaller">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb mb-0">
                                <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                                <li class="breadcrumb-item"><a href="${baseURL}/admin/users.do"><@s.text name="breadcrumb.admin.users"/></a></li>
                                <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.admin.users.user"/></li>
                            </ol>
                        </nav>
                    </div>

                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal">
                        <#if editUserPage>${displayName}<#else><@s.text name="admin.user.title.new"/></#if>
                    </h1>

                    <#if editUserPage>
                        <div class="ov-meta">
                            <#if user.deactivated>
                                <span class="fs-smaller-2 text-nowrap dt-content-pill user-deactivated"><@s.text name="admin.user.status.deactivated"/></span>
                            </#if>
                            <a href="mailto:${user.email}">${user.email}</a>
                            <span>·</span>
                            <span><@s.text name="admin.users.last.login"/>: <#if user.lastLogin??>${user.lastLogin?datetime?string("yyyy-MM-dd HH:mm")}<#else><@s.text name="admin.users.last.login.never"/></#if></span>
                        </div>
                    </#if>
                </div>
                <div class="ov-acts">
                    <button type="submit" id="save" name="save" value="save" form="newuser" class="button btn btn-sm btn-gbif-solid top-button"><@icon name="save"/> <@s.text name="button.save"/></button>
                    <button type="submit" id="cancel" name="cancel" value="cancel" form="newuser" formnovalidate class="button btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></button>
                    <#if editUserPage>
                        <#-- more actions last, as on the resource overview -->
                        <div class="dropdown us-acts">
                            <button id="btnGroup" type="button" class="btn btn-sm btn-outline-secondary top-button ov-more-btn" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.moreActions'/>" aria-label="<@s.text name='manage.overview.moreActions'/>"><@icon name="more_horiz"/></button>
                            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="btnGroup">
                                <li><a class="custom-link dropdown-item us-reset" href="${actionUrl}&amp;resetPassword=true" data-post data-name="${displayName}"><@icon name="key"/> <@s.text name="button.resetPassword"/></a></li>
                                <#if user.deactivated>
                                    <li><a class="custom-link dropdown-item" href="${actionUrl}&amp;activate=true" data-post><@icon name="check_circle"/> <@s.text name="button.activate"/></a></li>
                                <#elseif ownAccount>
                                    <li>
                                        <span class="dropdown-item disabled" aria-disabled="true"><@icon name="lock"/> <@s.text name="button.deactivate"/>
                                            <small><@s.text name="admin.user.deactivate.blocked.current"/></small>
                                        </span>
                                    </li>
                                <#else>
                                    <li><a class="custom-link dropdown-item us-deactivate" href="${actionUrl}&amp;deactivate=true" data-post data-name="${displayName}"><@icon name="lock"/> <@s.text name="button.deactivate"/></a></li>
                                </#if>
                                <li><hr class="dropdown-divider"></li>
                                <#if deleteBlockedReason??>
                                    <li>
                                        <span class="dropdown-item disabled" aria-disabled="true"><@icon name="delete"/> <@s.text name="button.delete"/>
                                            <small>${deleteBlockedReason}</small>
                                        </span>
                                    </li>
                                <#else>
                                    <li><a class="custom-link dropdown-item us-danger us-delete" href="${actionUrl}&amp;deleteFlag=true" data-post data-name="${displayName}"><@icon name="delete"/> <@s.text name="button.delete"/></a></li>
                                </#if>
                            </ul>
                        </div>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <main class="container main-content-container mb-5">
        <div class="my-3 us-page">
            <#if editUserPage && user.deactivated>
                <div class="ov-banner warn">
                    <span class="ov-ic"><@icon name="lock"/></span>
                    <div class="ov-grow">
                        <h2><@s.text name="admin.user.deactivated.title"/></h2>
                        <p class="ov-text"><@s.text name="admin.user.deactivated.text"/></p>
                    </div>
                    <a href="${actionUrl}&amp;activate=true" data-post class="btn btn-sm btn-outline-gbif-primary"><@s.text name="button.activate"/></a>
                </div>
            </#if>

            <form id="newuser" class="needs-validation" action="user.do" method="post">
                <#if editUserPage>
                    <input type="hidden" name="id" value="${user.email}">
                </#if>

                <section class="ov-card">
                    <div class="ov-ch"><h2><@s.text name="admin.user.section.account"/></h2></div>
                    <div class="row g-3 mt-0">
                        <div class="col-md-6">
                            <@input name="user.firstname" value=user.firstname! />
                        </div>
                        <div class="col-md-6">
                            <@input name="user.lastname" value=user.lastname! />
                        </div>
                        <#if newUserPage>
                            <div class="col-md-6">
                                <@input name="user.email" value=user.email! />
                            </div>
                            <div class="col-12">
                                <div class="ov-banner info compact">
                                    <span class="ov-ic"><@icon name="info_outlined"/></span>
                                    <div class="ov-grow"><@s.text name="admin.user.email.note"/></div>
                                </div>
                            </div>
                        </#if>
                    </div>
                </section>

                <section class="ov-card">
                    <div class="ov-ch"><h2 id="us-role-label"><@s.text name="user.role"/></h2></div>
                    <p class="ov-sub"><@s.text name="admin.user.role.intro"/></p>
                    <#assign currentRole = (user.role.name())!"User"/>
                    <div class="us-roles" role="radiogroup" aria-labelledby="us-role-label">
                        <#list ["User", "Manager", "Publisher", "Admin"] as r>
                            <label class="us-role">
                                <input type="radio" name="user.role" value="${r}"<#if currentRole == r> checked</#if>>
                                <span>
                                    <span class="us-role-name"><@s.text name="user.roles.${r?lower_case}"/></span>
                                    <span class="us-role-desc"><@s.text name="admin.user.role.${r?lower_case}.desc"/></span>
                                </span>
                            </label>
                        </#list>
                    </div>
                    <@s.fielderror cssClass="invalid-feedback list-unstyled d-block my-1" fieldName="user.role"/>
                </section>

                <#if newUserPage>
                    <section class="ov-card">
                        <div class="ov-ch"><h2><@s.text name="user.password"/></h2></div>
                        <div class="row g-3 mt-0">
                            <div class="col-md-6">
                                <@input name="user.password" type="password" />
                            </div>
                            <div class="col-md-6">
                                <@input name="password2" i18nkey="user.password2" type="password" />
                            </div>
                        </div>
                    </section>
                </#if>
            </form>

            <#if editUserPage>
                <section class="ov-card">
                    <div class="ov-ch">
                        <h2><@s.text name="admin.users.resources"/></h2>
                        <span class="ov-sub m-0">${userResources?size}</span>
                    </div>
                    <#assign adminUser = user.role.name() == "Admin"/>
                    <#if adminUser>
                        <p class="ov-sub"><@s.text name="admin.user.resources.admin"/></p>
                    </#if>
                    <#if userResources?has_content>
                        <table class="ov-table us-res">
                            <thead>
                            <tr>
                                <th><@s.text name="manage.home.resource"/></th>
                                <#if !adminUser><th><@s.text name="admin.user.resources.link"/></th></#if>
                            </tr>
                            </thead>
                            <tbody>
                            <#list userResources as ur>
                                <tr<#if (ur?index >= 10)> class="us-more" hidden</#if>>
                                    <td>
                                        <a href="${baseURL}/manage/resource.do?r=${ur.shortname?url}" class="resource-table-link">${ur.title}</a>
                                        <#if ur.title != ur.shortname><div class="mh-shortname">${ur.shortname}</div></#if>
                                    </td>
                                    <#if !adminUser>
                                        <td class="text-nowrap">
                                            <#if ur.creator><span class="us-tag"><@s.text name="admin.user.resources.creator"/></span></#if>
                                            <#if ur.manager><span class="us-tag"><@s.text name="admin.user.resources.manager"/></span></#if>
                                        </td>
                                    </#if>
                                </tr>
                            </#list>
                            </tbody>
                        </table>
                        <#if (userResources?size > 10)>
                            <button type="button" class="us-show-all" id="us-show-all"><@s.text name="admin.extension.usedBy.showAll"/>: ${userResources?size}</button>
                        </#if>
                    <#else>
                        <p class="ov-sub mb-0"><@s.text name=adminUser?then("admin.user.resources.none.admin", "admin.user.resources.none")/></p>
                    </#if>
                </section>
            </#if>
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
