/**
 *  Copyright (C) 2019 Christian M. Zmasek
 *  Copyright (C) 2019 J. Craig Venter Institute
 *  All rights reserved
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 *
 *  Created by czmasek on 7/7/2016.
 */

// v 1.1.7
// 2019-05-16
//
// phyloxml.js is a JavaScript program for reading (SAX style parser)
// and writing phylogenetic trees in phyloXML format.
//
// phyloXML website: http://www.phyloxml.org/
//
// Availability:
//   https://github.com/cmzmasek/phyloxml-js
//   https://www.npmjs.com/package/phyloxml
//
// Dependencies:
//  sax.js (1.2.4): https://www.npmjs.com/package/sax/v/1.2.4
//
//
// Example:
// This basic example shows how to parse a phyloXML formatted String into to a
// object representing a phylogenetic tree. Followed by printing some elements
// and then converting the object back to a phyloXML formatted String.
//
// Change './phyloxml' to 'phyloxml' if you use this code outside of this package
//
// var phyloXml = require('./phyloxml').phyloXml;
//
// phlyoXmlFormattedString = '<phyloxml xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
//     'xmlns="http://www.phyloxml.org" ' +
//     'xsi:schemaLocation="http://www.phyloxml.org http://www.phyloxml.org/1.10/phyloxml.xsd">' +
//     '<phylogeny rooted="true" rerootable="false">' +
//     '<clade><branch_length>0.2</branch_length>' +
//     '<clade><branch_length>0.4</branch_length><name>A</name></clade>' +
//     '<clade><branch_length>0.6</branch_length><name>B</name></clade>' +
//     '</clade>' +
//     '</phylogeny>' +
//     '</phyloxml>';
//
// var phylogeneticTree = phyloXml.parse(phlyoXmlFormattedString, {trim: true, normalize: true})[0];
//
// console.log('Root branch length  : ' + phylogeneticTree.children[0].branch_length);
// console.log('Node A name         : ' + phylogeneticTree.children[0].children[0].name);
// console.log('Node A branch length: ' + phylogeneticTree.children[0].children[0].branch_length);
// console.log('Node B name         : ' + phylogeneticTree.children[0].children[1].name);
// console.log('Node B branch length: ' + phylogeneticTree.children[0].children[1].branch_length);
//
// console.log('Entire tree in phyloXML format:');
// console.log(phyloXml.toPhyloXML(phylogeneticTree, 4));
//
//
// Synchronous parsing of phyloXML-formatted String:
//
// var px = require('./phyloxml').phyloXml;
// var phys = px.parse(phyloxmlText, {trim: true, normalize: true});
// console.log(px.toPhyloXML(phys[0], 6));
//
//
// Asynchronous parsing of phyloXML-formatted Stream:
//
// var fs = require('fs');
// var px = require('./phyloxml').phyloXml;
// var stream = fs.createReadStream(xmlFile, {encoding: 'utf8'});
// px.parseAsync(stream, {trim: true, normalize: true});


