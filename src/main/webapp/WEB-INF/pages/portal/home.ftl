<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>
<link rel="stylesheet" href="${baseURL}/styles/portal-home.css">

<div class="container">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<main class="container main-content-container border rounded my-4">
    <div class="py-md-4 py-3 px-md-5 px-3">
        <#-- no visible title: the filters, search and count say what the page is; the heading stays for screen readers
             and search engines -->
        <h1 class="visually-hidden"><@s.text name="portal.home.title"/></h1>
        <#if (resourcesSize>0)>
            <div class="ph-layout" id="ph">
                <aside aria-label="<@s.text name="portal.home.filters"/>">
                    <details class="ph-filters" id="ph-filters" open>
                        <summary><@s.text name="portal.home.filters"/></summary>
                        <#-- the values are written by the script below, from /api/home-resources -->
                        <section class="ph-facet" data-facet="types" hidden><h2><@s.text name="portal.home.type"/></h2><div class="ph-values"></div></section>
                        <section class="ph-facet" data-facet="organisations" hidden><h2><@s.text name="portal.home.organisation"/></h2><div class="ph-values"></div></section>
                        <section class="ph-facet" data-facet="licences" hidden><h2><@s.text name="portal.resource.license"/></h2><div class="ph-values"></div></section>
                        <section class="ph-facet" data-facet="updated" hidden><h2><@s.text name="portal.home.last.publication"/></h2><div class="ph-values"></div></section>
                    </details>
                    <div class="ph-machines">
                        <h2><@s.text name="portal.home.machines"/></h2>
                        <a href="${baseURL}/rss.do"><@s.text name="portal.home.machines.rss"/></a>
                        <a href="${baseURL}/dcat"><@s.text name="portal.home.machines.dcat"/></a>
                        <a href="${baseURL}/inventory/dataset"><@s.text name="portal.home.machines.inventory"/></a>
                    </div>
                </aside>

                <div>
                    <form class="ph-search" id="ph-search-form" role="search">
                        <input type="search" id="ph-q" class="form-control form-control-sm" placeholder="<@s.text name="portal.home.search.placeholder"/>" aria-label="<@s.text name="portal.home.search.placeholder"/>" autocomplete="off">
                    </form>
                    <div class="ph-chips" id="ph-chips"></div>
                    <div class="ph-bar">
                        <span class="ph-count" id="ph-count" aria-live="polite"></span>
                        <label><@s.text name="portal.home.sort"/>
                            <select id="ph-sort" class="form-select form-select-sm">
                                <option value="published"><@s.text name="portal.home.sort.published"/></option>
                                <option value="title"><@s.text name="portal.home.sort.title"/></option>
                                <option value="records"><@s.text name="portal.home.sort.records"/></option>
                            </select>
                        </label>
                        <span class="ph-seg" role="group" aria-label="<@s.text name="portal.home.view"/>">
                            <button type="button" data-view="list" aria-pressed="true"><@s.text name="portal.home.view.list"/></button><button type="button" data-view="table" aria-pressed="false"><@s.text name="portal.home.view.table"/></button>
                        </span>
                    </div>
                    <div class="ph-results" id="ph-results"></div>
                    <nav class="ph-pager" id="ph-pager" aria-label="<@s.text name="portal.home.pages"/>"></nav>
                </div>
            </div>

            <#-- texts for the script; {0} and {1} are filled in there -->
            <template id="ph-texts">
                <span data-k="count"><@s.text name="portal.home.count"><@s.param>{0}</@s.param><@s.param>{1}</@s.param></@s.text></span>
                <span data-k="countAll"><@s.text name="portal.home.count.all"><@s.param>{0}</@s.param></@s.text></span>
                <span data-k="remove"><@s.text name="portal.home.filter.remove"><@s.param>{0}</@s.param></@s.text></span>
                <span data-k="clear"><@s.text name="portal.home.filter.clear"/></span>
                <span data-k="noMatch"><@s.text name="portal.home.noMatch"/></span>
                <span data-k="noMatchHint"><@s.text name="portal.home.noMatch.hint"/></span>
                <span data-k="error"><@s.text name="portal.home.error"/></span>
                <span data-k="records"><@s.text name="portal.home.records.unit"/></span>
                <span data-k="metadataOnly"><@s.text name="portal.home.metadataOnly"/></span>
                <span data-k="noLogo"><@s.text name="portal.home.noLogo"/></span>
                <span data-k="onGbif"><@s.text name="portal.home.onGbif"/></span>
                <span data-k="name"><@s.text name="portal.home.name"/></span>
                <span data-k="organisation"><@s.text name="portal.home.organisation"/></span>
                <span data-k="type"><@s.text name="portal.home.type"/></span>
                <span data-k="licence"><@s.text name="portal.resource.license"/></span>
                <span data-k="recordsTitle"><@s.text name="portal.home.records"/></span>
                <span data-k="lastPublication"><@s.text name="portal.home.last.publication"/></span>
                <span data-k="previous"><@s.text name="pager.previous"/></span>
                <span data-k="next"><@s.text name="pager.next"/></span>
            </template>
        <#else>
            <div class="ph-empty">
                <h2><@s.text name="portal.home.empty.title"/></h2>
                <p><@s.text name="portal.home.empty.text"/></p>
                <#if managerRights!false>
                    <p><@s.text name="portal.home.empty.manage"><@s.param>${baseURL}/manage/</@s.param></@s.text></p>
                </#if>
            </div>
        </#if>
    </div>
