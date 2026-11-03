
function setAgentRegularInput(inputName, params) {
    const inputNameInId = (inputName === "city" || inputName === "province" || inputName === "postalCode")
        ? "address." + inputName : inputName;

    $(`#${params.itemId} [id$='${inputName}']`)
        .attr("id", `${agentInputPath(params)}.${inputNameInId}`)
        .attr("name", function () {
            return $(this).attr("id");
        });
    $(`#${params.itemId} [for$='${inputName}']`)
        .attr("for", `${agentInputPath(params)}.${inputNameInId}`);
}

function setAgentRepeatableInput(inputName, params) {
    params.ext = {
        inputNamePlural: (inputName === "address") ? inputName : inputName + 's',
        inputNameInId: (inputName === "address") ? "address.address" : inputName
    }

    setAgentRepeatableInputMainId(inputName, params);
    setAgentRepeatableInputAddNewLink(inputName, params);
    setAgentRepeatableInputItems(inputName, params)
}

function setAgentRepeatableInputMainId(inputName, params) {
    $(`#${params.itemId} .${params.entity.name}-${params.ext.inputNamePlural}`)
        .attr("id", `${params.entity.name}-${params.index}-${params.ext.inputNamePlural}`);
}

function setAgentRepeatableInputAddNewLink(inputName, params) {
    $(`#${params.itemId} [id^='plus-${params.entity.name}-${inputName}']`)
        .attr("id", `plus-${params.entity.name}-${inputName}-${params.index}`);
}

function setAgentRepeatableInputItems(inputName, params) {
    $(`#${params.itemId} .${params.entity.name}-${params.ext.inputNamePlural} .${inputName}-item`)
        .each(function (inputIndex) {
            setAgentRepeatableInputItem($(this), inputName, inputIndex, params);
        });
}

function setAgentRepeatableInputItem($item, inputName, inputIndex, params) {
    const repeatableItemId = `${params.entity.name}-${params.index}-${inputName}-${inputIndex}`;

    // item's id
    $item.attr("id", repeatableItemId);

    // input's id and name
    $(`#${repeatableItemId} input`)
        .attr("id", `${agentInputPath(params)}.${params.ext.inputNameInId}[${inputIndex}]`)
        .attr("name", function () {return $(this).attr("id");});

    // remove link
    $(`#${repeatableItemId} .removeSubEntity`)
        .attr("id", `${params.entity.name}-${inputName}-remove-${params.index}-${inputIndex}`);
}

function setAgentRegularDropdown(dropdownName, params) {
    const dropdownNameInId = (dropdownName === "country") ? "address.country" : dropdownName;
    let $dropdown = $(`#${params.itemId} [id$='${dropdownName}']`);

    $dropdown
        .attr("id", `${agentInputPath(params)}.${dropdownNameInId}`)
        .attr("name", function () {return $(this).attr("id");});
    $(`#${params.itemId} [for$='${dropdownName}']`)
        .attr("for", `${agentInputPath(params)}.${dropdownNameInId}`);

}

function setAgentIdentifier(params) {
    // identifiers block id
    $(`#${params.itemId} .${params.entity.name}-identifiers`)
        .attr("id", `${params.entity.name}-${params.index}-identifiers`);

    // identifier add new link
    $(`#${params.itemId} [id^='plus-${params.entity.name}-identifier']`)
        .attr("id", `plus-${params.entity.name}-identifier-${params.index}`);

    // identifier item
    $(`#${params.itemId} .${params.entity.name}-identifiers .identifier-item`)
        .each(function (identifierIndex) {
            // identifier item id
            $(this).attr("id", `${params.entity.name}-${params.index}-identifier-${identifierIndex}`);

            // identifier directory dropdown element id/name
            $(this)
                .find("select")
                .attr("id", `${agentInputPath(params)}.userIds[${identifierIndex}].directory`)
                .attr("name", function () {
                    return $(this).attr("id");
                });

            // identifier input element id/name
            $(this)
                .find("input")
                .attr("id", `${agentInputPath(params)}.userIds[${identifierIndex}].identifier`)
                .attr("name", function () {
                    return $(this).attr("id");
                });

            // identifier remove link
            $(`#${params.entity.name}-${params.index}-identifier-${identifierIndex} .removeIdentifier`)
                .attr("id", `${params.entity.name}-identifier-remove-${params.index}-${identifierIndex}`);
        });
}

/**
 * Form path of the agent of a party item (people and organisations on the contacts page): eml.parties[0].agent.
 */
function agentInputPath(params) {
    return `eml.parties[${params.index}].agent`;
}

/**
 * Sets the ids and names of a party's role inputs (creator, contact, metadataProvider checkboxes and the
 * associatedPartyRoles multi-select) for its index, and initialises the multi-select of a new party.
 */
function setPartyRoleInputs(params) {
    const $item = $(`#${params.itemId}`);

    $item.find("[data-party-field]").each(function () {
        const field = $(this).attr("data-party-field");
        $(this)
            .attr("id", `eml.parties[${params.index}].${field}`)
            .attr("name", `eml.parties[${params.index}].${field}`);
    });
    $item.find("[data-party-for]").each(function () {
        $(this).attr("for", `eml.parties[${params.index}].${$(this).attr("data-party-for")}`);
    });

    if (params.isNew) {
        $item.find(".party-associated-roles").select2({
            placeholder: `${params.translations.associatedPartyRoles}`,
            language: {
                noResults: function () {
                    return `${params.translations.notFound}`;
                }
            },
            width: "100%",
            theme: 'bootstrap4'
        });
    }
}
