/**
 * Renders the pre-check panel markup from a DataPackagePrecheck result.
 * Pairs with datapackage-precheck.js — that module produces the data,
 * this one turns it into DOM.
 * Both should be kept as static assets, not inline
 * in a .ftl template.
 *
 * The panel says first whether the archive can be imported, then the problems with what to do about them,
 * then the contents (collapsed unless a file has a problem).
 *
 * Usage:
 *   const result = await DataPackagePrecheck.run(file, dataPackages, installed);
 *   renderPrecheckPanel(result, document.getElementById('precheckPanel'), messages, options);
 *
 * messages holds the translated texts (manage.resource.create.precheck.* keys),
 * with {0}-style placeholders left in for the arguments. options are all optional:
 * extensionsUrl, dataPackagesUrl (install links, only for admins), typeLabels (resource type -> its label)
 * and installable (whether the registry has what's missing, see showInstallable() in create_input.ftl).
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

    // Darker than the theme colours, so text on the tinted backgrounds stays readable
    const STATUS = {
        success: { bg: '#EEF6EE', fg: '#2F6B2F', border: '#CFE3CF', icon: 'bi-check-circle' },
        warning: { bg: '#FFF6DD', fg: '#6B4A00', border: '#F1DDA4', icon: 'bi-exclamation-circle' },
        danger: { bg: '#FDEEEF', fg: '#A12F3B', border: '#F0C8CD', icon: 'bi-x-circle' },
    };

    // Row and problem levels: icon and colours
    const LEVELS = {
        ok: { color: '#3E8E3E', text: '#6C757D', icon: 'bi-check2' },
        warning: { color: '#8A6100', text: '#6B4A00', icon: 'bi-exclamation-triangle' },
        danger: { color: '#B8384A', text: '#A12F3B', icon: 'bi-x-circle' },
        muted: { color: '#ADB5BD', text: '#6C757D', icon: 'bi-dash' },
    };

    const LEVEL_ORDER = ['ok', 'muted', 'warning', 'danger'];

    const MONO = "'Roboto Mono', SFMono-Regular, Menlo, monospace";

    function escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    let messages = {};
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

    function formatSize(bytes) {
        if (bytes == null) return '';
        const units = ['B', 'KB', 'MB', 'GB'];
        let value = bytes;
        let unit = 0;
        while (value >= 1024 && unit < units.length - 1) {
            value /= 1024;
            unit++;
        }
        return `${unit === 0 || value >= 10 ? Math.round(value) : value.toFixed(1)} ${units[unit]}`;
    }

    // File name without folders and extension, to tell whether it just repeats the table name
    function stem(path) {
        return (path ?? '').split('/').pop().replace(/\.[^.]*$/, '').toLowerCase();
    }

    // Label of a resource type this IPT knows, null for any other (an uninstalled data package type, say)
    function typeLabel(type) {
        return options.typeLabels?.[type] ?? null;
    }

    function worst(levels) {
        return levels.reduce((a, b) => (LEVEL_ORDER.indexOf(b) > LEVEL_ORDER.indexOf(a) ? b : a), 'ok');
    }

    // Level of a row from its notes, notes that only inform don't count
    function levelOf(notes) {
        return worst(notes.map(([level]) => level).filter((level) => level !== 'muted'));
    }

    // ---- Problems ----

    // A precheck problem as shown: { level, text, detail, notes, install }, all HTML but install
    // ({ kind, href }: installable by this person, one button on the first problem covers all of a kind)
    function problemView(p) {
        const view = {
            level: p.level === 'error' ? 'danger' : 'warning',
            text: msg(p.key, ...p.args),
            detail: p.detail ? escapeHtml(p.detail) : null,
            notes: p.note ? [msg(p.note.key, ...p.note.args)] : [],
            install: null,
        };
        if (p.install) addInstallHint(view, p.install);
        return view;
    }

    // Only offers to install what the registry has: admins get a button, everybody else is pointed to them
    function addInstallHint(view, { kind, id }) {
        const installable = options.installable?.[kind]?.[id]; // undefined until the registry answered
        const installUrl = kind === 'rowTypes' ? options.extensionsUrl : options.dataPackagesUrl;
        if (installable === false) {
            view.notes.push(msg('install.unavailable'));
        } else if (!installUrl) {
            view.notes.push(msg('install.askAdmin'));
        } else if (installable) {
            view.install = { kind, href: installUrl };
        }
    }

    function statusOf(problems) {
        if (problems.some((p) => p.level === 'danger')) return 'danger';
        return problems.length ? 'warning' : 'success';
    }

    // ---- Rows ----

    // A row of the contents, all HTML: { level, name, role, file, size, notes: [[level, html]], indent, missing },
    // or a group heading { group }
    function row(fields) {
        return { level: 'ok', role: null, file: null, size: null, notes: [], indent: false, missing: false, ...fields };
    }

    function notImportedRows(files) {
        if (!files.length) return [];
        return [
            { group: msg('group.notImported') },
            ...files.map((f) => row({ level: 'muted', name: escapeHtml(f.path), size: f.size, notes: f.notes ?? [] })),
        ];
    }

    // ---- Darwin Core Archive ----

    function dwcaTableRow(table, layout) {
        const notes = [];
        if (table.skipped) {
            notes.push(['warning', msg('row.noCoreId')]);
        } else {
            if (!table.rowType) notes.push(['danger', msg(layout === 'single-file' ? 'row.noIdColumn' : 'row.noRowType')]);
            else if (!table.installed) notes.push(['danger', msg('row.notInstalled')]);
            if (!table.found) notes.push(['danger', msg('row.missing')]);
            if (table.idMissing) notes.push(['danger', msg('row.noId')]);
        }
        if (table.detectedFrom) notes.push(['muted', msg('row.detectedFrom', table.detectedFrom)]);

        return row({
            level: levelOf(notes),
            name: table.title ? escapeHtml(table.title) : msg('row.unknownCore'),
            role: msg(table.role === 'core' ? 'dwca.core' : 'dwca.extension'),
            file: table.path ? escapeHtml(table.path) : null,
            size: table.size,
            missing: !table.found,
            notes,
        });
    }

    function dwcaView(result) {
        const core = result.tables.find((t) => t.role === 'core');
        const extensions = result.tables.filter((t) => t.role === 'extension');

        const rows = result.tables.map((table) => dwcaTableRow(table, result.layout));
        const extraPaths = result.tables.flatMap((t) => t.extraPaths)
            .map((path) => ({ path, size: null, notes: [['muted', msg('row.extraLocation')]] }));
        rows.push(...notImportedRows([...extraPaths, ...result.unmappedFiles]));

        // the resource type the core makes (Occurrence, Checklist...), else the core's own name
        const type = result.datasetType && result.datasetType !== 'other'
            ? typeLabel(result.datasetType)
            : core?.installed ? core.title : null;
        return {
            kind: msg(result.layout === 'single-file' ? 'kind.dwcaSingleFile' : 'kind.dwca'),
            summary: [
                type ? escapeHtml(type) : null,
                extensions.length ? msg('summary.extensions', extensions.length) : null,
            ],
            problems: result.problems.map(problemView),
            rows,
            count: msg('contents.files', result.tables.length + extraPaths.length + result.unmappedFiles.length),
            metadata: result.metadataFiles,
        };
    }

    // ---- IPT resource folder ----

    function resourceFolderView(result) {
        const rows = result.sources.map((source) => {
            const notes = [];
            if (source.tables.some((t) => !t.installed && !t.schema)) notes.push(['danger', msg('row.notInstalled')]);
            if (!source.found) notes.push(['warning', msg('row.missing')]);
            if (source.kind === 'sqlsource') notes.push(['muted', msg('row.sqlSource')]);
            if (!source.tables.length) notes.push(['muted', msg('row.notMapped')]);

            // a missing data package type is explained once, in the problems
            const typeMissing = source.tables.some((t) => t.schema && !t.installed);
            const name = source.tables.length ? source.tables.map((t) => t.title).join(', ') : source.name;
            const level = levelOf(notes);

            return row({
                level: typeMissing && level === 'ok' ? 'muted' : level,
                name: escapeHtml(name),
                // the source file is only worth showing when it isn't named after its table
                file: source.path && stem(source.path) !== stem(name) ? escapeHtml(source.path) : null,
                size: source.size,
                missing: !source.found,
                notes,
            });
        });

        const dataPackage = result.sources.some((s) => s.tables.some((t) => t.schema));
        const count = dataPackage
            ? msg('contents.tables', result.sources.reduce((n, s) => n + s.tables.length, 0))
            : msg('contents.sources', result.sources.length);

        // the type is only named when it's known here, an uninstalled data package type is in the problems
        const type = result.missingDataPackageType ? null : typeLabel(result.datasetType);
        return {
            kind: msg('kind.iptResource'),
            note: msg('kind.iptResource.note'),
            summary: [type ? escapeHtml(type) : null, count],
            problems: result.problems.map(problemView),
            rows,
            count,
            metadata: result.metadataFiles,
        };
    }

    // ---- Data package ----

    function dataPackageView(result) {
        // a data package type is selected, but the archive has no datapackage.json
        if (result.noManifest) {
            const type = typeLabel(result.datasetType);
            return {
                kind: msg('kind.dataPackage'),
                summary: [type ? escapeHtml(type) : null],
                problems: [{ level: 'danger', text: msg('dp.noManifest'), detail: null, notes: [msg('dp.noManifest.note')] }],
                rows: [],
                count: '',
                metadata: [],
            };
        }

        const allFiles = result.files || [];
        const dataFiles = allFiles.filter((f) => !isMetadataFile(f.path));
        const typeValue = result.actualType?.value !== 'unknown' ? result.actualType?.value : null;
        const typeName = typeLabel(typeValue) ?? typeValue;
        // no installed type fits: said once, rather than for every table
        const typeUnknown = !!result.files && !result.datasetType;

        // tables by name, with their files in and missing from the archive
        const tables = new Map();
        const table = (name) => {
            if (!tables.has(name)) tables.set(name, { name, inSchema: true, files: [] });
            return tables.get(name);
        };
        dataFiles.forEach((f) => f.resources.forEach((r) => {
            table(r.name).inSchema = r.inSchema;
            table(r.name).files.push({ path: f.path, size: f.size, found: true });
        }));
        (result.unresolvedPaths ?? []).forEach((u) => table(u.resourceName).files.push({
            path: typeof u.path === 'string' ? u.path : null,
            size: null,
            found: false,
        }));
        const unmapped = dataFiles.filter((f) => !f.resources.length);

        const problems = [];
        const rows = [];
        for (const t of tables.values()) {
            const missing = t.files.filter((f) => !f.found);
            const missingPaths = missing.map((f) => f.path ?? '').join(', ');
            const notes = [];

            if (!t.inSchema && !typeUnknown) {
                notes.push(['danger', msg('row.notInSchema', typeName)]);
                problems.push({
                    level: 'danger',
                    text: msg('dp.problem.notInSchema', t.name, typeName),
                    detail: escapeHtml(t.files.map((f) => f.path).join(', ')),
                    notes: [msg('dp.problem.notInSchema.note')],
                });
            }
            if (missing.length && missing.length === t.files.length) {
                problems.push({
                    level: 'warning',
                    text: msg('dp.problem.tableSkipped', t.name),
                    detail: escapeHtml(missingPaths),
                    notes: [msg('dp.problem.tableSkipped.note')],
                });
            } else if (missing.length) {
                problems.push({
                    level: 'warning',
                    text: msg('dp.problem.tablePartial', t.name),
                    detail: escapeHtml(missingPaths),
                    notes: [msg('dp.problem.tablePartial.note', missing.length, t.files.length)],
                });
            }

            if (t.files.length === 1) {
                const f = t.files[0];
                if (!f.found) notes.push(['warning', msg('row.missing')]);
                rows.push(row({
                    level: typeUnknown && !notes.length ? 'muted' : levelOf(notes),
                    name: escapeHtml(t.name),
                    file: f.path && stem(f.path) !== stem(t.name) ? escapeHtml(f.path) : null,
                    size: f.size,
                    missing: !f.found,
                    notes,
                }));
            } else {
                // a table with several files lists them under it
                if (missing.length) notes.push(['warning', msg('row.partlyMissing', missing.length, t.files.length)]);
                rows.push(row({ level: levelOf(notes), name: escapeHtml(t.name), notes }));
                t.files.forEach((f) => rows.push(row({
                    level: f.found ? 'ok' : 'warning',
                    name: f.path ? escapeHtml(f.path) : `<em>${msg('noPath')}</em>`,
                    size: f.size,
                    notes: f.found ? [] : [['warning', msg('row.missing')]],
                    indent: true,
                })));
            }
        }
        rows.push(...notImportedRows(unmapped));

        if (unmapped.length) {
            problems.push({
                level: 'warning',
                text: msg('dp.problem.unmapped', unmapped.length),
                detail: escapeHtml(unmapped.map((f) => f.path).join(', ')),
                notes: [],
            });
        }
        if (typeUnknown) {
            problems.unshift({
                level: 'danger',
                text: msg('dp.unknownType'),
                detail: result.profile ? escapeHtml(result.profile) : null,
                notes: [msg('dp.unknownType.note')],
            });
        } else if (result.typeFromTables) {
            problems.unshift({
                level: 'warning',
                text: msg('dp.typeFromTables', typeName),
                detail: result.profile ? escapeHtml(result.profile) : null,
                notes: [msg('dp.typeFromTables.note')],
            });
        }
        // other reasons it can't be imported (no resources, nothing found, unreadable) only come as text
        if (!result.ready && !problems.some((p) => p.level === 'danger')) {
            problems.unshift({
                level: 'danger',
                text: result.reason ? escapeHtml(result.reason) : msg('unreadable'),
                detail: null,
                notes: [],
            });
        }

        const version = result.version?.value && result.version.value !== 'unknown' ? ' ' + result.version.value : '';
        return {
            kind: msg('kind.dataPackage'),
            summary: [
                typeName ? escapeHtml(typeName + version) : null,
                tables.size ? msg('contents.tables', tables.size) : null,
            ],
            problems,
            rows,
            count: msg('contents.tables', tables.size),
            metadata: allFiles.filter((f) => isMetadataFile(f.path)).map((f) => f.path),
        };
    }

    // ---- Markup ----

    function iconMarkup(level) {
        const l = LEVELS[level];
        return `<i class="bi ${l.icon}" style="font-size:16px; line-height:1; color:${l.color};" aria-hidden="true"></i>`;
    }

    // One button per kind of thing to install, on the first problem of that kind, e.g. "Install extensions"
    // for all the missing extensions. Problem -> button markup.
    function installButtons(problems) {
        const buttons = new Map();
        const kinds = new Set();
        for (const p of problems) {
            if (!p.install || kinds.has(p.install.kind)) continue;
            kinds.add(p.install.kind);
            const count = problems.filter((other) => other.install?.kind === p.install.kind).length;
            const label = p.install.kind === 'rowTypes'
                ? msg(count > 1 ? 'install.extensions' : 'install.extension')
                : msg('install.dataPackageType');
            buttons.set(p, `<a href="${escapeHtml(p.install.href)}" class="btn btn-sm btn-outline-gbif-primary" style="white-space:nowrap;">${label}</a>`);
        }
        return buttons;
    }

    function headerMarkup(view, status, totalSize) {
        const s = STATUS[status];
        const title = view.title ?? (status === 'success' ? msg('title.ready')
            : status === 'warning' ? msg('title.warnings', view.problems.length) : msg('title.error'));
        const chips = [view.kind, ...view.summary, totalSize ? escapeHtml(formatSize(totalSize)) : null]
            .filter(Boolean)
            .map((chip) => `<span style="font-size:12px; line-height:16px; padding:3px 9px; border-radius:999px; background:#FFFFFF; border:1px solid #DEE2E6; color:#4E565F;">${chip}</span>`)
            .join('');
        return `
    <div style="display:flex; gap:8px; align-items:flex-start; padding:16px 18px; background:${s.bg};">
      <i class="bi ${s.icon}" style="font-size:16px; line-height:28px; color:${s.fg};" aria-hidden="true"></i>
      <div style="display:flex; flex-direction:column; gap:8px; min-width:0;">
        <div style="font-size:16px; font-weight:500; line-height:28px; color:${s.fg};">${title}</div>
        <div style="display:flex; flex-wrap:wrap; gap:6px;">${chips}</div>
        ${view.note ? `<div style="font-size:13px; color:#5C636A;">${view.note}</div>` : ''}
      </div>
    </div>`;
    }

    function problemsMarkup(problems, status) {
        if (!problems.length) return '';
        const buttons = installButtons(problems);
        const items = problems.map((p) => `
      <div style="display:grid; grid-template-columns:18px minmax(0,1fr) auto; column-gap:12px; align-items:start; padding:12px 18px; border-bottom:1px solid #F1F3F5;">
        <span style="padding-top:2px;">${iconMarkup(p.level)}</span>
        <div style="display:flex; flex-direction:column; gap:4px; min-width:0;">
          <div style="font-size:14px; line-height:18px; color:#212529;">${p.text}</div>
          ${p.detail ? `<div style="font-size:12px; line-height:16px; color:#5C636A; font-family:${MONO}; overflow-wrap:anywhere;">${p.detail}</div>` : ''}
          ${p.notes.map((note) => `<div style="font-size:13px; line-height:18px; color:#5C636A;">${note}</div>`).join('')}
        </div>
        ${buttons.get(p) ?? ''}
      </div>`).join('');
        return `<div style="border-top:1px solid ${STATUS[status].border};">${items}</div>`;
    }

    function rowMarkup(r) {
        if (r.group) {
            return `
      <div style="padding:12px 18px 4px; font-size:11px; font-weight:500; letter-spacing:0.06em; text-transform:uppercase; color:#6C757D;">${r.group}</div>`;
        }
        const notes = r.notes.length
            ? `<div style="font-size:12px;">${r.notes.map(([level, html]) => `<span style="color:${LEVELS[level].text};">${html}</span>`).join('; ')}</div>`
            : '';
        return `
      <div style="display:grid; grid-template-columns:16px minmax(0,1fr) auto 64px; column-gap:10px; align-items:center; padding:7px 18px 7px ${r.indent ? 44 : 18}px;">
        ${iconMarkup(r.level)}
        <div style="display:flex; flex-direction:column; gap:2px; min-width:0;">
          <div style="display:flex; align-items:baseline; gap:8px; flex-wrap:wrap;">
            <span style="font-size:14px; color:${r.level === 'muted' ? '#6C757D' : '#212529'}; overflow-wrap:anywhere;">${r.name}</span>
            ${r.role ? `<span style="font-size:11px; letter-spacing:0.04em; text-transform:uppercase; color:#6C757D;">${r.role}</span>` : ''}
          </div>
          ${notes}
        </div>
        <span style="font-family:${MONO}; font-size:12px; color:#6C757D; overflow-wrap:anywhere; ${r.missing ? 'font-style:italic; text-decoration:line-through;' : ''}">${r.file ?? ''}</span>
        <span style="font-size:12px; color:#6C757D; text-align:right;">${escapeHtml(formatSize(r.size))}</span>
      </div>`;
    }

    function metadataMarkup(paths) {
        if (!paths.length) return '';
        const chips = paths.map((p) => `
        <span style="display:inline-flex; align-items:center; gap:4px; font-size:12px; font-family:${MONO}; padding:2px 8px; border-radius:4px; background:#F1F3F5; color:#4E565F;">
          <i class="bi bi-file-earmark-text" aria-hidden="true"></i>${escapeHtml(p)}</span>`).join('');
        return `
      <span style="display:flex; align-items:center; gap:6px; flex-wrap:wrap;">
        <span style="font-size:12px; color:#5C636A;">${msg('metadata')}</span>${chips}
      </span>`;
    }

    // Collapsible contents, a native <details> (styles in create_input.ftl), with the metadata files beside the toggle
    function contentsMarkup(view, open) {
        const bar = 'display:flex; align-items:center; justify-content:space-between; gap:12px; flex-wrap:wrap; min-height:44px; padding:0 18px; border-top:1px solid #EEF0F2;';
        if (!view.rows.length) {
            return view.metadata.length ? `<div style="${bar}"><span></span>${metadataMarkup(view.metadata)}</div>` : '';
        }
        return `
    <details class="precheck-contents"${open ? ' open' : ''}>
      <summary style="${bar} cursor:pointer;">
        <span style="display:flex; align-items:center; gap:8px; font-size:14px; color:#2C6E9E;">
          <i class="bi bi-chevron-right precheck-chevron" aria-hidden="true"></i>
          <span class="precheck-show">${msg('contents.show')} · ${view.count}</span>
          <span class="precheck-hide">${msg('contents.hide')} · ${view.count}</span>
        </span>
        ${metadataMarkup(view.metadata)}
      </summary>
      <div style="padding:4px 0 10px; border-top:1px solid #EEF0F2;">
        ${view.rows.map(rowMarkup).join('')}
      </div>
    </details>`;
    }

    function viewOf(result) {
        if (result.type === 'data-package') return dataPackageView(result);
        if (result.type === 'dwc-a') return dwcaView(result);
        if (result.type === 'ipt-resource') return resourceFolderView(result);
        return null;
    }

    function renderPrecheckPanel(result, container, translatedMessages, panelOptions) {
        messages = translatedMessages ?? {};
        options = panelOptions ?? {};

        const view = viewOf(result);
        if (!view) {
            container.innerHTML = '';
            return;
        }

        // the archive couldn't be fully read here, so its problems aren't a reason to stop the upload
        if (result.unverified) {
            view.problems.filter((p) => p.level === 'danger').forEach((p) => { p.level = 'warning'; });
            view.title = msg('title.unverified');
        }

        const status = statusOf(view.problems);
        // Open when a file has a problem. Shown again for the same archive (with the registry's answer),
        // it stays as the person left it.
        const previous = container.precheckResult === result ? container.querySelector('.precheck-contents') : null;
        const open = previous
            ? previous.open
            : view.rows.some((r) => r.group || r.level === 'warning' || r.level === 'danger');
        container.precheckResult = result;

        container.innerHTML = `
  <section class="mt-3" style="background:#FFFFFF; border:1px solid ${STATUS[status].border}; border-radius:10px; overflow:hidden;">
    ${headerMarkup(view, status, result.totalSize)}
    ${problemsMarkup(view.problems, status)}
    ${contentsMarkup(view, open)}
  </section>`;
    }

    global.renderPrecheckPanel = renderPrecheckPanel;
})(window);
