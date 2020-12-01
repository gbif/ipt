<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.autopublish.title'/></title>

    <#assign currentMenu = "manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <script>
        $(document).ready(function() {
            initHelp();

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
                $('#updateFrequencyMonth').hide();
                $('#updateFrequencyBiMonth').hide();
                $('#updateFrequencyDay').hide();
                $('#updateFrequencyDayOfWeek').hide();
                if (str == "annually") {
                    $('#introAnnually').show();
                    $('#helpAnnually').show();
                    $('#frequencyDetailsEvery').show();
                    $('#updateFrequencyMonth').show();
                    $('#updateFrequencyDay').show();
                    $('#frequencyDetailsAt').show();
                } else if (str == "biannually") {
                    $('#introBiAnnually').show();
                    $('#helpBiAnnually').show();
                    $('#frequencyDetailsEvery').show();
                    $('#updateFrequencyBiMonth').show();
                    $('#updateFrequencyDay').show();
                    $('#frequencyDetailsAt').show();
                } else if (str == "monthly") {
                    $('#introMonthly').show();
                    $('#helpMonthly').show();
                    $('#frequencyDetailsEvery').show();
                    $('#updateFrequencyDay').show();
                    $('#frequencyDetailsAt').show();
                } else if (str == "weekly") {
                    $('#introWeekly').show();
                    $('#helpWeekly').show();
                    $('#frequencyDetailsEvery').show();
                    $('#updateFrequencyDayOfWeek').show();
                    $('#frequencyDetailsAt').show();
                } else if (str == "daily") {
                    $('#introDaily').show();
                    $('#frequencyDetailsAt').show();
                } else {
                    $('#introOff').show();
                }

            }).change();

        });

    </script>

    <div class="grid_17 suffix_7">
        <form class="topForm" action="auto-publish.do" method="post">
            <h1><@s.text name='manage.autopublish.title'/></h1>
            <p><@s.text name='manage.autopublish.intro'/></p>
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

            <div>
                <label for="updateFrequency"><@s.text name="manage.autopublish.frequency"/></label>
                <select id="updateFrequency" name="updateFrequency" size="1">
                    <#list frequencies?keys as val>
                        <option value="${val}" <#if (updateFrequency!"")==val> selected="selected"</#if>>
                            <@s.text name="${frequencies.get(val)}"/>
                        </option>
                    </#list>
                </select>
            </div>

            <p id="introAnnually">
                <br/>
                <@s.text name="manage.autopublish.intro.annually"/>
            </p>
            <p id="introDaily">
                <br/>
                <@s.text name="manage.autopublish.intro.daily"/>
            </p>
            <p id="introBiAnnually">
                <br/>
                <@s.text name="manage.autopublish.intro.biannually"/>
            </p>
            <p id="introMonthly">
                <br/>
                <@s.text name="manage.autopublish.intro.monthly"/>
            </p>
            <p id="introWeekly">
                <br/>
                <@s.text name="manage.autopublish.intro.weekly"/>
            </p>
            <p id="introOff">
                <br/>
                <@s.text name="manage.autopublish.intro.off"/>
            </p>

            <div id="frequencyDetails" class="autop">
                <div id="frequencyDetailsEvery">
                    <div>
                        <label>Every</label>
                    </div>
                    <div>
                        <select id="updateFrequencyDay" name="updateFrequencyDay" size="1">
                            <#list days?keys as val>
                                <option value="${val}" <#if (updateFrequencyDay!"")?string==val?string> selected="selected"</#if>>
                                    <@s.text name="${days.get(val)}"/>
                                </option>
                            </#list>
                        </select>
                        <select id="updateFrequencyMonth" name="updateFrequencyMonth" size="1">
                            <#list months?keys as val>
                                <option value="${val}" <#if (updateFrequencyMonth!"")==val> selected="selected"</#if>>
                                    <@s.text name="${months.get(val)}"/>
                                </option>
                            </#list>
                        </select>
                        <select id="updateFrequencyBiMonth" name="updateFrequencyBiMonth" size="1">
                            <#list biMonths?keys as val>
                                <option value="${val}" <#if (updateFrequencyBiMonth!"")==val> selected="selected"</#if>>
                                    <@s.text name="${biMonths.get(val)}"/>
                                </option>
                            </#list>
                        </select>
                        <select id="updateFrequencyDayOfWeek" name="updateFrequencyDayOfWeek" size="1">
                            <#list daysOfWeek?keys as val>
                                <option value="${val}" <#if (updateFrequencyDayOfWeek!"")==val> selected="selected"</#if>>
                                    <@s.text name="${daysOfWeek.get(val)}"/>
                                </option>
                            </#list>
                        </select>
                        <img class="infoImg" src="${baseURL}/images/info.gif" />
                        <div class="info">
                            <span id="helpAnnually"><@s.text name="manage.autopublish.help.annually"/></span>
                            <span id="helpBiAnnually"><@s.text name="manage.autopublish.help.biannually"/></span>
                            <span id="helpMonthly"><@s.text name="manage.autopublish.help.monthly"/></span>
                            <span id="helpWeekly"><@s.text name="manage.autopublish.help.weekly"/></span>
                        </div>
                    </div>
                </div>
                <div id="frequencyDetailsAt">
                    <div>
                        <label>At</label>
                    </div>
                    <div>
                        <select id="updateFrequencyHour" name="updateFrequencyHour" size="1">
                            <#list hours?keys as val>
                                <option value="${val}" <#if (updateFrequencyHour!"")?string==val?string> selected="selected"</#if>>
                                    <@s.text name="${hours.get(val)}"/>
                                </option>
                            </#list>
                        </select>
                        h
                        <select id="updateFrequencyMinute" name="updateFrequencyMinute" size="1">
                            <#list minutes?keys as val>
                                <option value="${val}" <#if (updateFrequencyMinute!"")?string==val?string> selected="selected"</#if>>
                                    <@s.text name="${minutes.get(val)}"/>
                                </option>
                            </#list>
                        </select>
                        <img class="infoImg" src="${baseURL}/images/info.gif" />
                        <div class="info">
                            <@s.text name="manage.autopublish.help.hour"/>
                        </div>
                    </div>
                </div>
            </div>

            <br/>
            <br/>

            <div class="buttons">
                <@s.submit cssClass="button" name="save" value="save" key="button.save"/>
                <@s.submit cssClass="button" name="cancel" value="cancel" key="button.cancel"/>
            </div>

        </form>
    </div>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
