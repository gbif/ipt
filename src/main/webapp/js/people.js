/*
 * Shared by the contacts and project pages (manage/eml/contacts.ftl, project.ftl): identifier rows that recognise the
 * personnel directory of what is pasted, and the "Copy from another resource" picker (manager-api/suggest-people,
 * markup in manage/eml/people_picker.ftl).
 *
 * An identifier row ([data-id-row]) holds the visible input [data-id-full], the posted identifier (hidden input) and
 * the posted directory (select). The texts come from the page: identifierRecognised, identifierOrcidFormat,
 * identifierUnknown, pickerLoading, pickerNone, pickerNoMatch, pickerFailed, pickerUsedIn.
 */
window.IptPeople = (function () {
    'use strict';

    // personnel directories, recognised from what is pasted; the stored directory is the matching option of the select
    const DIRECTORIES = [
        {name: 'ORCID', host: 'orcid.org', prefixes: ['orcid.org/']},
        {name: 'ROR', host: 'ror.org', prefixes: ['ror.org/']},
        {name: 'Wikidata', host: 'wikidata.org', prefixes: ['wikidata.org/entity/', 'wikidata.org/wiki/']},
        {name: 'ResearcherID', host: 'researcherid.com', prefixes: ['researcherid.com/rid/']},
        {name: 'Google Scholar', host: 'scholar.google.com', prefixes: ['scholar.google.com/citations?user=']},
        {name: 'LinkedIn', host: 'linkedin.com', prefixes: ['linkedin.com/profile/view?id=', 'linkedin.com/in/']}
    ];
    const ORCID = /^\d{4}-\d{4}-\d{4}-\d{3}[\dX]$/i;

    function esc(value) {
        return String(value == null ? '' : value).replace(/[&<>"']/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
    }

    function directoryOption(select, directory) {
        return Array.from(select.options).find(option => option.value && option.value.toLowerCase().includes(directory.host));
    }

    // recognises the personnel directory of a pasted identifier: a URL of a known directory, or a bare ORCID iD,
    // Wikidata item or ROR id
    function detect(value, select) {
        const v = (value || '').trim();
        if (!v) {
            return null;
        }
        const bare = v.replace(/^https?:\/\//i, '').replace(/^www\./i, '');
        let found = null;
        for (const directory of DIRECTORIES) {
            const prefix = directory.prefixes.find(p => bare.toLowerCase().startsWith(p));
            if (prefix) {
                found = {directory: directory, identifier: bare.slice(prefix.length).replace(/\/$/, '')};
                break;
            }
        }
        if (!found) {
            if (ORCID.test(v)) {
                found = {directory: DIRECTORIES[0], identifier: v.toUpperCase()};
            } else if (/^Q\d+$/.test(v)) {
                found = {directory: DIRECTORIES[2], identifier: v};
            } else if (/^0[a-z0-9]{6}\d{2}$/i.test(v)) {
                found = {directory: DIRECTORIES[1], identifier: v.toLowerCase()};
            }
        }
        if (!found || !found.identifier) {
            return null;
        }
        const option = select && directoryOption(select, found.directory);
        return option ? Object.assign(found, {value: option.value}) : null;
    }

    // the directory recognised in an identifier row, or null
    function detectRow(row) {
        return detect(row.querySelector('[data-id-full]').value, row.querySelector('select'));
    }

    function showDetection(row, detected, full, text) {
        const out = row.querySelector('[data-id-detect]');
        const select = row.querySelector('select');
        const posted = !!row.querySelector('.field-error li');
        let message = '', state = '';
        if (detected) {
            const badOrcid = detected.directory.name === 'ORCID' && !ORCID.test(detected.identifier);
            message = badOrcid ? text.identifierOrcidFormat : text.identifierRecognised + ' ' + detected.directory.name;
            state = badOrcid ? 'warn' : 'ok';
        } else if (full && !select.value) {
            // once a directory is picked (or kept from the EML) there is nothing more to ask; something that starts
            // like an ORCID iD gets the ORCID hint instead
            message = /^\d{4}-\d{4}-/.test(full) ? text.identifierOrcidFormat : text.identifierUnknown;
            state = 'warn';
        }
        out.textContent = message;
        out.className = 'party-id-detect' + (state ? ' ' + state : '');
        // the directory is picked by hand only when it can't be recognised, or the posted one is wrong
        select.classList.toggle('d-none', !!detected || (!full && !posted && !select.value));
    }

    function initIdentifierRow(row, text) {
        const full = row.querySelector('[data-id-full]');
        const select = row.querySelector('select');
        const detected = detect(full.value, select);
        // a stored directory that matches the stored value counts as recognised, like one picked while typing
        if (detected && detected.value === select.value) {
            select.dataset.auto = 'true';
        }
        showDetection(row, detected, full.value.trim(), text);
        // form-validation.js marks the posted (hidden) identifier input; show it on the visible one
        row.querySelector('input[type=hidden]').classList.remove('is-invalid');
        if (row.querySelector('.field-error li')) {
            full.classList.add('is-invalid');
            select.classList.remove('d-none');
        }
    }

    // keeps the posted directory and identifier in step with the pasted identifier
    function updateIdentifier(row, text) {
        const full = row.querySelector('[data-id-full]').value.trim();
        const select = row.querySelector('select');
        const identifier = row.querySelector('input[type=hidden]');
        const detected = detect(full, select);
        if (detected) {
            select.value = detected.value;
            // picked here, not by the user: dropped again if the value stops being recognised
            select.dataset.auto = 'true';
            identifier.value = detected.identifier;
        } else if (!full) {
            select.value = '';
            identifier.value = '';
        } else {
            if (select.dataset.auto) {
                select.value = '';
                delete select.dataset.auto;
            }
            identifier.value = select.value && full.toLowerCase().startsWith(select.value.toLowerCase())
                ? full.slice(select.value.length) : full;
        }
        showDetection(row, detected, full, text);
    }

    // fills a new identifier row with a stored user id ({directory, identifier})
    function setIdentifier(row, userId, text) {
        const select = row.querySelector('select');
        const option = Array.from(select.options).find(o => o.value && o.value.replace(/^https?:\/\//, '') === (userId.directory || '').replace(/^https?:\/\//, ''));
        select.value = option ? option.value : '';
        row.querySelector('input[type=hidden]').value = userId.identifier;
        row.querySelector('[data-id-full]').value = /^https?:\/\//i.test(userId.identifier) || !select.value ? userId.identifier : select.value + userId.identifier;
        showDetection(row, detectRow(row), userId.identifier, text);
    }

    // the user ids of the identifier rows under an element
    function userIdsOf(el) {
        return Array.from(el.querySelectorAll('[data-id-row]'))
            .map(row => ({directory: row.querySelector('select').value, identifier: row.querySelector('input[type=hidden]').value}))
            .filter(userId => userId.identifier);
    }

    function nameOfAgent(agent) {
        const name = [agent.salutation, agent.firstName, agent.lastName].filter(Boolean).join(' ').trim();
        return name || agent.organisation || (agent.position || [])[0] || '';
    }

    /*
     * The "Copy from another resource" picker: lists the people of the user's other resources and hands the chosen
     * one to onPick(person, target). options: url, text, usageText(usage) (how a person is used in a resource), onPick.
     */
    function picker(options) {
        const text = options.text;
        const el = document.getElementById('party-picker');
        const modal = new bootstrap.Modal(el);
        const search = document.getElementById('party-picker-search');
        const results = document.getElementById('party-picker-results');
        const ok = document.getElementById('party-picker-ok');
        let people = null, selected = null, target = null;

        function render() {
            const query = search.value.trim().toLowerCase();
            if (!people) {
                return;
            }
            if (!people.length) {
                results.innerHTML = `<div class="party-picker-msg">${esc(text.pickerNone)}</div>`;
                ok.disabled = true;
                return;
            }
            const matches = people
                .map((person, index) => ({person: person, index: index}))
                .filter(({person}) => {
                    const agent = person.agent || {};
                    const haystack = [nameOfAgent(agent), agent.organisation].concat(agent.email || [], (agent.userIds || []).map(u => u.identifier))
                        .join(' ').toLowerCase();
                    return !query || haystack.includes(query);
                });
            if (selected != null && !matches.some(match => match.index === selected)) {
                selected = null;
            }
            ok.disabled = selected == null;
            results.innerHTML = matches.length
                ? matches.map(({person, index}) => {
                    const agent = person.agent || {};
                    const details = [agent.organisation && [agent.firstName, agent.lastName].some(Boolean) ? agent.organisation : '',
                        (agent.position || [])[0], (agent.email || [])[0]].filter(Boolean).join(' \u00B7 ');
                    const isSelected = selected === index;
                    return `<div class="party-picker-result${isSelected ? ' sel' : ''}" data-i="${index}" tabindex="0" role="option" aria-selected="${isSelected}">`
                        + `<span class="party-who"><span class="party-name">${esc(nameOfAgent(agent))}</span>`
                        + `<span class="party-sub">${details ? esc(details) + '<br>' : ''}<i>${esc(text.pickerUsedIn)} ${esc((person.usedIn || []).map(options.usageText).join('; '))}</i></span></span>`
                        + '</div>';
                }).join('')
                : `<div class="party-picker-msg">${esc(text.pickerNoMatch)}</div>`;
        }

        el.addEventListener('shown.bs.modal', () => search.focus());
        search.addEventListener('input', render);
        results.addEventListener('click', event => {
            const result = event.target.closest('[data-i]');
            if (result) {
                selected = Number(result.dataset.i);
                render();
            }
        });
        results.addEventListener('dblclick', event => {
            if (event.target.closest('[data-i]')) {
                ok.click();
            }
        });
        results.addEventListener('keydown', event => {
            const result = event.target.closest('[data-i]');
            if (result && (event.key === 'Enter' || event.key === ' ')) {
                event.preventDefault();
                selected = Number(result.dataset.i);
                ok.click();
            }
        });
        ok.addEventListener('click', () => {
            if (selected == null || !people) {
                return;
            }
            const person = people[selected];
            const into = target && target.isConnected ? target : null;
            modal.hide();
            options.onPick(person, into);
        });

        return {
            // target: the card to copy into, or null for a new one
            open(into) {
                target = into;
                selected = null;
                search.value = '';
                modal.show();
                if (people) {
                    render();
                    return;
                }
                results.innerHTML = `<div class="party-picker-msg">${esc(text.pickerLoading)}</div>`;
                fetch(options.url, {credentials: 'same-origin', headers: {Accept: 'application/json'}})
                    .then(response => {
                        if (!response.ok) {
                            throw new Error(response.status);
                        }
                        return response.json();
                    })
                    .then(list => {
                        people = Array.isArray(list) ? list : [];
                        render();
                    })
                    .catch(() => {
                        results.innerHTML = `<div class="party-picker-msg">${esc(text.pickerFailed)}</div>`;
                    });
            }
        };
    }

    return {
        esc: esc,
        detect: detect,
        detectRow: detectRow,
        showDetection: showDetection,
        initIdentifierRow: initIdentifierRow,
        updateIdentifier: updateIdentifier,
        setIdentifier: setIdentifier,
        userIdsOf: userIdsOf,
        nameOfAgent: nameOfAgent,
        picker: picker
    };
})();
