/*
 * Core types and extensions admin page (admin/extensions.ftl): tabs, search and filter chips, the "Used by" lists,
 * and the update and remove dialogs, filled in from the button that opens them.
 */
document.addEventListener("DOMContentLoaded", function () {
    var tabs = Array.prototype.slice.call(document.querySelectorAll(".ex-tab[data-tab]"));
    var search = document.getElementById("ex-q");

    function panel(name) {
        return document.getElementById("ex-" + name);
    }

    function showTab(name, focus) {
        if (!panel(name)) return;
        tabs.forEach(function (t) {
            var on = t.dataset.tab === name;
            t.setAttribute("aria-selected", on ? "true" : "false");
            t.tabIndex = on ? 0 : -1;
            panel(t.dataset.tab).hidden = !on;
            if (on && focus) t.focus();
        });
        try { history.replaceState(null, "", "#" + name); } catch (e) { /* ignore */ }
        apply();
    }

    tabs.forEach(function (t, i) {
        t.addEventListener("click", function () { showTab(t.dataset.tab); });
        t.addEventListener("keydown", function (e) {
            var d = e.key === "ArrowRight" ? 1 : e.key === "ArrowLeft" ? -1 : 0;
            if (d) {
                e.preventDefault();
                showTab(tabs[(i + d + tabs.length) % tabs.length].dataset.tab, true);
            }
        });
    });

    // filter chips: one per panel is on
    document.querySelectorAll(".ex-panel .ex-chips").forEach(function (group) {
        group.addEventListener("click", function (e) {
            var chip = e.target.closest(".ex-chip");
            if (!chip) return;
            group.querySelectorAll(".ex-chip").forEach(function (c) { c.classList.toggle("on", c === chip); });
            apply();
        });
    });

    function matchesFilter(item, filter) {
        if (!filter || filter === "all") return true;
        if (filter === "update") return item.dataset.update === "true" || (item.dataset.filters || "").split(" ").indexOf("update") >= 0;
        return (item.dataset.filters || "").split(" ").indexOf(filter) >= 0;
    }

    // hides the rows or cards of the visible panel that don't match the search and the chip; group rows go with their rows
    function apply() {
        var q = (search && search.value || "").trim().toLowerCase();
        document.querySelectorAll(".ex-panel").forEach(function (p) {
            var chip = p.querySelector(".ex-chip.on");
            var filter = chip ? chip.dataset.filter : "all";
            var items = p.querySelectorAll(".ex-row, .ex-card");
            var shown = 0;
            items.forEach(function (item) {
                var ok = (!q || (item.dataset.search || "").indexOf(q) >= 0) && matchesFilter(item, filter);
                item.classList.toggle("ex-hidden", !ok);
                if (ok) shown++;
            });
            p.querySelectorAll("tr.ov-grp").forEach(function (g) {
                var any = false;
                for (var r = g.nextElementSibling; r && !r.classList.contains("ov-grp"); r = r.nextElementSibling) {
                    if (!r.classList.contains("ex-hidden")) { any = true; break; }
                }
                g.classList.toggle("ex-hidden", !any);
            });
            var none = p.querySelector("[data-no-match]");
            if (none) none.hidden = !(items.length && !shown);
            p.querySelectorAll("table").forEach(function (t) { t.hidden = items.length > 0 && !shown; });
        });
    }

    if (search) search.addEventListener("input", apply);

    // "Used by": the resources mapping an extension
    document.addEventListener("click", function (e) {
        var b = e.target.closest("[data-toggle-used]");
        if (!b) return;
        var list = document.getElementById(b.getAttribute("aria-controls"));
        var open = b.getAttribute("aria-expanded") !== "true";
        b.setAttribute("aria-expanded", open ? "true" : "false");
        if (list) list.hidden = !open;
    });

    function fill(template, values) {
        return template.replace(/\{(\d+)\}/g, function (m, i) { return values[i] != null ? values[i] : m; });
    }

    var updateModal = document.getElementById("ex-update-modal");
    if (updateModal) {
        updateModal.addEventListener("show.bs.modal", function (e) {
            var b = e.relatedTarget;
            if (!b) return;
            var title = updateModal.querySelector(".modal-title");
            title.textContent = fill(title.dataset.template, [b.dataset.title]);
            var versions = updateModal.querySelector("[data-versions]");
            versions.textContent = fill(versions.dataset.template, [b.dataset.from, b.dataset.to]);
            updateModal.querySelector("input[name=id]").value = b.dataset.id;
            var used = b.dataset.resources ? document.getElementById(b.dataset.resources) : null;
            var names = used ? Array.prototype.map.call(used.querySelectorAll("li"), function (li) { return li.textContent.trim(); }) : [];
            var list = updateModal.querySelector(".ex-modal-list");
            list.textContent = "";
            names.forEach(function (n) {
                var li = document.createElement("li");
                li.textContent = n;
                list.appendChild(li);
            });
            updateModal.querySelector("[data-with-resources]").hidden = !names.length;
            updateModal.querySelector("[data-without-resources]").hidden = names.length > 0;
        });
    }

    var removeModal = document.getElementById("ex-remove-modal");
    if (removeModal) {
        removeModal.addEventListener("show.bs.modal", function (e) {
            var b = e.relatedTarget;
            if (!b) return;
            var title = removeModal.querySelector(".modal-title");
            title.textContent = fill(title.dataset.template, [b.dataset.title]);
            removeModal.querySelector("input[name=id]").value = b.dataset.id;
        });
    }

    // a busy state while the server works: installing downloads the definition and its vocabularies,
    // updating migrates mappings, and synchronising downloads everything installed again
    function busy(form) {
        form.addEventListener("submit", function () {
            var button = form.querySelector("button[type=submit]");
            if (!button) return;
            var busyText = button.querySelector(".ex-busy-text"), idleText = button.querySelector(".ex-idle-text");
            if (busyText && idleText) { busyText.hidden = false; idleText.hidden = true; }
            setTimeout(function () { button.disabled = true; }, 0);
        });
    }
    document.querySelectorAll(".ex-install-form, #ex-update-modal form, #ex-remove-modal form").forEach(busy);
    var sync = document.getElementById("ex-synchronise-form");
    if (sync) {
        busy(sync);
        sync.addEventListener("submit", function () {
            var icon = sync.querySelector(".ex-sync-icon");
            if (icon) icon.classList.add("ex-spin");
        });
    }

    function fromHash() {
        var name = (location.hash || "").slice(1);
        showTab(panel(name) && tabs.some(function (t) { return t.dataset.tab === name; }) ? name : "installed");
    }
    window.addEventListener("hashchange", fromHash);
    fromHash();
});
