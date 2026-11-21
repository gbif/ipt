/*
 * Project page (manage/eml/project.ftl): personnel, awards and related projects as folding cards, like the contacts
 * page. A list ([data-list]) holds cards ([data-item]); every input carries data-name, its path inside the card
 * (userIds[].identifier for a repeatable value). reindex() turns it into the form name, e.g.
 * eml.project.relatedProjects[1].personnel[0].lastName, after every add, remove and move, so the posted lists follow
 * the card order. A related project card holds a list of its own (personnel), named relative to the card.
 */
(function () {
    'use strict';

    const P = window.IptPeople;
    const FLAGS = ['contact', 'creator', 'metadataProvider'];

    let root, form, text, picker, roleLabels = {};

    document.addEventListener('DOMContentLoaded', init);

    function init() {
        root = document.getElementById('project-page');
        if (!root) {
            return;
        }
        form = document.getElementById('project-form');
        const i18n = document.getElementById('pp-i18n');
        text = i18n.dataset;
        i18n.querySelectorAll('[data-role]').forEach(el => roleLabels[el.dataset.role] = el.textContent.trim());

        root.querySelectorAll('[data-item]').forEach(initCard);
        root.addEventListener('click', onClick);
        root.addEventListener('keydown', onKeydown);
        root.addEventListener('input', onInput);
        root.addEventListener('change', onInput);
        root.addEventListener('show.bs.dropdown', onDropdownShow);
        form.addEventListener('submit', onSubmit);
        topLists().forEach(initSortable);
        picker = P.picker({
            url: root.dataset.peopleUrl,
            text: text,
            usageText: usageText,
            onPick: (person, target) => copyInto(target, person.agent, picker.list)
        });

        reindex();
        refreshAll();

        // cards with posted errors start open; bring the first one into view
        const firstWithErrors = root.querySelector('[data-item].has-errors');
        if (firstWithErrors) {
            setTimeout(() => firstWithErrors.scrollIntoView({behavior: 'smooth', block: 'center'}), 100);
        }
    }

    // ---------- reading ----------

    function topLists() {
        return Array.from(root.querySelectorAll('[data-list]')).filter(list => !list.closest('[data-item]'));
    }

    function items(list) {
        return Array.from(list.querySelector(':scope > [data-items]').children).filter(el => el.matches('[data-item]'));
    }

    // the elements of a card itself, not of the cards nested in it
    function own(item, selector) {
        return Array.from(item.querySelectorAll(selector)).filter(el => el.closest('[data-item]') === item);
    }

    function listOf(item) {
        return item.parentElement.closest('[data-list]');
    }

    function field(item, name) {
        const el = own(item, `[data-name="${name}"]`)[0];
        return el ? el.value.trim() : '';
    }

    function repRows(group) {
        return Array.from(group.querySelectorAll(':scope > [data-rep-rows] > [data-rep-row]'));
    }

    function personName(item) {
        return [field(item, 'salutation'), field(item, 'firstName'), field(item, 'lastName')].filter(Boolean).join(' ');
    }

    function displayName(item) {
        return item.dataset.kind === 'person' ? personName(item) : field(item, 'title');
    }

    // ---------- names and indexes ----------

    function setName(el, name) {
        el.name = name;
        el.id = name;
    }

    function reindex() {
        topLists().forEach(list => reindexList(list, ''));
    }

    function reindexList(list, prefix) {
        items(list).forEach((item, index) => {
            const base = `${prefix}${list.dataset.list}[${index}].`;
            own(item, '[data-rep]').forEach(group => {
                repRows(group).forEach((row, rowIndex) => {
                    row.querySelectorAll('[data-name]').forEach(el => setName(el, base + el.dataset.name.replace('[]', `[${rowIndex}]`)));
                    const full = row.querySelector('[data-id-full]');
                    if (full) {
                        full.id = `${base}userIds[${rowIndex}].full`;
                    }
                });
                const first = group.querySelector('[data-rep-rows] input:not([type=hidden])');
                group.querySelector('[data-rep-label]').htmlFor = first ? first.id : '';
            });
            own(item, '[data-name]').forEach(el => {
                if (!el.closest('[data-rep-row]')) {
                    setName(el, base + el.dataset.name);
                }
            });
            own(item, 'label[data-for]').forEach(label => label.htmlFor = base + label.dataset.for);
            own(item, '[data-list]').forEach(nested => reindexList(nested, base));
        });
    }

    // ---------- drawing ----------

    const esc = P.esc;

    function refreshAll() {
        root.querySelectorAll('[data-list]').forEach(list => {
            const all = items(list);
            all.forEach((item, index) => {
                item.querySelector('[data-act="up"]').disabled = index === 0;
                item.querySelector('[data-act="down"]').disabled = index === all.length - 1;
            });
            list.querySelector(':scope > [data-empty]').hidden = all.length > 0;
        });
        root.querySelectorAll('[data-item]').forEach(refreshCard);
    }

    function refreshCard(item) {
        const kind = item.dataset.kind;
        const name = displayName(item);
        const nameEl = own(item, '[data-summary-name]')[0];
        nameEl.textContent = name || text['new' + kind.charAt(0).toUpperCase() + kind.slice(1)];
        nameEl.classList.toggle('party-name-empty', !name);

        let problem = '';
        const sub = [];
        const chips = [];
        if (kind === 'person') {
            if (!field(item, 'lastName')) {
                problem = text.needsLastName;
            }
            const role = own(item, 'select[data-name="role"]')[0];
            if (role.value) {
                sub.push(role.options[role.selectedIndex].text.trim());
            }
            const recognised = own(item, '[data-id-row]').map(P.detectRow).find(Boolean);
            if (recognised) {
                chips.push(`<span class="party-chip">${esc(recognised.directory.name)}</span>`);
            }
        } else if (kind === 'award') {
            if (!field(item, 'title') || !field(item, 'funderName')) {
                problem = text.awardNeeds;
            }
            sub.push(field(item, 'funderName'), field(item, 'awardNumber'));
        } else {
            const people = items(own(item, '[data-list]')[0]);
            if (!field(item, 'title')) {
                problem = text.needsTitle;
            }
            sub.push(field(item, 'identifier'), `${text.personnel}: ${people.length}`);
            item.classList.toggle('pp-no-people', !people.length);
        }
        if (item.classList.contains('has-errors')) {
            chips.unshift(`<span class="party-chip err">${esc(text.hasErrors)}</span>`);
        }

        const line = sub.filter(Boolean).join(' \u00B7 ');
        const subEl = own(item, '[data-summary-sub]')[0];
        if (problem) {
            subEl.innerHTML = `<span class="err">${esc(problem)}</span>` + (line ? ' \u00B7 ' + esc(line) : '');
        } else {
            subEl.textContent = line || '\u00A0';
        }
        own(item, '[data-summary-chips]')[0].innerHTML = chips.join('');
        item.classList.toggle('invalid', !!problem || item.classList.contains('has-errors') || item.classList.contains('pp-no-people'));
    }

    // ---------- cards ----------

    function initCard(item) {
        own(item, '[data-id-row]').forEach(row => P.initIdentifierRow(row, text));
    }

    function newCard(list) {
        const fragment = document.getElementById(list.dataset.template).content.cloneNode(true);
        const item = fragment.querySelector('[data-item]');
        list.querySelector(':scope > [data-items]').appendChild(fragment);
        item.querySelectorAll('[data-item]').forEach(initCard);
        initCard(item);
        if (typeof initInfoPopovers === 'function') {
            initInfoPopovers(item);
        }
        return item;
    }

    function setOpen(item, open) {
        item.classList.toggle('open', open);
        own(item, '.party-summary')[0].setAttribute('aria-expanded', open);
    }

    function changed() {
        // tells the unsaved changes warning (footer.ftl) that something changed
        form.dispatchEvent(new Event('input', {bubbles: true}));
    }

    function toast(message) {
        const el = document.getElementById('party-toast');
        el.textContent = message;
        el.classList.add('show');
        clearTimeout(toast.timer);
        toast.timer = setTimeout(() => el.classList.remove('show'), 2400);
    }

    function move(item, step) {
        const all = items(listOf(item));
        const neighbour = all[all.indexOf(item) + step];
        if (!neighbour) {
            return;
        }
        item.parentElement.insertBefore(item, step < 0 ? neighbour : neighbour.nextSibling);
        reindex();
        refreshAll();
        changed();
        toast(`${text.moved} ${items(listOf(item)).indexOf(item) + 1}`);
    }

    // drag by the grip; only the top-level lists have grips, so a related project's people move with up and down
    function initSortable(list) {
        if (typeof sortable !== 'function') {
            return;
        }
        const holder = list.querySelector(':scope > [data-items]');
        const instance = sortable(holder, {
            forcePlaceholderSize: true,
            placeholderClass: 'border',
            handle: '.party-grip',
            items: '[data-item]'
        });
        if (instance[0] && !instance[0].dataset.ppSortable) {
            instance[0].dataset.ppSortable = 'true';
            instance[0].addEventListener('sortupdate', () => {
                reindex();
                refreshAll();
                changed();
            });
        }
    }

    function addRow(group, focus) {
        const fragment = group.querySelector(':scope > template[data-rep-template]').content.cloneNode(true);
        const row = fragment.querySelector('[data-rep-row]');
        group.querySelector('[data-rep-rows]').appendChild(fragment);
        if (row.hasAttribute('data-id-row')) {
            P.initIdentifierRow(row, text);
        }
        reindex();
        if (focus) {
            row.querySelector('input:not([type=hidden])').focus();
        }
        return row;
    }

    // ---------- copying people ----------

    function agentOf(item) {
        return {
            salutation: field(item, 'salutation'),
            firstName: field(item, 'firstName'),
            lastName: field(item, 'lastName'),
            userIds: P.userIdsOf(item)
        };
    }

    // the people of the contacts page
    function contacts() {
        return Array.from(document.querySelectorAll('#pp-contacts [data-source]')).map(el => ({
            agent: {
                salutation: el.dataset.salutation,
                firstName: el.dataset.firstName,
                lastName: el.dataset.lastName,
                userIds: Array.from(el.querySelectorAll('[data-identifier]')).map(i => ({directory: i.dataset.directory, identifier: i.dataset.identifier}))
            },
            roles: el.dataset.roles
        }));
    }

    // names and identifiers only: the role is what this person does in this project
    function applyAgent(item, agent) {
        ['salutation', 'firstName', 'lastName'].forEach(name => own(item, `[data-name="${name}"]`)[0].value = agent[name] || '');
        const ids = own(item, '[data-rep="identifier"]')[0];
        repRows(ids).forEach(row => row.remove());
        (agent.userIds || []).filter(userId => userId && userId.identifier).forEach(userId => P.setIdentifier(addRow(ids, false), userId, text));
        reindex();
        setOpen(item, true);
        refreshAll();
        changed();
    }

    function copyInto(target, agent, list) {
        let item = target;
        if (!item) {
            item = newCard(list);
            reindex();
            topLists().forEach(initSortable);
        }
        applyAgent(item, agent);
        toast(`${text.copied}: ${P.nameOfAgent(agent)}`);
        item.scrollIntoView({behavior: 'smooth', block: 'nearest'});
    }

    function menuButton(act, index, name, note) {
        return `<button type="button" class="dropdown-item" data-act="${act}" data-src="${index}">${esc(name)}`
            + (note ? `<small>${esc(note)}</small>` : '') + '</button>';
    }

    // people of the contacts page and the other people on this page, then the other resources
    function buildCopyMenu(menu, forItem) {
        const people = contacts();
        const others = Array.from(root.querySelectorAll('[data-item][data-kind="person"]')).filter(item => item !== forItem && personName(item));
        let html = '';
        if (people.length) {
            html += `<h6 class="dropdown-header">${esc(text.copyContacts)}</h6><div class="party-menu-list">`
                + people.map((person, index) => menuButton('copy-contact', index, P.nameOfAgent(person.agent), person.roles)).join('') + '</div>';
        }
        html += `<h6 class="dropdown-header">${esc(forItem ? text.copyReplace : text.copyThisPage)}</h6>`
            + (others.length
                ? '<div class="party-menu-list">' + others.map(item => {
                    const role = own(item, 'select[data-name="role"]')[0];
                    return menuButton('copy-person', allPeople().indexOf(item), personName(item), role.value ? role.options[role.selectedIndex].text.trim() : '');
                }).join('') + '</div>'
                : `<span class="dropdown-item-text text-muted">${esc(text.copyNobody)}</span>`);
        html += '<div class="dropdown-divider"></div>'
            + `<button type="button" class="dropdown-item" data-act="copy-another">${esc(text.copyAnother)}</button>`;
        menu.innerHTML = html;
    }

    function allPeople() {
        return Array.from(root.querySelectorAll('[data-item][data-kind="person"]'));
    }

    function usageText(usage) {
        const roles = FLAGS.filter(name => usage[name]).map(name => text['chip' + name.charAt(0).toUpperCase() + name.slice(1)].toLowerCase())
            .concat((usage.associatedPartyRoles || []).map(role => (roleLabels[role] || role).toLowerCase()));
        if (usage.projectPersonnel) {
            roles.push(text.pickerPersonnel);
        }
        return usage.resource + (roles.length ? ` (${roles.join(', ')})` : '');
    }

    // ---------- events ----------

    function onDropdownShow(event) {
        const toggle = event.target;
        if (toggle.dataset.act === 'copy-menu') {
            buildCopyMenu(toggle.parentElement.querySelector('.dropdown-menu'), toggle.closest('[data-item][data-kind="person"]'));
        }
    }

    function onClick(event) {
        const target = event.target.closest('[data-act]');
        if (!target || !root.contains(target)) {
            return;
        }
        const act = target.dataset.act;
        const item = target.closest('[data-item]');
        // the card a copy goes into: the person card of the menu, or none (a new card in the menu's list)
        const person = target.closest('[data-item][data-kind="person"]');
        const list = target.closest('[data-list]');

        switch (act) {
            case 'toggle':
                // clicks on the buttons, menus and grip in the header don't fold the card
                if (event.target.closest('button, a, .dropdown-menu, .party-grip')) {
                    return;
                }
                setOpen(item, !item.classList.contains('open'));
                break;
            case 'up':
            case 'down':
                move(item, act === 'up' ? -1 : 1);
                (target.disabled ? own(item, `[data-act="${act === 'up' ? 'down' : 'up'}"]`)[0] : target).focus();
                break;
            case 'remove': {
                const name = displayName(item);
                const kind = item.dataset.kind;
                item.remove();
                reindex();
                refreshAll();
                changed();
                toast(`${text.removed}: ${name || text['new' + kind.charAt(0).toUpperCase() + kind.slice(1)]}`);
                break;
            }
            case 'add': {
                const created = newCard(list);
                setOpen(created, true);
                if (created.dataset.kind === 'project') {
                    // a related project needs at least one person
                    setOpen(newCard(own(created, '[data-list]')[0]), true);
                }
                reindex();
                refreshAll();
                topLists().forEach(initSortable);
                changed();
                created.querySelector('input:not([type=hidden])').focus();
                break;
            }
            case 'add-rep':
                addRow(target.closest('[data-rep]'), true);
                changed();
                break;
            case 'rm-rep': {
                const row = target.closest('[data-rep-row]');
                const group = row.closest('[data-rep]');
                row.remove();
                reindex();
                refreshCard(item);
                changed();
                const next = group.querySelector('[data-rep-rows] input:not([type=hidden])');
                (next || group.querySelector('[data-act="add-rep"]')).focus();
                break;
            }
            case 'copy-contact':
                copyInto(person, contacts()[Number(target.dataset.src)].agent, list);
                break;
            case 'copy-person':
                copyInto(person, agentOf(allPeople()[Number(target.dataset.src)]), list);
                break;
            case 'copy-another':
                picker.list = list;
                picker.open(person);
                break;
        }
    }

    function onKeydown(event) {
        if ((event.key === 'Enter' || event.key === ' ') && event.target.matches('.party-summary')) {
            event.preventDefault();
            const item = event.target.closest('[data-item]');
            setOpen(item, !item.classList.contains('open'));
        }
    }

    function onInput(event) {
        const item = event.target.closest('[data-item]');
        if (!item) {
            return;
        }
        const row = event.target.closest('[data-id-row]');
        if (row) {
            if (event.target.tagName === 'SELECT') {
                // a directory picked by the user stays, whatever is typed
                delete event.target.dataset.auto;
            }
            P.updateIdentifier(row, text);
        }
        // a posted error no longer applies once its field is edited; a related project also holds its people's errors
        event.target.classList.remove('is-invalid');
        for (let card = item; card; card = card.parentElement.closest('[data-item]')) {
            if (!card.querySelector('.is-invalid')) {
                card.classList.remove('has-errors');
                own(card, '.field-error').forEach(el => el.remove());
            }
        }
        refreshAll();
    }

    // empty values would be posted as empty identifiers; leave them out
    function onSubmit() {
        root.querySelectorAll('[data-rep-row]').forEach(row => {
            const input = row.querySelector('[data-id-full]') || row.querySelector('input');
            if (!input.value.trim()) {
                row.remove();
            }
        });
        root.querySelectorAll('[data-id-row]').forEach(row => P.updateIdentifier(row, text));
        reindex();
    }
})();
