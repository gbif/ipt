/*
 * Translations of EML metadata fields: the title and the description (a DocBook field). Other fields can't be
 * translated yet.
 *
 * Every translated field on a page gets the same language tabs above it: the metadata language first, then the
 * translations. Selecting a tab shows that language in every field; adding or removing a language does so for every
 * field on the page.
 *
 *   title       -> the eml.title input (metadata language), plus a pair of titleTranslationLanguage and
 *                  titleTranslationValue parameters per translation, and titleTranslationsPosted
 *   DocBook     -> one editor per language, combined into the field's input: one <section xml:lang=".."> per
 *                  language, the metadata language first; no sections if there are no translations
 *
 * Usage:
 *   const translations = IptMultilingualFields.create({
 *     defaultLanguage: "en",           // language tag of the metadata language
 *     languages: ["es"],               // languages of the existing translations, on any page
 *     names: {en: "English", ...},     // languages that can be added, tag -> name
 *     selectValues: {eng: "en", ...}   // values of the metadata language dropdown -> tags
 *   });
 *   translations.titleField({input, translations: {es: "..."}, label, hint});
 *   translations.docBookField({mount, input, value, label, hint});
 *
 * hint: shown under the tabs while there are translations, with {0} for the name of the metadata language
 *   translations.setDefault("spa");    // metadata language changed
 */
