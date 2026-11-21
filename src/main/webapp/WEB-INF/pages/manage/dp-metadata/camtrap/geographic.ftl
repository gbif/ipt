<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.CamtrapMetadataAction" -->
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.datapackagemetadata.camtrap.geographic.title'/></title>
    <link rel="stylesheet" href="${baseURL}/styles/leaflet/leaflet-1.9.4.css" />
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <script src="${baseURL}/js/leaflet/leaflet-1.9.4.js"></script>
    <script src="${baseURL}/js/dp-metadata.js"></script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "geographic"/>
    <#assign savebarAction = "camtrap-metadata-geographic"/>
    <#assign materialHelpIcons = true/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>
    <#include "scope_macros.ftl"/>

    <#-- a GeoJSON coordinates array as JavaScript -->
    <#macro json value><#if value?is_sequence>[<#list value as it><@json it/><#sep>,</#list>]<#elseif value?is_number>${value?c}<#else>null</#if></#macro>

    <#assign inferredScope = (inferredMetadata.inferredGeographicScope)!{}/>
    <#assign inferredOk = (inferredScope.inferred)!false && !(inferredScope.errors)?has_content/>
    <#assign spatialType = (metadata.spatial.type)!""/>
    <#assign spatialMappable = spatialType?has_content && (metadata.spatial.coordinates)?has_content
        && ["Point", "MultiPoint", "LineString", "MultiLineString", "Polygon", "MultiPolygon"]?seq_contains(spatialType?string)/>

    <#macro extent prefix>
        <dt><@s.text name="eml.geospatialCoverages.boundingCoordinates.min.longitude"/></dt><dd data-extent="${prefix}-w">-</dd>
        <dt><@s.text name="eml.geospatialCoverages.boundingCoordinates.max.longitude"/></dt><dd data-extent="${prefix}-e">-</dd>
        <dt><@s.text name="eml.geospatialCoverages.boundingCoordinates.min.latitude"/></dt><dd data-extent="${prefix}-s">-</dd>
        <dt><@s.text name="eml.geospatialCoverages.boundingCoordinates.max.latitude"/></dt><dd data-extent="${prefix}-n">-</dd>
    </#macro>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="geographic-scope-form" class="needs-validation track-unsaved" action="camtrap-metadata-${section}.do" method="post" novalidate>
        <input type="hidden" name="r" value="${resource.shortname}" />

        <#include "metadata_heading.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "metadata_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <div class="my-md-3 p-3">
                            <@scopeModes field="resource.inferGeocoverageAutomatically" page="geographic" infer=resource.inferGeocoverageAutomatically/>

                            <div id="scope-inferred" class="dp-scope" hidden>
                                <@inferBar scope=inferredScope page="geographic"/>
                            </div>

                            <div class="dp-scope dp-map-layout">
                                <div id="scope-map" class="dp-map"></div>
                                <div>
                                    <div id="values-inferred" class="dp-scope-part" hidden>
                                        <#if inferredOk>
                                            <dl class="dp-values mb-0">
                                                <dt><@s.text name="datapackagemetadata.geographic.type"/></dt><dd>Polygon</dd>
                                                <@extent prefix="inferred"/>
                                            </dl>
                                        <#else>
                                            <div class="dp-values-empty"><@s.text name="datapackagemetadata.noData"/></div>
                                        </#if>
                                    </div>
                                    <div id="values-stored" class="dp-scope-part" hidden>
                                        <dl class="dp-values mb-0" id="stored-values"<#if !spatialType?has_content> hidden</#if>>
                                            <dt><@s.text name="datapackagemetadata.geographic.type"/></dt><dd id="stored-type">${spatialType}</dd>
                                            <@extent prefix="stored"/>
                                            <#if (metadata.coordinatePrecision)??>
                                                <dt id="stored-precision-label"><@s.text name="datapackagemetadata.coordinatePrecision"/></dt><dd id="stored-precision">${metadata.coordinatePrecision}</dd>
                                            </#if>
                                        </dl>
                                        <div id="stored-none" class="dp-values-empty"<#if spatialType?has_content> hidden</#if>><@s.text name="datapackagemetadata.noData"/></div>
                                    </div>
                                </div>
                            </div>

                            <div id="scope-stored" class="dp-scope dp-geojson" hidden>
                                <label for="custom-data-textarea" class="form-label text-smaller"><@s.text name="manage.datapackagemetadata.camtrap.geographic.replace"/></label>
                                <textarea id="custom-data-textarea" name="customGeoJson" rows="4" class="form-control" placeholder="<@s.text name='datapackagemetadata.geographic.custom.input'/>">${customGeoJson!}</textarea>
                                <div class="dp-geojson-bar">
                                    <input type="file" id="custom-data-input" accept=".json,.geojson,application/json,application/geo+json" hidden>
                                    <button type="button" id="file-selector" class="btn btn-link metadata-action-link custom-link"><@icon name="upload" class="link-icon-primary"/> <span><@s.text name="basic.uploadFile"/></span></button>
                                    <span id="geojson-msg" class="dp-geojson-msg" aria-live="polite"></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>

        <#include "/WEB-INF/pages/manage/eml/eml_savebar.ftl"/>
    </form>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <#include "/WEB-INF/pages/inc/footer.ftl">

    <script>
        $(document).ready(function () {
            var INVALID = '${action.getText("datapackagemetadata.geographic.error.customGeojson")?js_string?no_esc}';
            <#if inferredOk>
            var INFERRED = {
                type: 'Polygon',
                coordinates: [[
                    [${(inferredScope.minLongitude!0)?c}, ${(inferredScope.minLatitude!0)?c}],
                    [${(inferredScope.maxLongitude!0)?c}, ${(inferredScope.minLatitude!0)?c}],
                    [${(inferredScope.maxLongitude!0)?c}, ${(inferredScope.maxLatitude!0)?c}],
                    [${(inferredScope.minLongitude!0)?c}, ${(inferredScope.maxLatitude!0)?c}],
                    [${(inferredScope.minLongitude!0)?c}, ${(inferredScope.minLatitude!0)?c}]
                ]]
            };
            <#else>
            var INFERRED = null;
            </#if>
            <#if spatialMappable>
            var STORED = {type: '${spatialType?string?js_string?no_esc}', coordinates: <@json metadata.spatial.coordinates/>};
            <#else>
            var STORED = null;
            </#if>

            var pixelRatio = parseInt(window.devicePixelRatio) || 1;
            var map = L.map('scope-map', {zoomControl: false}).setView([0, 0], 1);
            L.control.zoom({position: 'topright'}).addTo(map);
            map.attributionControl.setPrefix('<a href="https://leafletjs.com">Leaflet</a>');
            L.tileLayer('https://tile.gbif.org/3857/omt/{z}/{x}/{y}@{r}x.png?style=osm-bright'.replace('{r}', pixelRatio), {
                minZoom: 1,
                maxZoom: 17,
                zoomOffset: -1,
                tileSize: 512
            }).addTo(map);
            var layer = null;

            function round(value) {
                return Math.round(value * 1e6) / 1e6;
            }

            // draws the geometry and writes its extent into the dd elements of the part (prefix inferred or stored)
            function show(geojson, prefix) {
                if (layer) {
                    map.removeLayer(layer);
                    layer = null;
                }
                var bounds = null;
                if (geojson) {
                    try {
                        layer = L.geoJSON(geojson, {style: {color: '#4C9C2E', weight: 2, fillOpacity: .15}}).addTo(map);
                        bounds = layer.getBounds();
                    } catch (e) {
                        layer = null;
                    }
                }
                if (bounds && bounds.isValid()) {
                    map.fitBounds(bounds, {padding: [20, 20], maxZoom: 10});
                } else {
                    map.setView([0, 0], 1);
                    bounds = null;
                }
                var values = bounds ? {w: bounds.getWest(), e: bounds.getEast(), s: bounds.getSouth(), n: bounds.getNorth()} : {};
                ['w', 'e', 's', 'n'].forEach(function (side) {
                    $('[data-extent="' + prefix + '-' + side + '"]').text(bounds ? round(values[side]) : '-');
                });
                map.invalidateSize();
            }

            // pasted or uploaded GeoJSON replaces the stored geometry when saved: shown instead of it
            var $custom = $('#custom-data-textarea');
            function customGeojson() {
                var text = $custom.val().trim();
                if (!text) {
                    $('#geojson-msg').text('').removeClass('bad');
                    return null;
                }
                try {
                    var json = JSON.parse(text);
                    if (!json || typeof json.type !== 'string') {
                        throw new Error('no type');
                    }
                    $('#geojson-msg').text('').removeClass('bad');
                    return json;
                } catch (e) {
                    $('#geojson-msg').text(INVALID).addClass('bad');
                    return null;
                }
            }

            var mode;
            function render() {
                $('#values-inferred').prop('hidden', mode !== 'inferred');
                $('#values-stored').prop('hidden', mode !== 'stored');
                if (mode === 'inferred') {
                    show(INFERRED, 'inferred');
                    return;
                }
                var custom = customGeojson();
                var hasStored = ${spatialType?has_content?c};
                $('#stored-type').text(custom ? custom.type : '${spatialType?string?js_string?no_esc}');
                // the precision belongs to the stored (inferred) box, not to a replacement
                $('#stored-precision, #stored-precision-label').prop('hidden', !!custom);
                $('#stored-values').prop('hidden', !custom && !hasStored);
                $('#stored-none').prop('hidden', !!custom || hasStored);
                show(custom || STORED, 'stored');
            }

            dpScope(function (newMode) {
                mode = newMode;
                if (mode === 'inferred') {
                    // switching back to the data drops a pasted replacement, as before
                    $custom.val('');
                }
                render();
            });

            $custom.on('input', render);

            $('#file-selector').on('click', function () {
                $('#custom-data-input').trigger('click');
            });
            $('#custom-data-input').on('change', function () {
                var file = this.files[0];
                if (!file) {
                    return;
                }
                var reader = new FileReader();
                reader.onload = function (e) {
                    try {
                        $custom.val(JSON.stringify(JSON.parse(e.target.result)));
                    } catch (error) {
                        $custom.val(e.target.result);
                    }
                    $custom[0].dispatchEvent(new Event('input', {bubbles: true}));
                };
                reader.readAsText(file);
                this.value = '';
            });

            $("#re-infer-link").on('click', displayProcessing);

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
