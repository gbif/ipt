/*
 * Lists of cards on the Camtrap DP metadata pages (manage/dp-metadata/camtrap): add, remove and renumber.
 *
 * dpList({
 *   key: 'contributor',              // card ids <key>-item-<i>, remove link ids <key>-removeLink-<i>
 *   prefix: 'metadata.contributors', // id/name/for of the fields: <prefix>[<i>]... after every change
 *   onAdd: function ($card) {}       // optional: e.g. select2 on the new card's selects
 * })
 *
 * The page has #<key>-items (the cards, class item), #baseItem-<key> (hidden template outside the form),
 * #plus-<key> (the add link), optionally #<key>-none (shown without cards). A remove link in a card has the
 * class remove-item. A [data-card-title] element shows the value of the card's field matched by its
 * data-title-from selector, or its data-empty text.
 *
 * dpScope(onChange) on the scope pages (camtrap/scope_macros.ftl): the source cards set #scope-infer and show
 * #scope-inferred or #scope-stored; onChange(mode) is called with "inferred" or "stored", also once at the start.
 * Opening the page with reinferMetadata (the Re-infer link) starts in "inferred".
 */
(function () {
    'use strict';

    function escapeRegExp(text) {
        return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    function updateTitle($card) {
        $card.find('[data-card-title]').each(function () {
            var $title = $(this);
            var value = '';
            $card.find($title.attr('data-title-from')).each(function () {
                value = value || ($(this).val() || '').trim();
            });
            $title.text(value || $title.attr('data-empty')).toggleClass('empty', !value);
        });
    }

    window.dpList = function (options) {
        var key = options.key;
        var $list = $('#' + key + '-items');
        var $none = $('#' + key + '-none');
        var pattern = new RegExp('^' + escapeRegExp(options.prefix) + '(\\[\\d+\\])?');

        function cards() {
            return $list.children('.item');
        }

        function renumber() {
            cards().each(function (index) {
                var $card = $(this);
                $card.attr('id', key + '-item-' + index);
                $card.find('.remove-item').attr('id', key + '-removeLink-' + index);
                $card.find('[id], [name], [for]').each(function () {
                    var element = this;
                    ['id', 'name', 'for'].forEach(function (attribute) {
                        var value = element.getAttribute(attribute);
                        if (value && pattern.test(value)) {
                            element.setAttribute(attribute, value.replace(pattern, options.prefix + '[' + index + ']'));
                        }
                    });
                });
                // an error posted back belongs to the place it was posted for: it goes when its card moves
                $card.find('.field-error').each(function () {
                    var field = $(this).siblings('input, select, textarea')[0];
                    if (field && this.id !== 'field-error-' + field.id) {
                        field.classList.remove('is-invalid');
                        $(this).remove();
                    }
                });
            });
            $none.prop('hidden', cards().length > 0);
        }

        $('#plus-' + key).on('click', function (event) {
            event.preventDefault();
            var $card = $('#baseItem-' + key).clone().removeAttr('style');
            $card.hide().appendTo($list).slideDown('fast');
            renumber();
            if (typeof initInfoPopovers === 'function') {
                initInfoPopovers($card[0]);
            }
            if (options.onAdd) {
                options.onAdd($card);
            }
            updateTitle($card);
            $card.find('input:visible, textarea:visible').first().trigger('focus');
        });

        $list.on('click', '.remove-item', function (event) {
            event.preventDefault();
            $(this).closest('.item').slideUp('fast', function () {
                $(this).remove();
                renumber();
            });
        });

        $list.on('input change', 'input, select', function () {
            updateTitle($(this).closest('.item'));
        });

        cards().each(function () {
            updateTitle($(this));
        });
        $none.prop('hidden', cards().length > 0);

        return {renumber: renumber, updateTitle: updateTitle};
    };

    window.dpScope = function (onChange) {
        var $modes = $('.geo-mode[data-mode]');
        var $infer = $('#scope-infer');

        function setMode(mode, initial) {
            $modes.each(function () {
                var on = $(this).data('mode') === mode;
                $(this).toggleClass('selected', on).find('input').prop('checked', on);
            });
            $infer.val(mode === 'inferred' ? 'true' : 'false');
            $('#scope-inferred').prop('hidden', mode !== 'inferred');
            $('#scope-stored').prop('hidden', mode !== 'stored');
            if (!initial) {
                // inc/footer.ftl shows the save bar on input events of the form (later: its listener may not be set yet)
                setTimeout(function () {
                    $infer[0].dispatchEvent(new Event('input', {bubbles: true}));
                }, 0);
            }
            if (onChange) {
                onChange(mode);
            }
        }

        $modes.find('input').on('change', function () {
            setMode($(this).val(), false);
        });

        // after Re-infer on a page switched to "From my data" but not saved: still to be saved
        var reinfer = new URLSearchParams(window.location.search).get('reinferMetadata') === 'true';
        var stored = $infer.val() === 'true';
        setMode(stored || reinfer ? 'inferred' : 'stored', stored || !reinfer);
        return setMode;
    };
})();
