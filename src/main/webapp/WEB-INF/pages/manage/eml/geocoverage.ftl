<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header-bootstrap.ftl">
<title xmlns="http://www.w3.org/1999/html"><@s.text name='manage.metadata.geocoverage.title'/></title>
    <#assign auxTopNavbar=true />
    <#assign auxTopNavbarPage = "metadata" />
    <#assign currentMenu="manage"/>

    <link rel="stylesheet" href="${baseURL}/styles/leaflet/leaflet.css" />
    <link rel="stylesheet" href="${baseURL}/styles/leaflet/locationfilter.css" />
    <script type="text/javascript" src="${baseURL}/js/leaflet/leaflet.js"></script>
    <script type="text/javascript" src="${baseURL}/js/leaflet/tile.stamen.js"></script>
    <script type="text/javascript" src="${baseURL}/js/leaflet/locationfilter.js"></script>

    <script>
        $(document).ready(function() {
            initHelp();

            var newBboxBase = "eml\\.geospatialCoverages\\[0\\]\\.boundingCoordinates\\.";
            var maxLatId = newBboxBase + "max\\.latitude";
            var minLatId = newBboxBase + "min\\.latitude";
            var maxLngId = newBboxBase + "max\\.longitude";
            var minLngId = newBboxBase + "min\\.longitude";

            var minLngValLimit = -180;
            var maxLngValLimit = 180;
            var minLatValLimit = -90;
            var maxLatValLimit = 90;

            var map = new L.map('map').setView([0, 0], 10).setMaxBounds(L.latLngBounds(L.latLng(-90, -360), L.latLng(90, 360)));

            var layer = new L.StamenTileLayer("terrain");
            map.addLayer(layer, {
                detectRetina: true
            });

            // populate coordinate fields, using min max values as defaults if none exist
            var minLngVal = isNaN(parseFloat($("#"+minLngId).val())) ? minLngValLimit : parseFloat($("#"+minLngId).val());
            var maxLngVal = isNaN(parseFloat($("#"+maxLngId).val())) ? maxLngValLimit : parseFloat($("#"+maxLngId).val());
            var minLatVal = isNaN(parseFloat($("#"+minLatId).val())) ? minLatValLimit : parseFloat($("#"+minLatId).val());
            var maxLatVal = isNaN(parseFloat($("#"+maxLatId).val())) ? maxLatValLimit : parseFloat($("#"+maxLatId).val());

            // make the location filter: a draggable/resizable rectangle
            var locationFilter = new L.LocationFilter({
                enable: true,
                enableButton: false,
                adjustButton:false,
                bounds:  L.latLngBounds(L.latLng(minLatVal, minLngVal), L.latLng(maxLatVal, maxLongitudeAdjust(maxLngVal, minLngVal)))
            }).addTo(map);

            // checks if global coverage is set. If on, coordinate input fields are hidden and the map disabled
            if (maxLatVal == maxLatValLimit && minLatVal == minLatValLimit && maxLngVal == maxLngValLimit && minLngVal == minLngValLimit) {
                $('input[name=globalCoverage]').attr('checked', true);
                $("#"+minLngId).attr("value", minLngValLimit);
                $("#"+maxLngId).attr("value", maxLngValLimit);
                $("#"+minLatId).attr("value", minLatValLimit);
                $("#"+maxLatId).attr("value", maxLatValLimit);
                $("#coordinates").slideUp('slow');
                locationFilter.disable();
                map.fitWorld();
            }

            /** This function updates the map each time the global coverage checkbox is checked or unchecked  */
            $(":checkbox").click(function() {
                if($("#globalCoverage").is(":checked")) {
                    $("#"+minLngId).attr("value", minLngValLimit);
                    $("#"+maxLngId).attr("value", maxLngValLimit);
                    $("#"+minLatId).attr("value", minLatValLimit);
                    $("#"+maxLatId).attr("value", maxLatValLimit);
                    $("#coordinates").slideUp('slow');
                    locationFilter.disable();
                    map.fitWorld();
                } else {
                    var minLngVal = parseFloat(${(eml.geospatialCoverages[0].boundingCoordinates.min.longitude)!\-180?c});
                    var maxLngVal = parseFloat(${(eml.geospatialCoverages[0].boundingCoordinates.max.longitude)!180?c});
                    var minLatVal = parseFloat(${(eml.geospatialCoverages[0].boundingCoordinates.min.latitude)!\-90?c});
                    var maxLatVal = parseFloat(${(eml.geospatialCoverages[0].boundingCoordinates.max.latitude)!90?c});
                    $("#"+minLngId).attr("value", minLngVal);
                    $("#"+maxLngId).attr("value", maxLngVal);
                    $("#"+minLatId).attr("value", minLatVal);
                    $("#"+maxLatId).attr("value", maxLatVal);
                    $("#coordinates").slideDown('slow');
                    locationFilter.enable();
                    locationFilter.setBounds(L.latLngBounds(L.latLng(minLatVal, minLngVal), L.latLng(maxLatVal, maxLongitudeAdjust(maxLngVal, minLngVal))));
                }
            });

            /** This function updates the coordinate input fields to mirror bounding box coordinates, after each map change event  */
            locationFilter.on("change", function (e) {
                $("#"+minLatId).attr("value", clamp(locationFilter.getBounds()._southWest.lat, minLatValLimit, maxLatValLimit));
                $("#"+minLngId).attr("value", datelineAdjust(locationFilter.getBounds()._southWest.lng));
                $("#"+maxLatId).attr("value", clamp(locationFilter.getBounds()._northEast.lat, minLatValLimit, maxLatValLimit));
                $("#"+maxLngId).attr("value", datelineAdjust(locationFilter.getBounds()._northEast.lng));
            });

            // lock map on disable
            locationFilter.on("disabled", function (e) {
                locationFilter.setBounds(L.latLngBounds(L.latLng(minLatVal, minLngVal), L.latLng(maxLatVal, maxLngVal)))
            });

            /**
             * Adjusts longitude with respect to dateLine.
             *
             * @param {number} lng The longitude value to adjust.
             * @returns {number} The adjusted longitude value.
             */
            function datelineAdjust(lng) {
                return ((lng+180)%360)-180;
            }

            /**
             * Function adjusts max longitude as work-around for leaflet bug occurring when rendering map with max longitude
             * smaller than min longitude.
             *
             * @param {number} maxLng The max longitude value.
             * @param {number} minLng The min longitude value.
             * @returns {number} The adjusted longitude value.
             */
            function maxLongitudeAdjust(maxLng, minLng) {
                if (maxLng < minLng) {
                    maxLng = maxLng + 360;
                }
                return maxLng;
            }

            /**
             * Restricts latitude to be between min and max. Returns min if latitude is less than min.
             * Returns max if latitude is greater than max.
             *
             * @param {number} lat The latitude value to adjust.
             * @param {number} min The minimum latitude value permitted.
             * @param {number} max The maximum latitude value permitted.
             * @returns {number} The restricted latitude value.
             */
            function clamp(lat, min, max) {
                return Math.min(Math.max(lat, min), max);
            }

            /** This function adjusts the map each time the user enters a  */
            $("#bbox input").keyup(function() {
                var minLngVal = parseFloat($("#"+minLngId).val());
                var maxLngVal = parseFloat($("#"+maxLngId).val());
                var minLatVal = parseFloat($("#"+minLatId).val());
                var maxLatVal = parseFloat($("#"+maxLatId).val());

                if(isNaN(minLngVal)) {
                    minLngVal=minLngValLimit;
                }
                if(isNaN(maxLngVal)) {
                    maxLngVal=maxLngValLimit;
                }
                if(isNaN(minLatVal)) {
                    minLatVal = minLatValLimit;
                }
                if(isNaN(maxLatVal)) {
                    maxLatVal = maxLatValLimit;
                }
                locationFilter.setBounds(L.latLngBounds(L.latLng(minLatVal, minLngVal), L.latLng(maxLatVal, maxLongitudeAdjust(maxLngVal, minLngVal))))
            });
        });
    </script>

    <style>
        #map {
            height: 320px;
            border: none;
            margin-top: 18px;
        }
    </style>

    <#include "/WEB-INF/pages/inc/menu-bootstrap.ftl">
    <#include "/WEB-INF/pages/macros/forms-bootstrap.ftl"/>

