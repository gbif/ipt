/**
 * Renders the pre-check panel markup from a DataPackagePrecheck result.
 * Pairs with datapackage-precheck.js — that module produces the data,
 * this one turns it into DOM.
 * Both should be kept as static assets, not inline
 * in a .ftl template.
 *
 * Usage:
 *   const result = await DataPackagePrecheck.run(file, dataPackages, installed);
 *   renderPrecheckPanel(result, file.name, document.getElementById('precheckPanel'), messages, options);
 *
 * messages holds the translated texts (manage.resource.create.precheck.* keys),
 * with {0}-style placeholders left in for the arguments. options.extensionsUrl and
 * options.dataPackagesUrl are optional, missing extensions and data package types link to them
 * when options.installable says the registry has them.
 */
(function (global) {
    'use strict';

    const METADATA_FILENAMES = new Set([
        'eml.xml',
        'meta.xml',
        'datapackage.json',
        'metadata.yml',
        'metadata.yaml',
    ]);

    function escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    let messages = {};
    // { extensionsUrl, dataPackagesUrl } - links to install what's missing, only given to admins
    // { installable: { rowTypes: { <rowType>: boolean }, dataPackageTypes: { <identifier>: boolean } } }
    //   - whether the registry has what's missing, unknown until asked
    let options = {};

    // Translated text as escaped HTML, with {0}, {1}... replaced by the arguments
    function msg(key, ...args) {
        const text = escapeHtml(messages[key] ?? key);
        return text.replace(/\{(\d+)}/g, (match, i) => (i < args.length ? escapeHtml(String(args[i])) : match));
    }

    function isMetadataFile(path) {
        // path is already normalized (lowercased, no leading slash) by DataPackagePrecheck
        const base = path.includes('/') ? path.slice(path.lastIndexOf('/') + 1) : path;
        return METADATA_FILENAMES.has(base);
    }

    // Same three states as the verdict: success = mapped and in schema,
    // warning = imported partially or not at all, danger = blocks the import
    const STATUS_ICONS = {
        success: '<i class="bi bi-check2 text-gbif-primary" style="font-size:16px;"></i>',
        warning: '<i class="bi bi-exclamation-triangle text-gbif-warning-dark" style="font-size:14px;"></i>',
        danger: '<i class="bi bi-x text-gbif-danger" style="font-size:16px;"></i>',
    };

    const STATUS_ORDER = { danger: 0, warning: 1, success: 2 };

    // A file present in the archive
    function fileRowModel(file) {
        if (file.resources.length === 0) {
            return {
                status: 'warning',
                path: escapeHtml(file.path),
                target: null,
                note: `<span class="text-gbif-warning-dark">${msg('unmapped')}</span>`,
            };
        }

        const names = file.resources
            .map((r) => {
                const color = r.inSchema ? '' : 'rgb(var(--color-gbif-danger))';
                const suffix = r.inSchema ? '' : ' ' + msg('notInSchema');
                return `<span style="color:${color};">${escapeHtml(r.name)}${suffix}</span>`;
            })
            .join(', ');

        return {
            status: file.resources.every((r) => r.inSchema) ? 'success' : 'danger',
            path: escapeHtml(file.path),
            target: names,
            note: '',
        };
    }

    // A file declared in datapackage.json but absent from the archive
    function missingRowModel(unresolved, skippedResources) {
        const path = typeof unresolved.path === 'string'
            ? escapeHtml(unresolved.path)
            : `<em>${msg('noPath')}</em>`;
        // The resource is only skipped when none of its files are in the archive
        const note = skippedResources.has(unresolved.resourceName) ? msg('missing.skipped') : msg('missing.partial');

        return {
            status: 'warning',
            path: `<span class="text-muted">${path}</span>`,
            target: escapeHtml(unresolved.resourceName),
            note: `<span class="text-gbif-warning-dark">${note}</span>`,
        };
    }

    // Cells of one row in the 4-column grid (icon, file, arrow, table and note);
    // borders go on every cell so they join into a single row divider
    function rowCells(row, isLast) {
        const cell = `display:flex; align-items:center; padding:8px 12px 8px 0; ${isLast ? '' : 'border-bottom:0.5px solid var(--border-color);'}`;
        const arrow = row.target ? '<i class="bi bi-arrow-right" style="font-size:13px;"></i>' : '';
        // Dash between table name and note, so they don't read as one phrase
        const separator = row.target && row.note ? '<span class="text-muted">—</span>' : '';
        return `
        <div style="${cell}">${STATUS_ICONS[row.status]}</div>
        <div style="${cell} font-size:14px; overflow-wrap:anywhere;">${row.path}</div>
        <div style="${cell}">${arrow}</div>
        <div style="${cell} flex-wrap:wrap; gap:6px; font-size:13px; padding-right:0;">${row.target ?? ''}${separator}${row.note}</div>`;
    }

    function dataPackageRows(dataFiles, result) {
        const skippedResources = new Set(result.skippedResources ?? []);
        return rowsGrid([
            ...dataFiles.map(fileRowModel),
            ...(result.unresolvedPaths ?? []).map((u) => missingRowModel(u, skippedResources)),
        ]);
    }

    // Notes of a row, coloured by how serious they are, joined into one flex item
    const NOTE_CLASSES = { danger: 'text-gbif-danger', warning: 'text-gbif-warning-dark', muted: 'text-muted' };

    function notesMarkup(notes) {
        return notes.length
            ? `<span>${notes.map(([level, html]) => `<span class="${NOTE_CLASSES[level]}">${html}</span>`).join('; ')}</span>`
            : '';
    }

    // Only offers to install what the registry has: admins get a link, everybody else is pointed to them.
    // kind: 'rowTypes' | 'dataPackageTypes'
    function notInstalledNote(kind, identifier) {
        const installable = options.installable?.[kind]?.[identifier]; // undefined until the registry answered
        if (installable === false) return msg('notInstalled.unavailable');

        const installUrl = kind === 'rowTypes' ? options.extensionsUrl : options.dataPackagesUrl;
        if (!installUrl) return msg('notInstalled.contactAdmin');
        return installable
            ? `${msg('notInstalled')}, <a href="${escapeHtml(installUrl)}">${msg('notInstalled.install')}</a>`
            : msg('notInstalled');
    }

    function dwcaRows(result) {
        const rows = [];

        for (const table of result.tables) {
            const notes = [];
            let status = 'success';
            let target = null;

            // The server never sees a skipped extension, so its other problems don't matter
            if (table.skipped) {
                rows.push({
                    status: 'warning',
                    path: escapeHtml(table.path ?? ''),
                    target: escapeHtml(table.title ?? table.rowType ?? ''),
                    note: notesMarkup([['warning', msg('dwca.noCoreId')]]),
                });
                continue;
            }

            if (table.rowType && table.installed) {
                const role = msg(table.role === 'core' ? 'dwca.core' : 'dwca.extension');
                target = `${escapeHtml(table.title)} <span class="text-muted">(${role})</span>`;
            } else if (table.rowType) {
                status = 'danger';
                target = `<span class="text-gbif-danger">${escapeHtml(table.rowType)}</span>`;
                notes.push(['danger', notInstalledNote('rowTypes', table.rowType)]);
            } else {
                status = 'danger';
                notes.push(['danger', msg(result.layout === 'single-file' ? 'dwca.noIdColumn' : 'dwca.noRowType')]);
            }
            if (!table.found) {
                status = 'danger';
                notes.push(['danger', msg('dwca.fileMissing')]);
            }
            if (table.idMissing) {
                status = 'danger';
                notes.push(['danger', msg('dwca.noId')]);
            }
            if (table.detectedFrom) {
                notes.push(['muted', msg('dwca.detectedFrom', table.detectedFrom)]);
            }

            const path = table.path ? escapeHtml(table.path) : `<em>${msg('noPath')}</em>`;
            rows.push({
                status,
                path: table.found ? path : `<span class="text-muted">${path}</span>`,
                target,
                note: notesMarkup(notes),
            });

            for (const extraPath of table.extraPaths) {
                rows.push({
                    status: 'warning',
                    path: escapeHtml(extraPath),
                    target: escapeHtml(table.title ?? table.rowType ?? ''),
                    note: notesMarkup([['warning', msg('dwca.extraLocation')]]),
                });
            }
        }

        for (const path of result.unmappedFiles) {
            rows.push({
                status: 'warning',
                path: escapeHtml(path),
                target: null,
                note: notesMarkup([['warning', msg(result.layout === 'single-file' ? 'dwca.notImported' : 'dwca.unmapped')]]),
            });
        }

        return rowsGrid(rows);
    }

    function resourceFolderRows(result) {
        return rowsGrid(result.sources.map((source) => {
            const notes = [];
            let status = 'success';

            // Data package tables keep their name, the missing type is named in the verdict
            const target = source.tables
                .map((t) => {
                    if (t.installed) return escapeHtml(t.title);
                    return `<span class="text-gbif-danger">${escapeHtml(t.schema ? t.title : t.rowType)}</span>`;
                })
                .join(', ');
            // Extensions are installed one by one, a data package type as a whole, so that's only in the verdict
            if (source.tables.some((t) => !t.installed)) status = 'danger';
            const missingExtension = source.tables.find((t) => !t.installed && !t.schema);
            if (missingExtension) notes.push(['danger', notInstalledNote('rowTypes', missingExtension.rowType)]);
            if (!source.found) {
                if (status === 'success') status = 'warning';
                notes.push(['warning', msg('ipt.fileMissing')]);
            }
            if (source.kind === 'sqlsource') notes.push(['muted', msg('ipt.sqlSource')]);
            if (!source.tables.length) notes.push(['muted', msg('ipt.notMapped')]);

            const path = escapeHtml(source.path);
            return {
                status,
                path: source.found ? path : `<span class="text-muted">${path}</span>`,
                target: target || null,
                note: notesMarkup(notes),
            };
        }));
    }

    function rowsGrid(rows) {
        if (!rows.length) {
            return `<p style="font-size:13px; padding:8px 0;">${msg('noDataFiles')}</p>`;
        }

        // Problems first, original order otherwise (sort is stable)
        rows.sort((a, b) => STATUS_ORDER[a.status] - STATUS_ORDER[b.status]);

        return `
      <div style="display:grid; grid-template-columns:auto auto auto 1fr;">
        ${rows.map((row, i) => rowCells(row, i === rows.length - 1)).join('')}
      </div>`;
    }

    function metadataRow(path, isLast) {
        const border = isLast ? '' : 'border-bottom:0.5px solid var(--border-color);';
        return `
      <div style="display:flex; align-items:center; gap:8px; padding:6px 0; ${border}">
        <i class="bi bi-file-earmark-text" style="font-size:14px;"></i>
        <span style="font-size:13px;">${escapeHtml(path)}</span>
      </div>`;
    }

    function metadataSection(paths) {
        if (!paths.length) return '';
        const rows = paths.map((p, i) => metadataRow(p, i === paths.length - 1)).join('');
        return `
      <div style="border-top:0.5px solid var(--border-color); margin-top:12px; padding-top:12px;">
        <p style="font-size:13px; margin:0 0 8px;">${msg('metadata')}</p>
        ${rows}
      </div>`;
    }

    function dataPackageVerdict(result) {
        const skipped = result.skippedResources ?? [];
        const missing = result.unresolvedPaths ?? [];
        // Metadata files are listed separately, so they don't count as unmapped data
        const unmapped = (result.unmappedFiles ?? []).filter((f) => !isMetadataFile(f.path));

        if (!result.ready) {
            return verdictSection('danger', result.reason ? [escapeHtml(result.reason)] : []);
        }

        // a summary only, the affected files are marked in the list above
        const items = [];
        if (missing.length > 0) {
            items.push(skipped.length > 0
                ? msg('warning.missingSkipped', missing.length, skipped.length)
                : msg('warning.missing', missing.length));
        }
        if (unmapped.length > 0) {
            items.push(msg('warning.unmapped', unmapped.length));
        }
        return verdictSection(items.length ? 'warning' : 'success', items);
    }

    // DwC-As and resource folders list their problems as text keys
    // Problems about something missing, followed by whether and how it can be installed
    const NOT_INSTALLED_PROBLEMS = { 'ipt.error.dataPackageTypeNotInstalled': 'dataPackageTypes' };

    function problemsVerdict(result) {
        const problems = result.ready ? result.warnings : result.errors;
        const items = problems.map((p) => {
            const text = msg(p.key, ...p.args);
            const kind = NOT_INSTALLED_PROBLEMS[p.key];
            return kind ? `${text} ${notInstalledNote(kind, p.args[0])}` : text;
        });
        return verdictSection(!result.ready ? 'danger' : items.length ? 'warning' : 'success', items);
    }

    // state: 'success' | 'warning' | 'danger', items: HTML of the problems behind it
    function verdictSection(state, items) {
        if (state === 'success') {
            return `
  <div style="border-top:0.5px solid var(--border-color); margin-top:12px; padding-top:12px; display:flex; align-items:center; gap:8px;">
    <i class="bi bi-check2 text-gbif-primary" style="font-size:16px;"></i>
    <span class="text-gbif-primary" style="font-size:13px;">${msg('success')}</span>
  </div>`;
        }

        // A single error fits on the headline, more get a list like the warnings
        if (state === 'danger' && items.length <= 1) {
            const message = `${msg('error')}${items.length ? ' — ' + items[0] : ''}`;
            return `
  <div style="border-top:0.5px solid var(--border-color); margin-top:12px; padding-top:12px; display:flex; align-items:center; gap:8px;">
    <i class="bi bi-x text-gbif-danger" style="font-size:16px;"></i>
    <span class="text-gbif-danger" style="font-size:13px;">${message}</span>
  </div>`;
        }

        const [icon, cssClass, headline] = state === 'danger'
            ? ['bi-x', 'text-gbif-danger', msg('error.list')]
            : ['bi-exclamation-triangle', 'text-gbif-warning-dark', msg('warning')];

        return `
  <div style="border-top:0.5px solid var(--border-color); margin-top:12px; padding-top:12px;">
    <div style="display:flex; align-items:center; gap:8px;">
      <i class="bi ${icon} ${cssClass}" style="font-size:16px;"></i>
      <span class="${cssClass}" style="font-size:13px;">${headline}</span>
    </div>
    <ul style="margin:6px 0 0 24px; padding:0; font-size:13px;" class="${cssClass}">
      ${items.map((item) => `<li>${item}</li>`).join('')}
    </ul>
  </div>`;
    }

    // File rows, metadata files and verdict for each kind of archive
    function panelSections(result) {
        if (result.type === 'data-package') {
            const allFiles = result.files || [];
            return {
                kind: msg('kind.dataPackage'),
                heading: msg('files'),
                rows: dataPackageRows(allFiles.filter((f) => !isMetadataFile(f.path)), result),
                metadata: allFiles.filter((f) => isMetadataFile(f.path)).map((f) => f.path),
                verdict: dataPackageVerdict(result),
            };
        }
        if (result.type === 'dwc-a') {
            return {
                kind: msg(result.layout === 'single-file' ? 'kind.dwcaSingleFile' : 'kind.dwca'),
                heading: msg('files.dwca'),
                rows: dwcaRows(result),
                metadata: result.metadataFiles,
                verdict: problemsVerdict(result),
            };
        }
        if (result.type === 'ipt-resource') {
            return {
                kind: msg('kind.iptResource'),
                heading: msg('files.iptResource'),
                rows: resourceFolderRows(result),
                metadata: result.metadataFiles,
                verdict: problemsVerdict(result),
            };
        }
        return null;
    }

    function renderPrecheckPanel(result, fileName, container, translatedMessages, panelOptions) {
        messages = translatedMessages ?? {};
        options = panelOptions ?? {};

        const sections = panelSections(result);

        container.innerHTML = `
      <div class="mt-3" style="border-radius: 12px; border: 0.5px solid var(--border-color); padding: 1rem 1.25rem;">

        <div style="display:flex; align-items:center; gap:8px; margin-bottom:12px;">
          <i class="bi bi-file-earmark-zip" style="font-size:20px;"></i>
          <div>
            <div style="font-weight:500; font-size:15px;">${escapeHtml(fileName)}</div>
            ${sections ? `<div class="text-muted" style="font-size:13px;">${sections.kind}</div>` : ''}
          </div>
        </div>

        ${
            sections
                ? `
        <div style="border-top:0.5px solid var(--border-color); padding-top:12px;">
          <p style="font-size:13px; margin:0 0 8px;">${sections.heading}</p>
          ${sections.rows}
        </div>
        ${metadataSection(sections.metadata)}
        ${sections.verdict}
        `
                : `
        <div style="border-top:0.5px solid var(--border-color); padding-top:12px;">
          <p style="font-size:13px; margin:0;">${result.reason ? escapeHtml(result.reason) : msg('unreadable')}</p>
        </div>
        ${verdictSection('danger', [])}
        `
        }

      </div>`;
    }

    global.renderPrecheckPanel = renderPrecheckPanel;
})(window);