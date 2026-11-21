/*
 * Contacts page (manage/eml/contacts.ftl): one list of people and organisations, each a folding card holding its
 * roles. Every input carries data-name, its path inside the party (agent.email[] for a list); reindex() turns it into
 * the form name eml.parties[i].<path> after every add, remove and move, so the posted list follows the card order.
 */
(function () {
    'use strict';

    // dataset roles: hidden inputs eml.parties[i].<flag> = true/false
    const FLAGS = ['contact', 'creator', 'metadataProvider'];
    // associated party roles that put the party in the citation, see Resource.generateResourceCitation
    const CITED_OTHER_ROLES = ['originator', 'metadataProvider'];
    // not offered in the "+ Role" menu: the Metadata provider chip covers it (a role already held still shows)
    const HIDDEN_OTHER_ROLES = ['metadataProvider'];
    const P = window.IptPeople;
    const REPEATABLE = ['position', 'email', 'homepage', 'phone', 'address'];

    let root, list, form, text, roleLabels = {}, roleDescriptions = {}, filter = 'all';
    let picker;

    document.addEventListener('DOMContentLoaded', init);

    function init() {
        root = document.getElementById('parties');
        if (!root) {
            return;
        }
        list = document.getElementById('party-items');
        form = document.getElementById('contacts-form');
        const i18n = document.getElementById('party-i18n');
        text = i18n.dataset;
        i18n.querySelectorAll('[data-role]').forEach(el => {
            roleLabels[el.dataset.role] = el.textContent.trim();
            roleDescriptions[el.dataset.role] = el.dataset.description || '';
        });

        cards().forEach(card => initCard(card, false));

        root.addEventListener('click', onClick);
        root.addEventListener('keydown', onKeydown);
        root.addEventListener('input', onInput);
        root.addEventListener('change', onInput);
        root.addEventListener('show.bs.dropdown', onDropdownShow);
        // select2 fires jQuery events only
        $(root).on('change', 'select.party-country', () => refreshAll());
        form.addEventListener('submit', onSubmit);
        initCitationSwitch();
        initSortable();
        initPicker();

        reindex();
        refreshAll();

        // cards with posted errors start open; bring the first one into view
        const firstWithErrors = list.querySelector('[data-party].has-errors');
        if (firstWithErrors) {
            setTimeout(() => firstWithErrors.scrollIntoView({behavior: 'smooth', block: 'center'}), 100);
        }
    }

    // ---------- reading a card ----------

    function cards() {
        return Array.from(list.querySelectorAll(':scope > [data-party]'));
    }

    function visibleCards() {
        return cards().filter(card => !card.hidden);
    }

    function field(card, name) {
        const el = card.querySelector(`[data-name="${name}"]`);
        return el ? el.value.trim() : '';
    }

    function flag(card, name) {
        return card.querySelector(`input[data-flag="${name}"]`).value === 'true';
    }

    function setFlag(card, name, on) {
        card.querySelector(`input[data-flag="${name}"]`).value = on ? 'true' : 'false';
    }

    function otherRoles(card) {
        return Array.from(card.querySelectorAll('[data-other-roles] input')).map(input => input.value);
    }

    function repGroup(card, name) {
        return card.querySelector(`[data-rep="${name}"]`);
    }

    function repRows(group) {
        return Array.from(group.querySelectorAll(':scope > [data-rep-rows] > [data-rep-row]'));
    }

    function repValues(card, name) {
        return repRows(repGroup(card, name))
            .map(row => row.querySelector('input').value.trim())
            .filter(Boolean);
    }

    function displayName(card) {
        const name = [field(card, 'agent.salutation'), field(card, 'agent.firstName'), field(card, 'agent.lastName')]
            .filter(Boolean).join(' ');
        return name || field(card, 'agent.organisation') || repValues(card, 'position')[0] || '';
    }

    function subline(card) {
        const person = [field(card, 'agent.firstName'), field(card, 'agent.lastName')].filter(Boolean).join(' ');
        const organisation = field(card, 'agent.organisation');
        const position = repValues(card, 'position')[0];
        const parts = [];
        if (person && organisation) {
            parts.push(organisation);
        }
        if (position && (person || organisation)) {
            parts.push(position);
        }
        const email = repValues(card, 'email')[0];
        if (email) {
            parts.push(email);
        }
        return parts.join(' \u00B7 ');
    }

    // the validator's rule: at least one of last name, organisation and position
    function hasName(card) {
        return !!(field(card, 'agent.lastName') || field(card, 'agent.organisation') || repValues(card, 'position').length);
    }

    function hasRole(card) {
        return FLAGS.some(name => flag(card, name)) || otherRoles(card).length > 0;
    }

    // same as Resource.getCitationAgentName
    function citationName(card) {
        const lastName = field(card, 'agent.lastName');
        const firstNames = field(card, 'agent.firstName');
        const organisation = field(card, 'agent.organisation');
        if (lastName) {
            if (!firstNames) {
                return lastName;
            }
            return lastName + ' ' + firstNames.split(/\s+/).map(name => name.charAt(0).toUpperCase()).join(' ');
        }
        return !firstNames && organisation ? organisation : '';
    }

    // authors in citation order, see Resource.generateResourceCitation: creators, metadata providers, then
    // associated parties that are originators or metadata providers; each name once
    function citationAuthors() {
        const all = cards();
        const authors = [];
        const add = card => {
            const name = citationName(card);
            if (name && !authors.some(author => author.name === name)) {
                authors.push({name: name, card: card});
            }
        };
        all.filter(card => flag(card, 'creator')).forEach(add);
        all.filter(card => flag(card, 'metadataProvider')).forEach(add);
        all.forEach(card => otherRoles(card).filter(role => CITED_OTHER_ROLES.includes(role)).forEach(() => add(card)));
        return authors;
    }

    // ---------- names and indexes ----------

    function setName(el, name) {
        el.name = name;
        // several hidden inputs share the roles name; the rest get the name as id, which form-validation.js uses
        if (el.dataset.name !== 'associatedPartyRoles') {
            el.id = name;
        }
    }

    function reindex() {
        cards().forEach((card, index) => {
            const base = `eml.parties[${index}].`;
            card.querySelectorAll('[data-rep]').forEach(group => {
                repRows(group).forEach((row, rowIndex) => {
                    row.querySelectorAll('[data-name]').forEach(el => setName(el, base + el.dataset.name.replace('[]', `[${rowIndex}]`)));
                    const full = row.querySelector('[data-id-full]');
                    if (full) {
                        full.id = `${base}agent.userIds[${rowIndex}].full`;
                    }
                });
                const first = group.querySelector('[data-rep-rows] input:not([type=hidden])');
                group.querySelector('[data-rep-label]').htmlFor = first ? first.id : '';
            });
            card.querySelectorAll('[data-name]').forEach(el => {
                if (!el.closest('[data-rep-row]')) {
                    setName(el, base + el.dataset.name);
                }
            });
            card.querySelectorAll('label[data-for]').forEach(label => label.htmlFor = base + label.dataset.for);
        });
    }

    // ---------- drawing ----------

    const esc = P.esc;

    function flagLabel(name) {
        return text['chip' + name.charAt(0).toUpperCase() + name.slice(1)];
    }

    function roleLabel(role) {
        return roleLabels[role] || role;
    }

    function refreshAll() {
        const all = cards();
        const authors = citationAuthors();

        all.forEach(card => {
            card.hidden = filter !== 'all' && !flag(card, filter);
            refreshCard(card, authors);
        });

        // filter tabs: how many hold each role; contacts and creators are required
        root.querySelector('[data-count="all"]').textContent = all.length;
        FLAGS.forEach(name => {
            const count = all.filter(card => flag(card, name)).length;
            const tab = root.querySelector(`.party-ftab[data-filter="${name}"]`);
            const state = tab.querySelector('[data-state]');
            const missing = count === 0 && tab.dataset.required === 'true';
            tab.querySelector('[data-count]').textContent = count;
            tab.classList.toggle('missing', missing);
            state.textContent = missing ? text.filterRequired : '';
            state.hidden = !missing;
        });
        root.querySelectorAll('.party-ftab').forEach(tab => {
            const active = tab.dataset.filter === filter;
            tab.classList.toggle('active', active);
            tab.setAttribute('aria-pressed', active);
        });

        // up and down move among the cards shown
        const shown = visibleCards();
        shown.forEach((card, index) => {
            card.querySelector('[data-act="up"]').disabled = index === 0;
            card.querySelector('[data-act="down"]').disabled = index === shown.length - 1;
        });

        const empty = root.querySelector('[data-empty]');
        empty.hidden = shown.length > 0;
        empty.textContent = all.length ? text.filterEmpty : text.empty;

        root.querySelector('[data-add-label]').textContent = filter === 'all' ? text.add : text['add' + filter.charAt(0).toUpperCase() + filter.slice(1)];
        root.querySelector('[data-act="toggle-all"]').textContent = shown.some(card => card.classList.contains('open')) ? text.collapseAll : text.expandAll;
        root.querySelector('[data-act="toggle-all"]').hidden = shown.length === 0;

        refreshCitation(authors);
    }

    // auto-generation of the citation, the same setting as on the Citations and links page (#3322)
    function initCitationSwitch() {
        const toggle = root.querySelector('[data-citation-switch]');
        if (!toggle) {
            return;
        }
        toggle.addEventListener('change', () => {
            root.querySelector('[data-citation-auto]').value = String(toggle.checked);
            // only there when it was on; empties the generated citation when switched off
            const clear = root.querySelector('[data-citation-clear]');
            if (clear) {
                clear.disabled = toggle.checked;
            }
            root.querySelector('[data-citation-on]').hidden = !toggle.checked;
            root.querySelector('[data-citation-off]').hidden = toggle.checked;
        });
    }

    function refreshCitation(authors) {
        const out = root.querySelector('[data-citation-text]');
        root.querySelector('[data-citation]').hidden = filter !== 'all' && filter !== 'creator';
        if (!out) {
            return;
        }
        const names = authors.length
            ? authors.map(author => `<mark>${esc(author.name)}</mark>`).join(', ')
            : `<span class="party-citation-none">${esc(text.citationNoCreators)}</span>`;
        // the authors are what this page changes; the rest is shown muted
        out.innerHTML = names + `<span class="party-citation-rest">${esc(root.dataset.citationTail)}</span>`;
    }

    function refreshCard(card, authors) {
        if (!card) {
            return;
        }
        authors = authors || citationAuthors();
        const name = displayName(card);
        const named = hasName(card);
        const withRole = hasRole(card);

        const nameEl = card.querySelector('[data-summary-name]');
        nameEl.textContent = name || text.new;
        nameEl.classList.toggle('party-name-empty', !name);

        const sub = card.querySelector('[data-summary-sub]');
        if (!named) {
            sub.innerHTML = `<span class="err">${esc(text.needsName)}</span>`;
        } else if (!withRole) {
            sub.innerHTML = `<span class="err">${esc(text.needsRole)}</span>` + (subline(card) ? ' \u00B7 ' + esc(subline(card)) : '');
        } else {
            sub.textContent = subline(card) || '\u00A0';
        }

        const chips = [];
        if (card.classList.contains('has-errors')) {
            chips.push(`<span class="party-chip err">${esc(text.hasErrors)}</span>`);
        }
        const recognised = Array.from(card.querySelectorAll('[data-id-row]'))
            .map(P.detectRow)
            .find(Boolean);
        if (recognised) {
            chips.push(`<span class="party-chip">${esc(recognised.directory.name)}</span>`);
        }
        card.querySelector('[data-summary-chips]').innerHTML = chips.join('');

        card.classList.toggle('invalid', !named || !withRole || card.classList.contains('has-errors'));
        card.querySelector('[data-name-error]').hidden = named;
        card.querySelector('[data-role-error]').hidden = withRole;
        card.classList.toggle('no-role', !withRole);

        // dataset role chips; the creator chip shows the author position
        card.querySelectorAll('.party-role[data-act="role"]').forEach(button => {
            const name = button.dataset.flag;
            const on = flag(card, name);
            const position = authors.findIndex(author => author.card === card) + 1;
            button.classList.toggle('on', on);
            button.setAttribute('aria-pressed', on);
            // says what a click does
            button.title = (on ? text.roleRemove : text.roleAdd) + ' ' + flagLabel(name);
            button.innerHTML = (on ? '' : '+ ') + esc(flagLabel(name))
                + (name === 'creator' && on && position ? ` <span class="n" title="${esc(text.authorPosition)}">#${position}</span>` : '');
        });

        // other (associated party) roles
        const roles = otherRoles(card);
        card.querySelector('[data-other-role-chips]').innerHTML = roles.map(role => {
            const cited = CITED_OTHER_ROLES.includes(role);
            return `<button type="button" class="party-role other on" data-act="rm-other-role" data-role="${esc(role)}"`
                + ` title="${esc(text.roleRemove + ' ' + roleLabel(role) + (cited ? ' (' + text.otherRolesCitedTitle + ')' : ''))}">`
                + esc(roleLabel(role)) + (cited ? ` <span class="n">\u00B7 ${esc(text.otherRolesCited)}</span>` : '')
                + ' <span class="n" aria-hidden="true">\u00D7</span></button>';
        }).join('');
        card.querySelector('[data-act="other-roles-menu"]').textContent = '+ ' + (roles.length ? text.otherRolesAdd : text.otherRolesAddFirst);

        // folded sections say what they hold
        const country = card.querySelector('select.party-country');
        const countryLabel = country && country.value ? country.options[country.selectedIndex].text.trim() : '';
        const address = [field(card, 'agent.address.city'), countryLabel].filter(Boolean).join(', ')
            || (repValues(card, 'address').length ? repValues(card, 'address')[0] : '');
        card.querySelector('[data-address-summary]').textContent = address ? ' \u2013 ' + address : '';
        const salutation = field(card, 'agent.salutation');
        card.querySelector('[data-more-summary]').textContent = salutation ? ': ' + salutation : '';
    }

    // ---------- cards ----------

    function initCard(card, isNew) {
        $(card).find('select.party-country').select2({
            placeholder: text.countryPlaceholder,
            language: {noResults: () => text.noResults},
            width: '100%',
            allowClear: true,
            theme: 'bootstrap4'
        });
        card.querySelectorAll('[data-id-row]').forEach(row => P.initIdentifierRow(row, text));
        if (isNew && typeof initInfoPopovers === 'function') {
            initInfoPopovers(card);
        }
        // address and salutation stay folded unless they hold an error
        card.querySelectorAll('details').forEach(details => {
            if (details.querySelector('.field-error li')) {
                details.open = true;
            }
        });
    }

    function newCard() {
        const fragment = document.getElementById('party-template').content.cloneNode(true);
        const card = fragment.querySelector('[data-party]');
        list.appendChild(fragment);
        initCard(card, true);
        return card;
    }

    function setOpen(card, open) {
        card.classList.toggle('open', open);
        card.querySelector('.party-summary').setAttribute('aria-expanded', open);
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

    // moves a card one place up or down among the cards shown, keeping hidden cards where they are
    function move(card, step) {
        const shown = visibleCards();
        const neighbour = shown[shown.indexOf(card) + step];
        if (!neighbour) {
            return;
        }
        if (step < 0) {
            list.insertBefore(card, neighbour);
        } else {
            list.insertBefore(card, neighbour.nextSibling);
        }
        reindex();
        refreshAll();
        changed();
        toast(`${text.moved} ${visibleCards().indexOf(card) + 1}`);
    }

    function initSortable() {
        if (typeof sortable !== 'function') {
            return;
        }
        const instance = sortable('#party-items', {
            forcePlaceholderSize: true,
            placeholderClass: 'border',
            handle: '.party-grip',
            items: '[data-party]'
        });
        if (instance[0] && !instance[0].dataset.partySortable) {
            instance[0].dataset.partySortable = 'true';
            instance[0].addEventListener('sortupdate', () => {
                reindex();
                refreshAll();
                changed();
            });
        }
    }

    // ---------- repeatable values and identifiers ----------

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

    function clearRows(group) {
        repRows(group).forEach(row => row.remove());
    }

    // ---------- copying ----------

    function agentOf(card) {
        const values = name => repValues(card, name);
        return {
            salutation: field(card, 'agent.salutation'),
            firstName: field(card, 'agent.firstName'),
            lastName: field(card, 'agent.lastName'),
            organisation: field(card, 'agent.organisation'),
            position: values('position'),
            email: values('email'),
            homepage: values('homepage'),
            phone: values('phone'),
            address: {
                address: values('address'),
                city: field(card, 'agent.address.city'),
                province: field(card, 'agent.address.province'),
                country: field(card, 'agent.address.country'),
                postalCode: field(card, 'agent.address.postalCode')
            },
            userIds: P.userIdsOf(card)
        };
    }

    // copies the contact details of an agent (from this page or the suggest-people JSON); roles are not copied
    function applyAgent(card, agent) {
        const set = (name, value) => {
            const el = card.querySelector(`[data-name="${name}"]`);
            el.value = value || '';
        };
        const address = agent.address || {};
        set('agent.salutation', agent.salutation);
        set('agent.firstName', agent.firstName);
        set('agent.lastName', agent.lastName);
        set('agent.organisation', agent.organisation);
        set('agent.address.city', address.city);
        set('agent.address.province', address.province);
        set('agent.address.postalCode', address.postalCode);
        $(card).find('select.party-country').val(address.country || '').trigger('change');

        const lists = {position: agent.position, email: agent.email, homepage: agent.homepage, phone: agent.phone, address: address.address};
        REPEATABLE.forEach(name => {
            const group = repGroup(card, name);
            clearRows(group);
            (lists[name] || []).filter(Boolean).forEach(value => addRow(group, false).querySelector('input').value = value);
        });

        const ids = repGroup(card, 'identifier');
        clearRows(ids);
        (agent.userIds || []).filter(userId => userId && userId.identifier).forEach(userId => {
            P.setIdentifier(addRow(ids, false), userId, text);
        });

        reindex();
        setOpen(card, true);
        refreshAll();
        changed();
    }

    const nameOfAgent = P.nameOfAgent;

    // adds associated party roles a card doesn't hold yet
    function addOtherRoles(card, roles) {
        const held = otherRoles(card);
        roles.filter(role => role && !held.includes(role)).forEach(role => {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.dataset.name = 'associatedPartyRoles';
            input.value = role;
            card.querySelector('[data-other-roles]').appendChild(input);
            held.push(role);
        });
    }

    // copies contact details and the associated party roles; the dataset roles (contact, creator, metadata
    // provider) are this resource's own choice and are left as they are
    function copyInto(target, agent, roles) {
        let card = target;
        if (!card) {
            card = newCard();
            if (filter !== 'all') {
                setFlag(card, filter, true);
            }
            reindex();
        }
        addOtherRoles(card, roles || []);
        applyAgent(card, agent);
        const pickRole = !hasRole(card) ? ' \u2013 ' + text.copiedPickRole : '';
        toast(`${text.copied}: ${nameOfAgent(agent)}${pickRole}`);
        card.scrollIntoView({behavior: 'smooth', block: 'nearest'});
    }

    function rolesSummary(card) {
        return FLAGS.filter(name => flag(card, name)).map(name => flagLabel(name).toLowerCase())
            .concat(otherRoles(card).map(role => roleLabel(role).toLowerCase()))
            .join(', ');
    }

    function buildCopyMenu(menu, forCard) {
        const others = cards().filter(card => card !== forCard && displayName(card));
        // the people scroll, so a long list stays on the screen and "From another resource" stays in view (#3322)
        menu.innerHTML = `<h6 class="dropdown-header">${esc(forCard ? text.copyReplace : text.copyThisPage)}</h6>`
            + (others.length
                ? '<div class="party-menu-list">' + others.map(card => `<button type="button" class="dropdown-item" data-act="copy-from" data-src="${cards().indexOf(card)}">`
                    + `${esc(displayName(card))}<small>${esc(rolesSummary(card) || text.copyNoRole)}</small></button>`).join('') + '</div>'
                : `<span class="dropdown-item-text text-muted">${esc(text.copyNobody)}</span>`)
            + '<div class="dropdown-divider"></div>'
            + `<button type="button" class="dropdown-item" data-act="copy-another">${esc(text.copyAnother)}</button>`;
    }

    function buildOtherRolesMenu(menu, card) {
        const held = otherRoles(card);
        menu.innerHTML = `<h6 class="dropdown-header">${esc(text.otherRolesMenu)}</h6>`
            + Object.keys(roleLabels).filter(role => !held.includes(role) && !HIDDEN_OTHER_ROLES.includes(role)).map(role =>
                `<button type="button" class="dropdown-item" data-act="add-other-role" data-role="${esc(role)}">${esc(roleLabels[role])}`
                + (roleDescriptions[role] ? `<small>${esc(roleDescriptions[role])}</small>` : '')
                + (CITED_OTHER_ROLES.includes(role) ? `<small class="party-cited-note">${esc(text.otherRolesCitedTitle)}</small>` : '')
                + '</button>').join('');
    }

    // ---------- events ----------

    function onDropdownShow(event) {
        const toggle = event.target;
        const menu = toggle.parentElement.querySelector('.dropdown-menu');
        const card = toggle.closest('[data-party]');
        if (toggle.dataset.act === 'copy-menu') {
            buildCopyMenu(menu, card);
        } else if (toggle.dataset.act === 'other-roles-menu') {
            buildOtherRolesMenu(menu, card);
        }
    }

    function onClick(event) {
        const target = event.target.closest('[data-act]');
        if (!target || !root.contains(target)) {
            return;
        }
        const act = target.dataset.act;
        const card = target.closest('[data-party]');

        if (act === 'toggle') {
            // clicks on the buttons, menus and grip in the header don't fold the card
            if (event.target.closest('button, a, .dropdown-menu, .party-grip')) {
                return;
            }
            setOpen(card, !card.classList.contains('open'));
            refreshAll();
            return;
        }

        switch (act) {
            case 'toggle-all': {
                const shown = visibleCards();
                const open = !shown.some(c => c.classList.contains('open'));
                shown.forEach(c => setOpen(c, open));
                refreshAll();
                break;
            }
            case 'filter':
                filter = target.dataset.filter;
                refreshAll();
                break;
            case 'role': {
                const name = target.dataset.flag;
                const on = !flag(card, name);
                setFlag(card, name, on);
                refreshAll();
                changed();
                // the card may have left the filtered view
                const again = card.querySelector(`[data-act="role"][data-flag="${name}"]`);
                if (!card.hidden) {
                    again.focus();
                }
                break;
            }
            case 'add-other-role': {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.dataset.name = 'associatedPartyRoles';
                input.value = target.dataset.role;
                card.querySelector('[data-other-roles]').appendChild(input);
                reindex();
                refreshAll();
                changed();
                // the new chip takes the focus once the menu has closed and handed it back to its button
                setTimeout(() => card.querySelector(`[data-act="rm-other-role"][data-role="${CSS.escape(input.value)}"]`)?.focus());
                break;
            }
            case 'rm-other-role': {
                const role = target.dataset.role;
                const input = Array.from(card.querySelectorAll('[data-other-roles] input')).find(i => i.value === role);
                if (input) {
                    input.remove();
                }
                refreshAll();
                changed();
                card.querySelector('[data-act="other-roles-menu"]').focus();
                break;
            }
            case 'up':
            case 'down':
                move(card, act === 'up' ? -1 : 1);
                if (!target.disabled) {
                    target.focus();
                } else {
                    card.querySelector(`[data-act="${act === 'up' ? 'down' : 'up'}"]`).focus();
                }
                break;
            case 'remove': {
                const name = displayName(card);
                $(card).find('select.party-country').select2('destroy');
                card.remove();
                reindex();
                refreshAll();
                changed();
                toast(`${text.removed}: ${name || text.new}`);
                break;
            }
            case 'add': {
                const created = newCard();
                if (filter !== 'all') {
                    setFlag(created, filter, true);
                }
                setOpen(created, true);
                reindex();
                refreshAll();
                initSortable();
                changed();
                created.querySelector('[data-name="agent.firstName"]').focus();
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
                refreshCard(card);
                changed();
                const next = group.querySelector('[data-rep-rows] input:not([type=hidden])');
                (next || group.querySelector('[data-act="add-rep"]')).focus();
                break;
            }
            case 'copy-from': {
                const source = cards()[Number(target.dataset.src)];
                copyInto(card, agentOf(source), otherRoles(source));
                break;
            }
            case 'copy-another':
                picker.open(card);
                break;
        }
    }

    function onKeydown(event) {
        if ((event.key === 'Enter' || event.key === ' ') && event.target.matches('.party-summary')) {
            event.preventDefault();
            const card = event.target.closest('[data-party]');
            setOpen(card, !card.classList.contains('open'));
            refreshAll();
        }
    }

    function onInput(event) {
        const card = event.target.closest('[data-party]');
        if (!card) {
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
        // a posted error no longer applies once its field is edited; the name rule marks three fields at once
        if (event.target.matches('[data-name-field], [data-name="agent.position[]"]') && hasName(card)) {
            card.querySelectorAll('[data-name-field], [data-name="agent.position[]"]').forEach(el => el.classList.remove('is-invalid'));
        } else {
            event.target.classList.remove('is-invalid');
        }
        if (!card.querySelector('.is-invalid')) {
            card.classList.remove('has-errors');
            card.querySelectorAll('.field-error').forEach(el => el.remove());
        }
        refreshAll();
    }

    // empty values would be posted as invalid emails, positions and so on; leave them out
    function onSubmit() {
        list.querySelectorAll('[data-rep-row]').forEach(row => {
            const input = row.querySelector('[data-id-full]') || row.querySelector('input');
            if (!input.value.trim()) {
                row.remove();
            }
        });
        list.querySelectorAll('[data-id-row]').forEach(row => P.updateIdentifier(row, text));
        reindex();
    }

    // ---------- copy from another resource ----------

    function initPicker() {
        picker = P.picker({
            url: root.dataset.peopleUrl,
            text: text,
            usageText: usageText,
            // the associated party roles held in any of the other resources
            onPick: (person, target) => copyInto(target, person.agent, (person.usedIn || []).flatMap(usage => usage.associatedPartyRoles || []))
        });
    }

    function usageText(usage) {
        const roles = FLAGS.filter(name => usage[name]).map(name => flagLabel(name).toLowerCase())
            .concat((usage.associatedPartyRoles || []).map(role => roleLabel(role).toLowerCase()));
        if (usage.projectPersonnel) {
            roles.push(text.pickerPersonnel);
        }
        return usage.resource + (roles.length ? ` (${roles.join(', ')})` : '');
    }
})();
