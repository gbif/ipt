/**
 * Public resource pages (portal/resource_*.ftl): copy buttons, "all versions", folded taxa chips, the geographic
 * coverage map, the temporal coverage time bar and the preview notice.
 */
(function () {
    'use strict';

    // Copy: copies the text of data-copy-target, then shows data-copied on the button for a moment
    document.querySelectorAll('[data-copy-target]').forEach(function (button) {
        button.addEventListener('click', function () {
            var target = document.querySelector(button.dataset.copyTarget);
            if (!target || !navigator.clipboard) return;
            var text = target.innerText.replace(/\s+/g, ' ').trim();
            navigator.clipboard.writeText(text).then(function () {
                var label = button.querySelector('span');
                var original = label.textContent;
                label.textContent = button.dataset.copied;
                setTimeout(function () { label.textContent = original; }, 1800);
            });
        });
    });

    // Versions: the first five are shown, the button shows the rest
    var allVersions = document.getElementById('rp-versions-all');
    if (allVersions) {
        allVersions.addEventListener('click', function () {
            document.querySelectorAll('.rp-version-more').forEach(function (li) { li.hidden = false; });
            allVersions.remove();
        });
    }

    // Taxa: the first twelve names of a rank are shown, "+n" shows the rest
    document.querySelectorAll('.rp-chip-more').forEach(function (button) {
        button.addEventListener('click', function () {
            button.parentElement.querySelectorAll('.rp-chip[hidden]').forEach(function (chip) { chip.hidden = false; });
            button.remove();
        });
    });

    // Map: one rectangle per bounding box in data-boxes, [minLat, minLng, maxLat, maxLng] (EML),
    // or a GeoJSON geometry in data-geojson (Camtrap DP)
    var mapElement = document.getElementById('rp-map');
    if (mapElement && window.L) {
        var boxes = mapElement.dataset.boxes ? JSON.parse(mapElement.dataset.boxes) : [];
        var map = L.map(mapElement, { scrollWheelZoom: false, zoomControl: true, worldCopyJump: true });
        map.attributionControl.setPrefix('<a href="https://leafletjs.com">Leaflet</a>');
        var pixelRatio = parseInt(window.devicePixelRatio) || 1;
        L.tileLayer('https://tile.gbif.org/3857/omt/{z}/{x}/{y}@' + pixelRatio + 'x.png?style=osm-bright', {
            minZoom: 1,
            maxZoom: 17,
            zoomOffset: -1,
            tileSize: 512,
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map);

        var bounds = null;
        boxes.forEach(function (b) {
            var west = b[1], east = b[3];
            // a box crossing the 180th meridian has its west edge east of its east edge
            if (west > east) east += 360;
            var rectangle = L.rectangle([[b[0], west], [b[2], east]], { color: '#3d8b3d', weight: 2, fillOpacity: 0.15 }).addTo(map);
            bounds = bounds ? bounds.extend(rectangle.getBounds()) : rectangle.getBounds();
        });
        if (mapElement.dataset.geojson) {
            var shape = L.geoJSON(JSON.parse(mapElement.dataset.geojson), {
                style: { color: '#3d8b3d', weight: 2, fillOpacity: 0.15 },
                pointToLayer: function (feature, latlng) { return L.circleMarker(latlng, { radius: 6, color: '#3d8b3d' }); }
            }).addTo(map);
            bounds = bounds ? bounds.extend(shape.getBounds()) : shape.getBounds();
        }
        if (bounds && bounds.isValid()) {
            map.fitBounds(bounds, { padding: [12, 12], maxZoom: 10 });
        } else {
            map.setView([0, 0], 1);
        }
    }

    // Time bar: one span per date range, on an axis with rounded ticks
    var timeline = document.querySelector('.rp-timeline');
    if (timeline) {
        var ranges = JSON.parse(timeline.dataset.ranges).map(function (r) {
            return [new Date(r[0]).getTime(), new Date(r[1]).getTime()];
        });
        var min = Math.min.apply(null, ranges.map(function (r) { return r[0]; }));
        var max = Math.max.apply(null, ranges.map(function (r) { return r[1]; }));
        var firstYear = new Date(min).getUTCFullYear(), lastYear = new Date(max).getUTCFullYear() + 1;
        var steps = [1, 2, 5, 10, 20, 25, 50, 100, 200, 250, 500, 1000];
        var step = steps.find(function (s) { return (lastYear - firstYear) / s <= 6; }) || 1000;
        var start = Math.floor(firstYear / step) * step, end = Math.ceil(lastYear / step) * step;
        if (end === start) end = start + step;
        var from = Date.UTC(start, 0, 1), to = Date.UTC(end, 0, 1);
        var at = function (t) { return ((t - from) / (to - from) * 100).toFixed(2) + '%'; };
        var rtl = getComputedStyle(timeline).direction === 'rtl';
        var side = rtl ? 'right' : 'left';

        var axis = document.createElement('div');
        axis.className = 'rp-axis';
        timeline.appendChild(axis);
        ranges.forEach(function (r) {
            var span = document.createElement('div');
            span.className = 'rp-span';
            span.style[side] = at(r[0]);
            span.style.width = (((r[1] - r[0]) / (to - from)) * 100).toFixed(2) + '%';
            timeline.appendChild(span);
        });
        for (var year = start; year <= end; year += step) {
            var tick = document.createElement('span');
            tick.className = 'rp-tick';
            tick.style[side] = at(Date.UTC(year, 0, 1));
            tick.textContent = year;
            timeline.appendChild(tick);
        }
    }
})();

// Preview notice: keep it right under the fixed navbar, whose height changes with the logo, the window width and
// scrolling (it shrinks once the page is scrolled)
(function () {
    var sticky = document.querySelector('.rp-preview-sticky');
    var spacer = document.querySelector('.rp-preview-spacer');
    var navbar = document.querySelector('nav.main-nav');
    if (!sticky || !navbar) return;
    // the body's top padding already leaves room under the navbar at the top of the page (where the navbar is
    // tallest), so the spacer only adds what the notice needs beyond that
    var tallestNavbar = 0;
    function update() {
        var navbarHeight = navbar.getBoundingClientRect().height;
        tallestNavbar = Math.max(tallestNavbar, navbarHeight);
        sticky.style.setProperty('--rp-navbar', navbarHeight + 'px');
        if (spacer) {
            var bodyPadding = parseFloat(getComputedStyle(document.body).paddingTop) || 0;
            spacer.style.height = Math.max(0, sticky.offsetHeight - (bodyPadding - tallestNavbar)) + 'px';
        }
    }
    update();
    if (window.ResizeObserver) {
        var observer = new ResizeObserver(update);
        observer.observe(navbar);
        observer.observe(sticky);
    } else {
        window.addEventListener('resize', update);
        window.addEventListener('scroll', update, {passive: true});
    }
})();
