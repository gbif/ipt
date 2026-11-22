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
                return $('<span class="mp-gen-opt">').append($('<span>').text(o.text), $('<small>').text(desc));
            }});
    }
    renderRows();

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
    $(document).on('change', '.mp-gen-kind', function () { renderGen($(this).closest('.mp-gen')); });
    $(document).on('change', '.mp-col', function () { renderGen($(this).closest('.mp-colcell').siblings('.mp-gen')); });
    $(document).on('click', '.mp-gen-open', function () {
        var $kind = $(this).prop('hidden', true).siblings('.mp-gen').prop('hidden', false).find('.mp-gen-kind');
        // select2 hides the select: focus its box instead
        if ($kind.data('select2')) $kind.next('.select2').find('.select2-selection').trigger('focus');
        else $kind.trigger('focus');
    });
    $('.mp-gen').each(function () { renderGen($(this)); });
});
