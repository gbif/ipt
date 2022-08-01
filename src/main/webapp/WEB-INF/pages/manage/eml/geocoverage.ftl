<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
<title xmlns="http://www.w3.org/1999/html"><@s.text name='manage.metadata.geocoverage.title'/></title>
    <#assign currentMetadataPage = "geocoverage"/>
    <#assign currentMenu="manage"/>

    <link rel="stylesheet" href="${baseURL}/styles/leaflet/leaflet.css" />
    <link rel="stylesheet" href="${baseURL}/styles/leaflet/locationfilter.css" />
    <script src="${baseURL}/js/leaflet/leaflet.js"></script>
    <script src="${baseURL}/js/leaflet/tile.stamen.js"></script>
    <script src="${baseURL}/js/leaflet/locationfilter.js"></script>

    <script>
        $(document).ready(function() {
            var newBboxBase = "eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.";
            var maxLatId = newBboxBase + "max\\.latitude";
            var minLatId = newBboxBase + "min\\.latitude";
            var maxLngId = newBboxBase + "max\\.longitude";
            var minLngId = newBboxBase + "min\\.longitude";

            const MIN_LNG_VAL_LIMIT = -180;
            const MAX_LNG_VAL_LIMIT = 180;
            const MIN_LAT_VAL_LIMIT = -90;
            const MAX_LAT_VAL_LIMIT = 90;

            var map = new L.map('map').setView([0, 0], 10).setMaxBounds(L.latLngBounds(L.latLng(-90, -360), L.latLng(90, 360)));

            var layer = new L.StamenTileLayer("terrain");
            map.addLayer(layer, {
                detectRetina: true
            });

            // populate coordinate fields, using min max values as defaults if none exist
            var minLngVal = isNaN(parseFloat($("#" + minLngId).val())) ? MIN_LNG_VAL_LIMIT : parseFloat($("#" + minLngId).val());
            var maxLngVal = isNaN(parseFloat($("#" + maxLngId).val())) ? MAX_LNG_VAL_LIMIT : parseFloat($("#" + maxLngId).val());
            var minLatVal = isNaN(parseFloat($("#" + minLatId).val())) ? MIN_LAT_VAL_LIMIT : parseFloat($("#" + minLatId).val());
            var maxLatVal = isNaN(parseFloat($("#" + maxLatId).val())) ? MAX_LAT_VAL_LIMIT : parseFloat($("#" + maxLatId).val());

            // make the location filter: a draggable/resizable rectangle
            var locationFilter = new L.LocationFilter({
                enable: true,
                enableButton: false,
                adjustButton: false,
                bounds:  L.latLngBounds(L.latLng(minLatVal, minLngVal), L.latLng(maxLatVal, maxLngVal))
            }).addTo(map);

            // checks if global coverage is set. If on, coordinate input fields are hidden and the map disabled
            if (maxLatVal === MAX_LAT_VAL_LIMIT && minLatVal === MIN_LAT_VAL_LIMIT && maxLngVal === MAX_LNG_VAL_LIMIT && minLngVal === MIN_LNG_VAL_LIMIT) {
                $('input[name=globalCoverage]').attr('checked', true);
                $("#" + minLngId).val(MIN_LNG_VAL_LIMIT);
                $("#" + maxLngId).val(MAX_LNG_VAL_LIMIT);
                $("#" + minLatId).val(MIN_LAT_VAL_LIMIT);
                $("#" + maxLatId).val(MAX_LAT_VAL_LIMIT);
                $("#coordinates").hide();
                locationFilter.disable();
                map.fitWorld();
            }

            $("#preview-inferred-geo").click(function (e) {
                e.preventDefault();
                setInferredCoordinatesToInputs();
                $("#dateInferred").show();
                $("#globalCoverage").prop("checked", false);
                adjustMapWithInferredCoordinates();
                <#if (inferredMetadata.inferredGeographicCoverage)?? && inferredMetadata.inferredGeographicCoverage.errors?size gt 0>
                    $(".metadata-error-alert").show();
                </#if>
            });

            if ($("#globalCoverage").is(':checked')) {
                $('#inferGeocoverageAutomaticallyWrapper').hide();
                $('.intro').hide();
            }

            if ($("#inferGeocoverageAutomatically").is(':checked')) {
                $('#globalCoverageWrapper').hide();
                $('#coordinates').hide();
                $('.intro').hide();
                $('#preview-inferred-geo').hide();
                $('#static-coordinates').show();
                $("#dateInferred").show();
                adjustMapWithInferredCoordinates()
                setInferredCoordinatesToInputs();
            }

            $("#inferGeocoverageAutomatically").click(function() {
                if ($("#inferGeocoverageAutomatically").is(':checked')) {
                    $('#globalCoverageWrapper').hide();
                    $('#coordinates').hide();
                    $('.intro').hide();
                    $('#preview-inferred-geo').hide();
                    $('#static-coordinates').show();
                    $("#dateInferred").show();
                    adjustMapWithInferredCoordinates(true)
                    setInferredCoordinatesToInputs()
                    $("#globalCoverage").prop("checked", false);
                    $("#inferGeocoverageAutomatically").prop("checked", true);
                    $("#coordinates").hide();
                    $("#static-coordinates").show();
                } else {
                    $('#globalCoverageWrapper').show();
                    $('#coordinates').show();
                    $('.intro').show();
                    $('#preview-inferred-geo').show();
                    $('#static-coordinates').hide();
                }
            });

            function adjustMapWithInferredCoordinates(skipAdditionalAdjustment) {
                locationFilter.enable();
                var minLngVal = parseFloat(${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.min.longitude)!\-180?c});
                var maxLngVal = parseFloat(${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.max.longitude)!180?c});
                var minLatVal = parseFloat(${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.min.latitude)!\-90?c});
                var maxLatVal = parseFloat(${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.max.latitude)!90?c});
                locationFilter.setBounds(L.latLngBounds(L.latLng(minLatVal, minLngVal), L.latLng(maxLatVal, maxLngVal)), skipAdditionalAdjustment);
            }

            function setInferredCoordinatesToInputs() {
                <#if (inferredMetadata.inferredGeographicCoverage.data)??>
                    $("#eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.min\\.longitude").val(${inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.min.longitude});
                    $("#eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.max\\.longitude").val(${inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.max.longitude});
                    $("#eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.min\\.latitude").val(${inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.min.latitude});
                    $("#eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.max\\.latitude").val(${inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.max.latitude});
                </#if>
            }

            /** This function updates the map each time the global coverage checkbox is checked or unchecked  */
            $("#globalCoverage").click(function() {
                if($("#globalCoverage").is(":checked")) {
                    $("#" + minLngId).val(MIN_LNG_VAL_LIMIT);
                    $("#" + maxLngId).val(MAX_LNG_VAL_LIMIT);
                    $("#" + minLatId).val(MIN_LAT_VAL_LIMIT);
                    $("#" + maxLatId).val(MAX_LAT_VAL_LIMIT);
                    $("#coordinates").hide();
                    $('#inferGeocoverageAutomaticallyWrapper').hide();
                    $('.intro').hide();
                    locationFilter.disable();
                    map.fitWorld();
                } else {
                    var minLngVal = parseFloat(${(eml.geospatialCoverages[0].boundingCoordinates.min.longitude)!\-180?c});
                    var maxLngVal = parseFloat(${(eml.geospatialCoverages[0].boundingCoordinates.max.longitude)!180?c});
                    var minLatVal = parseFloat(${(eml.geospatialCoverages[0].boundingCoordinates.min.latitude)!\-90?c});
                    var maxLatVal = parseFloat(${(eml.geospatialCoverages[0].boundingCoordinates.max.latitude)!90?c});
                    $("#" + minLngId).val(minLngVal);
                    $("#" + maxLngId).val(maxLngVal);
                    $("#" + minLatId).val(minLatVal);
                    $("#" + maxLatId).val(maxLatVal);
                    $("#coordinates").show();
                    $('#inferGeocoverageAutomaticallyWrapper').show();
                    $('.intro').show();
                    locationFilter.enable();
                    locationFilter.setBounds(L.latLngBounds(L.latLng(minLatVal, minLngVal), L.latLng(maxLatVal, maxLngVal)));
                }
            });


            /** This function updates the coordinate input fields to mirror bounding box coordinates, after each map change event  */
            locationFilter.on("change", function (e) {
                if (!e.skipMapAdjustment) {
                    // manual adjustments - stop inferring automatically
                    $("#inferGeocoverageAutomatically").prop("checked", false);
                    $("#coordinates").show();
                    $("#globalCoverageWrapper").show();
                    $("#static-coordinates").hide();
                    $('#preview-inferred-geo').show();
                    $('.intro').show();

                    var minLatVal = locationFilter.getBounds()._southWest.lat
                    var minLngVal = locationFilter.getBounds()._southWest.lng
                    var maxLatVal = locationFilter.getBounds()._northEast.lat
                    var maxLngVal = locationFilter.getBounds()._northEast.lng

                    $("#" + minLatId).val(minLatVal);
                    $("#" + minLngId).val(minLngVal);
                    $("#" + maxLatId).val(maxLatVal);
                    $("#" + maxLngId).val(maxLngVal);
                }
            });

            // lock map on disable
            locationFilter.on("disabled", function (e) {
                locationFilter.setBounds(L.latLngBounds(L.latLng(minLatVal, minLngVal), L.latLng(maxLatVal, maxLngVal)))
            });

            /** This function adjusts the map each time the user enters data */
            $("#bbox input").keyup(function() {
                var minLngStr = $("#" + minLngId).val();
                var maxLngStr = $("#" + maxLngId).val();
                var minLatStr = $("#" + minLatId).val();
                var maxLatStr = $("#" + maxLatId).val();

                // ignore these values
                if (minLngStr.endsWith(".") || minLngStr === "-" || minLngStr === ""
                    || maxLngStr.endsWith(".") || maxLngStr === "-" || maxLngStr === ""
                    || minLatStr.endsWith(".") || minLatStr === "-" || minLatStr === ""
                    || maxLatStr.endsWith(".") || maxLatStr === "-" || maxLatStr === "") {
                    return
                }

                var minLngVal = parseFloat(minLngStr);
                var maxLngVal = parseFloat(maxLngStr);
                var minLatVal = parseFloat(minLatStr);
                var maxLatVal = parseFloat(maxLatStr);

                if (isNaN(minLngVal)) {
                    minLngVal = MIN_LNG_VAL_LIMIT;
                }
                if (isNaN(maxLngVal)) {
                    maxLngVal = MAX_LNG_VAL_LIMIT;
                }
                if (isNaN(minLatVal)) {
                    minLatVal = MIN_LAT_VAL_LIMIT;
                }
                if (isNaN(maxLatVal)) {
                    maxLatVal = MAX_LAT_VAL_LIMIT;
                }
                locationFilter.setBounds(L.latLngBounds(L.latLng(minLatVal, minLngVal), L.latLng(maxLatVal, maxLngVal)), true);
            });

            $('#metadata-section').change(function () {
                var metadataSection = $('#metadata-section').find(':selected').val()
                $(location).attr('href', 'metadata-' + metadataSection + '.do?r=${resource.shortname!r!}');
            });

            function validateCoordinates(event) {
                // prevent form submit
                event.preventDefault();

                // remove error messages and error classes before validation
                $('.invalid-feedback').remove();
                $('.form-control').removeClass('is-invalid');

                var minLng = $("#eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.min\\.longitude")
                var maxLng = $("#eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.max\\.longitude")
                var minLat = $("#eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.min\\.latitude")
                var maxLat = $("#eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.max\\.latitude")

                // get error messages
                var textLng = '${action.getText("validation.longitude.value")}';
                var textLat = '${action.getText("validation.latitude.value")}';

                var submitForm = true;

                // validate
                if (minLng) {
                    var minLngVal = minLng.val();
                    if (minLngVal < -180) {
                        minLng.addClass("is-invalid");
                        minLng.after("<ul id=\"field-error-eml.geospatialCoverages[0].boundingCoordinates.min.longitude\" class=\"invalid-feedback list-unstyled field-error my-1\"><li><span>" + textLng + "</span></li></ul>")
                        submitForm = false;
                    }
                }

                if (maxLng) {
                    var maxLngVal = maxLng.val();
                    if (maxLngVal > 180) {
                        maxLng.addClass("is-invalid");
                        maxLng.after("<ul id=\"field-error-eml.geospatialCoverages[0].boundingCoordinates.max.longitude\" class=\"invalid-feedback list-unstyled field-error my-1\"><li><span>" + textLng + "</span></li></ul>")
                        submitForm = false;
                    }
                }

                if (minLat) {
                    var minLatVal = minLat.val();
                    if (minLatVal < -90) {
                        minLat.addClass("is-invalid");
                        minLat.after("<ul id=\"field-error-eml.geospatialCoverages[0].boundingCoordinates.min.latitude\" class=\"invalid-feedback list-unstyled field-error my-1\"><li><span>" + textLat + "</span></li></ul>")
                        submitForm = false;
                    }
                }

                if (maxLat) {
                    var maxLatVal = maxLat.val();
                    if (maxLatVal > 90) {
                        maxLat.addClass("is-invalid");
                        maxLat.after("<ul id=\"field-error-eml.geospatialCoverages[0].boundingCoordinates.max.latitude\" class=\"invalid-feedback list-unstyled field-error my-1\"><li><span>" + textLat + "</span></li></ul>")
                        submitForm = false;
                    }
                }

                // submit form if no errors
                if (submitForm) {
                    $("#geocoverage-form").submit();
                }
            }

            $("#save").click(function (event) {
                validateCoordinates(event);
            });
            $("#top-save").click(function (event) {
                validateCoordinates(event);
            });
        });
    </script>

    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

