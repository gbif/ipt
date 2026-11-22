/*
 * Data package mapping page (dataPackageMapping.ftl): how the rows are made (Rows section) and the generated values
 * of the fields. js/mapping.js counts a generated value as mapped and marks the page unsaved.
 */
$(function () {
    'use strict';
    var $rows = $('#mp-rows');
    if (!$rows.length) return;

    function mode() { return $rows.find('input[name=rowMode]:checked').val() || 'ROW'; }

    function renderRows() {
        var m = mode();
        $rows.find('.mp-choice').each(function () {
            $(this).toggleClass('on', $(this).find('input').is(':checked'));
        });
        $rows.find('[data-for]').each(function () {
            $(this).prop('hidden', $(this).attr('data-for').split(' ').indexOf(m) < 0);
        });
        // one row per distinct value always merges; list items may
        var $merge = $('#mp-merge');
        if (m === 'DISTINCT') $merge.prop('checked', true).prop('disabled', true);
        else $merge.prop('disabled', false);
        var merging = m !== 'ROW' && $merge.is(':checked');
        $('#mp-mergekey').prop('disabled', !merging);
        // the merge rule only matters when rows merge
        $('#mp-merge-info').prop('hidden', !merging);
    }
    $rows.on('change', 'input[name=rowMode], #mp-merge', renderRows);
    if ($.fn.select2) {
        $('#mp-split').select2({theme: 'bootstrap4', width: '100%', closeOnSelect: false});
        // a table can have dozens of fields: searchable
        $('#mp-mergekey').select2({theme: 'bootstrap4', width: 'resolve', dropdownAutoWidth: true});
        // the generated value: a short list, so no search box, but the look of the column dropdown above it
        // each option says what it does under its name; the box shows the name only
        $('.mp-gen-kind').select2({theme: 'bootstrap4', width: '100%', minimumResultsForSearch: Infinity,
            // wider and taller than the box, so the descriptions are read without scrolling
            dropdownCssClass: 'mp-gen-dd',
            templateResult: function (o) {
                var desc = o.element && $(o.element).attr('data-desc');
                if (!desc) return o.text;
                var $opt = $('<span class="mp-gen-opt">').append($('<span>').text(o.text), $('<small>').text(desc));
                // an option for list items, while the rows aren't made from a list: why it can't be chosen
                if (o.disabled) $opt.append($('<small class="mp-gen-why">').text($rows.attr('data-needs-lists')));
                return $opt;
            }});
    }
    renderRows();

    // ---------- options for list items ----------
    // the list columns the rows are made from, as they're chosen now (saved or not)
    function lists() {
        return mode() === 'ITEMS' ? ($('#mp-split').val() || []) : [];
    }

    // each item, its place and the matching item need rows made from a list; one that's chosen stays enabled, so it's
    // still posted and the field shows the problem (js/mapping.js)
    function renderListOptions() {
        var has = lists().length > 0;
        $('.mp-gen-kind option[data-lists]').each(function () {
            $(this).prop('disabled', !has && !this.selected);
        });
    }

    // the matching item: one column choice per list in Rows, in the order they're posted (pair_N_K is the K-th list);
    // choices already made are kept for the lists that are still there
    function renderPairs() {
        var ls = lists(), $names = $('#mp-split option');
        $('.mp-gen-pairs .mp-pairs').each(function () {
            var $box = $(this), i = $box.closest('.mp-field').attr('data-i'), chosen = {};
            $box.find('select[data-list]').each(function () { chosen[$(this).attr('data-list')] = $(this).val(); });
            $box.empty();
            if (!ls.length) {
                $box.append($('<div>').text($rows.attr('data-pair-none')));
                return;
            }
            ls.forEach(function (list, k) {
                var name = $names.filter('[value="' + list + '"]').text();
                var $sel = $('<select class="form-select form-select-sm">').attr({'data-list': list, name: 'pair_' + i + '_' + k})
                    .append($('<option value="">'));
                $names.each(function () { $sel.append($('<option>').val(this.value).text($(this).text())); });
                $sel.val(chosen[list] || '');
                $box.append($('<label class="mp-pair">').append($('<span>').text($rows.attr('data-pair-for').replace('{0}', name)), $sel));
            });
        });
    }

    // bound on the inputs themselves: they run before js/mapping.js, which checks the fields on the same change
    $rows.find('input[name=rowMode], #mp-split').on('change', function () {
        renderListOptions();
        renderPairs();
    });
    renderListOptions();

    function renderGen($gen) {
        var kind = $gen.find('.mp-gen-kind').val() || '';
        $gen.find('.mp-gen-tpl').prop('hidden', kind !== 'TEMPLATE');
        $gen.find('[data-kind-help]').each(function () {
            $(this).prop('hidden', $(this).attr('data-kind-help') !== kind);
        });
        // a generated value replaces the column, except an ID made from it
        var $colcell = $gen.siblings('.mp-colcell');
        $colcell.toggleClass('mp-generated', kind !== '' && kind !== 'ID');
        // and the constant value, always: it's dropped when the mapping is saved
        $gen.closest('.mp-field').toggleClass('mp-const-off', kind !== '');
        // an ID is made from the column when one is chosen, otherwise from each list item
        var hasCol = ($colcell.find('.mp-col').val() || '') !== '';
        $gen.find('[data-id-from=col]').prop('hidden', !hasCol);
        $gen.find('[data-id-from=item]').prop('hidden', hasCol);
    }
    $(document).on('change', '.mp-gen-kind', function () {
        renderGen($(this).closest('.mp-gen'));
        // an option for list items left behind can't be chosen again without lists
        renderListOptions();
    });
    $(document).on('change', '.mp-col', function () { renderGen($(this).closest('.mp-colcell').siblings('.mp-gen')); });
    $(document).on('click', '.mp-gen-open', function () {
        var $kind = $(this).prop('hidden', true).siblings('.mp-gen').prop('hidden', false).find('.mp-gen-kind');
        // select2 hides the select: focus its box instead
        if ($kind.data('select2')) $kind.next('.select2').find('.select2-selection').trigger('focus');
        else $kind.trigger('focus');
    });
    $('.mp-gen').each(function () { renderGen($(this)); });
});
