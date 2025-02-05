<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
<title xmlns="http://www.w3.org/1999/html"><@s.text name='manage.metadata.geocoverage.title'/></title>
    <#assign currentMetadataPage = "geocoverage"/>
    <#assign currentMenu="manage"/>

    <link rel="stylesheet" href="${baseURL}/styles/leaflet/leaflet.css" />
    <link rel="stylesheet" href="${baseURL}/styles/leaflet/locationfilter.css" />
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <script src="${baseURL}/js/leaflet/leaflet.js"></script>
    <script src="${baseURL}/js/leaflet/locationfilter.js"></script>

    <#assign currentLocale = .vars["locale"]/>
    <#if currentLocale == "es" || currentLocale == "fr" || currentLocale == "ru" || currentLocale == "pt" >
        <#assign decimalDelimiter = ","/>
        <#assign wrongDecimalDelimiter = "."/>
    <#else>
        <#assign decimalDelimiter = "."/>
        <#assign wrongDecimalDelimiter = ","/>
    </#if>

    <script>
        $(document).ready(function() {
            function updateQueryParam(param, value) {
                const url = new URL(window.location.href);
                url.searchParams.set(param, value);
                history.replaceState(null, '', url);
            }

            function deleteQueryParam(param) {
                const url = new URL(window.location.href);
                url.searchParams.delete(param)
                history.replaceState(null, '', url);
            }

            // Function to check if query param exists and update checkbox accordingly
            function checkUrlParams() {
                // if "inferAutomatically" present and true, tick the inferGeocoverageAutomatically checkbox
                const urlParams = new URLSearchParams(window.location.search);
                const checkboxParam = urlParams.get('inferAutomatically');
                if (checkboxParam === 'true') {
                    $('#inferGeocoverageAutomatically').prop('checked', true);
                }

                // remove "reinferMetadata" param on load
                const reInferParam = urlParams.get('reinferMetadata');
                if (reInferParam === 'true') {
                    deleteQueryParam("reinferMetadata")
                }
            }

            // add/remove "inferAutomatically" param when clicking checkbox
            $('#inferGeocoverageAutomatically').change(function() {
                if ($(this).is(':checked')) {
                    updateQueryParam('inferAutomatically', 'true');
                } else {
                    deleteQueryParam('inferAutomatically');
                }
            });

            // Check query params on page load
            checkUrlParams();

            $("#re-infer-link").on('click', displayProcessing);

            var newBboxBase = "eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.";
            var maxLatId = newBboxBase + "max\\.latitude";
            var minLatId = newBboxBase + "min\\.latitude";
            var maxLngId = newBboxBase + "max\\.longitude";
            var minLngId = newBboxBase + "min\\.longitude";

            const MIN_LNG_VAL_LIMIT = -180;
            const MAX_LNG_VAL_LIMIT = 180;
            const MIN_LAT_VAL_LIMIT = -90;
            const MAX_LAT_VAL_LIMIT = 90;

            var pixel_ratio = parseInt(window.devicePixelRatio) || 1;

            var max_zoom = 16;
            var tile_size = 512;

            var map = L.map('map').setView([0, 0], 1);

            L.tileLayer('https://tile.gbif.org/3857/omt/{z}/{x}/{y}@{r}x.png?style=osm-bright'.replace('{r}', pixel_ratio), {
                minZoom: 1,
                maxZoom: max_zoom + 1,
                zoomOffset: -1,
                tileSize: tile_size
            }).addTo(map);

            // populate coordinate fields, using min max values as defaults if none exist
            var minLngInputValue = $("#" + minLngId).val();
            var maxLngInputValue = $("#" + maxLngId).val();
            var minLatInputValue = $("#" + minLatId).val();
            var maxLatInputValue = $("#" + maxLatId).val();
            var minLngVal = isNaN(parseFloat(minLngInputValue)) ? MIN_LNG_VAL_LIMIT : parseFloat(minLngInputValue.replace(",", "."));
            var maxLngVal = isNaN(parseFloat(maxLngInputValue)) ? MAX_LNG_VAL_LIMIT : parseFloat(maxLngInputValue.replace(",", "."));
            var minLatVal = isNaN(parseFloat(minLatInputValue)) ? MIN_LAT_VAL_LIMIT : parseFloat(minLatInputValue.replace(",", "."));
            var maxLatVal = isNaN(parseFloat(maxLatInputValue)) ? MAX_LAT_VAL_LIMIT : parseFloat(maxLatInputValue.replace(",", "."));

            var adjustedBounds = adjustBoundsForDateLine(minLngVal, maxLngVal);

            var bounds = L.latLngBounds(
                L.latLng(minLatVal, adjustedBounds.west),  // Southwest corner
                L.latLng(maxLatVal, adjustedBounds.east)   // Northeast corner
            );

            // make the location filter: a draggable/resizable rectangle
            var locationFilter = new L.LocationFilter({
                enable: true,
                enableButton: false,
                adjustButton: false,
                bounds:  bounds
            }).addTo(map);

            // Function to adjust the longitude values if they cross the international date line
            function adjustBoundsForDateLine(minLng, maxLng) {
                if (minLng > maxLng) {
                    return {
                        west: minLng,
                        east: maxLng + 360
                    };
                } else {
                    return {
                        west: minLng,
                        east: maxLng
                    };
                }
            }

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

                // replace "," with "." (if needed) for correct work of locationFilter
                var minLngRaw = "${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.min.longitude)!?replace(",", ".")}"
                var minLngVal = parseFloat(minLngRaw);
                if (isNaN(minLngVal)) minLngVal = -180;

                var maxLngRaw = "${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.max.longitude)!?replace(",", ".")}";
                var maxLngVal = parseFloat(maxLngRaw);
                if (isNaN(maxLngVal)) maxLngVal = 180;

                var minLatRaw = "${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.min.latitude)!?replace(",", ".")}";
                var minLatVal = parseFloat(minLatRaw);
                if (isNaN(minLatVal)) minLatVal = -90;

                var maxLatRaw = "${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.max.latitude)!?replace(",", ".")}";
                var maxLatVal = parseFloat(maxLatRaw);
                if (isNaN(maxLatVal)) maxLatVal = 90;

                locationFilter.setBounds(L.latLngBounds(L.latLng(minLatVal, minLngVal), L.latLng(maxLatVal, maxLngVal)), skipAdditionalAdjustment);
            }

            function setInferredCoordinatesToInputs() {
                <#if (inferredMetadata.inferredGeographicCoverage.data)??>
                    $("#eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.min\\.longitude").val("${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.min.longitude)!}");
                    $("#eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.max\\.longitude").val("${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.max.longitude)!}");
                    $("#eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.min\\.latitude").val("${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.min.latitude)!}");
                    $("#eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.max\\.latitude").val("${(inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.max.latitude)!}");
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

            function normalizeLongitude(lng) {
                // Normalize longitude to the range of -180 to 180
                return ((lng + 180) % 360 + 360) % 360 - 180;
            }

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

                    var minLatVal = locationFilter.getBounds()._southWest.lat;
                    var minLngVal = normalizeLongitude(locationFilter.getBounds()._southWest.lng);
                    var maxLatVal = locationFilter.getBounds()._northEast.lat;
                    var maxLngVal = normalizeLongitude(locationFilter.getBounds()._northEast.lng);

                    // only language
                    var localeLanguageCode = "${currentLocale}".split("_")[0];

                    var minLatValFormatted = minLatVal.toLocaleString(localeLanguageCode);
                    var minLngValFormatted = minLngVal.toLocaleString(localeLanguageCode);
                    var maxLatValFormatted = maxLatVal.toLocaleString(localeLanguageCode);
                    var maxLngValFormatted = maxLngVal.toLocaleString(localeLanguageCode);

                    $("#" + minLatId).val(minLatValFormatted);
                    $("#" + minLngId).val(minLngValFormatted);
                    $("#" + maxLatId).val(maxLatValFormatted);
                    $("#" + maxLngId).val(maxLngValFormatted);
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

                var adjustedLongitude = adjustBoundsForDateLine(minLngVal, maxLngVal);

                locationFilter.setBounds(L.latLngBounds(L.latLng(minLatVal, adjustedLongitude.west), L.latLng(maxLatVal, adjustedLongitude.east)), true);
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
                var textLng = '${action.getText("validation.longitude.value")?js_string}';
                var textLat = '${action.getText("validation.latitude.value")?js_string}';
                var textWrongDecimalSeparator = '${action.getText("validation.coordinates.wrong.separator")?js_string}';

                var submitForm = true;

                // validate
                if (minLng) {
                    var minLngVal = minLng.val();
                    if (minLngVal < -180) {
                        minLng.addClass("is-invalid");
                        minLng.after("<ul id=\"field-error-eml.geospatialCoverages[0].boundingCoordinates.min.longitude\" class=\"invalid-feedback list-unstyled field-error my-1\"><li><span>" + textLng + "</span></li></ul>")
                        submitForm = false;
                    }

                    if (minLngVal.includes("${wrongDecimalDelimiter}")) {
                        minLng.addClass("is-invalid");
                        minLng.after("<ul id=\"field-error-eml.geospatialCoverages[0].boundingCoordinates.min.longitude\" class=\"invalid-feedback list-unstyled field-error my-1\"><li><span>" + textWrongDecimalSeparator + "</span></li></ul>")
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

                    if (maxLngVal.includes("${wrongDecimalDelimiter}")) {
                        maxLng.addClass("is-invalid");
                        maxLng.after("<ul id=\"field-error-eml.geospatialCoverages[0].boundingCoordinates.max.longitude\" class=\"invalid-feedback list-unstyled field-error my-1\"><li><span>" + textWrongDecimalSeparator + "</span></li></ul>")
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

                    if (minLatVal.includes("${wrongDecimalDelimiter}")) {
                        minLat.addClass("is-invalid");
                        minLat.after("<ul id=\"field-error-eml.geospatialCoverages[0].boundingCoordinates.min.latitude\" class=\"invalid-feedback list-unstyled field-error my-1\"><li><span>" + textWrongDecimalSeparator + "</span></li></ul>")
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

                    if (maxLatVal.includes("${wrongDecimalDelimiter}")) {
                        maxLat.addClass("is-invalid");
                        maxLat.after("<ul id=\"field-error-eml.geospatialCoverages[0].boundingCoordinates.max.latitude\" class=\"invalid-feedback list-unstyled field-error my-1\"><li><span>" + textWrongDecimalSeparator + "</span></li></ul>")
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

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>

    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">

    <div id="geocoverage-no-available-data-warning" class="alert alert-warning alert-dismissible fade show d-flex" style="display: none !important;" role="alert">
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
                    <div class="alert alert-danger alert-dismissible fade show d-flex metadata-error-alert" role="alert" style="display: none !important;">
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

<form id="geocoverage-form" class="needs-validation" action="metadata-${section}.do" method="post" novalidate>
    <div class="container-fluid bg-body border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3">
                <div class="text-center fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.metadata"/></li>
                        </ol>
                    </nav>
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
    </div>

    <#include "metadata_section_select.ftl"/>

    <div class="container-fluid bg-body">
        <div class="container bd-layout main-content-container">
            <main class="bd-main">
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
                                    <a id="preview-inferred-geo" class="metadata-action-link custom-link p-1" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name="eml.previewInferred"/></span>
                                    </a>
                                </div>
                                <div id="dateInferred" class="text-smaller mt-0 d-flex justify-content-end" style="display: none !important;">
                                    <span class="fs-smaller-2 p-1">${(inferredMetadata.lastModified?datetime?string.medium)!}&nbsp;</span>
                                    <a id="re-infer-link" href="metadata-geocoverage.do?r=${resource.shortname}&amp;reinferMetadata=true&amp;inferAutomatically=true" class="metadata-action-link custom-link p-1">
                                        <span>
                                            <svg class="link-icon link-icon-primary" viewBox="0 0 24 24">
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
                                        <label class="form-label" for="eml.geospatialCoverages[0].boundingCoordinates.min.longitude">
                                            <@s.text name="eml.geospatialCoverages.boundingCoordinates.min.longitude"/>
                                            <span class="text-gbif-danger">&#42;</span>
                                        </label>
                                        <input type="text" class="form-control" id="eml.geospatialCoverages[0].boundingCoordinates.min.longitude" name="eml.geospatialCoverages[0].boundingCoordinates.min.longitude" <#if (eml.geospatialCoverages[0].boundingCoordinates.min.longitude)?has_content>value="${(eml.geospatialCoverages[0].boundingCoordinates.min.longitude)?string["0.###"]}" <#else>value=""</#if> />
                                        <@s.fielderror id="field-error-eml.geospatialCoverages[0].boundingCoordinates.min.longitude" cssClass="invalid-feedback list-unstyled field-error my-1" fieldName="eml.geospatialCoverages[0].boundingCoordinates.min.longitude"/>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label" for="eml.geospatialCoverages[0].boundingCoordinates.max.longitude">
                                            <@s.text name="eml.geospatialCoverages.boundingCoordinates.max.longitude"/>
                                            <span class="text-gbif-danger">&#42;</span>
                                        </label>
                                        <input type="text" id="eml.geospatialCoverages[0].boundingCoordinates.max.longitude" name="eml.geospatialCoverages[0].boundingCoordinates.max.longitude" class="form-control" <#if (eml.geospatialCoverages[0].boundingCoordinates.max.longitude)?has_content> value="${(eml.geospatialCoverages[0].boundingCoordinates.max.longitude)?string["0.###"]}" <#else>value=""</#if> />
                                        <@s.fielderror id="field-error-eml.geospatialCoverages[0].boundingCoordinates.max.longitude" cssClass="invalid-feedback list-unstyled field-error my-1" fieldName="eml.geospatialCoverages[0].boundingCoordinates.max.longitude"/>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label" for="eml.geospatialCoverages[0].boundingCoordinates.min.latitude">
                                            <@s.text name="eml.geospatialCoverages.boundingCoordinates.min.latitude"/>
                                            <span class="text-gbif-danger">&#42;</span>
                                        </label>
                                        <input type="text" id="eml.geospatialCoverages[0].boundingCoordinates.min.latitude" name="eml.geospatialCoverages[0].boundingCoordinates.min.latitude" class="form-control" <#if (eml.geospatialCoverages[0].boundingCoordinates.min.latitude)?has_content> value="${(eml.geospatialCoverages[0].boundingCoordinates.min.latitude)?string["0.###"]}" <#else>value=""</#if> />
                                        <@s.fielderror id="field-error-eml.geospatialCoverages[0].boundingCoordinates.min.latitude" cssClass="invalid-feedback list-unstyled field-error my-1" fieldName="eml.geospatialCoverages[0].boundingCoordinates.min.latitude"/>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label" for="eml.geospatialCoverages[0].boundingCoordinates.max.latitude">
                                            <@s.text name="eml.geospatialCoverages.boundingCoordinates.max.latitude"/>
                                            <span class="text-gbif-danger">&#42;</span>
                                        </label>
                                        <input type="text" id="eml.geospatialCoverages[0].boundingCoordinates.max.latitude" name="eml.geospatialCoverages[0].boundingCoordinates.max.latitude" class="form-control" <#if (eml.geospatialCoverages[0].boundingCoordinates.max.latitude)?has_content>value="${(eml.geospatialCoverages[0].boundingCoordinates.max.latitude)?string["0.###"]}" <#else>value=""</#if> />
                                        <@s.fielderror id="field-error-eml.geospatialCoverages[0].boundingCoordinates.max.latitude" cssClass="invalid-feedback list-unstyled field-error my-1" fieldName="eml.geospatialCoverages[0].boundingCoordinates.max.latitude"/>
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
