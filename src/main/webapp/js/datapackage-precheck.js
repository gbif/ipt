/**
 * Verifies the content of the archive, checking datapackage.json and
 * available table schemas in the IPT. Does NOT inspect table contents,
 * columns, or relationships between resources — structural existence only.
 *
 * Reads only the ZIP central directory plus the datapackage.json entry, via
 * ranged File.slice() calls, so it never loads the whole archive into memory.
 *
 * Usage:
 *   const result = await DataPackagePrecheck.run(fileInput.files[0]);
 *   // result.type   -> 'data-package' | 'dwc-a' | 'unknown'
 *   // result.ready  -> boolean, can IPT create a resource from this
 *   // result.reason -> string explaining a not-ready state, else null
 *   // result.files -> [{ path, resources: [{ name, inSchema }] }]
 *   // result.unmappedFiles -> string[]
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
            const nameLen = buf.getUint16(offset + 28, true);
            const extraLen = buf.getUint16(offset + 30, true);
            const commentLen = buf.getUint16(offset + 32, true);
            const localHeaderOffset = buf.getUint32(offset + 42, true);
            const nameBytes = new Uint8Array(buf.buffer, buf.byteOffset + offset + 46, nameLen);
            const name = decoder.decode(nameBytes);

            entries.push({ name, isDirectory: name.endsWith('/'), method, compressedSize, localHeaderOffset });
            offset += 46 + nameLen + extraLen + commentLen;
        }

        return entries;
    }

    // Local file headers can pad name/extra fields slightly differently than
    // the central directory does, so we read the local header to find the
    // real start of the compressed data rather than assuming an offset.
    async function extractEntry(file, entry) {
        const headBuf = new DataView(
            await readSlice(file, entry.localHeaderOffset, entry.localHeaderOffset + 30)
        );
        if (headBuf.getUint32(0, true) !== LOCAL_HEADER_SIGNATURE) {
            throw new Error(`Corrupt local file header for ${entry.name}`);
        }
        const nameLen = headBuf.getUint16(26, true);
        const extraLen = headBuf.getUint16(28, true);
        const dataStart = entry.localHeaderOffset + 30 + nameLen + extraLen;
        const compressed = await readSlice(file, dataStart, dataStart + entry.compressedSize);

        if (entry.method === 0) return new Uint8Array(compressed); // stored, no compression

        if (entry.method === 8) {
            if (typeof DecompressionStream === 'undefined') {
                throw new Error(
                    'This browser has no native inflate support (DecompressionStream). ' +
                    'Vendor a small fallback inflate library for older browsers if needed.'
                );
            }
            const stream = new Blob([compressed]).stream().pipeThrough(new DecompressionStream('deflate-raw'));
            return new Uint8Array(await new Response(stream).arrayBuffer());
        }

        throw new Error(`Unsupported compression method (${entry.method}) for ${entry.name}`);
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

    async function run(file, availableDataPackages) {
        let entries;
        try {
            entries = await readCentralDirectory(file);
        } catch (err) {
            return { type: 'unknown', ready: false, reason: 'Could not read this as a zip archive: ' + err.message };
        }

        const fileEntries = entries.filter((e) => !e.isDirectory && !isMacZipArtifact(e.name));
        const byNormalizedPath = new Map(fileEntries.map((e) => [normalizePath(e.name), e]));

        const manifestEntry = fileEntries.find((e) => normalizePath(e.name) === 'datapackage.json');
        if (!manifestEntry) {
            const looksLikeDwcA = fileEntries.some((e) => /(^|\/)meta\.xml$/i.test(e.name));
            return {
                type: looksLikeDwcA ? 'dwc-a' : 'unknown',
                ready: false,
                reason: 'No datapackage.json found at the archive root.',
                files: fileEntries.map((e) => e.name),
            };
        }

        let manifest;
        try {
            const bytes = await extractEntry(file, manifestEntry);
            manifest = JSON.parse(new TextDecoder('utf-8').decode(bytes));
        } catch (err) {
            return { type: 'data-package', ready: false, reason: 'datapackage.json could not be read: ' + err.message };
        }

        const actualType = inferActualType(manifest);
        const version = inferVersion(manifest);
        const dataPackageSchema = findDataPackageSchema(availableDataPackages, actualType);
        const knownTableNames = new Set(
            (dataPackageSchema?.tableSchemas ?? []).map((t) => t.name)
        );

        const resources = Array.isArray(manifest.resources) ? manifest.resources : [];

        // normalizedPath -> Set<resourceName>, plus any declared path that never resolved
        const pathToResources = new Map();
        const unresolvedPaths = []; // { resourceName, path }

        for (const resource of resources) {
            const name = resource.name || '(unnamed resource)';
            const declaredPaths = Array.isArray(resource.path) ? resource.path : [resource.path];

            for (const p of declaredPaths) {
                if (typeof p !== 'string') {
                    unresolvedPaths.push({ resourceName: name, path: p });
                    continue;
                }
                const normalized = normalizePath(p);
                if (byNormalizedPath.has(normalized)) {
                    if (!pathToResources.has(normalized)) pathToResources.set(normalized, new Set());
                    pathToResources.get(normalized).add(name);
                } else {
                    unresolvedPaths.push({ resourceName: name, path: p });
                }
            }
        }

        const fileResults = [...byNormalizedPath.keys()].map((path) => {
            const mappedResources = [...(pathToResources.get(path) ?? [])];
            return {
                path,
                resources: mappedResources.map((name) => ({
                    name,
                    inSchema: knownTableNames.has(name),
                })),
            };
        });

        const unmappedFiles = fileResults.filter((f) => f.resources.length === 0);
        const unknownSchemaResources = [
            ...new Set(
                fileResults
                    .flatMap((f) => f.resources)
                    .filter((r) => !r.inSchema)
                    .map((r) => r.name)
            ),
        ];

        const ready =
            resources.length > 0 &&
            unresolvedPaths.length === 0 &&
            unknownSchemaResources.length === 0;

        const reasons = [];
        if (resources.length === 0) {
            reasons.push('datapackage.json declares no resources.');
        }
        if (unresolvedPaths.length > 0) {
            reasons.push(`Unresolved path(s): ${unresolvedPaths.map((u) => `${u.resourceName} → ${u.path}`).join(', ')}`);
        }
        if (unknownSchemaResources.length > 0) {
            reasons.push(`Resource(s) not recognized by schema: ${unknownSchemaResources.join(', ')}`);
        }

        return {
            type: 'data-package',
            actualType,
            version,
            ready,
            reason: reasons.length ? reasons.join(' ') : null,
            files: fileResults,
            unmappedFiles,
        };
    }

    global.DataPackagePrecheck = { run };
})(window);