</main>

<#if (resourcesSize>0)>
<script>
    <#-- list and filters of /api/home-resources (HomeAction#search); the search, filters, sort, view and page are kept
         in the URL so a search can be shared -->
    (function () {
        const API = "${baseURL?js_string?no_esc}/api/home-resources";
        const RESOURCE = "${baseURL?js_string?no_esc}/resource?r=";
        const ICONS = "${baseURL?js_string?no_esc}/images/icons/material-icons.svg#";
        const PER_PAGE = 10;
        const FACETS = {types: "type", organisations: "organisation", licences: "licence", updated: "updated"};
        const SINGLE = {updated: true};

        const $ = id => document.getElementById(id);
        const T = {};
        $("ph-texts").content.querySelectorAll("[data-k]").forEach(el => T[el.dataset.k] = el.textContent.trim());
        const fmt = (s, ...args) => s.replace(/\{(\d)}/g, (m, i) => args[i] !== undefined ? args[i] : m);
        const esc = s => String(s == null ? "" : s).replace(/[&<>"']/g, c => ({"&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"}[c]));
        const icon = name => '<svg class="material-icon" aria-hidden="true" focusable="false"><use href="' + ICONS + name + '"/></svg>';

        // the state, read from the URL
        const params = new URLSearchParams(location.search);
        const state = {
            q: params.get("q") || "",
            filters: {types: params.getAll("type"), organisations: params.getAll("organisation"), licences: params.getAll("licence"), updated: params.getAll("updated").slice(0, 1)},
            sort: ["published", "title", "records"].includes(params.get("sort")) ? params.get("sort") : "published",
            view: params.get("view") === "table" ? "table" : "list",
            page: Math.max(1, parseInt(params.get("page"), 10) || 1)
        };
        let last = null, controller = null, labels = {};

        function writeUrl() {
            const p = new URLSearchParams();
            if (state.q) p.set("q", state.q);
            Object.keys(FACETS).forEach(f => state.filters[f].forEach(v => p.append(FACETS[f], v)));
            if (state.sort !== "published") p.set("sort", state.sort);
            if (state.view !== "list") p.set("view", state.view);
            if (state.page > 1) p.set("page", state.page);
            const query = p.toString();
            history.replaceState(null, "", location.pathname + (query ? "?" + query : ""));
        }

        function load() {
            writeUrl();
            const p = new URLSearchParams();
            if (state.q) p.set("q", state.q);
            Object.keys(FACETS).forEach(f => state.filters[f].forEach(v => p.append(FACETS[f], v)));
            p.set("sort", state.sort);
            p.set("offset", (state.page - 1) * PER_PAGE);
            p.set("limit", PER_PAGE);
            if (controller) controller.abort();
            controller = new AbortController();
            $("ph-results").setAttribute("aria-busy", "true");
            fetch(API + "?" + p, {headers: {"Accept": "application/json"}, signal: controller.signal})
                .then(r => { if (!r.ok) throw new Error(r.status); return r.json(); })
                .then(data => {
                    // past the last page (e.g. a shared link to a page that no longer exists): show the last one
                    const pages = Math.max(1, Math.ceil(data.matched / PER_PAGE));
                    if (state.page > pages) { state.page = pages; load(); return; }
                    last = data;
                    render();
                })
                .catch(e => {
                    if (e.name === "AbortError") return;
                    $("ph-results").removeAttribute("aria-busy");
                    $("ph-results").innerHTML = '<div class="ph-empty"><p>' + esc(T.error) + '</p></div>';
                });
        }

        // a text with the search string marked, escaped
        function hl(text) {
            const s = String(text == null ? "" : text), q = state.q.trim();
            if (!q) return esc(s);
            const lower = s.toLowerCase(), ql = q.toLowerCase();
            let out = "", i = 0, j;
            while ((j = lower.indexOf(ql, i)) !== -1) {
                out += esc(s.slice(i, j)) + '<mark class="ph-mark">' + esc(s.slice(j, j + q.length)) + "</mark>";
                i = j + q.length;
            }
            return out + esc(s.slice(i));
        }

        function renderFacets() {
            labels = {};
            document.querySelectorAll(".ph-facet").forEach(box => {
                const f = box.dataset.facet, values = last.facets[f] || [];
                labels[f] = {};
                values.forEach(v => labels[f][v.value] = v.label);
                // an organisation filter only when there are two or more
                box.hidden = f === "organisations" ? values.length < 2 : values.length === 0;
                const type = SINGLE[f] ? "radio" : "checkbox";
                box.querySelector(".ph-values").innerHTML = values.map(v => {
                    const on = state.filters[f].includes(v.value);
                    return '<label class="' + (!v.count && !on ? "ph-zero" : "") + '"><input class="form-check-input" type="' + type + '" name="ph-' + f + '" value="' + esc(v.value) + '"' + (on ? " checked" : "") + '>'
                        + '<span>' + esc(v.label) + '</span><span class="ph-n">' + v.count + '</span></label>';
                }).join("");
            });
        }

        function renderChips() {
            const chips = [];
            Object.keys(FACETS).forEach(f => state.filters[f].forEach(v => chips.push({f: f, v: v, label: (labels[f] || {})[v] || v})));
            if (state.q) chips.push({f: "q", v: "", label: "“" + state.q + "”"});
            $("ph-chips").innerHTML = chips.map(c => '<span class="ph-chip">' + esc(c.label)
                + '<button type="button" data-f="' + esc(c.f) + '" data-v="' + esc(c.v) + '" aria-label="' + esc(fmt(T.remove, c.label)) + '">' + icon("close") + '</button></span>').join("")
                + (chips.length > 1 ? '<button type="button" class="ph-chip-clear" data-f="all">' + esc(T.clear) + '</button>' : "");
        }

        function pills(item) {
            const p = [];
            if (item.typeLabel) p.push('<span class="dt-content-pill type-' + esc(item.type) + '">' + esc(item.typeLabel) + '</span>');
            if (item.subtypeLabel) p.push('<span class="dt-content-pill type-' + esc(item.type) + '">' + esc(item.subtypeLabel) + '</span>');
            if (item.licenceLabel) p.push('<span class="ph-pill">' + esc(item.licenceLabel) + '</span>');
            if (item.doiUrl) p.push('<a class="ph-pill" href="' + esc(item.doiUrl) + '" title="' + esc(item.doi) + '">DOI</a>');
            if (item.gbifUrl) p.push('<a class="ph-pill ph-pill-gbif" href="' + esc(item.gbifUrl) + '">' + esc(T.onGbif) + ' ' + icon("open_in_new") + '</a>');
            return p.join("");
        }

        function renderList(items) {
            return items.map(item => {
                const url = RESOURCE + encodeURIComponent(item.shortname);
                // without a logo, a box with an icon (no text: it must fit in any language), so the rows stay aligned
                const logo = item.logoUrl
                    ? '<a class="ph-logo" href="' + esc(url) + '" tabindex="-1" aria-hidden="true"><img src="' + esc(item.logoUrl) + '" alt="" loading="lazy"></a>'
                    : '<a class="ph-logo ph-logo-none" href="' + esc(url) + '" tabindex="-1" aria-hidden="true" title="' + esc(T.noLogo) + '">' + icon("hide_image") + '</a>';
                const records = item.metadataOnly ? "<b>—</b>" + esc(T.metadataOnly) : "<b>" + esc(item.recordsLabel) + "</b>" + esc(T.records);
                return '<article class="ph-res">' + logo
                    + '<div><h3><a href="' + esc(url) + '">' + hl(item.title) + '</a></h3>'
                    + (item.organisation ? '<div class="ph-org">' + hl(item.organisation) + '</div>' : "")
                    + (item.description ? '<p class="ph-desc">' + hl(item.description) + '</p>' : "")
                    + '<div class="ph-pills">' + pills(item) + '</div></div>'
                    + '<div class="ph-meta">' + records
                    + (item.lastPublished ? '<br><span title="' + esc(T.lastPublication + ": " + item.lastPublishedTime) + '">' + esc(item.lastPublished) + '</span>' : "")
                    + '</div>'
                    + '</article>';
            }).join("");
        }

        function renderTable(items) {
            return '<div class="ph-table-wrap"><table class="ph-table"><thead><tr><th scope="col">' + esc(T.name) + '</th><th scope="col">' + esc(T.organisation) + '</th><th scope="col">' + esc(T.type)
                + '</th><th scope="col">' + esc(T.licence) + '</th><th scope="col" class="ph-num">' + esc(T.recordsTitle) + '</th><th scope="col" class="ph-num">' + esc(T.lastPublication) + '</th></tr></thead><tbody>'
                + items.map(item => '<tr><td><a href="' + esc(RESOURCE + encodeURIComponent(item.shortname)) + '">' + hl(item.title) + '</a></td>'
                    + '<td>' + hl(item.organisation || "") + '</td>'
                    + '<td>' + (item.typeLabel ? '<span class="dt-content-pill fs-smaller-2 text-nowrap type-' + esc(item.type) + '">' + esc(item.typeLabel) + '</span>' : "") + '</td>'
                    + '<td class="text-nowrap">' + esc(item.licenceLabel || "") + '</td>'
                    + '<td class="ph-num">' + (item.metadataOnly ? "—" : esc(item.recordsLabel)) + '</td>'
                    + '<td class="ph-num" title="' + esc(item.lastPublishedTime || "") + '">' + esc(item.lastPublished || "") + '</td></tr>').join("")
                + '</tbody></table></div>';
        }

        function renderPager() {
            const pages = Math.ceil(last.matched / PER_PAGE), cur = state.page;
            if (pages <= 1) { $("ph-pager").innerHTML = ""; return; }
            // first, last, and two either side of the current page
            const shown = [];
            for (let i = 1; i <= pages; i++) if (i === 1 || i === pages || Math.abs(i - cur) <= 2) shown.push(i);
            let html = '<button type="button" data-p="' + (cur - 1) + '"' + (cur === 1 ? " disabled" : "") + '>' + esc(T.previous) + '</button>';
            shown.forEach((p, k) => {
                if (k && p - shown[k - 1] > 1) html += '<span class="ph-gap" aria-hidden="true">…</span>';
                html += '<button type="button" data-p="' + p + '"' + (p === cur ? ' aria-current="page"' : "") + '>' + p + '</button>';
            });
            html += '<button type="button" data-p="' + (cur + 1) + '"' + (cur === pages ? " disabled" : "") + '>' + esc(T.next) + '</button>';
            $("ph-pager").innerHTML = html;
        }

        function render() {
            renderFacets();
            renderChips();
            const filtered = state.q || Object.keys(FACETS).some(f => state.filters[f].length);
            $("ph-count").innerHTML = filtered
                ? fmt(esc(T.count), "<b>" + last.matched + "</b>", last.total)
                : fmt(esc(T.countAll), "<b>" + last.total + "</b>");
            $("ph-results").removeAttribute("aria-busy");
            $("ph-results").innerHTML = !last.items.length
                ? '<div class="ph-empty"><h3>' + esc(T.noMatch) + '</h3><p>' + esc(T.noMatchHint) + '</p></div>'
                : state.view === "table" ? renderTable(last.items) : renderList(last.items);
            renderPager();
        }

        // events
        $("ph-q").value = state.q;
        $("ph-sort").value = state.sort;
        document.querySelectorAll("[data-view]").forEach(b => b.setAttribute("aria-pressed", String(b.dataset.view === state.view)));

        let typing = null;
        $("ph-q").addEventListener("input", () => {
            clearTimeout(typing);
            typing = setTimeout(() => { state.q = $("ph-q").value.trim(); state.page = 1; load(); }, 250);
        });
        $("ph-search-form").addEventListener("submit", e => {
            e.preventDefault();
            clearTimeout(typing);
            state.q = $("ph-q").value.trim(); state.page = 1; load();
        });
        // select2 as the other selects (e.g. the organisation filter on Manage resources); it reports changes through jQuery
        jQuery("#ph-sort").select2({
            width: "resolve",
            dropdownAutoWidth: true,
            minimumResultsForSearch: Infinity,
            theme: "bootstrap4"
        }).on("change", function () { state.sort = this.value; state.page = 1; load(); });
        document.querySelectorAll("[data-view]").forEach(b => b.addEventListener("click", () => {
            state.view = b.dataset.view;
            document.querySelectorAll("[data-view]").forEach(x => x.setAttribute("aria-pressed", String(x === b)));
            writeUrl();
            if (last) render();
        }));
        $("ph-filters").addEventListener("change", e => {
            const input = e.target, f = input.closest(".ph-facet") && input.closest(".ph-facet").dataset.facet;
            if (!f) return;
            if (SINGLE[f]) state.filters[f] = input.checked ? [input.value] : [];
            else state.filters[f] = input.checked ? state.filters[f].concat(input.value) : state.filters[f].filter(v => v !== input.value);
            state.page = 1; load();
        });
        // a second click on the chosen radio button clears it
        $("ph-filters").addEventListener("click", e => {
            const input = e.target;
            if (input.type === "radio" && input.checked && state.filters.updated.includes(input.value) && input.dataset.was === "1") {
                input.checked = false; state.filters.updated = []; state.page = 1; load();
            }
        });
        $("ph-filters").addEventListener("pointerdown", e => {
            const label = e.target.closest("label"), radio = label && label.querySelector("input[type=radio]");
            if (radio) radio.dataset.was = radio.checked ? "1" : "0";
        });
        $("ph-chips").addEventListener("click", e => {
            const b = e.target.closest("button[data-f]");
            if (!b) return;
            const f = b.dataset.f;
            if (f === "all") { Object.keys(FACETS).forEach(k => state.filters[k] = []); state.q = ""; $("ph-q").value = ""; }
            else if (f === "q") { state.q = ""; $("ph-q").value = ""; }
            else state.filters[f] = state.filters[f].filter(v => v !== b.dataset.v);
            state.page = 1; load();
        });
        $("ph-pager").addEventListener("click", e => {
            const b = e.target.closest("button[data-p]");
            if (!b || b.disabled) return;
            state.page = parseInt(b.dataset.p, 10);
            load();
            $("ph").scrollIntoView({behavior: "smooth", block: "start"});
        });

        // a logo that doesn't load (gone, or blocked by its site) shows as no logo
        $("ph-results").addEventListener("error", e => {
            const img = e.target;
            if (img.tagName !== "IMG" || !img.parentElement.classList.contains("ph-logo")) return;
            const box = img.parentElement;
            box.classList.add("ph-logo-none");
            box.title = T.noLogo;
            box.innerHTML = icon("hide_image");
        }, true);

        // narrow screens: the filters start collapsed
        if (window.matchMedia("(max-width: 767.98px)").matches) $("ph-filters").open = false;

        load();
    })();
</script>
</#if>

<#include "/WEB-INF/pages/inc/footer.ftl">
