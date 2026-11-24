/*
 * Value translation pages (manage/translation.ftl, manage/dataPackageTranslation.ftl; markup in macros/translation.ftl).
 *
 * Rows are the translated source values. Each row has a state: "set" (translated), "blank" (published empty) or
 * "none" (just added, nothing chosen yet, which blocks saving). The "Add a translation" picker (#tr-add) lists the
 * other source values and takes typed ones; removing a row puts its value back in the picker.
 * A data package field's type and constraints (#tr-field data-*) are checked before saving.
 */
$(function () {
    'use strict';

    var $form = $('#translation-form');
    if (!$form.length) return;
    var $rows = $('#tr-rows');
    var i18n = $('#tr-i18n').data();
    var $vocab = $('#tr-vocab');
    var $add = $('#tr-add');
    // read as attributes: .data() would turn a pattern such as [1,2] into an array
    var $field = $('#tr-field');
    var rules = {
        type: $field.attr('data-type'),
        pattern: $field.attr('data-pattern'),
        minimum: $field.attr('data-minimum'),
        maximum: $field.attr('data-maximum'),
        required: $field.attr('data-required') === 'true'
    };
    var newKeys = 0;
    // a search box only where the list is long enough to need one
    var SEARCH_FROM = 10;

    function rowsAll() { return $rows.children('.tr-row'); }
    function sourceOf($row) { return $row.find('input[name^=smap]').val(); }

    // ---------- vocabulary options, copied into a select when it's first used ----------
    function fillOptions(select) {
        if (!$vocab.length || select.getAttribute('data-filled')) return;
        var value = select.value;
        var known = $vocab.find('option').filter(function () { return this.value === value; }).length > 0;
        var saved = known ? null : $(select).find('option:selected').clone();
        select.innerHTML = $vocab.html();
        // a saved value that isn't in the vocabulary stays, so saving doesn't drop it
        if (saved && value !== '') $(select).children().first().after(saved);
        select.value = value;
        select.setAttribute('data-filled', '1');
    }
    $rows.on('mousedown focus keydown', 'select.tr-input', function () { fillOptions(this); });

    // ---------- row state ----------
    $rows.on('change input', '.tr-input', function () {
        var $row = $(this).closest('.tr-row'), v = $(this).val();
        $row.find('.tr-server-msg').remove();
        $row.attr('data-state', v !== '' && v !== null ? 'set' : 'none');
        if ($row.attr('data-checked')) check($row);
        changed();
    });
    $rows.on('click', '.tr-blank-btn', function () {
        var $row = $(this).closest('.tr-row');
        $row.find('.tr-input').val('');
        $row.find('.tr-server-msg').remove();
        $row.attr('data-state', 'blank');
        check($row);
        changed();
    });
    // removing a translation: the value is published as it is again, and can be picked again
    $rows.on('click', '.tr-clear-btn', function () {
        var $row = $(this).closest('.tr-row');
        var value = sourceOf($row), key = $row.attr('data-key');
        // the key as the option's value, the source value aside: an empty one would be taken for the placeholder
        var o = new Option(value === '' ? '(' + i18n.empty + ')' : value, key);
        o.setAttribute('data-source', value);
        // back in its place: the picker is sorted, ignoring case, as ValueTranslation sorts the values
        var lower = value.toLowerCase();
        var $after = $add.children('option').filter(function () {
            return this.value !== '' && (this.getAttribute('data-source') || '').toLowerCase() > lower;
        }).first();
        if ($after.length) $after.before(o); else $add.append(o);
        $row.remove();
        changed();
    });

    // ---------- checks: a value chosen, and a data package field's type and constraints ----------
    function problemOf(value, state) {
        if (state === 'none') return i18n.missing;
        if (state === 'blank') return rules.required ? i18n.msgRequired : null;
        var type = rules.type;
        if (type === 'number' && !(!isNaN(parseFloat(value)) && isFinite(value))) return i18n.msgNumber;
        if (type === 'integer' && !/^-?\d+$/.test(value)) return i18n.msgInteger;
        if (type === 'boolean' && value !== 'true' && value !== 'false') return i18n.msgBoolean;
        if (rules.pattern !== undefined && rules.pattern !== '') {
            try {
                // a Table Schema pattern must match the whole value
                if (!new RegExp('^(?:' + rules.pattern + ')$').test(value)) return i18n.msgPattern;
            } catch (e) { /* an invalid pattern isn't the publisher's to fix here */ }
        }
        if (type === 'number' || type === 'integer') {
            var n = parseFloat(value);
            if (rules.minimum !== undefined && rules.minimum !== '' && n < +rules.minimum) return i18n.msgMinimum;
            if (rules.maximum !== undefined && rules.maximum !== '' && n > +rules.maximum) return i18n.msgMaximum;
        }
        return null;
    }
    function check($row) {
        var msg = problemOf($row.find('.tr-input').val(), $row.attr('data-state'));
        $row.attr('data-checked', '1');
        $row.find('.tr-problem').prop('hidden', !msg).text(msg || '');
        $row.find('.tr-input').toggleClass('is-invalid', !!msg);
        return !msg;
    }

    // ---------- search ----------
    function render() {
        var count = rowsAll().length;
        $('.tr-search').prop('hidden', count < SEARCH_FROM);
        var q = count < SEARCH_FROM ? '' : ($('#tr-q').val() || '').trim().toLowerCase();
        var shown = 0;
        rowsAll().each(function () {
            var $row = $(this), $in = $row.find('.tr-input');
            var text = sourceOf($row) + ' ' + ($in.is('select') ? $in.find('option:selected').text() : $in.val());
            var hit = !q || text.toLowerCase().indexOf(q) >= 0;
            $row.prop('hidden', !hit);
            if (hit) shown++;
        });
        $('#tr-nomatch').prop('hidden', !(count > 0 && shown === 0));
        $('#tr-empty').prop('hidden', count > 0);
        $('.tr-head').prop('hidden', count === 0);
    }
    $('#tr-q').on('input', render);
    $('#tr-clear-q').on('click', function () { $('#tr-q').val(''); render(); });

    // ---------- adding a translation ----------
    function flash($row) {
        $row[0].scrollIntoView({ block: 'center', behavior: 'smooth' });
        $row.removeClass('flash');
        setTimeout(function () { $row.addClass('flash'); }, 20);
        $row.find('.tr-input').trigger('focus');
    }
    function addValue(value, key) {
        var $msg = $('#tr-add-msg').prop('hidden', true);
        var $existing = rowsAll().filter(function () { return sourceOf($(this)) === value; });
        if ($existing.length) {
            $msg.text(i18n.addExists).prop('hidden', false);
            $('#tr-q').val('');
            render();
            flash($existing.first());
            return;
        }
        key = key || ('n' + (++newKeys));
        var $row = $($('#tr-row-template').html().trim());
        $row.attr('data-key', key);
        $row.find('input[name^=smap]').attr('name', "smap['" + key + "']").val(value);
        // an empty value keeps the template's "empty" label
        if (value !== '') $row.find('.tr-val').removeClass('tr-val-empty').text(value);
        $row.find('.tr-input').attr('aria-label', $row.find('.tr-input').attr('aria-label') + value);
        $rows.append($row);
        $('#tr-q').val('');
        changed();
        flash($row);
    }
    $add.select2({
        placeholder: i18n.addPlaceholder,
        // typed values: a source with more distinct values than the IPT reads, or one it will have later
        tags: true,
        allowClear: false,
        width: '100%',
        theme: 'bootstrap4',
        dropdownCssClass: 'text-smaller',
        language: { noResults: function () { return i18n.noResults; } }
    }).on('select2:select', function (e) {
        var data = e.params.data, el = data.element;
        var listed = el && el.hasAttribute('data-source');
        if (listed) $(el).remove();
        // a typed value leaves an option behind (select2 tags), not always given in the event: the row stands for it now
        $add.find('option[data-select2-tag]').remove();
        $add.val(null).trigger('change.select2');
        // a listed value keeps its key; a typed one gets a new key
        if (listed) addValue(el.getAttribute('data-source'), data.id); else addValue(data.text);
    });

    // ---------- dirty state + submit ----------
    var dirty = false, submitting = false, automapped = +i18n.automapped > 0;
    function formState() {
        return rowsAll().map(function () {
            var $row = $(this);
            return sourceOf($row) + '\u0000' + $row.attr('data-state') + '\u0000' + ($row.find('.tr-input').val() || '');
        }).get().join('\u0001');
    }
    var saved = formState();
    function changed() {
        dirty = automapped || formState() !== saved;
        $('#tr-savebar').toggleClass('show', dirty).attr('aria-hidden', dirty ? 'false' : 'true');
        $('body').toggleClass('mp-dirty', dirty);
        render();
    }
    $(window).on('beforeunload', function (e) {
        if (dirty && !submitting) {
            e.preventDefault();
            e.returnValue = i18n.unsaved;
            return i18n.unsaved;
        }
    });
    $('#tr-discard').on('click', function () { submitting = true; });

    $form.on('submit', function (e) {
        var submitter = e.originalEvent ? e.originalEvent.submitter : null;
        var deleting = submitter && submitter.name === 'deleteFlag';
        if (!deleting) {
            var $bad = rowsAll().filter(function () { return !check($(this)); });
            if ($bad.length) {
                e.preventDefault();
                $('#tr-q').val('');
                render();
                flash($bad.first());
                return;
            }
        }
        rowsAll().each(function () {
            var $row = $(this), key = $row.attr('data-key'), state = $row.attr('data-state');
            var name = "tmap['" + key + "']";
            $row.find('.tr-input').attr('name', state === 'set' ? name : null);
            $row.find('.tr-blank').attr('name', state === 'blank' ? name : null);
        });
        submitting = true;
    });

    rowsAll().each(function () { check($(this)); });
    changed();
});
