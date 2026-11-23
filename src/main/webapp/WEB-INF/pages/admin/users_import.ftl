<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.UserImportAction" -->
    <#include "/WEB-INF/pages/inc/header.ftl">
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <link rel="stylesheet" href="${baseURL}/styles/users.css">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <title><@s.text name="admin.users.import.title"/></title>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">

    <#if completed>
        <#assign createdRows = rows?filter(r -> r.created)/>
        <#assign skippedRows = rows?filter(r -> !r.created)/>
    </#if>

    <#macro rolePill row>
        <#if row.role??><span class="fs-smaller-2 text-nowrap dt-content-pill user-${row.role.name()?lower_case}"><@s.text name="user.roles.${row.role.name()?lower_case}"/></span><#else>${row.roleValue!}</#if>
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
        <select name="roles" class="form-select form-select-sm <#if row?has_content && hasError(row, "role")>is-invalid</#if>" aria-label="<@s.text name="admin.users.role"/>">
            <#if row?has_content && !row.role?? && row.roleValue??>
                <option value="${row.roleValue}" selected><@s.text name="admin.users.import.role.unknown"><@s.param>${row.roleValue}</@s.param></@s.text></option>
            </#if>
            <#list roleOptions as option>
                <option value="${option.name()}" <#if row?has_content && row.role?? && row.role.name() == option.name()>selected</#if>>
                    <@s.text name="user.roles.${option.name()?lower_case}"/>
                </option>
            </#list>
        </select>
    </#macro>

    <#macro removeButton>
        <button type="button" class="us-more-btn imp-remove" title="<@s.text name="button.remove"/>" aria-label="<@s.text name="button.remove"/>"><@icon name="delete"/></button>
    </#macro>

    <script>
        $(document).ready(function () {
            $("#rowsForm").on("submit", displayProcessing);

            // upload: choosing or dropping a file goes straight to the check, nothing is created there
            var fileInput = $("#file");
            function sendFile() {
                if (fileInput[0].files.length > 0) {
                    displayProcessing();
                    $("#uploadForm").trigger("submit");
                }
            }
            $("#chooseFile").on("click", function () { fileInput.trigger("click"); });
            fileInput.on("change", sendFile);
            // a file dropped next to the area would be opened by the browser, leaving the page
            if (fileInput.length) {
                $(document).on("dragover drop", function (e) { e.preventDefault(); });
            }
            $("#chooseFile").on("dragover dragenter", function (e) {
                e.preventDefault();
                $(this).addClass("is-over");
            }).on("dragleave dragend", function () {
                $(this).removeClass("is-over");
            }).on("drop", function (e) {
                e.preventDefault();
                $(this).removeClass("is-over");
                // only one file is read, so pass on only the first one
                var files = e.originalEvent.dataTransfer.files;
                if (files.length > 0) {
                    var first = new DataTransfer();
                    first.items.add(files[0]);
                    fileInput[0].files = first.files;
                    sendFile();
                }
            });

            // review: after any edit the users have to be checked again before accounts can be created
            function markEdited() {
                $("#confirm").prop("disabled", true);
                $("#revalidate").removeClass("btn-outline-secondary").addClass("btn-outline-gbif-primary");
                $("#editedNotice").prop("hidden", false);
            }

            $("#rowsForm").on("input change", "input, select", markEdited);

            // Enter in a field would submit with the form's first submit button, Create, and create the accounts
            $("#rowsForm").on("keydown", "input", function (e) {
                if (e.key === "Enter") {
                    e.preventDefault();
                    $("#revalidate").click();
                }
            });

            // a user is its row plus the row with its problems
            $("#rowsForm").on("click", ".imp-remove", function () {
                var row = $(this).closest("tr");
                row.next("tr.imp-errs").remove();
                row.remove();
                markEdited();
            });

            $("#addRow").on("click", function () {
                $("#rowsBody").append(document.getElementById("newRowTemplate").content.cloneNode(true));
                $("#rowsBody tr.imp-row:last input[type=text]:first").focus();
                markEdited();
            });

            $("#problemsOnly").on("change", function () {
                $("#rowsBody").toggleClass("imp-problems-only", this.checked);
            });

            // created: copy one password. The Clipboard API needs HTTPS (or localhost); many IPTs run on plain HTTP,
            // so fall back to a selected text area there
            function copy(text) {
                if (navigator.clipboard && window.isSecureContext) return navigator.clipboard.writeText(text);
                var area = document.createElement("textarea");
                area.value = text;
                area.setAttribute("readonly", "");
                area.style.position = "fixed";
                area.style.opacity = "0";
                document.body.appendChild(area);
                area.select();
                var ok = document.execCommand("copy");
                area.remove();
                return ok ? Promise.resolve() : Promise.reject();
            }

            $(".imp-copy").on("click", function () {
                var button = $(this);
                copy(button.closest("tr").attr("data-password")).then(function () {
                    button.addClass("is-copied").attr("title", "<@s.text name="admin.users.import.copied"/>");
                    setTimeout(function () {
                        button.removeClass("is-copied").attr("title", "<@s.text name="button.copy"/>");
                    }, 1500);
                });
            });

            // download the generated credentials, as they are not stored anywhere in plain text
            $("#downloadCredentials").on("click", function () {
                var quote = function (v) { return '"' + String(v).replace(/"/g, '""') + '"'; };
                var lines = ["firstname,lastname,email,role,password"];
                $("#createdTable tr[data-password]").each(function () {
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
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3 ov-head ov-head-left">
                <div class="ov-grow">
                    <div class="fs-smaller">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb mb-0">
                                <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                                <li class="breadcrumb-item"><a href="${baseURL}/admin/users.do"><@s.text name="breadcrumb.admin.users"/></a></li>
                                <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.admin.users.import"/></li>
                            </ol>
                        </nav>
                    </div>

                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal">
                        <@s.text name="admin.users.import.title"/>
                    </h1>

                    <#if preview>
                        <div class="ov-meta">
                            <span><@s.text name="admin.users.import.count.users"/>: ${rows?size}</span>
                            <span>·</span>
                            <span><@s.text name="admin.users.import.count.ready"/>: ${validCount}</span>
                            <#if invalidCount gt 0>
                                <span>·</span>
                                <span class="text-gbif-danger"><@s.text name="admin.users.import.count.problems"/>: ${invalidCount}</span>
                            </#if>
                        </div>
                    <#elseif completed>
                        <div class="ov-meta">
                            <span><@s.text name="admin.users.import.count.created"/>: ${createdRows?size}</span>
                            <#if skippedRows?has_content>
                                <span>·</span>
                                <span class="text-gbif-danger"><@s.text name="admin.users.import.status.skipped"/>: ${skippedRows?size}</span>
                            </#if>
                        </div>
                    </#if>
                </div>
                <div class="ov-acts">
                    <#if preview>
                        <button id="confirm" type="submit" form="rowsForm" formaction="usersImportConfirm.do" class="btn btn-sm btn-gbif-solid top-button" <#if validCount == 0>disabled</#if>>
                            <@s.text name="admin.users.import.button.create"/>
                        </button>
                        <button id="revalidate" type="submit" form="rowsForm" formaction="usersImportRevalidate.do" class="btn btn-sm btn-outline-secondary top-button">
                            <@s.text name="admin.users.import.button.revalidate"/>
                        </button>
                        <#-- back to the upload: drops the uploaded file and the checked users -->
                        <a href="${baseURL}/admin/usersImport.do" class="btn btn-sm btn-outline-secondary top-button">
                            <@s.text name="button.back"/>
                        </a>
                    <#elseif completed>
                        <#if createdRows?has_content>
                            <button type="button" id="downloadCredentials" class="btn btn-sm btn-outline-gbif-primary top-button">
                                <@icon name="download"/> <@s.text name="admin.users.import.button.downloadCredentials"/>
                            </button>
                        </#if>
                        <a href="${baseURL}/admin/users.do" class="btn btn-sm btn-outline-secondary top-button">
                            <@s.text name="button.back"/>
                        </a>
                    <#else>
                        <a href="${baseURL}/admin/users.do" class="btn btn-sm btn-outline-secondary top-button">
                            <@s.text name="button.back"/>
                        </a>
                    </#if>
                </div>
            </div>
        </div>
    </div>

    <main class="container main-content-container mb-5">
        <div class="my-3 us-page imp-page">
            <#if completed>
                <#if createdRows?has_content>
                    <div class="ov-banner warn">
                        <span class="ov-ic"><@icon name="key"/></span>
                        <div class="ov-grow">
                            <h2><@s.text name="admin.users.import.passwords.share.title"/></h2>
                            <p class="ov-text"><@s.text name="admin.users.import.completed.intro"/></p>
                        </div>
                    </div>

                    <section class="ov-card">
                        <div class="ov-ch"><h2><@s.text name="admin.users.import.section.created"/></h2></div>
                        <div class="table-responsive">
                            <table id="createdTable" class="ov-table imp-table mt-2">
                                <thead>
                                <tr>
                                    <th><@s.text name="admin.users.name"/></th>
                                    <th><@s.text name="admin.users.role"/></th>
                                    <th><@s.text name="user.password"/></th>
                                    <th></th>
                                </tr>
                                </thead>
                                <tbody>
                                <#list createdRows as row>
                                    <#assign mailBody = "Dear " + row.firstname + ",\n\nWe would like to inform you that we have created an IPT account for you.\n\nAccount information:\n\nIPT: " + baseURL + "\nEmail: " + row.email + "\nPassword: " + row.password + "\nRole: " + action.getEnglishRoleName(row.role) + "\n\nThank you for your attention."/>
                                    <tr data-firstname="${row.firstname}" data-lastname="${row.lastname}" data-email="${row.email}" data-role="${row.role.name()}" data-password="${row.password}">
                                        <td>
                                            <div class="imp-name">${row.firstname} ${row.lastname}</div>
                                            <div class="imp-email">${row.email}</div>
                                        </td>
                                        <td><@rolePill row/></td>
                                        <td class="text-nowrap">
                                            <code class="imp-password">${row.password}</code>
                                            <button type="button" class="us-more-btn imp-copy" title="<@s.text name="button.copy"/>" aria-label="<@s.text name="button.copy"/>"><@icon name="content_copy"/><@icon name="check" class="imp-copied"/></button>
                                        </td>
                                        <td class="imp-acts">
                                            <a href="mailto:${row.email}?subject=${"IPT account"?url}&body=${mailBody?url}" target="_blank" class="us-more-btn custom-link" title="<@s.text name="admin.users.import.share"/>" aria-label="<@s.text name="admin.users.import.share"/>"><@icon name="mail"/></a>
                                        </td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                    </section>
                </#if>

                <#if skippedRows?has_content>
                    <section class="ov-card">
                        <div class="ov-ch"><h2><@s.text name="admin.users.import.status.skipped"/></h2></div>
                        <div class="table-responsive">
                            <table class="ov-table imp-table mt-2">
                                <thead>
                                <tr>
                                    <th><@s.text name="admin.users.import.line"/></th>
                                    <th><@s.text name="admin.users.name"/></th>
                                    <th><@s.text name="admin.users.role"/></th>
                                    <th><@s.text name="admin.users.import.problems"/></th>
                                </tr>
                                </thead>
                                <tbody>
                                <#list skippedRows as row>
                                    <tr>
                                        <td class="imp-line"><#if row.line gt 0>${row.line}</#if></td>
                                        <td>
                                            <div class="imp-name">${row.firstname!} ${row.lastname!}</div>
                                            <div class="imp-email">${row.email!}</div>
                                        </td>
                                        <td><@rolePill row/></td>
                                        <td>
                                            <ul class="imp-err-list">
                                                <#list row.errors as error><li><@icon name="error"/> <@s.text name=error/></li><#else><li><@icon name="error"/> <@s.text name="admin.users.import.error.stopped"/></li></#list>
                                            </ul>
                                        </td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                    </section>
                </#if>

            <#elseif preview>
                <div class="ov-banner info">
                    <span class="ov-ic"><@icon name="info"/></span>
                    <div class="ov-grow">
                        <p class="ov-text mt-0"><@s.text name="admin.users.import.review.create"/></p>
                    </div>
                </div>

                <div id="editedNotice" class="ov-banner warn" role="status" hidden>
                    <span class="ov-ic"><@icon name="warning"/></span>
                    <div class="ov-grow">
                        <p class="ov-text mt-0"><@s.text name="admin.users.import.preview.edited"/></p>
                    </div>
                </div>

                <section class="ov-card">
                    <div class="ov-ch">
                        <h2><@s.text name="admin.users.import.section.review"/></h2>
                        <span class="ov-sp"></span>
                        <#if invalidCount gt 0>
                            <div class="form-check form-switch mb-0 imp-filter">
                                <input class="form-check-input" type="checkbox" role="switch" id="problemsOnly">
                                <label class="form-check-label" for="problemsOnly"><@s.text name="admin.users.import.filter.problems"/></label>
                            </div>
                        </#if>
                    </div>
                    <p class="ov-sub"><@s.text name="admin.users.import.review.edit"/></p>

                    <form id="rowsForm" action="usersImportRevalidate.do" method="post">
                        <div class="table-responsive">
                            <table class="ov-table imp-table imp-edit">
                                <thead>
                                <tr>
                                    <th class="imp-line"><@s.text name="admin.users.import.line"/></th>
                                    <th><@s.text name="user.firstname"/></th>
                                    <th><@s.text name="user.lastname"/></th>
                                    <th><@s.text name="admin.users.email"/></th>
                                    <th><@s.text name="admin.users.role"/></th>
                                    <th></th>
                                </tr>
                                </thead>
                                <tbody id="rowsBody">
                                <#list rows as row>
                                    <tr class="imp-row<#if !row.valid> imp-bad</#if>">
                                        <td class="imp-line">
                                            <#if row.valid>
                                                <span title="<@s.text name="admin.users.import.status.ok"/>"><@icon name="check_circle" class="text-gbif-success"/></span>
                                            <#else>
                                                <span title="<@s.text name="admin.users.import.problems"/>"><@icon name="error" class="text-gbif-danger"/></span>
                                            </#if>
                                            <#if row.line gt 0>${row.line}</#if>
                                            <input type="hidden" name="lines" value="${row.line}">
                                        </td>
                                        <td><input type="text" name="firstnames" value="${row.firstname!}" class="form-control form-control-sm <#if hasError(row, "firstname")>is-invalid</#if>" aria-label="<@s.text name="user.firstname"/>"></td>
                                        <td><input type="text" name="lastnames" value="${row.lastname!}" class="form-control form-control-sm <#if hasError(row, "lastname")>is-invalid</#if>" aria-label="<@s.text name="user.lastname"/>"></td>
                                        <td><input type="text" name="emails" value="${row.email!}" class="form-control form-control-sm <#if hasError(row, "email")>is-invalid</#if>" aria-label="<@s.text name="admin.users.email"/>"></td>
                                        <td><@roleSelect row/></td>
                                        <td class="imp-acts"><@removeButton/></td>
                                    </tr>
                                    <#if !row.valid>
                                        <tr class="imp-errs">
                                            <td></td>
                                            <td colspan="5">
                                                <ul class="imp-err-list">
                                                    <#list row.errors as error><li><@s.text name=error/></li></#list>
                                                </ul>
                                            </td>
                                        </tr>
                                    </#if>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                    </form>
                    <template id="newRowTemplate">
                        <tr class="imp-row imp-new">
                            <td class="imp-line"><input type="hidden" name="lines" value="0"></td>
                            <td><input type="text" name="firstnames" class="form-control form-control-sm" aria-label="<@s.text name="user.firstname"/>"></td>
                            <td><input type="text" name="lastnames" class="form-control form-control-sm" aria-label="<@s.text name="user.lastname"/>"></td>
                            <td><input type="text" name="emails" class="form-control form-control-sm" aria-label="<@s.text name="admin.users.email"/>"></td>
                            <td><@roleSelect/></td>
                            <td class="imp-acts"><@removeButton/></td>
                        </tr>
                    </template>
                    <div class="add-area">
                        <button type="button" id="addRow" data-act="add"><@icon name="add"/> <@s.text name="admin.users.import.button.addRow"/></button>
                    </div>
                </section>

            <#else>
                <form id="uploadForm" action="usersImportUpload.do" method="post" enctype="multipart/form-data">
                    <button type="button" id="chooseFile" class="dlg-drop imp-drop">
                        <span class="dlg-drop-in">
                            <span class="dlg-drop-icon"><@icon name="cloud_upload"/></span>
                            <span class="dlg-drop-main"><b><@s.text name="admin.users.import.choose"/></b> <@s.text name="admin.users.import.drop"/></span>
                            <span class="dlg-drop-help"><@s.text name="admin.users.import.drop.help"><@s.param>${maxRows}</@s.param></@s.text></span>
                        </span>
                    </button>
                    <input type="file" id="file" name="file" accept=".csv,.txt,text/csv" hidden>
                </form>

                <div class="ov-banner info mt-3">
                    <span class="ov-ic"><@icon name="key"/></span>
                    <div class="ov-grow">
                        <p class="ov-text mt-0"><@s.text name="admin.users.import.passwords"/></p>
                    </div>
                </div>

                <section class="ov-card">
                    <div class="ov-ch">
                        <h2><@s.text name="admin.users.import.section.format"/></h2>
                        <span class="ov-sp"></span>
                        <a class="ov-hact custom-link imp-template" href="data:text/csv;charset=utf-8,${"firstname,lastname,email,role\r\n"?url}" download="ipt-users-template.csv"><@icon name="download"/> <@s.text name="admin.users.import.template"/></a>
                    </div>
                    <p class="ov-sub"><@s.text name="admin.users.import.format.intro"/></p>
                    <table class="ov-table imp-table imp-format">
                        <thead>
                        <tr>
                            <th><@s.text name="admin.users.import.column"/></th>
                            <th></th>
                            <th><@s.text name="admin.users.import.column.content"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td><code>firstname</code></td>
                            <td><span class="us-tag"><@s.text name="admin.users.import.column.required"/></span></td>
                            <td><@s.text name="user.firstname"/></td>
                        </tr>
                        <tr>
                            <td><code>lastname</code></td>
                            <td><span class="us-tag"><@s.text name="admin.users.import.column.required"/></span></td>
                            <td><@s.text name="user.lastname"/></td>
                        </tr>
                        <tr>
                            <td><code>email</code></td>
                            <td><span class="us-tag"><@s.text name="admin.users.import.column.required"/></span></td>
                            <td><@s.text name="admin.users.import.column.email"/></td>
                        </tr>
                        <tr>
                            <td><code>role</code></td>
                            <td><span class="us-tag imp-optional"><@s.text name="admin.users.import.column.optional"/></span></td>
                            <td>
                                <@s.text name="admin.users.import.column.role"/>
                                <div class="imp-roles">
                                    <#list roleOptions as option>
                                        <#assign roleLabel><@s.text name="user.roles.${option.name()?lower_case}"/></#assign>
                                        <span class="imp-role-opt"><code>${option.name()}</code><#if roleLabel?markup_string?trim != option.name()><span class="imp-role-full">${roleLabel}</span></#if></span>
                                    </#list>
                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </section>
            </#if>
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
