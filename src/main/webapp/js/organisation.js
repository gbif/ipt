/*
 * Organization page (admin/organisation.ftl): the organization picker with the shared token request link, the DOI
 * account switch, copy buttons, the Remove confirmation and the resources "Show all".
 */
document.addEventListener("DOMContentLoaded", () => {
    const $id = (id) => document.getElementById(id);

    // --- saving checks the token with GBIF (and the DOI account with DataCite): show progress
    ["organisationsForm", "og-token"].forEach((id) => {
        const form = $id(id);
        if (form && typeof displayProcessing === "function") form.addEventListener("submit", displayProcessing);
    });

    // --- copy buttons
    document.addEventListener("click", (e) => {
        const b = e.target.closest(".st-copy");
        if (!b || !navigator.clipboard) return;
        navigator.clipboard.writeText(b.dataset.copy).then(() => {
            b.classList.add("st-copied");
            setTimeout(() => b.classList.remove("st-copied"), 1500);
        }, () => { /* nothing copied */ });
    });

    // --- Remove posts (data-post) once confirmed
    const remove = document.querySelector(".og-remove");
    if (remove) {
        $(remove).jConfirmAction({
            titleQuestion: remove.dataset.title,
            question: remove.dataset.text,
            yesAnswer: remove.dataset.yes,
            cancelAnswer: remove.dataset.cancel,
            buttonType: "danger"
        });
    }

    // --- resources: show the rest
    const showAll = $id("us-show-all");
    if (showAll) {
        showAll.addEventListener("click", () => {
            document.querySelectorAll(".us-res tr.us-more").forEach((tr) => { tr.hidden = false; });
            showAll.remove();
        });
    }

    // --- DOI switches: a fieldset is sent only while its switch is on (off clears it on save)
    document.querySelectorAll("[data-toggles]").forEach((sw) => {
        const target = $id(sw.dataset.toggles);
        sw.addEventListener("change", () => {
            target.hidden = !sw.checked;
            target.disabled = !sw.checked;
        });
    });

    // --- organization picker (adding only)
    const org = $id("organisation.key");
    if (!org || org.tagName !== "SELECT") return;
    const set = (id, value) => { $id(id).value = value || ""; };
    const request = $id("requestDetails");
    const alias = $id("organisation.alias");
    const fields = ["primaryContactType", "primaryContactName", "primaryContactEmail", "nodeKey", "nodeName", "homepageURL", "description"];
    alias.addEventListener("input", () => { alias.dataset.auto = "false"; });

    $(org).select2({
        placeholder: org.dataset.placeholder,
        allowClear: true,
        width: "100%",
        theme: "bootstrap4"
    }).on("change", () => {
        const key = org.value;
        const name = key ? org.options[org.selectedIndex].text : "";
        set("organisation.name", name);
        if (alias.value.trim() === "" || alias.dataset.auto === "true") {
            alias.value = name;
            alias.dataset.auto = "true";
        }
        fields.forEach((f) => set("organisation." + f, ""));
        request.hidden = true;
        request.replaceChildren();
        if (!key) return;

        // the organization's details, and its contact to ask for the shared token
        $.getJSON(org.dataset.registry + "organisation/" + key + ".json", (data) => {
            if (org.value !== key) return;
            fields.forEach((f) => set("organisation." + f, Array.isArray(data[f]) ? data[f][0] : data[f]));
            if (data.primaryContactEmail) {
                const subject = "Shared token request for " + name;
                const body = "Dear " + (data.primaryContactName || "colleague") + ",\n\n"
                    + "I administer an Integrated Publishing Toolkit (IPT) and would like to publish datasets under " + name + " from it. "
                    + "For this I need the organization's shared token. Could you please send it to me?\n\n"
                    + "Thank you.";
                const a = document.createElement("a");
                a.href = "mailto:" + data.primaryContactEmail + "?subject=" + encodeURIComponent(subject) + "&body=" + encodeURIComponent(body);
                a.textContent = org.dataset.textRequest.replace("{0}", data.primaryContactName || data.primaryContactEmail);
                request.replaceChildren(a);
                request.hidden = false;
            }
        });
    });
    // shown again after a refused save: fetch the contact again for the request link
    if (org.value) $(org).trigger("change");
});
