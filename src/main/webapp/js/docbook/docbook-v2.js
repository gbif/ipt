// Function to convert HTML to EML DocBook
function convertToDocBook(html) {
    const parser = new DOMParser();
    const doc = parser.parseFromString(`<body>${html}</body>`, "text/html");
    const body = doc.body;
    const serializer = new XMLSerializer();

    const TAG_MAP = {
        b: "emphasis",
        strong: "emphasis",
        i: "emphasis",
        em: "emphasis",
        p: "para",
        ul: "itemizedlist",
        ol: "orderedlist",
        li: "listitem",
        sub: "subscript",
        sup: "superscript",
        pre: "literalLayout",
        code: "literalLayout",
        literal: "literalLayout",
        a: "ulink",
        section: "section",
        div: "section",
        title: "title"
    };

    function isInline(tag) {
        return ["b", "strong", "i", "em", "span", "a", "sub", "sup", "code", "literal"].includes(tag);
    }

    function convertNode(node, xmlDoc) {
        if (node.nodeType === Node.TEXT_NODE) {
            let text = node.textContent;

            // Do NOT collapse whitespace blindly inside block-level contexts
            const parent = node.parentElement;
            const parentTag = parent ? parent.tagName.toLowerCase() : "";

            const isLiteral = ["pre", "code", "literal"].includes(parentTag);
            const isBlock = ["p", "div", "section", "li", "ol", "ul", "body"].includes(parentTag);

            if (isLiteral) {
                // preserve exactly
                return xmlDoc.createTextNode(text);
            }

            if (isBlock) {
                // collapse MULTIPLE spaces but keep leading/trailing
                text = text.replace(/\s{2,}/g, " ");
                return text ? xmlDoc.createTextNode(text) : null;
            }

            // inline context (default)
            // collapse but ADD a leading/trailing space if needed
            text = text.replace(/\s+/g, " ");

            return text ? xmlDoc.createTextNode(text) : null;
        }

        if (node.nodeType !== Node.ELEMENT_NODE) return null;

        const tag = node.tagName.toLowerCase();

        // Handle pre/code/literal
        if (tag === "pre" || tag === "code" || tag === "literal") {
            const para = xmlDoc.createElement("para");
            const literal = xmlDoc.createElement("literalLayout");
            literal.textContent = node.textContent.replace(/^\n+|\n+$/g, "");
            para.appendChild(literal);
            return para;
        }

        // Handle <li>
        if (tag === "li") {
            const listItem = xmlDoc.createElement("listitem");
            let para = xmlDoc.createElement("para");
            listItem.appendChild(para);

            node.childNodes.forEach((c) => {
                const converted = convertNode(c, xmlDoc);
                if (!converted) return;

                if (
                    converted.nodeType === 1 &&
                    ["para", "itemizedlist", "orderedlist"].includes(converted.tagName)
                ) {
                    if (para.childNodes.length > 0) {
                        para = xmlDoc.createElement("para");
                        listItem.appendChild(para);
                    }
                    listItem.appendChild(converted);
                } else {
                    para.appendChild(converted);
                }
            });
            if (!para.hasChildNodes()) listItem.removeChild(para);
            return listItem;
        }

        // Handle <div>
        if (tag === "section" || tag === "div") {
            const section = xmlDoc.createElement("section");

            let currentPara = null;
            let hasTitle = false;

            node.childNodes.forEach((c) => {
                if (c.nodeType === Node.ELEMENT_NODE && c.tagName.toLowerCase() === "h1" && !hasTitle) {
                    const title = xmlDoc.createElement("title");
                    title.textContent = c.textContent.trim();
                    section.appendChild(title);
                    hasTitle = true;
                    return;
                }

                const converted = convertNode(c, xmlDoc);
                if (!converted) return;

                const allowed = ["para", "section", "title"];
                if (converted.nodeType === Node.TEXT_NODE || (converted.nodeType === 1 && !allowed.includes(converted.tagName))) {
                    if (!currentPara) {
                        currentPara = xmlDoc.createElement("para");
                        section.appendChild(currentPara);
                    }
                    currentPara.appendChild(converted);
                } else {
                    currentPara = null;
                    section.appendChild(converted);
                }
            });

            return section;
        }

        // Handle <title>
        if (tag === "title") {
            const title = xmlDoc.createElement("title");
            node.childNodes.forEach((c) => {
                const converted = convertNode(c, xmlDoc);
                if (converted) title.appendChild(converted);
            });
            return title;
        }

        // Handle <a> → <ulink>
        if (tag === "a") {
            const ulink = xmlDoc.createElement("ulink");
            const href = node.getAttribute("href") || "";
            ulink.setAttribute("url", href);
            const citetitle = xmlDoc.createElement("citetitle");
            node.childNodes.forEach((c) => {
                const converted = convertNode(c, xmlDoc);
                if (converted) citetitle.appendChild(converted);
            });
            ulink.appendChild(citetitle);
            return ulink;
        }

        const mapped = TAG_MAP[tag] || (isInline(tag) ? "emphasis" : "para");
        const el = xmlDoc.createElement(mapped);

        node.childNodes.forEach((c) => {
            const converted = convertNode(c, xmlDoc);
            if (converted) {
                // prevent para nesting
                if (
                    converted.nodeType === 1 &&
                    converted.tagName === "para" &&
                    el.tagName === "para"
                ) {
                    converted.childNodes.forEach((child) =>
                        el.appendChild(child.cloneNode(true))
                    );
                } else {
                    el.appendChild(converted);
                }
            }
        });

        if (el.tagName === "para" && !el.hasChildNodes()) return null;
        return el;
    }

    const xmlDoc = document.implementation.createDocument(null, null, null);
    let result = "";

    body.childNodes.forEach((node) => {
        const converted = convertNode(node, xmlDoc);
        if (!converted) return;
        if (["itemizedlist", "orderedlist"].includes(converted.tagName)) {
            const para = xmlDoc.createElement("para");
            para.appendChild(converted);
            result += serializer.serializeToString(para);
        } else {
            result += serializer.serializeToString(converted);
        }
    });

    return result;
}


