
function setAgentRegularInput(inputName, params) {
    const inputNameInId = (inputName === "city" || inputName === "province" || inputName === "postalCode")
        ? "address." + inputName : inputName;

    $(`#${params.itemId} [id$='${inputName}']`)
        .attr("id", `eml.${params.entity.pluralName}[${params.index}].${inputNameInId}`)
        .attr("name", function () {
            return $(this).attr("id");
        });
    $(`#${params.itemId} [for$='${inputName}']`)
        .attr("for", `eml.${params.entity.pluralName}[${params.index}].${inputNameInId}`);
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
        .attr("id", `eml.${params.entity.pluralName}[${params.index}].${params.ext.inputNameInId}[${inputIndex}]`)
        .attr("name", function () {return $(this).attr("id");});

    // remove link
    $(`#${repeatableItemId} .removeSubEntity`)
        .attr("id", `${params.entity.name}-${inputName}-remove-${params.index}-${inputIndex}`);
}

function setAgentRegularDropdown(dropdownName, params) {
    const dropdownNameInId = (dropdownName === "country") ? "address.country" : dropdownName;
    let $dropdown = $(`#${params.itemId} [id$='${dropdownName}']`);

    $dropdown
        .attr("id", `eml.${params.entity.pluralName}[${params.index}].${dropdownNameInId}`)
        .attr("name", function () {return $(this).attr("id");});
    $(`#${params.itemId} [for$='${dropdownName}']`)
        .attr("for", `eml.${params.entity.pluralName}[${params.index}.${dropdownNameInId}`);

    if (params.isNew) {
        if (dropdownName === "role") {
            initializeDropdownComponentForRole(dropdownName, params);
        } else if (dropdownName === "country") {
            initializeDropdownComponentForCountry(dropdownName, params)
        }
    }
}

function initializeDropdownComponentForRole(dropdownName, params) {
    let $dropdown = $(`#${params.itemId} [id$='${dropdownName}']`);

    $dropdown
        .select2({
            placeholder: `${params.translations.role}`,
            language: {
                noResults: function () {
                    return `${params.translations.notFound}`;
                }
            },
            width: "100%",
            allowClear: true,
            theme: 'bootstrap4'
        });
}

function initializeDropdownComponentForCountry(dropdownName, params) {
    let $dropdown = $(`#${params.itemId} [id$='${dropdownName}']`);

    $dropdown
        .select2({
            placeholder: `${params.translations.country}`,
            language: {
                noResults: function () {
                    return `${params.translations.notFound}`;
                }
            },
            width: "100%",
            allowClear: true,
            theme: 'bootstrap4'
        });
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
                .attr("id", `eml.${params.entity.pluralName}[${params.index}].userIds[${identifierIndex}].directory`)
                .attr("name", function () {
                    return $(this).attr("id");
                });

            // select2 initialization
            if (params.isNew) {
                $(this)
                    .find("select")
                    .select2({
                        placeholder: `${params.translations.directory}`,
                        language: {
                            noResults: function () {
                                return `${params.translations.notFound}`;
                            }
                        },
                        width: "100%",
                        minimumResultsForSearch: 'Infinity',
                        allowClear: true,
                        theme: 'bootstrap4'
                    });
            }

            // identifier input element id/name
            $(this)
                .find("input")
                .attr("id", `eml.${params.entity.pluralName}[${params.index}].userIds[${identifierIndex}].identifier`)
                .attr("name", function () {
                    return $(this).attr("id");
                });

            // identifier remove link
            $(`#${params.entity.name}-${params.index}-identifier-${identifierIndex} .removeIdentifier`)
                .attr("id", `${params.entity.name}-identifier-remove-${params.index}-${identifierIndex}`);
        });
}