(function (global) {
    'use strict';

    let labels = {
        addTranslation: 'Add translation',
        removeTranslation: 'Remove the {0} translation',
        metadataLanguage: 'Metadata language',
        confirmRemove: 'Remove the {0} translation?',
        confirmRemoveText: 'The {0} title and description are removed when you save.',
        confirmRemoveButton: 'Remove',
        cancel: 'Cancel',
        // for the warning image of the confirmation dialog
        baseUrl: '',
        translationOf: '{0} ({1})'
    };

    function format(text) {
        const args = Array.prototype.slice.call(arguments, 1);
        return String(text).replace(/\{(\d)\}/g, (match, index) => (args[index] !== undefined ? args[index] : ''));
    }

    function el(tag, attrs, children) {
        const element = document.createElement(tag);
        Object.entries(attrs || {}).forEach(([key, value]) => {
            if (key === 'class') element.className = value;
            else if (key === 'text') element.textContent = value;
            else element.setAttribute(key, value);
        });
        (children || []).forEach(child => element.appendChild(child));
        return element;
    }

    /**
     * Asks for confirmation in the IPT confirmation dialog (the look of js/jconfirmation.jquery.js, in the
     * #dialog-confirm container of the page menu), then calls onConfirm. Falls back to the browser dialog if the
     * container or Bootstrap is missing.
     */
    function confirmDialog(title, text, yesLabel, onConfirm) {
        const container = document.getElementById('dialog-confirm');
        if (!container || !global.bootstrap || !global.bootstrap.Modal) {
            if (global.confirm(title)) onConfirm();
            return;
        }
        const yes = el('button', {type: 'button', class: 'btn btn-sm btn-outline-gbif-danger', text: yesLabel});
        const cancel = el('button', {
            type: 'button', class: 'btn btn-sm btn-outline-secondary', 'data-bs-dismiss': 'modal', text: labels.cancel
        });
        const dialog = el('div', {class: 'modal-dialog modal-confirm modal-dialog-centered'}, [
            el('div', {class: 'modal-content'}, [
                el('div', {class: 'modal-header flex-column'}, [
                    el('img', {src: labels.baseUrl + '/images/logo-modal-warning.png', alt: '', class: 'modal-image'})
                ]),
                el('div', {class: 'modal-body'}, [
                    el('h5', {class: 'modal-title w-100', id: 'staticBackdropLabel', text: title}),
                    el('p', {class: 'mb-0', text: text})
                ]),
                el('div', {class: 'modal-footer justify-content-center'}, [cancel, yes])
            ])
        ]);
        container.replaceChildren(dialog);
        // getOrCreateInstance is missing from the Bootstrap version the IPT ships
        const modal = global.bootstrap.Modal.getInstance(container) || new global.bootstrap.Modal(container);
        yes.addEventListener('click', () => {
            modal.hide();
            onConfirm();
        });
        modal.show();
    }

    function create(settings) {
        const names = settings.names || {};
        const selectValues = settings.selectValues || {};
        const norm = code => {
            const value = String(code || '').trim();
            return (selectValues[value] || selectValues[value.toLowerCase()] || value).toLowerCase().replace('_', '-');
        };

        let defaultLang = norm(settings.defaultLanguage) || 'en';
        let languages = [];
        let active = defaultLang;
        const fields = [];

        const all = () => [defaultLang].concat(languages);
        const nameOf = lang => names[lang] || lang;

        function include(lang) {
            if (lang && lang !== defaultLang && languages.indexOf(lang) < 0) languages.push(lang);
        }

        (settings.languages || []).forEach(lang => include(norm(lang)));

        function refresh() {
            if (all().indexOf(active) < 0) active = defaultLang;
            fields.forEach(field => field.refresh());
        }

        function activate(lang, focusField) {
            active = lang;
            refresh();
            if (focusField) focusField.focus(lang);
        }

        function add(code, focusField) {
            const lang = norm(code);
            if (!lang || all().indexOf(lang) >= 0) return;
            languages.push(lang);
            fields.forEach(field => field.addLanguage(lang));
            activate(lang, focusField);
        }

        function remove(lang) {
            const doRemove = () => {
                languages = languages.filter(l => l !== lang);
                fields.forEach(field => field.removeLanguage(lang));
                refresh();
            };
            // nothing to lose: no need to ask
            if (!fields.some(field => field.hasContent(lang))) {
                doRemove();
                return;
            }
            confirmDialog(format(labels.confirmRemove, nameOf(lang)), format(labels.confirmRemoveText, nameOf(lang)),
                labels.confirmRemoveButton, doRemove);
        }

        /**
         * The metadata language changed. If it was a translation, the two languages swap places; otherwise the
         * content in the old metadata language is now taken to be in the new one.
         */
        function setDefault(code) {
            const lang = norm(code);
            if (!lang || lang === defaultLang) return;
            const old = defaultLang;
            const swap = languages.indexOf(lang) >= 0;
            if (swap) {
                languages = languages.map(l => (l === lang ? old : l));
            }
            defaultLang = lang;
            fields.forEach(field => (swap ? field.swapDefault(old, lang) : field.relabel(old, lang)));
            if (active === old && !swap) active = lang;
            refresh();
        }

        /**
         * Language tabs, shown above a field. Only the "add translation" dropdown while there are no translations.
         */
        function tabBar(field) {
            const bar = el('div', {class: 'ipt-ml-tabs d-flex flex-wrap align-items-center gap-1 mb-1'});

            bar.render = () => {
                bar.replaceChildren();
                if (languages.length) {
                    const tabs = el('div', {class: 'ipt-ml-tablist d-flex flex-wrap gap-1', role: 'tablist'});
                    all().forEach(lang => {
                        const selected = lang === active;
                        const tab = el('button', {
                            type: 'button', role: 'tab', class: 'btn btn-sm ipt-ml-tab' + (selected ? ' active' : ''),
                            'aria-selected': String(selected), lang, text: nameOf(lang)
                        });
                        if (lang === defaultLang) {
                            tab.title = labels.metadataLanguage;
                            tab.prepend(el('i', {class: 'bi bi-star-fill me-1 ipt-ml-default', 'aria-hidden': 'true'}));
                        }
                        tab.addEventListener('click', () => activate(lang, field));
                        const group = el('span', {class: 'ipt-ml-tabgroup d-inline-flex'}, [tab]);
                        if (lang !== defaultLang) {
                            const label = format(labels.removeTranslation, nameOf(lang));
                            const close = el('button', {
                                type: 'button', class: 'btn btn-sm ipt-ml-remove' + (selected ? ' active' : ''),
                                title: label, 'aria-label': label
                            }, [el('i', {class: 'bi bi-x', 'aria-hidden': 'true'})]);
                            close.addEventListener('click', () => remove(lang));
                            group.appendChild(close);
                        }
                        tabs.appendChild(group);
                    });
                    bar.appendChild(tabs);
                }

                const used = all();
                const available = Object.keys(names).filter(lang => used.indexOf(lang) < 0);
                if (available.length) {
                    const select = el('select', {
                        class: 'form-select form-select-sm ipt-ml-add ms-auto', 'aria-label': labels.addTranslation
                    }, [el('option', {value: '', text: '+ ' + labels.addTranslation})]);
                    available.forEach(lang => select.appendChild(el('option', {value: lang, text: nameOf(lang)})));
                    bar.appendChild(select);
                    const onChange = () => {
                        if (select.value) add(select.value, field);
                    };
                    // select2 (when loaded) makes the long list searchable. It reports a choice through jQuery, which
                    // native listeners don't receive, so listen through jQuery then.
                    const $ = global.jQuery;
                    if ($ && $.fn && $.fn.select2) {
                        $(select).select2({
                            theme: 'bootstrap4',
                            width: '14rem',
                            dropdownAutoWidth: true,
                            placeholder: '+ ' + labels.addTranslation,
                            // the default matching, keeping the search text for the sorter
                            matcher: (params, data) => {
                                select.iptSearchTerm = params.term || '';
                                return $.fn.select2.defaults.defaults.matcher(params, data);
                            },
                            // names starting with the search text first: "fren" -> French before "Creoles, French-based"
                            sorter: results => {
                                const term = (select.iptSearchTerm || '').trim().toLowerCase();
                                if (!term) return results;
                                const starts = r => (r.text || '').toLowerCase().startsWith(term) ? 0 : 1;
                                return results.slice().sort((a, b) => starts(a) - starts(b));
                            }
                        });

                        $(select).next('.select2').addClass('ipt-ml-add-select2 ms-auto');
                        $(select).on('change', onChange);
                    } else {
                        select.addEventListener('change', onChange);
                    }
                }
            };
            return bar;
        }

        /**
         * A hint on which language is the main one, shown under the tabs while there are translations. The text has
         * {0} for the name of the metadata language.
         */
        function hintLine(text) {
            const hint = el('div', {class: 'ipt-ml-hint text-muted mb-1'});
            hint.render = () => {
                hint.hidden = !text || !languages.length;
                if (!hint.hidden) hint.textContent = format(text, nameOf(defaultLang));
            };
            return hint;
        }

        /**
         * The title: the eml.title input holds the metadata language; each translation gets an input of its own.
         */
        function titleField(options) {
            const defaultInput = options.input;
            if (!defaultInput) return null;
            const label = options.label || '';
            const container = el('div', {class: 'ipt-ml-panels'});
            const panels = {}; // lang -> {panel, input, language}
            const hint = hintLine(options.hint);
            let bar = null;

            function makePanel(lang, value) {
                const language = el('input', {type: 'hidden', name: 'titleTranslationLanguage', value: lang});
                const input = el('input', {
                    type: 'text', class: 'form-control', name: 'titleTranslationValue', lang,
                    'aria-label': format(labels.translationOf, label, nameOf(lang))
                });
                input.value = value || '';
                const panel = el('div', {class: 'ipt-ml-panel'}, [language, input]);
                container.appendChild(panel);
                panels[lang] = {panel, input, language};
            }

            const inputOf = lang => (lang === defaultLang ? defaultInput : panels[lang] && panels[lang].input);

            const field = {
                refresh() {
                    bar.render();
                    hint.render();
                    defaultInput.hidden = active !== defaultLang;
                    defaultInput.setAttribute('lang', defaultLang);
                    Object.keys(panels).forEach(lang => {
                        panels[lang].panel.hidden = lang !== active;
                    });
                },
                focus(lang) {
                    const input = inputOf(lang);
                    if (input) input.focus();
                },
                addLanguage(lang) {
                    if (lang !== defaultLang && !panels[lang]) makePanel(lang, '');
                },
                removeLanguage(lang) {
                    if (!panels[lang]) return;
                    panels[lang].panel.remove();
                    delete panels[lang];
                    defaultInput.dispatchEvent(new Event('input', {bubbles: true}));
                },
                hasContent(lang) {
                    const input = inputOf(lang);
                    return !!(input && input.value.trim());
                },
                swapDefault(old, lang) {
                    // the translation in the new metadata language moves to eml.title, the old title becomes a translation
                    const entry = panels[lang];
                    const value = entry.input.value;
                    entry.input.value = defaultInput.value;
                    defaultInput.value = value;
                    entry.language.value = old;
                    entry.input.setAttribute('lang', old);
                    entry.input.setAttribute('aria-label', format(labels.translationOf, label, nameOf(old)));
                    delete panels[lang];
                    panels[old] = entry;
                },
                relabel() {
                    // eml.title is always in the metadata language
                }
            };
            bar = tabBar(field);
            // tells the server that the translations were posted, so that none means "all removed", not "not loaded"
            container.appendChild(el('input', {type: 'hidden', name: 'titleTranslationsPosted', value: 'true'}));
            defaultInput.parentNode.insertBefore(bar, defaultInput);
            defaultInput.parentNode.insertBefore(hint, defaultInput);
            defaultInput.after(container);

            const translations = {};
            Object.entries(options.translations || {}).forEach(([code, value]) => {
                const lang = norm(code);
                if (!lang || lang === defaultLang || translations[lang] !== undefined) return;
                translations[lang] = value;
                include(lang);
            });
            languages.forEach(lang => makePanel(lang, translations[lang]));
            // a required title left empty: show it, so the browser can point at it
            defaultInput.addEventListener('invalid', () => {
                if (active !== defaultLang) activate(defaultLang);
            });

            fields.push(field);
            // fields created earlier may not have the languages of this one yet
            fields.forEach(other => languages.forEach(lang => other.addLanguage(lang)));
            refresh();
            return field;
        }

        /**
         * A DocBook field: one editor per language, combined into the field's input.
         */
        function docBookField(options) {
            const mount = options.mount;
            const input = options.input;
            if (!mount || !input) return null;
            const label = options.label || '';

            const values = {};
            const parts = global.IptDocBookEditor.splitLanguages(options.value);
            if (parts) {
                parts.forEach(part => {
                    const lang = norm(part.lang);
                    if (values[lang] === undefined) values[lang] = part.value;
                    include(lang);
                });
            } else {
                values[defaultLang] = options.value || '';
            }

            const container = el('div', {class: 'ipt-ml-panels'});
            const editors = {}; // lang -> {panel, input, editor}
            const hint = hintLine(options.hint);
            let bar = null;

            function combine() {
                input.value = global.IptDocBookEditor.joinLanguages(
                    all().map(lang => ({lang, value: editors[lang] ? editors[lang].input.value : ''})));
            }

            function makeEditor(lang, value) {
                const panel = el('div', {class: 'ipt-ml-panel', lang});
                // not submitted: the languages are combined into the field's input
                const languageInput = el('input', {type: 'hidden'});
                panel.appendChild(languageInput);
                container.appendChild(panel);
                const editor = global.IptDocBookEditor.create({
                    mount: panel,
                    input: languageInput,
                    value: value || '',
                    ariaLabel: lang === defaultLang ? label : format(labels.translationOf, label, nameOf(lang))
                });
                languageInput.addEventListener('input', combine);
                editors[lang] = {panel, input: languageInput, editor};
            }

            const field = {
                refresh() {
                    bar.render();
                    hint.render();
                    Object.keys(editors).forEach(lang => {
                        editors[lang].panel.hidden = lang !== active;
                    });
                    combine();
                },
                focus(lang) {
                    if (editors[lang] && editors[lang].editor) editors[lang].editor.focus();
                },
                addLanguage(lang) {
                    if (!editors[lang]) makeEditor(lang, '');
                },
                removeLanguage(lang) {
                    if (!editors[lang]) return;
                    editors[lang].panel.remove();
                    delete editors[lang];
                    combine();
                    input.dispatchEvent(new Event('input', {bubbles: true}));
                },
                hasContent(lang) {
                    return !!(editors[lang] && editors[lang].input.value.trim());
                },
                swapDefault() {
                    // every language has an editor of its own: only the order changes
                },
                relabel(old, lang) {
                    if (!editors[old]) return;
                    editors[lang] = editors[old];
                    editors[lang].panel.setAttribute('lang', lang);
                    delete editors[old];
                }
            };

            bar = tabBar(field);
            mount.appendChild(bar);
            mount.appendChild(hint);
            mount.appendChild(container);

            all().forEach(lang => makeEditor(lang, values[lang]));
            fields.push(field);
            fields.forEach(other => languages.forEach(lang => other.addLanguage(lang)));
            refresh();
            return field;
        }

        return {titleField, docBookField, setDefault, add, activate, languages: () => all()};
    }

    global.IptMultilingualFields = {
        create,
        setLabels: custom => {
            labels = Object.assign({}, labels, custom);
        }
    };
})(window);

