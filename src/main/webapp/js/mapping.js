/**
 * Mapping pages (manage/mapping.ftl for DwC-A, manage/dataPackageMapping.ftl for data packages): record ID choice,
 * filter sentence and preview, counts, checks of the sample values, the show / search tools for the fields and the
 * by-source-column view.
 *
 * Everything is read from the page: the columns (#mp-columns), the 5 sample rows with the data types each value
 * fails on the server (#mp-peek), the field rows (.mp-field) and the texts (#mp-i18n). The form posts the same
 * parameters as before; controls with form="mp-ui" aren't submitted.
 *
 * A data package mapping has no record ID section (#mp-id) and one list of fields; its fields carry the sample cells
 * publishing rejects (data-bad="column.row ...", checked on the server against the table schema) instead of #mp-peek's
 * data types.
 */
(function ($) {
    'use strict';

    var TYPES = ['integer', 'decimal', 'date', 'uri'];

    $(function () {
        var $form = $('#mappingForm');
        if (!$form.length) return;

        var i18n = $('#mp-i18n').data();
        function fmt(s) {
            var args = arguments;
            return String(s || '').replace(/\{(\d+)\}/g, function (m, n) {
                var v = args[+n + 1];
                return v === undefined ? m : v;
            });
        }
        function esc(s) {
            return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) {
                return {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c];
            });
        }
        function quoted(list) {
            return list.map(function (v) { return '\u201c' + v + '\u201d'; }).join(', ');
        }

        // ---------- data from the page ----------
        var columns = $('#mp-columns li').map(function () { return $(this).text(); }).get();
        var rows = $('#mp-peek tr').map(function () {
            return [$(this).children('td').map(function () {
                var bad = String($(this).attr('data-bad') || '');
                return {v: $(this).text(), bad: bad ? bad.split(' ') : []};
            }).get()];
        }).get();

        function sample(ci, ri) {
            var row = rows[ri];
            return row && row[ci] ? row[ci] : {v: '', bad: []};
        }

        var fields = $('.mp-field').map(function () {
            var $row = $(this);
            var $const = $row.find('.mp-const');
            var tr = {}, trBad = {};
            $row.find('.mp-tr').each(function () {
                tr[$(this).attr('data-from')] = $(this).attr('data-to');
                if ($(this).attr('data-bad') === 'true') trBad[$(this).attr('data-to')] = true;
            });
            // data packages: the cells the field rejects, by "column.row"
            var cells = null;
            if ($row.attr('data-bad') !== undefined) {
                cells = {};
                String($row.attr('data-bad')).split(' ').forEach(function (c) { if (c) cells[c] = true; });
            }
            var vocab = null;
            if ($row.attr('data-vocab') !== undefined && $const.is('select')) {
                vocab = {};
                $const.find('option').each(function () {
                    if (this.value) {
                        vocab[this.value.toLowerCase()] = true;
                        vocab[$(this).text().trim().toLowerCase()] = true;
                    }
                });
            }
            return {
                $row: $row,
                i: $row.attr('data-i'),
                group: $row.attr('data-group'),
                label: $row.attr('data-label'),
                term: $row.attr('data-term'),
                required: $row.attr('data-required') === 'true',
                type: $row.attr('data-type'),
                vocabTitle: $row.attr('data-vocab'),
                vocab: vocab,
                mirrored: $row.attr('data-mirrored') === 'true',
                search: $row.attr('data-search') || '',
                $col: $row.find('.mp-col'),
                // data package mappings: a generated value (list item, key, template...), js/dp-rows.js
                $gen: $row.find('.mp-gen-kind'),
                $const: $const,
                $doi: $row.find('#doiUsedForDatasetId'),
                $samples: $row.find('.mp-samples'),
                $msgs: $row.find('.mp-msgs'),
                $translate: $row.find('.mp-translate'),
                tr: tr,
                trBad: trBad,
                cells: cells,
                badConst: $row.attr('data-bad-const'),
                unique: $row.attr('data-unique') === 'true',
                hasEnum: $row.attr('data-enum') === 'true',
                expect: $row.attr('data-expect') || ''
            };
        }).get();
        // a data package mapping: its fields' values are checked on the server (data-bad)
        var isDp = fields.length > 0 && fields[0].cells !== null;
        var byIndex = {};
        fields.forEach(function (f) { byIndex[f.i] = f; });

        // a generated value other than an ID made from the column doesn't use the column
        function generated(f) {
            return (f.$gen && f.$gen.length && f.$gen.val()) || '';
        }
        function colOf(f) {
            var g = generated(f);
            if (g && g !== 'ID') return -1;
            var v = f.$col.val();
            return v === '' || v == null ? -1 : +v;
        }
        function isMapped(f) {
            if (f.mirrored) return idMode() === 'column' && idCol() >= 0;
            return colOf(f) >= 0 || generated(f) !== '' || $.trim(f.$const.val() || '') !== '' || f.$doi.is(':checked');
        }
        function hasTranslations(f) {
            return colOf(f) >= 0 && Object.keys(f.tr).length > 0;
        }

        // the values of a mapped field's column, as published: translated, and checked against its type and vocabulary
        function checkedSamples(f) {
            var ci = colOf(f);
            if (ci < 0) return [];
            return rows.map(function (r, ri) {
                var s = sample(ci, ri), v = s.v, t = Object.prototype.hasOwnProperty.call(f.tr, v) ? f.tr[v] : null;
                var pub = t != null ? t : v, state = 'ok';
                if ($.trim(pub) === '') state = 'empty';
                else if (f.cells) state = (t == null ? f.cells[ci + '.' + ri] : f.trBad[t]) ? 'bad' : 'ok';
                else if (t == null && TYPES.indexOf(f.type) >= 0 && s.bad.indexOf(f.type) >= 0) state = 'bad';
                else if (f.vocab && !f.vocab[$.trim(pub).toLowerCase()]) state = 'bad';
                return {v: v, t: t, pub: pub, state: state};
            });
        }

        function problems(f) {
            var out = [];
            if (f.mirrored) return out;
            if (f.required && !isMapped(f)) out.push({cls: 'bad', text: i18n.msgRequired});
            var bad = [], samples = checkedSamples(f);
            samples.forEach(function (s) {
                if (s.state === 'bad' && bad.indexOf(s.pub) < 0) bad.push(s.pub);
            });
            // data packages: the saved constant, if the server rejected it; a changed one is checked once saved
            var constant = $.trim(f.$const.val() || '');
            if (f.badConst != null && constant === f.badConst && bad.indexOf(constant) < 0) bad.push(constant);
            if (bad.length) {
                var text = f.cells ? (f.hasEnum ? fmt(i18n.msgEnum, quoted(bad)) : fmt(i18n.msgInvalid, f.expect || f.type, quoted(bad)))
                    : f.vocab ? fmt(i18n.msgVocab, f.vocabTitle, quoted(bad)) : fmt(i18n[{integer: 'msgInteger', decimal: 'msgDecimal', date: 'msgDate', uri: 'msgUri'}[f.type]], quoted(bad));
                // a data package isn't published with a value its schema rejects
                out.push({cls: f.cells ? 'bad' : 'w', text: text});
            }
            if (f.cells && samples.length) {
                // publishing fails on these too: an empty required value without a constant, a repeated unique one
                var noConst = $.trim(f.$const.val() || '') === '';
                var empty = samples.filter(function (s) { return s.state === 'empty'; }).length;
                if (f.required && noConst && empty) out.push({cls: 'bad', text: fmt(i18n.msgRequiredEmpty, empty)});
                // rows merged by key or made per list item aren't the source rows: their samples can repeat
                var perRow = ($('#mp-rows input[name=rowMode]:checked').val() || 'ROW') === 'ROW';
                if (f.unique && perRow) {
                    var vals = samples.map(function (s) { return $.trim(s.pub); });
                    var dup = vals.filter(function (v, k) { return v && vals.indexOf(v) !== k; });
                    if (dup.length) out.push({cls: 'bad', text: fmt(i18n.idDup, dup.filter(function (v, k) { return dup.indexOf(v) === k; }).join(', '))});
                }
            }
            return out;
        }

        // ---------- record ID ----------
        var $idSection = $('#mp-id');
        var isCore = $idSection.attr('data-core') === 'true';
        var $idCol = $('#mp-idcol');
        function idMode() {
            // a data package mapping has no record ID: its key fields are fields like the others
            if (!$idSection.length) return 'none';
            return $('input[name=mp-idmode]:checked').val() || 'column';
        }
        function idCol() {
            var v = $idCol.val();
            return v === '' || v == null ? -1 : +v;
        }
        // the ID set-up of the other core mappings (MappingAction.getCoreIdSetups)
        var coreSetups = $('#mp-core-setups li').map(function () {
            return {mode: $(this).attr('data-mode'), suffix: $(this).attr('data-suffix') || ''};
        }).get();
        var hasExtensions = $idSection.attr('data-has-extensions') === 'true';
        var isOccurrence = $idSection.attr('data-occurrence') === 'true';
        // without occurrenceIDs GBIF tells records apart by institutionCode + collectionCode + catalogNumber, so for an
        // Occurrence core the "no ID" warning depends on those three being mapped
        function renderNoIdWarning() {
            if (!isOccurrence || idMode() !== 'none') return;
            var triplet = ['dwc:institutionCode', 'dwc:collectionCode', 'dwc:catalogNumber'].every(function (t) {
                return fields.some(function (f) { return f.term === t && isMapped(f); });
            });
            $idSection.find('.mp-id-none-warn').prop('hidden', true);
            $idSection.find('.mp-id-none-notriplet').prop('hidden', triplet);
            $idSection.find('.mp-id-none-triplet').prop('hidden', !triplet);
        }
        var idBad = false;

        // the checks ExtensionMappingValidator makes on save: {cls, text} or null
        function suffixProblem(mode, suffix) {
            if (mode === 'line' && /^[\d\s]+$/.test(suffix) && $.trim(suffix)) return {cls: 'bad', text: i18n.suffixInteger};
            var sameMode = coreSetups.filter(function (c) { return c.mode === mode; });
            var sameSuffix = sameMode.filter(function (c) { return c.suffix === suffix; });
            if (isCore) {
                // two core mappings numbering their lines the same way give the same IDs
                if (mode === 'line' && sameSuffix.length) return {cls: 'bad', text: i18n.suffixSame};
            } else if (mode === 'line') {
                if (!sameSuffix.length) return {cls: 'bad', text: i18n.suffixNoCoreLine};
            } else if (mode === 'column' && sameMode.length && !sameSuffix.length) {
                // the extension may hold IDs that already end with the core's suffix, so only a warning
                return {cls: 'w', text: fmt(i18n.suffixNoCore, suffix)};
            }
            return null;
        }

        function renderId() {
            if (!$idSection.length) return;
            var mode = idMode(), ci = idCol();
            $idSection.find('.mp-choice').each(function () {
                $(this).toggleClass('on', $(this).find('input').is(':checked'));
            });
            // the submitted value: a column index, -1 line numbers, -2 UUIDs, empty for no ID
            $('#idColumn').val(mode === 'column' ? (ci >= 0 ? ci : '') : mode === 'line' ? '-1' : mode === 'uuid' ? '-2' : '');
            $idSection.find('.mp-id-column').prop('hidden', mode !== 'column');
            $idSection.find('.mp-id-suffix').prop('hidden', mode !== 'column' && mode !== 'line');
            $idSection.find('.mp-suffix-column').prop('hidden', mode !== 'column');
            $idSection.find('.mp-suffix-line').prop('hidden', mode !== 'line');
            if (mode === 'none' || mode === 'uuid') $('#mp-idsuffix').val('');
            $idSection.find('.mp-id-none').prop('hidden', mode !== 'none');
            $idSection.find('.mp-id-nolink').prop('hidden', mode === 'none');
            renderNoIdWarning();
            $('#mp-idcol-missing').prop('hidden', !(mode === 'column' && ci < 0));
            $idSection.find('.mp-id-mirror').prop('hidden', mode !== 'column');
            // generated UUIDs change on every publication, so the extensions' links would point at nothing
            var uuidWithExtensions = isCore && mode === 'uuid' && hasExtensions;
            $('#mp-id-uuid-ext').prop('hidden', !uuidWithExtensions);
            var sp = mode === 'column' || mode === 'line' ? suffixProblem(mode, $.trim($('#mp-idsuffix').val() || '')) : null;
            $('#mp-suffix-problem').attr('class', 'mp-msg ' + (sp ? sp.cls : '')).text(sp ? sp.text : '').prop('hidden', !sp);
            idBad = uuidWithExtensions || !!(sp && sp.cls === 'bad');

            var h = '';
            if (mode === 'column' && ci >= 0 && rows.length) {
                var vals = rows.map(function (r, ri) { return $.trim(sample(ci, ri).v); });
                // an extension has many rows for one core record, so only a core's IDs must be unique
                var dup = isCore ? vals.filter(function (v, k) { return v && vals.indexOf(v) !== k; }) : [];
                var empty = vals.filter(function (v) { return !v; }).length;
                h = '<label class="mp-fl">' + esc(i18n.sample) + '</label><div class="mp-samples">' + vals.map(function (v) {
                    return v ? '<span class="mp-sv' + (dup.indexOf(v) >= 0 ? ' bad' : '') + '" title="' + esc(v) + '">' + esc(v) + '</span>' : '<span class="mp-sv bad empty">' + esc(i18n.empty) + '</span>';
                }).join('') + '</div>' +
                    '<div class="mp-msg ' + (dup.length || empty ? 'bad' : 'ok') + '">' +
                    esc(dup.length ? fmt(i18n.idDup, dup.join(', ')) : empty ? fmt(i18n.idEmpty, empty) : (isCore ? i18n.idOk : i18n.idFilled)) + '</div>';
            }
            $('#mp-id-samples').html(h);

            // the core ID field of an extension (e.g. its eventID) always follows the link to the core
            fields.forEach(function (f) {
                if (f.mirrored) {
                    f.$col.val(mode === 'column' && ci >= 0 ? String(ci) : '');
                    f.$const.val('').prop('disabled', true);
                }
            });
        }

        // ---------- filter ----------
        var $filterOn = $('#mp-filter-on'), $fCol = $('#filterName'), $fCmp = $('#filterComp'), $fParam = $('#filterParam'), $fTime = $('#filterTime');
        function renderFilter() {
            var on = $filterOn.is(':checked');
            $('#mp-filter .mp-sentence').toggleClass('off', !on);
            $('#mp-filter-off').prop('hidden', on);
            if (!on) {
                // submitted empty, so a saved filter is removed
                $fCol.val('').trigger('change.select2');
                $fCmp.val('').trigger('change.select2');
                $fParam.val('');
            } else if (!$fCmp.val()) {
                $fCmp.val('IsNotNULL').trigger('change.select2');
            }
            var cmp = $fCmp.val(), withParam = cmp === 'Equals' || cmp === 'NotEquals';
            $fParam.prop('hidden', !withParam);
            if (!withParam) $fParam.val('');

            var fc = $fCol.val() === '' ? -1 : +$fCol.val();
            var h = '';
            if (on && fc >= 0 && rows.length) {
                // the filter sees translated values when checked after translation (GenerateDwca)
                var tr = {};
                if ($fTime.val() === 'AfterTranslation') {
                    fields.forEach(function (f) { if (colOf(f) === fc) $.extend(tr, f.tr); });
                }
                var param = $fParam.val(), ic = idMode() === 'column' ? idCol() : -1, kept = 0;
                var shown = [ic, fc].filter(function (c, k, a) { return c >= 0 && a.indexOf(c) === k; });
                h = '<div class="mp-preview"><table><tr><th></th>' + shown.map(function (c) { return '<th>' + esc(columns[c]) + '</th>'; }).join('') + '</tr>' +
                    rows.map(function (r, ri) {
                        var raw = sample(fc, ri).v, v = Object.prototype.hasOwnProperty.call(tr, raw) ? tr[raw] : raw;
                        v = $.trim(v);
                        var keep = cmp === 'IsNULL' ? !v : cmp === 'IsNotNULL' ? !!v : cmp === 'Equals' ? !!v && v === param : !v || v !== param;
                        if (keep) kept++;
                        return '<tr class="' + (keep ? '' : 'skip') + '"><td class="mp-keep">' + spriteIcon(keep ? 'check' : 'close') + esc(keep ? i18n.filterKept : i18n.filterSkipped) + '</td>' +
                            shown.map(function (c) { var x = sample(c, ri).v; return '<td>' + (x ? esc(x) : '<i>' + esc(i18n.empty) + '</i>') + '</td>'; }).join('') + '</tr>';
                    }).join('') + '</table></div><div class="mp-msg mu">' + esc(fmt(i18n.filterPreview, kept, rows.length)) + '</div>';
            }
            $('#mp-filter-preview').html(h);
        }

        // ---------- field rows ----------
        function renderField(f) {
            var ci = colOf(f), pr = problems(f);
            f.$row.toggleClass('miss', pr.some(function (p) { return p.cls === 'bad'; }));
            // publishing uses the constant only where the column is empty (GenerateDwca)
            if (f.$const.is('input')) f.$const.attr('placeholder', ci >= 0 ? i18n.constFallback : i18n.constAll);
            if (ci >= 0 && !f.mirrored && rows.length) {
                var constVal = $.trim(f.$const.val() || ''), checked = checkedSamples(f);
                var allEmpty = !constVal && checked.every(function (s) { return s.state === 'empty'; });
                f.$samples.html('<span class="lab">' + esc(i18n.sample) + ':</span>' + (allEmpty ? '<span class="mp-sv-none">' + esc(i18n.allEmpty) + '</span>' : checked.map(function (s) {
                    if (s.state === 'empty') return '<span class="mp-sv empty">' + esc(constVal || i18n.empty) + '</span>';
                    if (s.t != null) return '<span class="mp-sv tr' + (s.state === 'bad' ? ' w' : '') + '"><span class="from">' + esc(s.v) + '</span>' + esc(s.t) + '</span>';
                    return '<span class="mp-sv' + (s.state === 'bad' ? ' w' : '') + '" title="' + esc(s.v) + '">' + esc(s.v) + '</span>';
                }).join(''))).prop('hidden', false);
                // the translation link sits at the end of the sample line
                if (f.$translate.length) f.$samples.append(f.$translate);
            } else {
                f.$samples.empty().prop('hidden', true);
            }
            f.$msgs.html(pr.map(function (p) {
                return '<div class="mp-msg ' + p.cls + '">' + esc(p.text) + '</div>';
            }).join(''));
            // translations belong to the saved column: the translation page edits the saved mapping
            if (f.$translate.length) f.$translate.prop('hidden', String(ci) !== f.$translate.attr('data-saved-column'));
            if (!f.mirrored && f.$const.is('select')) f.$const.prop('disabled', false);
        }

        // ---------- show, search and groups ----------
        var view = 'field', show = 'default', query = '';
        var $groups = $('.mp-group');

        function matches(f) {
            if (query) return f.search.indexOf(query) >= 0 || f.term.toLowerCase().indexOf(query) >= 0;
            if (show === 'all') return true;
            if (show === 'problems') return problems(f).length > 0;
            if (show === 'unmapped') return !isMapped(f) && !f.mirrored;
            return isMapped(f) || f.required || f.mirrored || problems(f).length > 0;
        }

        // which rows are shown changes only with the show/search choice, not while editing, so a row doesn't vanish
        function applyShow(openMatches) {
            var anyShown = false;
            $groups.each(function () {
                var $g = $(this), g = $g.attr('data-g'), total = 0, shown = 0;
                fields.forEach(function (f) {
                    if (f.group !== g) return;
                    total++;
                    var ok = matches(f);
                    f.$row.toggleClass('mp-off', !ok);
                    if (ok) shown++;
                });
                var hideGroup = (query || show !== 'default') && shown === 0;
                $g.prop('hidden', hideGroup);
                if (!hideGroup) anyShown = true;
                var more = !query && show === 'default' ? total - shown : 0;
                $g.find('.mp-group-more').prop('hidden', more <= 0).children('span').text(fmt(i18n.groupMore, more));
                if (openMatches && shown > 0) setOpen($g, true);
            });
            $('#mp-nomatch').prop('hidden', anyShown).find('b').text(function (k, t) { return $(this).data('t') ? fmt($(this).data('t'), query) : t; });
            $('#mp-show button').each(function () { $(this).toggleClass('on', !query && $(this).attr('data-show') === show); });
        }

        function setOpen($g, open) {
            $g.toggleClass('open', open).find('.mp-group-h').attr('aria-expanded', open ? 'true' : 'false');
        }

        // ---------- by source column ----------
        function renderByColumn() {
            var feeds = columns.map(function () { return []; });
            fields.forEach(function (f) { var c = colOf(f); if (c >= 0 && !f.mirrored && feeds[c]) feeds[c].push(f); });
            var mode = idMode(), ic = idCol();
            var options = '<option value="">' + esc(i18n.mapTo) + '</option>';
            $groups.each(function () {
                var $g = $(this), g = $g.attr('data-g');
                var opts = fields.filter(function (f) { return f.group === g && !f.mirrored; }).map(function (f) {
                    // a field takes one column: say which one it has, as choosing it here takes it from there
                    var c = colOf(f);
                    return '<option value="' + f.i + '">' + esc(f.label) + (c >= 0 && columns[c] != null ? ' (' + esc(columns[c]) + ')' : '') + '</option>';
                }).join('');
                // one list of fields (data packages): no group to name
                options += $groups.length > 1 ? '<optgroup label="' + esc($g.find('.mp-group-h h3').text()) + '">' + opts + '</optgroup>' : opts;
            });
            var html = columns.map(function (c, ci) {
                if (query && c.toLowerCase().indexOf(query) < 0) return '';
                var isId = mode === 'column' && ic === ci, unused = !isId && !feeds[ci].length;
                return '<tr class="' + (unused ? 'un' : '') + '"><td class="c"><b>' + esc(c) + '</b><small>' + esc(fmt(i18n.columnN, ci + 1)) + (unused ? ' \u00b7 ' + esc(i18n.notUsed) : '') + '</small></td>' +
                    '<td><div class="mp-samples">' + (rows.every(function (r, ri) { return !$.trim(sample(ci, ri).v); }) ? '<span class="mp-sv-none">' + esc(i18n.allEmpty) + '</span>'
                        : rows.map(function (r, ri) { var v = sample(ci, ri).v; return v ? '<span class="mp-sv" title="' + esc(v) + '">' + esc(v) + '</span>' : '<span class="mp-sv empty">' + esc(i18n.empty) + '</span>'; }).join('')) + '</div></td>' +
                    '<td>' + (isId ? '<span class="mp-chip id">' + esc(i18n.recordId) + '</span>' : '') +
                    feeds[ci].map(function (f) {
                        return '<span class="mp-chip"><button type="button" class="mp-jump" data-jump="' + f.i + '">' + esc(f.label) + (hasTranslations(f) ? ' \u00b7 ' + esc(i18n.translated) : '') + '</button>' +
                            '<button type="button" data-unmap="' + f.i + '" title="' + esc(i18n.remove) + '" aria-label="' + esc(i18n.remove + ': ' + f.label) + '">\u00d7</button></span>';
                    }).join('') +
                    '<div><select class="form-select form-select-sm" form="mp-ui" data-map-column="' + ci + '" aria-label="' + esc(c + ': ' + i18n.mapTo) + '">' + options + '</select></div></td></tr>';
            }).join('');
            var $body = $('#mp-bycolumn tbody');
            $body.find('select[data-map-column]').each(function () { if ($(this).data('select2')) $(this).select2('destroy'); });
            $body.html(html);
            select2($body.find('select[data-map-column]'), {placeholder: i18n.mapTo});
        }

        // ---------- status line, sidebar and counts ----------
        function renderSummary() {
            var reqMissing = 0, withProblems = 0;
            var groupMapped = {}, groupProblems = {}, groupMissing = {}, groupTotal = {};
            var counts = {default: 0, problems: 0, unmapped: 0, all: fields.length};
            var used = {};
            fields.forEach(function (f) {
                var m = isMapped(f), pr = problems(f), c = colOf(f);
                if (f.required && !m) reqMissing++;
                if (pr.length) withProblems++;
                if (c >= 0) used[c] = true;
                groupTotal[f.group] = (groupTotal[f.group] || 0) + 1;
                if (m) groupMapped[f.group] = (groupMapped[f.group] || 0) + 1;
                if (pr.length) groupProblems[f.group] = (groupProblems[f.group] || 0) + 1;
                if (f.required && !m) groupMissing[f.group] = true;
                if (m || f.required || f.mirrored || pr.length) counts.default++;
                if (pr.length) counts.problems++;
                if (!m && !f.mirrored) counts.unmapped++;
            });
            var mode = idMode(), ic = idCol(), idMissing = mode === 'column' && ic < 0;
            if (mode === 'column' && ic >= 0) used[ic] = true;
            var unused = columns.length - Object.keys(used).length;
            var valueProblems = withProblems - reqMissing;

            // what needs doing, each item a way to it: red (bad) or orange (w)
            var items = [];
            if (idMissing || idBad) items.push({cls: 'bad', attr: 'data-goto-id', text: isCore ? i18n.statusId : i18n.statusLink});
            if (reqMissing) items.push({cls: 'bad', attr: 'data-goto-show="problems"', text: fmt(i18n.statusRequired, reqMissing)});
            // a data package isn't published with values its schema rejects: red there, orange for DwC-A
            if (valueProblems > 0) items.push({cls: isDp ? 'bad' : 'w', attr: 'data-goto-show="problems"', text: fmt(i18n.statusProblems, valueProblems)});
            // what the page knows from the server (dataPackageMapping.ftl): fields or a table not in the installed schema,
            // a mapping made against an older version
            $('#mp-status-extra li').each(function () {
                var $li = $(this);
                items.push({cls: $li.attr('data-cls'), attr: 'data-goto-section="' + esc($li.attr('data-goto')) + '"', text: $li.text()});
            });
            // not about this mapping but its core / extension / schema: a newer version in the registry, a link for
            // administrators
            var $status = $('#mp-status'), newer = $status.attr('data-newer');
            if (newer) items.push({cls: 'w', href: $status.attr('data-newer-url'), text: newer});

            // a button, a link, or text when there's nowhere to go
            function link(item, cls, content) {
                if (item.attr) return '<button type="button" class="' + cls + '" ' + item.attr + '>' + content + '</button>';
                if (item.href) return '<a class="' + cls + ' custom-link" href="' + esc(item.href) + '">' + content + '</a>';
                return '<span class="mp-status-note ' + item.cls + '">' + content + '</span>';
            }
            // under the heading: error / warning icon, as in the alerts (the same markup as the <@icon> macro)
            $status.html(items.map(function (item) {
                var icon = spriteIcon(item.cls === 'bad' ? 'error' : 'warning');
                return link(item, 'ov-link-btn ' + item.cls, icon + esc(item.text));
            }).join('<span class="mp-status-sep" aria-hidden="true">\u00b7</span>'));
            // the sidebar card (dataPackageMapping.ftl), as on the resource overview. With nothing to fix it doesn't say
            // "all fine": the value checks see only the sample rows, so they can't vouch for the whole source
            var $ready = $('#mp-ready');
            if ($ready.length) {
                $ready.children('b').text(items.length ? fmt(i18n.readyTitle, items.length) : i18n.ready);
                $ready.children('ul').html(items.length ? items.map(function (item) {
                    return '<li><span class="ov-dot ' + (item.cls === 'bad' ? 'bad' : 'warn') + '"></span>' + (item.attr || item.href ? link(item, 'ov-link-btn', esc(item.text)) : '<span>' + esc(item.text) + '</span>') + '</li>';
                }).join('') : '<li><span class="ov-dot ok"></span><span>' + esc(i18n.readyNone) + '</span></li>');
            }
            // in the source line: not a problem (one source can feed several mappings), but data that isn't published
            $('#mp-unused').prop('hidden', unused <= 0).children('button').text(fmt(i18n.statusUnused, unused));

            $('#mp-dot-id').attr('class', 'ov-dot ' + (idMissing || idBad ? 'bad' : 'ok'));
            $('#mp-dot-filter').attr('class', 'ov-dot ' + ($filterOn.is(':checked') ? 'ok' : 'off'));
            $('#mp-dot-fields').attr('class', 'ov-dot ' + (reqMissing || (isDp && valueProblems > 0) ? 'bad' : valueProblems > 0 ? 'warn' : 'ok'));

            $groups.each(function () {
                // red for a missing required field, orange for values to check
                var $g = $(this), g = $g.attr('data-g'), np = groupProblems[g] || 0, missing = !!groupMissing[g] || isDp;
                $g.find('[data-group-mapped]').text(fmt(i18n.groupMapped, groupMapped[g] || 0, groupTotal[g] || 0));
                $g.find('[data-group-problems]').text(fmt(i18n.groupProblems, np)).prop('hidden', !np).toggleClass('bad', missing).toggleClass('w', !missing);
                var title = fmt(i18n.groupMapped, groupMapped[g] || 0, groupTotal[g] || 0) + (np ? ' \u00b7 ' + fmt(i18n.groupProblems, np) : '');
                $('[data-group-count="' + g + '"]').text((groupMapped[g] || 0) + '/' + (groupTotal[g] || 0)).attr('title', title)
                    .toggleClass('bad', np > 0 && missing).toggleClass('w', np > 0 && !missing);
            });
            // no "0" on Problems: an empty count only draws the eye
            $('#mp-show button').each(function () { var k = $(this).attr('data-show'); $(this).find('.mp-count').text(k === 'problems' && !counts[k] ? '' : counts[k]).toggleClass('bad', $(this).attr('data-show') === 'problems' && (reqMissing > 0 || (isDp && counts.problems > 0))).toggleClass('w', $(this).attr('data-show') === 'problems' && counts.problems > 0 && !reqMissing && !isDp); });
        }

        function renderAll() {
            renderId();
            fields.forEach(renderField);
            renderFilter();
            renderSummary();
            if (view === 'column') renderByColumn();
        }

        function setView(v) {
            view = v;
            $('#mp-view button').each(function () { $(this).toggleClass('on', $(this).attr('data-view') === v); });
            $('#mp-byfield').prop('hidden', v !== 'field');
            $('#mp-bycolumn').prop('hidden', v !== 'column');
            $('#mp-show').prop('hidden', v !== 'field');
            $('#mp-q').attr('placeholder', v === 'field' ? i18n.searchField : i18n.searchColumn).attr('aria-label', v === 'field' ? i18n.searchField : i18n.searchColumn);
            if (v === 'column') renderByColumn(); else applyShow(!!query);
        }

        function jumpTo(f) {
            if (view !== 'field') setView('field');
            var $g = f.$row.closest('.mp-group');
            $g.prop('hidden', false);
            setOpen($g, true);
            f.$row.removeClass('mp-off');
            f.$row[0].scrollIntoView({block: 'center', behavior: 'smooth'});
            f.$row.removeClass('flash');
            setTimeout(function () { f.$row.addClass('flash'); }, 20);
        }

        // ---------- dirty state ----------
        // fields filled in from matching column names on this visit (MappingAction) aren't saved: unsaved from the start
        var dirty = false, submitting = false, saved = null, automapped = +i18n.automapped > 0;
        function formState() {
            // disabled fields (the mirrored core ID) aren't submitted, so they don't count
            return $form.serialize();
        }
        function markDirty() {
            dirty = automapped || (saved !== null && formState() !== saved);
            $('#mp-savebar').toggleClass('show', dirty).attr('aria-hidden', dirty ? 'false' : 'true');
            $('body').toggleClass('mp-dirty', dirty);
        }
        $(window).on('beforeunload', function (e) {
            if (dirty && !submitting) {
                e.preventDefault();
                e.returnValue = i18n.unsaved;
                return i18n.unsaved;
            }
        });
        $form.on('submit', function () { submitting = true; });
        $('#mp-delete-modal').on('submit click', '[name=deleteFlag]', function () { submitting = true; });
        $('#mp-discard').on('click', function () { submitting = true; });

        // ---------- events ----------
        // how the rows are made (data package mappings): changes which checks apply, e.g. unique samples
        $('#mp-rows').on('change input', 'input, select', function () { renderAll(); markDirty(); });
        $form.on('change input', '.mp-col, .mp-const, .mp-gen-kind, .mp-gen-tpl, .mp-gen-pairs select, #doiUsedForDatasetId', function () {
            var f = byIndex[$(this).closest('.mp-field').attr('data-i')];
            if (f) renderField(f);
            renderNoIdWarning();
            renderSummary();
            if (view === 'column') renderByColumn();
            if ($(this).is('.mp-col') && colOf(f) === ($fCol.val() === '' ? -1 : +$fCol.val())) renderFilter();
            markDirty();
        });
        // the page now checks what the server checked on the last save
        $idSection.on('change', 'input[name=mp-idmode], #mp-idcol', function () { $('#mp-id-server-problem').prop('hidden', true); renderAll(); markDirty(); });
        // "Publish without IDs" and back
        $idSection.on('click', '[data-set-idmode]', function () {
            $idSection.find('input[name=mp-idmode][value=' + $(this).attr('data-set-idmode') + ']').prop('checked', true).trigger('change');
        });
        $('#mp-idsuffix').on('input', function () { $('#mp-id-server-problem').prop('hidden', true); renderAll(); markDirty(); });
        $filterOn.on('change', function () { renderFilter(); renderSummary(); markDirty(); });
        $('#mp-filter').on('change input', '#filterName, #filterComp, #filterParam, #filterTime', function () { renderFilter(); renderSummary(); markDirty(); });

        $('.mp-info').on('click', function () {
            var $def = $('#' + $(this).attr('aria-controls')), open = $def.prop('hidden');
            $def.prop('hidden', !open);
            $(this).attr('aria-expanded', open ? 'true' : 'false');
        });
        $('.mp-group-h').on('click', function () {
            var $g = $(this).closest('.mp-group');
            setOpen($g, !$g.hasClass('open'));
        });
        $('[data-show-group]').on('click', function () {
            var $g = $(this).closest('.mp-group');
            show = 'all';
            applyShow(false);
            setOpen($g, true);
        });
        $('#mp-show button').on('click', function () {
            show = $(this).attr('data-show');
            query = '';
            $('#mp-q').val('');
            applyShow(show !== 'default');
        });
        $('#mp-view button').on('click', function () { setView($(this).attr('data-view')); });
        $('#mp-q').on('input', function () {
            query = $.trim($(this).val()).toLowerCase();
            if (view === 'column') renderByColumn(); else applyShow(!!query);
        });
        $('#mp-clear-q').on('click', function () {
            query = '';
            $('#mp-q').val('');
            applyShow(false);
        });
        // samples on or off, remembered in this browser; storage can be unavailable (private window, blocked)
        function setSamples(hidden) {
            $('#mp-byfield').toggleClass('mp-no-samples', hidden);
            $('#mp-samples-toggle').text(hidden ? i18n.showSamples : i18n.hideSamples);
        }
        var samplesHidden = false;
        try { samplesHidden = localStorage.getItem('ipt.mapping.hideSamples') === '1'; } catch (e) { /* default: shown */ }
        setSamples(samplesHidden);
        $('#mp-samples-toggle').on('click', function () {
            samplesHidden = !samplesHidden;
            setSamples(samplesHidden);
            try { localStorage.setItem('ipt.mapping.hideSamples', samplesHidden ? '1' : '0'); } catch (e) { /* not remembered */ }
        });
        $('#mp-expand-all').on('click', function () {
            var $visible = $groups.filter(':not([hidden])'), allOpen = $visible.length && $visible.filter('.open').length === $visible.length;
            $visible.each(function () { setOpen($(this), !allOpen); });
            $(this).text(allOpen ? i18n.expandAll : i18n.collapseAll);
        });
        $(document).on('click', '[data-goto-section]', function () {
            var el = document.getElementById($(this).attr('data-goto-section'));
            if (el) el.scrollIntoView({behavior: 'smooth'});
        });
        $(document).on('click', '[data-goto-id]', function () {
            document.getElementById('mp-id').scrollIntoView({behavior: 'smooth'});
        });
        $(document).on('click', '[data-goto-view]', function () {
            setView($(this).attr('data-goto-view'));
            document.getElementById('mp-fields').scrollIntoView({behavior: 'smooth'});
        });
        $(document).on('click', '[data-goto-show]', function () {
            if (view !== 'field') setView('field');
            show = $(this).attr('data-goto-show');
            query = '';
            $('#mp-q').val('');
            applyShow(true);
            document.getElementById('mp-fields').scrollIntoView({behavior: 'smooth'});
        });
        $('#mp-bycolumn').on('click', '[data-jump]', function () {
            jumpTo(byIndex[$(this).attr('data-jump')]);
        });
        $('#mp-bycolumn').on('click', '[data-unmap]', function () {
            byIndex[$(this).attr('data-unmap')].$col.val('').trigger('change');
        });
        $('#mp-bycolumn').on('change', '[data-map-column]', function () {
            var ci = $(this).attr('data-map-column'), f = byIndex[$(this).val()];
            if (!f) return;
            f.$col.val(ci).trigger('change');
            // the table was drawn again: back to the same column's dropdown, for the keyboard
            $('#mp-bycolumn select[data-map-column="' + ci + '"]').next('.select2').find('.select2-selection').trigger('focus');
        });
        $('[data-group-link]').on('click', function () {
            if (view !== 'field') setView('field');
            setOpen($('#mp-group-' + $(this).attr('data-group-link')), true);
        });

        // ---------- a Material icon from the sprite, the same markup as the <@icon> macro ----------
        function spriteIcon(name) {
            return '<svg class="material-icon" aria-hidden="true" focusable="false"><use href="' + esc(i18n.sprite) + '#' + name + '"/></svg>';
        }

        // ---------- dropdowns with search (select2), as on the other IPT pages ----------
        function select2(selector, opts) {
            if (!$.fn.select2) return;
            $(selector).select2($.extend({
                placeholder: '',
                language: {noResults: function () { return i18n.noResults; }},
                width: '100%',
                minimumResultsForSearch: 15,
                dropdownCssClass: 'text-smaller',
                theme: 'bootstrap4'
            }, opts));
        }
        // the mirrored core ID field follows the record ID and isn't chosen here
        select2('.mp-col:not(.mp-locked), select.mp-const', {allowClear: true});
        select2('#mp-idcol');
        select2('#filterName, #filterComp', {allowClear: true, width: 'resolve'});
        select2('#filterTime', {width: 'resolve'});

        // ---------- start ----------
        $('#mp-nomatch b').data('t', $('#mp-nomatch b').text());
        renderAll();
        applyShow(false);
        // open the classes with something to see; probably redundant ones stay closed. One list of fields (data
        // packages, .mp-group.single) is always open, see mapping.css
        $groups.each(function () {
            var $g = $(this);
            setOpen($g, !$g.hasClass('redundant') && $g.find('.mp-field:not(.mp-off)').length > 0);
        });
        // a field named in the URL, e.g. from a validation message
        if (location.hash && /^#field-\d+$/.test(location.hash) && byIndex[location.hash.slice(7)]) jumpTo(byIndex[location.hash.slice(7)]);

        // the state to compare with, once renderAll has filled in the values the page computes (idColumn, mirrored IDs)
        saved = formState();
        if (automapped) markDirty();

        if (window.IptScrollSpy) IptScrollSpy.init({tocSelector: '.bd-toc nav a'});
    });
})(jQuery);
