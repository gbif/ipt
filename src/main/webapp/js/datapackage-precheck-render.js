/**
 * Renders the pre-check panel markup from a DataPackagePrecheck result.
 * Pairs with datapackage-precheck.js — that module produces the data,
 * this one turns it into DOM.
 * Both should be kept as static assets, not inline
 * in a .ftl template.
 *
 * Usage:
 *   const result = await DataPackagePrecheck.run(file, dataPackages);
 *   renderPrecheckPanel(result, file.name, document.getElementById('precheckPanel'), messages);
 *
 * messages holds the translated texts (manage.resource.create.precheck.* keys),
 * with {0}-style placeholders left in for the arguments.
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

    function fileRows(dataFiles, result) {
        const skippedResources = new Set(result.skippedResources ?? []);
        const rows = [
            ...dataFiles.map(fileRowModel),
            ...(result.unresolvedPaths ?? []).map((u) => missingRowModel(u, skippedResources)),
        ];

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

    function metadataRow(file, isLast) {
        const border = isLast ? '' : 'border-bottom:0.5px solid var(--border-color);';
        return `
      <div style="display:flex; align-items:center; gap:8px; padding:6px 0; ${border}">
        <i class="bi bi-file-earmark-text" style="font-size:14px;"></i>
        <span style="font-size:13px;">${escapeHtml(file.path)}</span>
      </div>`;
    }

    function metadataSection(files) {
        if (!files.length) return '';
        const rows = files.map((f, i) => metadataRow(f, i === files.length - 1)).join('');
        return `
      <div style="border-top:0.5px solid var(--border-color); margin-top:12px; padding-top:12px;">
        <p style="font-size:13px; margin:0 0 8px;">${msg('metadata')}</p>
        ${rows}
      </div>`;
    }

    function verdictSection(result) {
        const skipped = result.skippedResources ?? [];
        const missing = result.unresolvedPaths ?? [];
        // Metadata files are listed separately, so they don't count as unmapped data
        const unmapped = (result.unmappedFiles ?? []).filter((f) => !isMetadataFile(f.path));

        let state; // 'success' | 'warning' | 'danger'
        if (!result.ready) {
            state = 'danger';
        } else if (missing.length > 0 || unmapped.length > 0) {
            state = 'warning';
        } else {
            state = 'success';
        }

        if (state === 'success') {
            return `
  <div style="border-top:0.5px solid var(--border-color); margin-top:12px; padding-top:12px; display:flex; align-items:center; gap:8px;">
    <i class="bi bi-check2 text-gbif-primary" style="font-size:16px;"></i>
    <span class="text-gbif-primary" style="font-size:13px;">${msg('success')}</span>
  </div>`;
        }

        if (state === 'danger') {
            const message = `${msg('error')}${result.reason ? ' — ' + escapeHtml(result.reason) : ''}`;
            return `
  <div style="border-top:0.5px solid var(--border-color); margin-top:12px; padding-top:12px; display:flex; align-items:center; gap:8px;">
    <i class="bi bi-x text-gbif-danger" style="font-size:16px;"></i>
    <span class="text-gbif-danger" style="font-size:13px;">${message}</span>
  </div>`;
        }

        // warning state - a summary only, the affected files are marked in the list above
        const bullets = [];
        if (missing.length > 0) {
            const text = skipped.length > 0
                ? msg('warning.missingSkipped', missing.length, skipped.length)
                : msg('warning.missing', missing.length);
            bullets.push(`
      <li>${text}</li>`);
        }
        if (unmapped.length > 0) {
            bullets.push(`
      <li>${msg('warning.unmapped', unmapped.length)}</li>`);
        }

        return `
  <div style="border-top:0.5px solid var(--border-color); margin-top:12px; padding-top:12px;">
    <div style="display:flex; align-items:center; gap:8px;">
      <i class="bi bi-exclamation-triangle text-gbif-warning-dark" style="font-size:16px;"></i>
      <span class="text-gbif-warning-dark" style="font-size:13px;">${msg('warning')}</span>
    </div>
    <ul style="margin:6px 0 0 24px; padding:0; font-size:13px;" class="text-gbif-warning-dark">
      ${bullets.join('')}
    </ul>
  </div>`;
    }

    function renderPrecheckPanel(result, fileName, container, translatedMessages) {
        messages = translatedMessages ?? {};

        const allFiles = result.files || [];
        const metadataFiles = allFiles.filter((f) => isMetadataFile(f.path));
        const dataFiles = allFiles.filter((f) => !isMetadataFile(f.path));

        container.innerHTML = `
      <div class="mt-3" style="border-radius: 12px; border: 0.5px solid var(--border-color); padding: 1rem 1.25rem;">

        <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:12px;">
          <div style="display:flex; align-items:center; gap:8px;">
            <i class="bi bi-file-earmark-zip" style="font-size:20px;"></i>
            <span style="font-weight:500; font-size:15px;">${escapeHtml(fileName)}</span>
          </div>
        </div>

        ${
            result.type === 'data-package'
                ? `
        <div style="border-top:0.5px solid var(--border-color); padding-top:12px;">
          <p style="font-size:13px; margin:0 0 8px;">${msg('files')}</p>
          ${fileRows(dataFiles, result)}
        </div>
        ${metadataSection(metadataFiles)}
        `
                : `
        <div style="border-top:0.5px solid var(--border-color); padding-top:12px;">
          <p style="font-size:13px; margin:0;">${result.reason ? escapeHtml(result.reason) : msg('unreadable')}</p>
        </div>
        `
        }

        ${verdictSection(result)}

      </div>`;
    }

    global.renderPrecheckPanel = renderPrecheckPanel;
})(window);