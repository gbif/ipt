<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.MetadataAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title xmlns="http://www.w3.org/1999/html"><@s.text name='manage.metadata.geocoverage.title'/></title>
    <#assign currentMetadataPage = "geocoverage"/>
    <#assign currentMenu="manage"/>

    <link rel="stylesheet" href="${baseURL}/styles/leaflet/leaflet-1.9.4.css" />
    <link rel="stylesheet" href="${baseURL}/styles/leaflet/locationfilter.css" />
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <script src="${baseURL}/js/leaflet/leaflet-1.9.4.js"></script>
    <script src="${baseURL}/js/leaflet/locationfilter-0.1-leaflet1.js"></script>
    <#include "/WEB-INF/pages/macros/popover.ftl"/>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2-ipt.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>

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

            var $inferAutomaticallyCheckbox = $('#resource\\.inferGeocoverageAutomatically');

            // Function to check if query param exists and update checkbox accordingly
            function checkUrlParams() {
                // if "inferAutomatically" present and true, tick the inferGeocoverageAutomatically checkbox
                const urlParams = new URLSearchParams(window.location.search);
                const checkboxParam = urlParams.get('inferAutomatically');
                if (checkboxParam === 'true') {
                    $inferAutomaticallyCheckbox.prop('checked', true);
                }

                // remove "reinferMetadata" param on load
                const reInferParam = urlParams.get('reinferMetadata');
                if (reInferParam === 'true') {
                    deleteQueryParam("reinferMetadata")
                }
            }

            // add/remove "inferAutomatically" param when clicking checkbox
            $inferAutomaticallyCheckbox.change(function() {
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

            var map = L.map('map', {zoomControl: false}).setView([0, 0], 1);
            L.control.zoom({position: 'topright'}).addTo(map);
            map.attributionControl.setPrefix('<a href="https://leafletjs.com">Leaflet</a>');

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

            // no stored geographic coverage: the fields stay empty, so the part is skipped on save and doesn't block the
            // taxonomic and temporal parts with a missing description (#3319)
            var hasStoredCoverage = [minLngInputValue, maxLngInputValue, minLatInputValue, maxLatInputValue]
                .some(function (v) { return v !== undefined && v.trim() !== ""; });

            // checks if global coverage is set. If on, coordinate input fields are hidden and the map disabled
            if (hasStoredCoverage && maxLatVal === MAX_LAT_VAL_LIMIT && minLatVal === MIN_LAT_VAL_LIMIT && maxLngVal === MAX_LNG_VAL_LIMIT && minLngVal === MIN_LNG_VAL_LIMIT) {
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
                    $(".geo-error-alert").show();
                </#if>
            });

            if ($("#globalCoverage").is(':checked')) {
                $('#s-geographic .intro').hide();
            }

            if ($inferAutomaticallyCheckbox.is(':checked')) {
                $('#globalCoverageWrapper').hide();
                $('#coordinates').hide();
                $('#s-geographic .intro').hide();
                $('#preview-inferred-geo').hide();
                $('#static-coordinates').show();
                $("#dateInferred").show();
                adjustMapWithInferredCoordinates()
                setInferredCoordinatesToInputs();
            }

            $inferAutomaticallyCheckbox.click(function() {
                if ($inferAutomaticallyCheckbox.is(':checked')) {
                    $('#globalCoverageWrapper').hide();
                    $('#coordinates').hide();
                    $('#s-geographic .intro').hide();
                    $('#preview-inferred-geo').hide();
                    $('#static-coordinates').show();
                    $("#dateInferred").show();
                    adjustMapWithInferredCoordinates(true)
                    setInferredCoordinatesToInputs()
                    $("#globalCoverage").prop("checked", false);
                    $inferAutomaticallyCheckbox.prop("checked", true);
                    $("#coordinates").hide();
                    $("#static-coordinates").show();
                } else {
                    $('#globalCoverageWrapper').show();
                    $('#coordinates').show();
                    $('#s-geographic .intro').show();
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
                    $('#s-geographic .intro').hide();
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
                    $('#s-geographic .intro').show();
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
                    $inferAutomaticallyCheckbox.prop("checked", false);
                    $("#coordinates").show();
                    $("#globalCoverageWrapper").show();
                    $("#static-coordinates").hide();
                    $('#preview-inferred-geo').show();
                    $('#s-geographic .intro').show();

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

            $("#savebar-save").click(function (event) {
                validateCoordinates(event);
            });
            $("#top-save").click(function (event) {
                validateCoordinates(event);
            });

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
    <#-- taxonomic coverage (was a page of its own, #3319); its inference ids are prefixed taxonomic-, its list keeps the plain
         ids of macros/metadata.ftl -->
    <script>
        $(document).ready(function(){
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

            var $inferAutomaticallyCheckbox = $('#resource\\.inferTaxonomicCoverageAutomatically');

            // Function to check if query param exists and update checkbox accordingly
            function checkUrlParams() {
                // if "inferTaxonomicAutomatically" present and true, tick the inferTaxonomicCoverageAutomatically checkbox
                const urlParams = new URLSearchParams(window.location.search);
                const checkboxParam = urlParams.get('inferTaxonomicAutomatically');
                if (checkboxParam === 'true') {
                    // select checkbox
                    $inferAutomaticallyCheckbox.prop('checked', true);
                    // enable description input
                    $('div#static-taxanomic textarea').show().prop('disabled', false);
                }
            }

            var isInferAutomaticallyChecked = $inferAutomaticallyCheckbox.is(":checked");
            if (isInferAutomaticallyChecked) {
                // enable description input
                $('div#static-taxanomic textarea').show().prop('disabled', false);
            }

            // add/remove "inferTaxonomicAutomatically" param when clicking checkbox
            $inferAutomaticallyCheckbox.change(function() {
                if ($(this).is(':checked')) {
                    updateQueryParam('inferTaxonomicAutomatically', 'true');
                    $('div#static-taxanomic textarea').show().prop('disabled', false);
                } else {
                    deleteQueryParam('inferTaxonomicAutomatically');
                    $('div#static-taxanomic textarea').hide().prop('disabled', true);
                }
            });

            // Check query params on page load
            checkUrlParams();

            $('#plus').click(function () {
                var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'))
                var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
                    return new bootstrap.Popover(popoverTriggerEl)
                })
                try {
                    var popover = new bootstrap.Popover(document.querySelector('.popover-dismiss'), {
                        trigger: 'focus'
                    })
                } catch (TypeError) {
                }
            });

            function initAndGetSortable(selector) {
                return sortable(selector, {
                    forcePlaceholderSize: true,
                    placeholderClass: 'border',
                    handle: '.handle'
                });
            }

            const sortable_taxcoverages = initAndGetSortable('#items');
            sortable_taxcoverages[0].addEventListener('sortupdate', changeInputNamesTaxCoverageAfterDragging);
            sortable_taxcoverages[0].addEventListener('drag', dragScroll);

            $("[id^='subItems-']").each(function (index) {
                const sortable_taxa = initAndGetSortable("#" + $(this)[0].id);
                sortable_taxa[0].addEventListener('sortupdate', changeInputNamesTaxonAfterDragging);
                sortable_taxa[0].addEventListener('drag', dragScroll);
            });

            function dragScroll(e) {
                var cursor = e.pageY;
                var parentWindow = parent.window;
                var pixelsToTop = $(parentWindow).scrollTop();
                var screenHeight = $(parentWindow).height();

                if ((cursor - pixelsToTop) > screenHeight * 0.9) {
                    parentWindow.scrollBy(0, (screenHeight / 30));
                } else if ((cursor - pixelsToTop) < screenHeight * 0.1) {
                    parentWindow.scrollBy(0, -(screenHeight / 30));
                }
            }

            function changeInputNamesTaxonAfterDragging(e) {
                displayProcessing();
                var targetId = e.target.id;
                var parentIndex = targetId.split("-")[1];
                var items = $("#" + e.target.id + " div.sub-item");

                items.each(function (index) {
                    var elementId = $(this)[0].id;

                    $("div#" + elementId + " input[id$='scientificName']").attr("name", "eml.taxonomicCoverages[" + parentIndex + "].taxonKeywords[" + index + "].scientificName");
                    $("div#" + elementId + " input[id$='commonName']").attr("name", "eml.taxonomicCoverages[" + parentIndex + "].taxonKeywords[" + index + "].commonName");
                    $("div#" + elementId + " select[id$='rank']").attr("name", "eml.taxonomicCoverages[" + parentIndex + "].taxonKeywords[" + index + "].rank");
                });

                hideProcessing();
            }

            function changeInputNamesTaxCoverageAfterDragging(e) {
                displayProcessing();
                var items = $("#items div.item");

                items.each(function (index) {
                    var elementId = $(this)[0].id;

                    $("div#" + elementId + " textarea[id$='description']").attr("name", "eml.taxonomicCoverages[" + index + "].description");
                    $("div#" + elementId + " input[id^='add-button']").attr("name", "add-button-" + index);

                    var scientificNames = $("#" + elementId + " input[id$='scientificName']");
                    scientificNames.each(function (sub_index) {
                        $(this).attr("name", "eml.taxonomicCoverages[" + index + "].taxonKeywords[" + sub_index + "].scientificName");
                    });

                    var commonNames = $("#" + elementId + " input[id$='commonName']");
                    commonNames.each(function (sub_index) {
                        $(this).attr("name", "eml.taxonomicCoverages[" + index + "].taxonKeywords[" + sub_index + "].commonName");
                    });

                    var ranks = $("#" + elementId + " select[id$='rank']");
                    ranks.each(function (sub_index) {
                        $(this).attr("name", "eml.taxonomicCoverages[" + index + "].taxonKeywords[" + sub_index + "].rank");
                    });
                });

                hideProcessing();
            }

            $("#taxonomic-re-infer-link").on('click', displayProcessing);

            $('[id^="eml.taxonomicCoverages"][id$=".rank"]').select2({
                placeholder: '${action.getText("eml.rank.selection")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                allowClear: true,
                theme: 'bootstrap4'
            });
        });
    </script>
    <#-- temporal coverage (was a page of its own, #3319); its ids are prefixed temporal- where the geographic part uses the same -->
    <script>
        var DATE_RANGE = "DATE_RANGE";
        var FORMATION_PERIOD = "FORMATION_PERIOD";
        var LIVING_TIME_PERIOD = "LIVING_TIME_PERIOD";
        var SINGLE_DATE = "SINGLE_DATE";
        var count;
        // a function called when adding new temporal coverages
        // an element is cloned and the IDs reset etc etc
        $(document).ready(function () {
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

            var $inferAutomaticallyCheckbox = $('#resource\\.inferTemporalCoverageAutomatically');

            // Function to check if query param exists and update checkbox accordingly
            function checkUrlParams() {
                // if "inferTemporalAutomatically" present and true, tick the inferTemporalCoverageAutomatically checkbox
                const urlParams = new URLSearchParams(window.location.search);
                const checkboxParam = urlParams.get('inferTemporalAutomatically');
                if (checkboxParam === 'true') {
                    $inferAutomaticallyCheckbox.prop('checked', true);
                }

            }

            // add/remove "inferTemporalAutomatically" param when clicking checkbox
            $inferAutomaticallyCheckbox.change(function() {
                if ($(this).is(':checked')) {
                    updateQueryParam('inferTemporalAutomatically', 'true');
                } else {
                    deleteQueryParam('inferTemporalAutomatically');
                }
            });

            // Check query params on page load
            checkUrlParams();

            $("#temporal-re-infer-link").on('click', displayProcessing);

            calculateCount();

            function calculateCount() {
                var lastChild = $("#temporals .tempo:last-child").attr("id");
                if (lastChild !== undefined) {
                    count = parseInt(lastChild.split("-")[1]) + 1;
                } else {
                    count = new Number(0);
                }
            }

            $("#plus-temporal").click(function (event) {
                event.preventDefault();
                calculateCount();
                var idNewForm = "temporal-" + count;
                var newForm = $("#base-temporal-99999").clone().attr("id", idNewForm).css('display', '');
                // Add the fields depending on the actual value in the select
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

            let $inferTempCoverageAutomaticallyCheckbox = $("#resource\\.inferTemporalCoverageAutomatically");
            if ($inferTempCoverageAutomaticallyCheckbox.is(':checked')) {
                $("#temporals .tempo").remove();
                $('#s-temporal .intro').hide();
                $('#preview-inferred-temporal').hide();
                $('#s-temporal .addNew').hide();
                $('#static-temporal').show();
                $("#temporal-dateInferred").show();
            }

            $inferTempCoverageAutomaticallyCheckbox.click(function() {
                if ($inferTempCoverageAutomaticallyCheckbox.is(':checked')) {
                    $("#temporals .tempo").remove();
                    $('#s-temporal .intro').hide();
                    $('#preview-inferred-temporal').hide();
                    $('#s-temporal .addNew').hide();
                    $('#static-temporal').show();
                    $("#temporal-dateInferred").show();
                } else {
                    $('#s-temporal .intro').show();
                    $('#preview-inferred-temporal').show();
                    $('#s-temporal .addNew').show();
                    $('#static-temporal').hide();
                    $("#temporal-dateInferred").hide();
                }
            });

            $("#preview-inferred-temporal").click(function(event) {
                event.preventDefault();

                $("#temporal-dateInferred").show();

                <#if (inferredMetadata.inferredTemporalCoverage)?? && inferredMetadata.inferredTemporalCoverage.errors?size gt 0>
                $(".temporal-error-alert").show();
                </#if>

                <#if (inferredMetadata.inferredTemporalCoverage.data)??>
                count = 0;
                // remove all current items
                $("#temporals .tempo").remove();

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
                $("#" + idNewForm + " .removeTemporalLink").attr("id", "temporal-removeLink-" + index);
                // Remove Link (registering the event for the new links).
                $("#" + idNewForm + " .removeTemporalLink").off("click").click(
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
                }
                if (typeSubForm === FORMATION_PERIOD) {
                    $("#" + idNewForm + " [id^='formation-']").attr("id", "formation-" + index);
                    $("#" + idNewForm + " [id$='formationPeriod']").attr("id", "eml.temporalCoverages[" + index + "].formationPeriod").attr("name", function () {
                        return $(this).attr("id");
                    });
                }
                if (typeSubForm === LIVING_TIME_PERIOD) {
                    $("#" + idNewForm + " [id^='living-']").attr("id", "living-" + index);
                    $("#" + idNewForm + " [id$='livingTimePeriod']").attr("id", "eml.temporalCoverages[" + index + "].livingTimePeriod").attr("name", function () {
                        return $(this).attr("id");
                    });
                }
                if (typeSubForm === SINGLE_DATE) {
                    $("#" + idNewForm + " [id^='single-']").attr("id", "single-" + index);
                    $("#" + idNewForm + " [id$='startDate']").attr("id", "eml.temporalCoverages[" + index + "].startDate").attr("name", function () {
                        return $(this).attr("id");
                    });

                    // replace generic 'inputName-startDate' with a proper value at 'data-bs-content' attribute to be able to bind help options
                    var popovers = $("#" + idNewForm + " a.popover-link");
                    popovers[0].setAttribute("data-bs-content", popovers[0].getAttribute('data-bs-content').replace('inputName-startDate', "inputName-eml.temporalCoverages[" + index + "].startDate"))
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
            $(".removeTemporalLink").click(function (event) {
                event.preventDefault();
                removeTemporal(event);
            });

            function removeTemporal(event) {
                var $target = $(event.target);
                if (!$target.is('a')) {
                    $target = $(event.target).closest('a');
                }
                var index = $target.attr("id").split("-").pop();
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

            // reordering
            function initAndGetSortable(selector) {
                return sortable(selector, {
                    forcePlaceholderSize: true,
                    placeholderClass: 'border',
                    handle: '.handle'
                });
            }

            const sortable_temporals = initAndGetSortable('#temporals');

            sortable_temporals[0].addEventListener('sortupdate', changeInputNamesAfterDragging);
            sortable_temporals[0].addEventListener('drag', dragScroll);

            function dragScroll(e) {
                var cursor = e.pageY;
                var parentWindow = parent.window;
                var pixelsToTop = $(parentWindow).scrollTop();
                var screenHeight = $(parentWindow).height();

                if ((cursor - pixelsToTop) > screenHeight * 0.9) {
                    parentWindow.scrollBy(0, (screenHeight / 30));
                } else if ((cursor - pixelsToTop) < screenHeight * 0.1) {
                    parentWindow.scrollBy(0, -(screenHeight / 30));
                }
            }

            function changeInputNamesAfterDragging(e) {
                displayProcessing();
                var contactItems = $("#temporals div.tempo");

                contactItems.each(function (index) {
                    var elementId = $(this)[0].id;

                    $("div#" + elementId + " input[id$='startDate']").attr("name", "eml.temporalCoverages[" + index + "].startDate");
                    $("div#" + elementId + " input[id$='endDate']").attr("name", "eml.temporalCoverages[" + index + "].endDate");
                    $("div#" + elementId + " input[id$='formationPeriod']").attr("name", "eml.temporalCoverages[" + index + "].formationPeriod");
                    $("div#" + elementId + " input[id$='livingTimePeriod']").attr("name", "eml.temporalCoverages[" + index + "].livingTimePeriod");
                });

                hideProcessing();
            }
        });
    </script>
    <style>
        input {
            max-height: 2.375rem !important;
        }
    </style>

    <style>
        input {
            max-height: 2.375rem !important;
        }
    </style>

    <#include "/WEB-INF/pages/macros/icon.ftl"/>

    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
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
                    <div class="alert alert-danger alert-dismissible fade show d-flex geo-error-alert" role="alert" style="display: none !important;">
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

        <div id="taxcoverage-no-available-data-warning" class="alert alert-warning alert-dismissible fade show d-flex" style="display: none !important;" role="alert">
            <div class="me-3">
                <i class="bi bi-exclamation-triangle alert-orange-2 fs-bigger-2 me-2"></i>
            </div>
            <div class="overflow-x-hidden pt-1">
                <span><@s.text name="eml.warning.reinfer"/></span>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>

                <#if (inferredMetadata.inferredTaxonomicCoverage)??>
                    <#list inferredMetadata.inferredTaxonomicCoverage.errors as error>
                        <div class="alert alert-danger alert-dismissible fade show d-flex taxonomic-error-alert" role="alert" style="display: none !important;">
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

        <div id="tempcoverage-no-available-data-warning" class="alert alert-warning alert-dismissible fade show d-flex" style="display: none !important;" role="alert">
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
                        <div class="alert alert-danger alert-dismissible fade show d-flex temporal-error-alert" role="alert" style="display: none !important;">
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

<form id="geocoverage-form" class="needs-validation track-unsaved" action="metadata-${section}.do" method="post" novalidate>
    <#include "eml_heading.ftl"/>

    <#include "metadata_section_select.ftl"/>

    <div class="container-fluid bg-body">
        <div class="container bd-layout main-content-container">
            <main class="bd-main">
                <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                    <#include "eml_sidebar.ftl"/>
                </div>

                <div class="bd-content">
                    <div class="my-md-3 p-3" id="s-geographic" data-toc="${action.getText('manage.overview.geocoverage')}">
                        <@textinline name="manage.overview.geocoverage"/>

                        <#if resource.dataPackage==false || resource.dwcDp>
                        <div class="row g-2 mt-0">
                            <div class="col-md-6">
                                <@checkbox name="resource.inferGeocoverageAutomatically" value=resource.inferGeocoverageAutomatically i18nkey="eml.inferAutomatically"/>
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
                        <#include "/WEB-INF/pages/inc/inferring_status.ftl"/>
                        </#if>

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

                    <!-- Taxonomic coverage (was a page of its own, #3319) -->
                    <div class="my-md-3 p-3" id="s-taxonomic" data-toc="${action.getText('submenu.taxcoverage')}">
                        <@textinline name="submenu.taxcoverage"/>

                        <#if resource.dataPackage==false || resource.dwcDp>
                        <div class="row g-2 mt-0">
                            <div class="col-md-6">
                                <@checkbox name="resource.inferTaxonomicCoverageAutomatically" value=resource.inferTaxonomicCoverageAutomatically i18nkey="eml.inferAutomatically"/>
                            </div>

                            <div id="taxonomic-preview-links" class="col-md-6">
                                <div class="d-flex justify-content-end">
                                    <a id="preview-inferred-taxonomic" class="metadata-action-link custom-link p-1" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name="eml.previewInferred"/></span>
                                    </a>
                                </div>
                                <div id="taxonomic-dateInferred" class="text-smaller mt-0 d-flex justify-content-end" style="display: none !important;">
                                    <span class="fs-smaller-2 p-1">${(inferredMetadata.lastModified?datetime?string.medium)!}&nbsp;</span>
                                    <a id="taxonomic-re-infer-link" href="metadata-geocoverage.do?r=${resource.shortname}&amp;reinferMetadata=true&amp;inferTaxonomicAutomatically=true#s-taxonomic" class="metadata-action-link custom-link p-1">
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
                        </#if>

                        <p class="my-3 intro">
                            <@s.text name='manage.metadata.taxcoverage.intro'/>
                        </p>

                        <div id="items">
                            <!-- Adding the taxonomic coverages that already exists on the file -->
                            <#assign next_agent_index=0 />
                            <#list eml.taxonomicCoverages as item>
                                <div id='item-${item_index}' data-ipt-item-index="${item_index}" class="item border-bottom">
                                    <div class="handle d-flex justify-content-end mt-2">
                                        <a id="removeLink-${item_index}" class="removeLink metadata-action-link custom-link" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                </svg>
                                            </span>
                                            <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.item'/></span>
                                        </a>
                                    </div>

                                    <div class="mt-2">
                                        <@text i18nkey="eml.taxonomicCoverages.description" help="i18n" name="eml.taxonomicCoverages[${item_index}].description" />
                                    </div>

                                    <!-- Taxon list-->
                                    <div class="mt-3 mb-1 fs-smaller-2 d-flex justify-content-end">
                                        <div>
                                            <a id="taxonsLink-${item_index}" class="show-taxonList mt-1 metadata-action-link custom-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                    </svg>
                                                </span>
                                                <span>
                                                    <@s.text name="manage.metadata.taxcoverage.addSeveralTaxa"/>
                                                </span>
                                            </a>
                                            <@popoverPropertyInfo "manage.metadata.taxcoverage.addSeveralTaxa.help" />
                                        </div>
                                    </div>

                                    <div id="list-${item_index}" class="half addSeveralTaxa my-2" style="display:none">
                                        <@text i18nkey="eml.taxonomicCoverages.taxonList" help="i18n" name="taxon-list-${item_index}" value="" />
                                        <div id="addSeveralTaxaButtons" class="buttons mt-2">
                                            <a href="" id="add-button-${item_index}" class="metadata-action-link custom-link">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                    </svg>
                                                </span>
                                                <span>
                                                    <@s.text name='button.add'/>
                                                </span>
                                            </a>
                                        </div>
                                    </div>
                                    <div class="row g-3">
                                        <div class="col-lg-4">
                                            <@label i18nkey="eml.taxonomicCoverages.taxonKeyword.scientificName" requiredField=true />
                                        </div>

                                        <div class="col-lg-4">
                                            <@label i18nkey="eml.taxonomicCoverages.taxonKeyword.commonName" />
                                        </div>

                                        <div class="col-lg-4">
                                            <@label i18nkey="eml.taxonomicCoverages.taxonKeyword.rank" />
                                        </div>
                                    </div>
                                    <div id="subItems-${item_index}" class="mt-0 subItems">
                                        <#if (item.taxonKeywords)??>
                                            <#list item.taxonKeywords as subItem>
                                                <div id="subItem-${item_index}-${subItem_index}" class="sub-item" data-ipt-item-index="${item_index}">
                                                    <div class="row g-3 py-1">
                                                        <div class="col-lg-4">
                                                            <@input i18nkey="eml.taxonomicCoverages.taxonKeyword.scientificName" name="eml.taxonomicCoverages[${item_index}].taxonKeywords[${subItem_index}].scientificName" value=subItem.scientificName! requiredField=true withLabel=false />
                                                        </div>

                                                        <div class="col-lg-4">
                                                            <@input i18nkey="eml.taxonomicCoverages.taxonKeyword.commonName" name="eml.taxonomicCoverages[${item_index}].taxonKeywords[${subItem_index}].commonName" value=subItem.commonName! withLabel=false />
                                                        </div>

                                                        <div class="col-lg-4 d-flex">
                                                            <div class="flex-grow-1">
                                                                <@select i18nkey="eml.taxonomicCoverages.taxonKeyword.rank" name="eml.taxonomicCoverages[${item_index}].taxonKeywords[${subItem_index}].rank" options=ranks! value="${(eml.taxonomicCoverages[item_index].taxonKeywords[subItem_index].rank)!?lower_case}" withLabel=false />
                                                            </div>

                                                            <div>
                                                                <a id="trash-${item_index}-${subItem_index}" class="removeSubEntity metadata-action-link custom-link" href="">
                                                                    <span>
                                                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                                        </svg>
                                                                    </span>
                                                                </a>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </#list>
                                        </#if>
                                    </div>
                                    <div class="pb-1 mt-1">
                                        <a id="plus-subItem-${item_index}" href="" class="metadata-action-link custom-link">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                    <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                </svg>
                                            </span>
                                            <span><@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.taxcoverage.taxon.item' /></span>
                                        </a>
                                    </div>
                                </div>
                            </#list>
                        </div>

                        <div class="addNew mt-2">
                            <a id="plus" class="plus plus-taxcoverage metadata-action-link custom-link" href="">
                                <span>
                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                    </svg>
                                </span>
                                <span><@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.taxcoverage.item' /></span>
                            </a>
                        </div>

                        <!-- Static data -->
                        <div id="static-taxanomic" class="mt-4" style="display: none;">
                            <div class="mt-2 mb-4">
                                <@text i18nkey="eml.taxonomicCoverages.description" help="i18n" name="eml.taxonomicCoverages[0].description" disabled=true />
                            </div>

                            <!-- Data is inferred, preview -->
                            <#if (inferredMetadata.inferredTaxonomicCoverage.organizedData.keywords)??>
                                <div class="table-responsive fs-smaller">
                                    <table class="table table-sm table-borderless">
                                        <#list inferredMetadata.inferredTaxonomicCoverage.organizedData.keywords as k>
                                            <#if k.rank?has_content && ranks.get(k.rank?string)?has_content && ((k.displayNames?size > 0) || (inferredMetadata.inferredTaxonomicCoverage.rankWarnings?has_content && inferredMetadata.inferredTaxonomicCoverage.rankWarnings.get(k.rank?string)?has_content)) >
                                                <tr>
                                                    <#-- 1st col, write rank name once -->
                                                    <th class="col-4">${ranks.get(k.rank)?cap_first!}</th>
                                                    <#-- 2nd col, write comma separated list of names in format: scientific name (common name) -->
                                                    <td>
                                                        <#if inferredMetadata.inferredTaxonomicCoverage.rankWarnings?has_content>
                                                            <#assign rankWarning=inferredMetadata.inferredTaxonomicCoverage.rankWarnings.get(k.rank?string)!""/>
                                                        </#if>

                                                        <#if rankWarning?has_content>
                                                            <code><@s.text name="${rankWarning}"/></code>
                                                        <#else>
                                                            <#list k.displayNames as name>
                                                                &nbsp;${name}<#if name_has_next>,</#if>
                                                            </#list>
                                                        </#if>
                                                    </td>
                                                </tr>
                                            </#if>
                                        </#list>
                                    </table>
                                </div>
                            <!-- Data infer finished, but there are errors/warnings -->
                            <#elseif (inferredMetadata.inferredTaxonomicCoverage)?? && inferredMetadata.inferredTaxonomicCoverage.errors?size != 0>
                                <#list inferredMetadata.inferredTaxonomicCoverage.errors as error>
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

                        <!-- The base form that is going to be cloned every time a user click on the 'add' link -->
                        <!-- The next divs are hidden. -->
                        <div id="baseItem" class="item clearfix" style="display:none">
                            <div class="handle d-flex justify-content-end mt-2">
                                <a id="removeLink" class="removeLink metadata-action-link custom-link" href="">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                        </svg>
                                    </span>
                                    <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.taxcoverage.item'/></span>
                                </a>
                            </div>

                            <div class="mt-2">
                                <@text i18nkey="eml.taxonomicCoverages.description" help="i18n" name="description" />
                            </div>

                            <!-- Taxon list-->
                            <div class="addNew mt-3 mb-1 fs-smaller-2 d-flex justify-content-end">
                                <a id="taxonsLink" class="show-taxonList metadata-action-link custom-link" href="">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                        </svg>
                                    </span>
                                    <span><@s.text name='manage.metadata.taxcoverage.addSeveralTaxa' /></span>
                                </a>
                            </div>
                            <div id="list" class="mt-2" style="display:none">
                                <@text i18nkey="eml.taxonomicCoverages.taxonList" help="i18n" name="taxon-list" value="" />
                                <div class="buttons taxon-list my-2">
                                    <a href="" id="add-button" class="metadata-action-link custom-link">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name="button.add"/></span>
                                    </a>
                                </div>
                            </div>
                            <div class="row g-3">
                                <div class="col-lg-4">
                                    <@label i18nkey="eml.taxonomicCoverages.taxonKeyword.scientificName" requiredField=true />
                                </div>

                                <div class="col-lg-4">
                                    <@label i18nkey="eml.taxonomicCoverages.taxonKeyword.commonName" />
                                </div>

                                <div class="col-lg-4">
                                    <@label i18nkey="eml.taxonomicCoverages.taxonKeyword.rank" />
                                </div>
                            </div>
                            <div id="subItems" class="mt-0 subItems">
                            </div>
                            <div class="addNew border-bottom pb-1 mt-1">
                                <a id="plus-subItem" href="" class="metadata-action-link custom-link">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                        </svg>
                                    </span>
                                    <span><@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.taxcoverage.taxon.item' /></span>
                                </a>
                            </div>
                        </div>

                        <div id="subItem-9999" class="sub-item" style="display:none">
                            <div class="row g-3 py-1">
                                <div class="col-lg-4">
                                    <@input i18nkey="eml.taxonomicCoverages.taxonKeyword.scientificName" name="scientificName" requiredField=true withLabel=false />
                                </div>

                                <div class="col-lg-4">
                                    <@input i18nkey="eml.taxonomicCoverages.taxonKeyword.commonName" name="commonName" withLabel=false />
                                </div>

                                <div class="col-lg-4 d-flex">
                                    <div class="flex-grow-1">
                                        <@select i18nkey="eml.taxonomicCoverages.taxonKeyword.rank"  name="rank" options=ranks! withLabel=false />
                                    </div>

                                    <div class="">
                                        <a id="trash" class="removeSubEntity metadata-action-link custom-link" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                </svg>
                                            </span>
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Temporal coverage (was a page of its own, #3319) -->
                    <div class="my-md-3 p-3" id="s-temporal" data-toc="${action.getText('submenu.tempcoverage')}">
                        <@textinline name="submenu.tempcoverage"/>

                        <#if resource.dataPackage==false || resource.dwcDp>
                        <div class="row g-2 mt-0">
                            <div class="col-md-6">
                                <@checkbox name="resource.inferTemporalCoverageAutomatically" value=resource.inferTemporalCoverageAutomatically i18nkey="eml.inferAutomatically"/>
                            </div>

                            <div id="temporal-preview-links" class="col-md-6">
                                <div class="d-flex justify-content-end">
                                    <a id="preview-inferred-temporal" class="metadata-action-link custom-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name="eml.previewInferred"/></span>
                                    </a>
                                </div>
                                <div id="temporal-dateInferred" class="text-smaller mt-0 d-flex justify-content-end" style="display: none !important;">
                                    <span class="fs-smaller-2 p-1">${(inferredMetadata.lastModified?datetime?string.medium)!}&nbsp;</span>
                                    <a id="temporal-re-infer-link" href="metadata-geocoverage.do?r=${resource.shortname}&amp;reinferMetadata=true&amp;inferTemporalAutomatically=true#s-temporal" class="metadata-action-link custom-link p-1">
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
                        </#if>

                        <p class="mb-0 my-3 intro">
                            <@s.text name='manage.metadata.tempcoverage.intro'/>
                        </p>

                        <div id="temporals">
                            <!-- Adding the temporal coverages that already exists on the file -->
                            <#assign next_agent_index=0 />
                            <#list eml.temporalCoverages as temporalCoverage>
                                <div id="temporal-${temporalCoverage_index}" class="tempo clearfix row g-3 border-bottom pb-3" >
                                    <div class="handle d-flex justify-content-end mt-4">
                                        <a id="temporal-removeLink-${temporalCoverage_index}" class="removeTemporalLink metadata-action-link custom-link" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
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
                                                    <@input type="date" i18nkey="eml.temporalCoverages.startDate" value="${temporalCoverage.startDate!?date?string('yyyy-MM-dd')}" name="eml.temporalCoverages[${temporalCoverage_index}].startDate" help="i18n"/>
                                                </div>
                                                <div class="col-lg-6">
                                                    <@input type="date" i18nkey="eml.temporalCoverages.endDate" value="${temporalCoverage.endDate!?date?string('yyyy-MM-dd')}" name="eml.temporalCoverages[${temporalCoverage_index}].endDate" help="i18n"/>
                                                </div>
                                            </div>
                                        </div>

                                    <#elseif "${temporalCoverage.type}" == "SINGLE_DATE" >
                                        <div id="single-${temporalCoverage_index}" class="typeForm col-lg-6" >
                                            <div>
                                                <#if eml.temporalCoverages[temporalCoverage_index].startDate?has_content>
                                                    <@input type="date" i18nkey="eml.temporalCoverages.singleDate" value="${temporalCoverage.startDate!?date!?string('yyyy-MM-dd')}" name="eml.temporalCoverages[${temporalCoverage_index}].startDate" help="i18n"/>
                                                <#else>
                                                    <@input type="date" i18nkey="eml.temporalCoverages.singleDate" value="" name="eml.temporalCoverages[${temporalCoverage_index}].startDate" help="i18n"/>
                                                </#if>
                                            </div>

                                        </div>

                                    <#elseif "${temporalCoverage.type}" == "FORMATION_PERIOD" >
                                        <div id="formation-${temporalCoverage_index}" class="typeForm col-lg-6" >
                                            <div>
                                                <@input i18nkey="eml.temporalCoverages.formationPeriod" name="eml.temporalCoverages[${temporalCoverage_index}].formationPeriod" value=temporalCoverage.formationPeriod! help="i18n" />
                                            </div>
                                        </div>

                                    <#else> <!-- LIVING_TIME_PERIOD -->
                                        <div id="living-${temporalCoverage_index}" class="typeForm col-lg-6"  >
                                            <div>
                                                <@input i18nkey="eml.temporalCoverages.livingTimePeriod" name="eml.temporalCoverages[${temporalCoverage_index}].livingTimePeriod" value=temporalCoverage.livingTimePeriod! help="i18n" />
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
                            <a id="plus-temporal" href="" class="metadata-action-link custom-link">
                                <span>
                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                    </svg>
                                </span>
                                <span><@s.text name='manage.metadata.addnew' /> <@s.text name='manage.metadata.tempcoverage.item' /></span>
                            </a>
                        </div>

                        <!-- The base form that is going to be cloned every time an user click in the 'add' link -->
                        <div id="base-temporal-99999" class="tempo clearfix row g-3 border-bottom pb-3" style="display:none">
                            <div class="handle d-flex justify-content-end mt-4">
                                <a id="temporal-removeLink" class="removeTemporalLink metadata-action-link custom-link" href="">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
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
                                    <@input type="date" i18nkey="eml.temporalCoverages.startDate" name="startDate" help="i18n"/>
                                </div>
                                <div class="col-lg-6">
                                    <@input type="date" i18nkey="eml.temporalCoverages.endDate" name="endDate" help="i18n"/>
                                </div>
                            </div>
                        </div>

                        <!-- SINGLE DATE -->
                        <div id="single-99999" class="typeForm col-lg-6" style="display:none">
                            <div>
                                <@input type="date" i18nkey="eml.temporalCoverages.singleDate" name="startDate" help="i18n" />
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

    <#include "eml_savebar.ftl"/>
</form>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <#include "/WEB-INF/pages/inc/footer.ftl">
