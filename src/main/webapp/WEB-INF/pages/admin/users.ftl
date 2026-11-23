<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.UserAccountsAction" -->
    <#include "/WEB-INF/pages/inc/header.ftl">
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/manage-home.css">
    <link rel="stylesheet" href="${baseURL}/styles/users.css">
    <title><@s.text name="admin.home.manageUsers"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script src="${baseURL}/js/dataTables/dataTables-3.1.5.min.js"></script>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/icon.ftl"/>

    <#assign currentEmail = ((currentUser.email)!"")?lower_case/>
    <#assign roles = []/>
    <#list users as u><#if !roles?seq_contains(u.role.name())><#assign roles = roles + [u.role.name()]/></#if></#list>
    <#assign showFilter = (roles?size > 1) || (deactivatedCount > 0)/>

    <script>
        $(document).ready(function () {
            // the row menu links post (data-post) once confirmed. Bound on the links, not delegated: csrf.js posts data-post links from a document click handler that runs before a delegated one could stop it. DataTables keeps the row nodes, so the handlers survive paging
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

            // role filter: matches the row's data-role, or data-deactivated for "Deactivated"
            var filter = "";
            $.fn.dataTable.ext.search.push(function (settings, data, index, rowData, counter) {
                var api = new $.fn.dataTable.Api(settings);
                if (!filter || api.table().node().id !== "rtable") return true;
                var row = api.row(index).node();
                return filter === "deactivated" ? row.dataset.deactivated === "true" : row.dataset.role === filter;
            });

            var dt = $('#rtable').DataTable({
                pageLength: 20,
                lengthChange: false,
                order: [[0, "asc"]],
                layout: {
                    topStart: document.getElementById("us-count"),
                    topEnd: [document.getElementById("us-toolbar"), "search"].filter(Boolean),
                    bottomStart: null,
                    bottomEnd: null,
                    bottom: {paging: {firstLast: false}}
                },
                language: {
                    zeroRecords: "<@s.text name="dataTables.sZeroRecords.users"/>",
                    search: "",
                    searchPlaceholder: "<@s.text name="manage.mapping.filter"/>",
                    paginate: {
                        next: "<@s.text name="pager.next"/>",
                        previous: "<@s.text name="pager.previous"/>"
                    }
                },
                columnDefs: [
                    {targets: [2, 3], orderSequence: ["desc", "asc"]},
                    {targets: [4], orderable: false, searchable: false}
                ]
            });

            // "Users: 4", "Users: 2 of 4" when the search or role filter leaves some out
            var texts = {};
            document.getElementById("us-texts").content.querySelectorAll("[data-k]").forEach(function (el) { texts[el.dataset.k] = el.innerHTML.trim(); });
            function count() {
                var info = dt.page.info();
                document.getElementById("us-count").innerHTML = info.recordsDisplay < info.recordsTotal
                    ? texts.count.replace("{0}", "<b>" + info.recordsDisplay + "</b>").replace("{1}", info.recordsTotal)
                    : texts.countAll.replace("{0}", "<b>" + info.recordsTotal + "</b>");
            }
            dt.on("draw", count);
            count();

            $("#us-role").on("change", function () {
                filter = $(this).val();
                dt.draw();
            });
        });
    </script>

    <template id="us-texts">
        <span data-k="count"><@s.text name="admin.users.count.filtered"/></span>
        <span data-k="countAll"><@s.text name="admin.users.count"/></span>
    </template>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container-fluid bg-body border-bottom">
        <div class="container border rounded-2 mb-4">
            <div class="container my-3 p-3 ov-head ov-head-left">
                <div class="ov-grow">
                    <div class="fs-smaller">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb mb-0">
                                <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                                <li class="breadcrumb-item"><@s.text name="breadcrumb.admin.users"/></li>
                            </ol>
                        </nav>
                    </div>

                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal">
                        <@s.text name="admin.home.manageUsers"/>
                    </h1>

                    <div class="ov-meta">
                        <span><@s.text name="admin.users.admins"/>: ${adminCount}</span>
                        <#if (deactivatedCount > 0)>
                            <span>·</span>
                            <span><@s.text name="admin.user.status.deactivated"/>: ${deactivatedCount}</span>
                        </#if>
                    </div>
                </div>
                <div class="ov-acts">
                    <a id="create" href="${baseURL}/admin/user.do" class="btn btn-sm btn-outline-gbif-primary top-button"><@s.text name="button.create"/></a>
                    <a href="${baseURL}/admin/usersImport.do" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.import"/></a>
                    <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
                </div>
            </div>
        </div>
    </div>

    <main class="container main-content-container border rounded-2 mt-4 mb-5">
        <div class="py-4 px-md-5 px-3">
            <div id="us-count" class="mh-count"></div>
            <#if showFilter>
                <div id="us-toolbar" class="mh-toolbar">
                    <select id="us-role" class="form-select form-select-sm us-role-filter" aria-label="<@s.text name="admin.users.role"/>">
                        <option value=""><@s.text name="admin.users.allRoles"/></option>
                        <#list ["Admin", "Publisher", "Manager", "User"] as r>
                            <#if roles?seq_contains(r)>
                                <option value="${r}"><@s.text name="user.roles.${r?lower_case}"/></option>
                            </#if>
                        </#list>
                        <#if (deactivatedCount > 0)>
                            <option value="deactivated"><@s.text name="admin.user.status.deactivated"/></option>
                        </#if>
                    </select>
                </div>
            </#if>

            <div class="table-responsive text-smaller pt-2">
            <table id="rtable" class="display dataTable mh-table us-table w-100">
                <thead>
                <tr>
                    <th><@s.text name="admin.users.name"/></th>
                    <th><@s.text name="admin.users.role"/></th>
                    <th class="dt-right"><@s.text name="admin.users.resources"/></th>
                    <th><@s.text name="admin.users.last.login"/></th>
                    <th><span class="visually-hidden"><@s.text name="manage.overview.moreActions"/></span></th>
                </tr>
                </thead>
                <tbody>
                <#list users as u>
                    <#assign key = u.email?lower_case/>
                    <#assign editUrl>${baseURL}/admin/user.do?id=${u.email?url}</#assign>
                    <#assign displayName = u.name!u.email/>
                    <tr data-role="${u.role.name()}" data-deactivated="${u.deactivated?c}"<#if u.deactivated> class="us-off"</#if>>
                        <td data-order="${displayName?lower_case}" data-search="${displayName} ${u.email}">
                            <a href="${editUrl}" class="resource-table-link">${displayName}</a>
                            <#if key == currentEmail><span class="us-you"><@s.text name="admin.users.you"/></span></#if>
                            <div class="mh-shortname">${u.email}</div>
                        </td>
                        <td data-order="${u.role.ordinal()}">
                            <span class="fs-smaller-2 text-nowrap dt-content-pill user-${u.role?lower_case}"><@s.text name="user.roles.${u.role?lower_case}"/></span>
                            <#if u.deactivated>
                                <span class="fs-smaller-2 text-nowrap dt-content-pill user-deactivated"><@s.text name="admin.user.status.deactivated"/></span>
                            </#if>
                        </td>
                        <#assign n = resourceCounts[key]!0/>
                        <td class="dt-right" data-order="${n}">${n}</td>
                        <td data-order="${(u.lastLogin?long?c)!0}" class="text-nowrap">
                            <#if u.lastLogin??>${u.lastLogin?datetime?string("yyyy-MM-dd HH:mm")}<#else><span class="mh-muted"><@s.text name="admin.users.last.login.never"/></span></#if>
                        </td>
                        <td class="us-acts">
                            <div class="dropdown">
                                <button type="button" class="us-more-btn" data-bs-toggle="dropdown" data-bs-popper-config='{"strategy":"fixed"}' aria-expanded="false" title="<@s.text name='manage.overview.moreActions'/>" aria-label="<@s.text name='manage.overview.moreActions'/>"><@icon name="more_horiz"/></button>
                                <ul class="dropdown-menu dropdown-menu-end">
                                    <li><a class="custom-link dropdown-item" href="${editUrl}"><@icon name="edit"/> <@s.text name="button.edit"/></a></li>
                                    <li><a class="custom-link dropdown-item us-reset" href="${editUrl}&amp;resetPassword=true" data-post data-name="${displayName}"><@icon name="key"/> <@s.text name="button.resetPassword"/></a></li>
                                    <#if u.deactivated>
                                        <li><a class="custom-link dropdown-item" href="${editUrl}&amp;activate=true" data-post><@icon name="check_circle"/> <@s.text name="button.activate"/></a></li>
                                    <#elseif key == currentEmail>
                                        <li>
                                            <span class="dropdown-item disabled" aria-disabled="true"><@icon name="lock"/> <@s.text name="button.deactivate"/>
                                                <small><@s.text name="admin.user.deactivate.blocked.current"/></small>
                                            </span>
                                        </li>
                                    <#else>
                                        <li><a class="custom-link dropdown-item us-deactivate" href="${editUrl}&amp;deactivate=true" data-post data-name="${displayName}"><@icon name="lock"/> <@s.text name="button.deactivate"/></a></li>
                                    </#if>
                                    <li><hr class="dropdown-divider"></li>
                                    <#if deleteBlocked[key]??>
                                        <li>
                                            <span class="dropdown-item disabled" aria-disabled="true"><@icon name="delete"/> <@s.text name="button.delete"/>
                                                <small>${deleteBlocked[key]}</small>
                                            </span>
                                        </li>
                                    <#else>
                                        <li><a class="custom-link dropdown-item us-danger us-delete" href="${editUrl}&amp;deleteFlag=true" data-post data-name="${displayName}"><@icon name="delete"/> <@s.text name="button.delete"/></a></li>
                                    </#if>
                                </ul>
                            </div>
                        </td>
                    </tr>
                </#list>
                </tbody>
            </table>
            </div>
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
