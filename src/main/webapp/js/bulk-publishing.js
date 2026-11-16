/*
 * Bulk publishing page (admin/bulk_publication.ftl): the scope cards and the resource table (ticks, chips, search,
 * counts, 50 rows a page), the confirmation dialog that posts the choice, and the run report
 * (admin/bulk_report.ftl), filtered and paged on the server and polled while it runs, with open logs kept.
 */
document.addEventListener("DOMContentLoaded", function () {
    var PER_PAGE = 50;
    var main = document.getElementById("bp-main");
    var setup = document.getElementById("bp-setup");
    var runSection = document.getElementById("bp-run");
    var publishBtn = document.getElementById("bp-publish");
    var newBtn = document.getElementById("bp-new");

    function esc(s) {
        return String(s).replace(/[&<>"]/g, function (c) { return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]; });
    }

    // first, last, and two either side of the current page, as the pager on the home page
    function renderPager(nav, total, cur, size) {
        if (!nav) return;
        var pages = Math.ceil(total / size);
        if (pages <= 1) { nav.innerHTML = ""; return; }
        var shown = [];
        for (var i = 1; i <= pages; i++) if (i === 1 || i === pages || Math.abs(i - cur) <= 2) shown.push(i);
        var html = '<button type="button" data-p="' + (cur - 1) + '"' + (cur === 1 ? " disabled" : "") + '>' + esc(main.dataset.prev) + '</button>';
        shown.forEach(function (p, k) {
            if (k && p - shown[k - 1] > 1) html += '<span class="bp-gap" aria-hidden="true">\u2026</span>';
            html += '<button type="button" data-p="' + p + '"' + (p === cur ? ' aria-current="page"' : "") + '>' + p + '</button>';
        });
        html += '<button type="button" data-p="' + (cur + 1) + '"' + (cur === pages ? " disabled" : "") + '>' + esc(main.dataset.next) + '</button>';
        nav.innerHTML = html;
    }

    function onPage(nav, pick) {
        if (nav) nav.addEventListener("click", function (e) {
            var b = e.target.closest("button[data-p]");
            if (b && !b.disabled) pick(parseInt(b.dataset.p, 10));
        });
    }

    /* ---------- setup ---------- */
    var rows = Array.prototype.slice.call(document.querySelectorAll("#bp-setup tr.bp-row"));
    var scope = "CHANGED";
    var chip = "included";
    var page = 1;
    var picked = {};   // SELECTED: ticked shortnames
    var excluded = {}; // EXCLUDED: unticked shortnames
    var search = document.getElementById("bp-q");
    var tickActs = document.querySelector(".bp-tick-acts");
    var tickAll = document.getElementById("bp-tick-all");
    var hint = document.getElementById("bp-list-hint");
    var setupPager = document.getElementById("bp-setup-pager");

    // flags are attributes that are there only when true
    function d(row, key) { return row.hasAttribute("data-" + key); }
    function title(row) { return row.querySelector(".bp-name a").textContent.trim(); }
    var searchText = new Map(rows.map(function (row) { return [row, (title(row) + " " + row.dataset.sn).toLowerCase()]; }));

    function inScope(row, s) {
        var sn = row.dataset.sn;
        switch (s || scope) {
            case "ALL": return true;
            case "CHANGED": return d(row, "changed");
            case "SELECTED": return !!picked[sn];
            default: return !excluded[sn];
        }
    }

    function included(row) { return inScope(row) && d(row, "ready"); }

    var chips = {
        all: function () { return true; },
        included: included,
        notReady: function (row) { return !d(row, "ready"); },
        registered: function (row) { return d(row, "registered"); },
        auto: function (row) { return d(row, "auto"); }
    };

    function setText(sel, value) {
        Array.prototype.forEach.call(document.querySelectorAll(sel), function (el) { el.textContent = value; });
    }

    function count(fn) { return rows.filter(fn).length; }

    // the rows the chip and the search match, on all pages
    function matching() {
        var q = search ? search.value.trim().toLowerCase() : "";
        return rows.filter(function (row) { return chips[chip](row) && (!q || searchText.get(row).indexOf(q) >= 0); });
    }

    function render() {
        var editable = scope === "SELECTED" || scope === "EXCLUDED";
        var matched = matching();
        var pages = Math.max(1, Math.ceil(matched.length / PER_PAGE));
        if (page > pages) page = pages;
        var onThisPage = new Set(matched.slice((page - 1) * PER_PAGE, page * PER_PAGE));
        rows.forEach(function (row) {
            var ck = row.querySelector("[data-ck]");
            ck.disabled = !editable;
            // picked by hand: the choice; otherwise: whether it will be published
            ck.checked = scope === "SELECTED" ? !!picked[row.dataset.sn] : scope === "EXCLUDED" ? !excluded[row.dataset.sn] : included(row);
            row.classList.toggle("bp-off", !included(row));
            row.classList.toggle("bp-hidden", !onThisPage.has(row));
        });
        renderPager(setupPager, matched.length, page, PER_PAGE);
        var noMatch = document.getElementById("bp-nomatch");
        if (noMatch) noMatch.hidden = matched.length > 0 || rows.length === 0;

        document.querySelectorAll(".bp-scope").forEach(function (label) {
            label.classList.toggle("on", label.querySelector("input").value === scope);
        });
        ["CHANGED", "ALL", "SELECTED", "EXCLUDED"].forEach(function (s) {
            var ready = count(function (row) { return inScope(row, s) && d(row, "ready"); });
            var notReady = count(function (row) { return inScope(row, s) && !d(row, "ready"); });
            setText('[data-scope-count="' + s + '"]', ready);
            setText('[data-scope-skip-n="' + s + '"]', notReady);
            document.querySelector('[data-scope-skip="' + s + '"]').hidden = notReady === 0;
        });
        Object.keys(chips).forEach(function (k) { setText('[data-chip-count="' + k + '"]', count(chips[k])); });
        document.querySelectorAll(".bp-chips .ex-chip").forEach(function (b) { b.classList.toggle("on", b.dataset.chip === chip); });

        if (tickActs) {
            tickActs.hidden = !editable;
            if (editable) tickAll.textContent = tickAll.dataset[scope === "SELECTED" ? "selected" : "excluded"];
        }
        if (hint) hint.textContent = hint.dataset[scope === "SELECTED" ? "selected" : scope === "EXCLUDED" ? "excluded" : "fixed"];

        var publish = rows.filter(included);
        var skip = rows.filter(function (row) { return inScope(row) && !d(row, "ready"); });
        setText('[data-sum="publish"]', publish.length);
        setText('[data-sum="skip"]', skip.length);
        if (publishBtn) publishBtn.disabled = publish.length === 0;
    }

    if (setup) {
        setup.querySelectorAll('input[name="bp-scope"]').forEach(function (input) {
            input.addEventListener("change", function () {
                scope = input.value;
                // picking by hand needs every resource; otherwise show what will be published
                chip = scope === "SELECTED" ? "all" : "included";
                page = 1;
                render();
            });
        });
        setup.querySelectorAll(".bp-chips .ex-chip").forEach(function (b) {
            b.addEventListener("click", function () { chip = b.dataset.chip; page = 1; render(); });
        });
        rows.forEach(function (row) {
            row.querySelector("[data-ck]").addEventListener("change", function (e) {
                var sn = row.dataset.sn;
                if (scope === "SELECTED") { if (e.target.checked) picked[sn] = true; else delete picked[sn]; }
                if (scope === "EXCLUDED") { if (e.target.checked) delete excluded[sn]; else excluded[sn] = true; }
                render();
            });
        });
        if (search) search.addEventListener("input", function () { page = 1; render(); });
        // every resource the chip and search match, on all pages
        if (tickAll) tickAll.addEventListener("click", function () {
            matching().forEach(function (row) {
                if (scope === "SELECTED") picked[row.dataset.sn] = true; else excluded[row.dataset.sn] = true;
            });
            render();
        });
        var reset = document.getElementById("bp-tick-reset");
        if (reset) reset.addEventListener("click", function () {
            if (scope === "SELECTED") picked = {}; else excluded = {};
            render();
        });
        onPage(setupPager, function (p) {
            page = p;
            render();
            setup.querySelector(".bp-tools").scrollIntoView({ behavior: "smooth", block: "start" });
        });
        render();
    }

    /* ---------- confirmation ---------- */
    var modalEl = document.getElementById("bp-confirm");
    var form = document.getElementById("bp-form");

    function fillList(name, list) {
        var el = modalEl.querySelector('[data-list="' + name + '"]');
        el.textContent = list.map(title).join(", ");
        document.getElementById("bp-confirm-" + name).hidden = list.length === 0;
    }

    if (publishBtn && modalEl) {
        publishBtn.addEventListener("click", function () {
            var publish = rows.filter(included);
            fillList("live", publish.filter(function (row) { return scope === "CHANGED" && d(row, "live"); }));
            fillList("skip", rows.filter(function (row) { return inScope(row) && !d(row, "ready"); }));

            // the scope and the ticked or unticked resources
            form.querySelectorAll('input[name="selectedResources"], input[name="excludedResources"]').forEach(function (i) { i.remove(); });
            form.querySelector('input[name="publishMode"]').value = scope;
            var names = scope === "SELECTED" ? Object.keys(picked) : scope === "EXCLUDED" ? Object.keys(excluded) : [];
            names.forEach(function (sn) {
                var input = document.createElement("input");
                input.type = "hidden";
                input.name = scope === "SELECTED" ? "selectedResources" : "excludedResources";
                input.value = sn;
                form.appendChild(input);
            });
            bootstrap.Modal.getOrCreateInstance(modalEl).show();
        });
        form.addEventListener("submit", function () { document.getElementById("bp-start").disabled = true; });
    }

    /* ---------- run ---------- */
    if (newBtn) newBtn.addEventListener("click", function () {
        runSection.hidden = true;
        setup.hidden = false;
        newBtn.hidden = true;
        publishBtn.hidden = false;
        render();
    });

    if (!runSection) return;
    var report = document.getElementById("bp-report");
    var rchip = "";
    var runPage = 1;
    var openLogs = {};

    // the pager under the results, from what the server rendered
    function runPager() {
        var block = document.getElementById("bp-run-block");
        if (!block) return;
        runPage = parseInt(block.dataset.page, 10);
        renderPager(document.getElementById("bp-run-pager"), parseInt(block.dataset.matched, 10), runPage, parseInt(block.dataset.size, 10));
    }

    function reportUrl(extra) {
        return runSection.dataset.url + "?filter=" + encodeURIComponent(rchip) + "&page=" + runPage
            + "&open=" + encodeURIComponent(Object.keys(openLogs).join(",")) + (extra || "");
    }

    function fetchReport(url) {
        // X-Requested-With: logged out, the server answers 401 instead of redirecting to the login page
        return fetch(url, { credentials: "same-origin", cache: "no-store", headers: { "X-Requested-With": "XMLHttpRequest" } }).then(function (res) {
            // the session expired: open the page again, which goes to the login and returns here
            if (res.status === 401) { window.location.href = runSection.dataset.reload; return null; }
            if (!res.ok) throw new Error(res.status);
            return res.text();
        });
    }

    function load() {
        return fetchReport(reportUrl()).then(function (html) {
            if (html === null) return;
            var scrolls = {};
            report.querySelectorAll(".bp-log").forEach(function (l) { scrolls[l.closest("tr").dataset.sn] = l.scrollTop; });
            report.innerHTML = html;
            report.querySelectorAll(".bp-log").forEach(function (l) {
                var sn = l.closest("tr").dataset.sn;
                if (scrolls[sn] !== undefined) l.scrollTop = scrolls[sn];
            });
            runPager();
        });
    }

    report.addEventListener("click", function (e) {
        var b = e.target.closest("[data-rchip]");
        if (b) { rchip = b.dataset.rchip; runPage = 1; load(); return; }
        var p = e.target.closest("#bp-run-pager button[data-p]");
        if (p) {
            if (!p.disabled) {
                runPage = parseInt(p.dataset.p, 10);
                load().then(function () { report.querySelector(".bp-runtable").scrollIntoView({ behavior: "smooth", block: "start" }); });
            }
            return;
        }
        var log = e.target.closest("[data-log]");
        if (log) {
            var sn = log.dataset.log;
            if (openLogs[sn]) delete openLogs[sn]; else openLogs[sn] = true;
            load();
            return;
        }
        if (e.target.closest("#bp-copy")) copyReport(e.target.closest("#bp-copy"));
    });

    // a plain-text summary of every resource in the run, for an email or an issue
    function copyReport(btn) {
        var lines = [document.querySelector("h1").textContent.trim(), report.querySelector(".bp-runtop .pub-meta").textContent.replace(/\s+/g, " ").trim(),
            report.querySelector(".bp-banner b").textContent.trim() + " " + report.querySelector(".bp-sum").textContent.replace(/\s+/g, " ").trim(), ""];
        var url = runSection.dataset.url + "?all=true";
        fetchReport(url).then(function (html) {
            if (html === null) return;
            var doc = new DOMParser().parseFromString(html, "text/html");
            doc.querySelectorAll("tr.bp-row").forEach(function (tr) {
                var v = tr.dataset.version ? " " + tr.dataset.version : "";
                lines.push(tr.dataset.state + "\t" + tr.dataset.sn + v + (tr.dataset.error ? "\t" + tr.dataset.error : ""));
            });
            return navigator.clipboard.writeText(lines.join("\n")).then(function () {
                var label = btn.lastChild.textContent;
                btn.lastChild.textContent = " " + btn.dataset.done;
                setTimeout(function () { btn.lastChild.textContent = label; }, 2000);
            });
        }).catch(function (e) { console.error("Copying the report failed", e); });
    }

    runPager();

    // poll while it runs; when it has finished, reload once so the heading and the setup view are up to date
    function poll() {
        if (runSection.dataset.running !== "true") return;
        setTimeout(function () {
            load().then(function () {
                var block = document.getElementById("bp-run-block");
                if (block && block.dataset.finished === "true") window.location.href = runSection.dataset.reload;
                else poll();
            }).catch(function () { poll(); });
        }, 2000);
    }
    poll();
});