// Function to convert EML DocBook to HTML
function convertToHtml(docBook) {
    // If DocBook looks HTML-escaped, decode minimal common entities (optional)
    if (typeof docBook !== "string") return "";
    if (docBook.includes("&lt;")) {
        // minimal safe decode for common entities (don't decode arbitrary input)
        docBook = docBook.replace(/&lt;/g, "<").replace(/&gt;/g, ">")
            .replace(/&amp;/g, "&").replace(/&quot;/g, '"').replace(/&apos;/g, "'");
    }

    // Wrap fragment in a single root so XML parser accepts it
    const parser = new DOMParser();
    const xml = parser.parseFromString(`<root>${docBook}</root>`, "application/xml");

    // Check for parser errors
    if (xml.getElementsByTagName("parsererror").length > 0) {
        // Fallback: return original string (or you could return empty string or throw)
        console.warn("convertToHtml: XML parse error — returning original input.");
        return docBook;
    }

    const serializer = new XMLSerializer();

    // Lowercase keys
    const TAG_MAP = {
        para: "p",
        emphasis: "em",
        subscript: "sub",
        superscript: "sup",
        literallayout: "pre",   // lowercase
        itemizedlist: "ul",
        orderedlist: "ol",
        listitem: "li",
        section: "div",
        title: "h1",
        ulink: "a",
        citetitle: null // handled inside <a>
    };

    function convertNode(node, htmlDoc) {
        if (node.nodeType === Node.TEXT_NODE) {
            let text = node.textContent;

            // Collapse whitespace sequences but do NOT trim
            text = text.replace(/\s+/g, " ");

            return text ? htmlDoc.createTextNode(text) : null;
        }

        if (node.nodeType !== Node.ELEMENT_NODE) return null;

        const tag = (node.tagName || node.nodeName || "").toLowerCase();
        const mapped = TAG_MAP[tag] || "span";

        // Handle <ulink>
        if (tag === "ulink") {
            const a = htmlDoc.createElement("a");
            const href = node.getAttribute("url");
            if (href) a.setAttribute("href", href);

            // Prefer <citetitle> content if present, otherwise fallback to children
            const citetitles = Array.from(node.childNodes).filter(c => c.nodeType === Node.ELEMENT_NODE && (c.tagName || c.nodeName).toLowerCase() === "citetitle");
            if (citetitles.length) {
                citetitles.forEach(ct => {
                    Array.from(ct.childNodes).forEach(inner => {
                        const conv = convertNode(inner, htmlDoc);
                        if (conv) a.appendChild(conv);
                    });
                });
            } else {
                Array.from(node.childNodes).forEach(c => {
                    const conv = convertNode(c, htmlDoc);
                    if (conv) a.appendChild(conv);
                });
            }
            return a;
        }

        // Handle <section> -> <div>
        if (tag === "section") {
            const div = htmlDoc.createElement("div");
            Array.from(node.childNodes).forEach(c => {
                const converted = convertNode(c, htmlDoc);
                if (converted) div.appendChild(converted);
            });
            return div;
        }

        // Handle literalLayout -> <pre>
        if (tag === "literallayout") {
            const pre = htmlDoc.createElement("pre");
            // Preserve formatting inside literalLayout; use textContent
            pre.textContent = node.textContent;
            return pre;
        }

        // Regular mapping
        const el = htmlDoc.createElement(mapped);
        Array.from(node.childNodes).forEach(c => {
            const converted = convertNode(c, htmlDoc);
            if (converted) el.appendChild(converted);
        });
        return el;
    }

    const htmlDoc = document.implementation.createHTMLDocument("");
    let result = "";

    Array.from(xml.documentElement.childNodes).forEach(node => {
        const converted = convertNode(node, htmlDoc);
        if (converted) result += serializer.serializeToString(converted);
    });

    // Replace xmlns namespace
    return result.replace(/\s+xmlns="[^"]*"/g, "");
}


// Function to validate HTML (it must contain only allowed tags)
function validateHTML(html) {
    // Define allowed tags
    // br - is not allowed but will be cleaned in the beginning
    const allowedTags = [
        'div',
        'h1', 'h2', 'h3', 'h4', 'h5',
        'ul', 'ol', 'li',
        'p', 'em', 'b', 'sub', 'sup', 'pre', 'a',
        'br'
    ];

    // Match all HTML tags in the string
    const regex = /<\/?([a-z][a-z0-9]*)\b[^>]*>/gi;
    let match;

    // Loop through all found tags
    while ((match = regex.exec(html)) !== null) {
        // Extract the tag name from the match
        const tagName = match[1].toLowerCase();

        // Check if the tag is not in the allowed list
        if (!allowedTags.includes(tagName)) {
            return { isValid: false, tag: match[0] }; // Forbidden tag found
        }
    }

    return { isValid: true }; // No forbidden tags found
}
