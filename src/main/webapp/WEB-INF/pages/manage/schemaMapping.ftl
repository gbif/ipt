<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl"/>
    <title><@s.text name="manage.mapping.title"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
        // spy scroll and manage sidebar menu
        $(window).scroll(function () {
            var scrollPosition = $(document).scrollTop();

            $('.bd-toc nav a.sidebar-navigation-link').each(function () {
                var currentLink = $(this);
                var anchor = $(currentLink.attr("href"));
                var sectionId = anchor[0].id.replace("anchor-", "");
                var section = $("#" + sectionId);
                var sectionsContainer = $("#sections");

                if (sectionsContainer.position().top - 100 > scrollPosition) {
                    var removeActiveFromThisLink = $('.bd-toc nav a.active');
                    removeActiveFromThisLink.removeClass('active');
                } else if (section.position().top - 100  <= scrollPosition
                    && section.position().top + section.height() > scrollPosition) {
                    if (!currentLink.hasClass("active")) {
                        var removeFromThisLink = $('.bd-toc nav a.active');
                        removeFromThisLink.removeClass('active');
                        $(this).addClass('active');
                    }
                }
            });
        })
    </script>

    <#assign currentMenu = "manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl"/>
    <#include "/WEB-INF/pages/macros/forms.ftl"/>
    <#include "/WEB-INF/pages/macros/popover.ftl"/>

    <#macro showField field index>
        <div class="row py-1 g-2 mappingRow border-bottom">
            <div class="col-lg-4 pt-1">
                <strong>${field.name}</strong>
            </div>

            <div class="col-lg-4">
                <select id="fIdx${index}" class="fidx form-select" name="fields[${index}].index">
                    <option value=""></option>
                </select>
            </div>

            <div class="col-lg-4">
                <input id="fVal${index}" class="fval form-control" name="fields[${index}].defaultValue" value="${field.defaultValue!}"/>
            </div>
        </div>
    </#macro>

    <#macro threeButtons>
        <div class="col-12 my-3">
            <@s.submit cssClass="button btn btn-outline-gbif-primary" name="save" key="button.save"/>
            <@s.submit cssClass="confirm btn btn-outline-gbif-danger" name="delete" key="button.delete"/>
            <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.back"/>
        </div>
    </#macro>

    <form id="mappingForm" class="needs-validation" action="schemaMapping.do" method="post">
        <div class="container-fluid bg-body border-bottom">

            <div class="container pt-2">
                <#include "/WEB-INF/pages/inc/action_alerts.ftl">
            </div>

            <div class="container p-3">

                <div class="text-center">
                    <h5 class="pt-2 text-gbif-header fs-4 fw-400 text-center">
                        <@popoverPropertyInfo "manage.mapping.intro"/>
                        <@s.text name='manage.mapping.title'/>
                    </h5>

                    <div class="text-center fs-smaller">
                        <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                    </div>

                </div>
            </div>
        </div>

        <div class="container-fluid bg-body">
            <div class="container bd-layout">

                <main class="bd-main">

                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <nav id="sidebar-content">
                            <ul>
                                <#list dataSchema.subSchemas as subSchema>
                                    <li>
                                        <a href="#anchor-${subSchema.name}" class="sidebar-navigation-link">${subSchema.title}</a>
                                    </li>
                                </#list>
                            </ul>
                        </nav>
                    </div>

                    <div class="bd-content ps-lg-4">
                        <div id="sections">
                            <#list dataSchema.subSchemas as subSchema>
                                <span class="anchor anchor-home-resource-page" id="anchor-${subSchema.name}"></span>
                                <div id="${subSchema.name}" class="mt-5">
                                    <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
                                        ${subSchema.title}
                                    </h4>
                                    <#list subSchema.fields as field>
                                        <@showField field field_index/>
                                    </#list>
                                    <div>
                                        <@threeButtons/>
                                    </div>
                                </div>
                            </#list>
                        </div>
                    </div>
                </main>
            </div>
        </div>

    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl"/>
</#escape>
