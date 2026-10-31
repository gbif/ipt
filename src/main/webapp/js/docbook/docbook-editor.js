/*
 * Rich text editor for EML text fields (description, purpose, introduction, getting started, acknowledgements).
 *
 * The editor is built on ProseMirror (js/prosemirror/prosemirror-bundle.min.js, exposed as window.PM) with a schema
 * restricted to what the EML text module (eml-text.xsd) can hold, so the editor cannot produce content that
 * does not convert to valid DocBook:
 *
 *   paragraph    -> <para>
 *   heading      -> starts a <section> with a <title>; the section runs until the next heading
 *   bullet list  -> <para><itemizedlist><listitem>...
 *   ordered list -> <para><orderedlist><listitem>...
 *   code block   -> <para><literalLayout>
 *   bold         -> <emphasis>
 *   subscript    -> <subscript>
 *   superscript  -> <superscript>
 *   link         -> <ulink url="..."><citetitle>...</citetitle></ulink>
 *
 * Marks exclude each other: in EML, <emphasis> and <citetitle> hold text only, and <subscript>/<superscript> can only
 * nest each other. The editor document is converted directly to and from DocBook; there is no HTML step.
 *
 * Usage:
 *   IptDocBookEditor.create({
 *     mount: element,         // element to render the editor into
 *     input: hiddenInput,     // receives the DocBook value, kept in sync on every change
 *     value: docBookString,   // initial value
 *     ariaLabel: "Description"
 *   });
 */