(function phyloXml() {

    "use strict";

    var sax = null;
    if (typeof module !== 'undefined' && module.exports && !global.xmldocAssumeBrowser) {
        // Being used in a Node-like environment: the sax DEPENDENCY, not a
        // relative path. './sax' looked for a sax.js beside this file, which
        // never existed -- the published tarball has no sax in it, and the
        // repo's own vendored copy sat in lib/ (itself unpublished and
        // unreferenced, and since removed in favour of this dependency). So
        // `require('phyloxml')` threw "Cannot find module './sax'" for every
        // Node consumer, and the test suite in test/ could not run at all.
        sax = require('sax');
    }
    else if (typeof window !== "undefined") {
        // Attached to the Window object in a browser
        sax = window.sax;
        if (!sax) {
            throw new Error("Expected sax to be defined. Make sure you are including sax.js before this file.");
        }
    }
    else {
        sax = this.sax;
        if (!sax) {
            throw new Error("Expected sax to be defined. Make sure you are including sax.js before this file.");
        }
    }

    // --------------------------------------------------------------
    // phyloXML constants
    // --------------------------------------------------------------

    // Accession
    var ACCESSION = 'accession';
    var ACCESSION_SOURCE_ATTR = 'source';
    var ACCESSION_COMMENT_ATTR = 'comment';

    // Annotation
    var ANNOTATION = 'annotation';
    var ANNOTATION_REF_ATTR = 'ref';
    var ANNOTATION_SOURCE_ATTR = 'source';
    var ANNOTATION_EVIDENCE_ATTR = 'evidence';
    var ANNOTATION_TYPE_ATTR = 'type';
    var ANNOTATION_DESC = 'desc';

    // Clade
    var CLADE = 'clade';
    var CLADE_BRANCH_LENGTH = 'branch_length';
    var CLADE_ID_SOURCE_ATTR = 'id_source';
    var CLADE_COLLAPSE_ATTR = 'collapse';
    var CLADE_NAME = 'name';
    var CLADE_WIDTH = 'width';

    // Clade Relation
    var CLADE_RELATION = 'clade_relation';
    var CLADE_RELATION_DISTANCE_ATTR = 'distance';
    var CLADE_RELATION_ID_REF_0_ATTR = 'id_ref_0';
    var CLADE_RELATION_ID_REF_1_ATTR = 'id_ref_1';
    var CLADE_RELATION_TYPE_ATTR = 'type';

    // Sequence Relation
    var SEQUENCE_RELATION = 'sequence_relation';
    var SEQUENCE_RELATION_DISTANCE_ATTR = 'distance';
    var SEQUENCE_RELATION_ID_REF_0_ATTR = 'id_ref_0';
    var SEQUENCE_RELATION_ID_REF_1_ATTR = 'id_ref_1';
    var SEQUENCE_RELATION_TYPE_ATTR = 'type';

    // Color
    var COLOR = 'color';
    var COLOR_RED = 'red';
    var COLOR_GREEN = 'green';
    var COLOR_BLUE = 'blue';
    var COLOR_ALPHA = 'alpha';

    // Confidence
    var CONFIDENCE = 'confidence';
    var CONFIDENCE_TYPE_ATTR = 'type';
    var CONFIDENCE_STDDEV_ATTR = 'stddev';
    var CONFIDENCES = 'confidences';

    // Cross References
    var CROSS_REFERENCES = 'cross_references';

    // Date
    var DATE = 'date';
    var DATE_UNIT_ATTR = 'unit';
    var DATE_DESC = 'desc';
    var DATE_VALUE = 'value';
    var DATE_MINIMUM = 'minimum';
    var DATE_MAXIMUM = 'maximum';

    // Distribution
    var DISTRIBUTION = 'distribution';
    var DISTRIBUTION_DESC = 'desc';

    // Domain Architecture
    var DOMAIN_ARCHITECTURE = 'domain_architecture';
    var DOMAIN_ARCHITECTURE_LENGTH_ATTR = 'length';

    // Events
    var EVENTS = 'events';
    var EVENTS_TYPE = 'type';
    var EVENTS_DUPLICATIONS = 'duplications';
    var EVENTS_SPECIATIONS = 'speciations';
    var EVENTS_LOSSES = 'losses';

    // Id
    var ID = 'id';
    var ID_PROVIDER_ATTR = 'provider';

    // Mol Seq
    var MOLSEQ = 'mol_seq';
    var MOLSEQ_IS_ALIGNED_ATTR = 'is_aligned';

    // Phylogeny
    var PHYLOGENY = 'phylogeny';

    // Phyloxml
    var PHYLOXML = 'phyloxml';

    // Point
    var POINT = 'point';
    var POINT_ALT_UNIT_ATTR = 'alt_unit';
    var POINT_GEODETIC_DATUM_ATTR = 'geodetic_datum';
    var POINT_LAT = 'lat';
    var POINT_LONG = 'long';
    var POINT_ALT = 'alt';

    // Property
    var PROPERTY = 'property';
    var PROPERTY_REF_ATTR = 'ref';
    var PROPERTY_ID_REF_ATTR = 'id_ref';
    var PROPERTY_UNIT_ATTR = 'unit';
    var PROPERTY_DATATYPE_ATTR = 'datatype';
    var PROPERTY_APPLIES_TO_ATTR = 'applies_to';
    var PROPERTIES = 'properties';

    // Protein Domain
    var PROTEINDOMAIN = 'domain';
    var PROTEINDOMAIN_FROM_ATTR = 'from';
    var PROTEINDOMAIN_TO_ATTR = 'to';
    var PROTEINDOMAIN_CONFIDENCE_ATTR = 'confidence';
    var PROTEINDOMAIN_ID_ATTR = 'id';

    // Reference
    var REFERENCE = 'reference';
    var REFERENCE_DOI_ATTR = 'doi';
    var REFERENCE_DESC = 'desc';

    // Sequence
    var SEQUENCE = 'sequence';
    var SEQUENCE_ID_SOURCE_ATTR = 'id_source';
    var SEQUENCE_ID_REF_ATTR = 'id_ref';
    var SEQUENCE_TYPE_ATTR = 'type';
    var SEQUENCE_SYMBOL = 'symbol';
    var SEQUENCE_NAME = 'name';
    var SEQUENCE_GENE_NAME = 'gene_name';
    var SEQUENCE_LOCATION = 'location';
    var SEQUENCES = 'sequences';

    // Taxonomy
    var TAXONOMY = 'taxonomy';
    var TAXONOMY_ID_SOURCE_ATTR = 'id_source';
    var TAXONOMY_CODE = 'code';
    var TAXONOMY_SCIENTIFIC_NAME = 'scientific_name';
    var TAXONOMY_AUTHORITY = 'authority';
    var TAXONOMY_COMMON_NAME = 'common_name';
    var TAXONOMY_SYNONYM = 'synonym';
    var TAXONOMY_RANK = 'rank';
    var TAXONOMIES = 'taxonomies';
    var TAXONOMY_SYNONYMS = 'synonyms';

    // Uri
    var URI = 'uri';
    var URI_TYPE_ATTR = 'type';
    var URI_DESC_ATTR = 'desc';

    // Phylogeny
    var PHYLOGENY_ROOTED_ATTR = 'rooted';
    var PHYLOGENY_REROOTABLE_ATTR = 'rerootable';
    var PHYLOGENY_BRANCH_LENGTH_UNIT_ATTR = 'branch_length_unit';
    var PHYLOGENY_TYPE_ATTR = 'type';
    var PHYLOGENY_NAME = 'name';
    var PHYLOGENY_DESCRIPTION = 'description';
    var PHYLOGENY_DATE = 'date';

    // Simple_Characteristics (to be deprecated!)
    var X_SIMPLE_CHARACTERISTICS = 'Simple_Characteristics';
    var X_SIMPLE_CHARACTERISTIC_COUNTRY = 'Country';
    var X_SIMPLE_CHARACTERISTIC_YEAR = 'Year';
    var X_SIMPLE_CHARACTERISTIC_HOST = 'Host';
    var X_SIMPLE_CHARACTERISTIC_HA = 'HA';
    var X_SIMPLE_CHARACTERISTIC_NA = 'NA';

    // appType (special for Virus BRC)
    // REMOVED 2026-09-10: var APPTYPE = 'flu_type'.
    // A hardcoded reader for <flu_type>, an IRD/BV-BRC extension element that
    // is not part of phyloXML. It set phylogeny.desc, which nothing in
    // Archaeopteryx.js ever read and which the writer below never wrote back,
    // so the value was inert in both directions. It also only worked where
    // those files happened to put the element, BEFORE the clade, which is a
    // position the phyloXML schema does not allow: at the position the schema
    // does require (last, under the ##other wildcard) the handler fired with
    // an empty object stack and killed the parse. Extension elements belong
    // in their own namespace and are ignored by this reader; anything worth
    // surfacing should come through <property>, which is phyloXML's own
    // extension mechanism and is already supported.

    // Unknown source, id, confidence type:
    var UNKNOWN = 'unknown';
    // the arrays the reader collects repeated elements into
    var URIS = 'uris';
    var ANNOTATIONS = 'annotations';
    var DISTRIBUTIONS = 'distributions';
    var REFERENCES = 'references';
    var POINTS = 'points';
    // the style properties whose value the desktop writes as a colour
    var STYLE_COLOR_REFS = {'style:font_color': true, 'style:node_color': true};
    // the node styles the desktop reads, in the order it writes them
    var NODE_STYLE_ORDER = ['style:font', 'style:font_size', 'style:font_style', 'style:font_color',
        'style:node_shape', 'style:node_size', 'style:node_color', 'style:node_fill_type'];

    // --------------------------------------------------------------
    // Instance variables
    // --------------------------------------------------------------
    var _phylogenies = null;
    var _phylogeny = null;
    var _cladeStack = null;
    var _tagStack = null;
    var _objectStack = null;

    // --------------------------------------------------------------
    // Others
    // --------------------------------------------------------------
    var PROPERTY_REF_RE = /[a-zA-Z0-9_]+:\S+/;
    var PROPERTY_UNIT_RE = /[a-zA-Z0-9_]+:\S+/;
    var PROPERTY_DATATYPE_RE = /xsd:\S+/;

    // --------------------------------------------------------------
    // Functions for object creation
    // --------------------------------------------------------------
    function newAccession(tag) {
        var parent = _tagStack.get(1);
        if (!(parent === SEQUENCE || parent === CROSS_REFERENCES)) {
            throw new PhyloXmlError("found accession outside of sequence or cross-references");
        }
        var acc = {};
        acc.value = null;
        acc.source = getAttribute(ACCESSION_SOURCE_ATTR, tag.attributes);
        acc.comment = getAttribute(ACCESSION_COMMENT_ATTR, tag.attributes);
        if (!acc.source) {
            acc.source = UNKNOWN;
        }
        if (parent === SEQUENCE) {
            getCurrentObject().accession = acc;
        }
        else {
            addToArrayInCurrentObjectUnnamed(acc);
        }
        _objectStack.push(acc);
    }

    function newAnnotation(tag) {
        var parent = _tagStack.get(1);
        if (parent != SEQUENCE) {
            throw new PhyloXmlError("found annotation outside of sequence");
        }
        var ann = {};
        ann.evidence = getAttribute(ANNOTATION_EVIDENCE_ATTR, tag.attributes);
        ann.ref = getAttribute(ANNOTATION_REF_ATTR, tag.attributes);
        ann.source = getAttribute(ANNOTATION_SOURCE_ATTR, tag.attributes);
        ann.type = getAttribute(ANNOTATION_TYPE_ATTR, tag.attributes);
        addToArrayInCurrentObject('annotations', ann);
        _objectStack.push(ann);
    }

    function newBranchColor() {
        var parent = _tagStack.get(1);
        if (parent != CLADE) {
            throw new PhyloXmlError("found branch color outside of clade");
        }
        var col = {};
        col.red = 0;
        col.green = 0;
        col.blue = 0;
        getCurrentObject().color = col;
        _objectStack.push(col);
    }

    function newClade(tag) {
        var newClade = {};
        newClade.branch_length = getAttributeAsFloat(CLADE_BRANCH_LENGTH, tag.attributes);
        newClade.collapse = getAttributeAsBoolean(CLADE_COLLAPSE_ATTR, tag.attributes);
        if (CLADE_ID_SOURCE_ATTR in tag.attributes) {
            newClade.id_source = tag.attributes[CLADE_ID_SOURCE_ATTR];
        }
        if (_phylogeny === null) {
            var phylogeny_data = _objectStack.pop();
            if (!_objectStack.isEmpty()) {
                throw new PhyloXmlError('severe phyloXML format error');
            }
            _phylogeny = phylogeny_data;
            _phylogeny.children = [newClade];
        }
        else {
            var currClade = getCurrentClade();
            if (currClade.children === undefined) {
                currClade.children = [newClade];
            }
            else {
                currClade.children.push(newClade);
            }
        }
        _cladeStack.push(newClade);
        _objectStack.push(newClade);
    }

    function newCladeRelation(tag) {
        var cr = {};
        cr.distance = getAttributeAsFloat(CLADE_RELATION_DISTANCE_ATTR, tag.attributes);
        cr.id_ref_0 = getAttribute(CLADE_RELATION_ID_REF_0_ATTR, tag.attributes);
        cr.id_ref_1 = getAttribute(CLADE_RELATION_ID_REF_1_ATTR, tag.attributes);
        cr.type = getAttribute(CLADE_RELATION_TYPE_ATTR, tag.attributes);
        addToArrayInCurrentObject('clade_relations', cr);
        _objectStack.push(cr);
    }

    function newSequenceRelation(tag) {
        var sr = {};
        sr.distance = getAttributeAsFloat(SEQUENCE_RELATION_DISTANCE_ATTR, tag.attributes);
        sr.id_ref_0 = getAttribute(SEQUENCE_RELATION_ID_REF_0_ATTR, tag.attributes);
        sr.id_ref_1 = getAttribute(SEQUENCE_RELATION_ID_REF_1_ATTR, tag.attributes);
        sr.type = getAttribute(SEQUENCE_RELATION_TYPE_ATTR, tag.attributes);
        addToArrayInCurrentObject('sequence_relations', sr);
        _objectStack.push(sr);
    }

    function newConfidence(tag) {
        var conf = {};
        conf.value = null;
        conf.type = getAttribute(CONFIDENCE_TYPE_ATTR, tag.attributes);
        conf.stddev = getAttributeAsFloat(CONFIDENCE_STDDEV_ATTR, tag.attributes);
        var parent = _tagStack.get(1);
        if (parent === CLADE || parent === PHYLOGENY) {
            addToArrayInCurrentObject(CONFIDENCES, conf);
        }
        else if (parent === ANNOTATION || parent === EVENTS || parent === CLADE_RELATION || parent === SEQUENCE_RELATION) {
            getCurrentObject().confidence = conf;
        }
        _objectStack.push(conf);
    }

    function newCrossReferences() {
        var parent = _tagStack.get(1);
        if (parent != SEQUENCE) {
            throw new PhyloXmlError("found cross-reference outside of sequence");
        }
        var xrefs = [];
        getCurrentObject().cross_references = xrefs;
        _objectStack.push(xrefs);
    }

    function newDate(tag) {
        var date = {};
        date.unit = getAttribute(DATE_UNIT_ATTR, tag.attributes);
        getCurrentObject().date = date;
        _objectStack.push(date);
    }

    function newDistribution(tag) {
        var dist = {};
        dist.desc = null;
        dist.unit = getAttribute(DATE_UNIT_ATTR, tag.attributes);
        addToArrayInCurrentObject('distributions', dist);
        _objectStack.push(dist);
    }

    function newDomainArchitecture(tag) {
        var da = {};
        da.domains = null;
        da.length = getAttributeAsInt(DOMAIN_ARCHITECTURE_LENGTH_ATTR, tag.attributes);
        getCurrentObject().domain_architecture = da;
        _objectStack.push(da);
    }

    function newEvents() {
        var events = {};
        getCurrentObject().events = events;
        _objectStack.push(events);
    }

    function newId(tag) {
        var i = {};
        i.value = null;
        i.provider = getAttribute(ID_PROVIDER_ATTR, tag.attributes);
        getCurrentObject().id = i;
        _objectStack.push(i);
    }

    function newMolecularSequence(tag) {
        var mol_seq = {};
        mol_seq.is_aligned = getAttributeAsBoolean(MOLSEQ_IS_ALIGNED_ATTR, tag.attributes);
        getCurrentObject().mol_seq = mol_seq;
        _objectStack.push(mol_seq);
    }

    function newPoint(tag) {
        var p = {};
        p.alt_unit = getAttribute(POINT_ALT_UNIT_ATTR, tag.attributes);
        p.geodetic_datum = getAttribute(POINT_GEODETIC_DATUM_ATTR, tag.attributes);
        var parent = _tagStack.get(1);
        if (parent === DISTRIBUTION) {
            addToArrayInCurrentObject('points', p);
        }
        _objectStack.push(p);
    }

    function newProperty(tag) {
        var prop = {};
        prop.ref = getAttribute(PROPERTY_REF_ATTR, tag.attributes);
        prop.unit = getAttribute(PROPERTY_UNIT_ATTR, tag.attributes);
        prop.datatype = getAttribute(PROPERTY_DATATYPE_ATTR, tag.attributes);
        prop.applies_to = getAttribute(PROPERTY_APPLIES_TO_ATTR, tag.attributes);
        prop.id_ref = getAttribute(PROPERTY_ID_REF_ATTR, tag.attributes);

        if (!prop.ref) {
            throw new PhyloXmlError('property ref is missing');
        }
        if (!prop.datatype) {
            throw new PhyloXmlError('property data-type is missing');
        }
        if (!prop.applies_to) {
            throw new PhyloXmlError('property applies-to is missing');
        }
        if (!PROPERTY_REF_RE.test(prop.ref)) {
            throw new PhyloXmlError('property ref is ill-formatted: ' + prop.ref);
        }
        if (!PROPERTY_DATATYPE_RE.test(prop.datatype)) {
            throw new PhyloXmlError('property data-type is ill-formatted: ' + prop.datatype);
        }
        if (prop.unit && !PROPERTY_UNIT_RE.test(prop.unit)) {
            throw new PhyloXmlError('property unit is ill-formatted: ' + prop.unit);
        }

        addToArrayInCurrentObject(PROPERTIES, prop);
        _objectStack.push(prop);
    }

    function newProteinDomain(tag) {
        var pd = {};
        pd.name = null;
        pd.from = getAttributeAsInt(PROTEINDOMAIN_FROM_ATTR, tag.attributes);
        pd.to = getAttributeAsInt(PROTEINDOMAIN_TO_ATTR, tag.attributes);
        pd.confidence = getAttributeAsFloat(PROTEINDOMAIN_CONFIDENCE_ATTR, tag.attributes);
        pd.id = getAttribute(PROTEINDOMAIN_ID_ATTR, tag.attributes);
        addToArrayInCurrentObject('domains', pd);
        _objectStack.push(pd);
    }

    function newReference(tag) {
        var reference = {};
        reference.doi = getAttribute(REFERENCE_DOI_ATTR, tag.attributes);
        addToArrayInCurrentObject('references', reference);
        _objectStack.push(reference);
    }

    function newSequence(tag) {
        var seq = {};
        seq.type = getAttribute(SEQUENCE_TYPE_ATTR, tag.attributes);
        seq.id_source = getAttribute(SEQUENCE_ID_SOURCE_ATTR, tag.attributes);
        seq.id_ref = getAttribute(SEQUENCE_ID_REF_ATTR, tag.attributes);
        addToArrayInCurrentObject(SEQUENCES, seq);
        _objectStack.push(seq);
    }

    function newTaxonomy(tag) {
        var tax = {};
        tax.id_source = getAttribute(TAXONOMY_ID_SOURCE_ATTR, tag.attributes);
        addToArrayInCurrentObject(TAXONOMIES, tax);
        _objectStack.push(tax);
    }

    function newUri(tag) {
        var uri = {};
        uri.value = null;
        uri.desc = getAttribute(URI_DESC_ATTR, tag.attributes);
        uri.type = getAttribute(URI_TYPE_ATTR, tag.attributes);
        addToArrayInCurrentObject('uris', uri);
        _objectStack.push(uri);
    }

    function newPhylogeny(tag) {
        var phy = {};
        phy.rooted = getAttributeAsBoolean(PHYLOGENY_ROOTED_ATTR, tag.attributes);
        if (phy.rooted === undefined) {
            phy.rooted = true;
        }
        phy.rerootable = getAttributeAsBoolean(PHYLOGENY_REROOTABLE_ATTR, tag.attributes);
        if (phy.rerootable === undefined) {
            phy.rerootable = true;
        }
        phy.branch_length_unit = getAttribute(PHYLOGENY_BRANCH_LENGTH_UNIT_ATTR, tag.attributes);
        phy.type = getAttribute(PHYLOGENY_TYPE_ATTR, tag.attributes);
        _objectStack.push(phy);
    }

    function newSimpleCharacteristics() {
        //To be deprecated.
        var sc = {};
        getCurrentObject().simple_characteristics = sc;
        _objectStack.push(sc);
    }


    // --------------------------------------------------------------
    // Functions for processing text
    // --------------------------------------------------------------

    function inAccession(text) {
        getCurrentObject().value = text;
    }

    function inAnnotation(text) {
        if (getCurrentTag() === ANNOTATION_DESC) {
            getCurrentObject().desc = text;
        }
    }

    function inBranchColor(text) {
        if (getCurrentTag() === COLOR_RED) {
            getCurrentObject().red = parseIntNumber(text);
        }
        else if (getCurrentTag() === COLOR_GREEN) {
            getCurrentObject().green = parseIntNumber(text);
        }
        else if (getCurrentTag() === COLOR_BLUE) {
            getCurrentObject().blue = parseIntNumber(text);
        }
        if (getCurrentTag() === COLOR_ALPHA) {
            getCurrentObject().alpha = parseIntNumber(text);
        }
    }

    function inClade(text) {
        if (getCurrentTag() === CLADE_NAME) {
            getCurrentClade().name = text;
        }
        else if (getCurrentTag() === CLADE_BRANCH_LENGTH) {
            getCurrentClade().branch_length = parseFloatNumber(text);
        }
        else if (getCurrentTag() === CLADE_WIDTH) {
            getCurrentClade().width = parseFloatNumber(text);
        }
    }

    function inConfidence(text) {
        getCurrentObject().value = parseFloatNumber(text);
    }

    function inDate(text) {
        if (getCurrentTag() === DATE_DESC) {
            getCurrentObject().desc = text;
        }
        else if (getCurrentTag() === DATE_VALUE) {
            getCurrentObject().value = parseFloatNumber(text);
        }
        else if (getCurrentTag() === DATE_MINIMUM) {
            getCurrentObject().minimum = parseFloatNumber(text);
        }
        else if (getCurrentTag() === DATE_MAXIMUM) {
            getCurrentObject().maximum = parseFloatNumber(text);
        }
    }

    function inDistribution(text) {
        if (getCurrentTag() === DISTRIBUTION_DESC) {
            getCurrentObject().desc = text;
        }
    }

    function inEvents(text) {
        if (getCurrentTag() === EVENTS_TYPE) {
            getCurrentObject().type = text;
        }
        else if (getCurrentTag() === EVENTS_DUPLICATIONS) {
            getCurrentObject().duplications = parseIntNumber(text);
        }
        else if (getCurrentTag() === EVENTS_SPECIATIONS) {
            getCurrentObject().speciations = parseIntNumber(text);
        }
        else if (getCurrentTag() === EVENTS_LOSSES) {
            getCurrentObject().losses = parseIntNumber(text);
        }
    }

    function inId(text) {
        getCurrentObject().value = text;
    }

    function inMolecularSequence(text) {
        getCurrentObject().value = text;
    }

    function inPoint(text) {
        if (getCurrentTag() === POINT_LAT) {
            getCurrentObject().lat = text;
        }
        else if (getCurrentTag() === POINT_LONG) {
            getCurrentObject().long = text;
        }
        else if (getCurrentTag() === POINT_ALT) {
            getCurrentObject().alt = text;
        }
    }

    function inProperty(text) {
        getCurrentObject().value = text;
    }

    function inProteinDomain(text) {
        getCurrentObject().name = text;
    }

    function inPhylogeny(text) {
        if (getCurrentTag() === PHYLOGENY_NAME) {
            getCurrentObject().name = text;
        }
        else if (getCurrentTag() === PHYLOGENY_DESCRIPTION) {
            getCurrentObject().description = text;
        }
        else if (getCurrentTag() === PHYLOGENY_DATE) {
            getCurrentObject().date = text;
        }
    }

    function inReference(text) {
        if (getCurrentTag() === REFERENCE_DESC) {
            getCurrentObject().desc = text;
        }
    }

    function inSequence(text) {
        if (getCurrentTag() === SEQUENCE_SYMBOL) {
            getCurrentObject().symbol = text;
        }
        else if (getCurrentTag() === SEQUENCE_NAME) {
            getCurrentObject().name = text;
        }
        else if (getCurrentTag() === SEQUENCE_GENE_NAME) {
            getCurrentObject().gene_name = text;
        }
        else if (getCurrentTag() === SEQUENCE_LOCATION) {
            getCurrentObject().location = text;
        }
    }

    function inTaxonomy(text) {
        if (getCurrentTag() === TAXONOMY_CODE) {
            getCurrentObject().code = text;
        }
        else if (getCurrentTag() === TAXONOMY_SCIENTIFIC_NAME) {
            getCurrentObject().scientific_name = text;
        }
        else if (getCurrentTag() === TAXONOMY_AUTHORITY) {
            getCurrentObject().authority = text;
        }
        else if (getCurrentTag() === TAXONOMY_COMMON_NAME) {
            getCurrentObject().common_name = text;
        }
        else if (getCurrentTag() === TAXONOMY_SYNONYM) {
            addToArrayInCurrentObject(TAXONOMY_SYNONYMS, text);
        }
        else if (getCurrentTag() === TAXONOMY_RANK) {
            getCurrentObject().rank = text;
        }
    }

    function inUri(text) {
        getCurrentObject().value = text;
    }

    function inSimpleCharacteristics(text) {
        //To be deprecated.
        if (getCurrentTag() === X_SIMPLE_CHARACTERISTIC_COUNTRY) {
            getCurrentObject().country = text;
        }
        else if (getCurrentTag() === X_SIMPLE_CHARACTERISTIC_HOST) {
            getCurrentObject().host = text;
        }
        else if (getCurrentTag() === X_SIMPLE_CHARACTERISTIC_YEAR) {
            getCurrentObject().year = text;
        }
        else if (getCurrentTag() === X_SIMPLE_CHARACTERISTIC_HA) {
            getCurrentObject().ha = text;
        }
        else if (getCurrentTag() === X_SIMPLE_CHARACTERISTIC_NA) {
            getCurrentObject().na = text;
        }
    }

    // --------------------------------------------------------------
    // Functions for SAX parser
    // --------------------------------------------------------------
    function phyloxmlOnopentag(tag) {
        _tagStack.push(tag.name);
        switch (tag.name) {
            case CLADE:
                newClade(tag);
                break;
            case ACCESSION:
                newAccession(tag);
                break;
            case ANNOTATION:
                newAnnotation(tag);
                break;
            case CLADE_RELATION:
                if (_tagStack.get(1) === PHYLOGENY) {
                    newCladeRelation(tag);
                }
                break;
            case COLOR:
                newBranchColor();
                break;
            case CONFIDENCE:
                newConfidence(tag);
                break;
            case CROSS_REFERENCES:
                newCrossReferences();
                break;
            case DATE:
                if (_tagStack.get(1) === CLADE) {
                    newDate(tag);
                }
                break;
            case DISTRIBUTION:
                newDistribution(tag);
                break;
            case DOMAIN_ARCHITECTURE:
                newDomainArchitecture(tag);
                break;
            case EVENTS:
                newEvents();
                break;
            case ID:
                newId(tag);
                break;
            case MOLSEQ:
                newMolecularSequence(tag);
                break;
            case POINT:
                newPoint(tag);
                break;
            case PROTEINDOMAIN:
                newProteinDomain(tag);
                break;
            case PHYLOGENY:
                newPhylogeny(tag);
                break;
            case PROPERTY:
                newProperty(tag);
                break;
            case REFERENCE:
                newReference(tag);
                break;
            case SEQUENCE:
                newSequence(tag);
                break;
            case SEQUENCE_RELATION:
                if (_tagStack.get(1) === PHYLOGENY) {
                    newSequenceRelation(tag);
                }
                break;
            case TAXONOMY:
                newTaxonomy(tag);
                break;
            case URI:
                newUri(tag);
                break;
            case X_SIMPLE_CHARACTERISTICS:
                //To be deprecated.
                newSimpleCharacteristics();
                break;
            default:
        }
    }

    function phyloxmlOnclosetag(tag) {
        if (tag === CLADE) {
            _tagStack.pop();
            _objectStack.pop();
            _cladeStack.pop();
        }
        else if (
            tag === ACCESSION
            || tag === ANNOTATION
            || ( tag === CLADE_RELATION && (_tagStack.get(1) === PHYLOGENY) )
            || tag === COLOR
            || tag === CONFIDENCE
            || tag === CROSS_REFERENCES
            || ( tag === DATE && (_tagStack.get(1) === CLADE) )
            || tag === DISTRIBUTION
            || tag === TAXONOMY
            || tag === ID
            || tag === EVENTS
            || tag === MOLSEQ
            || tag === REFERENCE
            || tag === DOMAIN_ARCHITECTURE
            || tag === PROTEINDOMAIN
            || tag === SEQUENCE
            || ( tag === SEQUENCE_RELATION && (_tagStack.get(1) === PHYLOGENY) )
            || tag === PROPERTY
            || tag === POINT
            || tag === URI
            || tag === X_SIMPLE_CHARACTERISTICS) {
            _tagStack.pop();
            _objectStack.pop();
        }
        else if (!(tag === PHYLOGENY || tag === PHYLOXML)) {
            _tagStack.pop();
        }
        else if (tag === PHYLOGENY) {
            phyloxmlOnClosetagSanityCheck();
            _phylogenies.push(_phylogeny);
            startNewPhylogeny();
        }
    }

    function phyloxmlOntext(text) {
        // Text belonging to no object at all. That happens for content the
        // reader does not model sitting outside every element it does -- a
        // foreign-namespace extension after the tree, for instance, which
        // phyloXML explicitly permits through its ##other wildcard and puts
        // at the END of a phylogeny, past the point where this parser has
        // popped the phylogeny off the stack. There is nothing to attach the
        // text to, so it is skipped. Without this the handlers below reach
        // into an empty stack and the whole parse dies on a file that is
        // perfectly valid: a viewer that refuses a valid file is worse than
        // one that ignores an element it has no use for.
        if (_objectStack.isEmpty()) {
            return;
        }
        var parentTag = _tagStack.get(1);
        var currentTag = _tagStack.peek();
        if (parentTag === CLADE) {
            inClade(text);
        }
        else if (parentTag === ANNOTATION) {
            inAnnotation(text);
        }
        else if (parentTag === COLOR) {
            inBranchColor(text);
        }
        else if (parentTag === DATE) {
            inDate(text);
        }
        else if (parentTag === DISTRIBUTION) {
            inDistribution(text);
        }
        else if (parentTag === EVENTS) {
            inEvents(text);
        }
        else if (parentTag === REFERENCE) {
            inReference(text);
        }
        else if (parentTag === PHYLOGENY) {
            inPhylogeny(text);
        }
        else if (parentTag === POINT) {
            inPoint(text);
        }
        else if (parentTag === SEQUENCE) {
            inSequence(text);
        }
        else if (parentTag === TAXONOMY) {
            inTaxonomy(text);
        }
        if (currentTag === ACCESSION) {
            inAccession(text);
        }
        else if (currentTag === CONFIDENCE) {
            inConfidence(text);
        }
        else if (currentTag === ID) {
            inId(text);
        }
        else if (currentTag === MOLSEQ) {
            inMolecularSequence(text);
        }
        else if (currentTag === PROTEINDOMAIN) {
            inProteinDomain(text);
        }
        else if (currentTag === PROPERTY) {
            inProperty(text);
        }
        else if (currentTag === URI) {
            inUri(text);
        }
        else if (parentTag === X_SIMPLE_CHARACTERISTICS) {
            inSimpleCharacteristics(text);
        }
    }

    function phyloxmlOnerror(error) {
        throw error;
    }

    function addPhyloxmlParserEvents(sax_parser) {
        sax_parser.onopentag = phyloxmlOnopentag;
        sax_parser.onclosetag = phyloxmlOnclosetag;
        sax_parser.ontext = phyloxmlOntext;
        sax_parser.onerror = phyloxmlOnerror;
        // Ignoring: oncdata, oncomment, ondoctype
    }

    // --------------------------------------------------------------
    // Helper functions
    // --------------------------------------------------------------
    function getCurrentClade() {
        return _cladeStack.peek();
    }

    function getCurrentTag() {
        return _tagStack.peek();
    }

    function getCurrentObject() {
        return _objectStack.peek();
    }

    function getAttribute(attribute_name, attributes) {
        if (attribute_name in attributes) {
            return attributes[attribute_name];
        }
        return undefined;
    }

    function getAttributeAsInt(attribute_name, attributes) {
        if (attribute_name in attributes) {
            return parseIntNumber(attributes[attribute_name]);
        }
        return undefined;
    }

    function getAttributeAsFloat(attribute_name, attributes) {
        if (attribute_name in attributes) {
            return parseFloatNumber(attributes[attribute_name]);
        }
        return undefined;
    }

    function getAttributeAsBoolean(attribute_name, attributes) {
        if (attribute_name in attributes) {
            return parseBoolean(attributes[attribute_name]);
        }
        return undefined;
    }

    function addToArrayInCurrentObject(name, value) {
        var parent = null;
        if (getCurrentObject()) {
            parent = getCurrentObject();
        }
        else {
            parent = _phylogeny;
        }
        var ary = parent[name];
        if (ary) {
            ary.push(value);
        }
        else {
            parent[name] = [value];
        }
    }

    function addToArrayInCurrentObjectUnnamed(value) {
        var obj = getCurrentObject();
        obj.push(value);
    }

    function parseFloatNumber(text) {
        var f = parseFloat(text);
        if (isNaN(f)) {
            throw new PhyloXmlError("could not parse floating point number from '" + text + "'");
        }
        return f;
    }

    function parseIntNumber(text) {
        var i = parseInt(text);
        if (isNaN(i)) {
            throw new PhyloXmlError("could not parse integer number from '" + text + "'");
        }
        return i;
    }

    function parseBoolean(text) {
        if (text === 'true') {
            return true;
        }
        else if (text === 'false') {
            return false;
        }
        else {
            throw new PhyloXmlError("could not parse boolean from '" + text + "'");
        }
    }

    function startNewPhylogeny() {
        _phylogeny = null;
        _cladeStack = new Stack();
        _tagStack = new Stack();
        _objectStack = new Stack();
    }

    function phyloxmlOnClosetagSanityCheck() {
        if (!(_cladeStack.isEmpty() && _objectStack.isEmpty() )) {
            throw new PhyloXmlError('severe phyloXML format error')
        }
    }

    function finalSanityCheck() {
        if (!_tagStack.isEmpty()) {
            throw new PhyloXmlError('severe phyloXML format error');
        }
    }

    // --------------------------------------------------------------
    // Stack
    // --------------------------------------------------------------
    function Stack() {
        this._stack = [];
        this.pop = function () {
            var p = this._stack.pop();
            if (p === undefined) {
                throw new Error('severe phyloXML format error')
            }
            return p;
        };
        this.push = function (item) {
            this._stack.push(item);
        };
        this.peek = function () {
            return this._stack[this._stack.length - 1];
        };
        this.get = function (i) {
            return this._stack[this._stack.length - (1 + i)];
        };
        this.length = function () {
            return this._stack.length;
        };
        this.isEmpty = function () {
            return this._stack.length < 1;
        };
    }

    // --------------------------------------------------------------
    // phyloXML error
    // --------------------------------------------------------------
    function PhyloXmlError(message) {
        this.name = 'phyloXmlError';
        this.message = message || 'phyloXML format error';
    }

    PhyloXmlError.prototype = Object.create(Error.prototype);

    // --------------------------------------------------------------
    // To phyloXML
    // --------------------------------------------------------------
    phyloXml.toPhyloXML_ = function (phy, dec) {
        var x = '';
        var ind = '';
        // The desktop's layout, so that the two programs' files agree to the
        // byte: <phylogeny> at the margin, two spaces per level below it.
        // This wrote <phylogeny> one space in and one space per level, and
        // every file differed on every line (found 2026-09-30 by running the
        // desktop's released 0.11.173 jar; earlier comparisons had trimmed
        // indentation and missed it).
        var INDENT = '  ';
        openPhyloXml();
        // in the order the desktop (forester) writes them, so that the two
        // programs' files agree to the byte and not only to an XML reader
        openPhylogeny(phy, [PHYLOGENY_ROOTED_ATTR, PHYLOGENY_BRANCH_LENGTH_UNIT_ATTR,
            PHYLOGENY_TYPE_ATTR, PHYLOGENY_REROOTABLE_ATTR]);
        addSingleElement(PHYLOGENY_NAME, phy.name);
        if (phy[ID]) {
            addSingleElement(ID, phy[ID].value, phy[ID], [ID_PROVIDER_ATTR]);
        }
        addSingleElement(PHYLOGENY_DESCRIPTION, phy.description);
        addSingleElement(PHYLOGENY_DATE, phy.date);
        // The desktop keeps ONE confidence for a phylogeny, the last one read,
        // and writes that; the schema allows several, and so does the reader.
        if (phy[CONFIDENCES] && phy[CONFIDENCES].length > 0) {
            addConfidence(phy[CONFIDENCES][phy[CONFIDENCES].length - 1]);
        }
        if (phy.children && phy.children.length === 1) {
            toPhyloXMLhelper(phy.children[0]);
        }
        // The phylogeny's own properties -- a figure setting, say -- after the
        // clade, where the schema puts them and the desktop writes them. They
        // were read and then dropped on the way out.
        addProperties(phy[PROPERTIES]);
        closePhylogeny();
        closePhyloXml();
        return x;

        function toPhyloXMLhelper(node) {
            var l;
            var i;

            openClade(node, [CLADE_ID_SOURCE_ATTR, CLADE_COLLAPSE_ATTR]);

            addSingleElement(CLADE_NAME, node.name);

            // A branch length of 0 is a LENGTH, not a missing value: zero-length
            // branches are ordinary in a tree whose sequences are identical, and
            // a multifurcation written out as a series of them. A truthiness
            // test dropped every one of them, so a tree read in and written back
            // out came back with those branches undefined -- silently, and on
            // real files (bunya_glyco.xml has dozens).
            if ((node[CLADE_BRANCH_LENGTH] !== undefined) && (node[CLADE_BRANCH_LENGTH] !== null)) {
                addSingleElement(CLADE_BRANCH_LENGTH, (dec && dec > 0) ? roundNumber(node[CLADE_BRANCH_LENGTH], dec) : node[CLADE_BRANCH_LENGTH]);
            }

            if (node[CONFIDENCES] && node[CONFIDENCES].length > 0) {
                l = node[CONFIDENCES].length;
                for (i = 0; i < l; ++i) {
                    var conf = node[CONFIDENCES][i];
                    if (!conf[CONFIDENCE_TYPE_ATTR]) {
                        conf[CONFIDENCE_TYPE_ATTR] = UNKNOWN;
                    }
                    addSingleElement(CONFIDENCE, conf.value, conf,
                        [CONFIDENCE_TYPE_ATTR, CONFIDENCE_STDDEV_ATTR]);
                }
            }

            addSingleElement(CLADE_WIDTH, node[CLADE_WIDTH]);

            if (node[COLOR]) {
                var col = node[COLOR];
                open(COLOR);
                addSingleElement(COLOR_RED, col[COLOR_RED]);
                addSingleElement(COLOR_GREEN, col[COLOR_GREEN]);
                addSingleElement(COLOR_BLUE, col[COLOR_BLUE]);
                addSingleElement(COLOR_ALPHA, col[COLOR_ALPHA]);
                close(COLOR);
            }

            if (node[TAXONOMIES] && node[TAXONOMIES].length > 0) {
                l = node[TAXONOMIES].length;
                for (i = 0; i < l; ++i) {
                    var tax = node[TAXONOMIES][i];
                    open(TAXONOMY, tax, [TAXONOMY_ID_SOURCE_ATTR]);
                    if (tax[ID]) {
                        if (!tax[ID][ID_PROVIDER_ATTR]) {
                            tax[ID][ID_PROVIDER_ATTR] = UNKNOWN;
                        }
                        addSingleElement(ID, tax[ID].value, tax[ID],
                            [ID_PROVIDER_ATTR]);
                    }
                    addSingleElement(TAXONOMY_CODE, tax[TAXONOMY_CODE]);
                    addSingleElement(TAXONOMY_SCIENTIFIC_NAME, tax[TAXONOMY_SCIENTIFIC_NAME]);
                    addSingleElement(TAXONOMY_AUTHORITY, tax[TAXONOMY_AUTHORITY]);
                    addSingleElement(TAXONOMY_COMMON_NAME, tax[TAXONOMY_COMMON_NAME]);
                    if (tax[TAXONOMY_SYNONYMS] && tax[TAXONOMY_SYNONYMS].length > 0) {
                        var ll = tax[TAXONOMY_SYNONYMS].length;
                        for (var ii = 0; ii < ll; ++ii) {
                            addSingleElement(TAXONOMY_SYNONYM, tax[TAXONOMY_SYNONYMS][ii]);
                        }
                    }
                    addSingleElement(TAXONOMY_RANK, tax[TAXONOMY_RANK]);
                    addUris(tax[URIS]);
                    close(TAXONOMY);
                }
            }

            if (node[SEQUENCES] && node[SEQUENCES].length > 0) {
                l = node[SEQUENCES].length;
                for (i = 0; i < l; ++i) {
                    var seq = node[SEQUENCES][i];
                    open(SEQUENCE, seq, [SEQUENCE_TYPE_ATTR, SEQUENCE_ID_SOURCE_ATTR, SEQUENCE_ID_REF_ATTR]);
                    addSingleElement(SEQUENCE_SYMBOL, seq[SEQUENCE_SYMBOL]);
                    if (seq[ACCESSION]) {
                        if (!seq[ACCESSION][ACCESSION_SOURCE_ATTR]) {
                            seq[ACCESSION][ACCESSION_SOURCE_ATTR] = UNKNOWN;
                        }
                        addSingleElement(ACCESSION, seq[ACCESSION].value, seq[ACCESSION],
                            [ACCESSION_SOURCE_ATTR, ACCESSION_COMMENT_ATTR]);
                    }
                    addSingleElement(SEQUENCE_NAME, seq[SEQUENCE_NAME]);
                    addSingleElement(SEQUENCE_GENE_NAME, seq[SEQUENCE_GENE_NAME]);
                    addSingleElement(SEQUENCE_LOCATION, seq[SEQUENCE_LOCATION]);
                    if (seq[MOLSEQ]) {
                        addSingleElement(MOLSEQ, seq[MOLSEQ].value, seq[MOLSEQ],
                            [MOLSEQ_IS_ALIGNED_ATTR]);
                    }
                    addUris(seq[URIS]);
                    addAnnotations(seq[ANNOTATIONS]);
                    addCrossReferences(seq[CROSS_REFERENCES]);
                    addDomainArchitecture(seq[DOMAIN_ARCHITECTURE]);
                    close(SEQUENCE);
                }
            }

            if (node[EVENTS]) {
                var ev = node[EVENTS];
                open(EVENTS);
                addSingleElement(EVENTS_TYPE, ev[EVENTS_TYPE]);
                addSingleElement(EVENTS_DUPLICATIONS, ev[EVENTS_DUPLICATIONS]);
                addSingleElement(EVENTS_SPECIATIONS, ev[EVENTS_SPECIATIONS]);
                addSingleElement(EVENTS_LOSSES, ev[EVENTS_LOSSES]);
                if (ev[CONFIDENCE]) {
                    var evconf = ev[CONFIDENCE];
                    addSingleElement(CONFIDENCE, evconf.value, evconf,
                        [CONFIDENCE_TYPE_ATTR, CONFIDENCE_STDDEV_ATTR]);
                }
                close(EVENTS);
            }

            addDistributions(node[DISTRIBUTIONS]);

            // A clade's <date>. phyloXML's sequence order puts it after
            // <distribution> and before <property>, and its own children are
            // fixed as desc, value, minimum, maximum -- another order is
            // schema-INVALID, not merely different.
            //
            // Written whenever the node has a date at all, a desc-only date
            // included, and deliberately NOT rounded by `dec` the way a branch
            // length is. A branch length's decimals are display precision; a
            // date is a position in time, so rounding changes what it SAYS
            // (2005.25 to two decimals is a season, to none a year). It also
            // has to survive exactly: TreeAnnotator writes an exactly dated
            // tip's bounds as {9.0, 9.000000000000004}, and rounding would
            // quietly "clean" that. Whether a bound pair is a genuine WIDTH is
            // a question for whoever DRAWS it, never for whoever stores it --
            // a writer that tidied its input would make two programs' files
            // diverge. Matches the desktop Archaeopteryx's Date.toPhyloXML
            // (their spec, 2026-09-17), which preserves the parsed number.
            //
            // Until now no clade date was written at all: a dated tree read in
            // and written back out lost its entire time dimension, silently.
            if (node[DATE]) {
                var date = node[DATE];
                // `unit` is written when the date states one, and left out
                // when it is absent or EMPTY: the desktop's writer skips an
                // attribute with nothing in it, so a date without a unit is
                // <date>, not <date unit="">. (This wrote the empty attribute
                // until 1.1.1, in the belief that the desktop did; its own
                // files, written by its released jar, said otherwise.)
                var dateAttr = {};
                if (date[DATE_UNIT_ATTR] !== undefined && date[DATE_UNIT_ATTR] !== null
                    && String(date[DATE_UNIT_ATTR]).length > 0) {
                    dateAttr[DATE_UNIT_ATTR] = date[DATE_UNIT_ATTR];
                }
                open(DATE, dateAttr, [DATE_UNIT_ATTR]);
                addSingleElement(DATE_DESC, date[DATE_DESC]);
                addSingleElement(DATE_VALUE, date[DATE_VALUE]);
                addSingleElement(DATE_MINIMUM, date[DATE_MINIMUM]);
                addSingleElement(DATE_MAXIMUM, date[DATE_MAXIMUM]);
                close(DATE);
            }

            addReferences(node[REFERENCES]);

            addProperties(inDesktopStyleOrder(node[PROPERTIES]));

            if (node.children) {
                l = node.children.length;
                for (i = 0; i < l; ++i) {
                    toPhyloXMLhelper(node.children[i]);
                }
            }
            else if (node._children) {
                l = node._children.length;
                for (i = 0; i < l; ++i) {
                    toPhyloXMLhelper(node._children[i]);
                }
            }

            closeClade();

        } // toPhyloXMLhelper

        // ---- the elements this writer dropped until 1.1.5 --------------------
        // Each is written as the desktop Archaeopteryx's writer (forester)
        // writes it, element order, attribute order and all, so a file saved
        // by either program is the same file. Where the desktop REORDERS --
        // annotations and cross-references are sorted sets there, domains a map
        // keyed by their start -- this does the same, duplicates dropped as a
        // sorted set drops them; files are compared whole, so the order is part
        // of the format. Pinned against a file written by the 0.11.173 jar
        // (test/data/complete_desktop_0.11.173.xml).

        function addConfidence(conf) {
            if (!conf) {
                return;
            }
            if (!conf[CONFIDENCE_TYPE_ATTR]) {
                conf[CONFIDENCE_TYPE_ATTR] = UNKNOWN;
            }
            addSingleElement(CONFIDENCE, conf.value, conf, [CONFIDENCE_TYPE_ATTR, CONFIDENCE_STDDEV_ATTR]);
        }

        function addUris(uris) {
            (uris || []).forEach(function (u) {
                if (u && u.value !== undefined && u.value !== null) {
                    addSingleElement(URI, u.value, u, [URI_TYPE_ATTR, URI_DESC_ATTR]);
                }
            });
        }

        // an element with attributes and nothing inside, written open and
        // closed on one line as the desktop writes it: <annotation ref="x"></annotation>
        function addEmptyElement(elemName, object, attributeNames) {
            x += ind + '<' + elemName;
            addAttributes(object, attributeNames);
            x += '></' + elemName + '>\n';
        }

        // Sorted, and a second entry the order calls equal is dropped: what a
        // Java TreeSet with that comparator keeps.
        function sortedSet(list, compare) {
            var out = [];
            (list || []).slice().sort(compare).forEach(function (item) {
                if (item && (out.length === 0 || compare(out[out.length - 1], item) !== 0)) {
                    out.push(item);
                }
            });
            return out;
        }

        function compareStrings(a, b) {
            a = (a === undefined || a === null) ? '' : String(a);
            b = (b === undefined || b === null) ? '' : String(b);
            return a < b ? -1 : (a > b ? 1 : 0);
        }

        // by ref, then by desc (Annotation.compareTo)
        function addAnnotations(annotations) {
            var ANNOTATION_ATTRS = [ANNOTATION_REF_ATTR, ANNOTATION_EVIDENCE_ATTR, ANNOTATION_TYPE_ATTR,
                ANNOTATION_SOURCE_ATTR];
            sortedSet(annotations, function (a, b) {
                return compareStrings(a[ANNOTATION_REF_ATTR], b[ANNOTATION_REF_ATTR])
                    || compareStrings(a[ANNOTATION_DESC], b[ANNOTATION_DESC]);
            }).forEach(function (ann) {
                var hasBody = ann[CONFIDENCE] || (ann[PROPERTIES] && ann[PROPERTIES].length > 0)
                    || (ann[URIS] && ann[URIS].length > 0)
                    || (typeof ann[ANNOTATION_DESC] === 'string' && ann[ANNOTATION_DESC].trim().length > 0);
                if (!hasBody) {
                    addEmptyElement(ANNOTATION, ann, ANNOTATION_ATTRS);
                    return;
                }
                open(ANNOTATION, ann, ANNOTATION_ATTRS);
                addSingleElement(ANNOTATION_DESC, ann[ANNOTATION_DESC]);
                addConfidence(ann[CONFIDENCE]);
                addProperties(ann[PROPERTIES]);
                addUris(ann[URIS]);
                close(ANNOTATION);
            });
        }

        // by source then value, run together (Accession's _source_value)
        function addCrossReferences(xrefs) {
            var sorted = sortedSet(xrefs, function (a, b) {
                return compareStrings((a[ACCESSION_SOURCE_ATTR] || '') + a.value,
                    (b[ACCESSION_SOURCE_ATTR] || '') + b.value);
            });
            if (sorted.length < 1) {
                return;
            }
            open(CROSS_REFERENCES);
            sorted.forEach(function (acc) {
                var a = {};
                a[ACCESSION_SOURCE_ATTR] = acc[ACCESSION_SOURCE_ATTR] || UNKNOWN;
                a[ACCESSION_COMMENT_ATTR] = acc[ACCESSION_COMMENT_ATTR];
                addSingleElement(ACCESSION, acc.value, a, [ACCESSION_SOURCE_ATTR, ACCESSION_COMMENT_ATTR]);
            });
            close(CROSS_REFERENCES);
        }

        // Domains by where they start; two that start together keep the order
        // they were read in (the desktop's key nudges the later one up).
        function addDomainArchitecture(da) {
            if (!da) {
                return;
            }
            var len = {};
            len[DOMAIN_ARCHITECTURE_LENGTH_ATTR] = da[DOMAIN_ARCHITECTURE_LENGTH_ATTR];
            open(DOMAIN_ARCHITECTURE, len, [DOMAIN_ARCHITECTURE_LENGTH_ATTR]);
            (da.domains || []).map(function (d, i) {
                return {d: d, i: i};
            }).sort(function (p, q) {
                return (p.d.from - q.d.from) || (p.i - q.i);
            }).forEach(function (e) {
                var d = e.d;
                var a = {};
                a[PROTEINDOMAIN_FROM_ATTR] = d.from;
                a[PROTEINDOMAIN_TO_ATTR] = d.to;
                a[PROTEINDOMAIN_CONFIDENCE_ATTR] = (typeof d.confidence === 'number' && isFinite(d.confidence))
                    ? d.confidence : 0;
                a[PROTEINDOMAIN_ID_ATTR] = d.id;
                var name = (d.name === undefined || d.name === null) ? '' : String(d.name);
                x += ind + '<' + PROTEINDOMAIN;
                addAttributes(a, [PROTEINDOMAIN_FROM_ATTR, PROTEINDOMAIN_TO_ATTR, PROTEINDOMAIN_CONFIDENCE_ATTR,
                    PROTEINDOMAIN_ID_ATTR]);
                x += '>' + replaceUnsafeChars(name) + '</' + PROTEINDOMAIN + '>\n';
            });
            close(DOMAIN_ARCHITECTURE);
        }

        function addDistributions(distributions) {
            (distributions || []).forEach(function (dist) {
                var points = (dist[POINTS] || []).filter(function (pt) {
                    return pt && pt[POINT_LAT] !== undefined && pt[POINT_LAT] !== null
                        && pt[POINT_LONG] !== undefined && pt[POINT_LONG] !== null;
                });
                var desc = typeof dist[DISTRIBUTION_DESC] === 'string' ? dist[DISTRIBUTION_DESC].trim() : '';
                if (desc.length < 1 && points.length < 1) {
                    return;
                }
                open(DISTRIBUTION);
                addSingleElement(DISTRIBUTION_DESC, dist[DISTRIBUTION_DESC]);
                points.forEach(function (pt) {
                    var hasAlt = pt[POINT_ALT] !== undefined && pt[POINT_ALT] !== null && String(pt[POINT_ALT]).length > 0;
                    open(POINT, pt, hasAlt ? [POINT_GEODETIC_DATUM_ATTR, POINT_ALT_UNIT_ATTR] : [POINT_GEODETIC_DATUM_ATTR]);
                    addSingleElement(POINT_LAT, pt[POINT_LAT]);
                    addSingleElement(POINT_LONG, pt[POINT_LONG]);
                    if (hasAlt) {
                        addSingleElement(POINT_ALT, pt[POINT_ALT]);
                    }
                    close(POINT);
                });
                close(DISTRIBUTION);
            });
        }

        // a reference is written open and closed on two lines even when it
        // holds nothing, as the desktop writes it
        function addReferences(references) {
            (references || []).forEach(function (ref) {
                open(REFERENCE, ref, [REFERENCE_DOI_ATTR]);
                addSingleElement(REFERENCE_DESC, ref[REFERENCE_DESC]);
                close(REFERENCE);
            });
        }

        // A clade's properties in the order the desktop writes them. It does not
        // keep a node's style (a style: property that applies to the node) as a
        // property: it reads it into the node's style and writes it back AFTER
        // every other property, in one fixed order -- font, font size, font
        // style, font colour, node shape, node size, node colour, fill. Every
        // other property, a style: one about the clade included, keeps the
        // file's order. (The desktop drops a node style it does not know, such
        // as style:node_transparency; it is kept here, in the file's order.)
        function inDesktopStyleOrder(props) {
            if (!props || props.length < 2) {
                return props;
            }
            var rest = [];
            var styled = [];
            props.forEach(function (p) {
                var rank = p ? NODE_STYLE_ORDER.indexOf(p[PROPERTY_REF_ATTR]) : -1;
                if (rank >= 0 && p[PROPERTY_APPLIES_TO_ATTR] === 'node') {
                    styled.push({p: p, rank: rank});
                }
                else {
                    rest.push(p);
                }
            });
            styled.sort(function (a, b) {
                return a.rank - b.rank;
            });
            return rest.concat(styled.map(function (e) {
                return e.p;
            }));
        }

        function addProperties(props) {
            if (!props || props.length < 1) {
                return;
            }
            for (var i = 0; i < props.length; ++i) {
                var prop = props[i];
                if (!prop[PROPERTY_APPLIES_TO_ATTR]) {
                    throw new PhyloXmlError("property applies-to is missing");
                }
                if (!prop[PROPERTY_DATATYPE_ATTR]) {
                    throw new PhyloXmlError("property data-type is missing");
                }
                if (!prop[PROPERTY_REF_ATTR]) {
                    throw new PhyloXmlError("property ref is missing");
                }
                // only a NODE's style is the desktop's to rewrite; one about the
                // clade is an ordinary property there, written as it stands
                var value = (STYLE_COLOR_REFS[prop[PROPERTY_REF_ATTR]] && prop[PROPERTY_APPLIES_TO_ATTR] === 'node')
                    ? styleColor(prop.value) : prop.value;
                addSingleElement(PROPERTY, value, prop, [PROPERTY_REF_ATTR,
                    PROPERTY_UNIT_ATTR, PROPERTY_DATATYPE_ATTR, PROPERTY_APPLIES_TO_ATTR,
                    PROPERTY_ID_REF_ATTR]);
            }
        }

        // A node's style colour (style:font_color, style:node_color) is written
        // as the desktop writes it: lower-case "#rrggbb". The desktop does not
        // keep the text -- it reads the colour as a number (java.awt.Color.decode,
        // i.e. Integer.decode: "#" or "0x" hex, a leading 0 octal, else decimal)
        // and prints that number back -- so "#E65050" comes back "#e65050" and
        // every spelling it understands comes back in the one form. A value it
        // cannot read is written as it stands here.
        function styleColor(v) {
            var t = (v === undefined || v === null) ? '' : String(v).trim();
            var m = /^([-+]?)(?:(0[xX]|#)([0-9a-fA-F]+)|(0[0-7]+)|([0-9]+))$/.exec(t);
            if (!m) {
                return v;
            }
            var n = m[3] !== undefined ? parseInt(m[3], 16)
                : (m[4] !== undefined ? parseInt(m[4], 8) : parseInt(m[5], 10));
            if (m[1] === '-') {
                n = -n;
            }
            if (!isFinite(n) || n > 2147483647 || n < -2147483648) {
                return v;                          // outside an int: Integer.decode refuses it
            }
            var hex = function (c) {
                return (c < 16 ? '0' : '') + c.toString(16);
            };
            return '#' + hex((n >> 16) & 255) + hex((n >> 8) & 255) + hex(n & 255);
        }

        function addSingleElement(elemName, elemValue, object, attributeNames) {
            if ((elemValue !== null) && (elemValue !== undefined)) {
                if (typeof elemValue === 'string' || elemValue instanceof String) {
                    elemValue = elemValue.trim();
                    if (elemValue.length > 0) {
                        if ((elemValue.indexOf('&') > -1) || ( elemValue.indexOf('<') > -1) || ( elemValue.indexOf('>') > -1)
                            || (elemValue.indexOf('"') > -1) || ( elemValue.indexOf("'") > -1)) {
                            elemValue = replaceUnsafeChars(elemValue);
                        }
                    }
                    else {
                        return;
                    }
                }
                x += ( ind + '<' + elemName);
                if (object && attributeNames && attributeNames.length > 0) {
                    addAttributes(object, attributeNames);
                }
                x += ( '>' + elemValue + '</' + elemName + '>\n');
            }
        }

        function open(elemName, object, attributeNames) {
            if (object && attributeNames && attributeNames.length > 0) {
                x += ( ind + '<' + elemName);
                addAttributes(object, attributeNames);
                x += '>\n';
            }
            else {
                x += (ind + '<' + elemName + '>\n' );
            }
            ind = ind + INDENT;
        }

        function close(elemName) {
            reduceInd();
            x += ( ind + '</' + elemName + '>\n' );
        }

        function openClade(object, attributeNames) {
            if (object && attributeNames && attributeNames.length > 0) {
                x += ind + '<clade';
                addAttributes(object, attributeNames);
                x += '>\n';
            }
            else {
                x += ind + '<clade>\n';
            }
            ind = ind + INDENT;
        }

        function closeClade() {
            reduceInd();
            x += ind + '</clade>\n';
        }

        function openPhylogeny(object, attributeNames) {
            if (object[PHYLOGENY_ROOTED_ATTR] === undefined ||
                object[PHYLOGENY_ROOTED_ATTR] === null) {
                object[PHYLOGENY_ROOTED_ATTR] = true;
            }
            if (object[PHYLOGENY_REROOTABLE_ATTR] === undefined ||
                object[PHYLOGENY_REROOTABLE_ATTR] === null) {
                object[PHYLOGENY_REROOTABLE_ATTR] = true;
            }
            if (object && attributeNames && attributeNames.length > 0) {
                x += '<phylogeny';
                addAttributes(object, attributeNames);
                x += '>\n';
            }
            else {
                x += '<phylogeny>\n';
            }
            ind = INDENT;
        }

        function closePhylogeny() {
            ind = '';
            x += '</phylogeny>\n';
        }

        function openPhyloXml() {
            ind = '';
            x += '<?xml version="1.0" encoding="UTF-8"?>\n';
            // No xsi:schemaLocation hint. It used to point at
            // http://www.phyloxml.org/1.20/phyloxml.xsd, and phyloxml.org
            // lapsed: the domain is held by an unrelated party now, so a tool
            // that honours the hint and fetches it reaches whatever they
            // serve. The hint was never needed to read a file -- it is advice
            // to a validator about where a schema MIGHT live, not part of the
            // format -- so writing it is a pointer to somewhere we no longer
            // control for no benefit.
            //
            // The NAMESPACE below is deliberately unchanged. It happens to
            // look like a URL but it is an opaque identifier, it is in every
            // phyloXML file in existence, and nothing dereferences it.
            // Changing it would break the format rather than protect anyone.
            //
            // Files that still CARRY the hint must keep parsing -- every
            // phyloXML file written before 2026-09-10 has one. The parser
            // ignores it either way; see the round-trip test.
            x += '<phyloxml xmlns="http://www.phyloxml.org">\n';
        }

        // no newline after the last tag, as the desktop writes it
        function closePhyloXml() {
            x += '</phyloxml>';
        }

        function addAttributes(object, attributeNames) {
            var l = attributeNames.length;
            for (var i = 0; i < l; ++i) {
                var attributeName = attributeNames[i];
                if (attributeName && ( object[attributeName] !== undefined && object[attributeName] !== null )) {
                    x += (' ' + attributeName + '="' + object[attributeName] + '"' );
                }
            }
        }

        function roundNumber(num, dec) {
            return Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
        }

        function replaceUnsafeChars(str) {
            return str
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&apos;");
        }

        function reduceInd() {
            ind = ind.substring(0, Math.max(0, ind.length - INDENT.length));
        }

    }; // toPhyloXML_


    // --------------------------------------------------------------
    // Main functions
    // --------------------------------------------------------------

    /**
     * To parse phyloXML formatted trees from a stream asynchronously.
     *
     * @param stream - The stream to be parsed.
     * @param parseOptions - Options dict for the SAX parser.
     *                       (example: {trim: true, normalize: true}).
     */
    phyloXml.parseAsync = function (stream, parseOptions) {
        _phylogenies = [];
        startNewPhylogeny();
        var sax_parser = sax.createStream(true, parseOptions);
        addPhyloxmlParserEvents(sax_parser);
        stream.pipe(sax_parser);

        sax_parser.on('end', function () {
            finalSanityCheck();
            var len = _phylogenies.length;
            console.log("parsed " + len + " trees");
            // for (var i = 0; i < len; ++i) {
            //    do something, for example:
            //    var str = JSON.stringify(_phylogenies[i], null, 2);
            //}
        });

        process.stdout.on('drain', function () {
            stream.resume();
        });
    };

    /**
     * To parse a phyloXML formatted source.
     *
     * @param source - The source.
     * @param parseOptions - Options dict for the SAX parser
     *                       (example: {trim: true, normalize: true}).
     * @returns {*} - Array of phylogentic tree objects.
     */
    phyloXml.parse = function (source, parseOptions) {
        source && ( source = source.toString().trim());

        if (!source) {
            throw new Error('phyloXML source is empty');
        }

        _phylogenies = [];
        startNewPhylogeny();
        var sax_parser = sax.parser(true, parseOptions);
        addPhyloxmlParserEvents(sax_parser);

        sax_parser.onend = function () {
            finalSanityCheck();
        };

        sax_parser.write(source).close();
        return _phylogenies;
    };


    /**
     * To convert a phylogentic tree object to a phyloXML formatted string.
     *
     * @param phy - A phylogentic tree object.
     * @param decPointsMax - Maximal number of decimal points for branch lengths (optional).
     * @returns A phyloXML formatted string.
     */
    phyloXml.toPhyloXML = function (phy, decPointsMax) {
        return phyloXml.toPhyloXML_(phy, decPointsMax);
    };

    // --------------------------------------------------------------
    // For exporting
    // --------------------------------------------------------------
    if (typeof module !== 'undefined' && module.exports && !global.xmldocAssumeBrowser)
        module.exports.phyloXml = phyloXml;
    else if (typeof window !== "undefined")
        window.phyloXml = phyloXml;
    else
        this.phyloXml = phyloXml;
})();