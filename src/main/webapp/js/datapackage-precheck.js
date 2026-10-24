/**
 * Verifies the content of an archive before it's uploaded, mirroring what the server
 * checks when creating a resource from it:
 * - IPT resource folder: resource.xml, its sources and whether the mapped extensions are installed
 * - data package: datapackage.json against the available table schemas
 * - Darwin Core Archive: meta.xml (or a single data file) against the installed extensions
 * Does NOT inspect table contents or relationships between tables — structural existence only.
 *
 * Reads only the ZIP central directory plus the descriptor entries (and the start of a data file
 * without meta.xml), via ranged File.slice() calls, so it never loads the whole archive into memory.
 *
 * Usage:
 *   const result = await DataPackagePrecheck.run(fileInput.files[0], dataPackages, installed, selectedType);
 *   // selectedType -> the resource type selected, if any: like on the server, a data package type checks the archive
 *   //   as that data package, any other type as a DwC-A. None: the type is guessed from the archive.
 *   // DataPackagePrecheck.blocksUpload(result) -> true when the server would reject the archive
 *   // installed.extensions -> [{ rowType, title }]
 *   // installed.dataPackageIdentifiers -> [[identifier, ...]], the identifiers accepted for each data package type
 *   // result.type   -> 'ipt-resource' | 'data-package' | 'dwc-a' | 'unknown'
 *
 *   // result.totalSize -> uncompressed size of all files, in bytes (every type)
 *   // result.unverified -> true when an entry couldn't be read here (e.g. an unsupported compression)
 *
 * Resource folders and DwC-As: see checkResourceFolder() and dwcaResult() for the result,
 * problems are in result.problems, see problem().
 * DataPackagePrecheck.missingTypes(result) lists the extensions and data package types it needs but aren't installed.
 *
 * Data packages:
 *   // result.ready  -> boolean, can IPT create a resource from this
 *   // result.reason -> string explaining a not-ready state, else null
 *   // result.actualType, result.version -> data package type and version from datapackage.json,
 *   //   or from the installed type having all its tables (result.typeFromTables) when its profile isn't known
 *   // result.datasetType -> the type to select, null if no installed type fits
 *   // result.files -> [{ path, size, resources: [{ name, inSchema }] }]
 *   // result.unmappedFiles -> entries of result.files not referenced by any resource
 *   // result.skippedResources -> string[], resources with none of their files in the archive
 *   // result.unresolvedPaths -> [{ resourceName, path }], declared paths missing from the archive
 */
