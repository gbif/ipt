<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.PublicationSettingsAction" -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.publicationSettings.title'/>: ${resource.title!resource.shortname}</title>

<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2-ipt.css">
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>

<#-- Auto-publishing states:
     configurable  = full form
     turnOffOnly   = admin-only mode as a non-admin, or auto-publishing off for the whole IPT, and this resource has a
                     schedule: it can only be turned off (so it doesn't come back if auto-publishing is turned on again)
     otherwise     = a note only -->
<#assign iptAutoOn = cfg.autoPublishingEnabled/>
<#assign configurable = action.autoPublishingConfigurable/>
<#assign hasSchedule = resource.usesAutoPublishing() && resource.updateFrequency??/>
<#assign turnOffOnly = !configurable && hasSchedule/>
<#assign showForm = configurable || turnOffOnly/>
<#assign suspended = hasSchedule && action.hasMaxProcessFailures()/>
<#assign registeredOrDoi = action.registeredOrHasDoi/>
<#assign selectedOrg = (id?has_content)?then(id, (resource.organisation.key)!'')/>
<#assign savedOrg = (resource.organisation.key)!''/>

<form id="publication-form" action="publication-settings.do" method="post" novalidate>
    <input type="hidden" name="r" value="${resource.shortname}" />

    <div class="container px-0 ov-alerts">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container-fluid border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3 ov-head ov-head-left">
                <div class="ov-grow">
                    <div class="fs-smaller">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb mb-0">
                                <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                                <li class="breadcrumb-item ov-crumb-resource"><a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a></li>
                                <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.publicationSettings"/></li>
                            </ol>
                        </nav>
                    </div>

                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal">
                        <@s.text name="manage.publicationSettings.title"/>
                    </h1>
                </div>

                <div class="ov-acts">
                    <button type="submit" id="save" class="btn btn-sm btn-gbif-solid top-button" name="save" value="save"><@icon name="save"/> <@s.text name="button.save"/></button>
                    <button type="submit" id="cancel" class="btn btn-sm btn-outline-secondary top-button" name="cancel" value="cancel" formnovalidate><@s.text name="button.back"/></button>
                </div>
            </div>
        </div>
    </div>

    <div class="container-fluid bg-body">
        <main class="container main-content-container mb-md-4 pt-4">

            <#-- publishing organization -->
            <section class="ps-section" id="organisation">
                <div class="ps-sec-head"><h2><@s.text name="eml.publishingOrganisation"/></h2></div>
                <p class="ps-lead"><@s.text name="manage.publicationSettings.organisation.lead"/></p>

                <div class="row">
                    <div class="col-lg-6">
                        <label for="id" class="form-label ps-label"><@s.text name="manage.publicationSettings.organisation.label"/></label>
                        <select name="id" id="id" class="form-select" required>
                            <#list organisations?keys as val>
                                <option value="${val}"<#if selectedOrg == val> selected</#if>>${organisations.get(val)}</option>
                            </#list>
                        </select>
                        <@s.fielderror cssClass="invalid-feedback list-unstyled field-error my-1 d-block" fieldName="id"/>
                    </div>
                </div>

                <#if registeredOrDoi>
                    <div class="ov-banner info compact mt-3 ps-note" role="status">
                        <@icon name="info" class="ov-ic"/>
                        <div class="ov-grow"><@s.text name="manage.publicationSettings.organisation.registered"><@s.param><a href="mailto:helpdesk@gbif.org">helpdesk@gbif.org</a></@s.param></@s.text></div>
                    </div>
                    <div class="ov-banner warn compact mt-2 ps-note" id="ps-org-changed" role="status" hidden>
                        <@icon name="warning" class="ov-ic"/>
                        <div class="ov-grow"><@s.text name="manage.publicationSettings.organisation.changed"><@s.param><a href="mailto:helpdesk@gbif.org">helpdesk@gbif.org</a></@s.param></@s.text></div>
                    </div>
                </#if>
            </section>

            <#-- auto-publishing -->
            <section class="ps-section" id="autopublishing">
                <div class="ps-sec-head">
                    <h2><@s.text name="manage.overview.autopublish.title"/></h2>
                    <span class="ps-pill" id="ps-pill"><#if !showForm><@s.text name="manage.publicationSettings.auto.off"/></#if></span>
                </div>

                <#if suspended>
                    <div class="ov-banner bad compact ps-note mb-3" role="status">
                        <@icon name="error" class="ov-ic text-gbif-danger"/>
                        <div class="ov-grow"><@s.text name="manage.publicationSettings.auto.suspended"/></div>
                    </div>
                </#if>

                <#if !iptAutoOn || !configurable>
                    <div class="ov-banner info compact ps-note mb-3" role="status">
                        <@icon name="lock" class="ov-ic"/>
                        <div class="ov-grow">
                            <#if !iptAutoOn && hasSchedule><@s.text name="manage.overview.autopublish.disabledByAdmin.disableOnly"/>
                            <#elseif !iptAutoOn><@s.text name="manage.overview.autopublish.disabledByAdmin"/>
                            <#elseif hasSchedule><@s.text name="manage.overview.autopublish.adminOnly.disableOnly"/>
                            <#else><@s.text name="manage.overview.autopublish.adminOnly"/></#if>
                        </div>
                    </div>
                </#if>

                <#if configurable && resource.isDeprecatedAutoPublishingConfiguration()>
                    <div class="ov-banner warn compact ps-note mb-3" role="status">
                        <@icon name="warning" class="ov-ic"/>
                        <div class="ov-grow"><@s.text name="manage.publicationSettings.auto.deprecated"/></div>
                    </div>
                </#if>

                <#if showForm>
                    <#-- not with auto-publishing off for the whole IPT: the schedule doesn't run, the note above says so -->
                    <#if iptAutoOn><p class="ps-lead" id="ps-lead-on"><@s.text name="manage.publicationSettings.auto.lead.on"/></p></#if>
                    <p class="ps-lead" id="ps-lead-off"><@s.text name="manage.publicationSettings.auto.lead.off"/></p>

                    <div class="ps-label" id="ps-freq-label"><@s.text name="manage.publicationSettings.auto.frequency"/></div>
                    <div class="ps-freq" role="radiogroup" aria-labelledby="ps-freq-label">
                        <label><input type="radio" name="updateFrequency" value="off"<#if frequency == "off"> checked</#if>> <@s.text name="manage.publicationSettings.auto.off"/></label>
                        <#list frequencies?keys as f>
                            <#-- turn-off only: the saved frequency stays selectable (posts no change), the others can't be picked -->
                            <label><input type="radio" name="updateFrequency" value="${f}" data-label="${frequencies.get(f)}"<#if frequency == f> checked</#if><#if !configurable && frequency != f> disabled</#if>> ${frequencies.get(f)}</label>
                        </#list>
                    </div>

                    <div id="ps-schedule"<#if !configurable> class="ps-readonly"</#if>>
                        <#assign tz><span class="ps-tz"><@s.text name="manage.publicationSettings.schedule.timezone"><@s.param>${serverTimeZone}</@s.param></@s.text></span></#assign>
                        <#macro timeInput><input type="time" class="form-control form-control-sm ps-ctl" name="updateFrequencyTime" value="${frequencyTime}" aria-label="<@s.text name='manage.publicationSettings.schedule.time'/>"<#if !configurable> disabled</#if>></#macro>
                        <#macro daySelect><select class="form-select form-select-sm ps-ctl" name="updateFrequencyDay" aria-label="<@s.text name='manage.publicationSettings.schedule.day'/>"<#if !configurable> disabled</#if>><#list 1..31 as d><option value="${d}"<#if frequencyDay == d> selected</#if>>${d}</option></#list></select></#macro>

                        <div class="ps-sentence" data-freq="daily">
                            <@s.text name="manage.publicationSettings.schedule.daily"><@s.param><@timeInput/></@s.param></@s.text> ${tz}
                        </div>
                        <div class="ps-sentence" data-freq="weekly">
                            <@s.text name="manage.publicationSettings.schedule.weekly">
                                <@s.param><select class="form-select form-select-sm ps-ctl" name="updateFrequencyDayOfWeek" aria-label="<@s.text name='manage.publicationSettings.schedule.dayOfWeek'/>"<#if !configurable> disabled</#if>><#list daysOfWeek?keys as v><option value="${v}"<#if (frequencyDayOfWeek!"monday") == v> selected</#if>>${daysOfWeek.get(v)}</option></#list></select></@s.param>
                                <@s.param><@timeInput/></@s.param>
                            </@s.text> ${tz}
                        </div>
                        <div class="ps-sentence" data-freq="monthly">
                            <@s.text name="manage.publicationSettings.schedule.monthly">
                                <@s.param><@daySelect/></@s.param>
                                <@s.param><@timeInput/></@s.param>
                            </@s.text> ${tz}
                        </div>
                        <div class="ps-sentence" data-freq="biannually">
                            <@s.text name="manage.publicationSettings.schedule.biannually">
                                <@s.param><@daySelect/></@s.param>
                                <@s.param><select class="form-select form-select-sm ps-ctl" name="updateFrequencyBiMonth" aria-label="<@s.text name='manage.publicationSettings.schedule.months'/>"<#if !configurable> disabled</#if>><#list biMonths?keys as v><option value="${v}"<#if (frequencyBiMonth!"january_july") == v> selected</#if>>${biMonths.get(v)}</option></#list></select></@s.param>
                                <@s.param><@timeInput/></@s.param>
                            </@s.text> ${tz}
                        </div>
                        <div class="ps-sentence" data-freq="annually">
                            <@s.text name="manage.publicationSettings.schedule.annually">
                                <@s.param><@daySelect/></@s.param>
                                <@s.param><select class="form-select form-select-sm ps-ctl" name="updateFrequencyMonth" aria-label="<@s.text name='manage.publicationSettings.schedule.month'/>"<#if !configurable> disabled</#if>><#list months?keys as v><option value="${v}"<#if (frequencyMonth!"january") == v> selected</#if>>${months.get(v)}</option></#list></select></@s.param>
                                <@s.param><@timeInput/></@s.param>
                            </@s.text> ${tz}
                        </div>

                        <#-- the saved date first; the page works it out again as the schedule is edited -->
                        <p class="ps-next" id="ps-next"<#if !iptAutoOn || suspended> hidden</#if>>
                            <@s.text name="manage.publicationSettings.next"><@s.param><b id="ps-next-date"><#if !(action.hasFieldErrors() || action.hasActionErrors())>${(resource.nextPublished?datetime?string.medium_short)!}</#if></b></@s.param></@s.text>
                        </p>

                        <div class="ps-label mb-2"><@s.text name="manage.publicationSettings.auto.options"/></div>
                        <div class="ps-opts">
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" name="skipUnchanged" id="skipUnchanged" value="true"<#if skipUnchanged> checked</#if><#if !configurable> disabled</#if>>
                                <label class="form-check-label" for="skipUnchanged"><@s.text name="manage.publicationSettings.auto.skipUnchanged"/>
                                    <span class="ps-sub"><@s.text name="manage.publicationSettings.auto.skipUnchanged.help"/></span></label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" name="skipDrop" id="skipDrop" value="true"<#if skipDrop> checked</#if><#if !configurable> disabled</#if>>
                                <label class="form-check-label" for="skipDrop"><@s.text name="manage.publicationSettings.auto.skipDrop"><@s.param><input type="number" class="form-control form-control-sm ps-ctl ps-num<#if fieldErrors.recordsDropThreshold??> is-invalid</#if>" name="recordsDropThreshold" id="recordsDropThreshold" min="0" max="100" step="1" value="${recordsDropThresholdInput!recordsDropThreshold?c}" aria-label="<@s.text name='manage.publicationSettings.auto.dropThreshold'/>"<#if !configurable> disabled</#if>></@s.param></@s.text></label>
                                <@s.fielderror cssClass="invalid-feedback list-unstyled field-error my-1 d-block" fieldName="recordsDropThreshold"/>
                                <#if (resource.recordsPublished > 0)>
                                    <span class="ps-sub" id="ps-drop-help" data-records="${resource.recordsPublished?c}"><@s.text name="manage.publicationSettings.auto.skipDrop.help"><@s.param>${resource.recordsPublished?string.number}</@s.param><@s.param><span id="ps-drop-min"></span></@s.param></@s.text></span>
                                </#if>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" name="notifyPublicationFailure" id="notifyPublicationFailure" value="true"<#if notifyFailure> checked</#if><#if !configurable> disabled</#if>>
                                <label class="form-check-label" for="notifyPublicationFailure"><@s.text name="manage.publicationSettings.auto.notify"/></label>
                                <div class="ps-notify" id="ps-notify">
                                    <label for="failureEmails" class="form-label ps-label mt-2"><@s.text name="manage.publicationSettings.auto.emails"/></label>
                                    <input type="text" class="form-control form-control-sm<#if fieldErrors.failureEmails??> is-invalid</#if>" name="failureEmails" id="failureEmails" value="${failureEmails}" placeholder="data@example.org, curator@example.org"<#if !configurable> disabled</#if>>
                                    <@s.fielderror cssClass="invalid-feedback list-unstyled field-error my-1 d-block" fieldName="failureEmails"/>
                                    <span class="ps-sub"><@s.text name="manage.publicationSettings.auto.emails.help"/></span>
                                    <#if !publicationFailureEmailConfigured>
                                        <div class="ov-banner warn compact mt-2" role="status">
                                            <@icon name="warning" class="ov-ic"/>
                                            <div class="ov-grow"><@s.text name="manage.autopublish.options.notifyFailure.notConfigured"/></div>
                                        </div>
                                    </#if>
                                </div>
                            </div>
                        </div>
                    </div>
                </#if>
            </section>
        </main>
    </div>
</form>

<style>
    .ps-section { margin-bottom: 32px; }
    .ps-sec-head { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; border-bottom: 1px solid #dee2e6; padding-bottom: 6px; margin-bottom: 14px; }
    .ps-sec-head h2 { font-size: 1.1rem; font-weight: 500; color: #4e565f; margin: 0; }
    .ps-lead { color: #6c757d; font-size: 0.875rem; margin: 0 0 14px; max-width: 68ch; }
    .ps-label { font-size: 0.875rem; font-weight: 600; margin-bottom: 6px; }
    .ps-note { max-width: 68ch; }
    .ps-pill { font-size: 0.72rem; font-weight: 500; border-radius: 10px; padding: 1px 8px; line-height: 18px; color: #495057; background: #f1f3f5; }
    .ps-pill.on { color: #3f7a2b; background: rgba(var(--color-gbif-primary), .12); }
    .ps-pill.bad { color: rgb(var(--color-gbif-danger)); background: rgba(var(--color-gbif-danger), .08); }
    .ps-pill.warn { color: #7a5700; background: rgba(var(--alert-warning-icon-color), .12); }
    .ps-freq { display: flex; flex-wrap: wrap; gap: 6px; margin: 0 0 16px; }
    .ps-freq label { border: 1px solid #ced4da; border-radius: 4px; padding: 4px 12px; cursor: pointer; font-size: 0.875rem; color: #495057; display: flex; align-items: center; gap: 6px; margin: 0; }
    .ps-freq input { accent-color: rgb(var(--color-gbif-primary)); margin: 0; }
    .ps-freq label:has(input:checked) { border-color: rgb(var(--color-gbif-primary)); background: rgba(var(--color-gbif-primary), .07); color: #212529; }
    .ps-freq label:has(input:disabled) { opacity: .5; cursor: not-allowed; }
    .ps-sentence { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; font-size: 0.9375rem; margin-bottom: 8px; }
    .ps-sentence[hidden] { display: none; }
    .ps-ctl { width: auto; display: inline-block; }
    .ps-num { width: 4.5rem; margin: 0 4px; }
    .ps-tz { font-size: 0.8rem; color: #6c757d; }
    .ps-next { font-size: 0.875rem; color: #6c757d; margin: 0 0 20px; }
    .ps-next b { color: #212529; font-weight: 600; font-variant-numeric: tabular-nums; }
    .ps-opts { display: flex; flex-direction: column; gap: 12px; max-width: 68ch; font-size: 0.875rem; }
    .ps-opts .form-check-label { display: inline; }
    .ps-opts .form-check-input { accent-color: rgb(var(--color-gbif-primary)); }
    .ps-sub { display: block; font-size: 0.8rem; color: #6c757d; }
    .ps-notify { max-width: 460px; }
    .ps-notify[hidden] { display: none; }
    .ps-readonly .ps-sentence, .ps-readonly .ps-opts { opacity: .6; }
</style>

<script>
    $(function () {
        $('select#id').select2({
            placeholder: '${action.getText("admin.organisation.name.select")?js_string?no_esc}',
            language: { noResults: function () { return '${selectNoResultsFound}'; } },
            width: "100%",
            minimumResultsForSearch: 15,
            theme: 'bootstrap4',
            allowClear: true
        });

        <#if registeredOrDoi>
        // registered or with a DOI: changing the organization here isn't enough, GBIF has to make the change too
        $('#id').on('change', function () {
            $('#ps-org-changed').prop('hidden', $(this).val() === '${savedOrg?js_string?no_esc}');
        });
        </#if>

        <#if showForm>
        var configurable = ${configurable?c};
        var iptAutoOn = ${iptAutoOn?c};
        var suspended = ${suspended?c};
        var savedFreq = '${frequency?js_string?no_esc}';
        var pillText = {
            off: '${action.getText("manage.publicationSettings.auto.off")?js_string?no_esc}',
            suspended: '${action.getText("manage.overview.autopublish.suspended")?js_string?no_esc}',
            notRunning: '${action.getText("manage.publicationSettings.auto.pill.notRunning")?js_string?no_esc}'
        };

        function currentFreq() { return $('input[name=updateFrequency]:checked').val() || 'off'; }

        function render() {
            var f = currentFreq(), on = f !== 'off';
            $('#ps-lead-on').prop('hidden', !on);
            $('#ps-lead-off').prop('hidden', on);
            $('#ps-schedule').prop('hidden', !on);
            // only the chosen frequency's fields are posted
            $('.ps-sentence').each(function () {
                var mine = $(this).data('freq') === f;
                $(this).prop('hidden', !mine);
                if (configurable) $(this).find('select, input').prop('disabled', !mine);
            });
            var $pill = $('#ps-pill').removeClass('on bad warn');
            if (!on) $pill.text(pillText.off);
            else if (!iptAutoOn) $pill.addClass('warn').text(pillText.notRunning);
            else if (suspended && f === savedFreq) $pill.addClass('bad').text(pillText.suspended);
            else $pill.addClass('on').text($('input[name=updateFrequency]:checked').data('label'));
            $('#ps-notify').prop('hidden', !$('#notifyPublicationFailure').is(':checked'));
            renderDrop();
        }

        function renderDrop() {
            var $help = $('#ps-drop-help');
            if (!$help.length) return;
            var p = parseInt($('#recordsDropThreshold').val(), 10);
            var ok = $('#skipDrop').is(':checked') && p >= 0 && p <= 100;
            $help.prop('hidden', !ok);
            if (ok) {
                // the run is skipped when the drop is more than p %, so fewer than this many records
                $('#ps-drop-min').text(Math.ceil($help.data('records') * (1 - p / 100)).toLocaleString(locale));
            }
        }

        // The next publication, worked out the way the server does it (Calendar, lenient: day 31 in a 30-day month is
        // the 1st of the next one), in the server's wall-clock time. Dates are kept as UTC so the browser's own time
        // zone and summer time don't come into it.
        var locale = '${currentLocale?replace("_", "-")?js_string?no_esc}';
        var nowParts = '${serverNow?js_string?no_esc}'.split(/[-T:]/).map(Number);
        var now = Date.UTC(nowParts[0], nowParts[1] - 1, nowParts[2], nowParts[3], nowParts[4]);
        var MONTHS = ['january', 'february', 'march', 'april', 'may', 'june', 'july', 'august', 'september', 'october', 'november', 'december'];
        var BIMONTHS = ['january_july', 'february_august', 'march_september', 'april_october', 'may_november', 'june_december'];
        var DAYS = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'];

        // Calendar.add(MONTH/YEAR) keeps the day if it can, else the month's last day
        function addMonths(t, n) {
            var d = new Date(t), y = d.getUTCFullYear(), m = d.getUTCMonth() + n;
            var last = new Date(Date.UTC(y, m + 1, 0)).getUTCDate();
            return Date.UTC(y, m, Math.min(d.getUTCDate(), last), d.getUTCHours(), d.getUTCMinutes());
        }

        function nextPublication(f, $s) {
            var time = ($s.find('[name=updateFrequencyTime]').val() || '12:00').split(':').map(Number);
            var day = parseInt($s.find('[name=updateFrequencyDay]').val(), 10);
            var n = new Date(now), y = n.getUTCFullYear(), t;
            switch (f) {
                case 'daily':
                    t = Date.UTC(y, n.getUTCMonth(), n.getUTCDate(), time[0], time[1]);
                    if (t < now) t += 86400000;
                    return t;
                case 'weekly':
                    var dow = DAYS.indexOf($s.find('[name=updateFrequencyDayOfWeek]').val());
                    t = Date.UTC(y, n.getUTCMonth(), n.getUTCDate() + ((dow - n.getUTCDay() + 7) % 7), time[0], time[1]);
                    if (t < now) t += 7 * 86400000;
                    return t;
                case 'monthly':
                    t = Date.UTC(y, n.getUTCMonth(), day, time[0], time[1]);
                    return t < now ? addMonths(t, 1) : t;
                case 'biannually':
                    t = Date.UTC(y, BIMONTHS.indexOf($s.find('[name=updateFrequencyBiMonth]').val()), day, time[0], time[1]);
                    if (t < now) t = addMonths(t, 6);
                    if (t < now) t = addMonths(t, 6);
                    return t;
                case 'annually':
                    t = Date.UTC(y, MONTHS.indexOf($s.find('[name=updateFrequencyMonth]').val()), day, time[0], time[1]);
                    return t < now ? addMonths(t, 12) : t;
            }
            return null;
        }

        function renderNext() {
            var f = currentFreq();
            if (f === 'off') return;
            var t = nextPublication(f, $('.ps-sentence[data-freq=' + f + ']'));
            if (t == null) return;
            $('#ps-next-date').text(new Intl.DateTimeFormat(locale, {
                timeZone: 'UTC', day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
            }).format(new Date(t)));
            $('#ps-next').prop('hidden', !iptAutoOn || suspended);
        }

        $('input[name=updateFrequency]').on('change', function () { render(); if (configurable) renderNext(); });
        $('.ps-sentence').on('change input', 'select, input', renderNext);
        $('#notifyPublicationFailure').on('change', render);
        $('#skipDrop').on('change', renderDrop);
        $('#recordsDropThreshold').on('input', renderDrop);
        render();
        // nothing saved yet to show: work it out now
        if (configurable && !$('#ps-next-date').text().trim()) renderNext();
        </#if>
    });
</script>

<#include "/WEB-INF/pages/inc/footer.ftl">
