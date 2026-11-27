<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.OrganisationsAction" -->
    <#include "/WEB-INF/pages/inc/header.ftl">
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/manage-home.css">
    <link rel="stylesheet" href="${baseURL}/styles/users.css">
    <link rel="stylesheet" href="${baseURL}/styles/organisations.css">
    <title><@s.text name="admin.home.organisations"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script src="${baseURL}/js/dataTables/dataTables-3.1.5.min.js"></script>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/icon.ftl"/>

    <#assign registered = registeredIpt?has_content/>
    <#assign hostKey = hostingOrganisationKey!""/>

    <#if registered>
    <script>
        $(document).ready(function () {
            // bound on the links before DataTables starts: csrf.js posts data-post links from a document click handler
            $('.og-remove').jConfirmAction({
                titleQuestion: "<@s.text name="dialog.organisation.delete.title"/>",
                question: "<@s.text name="dialog.organisation.delete.text"/>",
                yesAnswer: "<@s.text name="button.remove"/>",
                cancelAnswer: "<@s.text name="button.cancel"/>",
                buttonType: "danger"
            });

            var dt = $('#rtable').DataTable({
                pageLength: 20,
                lengthChange: false,
                order: [[0, "asc"]],
                layout: {
                    topStart: document.getElementById("og-count"),
                    topEnd: "search",
                    bottomStart: null,
                    bottomEnd: null,
                    bottom: {paging: {firstLast: false}}
                },
                language: {
                    emptyTable: "<@s.text name="admin.organisations.none"/>",
                    zeroRecords: "<@s.text name="dataTables.sZeroRecords.organisations"/>",
                    search: "",
                    searchPlaceholder: "<@s.text name="manage.mapping.filter"/>",
                    paginate: {
                        next: "<@s.text name="pager.next"/>",
                        previous: "<@s.text name="pager.previous"/>"
                    }
                },
                columnDefs: [
                    {targets: [1], orderSequence: ["desc", "asc"]},
                    {targets: [4], orderable: false, searchable: false}
                ]
            });

            // "Organizations: 4", "Organizations: 2 of 4" while the search leaves some out
            var texts = {};
            document.getElementById("og-texts").content.querySelectorAll("[data-k]").forEach(function (el) { texts[el.dataset.k] = el.innerHTML.trim(); });
            function count() {
                var info = dt.page.info();
                document.getElementById("og-count").innerHTML = info.recordsDisplay < info.recordsTotal
                    ? texts.count.replace("{0}", "<b>" + info.recordsDisplay + "</b>").replace("{1}", info.recordsTotal)
                    : texts.countAll.replace("{0}", "<b>" + info.recordsTotal + "</b>");
            }
            dt.on("draw", count);
            count();

            // the dialog sits above the processing spinner: close it, then show the spinner and block a second submit
            $("#og-sync").on("submit", function () {
                var button = this.querySelector("button[type=submit]");
                setTimeout(function () { button.disabled = true; }, 0);
                bootstrap.Modal.getOrCreateInstance(document.getElementById("og-sync-modal")).hide();
                displayProcessing();
            });
        });
    </script>

    <template id="og-texts">
        <span data-k="count"><@s.text name="admin.organisations.count.filtered"/></span>
        <span data-k="countAll"><@s.text name="admin.organisations.count"/></span>
    </template>
    </#if>

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
                                <li class="breadcrumb-item"><@s.text name="breadcrumb.admin.organisations"/></li>
                            </ol>
                        </nav>
                    </div>

                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal">
                        <@s.text name="admin.home.organisations"/>
                    </h1>

                    <#if registered>
                        <div class="ov-meta">
                            <span><@s.text name="admin.organisations.doiAccount"/>:
                                <#if activeDoiOrganisation??>${activeDoiOrganisation.alias!activeDoiOrganisation.name}<#else><@s.text name="admin.organisations.doiAccount.none"/></#if>
                            </span>
                        </div>
                    </#if>
                </div>
                <div class="ov-acts">
                    <#if registered>
                        <a id="add" href="${baseURL}/admin/organisation.do" class="btn btn-sm btn-outline-gbif-primary top-button"><@s.text name="button.add"/></a>
                        <button type="button" class="btn btn-sm btn-outline-secondary top-button" data-bs-toggle="modal" data-bs-target="#og-sync-modal"><@s.text name="button.synchronise"/></button>
                    </#if>
                    <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
                </div>
            </div>
        </div>
    </div>

    <main class="container main-content-container border rounded-2 mt-4 mb-5">
        <#if !registered>
            <div class="py-4 px-md-5 px-3">
                <div class="ov-banner info mb-0">
                    <span class="ov-ic"><@icon name="info_outlined"/></span>
                    <div class="ov-grow"><@s.text name="admin.home.organisations.disabled"/></div>
                    <a href="${baseURL}/admin/registration.do" class="btn btn-sm btn-outline-gbif-primary"><@s.text name="admin.organisations.register"/></a>
                </div>
            </div>
        <#else>
            <div class="py-4 px-md-5 px-3">
                <p class="ov-sub og-intro"><@s.text name="admin.organisations.intro"/></p>
                <div id="og-count" class="mh-count"></div>

                <div class="table-responsive text-smaller pt-2">
                <table id="rtable" class="display dataTable mh-table us-table og-table w-100">
                    <thead>
                    <tr>
                        <th><@s.text name="admin.organisation.key"/></th>
                        <th class="dt-right"><@s.text name="admin.users.resources"/></th>
                        <th><@s.text name="admin.organisations.publishing"/></th>
                        <th><@s.text name="admin.organisations.doiAccount"/></th>
                        <th><span class="visually-hidden"><@s.text name="manage.overview.moreActions"/></span></th>
                    </tr>
                    </thead>
                    <tbody>
                    <#assign counts = resourceCounts/>
                    <#assign blocked = deleteBlocked/>
                    <#list linkedOrganisations as o>
                        <#assign key = o.key?string/>
                        <#assign editUrl>${baseURL}/admin/organisation.do?id=${key}</#assign>
                        <#assign label = o.alias!o.name!key/>
                        <tr>
                            <td data-order="${label?lower_case}" data-search="${label} ${o.name!} ${key}">
                                <a href="${editUrl}" class="resource-table-link">${label}</a>
                                <#if key == hostKey><span class="us-you"><@s.text name="admin.organisations.hosting"/></span></#if>
                                <#if (o.name)?has_content && o.name != label><div class="mh-shortname">${o.name}</div></#if>
                            </td>
                            <#assign n = counts[key]!0/>
                            <td class="dt-right" data-order="${n}">${n}</td>
                            <td data-order="${o.canHost?c}">
                                <#if o.canHost>
                                    <@s.text name="admin.organisations.publishing.yes"/>
                                <#else>
                                    <span class="mh-muted"><@s.text name="admin.organisations.publishing.no"/></span>
                                </#if>
                            </td>
                            <td data-order="${o.agencyAccountPrimary?then('3', o.doiRegistrationAgency???then('2', o.associatedWithDoiRegistrationAgency?then('1', '0')))}">
                                <#if o.doiRegistrationAgency??>
                                    DataCite<#if o.doiPrefix?has_content> <span class="mh-muted">${o.doiPrefix}</span></#if>
                                    <#if o.agencyAccountPrimary><span class="us-tag og-active"><@s.text name="admin.organisations.doiAccount.active"/></span></#if>
                                <#elseif o.associatedWithDoiRegistrationAgency>
                                    <span class="mh-muted"><@s.text name="admin.organisations.doiAccount.shared"/></span>
                                <#else>
                                    <span class="mh-muted">&ndash;</span>
                                </#if>
                            </td>
                            <td class="us-acts">
                                <div class="dropdown">
                                    <button type="button" class="us-more-btn" data-bs-toggle="dropdown" data-bs-popper-config='{"strategy":"fixed"}' aria-expanded="false" title="<@s.text name='manage.overview.moreActions'/>" aria-label="<@s.text name='manage.overview.moreActions'/>"><@icon name="more_horiz"/></button>
                                    <ul class="dropdown-menu dropdown-menu-end">
                                        <li><a class="custom-link dropdown-item" href="${editUrl}"><@icon name="edit"/> <@s.text name="button.edit"/></a></li>
                                        <li><a class="custom-link dropdown-item" href="${portalUrl}/publisher/${key}" target="_blank" rel="noopener"><@icon name="open_in_new"/> <@s.text name="about.link"/></a></li>
                                        <li><hr class="dropdown-divider"></li>
                                        <#if blocked[key]??>
                                            <li>
                                                <span class="dropdown-item disabled" aria-disabled="true"><@icon name="delete"/> <@s.text name="button.remove"/>
                                                    <small>${blocked[key]}</small>
                                                </span>
                                            </li>
                                        <#else>
                                            <li><a class="custom-link dropdown-item us-danger og-remove" href="${editUrl}&amp;deleteFlag=true" data-post data-name="${label}"><@icon name="delete"/> <@s.text name="button.remove"/></a></li>
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
        </#if>
    </main>

    <#if registered>
    <#-- synchronize: says what comes from the Registry and what stays as set here -->
    <div class="modal fade" id="og-sync-modal" tabindex="-1" role="alertdialog" aria-labelledby="og-sync-modal-title" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered dlg-alert">
            <form id="og-sync" class="modal-content" action="organisationsSynchronize.do" method="post">
                <div class="modal-header">
                    <span class="dlg-tone info" aria-hidden="true"><@icon name="autorenew"/></span>
                    <h5 class="modal-title" id="og-sync-modal-title"><@s.text name="admin.organisations.synchronise.title"/></h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<@s.text name='button.cancel'/>"></button>
                </div>
                <div class="modal-body text-smaller">
                    <p><@s.text name="admin.organisations.synchronise.text"/></p>
                    <p class="mb-0"><@s.text name="admin.organisations.synchronise.kept"/></p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                    <button type="submit" class="btn btn-sm btn-gbif-solid" name="synchronise" value="synchronise"><@s.text name="button.synchronise"/></button>
                </div>
            </form>
        </div>
    </div>
    </#if>

    <#include "/WEB-INF/pages/inc/footer.ftl">