/*
 * Translated title and description on the resource page. They are shown in the interface language if translated,
 * in the metadata language otherwise, with a note naming the other languages. The names are links, so that a
 * translation in a language the IPT interface does not offer can be read too.
 *
 *   IptMultilingualFields.translationNote(settings, {note, title, label});
 *
 * settings: {defaultLanguage, languages, names: {tag: name}, titles: {tag: title}, preferred,
 *            codes: {language of a description section as written, e.g. "spa": tag, e.g. "es"}}
 */
(function (global) {
    'use strict';

    function translationNote(settings, options) {
        const note = options.note;
        if (!settings) return;
        const all = [settings.defaultLanguage].concat(settings.languages || []);
        const names = settings.names || {};
        const titles = settings.titles || {};
        const codes = settings.codes || {};
        const normalize = code => (Object.prototype.hasOwnProperty.call(codes, code) ? codes[code] : code);

        function show(lang) {
            const titleLang = titles[lang] ? lang : settings.defaultLanguage;
            if (options.title && titles[titleLang]) {
                options.title.textContent = titles[titleLang];
                options.title.setAttribute('lang', titleLang);
            }
            global.IptDocBookEditor.renderAll(document, {lang, fallback: settings.defaultLanguage, normalize});
            renderNote(lang);
        }

        function renderNote(lang) {
            if (!note) return;
            note.replaceChildren(document.createTextNode((options.label || 'Also available in:') + ' '));
            all.filter(code => code !== lang).forEach((code, index) => {
                if (index) note.appendChild(document.createTextNode(', '));
                const link = document.createElement('button');
                link.type = 'button';
                link.className = 'btn btn-link p-0 align-baseline metadata-translations-link';
                link.setAttribute('lang', code);
                link.textContent = names[code] || code;
                link.addEventListener('click', () => show(code));
                note.appendChild(link);
            });
            note.hidden = false;
        }

        show(all.indexOf(settings.preferred) >= 0 ? settings.preferred : settings.defaultLanguage);
    }

    if (global.IptMultilingualFields) {
        global.IptMultilingualFields.translationNote = translationNote;
    }
})(window);
