<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="title"/></title>
    <script>

        $(document).ready(function(){
            $('#organisation\\.key').click(function() {
                $('#organisation\\.name').val($('#organisation\\.key :selected').text());
            });
            //Hack needed for Internet Explorer X.*x
            $('#add').click(function() {
                window.location='organisation.do';
            });
            $('#cancel').click(function() {
                window.location='organisations.do?cancel=true';
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
    <script src="${baseURL}/js/jquery/jquery-3.5.1.min.js"></script>
    <script src="${baseURL}/js/jquery/jquery.dataTables-1.10.23.min.js"></script>
    <script src="${baseURL}/js/jquery/dataTables.bootstrap5-1.10.23.min.js"></script>

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
                    <@s.text name="admin.home.editOrganisations"/>
                </h1>

                <div class="mt-2">
                    <button id="add" class="btn btn-sm btn-outline-gbif-primary top-button"><@s.text name="button.add"/></button>
                    <button id="cancel" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.cancel"/></button>
                </div>
            </div>
        </div>
    </div>

    <main class="container">
        <div class="my-3 p-3">
            <@organisationsTable numOrganisationsShown=20 sEmptyTable="dataTables.sEmptyTable.organisations" columnToSortOn=0 sortOrder="asc" />
            <div id="tableContainer" class="table-responsive text-smaller mx-md-4 mx-2 pt-2"></div>
        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">

</#escape>
