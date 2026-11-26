/*
 * Lists of small cards on the EML pages (Citations and links, Collection Data). A list ([data-cards="eml.collections"])
 * holds cards ([data-card]) in [data-items]; a card's fields are posted as eml.collections[i].collectionName, or
 * eml.alternateIdentifiers[i] for a list of plain values. The template's fields are named TPL.collectionName (TPL for a
 * plain value). reindex() renames the fields, numbers the card titles (data-title, "Collection {0}") and sets up/down
 * after every add, remove and move, so the posted lists follow the card order.
 *
 * A card with a type switch ([data-switch]) shows the fields of the chosen type only ([data-type-fields]); the others
 * are not posted.
 */
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('[data-cards]').forEach(init);
    });

    function toast(message) {
        const el = document.getElementById('party-toast');
        if (!el) {
            return;
        }
        el.textContent = message;
        el.classList.add('show');
        clearTimeout(toast.timer);
        toast.timer = setTimeout(function () {
            el.classList.remove('show');
        }, 2400);
    }

    function init(list) {
        const base = list.dataset.cards;
        const holder = list.querySelector('[data-items]');
        const form = list.closest('form');
        const text = list.dataset;

        function cards() {
            return Array.from(holder.children).filter(function (el) {
                return el.matches('[data-card]');
            });
        }

        function changed() {
            // tells the unsaved changes warning (footer.ftl) that something changed
            form.dispatchEvent(new Event('input', {bubbles: true}));
        }

        function title(index) {
            return text.title.replace('{0}', index + 1);
        }

        function fields(card) {
            return Array.from(card.querySelectorAll('input[name], textarea[name], select[name]'));
        }

        function reindex() {
            const all = cards();
            all.forEach(function (card, index) {
                fields(card).forEach(function (el) {
                    if (el.dataset.f === undefined) {
                        el.dataset.f = el.name.replace(/^TPL|^[^\[]*\[\d+\]/, '');
                    }
                    const old = el.id;
                    const name = base + '[' + index + ']' + el.dataset.f;
                    if (old === name) {
                        return;
                    }
                    el.name = name;
                    el.id = name;
                    card.querySelectorAll('label').forEach(function (label) {
                        if (label.htmlFor === old) {
                            label.htmlFor = name;
                        }
                    });
                    // a field error belongs to the place it was posted for, so it goes when the card moves
                    const error = document.getElementById('field-error-' + old);
                    if (error && card.contains(error)) {
                        error.remove();
                        el.classList.remove('is-invalid');
                    }
                });
                const titleEl = card.querySelector('[data-card-title]');
                if (titleEl) {
                    titleEl.textContent = title(index);
                }
                card.querySelector('[data-act=up]').disabled = index === 0;
                card.querySelector('[data-act=down]').disabled = index === all.length - 1;
                card.classList.toggle('invalid', !!card.querySelector('.field-error li, .is-invalid'));
            });
            list.querySelector('[data-empty]').hidden = all.length > 0;
        }

        function setType(card, type) {
            card.dataset.type = type;
            card.querySelectorAll('[data-switch] [data-type]').forEach(function (button) {
                const on = button.dataset.type === type;
                button.classList.toggle('on', on);
                button.setAttribute('aria-checked', on);
            });
            card.querySelectorAll('[data-type-fields]').forEach(function (block) {
                block.hidden = block.dataset.typeFields !== type;
            });
        }

        function add() {
            const fragment = list.querySelector('template[data-template]').content.cloneNode(true);
            const card = fragment.querySelector('[data-card]');
            holder.appendChild(fragment);
            if (typeof initInfoPopovers === 'function') {
                initInfoPopovers(card);
            }
            reindex();
            const first = card.querySelector('input:not([type=hidden]), textarea');
            if (first) {
                first.focus();
            }
            changed();
        }

        list.addEventListener('click', function (event) {
            const button = event.target.closest('button[data-act], button[data-type]');
            if (!button || !list.contains(button)) {
                return;
            }
            const card = button.closest('[data-card]');
            const act = button.dataset.act;
            if (act === 'add') {
                add();
                return;
            }
            if (!card) {
                return;
            }
            if (button.closest('[data-switch]')) {
                setType(card, button.dataset.type);
            } else if (act === 'remove') {
                const number = cards().indexOf(card);
                card.remove();
                reindex();
                toast(text.removed + ': ' + title(number));
            } else if (act === 'up' && card.previousElementSibling) {
                holder.insertBefore(card, card.previousElementSibling);
                reindex();
                (button.disabled ? card.querySelector('[data-act=down]') : button).focus();
                toast(text.moved + ' ' + (cards().indexOf(card) + 1));
            } else if (act === 'down' && card.nextElementSibling) {
                holder.insertBefore(card.nextElementSibling, card);
                reindex();
                (button.disabled ? card.querySelector('[data-act=up]') : button).focus();
                toast(text.moved + ' ' + (cards().indexOf(card) + 1));
            } else {
                return;
            }
            changed();
        });

        // a posted error no longer applies once its field is edited
        list.addEventListener('input', function (event) {
            const card = event.target.closest('[data-card]');
            if (!card || !event.target.name) {
                return;
            }
            event.target.classList.remove('is-invalid');
            const error = document.getElementById('field-error-' + event.target.id);
            if (error) {
                error.remove();
            }
            card.classList.toggle('invalid', !!card.querySelector('.field-error li, .is-invalid'));
        });

        // empty cards are left out, so a blank card never blocks saving; fields of the other type aren't posted
        form.addEventListener('submit', function () {
            cards().forEach(function (card) {
                const typed = fields(card).filter(function (el) {
                    return el.type !== 'hidden' && !el.closest('[data-type-fields][hidden]');
                });
                if (!typed.some(function (el) { return el.value.trim(); })) {
                    card.remove();
                    return;
                }
                card.querySelectorAll('[data-type-fields][hidden] [name]').forEach(function (el) {
                    el.disabled = true;
                });
            });
            reindex();
        });

        if (typeof sortable === 'function') {
            const instance = sortable(holder, {
                forcePlaceholderSize: true,
                placeholderClass: 'border',
                handle: '.party-grip',
                items: '[data-card]'
            });
            instance[0].addEventListener('sortupdate', function () {
                reindex();
                changed();
            });
        }

        cards().forEach(function (card) {
            if (card.dataset.type) {
                setType(card, card.dataset.type);
            }
        });
        reindex();
    }
})();
