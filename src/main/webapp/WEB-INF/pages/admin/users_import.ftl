<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.UserImportAction" -->
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="admin.users.import.title"/></title>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <script src="${baseURL}/js/jquery/jquery.dataTables-1.13.6.min.js"></script>

    <#macro roleName row>
        <#if row.role??><span class="fs-smaller-2 text-nowrap dt-content-link dt-content-pill user-${row.role.name()?lower_case}"><@s.text name="user.roles.${row.role.name()?lower_case}"/></span><#else>${row.roleValue!}</#if>
    </#macro>

    <#-- whether the row has an error about the given field, e.g. validation.email.invalid for "email" -->
    <#function hasError row field>
        <#list row.errors as error>
            <#if error?starts_with("validation." + field + ".")
                || (field == "email" && (error == "admin.users.import.error.duplicate" || error == "admin.users.import.error.exists"))
                || (field == "role" && error == "admin.users.import.error.role")>
                <#return true>
            </#if>
        </#list>
        <#return false>
    </#function>

    <#macro roleSelect row="">
        <select name="roles" class="form-select form-select-sm <#if row?has_content && hasError(row, "role")>is-invalid</#if>">
            <#if row?has_content && !row.role?? && row.roleValue??>
                <option value="${row.roleValue}" selected>${row.roleValue}</option>
            </#if>
            <#list roleOptions as option>
                <option value="${option.name()}" <#if row?has_content && row.role?? && row.role.name() == option.name()>selected</#if>>
                    <@s.text name="user.roles.${option.name()?lower_case}"/>
                </option>
            </#list>
        </select>
    </#macro>

    <#macro removeButton>
        <button type="button" class="remove-row icon-button icon-button-sm" title="<@s.text name="button.remove"/>" aria-label="<@s.text name="button.remove"/>">
            <svg class="icon-button-svg icon-material-delete" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
            </svg>
        </button>
    </#macro>

    <script>
        $(document).ready(function () {
            // on submit rather than on click, so no spinner is shown when the browser blocks the form (no file chosen)
            $("#uploadForm, #rowsForm").on("submit", displayProcessing);

            // also run on load, as some browsers keep the chosen file when the page is reloaded
            $("#file").on("change", function () {
                $("#upload").prop("disabled", this.files.length === 0);
            }).trigger("change");

            var resultsTable = $("#resultsTable").DataTable({
                "pageLength": 20,
                "lengthChange": false,
                "autoWidth": false,
                "order": [],
                "language": {
                    "zeroRecords": "<@s.text name="dataTables.sZeroRecords.users"/>",
                    "info": "<@s.text name="dataTables.sInfo"/>",
                    "infoEmpty": "<@s.text name="dataTables.sInfoEmpty"/>",
                    "infoFiltered": "<@s.text name="dataTables.sInfoFiltered"/>",
                    "search": "<@s.text name="manage.mapping.filter"/>:",
                    "paginate": {
                        "next": "<@s.text name="pager.next"/>",
                        "previous": "<@s.text name="pager.previous"/>"
                    }
                },
                "columnDefs": [
                    { "orderable": false, "targets": [4, 5] }
                ]
            });

            // after any edit the rows have to be checked again before accounts can be created
            function markEdited() {
                $("#confirm").prop("disabled", true);
                $("#revalidate").removeClass("btn-outline-secondary").addClass("btn-outline-gbif-primary");
                $("#editedNotice").removeClass("d-none");
            }

            $("#rowsForm").on("input change", "input, select", markEdited);

            // Enter in a field would submit with the form's first submit button, Confirm, and create the accounts
            $("#rowsForm").on("keydown", "input", function (e) {
                if (e.key === "Enter") {
                    e.preventDefault();
                    $("#revalidate").click();
                }
            });

            $("#rowsForm").on("click", ".remove-row", function () {
                $(this).closest("tr").remove();
                markEdited();
            });

            $("#addRow").on("click", function (e) {
                e.preventDefault();
                var row = document.getElementById("newRowTemplate").content.cloneNode(true);
                $("#rowsBody").append(row);
                $("#rowsBody tr:last input[type=text]:first").focus();
                markEdited();
            });

            // download the generated credentials, as they are not stored anywhere in plain text
            $("#downloadCredentials").on("click", function (e) {
                e.preventDefault();
                var quote = function (v) { return '"' + String(v).replace(/"/g, '""') + '"'; };
                var lines = ["firstname,lastname,email,role,password"];
                $(resultsTable.rows().nodes()).filter("[data-password]").each(function () {
                    // attr rather than data, which converts values looking like numbers, booleans or JSON
                    var row = $(this);
                    lines.push(["firstname", "lastname", "email", "role", "password"]
                        .map(function (name) { return quote(row.attr("data-" + name)); }).join(","));
                });
                var blob = new Blob([lines.join("\r\n") + "\r\n"], {type: "text/csv;charset=utf-8"});
                var link = document.createElement("a");
                link.href = URL.createObjectURL(blob);
                link.download = "ipt-user-credentials.csv";
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                URL.revokeObjectURL(link.href);
            });
        });
    </script>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container-fluid bg-body border-bottom">
        <div class="container border rounded-2 mb-4">
            <div class="container my-3 p-3">
                <div class="text-center fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/users.do"><@s.text name="breadcrumb.admin.users"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.admin.users.import"/></li>
                        </ol>
                    </nav>
                </div>

                <div class="text-center">
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                        <@s.text name="admin.users.import.title"/>
                    </h1>

                    <div class="mt-2">
                        <#if preview>
                            <button id="confirm" type="submit" form="rowsForm" formaction="usersImportConfirm.do" class="btn btn-sm btn-outline-gbif-primary top-button" <#if validCount == 0>disabled</#if>>
                                <@s.text name="admin.users.import.button.confirm"><@s.param>${validCount}</@s.param></@s.text>
                            </button>
                            <button id="revalidate" type="submit" form="rowsForm" formaction="usersImportRevalidate.do" class="btn btn-sm btn-outline-secondary top-button">
                                <@s.text name="admin.users.import.button.revalidate"/>
                            </button>
                            <a href="${baseURL}/admin/usersImport.do" class="btn btn-sm btn-outline-secondary top-button">
                                <@s.text name="button.cancel"/>
                            </a>
                        <#elseif completed>
                            <a href="#" id="downloadCredentials" class="btn btn-sm btn-outline-gbif-primary top-button">
                                <@s.text name="admin.users.import.button.downloadCredentials"/>
                            </a>
                            <a href="${baseURL}/admin/users.do" class="btn btn-sm btn-outline-secondary top-button">
                                <@s.text name="button.back"/>
                            </a>
                        <#else>
                            <button id="upload" type="submit" form="uploadForm" class="btn btn-sm btn-outline-gbif-primary top-button" disabled>
                                <@s.text name="button.upload"/>
                            </button>
                            <a href="${baseURL}/admin/users.do" class="btn btn-sm btn-outline-secondary top-button">
                                <@s.text name="button.cancel"/>
                            </a>
                        </#if>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <#if preview || completed>
    <main class="container main-content-container border rounded-2 mt-4 mb-5">
        <div class="mb-3 py-4 px-5">
    <#else>
    <main class="container main-content-container">
        <div class="my-3 p-3">
    </#if>
            <#if completed>
                <p>
                    <@s.text name="admin.users.import.completed.intro"/>
                </p>
                <div class="table-responsive text-smaller pt-2">
                    <table id="resultsTable" class="display dataTable compact">
                        <thead>
                        <tr>
                            <th><@s.text name="admin.users.import.line"/></th>
                            <th><@s.text name="admin.users.name"/></th>
                            <th><@s.text name="admin.users.email"/></th>
                            <th><@s.text name="admin.users.role"/></th>
                            <th><@s.text name="user.password"/></th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <#list rows as row>
                            <#if row.created>
                                <#assign mailBody = "Dear " + row.firstname + ",\n\nWe would like to inform you that we have created an IPT account for you.\n\nAccount information:\n\nIPT: " + baseURL + "\nEmail: " + row.email + "\nPassword: " + row.password + "\nRole: " + row.role.name() + "\n\nThank you for your attention."/>
                                <tr data-firstname="${row.firstname}" data-lastname="${row.lastname}" data-email="${row.email}" data-role="${row.role.name()}" data-password="${row.password}">
                                    <td><#if row.line gt 0>${row.line}</#if></td>
                                    <td>${row.firstname} ${row.lastname}</td>
                                    <td>${row.email}</td>
                                    <td><@roleName row/></td>
                                    <td><code>${row.password}</code></td>
                                    <td class="text-end">
                                        <a href="mailto:${row.email}?subject=${"IPT account"?url}&body=${mailBody?url}" target="_blank" class="icon-button icon-button-sm" title="<@s.text name="admin.users.import.share"/>" aria-label="<@s.text name="admin.users.import.share"/>">
                                            <svg class="icon-button-svg" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                                <path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4-8 5-8-5V6l8 5 8-5v2z"></path>
                                            </svg>
                                        </a>
                                    </td>
                                </tr>
                            <#else>
                                <tr>
                                    <td><#if row.line gt 0>${row.line}</#if></td>
                                    <td>${row.firstname!} ${row.lastname!}</td>
                                    <td>${row.email!}</td>
                                    <td><@roleName row/></td>
                                    <td class="text-gbif-danger">
                                        <@s.text name="admin.users.import.status.skipped"/><#if row.errors?has_content>:
                                        <#list row.errors as error><@s.text name=error/><#sep>; </#list></#if>
                                    </td>
                                    <td></td>
                                </tr>
                            </#if>
                        </#list>
                        </tbody>
                    </table>
                </div>
            <#elseif preview>
                <p>
                    <@s.text name="admin.users.import.preview.intro">
                        <@s.param>${rows?size}</@s.param>
                        <@s.param>${validCount}</@s.param>
                        <@s.param>${invalidCount}</@s.param>
                    </@s.text>
                </p>
                <p class="text-smaller">
                    <@s.text name="admin.users.import.preview.edit"/>
                </p>
                <div id="editedNotice" class="alert alert-warning text-smaller d-none" role="status">
                    <@s.text name="admin.users.import.preview.edited"/>
                </div>
                <form id="rowsForm" action="usersImportRevalidate.do" method="post">
                    <div class="table-responsive text-smaller pt-2">
                        <table class="display dataTable compact">
                            <thead>
                            <tr>
                                <th><@s.text name="admin.users.import.line"/></th>
                                <th><@s.text name="user.firstname"/></th>
                                <th><@s.text name="user.lastname"/></th>
                                <th><@s.text name="admin.users.email"/></th>
                                <th><@s.text name="admin.users.role"/></th>
                                <th><@s.text name="admin.users.import.status"/></th>
                                <th></th>
                            </tr>
                            </thead>
                            <tbody id="rowsBody">
                            <#list rows as row>
                                <tr>
                                    <td>
                                        <#if row.line gt 0>${row.line}</#if>
                                        <input type="hidden" name="lines" value="${row.line}">
                                    </td>
                                    <td><input type="text" name="firstnames" value="${row.firstname!}" class="form-control form-control-sm <#if hasError(row, "firstname")>is-invalid</#if>"></td>
                                    <td><input type="text" name="lastnames" value="${row.lastname!}" class="form-control form-control-sm <#if hasError(row, "lastname")>is-invalid</#if>"></td>
                                    <td><input type="text" name="emails" value="${row.email!}" class="form-control form-control-sm <#if hasError(row, "email")>is-invalid</#if>"></td>
                                    <td><@roleSelect row/></td>
                                    <td>
                                        <#if row.valid>
                                            <@s.text name="admin.users.import.status.ok"/>
                                        <#else>
                                            <span class="text-gbif-danger"><#list row.errors as error><@s.text name=error/><#sep>; </#list></span>
                                        </#if>
                                    </td>
                                    <td class="text-end"><@removeButton/></td>
                                </tr>
                            </#list>
                            </tbody>
                        </table>
                    </div>
                </form>
                <template id="newRowTemplate">
                    <tr>
                        <td><input type="hidden" name="lines" value="0"></td>
                        <td><input type="text" name="firstnames" class="form-control form-control-sm"></td>
                        <td><input type="text" name="lastnames" class="form-control form-control-sm"></td>
                        <td><input type="text" name="emails" class="form-control form-control-sm"></td>
                        <td><@roleSelect/></td>
                        <td></td>
                        <td class="text-end"><@removeButton/></td>
                    </tr>
                </template>
                <a href="#" id="addRow" class="action-link-button action-link-button-primary">
                    <svg class="action-link-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                    </svg>
                    <@s.text name="admin.users.import.button.addRow"/>
                </a>
            <#else>
                <p>
                    <@s.text name="admin.users.import.intro"><@s.param>${maxRows}</@s.param></@s.text>
                </p>
                <p>
                    <@s.text name="admin.users.import.format"/>
                </p>
                <p>
                    <a href="data:text/csv;charset=utf-8,${"firstname,lastname,email,role\r\n"?url}" download="ipt-users-template.csv">
                        <@s.text name="admin.users.import.template"/>
                    </a>
                </p>

                <form id="uploadForm" action="usersImportUpload.do" method="post" enctype="multipart/form-data">
                    <div class="row g-3">
                        <div class="col-lg-6">
                            <label for="file" class="form-label"><@s.text name="admin.users.import.file"/></label>
                            <input type="file" id="file" name="file" class="form-control" accept=".csv,.txt,text/csv" required>
                        </div>
                    </div>
                </form>
            </#if>
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
