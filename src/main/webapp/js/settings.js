/*
 * IPT settings page (admin/config.ftl): unsaved-changes bar, notes that follow the fields (public URL change, failure
 * emails, old Google Analytics IDs), the saved SMTP password, archiving options, the server location map, copy
 * buttons, checks before Save and a confirmation before a public URL change logs the administrator out.
 */
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("settings-form");
    if (!form) return;
    const text = form.dataset;
    const $ = (id) => document.getElementById(id);

    // --- unsaved changes
    // the password field only counts once something is typed in it
    const snapshot = () => JSON.stringify(Array.from(new FormData(form).entries())
        .filter(([k, v]) => k !== "save" && !(k === "mailSmtpPassword" && v === "")));
    const initial = snapshot();
    // a save the server refused: the values on the page are not saved
    const refused = text.unsaved === "true";
    const savebar = $("st-savebar");
    let leaving = false;
    let urlConfirmed = false;

    const isDirty = () => refused || snapshot() !== initial;
    function updateSavebar() {
        const dirty = isDirty();
        savebar.classList.toggle("show", dirty);
        savebar.setAttribute("aria-hidden", dirty ? "false" : "true");
    }

    // --- public URL: what a change does, before Save
    const baseUrl = $("baseUrl");
    const noSlash = (value) => value.trim().replace(/\/+$/, "");
    const savedUrl = noSlash(text.savedUrl || "");
    const isLocalhost = (value) => {
        try {
            const host = new URL(value).hostname.toLowerCase();
            return host === "localhost" || host === "127.0.0.1";
        } catch (e) {
            return false;
        }
    };
    const urlChanged = () => noSlash(baseUrl.value) !== savedUrl;
    function updateUrlNotes() {
        $("st-url-note").hidden = !urlChanged();
        $("st-url-localhost").hidden = !(text.production === "true" && urlChanged() && isLocalhost(baseUrl.value.trim()));
    }

    // --- failure emails go out with an SMTP host and an administrator email
    const state = $("st-mail-state");
    function updateMailState() {
        const on = $("mailSmtpHost").value.trim() !== "" && $("adminEmail").value.trim() !== "";
        state.querySelector(".st-state-on").hidden = !on;
        state.querySelector(".st-state-off").hidden = on;
    }

    // --- the saved SMTP password: keep, change or remove
    const pw = $("mailSmtpPassword");
    const pwRemove = $("mailSmtpPasswordRemove");
    const pwSaved = $("st-pw-saved");
    const pwRemoved = $("st-pw-removed");
    if (pwSaved) {
        $("st-pw-change").addEventListener("click", () => {
            pwSaved.hidden = true;
            pw.hidden = false;
            pw.disabled = false;
            pw.focus();
        });
        $("st-pw-remove").addEventListener("click", () => {
            pwRemove.value = "true";
            pwSaved.hidden = true;
            pwRemoved.hidden = false;
            updateSavebar();
        });
        $("st-pw-undo").addEventListener("click", () => {
            pwRemove.value = "false";
            pwRemoved.hidden = true;
            pwSaved.hidden = false;
            updateSavebar();
        });
    }

    // --- archiving: how many versions only matters while versions are kept
    const archivalMode = $("archivalMode");
    archivalMode.addEventListener("change", () => $("st-keep").disabled = !archivalMode.checked);
    $("archivalLimit").addEventListener("focus", () => $("archivalKeepLast").checked = true);

    // --- Google Analytics: GA4 ignores the old UA- IDs
    const analytics = $("analyticsKey");
    const updateUa = () => $("st-ua-note").hidden = !/^UA-/i.test(analytics.value.trim());

    // --- server location
    const lat = $("latitude");
    const lon = $("longitude");
    const num = (input) => {
        const v = input.value.trim();
        return v === "" || isNaN(Number(v)) ? null : Number(v);
    };
    const validLocation = () => {
        const a = num(lat), b = num(lon);
        return a !== null && b !== null && a >= -90 && a <= 90 && b >= -180 && b <= 180 ? [a, b] : null;
    };
    let map = null, marker = null;
    const mapEl = $("st-map");
    if (window.L && mapEl) {
        const ratio = parseInt(window.devicePixelRatio) || 1;
        map = L.map(mapEl, {zoomControl: false, worldCopyJump: true})
            .setMaxBounds(L.latLngBounds(L.latLng(-90, -180), L.latLng(90, 180)));
        L.control.zoom({position: "topright"}).addTo(map);
        map.attributionControl.setPrefix('<a href="https://leafletjs.com">Leaflet</a>');
        L.tileLayer(mapEl.dataset.tiles.replace("{r}", ratio), {minZoom: 1, maxZoom: 17, zoomOffset: -1, tileSize: 512}).addTo(map);
        L.Icon.Default.imagePath = mapEl.dataset.images;
        const start = validLocation();
        map.setView(start || [20, 0], start ? 5 : 1);
        map.on("click", (e) => {
            const p = e.latlng.wrap();
            lat.value = p.lat.toFixed(4);
            lon.value = p.lng.toFixed(4);
            clearError(lat);
            clearError(lon);
            updateMarker(false);
            updateSavebar();
        });
    }
    function updateMarker(pan) {
        if (!map) return;
        const p = validLocation();
        if (!p) {
            if (marker) {
                marker.remove();
                marker = null;
            }
            return;
        }
        if (!marker) marker = L.marker(p).addTo(map);
        else marker.setLatLng(p);
        if (pan) map.setView(p, Math.max(map.getZoom(), 4));
    }
    // "52.16, 4.49" pasted into latitude fills both; "52,16" (a decimal comma) is left alone
    lat.addEventListener("input", () => {
        const m = lat.value.match(/^\s*(-?\d+(?:\.\d+)?)(?:\s*[,;]\s+|\s*;\s*|\s+)(-?\d+(?:\.\d+)?)\s*$/);
        if (m) {
            lat.value = m[1];
            lon.value = m[2];
        }
    });
    [lat, lon].forEach((input) => input.addEventListener("input", () => updateMarker(true)));
    $("st-clear-location").addEventListener("click", () => {
        lat.value = "";
        lon.value = "";
        clearError(lat);
        clearError(lon);
        updateMarker(false);
        if (map) map.setView([20, 0], 1);
        updateSavebar();
    });

    // --- copy buttons. The Clipboard API needs HTTPS (or localhost); many IPTs run on plain HTTP
    function copy(value) {
        if (navigator.clipboard && window.isSecureContext) return navigator.clipboard.writeText(value);
        const area = document.createElement("textarea");
        area.value = value;
        area.setAttribute("readonly", "");
        area.style.position = "fixed";
        area.style.opacity = "0";
        document.body.appendChild(area);
        area.select();
        const ok = document.execCommand("copy");
        area.remove();
        return ok ? Promise.resolve() : Promise.reject();
    }
    document.addEventListener("click", (e) => {
        const b = e.target.closest(".st-copy");
        if (!b) return;
        copy(b.dataset.copy).then(() => {
            const title = b.title;
            b.classList.add("st-copied");
            b.title = text.textCopied;
            setTimeout(() => { b.classList.remove("st-copied"); b.title = title; }, 1500);
        }, () => { /* nothing copied */ });
    });

    // --- checks before Save; the server checks the same again
    function showError(input, message) {
        input.classList.add("is-invalid");
        let msg = input.parentElement.querySelector(".st-js-error");
        if (!msg) {
            msg = document.createElement("div");
            msg.className = "invalid-feedback d-block st-js-error";
            input.insertAdjacentElement("afterend", msg);
        }
        msg.textContent = message;
    }
    function clearError(input) {
        input.classList.remove("is-invalid");
        const msg = input.parentElement.querySelector(".st-js-error");
        if (msg) msg.remove();
    }
    form.querySelectorAll("input").forEach((input) => input.addEventListener("input", () => clearError(input)));

    function check() {
        const bad = [];
        let url = null;
        try { url = new URL(baseUrl.value.trim()); } catch (e) { /* not a URL */ }
        if (!url || !/^https?:$/.test(url.protocol)) {
            showError(baseUrl, text.textUrl);
            bad.push(baseUrl);
        }
        const a = lat.value.trim(), b = lon.value.trim();
        if ((a === "") !== (b === "")) {
            const empty = a === "" ? lat : lon;
            showError(empty, text.textBoth);
            bad.push(empty);
        } else if (a !== "") {
            const la = num(lat), lo = num(lon);
            if (la === null || la < -90 || la > 90) { showError(lat, text.textLat); bad.push(lat); }
            if (lo === null || lo < -180 || lo > 180) { showError(lon, text.textLon); bad.push(lon); }
        }
        const limit = $("archivalLimit");
        if (archivalMode.checked && $("archivalKeepLast").checked && !/^\s*[1-9]\d*\s*$/.test(limit.value)) {
            showError(limit, text.textLimit);
            bad.push(limit);
        }
        return bad;
    }

    form.addEventListener("submit", (e) => {
        const bad = check();
        if (bad.length) {
            e.preventDefault();
            bad[0].closest(".ov-card").scrollIntoView({behavior: "smooth", block: "start"});
            bad[0].focus({preventScroll: true});
            return;
        }
        // a new public URL logs the administrator out: ask first
        if (urlChanged() && !urlConfirmed && window.IPT && IPT.confirm) {
            e.preventDefault();
            const submitter = e.submitter;
            IPT.confirm({tone: "warn", title: text.textUrlTitle, text: text.textUrlText, confirm: text.textUrlConfirm})
                .then((ok) => {
                    if (!ok) return;
                    urlConfirmed = true;
                    leaving = true;
                    if (form.requestSubmit) form.requestSubmit(submitter); else form.submit();
                });
            return;
        }
        leaving = true;
    });
    $("st-discard").addEventListener("click", () => leaving = true);
    window.addEventListener("beforeunload", (e) => {
        if (!leaving && isDirty()) {
            e.preventDefault();
            e.returnValue = text.textLeave;
        }
    });

    form.addEventListener("input", () => {
        updateUrlNotes();
        updateMailState();
        updateUa();
        updateSavebar();
    });
    form.addEventListener("change", updateSavebar);

    updateUrlNotes();
    updateMailState();
    updateUa();
    updateMarker(false);
    updateSavebar();
});