(function (global) {
    'use strict';

    if (!global.PM) {
        console.error('IptDocBookEditor: ProseMirror bundle (window.PM) is not loaded');
        return;
    }

    const {Schema, DOMParser: PMDOMParser} = PM.model;
    const {EditorState, TextSelection} = PM.state;
    const {EditorView} = PM.view;
    const {baseKeymap, toggleMark, setBlockType, exitCode} = PM.commands;
    const {keymap} = PM.keymap;
    const {history, undo, redo} = PM.history;
    const {wrapInList, splitListItem, liftListItem, sinkListItem} = PM.schemaList;
    const {inputRules, wrappingInputRule, textblockTypeInputRule} = PM.inputrules;
    const ALLOWED_URL = /^(https?|ftp):\/\/\S+$|^mailto:\S+$/i;

    /**
     * Normalises a URL to one the server-side sanitizer keeps (http, https, ftp, mailto), or returns null.
     */
    function safeUrl(url) {
        if (typeof url !== 'string') return null;
        let value = url.trim();
        if (/^www\./i.test(value)) value = 'https://' + value;
        return ALLOWED_URL.test(value) ? value : null;
    }

    const schema = new Schema({
        nodes: {
            doc: {content: '(block | heading)+'},
            paragraph: {
                group: 'block',
                content: 'inline*',
                parseDOM: [{tag: 'p'}],
                toDOM: () => ['p', 0]
            },
            heading: {
                // <title> is text only
                content: 'text*',
                marks: '',
                defining: true,
                parseDOM: ['h1', 'h2', 'h3', 'h4', 'h5', 'h6'].map(tag => ({tag})),
                toDOM: () => ['h3', {class: 'ipt-dbe-heading'}, 0]
            },
            code_block: {
                group: 'block',
                content: 'text*',
                marks: '',
                code: true,
                defining: true,
                parseDOM: [{tag: 'pre', preserveWhitespace: 'full'}],
                toDOM: () => ['pre', ['code', 0]]
            },
            bullet_list: {
                group: 'block',
                content: 'list_item+',
                parseDOM: [{tag: 'ul'}],
                toDOM: () => ['ul', 0]
            },
            ordered_list: {
                group: 'block',
                content: 'list_item+',
                parseDOM: [{tag: 'ol'}],
                toDOM: () => ['ol', 0]
            },
            list_item: {
                content: 'paragraph block*',
                defining: true,
                parseDOM: [{tag: 'li'}],
                toDOM: () => ['li', 0]
            },
            text: {group: 'inline'}
        },
        marks: {
            emphasis: {
                excludes: '_',
                parseDOM: [{tag: 'b'}, {tag: 'strong'}, {tag: 'em'}, {tag: 'i'}],
                toDOM: () => ['strong', 0]
            },
            subscript: {
                excludes: '_',
                parseDOM: [{tag: 'sub'}],
                toDOM: () => ['sub', 0]
            },
            superscript: {
                excludes: '_',
                parseDOM: [{tag: 'sup'}],
                toDOM: () => ['sup', 0]
            },
            link: {
                excludes: '_',
                inclusive: false,
                attrs: {href: {}},
                parseDOM: [{
                    tag: 'a[href]',
                    getAttrs: dom => {
                        const href = safeUrl(dom.getAttribute('href'));
                        return href ? {href} : false;
                    }
                }],
                toDOM: mark => ['a', {href: mark.attrs.href, title: mark.attrs.href, rel: 'noopener noreferrer nofollow'}, 0]
            }
        }
    });

    const N = schema.nodes;
    const M = schema.marks;
    const WS = /[ \t\r\n]+/g;

    function localName(el) {
        return (el.localName || el.nodeName || '').toLowerCase();
    }

    /**
     * Collects inline content as a list of {text, marks} runs.
     */
    function collectInline(node, marks, runs) {
        node.childNodes.forEach(child => {
            if (child.nodeType === 3 || child.nodeType === 4) { // text, CDATA
                runs.push({text: child.nodeValue.replace(WS, ' '), marks});
            } else if (child.nodeType === 1) {
                collectInlineElement(child, marks, runs);
            }
        });
        return runs;
    }

    /**
     * Inline element: formatting becomes a mark, anything else (citetitle, value, unknown elements) keeps its text.
     */
    function collectInlineElement(element, marks, runs) {
        const name = localName(element);
        let childMarks = marks;
        if (name === 'emphasis') {
            childMarks = M.emphasis.create().addToSet(marks);
        } else if (name === 'subscript') {
            childMarks = M.subscript.create().addToSet(marks);
        } else if (name === 'superscript') {
            childMarks = M.superscript.create().addToSet(marks);
        } else if (name === 'ulink') {
            const href = safeUrl(element.getAttribute('url'));
            if (href) childMarks = M.link.create({href}).addToSet(marks);
        }
        collectInline(element, childMarks, runs);
    }

    /**
     * Turns {text, marks} runs into text nodes: whitespace collapsed across runs, ends trimmed, empty runs dropped.
     */
    function runsToNodes(runs) {
        const nodes = [];
        let previousEndsWithSpace = true; // trims leading whitespace
        runs.forEach(run => {
            let text = run.text;
            if (previousEndsWithSpace) text = text.replace(/^ /, '');
            if (!text) return;
            previousEndsWithSpace = text.endsWith(' ');
            nodes.push({text, marks: run.marks});
        });
        if (nodes.length) {
            const last = nodes[nodes.length - 1];
            last.text = last.text.replace(/ $/, '');
            if (!last.text) nodes.pop();
        }
        return nodes.map(n => schema.text(n.text, n.marks));
    }

    function paragraphFromRuns(runs) {
        const content = runsToNodes(runs);
        return content.length ? N.paragraph.create(null, content) : null;
    }

    function codeBlockFrom(el) {
        const text = el.textContent.replace(/^\n+|\n+$/g, '');
        return text.trim() ? N.code_block.create(null, schema.text(text)) : null;
    }

    function headingFrom(el) {
        const text = el.textContent.replace(WS, ' ').trim();
        return text ? N.heading.create(null, schema.text(text)) : null;
    }

    function listFrom(el) {
        const type = localName(el) === 'orderedlist' ? N.ordered_list : N.bullet_list;
        const items = [];
        el.childNodes.forEach(child => {
            if (child.nodeType !== 1 || localName(child) !== 'listitem') return;
            // list items cannot hold headings
            const blocks = blocksFrom(child).map(b => b.type === N.heading ? N.paragraph.create(null, b.content) : b);
            if (!blocks.length) return;
            if (blocks[0].type !== N.paragraph) blocks.unshift(N.paragraph.create());
            items.push(N.list_item.create(null, blocks));
        });
        return items.length ? type.create(null, items) : null;
    }

    /**
     * Converts the children of a DocBook element into editor blocks. Bare text and inline elements are gathered
     * into paragraphs; lists and literalLayout inside a <para> become blocks of their own.
     */
    function blocksFrom(parent) {
        const blocks = [];
        let runs = [];

        const flush = () => {
            const para = paragraphFromRuns(runs);
            if (para) blocks.push(para);
            runs = [];
        };
        const push = block => {
            if (block) blocks.push(block);
        };

        parent.childNodes.forEach(child => {
            if (child.nodeType === 3 || child.nodeType === 4) {
                runs.push({text: child.nodeValue.replace(WS, ' '), marks: []});
                return;
            }
            if (child.nodeType !== 1) return;

            switch (localName(child)) {
                case 'para':
                    flush();
                    blocksFrom(child).forEach(push);
                    break;
                case 'section':
                    flush();
                    // nested sections are flattened: every title becomes a heading
                    blocksFrom(child).forEach(push);
                    break;
                case 'title':
                    flush();
                    push(headingFrom(child));
                    break;
                case 'itemizedlist':
                case 'orderedlist':
                    flush();
                    push(listFrom(child));
                    break;
                case 'literallayout':
                    flush();
                    push(codeBlockFrom(child));
                    break;
                default:
                    collectInlineElement(child, [], runs);
            }
        });
        flush();
        return blocks;
    }

    function plainTextBlocks(text) {
        return String(text)
            .split(/\r?\n\s*\r?\n/)
            .map(chunk => chunk.replace(/\s+/g, ' ').trim())
            .filter(Boolean)
            .map(chunk => N.paragraph.create(null, schema.text(chunk)));
    }

    function makeDoc(blocks) {
        return N.doc.createChecked(null, blocks.length ? blocks : [N.paragraph.create()]);
    }

    // HTML or DocBook tags; anything else with "<" is plain text
    const MARKUP_TAG = new RegExp('</?(p|div|span|br|ul|ol|li|h[1-6]|pre|code|a|b|strong|em|i|u|sub|sup|table|tr|td|' +
        'para|section|title|itemizedlist|orderedlist|listitem|emphasis|subscript|superscript|literallayout|ulink|citetitle)\\b[^>]*>', 'i');

    /**
     * Content that is not well-formed DocBook: legacy HTML or plain text. HTML is parsed in an inert document (no
     * scripts run, nothing loads) and mapped through the schema, which drops anything it does not know.
     */
    function legacyDoc(value) {
        if (!MARKUP_TAG.test(value)) {
            return makeDoc(plainTextBlocks(value));
        }
        const html = new global.DOMParser().parseFromString(value, 'text/html');
        html.querySelectorAll('br').forEach(br => br.replaceWith(html.createTextNode('\n')));
        return PMDOMParser.fromSchema(schema).parse(html.body);
    }

    const DOCBOOK_START = /^<(para|section|title|itemizedlist|orderedlist|literalLayout|emphasis|ulink)\b/i;

    function docFromDocBook(value, nested) {
        if (typeof value !== 'string' || !value.trim()) return makeDoc([]);

        const xml = new global.DOMParser().parseFromString('<root>' + value + '</root>', 'application/xml');
        if (xml.getElementsByTagName('parsererror').length) return legacyDoc(value);

        const root = xml.documentElement;
        // DocBook stored as escaped text (e.g. "&lt;para&gt;...") - unescape once and parse as DocBook
        if (!nested && !root.firstElementChild && DOCBOOK_START.test(root.textContent.trim())) {
            return docFromDocBook(root.textContent.trim(), true);
        }
        if (!root.firstElementChild) return makeDoc(plainTextBlocks(root.textContent));

        try {
            return makeDoc(blocksFrom(root));
        } catch (e) {
            console.warn('IptDocBookEditor: could not map DocBook to the editor, using its text only', e);
            return makeDoc(plainTextBlocks(root.textContent));
        }
    }

    // characters not allowed in XML 1.0
    const INVALID_XML_CHARS = /[\u0000-\u0008\u000B\u000C\u000E-\u001F\uFFFE\uFFFF]/g;

    function esc(text) {
        return text.replace(INVALID_XML_CHARS, '')
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function escAttr(text) {
        return esc(text).replace(/"/g, '&quot;');
    }

    function inlineToDocBook(node) {
        let out = '';
        node.forEach(child => {
            const text = esc(child.text);
            const mark = child.marks[0]; // marks exclude each other, so there is at most one
            if (!mark) {
                out += text;
            } else if (mark.type === M.emphasis) {
                out += '<emphasis>' + text + '</emphasis>';
            } else if (mark.type === M.subscript) {
                out += '<subscript>' + text + '</subscript>';
            } else if (mark.type === M.superscript) {
                out += '<superscript>' + text + '</superscript>';
            } else if (mark.type === M.link) {
                out += '<ulink url="' + escAttr(mark.attrs.href) + '"><citetitle>' + text + '</citetitle></ulink>';
            }
        });
        return out;
    }

    function paragraphToDocBook(node) {
        const inner = inlineToDocBook(node);
        return inner.trim() ? '<para>' + inner + '</para>' : '';
    }

    function codeBlockToDocBook(node) {
        const text = node.textContent;
        return text.trim() ? '<para><literalLayout>' + esc(text) + '</literalLayout></para>' : '';
    }

    function listToDocBook(node) {
        const tag = node.type === N.ordered_list ? 'orderedlist' : 'itemizedlist';
        let items = '';
        node.forEach(item => {
            let inner = '';
            item.forEach(block => {
                if (block.type === N.paragraph) inner += paragraphToDocBook(block);
                else if (block.type === N.code_block) inner += codeBlockToDocBook(block);
                else inner += listToDocBook(block);
            });
            if (inner) items += '<listitem>' + inner + '</listitem>';
        });
        return items ? '<' + tag + '>' + items + '</' + tag + '>' : '';
    }

    function blockToDocBook(node) {
        if (node.type === N.paragraph) return paragraphToDocBook(node);
        if (node.type === N.code_block) return codeBlockToDocBook(node);
        // lists are not allowed directly in a text field or section, only inside a paragraph
        const list = listToDocBook(node);
        return list ? '<para>' + list + '</para>' : '';
    }

    function docToDocBook(doc) {
        let out = '';
        let inSection = false;
        let sectionHasBody = false;

        const closeSection = () => {
            if (!inSection) return;
            // a section must contain at least one paragraph
            if (!sectionHasBody) out += '<para></para>';
            out += '</section>';
            inSection = false;
        };

        doc.forEach(node => {
            if (node.type === N.heading) {
                const title = node.textContent.trim();
                if (!title) return;
                closeSection();
                out += '<section><title>' + esc(title) + '</title>';
                inSection = true;
                sectionHasBody = false;
                return;
            }
            const block = blockToDocBook(node);
            if (block) {
                out += block;
                sectionHasBody = true;
            }
        });
        closeSection();
        return out;
    }

    function isMarkActive(state, type) {
        const {from, $from, to, empty} = state.selection;
        if (empty) return !!type.isInSet(state.storedMarks || $from.marks());
        return state.doc.rangeHasMark(from, to, type);
    }

    function toggleBlock(type) {
        return (state, dispatch) => {
            const {$from} = state.selection;
            if ($from.parent.type === type) return setBlockType(N.paragraph)(state, dispatch);
            return setBlockType(type)(state, dispatch);
        };
    }

    function isBlockActive(state, type) {
        return state.selection.$from.parent.type === type;
    }

    function toggleList(type) {
        const other = type === N.bullet_list ? N.ordered_list : N.bullet_list;
        return (state, dispatch) => {
            const inner = innermostList(state);
            if (inner && inner.node.type === type) return liftListItem(N.list_item)(state, dispatch);
            if (inner && inner.node.type === other) {
                if (dispatch) dispatch(state.tr.setNodeMarkup(inner.pos, type));
                return true;
            }
            return wrapInList(type)(state, dispatch);
        };
    }

    function innermostList(state) {
        const {$from} = state.selection;
        for (let d = $from.depth; d > 0; d--) {
            const node = $from.node(d);
            if (node.type === N.bullet_list || node.type === N.ordered_list) return {node, pos: $from.before(d)};
        }
        return null;
    }

    function isListActive(state, type) {
        const inner = innermostList(state);
        return !!inner && inner.node.type === type;
    }

    /**
     * The range of the link around the cursor, or null.
     */
    function linkRange(state) {
        const {$from, empty, from, to} = state.selection;
        if (!empty) {
            let found = null;
            state.doc.nodesBetween(from, to, node => {
                const mark = M.link.isInSet(node.marks);
                if (mark && !found) found = mark;
            });
            return found ? {from, to, href: found.attrs.href} : null;
        }

        const parent = $from.parent;
        const start = $from.start();
        let mark = null;
        let index = -1;
        let offset = 0;
        // find the text node with a link touching the cursor
        for (let i = 0; i < parent.childCount; i++) {
            const child = parent.child(i);
            const childStart = start + offset;
            const childEnd = childStart + child.nodeSize;
            const candidate = M.link.isInSet(child.marks);
            if (candidate && childStart <= $from.pos && $from.pos <= childEnd) {
                mark = candidate;
                index = i;
                break;
            }
            offset += child.nodeSize;
        }
        if (!mark) return null;

        let rangeFrom = start + offset;
        let rangeTo = rangeFrom + parent.child(index).nodeSize;
        for (let i = index - 1; i >= 0 && mark.isInSet(parent.child(i).marks); i--) rangeFrom -= parent.child(i).nodeSize;
        for (let i = index + 1; i < parent.childCount && mark.isInSet(parent.child(i).marks); i++) rangeTo += parent.child(i).nodeSize;
        return {from: rangeFrom, to: rangeTo, href: mark.attrs.href};
    }

    function canLink(state) {
        return state.selection.$from.parent.type.allowsMarkType(M.link);
    }

    let labels = {
        bold: 'Bold',
        subscript: 'Subscript',
        superscript: 'Superscript',
        link: 'Link',
        heading: 'Section heading',
        bulletList: 'Bullet list',
        orderedList: 'Numbered list',
        codeBlock: 'Preformatted text',
        undo: 'Undo',
        redo: 'Redo',
        linkPlaceholder: 'https://',
        linkApply: 'Apply',
        linkRemove: 'Remove link',
        linkCancel: 'Cancel',
        linkInvalid: 'Enter a full address starting with https://, http://, ftp:// or mailto:'
    };

    const isMac = typeof navigator !== 'undefined' && /Mac|iP(hone|[oa]d)/.test(navigator.platform);

    // "Mod-Alt-1" -> "Ctrl+Alt+1" or "⌘⌥1"
    function shortcut(keys) {
        const names = isMac ? {Mod: '⌘', Alt: '⌥', Shift: '⇧'} : {Mod: 'Ctrl', Alt: 'Alt', Shift: 'Shift'};
        return keys.split('-').map(key => names[key] || key).join(isMac ? '' : '+');
    }

    function el(tag, attrs, children) {
        const element = document.createElement(tag);
        Object.entries(attrs || {}).forEach(([key, value]) => {
            if (key === 'class') element.className = value;
            else element.setAttribute(key, value);
        });
        (children || []).forEach(child => element.appendChild(child));
        return element;
    }

    function create(options) {
        const mount = options.mount;
        const input = options.input;
        if (!mount || !input) {
            console.error('IptDocBookEditor: mount and input elements are required');
            return null;
        }

        const linkBar = buildLinkBar();
        let view;

        const openLink = (state) => {
            if (!canLink(state)) return false;
            showLinkBar();
            return true;
        };

        const tools = [
            {icon: 'bi-type-bold', label: labels.bold, keys: 'Mod-B', run: toggleMark(M.emphasis), active: s => isMarkActive(s, M.emphasis)},
            {icon: 'bi-subscript', label: labels.subscript, keys: 'Mod-,', run: toggleMark(M.subscript), active: s => isMarkActive(s, M.subscript)},
            {icon: 'bi-superscript', label: labels.superscript, keys: 'Mod-.', run: toggleMark(M.superscript), active: s => isMarkActive(s, M.superscript)},
            {icon: 'bi-link-45deg', label: labels.link, keys: 'Mod-K', run: (s, d) => (d ? openLink(s) : canLink(s)), active: s => !!linkRange(s)},
            'separator',
            {icon: 'bi-type-h1', label: labels.heading, keys: 'Mod-Alt-1', run: toggleBlock(N.heading), active: s => isBlockActive(s, N.heading)},
            {icon: 'bi-list-ul', label: labels.bulletList, run: toggleList(N.bullet_list), active: s => isListActive(s, N.bullet_list)},
            {icon: 'bi-list-ol', label: labels.orderedList, run: toggleList(N.ordered_list), active: s => isListActive(s, N.ordered_list)},
            {icon: 'bi-code-square', label: labels.codeBlock, keys: 'Mod-Alt-C', run: toggleBlock(N.code_block), active: s => isBlockActive(s, N.code_block)},
            'separator',
            {icon: 'bi-arrow-counterclockwise', label: labels.undo, keys: 'Mod-Z', run: undo},
            {icon: 'bi-arrow-clockwise', label: labels.redo, keys: isMac ? 'Mod-Shift-Z' : 'Mod-Y', run: redo}
        ];

        const toolbar = el('div', {class: 'ipt-dbe-toolbar', role: 'toolbar'});
        tools.forEach(tool => {
            if (tool === 'separator') {
                toolbar.appendChild(el('span', {class: 'ipt-dbe-separator', 'aria-hidden': 'true'}));
                return;
            }
            const title = tool.keys ? tool.label + ' (' + shortcut(tool.keys) + ')' : tool.label;
            const button = el('button', {type: 'button', class: 'btn btn-sm ipt-dbe-button', title, 'aria-label': tool.label},
                [el('i', {class: 'bi ' + tool.icon, 'aria-hidden': 'true'})]);
            // keep the editor selection when clicking a button
            button.addEventListener('mousedown', e => e.preventDefault());
            button.addEventListener('click', () => {
                tool.run(view.state, view.dispatch, view);
                if (!linkBar.element.classList.contains('show')) view.focus();
            });
            tool.button = button;
            toolbar.appendChild(button);
        });

        const content = el('div', {class: 'ipt-dbe-content'});
        const wrapper = el('div', {class: 'ipt-dbe'}, [toolbar, linkBar.element, content]);
        mount.appendChild(wrapper);

        function updateToolbar(state) {
            tools.forEach(tool => {
                if (!tool.button) return;
                tool.button.disabled = !tool.run(state);
                const active = tool.active ? tool.active(state) : false;
                tool.button.classList.toggle('active', active);
                if (tool.active) tool.button.setAttribute('aria-pressed', String(active));
            });
        }

        function sync(state, notify) {
            input.value = docToDocBook(state.doc);
            // lets the unsaved-changes tracking notice toolbar edits too
            if (notify) input.dispatchEvent(new Event('input', {bubbles: true}));
        }

        function buildLinkBar() {
            const field = el('input', {
                type: 'text', inputmode: 'url', class: 'form-control form-control-sm',
                placeholder: labels.linkPlaceholder, 'aria-label': labels.link
            });
            const apply = el('button', {type: 'button', class: 'btn btn-sm btn-outline-gbif-primary'});
            apply.textContent = labels.linkApply;
            const remove = el('button', {type: 'button', class: 'btn btn-sm btn-outline-secondary'});
            remove.textContent = labels.linkRemove;
            const cancel = el('button', {type: 'button', class: 'btn btn-sm btn-outline-secondary'});
            cancel.textContent = labels.linkCancel;
            const error = el('div', {class: 'ipt-dbe-link-error text-danger'});
            const element = el('div', {class: 'ipt-dbe-linkbar'}, [
                el('div', {class: 'd-flex gap-1'}, [field, apply, remove, cancel]), error
            ]);

            apply.addEventListener('click', () => applyLink());
            remove.addEventListener('click', () => removeLink());
            cancel.addEventListener('click', () => hideLinkBar());
            field.addEventListener('keydown', e => {
                if (e.key === 'Enter') {
                    // do not submit the metadata form
                    e.preventDefault();
                    applyLink();
                } else if (e.key === 'Escape') {
                    e.preventDefault();
                    hideLinkBar();
                }
            });

            return {element, field, remove, error};
        }

        function showLinkBar() {
            const range = linkRange(view.state);
            linkBar.field.value = range ? range.href : '';
            linkBar.remove.hidden = !range;
            linkBar.error.textContent = '';
            linkBar.element.classList.add('show');
            linkBar.field.focus();
            linkBar.field.select();
        }

        function hideLinkBar() {
            linkBar.element.classList.remove('show');
            view.focus();
        }

        function applyLink() {
            const href = safeUrl(linkBar.field.value);
            if (!href) {
                linkBar.error.textContent = labels.linkInvalid;
                linkBar.field.focus();
                return;
            }
            const state = view.state;
            const mark = M.link.create({href});
            const range = linkRange(state);
            const {from, to, empty} = state.selection;
            let tr = state.tr;

            if (range) {
                tr = tr.removeMark(range.from, range.to, M.link).addMark(range.from, range.to, mark);
            } else if (!empty) {
                tr = tr.addMark(from, to, mark);
            } else {
                // nothing selected: insert the address itself as the link text
                tr = tr.insert(from, schema.text(href, [mark]));
                tr = tr.setSelection(TextSelection.create(tr.doc, from + href.length));
            }
            view.dispatch(tr.setStoredMarks([]));
            hideLinkBar();
        }

        function removeLink() {
            const range = linkRange(view.state);
            if (range) view.dispatch(view.state.tr.removeMark(range.from, range.to, M.link));
            hideLinkBar();
        }

        const listKeys = {
            'Enter': splitListItem(N.list_item),
            'Tab': sinkListItem(N.list_item),
            'Shift-Tab': liftListItem(N.list_item)
        };

        const editorKeys = {
            'Mod-z': undo,
            'Shift-Mod-z': redo,
            'Mod-y': redo,
            'Mod-b': toggleMark(M.emphasis),
            // DocBook has a single emphasis, so italic maps to it as well
            'Mod-i': toggleMark(M.emphasis),
            'Mod-,': toggleMark(M.subscript),
            'Mod-.': toggleMark(M.superscript),
            'Mod-k': openLink,
            'Mod-Alt-1': toggleBlock(N.heading),
            'Mod-Alt-0': setBlockType(N.paragraph),
            'Mod-Alt-c': toggleBlock(N.code_block),
            // there are no line breaks in DocBook: start a new paragraph instead
            'Shift-Enter': baseKeymap['Enter'],
            'Mod-Enter': exitCode
        };

        const rules = inputRules({
            rules: [
                wrappingInputRule(/^\s*([-*])\s$/, N.bullet_list),
                wrappingInputRule(/^\s*(\d+)\.\s$/, N.ordered_list),
                textblockTypeInputRule(/^#\s$/, N.heading),
                textblockTypeInputRule(/^```$/, N.code_block)
            ]
        });

        const state = EditorState.create({
            doc: docFromDocBook(options.value),
            plugins: [rules, keymap(listKeys), keymap(editorKeys), keymap(baseKeymap), history()]
        });

        view = new EditorView(content, {
            state,
            attributes: {
                role: 'textbox',
                'aria-multiline': 'true',
                'aria-label': options.ariaLabel || ''
            },
            dispatchTransaction(tr) {
                const newState = view.state.apply(tr);
                view.updateState(newState);
                updateToolbar(newState);
                if (tr.docChanged) sync(newState, true);
            }
        });

        updateToolbar(view.state);
        sync(view.state, false);

        return {
            view,
            getValue: () => docToDocBook(view.state.doc),
            focus: () => view.focus()
        };
    }

    /**
     * Renders a DocBook value read-only into an element, through the editor schema: only the elements the schema
     * knows are created (with DOM methods, never innerHTML), and links keep http, https, ftp and mailto addresses
     * only. Leaves the element as it is if the value is empty.
     */
    function render(element, value) {
        if (!element || typeof value !== 'string' || !value.trim()) return;
        const doc = docFromDocBook(value);
        const fragment = PM.model.DOMSerializer.fromSchema(schema).serializeFragment(doc.content);
        element.replaceChildren(fragment);
        element.classList.add('ipt-docbook');
    }

    /**
     * Renders every element with a data-docbook attribute (the DocBook value, HTML-escaped by the template).
     */
    function renderAll(root) {
        (root || document).querySelectorAll('[data-docbook]').forEach(element => {
            render(element, element.getAttribute('data-docbook'));
            element.removeAttribute('data-docbook');
        });
    }

    global.IptDocBookEditor = {
        create,
        render,
        renderAll,
        setLabels: custom => {
            labels = Object.assign({}, labels, custom);
        },
        // exposed for tests and for rendering DocBook elsewhere
        schema,
        fromDocBook: docFromDocBook,
        toDocBook: docToDocBook
    };
})(window);
