<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="admin.home.manageUsers"/></title>
    <script>

        $(document).ready(function(){
            //Hack needed for Internet Explorer
            $('#create').click(function() {
                window.location='user.do';
            });
            $('#cancel').click(function() {
                window.location='/';
            });
        });
    </script>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/usersTable.ftl"/>
    <script src="${baseURL}/js/jquery/jquery-3.5.1.min.js"></script>
    <script src="${baseURL}/js/jquery/jquery.dataTables-1.10.23.min.js"></script>
    <script src="${baseURL}/js/jquery/dataTables.bootstrap5-1.10.23.min.js"></script>

    <div class="container-fluid bg-body border-bottom">
        <div class="container my-3">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">
        </div>

        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="text-uppercase fw-bold fs-smaller-2">
                    <span><@s.text name="menu.admin"/></span>
                </div>

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