(function (global) {
    'use strict';

    const EOCD_SIGNATURE = 0x06054b50;
    const CENTRAL_DIR_SIGNATURE = 0x02014b50;
    const LOCAL_HEADER_SIGNATURE = 0x04034b50;
    const EOCD_MIN_SIZE = 22;
    const MAX_COMMENT_SIZE = 65535; // zip comment field is at most 64KB per spec

    function readSlice(file, start, end) {
        return file.slice(start, end).arrayBuffer();
    }

    // The end-of-central-directory record sits at the end of the file, after
    // an optional comment of unknown length, so we search backward for its
    // signature within the maximum possible comment size.
    async function findEndOfCentralDirectory(file) {
        const searchSize = Math.min(file.size, EOCD_MIN_SIZE + MAX_COMMENT_SIZE);
        const start = file.size - searchSize;
        const buf = new DataView(await readSlice(file, start, file.size));

        for (let i = buf.byteLength - EOCD_MIN_SIZE; i >= 0; i--) {
            if (buf.getUint32(i, true) === EOCD_SIGNATURE) {
                return {
                    centralDirSize: buf.getUint32(i + 12, true),
                    centralDirOffset: buf.getUint32(i + 16, true),
                };
            }
        }
        throw new Error('No end-of-central-directory record found — not a valid zip file');
    }

    async function readCentralDirectory(file) {
        const eocd = await findEndOfCentralDirectory(file);
        const buf = new DataView(
            await readSlice(file, eocd.centralDirOffset, eocd.centralDirOffset + eocd.centralDirSize)
        );
        const decoder = new TextDecoder('utf-8');
        const entries = [];
        let offset = 0;

        while (offset < buf.byteLength) {
            if (buf.getUint32(offset, true) !== CENTRAL_DIR_SIGNATURE) break;

            const method = buf.getUint16(offset + 10, true);
            const compressedSize = buf.getUint32(offset + 20, true);
            const size = buf.getUint32(offset + 24, true); // uncompressed
            const nameLen = buf.getUint16(offset + 28, true);
            const extraLen = buf.getUint16(offset + 30, true);
            const commentLen = buf.getUint16(offset + 32, true);
            const localHeaderOffset = buf.getUint32(offset + 42, true);
            const nameBytes = new Uint8Array(buf.buffer, buf.byteOffset + offset + 46, nameLen);
            const name = decoder.decode(nameBytes);

            entries.push({ name, isDirectory: name.endsWith('/'), method, compressedSize, size, localHeaderOffset });
            offset += 46 + nameLen + extraLen + commentLen;
        }

        return entries;
    }

    // Local file headers can pad name/extra fields slightly differently than
    // the central directory does, so we read the local header to find the
    // real start of the compressed data rather than assuming an offset.
    async function compressedData(file, entry) {
        const headBuf = new DataView(
            await readSlice(file, entry.localHeaderOffset, entry.localHeaderOffset + 30)
        );
        if (headBuf.getUint32(0, true) !== LOCAL_HEADER_SIGNATURE) {
            throw new Error(`Corrupt local file header for ${entry.name}`);
        }
        const nameLen = headBuf.getUint16(26, true);
        const extraLen = headBuf.getUint16(28, true);
        const dataStart = entry.localHeaderOffset + 30 + nameLen + extraLen;
        return file.slice(dataStart, dataStart + entry.compressedSize);
    }

    // Stream of the uncompressed entry content, null for stored (uncompressed) entries
    function inflate(entry, compressed) {
        if (entry.method === 0) return null;

        if (entry.method === 8) {
            if (typeof DecompressionStream === 'undefined') {
                throw new Error(
                    'This browser has no native inflate support (DecompressionStream). ' +
                    'Vendor a small fallback inflate library for older browsers if needed.'
                );
            }
            return compressed.stream().pipeThrough(new DecompressionStream('deflate-raw'));
        }

        throw new Error(`Unsupported compression method (${entry.method}) for ${entry.name}`);
    }

    // An entry the browser couldn't read (compression it doesn't support, corrupt data...): says nothing about
    // whether the server can import the archive, so a result with one never blocks the upload
    class ReadError extends Error {}

    async function extractEntry(file, entry) {
        try {
            const compressed = await compressedData(file, entry);
            const stream = inflate(entry, compressed);
            return new Uint8Array(await (stream ? new Response(stream) : compressed).arrayBuffer());
        } catch (err) {
            throw new ReadError(err.message);
        }
    }

    // Only the first maxBytes of the entry, so a big data file is never inflated completely
    async function extractEntryHead(file, entry, maxBytes = 64 * 1024) {
        try {
            return await readEntryHead(file, entry, maxBytes);
        } catch (err) {
            throw new ReadError(err.message);
        }
    }

    async function readEntryHead(file, entry, maxBytes) {
        const compressed = await compressedData(file, entry);
        const stream = inflate(entry, compressed);
        if (!stream) return new Uint8Array(await compressed.slice(0, maxBytes).arrayBuffer());

        const reader = stream.getReader();
        const chunks = [];
        let size = 0;
        while (size < maxBytes) {
            const { done, value } = await reader.read();
            if (done) break;
            chunks.push(value);
            size += value.length;
        }
        reader.cancel().catch(() => {});

        const head = new Uint8Array(size);
        let offset = 0;
        for (const chunk of chunks) {
            head.set(chunk, offset);
            offset += chunk.length;
        }
        return head;
    }

    function normalizePath(p) {
        return p.replace(/^\.\//, '').replace(/\\/g, '/').replace(/^\/+/, '').toLowerCase();
    }

    function isMacZipArtifact(name) {
        return (
            name.startsWith('__MACOSX/') ||
            name.split('/').pop() === '.DS_Store' ||
            name.split('/').pop().startsWith('._')
        );
    }

    const datasetTypeMap = {
        "Occurrence": "occurrence",
        "Checklist": "checklist",
        "Samplingevent": "samplingevent",
        "Material entity": "materialentity",
        "Metadata": "metadata",
        "Other": "other",
        "camtrap-dp": "camtrap-dp",
        "coldp": "coldp",
        "dwc-dp": "dwc-dp"
    };

    function inferActualType(manifest) {
        var result = {
            value: 'unknown',
            comment: "Could not infer dataset type"
        }

        try {
            const profile = (manifest.profile || '').toLowerCase();
            const resources = manifest.resources || [];

            if (profile.includes('/dwc-dp/')) {
                result.value = datasetTypeMap['dwc-dp'];
                result.comment = "Inferred from profile";
            } else if (profile.includes('/camtrap-dp/')) {
                result.value = datasetTypeMap['camtrap-dp'];
                result.comment = "Inferred from profile";
            } else if (profile.includes('/coldp/')) {
                result.value = datasetTypeMap['coldp'];
                result.comment = "Inferred from profile";
            } else if (profile.includes('data-package')) {
                const identifier = resources[0]?.schema?.identifier?.toLowerCase() || '';

                if (identifier.includes('/dwc-dp/')) {
                    result.value = datasetTypeMap['dwc-dp'];
                    result.comment = "Inferred from resources";
                } else if (identifier.includes('/coldp/')) {
                    result.value = datasetTypeMap['coldp'];
                    result.comment = "Inferred from resources";
                }
            }
        } catch (e) {
            console.log('Invalid datapackage.json: ' + e.message);
        }

        return result;
    }

    function inferVersion(manifest) {
        var result = {
            value: 'unknown',
            comment: "Could not infer version"
        }

        try {
            const profile = (manifest.profile || '').toLowerCase();
            const resources = manifest.resources || [];

            if (profile.includes('/dwc-dp/')) {
                result.value = extractVersion(profile, 'dwc-dp');
                result.comment = "Inferred from profile";
            } else if (profile.includes('/camtrap-dp/')) {
                result.value = extractVersion(profile, 'camtrap-dp');
                result.comment = "Inferred from profile";
            } else if (profile.includes('/coldp/')) {
                result.value = extractVersion(profile, 'coldp');
                result.comment = "Inferred from profile";
            } else if (profile.includes('data-package')) {
                const identifier = resources[0]?.schema?.url?.toLowerCase() || '';

                if (identifier.includes('/dwc-dp/')) {
                    result.value = extractVersion(identifier, 'dwc-dp');
                    result.comment = "Inferred from resources";
                } else if (identifier.includes('/coldp/')) {
                    result.value = extractVersion(identifier, 'coldp');
                    result.comment = "Inferred from resources";
                }
            }
        } catch (e) {
            console.log('Invalid datapackage.json: ' + e.message);
        }

        return result;
    }

    function findDataPackageSchema(availableDataPackages, actualType) {
        return availableDataPackages.find(item => item.name === actualType.value);
    }

    function extractVersion(url, keyword) {
        // Escape keyword in case it has regex special chars
        const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

        // Match: /<keyword>/<segment>/
        const match = url.match(new RegExp(`/${escapedKeyword}/([^/]+)/`));
        if (!match) return null;

        const candidate = match[1];

        // Validate that candidate actually looks like a version.
        // TODO: This might need an adjustment
        const versionPattern = /^\d+(\.\d+)*([_-][A-Za-z0-9]+)?$/;

        if (!versionPattern.test(candidate)) return null;

        return candidate;
    }

    // ---- Archive root, as the server sees it after unzipping ----

    // The server skips hidden files and removes a single top folder when unzipping (CompressionUtil),
    // so paths are taken relative to that folder. Keyed by normalized path, keeps the original for display.
    function archiveRootFiles(fileEntries) {
        const visible = fileEntries
            .filter((e) => !e.name.split('/').pop().startsWith('.'))
            .map((e) => ({ entry: e, original: e.name.replace(/^\.\//, '').replace(/\\/g, '/').replace(/^\/+/, '') }));

        const topFolders = new Set(visible.map((f) => (f.original.includes('/') ? f.original.split('/')[0] : '')));
        const prefix = topFolders.size === 1 && !topFolders.has('') ? [...topFolders][0] + '/' : '';

        return new Map(
            visible.map((f) => {
                const path = f.original.slice(prefix.length);
                return [path.toLowerCase(), { entry: f.entry, path }];
            })
        );
    }

    function parseXml(bytes) {
        const doc = new DOMParser().parseFromString(new TextDecoder('utf-8').decode(bytes), 'application/xml');
        const error = doc.getElementsByTagName('parsererror')[0];
        // Chrome wraps the message in a "This page contains the following errors" page, the <div> has the message
        if (error) throw new Error((error.querySelector('div') ?? error).textContent.trim().split('\n')[0]);
        return doc;
    }

    function childElements(parent, localName) {
        return parent ? [...parent.children].filter((el) => !localName || el.localName === localName) : [];
    }

    function childText(parent, localName) {
        return childElements(parent, localName)[0]?.textContent.trim() ?? null;
    }

    function installedTable(rowType, installedByRowType) {
        const installed = installedByRowType.get(rowType);
        return {
            rowType,
            title: installed?.title || rowType.split(/[/#]/).pop(),
            installed: !!installed,
        };
    }

    // ---- Darwin Core Archive ----

    const DWC_NS = 'http://rs.tdwg.org/dwc/terms/';

    // Resource type the server derives from the core (ResourceImportServiceImpl), anything else is "other"
    const CORE_DATASET_TYPES = {
        [DWC_NS + 'Occurrence']: 'occurrence',
        [DWC_NS + 'Taxon']: 'checklist',
        [DWC_NS + 'Event']: 'samplingevent',
    };

    // Without meta.xml the core type comes from the first of these columns found in the header (dwca-io)
    const ID_COLUMN_ROW_TYPES = [
        ['occurrenceid', DWC_NS + 'Occurrence'],
        ['taxonid', DWC_NS + 'Taxon'],
        ['eventid', DWC_NS + 'Event'],
    ];

    // Files dwca-io takes as data files when there is no meta.xml
    const DATA_FILE_EXTENSIONS = ['.csv', '.txt', '.tsv', '.tab', '.text', '.data', '.dwca'];

    const DWCA_METADATA_FILES = new Set(['meta.xml', 'eml.xml', 'metadata.xml']);

    // A problem the panel shows, key and args name a manage.resource.create.precheck.* text.
    // extra: detail (shown as is, e.g. a row type or file names), note ({ key, args }),
    // install ({ kind: 'rowTypes' | 'dataPackageTypes', id }: something missing that might be installable)
    function problem(level, key, args = [], extra = {}) {
        return { level, key, args, ...extra };
    }

    function sizeOf(rootFiles, path) {
        return path ? rootFiles.get(normalizePath(path))?.entry.size ?? null : null;
    }

    function dwcaResult(layout) {
        return {
            type: 'dwc-a',
            layout, // 'meta-xml' | 'single-file'
            ready: false,
            datasetType: null,
            // [{ role, rowType, title, installed, path, size, found, extraPaths, idMissing, skipped, detectedFrom }]
            tables: [],
            unmappedFiles: [], // [{ path, size }]
            metadataFiles: [], // paths
            problems: [], // see problem()
        };
    }

    function finishDwca(result) {
        const core = result.tables.find((t) => t.role === 'core');
        if (core?.rowType) result.datasetType = CORE_DATASET_TYPES[core.rowType] ?? 'other';
        result.ready = !result.problems.some((p) => p.level === 'error');
        return result;
    }

    function notInstalledProblem(table) {
        return problem('error', table.role === 'core' ? 'dwca.problem.coreNotInstalled' : 'dwca.problem.extensionNotInstalled',
            [table.title], { detail: table.rowType, install: { kind: 'rowTypes', id: table.rowType } });
    }

    // Column name as a bare lowercase term name, e.g. "dwc:occurrenceID" or a term URI -> "occurrenceid"
    function normalizeColumn(column) {
        return column.split(/[/#:]/).pop().replace(/[^A-Za-z0-9]/g, '').toLowerCase();
    }

    function headerColumns(bytes) {
        const line = new TextDecoder('utf-8').decode(bytes).replace(/^﻿/, '').split(/\r?\n/)[0];
        // The delimiter used most in the header line, tab wins a tie
        const delimiter = ['\t', ',', ';', '|']
            .map((d) => [d, line.split(d).length])
            .reduce((best, d) => (d[1] > best[1] ? d : best))[0];
        return line.split(delimiter).map((c) => c.trim().replace(/^"(.*)"$/, '$1'));
    }

    async function checkMetaXml(file, rootFiles, installedByRowType) {
        const result = dwcaResult('meta-xml');
        const referenced = new Set(['meta.xml']);

        let archive;
        try {
            archive = parseXml(await extractEntry(file, rootFiles.get('meta.xml').entry)).documentElement;
        } catch (err) {
            result.unverified = err instanceof ReadError;
            result.problems.push(problem('error', 'dwca.problem.unreadable', [], { detail: err.message }));
            return finishDwca(result);
        }

        const core = childElements(archive, 'core')[0];
        const extensions = childElements(archive, 'extension');
        if (!core) {
            result.problems.push(problem('error', 'dwca.problem.noCore'));
        } else if (!core.getAttribute('rowType')) {
            result.problems.push(problem('error', 'dwca.problem.noRowType'));
        }

        // dwca-io drops an extension without <coreid>, the server never sees it
        const linkedExtensions = extensions.filter((el) => childElements(el, 'coreid').length);

        const declared = [...(core ? [[core, 'core']] : []), ...extensions.map((el) => [el, 'extension'])];
        for (const [el, role] of declared) {
            const rowType = el.getAttribute('rowType');
            const locations = [...el.getElementsByTagNameNS('*', 'location')].map((l) => l.textContent.trim());
            locations.forEach((l) => referenced.add(normalizePath(l)));
            const [path = null, ...extraPaths] = locations;
            // The server only needs the core <id> when there are extensions to link to it
            const idElement = childElements(el, role === 'core' ? 'id' : 'coreid')[0];

            result.tables.push({
                role,
                ...(rowType ? installedTable(rowType, installedByRowType) : { rowType: null, title: null, installed: false }),
                path,
                size: sizeOf(rootFiles, path),
                found: path !== null && rootFiles.has(normalizePath(path)),
                extraPaths,
                idMissing: !idElement && (role === 'extension' || linkedExtensions.length > 0),
                skipped: role === 'extension' && !idElement,
                detectedFrom: null,
            });
        }

        const metadataAttribute = archive.getAttribute('metadata');
        if (metadataAttribute) referenced.add(normalizePath(metadataAttribute));

        for (const [key, f] of rootFiles) {
            if (DWCA_METADATA_FILES.has(key) || (metadataAttribute && key === normalizePath(metadataAttribute))) {
                result.metadataFiles.push(f.path);
            } else if (!referenced.has(key)) {
                result.unmappedFiles.push({ path: f.path, size: f.entry.size });
            }
        }

        // Each of these makes the server reject the archive, the skipped extensions aside
        for (const table of result.tables.filter((t) => !t.skipped)) {
            if (table.rowType && !table.installed) result.problems.push(notInstalledProblem(table));
            if (!table.found) {
                result.problems.push(table.path
                    ? problem('error', 'dwca.problem.fileMissing', [table.path])
                    : problem('error', 'dwca.problem.noFile', [table.title ?? table.role]));
            }
            if (table.idMissing) result.problems.push(problem('error', 'dwca.problem.noId'));
        }

        for (const table of result.tables.filter((t) => t.skipped)) {
            result.problems.push(problem('warning', 'dwca.problem.extensionSkipped', [table.title ?? table.path],
                { note: { key: 'dwca.problem.extensionSkipped.note', args: [table.path ?? ''] } }));
        }

        // The server only imports the first file of each table
        const extraPaths = result.tables.flatMap((t) => t.extraPaths);
        if (extraPaths.length) {
            result.problems.push(problem('warning', 'dwca.problem.extraLocations', [extraPaths.length],
                { detail: extraPaths.join(', ') }));
        }
        if (result.unmappedFiles.length) {
            result.problems.push(problem('warning', 'dwca.problem.unmapped', [result.unmappedFiles.length],
                { detail: result.unmappedFiles.map((f) => f.path).join(', ') }));
        }

        return finishDwca(result);
    }

    async function checkSingleDataFile(file, rootFiles, installedByRowType) {
        const result = dwcaResult('single-file');

        // dwca-io only looks at files directly in the archive root
        const dataFiles = [];
        for (const [key, f] of rootFiles) {
            if (DWCA_METADATA_FILES.has(key)) {
                result.metadataFiles.push(f.path);
            } else if (!key.includes('/') && DATA_FILE_EXTENSIONS.some((ext) => key.endsWith(ext))) {
                dataFiles.push(f);
            } else {
                result.unmappedFiles.push({ path: f.path, size: f.entry.size });
            }
        }

        if (dataFiles.length !== 1) {
            result.unmappedFiles.unshift(...dataFiles.map((f) => ({ path: f.path, size: f.entry.size })));
            result.problems.push(dataFiles.length
                ? problem('error', 'dwca.problem.manyDataFiles', [dataFiles.length],
                    { detail: dataFiles.map((f) => f.path).join(', ') })
                : problem('error', 'dwca.problem.noDataFiles'));
            return finishDwca(result);
        }

        const dataFile = dataFiles[0];
        let columns = [];
        try {
            columns = headerColumns(await extractEntryHead(file, dataFile.entry));
        } catch (err) {
            result.unverified = err instanceof ReadError;
            result.problems.push(problem('error', 'dwca.problem.dataUnreadable', [dataFile.path], { detail: err.message }));
        }

        const normalized = columns.map(normalizeColumn);
        const match = ID_COLUMN_ROW_TYPES.find(([column]) => normalized.includes(column));
        const core = {
            role: 'core',
            ...(match ? installedTable(match[1], installedByRowType) : { rowType: null, title: null, installed: false }),
            path: dataFile.path,
            size: dataFile.entry.size,
            found: true,
            extraPaths: [],
            idMissing: false,
            skipped: false,
            detectedFrom: match ? columns[normalized.indexOf(match[0])] : null,
        };
        result.tables.push(core);

        if (!match && columns.length) {
            result.problems.push(problem('error', 'dwca.problem.noIdColumn', [],
                { note: { key: 'dwca.problem.noIdColumn.note', args: [] } }));
        } else if (match && !core.installed) {
            result.problems.push(notInstalledProblem(core));
        }
        if (result.unmappedFiles.length) {
            result.problems.push(problem('warning', 'dwca.problem.otherFiles', [result.unmappedFiles.length],
                { detail: result.unmappedFiles.map((f) => f.path).join(', ') }));
        }

        return finishDwca(result);
    }

    // ---- IPT resource folder (a resource directory copied from another IPT's data directory) ----

    const RESOURCE_FOLDER_METADATA_FILES = ['eml.xml', 'datapackage.json', 'metadata.yml'];

    // Same test as ResourceManagerImpl.isIPTResourceFolder
    function isResourceFolder(rootFiles) {
        return rootFiles.has('resource.xml') && RESOURCE_FOLDER_METADATA_FILES.some((f) => rootFiles.has(f));
    }

    // resource.xml is written by XStream, which replaces repeated objects with a relative XPath reference
    function dereference(el) {
        const reference = el?.getAttribute('reference');
        if (!reference) return el;
        return el.ownerDocument
            .evaluate(reference, el, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)
            .singleNodeValue;
    }

    // Same matching as DataPackageSchemaManager.get(): any accepted identifier, ignoring the protocol
    function dataPackageTypeInstalled(identifier, dataPackageIdentifiers) {
        const stripped = (id) => id.replace(/^https?:\/\//, '');
        return dataPackageIdentifiers.some((ids) => ids.some((id) => stripped(id) === stripped(identifier)));
    }

    async function checkResourceFolder(file, rootFiles, installedByRowType, dataPackageIdentifiers) {
        const result = {
            type: 'ipt-resource',
            ready: false,
            datasetType: null,
            // [{ name, kind, path, size, found, tables: [{ rowType, title, installed, schema }] }]
            sources: [],
            missingDataPackageType: false,
            metadataFiles: ['resource.xml', ...RESOURCE_FOLDER_METADATA_FILES]
                .filter((f) => rootFiles.has(f))
                .map((f) => rootFiles.get(f).path),
            problems: [], // see problem()
        };

        let resource;
        try {
            resource = parseXml(await extractEntry(file, rootFiles.get('resource.xml').entry)).documentElement;
        } catch (err) {
            result.unverified = err instanceof ReadError;
            result.problems.push(problem('error', 'ipt.problem.unreadable', [], { detail: err.message }));
            return result;
        }

        result.datasetType = childText(resource, 'coreType')?.toLowerCase() || null;

        // Sources are stored as sources/<name>.<suffix>, database sources have no file
        const sourcesByElement = new Map();
        for (const el of childElements(childElements(resource, 'sources')[0])) {
            const name = childText(el, 'name') ?? '';
            const hasFile = el.localName !== 'sqlsource';
            const prefix = `sources/${name.toLowerCase()}.`;
            const stored = [...rootFiles.keys()].find((k) => k.startsWith(prefix) && !k.endsWith('.log'));
            const source = {
                name,
                kind: el.localName,
                path: stored ? rootFiles.get(stored).path : hasFile ? `sources/${name}` : null,
                size: stored ? rootFiles.get(stored).entry.size : null,
                found: !hasFile || !!stored,
                tables: [],
            };
            sourcesByElement.set(el, source);
            result.sources.push(source);
        }

        for (const mapping of childElements(childElements(resource, 'mappings')[0], 'mapping')) {
            const source = sourcesByElement.get(dereference(childElements(mapping, 'source')[0]));
            const rowType = dereference(childElements(mapping, 'extension')[0])?.textContent.trim();
            if (source && rowType) source.tables.push(installedTable(rowType, installedByRowType));
        }

        const missingTypes = new Set();
        const dataPackageType = childText(resource, 'dataPackageIdentifier');
        if (dataPackageType && !dataPackageTypeInstalled(dataPackageType, dataPackageIdentifiers)) {
            missingTypes.add(dataPackageType);
        }

        for (const mapping of childElements(childElements(resource, 'dataPackageMappings')[0], 'dataPackageMapping')) {
            const source = sourcesByElement.get(dereference(childElements(mapping, 'source')[0]));
            const schema = dereference(childElements(mapping, 'dataPackageSchema')[0])?.textContent.trim();
            const installed = !!schema && dataPackageTypeInstalled(schema, dataPackageIdentifiers);
            if (!installed && schema) missingTypes.add(schema);
            if (source) {
                source.tables.push({
                    rowType: schema,
                    title: childText(mapping, 'dataPackageTableSchemaName'),
                    installed,
                    schema: true,
                });
            }
        }

        // Mappings to extensions or a data package type that aren't installed make the server reject the folder
        // (ResourceLoaderImpl)
        const missingExtensions = new Map();
        result.sources.flatMap((s) => s.tables).filter((t) => !t.installed && !t.schema)
            .forEach((t) => missingExtensions.set(t.rowType, t));
        for (const t of missingExtensions.values()) {
            result.problems.push(problem('error', 'ipt.problem.extensionNotInstalled', [t.title],
                { detail: t.rowType, install: { kind: 'rowTypes', id: t.rowType } }));
        }
        for (const type of missingTypes) {
            result.problems.push(problem('error', 'ipt.problem.dataPackageTypeNotInstalled', [type.split('/').pop()],
                { detail: type, install: { kind: 'dataPackageTypes', id: type } }));
        }
        result.missingDataPackageType = missingTypes.size > 0;

        for (const source of result.sources.filter((s) => !s.found)) {
            result.problems.push(problem('warning', 'ipt.problem.sourceMissing', [source.name],
                { note: { key: 'ipt.problem.sourceMissing.note', args: [] } }));
        }

        result.ready = !result.problems.some((p) => p.level === 'error');
        return result;
    }

    // ---- Entry point ----

    async function run(file, availableDataPackages, installed = {}, selectedType = null) {
        let entries;
        try {
            entries = await readCentralDirectory(file);
        } catch (err) {
            return { type: 'unknown', ready: false, reason: 'Could not read this as a zip archive: ' + err.message };
        }

        const fileEntries = entries.filter((e) => !e.isDirectory && !isMacZipArtifact(e.name));
        const rootFiles = archiveRootFiles(fileEntries);
        const installedByRowType = new Map((installed.extensions ?? []).map((e) => [e.rowType, e]));
        const withTotalSize = (result) => ({
            ...result,
            totalSize: [...rootFiles.values()].reduce((sum, f) => sum + f.entry.size, 0),
        });

        // Checked first, like on the server: a resource folder can contain a datapackage.json too
        if (isResourceFolder(rootFiles)) {
            return withTotalSize(
                await checkResourceFolder(file, rootFiles, installedByRowType, installed.dataPackageIdentifiers ?? []));
        }

        // Find datapackage.json — prefer the shallowest one if there are multiple.
        const manifestCandidates = fileEntries
            .map((e) => ({ entry: e, normalized: normalizePath(e.name) }))
            .filter(({ normalized }) => normalized.split('/').pop() === 'datapackage.json')
            .sort((a, b) => a.normalized.split('/').length - b.normalized.split('/').length);

        const manifestCandidate = manifestCandidates[0];

        // ColDP's own metadata without a datapackage.json isn't checked here, it isn't a DwC-A either
        const colDpMetadata = ['metadata.yml', 'metadata.yaml'].some((f) => rootFiles.has(f));

        // The server imports a data package only with a data package type selected, anything else as a DwC-A.
        // Nothing selected yet: guessed from the archive, the page then selects the type found.
        const dataPackageTypes = new Set(availableDataPackages.map((s) => s.name));
        const asDataPackage = selectedType ? dataPackageTypes.has(selectedType) : !!manifestCandidate;

        if (!asDataPackage) {
            if (colDpMetadata && !manifestCandidate && !rootFiles.has('meta.xml')) return { type: 'unknown', ready: false };
            return withTotalSize(rootFiles.has('meta.xml')
                ? await checkMetaXml(file, rootFiles, installedByRowType)
                : await checkSingleDataFile(file, rootFiles, installedByRowType));
        }

        if (!manifestCandidate) {
            if (colDpMetadata) return { type: 'unknown', ready: false };
            return withTotalSize({ type: 'data-package', ready: false, noManifest: true, datasetType: selectedType });
        }

        const manifestEntry = manifestCandidate.entry;
        const manifestNormalized = manifestCandidate.normalized;

        // Root = the folder containing datapackage.json (could be '' if at zip root)
        const rootPrefix = manifestNormalized.slice(0, manifestNormalized.length - 'datapackage.json'.length);

        // Only consider files inside that root, keyed by path *relative to the root*
        const byNormalizedPath = new Map(
            fileEntries
                .map((e) => ({ entry: e, normalized: normalizePath(e.name) }))
                .filter(({ normalized }) => normalized.startsWith(rootPrefix))
                .map(({ entry, normalized }) => [normalized.slice(rootPrefix.length), entry])
        );

        let manifest;
        try {
            const bytes = await extractEntry(file, manifestEntry);
            manifest = JSON.parse(new TextDecoder('utf-8').decode(bytes));
        } catch (err) {
            return withTotalSize({
                type: 'data-package',
                ready: false,
                unverified: err instanceof ReadError,
                reason: 'datapackage.json could not be read: ' + err.message,
            });
        }

        const resources = Array.isArray(manifest.resources) ? manifest.resources : [];

        let actualType = inferActualType(manifest);
        let version = inferVersion(manifest);
        let dataPackageSchema = findDataPackageSchema(availableDataPackages, actualType);

        const selectedSchema = selectedType && availableDataPackages.find((s) => s.name === selectedType);
        if (selectedSchema) {
            dataPackageSchema = selectedSchema;
            if (actualType.value !== selectedSchema.name) {
                actualType = { value: selectedSchema.name, comment: 'Selected' };
                version = { value: selectedSchema.version, comment: 'Installed version' };
            }
        }

        // The server takes the type from the type select, not the profile. Without a known profile, the type is
        // the only installed one having all the tables, which the page then selects.
        let typeFromTables = false;
        if (!dataPackageSchema) {
            const names = resources.map((r) => r.name).filter(Boolean);
            const matching = availableDataPackages.filter((schema) => names.length
                && names.every((name) => (schema.tableSchemas ?? []).some((t) => t.name === name)));
            if (matching.length === 1) {
                dataPackageSchema = matching[0];
                actualType = { value: dataPackageSchema.name, comment: 'Inferred from tables' };
                version = { value: dataPackageSchema.version, comment: 'Installed version' };
                typeFromTables = true;
            }
        }

        const knownTableNames = new Set(
            (dataPackageSchema?.tableSchemas ?? []).map((t) => t.name)
        );

        // normalizedPath (relative to root) -> Set<resourceName>
        const pathToResources = new Map();
        const unresolvedPaths = []; // { resourceName, path }, doesn't affect ready
        const skippedResources = new Set(); // resource names with zero resolved paths

        for (const resource of resources) {
            const name = resource.name || '(unnamed resource)';
            // An empty path list is treated like a missing path, so the resource still shows up as unresolved
            const declaredPaths = Array.isArray(resource.path) && resource.path.length ? resource.path : [resource.path];
            let resolvedAny = false;

            for (const p of declaredPaths) {
                if (typeof p !== 'string') {
                    unresolvedPaths.push({ resourceName: name, path: p });
                    continue;
                }
                const normalized = normalizePath(p);
                if (byNormalizedPath.has(normalized)) {
                    if (!pathToResources.has(normalized)) pathToResources.set(normalized, new Set());
                    pathToResources.get(normalized).add(name);
                    resolvedAny = true;
                } else {
                    unresolvedPaths.push({ resourceName: name, path: p });
                }
            }

            if (!resolvedAny) {
                skippedResources.add(name);
            }
        }

        const fileResults = [...byNormalizedPath.keys()].map((path) => {
            const mappedResources = [...(pathToResources.get(path) ?? [])];
            return {
                path,
                size: byNormalizedPath.get(path).size,
                resources: mappedResources.map((name) => ({
                    name,
                    inSchema: knownTableNames.has(name),
                })),
            };
        });

        const IGNORED_UNMAPPED_FILES = new Set(['datapackage.json', 'eml.xml']);
        const unmappedFiles = fileResults.filter(
            (f) => f.resources.length === 0 && !IGNORED_UNMAPPED_FILES.has(f.path)
        );

        const resolvedResourceNames = new Set(
            fileResults.flatMap((f) => f.resources.map((r) => r.name))
        );

        const unknownSchemaResources = [
            ...new Set(
                fileResults
                    .flatMap((f) => f.resources)
                    .filter((r) => !r.inSchema)
                    .map((r) => r.name)
            ),
        ];

        // Ready if at least one resource actually resolved to a file, and none of
        // the resolved resources are unrecognized by the schema.
        const ready =
            resolvedResourceNames.size > 0 &&
            unknownSchemaResources.length === 0;

        const reasons = [];
        if (resources.length === 0) {
            reasons.push('datapackage.json declares no resources.');
        } else if (resolvedResourceNames.size === 0) {
            reasons.push('None of the declared resource paths could be found in the archive.');
        }

        if (unknownSchemaResources.length > 0) {
            reasons.push(`Resource(s) not recognized by schema: ${unknownSchemaResources.join(', ')}`);
        }

        return withTotalSize({
            type: 'data-package',
            actualType,
            version,
            // null when no installed type fits, the page then leaves the type to the person
            datasetType: dataPackageSchema ? dataPackageSchema.name : null,
            typeFromTables,
            profile: typeof manifest.profile === 'string' ? manifest.profile : null,
            ready,
            reason: reasons.length ? reasons.join(' ') : null,
            files: fileResults,
            unmappedFiles,
            skippedResources: [...skippedResources],
            unresolvedPaths,
        });
    }

    // Extensions and data package types the archive needs but this IPT doesn't have
    function missingTypes(result) {
        const installs = (result.problems ?? []).filter((p) => p.install).map((p) => p.install);
        const ids = (kind) => [...new Set(installs.filter((i) => i.kind === kind).map((i) => i.id))];
        return { rowTypes: ids('rowTypes'), dataPackageTypes: ids('dataPackageTypes') };
    }

    // The server would reject the archive. Never when the archive couldn't be fully read here.
    function blocksUpload(result) {
        return !!result && result.type !== 'unknown' && !result.ready && !result.unverified;
    }

    global.DataPackagePrecheck = { run, missingTypes, blocksUpload };
})(window);