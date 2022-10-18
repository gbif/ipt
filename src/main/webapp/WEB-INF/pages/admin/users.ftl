<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="admin.home.manageUsers"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/usersTable.ftl"/>
    <script src="${baseURL}/js/jquery/jquery.dataTables-1.10.23.min.js"></script>
    <script src="${baseURL}/js/jquery/dataTables.bootstrap5-1.10.23.min.js"></script>
    <script>
        $(document).ready(function(){
            var initConfirmationModal = function () {
                if ($('.userConfirmDeletion').length > 0 && $('.confirmPasswordReset').length > 0) {
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
                } else {
                    setTimeout(initConfirmationModal, 100); // check again in a moment
                }
            }

            initConfirmationModal();

            // Hack needed for Internet Explorer
            $('#create').click(function() {
                window.location='user.do';
            });
            $('#cancel').click(function() {
                window.location='/';
            });
        });
    </script>

    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">
        </div>

        <div class="container my-3 p-3">
            <div class="text-center text-uppercase fw-bold fs-smaller-2">
                <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                    <ol class="breadcrumb justify-content-center mb-0">
                        <li class="breadcrumb-item"><a href="/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                        <li class="breadcrumb-item"><@s.text name="breadcrumb.admin.users"/></li>
                    </ol>
                </nav>
            </div>

            <div class="text-center">
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="admin.home.manageUsers"/>
                </h1>

                <div class="mt-2">
                    <button id="create" class="btn btn-sm btn-outline-gbif-primary top-button"><@s.text name="button.create"/></button>
                    <button id="cancel" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.cancel"/></button>
                </div>
            </div>
        </div>
    </div>

    <main class="container">
        <div class="my-3 p-3">
            <@usersTable numUsersShown=20 sEmptyTable="dataTables.sEmptyTable.users" columnToSortOn=0 sortOrder="asc" />
            <div id="tableContainer" class="table-responsive text-smaller pt-2"></div>
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">

</#escape>
