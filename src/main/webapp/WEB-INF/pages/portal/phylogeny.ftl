<#-- Phylogenetic tree viewer, shared by the public page (published trees) and the manager preview (working copies) -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title>${treeFileName} - ${resource.title!resource.shortname}</title>
<#if preview><#assign currentMenu = "manage"/></#if>
<#include "/WEB-INF/pages/inc/menu.ftl">

<script src="${baseURL}/js/archaeopteryx/d3.v7.min.js"></script>
<script src="${baseURL}/js/archaeopteryx/sax.js"></script>
<script src="${baseURL}/js/archaeopteryx/phyloxml.js"></script>
<script src="${baseURL}/js/archaeopteryx/forester.js"></script>
<script src="${baseURL}/js/archaeopteryx/archaeopteryx.js"></script>
<script src="${baseURL}/js/archaeopteryx/canvg.global.js"></script>

<style>
    #phylogeny-viewer {
        position: relative;
        height: calc(100vh - 220px);
        min-height: 500px;
    }

    /* Archaeopteryx.js themes its control panel and overlays with --p-* custom properties: map them onto the IPT
       look (primary colour from UI management, Roboto, Bootstrap borders and radii). The library injects its own
       stylesheet after this one, hence the extra specificity of "body". */
    #phylogeny-viewer .aptx-panel,
    body .aptx-node-menu, body .aptx-suggest, body .aptx-dialog, body .aptx-busy, body .aptx-msa-nav, body .aptx-tip {
        --p-bg: rgba(255, 255, 255, 0.95);
        --p-ink: #575757;
        --p-muted: #6c757d;
        --p-line: #e9ecef;
        --p-line-strong: #dee2e6;
        --p-surface2: #f8f9fa;
        --p-accent: rgb(var(--color-gbif-primary));
        --p-accent-ink: rgb(var(--color-gbif-primary));
        --p-accent-weak: rgba(var(--color-gbif-primary), 0.12);
        font-family: Roboto, 'Helvetica Neue', Helvetica, Arial, sans-serif;
    }

    #phylogeny-viewer .aptx-panel {
        border-radius: var(--bs-border-radius);
        box-shadow: 0 .125rem .25rem rgba(0, 0, 0, .075);
    }

    #phylogeny-viewer .aptx-panel .prognamelink,
    #phylogeny-viewer .aptx-panel .prognamelink:link,
    #phylogeny-viewer .aptx-panel .prognamelink:visited {
        color: #4E565F;
        font-size: 0.875rem;
        font-weight: 400;
    }

    /* IPT has no dark mode: the script below forces the light theme, so the switch has nothing to do */
    #phylogeny-viewer .aptx-theme-btn {
        display: none;
    }
</style>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<div class="container-fluid bg-body border-bottom">
    <div class="container border rounded-2 mb-4">
        <div class="container my-3 p-3">
            <div class="text-center">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <#if preview>
                                <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                                <li class="breadcrumb-item"><a href="${backUrl}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                            <#else>
                                <li class="breadcrumb-item"><a href="${baseURL}"><@s.text name="breadcrumb.home"/></a></li>
                                <li class="breadcrumb-item"><a href="${backUrl}"><@s.text name="breadcrumb.resource"/></a></li>
                            </#if>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.phylogeny"/></li>
                        </ol>
                    </nav>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                    ${treeFileName}
                </h1>

                <div class="text-smaller">
                    <a href="${backUrl}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </div>

                <div id="phylogeny-status" class="text-gbif-primary fs-smaller-2 mt-2"></div>

                <div class="mt-2">
                    <a href="${treeUrl}" download="${treeFileName}" class="btn btn-sm btn-outline-gbif-primary mt-1 me-xl-1 top-button">
                        <@s.text name="button.download"/>
                    </a>
                    <a href="${backUrl}" class="btn btn-sm btn-outline-secondary mt-1 me-xl-1 top-button">
                        <@s.text name="button.back"/>
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="phylogeny-viewer"
     data-file-name="${treeFileName}"
     data-tree-url="${treeUrl}"
     data-tips-url="${tipsUrl!}"
     data-msg-loading="<@s.text name='portal.phylogeny.loading'/>"
     data-msg-error="<@s.text name='portal.phylogeny.error'/>"
     data-msg-linked="<@s.text name='portal.phylogeny.linked'/>"
     data-msg-not-linked="<@s.text name='portal.phylogeny.notLinked'/>"
     data-msg-not-published="<@s.text name='portal.phylogeny.notPublished'/>"
     data-msg-controls="<@s.text name='portal.phylogeny.controls'/>"></div>

