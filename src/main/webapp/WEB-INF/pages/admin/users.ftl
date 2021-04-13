<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
    <title><@s.text name="admin.home.manageUsers"/></title>
    <script type="text/javascript">

        $(document).ready(function(){
            //Hack needed for Internet Explorer
            $('#create').click(function() {
                window.location='user.do';
            });
            $('#cancel').click(function() {
                window.location='home.do';
            });
        });
    </script>
    <#assign currentMenu = "admin"/>
    <#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">
    <#include "/WEB-INF/pages/macros/usersTable-bootstrap.ftl"/>
    <script type="text/javascript" src="https://code.jquery.com/jquery-3.5.1.js"></script>
    <script type="text/javascript" src="https://cdn.datatables.net/1.10.23/js/jquery.dataTables.min.js"></script>
    <script type="text/javascript" src="https://cdn.datatables.net/1.10.23/js/dataTables.bootstrap5.min.js"></script>


    <main class="container">
        <div class="row g-3">
                <div class="my-3 p-3 bg-body rounded shadow-sm">
                    <#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl">

                    <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-primary text-center">
                        <@s.text name="admin.home.manageUsers"/>
                    </h5>

                    <p class="text-muted mx-md-4 mx-2 mb-0">
                        <@s.text name='manage.metadata.methods.intro'/>
                    </p>

                    <@usersTableBootstrap numUsersShown=20 sEmptyTable="dataTables.sEmptyTable.users" columnToSortOn=0 sortOrder="asc" />
                    <div id="tableContainer" class="table-responsive mx-md-4 mx-2 pt-2"></div>

                    <div class="mx-md-4 mx-2 mt-2">
                        <button id="create" class="btn btn-outline-success"><@s.text name="button.create"/></button>
                        <button id="cancel" class="btn btn-outline-secondary"><@s.text name="button.cancel"/></button>
                    </div>

                </div>
            </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">

</#escape>
