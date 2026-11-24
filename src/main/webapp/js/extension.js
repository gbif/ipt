/*
 * Extension page (inc/extension_page.ftl), also used by the vocabulary and data package pages: sidebar scroll-spy, "Used by" show all, term search and filter chips,
 * "show all details", copy buttons, and opening at the term in the URL fragment (extension.do?id=...#<qualified name>).
 */
document.addEventListener("DOMContentLoaded", function () {
    var search = document.getElementById("ext-q");
    var chips = document.getElementById("ext-chips");
    var terms = Array.prototype.slice.call(document.querySelectorAll(".ext-term"));
    var groups = Array.prototype.slice.call(document.querySelectorAll(".ext-group"));
    // the shared scroll-spy (js/scrollspy.js), refreshed when filtering hides sections
    var spy = window.IptScrollSpy ? IptScrollSpy.init({tocSelector: ".ext-toc .sidebar-navigation-link"}) : null;

    function has(term, filter) {
        return filter === "all" || (term.dataset.filters || "").split(" ").indexOf(filter) >= 0;
    }

    // hides the terms not matching the search and the chip, then the empty groups; counts follow the search
    function apply() {
        var q = (search && search.value || "").trim().toLowerCase();
        var chip = chips && chips.querySelector(".ex-chip.on");
        var filter = chip ? chip.dataset.filter : "all";
        var shown = 0;
        terms.forEach(function (t) {
            var ok = (!q || (t.dataset.search || "").indexOf(q) >= 0) && has(t, filter);
            t.hidden = !ok;
            if (ok) shown++;
        });
        groups.forEach(function (g) {
            var n = g.querySelectorAll(".ext-term:not([hidden])").length;
            var all = g.querySelectorAll(".ext-term").length;
            g.hidden = n === 0;
            var count = g.querySelector("[data-group-count]");
            if (count) count.textContent = n === all ? all : n + " / " + all;
            var toc = document.querySelector("[data-toc-count='" + g.dataset.group + "']");
            if (toc) {
                toc.textContent = n;
                toc.parentElement.classList.toggle("ext-zero", n === 0);
            }
        });
        // wrappers around several groups (the data package page's table groups): hidden when all their groups are
        document.querySelectorAll("[data-hide-empty]").forEach(function (w) {
            w.hidden = w.querySelectorAll(".ext-term:not([hidden])").length === 0;
        });
        var tocTotal = document.querySelector("[data-toc-count='']");
        if (tocTotal) tocTotal.textContent = shown;
        if (chips) {
            chips.querySelectorAll(".ex-chip").forEach(function (c) {
                var n = terms.filter(function (t) { return (!q || (t.dataset.search || "").indexOf(q) >= 0) && has(t, c.dataset.filter); }).length;
                c.querySelector("[data-chip-count]").textContent = n;
            });
        }
        document.getElementById("ext-no-match").hidden = shown > 0;
        if (spy) spy.refresh();
    }

    if (search) search.addEventListener("input", apply);
    if (chips) {
        chips.addEventListener("click", function (e) {
            var chip = e.target.closest(".ex-chip");
            if (!chip) return;
            chips.querySelectorAll(".ex-chip").forEach(function (c) { c.classList.toggle("on", c === chip); });
            apply();
        });
    }

    // "Used by": all resources, not only the first ones
    var usageAll = document.getElementById("ext-usage-all");
    if (usageAll) {
        usageAll.addEventListener("click", function () {
            document.querySelectorAll(".ext-more-row").forEach(function (r) { r.hidden = false; });
            usageAll.remove();
            if (spy) spy.refresh();
        });
    }

    // show or hide every term's details at once
    var allDetails = document.getElementById("ext-all-details");
    if (allDetails) {
        allDetails.addEventListener("click", function () {
            var open = allDetails.dataset.open !== "true";
            document.querySelectorAll(".ext-details").forEach(function (d) { d.open = open; });
            allDetails.dataset.open = open ? "true" : "false";
            allDetails.textContent = open ? allDetails.dataset.hide : allDetails.dataset.show;
        });
    }

    // copy buttons: row type and qualified names. The Clipboard API needs HTTPS (or localhost); many IPTs run on
    // plain HTTP, so fall back to a selected text area there
    function copy(text) {
        if (navigator.clipboard && window.isSecureContext) return navigator.clipboard.writeText(text);
        var area = document.createElement("textarea");
        area.value = text;
        area.setAttribute("readonly", "");
        area.style.position = "fixed";
        area.style.opacity = "0";
        document.body.appendChild(area);
        area.select();
        var ok = document.execCommand("copy");
        area.remove();
        return ok ? Promise.resolve() : Promise.reject();
    }

    document.addEventListener("click", function (e) {
        var b = e.target.closest(".ext-copy");
        if (!b) return;
        copy(b.dataset.copy).then(function () {
            var title = b.title;
            b.classList.add("ext-copied");
            b.title = b.dataset.copied;
            setTimeout(function () { b.classList.remove("ext-copied"); b.title = title; }, 1500);
        }, function () { /* nothing copied: the button stays as it was */ });
    });

    // opened at a term: make sure it is shown, open its details and bring it into view
    function fromHash() {
        if (!terms.length) return;
        var id;
        try { id = decodeURIComponent((location.hash || "").slice(1)); } catch (err) { return; }
        var term = id && document.getElementById(id);
        if (!term || !term.classList.contains("ext-term")) return;
        if (term.hidden) {
            if (search) search.value = "";
            if (chips) chips.querySelectorAll(".ex-chip").forEach(function (c) { c.classList.toggle("on", c.dataset.filter === "all"); });
            apply();
        }
        terms.forEach(function (t) { t.classList.toggle("ext-here", t === term); });
        var details = term.querySelector(".ext-details");
        if (details) details.open = true;
        term.scrollIntoView({block: "center"});
    }
    window.addEventListener("hashchange", fromHash);
    fromHash();
});