<script>
    (function () {
        var container = document.getElementById('phylogeny-viewer');
        var status = document.getElementById('phylogeny-status');
        var data = container.dataset;
        // the column the server marks linked tips with, see PhylogeneticTreeManagerImpl
        var LINKED_COLUMN = 'Linked specimen';

        // IPT has no dark mode: keep the viewer light whatever the OS or an earlier toggle says
        try {
            localStorage.setItem('aptx-panel-theme', 'light');
        } catch (e) {
            // storage unavailable, the viewer follows the OS preference
        }

        // The panel title reads "Archaeopteryx.js <version>": label it in IPT terms instead. The button still opens
        // the viewer's About box (credits and licence). The panel is rebuilt when switching trees, so keep watching.
        function relabelPanel() {
            var title = container.querySelector('.prognamelink');
            if (title && title.firstChild && title.firstChild.nodeType === Node.TEXT_NODE
                && title.firstChild.nodeValue !== data.msgControls) {
                title.firstChild.nodeValue = data.msgControls;
                title.setAttribute('aria-label', data.msgControls);
            }
        }
        new MutationObserver(relabelPanel).observe(container, {childList: true, subtree: true});

        function format(msg) {
            var args = Array.prototype.slice.call(arguments, 1);
            return msg.replace(/\{(\d+)\}/g, function (m, i) {
                return args[i];
            });
        }

        function fetchText(url) {
            return fetch(url, {credentials: 'same-origin'}).then(function (response) {
                if (!response.ok) {
                    var error = new Error('HTTP ' + response.status + ' loading ' + url);
                    error.status = response.status;
                    throw error;
                }
                return response.text();
            });
        }

        // Joins the tip metadata onto a tree. Tips no specimen cites get "no" in the linked column, so the column has
        // two values and is offered (and opened) as a colour visualization.
        function joinTips(tree, table) {
            var lines = table.split('\n').filter(function (line) {
                return line.length > 0;
            });
            var linked = {};
            lines.slice(1).forEach(function (line) {
                linked[line.split('\t')[0]] = true;
            });
            // empty cells for the remaining columns
            var padding = '\t'.repeat(lines[0].split('\t').length - 2);
            var names = forester.getAllExternalNodes(tree).map(function (node) {
                return node.name;
            }).filter(Boolean);
            names.forEach(function (name) {
                if (!linked[name]) {
                    lines.push(name + '\tno' + padding);
                }
            });
            forester.joinMetadataTable(tree, lines.join('\n'));
            return {
                linked: names.filter(function (name) {
                    return linked[name];
                }).length,
                tips: names.length
            };
        }

        status.textContent = data.msgLoading;

        Promise.all([
            fetchText(data.treeUrl),
            // tip metadata is optional: no published version, or no specimens citing this tree
            data.tipsUrl ? fetchText(data.tipsUrl).catch(function () {
                return null;
            }) : Promise.resolve(undefined)
        ]).then(function (results) {
            var trees = archaeopteryx.parseTrees(data.fileName, results[0]);
            var tips = results[1];
            var config = {};
            var messages = [];

            if (tips) {
                var counts = null;
                trees.forEach(function (tree) {
                    var c = joinTips(tree, tips);
                    counts = counts || c;
                });
                if (counts.linked > 0) {
                    config.initialVisualization = LINKED_COLUMN;
                }
                messages.push(format(data.msgLinked, counts.linked, counts.tips));
            } else if (tips === null) {
                messages.push(data.msgNotLinked);
            } else {
                messages.push(data.msgNotPublished);
            }

            archaeopteryx.launch(container, trees, config);
            status.textContent = messages.join(' ');
        }).catch(function (e) {
            console.error(e);
            status.textContent = '';
            container.innerHTML = '';
            var error = document.createElement('div');
            error.className = 'container text-gbif-danger text-center';
            error.textContent = data.msgError + ' ' + (e && e.message ? e.message : String(e));
            container.appendChild(error);
        });
    })();
</script>

<#include "/WEB-INF/pages/inc/footer.ftl">
