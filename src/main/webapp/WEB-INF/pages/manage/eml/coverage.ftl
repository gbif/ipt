<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.MetadataAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title xmlns="http://www.w3.org/1999/html"><@s.text name='manage.metadata.coverage.title'/></title>
    <#assign currentMetadataPage = "coverage"/>
    <#assign currentMenu="manage"/>

    <link rel="stylesheet" href="${baseURL}/styles/leaflet/leaflet-1.9.4.css" />
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <script src="${baseURL}/js/leaflet/leaflet-1.9.4.js"></script>
    <#include "/WEB-INF/pages/macros/popover.ftl"/>

    <#assign currentLocale = .vars["locale"]/>
    <#if currentLocale == "es" || currentLocale == "fr" || currentLocale == "ru" || currentLocale == "pt" >
        <#assign decimalDelimiter = ","/>
    <#else>
        <#assign decimalDelimiter = "."/>
    </#if>

    <#-- geographic coverage: where it comes from (inferred, drawn or entered, whole world) and one or more areas (#3317) -->
    <script>
        $(document).ready(function () {
            var DECIMAL = "${decimalDelimiter}";
            var COLORS = ["#4C9C2E", "#0078B4", "#664192", "#D1628E", "#E37C72", "#00B7EE"];
            var FIXED_COLOR = "#0078B4";
            var GLOBAL = {w: -180, e: 180, s: -90, n: 90};
            <#if (inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.min.longitude)??
                && (inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates.max.latitude)??>
            <#assign ibox = inferredMetadata.inferredGeographicCoverage.data.boundingCoordinates/>
            var INFERRED = {w: ${ibox.min.longitude?c}, e: ${ibox.max.longitude?c}, s: ${ibox.min.latitude?c}, n: ${ibox.max.latitude?c}};
            <#else>
            var INFERRED = null;
            </#if>
            var T = {
                area: "${action.getText('manage.metadata.geocoverage.area')?js_string?no_esc}",
                hintManual: "${action.getText('manage.metadata.geocoverage.hint.manual')?js_string?no_esc}",
                hintInferred: "${action.getText('manage.metadata.geocoverage.hint.inferred')?js_string?no_esc}",
                hintGlobal: "${action.getText('manage.metadata.geocoverage.hint.global')?js_string?no_esc}",
                lng: "${action.getText('validation.longitude.value')?js_string?no_esc}",
                lat: "${action.getText('validation.latitude.value')?js_string?no_esc}",
                swapped: "${action.getText('validation.coordinates.swapped.less', [action.getText('eml.geospatialCoverages.boundingCoordinates.min.latitude'), action.getText('eml.geospatialCoverages.boundingCoordinates.max.latitude')])?js_string?no_esc}",
                descriptionRequired: "${action.getText('validation.required', [action.getText('eml.geospatialCoverages.description')])?js_string?no_esc}",
                names: {
                    n: "${action.getText('eml.geospatialCoverages.boundingCoordinates.max.latitude')?js_string?no_esc}",
                    s: "${action.getText('eml.geospatialCoverages.boundingCoordinates.min.latitude')?js_string?no_esc}",
                    w: "${action.getText('eml.geospatialCoverages.boundingCoordinates.min.longitude')?js_string?no_esc}",
                    e: "${action.getText('eml.geospatialCoverages.boundingCoordinates.max.longitude')?js_string?no_esc}"
                },
                required: "${action.getText('validation.required')?js_string?no_esc}",
                invalid: "${action.getText('validation.invalid')?js_string?no_esc}"
            };
            var KEYS = ["w", "e", "s", "n"];
            var LIMIT = {w: 180, e: 180, s: 90, n: 90};

            var $form = $("#coverage-form");
            var $areas = $("#geo-areas");
            var $fixed = $("#geo-fixed");
            var $infer = $("#geo-infer");
            var mode;
            var sel = 0;

            function updateQueryParam(param, value) {
                const url = new URL(window.location.href);
                if (value === null) {
                    url.searchParams.delete(param);
                } else {
                    url.searchParams.set(param, value);
                }
                history.replaceState(null, '', url);
            }

            // the re-infer link reloads the page with inferAutomatically=true, so the mode stays "From my data"
            const urlParams = new URLSearchParams(window.location.search);
            if (urlParams.get('reinferMetadata') === 'true') {
                updateQueryParam("reinferMetadata", null);
            }
            $("#re-infer-link").on('click', displayProcessing);

            // numbers: both "," and "." are read; the form posts the locale's separator
            function num(v) {
                var t = String(v == null ? "" : v).trim().replace(",", ".");
                return t === "" ? NaN : Number(t);
            }
            function fmt(v) {
                return String(Math.round(v * 1000) / 1000).replace(".", DECIMAL);
            }
            function format(text, args) {
                return text.replace(/\{(\d)\}/g, function (m, i) { return args[i]; });
            }
            function notifyChange() {
                // inc/footer.ftl shows the save bar on input events of the form
                $form[0].dispatchEvent(new Event('input', {bubbles: true}));
            }

            // map
            var pixel_ratio = parseInt(window.devicePixelRatio) || 1;
            var map = L.map('map', {zoomControl: false, worldCopyJump: false}).setView([0, 0], 1);
            L.control.zoom({position: 'topright'}).addTo(map);
            map.attributionControl.setPrefix('<a href="https://leafletjs.com">Leaflet</a>');
            L.tileLayer('https://tile.gbif.org/3857/omt/{z}/{x}/{y}@{r}x.png?style=osm-bright'.replace('{r}', pixel_ratio), {
                minZoom: 1,
                maxZoom: 17,
                zoomOffset: -1,
                tileSize: 512
            }).addTo(map);
            var drawn = L.layerGroup().addTo(map);

            // an area's box as numbers, from its inputs
            function readBox($scope) {
                var b = {typed: {}};
                KEYS.forEach(function (k) {
                    var v = $scope.find("input[data-k=" + k + "]").val();
                    b[k] = num(v);
                    b.typed[k] = !!(v && v.trim());
                });
                return b;
            }
            function writeBox($scope, b) {
                KEYS.forEach(function (k) {
                    var $i = $scope.find("input[data-k=" + k + "]");
                    if (document.activeElement !== $i[0]) {
                        $i.val(fmt(b[k]));
                    }
                });
            }
            function isEmpty(b) {
                return KEYS.every(function (k) { return isNaN(b[k]) && !(b.typed && b.typed[k]); });
            }
            function boxErrors(b) {
                var e = {};
                KEYS.forEach(function (k) {
                    if (isNaN(b[k])) {
                        e[k] = format(b.typed && b.typed[k] ? T.invalid : T.required, [T.names[k]]);
                    } else if (Math.abs(b[k]) > LIMIT[k]) {
                        e[k] = T.names[k] + ": " + ((k === "w" || k === "e") ? T.lng : T.lat);
                    }
                });
                if (!e.s && !e.n && b.s > b.n) {
                    e.s = T.swapped;
                }
                return e;
            }
            function crosses(b) {
                return b.w > b.e;
            }
            // Leaflet bounds: a box across the 180th meridian continues east of 180
            function latLngBounds(b) {
                return L.latLngBounds([b.s, b.w], [b.n, crosses(b) ? b.e + 360 : b.e]);
            }
            function normLng(x) {
                return (x >= -180 && x <= 180) ? x : ((x + 540) % 360 + 360) % 360 - 180;
            }

            function cards() {
                return $areas.children(".geo-area");
            }
            function colorOf(i) {
                return COLORS[i % COLORS.length];
            }

            // messages under an area's coordinates; server messages (fielderror) stay until the area is edited
            function showCardState($card, live, force) {
                var b = readBox($card);
                var empty = isEmpty(b) && !force;
                var er = empty ? {} : boxErrors(b);
                KEYS.forEach(function (k) {
                    $card.find("input[data-k=" + k + "]").toggleClass("is-invalid", !!er[k]);
                });
                var $msg = $card.find(".geo-msg").empty();
                KEYS.forEach(function (k) {
                    if (er[k]) {
                        $msg.append($("<div class='text-gbif-danger'></div>").text(er[k]));
                    }
                });
                var x = !empty && !er.w && !er.e && crosses(b);
                $card.find(".geo-crosses").prop("hidden", !x);
                $card.find(".geo-crosses-help").prop("hidden", !x);
                if (live) {
                    $card.find(".geo-srv-error").remove();
                }
                markBad($card);
                return er;
            }

            function draw(fit) {
                drawn.clearLayers();
                var all = [];
                if (mode === "manual") {
                    cards().each(function (i) {
                        var $card = $(this);
                        var b = readBox($card);
                        if (isEmpty(b) || Object.keys(boxErrors(b)).length) {
                            return;
                        }
                        var c = colorOf(i);
                        var bounds = latLngBounds(b);
                        all.push(bounds);
                        var rect = L.rectangle(bounds, {color: c, weight: i === sel ? 3 : 1.5, fillOpacity: i === sel ? .18 : .06}).addTo(drawn);
                        rect.on("click", function () {
                            select(i, false);
                            // the card may be out of sight in the scrolling column next to the map
                            cards().eq(i)[0].scrollIntoView({behavior: "smooth", block: "nearest"});
                        });
                        if (i === sel) {
                            addHandles($card, b, rect, c);
                        }
                    });
                } else {
                    var fb = readBox($fixed);
                    if (!isEmpty(fb) && !Object.keys(boxErrors(fb)).length) {
                        var fbounds = latLngBounds(fb);
                        all.push(fbounds);
                        L.rectangle(fbounds, {color: FIXED_COLOR, weight: 2, fillOpacity: .1, dashArray: "6,4", interactive: false}).addTo(drawn);
                    }
                }
                if (fit) {
                    if (all.length) {
                        var ab = all[0];
                        all.forEach(function (x) { ab = ab.extend(x); });
                        map.fitBounds(ab.pad(.15), {maxZoom: 10});
                    } else {
                        map.setView([0, 0], 1);
                    }
                }
            }

            // corner handles (south-west, north-east) and a centre handle that moves the whole box
            function addHandles($card, b, rect, c) {
                function icon(cls) {
                    return L.divIcon({className: "geo-handle " + cls, iconSize: [14, 14], html: "<span style='border-color:" + c + "'></span>"});
                }
                var east = crosses(b) ? b.e + 360 : b.e;
                var sw = L.marker([b.s, b.w], {draggable: true, icon: icon("geo-corner"), keyboard: false}).addTo(drawn);
                var ne = L.marker([b.n, east], {draggable: true, icon: icon("geo-corner"), keyboard: false}).addTo(drawn);
                var mid = L.marker([(b.s + b.n) / 2, (b.w + east) / 2], {draggable: true, icon: icon("geo-centre"), keyboard: false}).addTo(drawn);

                function fromCorners() {
                    var p = sw.getLatLng(), q = ne.getLatLng();
                    var lo = Math.min(p.lng, q.lng), hi = Math.max(p.lng, q.lng);
                    var nb = {
                        s: Math.max(-90, Math.min(p.lat, q.lat)),
                        n: Math.min(90, Math.max(p.lat, q.lat)),
                        w: normLng(lo),
                        e: normLng(hi)
                    };
                    if (hi - lo >= 360) {
                        nb.w = -180;
                        nb.e = 180;
                    }
                    rect.setBounds([[nb.s, lo], [nb.n, hi]]);
                    mid.setLatLng([(nb.s + nb.n) / 2, (lo + hi) / 2]);
                    writeBox($card, nb);
                    showCardState($card, true);
                }
                sw.on("drag", fromCorners);
                ne.on("drag", fromCorners);

                var start;
                mid.on("dragstart", function () {
                    start = {c: mid.getLatLng(), p: sw.getLatLng(), q: ne.getLatLng()};
                });
                mid.on("drag", function () {
                    var now = mid.getLatLng();
                    var dLat = now.lat - start.c.lat, dLng = now.lng - start.c.lng;
                    // keep the box inside -90..90
                    dLat = Math.max(-90 - start.p.lat, Math.min(90 - start.q.lat, dLat));
                    sw.setLatLng([start.p.lat + dLat, start.p.lng + dLng]);
                    ne.setLatLng([start.q.lat + dLat, start.q.lng + dLng]);
                    fromCorners();
                });
                [sw, ne, mid].forEach(function (m) {
                    m.on("dragend", function () {
                        notifyChange();
                        draw(false);
                    });
                });
            }

            function select(i, fit) {
                sel = i;
                cards().each(function (j) { $(this).toggleClass("sel", j === i); });
                draw(false);
                if (fit) {
                    var $card = cards().eq(i);
                    var b = readBox($card);
                    if (!isEmpty(b) && !Object.keys(boxErrors(b)).length) {
                        map.fitBounds(latLngBounds(b).pad(.3), {maxZoom: 10});
                    }
                }
            }

            // names, ids and colours follow the position in the list
            function renumber() {
                cards().each(function (i) {
                    var $card = $(this);
                    $card.attr("id", "geo-area-" + i).css("--c", colorOf(i));
                    $card.find(".geo-area-title").text(format(T.area, [i + 1]));
                    $card.find("[name]").each(function () {
                        this.name = this.name.replace(/geospatialCoverages\[\d+\]/, "geospatialCoverages[" + i + "]");
                    });
                    $card.find("[id^=geo-][id*=-in-]").each(function () {
                        var k = this.id.replace(/^geo-\d+-in-/, "");
                        this.id = "geo-" + i + "-in-" + k;
                    });
                    $card.find("label[for^=geo-]").each(function () {
                        $(this).attr("for", $(this).attr("for").replace(/^geo-\d+-in-/, "geo-" + i + "-in-"));
                    });
                });
                $("#geo-none").prop("hidden", mode !== "manual" || cards().length > 0);
            }

            $("#plus-geo").on("click", function (e) {
                e.preventDefault();
                var i = cards().length;
                var html = $("#baseItem-geo").html().replace(/__i__/g, String(i));
                var $card = $(html).appendTo($areas);
                // a box in the middle of what the map shows
                var vb = map.getBounds(), c = vb.getCenter();
                var dLng = Math.min(90, (vb.getEast() - vb.getWest()) / 6), dLat = Math.min(45, (vb.getNorth() - vb.getSouth()) / 6);
                var cl = normLng(c.lng);
                var nb = {
                    w: Math.max(-180, cl - dLng),
                    e: Math.min(180, cl + dLng),
                    s: Math.max(-90, c.lat - dLat),
                    n: Math.min(90, c.lat + dLat)
                };
                writeBox($card, nb);
                renumber();
                showCardState($card, false);
                select(i, false);
                $card.find("textarea").trigger("focus");
            });

            $areas.on("click", ".removeGeoAreaLink", function (e) {
                e.preventDefault();
                var $card = $(this).closest(".geo-area");
                var i = cards().index($card);
                $card.remove();
                if (sel >= i && sel > 0) {
                    sel--;
                }
                renumber();
                select(Math.min(sel, Math.max(0, cards().length - 1)), false);
            });

            $areas.on("input", ".geo-compass input", function () {
                var $card = $(this).closest(".geo-area");
                sel = cards().index($card);
                cards().each(function (j) { $(this).toggleClass("sel", j === sel); });
                showCardState($card, true);
                draw(false);
            });
            $areas.on("focusin", ".geo-area", function () {
                var i = cards().index(this);
                if (i !== sel) {
                    select(i, false);
                }
            });
            $areas.on("click", ".geo-area-head", function (e) {
                if ($(e.target).closest("a").length) {
                    return;
                }
                select(cards().index($(this).closest(".geo-area")), true);
            });
            $areas.on("input", "textarea", function () {
                var $card = $(this).closest(".geo-area");
                $(this).removeClass("is-invalid").siblings(".geo-desc-msg").empty();
                $card.find(".geo-desc-srv-error").remove();
                $card.find(".geo-area-sub").text(this.value);
                markBad($card);
            });

            // a folded area still shows that something in it needs fixing
            function markBad($card) {
                var bad = $card.find(".is-invalid, .geo-srv-error, .geo-desc-srv-error").length > 0;
                $card.toggleClass("invalid", bad).find(".geo-area-bad").prop("hidden", !bad);
            }

            // the single area of "From my data" and "Whole world": box from the data or the whole world, own description
            function fillFixed() {
                var b = mode === "global" ? GLOBAL : INFERRED;
                var has = !!b;
                $fixed.find(".geo-fixed-box").prop("hidden", !has);
                $("#geo-fixed-none").prop("hidden", has);
                if (has) {
                    writeBox($fixed, b);
                    $fixed.find(".geo-fixed-coord").each(function () {
                        $(this).text(fmt(b[$(this).data("c")]));
                    });
                }
                // the description carries over from the first drawn area
                var $d = $fixed.find("textarea");
                if (!$d.val().trim()) {
                    $d.val(cards().first().find("textarea").val() || "");
                }
                // without a box there is nothing to post: the data is inferred when publishing
                $fixed.find("input, textarea").prop("disabled", !has);
            }

            function setMode(m, initial) {
                mode = m;
                $("#s-geographic .geo-mode").each(function () {
                    var on = $(this).data("mode") === m;
                    $(this).toggleClass("selected", on).find("input").prop("checked", on);
                });
                $infer.val(m === "inferred" ? "true" : "false");
                if (!initial) {
                    updateQueryParam("inferAutomatically", m === "inferred" ? "true" : null);
                }
                $("#geo-infer-bar").prop("hidden", m !== "inferred");
                var manual = m === "manual";
                $areas.prop("hidden", !manual).find("input, textarea").prop("disabled", !manual);
                $("#plus-geo").prop("hidden", !manual);
                $fixed.prop("hidden", manual);
                if (manual) {
                    $fixed.find("input, textarea").prop("disabled", true);
                } else {
                    fillFixed();
                }
                $("#geo-hint").text(manual ? T.hintManual : (m === "global" ? T.hintGlobal : T.hintInferred));
                renumber();
                draw(true);
            }

            $("#s-geographic .geo-mode input").on("change", function () {
                setMode($(this).val(), false);
            });

            // initial mode: inferred if turned on, whole world if the only stored area is the world
            function isGlobal(b) {
                return KEYS.every(function (k) { return b[k] === GLOBAL[k]; });
            }
            var initialMode = "manual";
            if ($infer.val() === "true" || (urlParams.get('inferAutomatically') === 'true' && $("#s-geographic .geo-mode[data-mode=inferred]").length)) {
                initialMode = "inferred";
            } else if (cards().length === 1 && isGlobal(readBox(cards().first())) && !cards().first().find(".geo-srv-error").length) {
                initialMode = "global";
            }
            cards().each(function () { showCardState($(this), false); });
            setMode(initialMode, true);
            // an area with a server error is selected first
            var $bad = cards().filter(function () { return $(this).find(".geo-srv-error, .geo-desc-srv-error").length > 0; }).first();
            select($bad.length ? cards().index($bad) : 0, false);

            function validateCoordinates(event) {
                event.preventDefault();
                var firstBad = null;
                var $scopes = mode === "manual" ? cards() : $();
                $scopes.each(function () {
                    var $card = $(this);
                    var b = readBox($card);
                    var $d = $card.find("textarea");
                    // an area left completely empty is dropped on save
                    if (isEmpty(b) && !$d.val().trim()) {
                        return;
                    }
                    var er = showCardState($card, false, true);
                    var descMissing = !$d.val().trim();
                    $d.toggleClass("is-invalid", descMissing);
                    $card.find(".geo-desc-msg").text(descMissing ? T.descriptionRequired : "");
                    markBad($card);
                    if ((Object.keys(er).length || descMissing) && firstBad === null) {
                        firstBad = $card;
                    }
                });
                if (mode !== "manual") {
                    var $fd = $fixed.find("textarea");
                    var fMissing = !$fd.prop("disabled") && !$fd.val().trim();
                    $fd.toggleClass("is-invalid", fMissing);
                    $fixed.find(".geo-desc-msg").text(fMissing ? T.descriptionRequired : "");
                    if (fMissing) {
                        firstBad = $fixed;
                    }
                }
                if (firstBad) {
                    if (firstBad.hasClass("geo-area")) {
                        select(cards().index(firstBad), true);
                    }
                    firstBad[0].scrollIntoView({behavior: "smooth", block: "center"});
                    return;
                }
                // post the locale's decimal separator
                $form.find(".geo-compass input:enabled, #geo-fixed input:enabled").each(function () {
                    this.value = this.value.trim().replace(/[.,]/, DECIMAL);
                });
                if (window.checkTaxonomicCoverage && !window.checkTaxonomicCoverage()) {
                    return;
                }
                $form.submit();
            }

            $("#savebar-save").click(function (event) {
                validateCoordinates(event);
            });
            $("#top-save").click(function (event) {
                validateCoordinates(event);
            });

            $('#metadata-section').change(function () {
                var metadataSection = $('#metadata-section').find(':selected').val()
                $(location).attr('href', 'metadata-' + metadataSection + '.do?r=${resource.shortname!r!}');
            });

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
    <#-- taxonomic coverage: where it comes from (inferred or by hand) and one or more groups of taxa -->
    <script>
        $(document).ready(function () {
            <#if (inferredMetadata.inferredTaxonomicCoverage.data.taxonKeywords)??>
            var TAX_INFERRED = [
                <#list inferredMetadata.inferredTaxonomicCoverage.data.taxonKeywords as t>
                {n: "${(t.scientificName!)?js_string?no_esc}", r: "${(t.rank!)?lower_case?js_string?no_esc}"}<#sep>,</#sep>
                </#list>
            ];
            <#else>
            var TAX_INFERRED = [];
            </#if>
            var TT = {
                group: "${action.getText('manage.metadata.taxcoverage.group')?js_string?no_esc}",
                taxa: "${action.getText('manage.metadata.taxcoverage.taxa')?js_string?no_esc}",
                duplicate: "${action.getText('manage.metadata.taxcoverage.duplicate')?js_string?no_esc}",
                sciRequired: "${action.getText('validation.required', [action.getText('eml.taxonomicCoverages.taxonKeyword.scientificName')])?js_string?no_esc}"
            };

            var $form = $("#coverage-form");
            var $section = $("#s-taxonomic");
            var $groups = $("#tax-groups");
            var $fixed = $("#tax-fixed");
            var $infer = $("#tax-infer");
            var mode;
            var sel = 0;

            function format(text, args) {
                return text.replace(/\{(\d)\}/g, function (m, i) { return args[i]; });
            }
            function notifyChange() {
                // inc/footer.ftl shows the save bar on input events of the form
                $form[0].dispatchEvent(new Event('input', {bubbles: true}));
            }
            function setUrlParam(value) {
                var url = new URL(window.location.href);
                if (value) {
                    url.searchParams.set("inferTaxonomicAutomatically", "true");
                } else {
                    url.searchParams.delete("inferTaxonomicAutomatically");
                }
                history.replaceState(null, '', url);
            }

            $("#taxonomic-re-infer-link").on('click', displayProcessing);

            function groups() {
                return $groups.children(".tax-group");
            }
            function rowsOf($g) {
                return $g.find(".tax-rows > .tax-row");
            }
            function isRowEmpty($r) {
                return !$r.find("[data-f=scientificName]").val().trim()
                    && !$r.find("[data-f=commonName]").val().trim()
                    && !$r.find("[data-f=rank]").val();
            }

            // names, ids and titles follow the position in the list
            function renumber() {
                groups().each(function (i) {
                    var $g = $(this);
                    $g.attr("id", "tax-group-" + i);
                    $g.find(".tax-group-title").text(format(TT.group, [i + 1]));
                    $g.find(".tax-description").attr({id: "tax-" + i + "-description", name: "eml.taxonomicCoverages[" + i + "].description"});
                    $g.find("label[for$=-description]").attr("for", "tax-" + i + "-description");
                    $g.find(".tax-list textarea").attr("id", "tax-" + i + "-list");
                    $g.find(".tax-list label").attr("for", "tax-" + i + "-list");
                    rowsOf($g).each(function (j) {
                        $(this).find("[data-f]").each(function () {
                            this.name = "eml.taxonomicCoverages[" + i + "].taxonKeywords[" + j + "]." + $(this).data("f");
                        });
                    });
                    refreshGroup($g);
                });
                $("#tax-none").prop("hidden", mode !== "manual" || groups().length > 0);
            }

            // count, duplicates and the folded-state warning of one group
            function refreshGroup($g) {
                var seen = {};
                var n = 0;
                rowsOf($g).each(function () {
                    var $r = $(this);
                    var name = $r.find("[data-f=scientificName]").val().trim().toLowerCase();
                    $r.removeClass("dup").removeAttr("title");
                    if (name) {
                        n++;
                        (seen[name] = seen[name] || []).push($r);
                    }
                });
                Object.keys(seen).forEach(function (k) {
                    if (seen[k].length > 1) {
                        seen[k].forEach(function ($r) { $r.addClass("dup").attr("title", TT.duplicate); });
                    }
                });
                $g.find(".tax-count").text(format(TT.taxa, [n]));
                var bad = $g.find(".is-invalid, .tax-srv-msg").length > 0;
                $g.toggleClass("invalid", bad).find(".tax-group-bad").prop("hidden", !bad);
            }

            function select(i) {
                sel = i;
                groups().each(function (j) { $(this).toggleClass("sel", j === i); });
            }

            function addRow($g, taxon) {
                var $r = $($("#baseItem-taxrow").html().replace(/__g__/g, "0").replace(/__t__/g, "0")).appendTo($g.find(".tax-rows"));
                if (taxon) {
                    $r.find("[data-f=scientificName]").val(taxon.n || "");
                    $r.find("[data-f=commonName]").val(taxon.c || "");
                    $r.find("[data-f=rank]").val(taxon.r || "");
                }
                return $r;
            }

            function addGroup(description, taxa) {
                var $g = $($("#baseItem-taxgroup").html().replace(/__g__/g, String(groups().length))).appendTo($groups);
                $g.find(".tax-description").val(description || "");
                $g.find(".tax-group-sub").text(description || "");
                if (taxa && taxa.length) {
                    taxa.forEach(function (t) { addRow($g, t); });
                } else {
                    addRow($g);
                }
                renumber();
                select(groups().length - 1);
                return $g;
            }

            $("#plus-taxgroup").on("click", function (e) {
                e.preventDefault();
                var $g = addGroup();
                $g.find(".tax-description").trigger("focus");
                notifyChange();
            });

            $groups.on("click", ".removeTaxGroupLink", function (e) {
                e.preventDefault();
                e.stopPropagation();
                var $g = $(this).closest(".tax-group");
                var i = groups().index($g);
                $g.remove();
                if (sel >= i && sel > 0) {
                    sel--;
                }
                renumber();
                select(Math.min(sel, Math.max(0, groups().length - 1)));
                notifyChange();
            });

            $groups.on("click", ".tax-group-head", function () {
                select(groups().index($(this).closest(".tax-group")));
            });
            $groups.on("focusin", ".tax-group", function () {
                var i = groups().index(this);
                if (i !== sel) {
                    select(i);
                }
            });

            $groups.on("click", ".tax-add-row", function (e) {
                e.preventDefault();
                var $g = $(this).closest(".tax-group");
                var $r = addRow($g);
                renumber();
                $r.find("[data-f=scientificName]").trigger("focus");
                notifyChange();
            });
            $groups.on("click", ".tax-remove-row", function (e) {
                e.preventDefault();
                var $g = $(this).closest(".tax-group");
                $(this).closest(".tax-row").remove();
                renumber();
                notifyChange();
            });

            // "Add several taxa": one name per line
            $groups.on("click", ".tax-show-list", function (e) {
                e.preventDefault();
                var $g = $(this).closest(".tax-group");
                $g.find(".tax-list").prop("hidden", false).find("textarea").trigger("focus");
                $g.find(".tax-actions").prop("hidden", true);
            });
            function closeList($g) {
                $g.find(".tax-list").prop("hidden", true).find("textarea").val("");
                $g.find(".tax-actions").prop("hidden", false);
            }
            $groups.on("click", ".tax-list-cancel", function () {
                closeList($(this).closest(".tax-group"));
            });
            $groups.on("click", ".tax-list-add", function () {
                var $g = $(this).closest(".tax-group");
                var lines = $g.find(".tax-list textarea").val().split("\n").map(function (l) { return l.trim(); }).filter(Boolean);
                if (lines.length) {
                    // the empty row a new group starts with is replaced
                    rowsOf($g).filter(function () { return isRowEmpty($(this)); }).remove();
                    lines.forEach(function (l) { addRow($g, {n: l}); });
                    renumber();
                    notifyChange();
                }
                closeList($g);
            });

            $groups.on("input change", ".tax-row [data-f]", function () {
                var $g = $(this).closest(".tax-group");
                $(this).removeClass("is-invalid tax-srv-error");
                if ($(this).data("f") === "scientificName") {
                    $g.find(".tax-srv-msg").remove();
                    $g.find(".tax-msg").empty();
                }
                refreshGroup($g);
            });
            $groups.on("input", ".tax-description", function () {
                $(this).closest(".tax-group").find(".tax-group-sub").text(this.value);
            });

            // "From my data": more than 24 names of a rank are folded
            $fixed.on("click", ".tax-show-all", function (e) {
                e.preventDefault();
                $(this).closest(".tax-names").addClass("all");
                $(this).remove();
            });

            // the inferred names as a new group to edit; publishing no longer replaces them
            $("#tax-copy").on("click", function (e) {
                e.preventDefault();
                var description = $fixed.find("textarea").val();
                setMode("manual", false);
                addGroup(description, TAX_INFERRED);
                notifyChange();
                groups().last()[0].scrollIntoView({behavior: "smooth", block: "nearest"});
            });

            function setMode(m, initial) {
                mode = m;
                $section.find(".tax-mode").each(function () {
                    var on = $(this).data("mode") === m;
                    $(this).toggleClass("selected", on).find("input").prop("checked", on);
                });
                $infer.val(m === "inferred" ? "true" : "false");
                if (!initial) {
                    setUrlParam(m === "inferred");
                }
                var manual = m === "manual";
                $("#tax-infer-bar").prop("hidden", manual);
                $fixed.prop("hidden", manual).find("textarea").prop("disabled", manual);
                $("#tax-manual").prop("hidden", !manual).find("input, select, textarea").prop("disabled", !manual);
                if (!manual) {
                    // the description carries over from the first group
                    var $d = $fixed.find("textarea");
                    if (!$d.val().trim()) {
                        $d.val(groups().first().find(".tax-description").val() || "");
                    }
                }
                renumber();
            }

            $section.find(".tax-mode input").on("change", function () {
                setMode($(this).val(), false);
                notifyChange();
            });

            var urlParams = new URLSearchParams(window.location.search);
            var initialMode = ($infer.val() === "true" || (urlParams.get('inferTaxonomicAutomatically') === 'true' && $section.find(".tax-mode").length))
                ? "inferred" : "manual";
            setMode(initialMode, true);
            // a group with a server error is selected first
            var $bad = groups().filter(function () { return $(this).find(".tax-srv-msg, .tax-srv-error").length > 0; }).first();
            select($bad.length ? groups().index($bad) : 0);

            // before the form is posted (geographic part, top of this file): empty rows and groups are dropped,
            // a taxon needs a scientific name
            window.checkTaxonomicCoverage = function () {
                if (mode !== "manual") {
                    return true;
                }
                rowsOf($groups).filter(function () { return isRowEmpty($(this)); }).remove();
                groups().filter(function () {
                    return rowsOf($(this)).length === 0 && !$(this).find(".tax-description").val().trim();
                }).remove();
                var firstBad = null;
                groups().each(function () {
                    var $g = $(this);
                    var missing = false;
                    rowsOf($g).each(function () {
                        var $s = $(this).find("[data-f=scientificName]");
                        if (!$s.val().trim()) {
                            $s.addClass("is-invalid");
                            missing = true;
                        }
                    });
                    $g.find(".tax-msg").text(missing ? TT.sciRequired : "");
                    if (missing && firstBad === null) {
                        firstBad = $g;
                    }
                });
                renumber();
                if (firstBad) {
                    select(groups().index(firstBad));
                    firstBad[0].scrollIntoView({behavior: "smooth", block: "center"});
                    return false;
                }
                return true;
            };
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

<form id="coverage-form" class="needs-validation track-unsaved" action="metadata-${section}.do" method="post" novalidate>
    <#include "eml_heading.ftl"/>

    <#include "metadata_section_select.ftl"/>

    <div class="container-fluid bg-body">
        <div class="container bd-layout main-content-container">
            <main class="bd-main">
                <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                    <#include "eml_sidebar.ftl"/>
                </div>

                <div class="bd-content">
                    <#-- one area of the geographic coverage: a box laid out like a compass and a description (#3317) -->
                    <#macro geoCoord index k path value="">
                        <div class="geo-${k}">
                            <label for="geo-${index}-in-${k}"><@s.text name="eml.geospatialCoverages.boundingCoordinates.${path}"/></label>
                            <input type="text" class="form-control form-control-sm" id="geo-${index}-in-${k}" data-k="${k}" inputmode="decimal" autocomplete="off"
                                   name="eml.geospatialCoverages[${index}].boundingCoordinates.${path}" value="<#if value?is_number>${value?string["0.###"]}</#if>"/>
                        </div>
                    </#macro>
                    <#macro geoArea index cov={}>
                        <div class="geo-area" id="geo-area-${index}">
                            <div class="geo-area-head">
                                <span class="geo-sw" aria-hidden="true"></span>
                                <span class="geo-area-name">
                                    <span class="geo-area-title"><@s.text name="manage.metadata.geocoverage.area"><@s.param>${index + 1}</@s.param></@s.text></span>
                                    <span class="geo-area-sub">${(cov.description)!}</span>
                                </span>
                                <span class="geo-area-bad" hidden><@icon name="error"/></span>
                                <span class="geo-crosses" hidden><@s.text name="manage.metadata.geocoverage.crosses"/></span>
                                <a href="" class="removeGeoAreaLink metadata-action-link custom-link" title="<@s.text name='manage.metadata.geocoverage.removeArea'/>" aria-label="<@s.text name='manage.metadata.geocoverage.removeArea'/>">
                                    <@icon name="delete"/>
                                </a>
                            </div>
                            <div class="geo-compass">
                                <@geoCoord index=index k="n" path="max.latitude" value=(cov.boundingCoordinates.max.latitude)!""/>
                                <@geoCoord index=index k="w" path="min.longitude" value=(cov.boundingCoordinates.min.longitude)!""/>
                                <div class="geo-mid" aria-hidden="true">WGS84</div>
                                <@geoCoord index=index k="e" path="max.longitude" value=(cov.boundingCoordinates.max.longitude)!""/>
                                <@geoCoord index=index k="s" path="min.latitude" value=(cov.boundingCoordinates.min.latitude)!""/>
                            </div>
                            <#if index?is_number>
                                <#list ["min.longitude", "max.longitude", "min.latitude", "max.latitude"] as p>
                                    <@s.fielderror cssClass="list-unstyled text-gbif-danger geo-srv-error mb-0 mt-1" fieldName="eml.geospatialCoverages[${index}].boundingCoordinates.${p}"/>
                                </#list>
                            </#if>
                            <div class="geo-msg"></div>
                            <div class="geo-crosses-help" hidden><@s.text name="manage.metadata.geocoverage.crosses.help"/></div>
                            <label class="form-label" for="geo-${index}-in-description">
                                <@s.text name="eml.geospatialCoverages.description"/> <span class="text-gbif-danger">&#42;</span>
                            </label>
                            <textarea class="form-control form-control-sm" id="geo-${index}-in-description" rows="2" name="eml.geospatialCoverages[${index}].description">${(cov.description)!}</textarea>
                            <div class="geo-desc-msg text-gbif-danger"></div>
                            <#if index?is_number>
                                <@s.fielderror cssClass="list-unstyled text-gbif-danger geo-desc-srv-error mb-0 mt-1" fieldName="eml.geospatialCoverages[${index}].description"/>
                            </#if>
                        </div>
                    </#macro>

                    <div class="my-md-3 p-3" id="s-geographic" data-toc="${action.getText('manage.overview.geocoverage')}">
                        <@textinline name="manage.overview.geocoverage"/>

                        <#assign geoCanInfer = resource.dataPackage==false || resource.dwcDp/>
                        <input type="hidden" id="geo-infer" name="resource.inferGeocoverageAutomatically" value="${(geoCanInfer && resource.inferGeocoverageAutomatically)?c}"/>

                        <div class="geo-modes mt-2" role="radiogroup" aria-label="<@s.text name='manage.metadata.geocoverage.source'/>">
                            <#if geoCanInfer>
                                <label class="geo-mode" data-mode="inferred">
                                    <input type="radio" class="form-check-input flex-shrink-0" name="geoMode" value="inferred">
                                    <span class="flex-grow-1">
                                        <span class="geo-mode-label"><@s.text name="manage.metadata.geocoverage.mode.inferred"/></span>
                                        <span class="geo-mode-desc"><@s.text name="manage.metadata.geocoverage.mode.inferred.help"/></span>
                                    </span>
                                </label>
                            </#if>
                            <label class="geo-mode" data-mode="manual">
                                <input type="radio" class="form-check-input flex-shrink-0" name="geoMode" value="manual">
                                <span class="flex-grow-1">
                                    <span class="geo-mode-label"><@s.text name="manage.metadata.geocoverage.mode.manual"/></span>
                                    <span class="geo-mode-desc"><@s.text name="manage.metadata.geocoverage.mode.manual.help"/></span>
                                </span>
                            </label>
                            <label class="geo-mode" data-mode="global">
                                <input type="radio" class="form-check-input flex-shrink-0" name="geoMode" value="global">
                                <span class="flex-grow-1">
                                    <span class="geo-mode-label"><@s.text name="manage.metadata.geocoverage.mode.global"/></span>
                                    <span class="geo-mode-desc"><@s.text name="manage.metadata.geocoverage.mode.global.help"/></span>
                                </span>
                            </label>
                        </div>

                        <#if geoCanInfer>
                            <#-- inferring failed: a warning, publishing goes on without the inferred coverage -->
                            <#assign geoInferFailed = !(inferredMetadata.inferredGeographicCoverage.data)?? && (inferredMetadata.inferredGeographicCoverage)?? && inferredMetadata.inferredGeographicCoverage.errors?size != 0/>
                            <div id="geo-infer-bar" class="<#if geoInferFailed>ov-banner warn compact cov-infer-warn<#else>geo-infer-bar</#if>" hidden>
                                <#if (inferredMetadata.inferredGeographicCoverage.data)??>
                                    <span class="flex-grow-1"><@s.text name="manage.metadata.geocoverage.inferredOn"><@s.param>${(inferredMetadata.lastModified?datetime?string.medium)!}</@s.param></@s.text></span>
                                <#elseif (inferredMetadata.inferredGeographicCoverage)?? && inferredMetadata.inferredGeographicCoverage.errors?size != 0>
                                    <span class="ov-ic"><@icon name="warning"/></span>
                                    <span class="ov-grow">
                                        <#list inferredMetadata.inferredGeographicCoverage.errors as error>
                                            <span class="d-block"><@s.text name="${error}"/></span>
                                        </#list>
                                    </span>
                                <#else>
                                    <span class="flex-grow-1"><@s.text name="eml.warning.reinfer"/></span>
                                </#if>
                                <a id="re-infer-link" href="metadata-coverage.do?r=${resource.shortname}&amp;reinferMetadata=true&amp;inferAutomatically=true" class="<#if geoInferFailed>align-self-center<#else>metadata-action-link custom-link</#if>">
                                    <#if !geoInferFailed><@icon name="autorenew" class="link-icon-primary"/> </#if><span><@s.text name="eml.reinfer"/></span>
                                </a>
                            </div>
                            <#include "/WEB-INF/pages/inc/inferring_status.ftl"/>
                        </#if>

                        <p id="geo-hint" class="geo-hint"></p>
                        <div class="geo-layout">
                            <div id="map"></div>
                            <div class="geo-side">
                                <#-- "Draw or enter": any number of areas -->
                                <div id="geo-areas" class="geo-areas">
                                    <#list eml.geospatialCoverages as cov>
                                        <@geoArea index=cov_index cov=cov/>
                                    </#list>
                                </div>
                                <p id="geo-none" class="geo-hint mt-0" hidden><@s.text name="manage.metadata.geocoverage.noArea"/></p>
                                <a id="plus-geo" href="" class="geo-add">
                                    <@icon name="add"/> <span><@s.text name="manage.metadata.geocoverage.addArea"/></span>
                                </a>
                                <template id="baseItem-geo"><@geoArea index="__i__"/></template>

                                <#-- "From my data" and "Whole world": one area, the box is given, the description is the user's -->
                                <div id="geo-fixed" class="geo-fixed" hidden>
                                    <#assign firstGeo = (eml.geospatialCoverages[0])!{}/>
                                    <p id="geo-fixed-none" class="geo-hint mb-0" hidden><@s.text name="eml.warning.reinfer"/></p>
                                    <div class="geo-fixed-box">
                                        <div class="geo-fixed-coords">
                                            <span><@s.text name="eml.geospatialCoverages.boundingCoordinates.min.longitude"/> <b class="geo-fixed-coord" data-c="w"></b></span>
                                            <span><@s.text name="eml.geospatialCoverages.boundingCoordinates.max.longitude"/> <b class="geo-fixed-coord" data-c="e"></b></span>
                                            <span><@s.text name="eml.geospatialCoverages.boundingCoordinates.min.latitude"/> <b class="geo-fixed-coord" data-c="s"></b></span>
                                            <span><@s.text name="eml.geospatialCoverages.boundingCoordinates.max.latitude"/> <b class="geo-fixed-coord" data-c="n"></b></span>
                                        </div>
                                        <input type="hidden" data-k="w" name="eml.geospatialCoverages[0].boundingCoordinates.min.longitude" disabled/>
                                        <input type="hidden" data-k="e" name="eml.geospatialCoverages[0].boundingCoordinates.max.longitude" disabled/>
                                        <input type="hidden" data-k="s" name="eml.geospatialCoverages[0].boundingCoordinates.min.latitude" disabled/>
                                        <input type="hidden" data-k="n" name="eml.geospatialCoverages[0].boundingCoordinates.max.latitude" disabled/>
                                        <label class="form-label mt-2" for="geo-fixed-description">
                                            <@s.text name="eml.geospatialCoverages.description"/> <span class="text-gbif-danger">&#42;</span>
                                        </label>
                                        <textarea class="form-control form-control-sm" id="geo-fixed-description" rows="2" name="eml.geospatialCoverages[0].description" disabled>${(firstGeo.description)!}</textarea>
                                        <div class="geo-desc-msg text-gbif-danger"></div>
                                        <@s.fielderror cssClass="list-unstyled text-gbif-danger mb-0 mt-1" fieldName="eml.geospatialCoverages[0].description"/>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- internal parameter -->
                        <input name="r" type="hidden" value="${resource.shortname}" />
                    </div>

                    <#-- one taxon of a group: scientific name, common name, rank -->
                    <#macro taxRow g t taxon={}>
                        <#local sciField = "eml.taxonomicCoverages[${g}].taxonKeywords[${t}].scientificName"/>
                        <#local taxRank = ((taxon.rank)!"")?lower_case/>
                        <tr class="tax-row">
                            <td>
                                <input type="text" class="form-control form-control-sm tax-sci<#if (action.fieldErrors[sciField])??> is-invalid tax-srv-error</#if>" data-f="scientificName" autocomplete="off"
                                       name="${sciField}" value="${(taxon.scientificName)!}" aria-label="<@s.text name='eml.taxonomicCoverages.taxonKeyword.scientificName'/>"/>
                            </td>
                            <td>
                                <input type="text" class="form-control form-control-sm" data-f="commonName" autocomplete="off"
                                       name="eml.taxonomicCoverages[${g}].taxonKeywords[${t}].commonName" value="${(taxon.commonName)!}" aria-label="<@s.text name='eml.taxonomicCoverages.taxonKeyword.commonName'/>"/>
                            </td>
                            <td>
                                <select class="form-select form-select-sm" data-f="rank" name="eml.taxonomicCoverages[${g}].taxonKeywords[${t}].rank" aria-label="<@s.text name='eml.taxonomicCoverages.taxonKeyword.rank'/>">
                                    <#list ranks?keys as rk>
                                        <option value="${rk}"<#if rk == taxRank> selected</#if>>${ranks.get(rk)?cap_first}</option>
                                    </#list>
                                </select>
                            </td>
                            <td class="tax-x">
                                <a href="" class="tax-remove-row" title="<@s.text name='manage.metadata.taxcoverage.removeTaxon'/>" aria-label="<@s.text name='manage.metadata.taxcoverage.removeTaxon'/>"><@icon name="delete"/></a>
                            </td>
                        </tr>
                    </#macro>
                    <#-- one taxonomic coverage: a description and the taxa it covers; folds to its header when not selected -->
                    <#macro taxGroup index group={}>
                        <#local groupTaxa = (group.taxonKeywords)![]/>
                        <div class="tax-group" id="tax-group-${index}">
                            <div class="tax-group-head">
                                <span class="tax-group-name">
                                    <span class="tax-group-title"><@s.text name="manage.metadata.taxcoverage.group"><@s.param>${index + 1}</@s.param></@s.text></span>
                                    <span class="tax-group-sub">${(group.description)!}</span>
                                </span>
                                <span class="tax-group-bad" hidden><@icon name="error"/></span>
                                <span class="tax-count"></span>
                                <a href="" class="removeTaxGroupLink" title="<@s.text name='manage.metadata.taxcoverage.removeGroup'/>" aria-label="<@s.text name='manage.metadata.taxcoverage.removeGroup'/>">
                                    <@icon name="delete"/>
                                </a>
                            </div>
                            <div class="tax-group-body">
                                <label class="form-label" for="tax-${index}-description"><@s.text name="eml.taxonomicCoverages.description"/></label>
                                <textarea class="form-control form-control-sm tax-description" id="tax-${index}-description" rows="2" name="eml.taxonomicCoverages[${index}].description">${(group.description)!}</textarea>

                                <table class="tax-table">
                                    <thead>
                                    <tr>
                                        <th><@s.text name="eml.taxonomicCoverages.taxonKeyword.scientificName"/> <span class="text-gbif-danger">&#42;</span></th>
                                        <th><@s.text name="eml.taxonomicCoverages.taxonKeyword.commonName"/></th>
                                        <th class="tax-rank-col"><@s.text name="eml.taxonomicCoverages.taxonKeyword.rank"/></th>
                                        <th class="tax-x"></th>
                                    </tr>
                                    </thead>
                                    <tbody class="tax-rows">
                                    <#list groupTaxa as taxon>
                                        <@taxRow g=index t=taxon_index taxon=taxon/>
                                    </#list>
                                    </tbody>
                                </table>
                                <#if index?is_number>
                                    <#list groupTaxa as taxon>
                                        <@s.fielderror cssClass="list-unstyled text-gbif-danger tax-srv-msg mb-0 mt-1" fieldName="eml.taxonomicCoverages[${index}].taxonKeywords[${taxon_index}].scientificName"/>
                                    </#list>
                                </#if>
                                <div class="tax-msg text-gbif-danger"></div>

                                <div class="tax-actions">
                                    <a href="" class="tax-add-row metadata-action-link custom-link"><@icon name="add" class="link-icon-primary"/> <span><@s.text name="manage.metadata.taxcoverage.addTaxon"/></span></a>
                                    <a href="" class="tax-show-list metadata-action-link custom-link"><@icon name="add" class="link-icon-primary"/> <span><@s.text name="manage.metadata.taxcoverage.addSeveralTaxa"/></span></a>
                                </div>
                                <div class="tax-list" hidden>
                                    <label class="form-label" for="tax-${index}-list"><@s.text name="eml.taxonomicCoverages.taxonList"/></label>
                                    <textarea class="form-control form-control-sm" id="tax-${index}-list" rows="4"></textarea>
                                    <div class="tax-list-help"><@s.text name="eml.taxonomicCoverages.taxonList.help"/> <@s.text name="manage.metadata.taxcoverage.addSeveralTaxa.help"/></div>
                                    <div class="d-flex gap-2 mt-2">
                                        <button type="button" class="btn btn-sm btn-outline-gbif-primary tax-list-add"><@s.text name="button.add"/></button>
                                        <button type="button" class="btn btn-sm btn-outline-secondary tax-list-cancel"><@s.text name="button.cancel"/></button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </#macro>

                    <!-- Taxonomic coverage (was a page of its own, #3319): where it comes from and one or more groups -->
                    <div class="my-md-3 p-3" id="s-taxonomic" data-toc="${action.getText('submenu.taxcoverage')}">
                        <@textinline name="submenu.taxcoverage"/>

                        <#assign taxCanInfer = resource.dataPackage==false || resource.dwcDp/>
                        <input type="hidden" id="tax-infer" name="resource.inferTaxonomicCoverageAutomatically" value="${(taxCanInfer && resource.inferTaxonomicCoverageAutomatically)?c}"/>

                        <#if taxCanInfer>
                            <div class="geo-modes mt-2" role="radiogroup" aria-label="<@s.text name='manage.metadata.taxcoverage.source'/>">
                                <label class="geo-mode tax-mode" data-mode="inferred">
                                    <input type="radio" class="form-check-input flex-shrink-0" name="taxMode" value="inferred">
                                    <span class="flex-grow-1">
                                        <span class="geo-mode-label"><@s.text name="manage.metadata.taxcoverage.mode.inferred"/></span>
                                        <span class="geo-mode-desc"><@s.text name="manage.metadata.taxcoverage.mode.inferred.help"/></span>
                                    </span>
                                </label>
                                <label class="geo-mode tax-mode" data-mode="manual">
                                    <input type="radio" class="form-check-input flex-shrink-0" name="taxMode" value="manual">
                                    <span class="flex-grow-1">
                                        <span class="geo-mode-label"><@s.text name="manage.metadata.taxcoverage.mode.manual"/></span>
                                        <span class="geo-mode-desc"><@s.text name="manage.metadata.taxcoverage.mode.manual.help"/></span>
                                    </span>
                                </label>
                            </div>

                            <#-- inferring failed: a warning, publishing goes on without the inferred coverage -->
                            <#assign taxInferFailed = !(inferredMetadata.inferredTaxonomicCoverage.data)?? && (inferredMetadata.inferredTaxonomicCoverage)?? && inferredMetadata.inferredTaxonomicCoverage.errors?size != 0/>
                            <div id="tax-infer-bar" class="<#if taxInferFailed>ov-banner warn compact cov-infer-warn<#else>geo-infer-bar</#if>" hidden>
                                <#if (inferredMetadata.inferredTaxonomicCoverage.data)??>
                                    <span class="flex-grow-1"><@s.text name="manage.metadata.geocoverage.inferredOn"><@s.param>${(inferredMetadata.lastModified?datetime?string.medium)!}</@s.param></@s.text></span>
                                    <a id="tax-copy" href="" class="metadata-action-link custom-link" title="<@s.text name='manage.metadata.taxcoverage.copy.help'/>">
                                        <@icon name="content_copy" class="link-icon-primary"/> <span><@s.text name="manage.metadata.taxcoverage.copy"/></span>
                                    </a>
                                <#elseif (inferredMetadata.inferredTaxonomicCoverage)?? && inferredMetadata.inferredTaxonomicCoverage.errors?size != 0>
                                    <span class="ov-ic"><@icon name="warning"/></span>
                                    <span class="ov-grow">
                                        <#list inferredMetadata.inferredTaxonomicCoverage.errors as error>
                                            <span class="d-block"><@s.text name="${error}"/></span>
                                        </#list>
                                    </span>
                                <#else>
                                    <span class="flex-grow-1"><@s.text name="eml.warning.reinfer"/></span>
                                </#if>
                                <a id="taxonomic-re-infer-link" href="metadata-coverage.do?r=${resource.shortname}&amp;reinferMetadata=true&amp;inferTaxonomicAutomatically=true#s-taxonomic" class="<#if taxInferFailed>align-self-center<#else>metadata-action-link custom-link</#if>">
                                    <#if !taxInferFailed><@icon name="autorenew" class="link-icon-primary"/> </#if><span><@s.text name="eml.reinfer"/></span>
                                </a>
                            </div>
                        </#if>

                        <#-- "From my data": the names per rank, read-only; the description is the user's -->
                        <#if taxCanInfer>
                            <div id="tax-fixed" class="tax-fixed" hidden>
                                <#if (inferredMetadata.inferredTaxonomicCoverage.organizedData.keywords)??>
                                    <#assign taxWarnings = (inferredMetadata.inferredTaxonomicCoverage.rankWarnings)!{}/>
                                    <div class="tax-ranks">
                                        <#list inferredMetadata.inferredTaxonomicCoverage.organizedData.keywords as k>
                                            <#assign rankWarning = (k.rank?has_content && taxWarnings?has_content)?then(taxWarnings.get(k.rank?string)!"", "")/>
                                            <#if k.rank?has_content && ranks.get(k.rank?string)?has_content && (k.displayNames?size > 0 || rankWarning?has_content)>
                                                <div class="tax-rank-row">
                                                    <div class="tax-rank-name">${ranks.get(k.rank)?cap_first}</div>
                                                    <div class="tax-names">
                                                        <#if rankWarning?has_content>
                                                            <span class="tax-rank-warn"><@icon name="warning"/> <@s.text name="${rankWarning}"/></span>
                                                        <#else>
                                                            <#list k.displayNames as name>
                                                                <span class="tax-chip<#if name?index gte 24> tax-chip-more</#if>">${name}</span>
                                                            </#list>
                                                            <#if k.displayNames?size gt 24>
                                                                <a href="" class="tax-show-all metadata-action-link custom-link"><@s.text name="manage.metadata.taxcoverage.showAll"/></a>
                                                            </#if>
                                                        </#if>
                                                    </div>
                                                </div>
                                            </#if>
                                        </#list>
                                    </div>
                                </#if>
                                <label class="form-label mt-3" for="tax-fixed-description"><@s.text name="eml.taxonomicCoverages.description"/></label>
                                <textarea class="form-control form-control-sm" id="tax-fixed-description" rows="2" name="eml.taxonomicCoverages[0].description" disabled>${(eml.taxonomicCoverages[0].description)!}</textarea>
                            </div>
                        </#if>

                        <#-- "Enter by hand": any number of groups -->
                        <div id="tax-manual">
                            <div id="tax-groups" class="tax-groups">
                                <#list eml.taxonomicCoverages as group>
                                    <@taxGroup index=group_index group=group/>
                                </#list>
                            </div>
                            <p id="tax-none" class="geo-hint mt-0" hidden><@s.text name="manage.metadata.taxcoverage.noGroup"/></p>
                            <a id="plus-taxgroup" href="" class="geo-add">
                                <@icon name="add"/> <span><@s.text name="manage.metadata.taxcoverage.addGroup"/></span>
                            </a>
                        </div>
                        <template id="baseItem-taxgroup"><@taxGroup index="__g__"/></template>
                        <template id="baseItem-taxrow"><@taxRow g="__g__" t="__t__"/></template>
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
                                    <a id="temporal-re-infer-link" href="metadata-coverage.do?r=${resource.shortname}&amp;reinferMetadata=true&amp;inferTemporalAutomatically=true#s-temporal" class="metadata-action-link custom-link p-1">
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
