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
                var newForm = $("#temporal-99999").clone().attr("id", idNewForm).css('display', '');
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
            function updateFields(idNewForm, index) {
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
                var typeSubForm = $("#" + idNewForm + " #tempTypes-" + index).prop("value");
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
            </div>

            <div class="container my-3 p-3">

                <div class="text-center">
                    <h5 class="pt-2 text-gbif-header fs-4 fw-400 text-center">
                        <@s.text name='manage.metadata.tempcoverage.title'/>
                    </h5>
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

                <main class="bd-main bd-main-right">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content ps-lg-4">
                        <div class="my-md-3 p-3">

                            <p class="mb-0 mb-1">
                                <@s.text name='manage.metadata.tempcoverage.intro'/>
                            </p>

                            <div id="temporals">
                                <!-- Adding the temporal coverages that already exists on the file -->
                                <#assign next_agent_index=0 />
                                <#list eml.temporalCoverages as temporalCoverage>
                                    <div id="temporal-${temporalCoverage_index}" class="tempo clearfix row g-3 border-bottom pb-3" >
                                        <div class="d-flex justify-content-end">
                                            <a id="removeLink-${temporalCoverage_index}" class="removeLink" href=""><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.tempcoverage.item'/></a>
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

                            <!-- The add link and the buttons should be first. The next div is hidden. -->
                            <div class="addNew col-12 mt-1">
                                <a id="plus" href="" ><@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.tempcoverage.item' /></a>
                            </div>

                            <!-- internal parameter -->
                            <input name="r" type="hidden" value="${resource.shortname}" />


                            <!-- The base form that is going to be cloned every time an user click in the 'add' link -->
                            <div id="temporal-99999" class="tempo clearfix row g-3 border-bottom pb-3" style="display:none">
                                <div class="d-flex justify-content-end">
                                    <a id="removeLink" class="removeLink" href=""><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.tempcoverage.item'/></a>
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