<main class="container">
    <div class="row g-3">
        <div class="p-3 bg-body rounded shadow-sm">

        <#include "/WEB-INF/pages/inc/action_alerts-bootstrap.ftl">

        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
            <@s.text name='manage.metadata.geocoverage.title'/>:
            <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
        </h5>

        <p class="text-muted mx-md-4 mx-2"><@s.text name='manage.metadata.geocoverage.intro'/></p>

        <div id="map" class="mx-md-4 mx-2"></div>

        <div id="bbox" class="row mx-md-3 mx-1 g-3">
            <div class="col-12">
                <@checkbox name="globalCoverage" help="i18n" i18nkey="eml.geospatialCoverages.globalCoverage"/>
            </div>
            <div id="coordinates" class="row g-3 mt-0">
                <div class="col-md-6">
                    <@input name="eml.geospatialCoverages[0].boundingCoordinates.min.longitude" value="${(eml.geospatialCoverages[0].boundingCoordinates.min.longitude)!}" i18nkey="eml.geospatialCoverages.boundingCoordinates.min.longitude" requiredField=true />
                </div>
                <div class="col-md-6">
                    <@input name="eml.geospatialCoverages[0].boundingCoordinates.max.longitude" value="${(eml.geospatialCoverages[0].boundingCoordinates.max.longitude)!}" i18nkey="eml.geospatialCoverages.boundingCoordinates.max.longitude" requiredField=true />
                </div>
                <div class="col-md-6">
                    <@input name="eml.geospatialCoverages[0].boundingCoordinates.min.latitude" value="${(eml.geospatialCoverages[0].boundingCoordinates.min.latitude)!}" i18nkey="eml.geospatialCoverages.boundingCoordinates.min.latitude" requiredField=true />
                </div>
                <div class="col-md-6">
                    <@input name="eml.geospatialCoverages[0].boundingCoordinates.max.latitude" value="${(eml.geospatialCoverages[0].boundingCoordinates.max.latitude)!}" i18nkey="eml.geospatialCoverages.boundingCoordinates.max.latitude" requiredField=true />
                </div>
            </div>
        </div>

        <div class="row g-3 mx-md-3 mx-1 mt-2">
            <div class="col-12">
                <@text name="eml.geospatialCoverages[0].description" value="${(eml.geospatialCoverages[0].description)!}" i18nkey="eml.geospatialCoverages.description" requiredField=true />
            </div>

            <div class="col-12">
                <@s.submit cssClass="button btn btn-outline-gbif-primary" name="save" key="button.save" />
                <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.cancel" />
            </div>
        </div>


        <!-- internal parameter -->
        <input name="r" type="hidden" value="${resource.shortname}" />
    </div>
    </div>
</main>
    </form>

    <#include "/WEB-INF/pages/inc/footer-bootstrap.ftl">
</#escape>
