/**
 * Renders the pre-check panel markup from a DataPackagePrecheck result.
 * Pairs with datapackage-precheck.js — that module produces the data,
 * this one turns it into DOM.
 * Both should be kept as static assets, not inline
 * in a .ftl template (see datapackage-precheck.js).
 *
 * Usage:
 *   const result = await DataPackagePrecheck.run(file);
 *   renderPrecheckPanel(result, file.name, document.getElementById('precheckPanel'));
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

    function isMetadataFile(path) {
        // path is already normalized (lowercased, no leading slash) by DataPackagePrecheck
        const base = path.includes('/') ? path.slice(path.lastIndexOf('/') + 1) : path;
        return METADATA_FILENAMES.has(base);
    }

    function fileRow(file, isLast) {
        const hasResources = file.resources.length > 0;
        const allInSchema = hasResources && file.resources.every((r) => r.inSchema);
        const ok = hasResources && allInSchema;

        const icon = ok
            ? '<i class="bi bi-check2 text-gbif-primary" style="font-size:16px;"></i>'
            : '<i class="bi bi-x text-gbif-danger" style="font-size:16px;"></i>';

        let right;
        if (!hasResources) {
            right = `
        <div class="text-gbif-danger" style="display:flex; align-items:center; gap:6px; font-size:13px;">
          <span>not mapped to any resource</span>
        </div>`;
        } else {
            const names = file.resources
                .map((r) => {
                    const color = r.inSchema ? '' : 'rgb(var(--color-gbif-danger))';
                    const suffix = r.inSchema ? '' : ' (not in schema)';
                    return `<span style="color:${color};">${escapeHtml(r.name)}${suffix}</span>`;
                })
                .join(', ');
            right = `
        <div style="display:flex; align-items:center; gap:6px; font-size:13px;">
          <i class="bi bi-arrow-right" style="font-size:13px;"></i>
          <span>${names}</span>
        </div>`;
        }

        const border = isLast ? '' : 'border-bottom:0.5px solid var(--border-color);';
        return `
      <div style="display:flex; align-items:center; justify-content:space-between; padding:8px 0; ${border}">
        <div style="display:flex; align-items:center; gap:8px;">
          ${icon}
          <span style="font-size:14px;">${escapeHtml(file.path)}</span>
        </div>
        ${right}
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
        <p style="font-size:13px; margin:0 0 8px;">Metadata</p>
        ${rows}
      </div>`;
    }

    function verdictSection(result) {
        const colorClass = result.ready ? 'text-gbif-primary' : 'text-gbif-danger';
        const icon = result.ready
            ? '<i class="bi bi-check2 text-gbif-primary" style="font-size:16px;"></i>'
            : '<i class="bi bi-x text-gbif-danger" style="font-size:16px;"></i>';
        const message = result.ready
            ? 'IPT should be able to create a resource from this archive'
            : `Cannot create a resource${result.reason ? ' — ' + escapeHtml(result.reason) : ''}`;
        return `
      <div style="border-top:0.5px solid var(--border-color); margin-top:12px; padding-top:12px; display:flex; align-items:center; gap:8px;">
        ${icon}
        <span class="${colorClass}" style="font-size:13px;">${message}</span>
      </div>`;
    }

    function renderPrecheckPanel(result, fileName, container) {
        const allFiles = result.files || [];
        const metadataFiles = allFiles.filter((f) => isMetadataFile(f.path));
        const dataFiles = allFiles.filter((f) => !isMetadataFile(f.path));

        const fileRows = dataFiles.length
            ? dataFiles.map((f, i) => fileRow(f, i === dataFiles.length - 1)).join('')
            : `<p style="font-size:13px; padding:8px 0;">No data files found in archive.</p>`;

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
          <p style="font-size:13px; margin:0 0 8px;">File → tables mapping</p>
          ${fileRows}
        </div>
        ${metadataSection(metadataFiles)}
        `
                : `
        <div style="border-top:0.5px solid var(--border-color); padding-top:12px;">
          <p style="font-size:13px; margin:0;">${escapeHtml(result.reason || 'Could not read this archive.')}</p>
        </div>
        `
        }

        ${verdictSection(result)}

      </div>`;
    }

    global.renderPrecheckPanel = renderPrecheckPanel;
})(window);