<form id="geocoverage-form" class="needs-validation" action="metadata-${section}.do" method="post" novalidate>
    <div class="container-fluid bg-body border-bottom">
        <div class="container pt-2">
            <#include "/WEB-INF/pages/inc/action_alerts.ftl">

            <div id="geocoverage-no-available-data-warning" class="alert alert-warning mt-2 alert-dismissible fade show d-flex" style="display: none !important;" role="alert">
                <div class="me-3">
                    <i class="bi bi-exclamation-triangle alert-orange-2 fs-bigger-2 me-2"></i>
                </div>
                <div class="overflow-x-hidden pt-1">
                    <span><@s.text name="eml.warning.reinfer"/></span>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>

            <#if (inferredMetadata.inferredGeographicCoverage)??>
                <#list inferredMetadata.inferredGeographicCoverage.errors as error>
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
                <@s.text name="manage.overview.metadata"/>
            </div>

            <div class="text-center">
                <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                    <@s.text name='manage.metadata.geocoverage.title'/>
                </h1>
            </div>

            <div class="text-center fs-smaller">
                <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
            </div>

            <div class="text-center mt-2">
                <input type="submit" value="<@s.text name='button.save'/>" id="top-save" name="save" class="button btn btn-sm btn-outline-gbif-primary top-button">
                <input type="submit" value="<@s.text name='button.back'/>" id="top-cancel" name="cancel" class="button btn btn-sm btn-outline-secondary top-button">
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
                                <@checkbox name="inferGeocoverageAutomatically" value="${inferGeocoverageAutomatically?c}" i18nkey="eml.inferAutomatically"/>
                            </div>

                            <div id="preview-links" class="col-md-6">
                                <div class="d-flex justify-content-end">
                                    <a id="preview-inferred-geo" class="text-smaller" href="">
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
                                    <a href="metadata-geocoverage.do?r=${resource.shortname}&amp;reinferMetadata=true">
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

                        <div id="static-coordinates" class="mt-3" style="display: none;">
                            <!-- Data is inferred, preview -->
                            <#if (inferredMetadata.inferredGeographicCoverage.data)??>
                                <div class="table-responsive">
                                    <table class="table table-sm table-borderless">
                                        <tr>
                                            <th class="col-4"><@s.text name='eml.geospatialCoverages.boundingCoordinates'/></th>
                                            <td><@s.text name='eml.geospatialCoverages.boundingCoordinates.min.latitude'/>&nbsp;<@s.text name='eml.geospatialCoverages.boundingCoordinates.min.longitude'/>&nbsp;&#91;${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.min.latitude)!},&nbsp;${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.min.longitude)!}&#93;&#44;&nbsp;<@s.text name='eml.geospatialCoverages.boundingCoordinates.max.latitude'/>&nbsp;<@s.text name='eml.geospatialCoverages.boundingCoordinates.max.longitude'/>&nbsp;&#91;${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.max.latitude)!},&nbsp;${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.max.longitude)!}&#93;</td>
                                        </tr>
                                    </table>
                                </div>
                            <!-- Data infer finished, but there are errors/warnings -->
                            <#elseif (inferredMetadata.inferredGeographicCoverage)?? && inferredMetadata.inferredGeographicCoverage.errors?size != 0>
                                <#list inferredMetadata.inferredGeographicCoverage.errors as error>
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

                        <p class="intro mt-1"><@s.text name='manage.metadata.geocoverage.intro'/></p>

                        <div id="map"></div>

                        <div id="bbox" class="g-3">
                            <div id="globalCoverageWrapper" class="col-12">
                                <@checkbox name="globalCoverage" help="i18n" i18nkey="eml.geospatialCoverages.globalCoverage"/>
                            </div>

                            <div id="coordinates" class="mt-0">
                                <div id="separator-warning" class="callout callout-info text-smaller">
                                    <@s.text name='manage.metadata.geocoverage.warning'/>
                                </div>
                                <div class="row g-3 mt-0">
                                    <div class="col-md-6">
                                        <@input name="eml.geospatialCoverages[0].boundingCoordinates.min.longitude" value="${(eml.geospatialCoverages[0].boundingCoordinates.min.longitude?c)!}" i18nkey="eml.geospatialCoverages.boundingCoordinates.min.longitude" requiredField=true />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.geospatialCoverages[0].boundingCoordinates.max.longitude" value="${(eml.geospatialCoverages[0].boundingCoordinates.max.longitude?c)!}" i18nkey="eml.geospatialCoverages.boundingCoordinates.max.longitude" requiredField=true />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.geospatialCoverages[0].boundingCoordinates.min.latitude" value="${(eml.geospatialCoverages[0].boundingCoordinates.min.latitude?c)!}" i18nkey="eml.geospatialCoverages.boundingCoordinates.min.latitude" requiredField=true />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.geospatialCoverages[0].boundingCoordinates.max.latitude" value="${(eml.geospatialCoverages[0].boundingCoordinates.max.latitude?c)!}" i18nkey="eml.geospatialCoverages.boundingCoordinates.max.latitude" requiredField=true />
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="row g-3 mt-2">
                            <div class="col-12">
                                <@text name="eml.geospatialCoverages[0].description" value="${(eml.geospatialCoverages[0].description)!}" i18nkey="eml.geospatialCoverages.description" requiredField=true minlength=2 />
                            </div>
                        </div>


                        <!-- internal parameter -->
                        <input name="r" type="hidden" value="${resource.shortname}" />
                    </div>
                </div>
            </main>
        </div>
    </div>
</form>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
