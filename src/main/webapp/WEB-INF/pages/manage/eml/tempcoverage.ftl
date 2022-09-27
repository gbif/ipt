<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#setting number_format="#####.##">

    <script src="${baseURL}/js/datepicker/bootstrap-datepicker.min.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/datepicker/bootstrap-datepicker.min.css" />

    <script xmlns="http://www.w3.org/1999/html">
        var DATE_RANGE = "DATE_RANGE";
        var FORMATION_PERIOD = "FORMATION_PERIOD";
        var LIVING_TIME_PERIOD = "LIVING_TIME_PERIOD";
        var SINGLE_DATE = "SINGLE_DATE";
        var count;
        // a function called when adding new temporal coverages
        // an element is cloned and the IDs reset etc etc
        $(document).ready(function () {
            initCalendar();
            calculateCount();

            function calculateCount() {
                var lastChild = $("#temporals .tempo:last-child").attr("id");
                if (lastChild !== undefined) {
                    count = parseInt(lastChild.split("-")[1]) + 1;
                } else {
                    count = new Number(0);
                }
            }

            $("#plus").click(function (event) {
                event.preventDefault();
                var idNewForm = "temporal-" + count;
                var newForm = $("#base-temporal-99999").clone().attr("id", idNewForm).css('display', '');
                // Add the fields depending of the actual value in the select
                var typeSubForm = $("#tempTypes").prop("value");
                //Adding the 'sub-form' to the new form.
                addTypeForm(newForm, typeSubForm, true);
                $("#temporals").append(newForm);
                newForm.hide();
                //Updating the components of the new 'sub-form'.
                updateFields(idNewForm, count);
                $("#temporal-" + count).slideDown("slow").css('zoom', 1);

                count++;
            });

            if ($("#inferTemporalCoverageAutomatically").is(':checked')) {
                $("[id^=temporal-]").remove();
                $('.intro').hide();
                $('#preview-inferred-temporal').hide();
                $('.addNew').hide();
                $('#static-temporal').show();
                $("#dateInferred").show();
            }

            $("#inferTemporalCoverageAutomatically").click(function() {
                if ($("#inferTemporalCoverageAutomatically").is(':checked')) {
                    $("[id^=temporal-]").remove();
                    $('.intro').hide();
                    $('#preview-inferred-temporal').hide();
                    $('.addNew').hide();
                    $('#static-temporal').show();
                    $("#dateInferred").show();
                } else {
                    $('.intro').show();
                    $('#preview-inferred-temporal').show();
                    $('.addNew').show();
                    $('#static-temporal').hide();
                    $("#dateInferred").hide();
                }
            });

            $("#preview-inferred-temporal").click(function(event) {
                event.preventDefault();

                <#if (inferredMetadata.inferredTemporalCoverage)?? && inferredMetadata.inferredTemporalCoverage.errors?size gt 0>
                $(".metadata-error-alert").show();
                </#if>

                <#if (inferredMetadata.inferredTemporalCoverage.data)??>
                count = 0;
                // remove all current items
                $("[id^=temporal-]").remove();

                var idNewForm = "temporal-" + count;
                var newForm = $("#base-temporal-99999").clone().attr("id", idNewForm).css('display', '');

                //Adding the 'sub-form' to the new form.
                addTypeForm(newForm, DATE_RANGE, true);
                $("#temporals").append(newForm);
                newForm.hide();
                //Updating the components of the new 'sub-form'.
                updateFields(idNewForm, count, DATE_RANGE);
                $("#tempTypes-" + count).val(DATE_RANGE);
                $("#temporal-" + count).slideDown("slow").css('zoom', 1);
                $('#eml\\.temporalCoverages\\[' + count + '\\]\\.startDate').val("${inferredMetadata.inferredTemporalCoverage.data.startDate?string('yyyy-MM-dd')}")
                $('#eml\\.temporalCoverages\\[' + count + '\\]\\.endDate').val("${inferredMetadata.inferredTemporalCoverage.data.endDate?string('yyyy-MM-dd')}")
                count++;
                </#if>
            });

            /**
             * This method add a new subform to the form that is in the parameter.
             */
            function addTypeForm(theForm, typeSubForm, changeDisplay) {
                var newSubForm;
                if (typeSubForm === DATE_RANGE) {
                    newSubForm = $("#date-99999").clone();
                }
                if (typeSubForm === FORMATION_PERIOD) {
                    newSubForm = $("#formation-99999").clone();
                }
                if (typeSubForm === LIVING_TIME_PERIOD) {
                    newSubForm = $("#living-99999").clone();
                }
                if (typeSubForm === SINGLE_DATE) {
                    newSubForm = $("#single-99999").clone();
                }
                if (changeDisplay) {
                    newSubForm.css("display", "");
                }
                theForm.append(newSubForm);
            }

            /**
             * this method update the name and the id of the form to the consecutive number in the parameter.
             */
            function updateFields(idNewForm, index, typeSubFormDefault) {
                $("#" + idNewForm + " .removeLink").attr("id", "removeLink-" + index);
                // Remove Link (registering the event for the new links).
                $("#" + idNewForm + " .removeLink").click(
                    function (event) {
                        event.preventDefault();
                        removeTemporal(event);
                    }
                );
                // Select ==> tempTypes
                $("#" + idNewForm + " [id^='tempTypes']").attr("id", "tempTypes-" + index).attr("name", function () {
                    return $(this).attr("id");
                });
                // Update the fields depending of the actual value in the select
                var typeSubForm = typeSubFormDefault ? typeSubFormDefault : $("#" + idNewForm + " #tempTypes-" + index).prop("value");
                // Registering the event for the new selects.
                $("#" + idNewForm + " #tempTypes-" + count).change(
                    function () {
                        changeForm($(this));
                    }
                );

                if (typeSubForm === DATE_RANGE) {
                    $("#" + idNewForm + " [id^='date-']").attr("id", "date-" + index);
                    $("#" + idNewForm + " [id$='startDate']").attr("id", "eml.temporalCoverages[" + index + "].startDate").attr("name", function () {
                        return $(this).attr("id");
                    });
                    $("#" + idNewForm + " [id$='endDate']").attr("id", "eml.temporalCoverages[" + index + "].endDate").attr("name", function () {
                        return $(this).attr("id");
                    });

                    // replace generic 'inputName-startDate' and 'inputName-endDate' with a proper value at 'data-bs-content' attribute to be able to bind help options
                    var popovers = $("#" + idNewForm + " a.popover-link");
                    popovers[0].setAttribute("data-bs-content", popovers[0].getAttribute('data-bs-content').replace('inputName-startDate', "inputName-eml.temporalCoverages[" + index + "].startDate"))
                    popovers[1].setAttribute("data-bs-content", popovers[1].getAttribute('data-bs-content').replace('inputName-endDate', "inputName-eml.temporalCoverages[" + index + "].endDate"))

                    initCalendar("#date-" + index);
                }
                if (typeSubForm === FORMATION_PERIOD) {
                    $("#" + idNewForm + " [id^='formation-']").attr("id", "formation-" + index);
                    $("#" + idNewForm + " [id$='formationPeriod']").attr("id", "eml.temporalCoverages[" + index + "].formationPeriod").attr("name", function () {
                        return $(this).attr("id");
                    });
                    initCalendar("#formation-" + index);
                }
                if (typeSubForm === LIVING_TIME_PERIOD) {
                    $("#" + idNewForm + " [id^='living-']").attr("id", "living-" + index);
                    $("#" + idNewForm + " [id$='livingTimePeriod']").attr("id", "eml.temporalCoverages[" + index + "].livingTimePeriod").attr("name", function () {
                        return $(this).attr("id");
                    });
                    initCalendar("#living-" + index);
                }
                if (typeSubForm === SINGLE_DATE) {
                    $("#" + idNewForm + " [id^='single-']").attr("id", "single-" + index);
                    $("#" + idNewForm + " [id$='startDate']").attr("id", "eml.temporalCoverages[" + index + "].startDate").attr("name", function () {
                        return $(this).attr("id");
                    });

                    // replace generic 'inputName-startDate' with a proper value at 'data-bs-content' attribute to be able to bind help options
                    var popovers = $("#" + idNewForm + " a.popover-link");
                    popovers[0].setAttribute("data-bs-content", popovers[0].getAttribute('data-bs-content').replace('inputName-startDate', "inputName-eml.temporalCoverages[" + index + "].startDate"))

                    initCalendar("#single-" + index);
                }

                initInfoPopovers($("#" + idNewForm)[0]);
            }

            // This event should work for the temporal coverage that already exist in the file.
            $("[id^='tempTypes-']").change(function (event) {
                changeForm($(this));
            });

            function changeForm(select) {
                var selection = select.prop("value");
                var index = select.attr("id").split("-")[1];
                $("#temporal-" + index + " .typeForm").fadeOut(function () {
                    $(this).remove();
                    addTypeForm($("#temporal-" + index), selection, false);
                    $("#temporal-" + index + " .typeForm").fadeIn(function () {
                        updateFields("temporal-" + index, index);
                    });
                });
            }

            // This event should work for the temporal coverage that already exist in the file.
            $(".removeLink").click(function (event) {
                event.preventDefault();
                removeTemporal(event);
            });

            function removeTemporal(event) {
                var $target = $(event.target);
                if (!$target.is('a')) {
                    $target = $(event.target).closest('a');
                }
                var index = $target.attr("id").split("-")[1];
                // removing the form in the html.
                $('#temporal-' + index).slideUp("slow", function () {
                    $(this).remove();
                    $("#temporals .tempo").each(function (index) {
                        updateFields($(this).attr("id"), index);
                        $(this).attr("id", "temporal-" + index);
                    });
                    calculateCount();
                });
            }

            $(document.body).on('click', '.helpOptionLink', function (e) {
                e.preventDefault();
                // get all link classes
                var classes = $(this).attr('class').split(/\s+/);
                var inputName, inputValue

                for (var i = 0; i < classes.length; i++) {
                    // get input name in order to set value
                    if (classes[i].startsWith('inputName')) {
                        // get rid of prefix; escape dots and brackets
                        inputName = classes[i].replace('inputName-', '').replaceAll('.', '\\.').replaceAll('[', '\\[').replaceAll(']', '\\]');
                    }

                    // get value to be set
                    if (classes[i].startsWith('inputValue')) {
                        // get rid of prefix
                        inputValue = classes[i].replace('inputValue-', '');
                    }
                }

                $('#' + inputName).val(inputValue)
            });

            $('#metadata-section').change(function () {
                var metadataSection = $('#metadata-section').find(':selected').val()
                $(location).attr('href', 'metadata-' + metadataSection + '.do?r=${resource.shortname!r!}');
            });
        });
    </script>

    <title><@s.text name='manage.metadata.tempcoverage.title'/></title>
    <#assign currentMetadataPage = "tempcoverage"/>
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <form class="needs-validation" action="metadata-${section}.do" method="post" novalidate>

        <div class="container-fluid bg-body border-bottom">
            <div class="container pt-2">
                <#include "/WEB-INF/pages/inc/action_alerts.ftl">

                <div id="tempcoverage-no-available-data-warning" class="alert alert-warning mt-2 alert-dismissible fade show d-flex" style="display: none !important;" role="alert">
                    <div class="me-3">
                        <i class="bi bi-exclamation-triangle alert-orange-2 fs-bigger-2 me-2"></i>
                    </div>
                    <div class="overflow-x-hidden pt-1">
                        <span><@s.text name="eml.warning.reinfer"/></span>
                    </div>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>

                <#if (inferredMetadata.inferredTemporalCoverage)??>
                    <#list inferredMetadata.inferredTemporalCoverage.errors as error>
                        <div class="alert alert-danger mt-2 alert-dismissible fade show d-flex metadata-error-alert" role="alert" style="display: none !important;">
                            <div class="me-3">
                                <i class="bi bi-exclamation-circle alert-red-2 fs-bigger-2 me-2"></i>
                            </div>
                            <div class="overflow-x-hidden pt-1">
                                <span><@s.text name="${error}"/></span>
                            </div>
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </#list>
                </#if>
            </div>

            <div class="container my-3 p-3">
                <div class="text-center text-uppercase fw-bold fs-smaller-2">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.metadata"/></li>
                        </ol>
                    </nav>
                </div>

                <div class="text-center">
                    <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                        <@s.text name='manage.metadata.tempcoverage.title'/>
                    </h1>
                </div>

                <div class="text-center fs-smaller">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </div>

                <div class="text-center mt-2">
                    <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                    <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back"/>
                </div>
            </div>
        </div>

        <#include "metadata_section_select.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout">

                <main class="bd-main bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <div class="my-md-3 p-3">
                            <div class="row g-2 mt-0">
                                <div class="col-md-6">
                                    <@checkbox name="inferTemporalCoverageAutomatically" value="${inferTemporalCoverageAutomatically?c}" i18nkey="eml.inferAutomatically"/>
                                </div>

                                <div id="preview-links" class="col-md-6">
                                    <div class="d-flex justify-content-end">
                                        <a id="preview-inferred-temporal" class="text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                            </svg>
                                        </span>
                                            <span><@s.text name="eml.previewInferred"/></span>
                                        </a>
                                    </div>
                                    <div id="dateInferred" class="text-smaller mt-0 d-flex justify-content-end" style="display: none !important;">
                                        ${(inferredMetadata.lastModified?datetime?string.medium)!}&nbsp;
                                        <a href="metadata-tempcoverage.do?r=${resource.shortname}&amp;reinferMetadata=true">
                                        <span>
                                            <svg class="link-icon" viewBox="0 0 24 24">
                                                <path d="m19 8-4 4h3c0 3.31-2.69 6-6 6-1.01 0-1.97-.25-2.8-.7l-1.46 1.46C8.97 19.54 10.43 20 12 20c4.42 0 8-3.58 8-8h3l-4-4zM6 12c0-3.31 2.69-6 6-6 1.01 0 1.97.25 2.8.7l1.46-1.46C15.03 4.46 13.57 4 12 4c-4.42 0-8 3.58-8 8H1l4 4 4-4H6z"></path>
                                            </svg>
                                        </span>
                                            <span><@s.text name="eml.reinfer"/></span>
                                        </a>
                                    </div>
                                </div>
                            </div>

                            <p class="mb-0 my-3 intro">
                                <@s.text name='manage.metadata.tempcoverage.intro'/>
                            </p>

                            <div id="temporals">
                                <!-- Adding the temporal coverages that already exists on the file -->
                                <#assign next_agent_index=0 />
                                <#list eml.temporalCoverages as temporalCoverage>
                                    <div id="temporal-${temporalCoverage_index}" class="tempo clearfix row g-3 border-bottom pb-3" >
                                        <div class="d-flex justify-content-end mt-4">
                                            <a id="removeLink-${temporalCoverage_index}" class="removeLink text-smaller" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                    </svg>
                                                </span>
                                                <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.tempcoverage.item'/></span>
                                            </a>
                                        </div>

                                        <div class="col-lg-6">
                                            <@select i18nkey="eml.temporalCoverages.type"  name="tempTypes-${temporalCoverage_index}" options=tempTypes value="${temporalCoverage.type}" />
                                        </div>

                                        <!-- Adding new subform -->
                                        <#if "${temporalCoverage.type}" == "DATE_RANGE" >
                                            <div id="date-${temporalCoverage_index}" class="typeForm col-12">
                                                <div class="row g-3">
                                                    <div class="col-lg-6">
                                                        <@input date=true i18nkey="eml.temporalCoverages.startDate" name="eml.temporalCoverages[${temporalCoverage_index}].startDate" help="i18n" helpOptions={"YYYY-MM-DD":"YYYY-MM-DD"}/>
                                                    </div>
                                                    <div class="col-lg-6">
                                                        <@input date=true i18nkey="eml.temporalCoverages.endDate" name="eml.temporalCoverages[${temporalCoverage_index}].endDate" help="i18n" helpOptions={"YYYY-MM-DD":"YYYY-MM-DD"}/>
                                                    </div>
                                                </div>
                                            </div>

                                        <#elseif "${temporalCoverage.type}" == "SINGLE_DATE" >
                                            <div id="single-${temporalCoverage_index}" class="typeForm col-lg-6" >
                                                <div>
                                                    <@input date=true i18nkey="eml.temporalCoverages.singleDate" name="eml.temporalCoverages[${temporalCoverage_index}].startDate" help="i18n" helpOptions={"YYYY-MM-DD":"YYYY-MM-DD"}/>
                                                </div>

                                            </div>

                                        <#elseif "${temporalCoverage.type}" == "FORMATION_PERIOD" >
                                            <div id="formation-${temporalCoverage_index}" class="typeForm col-lg-6" >
                                                <div>
                                                    <@input i18nkey="eml.temporalCoverages.formationPeriod" name="eml.temporalCoverages[${temporalCoverage_index}].formationPeriod" help="i18n" />
                                                </div>
                                            </div>

                                        <#else> <!-- LIVING_TIME_PERIOD -->
                                            <div id="living-${temporalCoverage_index}" class="typeForm col-lg-6"  >
                                                <div>
                                                    <@input i18nkey="eml.temporalCoverages.livingTimePeriod" name="eml.temporalCoverages[${temporalCoverage_index}].livingTimePeriod" help="i18n" />
                                                </div>
                                            </div>
                                        </#if>

                                    </div>
                                </#list>
                            </div>

                            <!-- Static data -->
                            <div id="static-temporal" class="mt-4" style="display: none;">
                                <!-- Data is inferred, preview -->
                                <#if (inferredMetadata.inferredTemporalCoverage.data)??>
                                    <div class="table-responsive">
                                        <table class="table table-sm table-borderless">
                                            <tr>
                                                <th class="col-4"><@s.text name='eml.temporalCoverages.startDate'/> / <@s.text name='eml.temporalCoverages.endDate'/></th>
                                                <td>${(inferredMetadata.inferredTemporalCoverage.data.startDate?date)!} / ${(inferredMetadata.inferredTemporalCoverage.data.endDate?date)!}</td>
                                            </tr>
                                        </table>
                                    </div>
                                <!-- Data infer finished, but there are errors/warnings -->
                                <#elseif (inferredMetadata.inferredTemporalCoverage)?? && inferredMetadata.inferredTemporalCoverage.errors?size != 0>
                                    <#list inferredMetadata.inferredTemporalCoverage.errors as error>
                                        <div class="callout callout-danger text-smaller">
                                            <@s.text name="${error}"/>
                                        </div>
                                    </#list>
                                <!-- Other -->
                                <#else>
                                    <div class="callout callout-warning text-smaller">
                                        <@s.text name="eml.warning.reinfer"/>
                                    </div>
                                </#if>
                            </div>

                            <!-- The add link and the buttons should be first. The next div is hidden. -->
                            <div class="addNew col-12 mt-2">
                                <a id="plus" href="" class="text-smaller">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon">
                                            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                        </svg>
                                    </span>
                                    <span><@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.tempcoverage.item' /></span>
                                </a>
                            </div>

                            <!-- internal parameter -->
                            <input name="r" type="hidden" value="${resource.shortname}" />

                            <!-- The base form that is going to be cloned every time an user click in the 'add' link -->
                            <div id="base-temporal-99999" class="tempo clearfix row g-3 border-bottom pb-3" style="display:none">
                                <div class="d-flex justify-content-end mt-4">
                                    <a id="removeLink" class="removeLink text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.tempcoverage.item'/></span>
                                    </a>
                                </div>
                                <div class="col-lg-6">
                                    <@select i18nkey="eml.temporalCoverages.type"  name="tempTypes" options=tempTypes />
                                </div>
                            </div>

                            <!-- DATE RANGE -->
                            <div id="date-99999" class="typeForm col-12" style="display:none">
                                <div class="row g-3">
                                    <div class="col-lg-6">
                                        <@input date=true i18nkey="eml.temporalCoverages.startDate" name="startDate" help="i18n" helpOptions={"YYYY-MM-DD":"YYYY-MM-DD"}/>
                                    </div>
                                    <div class="col-lg-6">
                                        <@input date=true i18nkey="eml.temporalCoverages.endDate" name="endDate" help="i18n" helpOptions={"YYYY-MM-DD":"YYYY-MM-DD"}/>
                                    </div>
                                </div>
                            </div>

                            <!-- SINGLE DATE -->
                            <div id="single-99999" class="typeForm col-lg-6" style="display:none">
                                <div>
                                    <@input date=true i18nkey="eml.temporalCoverages.singleDate" name="startDate" help="i18n" helpOptions={"YYYY-MM-DD":"YYYY-MM-DD"} />
                                </div>
                            </div>

                            <!-- FORMATION PERIOD -->
                            <div id="formation-99999" class="typeForm col-lg-6" style="display:none">
                                <div>
                                    <@input i18nkey="eml.temporalCoverages.formationPeriod" name="formationPeriod" help="i18n" />
                                </div>
                            </div>

                            <!-- LIVING TIME PERIOD -->
                            <div id="living-99999" class="typeForm col-lg-6" style="display:none">
                                <div>
                                    <@input i18nkey="eml.temporalCoverages.livingTimePeriod" name="livingTimePeriod" help="i18n" />
                                </div>
                            </div>

                        </div>
                    </div>
                </main>
            </div>
        </div>

    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
