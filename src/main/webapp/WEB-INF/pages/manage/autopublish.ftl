<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.autopublish.title'/></title>

    <#assign currentMenu = "manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>
    <#include "/WEB-INF/pages/macros/popover.ftl"/>

    <script>
        $(document).ready(function() {
            // on select of publishing frequency set parameters for publishing frequency
            $('#updateFrequency').change(function () {
                var str = "";
                $("#updateFrequency option:selected").each(function () {
                    str += $(this).val();
                });

                $('#introAnnually').hide();
                $('#introBiAnnually').hide();
                $('#introMonthly').hide();
                $('#introWeekly').hide();
                $('#introDaily').hide();
                $('#introOff').hide();
                $('#helpAnnually').hide();
                $('#helpBiAnnually').hide();
                $('#helpMonthly').hide();
                $('#helpWeekly').hide();
                $('#frequencyDetailsEvery').hide();
                $('#frequencyDetailsAt').hide();
                $('#updateFrequencyMonthWrapper').hide();
                $('#updateFrequencyBiMonthWrapper').hide();
                $('#updateFrequencyDayWrapper').hide();
                $('#updateFrequencyDayOfWeekWrapper').hide();
                $('#updateFrequencyTimeWrapper').hide();

                if (str === "annually") {
                    $('#introAnnually').show();
                    $('#helpAnnually').show();
                    $('#frequencyDetailsEvery').show();
                    $('#updateFrequencyMonthWrapper').show();
                    $('#updateFrequencyDayWrapper').show();
                    $('#frequencyDetailsAt').show();
                    $('#updateFrequencyTimeWrapper').show();
                } else if (str === "biannually") {
                    $('#introBiAnnually').show();
                    $('#helpBiAnnually').show();
                    $('#frequencyDetailsEvery').show();
                    $('#updateFrequencyBiMonthWrapper').show();
                    $('#updateFrequencyDayWrapper').show();
                    $('#frequencyDetailsAt').show();
                    $('#updateFrequencyTimeWrapper').show();
                } else if (str === "monthly") {
                    $('#introMonthly').show();
                    $('#helpMonthly').show();
                    $('#frequencyDetailsEvery').show();
                    $('#updateFrequencyDayWrapper').show();
                    $('#frequencyDetailsAt').show();
                    $('#updateFrequencyTimeWrapper').show();
                } else if (str === "weekly") {
                    $('#introWeekly').show();
                    $('#helpWeekly').show();
                    $('#frequencyDetailsEvery').show();
                    $('#updateFrequencyDayOfWeekWrapper').show();
                    $('#frequencyDetailsAt').show();
                    $('#updateFrequencyTimeWrapper').show();
                } else if (str === "daily") {
                    $('#introDaily').show();
                    $('#frequencyDetailsAt').show();
                    $('#updateFrequencyTimeWrapper').show();
                } else {
                    $('#introOff').show();
                }

            }).change();
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
                        <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                        <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                        <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.autopublishing"/></li>
                    </ol>
                </nav>
            </div>

            <div class="text-center">
                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    <@s.text name="manage.autopublish.title"/>
                </h1>

                <div class="text-smaller">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </div>

                <div class="mt-2">
                    <@s.submit form="autopublish" cssClass="btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                    <@s.submit form="autopublish" cssClass="btn btn-sm btn-outline-secondary top-button" name="cancel"  key="button.cancel"/>
                </div>
            </div>
        </div>
    </div>

    <main class="container">
        <div class="my-3 p-3">
            <p class="text-center"><@s.text name='manage.autopublish.intro'/></p>

            <form id="autopublish" class="topForm" action="auto-publish.do" method="post">
                <#if resource.isDeprecatedAutoPublishingConfiguration()>
                    <ul class="fielderror">
                        <li><span><@s.text name='manage.overview.autopublish.deprecated.warning.description'/></span></li>
                    </ul>
                    <br/>
                </#if>
                <input type="hidden" name="r" value="${resource.shortname}" />

                <#-- Auto-publishing frequency-->
                <#assign updateFrequency="">
                <#if resource.updateFrequency??>
                    <#assign updateFrequency=resource.updateFrequency.identifier>
                </#if>
                <#-- Auto-publishing day-->
                <#assign updateFrequencyDay="">
                <#if resource.updateFrequencyDay??>
                    <#assign updateFrequencyDay=resource.updateFrequencyDay>
                </#if>
                <#-- Auto-publishing month-->
                <#assign updateFrequencyMonth="">
                <#if resource.updateFrequencyMonth??>
                    <#assign updateFrequencyMonth=resource.updateFrequencyMonth.identifier>
                </#if>
                <#-- Auto-publishing biMonth-->
                <#assign updateFrequencyBiMonth="">
                <#if resource.updateFrequencyBiMonth??>
                    <#assign updateFrequencyBiMonth=resource.updateFrequencyBiMonth.identifier>
                </#if>
                <#-- Auto-publishing dayOfWeek-->
                <#assign updateFrequencyDayOfWeek="">
                <#if resource.updateFrequencyDayOfWeek??>
                    <#assign updateFrequencyDayOfWeek=resource.updateFrequencyDayOfWeek.identifier>
                </#if>
                <#-- Auto-publishing hour-->
                <#assign updateFrequencyHour="">
                <#if resource.updateFrequencyHour??>
                    <#assign updateFrequencyHour=resource.updateFrequencyHour>
                </#if>
                <#-- Auto-publishing minute-->
                <#assign updateFrequencyMinute="">
                <#if resource.updateFrequencyMinute??>
                    <#assign updateFrequencyMinute=resource.updateFrequencyMinute>
                </#if>

                <div class="row justify-content-center">
                    <div class="form-group col-md-6 col-lg-4">
                        <label for="updateFrequency" class="form-label">
                            <@s.text name="manage.autopublish.frequency"/>
                        </label>
                        <select id="updateFrequency" class="form-select" name="updateFrequency" size="1">
                            <#list frequencies?keys as val>
                                <option value="${val}" <#if (updateFrequency!"")==val> selected="selected"</#if>>
                                    <@s.text name="${frequencies.get(val)}"/>
                                </option>
                            </#list>
                        </select>
                    </div>
                </div>

                <p id="introAnnually" class="text-center">
                    <br/>
                    <@s.text name="manage.autopublish.intro.annually"/>
                </p>
                <p id="introDaily" class="text-center">
                    <br/>
                    <@s.text name="manage.autopublish.intro.daily"/>
                </p>
                <p id="introBiAnnually" class="text-center">
                    <br/>
                    <@s.text name="manage.autopublish.intro.biannually"/>
                </p>
                <p id="introMonthly" class="text-center">
                    <br/>
                    <@s.text name="manage.autopublish.intro.monthly"/>
                </p>
                <p id="introWeekly" class="text-center">
                    <br/>
                    <@s.text name="manage.autopublish.intro.weekly"/>
                </p>
                <p id="introOff" class="text-center">
                    <br/>
                    <@s.text name="manage.autopublish.intro.off"/>
                </p>

                <div id="frequencyDetails" class="mt-2">
                    <div class="row g-2 justify-content-center">
                        <div id="updateFrequencyDayOfWeekWrapper" class="col col-sm-3 col-lg-2">
                            <select id="updateFrequencyDayOfWeek" class="form-select" name="updateFrequencyDayOfWeek" size="1">
                                <#list daysOfWeek?keys as val>
                                    <option value="${val}" <#if (updateFrequencyDayOfWeek!"")==val> selected="selected"</#if>>
                                        <@s.text name="${daysOfWeek.get(val)}"/>
                                    </option>
                                </#list>
                            </select>
                        </div>

                        <div id="updateFrequencyDayWrapper" class="col col-sm-3 col-md-2 col-lg-2 col-xl-1">
                            <#-- Day: 1, 2, 3, ...-->
                            <select id="updateFrequencyDay" class="form-select" name="updateFrequencyDay" size="1">
                                <#list days?keys as val>
                                    <option value="${val}" <#if (updateFrequencyDay!"")?string==val?string> selected="selected"</#if>>
                                        <@s.text name="${days.get(val)}"/>
                                    </option>
                                </#list>
                            </select>
                        </div>

                        <div id="updateFrequencyMonthWrapper" class="col-6 col-sm-6 col-md-4 col-lg-2">
                            <#-- Day: January, February, ... -->
                            <select id="updateFrequencyMonth" class="form-select" name="updateFrequencyMonth" size="1">
                                <#list months?keys as val>
                                    <option value="${val}" <#if (updateFrequencyMonth!"")==val> selected="selected"</#if>>
                                        <@s.text name="${months.get(val)}"/>
                                    </option>
                                </#list>
                            </select>
                        </div>

                        <div id="updateFrequencyBiMonthWrapper" class="col-6 col-sm-6 col-md-4 col-lg-2">
                            <#-- BiMonth: January/July, February/August, ... -->
                            <select id="updateFrequencyBiMonth" class="form-select" name="updateFrequencyBiMonth" size="1">
                                <#list biMonths?keys as val>
                                    <option value="${val}" <#if (updateFrequencyBiMonth!"")==val> selected="selected"</#if>>
                                        <@s.text name="${biMonths.get(val)}"/>
                                    </option>
                                </#list>
                            </select>
                        </div>

                        <div id="updateFrequencyTimeWrapper" class="col col-sm-3 col-md-2 col-lg-2 col-xl-1">
                            <input type="time" id="updateFrequencyTime" name="updateFrequencyTime" class="form-control" value="${updateFrequencyTime!"12:00"}">
                        </div>
                    </div>
                </div>
            </form>

        </div>
    </main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
