<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="title"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
        $(document).ready(function(){
            var initConfirmationModal = function () {
                if ($('.organizationConfirmDeletion').length > 0) {
                    $('.organizationConfirmDeletion').jConfirmAction({
                        titleQuestion: "<@s.text name="basic.confirm"/>",
                        question: "<@s.text name="admin.organisation.delete.confirmation.message"/>",
                        yesAnswer: "<@s.text name="basic.yes"/>",
                        cancelAnswer: "<@s.text name="basic.no"/>",
                        buttonType: "danger"
                    });
                } else {
                    setTimeout(initConfirmationModal, 100); // check again in a moment
                }
            }

            initConfirmationModal();

            setTimeout(initializeConfirmationWindows, 1000);

            function initializeConfirmationWindows() {
                $(".paginate_button").click(function () {
                    $('.organizationConfirmDeletion').jConfirmAction({
                        titleQuestion: "<@s.text name="basic.confirm"/>",
                        question: "<@s.text name="admin.organisation.delete.confirmation.message"/>",
                        yesAnswer: "<@s.text name="basic.yes"/>",
                        cancelAnswer: "<@s.text name="basic.no"/>",
                        buttonType: "danger"
                    });
                });
            }

            $('#organisation\\.key').click(function() {
                $('#organisation\\.name').val($('#organisation\\.key :selected').text());
            });
            $('#add').click(function() {
                window.location='organisation.do';
            });
            $('.edit').each(function() {
                $(this).click(function() {
                    window.location = $(this).parent('a').attr('href');
                });
            });
        });
    </script>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl">
    <#include "/WEB-INF/pages/macros/organisationsTable.ftl"/>
    <script src="${baseURL}/js/jquery/jquery.dataTables-1.13.6.min.js"></script>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container-fluid bg-body border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3">
                <div class="text-center fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                            <li class="breadcrumb-item"><@s.text name="breadcrumb.admin.organisations"/></li>
                        </ol>
                    </nav>
                </div>

                <div class="text-center">
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                        <@s.text name="admin.home.organisations"/>
                    </h1>

                    <div class="mt-2">
                        <#if registeredIpt?has_content>
                            <button id="add" class="btn btn-sm btn-outline-gbif-primary top-button"><@s.text name="button.add"/></button>
                        </#if>
                        <form class="d-inline-block" action="organisationsSynchronize.do" method="post">
                            <@s.submit name="synchronise" cssClass="btn btn-sm btn-outline-gbif-primary top-button" key="button.synchronise"/>
                        </form>
                        <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button">
                            <@s.text name="button.cancel"/>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <main class="container main-content-container border rounded-2 mt-4 mb-5">
        <div class="mb-3 py-4 px-5">
            <#if !registeredIpt?has_content>
                <div class="text-center">
                    <@s.text name="admin.home.organisations.disabled"/>
                </div>
            <#else>
                <@organisationsTable numOrganisationsShown=20 sEmptyTable="dataTables.sEmptyTable.organisations" columnToSortOn=0 sortOrder="asc" />
                <div id="tableContainer" class="table-responsive text-smaller pt-2"></div>
            </#if>
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">

</#escape>
