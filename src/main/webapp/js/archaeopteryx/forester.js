/**
 *  Copyright (C) 2026 Christian M. Zmasek
 *  Copyright (C) 2026 Yun Zhang
 *  Copyright (C) 2026 J. Craig Venter Institute
 *  All rights reserved
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, see
 *  <https://www.gnu.org/licenses/>.
 *
 */

// v 3.26.0
// 2026-09-17
//
// forester.js is a general suite for dealing with phylogenetic trees.
// 
// forester.js is used by Archaeopteryx.js:
//   https://github.com/cmzmasek/archaeopteryx-js
//   https://www.npmjs.com/package/archaeopteryx
//
// Availability:
//   https://github.com/cmzmasek/archaeopteryx-js
//   https://www.npmjs.com/package/archaeopteryx
//
// Dependencies: none
//
// REMOVED FROM THE PUBLIC API (2026-09-11). Each was exported, called by
// nothing -- not this library, not Archaeopteryx.js, not the test suite --
// and is recorded here so that a later "forester used to have X" can be
// matched to a decision instead of investigated from scratch:
//
//   collapse, unCollapse          the subtree-collapse feature they served was
//                                 removed from the viewer; they were the only
//                                 writers of node._children, whose handling
//                                 went with them
//   getChildren                   returned _children in preference to children,
//                                 so it only ever meant anything while collapse
//                                 existed
//   findByTaxonomyCode            superseded by the search machinery
//   findByTaxonomyScientificName  (searchWithSpec and friends)
//   calcAverageTreeHeight         never used by any caller
//   calcMaxDepth
//   calcBranchLengthSimpleStatistics
//   collectPropertyRefs           superseded by visualizationCandidates
//   isHasNodeData
//   removeMaxBranchLength
//   getOneDistinctTaxonomy
//
// forester.js ships inside the archaeopteryx npm package, so an outside caller
// could in principle have used any of these. That is the cost that was weighed
// and accepted: 12 of 80 exports earned nothing here, and dead code is not free
// -- it has to keep working, keep linting clean, and be considered in every
// refactor.
//
//
// In the following is a basic example shows how to parse a New Hampshire formatted String
// into to a object representing a phylogenetic tree.
// Followed by pre- and post-order traversal,
// and writing back to a New Hampshire formatted String.
//
// Change './forester' to 'forester' if you use this code outside of this package
//
// let forester = require('./forester').forester;
//
// let newHampshireFormattedString = "(((a:1,b:1,c:1)N:2,(d:1,e:1)M:4)O:4,f:1)R:1;";
// let phylogeneticTree = forester.parseNewHampshire(newHampshireFormattedString);
//
// console.log('Pre-order traversal:');
// forester.preOrderTraversalAll(forester.getTreeRoot(phylogeneticTree), function (n) {
//     console.log(n.name + ':' + n.branch_length);
// });
//
// console.log('Post-order traversal:');
// forester.postOrderTraversalAll(forester.getTreeRoot(phylogeneticTree), function (n) {
//     console.log(n.name + ':' + n.branch_length);
// });
//
// console.log('In New Hampshire format:');
// let nh = forester.toNewHampshire(phylogeneticTree);
// console.log(nh);


(function forester() {

    "use strict";

    const BRANCH_EVENT_REF = 'aptx:branch_event';
    const BRANCH_EVENT_DATATYPE = 'xsd:string';
    const BRANCH_EVENT_APPLIES_TO = 'parent_branch';
    const NH_FORMAT_ERR = 'New Hampshire (Newick) format error: ';

    const NUMBERS_ONLY_PATTERN = /^[-+]?[0-9\\.]+$/;

    // The release this file belongs to. archaeopteryx.js compares it with its
    // own (archaeopteryx.doctor): the two are separate files, a site that
    // concatenates its own bundle can pair them from different releases, and
    // until this constant nothing could tell. Bumped with the other four
    // places; test/visualization_test.js holds all five together.
    forester.VERSION = '3.26.0';


    /**
     * Sets links to parent nodes for all nodes in a
     * phyloXML-based tree object
     *
     * @param phy - A phyloXML-based tree object.
     */
    forester.addParents = function (phy) {
        if (phy.children) {
            for (let i = phy.children.length - 1; i >= 0; --i) {
                let c = phy.children[i];
                c.parent = phy;
                forester.addParents(c);
            }
        }
    };

    /**
     * Returns the real root node of a
     * phyloXML-based tree object.
     * Precondition: needs to have parents set.
     *
     * @param phy - A phyloXML-based tree object or node.
     * @returns {*} - The real tree root node.
     */
    forester.getTreeRoot = function (phy) {
        let root = phy;
        if (!root.parent && root.children && root.children.length === 1) {
            root = root.children[0];
        }
        while (root.parent && root.parent.parent) {
            root = root.parent;
        }
        return root;
    };

    /**
     * Visits all non-collapsed child nodes of a node
     * while applying a function in pre-order.
     *
     * @param node - The root of the subtree to traverse.
     * @param fn - The function to apply.
     */
    forester.preOrderTraversal = function (node, fn) {
        fn(node);
        if (node.children) {
            for (let i = node.children.length - 1; i >= 0; --i) {
                forester.preOrderTraversal(node.children[i], fn);
            }
        }
    };

    /**
     * Visits all child nodes of a node
     * while applying a function in pre-order.
     *
     * @param node - The root of the subtree to traverse.
     * @param fn - The function to apply.
     */
    // Kept as a distinct name because callers use both, but it IS
    // preOrderTraversal now: the two differed only in that this one also
    // descended into a collapsed node's hidden _children, and nothing can
    // collapse a node any more. See the removed-API note at the top.
    forester.preOrderTraversalAll = function (node, fn) {
        forester.preOrderTraversal(node, fn);
    };

    forester.postOrderTraversalAll = function (node, fn) {
        if (node.children) {
            let l = node.children.length;
            for (let i = 0; i < l; ++i) {
                forester.postOrderTraversalAll(node.children[i], fn);
            }
        }
        fn(node);
    };


    forester.findByNodeName = function (node, name) {
        let found = [];
        forester.preOrderTraversalAll(node, function (n) {
            if (n.name === name) {
                found.push(n);
            }
        });
        return found;
    };

    /**
     * To delete a sub-tree or external node.
     *
     * @param phy
     * @param nodeToDelete
     */
    forester.deleteSubtree = function (phy, nodeToDelete) {
        if (!phy) {
            throw ("cannot delete null tree");
        }
        if (!nodeToDelete) {
            throw ("cannot delete null node");
        }
        if (!nodeToDelete.parent || !nodeToDelete.parent.parent) {
            throw ("cannot delete root");
        }
        if (!nodeToDelete.parent.parent.parent) {
            throw ("cannot delete direct child of root");
        }

        let p = nodeToDelete.parent;

        if ((p.children) && (p.children.length > 1)) {
            let i = p.children.indexOf(nodeToDelete);
            if (i !== -1) {
                p.children.splice(i, 1);
            }
        }
        if (p.children.length === 1) {
            let pp = p.parent;
            let cni = forester.getChildNodeIndex(pp, p);
            if ((cni < 0) || (cni > (pp.children.length - 1))) {
                throw ("this should never have happened, child node index = " + cni);
            }
            let x = p.children[0];
            let stated = function (v) {
                return typeof v === 'number' && isFinite(v);
            };
            // What the merged branch is made of, for a clock-model tree's
            // divergence: each piece's span in time and the rate its node
            // stated, kept on x (see divergenceOfPieces). A removed node's
            // rate goes on scaling its own piece, as the desktop keeps it.
            let showsDiv = statesDivergenceUnit(phy);
            let root = forester.getTreeRoot(phy);
            let pieceOf = function (n) {
                if (Array.isArray(n._pieces)) {
                    return n._pieces;
                }
                let span = null;
                if (stated(n._timeLength)) {
                    span = n._timeLength;
                } else if (showsDiv) {
                    let d = auspiceNodeDate(n);
                    let pd = auspiceNodeDate(n.parent);
                    span = (d !== null && pd !== null) ? timeGapOf(root)(d, pd) : null;
                } else if (stated(n.branch_length)) {
                    span = n.branch_length;
                }
                return span === null ? null : [{span: span, rate: clockRateOf(n)}];
            };
            let xs = pieceOf(x);
            let ps = pieceOf(p);
            let pieces = (xs && ps) ? xs.concat(ps) : null;
            // The two lengths add up, sign and all: a stated 0 is a length (a
            // truthiness test once turned two zeros into no length at all),
            // and a negative span stays a negative span, so that a node keeps
            // its date. One length stated and one not: the one stated.
            let nbl = (stated(x.branch_length) && stated(p.branch_length)) ? (x.branch_length + p.branch_length)
                : (stated(x.branch_length) ? x.branch_length : (stated(p.branch_length) ? p.branch_length : undefined));
            // The length kept for the Time | Div switch goes with the branch
            // the same way: x now spans, in time, what it and the node removed
            // above it spanned. Left as it was, x came back from Div short by
            // its parent's length (0.65 years on a real Nextstrain build). A
            // length not kept for either leaves x with none.
            if (Object.prototype.hasOwnProperty.call(x, '_timeLength')
                || Object.prototype.hasOwnProperty.call(p, '_timeLength')) {
                x._timeLength = (stated(x._timeLength) && stated(p._timeLength))
                    ? (x._timeLength + p._timeLength) : undefined;
            }
            x.parent = pp;
            pp.children[cni] = x;
            x.branch_length = nbl;
            if (pieces) {
                x._pieces = pieces;
            } else {
                delete x._pieces;
            }
            // On a captured clock-model tree the cumulative divergence of x,
            // and of everything under it, moves by what the merged branch's
            // divergence is now against what x's was.
            if (pieces && stated(x._divergence) && stated(pp._divergence)) {
                let own = clockRateOf(x);
                if (own !== null) {
                    let shift = (pp._divergence + divergenceOfPieces(pieces, own)) - x._divergence;
                    if (shift !== 0) {
                        forester.preOrderTraversalAll(x, function (n) {
                            n._divergence += shift;
                        });
                    }
                }
            }
        }

    };

    // The divergence along a branch made of pieces (see forester.deleteSubtree):
    // each piece's span, where it is positive, times the rate its node stated,
    // or the surviving node's where it stated none. Clamped piece by piece,
    // as each was when it was a branch of its own, so that deleting a node
    // moves nobody in Div.
    function divergenceOfPieces(pieces, ownRate) {
        let sum = 0;
        for (let i = 0; i < pieces.length; ++i) {
            let r = (pieces[i].rate === null || pieces[i].rate === undefined) ? ownRate : pieces[i].rate;
            sum += Math.max(0, pieces[i].span) * r;
        }
        return sum;
    }

    /**
     * To re-root a tree object.
     *
     * @param phy - The tree to be re-rooted.
     * @param node - The node on where to place the new root (on its parent branch).
     * @param branchLength - The branch length to use if new root is not placed in the middle (if
     * non-negative).
     */
    forester.reRoot = function (phy, node, branchLength) {
        if (!phy) {
            throw ("cannot re-root null tree");
        }
        if (!node) {
            throw ("cannot re-root on null node");
        }
        // no position: the middle of the branch. 0 is a position -- the root
        // right at the node, where MAD rooting can put it.
        if (typeof branchLength !== 'number' || isNaN(branchLength)) {
            branchLength = -1;
        }
        if (forester.isString(node)) {
            let nodes = forester.findByNodeName(phy, node);
            if (nodes.length > 1) {
                throw ("node name '" + node + "' is not unique");
            } else if (nodes.length < 1) {
                throw ("node name '" + node + "' is not found");
            }
            node = nodes[0];
        }

        phy.rooted = true;
        let root = forester.getTreeRoot(phy);

        if (!node.parent || !node.parent.parent) {
            //do noting
        } else if (!node.parent.parent.parent) {
            if ((node.parent.children.length === 2) && (branchLength >= 0)) {
                let d = node.parent.children[0].branch_length + node.parent.children[1].branch_length;
                let other;
                if (node.parent.children[0] === node) {
                    other = node.parent.children[1];
                } else {
                    other = node.parent.children[0];
                }
                node.branch_length = branchLength;
                let dm = d - branchLength;
                if (dm >= 0) {
                    other.branch_length = dm;
                } else {
                    other.branch_length = 0;
                }
            }
            if (node.parent.children.length > 2) {
                let index = forester.getChildNodeIndex(node.parent, node);
                let dn = node.branch_length;
                let prev_root = root;
                prev_root.children.splice(index, 1);
                let nr = {};
                nr.children = [];
                forester.setChildNode(nr, 0, node);
                forester.setChildNode(nr, 1, prev_root);

                forester.copyBranchData(node, prev_root);

                phy.children[0] = nr;
                nr.parent = phy;
                if (branchLength >= 0) {
                    node.branch_length = branchLength;
                    let dnmp = dn - branchLength;
                    if (dnmp >= 0) {
                        prev_root.branch_length = dnmp;
                    } else {
                        prev_root.branch_length = 0;
                    }
                } else {
                    if (dn >= 0) {
                        let dn2 = dn / 2.0;
                        node.branch_length = dn2;
                        prev_root.branch_length = dn2;
                    }
                }
            }
        } else {
            let a = node;
            let new_root = {};
            let distance1;
            let distance2 = 0.0;
            let branch_data_1;
            let branch_data_2 = null;
            let b = a.parent;
            let c = b.parent;

            new_root.children = [];
            forester.setChildNode(new_root, 0, a);
            forester.setChildNode(new_root, 1, b);

            distance1 = c.branch_length;

            branch_data_1 = forester.getBranchData(c);

            c.branch_length = b.branch_length;

            forester.copyBranchData(b, c);
            forester.copyBranchData(a, b);

            // New root is always placed in the middle of the branch:
            if (!a.branch_length) {
                b.branch_length = undefined;
            } else {
                if (branchLength >= 0.0) {
                    let diff = a.branch_length - branchLength;
                    a.branch_length = branchLength;
                    b.branch_length = (diff >= 0.0 ? diff : 0.0);
                } else {
                    let d2 = a.branch_length / 2.0;
                    a.branch_length = d2;
                    b.branch_length = d2;
                }
            }
            setChildNodeOnly(b, forester.getChildNodeIndex(b, a), c);
            // moving to the old root, swapping references:
            while (c.parent.parent) {
                a = b;
                b = c;
                c = c.parent;
                setChildNodeOnly(b, forester.getChildNodeIndex(b, a), c);
                b.parent = a;
                distance2 = c.branch_length;
                branch_data_2 = forester.getBranchData(c);
                c.branch_length = distance1;
                forester.setBranchData(c, branch_data_1);
                distance1 = distance2;
                branch_data_1 = branch_data_2;
            }
            // removing the old root:
            if (c.children.length === 2) {
                let node2 = c.children[1 - forester.getChildNodeIndex(c, b)];
                node2.parent = b;
                if ((!c.branch_length) && (!node2.branch_length)) {
                    node2.branch_length = undefined;
                } else {
                    node2.branch_length = (c.branch_length >= 0.0 ? c.branch_length : 0.0) + (node2.branch_length >= 0.0 ? node2.branch_length : 0.0);
                }
                let cbd = forester.getBranchData(c);
                if (cbd) {
                    forester.setBranchData(node2, cbd);
                }
                let l = b.children.length;
                for (let i = 0; i < l; ++i) {
                    if (b.children[i] === c) {
                        setChildNodeOnly(b, i, node2);
                        break;
                    }
                }
            } else {
                c.parent = b;
                forester.removeChildNode(c, forester.getChildNodeIndex(c, b));
            }
            phy.children[0] = new_root;
            new_root.parent = phy;
            forester.addParents(phy);
        }

        function setChildNodeOnly(parentNode, i, node) {
            if (parentNode.children.length <= i) {
                parentNode.children.push(node);
            } else {
                parentNode.children[i] = node;
            }
        }
    };

    forester.midpointRoot = function (phy) {
        let root = forester.getTreeRoot(phy);
        let extNodes = forester.getAllExternalNodes(root);
        if ((extNodes.length < 2) || (forester.calcMaxBranchLength(root) <= 0)) {
            return;
        }
        let counter = 0;
        let totalNodes = forester.getAllNodes(phy).length;
        while (true) {
            if (++counter > (totalNodes + 1)) {
                throw('this should not have happened: midpoint rooting does not converge');
            }
            let a = null;
            let da = 0;
            let db = 0;
            let cl = forester.getTreeRoot(phy).children.length;
            for (let i = 0; i < cl; ++i) {
                let f = forester.getFurthestDescendant(forester.getTreeRoot(phy).children[i]);
                let df = forester.getDistance(f, forester.getTreeRoot(phy));
                if (df > 0) {
                    if (df > da) {
                        db = da;
                        da = df;
                        a = f;
                    } else if (df > db) {
                        db = df;
                    }
                }
            }
            let diff = da - db;
            if (diff < 0.0001) {
                break;
            }
            let x = da - (diff / 2.0);
            while ((x > a.branch_length) && a.parent) {
                x -= (a.branch_length > 0 ? a.branch_length : 0);
                a = a.parent;
            }
            forester.reRoot(phy, a, x);
        }
    };

    /** The confidence type of the per-branch values madRoot records. */
    forester.MAD_CONFIDENCE_TYPE = 'MAD';

    const MAD_EPSILON = 1e-9;
    // Tip pairs closer than this share of the tree's diameter are left out of
    // every sum, like pairs at distance 0. Their 1/d^2 terms (tips 1e-8 apart,
    // as FastTree's 5e-9 "zero" branches make them, reach 1e16) cancel
    // catastrophically in the subtractions below and swamp every other pair:
    // MAD then answered differently from every rooting, and re-rooting moved
    // the root back and forth. The diameter does not depend on the rooting, so
    // neither does the cut. Joint with the desktop (Christian, 2026-09-14).
    const MAD_NEAR_FRACTION = 1e-5;

    /**
     * Roots the tree by Minimal Ancestor Deviation (Tria, Landan & Dagan,
     * Nature Ecology & Evolution 1, 0193, 2017; doi:10.1038/s41559-017-0193),
     * ported from the desktop's PhylogenyMethods.madRoot.
     *
     * Under a (relaxed) clock the ancestor of two tips is equidistant from
     * both; the pair (i,j) with ancestor a deviates by |2*dist(a,i)/dist(i,j) - 1|.
     * For every branch the root position minimizing the summed squared
     * deviation of all tip pairs is found analytically, and the branch and
     * position with the smallest total become the root.
     *
     * Every internal branch gets a confidence of type 'MAD': the root-mean-
     * square deviation were the root placed on that branch -- low is good,
     * and the root's branch carries the smallest. Pendant branches get none,
     * and MAD values from an earlier run are replaced.
     *
     * Tip pairs closer than 1e-5 of the tree's diameter (identical sequences,
     * FastTree's 5e-9 "zero" branches) are left out of the deviation sums:
     * their deviation measures noise, and their terms would swamp the
     * precision of all the others.
     *
     * A no-op for fewer than three tips or a tree without branch lengths.
     * O(n^2) time and O(n) memory: the desktop fills an n x n distance matrix
     * only to sum its columns (1.4 GB at 13,000 tips), so the column sums are
     * added up here as the pairs are met, and a subtree's tips are a range of
     * tip numbers rather than a list.
     *
     * @param phy the tree
     * @returns {boolean} whether the tree was re-rooted
     */
    forester.madRoot = function (phy) {
        let root = forester.getTreeRoot(phy);
        if (!root) {
            return false;
        }
        let t = madTraversal(root);
        let pre = t.pre;
        let kids = t.kids;
        let post = t.post;
        let m = pre.length;
        let tipNo = new Int32Array(m).fill(-1);
        let tipPos = [];
        for (let k = 0; k < m; ++k) {
            if (kids[k].length === 0) {
                tipNo[k] = tipPos.length;
                tipPos.push(k);
            }
        }
        let n = tipPos.length;
        if (n < 3) {
            return false;
        }
        // depth (distance from the current root) of every node
        let depth = new Float64Array(m);
        let maxDepth = 0;
        for (let k = 1; k < m; ++k) {
            depth[k] = depth[t.parentOf[k]] + madLength(pre[k]);
            maxDepth = Math.max(maxDepth, depth[k]);
        }
        if (maxDepth <= 0) {
            return false;   // no usable branch lengths
        }
        // the diameter, the longest tip-to-tip path: at every node, its two
        // longest paths down through different children
        let longestDown = new Float64Array(m);
        let diameter = 0;
        for (let q = 0; q < m; ++q) {
            let k = post[q];
            let first = 0;
            let second = 0;
            for (let c = 0; c < kids[k].length; ++c) {
                let path = longestDown[kids[k][c]] + madLength(pre[kids[k][c]]);
                if (path > first) {
                    second = first;
                    first = path;
                } else if (path > second) {
                    second = path;
                }
            }
            longestDown[k] = first;
            diameter = Math.max(diameter, first + second);
        }
        let nearEps = Math.max(MAD_EPSILON, MAD_NEAR_FRACTION * diameter);
        let tipDepth = new Float64Array(n);
        for (let i = 0; i < n; ++i) {
            tipDepth[i] = depth[tipPos[i]];
        }
        // Pass 1 (post-order): the within-subtree deviation sums, and the
        // column sums sum_i 1/d^2 and sum_i 1/d of every tip. For a pair whose
        // common ancestor sits at depth dm, d = depth[i] + depth[j] - 2*dm and
        // the deviation is 2*(depth[i]-dm)/d - 1.
        let lo = new Int32Array(m);        // a subtree's tips are the numbers lo..hi-1
        let hi = new Int32Array(m);
        let down = new Float64Array(m);    // sum dev^2 over pairs with their ancestor inside the subtree
        let w0 = new Float64Array(m);      // sum_{i!=j in subtree} 1/d^2                 (ordered)
        let w1 = new Float64Array(m);      // sum_{i!=j in subtree} (2*depth[j]/d^2 - 1/d) (j second)
        let w2 = new Float64Array(m);      // sum_{i!=j in subtree} (2*depth[j]/d - 1)^2   (j second)
        let col0 = new Float64Array(n);
        let colInv = new Float64Array(n);
        let partners = new Int32Array(n);   // the tips each tip is counted against (d > nearEps)
        for (let q = 0; q < m; ++q) {
            let k = post[q];
            let ch = kids[k];
            if (ch.length === 0) {
                lo[k] = tipNo[k];
                hi[k] = tipNo[k] + 1;
                continue;
            }
            let dm = depth[k];
            let dwn = 0, sw0 = 0, sw1 = 0, sw2 = 0;
            for (let c = 0; c < ch.length; ++c) {
                dwn += down[ch[c]];
                sw0 += w0[ch[c]];
                sw1 += w1[ch[c]];
                sw2 += w2[ch[c]];
            }
            // the pairs whose ancestor is this node: one tip from each of two children
            for (let a = 0; a < ch.length; ++a) {
                for (let b = a + 1; b < ch.length; ++b) {
                    for (let i = lo[ch[a]]; i < hi[ch[a]]; ++i) {
                        let di = tipDepth[i];
                        for (let j = lo[ch[b]]; j < hi[ch[b]]; ++j) {
                            let dj = tipDepth[j];
                            let dij = (di - dm) + (dj - dm);
                            if (dij > nearEps) {
                                let inv = 1.0 / dij;
                                let inv2 = inv * inv;
                                let dev = (2.0 * (di - dm) * inv) - 1.0;
                                dwn += dev * dev;
                                sw0 += 2.0 * inv2;   // both orderings
                                sw1 += (2.0 * (di + dj) * inv2) - (2.0 * inv);
                                let gi = (2.0 * di * inv) - 1.0;
                                let gj = (2.0 * dj * inv) - 1.0;
                                sw2 += (gi * gi) + (gj * gj);
                                col0[i] += inv2;
                                col0[j] += inv2;
                                colInv[i] += inv;
                                colInv[j] += inv;
                                ++partners[i];
                                ++partners[j];
                            }
                        }
                    }
                }
            }
            lo[k] = lo[ch[0]];
            hi[k] = hi[ch[ch.length - 1]];
            down[k] = dwn;
            w0[k] = sw0;
            w1[k] = sw1;
            w2[k] = sw2;
        }
        // Pass 2 (post-order): per-subtree "all-i" sums a0,a1,a2; the cross
        // sums between a subtree and its complement are then b_k = a_k - w_k.
        let a0 = new Float64Array(m);
        let a1 = new Float64Array(m);
        let a2 = new Float64Array(m);
        let b0 = new Float64Array(m);
        let b1 = new Float64Array(m);
        let b2 = new Float64Array(m);
        for (let q = 0; q < m; ++q) {
            let k = post[q];
            let s0 = 0, s1 = 0, s2 = 0;
            if (kids[k].length === 0) {
                let j = tipNo[k];
                s0 = col0[j];
                s1 = (2.0 * tipDepth[j] * col0[j]) - colInv[j];
                // (2*depth/d - 1)^2 expands to 4*depth^2/d^2 - 4*depth/d + 1: the
                // 1 is owed once per pair actually summed. The desktop adds n - 1,
                // which also counts the pairs at distance 0 (identical sequences)
                // that every other sum skips.
                s2 = (4.0 * tipDepth[j] * tipDepth[j] * col0[j]) - (4.0 * tipDepth[j] * colInv[j]) + partners[j];
            } else {
                for (let c = 0; c < kids[k].length; ++c) {
                    s0 += a0[kids[k][c]];
                    s1 += a1[kids[k][c]];
                    s2 += a2[kids[k][c]];
                }
            }
            a0[k] = s0;
            a1[k] = s1;
            a2[k] = s2;
            b0[k] = s0 - w0[k];
            b1[k] = s1 - w1[k];
            b2[k] = s2 - w2[k];
        }
        // Pass 3 (pre-order): up[k] = the deviation sum of the pairs whose
        // ancestor lies outside subtree k, by the rerooting recursion, so
        // every branch's total is O(1).
        let up = new Float64Array(m);
        let atParent = new Float64Array(m);   // a branch's cross deviation with the root at its parent
        for (let k = 0; k < m; ++k) {
            let ch = kids[k];
            if (ch.length === 0) {
                continue;
            }
            let sumDown = 0;
            let sumAtParent = 0;
            for (let c = 0; c < ch.length; ++c) {
                let x = ch[c];
                sumDown += down[x];
                atParent[x] = madCross(depth[x], b0[x], b1[x], b2[x], madLength(pre[x]));
                sumAtParent += atParent[x];
            }
            // cross deviation among all the groups meeting here: the child subtrees and the complement
            let own = (k === 0) ? 0.0 : madCross(depth[k], b0[k], b1[k], b2[k], 0.0);
            let crossAmong = (sumAtParent + own) / 2.0;
            for (let c = 0; c < ch.length; ++c) {
                let x = ch[c];
                up[x] = up[k] + (sumDown - down[x]) + (crossAmong - atParent[x]);
            }
        }
        // The branch and position with the smallest total deviation, and every
        // branch's smallest MAD value keyed by the tips on its far side, so
        // the value finds its branch again after the re-root has turned
        // parents into children. The first minimum in post-order wins, as on
        // the desktop.
        let hash = madTipHashes(n);
        let nPairs = (n * (n - 1.0)) / 2.0;
        let madByKey = new Map();
        let bestSsd = Infinity;
        let best = -1;
        let bestX = 0;
        for (let q = 0; q < m; ++q) {
            let c = post[q];
            if (c === 0) {
                continue;
            }
            let length = madLength(pre[c]);
            // the optimal root position, as a distance from c toward its parent, kept on the branch
            let x = (b0[c] > MAD_EPSILON) ? (depth[c] - (b1[c] / (2.0 * b0[c]))) : 0.0;
            if (x < 0) {
                x = 0;
            } else if (x > length) {
                x = length;
            }
            let ssd = madCross(depth[c], b0[c], b1[c], b2[c], x) + down[c] + up[c];
            // a sum of squares; clamp a tiny negative left by cancellation, so sqrt is never NaN
            let mad = Math.sqrt(Math.max(0.0, ssd) / nPairs);
            let key = hash.key(hi[c] - lo[c], hash.xa[hi[c]] ^ hash.xa[lo[c]], hash.xb[hi[c]] ^ hash.xb[lo[c]], lo[c] === 0);
            let prev = madByKey.get(key);
            if (prev === undefined || mad < prev) {
                madByKey.set(key, mad);   // the two halves of a bifurcating root share a key
            }
            if (ssd < bestSsd) {
                bestSsd = ssd;
                best = c;
                bestX = x;
            }
        }
        if (best < 0) {
            return false;
        }
        let tipNumber = new Map();
        for (let i = 0; i < n; ++i) {
            tipNumber.set(pre[tipPos[i]], i);
        }
        forester.removeMadConfidences(phy);
        forester.reRoot(phy, pre[best], bestX);
        // annotate the internal branches of the re-rooted tree
        let r = madTraversal(forester.getTreeRoot(phy));
        let count = new Int32Array(r.pre.length);
        let xa = new Int32Array(r.pre.length);
        let xb = new Int32Array(r.pre.length);
        let hasFirst = new Uint8Array(r.pre.length);
        for (let q = 0; q < r.post.length; ++q) {
            let k = r.post[q];
            let ch = r.kids[k];
            if (ch.length === 0) {
                let i = tipNumber.get(r.pre[k]);
                count[k] = 1;
                xa[k] = hash.xa[i + 1] ^ hash.xa[i];
                xb[k] = hash.xb[i + 1] ^ hash.xb[i];
                hasFirst[k] = (i === 0) ? 1 : 0;
                continue;
            }
            for (let c = 0; c < ch.length; ++c) {
                count[k] += count[ch[c]];
                xa[k] ^= xa[ch[c]];
                xb[k] ^= xb[ch[c]];
                hasFirst[k] |= hasFirst[ch[c]];
            }
            if (k !== 0) {
                let mad = madByKey.get(hash.key(count[k], xa[k], xb[k], hasFirst[k] === 1));
                if (mad !== undefined) {
                    let kept = r.pre[k].confidences || [];
                    r.pre[k].confidences = kept.concat([{value: mad, type: forester.MAD_CONFIDENCE_TYPE}]);
                }
            }
        }
        return true;
    };

    /**
     * Removes every 'MAD' confidence (from madRoot) and keeps all others: a
     * tree rooted any other way no longer has the rooting those values rate.
     *
     * @param phy the tree
     */
    forester.removeMadConfidences = function (phy) {
        let root = forester.getTreeRoot(phy);
        if (!root) {
            return;
        }
        madTraversal(root).pre.forEach(function (node) {
            if (node.confidences && node.confidences.some(function (c) { return c.type === forester.MAD_CONFIDENCE_TYPE; })) {
                // a new array: re-rooting can leave two branches sharing one
                let kept = node.confidences.filter(function (c) { return c.type !== forester.MAD_CONFIDENCE_TYPE; });
                if (kept.length > 0) {
                    node.confidences = kept;
                } else {
                    delete node.confidences;
                }
            }
        });
    };

    // ---- representative tips ------------------------------------------------
    //
    // Tree-based dereplication, the desktop's "Select Representative Tips"
    // (RepresentativeTipSelector) ported step for step, the order of its sums
    // and its 1e-9 tolerance included: test/fixtures/rep-contract.tsv holds
    // the desktop's own results and extractions (RepContract.java), and the
    // tests hold ours to them.
    //
    // Tips are grouped into the maximal clades whose diameter -- the largest
    // patristic distance between two of their tips -- is at most a cutoff, and
    // each group keeps one representative. A clade's diameter only grows
    // rootward, so the groups are one cut through the tree. Without branch
    // lengths the distance is topological, one unit per edge.

    forester.REPRESENTATIVE_MEDOID = 'medoid';
    forester.REPRESENTATIVE_LONGEST_BRANCH = 'longest_branch';

    const REPRESENTATIVE_EPS = 1e-9;

    function repIsTip(n) {
        return !n.children || n.children.length === 0;
    }

    // below `top`, parents before children, children in their order
    function repPreorder(top) {
        let out = [];
        let stack = [top];
        while (stack.length > 0) {
            let n = stack.pop();
            out.push(n);
            if (n.children) {
                for (let i = n.children.length - 1; i >= 0; --i) {
                    stack.push(n.children[i]);
                }
            }
        }
        return out;
    }

    // the branch above a node, a missing or negative length counting 0
    function repEdge(n, topological) {
        if (topological) {
            return 1;
        }
        let d = n.branch_length;
        return (typeof d === 'number' && d > 0) ? d : 0;
    }

    /**
     * Whether any branch of the tree carries a length (0 included). Without
     * one, representative tips are chosen by topological distance, and a
     * distance cutoff means nothing.
     *
     * @param phy the tree
     * @returns {boolean}
     */
    forester.hasUsableBranchLengths = function (phy) {
        let root = forester.getTreeRoot(phy);
        return repPreorder(root).some(function (n) {
            return n !== root && typeof n.branch_length === 'number';
        });
    };

    // every node's diameter, in one pass from the tips up
    function repDiameters(pre, topological) {
        let height = new Map();
        let diameter = new Map();
        for (let i = pre.length - 1; i >= 0; --i) {
            let n = pre[i];
            if (repIsTip(n)) {
                height.set(n, 0);
                diameter.set(n, 0);
                continue;
            }
            let best1 = 0;   // the longest reach down through one child
            let best2 = 0;   // through another
            let maxChildDiameter = 0;
            for (let c = 0; c < n.children.length; ++c) {
                let child = n.children[c];
                let reach = height.get(child) + repEdge(child, topological);
                if (reach >= best1) {
                    best2 = best1;
                    best1 = reach;
                } else if (reach > best2) {
                    best2 = reach;
                }
                maxChildDiameter = Math.max(maxChildDiameter, diameter.get(child));
            }
            height.set(n, best1);
            diameter.set(n, Math.max(maxChildDiameter, n.children.length >= 2 ? best1 + best2 : 0));
        }
        return diameter;
    }

    // the groups' clades: the highest nodes whose diameter is within the cutoff
    function repGroupRoots(root, diameter, cutoff) {
        let roots = [];
        let stack = [root];
        while (stack.length > 0) {
            let n = stack.pop();
            if (repIsTip(n) || diameter.get(n) <= cutoff + REPRESENTATIVE_EPS) {
                roots.push(n);
            } else {
                for (let i = 0; i < n.children.length; ++i) {
                    stack.push(n.children[i]);
                }
            }
        }
        return roots;
    }

    // The cutoff whose group count comes closest to the target: the count
    // changes only at clade diameters and never grows with the cutoff, so a
    // binary search finds the smallest diameter giving at most the target,
    // and its neighbour below gives more. A tie keeps more representatives;
    // -1 stands for "below every diameter", every tip its own group.
    function repCutoffForTarget(pre, root, diameter, target, tipCount) {
        let values = [];
        pre.forEach(function (n) {
            if (!repIsTip(n)) {
                values.push(diameter.get(n));
            }
        });
        values.sort(function (a, b) { return a - b; });
        let cand = [];
        values.forEach(function (v) {
            if (cand.length === 0 || v > cand[cand.length - 1] + REPRESENTATIVE_EPS) {
                cand.push(v);
            }
        });
        let count = function (cutoff) {
            return repGroupRoots(root, diameter, cutoff).length;
        };
        let lo = 0;
        let hi = cand.length - 1;
        let boundary = cand.length - 1;
        while (lo <= hi) {
            let mid = (lo + hi) >>> 1;
            if (count(cand[mid]) <= target) {
                boundary = mid;
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }
        let tHigh = cand[boundary];
        let cHigh = count(tHigh);
        let tLow = boundary > 0 ? cand[boundary - 1] : -1;
        let cLow = boundary > 0 ? count(tLow) : tipCount;
        return (Math.abs(cLow - target) <= Math.abs(cHigh - target)) ? tLow : tHigh;
    }

    function repLongestBranch(members, topological) {
        let best = members[0];
        let bestLength = repEdge(best, topological);
        for (let i = 1; i < members.length; ++i) {
            let length = repEdge(members[i], topological);
            if (length > bestLength + REPRESENTATIVE_EPS) {
                best = members[i];
                bestLength = length;
            }
        }
        return best;
    }

    // The tip with the smallest summed distance to its group-mates, by the
    // rerooting sum-of-distances recursion (linear, never all pairs): `down`
    // sums the distances to the tips below a node, `up` to all the others.
    function repMedoid(pre, members, topological) {
        let tipsBelow = new Map();
        let down = new Map();
        for (let i = pre.length - 1; i >= 0; --i) {
            let n = pre[i];
            if (repIsTip(n)) {
                tipsBelow.set(n, 1);
                down.set(n, 0);
                continue;
            }
            let s = 0;
            let d = 0;
            for (let c = 0; c < n.children.length; ++c) {
                let child = n.children[c];
                let e = repEdge(child, topological);
                let cs = tipsBelow.get(child);
                s += cs;
                d += down.get(child) + (e * cs);
            }
            tipsBelow.set(n, s);
            down.set(n, d);
        }
        let total = tipsBelow.get(pre[0]);
        let up = new Map();
        up.set(pre[0], 0);
        pre.forEach(function (n) {
            if (repIsTip(n)) {
                return;
            }
            let un = up.get(n);
            let dn = down.get(n);
            for (let c = 0; c < n.children.length; ++c) {
                let child = n.children[c];
                let e = repEdge(child, topological);
                let cs = tipsBelow.get(child);
                up.set(child, un + dn - down.get(child) - (e * cs) + (e * (total - cs)));
            }
        });
        let best = members[0];
        let bestTotal = up.get(best);
        for (let i = 1; i < members.length; ++i) {
            let t = up.get(members[i]);
            if (t < bestTotal - REPRESENTATIVE_EPS) {
                best = members[i];
                bestTotal = t;
            }
        }
        return best;
    }

    /**
     * Chooses representative tips: groups the tips into the maximal clades
     * whose members are all within a distance of each other, and keeps one
     * tip per group -- the desktop's Select Representative Tips.
     *
     * By `cutoff`, no two tips of a group are farther apart than it. By
     * `target`, the cutoff is the one whose group count comes closest to that
     * many (a tie keeps more); the count made is reported, since only certain
     * counts are possible. Without branch lengths the distance counts edges.
     *
     * A protected tip is never dropped: it stands in for its group's
     * representative, and a group with several keeps them all -- which can
     * keep more tips than the target.
     *
     * The tree is not changed.
     *
     * @param phy the tree
     * @param options {cutoff: number} or {target: integer}, with
     *        pick: forester.REPRESENTATIVE_MEDOID (default, the most central
     *        tip) or forester.REPRESENTATIVE_LONGEST_BRANCH (the most
     *        divergent), and protectedTips: tips (nodes) never to drop
     * @returns {{groups: Array, keptTips: Array, keptCount: number,
     *          protectedKeptCount: number, tipCount: number,
     *          effectiveCutoff: number, topological: boolean, pick: string,
     *          requestedTarget: number, summary: string}}
     *          groups: {clade, members, kept}, in tree order of their first
     *          kept tip; requestedTarget is -1 for a cutoff
     */
    forester.selectRepresentativeTips = function (phy, options) {
        let opts = options || {};
        let root = phy ? forester.getTreeRoot(phy) : null;
        if (!root) {
            throw new Error('the tree is null or empty');
        }
        let byCutoff = opts.cutoff !== undefined;
        if (byCutoff === (opts.target !== undefined)) {
            throw new Error('give either a cutoff or a target');
        }
        if (byCutoff && (typeof opts.cutoff !== 'number' || !isFinite(opts.cutoff) || opts.cutoff < 0)) {
            throw new Error('cutoff must be a finite, non-negative number');
        }
        if (!byCutoff && !(Number.isInteger(opts.target) && opts.target >= 1)) {
            throw new Error('target number of representatives must be at least 1');
        }
        let pick = opts.pick === undefined ? forester.REPRESENTATIVE_MEDOID : opts.pick;
        if (pick !== forester.REPRESENTATIVE_MEDOID && pick !== forester.REPRESENTATIVE_LONGEST_BRANCH) {
            throw new Error('unknown representative pick: ' + pick);
        }
        let protectedTips = new Set(opts.protectedTips || []);

        let pre = repPreorder(root);
        let order = new Map();
        pre.forEach(function (n, i) {
            order.set(n, i);
        });
        let tips = pre.filter(repIsTip);
        let topological = !forester.hasUsableBranchLengths(phy);
        let diameter = repDiameters(pre, topological);
        let groupRoots;
        let effectiveCutoff;
        if (byCutoff) {
            groupRoots = repGroupRoots(root, diameter, opts.cutoff);
            effectiveCutoff = opts.cutoff;
        } else if (opts.target >= tips.length) {
            groupRoots = tips;
            effectiveCutoff = 0;
        } else if (opts.target <= 1) {
            groupRoots = [root];
            effectiveCutoff = diameter.get(root);
        } else {
            let cutoff = repCutoffForTarget(pre, root, diameter, opts.target, tips.length);
            groupRoots = repGroupRoots(root, diameter, cutoff);
            effectiveCutoff = Math.max(0, cutoff);
        }

        let protectedKeptCount = 0;
        let groups = groupRoots.map(function (clade) {
            let sub = repPreorder(clade);
            let members = sub.filter(repIsTip);
            let kept = members.filter(function (m) {
                return protectedTips.has(m);
            });
            if (kept.length > 0) {
                protectedKeptCount += kept.length;
            } else if (members.length === 1) {
                kept = [members[0]];
            } else if (pick === forester.REPRESENTATIVE_LONGEST_BRANCH) {
                kept = [repLongestBranch(members, topological)];
            } else {
                kept = [repMedoid(sub, members, topological)];
            }
            return {clade: clade, members: members, kept: kept};
        });
        groups.sort(function (a, b) {
            return order.get(a.kept[0]) - order.get(b.kept[0]);
        });
        let keptTips = [];
        groups.forEach(function (g) {
            g.kept.forEach(function (k) {
                keptTips.push(k);
            });
        });
        let result = {
            groups: groups,
            keptTips: keptTips,
            keptCount: keptTips.length,
            protectedKeptCount: protectedKeptCount,
            tipCount: tips.length,
            effectiveCutoff: effectiveCutoff,
            topological: topological,
            pick: pick,
            requestedTarget: byCutoff ? -1 : opts.target
        };
        result.summary = representativeSummary(result);
        return result;
    };

    // Java's Double.toString, which the desktop's texts print numbers with:
    // the shortest digits that read back as the number (as JS's), but always
    // with a fraction ("1.0"), and in E notation outside [0.001, 10^7).
    function javaDoubleText(d) {
        if (d === 0) {
            return (1 / d < 0) ? '-0.0' : '0.0';
        }
        if (!isFinite(d)) {
            return String(d);
        }
        let a = Math.abs(d);
        if (a >= 1e-3 && a < 1e7) {
            let s = String(d);
            return s.indexOf('.') < 0 ? s + '.0' : s;
        }
        let e = d.toExponential();
        let i = e.indexOf('e');
        let mantissa = e.slice(0, i);
        return (mantissa.indexOf('.') < 0 ? mantissa + '.0' : mantissa) + 'E' + e.slice(i + 1).replace('+', '');
    }

    // a distance to five decimals, a whole number without its fraction
    function representativeDistanceText(d) {
        let r = Math.round(d * 1e5) / 1e5;
        return (r === Math.round(r)) ? String(r) : javaDoubleText(r);
    }

    function representativeSummary(result) {
        let groups = result.groups.length;
        let s = 'Grouped ' + result.tipCount + (result.tipCount === 1 ? ' tip' : ' tips')
            + ' into ' + groups + (groups === 1 ? ' group.' : ' groups.');
        if (result.requestedTarget > 0 && groups !== result.requestedTarget) {
            s += ' (requested ' + result.requestedTarget + ')';
        }
        s += '\nEach group\'s tips are within a distance of ' + representativeDistanceText(result.effectiveCutoff)
            + ' of each other';
        if (result.topological) {
            s += ' (topological distance — the tree has no branch lengths)';
        }
        s += '.';
        if (result.protectedKeptCount > 0) {
            s += '\nKeeping ' + result.keptCount + (result.keptCount === 1 ? ' tip, including ' : ' tips, including ')
                + result.protectedKeptCount
                + (result.protectedKeptCount === 1 ? ' selected tip protected from removal.'
                    : ' selected tips protected from removal.');
        }
        s += '\nRepresentative per group: '
            + (result.pick === forester.REPRESENTATIVE_LONGEST_BRANCH ? 'most divergent (longest branch)'
                : 'most central (medoid)') + '.';
        return s;
    }

    // a copy of a node's own data (never its children or parent link)
    function repCopyData(v) {
        if (Array.isArray(v)) {
            return v.map(repCopyData);
        }
        if (v !== null && typeof v === 'object') {
            let o = {};
            Object.keys(v).forEach(function (k) {
                if (k !== 'parent' && k !== 'children') {
                    o[k] = repCopyData(v[k]);
                }
            });
            return o;
        }
        return v;
    }

    /**
     * A copy of the tree holding only the given tips, pruned as the desktop
     * prunes (Phylogeny.deleteSubtree): a node left with one child is
     * replaced by that child, whose branch gains the node's length (a missing
     * or negative length adds nothing; two of them leave the length missing);
     * a root left with one child is replaced by it. The new root keeps the
     * original root's own branch length (normally none), where the desktop's
     * depends on the order it deletes in. The tree is not changed.
     *
     * @param phy the tree
     * @param keep the tips to keep (nodes of phy), at least one
     * @returns the copy, with every node's data copied
     */
    forester.copyTreeKeepingTips = function (phy, keep) {
        let keepSet = new Set(keep);
        let top = phy.children && phy.children.length === 1 && !phy.parent ? phy : {children: [phy]};
        let copies = new Map();
        let copyTop = repCopyData(top === phy ? phy : {});
        copies.set(top, copyTop);
        let stack = [top];
        while (stack.length > 0) {
            let n = stack.pop();
            if (n.children) {
                copies.get(n).children = n.children.map(function (child) {
                    let cc = repCopyData(child);
                    copies.set(child, cc);
                    stack.push(child);
                    return cc;
                });
            }
        }
        let tips = repPreorder(top).filter(function (n) {
            return n !== top && repIsTip(n);
        });
        let dropped = tips.filter(function (n) {
            return !keepSet.has(n);
        });
        if (dropped.length === tips.length) {
            throw new Error('at least one tip must be kept');
        }
        let parentOf = new Map();
        stack = [copyTop];
        while (stack.length > 0) {
            let n = stack.pop();
            (n.children || []).forEach(function (child) {
                parentOf.set(child, n);
                stack.push(child);
            });
        }
        let add = function (a, b) {
            let okA = typeof a === 'number' && a >= 0;
            let okB = typeof b === 'number' && b >= 0;
            return (okA && okB) ? a + b : (okA ? a : (okB ? b : undefined));
        };
        dropped.forEach(function (tip) {
            let t = copies.get(tip);
            let p = parentOf.get(t);
            let i = p.children.indexOf(t);
            if (parentOf.get(p) === copyTop) {
                if (p.children.length === 2) {
                    let other = p.children[1 - i];
                    copyTop.children[0] = other;
                    parentOf.set(other, copyTop);
                } else {
                    p.children.splice(i, 1);
                }
            } else {
                let pp = parentOf.get(p);
                if (p.children.length === 2) {
                    let other = p.children[1 - i];
                    let length = add(p.branch_length, other.branch_length);
                    if (length === undefined) {
                        delete other.branch_length;
                    } else {
                        other.branch_length = length;
                    }
                    pp.children[pp.children.indexOf(p)] = other;
                    parentOf.set(other, pp);
                } else {
                    p.children.splice(i, 1);
                }
            }
            if (p.children.length === 0) {
                delete p.children;
            }
        });
        // The root keeps the original root's own branch length, normally
        // none. What the desktop's pruning leaves there depends on the order
        // it deletes tips in -- that is, on node ids -- so the same selection
        // gave 0.05 in one session and 0.25 in another (Christian, 2026-09-15).
        let rootLength = top.children[0].branch_length;
        if (typeof rootLength === 'number') {
            copyTop.children[0].branch_length = rootLength;
        } else {
            delete copyTop.children[0].branch_length;
        }
        return copyTop;
    };

    /**
     * Strips a file-type suffix -- a dot and 1 to 5 other characters, such as
     * .xml or .nexus -- from a tree name, as the desktop does.
     *
     * @param name
     * @returns {string|null}
     */
    forester.stripShortExtension = function (name) {
        return (name === null || name === undefined) ? null : String(name).replace(/\.[^.]{1,5}$/, '');
    };

    /**
     * The desktop's name for a tree of representative tips: the parent's name
     * without its file suffix, then the count -- mammals_233reps, _1rep --
     * or "tree" for an unnamed parent.
     *
     * @param parentName
     * @param count
     * @returns {string}
     */
    forester.representativeTreeName = function (parentName, count) {
        let stripped = forester.stripShortExtension(parentName);
        return (stripped ? stripped : 'tree') + '_' + count + (count === 1 ? 'rep' : 'reps');
    };

    /**
     * The desktop's provenance sentence for a tree of representative tips,
     * which it adds to the tree's description.
     *
     * @param byCutoff true for a cutoff, false for a target
     * @param cutoff
     * @param target
     * @param pick forester.REPRESENTATIVE_MEDOID or _LONGEST_BRANCH
     * @param count tips kept
     * @param parentName
     * @param parentTipCount
     * @returns {string}
     */
    forester.representativeTreeDescription = function (byCutoff, cutoff, target, pick, count, parentName, parentTipCount) {
        let pickText = pick === forester.REPRESENTATIVE_LONGEST_BRANCH ? 'longest-branch' : 'medoid';
        let algorithm = byCutoff
            ? 'distance-cutoff (maximum distance ' + javaDoubleText(cutoff) + ', ' + pickText + ' representative)'
            : 'target-count (target ' + target + ', ' + pickText + ' representative)';
        return 'Used the ' + algorithm + ' algorithm to select ' + count + ' representative '
            + (count === 1 ? 'tip' : 'tips') + ' from tree named "' + (parentName ? parentName : 'tree') + '" with '
            + parentTipCount + (parentTipCount === 1 ? ' tip.' : ' tips.');
    };

    /**
     * Whether a node carries data about the node itself, the kind a
     * re-rooting can take the meaning away from: a name, taxonomy, sequence
     * (a domain architecture lives there), events, distribution, date,
     * references, or a property about the node. Not data in this sense: the
     * branch above it -- length, support and MAD values, colour, width, a
     * property applying to the branch -- which re-rooting carries along,
     * visual styling (style: properties), the viewer's own aptx: properties,
     * and an empty taxonomy. The desktop's list is the same.
     *
     * @param node
     * @returns {boolean}
     */
    forester.nodeHasData = function (node) {
        let filled = function (x) {
            return x !== undefined && x !== null && x !== '' && !(Array.isArray(x) && x.length === 0);
        };
        if ((typeof node.name === 'string' && node.name.length > 0)
            || (node.taxonomies && node.taxonomies.some(function (t) {
                return t && Object.keys(t).some(function (key) { return filled(t[key]); });
            }))
            || (node.sequences && node.sequences.length > 0)
            || node.events
            || (node.distributions && node.distributions.length > 0)
            || node.date
            || (node.references && node.references.length > 0)) {
            return true;
        }
        return !!node.properties && node.properties.some(function (p) {
            return p.applies_to !== BRANCH_EVENT_APPLIES_TO
                && !(typeof p.ref === 'string' && (p.ref.indexOf('style:') === 0 || p.ref.indexOf('aptx:') === 0));
        });
    };

    /**
     * What a re-rooting would do to the data on internal nodes, worked out on
     * a bare copy so the tree itself is untouched: `annotated`, the internal
     * nodes carrying data (nodeHasData), and `changed`, those among them whose
     * clade -- the tips below -- the re-rooting changes. Those lie between
     * the old root and the new one (an old two-child root disappears); every
     * other node keeps its tips. Midpoint and MAD rooting find their root on
     * the copy first. Nothing is copied when no internal node carries data.
     *
     * @param phy the tree
     * @param method 'mad', 'midpoint', or 'node': the root on the branch above `node`
     * @param node the node, for 'node'
     * @returns {{annotated: Array, changed: Array}} nodes of `phy`
     */
    forester.cladesChangedByRerooting = function (phy, method, node) {
        let t = madTraversal(forester.getTreeRoot(phy));
        let annotated = [];
        for (let k = 0; k < t.pre.length; ++k) {
            if (t.kids[k].length > 0 && forester.nodeHasData(t.pre[k])) {
                annotated.push(t.pre[k]);
            }
        }
        if (annotated.length === 0) {
            return {annotated: annotated, changed: []};
        }
        // the copy: shape and branch lengths only, each copy knowing its original
        let copyOf = new Map();
        let tipNumber = new Map();
        for (let k = 0; k < t.pre.length; ++k) {
            let c = {branch_length: t.pre[k].branch_length, original: t.pre[k]};
            if (t.kids[k].length > 0) {
                c.children = [];
            } else {
                tipNumber.set(t.pre[k], tipNumber.size);
            }
            if (k > 0) {
                c.parent = copyOf.get(t.pre[t.parentOf[k]]);
                c.parent.children.push(c);
            }
            copyOf.set(t.pre[k], c);
        }
        let copy = {children: [copyOf.get(t.pre[0])]};
        copy.children[0].parent = copy;
        if (method === 'mad') {
            forester.madRoot(copy);
        } else if (method === 'midpoint') {
            forester.midpointRoot(copy);
        } else {
            forester.reRoot(copy, copyOf.get(node), -1);
        }
        // the tips below every node, before and after, as a count and two hashes
        let hash = madTipHashes(tipNumber.size);
        let cladeKeys = function (traversal, originalOf) {
            let count = new Int32Array(traversal.pre.length);
            let xa = new Int32Array(traversal.pre.length);
            let xb = new Int32Array(traversal.pre.length);
            let keys = new Map();
            for (let q = 0; q < traversal.post.length; ++q) {
                let k = traversal.post[q];
                let ch = traversal.kids[k];
                if (ch.length === 0) {
                    let i = tipNumber.get(originalOf(traversal.pre[k]));
                    count[k] = 1;
                    xa[k] = hash.xa[i + 1] ^ hash.xa[i];
                    xb[k] = hash.xb[i + 1] ^ hash.xb[i];
                    continue;
                }
                for (let c = 0; c < ch.length; ++c) {
                    count[k] += count[ch[c]];
                    xa[k] ^= xa[ch[c]];
                    xb[k] ^= xb[ch[c]];
                }
                let original = originalOf(traversal.pre[k]);
                if (original) {   // not the copy's new root node
                    keys.set(original, count[k] + ':' + xa[k] + ':' + xb[k]);
                }
            }
            return keys;
        };
        let before = cladeKeys(t, function (n) { return n; });
        let after = cladeKeys(madTraversal(forester.getTreeRoot(copy)), function (c) { return c.original; });
        return {
            annotated: annotated,
            changed: annotated.filter(function (n) { return after.get(n) !== before.get(n); })
        };
    };

    function madLength(node) {
        return node.branch_length > 0 ? node.branch_length : 0;
    }

    // The sum of squared cross-pair deviations between subtree c and its
    // complement, with the root at distance x from c toward its parent. With
    // K = 2*(x - depth[c]) a pair deviates by K/d + (2*depth[j]/d - 1), so the
    // sum is K^2*b0 + 2*K*b1 + b2.
    function madCross(depthC, b0, b1, b2, x) {
        let k = 2.0 * (x - depthC);
        return (k * k * b0) + (2.0 * k * b1) + b2;
    }

    // Pre-order (the root at 0, parents before children) and post-order,
    // children left to right, as node positions -- without recursion, since
    // a caterpillar tree nests as deep as it has tips.
    function madTraversal(root) {
        let pre = [];
        let parentOf = [];
        let stack = [root];
        let stackParent = [-1];
        while (stack.length > 0) {
            let node = stack.pop();
            let p = stackParent.pop();
            let k = pre.length;
            pre.push(node);
            parentOf.push(p);
            if (node.children) {
                for (let i = node.children.length - 1; i >= 0; --i) {
                    stack.push(node.children[i]);
                    stackParent.push(k);
                }
            }
        }
        let kids = pre.map(function () {
            return [];
        });
        for (let k = 1; k < pre.length; ++k) {
            kids[parentOf[k]].push(k);   // siblings come in left to right
        }
        // the mirrored pre-order, reversed, is the post-order left to right
        let post = [];
        let st = [0];
        while (st.length > 0) {
            let k = st.pop();
            post.push(k);
            for (let c = 0; c < kids[k].length; ++c) {
                st.push(kids[k][c]);
            }
        }
        post.reverse();
        return {pre: pre, parentOf: parentOf, kids: kids, post: post};
    }

    // A set of tips as a key that does not depend on the rooting: its size
    // and two 32-bit XOR hashes of its tips, taken on the side WITHOUT tip 0
    // so both sides of a branch give the same key. xa/xb are prefix XORs, so
    // the tips lo..hi-1 hash to xa[hi] ^ xa[lo].
    function madTipHashes(n) {
        let xa = new Int32Array(n + 1);
        let xb = new Int32Array(n + 1);
        let s = 0x2545f491;
        let next = function () {   // a fixed seed: the same tree always hashes the same
            s = (s + 0x9e3779b9) | 0;
            let z = s;
            z = Math.imul(z ^ (z >>> 16), 0x85ebca6b);
            z = Math.imul(z ^ (z >>> 13), 0xc2b2ae35);
            return z ^ (z >>> 16);
        };
        for (let i = 0; i < n; ++i) {
            xa[i + 1] = xa[i] ^ next();
            xb[i + 1] = xb[i] ^ next();
        }
        return {
            xa: xa,
            xb: xb,
            key: function (size, ha, hb, holdsFirst) {
                if (holdsFirst) {
                    size = n - size;
                    ha ^= xa[n];
                    hb ^= xb[n];
                }
                return size + ':' + ha + ':' + hb;
            }
        };
    }

    forester.getFurthestDescendant = function (node) {
        let children = forester.getAllExternalNodes(node);
        let farthest = null;
        let longest = -1000000;
        let l = children.length;
        for (let i = 0; i < l; ++i) {
            let dist = forester.getDistance(children[i], node);
            if (dist > longest) {
                farthest = children[i];
                longest = dist;
            }
        }
        return farthest;
    };

    /**
     * Calculates the distance between PhylogenyNodes n1 and n2.
     * PRECONDITION: n1 is a descendant of n2.
     *
     * @param n1 a descendant of n2
     * @param n2
     * @returns {number} distance between n1 and n2
     */
    forester.getDistance = function (n1, n2) {
        let d = 0.0;
        while (n1 !== n2) {
            if (n1.branch_length > 0.0) {
                d += n1.branch_length;
            }
            n1 = n1.parent;
        }
        return d;
    };

    forester.removeChildNode = function (parentNode, i) {
        if (!parentNode.children) {
            throw ("cannot remove the child node for a external node");
        }
        if ((i >= parentNode.children.length) || (i < 0)) {
            throw ("attempt to get child node " + i + " of a node with " + parentNode.children.length + " child nodes.");
        }
        parentNode.children[i].parent = undefined;
        parentNode.children.splice(i, 1);
    };


    /**
     * Inserts node node at the specified position i into the list of
     * child nodes of parentNode. This does not allow null slots in the list of child nodes:
     * If i is larger than the number of child nodes, node is just added to the
     * list, not placed at index i.
     */
    forester.setChildNode = function (parentNode, i, node) {
        node.parent = parentNode;
        if (parentNode.children.length <= i) {
            parentNode.children.push(node);
        } else {
            parentNode.children[i] = node;
        }
    };


    forester.getBranchData = function (node) {
        let branchData = null;
        if (node.width || node.color || node.confidences) {
            branchData = {};
            branchData.width = node.width;
            branchData.color = node.color;
            branchData.confidences = node.confidences;
        }
        return branchData;
    };


    forester.setBranchData = function (node, branchData) {
        if (branchData) {
            node.width = branchData.width;
            node.color = branchData.color;
            node.confidences = branchData.confidences;
        }
    };

    forester.copyBranchData = function (nodeFrom, nodeTo) {
        nodeTo.width = nodeFrom.width;
        nodeTo.color = nodeFrom.color;
        nodeTo.confidences = nodeFrom.confidences;
    };


    forester.getChildNodeIndex = function (parentNode, childNode) {
        if (!parentNode) {
            throw ("cannot get the child index for a root node");
        }
        let c = parentNode.children.length;
        for (let i = 0; i < c; ++i) {
            if (parentNode.children[i] === childNode) {
                return i;
            }
        }
        throw ("unexpected exception: Could not determine the child index for a node");
    };


    // ------------------------------------------------------------------
    // Automatic visualization candidates
    // ------------------------------------------------------------------
    //
    // Decides, from the tree alone, which of its elements are worth offering
    // as a Color, Color-range, or Shape visualization. This replaces the old
    // caller-supplied "nodeVisualizations" configuration: the tree is the
    // only input.
    //
    // Only external nodes are considered.
    //
    // Candidacy is decided on the TREE, once: at launch, and again only after
    // the user edits it (deletes a subtree). A VIEW -- the subtree the user
    // switches into -- never re-decides it; it only re-summarizes each
    // candidate over the tips on screen (visualizationSummary). A clade is by
    // nature a set of tips sharing a value, and one value is refused, so
    // re-classifying per view would (and until 2026-09-12 did) drop the
    // chosen colouring in most clades. The desktop works the same way.
    //
    // Candidates: taxonomy code / scientific name / common name, sequence
    // name / symbol / gene name, and node properties whose applies_to is
    // "node" or "clade" (isNodeScopedProperty). The "style:" namespace is
    // never a candidate -- the desktop reserves it for per-node rendering
    // instructions (font_color, node_shape, ...), so treating it as data
    // would mean colouring by a colour. Nor are record-keeping fields, by
    // NAME (VIS_EXCLUDED_WORD_RES below): authors, sets, data-use terms,
    // ids, accessions, identifiers, taxon ids.
    //
    // The rules, tuned against the real ViPR / BV-BRC trees in docs/data
    // (which test/visualization_test.js holds as executable fixtures) and
    // pinned for the desktop by test/fixtures/vis-contract.tsv (names) and
    // test/fixtures/vis-trees.tsv (data):
    //
    //   multi-value a ref carried more than once by any external node is
    //               not a candidate: a node cannot be two colours, and
    //               picking one silently is worse than not offering it.
    //   repetition  at least 2 distinct values (1 paints the whole tree
    //               alike). A CATEGORICAL field with as many distinct
    //               values as the tree has tips is an identifier and is
    //               refused; a NUMERIC one is kept, because a measurement
    //               is naturally one value per sample.
    //   coverage    a field on fewer than 2/3 of the tips is SPARSE: offered,
    //               ranked after everything dense, never opening a tree that
    //               has anything denser. Database exports are always patchy,
    //               and a half-annotated field is often the interesting one.
    //   categorical <= 20 distinct values -> Color. Above ~12 the reader
    //               leans on the legend, but the real trees cluster at
    //               15-17 (host names, countries, taxonomy codes).
    //   wide        21+ distinct values are still offered -- every value
    //               gets a colour and the LEGEND caps the display -- but
    //               never open a tree. If they repeat reasonably
    //               (distinct/covered <= 3/5) they rank after the numerics;
    //               if they barely repeat they are NEAR-UNIQUE and rank at
    //               the very bottom (strains, species names, dates).
    //   numeric     every value matches VIS_NUMERIC_RE, a decimal grammar
    //               pinned below; spellings of one number ("1", "1.0") fold
    //               to one value. Up to 10 distinct values default to
    //               individual colours -- numbers that few are usually codes
    //               (HA/NA subtypes), and ten is what the palette's strong
    //               first half holds -- 11 to 20 default to a Color-range,
    //               and both of those may be switched in the legend; above
    //               20 it is a range with no switch. The band is computed
    //               per VIEW. No uniqueness test for numbers.
    //   shape       <= 7 distinct values (d3 v7 has exactly 7 distinct
    //               fill symbols), numeric or not -- two years as two
    //               shapes is genuinely useful.
    //   in/out-group offered, ranked after the numerics: a fact about the
    //               analysis the person who rooted the tree already knows.
    //
    // Ranking, best first (tierOf below): 0 clean categorical, 1 numeric,
    // 2 wide, 3 in/out-group, 4 sparse, 5 near-unique; within a tier by
    // coverage x balance. The tree OPENS with the first candidate that is
    // not wide (openingVisualization).
    //
    const VIS_MIN_COVERAGE_NUM = 2;    // coverage >= 2/3, held as a
    const VIS_MIN_COVERAGE_DEN = 3;    // fraction so the test is integer-exact
    const VIS_MAX_COLOR_CATEGORIES = 20;
    const VIS_MAX_SHAPE_CATEGORIES = 7;
    const VIS_NUMERIC_CATEGORY_MAX = 10;   // <= this many distinct numbers -> colours by default
    const VIS_WIDE_REPEAT_NUM = 3;         // wide categorical: distinct/covered <= 0.6,
    const VIS_WIDE_REPEAT_DEN = 5;         // held integer-exact
    const VIS_EXCLUDED_REF_PREFIX = 'style:';
    // What "numeric" means, spelled out: an optional sign, decimal digits with
    // an optional fraction, an optional exponent. PINNED as a grammar because
    // the host language's own idea of a number is not portable: JavaScript's
    // Number() accepts "0x1A" and "0b101", Java's parseDouble accepts
    // "Infinity" and "NaN", and a field of either would be a gradient in one
    // program and a category in the other. Values are trimmed before this
    // sees them.
    const VIS_NUMERIC_RE = /^[+-]?(\d+\.?\d*|\.\d+)([eE][+-]?\d+)?$/;
    // Refs that are never a visualization, however their values distribute.
    // A taxon identifier repeats like a category and passes every statistical
    // test above, yet says nothing a colour could carry that the species name
    // beside it does not -- and reads as "11320" in a legend. Matched on the
    // ref's local name with case and separators ignored, so vipr:NCBI_Taxon_Id,
    // ncbi_taxid, taxon_id and taxonomy_id all count.
    const VIS_EXCLUDED_LOCAL_NAME_RE = /(taxonomy|taxon|tax)id$/;

    // The rest are matched on THE NAME THE MENU SHOWS, split into words.
    //
    // Matching the displayed name rather than the raw ref is the point, not a
    // convenience: `prettifyVisLabel` splits camelCase, so the ref
    // `dataUseTerms` reaches the user as "Data Use Terms". A first version of
    // this rule matched the raw ref, and so read that as one word and offered
    // it -- the menu said "Data Use Terms" while the rule saw "datauseterms".
    // Going through the same function means the rule and the label cannot
    // drift apart again.
    //
    // On top of that, every run of non-alphanumerics -- whitespace, '-', '_',
    // punctuation -- is one word break. Separator-aware on purpose, and that
    // is the whole difference from the rule above: "Authority" and "Dataset"
    // are ordinary properties and must survive, while "Abbr Authors" and
    // "Region Set" must not. Reading punctuation as a break also makes the
    // literal column name "Author(s)" come out as the words "author s".
    //
    // What these have in common is that they describe the RECORD rather than
    // the organism: who deposited it, which collection it belongs to, what may
    // be done with it and until when, and what to call it in a database. They repeat like
    // categories and so pass every statistical test, but a colour spent on one
    // says nothing about the tree.
    const VIS_EXCLUDED_WORD_RES = [
        /(^| )authors?( |$)/,   // Author, Authors, Author(s), Abbr Authors
        /(^| )set( |$)/,        // Region Set -- but not Dataset or Subset
        /(^| )data use( |$)/,   // Data use, Data-Use Terms
        /(^| )restricted ?until( |$)/,   // Restricted Until, restricted_until, restrictedUntil: a data-use embargo date
        /(^| )ids?( |$)/,       // genome_id, patric_id, GenomeID, Feature_ID
        /accessions?$/,         // ...Accession, ...Accessions
        /identifiers?$/         // ...Identifier, ...Identifiers
    ];
    // A note on why "id" is a WORD rule and accession/identifier are suffix
    // rules, since the inconsistency is deliberate. "accession" and
    // "identifier" are long enough that a name ending in those letters is one:
    // GBAccession is an accession. "id" is two letters and ends a great many
    // ordinary words -- Plasmid, Hybrid, Nucleic Acid, Lipid, Steroid, Orchid,
    // Centroid, Grid, Rapid -- any of which is a plausible property on a
    // biological tree. Matched as a word it catches genome_id, patric_id,
    // GenomeID, genomeId and Feature_ID while leaving every one of those
    // alone.

    // Offered, but never the tree's OPENING visualization unless it is the
    // only thing on offer. "In-Group" and "Out-Group" say which tips were the
    // study set and which were there to root it -- a fact about the ANALYSIS,
    // and one the person who rooted the tree already knows. They are also
    // typically an even two-value split with full coverage, which is exactly
    // the shape that wins the automatic pick, so without this they open trees
    // coloured by the least surprising thing in them.
    //
    // Both hyphenations and both one-word spellings, since the rule is about a
    // name rather than a punctuation style: In-Group, InGroup, In Group,
    // in_group, ingroup, and the same six for out. Plural too.
    //
    // Anchored as WORDS, which is not decoration: "Within Group" contains the
    // substring "in group" and must keep leading.
    const VIS_DEPRIORITIZED_WORD_RES = [
        /(^| )(in|out) groups?( |$)/,
        /(^| )(in|out)groups?( |$)/
    ];

    // The name as the MENU shows it, split into words -- see the note on
    // VIS_EXCLUDED_WORD_RES for why the displayed name is the right input.
    function visNameWords(ref) {
        let local = ref.indexOf(':') >= 0 ? ref.substring(ref.indexOf(':') + 1) : ref;
        return prettifyVisLabel(local).toLowerCase().replace(/[^a-z0-9]+/g, ' ').trim();
    }

    function matchesAny(res, words) {
        for (let i = 0, l = res.length; i !== l; ++i) {
            if (res[i].test(words)) {
                return true;
            }
        }
        return false;
    }

    function visExcludedRef(ref) {
        if (ref.indexOf(VIS_EXCLUDED_REF_PREFIX) === 0) {
            return true;
        }
        let local = ref.substring(ref.indexOf(':') + 1);
        if (VIS_EXCLUDED_LOCAL_NAME_RE.test(local.toLowerCase().replace(/[^a-z0-9]/g, ''))) {
            return true;
        }
        return matchesAny(VIS_EXCLUDED_WORD_RES, visNameWords(ref));
    }

    function visDeprioritizedRef(ref) {
        return matchesAny(VIS_DEPRIORITIZED_WORD_RES, visNameWords(ref));
    }

    // ---- display normalization --------------------------------------------
    //
    // Property values are grouped for colouring after a normalization pass,
    // following the desktop's PropertyColorScheme with two deliberate
    // extensions. The desktop's part: values are trimmed, underscores read as
    // spaces, whitespace runs collapse, grouping is case-insensitive, and for
    // refs literally named "host" / "country" a trailing qualifier is cut
    // (everything from the first ';' / ':' -- "USA:CA" groups as "USA",
    // "Homo sapiens; sex: M" as "Homo sapiens"). Our extensions, chosen for a
    // VISUALIZATION tool that should look good on the data it is given: a
    // small dictionary of common-animal synonyms folds scientific names and
    // spelling variants into one capitalized common name ("bovine", "calf",
    // "cattle" and "Bos taurus" are all Cow -- including "Human", where the
    // desktop folds the other way); and a qualifier cut that would leave an
    // unclosed "(" behind is trimmed back to before it, so
    // "Saimiri boliviensis (squirrel monkey; voucher: X)" reads as
    // "Saimiri boliviensis" rather than dangling.
    //
    // A value that normalizes to NOTHING -- "_", "___", a host that is only
    // its ";" qualifier -- is no value at all, everywhere: the tip is not
    // covered, no group is made, the legend shows no row for it, and it does
    // not count as the ref being carried twice. (Decided 2026-09-12; the
    // desktop already read it that way.)
    //
    // Matching is WHOLE-VALUE only (after a trailing parenthetical is tried
    // stripped: "Bos taurus (cattle)" looks up "bos taurus") -- never by
    // substring, so "ferret badger" (a Melogale, not a ferret) and
    // "42-day-old pig" keep their own rows. The dictionary applies to
    // property fields only; taxonomy and sequence elements are curated text
    // and stay verbatim. Node names, exports, search, and the node-data
    // dialog always show the raw values -- this is display grouping, nothing
    // more.
    // CROSS-IMPLEMENTATION CONTRACT with desktop Archaeopteryx: this
    // dictionary is carried verbatim on both sides (as agreed with the
    // desktop's Color-by parity work), so a value groups the same way in
    // both viewers. Extend BOTH implementations together, never just one.
    const VIS_SYNONYMS = {
        'Human': ['humans', 'homo sapiens', 'h. sapiens'],
        'Cow': ['bovine', 'calf', 'cattle', 'bull', 'heifer', 'bos taurus', 'b. taurus'],
        'Chicken': ['broiler chicken', 'broiler', 'hen', 'rooster', 'gallus gallus', 'g. gallus', 'gallus gallus domesticus'],
        'Mouse': ['house mouse', 'murine', 'mus musculus', 'm. musculus'],
        'Rat': ['brown rat', 'norway rat', 'black rat', 'rattus norvegicus', 'r. norvegicus', 'rattus rattus'],
        'Ferret': ['domestic ferret', 'mustela putorius furo', 'mustela furo', 'm. putorius furo'],
        'Guinea pig': ['cavy', 'domestic guinea pig', 'cavia porcellus', 'c. porcellus'],
        'Rhesus monkey': ['rhesus macaque', 'macaca mulatta', 'm. mulatta'],
        'Rabbit': ['european rabbit', 'oryctolagus cuniculus', 'o. cuniculus'],
        'Dog': ['canine', 'canis familiaris', 'canis lupus familiaris', 'c. familiaris'],
        'Cat': ['feline', 'domestic cat', 'felis catus', 'f. catus', 'felis silvestris catus'],
        'Duck': ['mallard', 'mallard duck', 'domestic duck', 'anas platyrhynchos', 'a. platyrhynchos'],
        'Pig': ['swine', 'porcine', 'hog', 'piglet', 'sus scrofa', 's. scrofa', 'sus scrofa domesticus'],
        'Horse': ['equine', 'mare', 'stallion', 'equus caballus', 'e. caballus'],
        'Sheep': ['ovine', 'lamb', 'ewe', 'ovis aries', 'o. aries'],
        'Goat': ['caprine', 'capra hircus', 'c. hircus'],
        'Camel': ['dromedary', 'bactrian camel', 'camelus dromedarius', 'camelus bactrianus', 'c. dromedarius']
    };
    // null prototype: the lookup is keyed by FILE values, and a value named
    // "toString" / "__proto__" must miss, not return an inherited member
    // (that crashed visualizationCandidates -- and so launch -- outright)
    const VIS_SYNONYM_LOOKUP = Object.create(null);
    Object.keys(VIS_SYNONYMS).forEach(function (canon) {
        VIS_SYNONYM_LOOKUP[canon.toLowerCase()] = canon;
        VIS_SYNONYMS[canon].forEach(function (syn) {
            VIS_SYNONYM_LOOKUP[syn] = canon;
        });
    });

    // ';' for host fields, ':' for country fields, null otherwise -- matched
    // on the ref's local name EXACTLY, so host_group and isolation_country
    // keep their full values.
    function visQualifierCut(ref) {
        let i = ref.lastIndexOf(':');
        let local = (i >= 0 ? ref.substring(i + 1) : ref).toLowerCase();
        if (local === 'country') {
            return ':';
        }
        if (local === 'host') {
            return ';';
        }
        return null;
    }

    // The display form a raw property value is grouped under (case is
    // preserved here; grouping lowercases it).
    function visDisplayLabel(value, cut) {
        let s = value;
        if (cut) {
            let at = s.indexOf(cut);
            if (at >= 0) {
                s = s.substring(0, at);
                // the cut may land inside a parenthetical; trim back to
                // before the first unclosed '('
                let open = [];
                for (let i = 0; i < s.length; ++i) {
                    if (s.charAt(i) === '(') {
                        open.push(i);
                    } else if (s.charAt(i) === ')') {
                        open.pop();
                    }
                }
                if (open.length > 0) {
                    s = s.substring(0, open[0]);
                }
            }
        }
        // the trailing trim matters: without it, a value that is ALL
        // underscores (e.g. "_") folds to a single space rather than empty,
        // so it survives the caller's empty-string drop; "_cat_" folds to
        // " cat " and never joins the "cat" group. (The leading trim alone
        // only catches whitespace that was already there before folding.)
        s = s.trim().replace(/_/g, ' ').replace(/\s+/g, ' ').trim();
        let hit = VIS_SYNONYM_LOOKUP[s.toLowerCase()];
        if (!hit) {
            let stripped = s.replace(/\s*\([^()]*\)\s*$/, '');
            if (stripped !== s && stripped.length > 0) {
                hit = VIS_SYNONYM_LOOKUP[stripped.toLowerCase()];
            }
        }
        return hit || s;
    }

    // How a numeric field draws by default, from how many distinct values it
    // shows: up to VIS_NUMERIC_CATEGORY_MAX as individual colours (numbers
    // that few are usually codes), up to VIS_MAX_COLOR_CATEGORIES as a
    // gradient the legend can switch back to colours, above that a gradient
    // only. Computed per VIEW: ten distinct years inside a clade draw better
    // as ten colours than as a slice of the whole tree's gradient.
    function visNumericModes(distinct) {
        return {
            colorMode: distinct <= VIS_NUMERIC_CATEGORY_MAX ? 'category' : 'range',
            switchable: distinct <= VIS_MAX_COLOR_CATEGORIES
        };
    }

    // Fixed candidate slots for the phyloXML elements (properties use their
    // ref). CROSS-IMPLEMENTATION CONTRACT with desktop Archaeopteryx: the
    // ids here (tax:code, seq:name, ...) and the rule that taxonomy/sequence
    // elements stay VERBATIM (never folded/grouped like property values,
    // see visDisplayLabel above) are pinned on both sides. Extend BOTH
    // implementations together, never just one.
    const VIS_ELEMENT_SLOTS = [
        {id: 'tax:code', kind: 'taxonomy', label: 'Taxonomy Code', get: function (t) { return t.code; }},
        {id: 'tax:scientific_name', kind: 'taxonomy', label: 'Scientific Name', get: function (t) { return t.scientific_name; }},
        {id: 'tax:common_name', kind: 'taxonomy', label: 'Common Name', get: function (t) { return t.common_name; }},
        {id: 'seq:name', kind: 'sequence', label: 'Sequence Name', get: function (s) { return s.name; }},
        {id: 'seq:symbol', kind: 'sequence', label: 'Sequence Symbol', get: function (s) { return s.symbol; }},
        {id: 'seq:gene_name', kind: 'sequence', label: 'Gene Name', get: function (s) { return s.gene_name; }}
    ];

    // "geographic_group" reads like a database column; a menu should say
    // "Geographic Group". Underscores become spaces, camelCase is split
    // (FluSeason -> Flu Season, GlobalH1Clade -> Global H1 Clade), and
    // all-lowercase words are capitalized. Words that already carry capitals
    // (PANGO, HA, H5N1) are left exactly as written.
    function prettifyVisLabel(name) {
        return name
            .replace(/_/g, ' ')
            .replace(/([a-z])([A-Z])/g, '$1 $2')
            .replace(/([0-9])([A-Z][a-z])/g, '$1 $2')
            .split(' ')
            .map(function (w) {
                return /^[a-z]/.test(w) ? w.charAt(0).toUpperCase() + w.substring(1) : w;
            })
            .join(' ');
    }

    // The display name for a property ref, as the Color-by menu and legends
    // show it: namespace dropped, then prettified. Exported so the node-data
    // dialog names a property the same way the rest of the viewer does.
    forester.propertyDisplayName = function (ref) {
        let local = ref.indexOf(':') >= 0 ? ref.substring(ref.indexOf(':') + 1) : ref;
        return prettifyVisLabel(local);
    };

    // Is this property about the node itself? phyloXML's applies_to has six
    // values and two of them describe the node's own data at a tip: 'node',
    // and 'clade' -- the clade rooted at an external node IS that node. Tools
    // differ on which they write: BV-BRC/ViPR exports say node, the repseq
    // pipeline says clade for every field, and until 2026-09-12 this library
    // accepted only node, so a repseq tree with country, host, subtype and
    // year on every tip offered NOTHING to colour by -- four of the thirteen
    // trees in test_trees, silently. The desktop applies no filter at all.
    // 'parent_branch' is deliberately still out: that is the branch above the
    // node, and colouring a node by its branch's property would be wrong.
    forester.isNodeScopedProperty = function (p) {
        return p.applies_to === 'node' || p.applies_to === 'clade';
    };

    // options.keepConstant: also return a NUMERIC field that has one value on
    // every tip. There is nothing to colour by in it, so the selection never
    // asks for it -- but a matrix does: a single-copy core gene is a column
    // of 1s, and dropping it from the heat map hid a gene the file lists.
    forester.visualizationCandidates = function (tree, options) {
        let keepConstant = !!(options && options.keepConstant);
        let total = 0;
        let stats = Object.create(null);   // id -> {kind, ref, label, nodes, values:Set, multi}; null-proto: ids embed file refs

        forester.preOrderTraversalAll(tree, function (n) {
            if (n.children) {
                return;
            }
            total++;
            // gather this node's values per candidate id first, so carrying
            // the same ref twice is visible as such
            let perNode = Object.create(null);
            function add(id, kind, ref, label, value) {
                if (value === undefined || value === null) {
                    return;
                }
                let v = String(value).trim();
                if (v.length === 0) {
                    return;
                }
                if (!perNode[id]) {
                    perNode[id] = {kind: kind, ref: ref, label: label, values: []};
                }
                perNode[id].values.push(v);
            }
            VIS_ELEMENT_SLOTS.forEach(function (slot) {
                let list = slot.kind === 'taxonomy' ? n.taxonomies : n.sequences;
                if (list) {
                    for (let i = 0; i < list.length; ++i) {
                        add(slot.id, slot.kind, null, slot.label, slot.get(list[i]));
                    }
                }
            });
            if (n.properties) {
                for (let i = 0; i < n.properties.length; ++i) {
                    let p = n.properties[i];
                    if (p.ref && forester.isNodeScopedProperty(p) && !visExcludedRef(p.ref)) {
                        add('prop:' + p.ref, 'property', p.ref, null, p.value);
                    }
                }
            }
            Object.keys(perNode).forEach(function (id) {
                let g = perNode[id];
                let cut = g.kind === 'property' ? visQualifierCut(g.ref) : null;
                // properties group under their normalized display form;
                // taxonomy / sequence elements are curated text, verbatim.
                // A value that folds to NOTHING ("_", a host that is only its
                // ";" qualifier) is NO VALUE, exactly like the empty string
                // dropped above: it does not cover the tip, makes no group,
                // and does not count as "carried twice". Until 2026-09-12 the
                // tip was counted covered here and the legend then showed a
                // row with an empty label (Christian: fix; the desktop had it
                // right).
                let displays = [];
                for (let i = 0; i < g.values.length; ++i) {
                    let display = g.kind === 'property' ? visDisplayLabel(g.values[i], cut) : g.values[i];
                    if (display.length > 0) {
                        displays.push(display);
                    }
                }
                if (displays.length === 0) {
                    return;
                }
                if (!stats[id]) {
                    stats[id] = {kind: g.kind, ref: g.ref, label: g.label, cut: cut,
                        nodes: 0, keys: Object.create(null), multi: false};
                }
                let s = stats[id];
                s.nodes++;
                if (displays.length > 1) {
                    s.multi = true;
                }
                displays.forEach(function (display) {
                    let key = g.kind === 'property' ? display.toLowerCase() : display;
                    if (!s.keys[key]) {
                        s.keys[key] = {count: 0, spellings: Object.create(null)};
                    }
                    s.keys[key].count++;
                    s.keys[key].spellings[display] = (s.keys[key].spellings[display] || 0) + 1;
                });
            });
        });

        let candidates = [];
        Object.keys(stats).forEach(function (id) {
            let s = stats[id];
            if (s.multi) {
                return;
            }
            let covered = s.nodes;
            // one legend row per group: the dictionary canonical where one
            // applied (it is then the only recorded spelling), otherwise the
            // most frequent raw spelling (ties alphabetically), capitalized
            let canon = Object.create(null);
            let counts = Object.create(null);
            Object.keys(s.keys).forEach(function (key) {
                let group = s.keys[key];
                let rep = null;
                let best = -1;
                Object.keys(group.spellings).forEach(function (spelling) {
                    let n = group.spellings[spelling];
                    if (n > best || (n === best && spelling < rep)) {
                        rep = spelling;
                        best = n;
                    }
                });
                if (s.kind === 'property') {
                    rep = rep.charAt(0).toUpperCase() + rep.substring(1);
                }
                canon[key] = rep;
                counts[rep] = group.count;
            });
            let values = Object.keys(counts);
            let numeric = values.every(function (v) {
                return VIS_NUMERIC_RE.test(v);
            });
            if (numeric) {
                // "1", "1.0" and "+1" are one value. Group the spellings by
                // the number they denote; the representative is the SHORTEST
                // spelling, ties alphabetically (a number has no preferred
                // spelling, so frequency would only pick the exporter's
                // habit). canon then maps every raw spelling to it, so
                // visualizationNodeValue folds a node's "1.0" the same way.
                let byNumber = Object.create(null);
                values.forEach(function (v) {
                    let k = String(Number(v));
                    let g = byNumber[k];
                    if (!g) {
                        byNumber[k] = {rep: v, count: counts[v]};
                    } else {
                        g.count += counts[v];
                        if (v.length < g.rep.length || (v.length === g.rep.length && v < g.rep)) {
                            g.rep = v;
                        }
                    }
                });
                let folded = Object.create(null);
                Object.keys(byNumber).forEach(function (k) {
                    folded[byNumber[k].rep] = byNumber[k].count;
                });
                Object.keys(canon).forEach(function (key) {
                    canon[key] = byNumber[String(Number(canon[key]))].rep;
                });
                counts = folded;
                values = Object.keys(counts);
            }
            let distinct = values.length;
            // Sparse fields are RANKED LAST, not refused. A half-annotated
            // field is often the most interesting thing in the tree -- someone
            // hand-annotates a subset precisely because it is worth marking --
            // it just should not be what opens the tree.
            //
            // This was a hard refusal, and it cost real data: on the 13,246-tip
            // BV-BRC flu tree, Country (8,031 covered, 28 values) and Region
            // (8,030, 13 values) both sit at 61% and were dropped from the menu
            // outright, while Isolation_Source cleared the bar by twelve tips.
            // The two fields a phylogeography user reaches for first were
            // missing from our own flagship demo, invisibly, because a refused
            // field leaves no trace in the UI. The desktop ranked instead of
            // refusing from 0.11.133 and was right; this matches it.
            //
            // The legend already carries the honest part: a "no value" row
            // with the uncovered count, at reduced opacity.
            let sparse = covered * VIS_MIN_COVERAGE_DEN < total * VIS_MIN_COVERAGE_NUM;
            if (distinct < 2 && !(keepConstant && numeric)) {
                return;
            }
            let colorMode;
            let switchable = false;
            let wide = false;
            let nearUnique = false;
            if (numeric) {
                // No uniqueness refusal for numbers. There used to be one
                // (distinct/covered > 9/10 -> refused) meant to catch numeric
                // identifiers, and it could not tell an identifier from a
                // MEASUREMENT: a read count, a viral load, an expression level
                // or a year is naturally one value per sample, which is what a
                // measurement is. Run over the desktop's gallery it refused
                // exactly those -- read_count, viral_load, expression_tpm, a
                // 1930-2010 year field -- on nine trees and left one opening
                // uncoloured. Run over ours it refused three things, every one
                // an identifier the NAME rules catch anyway (genome_id twice,
                // BVBRC_Accession). Identifiers are a fact about a name, not a
                // distribution, and the name rules are the right instrument.
                // Removed 2026-09-12, jointly with the desktop.
                let modes = visNumericModes(distinct);
                colorMode = modes.colorMode;
                switchable = modes.switchable;
                values.sort(function (a, b) {
                    return Number(a) - Number(b);
                });
            } else {
                // KNOWN EDGE, left alone BY DECISION (Christian, 2026-09-12):
                // this counts against TOTAL, so a field unique across its
                // annotated subset -- strain on a 1,170-tip flu tree, 1,168
                // distinct over the 1,168 tips that carry it -- is offered
                // rather than refused as the identifier it is. It lands as
                // near-unique, tier 5, the bottom of the menu, and never
                // opens a tree. Counting against COVERED instead closes that
                // (five corpus fields, four of them strain/collection date)
                // but also refuses a field carried by two tips with two
                // values, where all-unique is a sample of two, not evidence.
                // Both were put to him with the numbers; he chose to keep
                // this until a user complains. Also decided then: strain and
                // genome name are NOT excluded by name -- both are grouping
                // fields wherever an isolate contributes several sequences
                // (eight segment records per strain in bvbrc+flu_seg4), and
                // the data rules already sort the identifier case to the
                // bottom. Do not "fix" either without asking him.
                if (distinct >= total) {
                    return;
                }
                if (distinct > VIS_MAX_COLOR_CATEGORIES) {
                    // More than 20 values. If they repeat reasonably (distinct
                    // no more than 3/5 of covered) this is a WIDE category:
                    // offered, never leading. If they barely repeat -- but DO
                    // repeat, else the all-distinct test above would have
                    // refused it -- it goes to the very bottom of the menu.
                    // Christian, 2026-09-12: an all-distinct categorical is an
                    // identifier and stays refused; a barely-repeating one is
                    // nearly one, so it is offered last rather than hidden,
                    // because a refusal cannot be seen failing and a rank can.
                    // It is wide as well, so it never opens a tree.
                    wide = true;
                    if (distinct * VIS_WIDE_REPEAT_DEN > covered * VIS_WIDE_REPEAT_NUM) {
                        nearUnique = true;
                    }
                }
                colorMode = 'category';
                values.sort();
            }
            // Rank by how much a visualization would actually show: coverage
            // times balance, where balance is the normalized entropy of the
            // value distribution. A field that is one value on 92% of nodes
            // scores low even with full coverage; an even 4-way split on 90%
            // of nodes scores high.
            let entropy = 0;
            values.forEach(function (v) {
                let p = counts[v] / covered;
                entropy -= p * Math.log(p);
            });
            let balance = entropy / Math.log(distinct);
            candidates.push({
                id: id,
                kind: s.kind,
                ref: s.ref,
                // property labels drop the namespace prefix and are prettified;
                // a cross-namespace collision is resolved below by restoring
                // the full ref verbatim
                label: s.label || prettifyVisLabel(s.ref.indexOf(':') >= 0 ? s.ref.substring(s.ref.indexOf(':') + 1) : s.ref),
                numeric: numeric,
                coverage: covered,
                total: total,
                values: values,
                counts: counts,
                canon: (s.kind === 'property' || numeric) ? canon : null,
                cut: s.cut,
                score: (covered / total) * balance,
                colorMode: colorMode,
                switchable: switchable,
                wide: wide,
                nearUnique: nearUnique,
                sparse: sparse,
                deprioritized: visDeprioritizedRef(s.ref || id),
                shape: distinct <= VIS_MAX_SHAPE_CATEGORIES
            });
        });

        let labelCount = Object.create(null);
        candidates.forEach(function (c) {
            labelCount[c.label] = (labelCount[c.label] || 0) + 1;
        });
        candidates.forEach(function (c) {
            if (labelCount[c.label] > 1 && c.ref) {
                c.label = c.ref;
            }
        });

        // Best first: clean categorical fields, then EVERY numeric field, then
        // the wide categoricals (offered, never leading), then the
        // deprioritized ones, then the sparse, and last the barely-repeating
        // wide ones -- within each tier by score, ties alphabetically. What
        // OPENS the tree is openingVisualization below: the first entry that
        // is not wide. So tiers 0, 1, 3 and 4 can open a tree, each only when
        // nothing above it exists, and tiers 2 and 5 never do -- a wide field
        // above an In-Group does not stop the In-Group from opening the tree,
        // it just precedes it in the menu.
        function tierOf(c) {
            if (c.nearUnique) {
                return 5;
            }
            if (c.sparse) {
                return 4;
            }
            if (c.deprioritized) {
                return 3;
            }
            // Every numeric field is tier 1, including one with few enough
            // values to draw as discrete colours. Tiering on colour MODE put
            // a small measurement in tier 0 beside the real categories, where
            // an evenly spread one outscores them on entropy -- so with the
            // uniqueness refusal gone, Viral Load would open a tree instead of
            // Segment, and Year instead of Clade. A measurement is always
            // offered; a category, when there is one, still opens the tree.
            if (c.numeric) {
                return 1;
            }
            return c.wide ? 2 : 0;
        }
        candidates.sort(function (a, b) {
            let ta = tierOf(a);
            let tb = tierOf(b);
            if (ta !== tb) {
                return ta - tb;
            }
            if (a.score !== b.score) {
                return b.score - a.score;
            }
            let la = a.label.toLowerCase();
            let lb = b.label.toLowerCase();
            return la < lb ? -1 : (la > lb ? 1 : (a.id < b.id ? -1 : 1));
        });
        return candidates;
    };

    // Decides whether a property should REPLACE the node names as the
    // displayed tip label, and which one. Database exports often name their
    // tips with identifiers (PATRIC.10334.249.FJ478159..., 11320.305060)
    // while carrying the readable name in a property such as
    // BVBRC:genome_name. All of the following must hold, or the answer is
    // null and the names stand:
    //
    //   - at least 80% of the named external nodes have identifier-like
    //     names (no spaces, at least one digit) -- readable names are never
    //     overridden;
    //   - the property's local name ends in "name" (genome_name,
    //     sample_name, ...): only fields that say they are names qualify;
    //   - it covers at least 90% of the external nodes, is mostly distinct
    //     (>= 50%), and is mostly wordy (>= 50% of values contain a space)
    //     -- which is what separates genome_name from strain codes.
    //
    // Of several qualifiers, the best-covered wins, ties alphabetically.
    forester.nodeLabelProperty = function (tree) {
        let total = 0;
        let named = 0;
        let idLike = 0;
        let refs = {};   // ref -> {covered, values:Set, wordy}
        forester.preOrderTraversalAll(tree, function (n) {
            if (n.children) {
                return;
            }
            total++;
            if (n.name && String(n.name).trim().length > 0) {
                named++;
                let name = String(n.name).trim();
                if (!/\s/.test(name) && /\d/.test(name)) {
                    idLike++;
                }
            }
            if (n.properties) {
                let seen = {};
                for (let i = 0; i < n.properties.length; ++i) {
                    let p = n.properties[i];
                    if (!p.ref || !forester.isNodeScopedProperty(p) || seen[p.ref]
                        || p.ref.indexOf(VIS_EXCLUDED_REF_PREFIX) === 0) {
                        continue;
                    }
                    let local = p.ref.indexOf(':') >= 0 ? p.ref.substring(p.ref.indexOf(':') + 1) : p.ref;
                    if (!/name$/i.test(local)) {
                        continue;
                    }
                    let v = (p.value === undefined || p.value === null) ? '' : String(p.value).trim();
                    if (v.length === 0) {
                        continue;
                    }
                    seen[p.ref] = true;
                    if (!refs[p.ref]) {
                        refs[p.ref] = {covered: 0, values: new Set(), wordy: 0};
                    }
                    refs[p.ref].covered++;
                    refs[p.ref].values.add(v);
                    if (v.indexOf(' ') >= 0) {
                        refs[p.ref].wordy++;
                    }
                }
            }
        });
        if (named === 0 || idLike * 10 < named * 8) {
            return null;
        }
        let best = null;
        Object.keys(refs).sort().forEach(function (ref) {
            let r = refs[ref];
            if (r.covered * 10 < total * 9) {
                return;
            }
            if (r.values.size * 2 < r.covered) {
                return;
            }
            if (r.wordy * 2 < r.covered) {
                return;
            }
            if (!best || r.covered > refs[best].covered) {
                best = ref;
            }
        });
        return best;
    };

    // The desktop's reserved "style:" namespace, read back as the rendering
    // instruction it is (NodeVisualData on the desktop): per-node font colour,
    // node colour, node shape, font size and font style. The rest of the
    // vocabulary (font name, node_size, node_transparency, node_fill_type) is
    // not honoured by this viewer yet. Returns null when the node carries
    // none of the five.
    forester.nodeVisualStyle = function (node) {
        if (!node.properties) {
            return null;
        }
        let style = null;
        function put(key, value) {
            if (style === null) {
                style = {};
            }
            style[key] = value;
        }
        for (let i = 0; i < node.properties.length; ++i) {
            let p = node.properties[i];
            if (!p.ref || !forester.isNodeScopedProperty(p) || p.value === undefined || p.value === null) {
                continue;
            }
            let v = String(p.value).trim();
            if (v.length === 0) {
                continue;
            }
            if (p.ref === 'style:font_color') {
                put('fontColor', v);
            } else if (p.ref === 'style:node_color') {
                put('nodeColor', v);
            } else if (p.ref === 'style:node_shape') {
                // the desktop's shape names; rectangle renders as our square
                if (v === 'rectangle') {
                    put('shape', 'square');
                } else if (v === 'circle' || v === 'diamond') {
                    put('shape', v);
                }
            } else if (p.ref === 'style:font_size') {
                let n = Number(v);
                if (Number.isFinite(n) && n > 0) {
                    put('fontSize', Math.min(48, Math.max(4, n)));
                }
            } else if (p.ref === 'style:font_style') {
                if (v === 'italic' || v === 'bold' || v === 'bold_italic' || v === 'plain') {
                    put('fontStyle', v);
                }
            }
        }
        return style;
    };

    // The share of tips that must carry a prefix for it to count as boilerplate.
    // A strict longest-common-prefix (1.0) lets a handful of oddly-named tips
    // veto the strip for everyone else: on the BV-BRC influenza tree 13,096 of
    // 13,246 tips share a 51-character prefix, but the other 0.8% drag the LCP
    // down to "A" and nothing is stripped at all. 0.95 fixes that with real
    // margin, and still refuses splits that would leave two groups of tips
    // incomparable -- at 0.67 the same corpus strips the country code from 78%
    // of tips while 22% keep their full names. DESIGNED JOINTLY WITH THE
    // DESKTOP, which uses the identical value: this rule and its output are
    // byte-identical across both programs, so the threshold is not ours alone
    // to change.
    const COMMON_PREFIX_QUANTILE = 0.95;

    // The boring part of every tip name. When most displayed names share a
    // long prefix ("Influenza A virus ..."), a shortener that keeps the first
    // characters keeps exactly the characters that carry no information. This
    // returns the longest prefix shared by at least COMMON_PREFIX_QUANTILE of
    // the displayed external names -- the label property's value where one is
    // in effect, the node name otherwise -- cut back to the last separator so
    // no word is split, and only when it is long enough to matter
    // (>= 6 characters). The Short Names rendering strips it before
    // truncating, so what survives is the part that tells the tips apart. The
    // comparison is case-insensitive -- "Influenza A virus" and "Influenza A
    // Virus" are the same boring prefix -- so callers must strip by LENGTH,
    // comparing case-insensitively, not by exact match. Tips that do NOT
    // carry the prefix keep their full names; the caller's startsWith test
    // handles that without needing to know about the quantile.
    forester.commonNamePrefix = function (tree, labelProperty) {
        let names = [];
        let slot = labelProperty ? {kind: 'property', ref: labelProperty} : null;
        forester.preOrderTraversalAll(tree, function (n) {
            if (n.children) {
                return;
            }
            let name = slot ? forester.visualizationNodeValue(n, slot) : null;
            if (name === null && n.name !== undefined && n.name !== null) {
                let s = String(n.name).trim();
                if (s.length > 0) {
                    name = s;
                }
            }
            if (name !== null) {
                names.push(name);
            }
        });
        if (names.length < 2) {
            return '';
        }
        // Any k names sharing a prefix are CONTIGUOUS once sorted, so
        // comparing each sorted name with the one k-1 places later finds the
        // longest prefix shared by k of them exactly -- no approximation, and
        // O(n log n + n*L) rather than the quadratic scan the obvious reading
        // suggests. At q = 1.0, k = n and this reduces to the strict LCP.
        let lower = names.map(function (s) {
            return s.toLowerCase();
        }).sort();
        let n = lower.length;
        let k = Math.ceil(COMMON_PREFIX_QUANTILE * n);
        if (k < 2) {
            k = 2;
        }
        if (k > n) {
            k = n;
        }
        let bestLen = 0;
        let bestIdx = -1;
        for (let i = 0; i + k - 1 < n; ++i) {
            let a = lower[i];
            let b = lower[i + k - 1];
            let max = Math.min(a.length, b.length);
            let j = 0;
            while (j < max && a.charCodeAt(j) === b.charCodeAt(j)) {
                ++j;
            }
            if (j > bestLen) {
                bestLen = j;
                bestIdx = i;
            }
        }
        if (bestLen === 0) {
            return '';
        }
        let lowerPrefix = lower[bestIdx].substring(0, bestLen);
        // Casing comes from the first name in TRAVERSAL order that carries the
        // prefix. At q = 1.0 that is names[0], which is exactly what the
        // strict-LCP version returned, so the two agree character for
        // character -- and the desktop mirrors this traversal order
        // deliberately, so the two programs do too.
        let prefix = null;
        let carriers = [];
        for (let ci = 0; ci < names.length; ++ci) {
            let name = names[ci];
            if (name.length >= bestLen
                && name.substring(0, bestLen).toLowerCase() === lowerPrefix) {
                if (prefix === null) {
                    prefix = name.substring(0, bestLen);
                }
                carriers.push(name);
            }
        }
        if (prefix === null) {
            return '';
        }
        // Trim back to the last separator ONLY when the prefix actually
        // splits a word -- "ABC_ho" against "ABC_house"/"ABC_horse" does,
        // "Influenza A virus" against "...virus A/x" and "...virus(A/y)"
        // does not, whatever character each name continues with. Only the
        // names that CARRY the prefix get a say: one that does not share it
        // says nothing about whether the prefix splits a word, and letting it
        // vote throws the prefix away -- 19 tips of "ABCDEFG_..."/"ABCDEFG-..."
        // plus one unrelated longer tip yields "" instead of "ABCDEFG".
        let alnum = /[A-Za-z0-9]/;
        let splitsWord = alnum.test(prefix.charAt(prefix.length - 1))
            && carriers.some(function (name) {
                return name.length > prefix.length && alnum.test(name.charAt(prefix.length));
            });
        if (splitsWord) {
            let cut = -1;
            for (let i = prefix.length - 1; i >= 0; --i) {
                if (' /|_.-:'.indexOf(prefix.charAt(i)) >= 0) {
                    cut = i;
                    break;
                }
            }
            prefix = cut >= 0 ? prefix.substring(0, cut + 1) : '';
        }
        return prefix.length >= 6 ? prefix : '';
    };

    // Reads a node's value for one candidate, exactly as the classifier read
    // it when it built the candidate -- the two must never drift, or a node
    // could carry a value that maps to no colour. Returns the trimmed value,
    // or null when the node has none (the node then keeps the default look).
    // Multi-valued refs never become candidates, so "the first value" is
    // "the only value".
    forester.visualizationNodeValue = function (node, candidate) {
        function clean(v) {
            if (v === undefined || v === null) {
                return null;
            }
            let s = String(v).trim();
            return s.length > 0 ? s : null;
        }
        if (candidate.kind === 'property') {
            if (node.properties) {
                for (let i = 0; i < node.properties.length; ++i) {
                    let p = node.properties[i];
                    if (p.ref === candidate.ref && forester.isNodeScopedProperty(p)) {
                        let v = clean(p.value);
                        if (v !== null) {
                            // a classifier-built candidate folds the value the
                            // same way its groups were built; a bare
                            // {kind, ref} probe (labels, prefixes) reads raw
                            if (candidate.canon) {
                                let display = visDisplayLabel(v, candidate.cut || null);
                                if (display.length === 0) {
                                    // folds to nothing: no value, as the
                                    // classifier counted it -- keep looking
                                    continue;
                                }
                                return candidate.canon[display.toLowerCase()] || display;
                            }
                            return v;
                        }
                    }
                }
            }
            return null;
        }
        let list = candidate.kind === 'taxonomy' ? node.taxonomies : node.sequences;
        if (!list) {
            return null;
        }
        for (let i = 0; i < VIS_ELEMENT_SLOTS.length; ++i) {
            if (VIS_ELEMENT_SLOTS[i].id === candidate.id) {
                for (let j = 0; j < list.length; ++j) {
                    let v = clean(VIS_ELEMENT_SLOTS[i].get(list[j]));
                    if (v !== null) {
                        // verbatim, except that a numeric slot folds its
                        // spellings exactly as the classifier grouped them
                        return candidate.canon ? (candidate.canon[v] || v) : v;
                    }
                }
                return null;
            }
        }
        return null;
    };

    // The visualization a tree OPENS with: the first candidate that is not
    // wide (21+ values are offered, never imposed), or null for none. One
    // definition, used by the viewer and by the fixture generator, so the
    // rule the desktop ports is the rule the viewer runs.
    forester.openingVisualization = function (candidates) {
        for (let i = 0; i < candidates.length; ++i) {
            if (!candidates[i].wide) {
                return candidates[i];
            }
        }
        return null;
    };

    // What a VIEW shows of a candidate: its values, counts and coverage over
    // the tips under `root` -- the subtree the user switched into, or the
    // whole tree. Candidacy is decided ONCE, on the tree
    // (visualizationCandidates); a view never re-decides it, it only
    // re-summarizes. Until 2026-09-12 the viewer re-ran the classifier on
    // every subtree, and since a clade is by nature a set of tips sharing a
    // value, and one value is refused, entering a clade dropped the colouring
    // in 61% of the corpus's clades (4,192 of 6,857) and did not bring it back
    // on return. Values are read exactly as the classifier grouped them, so
    // everything found here is in the candidate's domain and keeps its
    // colour. A numeric candidate also gets the view's colour-mode band
    // (see visNumericModes); a category keeps its mode.
    // `root` is a node, walked in preorder, or an array of the tips to
    // describe (the viewer passes what is on screen, which a collapsed clade
    // shortens).
    forester.visualizationSummary = function (candidate, root) {
        let counts = Object.create(null);
        let total = 0;
        let coverage = 0;
        let tips = root;
        if (!Array.isArray(root)) {
            tips = [];
            forester.preOrderTraversalAll(root, function (n) {
                if (!n.children) {
                    tips.push(n);
                }
            });
        }
        tips.forEach(function (n) {
            if (n.children) {
                return;
            }
            total++;
            let v = forester.visualizationNodeValue(n, candidate);
            if (v !== null) {
                coverage++;
                counts[v] = (counts[v] || 0) + 1;
            }
        });
        let values = Object.keys(counts);
        if (candidate.numeric) {
            values.sort(function (a, b) {
                return Number(a) - Number(b);
            });
        } else {
            values.sort();
        }
        let summary = {values: values, counts: counts, coverage: coverage, total: total, distinct: values.length};
        if (candidate.numeric) {
            let modes = visNumericModes(values.length);
            summary.colorMode = modes.colorMode;
            summary.switchable = modes.switchable;
        }
        return summary;
    };

    // The candidates of a tree the user has EDITED (a subtree deleted), with
    // the fields they had chosen KEPT as long as those still carry a value
    // somewhere in what remains. The refusal rules decide what is offered,
    // never what is already chosen: colouring by Host and deleting every
    // clade but one must not silently uncolour the tree because one host is
    // "not a category". A kept field is appended after the offered ones and
    // flagged `kept`, so the menu holds it exactly as long as the user does;
    // the next edit drops it unless it is still chosen. `chosen` is the
    // previous candidate objects -- their grouping travels with them.
    forester.visualizationCandidatesKeeping = function (tree, chosen) {
        let candidates = forester.visualizationCandidates(tree);
        let ids = Object.create(null);
        candidates.forEach(function (c) {
            ids[c.id] = true;
        });
        (chosen || []).forEach(function (c) {
            if (!c || ids[c.id]) {
                return;
            }
            let s = forester.visualizationSummary(c, tree);
            if (s.coverage === 0) {
                return;
            }
            c.values = s.values;
            c.counts = s.counts;
            c.coverage = s.coverage;
            c.total = s.total;
            if (c.numeric) {
                c.colorMode = s.colorMode;
                c.switchable = s.switchable;
            }
            c.kept = true;
            ids[c.id] = true;
            candidates.push(c);
        });
        return candidates;
    };

    /**
     * The HEAT-MAP columns of a tree: every per-tip NUMERIC property ref, and the
     * one shared scale their values are drawn on.
     *
     * Candidacy is not decided here. A column is a ref that
     * `forester.visualizationCandidates` already calls a numeric property, so the
     * heat map offers exactly what Color-by offers and inherits the refusal rules
     * with it -- a ref carried twice by one node, a ref no tip has, an internal
     * `aptx:` ref. Two features disagreeing about what counts as a numeric field
     * would be worse than either rule.
     *
     * The ORDER is this function's own, and it is not first-appearance. The
     * candidate list is sorted by the selection ranking, which is right for a menu
     * and wrong for a matrix: a producer writes the columns in the order it means
     * them to be read -- gene classes together, say -- and that grouping is
     * information a matrix should keep.
     *
     * But a tip may be MISSING a column, and first-appearance then breaks on the
     * very thing it is for. Measured on the pan-genome demo: 7 of its 100 tips
     * carry no `dnaK`, the first tip is one of them, and first-appearance put a
     * core gene at column 40 of 40, splitting the core block it belongs to. So each
     * column sits at its MEAN position among the tips that do carry it -- every
     * tip's own property order is intact, and averaging merges those orders without
     * one gap being able to move a column. `dnaK` is at index 2 in the tips that
     * have it, `recA` at 2.93 across tips with and without, so the core block
     * survives. First appearance breaks a tie, so the result is deterministic.
     *
     * ONE scale spans every column, so a value means the same thing across the
     * matrix: a 4 in one gene is the colour a 4 is in every other. Per-column
     * scaling would make each column use the whole range and a colour mean
     * something different in each.
     *
     * Returns `{ refs: [{ref, label}], min, max }`, or refs: [] when the tree has
     * no numeric per-tip property. `min`/`max` are over every value of every
     * column and are `null` when there is nothing to scale.
     */
    forester.heatmapColumns = function (tree, only) {
        let wanted = null;               // the figure's columns, when a file names them
        if (Array.isArray(only)) {
            wanted = Object.create(null);
            only.forEach(function (ref) {
                wanted[ref] = true;
            });
        }
        let numeric = Object.create(null);
        let label = Object.create(null);
        forester.visualizationCandidates(tree, {keepConstant: true}).forEach(function (c) {
            if (c && c.kind === 'property' && c.numeric === true && c.ref && (!wanted || wanted[c.ref])) {
                numeric[c.ref] = true;
                label[c.ref] = c.label || c.ref;
            }
        });
        let seen = Object.create(null);    // ref -> its label, for the refs any tip carries
        let sequences = [];                // one per tip: the columns IT lists, in ITS order
        let min = null;
        let max = null;
        forester.preOrderTraversalAll(tree, function (n) {
            if (n.children && n.children.length > 0) {
                return;                      // per-TIP columns: an internal node has no row
            }
            let seq = [];
            (n.properties || []).forEach(function (p) {
                if (!p || !numeric[p.ref]) {
                    return;
                }
                seen[p.ref] = label[p.ref];
                seq.push(p.ref);
                let v = forester.heatmapValue(n, p.ref);
                if (v !== null) {
                    min = (min === null || v < min) ? v : min;
                    max = (max === null || v > max) ? v : max;
                }
            });
            if (seq.length > 0) {
                sequences.push(seq);
            }
        });
        // The consensus of what the tips actually said (see
        // heatmapConsensusOrder). Nothing here depends on the order the tips
        // were REACHED in, and that matters: the traversal follows the tree's
        // current child order, which ladderizing rewrites -- measured, the same
        // file once gave "B A" ladderized and "A B" not. A column order is a
        // fact about the values.
        let refs = forester.heatmapConsensusOrder(sequences, Object.keys(seen));
        return {
            refs: refs.map(function (ref) {
                return {ref: ref, label: seen[ref]};
            }),
            min: min,
            max: max
        };
    };

    // --------------------------------------------------------------
    // The desktop's figure setting (aptx:figure)
    // --------------------------------------------------------------
    // The desktop Archaeopteryx stores what a tree is to be drawn with in one
    // property of the PHYLOGENY: <property ref="aptx:figure" applies_to=
    // "phylogeny"> as a direct child of <phylogeny>. Its value (FigureSpec
    // on the desktop) is "v1;key=value;..." with every value escaped
    // (PropertyTextCodec): \\ \t \n \r, \_ a space, \p "|", \s "~",
    // \c ";", \e "=". columns= is a "|" list of ref~TYPE~SHAPE~normalized.
    // Only a direct child of <phylogeny> counts (Christian, 2026-09-30: "a
    // hard break"): desktops 0.11.117 to 0.11.172 wrote it on the root clade,
    // and such a copy is ignored here and dropped on the way out, as the
    // desktop does. An unknown version is no figure; unknown keys are ignored.
    forester.FIGURE_REF = 'aptx:figure';

    forester.unescapeFigureValue = function (v) {
        const map = {'\\': '\\', 't': '\t', 'n': '\n', 'r': '\r', '_': ' ', 'p': '|', 's': '~', 'c': ';', 'e': '='};
        return String(v).replace(/\\(.)/g, function (all, c) {
            return map[c] !== undefined ? map[c] : all;
        });
    };

    // {columns: [{ref, type, shape, normalized}]} from the tree's figure, or
    // null when it has none (or one this version cannot read).
    forester.readFigure = function (tree) {
        let props = tree && Array.isArray(tree.properties) ? tree.properties : [];
        let p = props.find(function (q) {
            return q && q.ref === forester.FIGURE_REF;
        });
        if (!p || typeof p.value !== 'string') {
            return null;
        }
        let parts = p.value.trim().split(';');
        if (parts[0] !== 'v1') {
            return null;
        }
        // labelProps: the properties the label shows, in order (null when the
        // figure does not say, [] when it says none); show: the desktop's
        // display switches by DisplayOption name ("show.SHOW_PROPERTIES=true")
        let figure = {columns: [], labelProps: null, show: {}};
        parts.slice(1).forEach(function (kv) {
            let at = kv.indexOf('=');
            if (at < 1) {
                return;
            }
            let key = kv.substring(0, at);
            let value = forester.unescapeFigureValue(kv.substring(at + 1));
            if (key === 'labelprops') {
                figure.labelProps = value.split('|').filter(function (r) {
                    return r.length > 0;
                });
                return;
            }
            if (key.indexOf('show.') === 0 && (value === 'true' || value === 'false')) {
                figure.show[key.substring(5)] = value === 'true';
                return;
            }
            if (key !== 'columns') {
                return;
            }
            value.split('|').forEach(function (col) {
                let f = col.split('~');
                if (f[0] && f[0].length > 0) {
                    figure.columns.push({ref: f[0], type: f[1] || '', shape: f[2] || 'CIRCLE',
                        normalized: f[3] === 'true'});
                }
            });
        });
        return figure;
    };

    // The refs the figure draws as a MATRIX, in its order ([] without one).
    forester.figureMatrixRefs = function (tree) {
        let figure = forester.readFigure(tree);
        let seen = Object.create(null);
        return figure ? figure.columns.filter(function (c) {
            let keep = c.type === 'MATRIX' && !seen[c.ref];
            seen[c.ref] = true;
            return keep;
        }).map(function (c) {
            return c.ref;
        }) : [];
    };

    // ---- Properties in the tip label (the desktop's "Properties" option) ----

    // A property a reader would want to see: not the viewer's own aptx:
    // metadata, not a style: instruction.
    const LABEL_PROPERTY_SEPARATOR = ' | ';

    forester.isUserVisiblePropertyRef = function (ref) {
        return typeof ref === 'string' && ref.length > 0 && ref.indexOf('aptx:') !== 0 && ref.indexOf('style:') !== 0;
    };

    // Every user-visible property ref any node carries, internal nodes
    // included, sorted by the name the reader sees (then by ref): what the
    // label-field chooser offers. Deliberately BROADER than the colour-by
    // candidates, which drop constant, per-tip-unique and internal-only fields
    // -- all perfectly good label text (the desktop's userVisiblePropertyRefs).
    forester.labelPropertyRefs = function (tree) {
        let seen = Object.create(null);
        forester.preOrderTraversalAll(tree, function (n) {
            (n.properties || []).forEach(function (p) {
                if (p && forester.isUserVisiblePropertyRef(p.ref)
                    && p.value !== undefined && p.value !== null && String(p.value).trim().length > 0) {
                    seen[p.ref] = true;
                }
            });
        });
        return Object.keys(seen).sort(function (a, b) {
            let da = forester.propertyDisplayName(a).toLowerCase();
            let db = forester.propertyDisplayName(b).toLowerCase();
            return da < db ? -1 : (da > db ? 1 : (a < b ? -1 : (a > b ? 1 : 0)));
        });
    };

    // A node's properties as one line of label text: values only, a unit
    // after its value (without its namespace), joined by " | " as the rest of
    // the label is. Not by ", " as on the desktop (Christian, 2026-09-30): a
    // value often holds a comma itself -- a BV-BRC host "Bottlenose dolphin,
    // Tursiops truncatus" -- and "host, species" then reads as three fields. `refs` picks and orders the fields; null is every
    // user-visible property in the node's own order, less `excluded` (the
    // heat map's columns: a field has one display role).
    forester.labelPropertiesText = function (node, refs, excluded) {
        let props = (node && node.properties) || [];
        let parts = [];
        let add = function (p) {
            if (!p || !forester.isUserVisiblePropertyRef(p.ref) || p.value === undefined || p.value === null) {
                return;
            }
            let v = String(p.value).trim();
            if (v.length < 1) {
                return;
            }
            if (typeof p.unit === 'string' && p.unit.length > 0) {
                v += ' ' + p.unit.substring(p.unit.lastIndexOf(':') + 1);
            }
            parts.push(v);
        };
        if (refs) {
            refs.forEach(function (ref) {
                props.forEach(function (p) {
                    if (p && p.ref === ref) {
                        add(p);
                    }
                });
            });
        } else {
            props.forEach(function (p) {
                if (!(excluded && p && excluded[p.ref])) {
                    add(p);
                }
            });
        }
        return parts.join(LABEL_PROPERTY_SEPARATOR);
    };

    // Removes an aptx:figure from every clade (the old place, now ignored),
    // so a tree saved from here carries no dead copy. Returns how many.
    forester.dropCladeFigures = function (tree) {
        let n = 0;
        forester.preOrderTraversalAll(tree, function (node) {
            if (node !== tree && Array.isArray(node.properties)) {
                let kept = node.properties.filter(function (q) {
                    return !(q && q.ref === forester.FIGURE_REF);
                });
                n += node.properties.length - kept.length;
                node.properties = kept;
            }
        });
        return n;
    };

    /**
     * The number in `node`'s heat-map cell for `ref`, or `null` when there is none.
     *
     * `null` is NOT zero and must never be drawn as one: a missing property is a
     * cell nobody filled in -- absence of evidence -- and on a presence/absence
     * matrix reading it as 0 would state the opposite of what the file says. An
     * empty, blank or non-numeric value is the same case. Only the FIRST value of
     * a repeated ref can be shown, there being one cell to show it in; such refs
     * are refused upstream as candidates, so this is a backstop, not the path.
     */
    forester.heatmapValue = function (node, ref) {
        let ps = (node && node.properties) || [];
        for (let i = 0; i < ps.length; i++) {
            if (ps[i] && ps[i].ref === ref) {
                let v = ps[i].value;
                if (v === null || v === undefined || String(v).trim() === '') {
                    return null;
                }
                let n = Number(String(v).trim());
                return isFinite(n) ? n : null;
            }
        }
        return null;
    };

    // ---- heat-map column ORDER -------------------------------------------
    //
    // The desktop's View > Order Matrix Columns, minus its Manual mode (which
    // needs a drag-to-reorder dialog we have not got). Every data-driven mode
    // starts from the columns in DOCUMENT order, so a result never depends on
    // the order they happened to be in: switching Alphabetical -> Clustered
    // and Document -> Clustered must give the same clustering.
    //
    // A cell that is not assessed never counts as 0, in any mode: blank is
    // absence of evidence, not evidence of absence.

    forester.HEATMAP_ORDER_MODES = ['document', 'clustered', 'clustered-presence', 'alphabetical',
        'frequency', 'manual'];

    /**
     * The value of every column at every tip, [column][tip], null where the
     * tip has no value -- the same number the cell is drawn from. EVERY tip
     * counts, collapsed or not, as the shared colour scale does: an order that
     * changed when a clade is collapsed would make the columns jump under the
     * reader.
     */
    forester.heatmapValueMatrix = function (tree, refs) {
        let tips = forester.getAllExternalNodes(tree);
        return refs.map(function (ref) {
            return tips.map(function (t) {
                return forester.heatmapValue(t, ref);
            });
        });
    };

    /**
     * Euclidean distance between every two columns, R's dist(method =
     * "euclidean") convention for missing values: only the tips where BOTH
     * columns have a value are used, and the squared sum is scaled up by
     * tips / used, so a pair assessed on fewer tips is not made to look closer
     * for it. A pair that shares no assessed tip at all has no distance; it is
     * +Infinity here (R returns NA and its hclust then refuses), so it joins
     * last.
     */
    forester.heatmapEuclideanDistances = function (v) {
        let n = v.length;
        let d = v.map(function () {
            return new Array(n).fill(0);
        });
        for (let a = 0; a < n; ++a) {
            for (let b = a + 1; b < n; ++b) {
                let sum = 0;
                let used = 0;
                let tips = v[a].length;
                for (let t = 0; t < tips; ++t) {
                    if (v[a][t] !== null && v[b][t] !== null) {
                        let dev = v[a][t] - v[b][t];
                        sum += dev * dev;
                        ++used;
                    }
                }
                let dist = (used === 0) ? Infinity
                    : Math.sqrt((used === tips) ? sum : (sum / (used / tips)));
                d[a][b] = dist;
                d[b][a] = dist;
            }
        }
        return d;
    };

    /**
     * Bray-Curtis dissimilarity between every two columns: sum|x - y| /
     * sum(x + y) over the tips where BOTH have a value (pairwise deletion, R
     * vegan's vegdist(method = "bray", na.rm = TRUE)). Being a ratio it
     * normalizes itself, so -- unlike Euclidean -- it needs no scale-up for
     * the tips it dropped.
     *
     * A tip where both columns are 0 adds nothing to either sum, so it drops
     * out: the DOUBLE ZERO, which Euclidean reads as agreement, is simply not
     * evidence here. Two genes that are each rare and never in the same tip
     * are maximally distant (1) rather than nearly identical. On 0/1 data this
     * is exactly the Sorensen-Dice dissimilarity 1 - 2|A and B| / (|A| + |B|).
     *
     * Two divergences from vegdist, both forced: a pair sharing no assessed
     * tip is +Infinity (vegdist gives NA, and hclust then refuses), and a pair
     * that is 0 at every tip it shares -- the undefined 0/0 -- is 0, because
     * those columns are identical wherever they can be compared (vegdist gives
     * NaN). The same rule covers the only other way the denominator can fail,
     * negative values cancelling: a pair that cannot be told apart is 0, one
     * that differs over a non-positive total is +Infinity rather than a
     * negative or NaN distance. Bray-Curtis is meant for values 0 or more.
     *
     * The result is a DISSIMILARITY, not a metric -- it does not obey the
     * triangle inequality, which complete linkage does not require.
     */
    forester.heatmapBrayCurtisDistances = function (v) {
        let n = v.length;
        let d = v.map(function () {
            return new Array(n).fill(0);
        });
        for (let a = 0; a < n; ++a) {
            for (let b = a + 1; b < n; ++b) {
                let num = 0;
                let den = 0;
                let used = 0;
                let tips = v[a].length;
                for (let t = 0; t < tips; ++t) {
                    if (v[a][t] !== null && v[b][t] !== null) {
                        num += Math.abs(v[a][t] - v[b][t]);
                        den += v[a][t] + v[b][t];
                        ++used;
                    }
                }
                let dist;
                if (used === 0) {
                    dist = Infinity;
                } else if (den <= 0) {
                    dist = (num === 0) ? 0 : Infinity;
                } else {
                    dist = num / den;
                }
                d[a][b] = dist;
                d[b][a] = dist;
            }
        }
        return d;
    };

    /**
     * The complete-linkage dendrogram of a symmetric distance matrix, the same
     * structure R's hclust returns, so it can be pinned against R wholesale:
     *
     *   left / right  one merge per stage, in R's $merge node-id convention --
     *                 a NEGATIVE value is the singleton of that index
     *                 (-1 = column 0), a POSITIVE value the cluster formed at
     *                 that earlier stage. Oriented by R's hcass2 rule
     *                 (singleton before cluster, lower index between
     *                 singletons, earlier stage between clusters), which is
     *                 what makes the leaf order reproducible.
     *   height        the distance each merge happened AT ($height) -- what a
     *                 drawn dendrogram's bar heights are. It can be +Infinity
     *                 for a pair sharing no assessed tip, so anything drawing
     *                 it has to decide what to do with a merge that has no
     *                 finite height.
     *   order         the leaves as $order reads them out, which is the column
     *                 order itself.
     *
     * Each step merges the closest two clusters; a cluster is labelled by its
     * lowest member index and a tie goes to the first pair the (i, j) scan
     * finds, which is what R's nearest-neighbour scan does. The merged
     * cluster's distance to every other is the LARGER of the two (complete
     * linkage, the Lance-Williams update R uses).
     */
    forester.heatmapCompleteLinkage = function (d) {
        let n = d.length;
        if (n === 0) {
            return {left: [], right: [], height: [], order: []};
        }
        if (n === 1) {
            return {left: [], right: [], height: [], order: [0]};
        }
        let dist = d.map(function (row) {
            return row.slice();
        });
        let active = new Array(n).fill(true);
        // node id of the cluster currently labelled i: -(i+1) for a singleton,
        // (stage+1) for a merged cluster
        let node = new Array(n);
        for (let i = 0; i < n; ++i) {
            node[i] = -(i + 1);
        }
        let left = new Array(n - 1);
        let right = new Array(n - 1);
        let height = new Array(n - 1);
        for (let stage = 0; stage < (n - 1); ++stage) {
            let bi = -1;
            let bj = -1;
            let best = NaN;
            for (let i = 0; i < n; ++i) {
                if (!active[i]) {
                    continue;
                }
                for (let j = i + 1; j < n; ++j) {
                    if (!active[j]) {
                        continue;
                    }
                    // strictly smaller only: the FIRST pair in (i, j) order
                    // wins a tie, as in R's scan
                    if ((bi < 0) || (dist[i][j] < best)) {
                        best = dist[i][j];
                        bi = i;
                        bj = j;
                    }
                }
            }
            // hcass2's orientation, as one rule: singletons are negative,
            // clusters positive by stage
            let a = node[bi];
            let b = node[bj];
            let l;
            let r;
            if ((a < 0) && (b < 0)) {
                l = (-a < -b) ? a : b;
                r = (-a < -b) ? b : a;
            } else if ((a < 0) || (b < 0)) {
                l = (a < 0) ? a : b;
                r = (a < 0) ? b : a;
            } else {
                l = Math.min(a, b);
                r = Math.max(a, b);
            }
            left[stage] = l;
            right[stage] = r;
            height[stage] = best;
            // complete linkage: the merged cluster (kept under the lower
            // label, bi) is as far as its farther half
            for (let k = 0; k < n; ++k) {
                if (active[k] && (k !== bi) && (k !== bj)) {
                    let m = Math.max(dist[bi][k], dist[bj][k]);
                    dist[bi][k] = m;
                    dist[k][bi] = m;
                }
            }
            active[bj] = false;
            node[bi] = stage + 1;
        }
        // expand from the root, in place, left child before right -- hcass2's IORDER
        let order = [left[n - 2], right[n - 2]];
        for (let stage = n - 2; stage >= 1; --stage) {
            let pos = order.indexOf(stage);   // the cluster formed at this stage (1-based node id)
            if (pos >= 0) {
                order[pos] = left[stage - 1];
                order.splice(pos + 1, 0, right[stage - 1]);
            }
        }
        return {
            left: left, right: right, height: height,
            order: order.map(function (v) {
                return -v - 1;
            })
        };
    };

    /**
     * `columns` (the {ref, label} objects forester.heatmapColumns returned, in
     * DOCUMENT order) re-ordered for `mode`, with the dendrogram behind that
     * order when there is one.
     *
     * The dendrogram is returned only for a clustered mode with at least three
     * columns -- fewer than three have one order up to a flip and are left
     * alone, so there is no clustering behind them -- and only when its leaves
     * ARE the order being returned. A dendrogram drawn over a matrix it does
     * not describe would have connectors that cross: a picture asserting a
     * grouping the columns do not have. Checking the answer beats enumerating
     * the ways to get there.
     */
    /**
     * `columns` put back into DOCUMENT order -- the consensus of what this
     * tree's tips actually said, read by heatmapConsensusOrder over exactly the
     * columns GIVEN. A column no tip carries casts no votes and cannot be
     * placed by them; it must not be dropped for that, so it lands at the end,
     * by ref.
     *
     * Every data-driven mode normalises through here precisely so a result
     * cannot depend on the order the columns happened to be in -- the previous
     * mode's, say. Going through heatmapColumns instead would drag in its
     * CANDIDACY rules (a constant column is refused as a visualization), and an
     * order must not move for a reason that has nothing to do with ordering.
     */
    forester.heatmapInDocumentOrder = function (tree, columns) {
        let cols = (columns || []).slice();
        let want = Object.create(null);
        cols.forEach(function (c) {
            want[c.ref] = c;
        });
        let sequences = [];
        forester.preOrderTraversalAll(tree, function (n) {
            if (n.children && n.children.length > 0) {
                return;
            }
            let seq = [];
            (n.properties || []).forEach(function (p) {
                if (p && want[p.ref]) {
                    seq.push(p.ref);
                }
            });
            if (seq.length > 0) {
                sequences.push(seq);
            }
        });
        // The same consensus forester.heatmapColumns reads, over exactly the
        // columns GIVEN, so it cannot be moved by a candidacy rule that has
        // nothing to do with ordering. A column no tip carries casts no votes
        // and has no position; it lands last, by ref, so the result depends
        // only on its inputs as a SET.
        return forester.heatmapConsensusOrder(sequences, cols.map(function (c) {
            return c.ref;
        })).map(function (ref) {
            return want[ref];
        });
    };

    /**
     * The order a heat map opens in when nobody said: a CLUSTERED one, with the
     * distance picked from the data.
     *
     * Bray-Curtis is the better read on presence/absence and abundance data --
     * it is what the double-zero problem calls for -- but it is meant for
     * values 0 or more, and these columns are every numeric field the tree
     * happens to carry rather than a hand-picked matrix. Measured: on a
     * latitude/longitude matrix 2 of 6 pairs come out with no distance at all
     * (+Infinity, so they join last arbitrarily), and a year column among small
     * ones sits at 0.9995 from every one of them -- maximally distant for being
     * LARGE rather than for any pattern. Within a block of comparable magnitude
     * it is exactly right; across magnitudes it measures magnitude.
     *
     * So: Bray-Curtis where there are zeros to ignore and nothing negative,
     * Euclidean otherwise. A zero is precisely the precondition for a double
     * zero to exist -- without one there is nothing for Bray-Curtis to drop,
     * and a matrix of years or coordinates is the safer read on Euclidean.
     */
    /**
     * How far the INPUT agrees with itself about the column order: {tips,
     * conflicting}, where a tip conflicts when its own property list is not in
     * the order `columns` gives (gaps are fine -- a tip that simply lacks a
     * column is not a disagreement).
     *
     * This is what lets the "As in the input" control say whether that order is
     * the file's or only a consensus. phyloXML gives every node its OWN
     * property list, unlike a table, so tips CAN list the same columns in
     * different orders, and a reader has no way of knowing without being told.
     * Measured on the shipped demos: 0 of 50 and 0 of 100 tips conflict, while
     * influenza.xml has 2 of 6.
     */

    /**
     * ONE column order out of many tips that each list their own columns in
     * their own order. `sequences` is one array of refs per tip -- that tip's
     * own order, gaps and all -- and `refs` is the set to place.
     *
     * Averaging each column's position was the obvious rule and it fails on a
     * shape that is not at all exotic: two groups of columns that no tip ever
     * carries TOGETHER. Tips holding A,B and tips holding X,Y put A and X both
     * at position 0, so the average interleaves them into X A Y B instead of
     * leaving the two blocks whole. No tie-break can mend that -- the average
     * itself is what is wrong, because it compares positions measured on tips
     * that share no frame of reference.
     *
     * So the order is read off a PRECEDENCE GRAPH instead. Each tip votes on
     * the pairs it actually lists next to each other, the majority direction
     * of each pair becomes an edge, and the columns come out of a topological
     * sort of that graph. Two blocks with no tip in common cast no votes about
     * each other, so nothing forces them apart, and the sort emits one and then
     * the other.
     *
     * Only ADJACENT pairs are looked at -- both the votes and the neighbour
     * sets the block rule reads -- which keeps this linear in the data. Every
     * pair would be quadratic in the column count, and a wide matrix is exactly
     * where that hurts: measured at 42 seconds on 2000 columns across 200 tips,
     * against 0.2 for adjacency. Transitivity comes from the sort rather than
     * from the votes, and a block stays whole through its chain of neighbours. The cost is that adjacency can manufacture a cycle where all
     * pairs would not -- tips A,B,C and C,A give A->B->C->A -- so cycles are
     * broken rather than assumed away.
     *
     * Where the graph says nothing, the old rule decides, so every case it
     * already got right is unchanged: mean position, then the better-attested
     * column, then the ref. What is new is that a column is preferred while it
     * still shares a tip with the one just placed, which is what keeps a block
     * together once it has been started.
     */
    forester.heatmapConsensusOrder = function (sequences, refs) {
        let all = (refs || []).slice();
        let n = all.length;
        if (n < 2) {
            return all;
        }
        let idx = Object.create(null);
        all.forEach(function (r, i) {
            idx[r] = i;
        });
        let sum = new Array(n).fill(0);
        let tips = new Array(n).fill(0);
        let votes = new Map();          // i*n + j -> tips putting i just before j
        let near = [];                  // i -> Set of columns some tip lists NEXT to i
        for (let i = 0; i < n; ++i) {
            near.push(new Set());
        }
        (sequences || []).forEach(function (seq) {
            let at = 0;
            let prev = -1;
            for (let k = 0; k < seq.length; ++k) {
                let j = idx[seq[k]];
                if (j === undefined) {
                    continue;
                }
                sum[j] += at;
                tips[j]++;
                at++;
                if (prev >= 0 && prev !== j) {
                    let key = (prev * n) + j;
                    votes.set(key, (votes.get(key) || 0) + 1);
                    // neighbours, for the block rule below. Adjacency again,
                    // not every pair on the tip: every pair is quadratic in the
                    // column count, and measured, it was 42 SECONDS on 2000
                    // columns across 200 tips. A block stays whole through its
                    // chain of neighbours, so adjacency is all it needs.
                    near[prev].add(j);
                    near[j].add(prev);
                }
                prev = j;
            }
        });
        let mean = new Array(n);
        for (let i = 0; i < n; ++i) {
            mean[i] = (tips[i] > 0) ? (sum[i] / tips[i]) : Infinity;
        }
        // the majority direction of each voted pair becomes an edge; a pair its
        // tips split evenly says nothing and gets none
        let out = [];
        let indeg = new Array(n).fill(0);
        for (let i = 0; i < n; ++i) {
            out.push([]);
        }
        votes.forEach(function (count, key) {
            let i = Math.floor(key / n);
            let j = key % n;
            if (count > (votes.get((j * n) + i) || 0)) {
                out[i].push(j);
                indeg[j]++;
            }
        });
        // the static key, used wherever the graph is silent: the rule this
        // replaced, so nothing it already ordered correctly moves
        function better(a, b, last) {
            if (last >= 0) {
                let na = near[last].has(a);
                let nb = near[last].has(b);
                if (na !== nb) {
                    return na;   // keep the block that is already open
                }
            }
            if (mean[a] !== mean[b]) {
                return mean[a] < mean[b];
            }
            if (tips[a] !== tips[b]) {
                return tips[a] > tips[b];
            }
            return all[a] < all[b];
        }
        let placed = new Array(n).fill(false);
        let order = [];
        let last = -1;
        for (let step = 0; step < n; ++step) {
            let pick = -1;
            for (let i = 0; i < n; ++i) {
                if (placed[i] || indeg[i] > 0) {
                    continue;
                }
                if (pick < 0 || better(i, pick, last)) {
                    pick = i;
                }
            }
            if (pick < 0) {
                // a cycle: no column is free. Break it at the column the fewest
                // others are waiting on, then by the same static key -- never
                // leave a column undrawn because its votes disagreed.
                for (let i = 0; i < n; ++i) {
                    if (placed[i]) {
                        continue;
                    }
                    if (pick < 0 || indeg[i] < indeg[pick]
                        || (indeg[i] === indeg[pick] && better(i, pick, last))) {
                        pick = i;
                    }
                }
            }
            placed[pick] = true;
            indeg[pick] = 0;
            order.push(all[pick]);
            out[pick].forEach(function (j) {
                if (!placed[j] && indeg[j] > 0) {
                    indeg[j]--;
                }
            });
            last = pick;
        }
        return order;
    };

    forester.heatmapInputOrderAgreement = function (tree, columns) {
        let out = {tips: 0, conflicting: 0};
        if (!tree || !columns || columns.length < 1) {
            return out;
        }
        let rank = Object.create(null);
        columns.forEach(function (c, i) {
            rank[c.ref] = i;
        });
        forester.preOrderTraversalAll(tree, function (n) {
            if (n.children && n.children.length > 0) {
                return;
            }
            let highest = -1;
            let ordered = true;
            let carries = false;
            (n.properties || []).forEach(function (p) {
                if (!p || !(p.ref in rank)) {
                    return;
                }
                carries = true;
                let r = rank[p.ref];
                if (r < highest) {
                    ordered = false;
                } else {
                    highest = r;
                }
            });
            if (!carries) {
                return;
            }
            out.tips++;
            if (!ordered) {
                out.conflicting++;
            }
        });
        return out;
    };

    forester.heatmapDefaultOrder = function (tree, columns) {
        if (!tree || !columns || columns.length < 1) {
            return 'document';
        }
        let v = forester.heatmapValueMatrix(tree, columns.map(function (c) {
            return c.ref;
        }));
        let any = false;
        let zero = false;
        let negative = false;
        v.forEach(function (row) {
            row.forEach(function (x) {
                if (x === null) {
                    return;
                }
                any = true;
                if (x === 0) {
                    zero = true;
                } else if (x < 0) {
                    negative = true;
                }
            });
        });
        if (!any) {
            return 'document';   // nothing to cluster on
        }
        return (zero && !negative) ? 'clustered-presence' : 'clustered';
    };

    /**
     * `columns` in the order `wanted` names them, which is a reader's own
     * arrangement and is never second-guessed.
     *
     * A column `wanted` does not name is NEW to it -- the tree was edited, a
     * table was joined since, the order came from a shared link made against a
     * different file. It cannot be placed by a list that has never heard of it,
     * so it goes after the ones that can, in the order the tips themselves give
     * (heatmapInDocumentOrder). A name for a column that is gone is ignored
     * rather than treated as an error: an order outliving one of its columns is
     * ordinary, not a fault.
     */
    forester.heatmapManualOrder = function (tree, columns, wanted) {
        let cols = (columns || []).slice();
        let by = Object.create(null);
        cols.forEach(function (c) {
            by[c.ref] = c;
        });
        let taken = Object.create(null);
        let out = [];
        (wanted || []).forEach(function (ref) {
            if (by[ref] && !taken[ref]) {
                taken[ref] = true;
                out.push(by[ref]);
            }
        });
        let rest = cols.filter(function (c) {
            return !taken[c.ref];
        });
        return (rest.length > 0) ? out.concat(forester.heatmapInDocumentOrder(tree, rest)) : out;
    };

    forester.heatmapOrder = function (tree, columns, mode, wanted) {
        let out = {columns: (columns || []).slice(), dendrogram: null};
        if (!tree || out.columns.length < 1 || !mode || mode === 'document') {
            return out;
        }
        if (mode === 'manual') {
            // a reader's own order: nothing re-sorts it, and nothing clustered
            // it, so there is no tree behind it to draw
            out.columns = forester.heatmapManualOrder(tree, out.columns, wanted);
            return out;
        }
        // Every data-driven mode starts from document order, so switching
        // Alphabetical -> Clustered and Document -> Clustered must give the
        // same clustering. Measured: without this the 6x6 linkage fixture fed
        // in reverse clustered to h6,h5,h3,h4,h2,h1 instead of R's
        // h6,h3,h5,h4,h1,h2 -- the index order drives the tie-break and the
        // hcass2 leaf order, so the incoming order really does leak through.
        let cols = forester.heatmapInDocumentOrder(tree, out.columns);
        out.columns = cols;
        if (mode === 'alphabetical') {
            out.columns = cols.slice().sort(function (a, b) {
                let la = String(a.label || a.ref).toLowerCase();
                let lb = String(b.label || b.ref).toLowerCase();
                // the raw ref breaks a tie, as the desktop's chooser sorts
                return (la < lb) ? -1 : ((la > lb) ? 1
                    : ((a.ref < b.ref) ? -1 : ((a.ref > b.ref) ? 1 : 0)));
            });
            return out;
        }
        let v = forester.heatmapValueMatrix(tree, cols.map(function (c) {
            return c.ref;
        }));
        if (mode === 'frequency') {
            // by the mean over the tips that HAVE a value, highest first -- on
            // 0/1 data exactly the fraction of tips carrying it. A blank is
            // left out, never averaged in as 0; a column with no value at all
            // goes last; ties keep document order (the sort is stable).
            let mean = v.map(function (row) {
                let sum = 0;
                let n = 0;
                row.forEach(function (x) {
                    if (x !== null) {
                        sum += x;
                        ++n;
                    }
                });
                return (n > 0) ? (sum / n) : -Infinity;
            });
            out.columns = cols.map(function (c, i) {
                return {c: c, i: i};
            }).sort(function (a, b) {
                return (mean[b.i] - mean[a.i]) || (a.i - b.i);
            }).map(function (e) {
                return e.c;
            });
            return out;
        }
        if (mode !== 'clustered' && mode !== 'clustered-presence') {
            return out;
        }
        if (cols.length < 3) {
            return out;   // one order up to a flip, and R puts index 1 first
        }
        let d = (mode === 'clustered')
            ? forester.heatmapEuclideanDistances(v)
            : forester.heatmapBrayCurtisDistances(v);
        let dendro = forester.heatmapCompleteLinkage(d);
        let ordered = dendro.order.map(function (g) {
            return cols[g];
        });
        out.columns = ordered;
        // the self-check: the picture is offered only if it describes the order
        if (dendro.order.length === cols.length) {
            out.dendrogram = dendro;
        }
        return out;
    };


    forester.collectBasicTreeProperties = function (tree) {
        let properties = {};
        properties.internalNodeData = false;
        properties.nodeNames = false;
        properties.longestNodeName = 0;
        properties.branchLengths = false;
        properties.confidences = false;
        // MAD values (madRoot) rate root positions, not clades: never support
        properties.madValues = false;
        // the largest confidence value seen -- how a caller tells a
        // posterior-probability tree (max <= 1) from a bootstrap tree
        properties.maxConfidence = 0;
        properties.nodeEvents = false;
        properties.branchColors = false;
        properties.sequences = false;
        properties.taxonomies = false;
        properties.alignedMolSeqs = true;
        properties.maxMolSeqLength = 0;
        // protein domain architectures on the tips: whether any tip carries
        // one, and the longest (Lmax, the scale every track shares)
        properties.domainArchitectures = false;
        properties.maxDomainArchitectureLength = 0;
        // genome regions on the tips (genes as located sequences): whether
        // any tip carries one, and the longest span in base pairs
        properties.geneRegions = false;
        properties.maxGeneRegionLength = 0;
        properties.externalNodesCount = 0;
        properties.nodeCount = 0;
        // Branches that carry a length AT ALL -- an explicit zero is a real
        // measurement, not a missing one, so these are counted separately from
        // the "positive" tally above. Split internal-vs-all because a missing
        // length means different things in the two places: an unmeasured TIP
        // still draws correctly (at its parent), while an unmeasured INTERNAL
        // branch destroys the scale. All four counts exclude the root, which
        // has no branch above it.
        properties.branchCount = 0;
        properties.branchesWithLength = 0;
        properties.internalBranchCount = 0;
        properties.internalBranchesWithLength = 0;
        properties.averageBranchLength = 0;
        // Positive lengths only, and the root included: these feed
        // averageBranchLength and nothing else. They are deliberately NOT the
        // same population as branchCount / branchesWithLength below, which
        // exclude the root and count an explicit zero as the measurement it is.
        // A `branchesWithPositiveLength` property built from this counter was
        // removed on 2026-09-11 -- nothing read it, and having two tallies over
        // two different node sets invited exactly the comparison that would be
        // wrong.
        let bl_counter = 0;
        let bl_sum = 0;
        // Counting the super-root would add a node and a branch that do not
        // exist -- skewing the branch-length fraction the viewer uses to choose
        // between a phylogram and a cladogram -- and from phyloXML would take
        // the tree's own name for the longest node name.
        let rootNode = realRootOf(tree);
        forester.preOrderTraversalAll(rootNode, function (n) {
            properties.nodeCount += 1;
            if (n !== rootNode) {
                let internal = !!(n.children);
                let measured = typeof n.branch_length === 'number' && isFinite(n.branch_length);
                properties.branchCount += 1;
                if (measured) {
                    properties.branchesWithLength += 1;
                }
                if (internal) {
                    properties.internalBranchCount += 1;
                    if (measured) {
                        properties.internalBranchesWithLength += 1;
                    }
                }
            }
            if (n.name && n.name.length > 0) {
                properties.nodeNames = true;
                if (n.name.length > properties.longestNodeName) {
                    properties.longestNodeName = n.name.length;
                }
                if ((n.children) && (n.parent)) {
                    properties.internalNodeData = true;
                }
            }
            if (!(n.children)) {
                properties.externalNodesCount += 1;
            }
            if (n.branch_length && n.branch_length > 0) {
                properties.branchLengths = true;
                bl_sum += n.branch_length;
                bl_counter += 1;
            }
            if (n.events) {
                properties.nodeEvents = true;
            }
            if (n.color) {
                properties.branchColors = true;
            }
            if (n.sequences && n.sequences.length > 0) {
                properties.sequences = true;

                if (n.children) {
                    properties.internalNodeData = true;
                } else {
                    let s = n.sequences[0];
                    if (s.mol_seq && s.mol_seq.value) {
                        if (s.mol_seq.value.length > properties.maxMolSeqLength) {
                            properties.maxMolSeqLength = s.mol_seq.value.length;
                        }
                        if (!s.mol_seq.is_aligned) {
                            properties.alignedMolSeqs = false;
                        }
                    }
                    let da = forester.domainArchitectureOf(n);
                    if (da) {
                        properties.domainArchitectures = true;
                        if (Number(da.length) > properties.maxDomainArchitectureLength) {
                            properties.maxDomainArchitectureLength = Number(da.length);
                        }
                    }
                    // a tree carries genome regions when some tip carries
                    // TWO genes or more: a gene-family tree whose tips each
                    // locate their one gene is not a tree of genomes
                    let gr = forester.geneRegionOf(n);
                    if (gr && gr.genes.length >= 2) {
                        properties.geneRegions = true;
                        let L = gr.span.end - gr.span.start + 1;
                        if (L > properties.maxGeneRegionLength) {
                            properties.maxGeneRegionLength = L;
                        }
                    }
                }
            }
            if (n.taxonomies && n.taxonomies.length > 0) {
                properties.taxonomies = true;
                if (n.children) {
                    properties.internalNodeData = true;
                }
            }
            if (n.confidences && n.confidences.length > 0) {
                for (let ci = 0; ci < n.confidences.length; ++ci) {
                    if (n.confidences[ci].type === forester.MAD_CONFIDENCE_TYPE) {
                        properties.madValues = true;
                        continue;
                    }
                    properties.confidences = true;
                    let cv = n.confidences[ci].value;
                    if (typeof cv === 'number' && isFinite(cv) && cv > properties.maxConfidence) {
                        properties.maxConfidence = cv;
                    }
                }
            }
            if (n.properties && n.properties.length > 0) {
                let l = n.properties.length;
                for (let p = 0; p < l; ++p) {
                    if (n.properties[p].ref === BRANCH_EVENT_REF && n.properties[p].datatype === BRANCH_EVENT_DATATYPE && n.properties[p].applies_to === BRANCH_EVENT_APPLIES_TO) {
                        properties.branchEvents = true;
                    }
                }
            }

        });


        if (bl_counter > 0) {
            properties.averageBranchLength = bl_sum / bl_counter;
        }


        return properties;
    };

    /**
     * This calculates the sum of the external
     * descendants of a node. It does not count descendants
     * of collapsed nodes.
     *
     * @param node - A node.
     * @returns {number} - The sum of external descendants.
     */
    forester.calcSumOfExternalDescendants = function (node) {
        let nodes = 0;
        forester.preOrderTraversal(node, function (n) {
            if (!n.children) {
                ++nodes;
            }
        });
        return nodes;
    };

    /**
     * This calculates the sum of all the external
     * descendants of a node. It does count descendants
     * of collapsed nodes.
     *
     * @param node - A node.
     * @returns {number} - The sum of all external descendants.
     */
    forester.calcSumOfAllExternalDescendants = function (node) {
        let nodes = 0;
        forester.preOrderTraversalAll(node, function (n) {
            if (!(n.children)) {
                ++nodes;
            }
        });
        return nodes;
    };

    // Ladderize: at every node, order the children by clade size --
    // largest first when largestFirst, smallest first when not. Works at ANY
    // child count, not just 2, so a polytomy (common on a phylodynamic tree,
    // e.g. an Auspice build, where every internal node may carry 3+ children)
    // is sorted exactly like a bifurcation. The sort is STABLE (ties keep
    // their existing relative order), so a node that already reads correctly
    // is never needlessly disturbed. Mutates the tree in place; returns
    // whether anything actually changed.
    forester.ladderize = function (node, largestFirst) {
        let changed = false;
        ord(node);
        return changed;

        function ord(n) {
            if (!n.children) {
                return;
            }
            let c = n.children;
            let l = c.length;
            if (l >= 2) {
                let counts = c.map(function (child) {
                    return forester.calcSumOfAllExternalDescendants(child);
                });
                let order = c.map(function (child, i) {
                    return i;
                });
                order.sort(function (i, j) {
                    if (counts[i] === counts[j]) {
                        return i - j;
                    }
                    return largestFirst ? (counts[j] - counts[i]) : (counts[i] - counts[j]);
                });
                if (order.some(function (idx, i) {
                    return idx !== i;
                })) {
                    changed = true;
                    n.children = order.map(function (idx) {
                        return c[idx];
                    });
                }
            }
            for (let i = 0; i < n.children.length; ++i) {
                ord(n.children[i]);
            }
        }
    };

    forester.getAllExternalNodes = function (node) {
        let nodes = [];
        forester.preOrderTraversalAll(node, function (n) {
            if (!n.children) {
                nodes.push(n);
            }
        });
        return nodes;
    };

    forester.getAllNodes = function (phy) {
        let nodes = [];
        forester.preOrderTraversalAll(forester.getTreeRoot(phy), function (n) {
            nodes.push(n);
        });
        return nodes;
    };

    forester.calcDepth = function (node) {

        let steps = 0;
        while (node.parent && node.parent.parent) {
            steps++;
            node = node.parent;
        }
        return steps;
    };


    // The nodes of `list` in PREORDER -- root first, then each subtree -- which
    // is the claim order the crowded-data rule needs and is NOT the order the
    // layout hands out. d3's descendants() is BREADTH-first (r,a,b,a1,a2,b1),
    // and the viewer reverses it for drawing, which puts the root LAST. Taking
    // that order for preorder inverts the whole rule: the leaf-most mark wins
    // and the one nearer the root is dropped, which is the opposite of what
    // the desktop does. Measured, after a review caught it: "descendants()
    // r,a,b,a1,a2,b1" and "reversed b1,a2,a1,b,a,r".
    //
    // Derived from the node objects rather than from any layout, so it cannot
    // drift with one: the root is the member whose parent is not itself in the
    // list, and children are visited in their own order. A node whose parent
    // is absent (a subtree view) roots its own walk, and anything unreachable
    // is appended so no mark is silently skipped.
    forester.preorderOf = function (list) {
        let present = new Set(list);
        let out = [];
        let seen = new Set();
        function walk(n) {
            if (seen.has(n)) {
                return;
            }
            seen.add(n);
            out.push(n);
            let kids = n.children;
            if (kids) {
                for (let i = 0; i < kids.length; ++i) {
                    if (present.has(kids[i])) {
                        walk(kids[i]);
                    }
                }
            }
        }
        for (let i = 0; i < list.length; ++i) {
            let n = list[i];
            if (!n.parent || !present.has(n.parent)) {
                walk(n);
            }
        }
        for (let i = 0; i < list.length; ++i) {
            if (!seen.has(list[i])) {
                out.push(list[i]);   // a cycle or a detached node: never dropped
            }
        }
        return out;
    };

    // The box to claim for a mark that RIDES ITS BRANCH -- the circular and
    // unrooted layouts, where a label is turned to the branch's direction
    // instead of standing along a row. The axis-aligned bounds of the turned
    // rectangle, centred on (cx, cy):
    //
    //     rot_w = |w*cos(a)| + |h*sin(a)|
    //     rot_h = |w*sin(a)| + |h*cos(a)|
    //
    // The desktop's formula, verbatim (TreePanel.paintBranchDataRadial), and
    // the second half of the joint rule below. It carries between the two
    // programs because both measure it in real SCREEN space -- the layout's
    // own units do not survive the trip, which is why this was parked as a
    // divergence until forester-71 sent the arithmetic.
    //
    // Deliberately conservative: a diagonal label's bounds are wider than its
    // ink, so a crowded number is dropped rather than overprinted. Exact at
    // the right angles, worst at 45 degrees. Insensitive to the sign of the
    // angle and to a half turn, which is what lets the 180-degree flip that
    // keeps the far half of a fan upright leave the box alone.
    forester.rotatedLabelBox = function (cx, cy, w, h, angle) {
        let cos = Math.abs(Math.cos(angle));
        let sin = Math.abs(Math.sin(angle));
        let rw = (w * cos) + (h * sin);
        let rh = (w * sin) + (h * cos);
        return [cx - (rw / 2), cy - (rh / 2), rw, rh];
    };

    // The occupancy map behind "auto-hide crowded branch data": a mark is
    // drawn only when its box overlaps nothing already granted in the same
    // pass. Ported from the desktop's LabelOccupancy (0.11.161) as a JOINT
    // rule on Christian's word, 2026-09-25, so neither side retunes it alone.
    //
    // Here rather than in the viewer because it is a pure algorithm and the
    // suite can reach it: the parts that MUST match the desktop -- first claim
    // wins, a refused claim records nothing, a zero-size box is granted and
    // not recorded, and boxes that merely touch do not overlap -- are exactly
    // the parts no screenshot would show going wrong.
    // A uniform grid rather than all-pairs: a 13,246-tip tree with branch
    // lengths on offers 18,511 boxes, and 171 million comparisons per redraw
    // is not a redraw.
    /**
     * The same first-come space-keeping as labelOccupancy, but for boxes that
     * are TURNED: each claim is a quadrilateral, and two of them overlap only
     * if their own outlines do.
     *
     * A turned label cannot use the axis-aligned map. The bounds of a rotated
     * rectangle grow with its length, so a long name lying near its spoke
     * reserves several times its own area, and the map refuses neighbours that
     * a reader can see are clear of it. Measured on a 48-label circular ring:
     * 28 pairs "overlapping" by the bounds where the outlines gave 0, and a
     * first-come pass over the same labels in the same order kept 48 by
     * outline against 32 by bounds -- sixteen names dropped for nothing.
     *
     * The grid is still used, on each quad's bounds, to find which claims are
     * worth testing; only the test itself is exact.
     *
     * @param cell - grid size, in the same units as the quads
     * @returns an object with claim(quad) and occupy(quad); a quad is four
     *          [x, y] corners in order round the shape
     */
    forester.orientedOccupancy = function (cell) {
        let size = Math.max(4, cell);
        let cells = new Map();
        let boundsOf = function (quad) {
            let x0 = quad[0][0], x1 = quad[0][0], y0 = quad[0][1], y1 = quad[0][1];
            for (let i = 1; i < quad.length; ++i) {
                x0 = Math.min(x0, quad[i][0]);
                x1 = Math.max(x1, quad[i][0]);
                y0 = Math.min(y0, quad[i][1]);
                y1 = Math.max(y1, quad[i][1]);
            }
            return [x0, y0, x1, y1];
        };
        // Separating-axis test. A rectangle's own two edge normals are enough
        // for itself, but BOTH shapes' normals are needed: with only one
        // shape's, a box that clears another along the OTHER's axis is
        // reported as overlapping. Strict, so shapes that merely touch do not
        // overlap -- as in labelOccupancy.
        let overlaps = function (P, Q) {
            for (let s = 0; s < 2; ++s) {
                let R = s === 0 ? P : Q;
                for (let k = 0; k < 2; ++k) {
                    let ax = R[k + 1][1] - R[k][1];
                    let ay = R[k][0] - R[k + 1][0];
                    let p0 = Infinity, p1 = -Infinity, q0 = Infinity, q1 = -Infinity;
                    for (let i = 0; i < P.length; ++i) {
                        let d = (P[i][0] * ax) + (P[i][1] * ay);
                        p0 = Math.min(p0, d);
                        p1 = Math.max(p1, d);
                    }
                    for (let i = 0; i < Q.length; ++i) {
                        let d = (Q[i][0] * ax) + (Q[i][1] * ay);
                        q0 = Math.min(q0, d);
                        q1 = Math.max(q1, d);
                    }
                    if (p1 <= q0 || q1 <= p0) {
                        return false;
                    }
                }
            }
            return true;
        };
        let record = function (quad, b) {
            let c1 = Math.floor(b[2] / size);
            let r1 = Math.floor(b[3] / size);
            for (let c = Math.floor(b[0] / size); c <= c1; ++c) {
                for (let r = Math.floor(b[1] / size); r <= r1; ++r) {
                    let key = c + ':' + r;
                    let bucket = cells.get(key);
                    if (!bucket) {
                        bucket = [];
                        cells.set(key, bucket);
                    }
                    bucket.push(quad);
                }
            }
        };
        let free = function (quad, b) {
            let seen = null;
            let c1 = Math.floor(b[2] / size);
            let r1 = Math.floor(b[3] / size);
            for (let c = Math.floor(b[0] / size); c <= c1; ++c) {
                for (let r = Math.floor(b[1] / size); r <= r1; ++r) {
                    let bucket = cells.get(c + ':' + r);
                    if (!bucket) {
                        continue;
                    }
                    for (let i = 0; i < bucket.length; ++i) {
                        // a quad spans several cells, so the same one comes up
                        // more than once; the test is the expensive part
                        if (seen === null) {
                            seen = new Set();
                        }
                        if (seen.has(bucket[i])) {
                            continue;
                        }
                        seen.add(bucket[i]);
                        if (overlaps(quad, bucket[i])) {
                            return false;
                        }
                    }
                }
            }
            return true;
        };
        let degenerate = function (quad) {
            if (!quad || quad.length !== 4) {
                return true;
            }
            let b = boundsOf(quad);
            return !((b[2] - b[0]) > 0) || !((b[3] - b[1]) > 0);
        };
        return {
            claim: function (quad) {
                if (degenerate(quad)) {
                    return true;   // reserves nothing, as a zero-size box does
                }
                let b = boundsOf(quad);
                if (!free(quad, b)) {
                    return false;
                }
                record(quad, b);
                return true;
            },
            // record without asking, for what is drawn whatever else is there
            occupy: function (quad) {
                if (degenerate(quad)) {
                    return true;
                }
                let b = boundsOf(quad);
                let clear = free(quad, b);
                record(quad, b);
                return clear;
            }
        };
    };

    forester.labelOccupancy = function (cell) {
        let size = Math.max(4, cell);
        let cells = new Map();
        let record = function (x, y, w, h) {
            let box = [x, y, w, h];
            let c1 = Math.floor((x + w) / size);
            let r1 = Math.floor((y + h) / size);
            for (let c = Math.floor(x / size); c <= c1; ++c) {
                for (let r = Math.floor(y / size); r <= r1; ++r) {
                    let key = c + ':' + r;
                    let bucket = cells.get(key);
                    if (!bucket) {
                        bucket = [];
                        cells.set(key, bucket);
                    }
                    bucket.push(box);
                }
            }
        };
        let free = function (x, y, w, h) {
            let c1 = Math.floor((x + w) / size);
            let r1 = Math.floor((y + h) / size);
            for (let c = Math.floor(x / size); c <= c1; ++c) {
                for (let r = Math.floor(y / size); r <= r1; ++r) {
                    let bucket = cells.get(c + ':' + r);
                    if (!bucket) {
                        continue;
                    }
                    for (let i = 0; i < bucket.length; ++i) {
                        let b = bucket[i];
                        // strict: boxes that merely touch do not overlap
                        if (x < (b[0] + b[2]) && (x + w) > b[0]
                            && y < (b[1] + b[3]) && (y + h) > b[1]) {
                            return false;
                        }
                    }
                }
            }
            return true;
        };
        return {
            // true when nothing already granted overlaps, and then recorded
            claim: function (x, y, w, h) {
                if (!(w > 0) || !(h > 0)) {
                    return true;   // reserves no space; recording it would only cost comparisons
                }
                if (!free(x, y, w, h)) {
                    return false;
                }
                record(x, y, w, h);
                return true;
            },
            // Record without asking, for what is drawn whatever else is there.
            // A refused CLAIM records nothing, so a label put through claim()
            // and refused would leave the space open and let a number print
            // straight through the name it lost to -- the opposite of the
            // point. Returns whether the space was clear, for measurement;
            // the box is recorded either way.
            occupy: function (x, y, w, h) {
                if (!(w > 0) || !(h > 0)) {
                    return true;
                }
                let clear = free(x, y, w, h);
                record(x, y, w, h);
                return clear;
            }
        };
    };

    // Where a protein domain can be read about, as {url, exact} or null.
    //
    // An InterPro ENTRY is addressed by ACCESSION and by nothing else --
    // measured, not assumed: /api/entry/pfam/PF00931/ answers 200 and names
    // NB-ARC, while /api/entry/pfam/NB-ARC/, /CARD/ and /Death/ are all 404.
    // phyloXML's <domain id="..."> carries the accession when the annotation
    // had one, and most files have none (our own apaf.xml names all 166 of its
    // boxes and gives not one an id), so the fallback is an InterPro text
    // search.
    //
    // The search goes by NAME, never by the id. An id that is not a bare Pfam
    // accession -- a versioned one from an hmmscan (PF00931.24), or a SMART or
    // CDD accession -- searches to nothing, while the name is exactly what a
    // text search is for. A version suffix is stripped for the entry URL
    // rather than disqualifying it: PF00931.24 IS PF00931.
    //
    // Lives here rather than in the viewer so it can be tested: a wrong URL
    // fails silently in someone else's browser, which is the worst place to
    // find out.
    const INTERPRO_ENTRY = 'https://www.ebi.ac.uk/interpro/entry/pfam/';
    const INTERPRO_SEARCH = 'https://www.ebi.ac.uk/interpro/search/text/';

    forester.domainReference = function (domain) {
        if (!domain) {
            return null;
        }
        let id = (domain.id || '').trim();
        let acc = /^(PF\d{5,})(?:\.\d+)?$/i.exec(id);
        if (acc) {
            return {url: INTERPRO_ENTRY + acc[1].toUpperCase() + '/', exact: true};
        }
        let q = (domain.name || '').trim() || id;
        return q ? {url: INTERPRO_SEARCH + encodeURIComponent(q) + '/', exact: false} : null;
    };

    // A domain's E-value for display. It spans many orders of magnitude, so a
    // small one keeps its exponent; the count of significant digits is the
    // only thing normalized, because a scan writes 7.2E-117 and 1E-6 and a
    // readout that mixed those forms would read as two different measures.
    forester.domainEvalueText = function (e) {
        if (typeof e !== 'number' || !isFinite(e)) {
            return null;
        }
        if (e === 0) {
            return '0';
        }
        return (Math.abs(e) >= 0.001 && Math.abs(e) < 1e7)
            ? String(forester.roundNumber(e, 6)) : e.toExponential(2);
    };

    // Everything the Tree Properties dialog reports, gathered in ONE pass:
    // the desktop's TreeFacts.scan, minus its histogram (Christian, 2026-09-23:
    // "we don't need a histogram ... but a more detailed stats would be
    // useful, min, median, max, n, also type ... also need to include the
    // properties").
    //
    // Side-effect free and DOM-free, so it can be tested in Node -- the whole
    // reason it lives here rather than in the dialog that renders it, which is
    // the same split the desktop makes between TreeFacts and
    // TreePropertiesForm.
    //
    // Support values are grouped BY TYPE, because a tree can carry a bootstrap
    // and a posterior on the same branch and their ranges are not comparable
    // (0-100 against 0-1); averaging them together would produce a number that
    // describes neither. MAD values are excluded for the same reason they are
    // excluded from the Newick support slot: they rate a root position, not a
    // clade.
    forester.treeStatistics = function (phyOrNode) {
        // Callers hand this either a phylogeny wrapper or the root node, and
        // the difference is one extra internal node, one extra branch and one
        // more of depth -- small enough to read as plausible. getTreeRoot
        // normalizes both, as collectBasicTreeProperties does.
        let root = phyOrNode ? forester.getTreeRoot(phyOrNode) : null;
        let stats = {
            tips: 0, internal: 0, nodes: 0, branches: 0,
            polytomies: 0, maxChildren: 0,
            depth: 0, height: 0, heightKnown: false,
            collapsed: 0,
            branchLengths: null,
            support: [],
            ultrametric: false,
            tipsWithTaxonomy: 0, distinctTaxonomies: 0, tipsWithTaxonomyId: 0,
            tipsWithSequence: 0, tipsWithMolSeq: 0, tipsWithDomains: 0, tipsWithGenes: 0,
            nodesWithDate: 0, tipsWithDistribution: 0, namedInternal: 0,
            duplications: 0, speciations: 0, nodesWithEvents: 0,
            propertyRefs: []
        };
        if (!root) {
            return stats;
        }
        let lengths = [];
        let zeroLengths = 0;
        let negativeLengths = 0;
        let byType = Object.create(null);
        let taxa = Object.create(null);
        let refs = Object.create(null);
        let refOrder = [];
        let tipDepths = [];
        let maxDepth = 0;

        (function walk(n, depth, distance) {
            ++stats.nodes;
            let kids = n.children || [];
            if (depth > maxDepth) {
                maxDepth = depth;
            }
            if (n !== root) {
                ++stats.branches;
                let b = n.branch_length;
                if (typeof b === 'number' && isFinite(b)) {
                    lengths.push(b);
                    if (b === 0) {
                        ++zeroLengths;
                    } else if (b < 0) {
                        ++negativeLengths;
                    }
                    distance += b;
                }
            }
            // support, by type, MAD left out
            if (n.confidences) {
                for (let i = 0; i < n.confidences.length; ++i) {
                    let c = n.confidences[i];
                    if (c.type === forester.MAD_CONFIDENCE_TYPE) {
                        continue;
                    }
                    if (typeof c.value !== 'number' || !isFinite(c.value)) {
                        continue;
                    }
                    let key = c.type || '';
                    if (!byType[key]) {
                        byType[key] = [];
                        stats.support.push({type: key, values: byType[key]});
                    }
                    byType[key].push(c.value);
                }
            }
            if (n.date && typeof n.date.value === 'number' && isFinite(n.date.value)) {
                ++stats.nodesWithDate;
            }
            if (n.properties) {
                let seen = Object.create(null);
                for (let i = 0; i < n.properties.length; ++i) {
                    let ref = n.properties[i] && n.properties[i].ref;
                    if (!ref || seen[ref]) {
                        continue;   // a node carrying a ref twice counts once
                    }
                    seen[ref] = true;
                    if (refs[ref] === undefined) {
                        refs[ref] = 0;
                        refOrder.push(ref);
                    }
                    ++refs[ref];
                }
            }
            if (n.events) {
                ++stats.nodesWithEvents;
                if (n.events.duplications > 0) {
                    stats.duplications += n.events.duplications;
                }
                if (n.events.speciations > 0) {
                    stats.speciations += n.events.speciations;
                }
            }
            if (kids.length > 0) {
                ++stats.internal;
                if (kids.length > stats.maxChildren) {
                    stats.maxChildren = kids.length;
                }
                if (kids.length > 2) {
                    ++stats.polytomies;
                }
                if (n !== root && n.name) {
                    ++stats.namedInternal;
                }
                for (let i = 0; i < kids.length; ++i) {
                    walk(kids[i], depth + 1, distance);
                }
                return;
            }
            ++stats.tips;
            tipDepths.push(distance);
            if (n.taxonomies && n.taxonomies.length > 0) {
                ++stats.tipsWithTaxonomy;
                let t = n.taxonomies[0];
                let label = t.scientific_name || t.code || t.common_name || '';
                if (label) {
                    taxa[label] = true;
                }
                if (t.id && t.id.value) {
                    ++stats.tipsWithTaxonomyId;
                }
            }
            if (n.sequences && n.sequences.length > 0) {
                ++stats.tipsWithSequence;
                let q = n.sequences[0];
                if (q.mol_seq && q.mol_seq.value) {
                    ++stats.tipsWithMolSeq;
                }
                if (forester.domainArchitectureOf(n)) {
                    ++stats.tipsWithDomains;
                }
                if (forester.geneRegionOf(n)) {
                    ++stats.tipsWithGenes;
                }
            }
            if (n.distributions && n.distributions.length > 0) {
                ++stats.tipsWithDistribution;
            }
        })(root, 0, 0);

        stats.depth = maxDepth;
        stats.distinctTaxonomies = Object.keys(taxa).length;
        stats.propertyRefs = refOrder.map(function (r) {
            return {ref: r, nodes: refs[r]};
        });
        stats.branchLengths = forester.describeValues(lengths);
        if (stats.branchLengths) {
            stats.branchLengths.zero = zeroLengths;
            stats.branchLengths.negative = negativeLengths;
        }
        stats.support = stats.support.map(function (g) {
            let d = forester.describeValues(g.values);
            d.type = g.type;
            return d;
        }).filter(function (d) {
            return d.n > 0;
        });
        if (tipDepths.length > 0) {
            // running, not Math.min.apply: the spread of 150k tip depths
            // overflows the call stack, as timeAxisInfo found before this
            let lo = Infinity;
            let hi = -Infinity;
            for (let i = 0; i < tipDepths.length; ++i) {
                if (tipDepths[i] < lo) {
                    lo = tipDepths[i];
                }
                if (tipDepths[i] > hi) {
                    hi = tipDepths[i];
                }
            }
            stats.height = hi;
            stats.heightKnown = lengths.length > 0;
            // ultrametric: every tip the same distance from the root, to a
            // tolerance scaled to the tree, since branch lengths are floats
            stats.ultrametric = stats.heightKnown && (hi - lo) <= (Math.max(hi, 1e-12) * 1e-6);
        }
        return stats;
    };

    // n, minimum, median, maximum, mean and sum of a list of numbers, or null
    // when there are none. The median is the middle value of the sorted list,
    // or the mean of the two middle ones -- taken on a COPY, because sorting
    // the caller's array would reorder data it still owns.
    forester.describeValues = function (values) {
        if (!values || values.length === 0) {
            return null;
        }
        let v = values.slice().sort(function (a, b) {
            return a - b;
        });
        let n = v.length;
        let sum = 0;
        for (let i = 0; i < n; ++i) {
            sum += v[i];
        }
        let mid = Math.floor(n / 2);
        return {
            n: n,
            min: v[0],
            max: v[n - 1],
            median: (n % 2 === 1) ? v[mid] : ((v[mid - 1] + v[mid]) / 2),
            mean: sum / n,
            sum: sum
        };
    };

    forester.calcMaxBranchLength = function (node) {
        let max = 0;
        forester.preOrderTraversalAll(node, function (n) {
            if (n !== node && n.branch_length && (n.branch_length > max)) {
                max = n.branch_length;
            }
        });
        return max;
    };


    /**
     * To parse a New Hampshire (Newick) formatted tree.
     *
     * @param nhStr - A New Hampshire (Newick) formatted string.
     * @param confidenceValuesInBrackets - Set to true if confidence values are in brackets (default: true)
     *                                     Format is: name:distance[confidence]
     *                                     Example: "bcl2:0.000393[95]"
     * @param confidenceValuesAsInternalNames - Set to true if confidence values are represented by internal names (default: false).
     * @returns {{}} - A phylogenetic tree object.
     */
    // ---------------------------------------------------------------
    // Extended Newick annotations: BEAST-style [&key=value,...] and
    // NHX [&&NHX:tag=value:...]
    // ---------------------------------------------------------------
    //
    // ALWAYS parsed (the desktop keeps this behind a
    // setParseBeastStyleExtendedTags option; here the blobs were simply
    // discarded before, so ingesting them can regress nothing). Ported from
    // the desktop's BeastAnnotationParser + the NHXParser tag loop.

    // Pre-tokenization pass: pull every [&...] annotation out of the Newick
    // string and leave a [@N] marker in its place -- the blob's commas,
    // colons and quotes must never reach the Newick tokenizer. A bracket NOT
    // starting with '&' (a [95] confidence) is left untouched, as is any
    // bracket inside a quoted label. Quotes and nested brackets inside an
    // annotation are honoured when finding its end.
    //
    // A QUOTE CHARACTER INSIDE A BLOB IS DATA unless it opens a quoted VALUE.
    // Auspice writes values bare -- country=Côte d'Ivoire -- and treating that
    // apostrophe as the start of a quoted string was a real bug, in both of
    // its forms: one such tip and the quote never closed, so the file was
    // refused over its "unbalanced parentheses"; two and the apostrophes
    // paired up ACROSS the tips, no error at all, the second tip gone and the
    // first one's country reading "Côte d'Ivoire],B:1[&country=Côte d'Ivoire".
    // (Real file: nextstrain_chikv_global_timetree.nexus, 16 apostrophes, all
    // of them that one country.) Matches the desktop's scanner rule.
    //
    // So a quote opens a run only where a value can START -- straight after
    // '=', or after '{', '[' or ',' inside a set -- and only if it is closed
    // by the same character standing where a value can END: before ',', '}',
    // ']' or the end. Anything else is a character like any other.
    function opensBlobQuote(s, p, from) {
        let m = p - 1;
        while (m >= from && /\s/.test(s.charAt(m))) {
            --m;
        }
        return m >= from && '={[,'.indexOf(s.charAt(m)) >= 0;
    }

    // The index of the quote closing the run opened at p, or -1. `bounded` is
    // for the extraction pass, which does not yet know where the blob ends:
    // there the search gives up at a ']' that is followed by Newick structure,
    // so a bare value that merely BEGINS with an apostrophe ('s-Hertogenbosch)
    // cannot reach into the next node's blob for its partner.
    function blobQuoteClose(s, p, bounded) {
        let q = s.charAt(p);
        for (let k = p + 1; k < s.length; ++k) {
            let c = s.charAt(k);
            if (c !== q && !(bounded && c === ']')) {
                continue;
            }
            let m = k + 1;
            while (m < s.length && /\s/.test(s.charAt(m))) {
                ++m;
            }
            let next = m < s.length ? s.charAt(m) : '';
            if (c === q) {
                if (next === '' || ',}]'.indexOf(next) >= 0) {
                    return k;
                }
            } else if (next === '' || ',):;(['.indexOf(next) >= 0) {
                return -1;
            }
        }
        return -1;
    }

    function extractBracketAnnotations(str) {
        if (str.indexOf('[') < 0) {
            return {text: str, blobs: []};
        }
        let out = '';
        let blobs = [];
        let inSq = false;
        let inDq = false;
        for (let i = 0; i < str.length; ++i) {
            let c = str.charAt(i);
            if (inSq || inDq) {
                out += c;
                if ((inSq && c === "'") || (inDq && c === '"')) {
                    inSq = false;
                    inDq = false;
                }
            } else if (c === "'") {
                inSq = true;
                out += c;
            } else if (c === '"') {
                inDq = true;
                out += c;
            } else if (c === '[') {
                let j = i + 1;
                let depth = 1;
                while (j < str.length && depth > 0) {
                    let cj = str.charAt(j);
                    if ((cj === "'" || cj === '"') && opensBlobQuote(str, j, i + 1)) {
                        let close = blobQuoteClose(str, j, true);
                        if (close > -1) {
                            j = close + 1;   // a quoted value: its brackets are data
                            continue;
                        }
                    }
                    if (cj === '[') {
                        ++depth;
                    } else if (cj === ']') {
                        --depth;
                    }
                    if (depth > 0) {
                        ++j;
                    }
                }
                let content = str.substring(i + 1, j);
                if (/^\s*&/.test(content)) {
                    out += '[@' + blobs.length + ']';
                    blobs.push(content.trim());
                } else {
                    out += str.substring(i, Math.min(j + 1, str.length));
                }
                i = j;
            } else {
                out += c;
            }
        }
        return {text: out, blobs: blobs};
    }

    function pushConfidence(node, value, type, stddev) {
        if (!node.confidences) {
            node.confidences = [];
        }
        let c = {type: type, value: value};
        if (stddev !== undefined && stddev !== null) {
            c.stddev = stddev;
        }
        node.confidences.push(c);
    }

    function nodeTaxonomy0(node) {
        if (!node.taxonomies) {
            node.taxonomies = [{}];
        }
        return node.taxonomies[0];
    }

    function nodeSequence0(node) {
        if (!node.sequences) {
            node.sequences = [{}];
        }
        return node.sequences[0];
    }

    // Split on TOP-LEVEL commas only: a comma inside {...}/[...] sets or
    // inside a quoted VALUE is data, not a separator (height_95%_HPD={1.4,1.5}
    // must stay one token). A quote that does not open a value is itself data
    // (opensBlobQuote): country=Côte d'Ivoire,region=Africa is two fields.
    //
    // TWO quoting rules live here, because two grammars do. In a Nexus
    // TRANSLATE table the things between the commas are LABELS, and a quote
    // opens one wherever it stands (1 'Korea, Republic of'). In a [&...] blob
    // they are key=value fields, and a quote opens only a VALUE. Giving the
    // table the blob's rule split 'Korea, Republic of' in two, which a test
    // caught the moment it was tried; `blob` says which grammar this is.
    function splitTopLevelCommas(s, blob) {
        let out = [];
        let depth = 0;
        let q = null;
        let cur = '';
        for (let i = 0; i < s.length; ++i) {
            let c = s.charAt(i);
            if (blob && (c === "'" || c === '"') && opensBlobQuote(s, i, 0)) {
                let close = blobQuoteClose(s, i, false);
                if (close > -1) {
                    cur += s.substring(i, close + 1);   // a quoted value, its commas data
                    i = close;
                    continue;
                }
            }
            if (q) {
                if (c === q) {
                    q = null;
                }
                cur += c;
            } else if (!blob && (c === "'" || c === '"')) {
                q = c;
                cur += c;
            } else if (c === '{' || c === '[') {
                ++depth;
                cur += c;
            } else if (c === '}' || c === ']') {
                if (depth > 0) {
                    --depth;
                }
                cur += c;
            } else if (c === ',' && depth === 0) {
                out.push(cur);
                cur = '';
            } else {
                cur += c;
            }
        }
        if (cur.length > 0) {
            out.push(cur);
        }
        return out;
    }

    // A NUMBER is a plain decimal with an optional exponent, and nothing else
    // -- the desktop's grammar (Christian, 2026-09-16). The test used to be
    // "parseFloat is finite AND Number is finite", and the two read different
    // languages: Number() understands 0x1A, 0b101 and 0o17, parseFloat() stops
    // at the letter and answers 0. So a trait that merely LOOKED like a hex
    // literal was typed numeric, and a height written that way dated its node
    // at 0 -- a wrong answer where a refusal was due. One pattern now decides,
    // for a value read as a number and for a property's datatype alike.
    const PLAIN_DECIMAL_RE = /^[+-]?(\d+\.?\d*|\.\d+)([eE][+-]?\d+)?$/;

    function parseBeastNumber(v) {
        let t = String(v).trim();
        if (!PLAIN_DECIMAL_RE.test(t)) {
            return null;
        }
        let d = parseFloat(t);
        return isFinite(d) ? d : null;   // 1e400 is well-formed and still not a number we can use
    }

    // FigTree's !color value: #rrggbb, or Java's SIGNED Color.getRGB() int,
    // which FigTree writes whenever the colour came from AWT (real files: every
    // tag in test_trees/influenza.tree is #-8381639, never hex). Each '>>>'
    // coerces to an unsigned 32-bit value first, so the low three bytes come
    // out as RGB regardless of sign; the alpha byte is discarded. Null when it
    // is neither.
    function parseFigTreeColor(value) {
        if (/^#[0-9a-f]{6}$/i.test(value)) {
            return {
                red: parseInt(value.substring(1, 3), 16),
                green: parseInt(value.substring(3, 5), 16),
                blue: parseInt(value.substring(5, 7), 16)
            };
        }
        if (/^#-?[0-9]{1,10}$/.test(value)) {
            let argb = Number(value.substring(1));
            return {red: (argb >>> 16) & 0xff, green: (argb >>> 8) & 0xff, blue: argb & 0xff};
        }
        return null;
    }

    // A two-value BEAST set {lo,hi} (or [lo,hi]) as [lo,hi] numbers, or null.
    function parseBeastInterval(v) {
        let s = v.trim();
        if (s.length < 3 || (s.charAt(0) !== '{' && s.charAt(0) !== '[')) {
            return null;
        }
        let parts = splitTopLevelCommas(s.substring(1, s.length - 1), true);
        if (parts.length !== 2) {
            return null;
        }
        let lo = parseBeastNumber(parts[0].trim());
        let hi = parseBeastNumber(parts[1].trim());
        return (lo !== null && hi !== null) ? [lo, hi] : null;
    }

    // Nexus/Newick quoting: a quoted token is wrapped in a matching pair, and a
    // literal quote INSIDE it is written twice. Reading one back therefore
    // means removing one matching outer pair and un-doubling what is inside --
    // 'Seba''s bat' is the single label "Seba's bat", not "Sebas bat", which is
    // what stripping every quote gave. The doubling is per quote, so a token
    // holding two escapes in a row un-doubles to two literal quotes; a pass
    // that removes quotes wholesale loses both.
    //
    // A token that is NOT well-formed -- an odd number of quotes, no closing
    // quote, a quote in the middle of a bare word -- is not a quoted token at
    // all. Those keep the old lenient behaviour of dropping stray quotes rather
    // than throwing: a viewer that refuses to open a file teaches the user
    // nothing, and Nexus in the wild is written by many programs. Strict on
    // output, lenient on input. Matches the desktop (0.11.141+).
    function unquoteLabel(s) {
        if (s === null || s === undefined) {
            return s;
        }
        let t = String(s).trim();
        let len = t.length;
        if (len > 1) {
            let q = t.charAt(0);
            if ((q === "'" || q === '"') && t.charAt(len - 1) === q) {
                return t.substring(1, len - 1).split(q + q).join(q);
            }
        }
        return t.replace(/['"]+/g, '');
    }

    function stripValueQuotes(v) {
        if (v.length >= 2
            && ((v.charAt(0) === '"' && v.charAt(v.length - 1) === '"')
                || (v.charAt(0) === "'" && v.charAt(v.length - 1) === "'"))) {
            return v.substring(1, v.length - 1);
        }
        return v;
    }

    // A property-ref-safe rendering of a BEAST key: keep letters/digits,
    // collapse every other run to one underscore, drop a trailing one
    // (rate_95%_HPD -> a clean beast:rate_95_HPD ref).
    function beastRefKey(key) {
        return key.replace(/[^A-Za-z0-9]+/g, '_').replace(/_$/, '');
    }

    // A BEAST / BEAST X / TreeAnnotator / FigTree / MrBayes [&...] blob onto
    // one node, each field mapped to the phyloXML structure the existing
    // display features consume:
    //  - posterior -> a confidence of type "posterior"; MrBayes prob (+
    //    prob_stddev) -> "posterior probability"; bootstrap -> "bootstrap";
    //  - node age height/height_mean/height_median + height_95%_HPD (or
    //    height_range) + date -> node.date value/min/max/desc (the node-age
    //    HPD bars draw the interval);
    //  - Auspice's "download Nexus" vocabulary lands exactly where
    //    parseAuspiceJson puts the same dataset, so one Nextstrain build opens
    //    the same way whichever format it was saved in: num_date -> the date
    //    VALUE with unit "year", plus a nextstrain:num_date property;
    //    num_date_CI={lo,hi} -> that date's minimum/maximum, on a tip too
    //    (there it is the sampling-date uncertainty); div -> a
    //    nextstrain:div property. A num_date outranks every height* (it is a
    //    calendar year, a height is an age before present), it alone carries
    //    the unit, and it never borrows the height's HPD as its interval;
    //  - FigTree !color=#rrggbb -> the branch color;
    //  - every other field (rate, length_*, traits, location, ...) -> a
    //    beast:<key> node property (numeric -> xsd:decimal, so Color-by
    //    picks it up).
    // The branch length lives on the Newick ":length" and is left untouched.
    // A malformed field is skipped, never aborting the parse.
    function applyBeastAnnotations(node, blob) {
        let heightMedian = null;
        let heightMean = null;
        let height = null;
        let hpd = null;
        let range = null;
        let dateDesc = null;
        let hpdText = null;
        let rangeText = null;
        let numDate = null;
        let numDateCi = null;
        let numDateCiKey = null;
        let prob = null;
        let probSd = null;
        splitTopLevelCommas(blob, true).forEach(function (token) {
            let eq = token.indexOf('=');
            if (eq <= 0) {
                return;
            }
            let key = token.substring(0, eq).trim();
            let value = stripValueQuotes(token.substring(eq + 1).trim());
            if (key.length === 0 || value.length === 0) {
                return;
            }
            let kl = key.toLowerCase();
            if (kl === 'posterior') {
                let d = parseBeastNumber(value);
                if (d !== null) {
                    pushConfidence(node, d, 'posterior');
                }
            } else if (kl === 'prob') {
                prob = parseBeastNumber(value);
            } else if (kl === 'prob_stddev') {
                probSd = parseBeastNumber(value);
            } else if (kl === 'bootstrap') {
                let b = parseBeastNumber(value);
                if (b !== null) {
                    pushConfidence(node, b, 'bootstrap');
                }
            } else if ((kl === '!color' || kl === '!colour') && parseFigTreeColor(value) !== null) {
                node.color = parseFigTreeColor(value);   // in the tree string it is the BRANCH colour
            } else if (kl === 'height_median') {
                heightMedian = value;
            } else if (kl === 'height_mean') {
                heightMean = value;
            } else if (kl === 'height') {
                height = value;
            } else if (kl === 'height_95%_hpd') {
                hpd = parseBeastInterval(value);
                hpdText = value;
            } else if (kl === 'height_range') {
                range = parseBeastInterval(value);
                rangeText = value;
            } else if (kl === 'date') {
                dateDesc = value;
            } else if (kl === 'num_date') {
                numDate = value;
            } else if (kl === 'num_date_ci') {
                numDateCi = value;
                numDateCiKey = beastRefKey(key);
            } else if (kl === 'div' && parseBeastNumber(value) !== null) {
                addNodeProperty(node, NEXTSTRAIN_PREFIX + 'div', value);
            } else if (key.charAt(0) === '!') {
                // A key that starts with '!' is one of FigTree's display
                // DIRECTIVES -- !color, !rotate, !collapse, !hilight, !name --
                // never a measurement, so it is never typed numeric. It
                // matters for the forms we refuse as a colour: !color=-8381639
                // (no '#') used to land as beast:_color typed xsd:decimal, and
                // Color-by offered FigTree's paint as a gradient. It is still
                // KEPT -- nothing in this reader throws data away -- under the
                // same ref, as text. A user's own trait called "color" has no
                // '!' and stays an ordinary trait. (The desktop's rule;
                // Christian, 2026-09-17: "do the same".)
                addNodeProperty(node, 'beast:' + beastRefKey(key), value, 'xsd:string');
            } else if (kl === 'mutations' || kl === 'mcc') {
                // TEXT, whatever it looks like (the desktop forces the same):
                // a list of mutations that happens to hold one number, or a
                // clade label that happens to be "3", is not a measurement,
                // and typing it decimal offers it to Color-by as a gradient
                addNodeProperty(node, 'beast:' + beastRefKey(key), value, 'xsd:string');
            } else {
                addNodeProperty(node, 'beast:' + beastRefKey(key), value);
            }
        });
        if (prob !== null) {
            pushConfidence(node, prob, 'posterior probability', probSd);
        }
        // A num_date that parses is the node's date, and nothing about a
        // height may touch it. One that does not parse is just a field: it and
        // its interval fall back to plain text, as any unknown key does.
        let year = (numDate !== null) ? parseBeastNumber(numDate) : null;
        let yearCi = (numDateCi !== null) ? parseBeastInterval(numDateCi) : null;
        if (numDate !== null) {
            addNodeProperty(node, (year !== null ? NEXTSTRAIN_PREFIX : 'beast:') + 'num_date', numDate);
        }
        if (numDateCi !== null && (year === null || yearCi === null)) {
            // an interval with no date to bracket, or not an interval at all
            addNodeProperty(node, (yearCi !== null ? NEXTSTRAIN_PREFIX : 'beast:') + numDateCiKey, numDateCi);
        }
        let date = {};
        if (year !== null) {
            date.value = year;
            date.unit = 'year';
            if (yearCi !== null) {
                date.minimum = yearCi[0];
                date.maximum = yearCi[1];
            }
            // provisional until the whole tree has been read: settleNumDates
            // decides whether this tree is time-scaled at all
            node._numDate = {ciKey: numDateCiKey, ciText: yearCi !== null ? numDateCi : null};
            // the heights it outranked are kept as what they were written as,
            // rather than dropped: no real file carries both, so nothing here
            // is lost to a guess
            [['height_median', heightMedian], ['height_mean', heightMean], ['height', height],
                ['height_95_HPD', hpdText], ['height_range', rangeText]].forEach(function (h) {
                if (h[1] !== null) {
                    addNodeProperty(node, 'beast:' + h[0], h[1]);
                }
            });
        } else {
            // age preference: median, then mean, then height -- and each piece
            // parsed independently, so an unparseable point value never
            // discards a valid {lo,hi} interval
            let v = heightMedian !== null ? heightMedian
                : (heightMean !== null ? heightMean : height);
            let dv = (v !== null) ? parseBeastNumber(v) : null;
            let interval = hpd || range;
            if (dv !== null) {
                date.value = dv;
            }
            if (interval) {
                date.minimum = interval[0];
                date.maximum = interval[1];
            }
        }
        if (dateDesc !== null) {
            date.desc = dateDesc;
        }
        if (Object.keys(date).length > 0) {
            node.date = date;
        }
    }

    // The classic NHX tag set, as the desktop maps it: S= taxonomy
    // scientific name, T= taxonomy id, B= support confidence, D= a
    // duplication (Y/T) / speciation (N/F) / undecided (?) event, GN=
    // sequence name, AC= sequence accession, C= an nh:comment property.
    // Unknown tags (and DS= domain structures) are ignored.
    function applyNhxTags(node, fields) {
        fields.forEach(function (tag) {
            let t = tag.trim();
            if (t.length < 3) {
                return;
            }
            if (t.startsWith('S=')) {
                nodeTaxonomy0(node).scientific_name = t.substring(2);
            } else if (t.startsWith('T=')) {
                nodeTaxonomy0(node).id = {value: t.substring(2)};
            } else if (t.startsWith('B=')) {
                let b = parseBeastNumber(t.substring(2));
                if (b !== null) {
                    pushConfidence(node, b, 'bootstrap');
                }
            } else if (t.startsWith('D=')) {
                let c = t.charAt(2);
                if (c === 'Y' || c === 'T') {
                    node.events = {duplications: 1};
                } else if (c === 'N' || c === 'F') {
                    node.events = {speciations: 1};
                } else if (c === '?') {
                    node.events = {type: 'speciation_or_duplication'};
                }
            } else if (t.startsWith('GN=')) {
                nodeSequence0(node).name = t.substring(3);
            } else if (t.startsWith('AC=')) {
                nodeSequence0(node).accession = {value: t.substring(3), source: '?'};
            } else if (t.startsWith('C=')) {
                addNodeProperty(node, 'nh:comment', t.substring(2));
            }
        });
    }

    // Inside a legacy [&&NHX:...] tag the desktop reads by its LABEL rule, and
    // so do we (Christian, 2026-09-16: "do what desktop does"):
    //  - UNQUOTED whitespace is formatting noise and is squeezed out -- its
    //    Test.testNHXParsingQuotes pins "[\t&\t&\n N\tH\tX:S=mo\tnkey !]" as
    //    S=monkey!, and S=Homo sapiens reads as Homosapiens;
    //  - a QUOTED run, either style, keeps what is inside it -- S="homo sapiens"
    //    is homo sapiens, the one way to put a two-word species into an NHX tag
    //    -- with a run of whitespace collapsed to one space, and may carry the
    //    ':' that would otherwise end the tag (S="a:b c":D=Y);
    //  - the quote characters themselves are never part of the value.
    // My first version squeezed quotes and ALL whitespace out of the blob, from
    // an inference off that one pinned case; the desktop then MEASURED its own
    // behaviour and the quoted forms differed (homosapiens here, homo sapiens
    // there). The opposite of a single-& blob, where quotes and spaces are
    // data -- so none of this runs until the blob has shown itself to be NHX.
    function nhxFields(blob) {
        let out = [];
        let cur = '';
        let q = null;
        let spaced = false;
        for (let i = 0; i < blob.length; ++i) {
            let c = blob.charAt(i);
            if (q) {
                if (c === q) {
                    q = null;
                } else if (/\s/.test(c)) {
                    if (!spaced) {
                        cur += ' ';
                        spaced = true;
                    }
                } else {
                    cur += c;
                    spaced = false;
                }
            } else if (c === "'" || c === '"') {
                q = c;
                spaced = false;
            } else if (c === ':') {
                out.push(cur);
                cur = '';
            } else if (!/\s/.test(c)) {
                cur += c;
            }
        }
        out.push(cur);
        return out;
    }

    function applyExtendedAnnotations(node, blob) {
        let fields = nhxFields(blob);
        if (/^&&NHX$/i.test(fields[0])) {
            applyNhxTags(node, fields.slice(1));
        } else {
            applyBeastAnnotations(node, blob.replace(/^&/, ''));
        }
    }

    // ---------------------------------------------------------------
    // A bare numeric date= as a node date VALUE (TreeTime)
    // ---------------------------------------------------------------
    //
    // applyBeastAnnotations files date= as a date DESC and nothing else, which
    // is what the desktop's BeastAnnotationParser does and stays that way --
    // in BEAST output the age lives in height*, and date= is a decoration.
    //
    // TreeTime has no height at all: it writes "[&mutations=...,date=2003.84]"
    // and the decimal year IS the node's position in time. Left as a desc the
    // tree carries no date value, so isTimeTree is false and the calendar axis
    // never appears -- a time tree that does not look like one.
    //
    // The catch is that TreeTime writes that SAME comment on both trees it
    // emits: timetree.nexus, whose branch lengths are years, and
    // divergence_tree.nexus, whose branch lengths are substitutions. No single
    // annotation says which file it came from, and promoting blindly would put
    // a calendar axis (which maps one branch-length unit to one year) on a
    // divergence tree and silently disable re-rooting for it.
    //
    // So the TREE is asked rather than the annotation sniffed: a numeric date
    // becomes a value only where the parent-to-child date differences actually
    // reproduce the branch lengths. That needs no format detection, it is
    // self-validating on any input, and it separates TreeTime's two files
    // exactly. The desc is left in place either way, so nothing is lost.
    const NUMERIC_DATE_ABS_TOL = 0.02;  // date= is written to 2 decimals, so a
    const NUMERIC_DATE_REL_TOL = 0.01;  // difference of two carries ~0.01 error

    function numericDateDesc(n) {
        if (!n.date || n.date.value !== undefined || typeof n.date.desc !== 'string') {
            return null;
        }
        return parseBeastNumber(n.date.desc);
    }

    // THE PAIR COUNT, shared by the date= promotion below and by
    // settleNumDates, so the two can never drift: a PAIR is a dated node, its
    // dated DIRECT parent and a branch length between them, and it AGREES
    // when the year difference reproduces that length within the tolerances.
    //
    // A pair must also be INFORMATIVE: max(|dYear|, |length|) > the absolute
    // tolerance. One that is not cannot tell years from substitutions at all
    // -- both numbers sit inside the tolerance, so it "agrees" whatever the
    // tree is measured in -- and it is left out of BOTH counts. That is not a
    // nicety. The 0.02 tolerance is larger than a dense tree's substitution
    // lengths, so on a densely sampled DIVERGENCE tree such pairs pile up as
    // agreement. Measured by rebuilding real Auspice time-tree exports as
    // their divergence trees (length = the div difference, same num_dates):
    //                 agreeing, every pair       informative pairs only
    //   H5N1 (2 y)    3620 of 9205  (39.3%)      1 of 5586
    //   chikungunya    672 of 2645  (25.4%)      0 of 1973
    //   measles        915 of 5388  (17.0%)      0 of 4473
    // while every time tree stays at 100% either way (H5N1 5586 of 5586).
    // 39% is under the majority, so nothing visible changed on those files;
    // it is headroom -- a denser build would have crossed it and been dated
    // on substitutions. Found by a review on the desktop, reproduced here to
    // the pair, and a JOINT RULE (Christian, 2026-09-17, both sessions). The
    // burden of proof is untouched: with no informative pair at all a date=
    // is still not promoted and a num_date still stands.
    function countDatePairs(root, yearOf) {
        let dated = [];
        let agree = 0;
        let pairs = 0;
        let stack = [[root, null]];
        while (stack.length > 0) {
            let top = stack.pop();
            let n = top[0];
            let year = yearOf(n);
            if (year !== null) {
                dated.push([n, year]);
                let parentYear = top[1];
                if (parentYear !== null && typeof n.branch_length === 'number'
                    && isFinite(n.branch_length)) {
                    let dYear = year - parentYear;
                    if (Math.max(Math.abs(dYear), Math.abs(n.branch_length)) > NUMERIC_DATE_ABS_TOL) {
                        ++pairs;
                        let tol = NUMERIC_DATE_ABS_TOL
                            + NUMERIC_DATE_REL_TOL * Math.abs(n.branch_length);
                        if (Math.abs(dYear - n.branch_length) <= tol) {
                            ++agree;
                        }
                    }
                }
            }
            if (n.children) {
                for (let i = 0; i < n.children.length; ++i) {
                    stack.push([n.children[i], year]);
                }
            }
        }
        return {dated: dated, pairs: pairs, agree: agree};
    }

    function promoteTimeScaledDates(phy) {
        let root = forester.getTreeRoot(phy);
        if (!root) {
            return;
        }
        let counted = countDatePairs(root, numericDateDesc);
        let dated = counted.dated;
        let pairs = counted.pairs;
        let agree = counted.agree;
        if (pairs < 2 || agree * 2 <= pairs) {
            return;
        }
        dated.forEach(function (d) {
            d[0].date.value = d[1];
            d[0].date.unit = 'year';
        });
    }

    // ---------------------------------------------------------------
    // An Auspice num_date is a date VALUE only on a time-scaled tree
    // ---------------------------------------------------------------
    //
    // Auspice's "download Nexus" offers the SAME annotations on two trees: the
    // time tree (…_timetree.nexus, branch lengths in years) and the divergence
    // tree (…_tree.nexus, branch lengths in substitutions). A date value is
    // what makes a tree a time tree here -- isTimeTree counts them -- and the
    // calendar axis maps one branch-length unit to one year, so dating the
    // divergence export would hang that axis on a tree measured in
    // substitutions and refuse its re-rooting. Measured on real exports, the
    // parent-to-child num_date differences reproduce the branch lengths on
    //   measles timetree   5388 of 5388 pairs
    //   chikv timetree     2645 of 2645 pairs
    //   lassa_gpc tree      169 of 2295 pairs (7.4%)
    // so this is the question promoteTimeScaledDates already asks of a
    // TreeTime date=, with the same pinned tolerances, put to the tree rather
    // than guessed from a file name. The difference is the burden of proof: a
    // date= may not be a value at all, so it needs evidence FOR; a num_date is
    // a date by its very name, so it stands unless there is evidence AGAINST
    // -- two comparable pairs or more, and no strict majority agreeing. A tree
    // too small to say anything keeps its dates.
    //
    // A JOINT RULE with the desktop (Christian, 2026-09-16), chosen over
    // keeping it here alone, over dating unconditionally, and over moving the
    // question up into isTimeTree -- which would have changed that answer for
    // every dated input, phyloXML and BEAST included. Do not retune it alone.
    //
    // Where the tree is not time-scaled nothing is lost: the year stays on
    // the node as nextstrain:num_date (numeric, so Color-by and search have
    // it) and its interval as nextstrain:num_date_CI, exactly what an
    // interval with no date to bracket becomes anyway.
    function settleNumDates(phy) {
        let root = forester.getTreeRoot(phy);
        if (!root) {
            return;
        }
        let counted = countDatePairs(root, function (n) {
            return n._numDate ? n.date.value : null;
        });
        let marked = counted.dated.map(function (d) {
            return d[0];
        });
        let pairs = counted.pairs;
        let agree = counted.agree;
        let notTimeScaled = pairs >= 2 && agree * 2 <= pairs;
        marked.forEach(function (n) {
            let m = n._numDate;
            delete n._numDate;
            if (!notTimeScaled) {
                // A TIP keeps its interval too. It used to be dropped here
                // and in parseAuspiceJson, for a DISPLAY reason -- a bar on a
                // tip read as a fossil range -- and that threw real data away:
                // a sample dated only to its month or year. Counted on real
                // exports: dengue 2347 of 3863 tips carry a genuine interval
                // (median 0.78 y), measles 1390 of 2985, enterovirus 715 of
                // 1600. The display now tells a sampled tip from a fossil
                // instead (drawTimeAxis); Christian, 2026-09-17, both programs.
                return;
            }
            delete n.date.value;
            delete n.date.unit;
            delete n.date.minimum;
            delete n.date.maximum;
            if (Object.keys(n.date).length === 0) {
                delete n.date;
            }
            if (m.ciText !== null) {
                addNodeProperty(n, NEXTSTRAIN_PREFIX + m.ciKey, m.ciText);
            }
        });
    }

    // ---------------------------------------------------------------
    // TreeTime's own namespace
    // ---------------------------------------------------------------
    //
    // TreeTime's annotations arrive through the BEAST path, so they were
    // landing as beast:<key> -- accurate about the syntax, wrong about the
    // producer, and confusing next to a real BEAST run. Christian asked for a
    // namespace of their own (2026-09-16).
    //
    // The producer is recognised on the TREE, not the file: a TreeTime tree
    // carries mutations= and no node age at all, where every BEAST/MrBayes
    // run states an age (height, height_mean, height_median, and the
    // height_95%_HPD / height_range intervals). So the test is "mutations
    // present, age absent", which cannot fire on a BEAST file and leaves that
    // shared contract with the desktop untouched.
    //
    // TreeTime's mugration output is a bare user-named trait -- [&region="x"]
    // and nothing else -- which no rule could attribute to any producer. It
    // keeps the generic namespace, correctly.
    const TREETIME_PREFIX = 'treetime:';
    const BEAST_PREFIX = 'beast:';

    // Must run BEFORE promoteTimeScaledDates: until then a date VALUE can
    // only have come from a BEAST height field or an Auspice num_date, and
    // either one says this is not TreeTime's own Nexus.
    function renameTreeTimeProperties(phy) {
        let nodes = forester.getAllNodes(phy);
        let mutations = false;
        for (let i = 0; i < nodes.length; ++i) {
            let d = nodes[i].date;
            if (d && (d.value !== undefined || d.minimum !== undefined
                || d.maximum !== undefined)) {
                return;
            }
            if (!mutations && nodes[i].properties) {
                mutations = nodes[i].properties.some(function (p) {
                    return p.ref === BEAST_PREFIX + 'mutations';
                });
            }
        }
        if (!mutations) {
            return;
        }
        nodes.forEach(function (n) {
            if (n.properties) {
                n.properties.forEach(function (p) {
                    if (typeof p.ref === 'string' && p.ref.indexOf(BEAST_PREFIX) === 0) {
                        p.ref = TREETIME_PREFIX + p.ref.substring(BEAST_PREFIX.length);
                    }
                });
            }
        });
    }

    // The namespace this tree's bracket annotations ended up in, for anything
    // added to it AFTER the pass above has run -- the Nexus reader hangs a
    // taxon's refused colour on its tip once the tree string is parsed, and
    // filed it under beast: on a tree the pass had just renamed: one tree, two
    // namespaces. The rename is all-or-nothing, so the tree already says which
    // it took; and asking it, rather than running the pass a second time, is
    // deliberate -- after promoteTimeScaledDates a TreeTime tree HAS date
    // values, and a second run would read that as "not TreeTime's own".
    // (Sound only while nothing read from a taxon can sway the decision; we
    // read !color alone there. The desktop reads the whole blob, and so has to
    // run its pass after the taxlabels instead.)
    function annotationPrefix(phy) {
        let nodes = forester.getAllNodes(phy);
        for (let i = 0; i < nodes.length; ++i) {
            let props = nodes[i].properties;
            if (props) {
                for (let j = 0; j < props.length; ++j) {
                    if (typeof props[j].ref === 'string' && props[j].ref.indexOf(TREETIME_PREFIX) === 0) {
                        return TREETIME_PREFIX;
                    }
                }
            }
        }
        return BEAST_PREFIX;
    }

    forester.parseNewHampshire = function (nhStr, confidenceValuesInBrackets, confidenceValuesAsInternalNames) {

        let NH_FORMAT_ERR_OPEN_PARENS = NH_FORMAT_ERR + 'likely cause: number of open parentheses is larger than number of close parentheses';
        let NH_FORMAT_ERR_CLOSE_PARENS = NH_FORMAT_ERR + 'likely cause: number of close parentheses is larger than number of open parentheses';

        if (confidenceValuesInBrackets === undefined) {
            confidenceValuesInBrackets = true;
        }
        if (confidenceValuesAsInternalNames === undefined) {
            confidenceValuesAsInternalNames = false;
        }
        // The two options used to be mutually exclusive and throwing. They are
        // not: brackets are consumed by the tokenizer, bare labels are handled
        // afterwards, so a file mixing the two dialects is read correctly with
        // both on. forester-Java never had this guard, so it was never part of
        // the shared contract -- and throwing on a combination a user
        // plausibly wants is what forced callers into workarounds.

        // A text holding several trees (one per ';') reads as its FIRST;
        // parseNewHampshireTrees reads them all. Before this the statements
        // ran together and the LAST tree came back, the others silently
        // dropped.
        {
            let statements = forester.splitNewHampshire(nhStr);
            if (statements.length > 1) {
                nhStr = statements[0];
            }
        }

        let ancs = [];
        let x = {};

        // [&...] annotation blobs (BEAST-style key=value, NHX) are pulled
        // out BEFORE tokenizing -- their commas/colons/quotes are data --
        // and re-attached to their node via the [@N] markers below
        let extracted = extractBracketAnnotations(nhStr);
        let sss = extracted.text;
        let annotations = extracted.blobs;

        let ss = sss.split(/(;|\(|\)|,|:|"|')/);
        let ssl = ss.length;
        let in_double_q = false;
        let in_single_q = false;
        let buffer = '';
        // In a quoted label a literal quote is DOUBLED. The tokenizer splits
        // on quotes, so a doubled one arrives as two quote elements with only
        // empty strings between them -- that is the signature. Seeing it means
        // 'emit one quote and stay inside the run', not 'close the run': the
        // run continuing is what keeps the label in one piece, and emitting
        // the character is what stops the apostrophe being swallowed.
        let doubledQuoteEnd = function (at, qch) {
            let j = at + 1;
            while (j < ssl && ss[j] === '') {
                ++j;
            }
            return (j < ssl && ss[j] === qch) ? j : -1;
        };
        for (let i = 0; i < ssl; ++i) {
            let element = ss[i].replace(/\s+/g, '');

            if (element === '"' && !in_single_q) {
                if (!in_double_q) {
                    in_double_q = true;
                } else {
                    let dq = doubledQuoteEnd(i, '"');
                    if (dq > -1) {
                        buffer += '"';
                        i = dq;
                    } else {
                        in_double_q = false;
                        if (x.name && x.name.length > 0) {
                            x.name = x.name + buffer;
                        } else {
                            x.name = buffer;
                        }
                        buffer = '';
                    }
                }
            } else if (element === "'" && !in_double_q) {
                if (!in_single_q) {
                    in_single_q = true;
                } else {
                    let dq = doubledQuoteEnd(i, "'");
                    if (dq > -1) {
                        buffer += "'";
                        i = dq;
                    } else {
                        in_single_q = false;
                        if (x.name && x.name.length > 0) {
                            x.name = x.name + buffer;
                        } else {
                            x.name = buffer;
                        }
                        buffer = '';
                    }
                }
            } else {
                if (in_double_q || in_single_q) {
                    buffer += ss[i].replace(/\s+/g, ' ');
                } else {
                    if (element === '(') {
                        if (!x) {
                            throw (NH_FORMAT_ERR_CLOSE_PARENS);
                        }
                        let subtree1 = {};
                        x.children = [subtree1];
                        ancs.push(x);
                        x = subtree1;
                    } else if (element === ',') {
                        if (ancs.length === 0) {
                            throw (NH_FORMAT_ERR_CLOSE_PARENS);
                        }
                        let subtree2 = {};
                        ancs[ancs.length - 1].children.push(subtree2);
                        x = subtree2;
                    } else if (element === ')') {
                        x = ancs.pop();
                    } else if (element === ':') {
                        // the separator before a branch length: the length itself
                        // is read by the branch below, so there is nothing to do here
                    } else {
                        // What came before decides what this element is: a name
                        // follows an opening bracket, a comma or a close, and a
                        // branch length follows a colon.
                        //
                        // At i === 0 there IS no previous token, and the string
                        // beginning with a label is the one-node tree "a;" --
                        // valid Newick, and for want of this line its name was
                        // read as nothing at all and written back as "". The
                        // start of input opens the tree, so it counts as '('.
                        let e = (i === 0) ? '(' : ss[i - 1];
                        if (e) {
                            e = e.trim();
                            // re-attach any annotation blobs riding on this
                            // element (name, branch length, or standalone) to
                            // the current node, and drop the markers
                            if (annotations.length > 0 && element.indexOf('[@') > -1) {
                                element = element.replace(/\[@(\d+)\]/g, function (m, k) {
                                    let blob = annotations[+k];
                                    if (blob !== undefined) {
                                        applyExtendedAnnotations(x, blob);
                                    }
                                    return '';
                                });
                            }
                            if ((e === ')') || (e === '(') || (e === ',')) {
                                if (element && element.length > 0) {
                                    if (element.charAt(element.length - 1) === "]") {
                                        let o = element.indexOf('[');
                                        if (o > -1) {
                                            if (confidenceValuesInBrackets === true) {
                                                addConfidence(x, element);
                                            }
                                            x.name = element.substring(0, o);
                                        } else {
                                            x.name = element;
                                        }
                                    } else {
                                        x.name = element;
                                        let op = x.name.indexOf('[');
                                        if (op > -1) {
                                            let cl = x.name.indexOf(']');
                                            if (cl > op) {
                                                x.name = x.name.substring(0, op) + x.name.substring(cl + 1, x.name.length);
                                            }
                                        }
                                    }
                                }
                            } else if (e === ':') {
                                if (element && element.length > 0) {
                                    if (element.charAt(element.length - 1) === ']') {
                                        let o1 = element.indexOf('[');
                                        if (o1 > -1) {
                                            if (confidenceValuesInBrackets === true) {
                                                addConfidence(x, element);
                                            }
                                            let bl = parseFloat(element.substring(0, o1));
                                            if (forester.isNumber(bl)) {
                                                x.branch_length = bl;
                                            }
                                        }
                                    } else {
                                        let b = parseFloat(element);
                                        if (forester.isNumber(b)) {
                                            x.branch_length = b;
                                        } else {
                                            throw (NH_FORMAT_ERR + 'could not parse branch-length from "' + element + '"');
                                        }
                                    }
                                }
                            } else if (e === '"' || e === "'") {
                                if ((element && element.length > 0) && (x.name && x.name.length > 0)) {
                                    if (element.charAt(element.length - 1) === "]") {
                                        let opp = element.indexOf('[');
                                        if (opp > -1) {
                                            if (confidenceValuesInBrackets === true) {
                                                addConfidence(x, element);
                                            }
                                            x.name = x.name + element.substring(0, opp);
                                        } else {
                                            x.name = x.name + element;
                                        }
                                    } else {
                                        x.name = x.name + element;
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
        if (ancs.length !== 0) {
            throw (NH_FORMAT_ERR_OPEN_PARENS);
        }
        if (!x) {
            throw (NH_FORMAT_ERR_CLOSE_PARENS);
        }

        let phy = {};
        phy.children = [x];

        forester.addParents(phy);

        if (confidenceValuesAsInternalNames === true) {
            moveInternalNodeNamesToConfidenceValues(phy);
        }

        renameTreeTimeProperties(phy);   // first: a provisional num_date still says "not TreeTime's own"
        settleNumDates(phy);
        promoteTimeScaledDates(phy);

        return phy;

        function addConfidence(x, element) {
            let confValue = parseConfidence(element);
            if (confValue != null) {
                x.confidences = [];
                let conf = {};
                conf.value = confValue;
                conf.type = 'unknown';
                x.confidences.push(conf);
            }
        }

        function parseConfidence(element) {
            let o = element.indexOf('[');
            if (o > -1) {
                let s = element.substring(o + 1, element.length - 1);
                if (NUMBERS_ONLY_PATTERN.test(s)) {
                    let confValue = parseFloat(s);
                    if (forester.isNumber(confValue)) {
                        return confValue;
                    } else {
                        throw (NH_FORMAT_ERR + 'could not parse confidence value from "' + element + '"');
                    }
                }
            }
            return null;
        }

        function moveInternalNodeNamesToConfidenceValues(node) {
            forester.preOrderTraversalAll(node, function (n) {
                if (n.children) {
                    if (n.name) {
                        let s = n.name;
                        if (NUMBERS_ONLY_PATTERN.test(s)) {
                            let confValue = parseFloat(s);
                            if ((confValue != null) && (forester.isNumber(confValue))) {
                                n.confidences = [];
                                let conf1 = {};
                                conf1.value = confValue;
                                conf1.type = 'unknown';
                                n.confidences.push(conf1);
                                n.name = undefined;
                            }
                        }
                    }
                }
            });
        }
    };

    // Splits a New Hampshire text into its tree statements, one per ';'
    // outside quotes and [...] comments (a ';' inside a quoted label or a
    // [&...] annotation is data), each trimmed; blank statements are
    // dropped. A text without a terminating ';' is one statement.
    forester.splitNewHampshire = function (nhStr) {
        let s = String(nhStr);
        let parts = [];
        let start = 0;
        let inSingle = false;
        let inDouble = false;
        let depth = 0;
        for (let i = 0, n = s.length; i < n; ++i) {
            let c = s.charAt(i);
            if (inSingle) {
                if (c === "'") {
                    inSingle = false;
                }
            } else if (inDouble) {
                if (c === '"') {
                    inDouble = false;
                }
            } else if (depth > 0) {
                if (c === ']') {
                    --depth;
                } else if (c === '[') {
                    ++depth;
                }
            } else if (c === "'") {
                inSingle = true;
            } else if (c === '"') {
                inDouble = true;
            } else if (c === '[') {
                depth = 1;
            } else if (c === ';') {
                parts.push(s.substring(start, i + 1));
                start = i + 1;
            }
        }
        parts.push(s.substring(start));
        return parts.map(function (p) {
            return p.trim();
        }).filter(function (p) {
            return p.length > 0;
        });
    };

    // Every tree in a New Hampshire text (a file can hold many, one per
    // ';'), each parsed as parseNewHampshire does, in file order. A text
    // with no statement at all is handed to parseNewHampshire whole, so it
    // fails the way an empty tree always did.
    forester.parseNewHampshireTrees = function (nhStr, confidenceValuesInBrackets, confidenceValuesAsInternalNames) {
        let statements = forester.splitNewHampshire(nhStr);
        if (statements.length === 0) {
            return [forester.parseNewHampshire(nhStr, confidenceValuesInBrackets, confidenceValuesAsInternalNames)];
        }
        return statements.map(function (statement) {
            return forester.parseNewHampshire(statement, confidenceValuesInBrackets, confidenceValuesAsInternalNames);
        });
    };

    // Residue normalization for a Nexus character matrix, ported from the
    // desktop's BasicSequence.createAaSequence / createDnaSequence /
    // createRnaSequence, which is what its Nexus reader builds a sequence
    // with. Three steps, in this order:
    //   1. uppercase;
    //   2. '.' becomes the gap '-'  (before step 3, or it would become X/N);
    //   3. anything outside the type's alphabet becomes the unspecified
    //      residue, 'X' for protein and 'N' for nucleotides.
    // So the Nexus missing symbol '?' arrives as X or N, which is what
    // Christian decided on 2026-09-23 ("normalize to X, same as desktop").
    // '?' is simply one character outside the alphabet; doing it alone would
    // have left us differing on '.' and on stray letters instead.
    //
    // This is the NEXUS MATRIX reader only. A phyloXML <mol_seq> is kept
    // exactly as written by both programs -- measured, not assumed: the
    // desktop reads "MK??AL.N" out of phyloXML unchanged.
    const AA_NOT_ALPHABET = /[^ARNDBCQEZGHILKMFPSTWYVXUO\-*]/g;
    const DNA_NOT_ALPHABET = /[^ACGTRYMKWSN\-*]/g;
    const RNA_NOT_ALPHABET = /[^ACGURYMKWSN\-*]/g;

    function normalizeMatrixResidues(block, datatype) {
        let out = block.toUpperCase().replace(/\./g, '-');
        if (datatype === 'protein') {
            return out.replace(AA_NOT_ALPHABET, 'X');
        }
        if (datatype === 'dna') {
            return out.replace(DNA_NOT_ALPHABET, 'N');
        }
        if (datatype === 'rna') {
            return out.replace(RNA_NOT_ALPHABET, 'N');
        }
        return out;
    }

    // Parses a Nexus-formatted string and returns an ARRAY of tree objects,
    // each in the same shape parseNewHampshire produces (a Nexus file can
    // hold any number of trees). Ported from the desktop's
    // NexusPhylogeniesParser: reads TAXLABELS, the TREES block (TRANSLATE
    // tables, TREE/UTREE statements, [&R]/[&U] rootedness, tree names and
    // titles) and CHARACTERS/DATA blocks -- a protein/dna/rna MATRIX
    // (sequential or interleaved, MATCHCHAR resolved, quoted labels,
    // comments stripped) becomes per-tip aligned molecular sequences in the
    // phyloXML shape (sequences[i].mol_seq.{is_aligned,value}), so a tree
    // read from Nexus shows its alignment track exactly like one read from
    // phyloXML. The two confidence options are handed through to
    // parseNewHampshire for each tree statement.
    forester.parseNexus = function (nexStr, confidenceValuesInBrackets, confidenceValuesAsInternalNames) {
        const NEXUS_FORMAT_ERR = 'Nexus format error: ';
        const TITLE_RE = /^title.?\s+([^;]+)/i;
        const TREE_NAME_RE = /^\s*.?tree\s+(.+?)\s*=/i;
        const ROOTEDNESS_RE = /=\s*\[&([RU])\]/i;
        const TRANSLATE_PAIR_RE = /([0-9A-Za-z]+)\s+(.+)/;
        const RESIDUES_RE = /^[A-Za-z\-_*?.]+$/;
        const DATATYPE_RE = /datatype\s*=\s*([a-z]+)/;
        const MATCHCHAR_RE = /matchchar\s*=\s*['"]?(\S)/;
        // the desktop's patterns, character for character: one optional space
        // either side of the '=', then the first non-space as the symbol
        const MISSING_RE = /missing\s?.\s?(\S)/;
        const GAP_RE = /gap\s?.\s?(\S)/;

        let trees = [];
        let taxlabels = [];
        let taxlabelColors = Object.create(null);   // label -> #rrggbb, from 'name'[&!color=...]
        let taxlabelColorsByKey = Object.create(null);   // the same, under the Nexus join key
        let taxlabelRefused = Object.create(null);   // label -> a !color value we could not read, kept as text
        let taxlabelRefusedByKey = Object.create(null);
        let taxlabelKeyCount = Object.create(null);   // join key -> how many taxlabels share it, annotated or not
        // null-prototype maps: a taxon named "__proto__" must stay data
        let translateMap = Object.create(null);
        let seqs = Object.create(null);
        let translateBuf = '';
        let nh = '';
        let name = '';
        let title = '';
        let inTreesBlock = false;
        let inTaxalabels = false;
        let inTranslate = false;
        let inTree = false;
        let inDataBlock = false;
        let inMatrix = false;
        let inDataComment = false;
        let datatype = null;
        let rootedInfoPresent = false;
        let isRooted = false;
        let matchchar = null;
        let matrixReferenceId = null;
        // The symbols a block DECLARES for missing data and for a gap. Nexus
        // lets a file choose them, so assuming '?' and '-' reads an all-missing
        // row of a "Missing=N" file as real residues -- the invented-sequence
        // bug the residue scan exists to prevent. Defaults are the conventions.
        let missingChar = '?';
        let gapChar = '-';
        // Matrix ids whose row carried at least one real residue. A row of
        // nothing but the missing and gap symbols states that the taxon has NO
        // data, and must not become a sequence: our own writer emits exactly
        // such rows to keep the matrix rectangular, so without this a save and
        // reopen invents a sequence of question marks for every tip that never
        // had one. Tracked here rather than tested on the finished sequence
        // because the desktop turns '?' into 'X' when it builds an AA
        // sequence, after which a missing row is indistinguishable from a
        // genuinely ambiguous one.
        let seqIdsWithResidues = Object.create(null);

        // Nexus treats '_' and ' ' as equivalent, labels may be quoted, and a
        // matrix often capitalizes taxon names differently from the tree -- so
        // a matrix row joins its tree tip through this canonical key.
        function joinKey(s) {
            return s.replace(/_/g, ' ').replace(/['"]+/g, '').trim().toLowerCase();
        }

        // Strip Nexus [ ... ] comments, tracking an OPEN comment across lines
        // so a multi-line comment inside the matrix cannot leak prose as a
        // spurious taxon row. Called only inside the data block -- the trees
        // block keeps [&R]/[&...], which are semantic there.
        function stripDataComments(s) {
            if (!inDataComment && s.indexOf('[') < 0) {
                return s;
            }
            let out = '';
            for (let i = 0; i < s.length; ++i) {
                let c = s.charAt(i);
                if (inDataComment) {
                    if (c === ']') {
                        inDataComment = false;
                    }
                } else if (c === '[') {
                    inDataComment = true;
                } else {
                    out += c;
                }
            }
            return out.trim();
        }

        function setTranslatePairs(buf) {
            let s = buf.trim();
            if (s.endsWith(';')) {
                s = s.slice(0, -1).trim();
            }
            // splitTopLevelCommas, not a plain split: a quoted label may
            // itself contain a comma ('Korea, Republic of' -- the very case
            // the Auspice writer quotes against elsewhere in this file)
            splitTopLevelCommas(s).forEach(function (pair) {
                let ti = pair.toLowerCase().indexOf('translate');
                if (ti > -1) {
                    pair = pair.substring(ti + 9);
                }
                if (pair.trim().length === 0) {
                    return; // a trailing comma before the ';' is tolerated
                }
                let m = TRANSLATE_PAIR_RE.exec(pair);
                if (!m) {
                    throw new Error(NEXUS_FORMAT_ERR + 'ill-formatted translate table entry: "'
                        + pair.trim() + '" -- is the Translate sub-command terminated with a ";"?');
                }
                // The sub-command terminator comes off BEFORE unquoting, or a
                // well-formed quoted value followed by ';' would not look
                // well-formed and would fall to the lenient path.
                let value = m[2].trim();
                if (value.endsWith(';')) {
                    value = value.slice(0, -1).trim();
                }
                value = unquoteLabel(value);
                translateMap[m[1]] = value;
            });
        }

        // One MATRIX row ("taxon residues..."): the id is the first token (a
        // quoted label may contain spaces), the residues are the rest with all
        // internal whitespace removed. Only protein/dna/rna matrices become
        // sequences. In an interleaved matrix each id reappears in a later
        // block, so a repeated id is CONCATENATED onto its row. MATCHCHAR
        // (e.g. '.') means "same as the first taxon at this position" and is
        // resolved against that reference row at the same absolute positions.
        function addMatrixRow(row) {
            if (datatype !== 'protein' && datatype !== 'dna' && datatype !== 'rna') {
                return;
            }
            let id;
            let rest;
            let c0 = row.charAt(0);
            if (c0 === "'" || c0 === '"') {
                let close = row.indexOf(c0, 1);
                if (close < 1) {
                    return;
                }
                id = row.substring(0, close + 1);
                rest = row.substring(close + 1);
            } else {
                let sp = row.indexOf(' ');
                if (sp < 1) {
                    return;
                }
                id = row.substring(0, sp);
                rest = row.substring(sp + 1);
            }
            let block = rest.replace(/\s+/g, '');
            if (block.length === 0 || !RESIDUES_RE.test(block)) {
                return;
            }
            if (matchchar && (matrixReferenceId !== null) && (id !== matrixReferenceId)
                && seqs[matrixReferenceId]) {
                let ref = seqs[matrixReferenceId].value;
                let offset = seqs[id] ? seqs[id].value.length : 0;
                let resolved = '';
                for (let j = 0; j < block.length; ++j) {
                    let c = block.charAt(j);
                    resolved += (c === matchchar && (offset + j) < ref.length)
                        ? ref.charAt(offset + j) : c;
                }
                block = resolved;
            }
            // '*' is NOT absence: it is a residue, the stop codon of a
            // translated alignment. What counts as absence is what the block
            // DECLARED, plus '.', which is the matchchar and gap convention.
            for (let j = 0; j < block.length; ++j) {
                let c = block.charAt(j).toLowerCase();
                if (c !== missingChar.toLowerCase() && c !== gapChar.toLowerCase() && c !== '.') {
                    seqIdsWithResidues[id] = true;
                    break;
                }
            }
            // ... and only THEN normalize: afterwards '?' has become X or N
            // and a row of nothing but missing data would read as residues.
            block = normalizeMatrixResidues(block, datatype);
            seqs[id] = {
                value: seqs[id] ? (seqs[id].value + block) : block,
                type: datatype
            };
            if (matrixReferenceId === null) {
                matrixReferenceId = id;
            }
        }

        // A complete tree statement has accumulated in nh: parse it and carry
        // over the block's translate table / taxlabels / matrix sequences.
        function finishTree() {
            if (nh.length === 0) {
                return;
            }
            let phy = forester.parseNewHampshire(nh, confidenceValuesInBrackets, confidenceValuesAsInternalNames);
            let myname = '';
            if (title && name) {
                myname = title.replace(/_/g, ' ').trim() + ' (' + name.trim() + ')';
            } else if (title) {
                myname = title.replace(/_/g, ' ').trim();
            } else if (name) {
                myname = name.trim();
            }
            if (myname) {
                phy.name = myname;
            }
            if (rootedInfoPresent) {
                phy.rooted = isRooted;
            }
            let seqsByKey = Object.create(null);
            for (let id in seqs) {
                if (seqIdsWithResidues[id]) {
                    seqsByKey[joinKey(id)] = seqs[id];
                }
            }
            let externals = forester.getAllExternalNodes(phy);
            let annotationNs = null;
            // A bare integer tip name counts as a TAXLABELS index only when
            // the WHOLE tree reads as index references: every tip a bare
            // integer AND every one of them in range. All-or-nothing, because
            // deciding it per tip fails silently and plausibly -- against six
            // labels, ((a,b,c),(1,2,3)) renamed just the three integers and
            // handed back a tree whose every tip was DUPLICATED, which still
            // parses and still renders; and a single out-of-range index left a
            // half-renamed tree behind for the same reason. Both are reachable
            // through our own writer: save as Nexus, reopen. So if any tip is
            // not an index, none of them are. Matches the desktop (0.11.140+).
            // A TRANSLATE entry still wins wherever it applies -- it is the
            // explicit mechanism, this is only the heuristic.
            let indexed = taxlabels.length > 0 && externals.every(function (node) {
                if (node.name && translateMap[node.name] !== undefined) {
                    return true;
                }
                if (!node.name || !/^\d+$/.test(node.name)) {
                    return false;
                }
                let i = parseInt(node.name, 10);
                return i > 0 && i <= taxlabels.length;
            });
            externals.forEach(function (node) {
                if (node.name && translateMap[node.name] !== undefined) {
                    node.name = translateMap[node.name];
                } else if (indexed) {
                    // The TAXLABELS tokenizer has already removed the outer
                    // quotes and un-doubled what was inside, so the label is
                    // used as-is: stripping quotes again here would undo the
                    // un-doubling and drop the apostrophe a second time.
                    node.name = taxlabels[parseInt(node.name, 10) - 1];
                }
                // A colour FigTree gave the TAXON is the colour of its LABEL
                // -- FigTree's own meaning, and the desktop's (Christian,
                // 2026-09-16) -- where a !color in the tree string is the
                // BRANCH's. It lands as the desktop's style:font_color
                // property, which is what Visual Styles already draws and what
                // phyloXML already carries, so nothing downstream is new.
                // The taxon is found by its exact name, else by the Nexus join
                // key (case-insensitive, '_' for ' ') -- but by the key ONLY
                // when it names exactly ONE taxlabel, counting every taxlabel,
                // annotated or not. Without that, Taxon_A[&!color=red] beside a
                // plain taxon_a coloured BOTH tips: taxon_a has no annotation
                // of its own, so it fell through to a key it shares. And a tip
                // matching two labels by key alone is ambiguous: no colour,
                // rather than whichever was written last. (Found by a review on
                // the desktop, which had the same fallback; its rule.)
                let loneKey = !!node.name && taxlabelKeyCount[joinKey(node.name)] === 1;
                let labelColor = !node.name ? undefined
                    : (taxlabelColors[node.name] !== undefined ? taxlabelColors[node.name]
                        : (loneKey ? taxlabelColorsByKey[joinKey(node.name)] : undefined));
                if (labelColor !== undefined) {
                    if (!node.properties) {
                        node.properties = [];
                    }
                    node.properties.push({ref: 'style:font_color', value: labelColor,
                        datatype: 'xsd:token', applies_to: 'node'});
                }
                let refusedColor = !node.name ? undefined
                    : (taxlabelRefused[node.name] !== undefined ? taxlabelRefused[node.name]
                        : (loneKey ? taxlabelRefusedByKey[joinKey(node.name)] : undefined));
                if (refusedColor !== undefined) {
                    if (annotationNs === null) {
                        annotationNs = annotationPrefix(phy);   // once per tree, and only if needed
                    }
                    addNodeProperty(node, annotationNs + '_color', refusedColor, 'xsd:string');
                }
                if (node.name) {
                    let s = seqsByKey[joinKey(node.name)];
                    if (s) {
                        if (!node.sequences) {
                            node.sequences = [];
                        }
                        node.sequences.push({
                            type: s.type,
                            mol_seq: {is_aligned: true, value: s.value}
                        });
                    }
                }
            });
            trees.push(phy);
            nh = '';
            name = '';
            rootedInfoPresent = false;
            isRooted = false;
        }

        let lines = String(nexStr).split(/\r\n|\r|\n/);
        for (let k = 0; k < lines.length; ++k) {
            let line = lines[k].trim();
            if (line.length === 0 || line.charAt(0) === '#' || line.charAt(0) === '>') {
                continue;
            }
            line = line.replace(/\s+/g, ' ').replace(/\s+;/g, ';');
            let lc = line.toLowerCase();
            if (/^begin\s+trees\b/.test(lc)) {
                inTreesBlock = true;
                inTaxalabels = false;
                inTranslate = false;
                inDataBlock = false;
                datatype = null;
                title = '';
            } else if (lc.startsWith('taxlabels')) {
                inTreesBlock = false;
                inTaxalabels = true;
                inTranslate = false;
                inDataBlock = false;
                datatype = null;
            } else if (lc.startsWith('translate')) {
                translateBuf = '';
                inTaxalabels = false;
                inTranslate = true;
                inDataBlock = false;
                datatype = null;
            } else if (/^begin\s+(characters|data)\b/.test(lc)) {
                inTaxalabels = false;
                inTreesBlock = false;
                inTranslate = false;
                inDataBlock = true;
                inMatrix = false;
                inDataComment = false;
                datatype = null;
                matchchar = null;
                matrixReferenceId = null;
                // scope the rows to THIS matrix block, so a later block
                // cannot cross-contaminate an earlier one
                seqs = Object.create(null);
                seqIdsWithResidues = Object.create(null);
                missingChar = '?';
                gapChar = '-';
            } else if (inTreesBlock) {
                if (lc.startsWith('title')) {
                    let tm = TITLE_RE.exec(line);
                    if (tm) {
                        title = tm[1];
                    }
                } else if (lc.startsWith('link')) {
                    // a LINK sub-command (e.g. "LINK TAXA=...") -- ignored
                } else if (lc.startsWith('end;') || lc.startsWith('endblock')) {
                    inTreesBlock = false;
                    inTree = false;
                    finishTree();
                } else if (lc.startsWith('tree ') || lc.startsWith('utree ')) {
                    finishTree(); // a previous statement still pending
                    inTree = true;
                    let nm = TREE_NAME_RE.exec(line);
                    if (nm) {
                        name = unquoteLabel(nm[1]);
                    }
                    let rm = ROOTEDNESS_RE.exec(line);
                    if (rm) {
                        rootedInfoPresent = true;
                        isRooted = rm[1].toUpperCase() === 'R';
                    }
                    // parseNewHampshire handles the remaining [&...] hot
                    // comments itself: BEAST-style annotations are parsed
                    // onto the nodes, a leading [&R]/[&U] is dropped
                    nh = line.substring(line.indexOf('=') + 1).trim();
                    if (lc.endsWith(';')) {
                        inTree = false;
                        finishTree();
                    }
                } else if (inTree) {
                    nh += line;
                    if (lc.endsWith(';')) {
                        inTree = false;
                        finishTree();
                    }
                }
            }
            if (inTaxalabels) {
                if (lc.startsWith('end;') || lc.startsWith('endblock')) {
                    inTaxalabels = false;
                } else {
                    // QUOTE-AWARE tokenization: 'Homo sapiens' is ONE label
                    // (that is what the quotes are for) -- a plain space split
                    // silently sheared such labels apart and shifted every
                    // numeric tip onto the wrong name. ';' (unquoted) ends
                    // the sub-command.
                    //
                    // A quote OPENS a run only at a TOKEN BOUNDARY. Inside a
                    // word it is just a character -- an unquoted O'Neil must
                    // not open a run and swallow the rest of the line, the
                    // terminating ';' included, which is what the first
                    // version of this tokenizer did: it merged every remaining
                    // label into one and left the other tips as bare numbers.
                    let tok = '';
                    let q = null;
                    let closed = false; // this token already held a quoted run
                    let push = function () {
                        if (tok.length > 0 && tok.toLowerCase() !== 'taxlabels') {
                            taxlabels.push(tok);
                            taxlabelKeyCount[joinKey(tok)] = (taxlabelKeyCount[joinKey(tok)] || 0) + 1;
                        }
                        tok = '';
                        closed = false;
                    };
                    for (let ci = 0; ci < line.length; ++ci) {
                        let ch = line.charAt(ci);
                        if (q) {
                            if (ch === q) {
                                q = null;
                                closed = true;
                            } else {
                                tok += ch;
                            }
                        } else if (ch === "'" || ch === '"') {
                            if (tok.length === 0 && !closed) {
                                q = ch;
                            } else if (closed) {
                                // A quote directly after a closing one is the
                                // doubled Nexus escape: emit ONE literal quote
                                // and let the run CONTINUE. The continuing is
                                // what keeps a quoted label holding an
                                // apostrophe in one piece instead of splitting
                                // it at the space (that was N1); emitting the
                                // character is the un-doubling half, deferred
                                // until both programs could land it together.
                                tok += ch;
                                q = ch;
                            } else {
                                // A quote in the middle of a BARE word is not
                                // an escape and not a delimiter -- an unquoted
                                // token may not legally hold one at all. It is
                                // dropped rather than kept, which is the older
                                // lenient behaviour and is SHARED with the
                                // desktop; keeping it here would be a new
                                // divergence, not a fix.
                                void ch;
                            }
                        } else if (ch === '[') {
                            // A bracket after a label is not part of it. FigTree
                            // hangs the taxon's colour here --
                            // 'NewYork_454_1999.05'[&!color=#-8381639] -- and it
                            // used to be glued onto the label, which then named
                            // no tip (invisible while a tree spells its tips
                            // out, wrong as soon as it refers to them by number).
                            // A [&...] belongs to the label just read; any other
                            // bracket is an ordinary Nexus comment.
                            //
                            // Only !color is read from it, and that is DECIDED
                            // (Christian, 2026-09-17: "keep it as it is"), a named
                            // difference from the desktop, which keeps every field
                            // of a taxon's annotation. No producer we know writes
                            // anything else here. Widening it is not a small edit:
                            // a posterior, a height or mutations arriving by this
                            // road could sway the per-tree pass, which would then
                            // have to run after the taxlabels (see annotationPrefix).
                            let end = line.indexOf(']', ci);
                            let inside = line.substring(ci + 1, end < 0 ? line.length : end).trim();
                            if (inside.charAt(0) === '&' && tok.length > 0) {
                                splitTopLevelCommas(inside.substring(1), true).forEach(function (field) {
                                    let eq = field.indexOf('=');
                                    let key = eq > 0 ? field.substring(0, eq).trim().toLowerCase() : '';
                                    let isColor = key === '!color' || key === '!colour';
                                    let raw = isColor ? stripValueQuotes(field.substring(eq + 1).trim()) : '';
                                    let rgb = isColor ? parseFigTreeColor(raw) : null;
                                    if (isColor && !rgb && raw.length > 0) {
                                        // not a colour we read (no '#', say): kept as
                                        // text on the tip, as in the tree string
                                        taxlabelRefused[tok] = raw;
                                        taxlabelRefusedByKey[joinKey(tok)] = raw;
                                    }
                                    if (rgb) {
                                        taxlabelColors[tok] = '#' + [rgb.red, rgb.green, rgb.blue].map(function (c) {
                                            return (c < 16 ? '0' : '') + c.toString(16);
                                        }).join('');
                                        taxlabelColorsByKey[joinKey(tok)] = taxlabelColors[tok];
                                    }
                                });
                            }
                            ci = end < 0 ? line.length : end;
                        } else if (ch === ' ') {
                            push();
                        } else if (ch === ';') {
                            inTaxalabels = false;
                            push();
                            break;
                        } else {
                            tok += ch;
                        }
                    }
                    push();
                }
            }
            if (inTranslate) {
                if (lc.startsWith('end;') || lc.startsWith('endblock')) {
                    inTranslate = false;
                } else {
                    translateBuf += ' ' + line;
                    if (line.endsWith(';')) {
                        inTranslate = false;
                        setTranslatePairs(translateBuf);
                    }
                }
            }
            if (inDataBlock) {
                line = stripDataComments(line);
                let dlc = line.toLowerCase();
                if (line.length === 0) {
                    // comment-only (or now-empty) line
                } else if (dlc.startsWith('end;') || dlc.startsWith('endblock')) {
                    inDataBlock = false;
                    inMatrix = false;
                    datatype = null;
                } else if (dlc.startsWith('link ')) {
                    // ignored; the trailing space keeps a taxon row whose
                    // name starts with "link" out of this branch
                } else if (!inMatrix) {
                    // block header: DIMENSIONS / FORMAT / CHARLABELS / ... --
                    // read DATATYPE and MATCHCHAR off FORMAT, enter the matrix
                    // on the MATRIX keyword, ignore the rest (a sub-command
                    // ending in ';' must NOT be mistaken for the block's end)
                    let dm = DATATYPE_RE.exec(dlc);
                    if (dm) {
                        datatype = dm[1];
                    }
                    let mm = MATCHCHAR_RE.exec(dlc);
                    if (mm) {
                        matchchar = mm[1];
                    }
                    let miss = MISSING_RE.exec(dlc);
                    if (miss) {
                        missingChar = miss[1];
                    }
                    let gp = GAP_RE.exec(dlc);
                    if (gp) {
                        gapChar = gp[1];
                    }
                    if (dlc === 'matrix' || dlc.startsWith('matrix ')) {
                        inMatrix = true;
                        let after = line.substring(6).trim();
                        let matrixEnds = false;
                        if (after.endsWith(';')) {
                            matrixEnds = true;
                            after = after.slice(0, -1).trim();
                        }
                        if (after.length > 0) {
                            addMatrixRow(after);
                        }
                        if (matrixEnds) {
                            inMatrix = false;
                            inDataBlock = false;
                            datatype = null;
                        }
                    }
                } else {
                    // inside the MATRIX: one taxon row per line until ';'
                    let matrixEnds = false;
                    if (line.endsWith(';')) {
                        matrixEnds = true;
                        line = line.slice(0, -1).trim();
                    }
                    if (line.length > 0) {
                        addMatrixRow(line);
                    }
                    if (matrixEnds) {
                        inMatrix = false;
                        inDataBlock = false;
                        datatype = null;
                    }
                }
            }
        }
        finishTree(); // EOF with a tree still pending (no closing "End;")
        return trees;
    };

    // ---------------------------------------------------------------
    // Auspice / Nextstrain
    // ---------------------------------------------------------------

    // Namespace for the node properties the Auspice parser writes, so
    // Nextstrain traits are colour-able/searchable and clearly distinguished
    // from BEAST's "beast:" namespace. Same prefix as the desktop.
    const NEXTSTRAIN_PREFIX = 'nextstrain:';

    // A compact string for a JSON number: a whole value drops the ".0" (a
    // clean categorical/integer property), otherwise the plain decimal
    // WITHOUT scientific notation (a small divergence like 1e-4 must read
    // as "0.0001" in the node-data dialog / as a searchable value).
    function plainNumberString(d) {
        if (Number.isInteger(d) && Math.abs(d) < 1e15) {
            return String(d);
        }
        let s = String(d);
        if (s.indexOf('e') < 0 && s.indexOf('E') < 0) {
            return s;
        }
        return d.toFixed(20).replace(/0+$/, '').replace(/\.$/, '');
    }

    // `datatype` is for the few values whose type is decided by what they ARE
    // rather than by what they look like (see mutations / mcc).
    function addNodeProperty(node, ref, value, datatype) {
        if (value === undefined || value === null || String(value).length === 0) {
            return;
        }
        if (!node.properties) {
            node.properties = [];
        }
        let v = String(value);
        node.properties.push({
            ref: ref,
            value: v,
            datatype: datatype || (parseBeastNumber(v) !== null ? 'xsd:decimal' : 'xsd:string'),
            applies_to: 'node'
        });
    }

    // Parses an Auspice / Nextstrain v2 dataset.json (string or already-parsed
    // object) into ONE tree object, mapping its per-node data onto the native
    // phyloXML shape so the existing overlays light it up -- ported from the
    // desktop's AuspiceJsonParser. TreeTime's own auspice_tree.json is read
    // here too (see the version check below):
    //  - node_attrs.num_date.value -> node.date value (decimal year) -> the
    //    calendar time axis; its .confidence [lo,hi] -> date minimum/maximum
    //    -> the node-age (HPD) bars;
    //  - node_attrs.div -> a nextstrain:div property (the divergence measure,
    //    kept for a future time<->divergence view);
    //  - every discrete trait (country, clade_membership, host, ...) -> a
    //    nextstrain:<key> node property (Color-by / search / node dialog);
    //    its .confidence {state:prob} -> nextstrain:<key>_set + _set_prob
    //    brace-list pair (the desktop's ancestral-state-pie encoding);
    //  - branch_attrs.labels.clade -> a nextstrain:clade_label property.
    // Branch lengths default to TIME (successive num_date differences); a
    // divergence-only build falls back to div differences. Deliberately NOT
    // ingested: the map, entropy and frequencies panels.
    forester.parseAuspiceJson = function (data) {
        let doc = forester.isString(data) ? JSON.parse(data) : data;
        if (!doc || typeof doc !== 'object' || Array.isArray(doc)) {
            throw new Error('not an Auspice dataset (the JSON root is not an object)');
        }
        if (!doc.tree || typeof doc.tree !== 'object' || Array.isArray(doc.tree)) {
            throw new Error('not an Auspice v2 dataset (expected "version":"v2" and a "tree" object)');
        }
        // The version stamp is taken as PRESENT-OR-IMPLIED: TreeTime writes a
        // fully valid v2 dataset ({meta, tree} with node_attrs.num_date,
        // branch_attrs, children) and simply never writes "version":"v2", so
        // demanding the stamp rejected the richest file TreeTime produces --
        // the only one carrying full-precision dates (its .nexus rounds them
        // to two decimals). The shape is still checked, so arbitrary JSON
        // keeps getting the clear error rather than a confusing parse.
        if (doc.version !== 'v2'
            && !(doc.meta && typeof doc.meta === 'object' && !Array.isArray(doc.meta)
                && (typeof doc.tree.name === 'string'
                    || (doc.tree.node_attrs && typeof doc.tree.node_attrs === 'object')
                    || Array.isArray(doc.tree.children)))) {
            throw new Error('not an Auspice v2 dataset (expected "version":"v2" and a "tree" object)');
        }

        function isScalar(v) {
            return (typeof v === 'string') || (typeof v === 'number') || (typeof v === 'boolean');
        }

        function scalarToString(v) {
            return (typeof v === 'number') ? plainNumberString(v) : String(v);
        }

        // a discrete trait's posterior distribution as the _set/_set_prob
        // brace-list pair; state names quoted so a comma/space in one (e.g.
        // "Korea, Republic of") cannot corrupt the list
        function applyTraitConfidence(node, trait, conf) {
            let states = [];
            let probs = [];
            Object.keys(conf).forEach(function (state) {
                let p = conf[state];
                if (typeof p !== 'number' || !isFinite(p) || state.length === 0) {
                    return;
                }
                states.push('"' + state.replace(/"/g, '') + '"');
                probs.push(plainNumberString(p));
            });
            if (states.length > 0) {
                addNodeProperty(node, NEXTSTRAIN_PREFIX + trait + '_set', '{' + states.join(',') + '}');
                addNodeProperty(node, NEXTSTRAIN_PREFIX + trait + '_set_prob', '{' + probs.join(',') + '}');
            }
        }

        function applyNodeAttrs(node, attrs) {
            Object.keys(attrs).forEach(function (key) {
                let val = attrs[key];
                if (key === 'num_date') {
                    if (val && typeof val === 'object' && typeof val.value === 'number') {
                        let date = {value: val.value, unit: 'year'};
                        if (Array.isArray(val.confidence) && val.confidence.length === 2
                            && typeof val.confidence[0] === 'number'
                            && typeof val.confidence[1] === 'number') {
                            date.minimum = val.confidence[0];
                            date.maximum = val.confidence[1];
                        }
                        node.date = date;
                        // the point date doubles as a numeric property, so the
                        // sampling date can drive Color-by (the classic
                        // Nextstrain colour-by-date view) and search
                        addNodeProperty(node, NEXTSTRAIN_PREFIX + 'num_date', plainNumberString(val.value));
                    }
                } else if (key === 'div') {
                    if (typeof val === 'number' && isFinite(val)) {
                        addNodeProperty(node, NEXTSTRAIN_PREFIX + 'div', plainNumberString(val));
                    }
                } else if (val && typeof val === 'object' && !Array.isArray(val)) {
                    // a discrete trait: {value, confidence{state:prob}, entropy}
                    if (isScalar(val.value)) {
                        addNodeProperty(node, NEXTSTRAIN_PREFIX + key, scalarToString(val.value));
                    }
                    if (val.confidence && typeof val.confidence === 'object'
                        && !Array.isArray(val.confidence)) {
                        applyTraitConfidence(node, key, val.confidence);
                    }
                } else if (isScalar(val)) {
                    addNodeProperty(node, NEXTSTRAIN_PREFIX + key, scalarToString(val)); // bare attr (accession, url, ...)
                }
            });
        }

        function buildNode(jn) {
            let node = {};
            if (typeof jn.name === 'string' && jn.name.length > 0) {
                node.name = jn.name;
            }
            if (jn.node_attrs && typeof jn.node_attrs === 'object') {
                applyNodeAttrs(node, jn.node_attrs);
            }
            let labels = jn.branch_attrs && jn.branch_attrs.labels;
            if (labels && typeof labels.clade === 'string' && labels.clade.length > 0) {
                addNodeProperty(node, NEXTSTRAIN_PREFIX + 'clade_label', labels.clade);
            }
            if (Array.isArray(jn.children) && jn.children.length > 0) {
                node.children = [];
                jn.children.forEach(function (c) {
                    if (c && typeof c === 'object') {
                        node.children.push(buildNode(c));
                    }
                });
                if (node.children.length === 0) {
                    delete node.children;
                }
            }
            return node;
        }

        let root = buildNode(doc.tree);
        let phy = {rooted: true, children: [root]};
        let title = doc.meta && doc.meta.title;
        if (typeof title === 'string' && title.trim().length > 0) {
            phy.name = title.trim();
        }
        // A build has no branch lengths of its own, so it opens in the metric
        // it states COMPLETELY: time when every node states num_date, else
        // divergence when every node states div, else as it always did (time
        // if any node is dated, divergence if none is). Joint with the
        // desktop, 2026-09-29. A num_date is a calendar year, so the gap is
        // child minus parent, and it keeps its sign.
        let opensInTime = everyNodeStates(root, auspiceNodeDate)
            || (!everyNodeStates(root, recordedDiv) && auspiceHasAnyDate(root));
        if (opensInTime) {
            setDeltaBranchLengths(root, null, auspiceNodeDate, calendarGap);
        } else {
            setDeltaBranchLengths(root, null, recordedDiv, divergenceGap);
        }
        // ...and says which, as the desktop's reader does: a saved copy then
        // states what its branch lengths measure
        phy.branch_length_unit = opensInTime ? CALENDAR_LENGTH_UNIT : DIVERGENCE_LENGTH_UNIT;
        // A tip keeps its date INTERVAL: on a Nextstrain build it is the
        // sampling-date uncertainty of a sample dated only to its month or
        // year, which is data. (It was dropped here until 2026-09-17 because
        // the time axis drew every tip interval as a fossil range; the axis
        // now tells the two apart -- see settleNumDates and drawTimeAxis.)
        forester.addParents(phy);
        return phy;
    };

    function auspiceNodeDate(node) {
        return (node.date && typeof node.date.value === 'number' && isFinite(node.date.value))
            ? node.date.value : null;
    }

    // The divergence a node RECORDS (nextstrain:div), or null. A plain decimal
    // number and nothing else (parseBeastNumber), as a clock rate is: every
    // number the switch reads is read one way, on both programs. parseFloat
    // read one out of the front of "0.005abc", "1,5" and "0x10" -- which the
    // Nexus reader had never let through, so the same value was a divergence
    // in a phyloXML file and none in a Nexus one. A NEGATIVE one is a value;
    // the branch along which it falls is drawn at 0 (divergenceGap).
    // Christian, 2026-09-29.
    function recordedDiv(node) {
        if (node.properties) {
            for (let i = 0; i < node.properties.length; ++i) {
                if (node.properties[i].ref === NEXTSTRAIN_PREFIX + 'div') {
                    return parseBeastNumber(node.properties[i].value);
                }
            }
        }
        return null;
    }

    function recordsDivergence(root) {
        let found = false;
        forester.preOrderTraversalAll(root, function (n) {
            if (!found && recordedDiv(n) !== null) {
                found = true;
            }
        });
        return found;
    }

    // A node's CUMULATIVE divergence from the root. Auspice states it outright
    // as nextstrain:div; a BEAST clock-model tree states it as branch length
    // x rate, summed at load and kept on the node (see forester.
    // captureDivergence). A tree states it ONE way: where any node records a
    // divergence nothing is captured, so a node that records none has none.
    // Until 2026-09-29 such a node fell back to the sum of the loaded lengths,
    // which on an Auspice tree are YEARS -- a tip whose div of 0.0008 was
    // missing drew its branch at 0.2794, where the file's own numbers said
    // 0.0002.
    function auspiceNodeDiv(node) {
        let d = recordedDiv(node);
        if (d !== null) {
            return d;
        }
        return (typeof node._divergence === 'number' && isFinite(node._divergence))
            ? node._divergence : null;
    }

    function auspiceHasAnyDate(node) {
        let found = false;
        forester.preOrderTraversalAll(node, function (n) {
            if (auspiceNodeDate(n) !== null) {
                found = true;
            }
        });
        return found;
    }

    // Branch lengths = successive differences of a cumulative per-node metric
    // (num_date -> the time view; nextstrain:div -> the divergence view).
    // The root's length is 0, and a node missing the metric (or whose parent
    // misses it) gets 0 -- so a time<->divergence toggle can never leave a
    // stale cross-scale length behind.
    function setDeltaBranchLengths(node, parentValue, metricOf, gapOf) {
        let v = metricOf(node);
        node.branch_length = (parentValue !== null && v !== null) ? gapOf(v, parentValue) : 0;
        let children = node.children;
        if (children) {
            for (let i = 0; i < children.length; ++i) {
                setDeltaBranchLengths(children[i], v, metricOf, gapOf);
            }
        }
    }

    // A TIME gap keeps its sign. A child dated before its parent is what the
    // file states -- real Nextstrain builds state it (42 branches in 4 of 11,
    // one of them by 4.09 years) -- so the length is negative and every node
    // keeps its own date. Clamped at 0 until 2026-09-29, which also made the
    // switch lossy: a time tree stating negative lengths came back from Div
    // with 0 in their place. (A negative length is DRAWN at 0, as it always
    // was and as the desktop draws it; this is about the value.)
    function calendarGap(v, parentValue) {
        return v - parentValue;
    }

    function ageGap(v, parentValue) {
        return parentValue - v;
    }

    // In the direction the tree's dates run: calendar dates increase toward
    // the tips, ages (a height) toward the root. As the desktop measures it.
    function timeGapOf(root) {
        return timeIncreasesTowardTips(root) ? calendarGap : ageGap;
    }

    // Divergence accumulates: a node recording less than its parent is noise,
    // not a branch running backwards, and draws at 0.
    function divergenceGap(v, parentValue) {
        return Math.max(0, v - parentValue);
    }

    // The time<->divergence plumbing: both metrics are RETAINED on a parsed
    // Auspice tree (the date values + the nextstrain:div properties), so a
    // future display toggle can rewrite the branch lengths from EITHER at any
    // time -- lossless and reversible, and reusing the exact recompute the
    // parser itself used, so the toggle can never drift from the loaded view.

    // Both leave the tree exactly as it was, and answer false, when their
    // layout cannot state every branch: a branch drawn at 0 because a datum is
    // MISSING says "nothing happened along this branch", which the file never
    // said. A datum that is stated is stated, whether zero or negative.

    // What a tree's branch lengths MEASURE is stated on the tree (phyloXML's
    // branch_length_unit), and each layout stamps its own: the scale bar, the
    // tree's properties and a saved file then all say the same. Divergence is
    // in substitutions per site; time is in the unit of the tree's own dates,
    // or plainly "time" where they state none (BEAST heights). The desktop's
    // words, byte for byte (BranchLengthLayout.distanceUnit).
    //
    // It is also how a clock-model tree saved from the Div view is known for
    // what it is when it is opened again (see captureDivergence).
    const DIVERGENCE_LENGTH_UNIT = 'subs/site';
    const CALENDAR_LENGTH_UNIT = 'year';
    const PLAIN_TIME_LENGTH_UNIT = 'time';

    // The unit of the first dated node that states one, in preorder, children
    // in file order; null where none does.
    function dateUnitOf(root) {
        let unit = null;
        (function walk(n) {
            if (unit !== null) {
                return;
            }
            if (n.date && typeof n.date.unit === 'string' && n.date.unit.trim().length > 0) {
                unit = n.date.unit.trim();
                return;
            }
            let children = n.children;
            if (children) {
                for (let i = 0; i < children.length; ++i) {
                    walk(children[i]);
                }
            }
        }(root));
        return unit;
    }

    // `phy` may be the tree or its root; only the tree carries a unit.
    function stampLengthUnit(phy, root, unit) {
        if (phy && phy !== root && typeof phy === 'object') {
            phy.branch_length_unit = unit;
        }
    }

    function statesDivergenceUnit(phy) {
        return !!phy && typeof phy.branch_length_unit === 'string'
            && phy.branch_length_unit.trim() === DIVERGENCE_LENGTH_UNIT;
    }

    // Back in TIME a tree gets the lengths it had in time: the ones it
    // arrived with, kept at load (see captureDivergence). So a file's own
    // lengths are what is shown, to the digit, where they and its node dates
    // disagree (carnivore.tree: 1.407 stated on a branch whose two heights are
    // 1.098 apart). Joint with the desktop (Christian, 2026-09-29). A tree that
    // kept none -- it did not arrive in time -- is laid out by its date gaps.
    forester.applyTimeBranchLengths = function (phy) {
        let root = forester.getTreeRoot(phy);
        if (root && root._timeLengthsKept === true) {
            if (!everyLengthWasStated(root)) {
                return false;    // a branch with no kept length: nothing to give back
            }
            forester.preOrderTraversalAll(root, function (n) {
                if (n !== root) {    // the root has no branch, and no layout touches what it states
                    n.branch_length = n._timeLength;
                }
            });
            stampLengthUnit(phy, root, dateUnitOf(root) || PLAIN_TIME_LENGTH_UNIT);
            return true;
        }
        if (!root || !everyNodeStates(root, auspiceNodeDate)) {
            return false;
        }
        layOutBelowRoot(root, auspiceNodeDate, timeGapOf(root));
        stampLengthUnit(phy, root, dateUnitOf(root) || PLAIN_TIME_LENGTH_UNIT);
        return true;
    };

    forester.applyDivergenceBranchLengths = function (phy) {
        let root = forester.getTreeRoot(phy);
        if (!root || !divergenceStatesEveryBranch(root)) {
            return false;
        }
        layOutBelowRoot(root, auspiceNodeDiv, divergenceGap);
        stampLengthUnit(phy, root, DIVERGENCE_LENGTH_UNIT);
        return true;
    };

    // The root has no branch to lay out: it keeps the length it has, or none.
    // Given a length of 0 by the switch, a tree saved from Div stated a root
    // branch its file never had, and the desktop's copy of it did not.
    function layOutBelowRoot(root, metricOf, gapOf) {
        let had = Object.prototype.hasOwnProperty.call(root, 'branch_length');
        let length = root.branch_length;
        setDeltaBranchLengths(root, null, metricOf, gapOf);
        if (had) {
            root.branch_length = length;
        } else {
            delete root.branch_length;
        }
    }

    function everyNodeStates(root, metricOf) {
        let every = true;
        forester.preOrderTraversalAll(root, function (n) {
            if (every && metricOf(n) === null) {
                every = false;
            }
        });
        return every;
    }

    // Recorded on every node (the root too: it is the parent its children are
    // measured from), or derived from a clock rate on every branch and a
    // length every one of which the file stated. A tree with neither has no
    // divergence to lay out: its branch lengths are what they are.
    function divergenceStatesEveryBranch(root) {
        if (recordsDivergence(root)) {
            return everyNodeStates(root, recordedDiv);
        }
        return root._divergenceFromRates === true && root._everyLengthStated === true;
    }

    // Asked at load where it was (captureDivergence): once the switch has been
    // pressed every branch has a length, whatever the file stated.
    function everyLengthWasStated(root) {
        return (root._everyLengthStated === undefined) ? everyBranchStatesLength(root)
            : (root._everyLengthStated === true);
    }

    /**
     * Keeps, at load, what the Time | Div switch needs to be lossless:
     *
     * - the branch lengths the tree ARRIVED with, where they are its time
     *   (`_timeLength`): on a BEAST clock-model tree always, and on a tree
     *   recording its divergence when its lengths are its date gaps. Back in
     *   Time the tree gets exactly these;
     * - on a clock-model tree -- a clock rate on every branch -- each node's
     *   cumulative divergence from the root (`_divergence`), summed from
     *   those lengths times each branch's rate.
     *
     * Both are private: no writer emits them, and a re-read of our own output
     * re-derives them. Nothing is kept on a tree with neither a recorded
     * divergence nor rates: it has no second layout, and the switch is never
     * offered on it.
     *
     * A tree may ARRIVE showing divergence: it was saved while Div was on
     * screen. One that records its divergence is known by its lengths, which
     * are not its date gaps. A clock-model tree is known by what it states --
     * the unit of its branch lengths is the one the divergence layout stamps
     * -- since its lengths are not its date gaps even in time. Such a tree
     * keeps no time lengths (it carries none): its time is laid out from its
     * dates, and its divergence is each rate times that. Until 2026-09-29 a
     * clock-model tree's lengths were taken for its time whatever they were,
     * and one saved from Div came back labelled Time, with a Div of rate x
     * divergence. Joint with the desktop.
     *
     * Call once per tree at load, before anything rewrites a branch length.
     *
     * @param phy the tree
     */
    forester.captureDivergence = function (phy) {
        let root = forester.getTreeRoot(phy);
        if (!root) {
            return;
        }
        root._divergenceFromRates = false;
        root._timeLengthsKept = false;
        root._everyLengthStated = everyBranchStatesLength(root);
        // A tree that RECORDS its divergence states it that way and no other:
        // none is derived beside it, so a node that records none has none (see
        // auspiceNodeDiv).
        let recorded = recordsDivergence(root);
        // A BEAST clock-model tree states its branch lengths in TIME and each
        // branch's clock rate beside it, so its divergence is length x rate
        // (substitutions per site). Only when EVERY branch states a rate: a
        // partial one would mix substitutions with years along a path.
        let rates = recorded ? null : everyBranchClockRate(root);
        if (!recorded && rates === null) {
            return;
        }
        let showsDivergence = rates !== null && statesDivergenceUnit(phy);
        let keep = (rates !== null && !showsDivergence) || (recorded && branchLengthsAreTime(root));
        let gapOf = showsDivergence ? timeGapOf(root) : null;
        (function walk(node, cumulative) {
            if (keep) {
                node._timeLength = node.branch_length;
            }
            if (rates) {
                node._divergence = cumulative;
            }
            let children = node.children;
            if (children) {
                for (let i = 0; i < children.length; ++i) {
                    // the branch's time: the length it arrived with, or, on
                    // a tree that arrived showing divergence, its date gap
                    let span = children[i].branch_length;
                    if (gapOf) {
                        let d = auspiceNodeDate(children[i]);
                        let pd = auspiceNodeDate(node);
                        span = (d !== null && pd !== null) ? gapOf(d, pd) : 0;
                    }
                    // a span of 0, or a negative one, adds no divergence; a
                    // branch made of pieces (a node deleted above it) adds
                    // each piece's
                    let step = 0;
                    if (rates && Array.isArray(children[i]._pieces)) {
                        step = divergenceOfPieces(children[i]._pieces, rates.get(children[i]));
                    } else if (rates && typeof span === 'number' && isFinite(span) && span > 0) {
                        step = span * rates.get(children[i]);
                    }
                    walk(children[i], cumulative + step);
                }
            }
        }(root, 0));
        root._divergenceFromRates = rates !== null;
        root._timeLengthsKept = keep;
    };

    // A length of 0, or a negative one, is a length the file stated; one that
    // is not there is not.
    function everyBranchStatesLength(root) {
        let every = true;
        forester.preOrderTraversalAll(root, function (n) {
            if (every && n !== root && !(typeof n.branch_length === 'number' && isFinite(n.branch_length))) {
                every = false;
            }
        });
        return every;
    }

    // A node's BEAST clock rate (the `rate` annotation) as everyBranchClockRate
    // reads it: a plain decimal, not negative; else null.
    function clockRateOf(n) {
        if (n.properties) {
            for (let i = 0; i < n.properties.length; ++i) {
                if (n.properties[i].ref === BEAST_PREFIX + 'rate') {
                    let r = parseBeastNumber(n.properties[i].value);
                    return (r === null || r < 0) ? null : r;
                }
            }
        }
        return null;
    }

    // Each non-root node's BEAST clock rate (the `rate` annotation), or null
    // unless every branch states a finite, non-negative one. A rate is a plain
    // decimal number and nothing else (parseBeastNumber): parseFloat reads a
    // number out of the front of "0.01abc" or "1,5", and a rate nobody wrote
    // would then scale a branch.
    function everyBranchClockRate(root) {
        let rates = new Map();
        let ok = true;
        let branches = 0;
        forester.preOrderTraversalAll(root, function (n) {
            if (!ok || n === root) {
                return;
            }
            ++branches;
            let r = null;
            if (n.properties) {
                for (let i = 0; i < n.properties.length; ++i) {
                    if (n.properties[i].ref === BEAST_PREFIX + 'rate') {
                        r = parseBeastNumber(n.properties[i].value);
                        break;
                    }
                }
            }
            if (r === null || r < 0) {
                ok = false;
                return;
            }
            rates.set(n, r);
        });
        return (ok && branches > 0) ? rates : null;
    }

    // Whether each picture has any DEPTH: some tip away from the root, in
    // time and in divergence. A tree whose tips all carry the root's date has
    // no time picture, and one whose divergence is 0 on every branch (every
    // rate 0, say) has no divergence picture: a switch to a tree drawn as a
    // point is not offered. Joint with the desktop (Christian, 2026-09-29:
    // "Always offer; refuse only zero").
    //
    // Until then the switch was offered only where the two pictures differed
    // by more than 2% of the tree's width at some tip, which refused a strict
    // clock, whose divergence is its time rescaled. One rule for both
    // programs replaced a threshold both would have had to justify.
    //
    // Both metrics are read WITHOUT touching branch_length, so this can be
    // asked at load without disturbing the tree. Depth is taken at the tips,
    // summed along the path as the layout would draw it.
    function bothPicturesHaveDepth(root) {
        let rootDate = auspiceNodeDate(root);
        let timeDepth = 0;
        let divDepth = 0;
        (function walk(n, parentDiv, d) {
            let nd = auspiceNodeDiv(n);
            let dHere = (n === root || nd === null || parentDiv === null) ? d : (d + divergenceGap(nd, parentDiv));
            if (!n.children || n.children.length === 0) {
                let nt = auspiceNodeDate(n);
                if (nt !== null && rootDate !== null) {
                    timeDepth = Math.max(timeDepth, Math.abs(nt - rootDate));
                }
                divDepth = Math.max(divDepth, dHere);
                return;
            }
            for (let i = 0; i < n.children.length; ++i) {
                walk(n.children[i], nd, dHere);
            }
        }(root, null, 0));
        return timeDepth > 0 && divDepth > 0;
    }

    // Which metric the CURRENT branch lengths hold -- a different question
    // from whether the two metrics differ, and it needs the pair-by-pair
    // comparison: after forester.applyTimeBranchLengths every branch length is
    // exactly its ends' date gap, and after applyDivergenceBranchLengths it is
    // not. Asked so a switch starts from what is on screen.
    function branchLengthsAreTime(root) {
        let pairs = 0;
        let same = 0;
        let gapOf = timeGapOf(root);
        forester.preOrderTraversalAll(root, function (n) {
            if (!n.children) {
                return;
            }
            let pv = auspiceNodeDate(n);
            if (pv === null) {
                return;
            }
            for (let i = 0; i < n.children.length; ++i) {
                let c = n.children[i];
                let cv = auspiceNodeDate(c);
                let bl = c.branch_length;
                if (cv === null || typeof bl !== 'number' || !isFinite(bl)) {
                    continue;
                }
                ++pairs;
                // signed, as the time view writes it: a length the file
                // states negative is its date gap too
                let gap = gapOf(cv, pv);
                let scale = Math.max(Math.abs(bl), Math.abs(gap), 1e-9);
                if (Math.abs(gap - bl) <= (scale * 1e-6)) {
                    ++same;
                }
            }
        });
        return pairs > 0 && (same * 20) >= (pairs * 19);
    }

    // Which way the tree's dates run: a CALENDAR date increases toward the
    // tips; a BEAST height runs the other way -- it is an age, largest at the
    // root. Measured, not assumed, by the majority of the parent-child pairs
    // that differ (a tie, or none, reads as ages): forester.
    // convertHeightsToDates turns heights into calendar dates, and a tree it
    // REFUSED still states ages. The time view takes its gaps in this
    // direction (timeGapOf), so the few pairs that run AGAINST it come out
    // negative.
    function timeIncreasesTowardTips(root) {
        let up = 0;
        let pairs = 0;
        forester.preOrderTraversalAll(root, function (n) {
            if (!n.children) {
                return;
            }
            let pv = auspiceNodeDate(n);
            if (pv === null) {
                return;
            }
            for (let i = 0; i < n.children.length; ++i) {
                let cv = auspiceNodeDate(n.children[i]);
                if (cv === null || cv === pv) {
                    continue;
                }
                ++pairs;
                if (cv > pv) {
                    ++up;
                }
            }
        });
        return pairs > 0 && (up * 2) > pairs;
    }

    /**
     * Which metric the tree's branch lengths currently state. An Auspice build
     * arrives in the time view (its parser writes date deltas); a BEAST or
     * Newick file arrives stating whatever it was written with, which is the
     * divergence view. Asked rather than assumed, so a toggle starts from
     * what is actually on screen.
     *
     * @param phy the tree
     * @returns {string} 'time' or 'divergence'
     */
    forester.branchLengthScale = function (phy) {
        let root = forester.getTreeRoot(phy);
        if (!root) {
            return 'divergence';
        }
        // A tree that kept the lengths it arrived with is in time exactly
        // while they are the ones on it (the switch back restores them).
        if (root._timeLengthsKept === true) {
            let kept = true;
            forester.preOrderTraversalAll(root, function (n) {
                if (n !== root && n.branch_length !== n._timeLength) {
                    kept = false;
                }
            });
            return kept ? 'time' : 'divergence';
        }
        return branchLengthsAreTime(root) ? 'time' : 'divergence';
    };

    /**
     * True when the tree can be laid out by TIME and by DIVERGENCE. The rule
     * is joint with the desktop (Christian, 2026-09-29):
     *
     * - it states a divergence: recorded on its nodes (nextstrain:div), or a
     *   clock rate on every branch (BEAST; divergence is length x rate, and
     *   the lengths are its time). A tree with neither is never offered the
     *   switch, whatever its dates say;
     * - BOTH layouts can state EVERY branch: every node is dated, every
     *   branch states a length, and every node records its divergence (the
     *   root too) or every branch states a rate. A value that is stated is
     *   stated, whether zero or negative; only an absent one is missing. A
     *   branch is never drawn at 0 because a datum is missing;
     * - each picture has some depth (bothPicturesHaveDepth).
     *
     * Otherwise the switch is not offered and the tree stays in the layout it
     * arrived in. Whether the two pictures DIFFER is not asked, nor which way
     * the dates run.
     *
     * @param phy the tree
     * @returns {boolean}
     */
    forester.hasTimeAndDivergence = function (phy) {
        let root = forester.getTreeRoot(phy);
        if (!root || !everyNodeStates(root, auspiceNodeDate) || !everyLengthWasStated(root)
            || !divergenceStatesEveryBranch(root)) {
            return false;
        }
        return bothPicturesHaveDepth(root);
    };

    // ---- the CLOCK PLOT ---------------------------------------------------
    // Every node as a point, its DATE against its DIVERGENCE from the root,
    // and a straight line through the tips: the slope is the rate the tips
    // accumulated divergence at, and where the line comes down to the root's
    // divergence is the date it puts the root on. TempEst's and TreeTime's
    // root-to-tip regression, Auspice's clock view. Christian, 2026-10-02:
    // a panel linked to the tree, named "Clock plot"; JS only until the
    // design is final.
    //
    // THE RULE, designed for two kinds of tree (both built; the second on
    // Christian's word, 2026-10-03):
    //
    // 1. 'divergence' -- a tree with Time and Div (hasTimeAndDivergence): each
    //    node states a date and a divergence from the root, recorded on it or
    //    summed from each branch's time x its clock rate. Every node is a
    //    point; which of the two layouts is on screen does not matter.
    // 2. 'root-to-tip' -- a tree whose branch lengths are its divergence and
    //    whose tips are dated, with no date on any ancestor (it is not a time
    //    tree): a tip's divergence is the sum of the stated lengths from the
    //    root. Tips only. This is the tree a clock plot is wanted for most --
    //    is there a signal worth dating? -- and the one whose answer depends
    //    on its root. TempEst's plot. A tip is dated by what the file STATES
    //    for it (a phyloXML <date>, a num_date annotation), or else by the
    //    date its NAME carries, read by the label-date parser both programs
    //    anchor BEAST heights with (Christian, 2026-10-03: names count, as
    //    TempEst reads them; a year alone is mid-year). An ancestor is never
    //    dated by its name. Every tip must be dated and every branch must
    //    state a length: a tip with either missing has no point, and a plot
    //    that drops tips silently answers a different question than it was
    //    asked. A clock-model tree (a rate on every branch) is never this
    //    kind: its lengths are time.
    //
    // In both, the plot is offered when its LINE can be drawn: three tips or
    // more, and not all of them on one date. A tree sampled at one moment has
    // no slope to estimate. Asked of the tree, never of the view: in the view
    // of a clade too small or too uniform for a line the points are drawn and
    // the line is not.
    //
    // The FIT is ordinary least squares over the TIPS alone. A tip's date is
    // an observation; an ancestor's was inferred, usually with a clock, and
    // counting it would have the estimate confirm itself. The line is not
    // forced through the root, so the date it reaches the root's divergence
    // at can be held against the date the tree states for the root -- where
    // the tree states one; a root-to-tip tree does not, and there the line's
    // date is the only estimate.
    const CLOCK_PLOT_MIN_TIPS = 3;

    // A tip's date on a root-to-tip tree: the one the file states, else the
    // one its name carries (forester.parseTipLabelDate: the rightmost date in
    // the label; a year alone is mid-year; an ambiguous numeric date is read
    // day first). {value, fromName, ambiguous}, or null for no date at all.
    function rootToTipDate(n) {
        let d = auspiceNodeDate(n);
        if (d !== null) {
            return {value: d, fromName: false, ambiguous: false};
        }
        let m = (typeof n.name === 'string' && n.name.length > 0) ? forester.parseTipLabelDate(n.name) : null;
        return m ? {value: m.decimalYear, fromName: true, ambiguous: m.ambiguous === true} : null;
    }

    // Kind 2 of THE RULE, before the tips are counted: dated tips (by the
    // file or by their names), undated ancestors (by the file; a name dates
    // no ancestor), a length on every branch, no clock rates.
    function isRootToTipTree(root) {
        if (!root || root._divergenceFromRates === true || !everyLengthWasStated(root)) {
            return false;
        }
        let ok = true;
        forester.preOrderTraversalAll(root, function (n) {
            if (!ok) {
                return;
            }
            ok = (n.children && n.children.length > 0) ? auspiceNodeDate(n) === null : rootToTipDate(n) !== null;
        });
        return ok;
    }

    // Each node's distance from the root along the stated branch lengths:
    // a root-to-tip tree's divergence.
    function rootToTipDistances(root) {
        let dist = new Map();
        (function walk(n, d) {
            dist.set(n, d);
            let children = n.children;
            if (children) {
                for (let i = 0; i < children.length; ++i) {
                    let len = children[i].branch_length;
                    walk(children[i], d + ((typeof len === 'number' && isFinite(len)) ? len : 0));
                }
            }
        }(root, 0));
        return dist;
    }

    /**
     * Which clock plot the tree has, or null for none (see THE RULE above).
     *
     * @param phy the tree
     * @returns {string|null} 'divergence', 'root-to-tip', or null
     */
    forester.clockPlotKind = function (phy) {
        if (!phy) {
            return null;
        }
        let kind = forester.hasTimeAndDivergence(phy) ? 'divergence'
            : (isRootToTipTree(forester.getTreeRoot(phy)) ? 'root-to-tip' : null);
        if (kind === null) {
            return null;
        }
        let dateOf = kind === 'root-to-tip' ? function (n) {
            return rootToTipDate(n).value;    // every tip has one: the kind says so
        } : auspiceNodeDate;
        let tips = 0;
        let first = null;
        let differ = false;
        forester.preOrderTraversalAll(forester.getTreeRoot(phy), function (n) {
            if (n.children && n.children.length > 0) {
                return;
            }
            ++tips;
            let d = dateOf(n);
            if (first === null) {
                first = d;
            } else if (d !== first) {
                differ = true;
            }
        });
        return (tips >= CLOCK_PLOT_MIN_TIPS && differ) ? kind : null;
    };

    /**
     * Ordinary least squares of y on x, computed about the means: a date is
     * near 2020 and its spread a few months, and sums of raw squares would
     * spend every digit on the 2020.
     *
     * @param xs the dates
     * @param ys the divergences, as many
     * @returns {{n, slope, intercept, r2, meanX, meanY}|null} null with fewer
     *          than three points or every x the same; r2 is null where every
     *          y is the same (no variance to explain)
     */
    forester.clockRegression = function (xs, ys) {
        let n = xs.length;
        if (n < CLOCK_PLOT_MIN_TIPS || ys.length !== n) {
            return null;
        }
        let mx = 0;
        let my = 0;
        for (let i = 0; i < n; ++i) {
            mx += xs[i];
            my += ys[i];
        }
        mx /= n;
        my /= n;
        let sxx = 0;
        let sxy = 0;
        let syy = 0;
        for (let i = 0; i < n; ++i) {
            let dx = xs[i] - mx;
            let dy = ys[i] - my;
            sxx += dx * dx;
            sxy += dx * dy;
            syy += dy * dy;
        }
        if (!(sxx > 0) || !isFinite(sxx) || !isFinite(sxy) || !isFinite(syy)) {
            return null;
        }
        let slope = sxy / sxx;
        return {
            n: n,
            slope: slope,
            intercept: my - (slope * mx),
            r2: syy > 0 ? ((sxy * sxy) / (sxx * syy)) : null,
            meanX: mx,
            meanY: my
        };
    };

    /**
     * The clock plot of the tree, or of the clade on view.
     *
     * @param phy the tree: the RULE is asked of it
     * @param view the tree or clade to plot (the tree, a node, or the
     *        viewer's subtree root); the whole tree where none is given
     * @returns {object|null} null where the tree has no clock plot; else
     *   kind      'divergence' or 'root-to-tip'
     *   forward   whether the dates increase toward the tips (calendar
     *             dates); false for ages
     *   unit      the dates' unit as the tree states it, or null
     *   divUnit   the divergence's unit: 'subs/site' for a tree with Time
     *             and Div, a root-to-tip tree's stated branch length unit,
     *             or null
     *   fromRates whether the divergence is time x clock rate
     *   calendar  whether the dates are calendar years: their unit says so,
     *             or they were read off the tips' names
     *   fromNames how many tips are dated by their names (a root-to-tip
     *             tree; 0 otherwise)
     *   ambiguousNames how many of those names carry a day/month-ambiguous
     *             numeric date, read day first
     *   points    [{node, date, div, tip}] in preorder; the tips'
     *             `residual` is their divergence less the line's. On a
     *             root-to-tip tree the tips only: no ancestor has a date
     *   root      {node, date, div}: the top of the view; `date` null on a
     *             root-to-tip tree
     *   fit       forester.clockRegression over the tips, with
     *             rate      the slope in the direction time runs: divergence
     *                       per unit of time (negative where the tips'
     *                       divergence falls with time)
     *             rootDate  the date the line reaches the root's divergence
     *                       at; null unless the rate is positive
     *             or null where the view has no line
     */
    forester.clockPlotData = function (phy, view) {
        let kind = forester.clockPlotKind(phy);
        if (kind === null) {
            return null;
        }
        let treeRoot = forester.getTreeRoot(phy);
        // a node is its own top; the tree, or the viewer's holder of a clade,
        // is a parentless wrapper around it
        let top = treeRoot;
        if (view) {
            top = (!view.parent && view.children && view.children.length === 1) ? view.children[0] : view;
        }
        // a root-to-tip tree's divergence is the path from the TREE's root,
        // in the view of a clade too: the clade's top then has one as well
        let rootToTip = kind === 'root-to-tip';
        let dist = rootToTip ? rootToTipDistances(treeRoot) : null;
        let divOf = rootToTip ? function (n) {
            return dist.get(n);
        } : auspiceNodeDiv;
        let points = [];
        let xs = [];
        let ys = [];
        let fromNames = 0;
        let ambiguousNames = 0;
        forester.preOrderTraversalAll(top, function (n) {
            let tip = !(n.children && n.children.length > 0);
            let date = auspiceNodeDate(n);
            if (rootToTip && tip && date === null) {
                let named = rootToTipDate(n);    // every tip has one: the kind says so
                date = named ? named.value : null;
                if (named && named.fromName) {
                    ++fromNames;
                    if (named.ambiguous) {
                        ++ambiguousNames;
                    }
                }
            }
            let div = divOf(n);
            if (date === null || div === null) {
                return;    // an undated ancestor (root-to-tip), or a guard, not a rule
            }
            points.push({node: n, date: date, div: div, tip: tip});
            if (tip) {
                xs.push(date);
                ys.push(div);
            }
        });
        let info = forester.timeAxisInfo(treeRoot);
        // a root-to-tip tree has no dated pair to read the direction off:
        // its dates are calendar dates unless their unit says ages
        let forward = rootToTip ? info.type !== 'geologic' : timeIncreasesTowardTips(treeRoot);
        // calendar years: the dates' unit says so, or they were read off the
        // tips' names, which carry no other kind of date
        let calendar = forward && (info.type === 'calendar' || (rootToTip && info.type === null && fromNames > 0));
        let root = {node: top, date: auspiceNodeDate(top), div: divOf(top)};
        let fit = forester.clockRegression(xs, ys);
        if (fit) {
            fit.rate = forward ? fit.slope : -fit.slope;
            fit.rootDate = (fit.rate > 0 && root.div !== null)
                ? ((root.div - fit.intercept) / fit.slope) : null;
            for (let i = 0; i < points.length; ++i) {
                if (points[i].tip) {
                    points[i].residual = points[i].div - (fit.intercept + (fit.slope * points[i].date));
                }
            }
        }
        let lengthUnit = (phy !== treeRoot && typeof phy.branch_length_unit === 'string'
            && phy.branch_length_unit.trim().length > 0) ? phy.branch_length_unit.trim() : null;
        return {
            kind: kind,
            forward: forward,
            unit: dateUnitOf(treeRoot),
            divUnit: rootToTip ? lengthUnit : DIVERGENCE_LENGTH_UNIT,
            fromRates: !rootToTip && treeRoot._divergenceFromRates === true && !recordsDivergence(treeRoot),
            calendar: calendar,
            fromNames: fromNames,
            ambiguousNames: ambiguousNames,
            points: points,
            root: root,
            fit: fit
        };
    };

    // A number that is not NaN. It used to test only for null, undefined and
    // NaN, and so answered TRUE for "hello", "", {}, [] and true -- which no
    // caller was hurt by, since all of them pass the result of parseFloat, but
    // the name promised a check it did not make.
    //
    // Infinity is deliberately still accepted, so that this stays a rename in
    // behaviour as well as in intent: parseFloat('1e999') is Infinity, and
    // whether a branch length of Infinity should be refused is a separate
    // question from whether a string is a number.
    forester.isNumber = function (v) {
        return typeof v === 'number' && v === v;
    };

    // How a label is written into Newick or Nexus, ported from the desktop's
    // ForesterUtil.santitizeStringForNH so both programs emit the same token
    // for the same name. Quoting rather than transliterating is what makes a
    // save-and-reopen lossless: the previous rule mapped every quote, comma,
    // paren and space to '_', which no reader can undo, so a tip named
    // "Cooper's Hawk" came back "Cooper_s_Hawk".
    //
    // The one case that still loses information is a name carrying BOTH quote
    // styles: there is no quote character left to wrap it in, so the
    // apostrophes become backticks. The desktop does the same, deliberately;
    // that case is a JOINT open item and is NOT to be fixed on one side.
    //
    // Note the asymmetry with the READER: a doubled '' is how a quote is
    // escaped INSIDE a quoted token, and our reader un-doubles it, but neither
    // writer produces that form -- both sidestep it by switching quote style.
    // Reading a form you do not write is intentional here.
    function sanitizeLabelForNH(s) {
        let t = String(s).replace(/\s+/g, ' ').trim();
        let hasSingle = t.indexOf("'") > -1;
        let hasDouble = t.indexOf('"') > -1;
        if (hasSingle && hasDouble) {
            return "'" + t.replace(/'/g, '`') + "'";
        }
        if (hasSingle) {
            return '"' + t + '"';
        }
        if (hasDouble || /[\s,():;[\]]/.test(t)) {
            return "'" + t + "'";
        }
        return t;
    }

    /**
     * To convert a phylogentic tree object to a New Hampshire (Newick) formatted string.
     *
     * @param phy - A phylogentic tree object.
     * @param decPointsMax - Maximal number of decimal points for branch lengths (optional)
     * @param replaceChars - RETIRED 2026-09-10 and ignored. It used to map
     *        every space, comma, paren, colon, semicolon, bracket and quote to
     *        '_', which no reader can undo: a tip named "Cooper's Hawk" was
     *        written Cooper_s_Hawk and came back that way. Labels are now
     *        always quoted instead, by the same rule the desktop uses, so a
     *        save-and-reopen keeps the name. The parameter is still accepted
     *        so positional callers keep working.
     * @param writeConfidences - to write confidence values in brackets
     * @returns {*} - a New Hampshire (Newick) formatted string.
     */
    // The label a node is written under, in New Hampshire and in Nexus alike:
    // name, then taxonomy (code / scientific / common), then the sequence's
    // name / symbol / gene name, then its ACCESSION -- each step tried in turn,
    // so a taxonomy element that is present but empty does not stop the search.
    // An EXTERNAL node left with nothing gets a 'node<N>' placeholder, N being
    // its 1-based position in tip order; an empty label would not parse back
    // out of TaxLabels, and in Newick it names nothing at all. An INTERNAL node
    // does NOT get one: a placeholder there would invent a name for an
    // ancestor, and an unlabeled internal node is perfectly ordinary.
    //
    // ONE chain for both writers. A Nexus file whose TaxLabels, matrix rows and
    // trees block disagree about a taxon cannot be joined back up, and a tree
    // saved as Newick and as Nexus should name its tips the same way -- ours
    // did not, until 2026-09-23: toNexus applied this chain and toNewHampshire
    // wrote node.name and nothing else, so a nameless tip was HUMAN in one file
    // and empty in the other. It is the desktop's chain
    // (PhylogenyNode.toNewHampshire), adopted on Christian's word, and the two
    // programs' output is compared byte for byte in the tests.
    function nhNodeLabel(node, placeholder) {
        let s = node.name || '';
        if (!s && node.taxonomies && node.taxonomies.length > 0) {
            let t = node.taxonomies[0];
            s = t.code || t.scientific_name || t.common_name || '';
        }
        if (!s && node.sequences && node.sequences.length > 0) {
            let q = node.sequences[0];
            s = q.name || q.symbol || q.gene_name || '';
        }
        if (!s && node.sequences && node.sequences.length > 0) {
            let a = node.sequences[0].accession;
            s = (a && a.value) ? a.value : '';
        }
        if (!s && placeholder) {
            s = placeholder;
        }
        return s;
    }

    // A placeholder label for every external node, by tip index, in the order
    // the tips are written (getAllExternalNodes collects them in pre-order from
    // the far side, so reversing it gives left-to-right). Used only for a node
    // that nothing else names.
    //
    // A tip may literally be CALLED "node2". Minting that same token for a
    // different tip gives two taxa one label: illegal Nexus, and our own reader
    // takes the repeated row for an interleaved continuation and hands both
    // tips the two sequences joined together. So the labels the tree already
    // produces are collected first and stepped over.
    function tipPlaceholders(phy) {
        let m = new Map();
        // An EMPTY tree has no tips to number. Its single top node looks like
        // an external node -- no children -- and numbering it would turn the
        // empty string, which parseNewHampshire accepts and toNewHampshire has
        // always written back as the empty string, into "node1;". (The desktop
        // throws on an empty string rather than parsing it, so there is no
        // joint behaviour to match here, only ours to keep.)
        if (!phy.children || phy.children.length !== 1 || !phy.children[0].children) {
            return m;
        }
        let ext = forester.getAllExternalNodes(phy).reverse();
        let taken = Object.create(null);
        ext.forEach(function (n) {
            let label = nhNodeLabel(n, null);
            if (label.length > 0) {
                taken[label] = true;
            }
        });
        let i = 1;
        let spare = ext.length + 1;
        ext.forEach(function (n) {
            let candidate = 'node' + (i++);
            while (taken[candidate]) {
                candidate = 'node' + (spare++);
            }
            taken[candidate] = true;
            m.set(n, candidate);
        });
        return m;
    }

    forester.toNewHampshire = function (phy, decPointsMax, replaceChars, writeConfidences) {
        void replaceChars; // retired: see the note above; labels are always quoted now
        let nh = "";
        let tips = tipPlaceholders(phy);
        if (phy.children && phy.children.length === 1) {
            toNewHampshireHelper(phy.children[0], true);
        }
        if (nh.length > 0) {
            return nh + ";";
        }
        return nh;

        function toNewHampshireHelper(node, last) {
            if (node.children) {
                let l = node.children.length;
                nh += "(";
                for (let i = 0; i < l; ++i) {
                    toNewHampshireHelper(node.children[i], i === l - 1);
                }
                nh += ")";
            }
            let label = nhNodeLabel(node, tips.get(node));
            if (label.length > 0) {
                nh += sanitizeLabelForNH(label);
            }
            if (node.branch_length !== undefined && node.branch_length !== null) {
                if (decPointsMax && decPointsMax > 0) {
                    nh += ":" + forester.roundNumber(node.branch_length, decPointsMax);
                } else {
                    nh += ":" + node.branch_length;
                }
            }
            // the support slot holds support: a MAD value (madRoot) never goes
            // there -- it would read as support, and would crowd out the
            // bootstrap on a branch carrying both. phyloXML keeps it, typed.
            //
            // Newick has ONE slot and phyloXML allows many, so a node carrying
            // both a bootstrap and a posterior has to lose one of them. It
            // writes the FIRST that is not MAD, which is what the desktop's
            // BranchData.getSupportConfidence returns and therefore what its
            // writer emits -- verified by running it: a node with
            // bootstrap=95 and posterior=0.99 comes out as "ab:0.3[95]".
            // This used to require EXACTLY one and so wrote nothing at all,
            // which lost the bootstrap too, on real files: the repo's own
            // phyloWithConfidences.xml has a node carrying bootstrap and
            // likelihood together.
            let support = writeConfidences && node.confidences
                ? node.confidences.filter(function (c) { return c.type !== forester.MAD_CONFIDENCE_TYPE; })
                : [];
            if (support.length > 0 && support[0].value !== undefined && support[0].value !== null) {
                if (decPointsMax && decPointsMax > 0) {
                    nh += "[" + forester.roundNumber(support[0].value, decPointsMax) + "]";
                } else {
                    nh += "[" + support[0].value + "]";
                }
            }
            if (!last) {
                nh += ",";
            }
        }

    };

    // A node's molecular sequence for the Nexus matrix: the first one carrying
    // residues, or null. is_aligned is deliberately NOT consulted -- the
    // desktop's writer does not have that flag and decides on the lengths
    // instead, and equal lengths are what a character matrix actually
    // requires. (A JS node may hold several sequences where a desktop node
    // holds one; the first with residues is the one written.)
    function molSeqOfNode(node) {
        if (!node.sequences) {
            return null;
        }
        for (let j = 0; j < node.sequences.length; ++j) {
            let q = node.sequences[j];
            if (q.mol_seq && q.mol_seq.value) {
                return q.mol_seq.value;
            }
        }
        return null;
    }

    // ForesterUtil.guessMolecularSequenceType, ported verbatim so that the two
    // programs declare the same DataType for the same residues: the letters a
    // nucleotide sequence cannot contain, then T for DNA and U for RNA, and
    // null when the sequence says nothing either way (all gaps, or A/C/G
    // alone). Note that it reads the residues and ignores any DECLARED type,
    // which is the desktop's rule and therefore ours -- and the right one:
    // bunya_glyco.xml declares type="protein" over 121 sequences of pure ACGT.
    //
    // F, P and V were added 2026-09-23 on Christian's word, jointly with the
    // desktop, because the test was missing most of the protein-exclusive
    // alphabet: under forester's own alphabets that is BDEFHILOPQVXZ and only
    // DEHILQ were tested. A protein built solely from nucleotide letters
    // therefore guessed DNA -- MKATSWNP has exactly one protein-exclusive
    // residue and it was P -- and a matrix wrongly declared DNA comes back
    // with every non-nucleotide residue replaced by N. Measured against
    // UniProt residue frequencies, the chance a protein carries none of the
    // tested letters falls from 12.5% to 3.3% at length 5 and from 1.6% to
    // 0.11% at length 10; a real alignment of a hundred columns was never at
    // risk either way. O, X and Z are protein-exclusive too and deliberately
    // left out: rare enough to buy almost nothing, and every letter added is
    // one both programs must add.
    //
    // V (and B, D, H) are nucleotide ambiguity codes in full IUPAC but not in
    // forester's DNA alphabet, which maps them to N, so they cannot survive in
    // a DNA sequence here and testing them for protein is consistent.
    //
    // NEITHER PROGRAM RETUNES THIS ALONE. A letter added on one side types the
    // same file two ways, which is worse than a blind spot they share;
    // testNexusMatrixDatatype pins the alphabet letter for letter.
    function guessMolSeqType(v) {
        let s = v.toUpperCase();
        if (s.indexOf('L') >= 0 || s.indexOf('I') >= 0 || s.indexOf('E') >= 0
            || s.indexOf('H') >= 0 || s.indexOf('D') >= 0 || s.indexOf('Q') >= 0
            || s.indexOf('F') >= 0 || s.indexOf('P') >= 0 || s.indexOf('V') >= 0) {
            return 'Protein';
        }
        if (s.indexOf('T') >= 0) {
            return 'DNA';
        }
        if (s.indexOf('U') >= 0) {
            return 'RNA';
        }
        return null;
    }

    // Writes a phylogeny as a Nexus-formatted string, ported from the
    // desktop's PhylogenyWriter: a TAXA block (Dimensions, TaxLabels) and a
    // TREES block (the tree under its name, [&R]/[&U] rootedness, the same
    // safe-character Newick toNewHampshire writes). Beyond the desktop
    // template, tips carrying ALIGNED molecular sequences also get a
    // CHARACTERS block (Dimensions, Format with the datatype, Matrix) --
    // carrying the tree and its alignment in one file is the point of Nexus,
    // and parseNexus reads the alignment back onto the tips.
    forester.toNexus = function (phy, decPointsMax, writeConfidences) {
        // The TaxLabels tokens, the Matrix row labels and the tree's tip
        // tokens must be byte-identical or nothing can join them back up, so
        // all three go through sanitizeLabelForNH -- the same helper
        // toNewHampshire writes the tree with.
        //
        // The chain lives in nhNodeLabel, shared with toNewHampshire, and it
        // returns the label UNQUOTED because every use here quotes it through
        // sanitizeLabelForNH -- the same helper the tree string goes through.
        //
        // No tip is renamed on the way past any more. This used to assign the
        // computed label to node.name for nameless tips so that toNewHampshire,
        // which knew only node.name, would write the same token; the caller's
        // tree was mutated and restored around the call. Now both writers ask
        // the same function, so there is nothing to patch up.
        let ext = forester.getAllExternalNodes(phy).reverse();
        let tips = tipPlaceholders(phy);

        function nexusLabel(node) {
            return nhNodeLabel(node, tips.get(node));
        }

        let s = '#NEXUS\n';
        s += 'Begin Taxa;\n';
        s += ' Dimensions NTax=' + ext.length + ';\n';
        s += ' TaxLabels';
        ext.forEach(function (node) {
            s += ' ' + sanitizeLabelForNH(nexusLabel(node));
        });
        s += ';\n';
        s += 'End;\n';

        // Every tip's label and, where there is one, its molecular sequence.
        // Written to the desktop's rules (PhylogenyWriter.writeNexusCharactersBlock),
        // because a Nexus file is a joint artifact: whichever program wrote it,
        // the other has to read the same bytes back.
        let labels = ext.map(function (node) {
            return sanitizeLabelForNH(nexusLabel(node));
        });
        let withSeq = [];
        ext.forEach(function (node, i) {
            let v = molSeqOfNode(node);
            if (v !== null) {
                withSeq.push({i: i, value: v});
            }
        });
        if (withSeq.length > 0) {
            // A matrix is keyed on the taxon label, so two tips sharing one
            // cannot be told apart: our reader takes the second row for an
            // interleaved continuation and hands BOTH tips the two sequences
            // joined together. The Taxa and Trees blocks have always written
            // such a tree -- invalid Nexus, but only cosmetically; a matrix
            // would make it corrupting, so it is not written.
            let seen = Object.create(null);
            let duplicate = null;
            for (let k = 0; k < labels.length; ++k) {
                if (seen[labels[k]]) {
                    duplicate = labels[k];
                    break;
                }
                seen[labels[k]] = true;
            }
            if (duplicate !== null) {
                s += '[ Molecular sequences were not written: two or more tips share the taxon label '
                    + duplicate + ', and a character matrix keyed on an ambiguous label cannot be'
                    + ' read back. ]\n';
                withSeq = [];
            }
        }
        if (withSeq.length > 0) {
            let nchar = withSeq[0].value.length;
            let ragged = -1;
            for (let k = 1; k < withSeq.length; ++k) {
                if (withSeq[k].value.length !== nchar) {
                    ragged = withSeq[k].value.length;
                    break;
                }
            }
            if (ragged >= 0) {
                // A Nexus matrix is rectangular, so sequences of unequal length
                // cannot be one: padding them would state an alignment that
                // does not exist. Say so in the file rather than leaving the
                // reader to wonder where the data went.
                s += '[ Molecular sequences were not written: they are of unequal length ('
                    + nchar + ' vs ' + ragged + '), so they are not an alignment and cannot'
                    + ' form a Nexus character matrix. ]\n';
            } else {
                // The datatype is a property of the whole MATRIX, so every
                // sequence decides it, not the first one that guesses non-null.
                // guessMolSeqType looks for residues only protein has, so a
                // short protein made of nucleotide letters guesses DNA -- and a
                // matrix wrongly declared DNA is read back with every
                // non-nucleotide residue replaced by N (MKATSWNP came back
                // MKATSWNN). Protein therefore wins any disagreement: calling a
                // nucleotide alignment protein leaves the residues readable,
                // the reverse destroys them. Gaps and missing symbols are
                // stripped first so they cannot sway the guess.
                let datatype = 'Protein';
                let sawAa = false;
                let sawNt = false;
                let isRna = false;
                for (let k = 0; k < withSeq.length; ++k) {
                    let bare = withSeq[k].value.replace(/[-.?*]/g, '');
                    if (bare.length < 1) {
                        continue;
                    }
                    let t = guessMolSeqType(bare);
                    if (t === 'DNA') {
                        sawNt = true;
                    } else if (t === 'RNA') {
                        sawNt = true;
                        isRna = true;
                    } else if (t !== null) {
                        sawAa = true;
                    }
                }
                if (sawNt && !sawAa) {
                    datatype = isRna ? 'RNA' : 'DNA';
                }
                let width = 0;
                labels.forEach(function (l) {
                    width = Math.max(width, l.length);
                });
                ++width;
                let missing = '?'.repeat(nchar);
                let byTip = Object.create(null);
                withSeq.forEach(function (r) {
                    byTip[r.i] = r.value;
                });
                s += 'Begin Characters;\n';
                // NChar ONLY: the Nexus standard allows NTax in a CHARACTERS
                // block's DIMENSIONS solely alongside NEWTAXA (the taxa are the
                // TAXA block's), and strict readers -- jebl, and so AliView --
                // reject the file over it
                s += ' Dimensions NChar=' + nchar + ';\n';
                s += ' Format DataType=' + datatype + ' Interleave=No Gap=- Missing=?;\n';
                s += ' Matrix\n';
                // A row per TAXON, not per sequence: a tip carrying none gets a
                // row of the missing symbol, so the matrix covers every taxon
                // the Taxa block declares. parseNexus reads such a row back as
                // absence of data, never as a sequence of question marks.
                labels.forEach(function (label, i) {
                    s += '  ' + label + ' '.repeat(width - label.length) + ' '
                        + (byTip[i] === undefined ? missing : byTip[i]) + '\n';
                });
                s += ' ;\n';
                s += 'End;\n';
            }
        }

        s += 'Begin Trees;\n';
        // the tree name was stripped of its quotes for the same reason the tip
        // names were transliterated, and loses an apostrophe the same way
        let treeName = phy.name ? String(phy.name).trim() : '';
        s += ' Tree ' + (treeName ? sanitizeLabelForNH(treeName) : 'tree1') + '=';
        s += (phy.rooted === false) ? '[&U]' : '[&R]';
        let nh = forester.toNewHampshire(phy, decPointsMax, true, writeConfidences);
        if (nh.length === 0) {
            // an empty tree would otherwise write "Tree tree1=[&R]" with no
            // tree and no terminating ';' -- a syntactically invalid file
            throw new Error('toNexus: the tree is empty (nothing to write)');
        }
        s += nh + '\n';
        s += 'End;\n';
        return s;
    };

    forester.getMolecularSequencesAsFasta = function (node, sep) {
        let fasta_all = '';
        let ext_nodes = forester.getAllExternalNodes(node).reverse();
        for (let j = 0, l = ext_nodes.length; j < l; ++j) {
            let n = ext_nodes[j];
            if (n.sequences) {
                for (let i = 0; i < n.sequences.length; ++i) {
                    let s = n.sequences[i];
                    if (s.mol_seq && s.mol_seq.value && s.mol_seq.value.length > 0) {
                        let seq = s.mol_seq.value;
                        let seqname = j;
                        if (s.name && s.name.length > 0) {
                            seqname = s.name
                        } else if (n.name && n.name.length > 0) {
                            seqname = n.name
                        }
                        let split_seq_ary = seq.match(/.{1,80}/g);
                        let split_seq = '';
                        for (let ii = 0; ii < split_seq_ary.length; ++ii) {
                            split_seq += split_seq_ary[ii] + sep;
                        }

                        let fasta = '>' + seqname + sep + split_seq;
                        fasta_all += fasta;
                    }
                }
            }
        }
        return fasta_all;
    }

    forester.roundNumber = function (num, dec) {
        return Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
    };

    forester.isString = function (s) {
        return (typeof s === 'string' || s instanceof String);
    };


    // --------------------------------------------------------------
    // Protein domain architectures
    // --------------------------------------------------------------
    // The pure half of drawing <domain_architecture> beside the tips, ported
    // from desktop Archaeopteryx (RenderableDomainArchitecture, TreePanel and
    // AptxUtil at forester 416c705b; the spec is section D1 of the repo's
    // TODO.md): which domains a threshold admits, where each box sits along
    // the backbone, the Tableau palette and how names take their colours,
    // the legend rows and the E-value readout. Everything a fixture can pin
    // lives here and test/domain_test.js checks it against the numbers the
    // desktop computed by running its own classes on apaf.xml. The SVG and
    // the controls are archaeopteryx.js's.

    const DOMAIN_PALETTE = ['#4E79A7', '#F28E2B', '#E15759', '#76B7B2', '#59A14F',
        '#EDC948', '#B07AA1', '#FF9DA7', '#9C755F', '#BAB0AC'];   // Tableau 10
    forester.DOMAIN_UNNAMED_COLOR = '#808080';                   // a domain with no name
    forester.DOMAIN_EVALUE_EXPONENT_DEFAULT = -3;
    forester.DOMAIN_EVALUE_EXPONENT_MIN = -20;
    forester.DOMAIN_EVALUE_EXPONENT_MAX = 3;

    function hexToRgb(hex) {
        let n = parseInt(hex.substring(1), 16);
        return [(n >> 16) & 255, (n >> 8) & 255, n & 255];
    }

    function rgbToHex(rgb) {
        return '#' + rgb.map(function (c) {
            let s = Math.max(0, Math.min(255, Math.round(c))).toString(16);
            return s.length < 2 ? '0' + s : s;
        }).join('');
    }

    // Colour i of the qualitative sequence: Tableau 10 for the first ten,
    // then the same ten shifted toward white (odd cycles) or black (even
    // cycles), further with each cycle, capped at 0.55.
    forester.domainQualitativeColor = function (i) {
        let base = hexToRgb(DOMAIN_PALETTE[i % DOMAIN_PALETTE.length]);
        let cycle = Math.floor(i / DOMAIN_PALETTE.length);
        if (cycle === 0) {
            return rgbToHex(base);
        }
        let t = Math.min(0.55, 0.2 * cycle);
        let toward = (cycle % 2 === 1) ? 255 : 0;
        return rgbToHex(base.map(function (c) {
            return Math.round(c + t * (toward - c));
        }));
    };

    forester.domainLighten = function (hex, t) {
        return rgbToHex(hexToRgb(hex).map(function (c) {
            return c + Math.round((255 - c) * t);
        }));
    };

    forester.domainDarken = function (hex, t) {
        return rgbToHex(hexToRgb(hex).map(function (c) {
            return Math.round(c * (1 - t));
        }));
    };

    // The ink a name is written in on its box: near-black on a light base,
    // white on a dark one.
    forester.domainLabelInk = function (hex) {
        let c = hexToRgb(hex);
        let lum = (0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]) / 255;
        return lum > 0.55 ? '#141a1d' : '#ffffff';
    };

    forester.domainEvalueThreshold = function (exponent) {
        return Math.pow(10, exponent);
    };

    // "10" with the exponent in superscript digits: 10⁻³, 10⁰, 10³.
    const SUPERSCRIPT_DIGITS = ['⁰', '¹', '²', '³', '⁴', '⁵', '⁶', '⁷', '⁸', '⁹'];
    forester.domainEvalueLabel = function (exponent) {
        let digits = String(Math.abs(exponent)).split('').map(function (d) {
            return SUPERSCRIPT_DIGITS[+d];
        }).join('');
        return '10' + (exponent < 0 ? '⁻' : '') + digits;
    };

    function domainNumber(v) {
        return (v === null || v === undefined || v === '') ? NaN : Number(v);
    }

    // The architecture a node carries: the first of its sequences that has
    // one, provided its length is a positive integer -- without a length
    // there is no backbone to draw on. The desktop refuses the whole file in
    // that case; refusing input over a drawing attribute is the one kind of
    // strictness this library does not copy, so the architecture is simply
    // not drawn.
    forester.domainArchitectureOf = function (node) {
        if (!node.sequences) {
            return null;
        }
        for (let i = 0; i < node.sequences.length; ++i) {
            let da = node.sequences[i].domain_architecture;
            if (da) {
                let L = domainNumber(da.length);
                return (Number.isInteger(L) && L > 0) ? da : null;
            }
        }
        return null;
    };

    // The drawable domains of an architecture, in from order (equal froms
    // keep file order). A malformed domain -- a coordinate missing or not an
    // integer, to <= from, no E-value -- is left out and counted, never
    // fatal; the caller reports the count once.
    forester.domainArchitectureDomains = function (da) {
        let domains = [];
        let ignored = 0;
        (da.domains || []).forEach(function (d) {
            let from = domainNumber(d.from);
            let to = domainNumber(d.to);
            let e = domainNumber(d.confidence);
            if (!Number.isInteger(from) || !Number.isInteger(to) || to <= from || !isFinite(e)) {
                ignored++;
                return;
            }
            // `id` is phyloXML's optional domain identifier, which for a Pfam
            // scan is the accession (PF00931). Carried through because the
            // hover readout links to the entry with it when it is there, and
            // has to search by name when it is not -- apaf.xml, our own domain
            // demo, has names and no ids at all.
            domains.push({name: d.name ? String(d.name) : '', from: from, to: to,
                length: to - from + 1, evalue: e, id: d.id ? String(d.id) : ''});
        });
        domains.sort(function (a, b) {
            return a.from - b.from;
        });
        return {domains: domains, ignored: ignored};
    };

    // The tree's domain facts: tips carrying an architecture, the longest
    // architecture (Lmax -- it sets one scale for every track and, counting
    // every domain whatever its E-value, never moves with the threshold),
    // the drawable domain count and the malformed count.
    forester.domainArchitectureStats = function (tree) {
        let stats = {tips: 0, maxLength: 0, domains: 0, ignored: 0};
        forester.preOrderTraversalAll(tree, function (n) {
            if (n.children) {
                return;
            }
            let da = forester.domainArchitectureOf(n);
            if (!da) {
                return;
            }
            stats.tips++;
            let L = Number(da.length);
            if (L > stats.maxLength) {
                stats.maxLength = L;
            }
            let dd = forester.domainArchitectureDomains(da);
            stats.domains += dd.domains.length;
            stats.ignored += dd.ignored;
        });
        return stats;
    };

    // What a threshold admits over the tips: the distinct drawn names sorted
    // by UTF-16 code unit (the palette's order -- Java's string order,
    // uppercase before lowercase, so DED sorts before Death), the drawn box
    // count, the legend rows in first-appearance order (tips in the order
    // given, domains in from order) each with its count of drawn boxes, and
    // how many drawn boxes have no name. `tips` is a root, walked in
    // preorder, or an array of tips in DISPLAY order -- the legend reads
    // the way the eye goes down the tree, which a ladderized display does
    // not do in data order.
    forester.domainSummary = function (tips, exponent) {
        let T = forester.domainEvalueThreshold(exponent);
        let counts = Object.create(null);
        let legend = [];
        let boxes = 0;
        let unnamed = 0;
        let nodes = tips;
        if (!Array.isArray(tips)) {
            nodes = [];
            forester.preOrderTraversalAll(tips, function (n) {
                if (!n.children) {
                    nodes.push(n);
                }
            });
        }
        nodes.forEach(function (n) {
            let da = forester.domainArchitectureOf(n);
            if (!da) {
                return;
            }
            forester.domainArchitectureDomains(da).domains.forEach(function (d) {
                if (d.evalue > T) {
                    return;
                }
                boxes++;
                if (d.name.length === 0) {
                    unnamed++;
                    return;
                }
                if (!(d.name in counts)) {
                    counts[d.name] = 0;
                    legend.push({name: d.name, count: 0});
                }
                counts[d.name]++;
            });
        });
        legend.forEach(function (row) {
            row.count = counts[row.name];
        });
        return {names: Object.keys(counts).sort(), boxes: boxes, legend: legend, unnamed: unnamed};
    };

    // One architecture's geometry: the backbone and the boxes the threshold
    // admits, given its start x and the px per residue f. Residue r covers
    // [(r-1) f, r f], so a domain from..to spans [start + (from-1) f,
    // start + to f] and a domain 1..L is exactly the backbone (decided with
    // the desktop 2026-09-12; until its 416c705b it placed at from f, one
    // residue to the right). A box with no width is skipped.
    forester.domainBoxes = function (da, start, f, exponent) {
        let T = forester.domainEvalueThreshold(exponent);
        let boxes = [];
        forester.domainArchitectureDomains(da).domains.forEach(function (d) {
            if (d.evalue > T) {
                return;
            }
            let w = d.length * f;
            if (!(w > 0) || !isFinite(w)) {
                return;
            }
            boxes.push({name: d.name, x: start + ((d.from - 1) * f), w: w, from: d.from, to: d.to,
                evalue: d.evalue, id: d.id});
        });
        return {backbone: {x: start, w: Number(da.length) * f}, boxes: boxes};
    };

    // --------------------------------------------------------------
    // Genome regions (the gene track)
    // --------------------------------------------------------------
    // A tip that is a genome can carry (part of) that genome as phyloXML
    // <sequence> elements: one per gene, each with a <location> on a contig
    // and its family in an <annotation ref="...">, plus one record per
    // contig. The viewer draws them as arrows on a backbone beside the tip,
    // the rows anchored on one family so gene order compares across genomes
    // (Christian, 2026-10-01: the encoding, the location grammar, anchoring
    // by default, the words "Genes" and "Genome regions"). Nothing in the
    // schema changes: <location> is phyloXML's "location of a sequence on a
    // genome/chromosome", an annotation's ref its "colon-separated reference
    // to an external database".
    //
    // Everything that can be tested without a DOM lives here: the grammar,
    // which sequences are genes and which the contig record, the anchor, the
    // mapping of every row onto one shared coordinate (flips, lanes, contig
    // ends), the arrow outline, the families, the legend rows and the links.
    // test/gene_test.js pins the numbers. The SVG and the controls are
    // archaeopteryx.js's.

    // contig:start-end(+) -- 1-based, inclusive, the strand optional. Split
    // on the LAST colon so a contig id with a colon of its own survives.
    // A located sequence WITH a strand is a gene; one WITHOUT is the record
    // of the contig itself (its accession, name and length), which is how
    // the drawing knows where a contig ends.
    const SEQUENCE_LOCATION_RE = /^(.+):(\d+)-(\d+)(?:\(([+-])\))?$/;

    forester.parseSequenceLocation = function (text) {
        if (typeof text !== 'string') {
            return null;
        }
        let m = SEQUENCE_LOCATION_RE.exec(text.trim());
        if (!m) {
            return null;
        }
        let start = Number(m[2]);
        let end = Number(m[3]);
        if (!Number.isSafeInteger(start) || !Number.isSafeInteger(end) || start < 1 || end < start) {
            return null;
        }
        let contig = m[1].trim();
        if (contig.length === 0) {
            return null;
        }
        return {contig: contig, start: start, end: end, strand: m[4] || null};
    };

    // The namespace of an annotation ref: "pgfam" of "pgfam:PGF_00019355".
    // A ref without a colon has none and names no family.
    forester.annotationNamespace = function (ref) {
        if (typeof ref !== 'string') {
            return null;
        }
        let c = ref.indexOf(':');
        return c > 0 ? ref.substring(0, c) : null;
    };

    function textOf(v) {
        return (v === null || v === undefined) ? '' : String(v);
    }

    // Every located sequence of a node, sorted into genes, contig records
    // and the ones whose location is not in the grammar (counted, never
    // fatal: <location> is free text in phyloXML, and an older file may say
    // "chromosome 3" there).
    function locatedSequences(node) {
        let out = {genes: [], records: [], ignored: 0};
        if (!node.sequences) {
            return out;
        }
        node.sequences.forEach(function (s, i) {
            if (!s || s.location === undefined || s.location === null || s.location === '') {
                return;
            }
            let loc = forester.parseSequenceLocation(s.location);
            if (!loc) {
                out.ignored++;
                return;
            }
            let acc = (s.accession && s.accession.value !== undefined && s.accession.value !== null
                && s.accession.value !== '')
                ? {source: textOf(s.accession.source), value: textOf(s.accession.value)} : null;
            if (loc.strand === null) {
                out.records.push({contig: loc.contig, start: loc.start, end: loc.end, accession: acc,
                    name: textOf(s.name)});
                return;
            }
            let refs = [];
            (s.annotations || []).forEach(function (a) {
                if (a && forester.annotationNamespace(a.ref) !== null) {
                    refs.push(a.ref);
                }
            });
            out.genes.push({index: i, contig: loc.contig, start: loc.start, end: loc.end, strand: loc.strand,
                length: loc.end - loc.start + 1, symbol: textOf(s.symbol), geneName: textOf(s.gene_name),
                product: textOf(s.name), accession: acc, refs: refs});
        });
        return out;
    }

    // The region a tip carries: its genes on ONE contig -- the contig with
    // the most genes (ties: the first listed) -- in position order, with
    // that contig's record when there is one, the span of the genes, and
    // how many genes on other contigs were left out. Null for a tip with no
    // genes (a record alone is not a region).
    forester.geneRegionOf = function (node) {
        let found = locatedSequences(node);
        if (found.genes.length === 0) {
            return null;
        }
        let counts = new Map();
        found.genes.forEach(function (g) {
            counts.set(g.contig, (counts.get(g.contig) || 0) + 1);
        });
        let contig = null;
        let best = 0;
        counts.forEach(function (n, c) {
            if (n > best) {
                best = n;
                contig = c;
            }
        });
        let genes = found.genes.filter(function (g) {
            return g.contig === contig;
        }).sort(function (a, b) {
            return (a.start - b.start) || (a.end - b.end) || (a.index - b.index);
        });
        let record = null;
        for (let i = 0; i < found.records.length; ++i) {
            if (found.records[i].contig === contig) {
                record = found.records[i];
                break;
            }
        }
        let span = {start: genes[0].start, end: genes[0].end};
        genes.forEach(function (g) {
            if (g.end > span.end) {
                span.end = g.end;
            }
        });
        return {contig: contig, genes: genes, record: record, span: span,
            otherContigs: found.genes.length - genes.length, contigs: counts.size};
    };

    // The tree's gene facts: tips with a region, genes drawn, genes on other
    // contigs left out, the longest span, and locations not in the grammar.
    forester.geneRegionStats = function (tree) {
        let stats = {tips: 0, genes: 0, otherContigs: 0, maxSpan: 0, ignored: 0};
        forester.preOrderTraversalAll(tree, function (n) {
            if (n.children) {
                return;
            }
            stats.ignored += locatedSequences(n).ignored;
            let r = forester.geneRegionOf(n);
            if (!r) {
                return;
            }
            stats.tips++;
            stats.genes += r.genes.length;
            stats.otherContigs += r.otherContigs;
            let L = r.span.end - r.span.start + 1;
            if (L > stats.maxSpan) {
                stats.maxSpan = L;
            }
        });
        return stats;
    };

    // The family namespaces the genes carry, most-used first (ties by code
    // unit): a BV-BRC export has both pgfam and plfam on every gene, and the
    // track colours by ONE of them.
    forester.geneFamilyNamespaces = function (regions) {
        let counts = new Map();
        regions.forEach(function (r) {
            if (!r) {
                return;
            }
            r.genes.forEach(function (g) {
                let seen = new Set();
                g.refs.forEach(function (ref) {
                    let ns = forester.annotationNamespace(ref);
                    if (ns !== null && !seen.has(ns)) {
                        seen.add(ns);
                        counts.set(ns, (counts.get(ns) || 0) + 1);
                    }
                });
            });
        });
        let out = [];
        counts.forEach(function (genes, ns) {
            out.push({ns: ns, genes: genes});
        });
        out.sort(function (a, b) {
            return (b.genes - a.genes) || (a.ns < b.ns ? -1 : (a.ns > b.ns ? 1 : 0));
        });
        return out;
    };

    // A gene's family in a namespace: its first annotation ref there, in
    // full ("pgfam:PGF_00019355"), or null.
    forester.geneFamilyOf = function (gene, ns) {
        if (!ns) {
            return null;
        }
        for (let i = 0; i < gene.refs.length; ++i) {
            if (forester.annotationNamespace(gene.refs[i]) === ns) {
                return gene.refs[i];
            }
        }
        return null;
    };

    // Which families could anchor the rows: for each, the regions carrying
    // it exactly once (where it CAN anchor) and the regions carrying it at
    // all; most single-copy regions first, ties by code unit. A product and
    // symbol ride along for the control's labels.
    forester.geneAnchorCandidates = function (regions, ns) {
        let fams = new Map();
        regions.forEach(function (r) {
            if (!r) {
                return;
            }
            let here = new Map();
            r.genes.forEach(function (g) {
                let fam = forester.geneFamilyOf(g, ns);
                if (fam === null) {
                    return;
                }
                here.set(fam, (here.get(fam) || 0) + 1);
                if (!fams.has(fam)) {
                    fams.set(fam, {family: fam, single: 0, tips: 0, product: g.product, symbol: g.symbol});
                }
            });
            here.forEach(function (n, fam) {
                let f = fams.get(fam);
                f.tips++;
                if (n === 1) {
                    f.single++;
                }
            });
        });
        let out = [];
        fams.forEach(function (f) {
            out.push(f);
        });
        out.sort(function (a, b) {
            return (b.single - a.single) || (a.family < b.family ? -1 : (a.family > b.family ? 1 : 0));
        });
        return out;
    };

    // The anchor a tree opens with: the family single-copy in the most
    // regions, provided that is at least half of them. Otherwise none, and
    // the rows are left-aligned.
    forester.geneDefaultAnchor = function (regions, ns) {
        let n = 0;
        regions.forEach(function (r) {
            if (r) {
                n++;
            }
        });
        let c = forester.geneAnchorCandidates(regions, ns);
        return (c.length > 0 && c[0].single > 0 && c[0].single * 2 >= n) ? c[0].family : null;
    };

    // The backbone reaches this far past the first and last gene, as a share
    // of the span -- unless the contig record says the contig ends first.
    forester.GENE_REGION_PAD = 0.02;

    // Every row onto one shared coordinate, in base pairs. A row whose
    // anchor family occurs exactly once puts that gene's midpoint at 0 and,
    // when the gene is on the minus strand, is MIRRORED so the anchor points
    // right and the neighbours flip with it (the Compare Region convention).
    // Any other row -- no anchor, no such gene, two of them -- is left-
    // aligned at the leftmost edge of the anchored rows (at 0 when there are
    // none). Overlapping genes take a second lane. `regions` is per
    // displayed tip, in display order, null where a tip has none; the result
    // has a row (or null) per entry and the extent over all rows.
    forester.geneTrackRows = function (regions, anchor, ns) {
        let rows = regions.map(function (region) {
            if (!region) {
                return null;
            }
            let span = region.span;
            let pad = forester.GENE_REGION_PAD * (span.end - span.start + 1);
            let b0 = span.start - pad;
            let b1 = span.end + pad;
            let capStart = false;
            let capEnd = false;
            if (region.record) {
                if (b0 <= region.record.start) {
                    b0 = region.record.start;
                    capStart = true;
                }
                if (b1 >= region.record.end) {
                    b1 = region.record.end;
                    capEnd = true;
                }
            }
            let anchors = anchor ? region.genes.filter(function (g) {
                return forester.geneFamilyOf(g, ns) === anchor;
            }) : [];
            let anchored = anchors.length === 1;
            let flip = anchored && anchors[0].strand === '-';
            return {region: region, b0: b0, b1: b1, capStart: capStart, capEnd: capEnd, anchored: anchored, flip: flip,
                origin: anchored ? (anchors[0].start + anchors[0].end) / 2 : null};
        });
        let min = Infinity;
        let max = -Infinity;
        let place = function (row) {
            let o = row.origin;
            let map = row.flip
                ? function (a, b) {
                    return [o - b, o - a];
                }
                : function (a, b) {
                    return [a - o, b - o];
                };
            let bb = map(row.b0, row.b1);
            let genes = row.region.genes.map(function (g) {
                let x = map(g.start, g.end);
                let dir = g.strand === '+' ? 1 : -1;
                return {gene: g, x0: x[0], x1: x[1], dir: row.flip ? -dir : dir, lane: 0};
            });
            genes.sort(function (p, q) {
                return (p.x0 - q.x0) || (p.x1 - q.x1);
            });
            // greedy lanes: the first lane that is free, else the one that
            // frees first; two lanes at most
            let ends = [-Infinity, -Infinity];
            genes.forEach(function (g) {
                let lane = g.x0 >= ends[0] ? 0 : (g.x0 >= ends[1] ? 1 : (ends[0] <= ends[1] ? 0 : 1));
                g.lane = lane;
                ends[lane] = Math.max(ends[lane], g.x1);
            });
            if (bb[0] < min) {
                min = bb[0];
            }
            if (bb[1] > max) {
                max = bb[1];
            }
            return {backbone: {x0: bb[0], x1: bb[1]}, capLeft: row.flip ? row.capEnd : row.capStart,
                capRight: row.flip ? row.capStart : row.capEnd, anchored: row.anchored, flip: row.flip,
                lanes: genes.some(function (g) {
                    return g.lane === 1;
                }) ? 2 : 1, genes: genes, region: row.region};
        };
        let out = new Array(rows.length);
        rows.forEach(function (row, i) {
            if (row && row.anchored) {
                out[i] = place(row);
            }
        });
        let left = isFinite(min) ? min : 0;
        rows.forEach(function (row, i) {
            if (row && !row.anchored) {
                row.origin = row.b0 - left;   // its backbone starts at the left edge
                out[i] = place(row);
            } else if (!row) {
                out[i] = null;
            }
        });
        return {rows: out, extent: isFinite(min) ? {min: min, max: max} : null};
    };

    // One row in pixels: the backbone and each gene's box, given the track's
    // start x, the px per base pair f and the extent's left edge.
    forester.geneRowGeometry = function (row, start, f, min) {
        let px = function (x) {
            return start + ((x - min) * f);
        };
        return {
            backbone: {x: px(row.backbone.x0), w: (row.backbone.x1 - row.backbone.x0) * f},
            capLeft: row.capLeft,
            capRight: row.capRight,
            genes: row.genes.map(function (g) {
                return {x: px(g.x0), w: (g.x1 - g.x0) * f, dir: g.dir, lane: g.lane, gene: g.gene};
            })
        };
    };

    // The arrow: a box whose leading end tapers to a point in the strand's
    // direction, the head half the height long; a gene shorter than the head
    // is a triangle and still shows its direction. Path data to a hundredth
    // of a pixel; '' for a degenerate box.
    forester.geneArrowPath = function (x, y, w, h, dir) {
        if (!isFinite(x) || !isFinite(y) || !(w > 0) || !isFinite(w) || !(h > 0) || !isFinite(h)) {
            return '';
        }
        let num = function (n) {
            return Math.round(n * 100) / 100;
        };
        let head = Math.min(h / 2, w);
        let body = w - head;
        let hh = h / 2;
        if (dir < 0) {
            // the point on the left
            return 'M' + num(x + w) + ',' + num(y)
                + (body > 0 ? 'h' + num(-body) : '')
                + 'l' + num(-head) + ',' + num(hh)
                + 'l' + num(head) + ',' + num(hh)
                + (body > 0 ? 'h' + num(body) : '') + 'Z';
        }
        return 'M' + num(x) + ',' + num(y)
            + (body > 0 ? 'h' + num(body) : '')
            + 'l' + num(head) + ',' + num(hh)
            + 'l' + num(-head) + ',' + num(hh)
            + (body > 0 ? 'h' + num(-body) : '') + 'Z';
    };

    forester.GENE_SINGLETON_COLOR = '#9e9e9e';    // a family met in one region only
    forester.GENE_NO_FAMILY_COLOR = '#cfcfcf';    // a gene with no family at all

    // The families over the regions given (display order): the SHARED ones
    // -- in two regions or more, or, when `shared` (a Set of family refs) is
    // given, the ones in it -- sorted by code unit (the palette's order),
    // the legend rows in first-appearance order with each family's region
    // and gene counts and its commonest product and symbol, and how many
    // drawn genes are singletons or carry no family. The viewer passes the
    // TREE's shared families as `shared` when it summarizes a view, so a
    // family shown once in a subtree keeps the colour it has on the tree.
    forester.geneFamilySummary = function (regions, ns, shared) {
        let fams = new Map();
        let singles = 0;
        let unfamilied = 0;
        regions.forEach(function (r) {
            if (!r) {
                return;
            }
            let here = new Set();
            r.genes.forEach(function (g) {
                let fam = forester.geneFamilyOf(g, ns);
                if (fam === null) {
                    unfamilied++;
                    return;
                }
                let f = fams.get(fam);
                if (!f) {
                    f = {family: fam, tips: 0, genes: 0, products: new Map(), symbols: new Map()};
                    fams.set(fam, f);
                }
                f.genes++;
                if (g.product) {
                    f.products.set(g.product, (f.products.get(g.product) || 0) + 1);
                }
                if (g.symbol) {
                    f.symbols.set(g.symbol, (f.symbols.get(g.symbol) || 0) + 1);
                }
                here.add(fam);
            });
            here.forEach(function (fam) {
                fams.get(fam).tips++;
            });
        });
        let commonest = function (m) {
            let best = '';
            let n = 0;
            m.forEach(function (count, text) {
                if (count > n) {
                    n = count;
                    best = text;
                }
            });
            return best;
        };
        let legend = [];
        let names = [];
        fams.forEach(function (f) {
            if (shared ? !shared.has(f.family) : f.tips < 2) {
                singles += f.genes;
                return;
            }
            names.push(f.family);
            legend.push({family: f.family, tips: f.tips, genes: f.genes,
                product: commonest(f.products), symbol: commonest(f.symbols)});
        });
        names.sort();
        return {names: names, legend: legend, singletons: singles, unfamilied: unfamilied};
    };

    // Where a click on a gene goes: its BV-BRC feature page for a BV-BRC /
    // PATRIC accession (fig|...), NCBI's protein page for a GenBank / RefSeq
    // one; nowhere otherwise.
    const BVBRC_FEATURE = 'https://www.bv-brc.org/view/Feature/';
    const NCBI_PROTEIN = 'https://www.ncbi.nlm.nih.gov/protein/';

    forester.geneReference = function (gene) {
        if (!gene || !gene.accession) {
            return null;
        }
        let v = gene.accession.value.trim();
        let src = gene.accession.source.toLowerCase();
        if (v.length === 0) {
            return null;
        }
        if (/^fig\|/.test(v) || /bv-?brc|patric/.test(src)) {
            return {url: BVBRC_FEATURE + encodeURIComponent(v).replace(/%7C/gi, '|'), site: 'BV-BRC'};
        }
        if (/ncbi|genbank|refseq|uniprot/.test(src)) {
            return {url: NCBI_PROTEIN + encodeURIComponent(v), site: 'NCBI'};
        }
        return null;
    };

    // The track's own scale bar: a round number of base pairs about
    // targetPx long, labelled in kb from 1000 up.
    forester.geneScaleBar = function (pxPerBp, targetPx) {
        let bar = forester.scaleBarLength(pxPerBp, targetPx);
        if (!bar) {
            return null;
        }
        let bp = bar.length;
        return {bp: bp, px: bar.px, label: bp >= 1000 ? (bp / 1000) + ' kb' : bp + ' bp'};
    };

    // A location as the readout and the node-data dialog show it.
    forester.geneLocationText = function (gene) {
        return gene.contig + ':' + gene.start + '-' + gene.end + '(' + gene.strand + ')';
    };

    // --------------------------------------------------------------
    // Scale bar
    // --------------------------------------------------------------
    // A phylogram's scale bar spans a round number of branch-length units
    // -- 1, 2 or 5 times a power of ten -- chosen so the bar comes out about
    // targetPx long at pxPerUnit pixels per unit. Returns {length, label,
    // px}, or null when the scale is unusable (zero, negative or infinite).
    // The label carries the tree's stated unit after the number ("0.05
    // subs/site"), as the distance axis does at its end, and nothing when
    // the file states none: a tree that does not say what its lengths
    // measure gets no guess (a BV-BRC user asked for units, 2026-10-08).
    forester.scaleBarLength = function (pxPerUnit, targetPx, unit) {
        if (!(pxPerUnit > 0) || !isFinite(pxPerUnit)) {
            return null;
        }
        let raw = (targetPx || 100) / pxPerUnit;
        let k = Math.floor(Math.log10(raw));
        let base = raw / Math.pow(10, k);
        let nice = base < 1.5 ? 1 : (base < 3.5 ? 2 : (base < 7.5 ? 5 : 10));
        let length = Number((nice * Math.pow(10, k)).toPrecision(2));
        let u = typeof unit === 'string' ? unit.trim() : '';
        return {length: length, label: String(length) + (u ? ' ' + u : ''), px: length * pxPerUnit};
    };

    // --------------------------------------------------------------
    // Metadata tables
    // --------------------------------------------------------------
    // A table beside the tree -- TSV or CSV with a header row, the first
    // column naming the tip -- joined onto the tips as node properties, so
    // that everything downstream of a property sees the columns as if the
    // file had carried them: the automatic Color-by and Shape candidates,
    // the legends, the search fields, the node-data dialog, the phyloXML
    // writer. Every other browser viewer takes such a table; it was the one
    // input this library lacked (field review, 2026-09-13).

    forester.METADATA_NAMESPACE = 'meta';

    function splitDelimitedLine(line, delimiter) {
        let out = [];
        let cur = '';
        let quoted = false;
        for (let i = 0; i < line.length; ++i) {
            let c = line.charAt(i);
            if (quoted) {
                if (c === '"') {
                    if (line.charAt(i + 1) === '"') {   // a doubled quote is a literal one
                        cur += '"';
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    cur += c;
                }
            } else if (c === '"') {
                quoted = true;
            } else if (c === delimiter) {
                out.push(cur);
                cur = '';
            } else {
                cur += c;
            }
        }
        out.push(cur);
        return out.map(function (s) {
            return s.trim();
        });
    }

    // Splits delimited text into its header and rows. The delimiter is
    // whichever of tab, comma and semicolon occurs most in the header line
    // (tab when none does); fields may be double-quoted; blank lines and
    // lines starting with '#' are skipped; Windows line ends are fine.
    // Header names and cells come back trimmed. Throws when there is no
    // header or fewer than two columns.
    forester.parseDelimitedTable = function (text) {
        let lines = String(text || '').split(/\r?\n/).filter(function (l) {
            return l.trim().length > 0 && l.charAt(0) !== '#';
        });
        if (lines.length === 0) {
            throw new Error('the table is empty');
        }
        let delimiter = '\t';
        let best = -1;
        ['\t', ',', ';'].forEach(function (d) {
            let n = lines[0].split(d).length - 1;
            if (n > best) {
                best = n;
                delimiter = d;
            }
        });
        let columns = splitDelimitedLine(lines[0], delimiter);
        if (columns.length < 2) {
            throw new Error('the table needs a header row with at least two columns: the tip name, then the data');
        }
        let rows = [];
        for (let i = 1; i < lines.length; ++i) {
            rows.push(splitDelimitedLine(lines[i], delimiter));
        }
        return {columns: columns, rows: rows, delimiter: delimiter};
    };

    // The property ref for a column. A header that already reads as a
    // phyloXML ref (ns:local, no whitespace) is kept as it is; any other
    // becomes "meta:" plus the header with its whitespace as '_' -- the
    // display name prettifies that back to spaces, so "Collection Date"
    // stays "Collection Date" in every menu.
    // The tips' data as a table, one row per tip in the order given, header
    // first: what the node menu's "Download Ext. Node Data" writes. The
    // columns and their names are the desktop's (NodeDataExporter.toNodeDataTsv),
    // so the two programs write the same table: name (always), the first
    // taxonomy's scientific name, common name, code, id and rank, the first
    // sequence's name, gene name, symbol, accession and type, the branch
    // length, then one column per property ref, sorted, holding its value and
    // joining repeats of that ref with '; '. A column no tip has a value for
    // is left out. When the tip names cannot key the rows (one blank or
    // repeated), a node_id column comes first, from idOf(tip, index) or the
    // row number. Tabs and line breaks inside a value become spaces. A
    // property keeps its ref as the header, so the table joins back onto a
    // tree with joinMetadataTable. One KNOWN divergence as of 2026-09-17: a
    // ref REPEATED on one node. We join every value with '; ' in document
    // order; the desktop's NodeDataExporter.propertyValue takes ps.get(0) and
    // drops the rest. Both programs wrote that bug independently, so the shape
    // of the API invited it rather than either side being careless. They mean
    // to follow us here (Christian deciding), so this is us ahead, not adrift.
    forester.externalNodeDataTable = function (tips, idOf) {
        tips = tips || [];
        if (tips.length === 0) {
            return {columns: [], rows: []};
        }
        let clean = function (v) {
            return (v === undefined || v === null) ? '' : String(v).replace(/[\t\n\r]/g, ' ');
        };
        let tax = function (n) {
            return (n.taxonomies && n.taxonomies[0]) || {};
        };
        let seq = function (n) {
            return (n.sequences && n.sequences[0]) || {};
        };
        let cols = [];
        let add = function (name, extract, force) {
            let vals = tips.map(function (n, i) {
                return clean(extract(n, i));
            });
            if (force || vals.some(function (v) { return v.length > 0; })) {
                cols.push({name: name, vals: vals});
            }
        };
        let seen = new Set();
        let unique = tips.every(function (n) {
            if (!n.name || seen.has(n.name)) {
                return false;
            }
            seen.add(n.name);
            return true;
        });
        if (!unique) {
            add('node_id', function (n, i) { return idOf ? idOf(n, i) : i + 1; }, true);
        }
        add('name', function (n) { return n.name; }, true);
        add('taxonomy_scientific_name', function (n) { return tax(n).scientific_name; });
        add('taxonomy_common_name', function (n) { return tax(n).common_name; });
        add('taxonomy_code', function (n) { return tax(n).code; });
        add('taxonomy_id', function (n) { return tax(n).id && tax(n).id.value; });
        add('taxonomy_rank', function (n) { return tax(n).rank; });
        add('sequence_name', function (n) { return seq(n).name; });
        add('gene_name', function (n) { return seq(n).gene_name; });
        add('sequence_symbol', function (n) { return seq(n).symbol; });
        add('sequence_accession', function (n) { return seq(n).accession && seq(n).accession.value; });
        add('sequence_type', function (n) { return seq(n).type; });
        add('branch_length', function (n) { return (typeof n.branch_length === 'number') ? n.branch_length : ''; });
        let refs = new Set();
        tips.forEach(function (n) {
            (n.properties || []).forEach(function (p) {
                if (p.ref) {
                    refs.add(p.ref);
                }
            });
        });
        Array.from(refs).sort().forEach(function (ref) {
            add(ref, function (n) {
                // Every value, not just the first: phyloXML lets a ref repeat
                // on a node, and until 2026-09-17 this kept only [0], so a tip
                // carrying meta:zeta twice exported one of them and lost the
                // other with nothing to show it had happened. Joined in
                // document order, which is the order the reader and the writer
                // both preserve.
                return (n.properties || []).filter(function (q) {
                    return q.ref === ref;
                }).map(function (q) {
                    return q.value;
                }).join('; ');
            });
        });
        return {
            columns: cols.map(function (c) { return c.name; }),
            rows: tips.map(function (n, i) {
                return cols.map(function (c) { return c.vals[i]; });
            })
        };
    };

    // externalNodeDataTable as tab-separated text, header line first; empty
    // for no tips.
    forester.externalNodeDataTsv = function (tips, idOf) {
        let t = forester.externalNodeDataTable(tips, idOf);
        if (t.columns.length === 0) {
            return '';
        }
        return [t.columns].concat(t.rows).map(function (r) {
            return r.join('\t');
        }).join('\n') + '\n';
    };

    forester.metadataColumnRef = function (header, index) {
        let h = String(header || '').trim();
        if (h.length === 0) {
            h = 'column_' + (index + 1);
        }
        if (/^[A-Za-z0-9_]+:\S+$/.test(h)) {
            return h;
        }
        return forester.METADATA_NAMESPACE + ':' + h.replace(/\s+/g, '_');
    };

    // Joins a table onto the tree's tips. The first column is the key,
    // matched to the tip's name exactly, then case-insensitively. Every
    // other column becomes one property per matched tip with a non-empty
    // cell (applies_to node; the datatype is xsd:integer or xsd:double when
    // every filled cell of the column is such a number, xsd:string
    // otherwise). A property the tip already carries under the same ref is
    // replaced -- the table wins. Returns what happened:
    //   {columns: [{header, ref, datatype, filled}], tips, matchedTips,
    //    unmatchedTips: [names], unmatchedRows: [keys], properties}
    forester.joinMetadataTable = function (tree, text) {
        let table = forester.parseDelimitedTable(text);
        let tips = forester.getAllExternalNodes(tree);
        let byName = Object.create(null);
        let byLower = Object.create(null);
        tips.forEach(function (n) {
            if (n.name) {
                byName[n.name] = n;
                let lower = n.name.toLowerCase();
                if (!byLower[lower]) {
                    byLower[lower] = n;
                }
            }
        });
        let columns = [];
        for (let j = 1; j < table.columns.length; ++j) {
            let filled = table.rows.map(function (r) {
                return r[j] === undefined ? '' : r[j];
            }).filter(function (v) {
                return v.length > 0;
            });
            let datatype = 'xsd:string';
            if (filled.length > 0) {
                if (filled.every(function (v) { return /^[+-]?\d+$/.test(v); })) {
                    datatype = 'xsd:integer';
                } else if (filled.every(function (v) { return VIS_NUMERIC_RE.test(v); })) {
                    datatype = 'xsd:double';
                }
            }
            columns.push({header: table.columns[j], ref: forester.metadataColumnRef(table.columns[j], j),
                datatype: datatype, filled: 0});
        }
        let matched = new Set();
        let unmatchedRows = [];
        let properties = 0;
        table.rows.forEach(function (r) {
            let key = r[0] === undefined ? '' : r[0];
            if (key.length === 0) {
                return;
            }
            let tip = byName[key] || byLower[key.toLowerCase()];
            if (!tip) {
                unmatchedRows.push(key);
                return;
            }
            matched.add(tip);
            columns.forEach(function (col, k) {
                let v = r[k + 1] === undefined ? '' : r[k + 1];
                if (v.length === 0) {
                    return;
                }
                if (!tip.properties) {
                    tip.properties = [];
                }
                let existing = null;
                for (let i = 0; i < tip.properties.length; ++i) {
                    if (tip.properties[i].ref === col.ref) {
                        existing = tip.properties[i];
                        break;
                    }
                }
                if (existing) {
                    existing.value = v;
                    existing.datatype = col.datatype;
                    existing.applies_to = 'node';
                } else {
                    tip.properties.push({ref: col.ref, value: v, datatype: col.datatype, applies_to: 'node'});
                }
                col.filled++;
                properties++;
            });
        });
        let unmatchedTips = tips.filter(function (n) {
            return !matched.has(n);
        }).map(function (n) {
            return n.name || '';
        });
        return {columns: columns, tips: tips.length, matchedTips: matched.size,
            unmatchedTips: unmatchedTips, unmatchedRows: unmatchedRows, properties: properties};
    };

    // --------------------------------------------------------------
    // Search engine
    // --------------------------------------------------------------
    // Field-and-mode search over a phylogeny (mirrors the desktop Archaeopteryx
    // redesign). A search is described by a spec:
    //   { field, mode, value, value2, caseSensitive, inverse }
    // where field comes from forester.availableSearchFields(root), mode is one of
    // the string modes ('contains', 'starts_with', 'ends_with', 'whole_word',
    // 'regex') or numeric modes ('eq', 'ne', 'lt', 'le', 'gt', 'ge', 'range'),
    // and ',' = OR / '+' = AND inside a text value. Used by archaeopteryx.js;
    // pure tree logic, no DOM -- tested by test/search_test.js.

    const SEARCH_NUMERIC_DATATYPES = new Set(['decimal', 'double', 'float', 'integer', 'int', 'long', 'short',
        'byte', 'unsignedint', 'unsignedlong', 'unsignedshort', 'unsignedbyte', 'nonnegativeinteger',
        'nonpositiveinteger', 'negativeinteger', 'positiveinteger']);

    function searchTaxa(n) { return (n.taxonomies && n.taxonomies.length) ? n.taxonomies : []; }
    function searchSeqs(n) { return (n.sequences && n.sequences.length) ? n.sequences : []; }

    // A search field is an object -- {label, numeric, extract(node, root)}
    // and a few flags -- and nothing more: the field menu lists them, a spec
    // carries one, the engine calls its extract. There are no field ids.
    // Until 2026-09-12 every field was named by a two-letter code (NN, TS,
    // SA, ...): the suffixes of the 2.x search syntax ("foo:NN"). 3.0.0
    // replaced that syntax with the field menu, and the alphabet outlived it
    // as ids until Christian spotted it. A field is multi-valued; any value
    // matching is a match; root is needed only by Node Type.
    //
    // Flags: `anyText` -- "Any Text" ORs the field in (the molecular
    // sequence and the domains stay out, as on the desktop); `always` -- in
    // the menu even when no node carries it; `suggest: false` -- the value
    // box does not offer its values as type-ahead (near-unique, huge);
    // `metrics` -- needs the per-node depth / distance / clade size first.
    function textField(label, extract, flags) {
        return Object.assign({label: label, numeric: false, extract: extract}, flags || {});
    }
    function numericField(label, extract, flags) {
        return Object.assign({label: label, numeric: true, extract: extract}, flags || {});
    }

    // The text fields of a node; the labels are the desktop's, verbatim.
    const SEARCH_NODE_NAME = textField('Node Name', n => (n.name ? [n.name] : []), {always: true, anyText: true});
    const SEARCH_TAXONOMY_SCIENTIFIC_NAME = textField('Taxonomy Scientific', n => searchTaxa(n).map(t => t.scientific_name).filter(Boolean), {anyText: true});
    const SEARCH_TAXONOMY_COMMON_NAME = textField('Taxonomy Common', n => searchTaxa(n).map(t => t.common_name).filter(Boolean), {anyText: true});
    const SEARCH_TAXONOMY_CODE = textField('Taxonomy Code', n => searchTaxa(n).map(t => t.code).filter(Boolean), {anyText: true});
    const SEARCH_TAXONOMY_ID = textField('Taxonomy Identifier', n => searchTaxa(n).map(t => t.id && t.id.value).filter(Boolean), {anyText: true});
    const SEARCH_TAXONOMY_SYNONYM = textField('Taxonomy Synonym', n => searchTaxa(n).reduce((a, t) => a.concat(t.synonyms || []), []).filter(Boolean), {anyText: true});
    const SEARCH_TAXONOMY_LINEAGE = textField('Taxonomy Lineage', n => searchTaxa(n).reduce((a, t) => a.concat(t.lineage || []), []).filter(Boolean), {anyText: true});
    const SEARCH_SEQUENCE_NAME = textField('Seq Name', n => searchSeqs(n).map(s => s.name).filter(Boolean), {anyText: true});
    const SEARCH_GENE_NAME = textField('Gene Name', n => searchSeqs(n).map(s => s.gene_name).filter(Boolean), {anyText: true});
    // phyloXML <sequence><symbol>: the gene symbol
    const SEARCH_SEQUENCE_SYMBOL = textField('Gene Symbol', n => searchSeqs(n).map(s => s.symbol).filter(Boolean), {anyText: true});
    const SEARCH_SEQUENCE_ACCESSION = textField('Seq Accession', n => searchSeqs(n).map(s => s.accession && s.accession.value).filter(Boolean), {anyText: true});
    const SEARCH_DOMAIN = textField('Domain', n => searchSeqs(n).reduce((a, s) => a.concat((s.domain_architecture && s.domain_architecture.domains) ? s.domain_architecture.domains.map(d => d.name) : []), []).filter(Boolean));
    const SEARCH_ANNOTATION = textField('Annotation', n => searchSeqs(n).reduce((a, s) => a.concat((s.annotations || []).reduce((b, an) => b.concat([an.desc, an.ref]), [])), []).filter(Boolean), {anyText: true});
    const SEARCH_CROSS_REFERENCE = textField('Cross-Reference', n => searchSeqs(n).reduce((a, s) => a.concat((s.cross_references || []).reduce((b, x) => b.concat([x.value, x.source, x.comment]), [])), []).filter(Boolean), {anyText: true});
    // The residues: the phyloXML and Nexus readers give mol_seq as {value,
    // is_aligned}, and a hand-built tree may carry the plain string. Reading
    // the object itself compared every query against "[object Object]", so
    // this field never matched a real file (its test built the string form).
    const SEARCH_MOLECULAR_SEQUENCE = textField('Molecular Sequence', n => searchSeqs(n).map(function (s) {
        return (s.mol_seq && typeof s.mol_seq === 'object') ? s.mol_seq.value : s.mol_seq;
    }).filter(Boolean), {suggest: false});
    // in menu order
    const SEARCH_TEXT_FIELDS = [
        SEARCH_NODE_NAME, SEARCH_TAXONOMY_SCIENTIFIC_NAME, SEARCH_TAXONOMY_COMMON_NAME, SEARCH_TAXONOMY_CODE,
        SEARCH_TAXONOMY_ID, SEARCH_TAXONOMY_SYNONYM, SEARCH_TAXONOMY_LINEAGE, SEARCH_SEQUENCE_NAME, SEARCH_GENE_NAME,
        SEARCH_SEQUENCE_SYMBOL, SEARCH_SEQUENCE_ACCESSION, SEARCH_DOMAIN, SEARCH_ANNOTATION, SEARCH_CROSS_REFERENCE,
        SEARCH_MOLECULAR_SEQUENCE
    ];

    // "Any Text": every anyText field above plus every custom property.
    const SEARCH_ANY_TEXT = textField('Any Text', function (node) {
        let out = [];
        SEARCH_TEXT_FIELDS.forEach(function (f) {
            if (f.anyText) out = out.concat(f.extract(node));
        });
        if (node.properties) {
            for (let i = 0; i < node.properties.length; ++i) {
                let p = node.properties[i];
                if (!isInternalPropRef(p.ref) && p.value !== null && p.value !== undefined && p.value !== '') out.push(p.value);
            }
        }
        return out;
    }, {suggest: false});

    const SEARCH_BRANCH_LENGTH = numericField('Branch Length', n => (typeof n.branch_length === 'number') ? [n.branch_length] : []);
    // support only: a MAD value rates a root position, not the clade
    const SEARCH_CONFIDENCE = numericField('Confidence', n => n.confidences ? n.confidences.filter(c => c.type !== forester.MAD_CONFIDENCE_TYPE).map(c => c.value).filter(v => typeof v === 'number') : []);
    const SEARCH_CLADE_SIZE = numericField('Clade Size (tips)', n => [n._srchClade], {metrics: true});
    const SEARCH_CHILD_COUNT = numericField('Number of Children', n => [n.children ? n.children.length : 0]);
    const SEARCH_DEPTH = numericField('Depth from Root', n => [n._srchDepth], {metrics: true});
    const SEARCH_DISTANCE_FROM_ROOT = numericField('Distance from Root', n => [n._srchDist], {metrics: true});
    const SEARCH_NODE_TYPE = textField('Node Type', function (node, root) {
        let kids = node.children;
        let isLeaf = !kids || kids.length === 0;
        return [isLeaf ? 'leaf' : (node === root ? 'root' : 'internal')];
    });

    // The built-in fields by name, for a caller that asks "does this tree
    // carry X?" -- the viewer's label presets test whether a field object is
    // among availableSearchFields(tree), by identity. Property fields have
    // no entry here: there is one per ref, made per tree.
    forester.searchFields = {
        anyText: SEARCH_ANY_TEXT,
        nodeName: SEARCH_NODE_NAME,
        taxonomyScientificName: SEARCH_TAXONOMY_SCIENTIFIC_NAME,
        taxonomyCommonName: SEARCH_TAXONOMY_COMMON_NAME,
        taxonomyCode: SEARCH_TAXONOMY_CODE,
        taxonomyId: SEARCH_TAXONOMY_ID,
        taxonomySynonym: SEARCH_TAXONOMY_SYNONYM,
        taxonomyLineage: SEARCH_TAXONOMY_LINEAGE,
        sequenceName: SEARCH_SEQUENCE_NAME,
        geneName: SEARCH_GENE_NAME,
        sequenceSymbol: SEARCH_SEQUENCE_SYMBOL,
        sequenceAccession: SEARCH_SEQUENCE_ACCESSION,
        domain: SEARCH_DOMAIN,
        annotation: SEARCH_ANNOTATION,
        crossReference: SEARCH_CROSS_REFERENCE,
        molecularSequence: SEARCH_MOLECULAR_SEQUENCE,
        branchLength: SEARCH_BRANCH_LENGTH,
        confidence: SEARCH_CONFIDENCE,
        cladeSize: SEARCH_CLADE_SIZE,
        childCount: SEARCH_CHILD_COUNT,
        depth: SEARCH_DEPTH,
        distanceFromRoot: SEARCH_DISTANCE_FROM_ROOT,
        nodeType: SEARCH_NODE_TYPE
    };

    // One field per custom property ref; numeric when its declared datatype
    // or, failing that, every one of its values says so.
    function propertyField(ref, numeric) {
        return {label: ref, numeric: numeric, extract: function (node) {
            let out = [];
            if (node.properties) {
                for (let i = 0; i < node.properties.length; ++i) {
                    let p = node.properties[i];
                    if (p.ref === ref && p.value !== null && p.value !== undefined && p.value !== '') out.push(p.value);
                }
            }
            return out;
        }};
    }

    function isInternalPropRef(ref) { return !ref || ref.indexOf('aptx:') === 0; }

    function datatypeIsNumeric(dt) {
        if (!dt) return false;
        let local = String(dt).toLowerCase();
        let c = local.lastIndexOf(':');
        if (c >= 0) local = local.substring(c + 1);
        return SEARCH_NUMERIC_DATATYPES.has(local);
    }

    function escapeSearchRegExp(str) {
        return String(str).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    // Accept comma as decimal separator when unambiguous (one comma, no period,
    // and not the US thousands pattern comma+exactly-3-digits). Returns null if
    // not a finite number.
    forester.parseFiniteDouble = function (s) {
        if (s === null || s === undefined) return null;
        s = String(s).trim();
        if (s.length === 0) return null;
        if (s.indexOf('.') < 0 && (s.split(',').length - 1) === 1 && !/,\d{3}$/.test(s)) {
            s = s.replace(',', '.');
        }
        let n = Number(s);
        return isFinite(n) ? n : null;
    };

    // Build a predicate value -> bool for one text term. Returns null for an
    // invalid regex (caller treats that as "never matches").
    forester.makeSearchStringTest = function (term, mode, caseSensitive) {
        if (mode === 'regex' || mode === 'whole_word') {
            let src = (mode === 'whole_word')
                ? ('(?<![\\p{L}\\p{N}])' + escapeSearchRegExp(term) + '(?![\\p{L}\\p{N}])')
                : term;
            let re;
            try { re = new RegExp(src, caseSensitive ? 'u' : 'iu'); }
            catch { return null; }
            return s => (s !== null && s !== undefined && re.test(String(s)));
        }
        let t = caseSensitive ? term : term.toLowerCase();
        return function (s) {
            if (s === null || s === undefined) return false;
            let str = caseSensitive ? String(s) : String(s).toLowerCase();
            if (mode === 'starts_with') return str.indexOf(t) === 0;
            if (mode === 'ends_with') return str.length >= t.length && str.lastIndexOf(t) === str.length - t.length;
            return str.indexOf(t) >= 0; // contains
        };
    };

    function numMatches(x, mode, a, lo, hi) {
        switch (mode) {
            case 'eq': return Math.abs(x - a) <= 1e-9 * Math.max(1, Math.abs(a));
            case 'ne': return Math.abs(x - a) > 1e-9 * Math.max(1, Math.abs(a));
            case 'lt': return x < a;
            case 'le': return x <= a;
            case 'gt': return x > a;
            case 'ge': return x >= a;
            case 'range': return x >= lo && x <= hi;
            default: return false;
        }
    }

    // The list of fields the given tree actually offers (drives the Field
    // dropdowns). Always exposes Any Text + Node Name; adds the text, numeric
    // and custom-property fields that are present, then structure fields.
    forester.availableSearchFields = function (root) {
        let fields = [SEARCH_ANY_TEXT];
        if (!root) {
            SEARCH_TEXT_FIELDS.forEach(function (f) { if (f.always) fields.push(f); });
            return fields;
        }

        let present = new Set();
        let hasBL = false, hasConf = false;
        let propRefs = {}; // ref -> { num, tot, dtNum, dtStr }
        forester.preOrderTraversalAll(root, function (n) {
            SEARCH_TEXT_FIELDS.forEach(function (f) {
                if (!f.always && !present.has(f) && f.extract(n).length > 0) present.add(f);
            });
            if (!hasBL && typeof n.branch_length === 'number' && n.branch_length >= 0) hasBL = true;
            if (!hasConf && n.confidences) {
                for (let i = 0; i < n.confidences.length; ++i) {
                    if (typeof n.confidences[i].value === 'number' && n.confidences[i].type !== forester.MAD_CONFIDENCE_TYPE) { hasConf = true; break; }
                }
            }
            if (n.properties) {
                for (let i = 0; i < n.properties.length; ++i) {
                    let p = n.properties[i];
                    if (isInternalPropRef(p.ref)) continue;
                    let r = propRefs[p.ref] || (propRefs[p.ref] = { num: 0, tot: 0, dtNum: false, dtStr: false });
                    r.tot++;
                    if (forester.parseFiniteDouble(p.value) !== null) r.num++;
                    if (p.datatype) { if (datatypeIsNumeric(p.datatype)) r.dtNum = true; else r.dtStr = true; }
                }
            }
        });

        SEARCH_TEXT_FIELDS.forEach(function (f) {
            if (f.always || present.has(f)) fields.push(f);
        });
        if (hasBL) fields.push(SEARCH_BRANCH_LENGTH);
        if (hasConf) fields.push(SEARCH_CONFIDENCE);
        let refs = Object.keys(propRefs).sort();
        for (let i = 0; i < refs.length; ++i) {
            let r = propRefs[refs[i]];
            let numeric = r.dtStr ? false : (r.dtNum ? true : (r.tot > 0 && r.num === r.tot));
            fields.push(propertyField(refs[i], numeric));
        }
        fields.push(SEARCH_CLADE_SIZE, SEARCH_CHILD_COUNT, SEARCH_DEPTH);
        if (hasBL) fields.push(SEARCH_DISTANCE_FROM_ROOT);
        fields.push(SEARCH_NODE_TYPE);
        return fields;
    };

    // Per-node depth / distance-to-root / clade size, computed on demand for the
    // structure search fields (cheap O(n), avoids staleness after tree edits).
    function computeSearchMetrics(root) {
        (function pre(n, depth, dist) {
            n._srchDepth = depth;
            let d = dist + (typeof n.branch_length === 'number' && n.branch_length > 0 ? n.branch_length : 0);
            n._srchDist = d;
            let kids = n.children;
            if (kids) for (let i = 0; i < kids.length; ++i) pre(kids[i], depth + 1, d);
        })(root, 0, 0);
        forester.postOrderTraversalAll(root, function (n) {
            let kids = n.children;
            if (!kids || kids.length === 0) { n._srchClade = 1; return; }
            let s = 0;
            for (let i = 0; i < kids.length; ++i) s += kids[i]._srchClade;
            n._srchClade = s;
        });
    }

    // Run one search spec { field, mode, value, value2, caseSensitive, inverse }
    // over the tree and return the Set of matching nodes.
    // A parsed tree is anchored on a SUPER-ROOT: a synthetic node whose single
    // child is the tree's actual root. It is not a node of the phylogeny -- it
    // exists to give the root a parent slot, which is what lets reRoot() move
    // the root around and what toNewHampshire() writes from. Both formats have
    // one; from phyloXML it also carries the <phylogeny> element's own name and
    // description, so it looks like a node with the TREE's name on it.
    //
    // Anything that reasons about the phylogeny's own nodes has to step over it.
    // This is the test getTreeRoot uses, minus that function's walk UP the tree,
    // which would escape a subtree a caller had deliberately scoped to.
    function realRootOf(root) {
        if (!root.parent && root.children && root.children.length === 1) {
            return root.children[0];
        }
        return root;
    }

    forester.searchWithSpec = function (root, spec) {
        let result = new Set();
        if (!root || !spec || !spec.field) return result;
        // Metrics stay relative to what the caller passed, so depth and distance
        // values are unchanged; only the set of nodes considered is narrowed.
        let nodes = realRootOf(root);
        let field = spec.field;
        if (field.metrics) computeSearchMetrics(root);

        let v = (spec.value === null || spec.value === undefined) ? '' : String(spec.value);
        v = v.replace(/\s+/g, ' ').trim();

        let test = null;
        if (field.numeric) {
            let a = forester.parseFiniteDouble(v);
            let b = (spec.mode === 'range') ? forester.parseFiniteDouble(spec.value2) : null;
            if (a === null || (spec.mode === 'range' && b === null)) return result; // invalid -> reset
            let lo = (b !== null) ? Math.min(a, b) : a;
            let hi = (b !== null) ? Math.max(a, b) : a;
            test = function (n) {
                let vals = field.extract(n, root);
                for (let i = 0; i < vals.length; ++i) {
                    let x = (typeof vals[i] === 'number') ? vals[i] : forester.parseFiniteDouble(vals[i]);
                    if (x !== null && numMatches(x, spec.mode, a, lo, hi)) return true;
                }
                return false;
            };
        } else {
            if (v.length < 1) return result;
            let splittable = spec.mode !== 'regex';
            let orTerms = (splittable && v.indexOf(',') >= 0) ? v.split(/,+/) : [v];
            let compiled = [];
            for (let oi = 0; oi < orTerms.length; ++oi) {
                let ot = orTerms[oi].trim();
                if (!ot) continue;
                let ands = (splittable && ot.indexOf('+') > 0) ? ot.split(/\++/) : [ot];
                let tests = [];
                let bad = false;
                for (let ai = 0; ai < ands.length; ++ai) {
                    let term = ands[ai].trim();
                    if (!term) continue;
                    let t = forester.makeSearchStringTest(term, spec.mode, spec.caseSensitive);
                    if (t === null) { bad = true; break; } // invalid regex
                    tests.push(t);
                }
                if (!bad && tests.length) compiled.push(tests);
            }
            if (!compiled.length) return result;
            test = function (n) {
                let vals = field.extract(n, root);
                for (let oi = 0; oi < compiled.length; ++oi) {
                    let ands = compiled[oi], ok = true;
                    for (let ai = 0; ai < ands.length; ++ai) {
                        let hit = false;
                        for (let vi = 0; vi < vals.length; ++vi) { if (ands[ai](vals[vi])) { hit = true; break; } }
                        if (!hit) { ok = false; break; }
                    }
                    if (ok) return true;
                }
                return false;
            };
        }

        forester.preOrderTraversalAll(nodes, function (n) { if (test(n)) result.add(n); });

        if (spec.inverse) {
            // Complement, scoped to nodes that actually carry this field.
            let inv = new Set();
            forester.preOrderTraversalAll(nodes, function (n) {
                if (!result.has(n) && field.extract(n, root).length > 0) inv.add(n);
            });
            return inv;
        }
        return result;
    };

    // Distinct, trimmed, sorted values of a specific text field across the tree,
    // for the value-box autocomplete. Empty for a numeric field and for the
    // fields flagged suggest: false (Any Text; the molecular sequence, which
    // is near-unique and huge). cap limits the list length (optional).
    forester.distinctSearchValues = function (root, field, cap) {
        if (!root || !field || field.numeric || field.suggest === false) return [];
        let set = new Set();
        forester.preOrderTraversalAll(root, function (n) {
            let vals = field.extract(n, root);
            for (let i = 0; i < vals.length; ++i) {
                if (vals[i] !== null && vals[i] !== undefined) {
                    let v = String(vals[i]).trim();
                    if (v.length > 0) set.add(v);
                }
            }
        });
        let arr = Array.from(set).sort(function (a, b) { return a.localeCompare(b); });
        if (cap && arr.length > cap) arr = arr.slice(0, cap);
        return arr;
    };


    // --------------------------------------------------------------
    // Geologic time scale + time-tree detection (the desktop's time axes)
    // --------------------------------------------------------------
    // The ICS International Chronostratigraphic Chart as the desktop embeds
    // it (GeologicTimeScale.java): {name, young, old, color}, ages in Ma,
    // colours the official ICS ones (kept even in monochrome exports -- the
    // timescale IS a colour key). Reference: Cohen, K.M., Harper, D.A.T.,
    // Gibbard, P.L. & Car, N. (2025, updated), The ICS International
    // Chronostratigraphic Chart this decade, Episodes 48: 105-115;
    // International Commission on Stratigraphy, www.stratigraphy.org.

    const GEO_SCALE = {
        eon: [
            {name: 'Phanerozoic', young: 0, old: 538.8, color: '#9AD9DD'},
            {name: 'Proterozoic', young: 538.8, old: 2500, color: '#FF70B8'},
            {name: 'Archean', young: 2500, old: 4031, color: '#FF3399'}
        ],
        era: [
            {name: 'Cenozoic', young: 0, old: 66, color: '#F2F91D'},
            {name: 'Mesozoic', young: 66, old: 251.902, color: '#67C5CA'},
            {name: 'Paleozoic', young: 251.902, old: 538.8, color: '#99C08D'},
            {name: 'Neoproterozoic', young: 538.8, old: 1000, color: '#FF9BCD'},
            {name: 'Mesoproterozoic', young: 1000, old: 1600, color: '#FF7EBF'},
            {name: 'Paleoproterozoic', young: 1600, old: 2500, color: '#E665A6'},
            {name: 'Neoarchean', young: 2500, old: 2800, color: '#FF5CAD'},
            {name: 'Mesoarchean', young: 2800, old: 3200, color: '#E62E8A'},
            {name: 'Paleoarchean', young: 3200, old: 3600, color: '#CC297A'},
            {name: 'Eoarchean', young: 3600, old: 4031, color: '#B2246B'}
        ],
        period: [
            {name: 'Quaternary', young: 0, old: 2.58, color: '#F9F97F'},
            {name: 'Neogene', young: 2.58, old: 23.04, color: '#FFE619'},
            {name: 'Paleogene', young: 23.04, old: 66, color: '#FD9A52'},
            {name: 'Cretaceous', young: 66, old: 143.1, color: '#7FC64E'},
            {name: 'Jurassic', young: 143.1, old: 201.4, color: '#34B2C9'},
            {name: 'Triassic', young: 201.4, old: 251.902, color: '#812B92'},
            {name: 'Permian', young: 251.902, old: 298.9, color: '#F04028'},
            {name: 'Carboniferous', young: 298.9, old: 358.86, color: '#67A599'},
            {name: 'Devonian', young: 358.86, old: 419.62, color: '#CB8C37'},
            {name: 'Silurian', young: 419.62, old: 443.1, color: '#B3E1B6'},
            {name: 'Ordovician', young: 443.1, old: 486.85, color: '#009270'},
            {name: 'Cambrian', young: 486.85, old: 538.8, color: '#7FA056'},
            {name: 'Ediacaran', young: 538.8, old: 635, color: '#FFC3E1'},
            {name: 'Cryogenian', young: 635, old: 720, color: '#FFAFD7'},
            {name: 'Tonian', young: 720, old: 1000, color: '#FFA5D2'},
            {name: 'Stenian', young: 1000, old: 1200, color: '#FFA5D2'},
            {name: 'Ectasian', young: 1200, old: 1400, color: '#FF98CC'},
            {name: 'Calymmian', young: 1400, old: 1600, color: '#FF8BC5'},
            {name: 'Statherian', young: 1600, old: 1800, color: '#EE93C1'},
            {name: 'Orosirian', young: 1800, old: 2050, color: '#E874AF'},
            {name: 'Rhyacian', young: 2050, old: 2300, color: '#EB84B8'},
            {name: 'Siderian', young: 2300, old: 2500, color: '#E874AF'}
        ],
        epoch: [
            {name: 'Holocene', young: 0, old: 0.0117, color: '#FEF2E0'},
            {name: 'Pleistocene', young: 0.0117, old: 2.58, color: '#FFF2AE'},
            {name: 'Pliocene', young: 2.58, old: 5.333, color: '#FFFF99'},
            {name: 'Miocene', young: 5.333, old: 23.04, color: '#FFFF00'},
            {name: 'Oligocene', young: 23.04, old: 33.9, color: '#FDC07A'},
            {name: 'Eocene', young: 33.9, old: 56, color: '#FDB46C'},
            {name: 'Paleocene', young: 56, old: 66, color: '#FDA75F'},
            {name: 'Late Cretaceous', young: 66, old: 100.5, color: '#A6D84A'},
            {name: 'Early Cretaceous', young: 100.5, old: 143.1, color: '#8CCD57'},
            {name: 'Late Jurassic', young: 143.1, old: 161.5, color: '#B3E3EE'},
            {name: 'Middle Jurassic', young: 161.5, old: 174.7, color: '#80CFD8'},
            {name: 'Early Jurassic', young: 174.7, old: 201.4, color: '#42AED0'},
            {name: 'Late Triassic', young: 201.4, old: 237, color: '#BD8CC3'},
            {name: 'Middle Triassic', young: 237, old: 246.7, color: '#B168B1'},
            {name: 'Early Triassic', young: 246.7, old: 251.902, color: '#983999'},
            {name: 'Lopingian', young: 251.902, old: 259.51, color: '#FBA794'},
            {name: 'Guadalupian', young: 259.51, old: 274.4, color: '#FB745C'},
            {name: 'Cisuralian', young: 274.4, old: 298.9, color: '#EF5845'},
            {name: 'Pennsylvanian', young: 298.9, old: 323.4, color: '#99C2B5'},
            {name: 'Mississippian', young: 323.4, old: 358.86, color: '#678F66'},
            {name: 'Late Devonian', young: 358.86, old: 382.31, color: '#F1E19D'},
            {name: 'Middle Devonian', young: 382.31, old: 393.47, color: '#F1C868'},
            {name: 'Early Devonian', young: 393.47, old: 419.62, color: '#E5AC4D'},
            {name: 'Pridoli', young: 419.62, old: 422.7, color: '#E6F5E1'},
            {name: 'Ludlow', young: 422.7, old: 426.7, color: '#BFE6CF'},
            {name: 'Wenlock', young: 426.7, old: 432.9, color: '#B3E1C2'},
            {name: 'Llandovery', young: 432.9, old: 443.1, color: '#99D7B3'},
            {name: 'Late Ordovician', young: 443.1, old: 458.2, color: '#7FCA93'},
            {name: 'Middle Ordovician', young: 458.2, old: 471.3, color: '#4DB47E'},
            {name: 'Early Ordovician', young: 471.3, old: 486.85, color: '#1A9D6F'},
            {name: 'Furongian', young: 486.85, old: 497, color: '#B3E095'},
            {name: 'Miaolingian', young: 497, old: 506.5, color: '#A6CF86'},
            {name: 'Series 2', young: 506.5, old: 521, color: '#99C078'},
            {name: 'Terreneuvian', young: 521, old: 538.8, color: '#8CB06C'}
        ],
        // The 101 ratified Phanerozoic stages, plus the Pridoli: a Series with
        // no stages of its own, standing in the stage row for its own span
        // (419.62-422.7 Ma) as on the printed ICS chart -- without it the row
        // would have a hole there. Same source as every rank above
        // (Macrostrat international timescale, id 11), which the ranks above
        // match byte for byte; the desktop generated its table from the same
        // query, so the two programs band from identical data.
        age: [
            {name: 'Meghalayan', young: 0, old: 0.0042, color: '#FEF2E0'},
            {name: 'Northgrippian', young: 0.0042, old: 0.0082, color: '#FEF2E0'},
            {name: 'Greenlandian', young: 0.0082, old: 0.0117, color: '#FEF2E0'},
            {name: 'Late Pleistocene', young: 0.0117, old: 0.129, color: '#FFF2C7'},
            {name: 'Chibanian', young: 0.129, old: 0.774, color: '#FFF2C7'},
            {name: 'Calabrian', young: 0.774, old: 1.8, color: '#FFF2C7'},
            {name: 'Gelasian', young: 1.8, old: 2.58, color: '#FFEDB3'},
            {name: 'Piacenzian', young: 2.58, old: 3.6, color: '#FFFFBF'},
            {name: 'Zanclean', young: 3.6, old: 5.333, color: '#FFFFB3'},
            {name: 'Messinian', young: 5.333, old: 7.246, color: '#FFFF73'},
            {name: 'Tortonian', young: 7.246, old: 11.63, color: '#FFFF66'},
            {name: 'Serravallian', young: 11.63, old: 13.82, color: '#FFFF59'},
            {name: 'Langhian', young: 13.82, old: 15.98, color: '#FFFF4D'},
            {name: 'Burdigalian', young: 15.98, old: 20.45, color: '#FFFF41'},
            {name: 'Aquitanian', young: 20.45, old: 23.04, color: '#FFFF33'},
            {name: 'Chattian', young: 23.04, old: 27.3, color: '#FEE6AA'},
            {name: 'Rupelian', young: 27.3, old: 33.9, color: '#FED99A'},
            {name: 'Priabonian', young: 33.9, old: 37.71, color: '#FDCDA1'},
            {name: 'Bartonian', young: 37.71, old: 41.03, color: '#FDC091'},
            {name: 'Lutetian', young: 41.03, old: 48.07, color: '#FCB482'},
            {name: 'Ypresian', young: 48.07, old: 56, color: '#FCA773'},
            {name: 'Thanetian', young: 56, old: 59.24, color: '#FDBF6F'},
            {name: 'Selandian', young: 59.24, old: 61.66, color: '#FEBF65'},
            {name: 'Danian', young: 61.66, old: 66, color: '#FDB462'},
            {name: 'Maastrichtian', young: 66, old: 72.2, color: '#F2FA8C'},
            {name: 'Campanian', young: 72.2, old: 83.6, color: '#E6F47F'},
            {name: 'Santonian', young: 83.6, old: 85.7, color: '#D9EF74'},
            {name: 'Coniacian', young: 85.7, old: 89.8, color: '#CCE968'},
            {name: 'Turonian', young: 89.8, old: 93.9, color: '#BFE35D'},
            {name: 'Cenomanian', young: 93.9, old: 100.5, color: '#B3DE53'},
            {name: 'Albian', young: 100.5, old: 113.2, color: '#CCEA97'},
            {name: 'Aptian', young: 113.2, old: 121.4, color: '#BFE48A'},
            {name: 'Barremian', young: 121.4, old: 125.77, color: '#B3DF7F'},
            {name: 'Hauterivian', young: 125.77, old: 132.6, color: '#A6D975'},
            {name: 'Valanginian', young: 132.6, old: 137.05, color: '#99D36A'},
            {name: 'Berriasian', young: 137.05, old: 143.1, color: '#8CCD60'},
            {name: 'Tithonian', young: 143.1, old: 149.2, color: '#D9F1F7'},
            {name: 'Kimmeridgian', young: 149.2, old: 154.8, color: '#CCECF4'},
            {name: 'Oxfordian', young: 154.8, old: 161.5, color: '#BFE7F1'},
            {name: 'Callovian', young: 161.5, old: 165.3, color: '#BFE7E5'},
            {name: 'Bathonian', young: 165.3, old: 168.2, color: '#B3E2E3'},
            {name: 'Bajocian', young: 168.2, old: 170.9, color: '#A6DDE0'},
            {name: 'Aalenian', young: 170.9, old: 174.7, color: '#9AD9DD'},
            {name: 'Toarcian', young: 174.7, old: 184.2, color: '#99CEE3'},
            {name: 'Pliensbachian', young: 184.2, old: 192.9, color: '#80C5DD'},
            {name: 'Sinemurian', young: 192.9, old: 199.5, color: '#67BCD8'},
            {name: 'Hettangian', young: 199.5, old: 201.4, color: '#4EB3D3'},
            {name: 'Rhaetian', young: 201.4, old: 205.7, color: '#E3B9DB'},
            {name: 'Norian', young: 205.7, old: 227.3, color: '#D6AAD3'},
            {name: 'Carnian', young: 227.3, old: 237, color: '#C99BCB'},
            {name: 'Ladinian', young: 237, old: 241.464, color: '#C983BF'},
            {name: 'Anisian', young: 241.464, old: 246.7, color: '#BC75B7'},
            {name: 'Olenekian', young: 246.7, old: 249.9, color: '#B051A5'},
            {name: 'Induan', young: 249.9, old: 251.902, color: '#A4469F'},
            {name: 'Changhsingian', young: 251.902, old: 254.14, color: '#FCC0B2'},
            {name: 'Wuchiapingian', young: 254.14, old: 259.51, color: '#FCB4A2'},
            {name: 'Capitanian', young: 259.51, old: 264.28, color: '#FB9A85'},
            {name: 'Wordian', young: 264.28, old: 266.9, color: '#FB8D76'},
            {name: 'Roadian', young: 266.9, old: 274.4, color: '#FB8069'},
            {name: 'Kungurian', young: 274.4, old: 283.3, color: '#E38776'},
            {name: 'Artinskian', young: 283.3, old: 290.1, color: '#E37B68'},
            {name: 'Sakmarian', young: 290.1, old: 293.52, color: '#E36F5C'},
            {name: 'Asselian', young: 293.52, old: 298.9, color: '#E36350'},
            {name: 'Gzhelian', young: 298.9, old: 303.7, color: '#CCD4C7'},
            {name: 'Kasimovian', young: 303.7, old: 307, color: '#BFD0C5'},
            {name: 'Moscovian', young: 307, old: 315.2, color: '#C7CBB9'},
            {name: 'Bashkirian', young: 315.2, old: 323.4, color: '#99C2B5'},
            {name: 'Serpukhovian', young: 323.4, old: 330.3, color: '#BFC26B'},
            {name: 'Visean', young: 330.3, old: 346.7, color: '#A6B96C'},
            {name: 'Tournaisian', young: 346.7, old: 358.86, color: '#8CB06C'},
            {name: 'Famennian', young: 358.86, old: 372.15, color: '#F2EDC5'},
            {name: 'Frasnian', young: 372.15, old: 382.31, color: '#F2EDAD'},
            {name: 'Givetian', young: 382.31, old: 387.95, color: '#F1E185'},
            {name: 'Eifelian', young: 387.95, old: 393.47, color: '#F1D576'},
            {name: 'Emsian', young: 393.47, old: 410.62, color: '#E5D075'},
            {name: 'Pragian', young: 410.62, old: 413.02, color: '#E5C468'},
            {name: 'Lochkovian', young: 413.02, old: 419.62, color: '#E5B75A'},
            {name: 'Pridoli', young: 419.62, old: 422.7, color: '#E6F5E1'},
            {name: 'Ludfordian', young: 422.7, old: 425, color: '#D9F0DF'},
            {name: 'Gorstian', young: 425, old: 426.7, color: '#CCECDD'},
            {name: 'Homerian', young: 426.7, old: 430.6, color: '#CCEBD1'},
            {name: 'Sheinwoodian', young: 430.6, old: 432.9, color: '#BFE6C3'},
            {name: 'Telychian', young: 432.9, old: 438.6, color: '#BFE6CF'},
            {name: 'Aeronian', young: 438.6, old: 440.5, color: '#B3E1C2'},
            {name: 'Rhuddanian', young: 440.5, old: 443.1, color: '#A6DCB5'},
            {name: 'Hirnantian', young: 443.1, old: 445.2, color: '#A6DBAB'},
            {name: 'Katian', young: 445.2, old: 452.8, color: '#99D69F'},
            {name: 'Sandbian', young: 452.8, old: 458.2, color: '#8CD094'},
            {name: 'Darriwilian', young: 458.2, old: 469.4, color: '#74C69C'},
            {name: 'Dapingian', young: 469.4, old: 471.3, color: '#66C092'},
            {name: 'Floian', young: 471.3, old: 477.1, color: '#41B087'},
            {name: 'Tremadocian', young: 477.1, old: 486.85, color: '#33A97E'},
            {name: 'Stage 10', young: 486.85, old: 491, color: '#E6F5C9'},
            {name: 'Jiangshanian', young: 491, old: 494.2, color: '#D9F0BB'},
            {name: 'Paibian', young: 494.2, old: 497, color: '#CCEBAE'},
            {name: 'Guzhangian', young: 497, old: 500.5, color: '#CCDFAA'},
            {name: 'Drumian', young: 500.5, old: 504.5, color: '#BFD99D'},
            {name: 'Wuliuan', young: 504.5, old: 506.5, color: '#B3D492'},
            {name: 'Stage 4', young: 506.5, old: 514.5, color: '#B3CA8E'},
            {name: 'Stage 3', young: 514.5, old: 521, color: '#A6C583'},
            {name: 'Stage 2', young: 521, old: 529, color: '#A6BA80'},
            {name: 'Fortunian', young: 529, old: 538.8, color: '#99B575'}
        ]
    };

    forester.geoIntervals = function (rank) {
        return GEO_SCALE[rank] || [];
    };

    // How far back a rank's intervals reach (its oldest boundary, Ma).
    forester.geoCoverage = function (rank) {
        let ivs = forester.geoIntervals(rank);
        return ivs.length > 0 ? ivs[ivs.length - 1].old : 0;
    };

    // The [coarse, fine] rank pair for the span [youngMa, oldMa] a tree
    // actually occupies -- the youngest tip to the root, not 0 to the root,
    // so a fossil-only clade bands on its own window rather than on the
    // whole stretch back from the present.
    //
    // It adapts FINER as well as coarser. A narrow Phanerozoic window --
    // one or two Series -- bands Series over STAGE (the ICS ages), which is
    // what a 66-100 Ma dinosaur tree needs to say anything at all: over
    // Period/Epoch it would read "Cretaceous / Late Cretaceous" and nothing
    // more. Wider than two Series the stages would be slivers, so the
    // existing ladder takes over: the finest pair that still fully covers the
    // range, so a deep tree never shows blank band segments. Shared rule with
    // the desktop's GeologicTimeScale.bandRanks -- change both or neither.
    forester.geoBandRanks = function (youngMa, oldMa) {
        if (oldMa <= forester.geoCoverage('age')) {   // stages are Phanerozoic-only
            let series = forester.geoOverlapping('epoch', youngMa, oldMa).length;
            if (series > 0 && series <= 2) {
                return ['epoch', 'age'];
            }
        }
        if (oldMa <= forester.geoCoverage('epoch')) {
            return ['period', 'epoch'];
        }
        if (oldMa <= forester.geoCoverage('period')) {
            return ['era', 'period'];
        }
        return ['eon', 'era'];
    };

    // The rank's intervals overlapping [youngMa, oldMa] (a zero-width window
    // becomes a point query).
    forester.geoOverlapping = function (rank, youngMa, oldMa) {
        let lo = Math.min(youngMa, oldMa);
        let hi = Math.max(youngMa, oldMa);
        return forester.geoIntervals(rank).filter(function (iv) {
            if (hi === lo) {
                return iv.young <= lo && lo < iv.old;
            }
            return iv.old > lo && iv.young < hi;
        });
    };

    // The rank's interval containing ageMa (young <= age < old), or null.
    forester.geoAt = function (rank, ageMa) {
        let ivs = forester.geoIntervals(rank);
        for (let i = 0; i < ivs.length; ++i) {
            if (ivs[i].young <= ageMa && ageMa < ivs[i].old) {
                return ivs[i];
            }
        }
        return null;
    };

    // ---- time-tree detection -------------------------------------------
    // Two date conventions: GEOLOGIC ages (Ma before present, decreasing
    // toward the tips) and CALENDAR years (increasing toward the tips).
    // Decided from the <date> unit attributes and NOTHING ELSE. A unitless
    // tree gets no axis: guessing from magnitude read every tip-dated BEAST
    // tree as geologic, because BEAST states a node's age as a unitless
    // `height` and an influenza tree spanning 12 years looks exactly like one
    // spanning 12 Ma. It drew Miocene/Pliocene bands and a "Ma" ruler under
    // tips labelled 1993..2005 -- a confident false claim across the whole
    // figure, which is worse than no axis. Measured over every tree we ship:
    // the guess decided 3 trees and got all 3 wrong, every other axis comes
    // from a unit, and none relied on the old [1500, 2200] year rule. The
    // desktop dropped the same rule on 2026-09-10 (their e9ac62ec) on the
    // same evidence; Christian, 2026-09-17, both programs.
    const GEO_DATE_UNITS = {
        mya: 1, ma: 1, myr: 1, myrs: 1, my: 1, ga: 1, gya: 1, bya: 1, kya: 1,
        'million years': 1, 'billion years': 1
    };
    const CAL_DATE_UNITS = {
        year: 1, years: 1, yr: 1, yrs: 1, cal: 1, ce: 1, ad: 1, calendar: 1,
        'calendar year': 1, 'calendar years': 1
    };

    // A date's bounds state a real interval only when they differ by more than
    // floating-point noise. TreeAnnotator writes
    // height_95%_HPD={9.0,9.000000000000004} on a tip it dated EXACTLY: one
    // number printed twice through binary floating point, not a width. Read as
    // a width it put a fossil-range bracket on 686 of influenza.tree's 687
    // tips. The tolerance is RELATIVE because the noise is: a bound near 2000
    // carries more of it than one near 0. Nothing real is this narrow -- a
    // single day is 0.0027 of a calendar year, while 1e-9 of 2000 is about a
    // minute. Both programs had this gap, on two paths each (an auto-enable
    // predicate with no tolerance, and a painter with no width test at all).
    const DATE_INTERVAL_REL_TOL = 1e-9;

    /**
     * Whether a date's minimum/maximum are a genuine interval rather than one
     * value printed twice. Every reader of date bounds must ask this, drawing
     * included: a bar floored at 1px draws noise as a visible bracket.
     *
     * @param date a node's date object
     * @returns {boolean}
     */
    forester.isGenuineDateInterval = function (date) {
        if (!date || typeof date.minimum !== 'number' || typeof date.maximum !== 'number'
            || !isFinite(date.minimum) || !isFinite(date.maximum)) {
            return false;
        }
        let scale = Math.max(Math.abs(date.minimum), Math.abs(date.maximum), 1);
        // the ABSOLUTE difference: bounds handed over in the wrong order still
        // state a width, and every painter already normalises the order before
        // drawing. The desktop's AptxUtil.hasDateIntervalWidth reads it the
        // same way, down to this constant -- the two programs must not draw
        // different figures from one file (their message, 2026-09-17).
        return Math.abs(date.maximum - date.minimum) > (scale * DATE_INTERVAL_REL_TOL);
    };

    /**
     * Whether the tree's branch lengths are time: most of its internal nodes
     * carry a date, and at least two do -- BEAST node heights, Nextstrain
     * dates, phyloXML <date>s on ancestors. Tip dates alone do not make one:
     * a divergence tree can carry collection dates, and re-rooting it (as a
     * root-to-tip regression does) is a normal step. A time tree is never
     * re-rooted: a new root would contradict the ancestors' dates. The
     * desktop's rule is the same (its detectTimeTree, internal-node half).
     *
     * @param phy the tree
     * @returns {boolean}
     */
    forester.isTimeTree = function (phy) {
        let internal = 0;
        let dated = 0;
        forester.preOrderTraversalAll(forester.getTreeRoot(phy), function (n) {
            if (n.children && n.children.length > 0) {
                ++internal;
                if (n.date && typeof n.date.value === 'number' && isFinite(n.date.value)) {
                    ++dated;
                }
            }
        });
        return dated >= 2 && dated * 2 > internal;
    };

    // Everything the viewer needs to decide about and draw a time axis:
    // {type: 'geologic'|'calendar'|null, rootAge, presentDate, dated,
    //  hasInternalIntervals, hasExternalIntervals}. rootAge (geologic) and
    // presentDate (calendar) are both the LARGEST date value -- the oldest
    // node for ages, the most recent tip for years.
    forester.timeAxisInfo = function (root) {
        let valued = 0;
        let maxVal = -Infinity; // running, not Math.max.apply: 150k dated tips overflow the call stack
        let geoUnits = 0;
        let calUnits = 0;
        let internal = 0;
        let external = 0;
        let datedInternal = 0;
        let datedExternal = 0;
        let hasInternalIntervals = false;
        let hasExternalIntervals = false;
        forester.preOrderTraversalAll(root, function (n) {
            let isExt = !n.children;
            if (isExt) {
                ++external;
            } else {
                ++internal;
            }
            let d = n.date;
            if (!d) {
                return;
            }
            let interval = forester.isGenuineDateInterval(d);
            if (interval) {
                if (isExt) {
                    hasExternalIntervals = true;
                } else {
                    hasInternalIntervals = true;
                }
            }
            if (typeof d.value !== 'number' || !isFinite(d.value)) {
                return; // 1e400 parses to Infinity and must never reach the tick loops
            }
            ++valued;
            if (d.value > maxVal) {
                maxVal = d.value;
            }
            if (isExt) {
                ++datedExternal;
            } else {
                ++datedInternal;
            }
            if (d.unit) {
                let u = String(d.unit).trim().toLowerCase();
                if (GEO_DATE_UNITS[u]) {
                    ++geoUnits;
                } else if (CAL_DATE_UNITS[u]) {
                    ++calUnits;
                }
            }
        });
        let type = null;
        if (valued > 0) {
            if (geoUnits > 0 && geoUnits >= calUnits) {
                type = 'geologic';
            } else if (calUnits > 0) {
                type = 'calendar';
            }
        }
        let dated = (datedInternal >= 2 && (datedInternal * 2) > internal)
            || (datedExternal >= 2 && (datedExternal * 2) > external);
        let maxValue = valued > 0 ? maxVal : 0;
        return {
            type: type,
            rootAge: type === 'geologic' ? maxValue : 0,
            presentDate: type === 'calendar' ? maxValue : 0,
            dated: dated,
            hasInternalIntervals: hasInternalIntervals,
            hasExternalIntervals: hasExternalIntervals
        };
    };

    // ---- dates in a tip's LABEL, and BEAST heights ----------------------
    // A tip-dated BEAST tree states a node's age as a unitless `height`: time
    // before the youngest tip, in whatever unit the run happened to use. The
    // sampling dates are usually in the tip labels themselves
    // (A_duck_Guangdong_12_2000, NewYork_705_1994.1, EBOV|KR817226|2014-06-10).
    // If the heights are YEARS, every tip's label date plus its height is the
    // same calendar date -- the date of height 0. That agreement is the whole
    // evidence: heights in months or days, or a strain number mistaken for a
    // date, break it and the tree is left alone.
    //
    // Ported from the desktop's TipDateExtractor and HeightDateConverter
    // (their 0.11.151, commit 6755ba12), whose rules and constants these are.
    // Christian, 2026-09-17: port it, after they commit.

    // A BARE year or decimal year has to look like a plausible sampling year or
    // a 4-digit strain number would date the tip. An explicit ISO / month-name
    // / slash date is strong evidence and gets a looser window.
    const TIP_DATE_MIN_BARE_YEAR = 1900;
    const TIP_DATE_MAX_BARE_YEAR = 2100;
    const TIP_DATE_MIN_YEAR = 1000;
    const TIP_DATE_MAX_YEAR = 2999;

    // Global, so each matcher can take the RIGHTMOST match; lastIndex is reset
    // before every use, because these are shared.
    const TIP_DATE_ISO_FULL = /(?<![0-9])(\d{4})-(\d{1,2})-(\d{1,2})(?![0-9])/g;
    const TIP_DATE_MONTH_FULL = /(?<![A-Za-z0-9])(\d{1,2})[-\s]([A-Za-z]{3,9})[-\s](\d{4})(?![0-9])/g;
    const TIP_DATE_SLASH_FULL = /(?<![0-9])(\d{1,4})[/.](\d{1,2})[/.](\d{1,4})(?![0-9])/g;
    const TIP_DATE_ISO_PARTIAL = /(?<![0-9])(\d{4})-(\d{1,2})(?![0-9-])/g;
    const TIP_DATE_MONTH_PARTIAL = /(?<![A-Za-z0-9])([A-Za-z]{3,9})[-\s](\d{4})(?![0-9])/g;
    const TIP_DATE_DECIMAL_YEAR = /(?<![0-9.])(\d{4}\.\d+)(?![0-9])/g;
    const TIP_DATE_BARE_YEAR = /(?<![0-9.])(\d{4})(?![0-9.])/g;

    const TIP_DATE_MONTHS_ABBR = ['jan', 'feb', 'mar', 'apr', 'may', 'jun', 'jul', 'aug', 'sep', 'oct', 'nov', 'dec'];
    const TIP_DATE_MONTHS_FULL = ['january', 'february', 'march', 'april', 'may', 'june', 'july', 'august',
        'september', 'october', 'november', 'december'];

    function tipDateYearLength(y) {
        return (((y % 4) === 0 && (y % 100) !== 0) || (y % 400) === 0) ? 366 : 365;
    }

    function tipDateMonthLength(y, m) {
        if (m === 2) {
            return tipDateYearLength(y) === 366 ? 29 : 28;
        }
        return ((m === 4) || (m === 6) || (m === 9) || (m === 11)) ? 30 : 31;
    }

    function tipDateDayOfYear(y, m, d) {
        let doy = d;
        for (let i = 1; i < m; ++i) {
            doy += tipDateMonthLength(y, i);
        }
        return doy;
    }

    // 1-12 for an English month name -- the EXACT 3-letter abbreviation or the
    // full name, case-insensitively; 0 otherwise. Exact, not a prefix, is what
    // stops "Marburg", "Junin" or "Decatur" being read as a month.
    function tipDateMonthNumber(name) {
        if (!name) {
            return 0;
        }
        let key = String(name).toLowerCase();
        for (let i = 0; i < 12; ++i) {
            if (TIP_DATE_MONTHS_ABBR[i] === key || TIP_DATE_MONTHS_FULL[i] === key) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * The decimal year of a (year, month, day): a month or day of 0 means "not
     * given" and maps to the middle of the interval it leaves open (a year to
     * .5, a month to mid-month, a day to mid-day). Leap-year aware. The
     * midpoint convention is BEAST's and TreeTime's.
     *
     * @param year
     * @param month 1-12, or <= 0 for "not given"
     * @param day 1-31, or <= 0 for "not given"
     * @returns {number}
     */
    forester.tipDateToDecimalYear = function (year, month, day) {
        if (month <= 0) {
            return year + 0.5;
        }
        let len = tipDateYearLength(year);
        if (day <= 0) {
            let firstDoy = tipDateDayOfYear(year, month, 1);
            return year + (((firstDoy - 1) + (tipDateMonthLength(year, month) / 2)) / len);
        }
        return year + ((tipDateDayOfYear(year, month, day) - 0.5) / len);
    };

    // A full date, validated (an impossible one like 2021-13-40 or Feb 30 is
    // not a date). The RANGE is the whole day the label names.
    function tipDateFromYmd(year, month, day, matched, format, ambiguous) {
        if ((year < TIP_DATE_MIN_YEAR) || (year > TIP_DATE_MAX_YEAR) || (month < 1) || (month > 12)
            || (day < 1) || (day > tipDateMonthLength(year, month))) {
            return null;
        }
        let len = tipDateYearLength(year);
        let doy = tipDateDayOfYear(year, month, day);
        return {
            decimalYear: forester.tipDateToDecimalYear(year, month, day),
            matchedText: matched, precision: 'day', formatLabel: format, ambiguous: ambiguous === true,
            rangeStart: year + ((doy - 1) / len), rangeEnd: year + (doy / len)
        };
    }

    // A year-month date: the day is unknown, so the range is the whole month.
    function tipDateFromYm(year, month, matched, format) {
        if ((year < TIP_DATE_MIN_YEAR) || (year > TIP_DATE_MAX_YEAR) || (month < 1) || (month > 12)) {
            return null;
        }
        let len = tipDateYearLength(year);
        let start = year + ((tipDateDayOfYear(year, month, 1) - 1) / len);
        return {
            decimalYear: forester.tipDateToDecimalYear(year, month, 0),
            matchedText: matched, precision: 'month', formatLabel: format, ambiguous: false,
            rangeStart: start, rangeEnd: start + (tipDateMonthLength(year, month) / len)
        };
    }

    // Every matcher keeps the LAST (rightmost) valid match in the label.
    function tipDateEachMatch(re, s, fn) {
        let found = null;
        let m;
        re.lastIndex = 0;
        while ((m = re.exec(s)) !== null) {
            let d = fn(m);
            if (d) {
                found = d;
            }
            if (m.index === re.lastIndex) {
                ++re.lastIndex; // a zero-width match would spin forever
            }
        }
        return found;
    }

    /**
     * The date a tip label states, or null. The most SPECIFIC format wins
     * (a full date before a year-month before a bare year), and within one
     * format the rightmost match does -- a label usually ends with its date.
     *
     * The result carries the decimal year (midpoint convention) AND the
     * calendar RANGE the label actually states: `2021` is all of 2021,
     * `2021-03` all of March, a day that whole day, and a decimal year its
     * last written digit (`1993.1` is 1993.05 to 1993.15). The range is what
     * makes different programs' conventions comparable -- BEAST counts a day
     * from its start, we read a label at its middle, half a day apart.
     *
     * @param label a tip's name
     * @param monthFirst true to read an ambiguous numeric date (both fields
     *        <= 12) month-first; the default is day-first, as the desktop's
     *        converter always asks for
     * @returns {object|null}
     */
    forester.parseTipLabelDate = function (label, monthFirst) {
        if (!label) {
            return null;
        }
        let s = String(label);
        let m = tipDateEachMatch(TIP_DATE_ISO_FULL, s, function (g) {
            return tipDateFromYmd(parseInt(g[1], 10), parseInt(g[2], 10), parseInt(g[3], 10), g[0],
                'ISO date (YYYY-MM-DD)', false);
        });
        if (m) {
            return m;
        }
        m = tipDateEachMatch(TIP_DATE_MONTH_FULL, s, function (g) {
            let mon = tipDateMonthNumber(g[2]);
            return mon > 0
                ? tipDateFromYmd(parseInt(g[3], 10), mon, parseInt(g[1], 10), g[0], 'month-name date', false)
                : null;
        });
        if (m) {
            return m;
        }
        m = tipDateEachMatch(TIP_DATE_SLASH_FULL, s, function (g) {
            let a = parseInt(g[1], 10);
            let b = parseInt(g[2], 10);
            let c = parseInt(g[3], 10);
            let year = -1;
            let month = -1;
            let day = -1;
            let ambiguous = false;
            if (g[1].length === 4) {          // YYYY/MM/DD -- unambiguous
                year = a;
                month = b;
                day = c;
            } else if (g[3].length === 4) {   // D/M/YYYY or M/D/YYYY
                year = c;
                if (a > 12) {
                    day = a;
                    month = b;
                } else if (b > 12) {
                    month = a;
                    day = b;
                } else {                      // both <= 12: genuinely ambiguous
                    ambiguous = true;
                    if (monthFirst === true) {
                        month = a;
                        day = b;
                    } else {
                        day = a;
                        month = b;
                    }
                }
            }
            return year > 0 ? tipDateFromYmd(year, month, day, g[0], 'numeric date', ambiguous) : null;
        });
        if (m) {
            return m;
        }
        m = tipDateEachMatch(TIP_DATE_ISO_PARTIAL, s, function (g) {
            return tipDateFromYm(parseInt(g[1], 10), parseInt(g[2], 10), g[0], 'ISO year-month (YYYY-MM)');
        });
        if (m) {
            return m;
        }
        m = tipDateEachMatch(TIP_DATE_MONTH_PARTIAL, s, function (g) {
            let mon = tipDateMonthNumber(g[1]);
            return mon > 0 ? tipDateFromYm(parseInt(g[2], 10), mon, g[0], 'month-name year') : null;
        });
        if (m) {
            return m;
        }
        m = tipDateEachMatch(TIP_DATE_DECIMAL_YEAR, s, function (g) {
            let v = parseFloat(g[1]);
            let y = Math.floor(v);
            if ((y < TIP_DATE_MIN_BARE_YEAR) || (y > TIP_DATE_MAX_BARE_YEAR)) {
                return null;
            }
            // the range is the last digit the label actually wrote
            let half = 0.5 * Math.pow(10, -(g[1].length - g[1].indexOf('.') - 1));
            return {
                decimalYear: v, matchedText: g[1], precision: 'day', formatLabel: 'decimal year',
                ambiguous: false, rangeStart: v - half, rangeEnd: v + half
            };
        });
        if (m) {
            return m;
        }
        return tipDateEachMatch(TIP_DATE_BARE_YEAR, s, function (g) {
            let y = parseInt(g[1], 10);
            if ((y < TIP_DATE_MIN_BARE_YEAR) || (y > TIP_DATE_MAX_BARE_YEAR)) {
                return null;
            }
            return {
                decimalYear: y + 0.5, matchedText: g[1], precision: 'year', formatLabel: 'year',
                ambiguous: false, rangeStart: y, rangeEnd: y + 1
            };
        });
    };

    // ---- unitless BEAST heights -> calendar dates -----------------------
    // How far a tip's label date plus its height may miss, in years: enough for
    // the one-day differences between programs' decimal-year conventions, and
    // far below the gaps that heights in months or days open up.
    const HEIGHT_DATE_TOLERANCE_YEARS = 0.01;
    // at least 19 of every 20 compared tips must agree on the date of height 0
    const HEIGHT_DATE_MIN_AGREEING_NUM = 19;
    const HEIGHT_DATE_MIN_AGREEING_DEN = 20;

    // 5 decimals, HALF_UP -- away from zero, which Math.round is not for a
    // negative half (it takes -0.5 to -0). The desktop rounds with BigDecimal
    // HALF_UP, and the anchor has to land on the same digits or every date on
    // the tree shifts.
    function heightDateRound5(x) {
        let s = x < 0 ? -1 : 1;
        return (s * Math.round(Math.abs(x) * 100000)) / 100000;
    }

    // Whether a compared tip allows height 0 at calendar date x. The tip's
    // allowed stretch is its LABEL RANGE plus its height, widened by the
    // tolerance at each end.
    function heightDateAllows(r, x) {
        return (x >= ((r[0] + r[2]) - HEIGHT_DATE_TOLERANCE_YEARS))
            && (x <= (r[1] + r[2] + HEIGHT_DATE_TOLERANCE_YEARS));
    }

    // The stretch of calendar dates allowed by the MOST tips, as [start, end],
    // or null when a separate stretch is allowed by as many -- then which date
    // height 0 is, is undecided, and a tree we cannot place we leave alone. A
    // sweep over the tips' allowed intervals, closed at both ends.
    //
    // The `tied` refusal is LIVE, and a test pins it. I first argued it was
    // unreachable -- two disjoint stretches would each need 19 tips in 20, so
    // 18 in 20 would allow both, and since a tip's allowed dates are one
    // interval those 18 allow everything between, making it one stretch. That
    // last step is wrong, and the desktop caught it: the 18 do cover the gap,
    // but a stretch is the MAXIMUM coverage, which is 19, and 18 < 19, so the
    // gap joins neither maximum and the two stay separate. The shape is
    // 19 / 18 / 19.
    //
    // It is reached by an ordinary curation error: a mostly year-labelled tree
    // (a bare year is ~1 year wide, so those tips span both candidates) with
    // two tips at height 0 whose day labels disagree -- a file cannot have two
    // different youngest tips. Remove this check and such a tree converts at a
    // fabricated anchor, silently. My own fixture had all its year tips in ONE
    // year, which made the different-sampling-times rule fire first and hide
    // the whole thing.
    function heightDateMostAllowed(ranges) {
        let events = [];
        ranges.forEach(function (r) {
            events.push([(r[0] + r[2]) - HEIGHT_DATE_TOLERANCE_YEARS, 1]);
            events.push([r[1] + r[2] + HEIGHT_DATE_TOLERANCE_YEARS, -1]);
        });
        // a start sorts before an end at the same x, so intervals that merely
        // touch do overlap
        events.sort(function (a, b) {
            return a[0] !== b[0] ? (a[0] - b[0]) : (b[1] - a[1]);
        });
        let depth = 0;
        let best = 0;
        let start = 0;
        let end = 0;
        let open = false;
        let tied = false;
        events.forEach(function (e) {
            if (e[1] > 0) {
                ++depth;
                if (depth > best) {
                    best = depth;
                    start = e[0];
                    open = true;
                    tied = false;
                } else if ((depth === best) && !open) {
                    tied = true;
                }
            } else {
                if (open) {
                    end = e[0];
                    open = false;
                }
                --depth;
            }
        });
        return ((best === 0) || tied) ? null : [start, end];
    }

    // Where the most precisely dated tips put height 0: the median of
    // (label date + height) over the tips whose label RANGE is at most twice
    // the narrowest. The middle of the allowed stretch would be pulled about by
    // the coarse labels -- influenza.tree mixes `1993.11` with `1997` (meaning
    // the whole of 1997), and that middle, 2005.2525, showed the tip labelled
    // 1994.1 as 1994.1025.
    function heightDatePrecisestMedian(tips) {
        let narrowest = Infinity;
        tips.forEach(function (r) {
            narrowest = Math.min(narrowest, r[1] - r[0]);
        });
        let values = [];
        tips.forEach(function (r) {
            if ((r[1] - r[0]) <= (2 * narrowest)) {
                values.push(r[3] + r[2]);
            }
        });
        values.sort(function (a, b) {
            return a - b;
        });
        let n = values.length;
        return ((n % 2) === 1) ? values[(n - 1) / 2] : ((values[(n / 2) - 1] + values[n / 2]) / 2);
    }

    /**
     * The calendar date (decimal year) of height 0 for a tip-dated BEAST tree,
     * or null when the tree does not qualify and must be left alone. Pure.
     *
     * Every condition has to hold: no node's date carries a unit (a unit says
     * what the numbers already are, and this never overrides one); the tree is
     * a time tree; a strict majority of tips carry both a height and a date in
     * the label; the date allowed by the most of those tips is allowed by at
     * least 19 in 20 of them and no separate stretch of dates is allowed by as
     * many; and the agreeing tips were sampled at different times -- tips all
     * from one year agree with heights in ANY unit and so prove nothing.
     *
     * @param phy a tree
     * @returns {{present: number, agreeing: number, compared: number}|null}
     */
    forester.inferHeightDateAnchor = function (phy) {
        if (!phy) {
            return null;
        }
        let root = forester.getTreeRoot(phy);
        if (!root) {
            return null;
        }
        let united = false;
        forester.preOrderTraversalAll(root, function (n) {
            if (n.date && n.date.unit && String(n.date.unit).trim().length > 0) {
                united = true;
            }
        });
        if (united || !forester.isTimeTree(phy)) {
            return null;
        }
        let tips = 0;
        let ranges = []; // per compared tip: [label start, label end, height, label date]
        forester.preOrderTraversalAll(root, function (n) {
            if (n.children && n.children.length > 0) {
                return;
            }
            ++tips;
            // not "has a date": a value of 0 -- the youngest tip's height -- is
            // a height, not an absence
            if (!n.date || typeof n.date.value !== 'number' || !isFinite(n.date.value)) {
                return;
            }
            let m = forester.parseTipLabelDate(n.name);
            if (m) {
                ranges.push([m.rangeStart, m.rangeEnd, n.date.value, m.decimalYear]);
            }
        });
        let compared = ranges.length;
        if ((compared * 2) <= tips) {
            return null;
        }
        let best = heightDateMostAllowed(ranges);
        if (best === null) {
            return null;
        }
        let mid = (best[0] + best[1]) / 2;
        let agreeingTips = [];
        let latestStart = -Infinity;
        let earliestEnd = Infinity;
        // the dates the agreeing tips actually STATE, un-widened: their labels
        // plus their heights, with no tolerance added
        let offerLo = -Infinity;
        let offerHi = Infinity;
        ranges.forEach(function (r) {
            if (heightDateAllows(r, mid)) {
                agreeingTips.push(r);
                latestStart = Math.max(latestStart, r[0]);
                earliestEnd = Math.min(earliestEnd, r[1]);
                offerLo = Math.max(offerLo, r[0] + r[2]);
                offerHi = Math.min(offerHi, r[1] + r[2]);
            }
        });
        let agreeing = agreeingTips.length;
        if ((agreeing * HEIGHT_DATE_MIN_AGREEING_DEN) < (compared * HEIGHT_DATE_MIN_AGREEING_NUM)) {
            return null;
        }
        if (latestStart <= earliestEnd) {
            return null; // every agreeing label range overlaps every other
        }
        // The anchor is clamped into what the agreeing tips actually STATE,
        // not into the stretch the tolerance opened up. The tolerance decides
        // AGREEMENT -- how far a tip may miss and still count -- and must not
        // then place the date: clamping into the widened stretch let the answer
        // drift by up to the tolerance, about four days. Found by the desktop
        // on ground truth: three Nextstrain .nwk trees whose .nexus siblings
        // carry the real dates were out by 4.15, 3.96 and 2.92 days, and land
        // within half a day after this (2026-09-17).
        //
        // Where the agreeing tips overlap only BECAUSE of the tolerance and
        // state nothing in common, there is nothing to clamp into and the
        // precise-label median stands.
        let median = heightDatePrecisestMedian(agreeingTips);
        let present = (offerLo <= offerHi) ? Math.max(offerLo, Math.min(offerHi, median)) : median;
        return {present: heightDateRound5(present), agreeing: agreeing, compared: compared};
    };

    /**
     * Rewrites every node's date as `present - height`, in years, with the
     * interval bounds SWAPPED: the older height bound is the earlier date.
     *
     * @param phy a tree
     * @param present the date of height 0, from forester.inferHeightDateAnchor
     */
    forester.convertHeightsToDates = function (phy, present) {
        forester.preOrderTraversalAll(forester.getTreeRoot(phy), function (n) {
            let d = n.date;
            if (!d) {
                return;
            }
            let hasValue = typeof d.value === 'number' && isFinite(d.value);
            let value = hasValue ? heightDateRound5(present - d.value) : undefined;
            // the swap: a LARGER height is further back, so it becomes the
            // SMALLER (earlier) date
            let min = (typeof d.maximum === 'number' && isFinite(d.maximum))
                ? heightDateRound5(present - d.maximum) : undefined;
            let max = (typeof d.minimum === 'number' && isFinite(d.minimum))
                ? heightDateRound5(present - d.minimum) : undefined;
            let next = {};
            if (d.desc !== undefined) {
                next.desc = d.desc;
            }
            if (value !== undefined) {
                next.value = value;
            }
            if (min !== undefined) {
                next.minimum = min;
            }
            if (max !== undefined) {
                next.maximum = max;
            }
            // a date with no point value keeps an empty unit, as the reader
            // writes it
            next.unit = hasValue ? 'year' : '';
            n.date = next;
        });
    };

    /**
     * The desktop's provenance sentence for a converted tree, which it appends
     * to the tree's description.
     *
     * @param anchor from forester.inferHeightDateAnchor
     * @param treeName
     * @param tipCount
     * @returns {string}
     */
    forester.heightDateDescription = function (anchor, treeName, tipCount) {
        let phrase = treeName ? ('tree named "' + treeName + '" with ') : 'a tree with ';
        let present = String(anchor.present);
        return 'Converted the node heights of ' + phrase + tipCount + (tipCount === 1 ? ' tip' : ' tips')
            + ' to calendar dates: the sampling dates in ' + anchor.agreeing + ' of ' + anchor.compared
            + ' tip labels put height 0 at ' + present + ', so each date is ' + present + ' minus the height.';
    };

    /**
     * Converts every tree of a load whose unitless heights can be placed in
     * calendar time, appending the provenance sentence to each converted
     * tree's description. A tree that does not qualify is left exactly as it
     * was. Returns the number of trees converted.
     *
     * @param phys one tree or an array of them
     * @returns {number}
     */
    forester.convertLoadedHeightsToDates = function (phys) {
        if (!phys) {
            return 0;
        }
        let list = Array.isArray(phys) ? phys : [phys];
        let converted = 0;
        list.forEach(function (phy) {
            let anchor = forester.inferHeightDateAnchor(phy);
            if (anchor === null) {
                return;
            }
            forester.convertHeightsToDates(phy, anchor.present);
            let prov = forester.heightDateDescription(anchor, phy.name,
                forester.getAllExternalNodes(forester.getTreeRoot(phy)).length);
            phy.description = (phy.description && String(phy.description).trim().length > 0)
                ? (phy.description + ' ' + prov) : prov;
            ++converted;
        });
        return converted;
    };

    // ---- axis tick mathematics -----------------------------------------
    // The smallest 1/2/5 x 10^k (k may be negative) step >= target.
    forester.niceAxisStep = function (target) {
        if (!(target > 0) || !isFinite(target)) {
            return 1;
        }
        let mag = Math.pow(10, Math.floor(Math.log(target) / Math.LN10));
        let candidates = [1, 2, 5, 10];
        for (let i = 0; i < candidates.length; ++i) {
            let s = candidates[i] * mag;
            if (s >= target - 1e-12) {
                return s;
            }
        }
        return 10 * mag;
    };

    // Tick ages for a "Ma before present" ruler: ~8 nice steps from 0 back
    // to the root age.
    forester.maAxisTickValues = function (rootAge) {
        return forester.distanceAxisTickValues(rootAge);
    };

    // Tick values for a distance axis: ~8 nice steps from 0 (the root) out
    // to maxDist (the deepest tip), in the tree's branch-length units.
    forester.distanceAxisTickValues = function (maxDist) {
        if (!(maxDist > 0) || !isFinite(maxDist)) {
            return [];
        }
        let step = forester.niceAxisStep(maxDist / 8);
        if (!(step > 0)) {
            return [];
        }
        let vals = [];
        for (let i = 0; (i * step) <= maxDist + (step * 1e-9); ++i) {
            vals.push(Number((i * step).toPrecision(12)));
        }
        return vals;
    };

    // Whole-year calendar ticks over [from, to]: nice 1/2/5 x 10^k integer
    // steps, ~7 ticks.
    forester.calendarTickYears = function (from, to) {
        let span = to - from;
        if (!(span > 0) || !isFinite(span) || !isFinite(from)) {
            return [];
        }
        let step = Math.max(1, Math.round(forester.niceAxisStep(span / 7)));
        let vals = [];
        for (let y = Math.ceil(from / step) * step; y <= to + 1e-9; y += step) {
            vals.push(y);
        }
        return vals;
    };

    // Month ticks over [from, to] in decimal years, for a span too short for
    // whole years (an outbreak sampled over some months): the first of every
    // 1st, 2nd, 3rd, 6th or 12th month, the smallest step leaving six ticks
    // or fewer; none where even a tick a year is too many. {value, year,
    // month}; month is 1-12, value the decimal year at which that month
    // begins.
    forester.calendarTickMonths = function (from, to) {
        let span = to - from;
        if (!(span > 0) || !isFinite(span) || !isFinite(from)) {
            return [];
        }
        let steps = [1, 2, 3, 6, 12];
        for (let s = 0; s < steps.length; ++s) {
            let step = steps[s];
            let ticks = [];
            let year = Math.floor(from);
            // month index = year * 12 + (month - 1); ticks where it divides
            for (let k = year * 12; ticks.length <= 6; ++k) {
                let y = Math.floor(k / 12);
                let m = (k - (y * 12)) + 1;
                let v = y + ((tipDateDayOfYear(y, m, 1) - 1) / tipDateYearLength(y));
                if (v > to + 1e-9) {
                    break;
                }
                if (v >= from - 1e-9 && (k % step) === 0) {
                    ticks.push({value: v, year: y, month: m});
                }
            }
            if (ticks.length <= 6) {
                return ticks;
            }
        }
        return [];
    };


    // --------------------------------------------------------------
    // Multiple sequence alignment (the desktop's alignment track)
    // --------------------------------------------------------------
    // Residue palettes, gap handling, conservation scoring and the hover
    // readout data -- all pure, shared by the viewer's alignment track.
    // Palettes are the desktop's (MsaColors.java): a Zappo-style 7-class
    // physico-chemical scheme for amino acids, one colour per base for
    // nucleotides, muted grey for ambiguity codes, and NO fill for a gap.

    const MSA_GAP_CHARS = {'-': true, '.': true, ' ': true, '~': true};

    // [r, g, b] triples so the letter-ink contrast rule can read them.
    const MSA_AA_CLASSES = [
        {residues: 'ILVAM', clazz: 'aliphatic (hydrophobic)', rgb: [240, 170, 170]},
        {residues: 'FWY', clazz: 'aromatic', rgb: [240, 190, 90]},
        {residues: 'KRH', clazz: 'positively charged', rgb: [120, 130, 240]},
        {residues: 'DE', clazz: 'negatively charged', rgb: [230, 100, 100]},
        {residues: 'STNQ', clazz: 'polar (hydrophilic)', rgb: [120, 200, 120]},
        {residues: 'PG', clazz: 'conformationally special', rgb: [220, 130, 220]},
        {residues: 'C', clazz: 'cysteine', rgb: [235, 220, 110]}
    ];
    const MSA_NT_RGB = {
        A: [120, 200, 120],
        C: [120, 130, 240],
        G: [230, 185, 80],
        T: [230, 110, 110],
        U: [230, 110, 110]
    };
    const MSA_UNKNOWN_RGB = [205, 205, 205];

    const MSA_AA_RGB = {};
    const MSA_AA_CLASS = {};
    MSA_AA_CLASSES.forEach(function (c) {
        for (let i = 0; i < c.residues.length; ++i) {
            MSA_AA_RGB[c.residues.charAt(i)] = c.rgb;
            MSA_AA_CLASS[c.residues.charAt(i)] = c.clazz;
        }
    });
    // The palette triples and the geologic table are handed out by reference;
    // freezing them keeps one careless caller mutation from permanently
    // recolouring a whole residue class (or renaming the Cretaceous).
    MSA_AA_CLASSES.forEach(function (c) {
        Object.freeze(c.rgb);
    });
    Object.keys(MSA_NT_RGB).forEach(function (k) {
        Object.freeze(MSA_NT_RGB[k]);
    });
    Object.freeze(MSA_UNKNOWN_RGB);
    Object.keys(GEO_SCALE).forEach(function (rank) {
        GEO_SCALE[rank].forEach(Object.freeze);
        Object.freeze(GEO_SCALE[rank]);
    });

    // Kyte-Doolittle hydropathy and full residue names, for the hover readout.
    const MSA_HYDROPATHY = {
        I: 4.5, V: 4.2, L: 3.8, F: 2.8, C: 2.5, M: 1.9, A: 1.8, G: -0.4,
        T: -0.7, S: -0.8, W: -0.9, Y: -1.3, P: -1.6, H: -3.2, E: -3.5,
        Q: -3.5, D: -3.5, N: -3.5, K: -3.9, R: -4.5
    };
    const MSA_AA_NAMES = {
        A: 'Alanine', R: 'Arginine', N: 'Asparagine', D: 'Aspartic acid',
        C: 'Cysteine', E: 'Glutamic acid', Q: 'Glutamine', G: 'Glycine',
        H: 'Histidine', I: 'Isoleucine', L: 'Leucine', K: 'Lysine',
        M: 'Methionine', F: 'Phenylalanine', P: 'Proline', S: 'Serine',
        T: 'Threonine', W: 'Tryptophan', Y: 'Tyrosine', V: 'Valine',
        U: 'Selenocysteine', O: 'Pyrrolysine',
        B: 'Asparagine or aspartic acid', Z: 'Glutamine or glutamic acid',
        X: 'Any amino acid', '*': 'stop'
    };
    const MSA_NT_NAMES = {
        A: 'Adenine', C: 'Cytosine', G: 'Guanine', T: 'Thymine',
        U: 'Uracil', N: 'any base'
    };
    const MSA_NT_CLASS = {
        A: 'purine', G: 'purine', C: 'pyrimidine', T: 'pyrimidine', U: 'pyrimidine'
    };

    forester.isMsaGap = function (ch) {
        return MSA_GAP_CHARS[ch] === true;
    };

    // The residue's fill as an [r,g,b] triple, or null for a gap (drawn as a
    // faint dash, not a filled cell).
    forester.msaResidueRgb = function (ch, nucleotide) {
        if (ch === undefined || ch === null || forester.isMsaGap(ch)) {
            return null;
        }
        let u = ch.toUpperCase();
        let rgb = nucleotide ? MSA_NT_RGB[u] : MSA_AA_RGB[u];
        return rgb ? rgb : MSA_UNKNOWN_RGB;
    };

    // Black or white letter ink over the given cell colour, by luminance --
    // the same rule the desktop uses.
    forester.msaLetterInk = function (rgb) {
        if (!rgb) {
            return '#404040'; // over a gap / unfilled cell, as on the desktop
        }
        let luminance = (0.299 * rgb[0]) + (0.587 * rgb[1]) + (0.114 * rgb[2]);
        return luminance < 140 ? '#ffffff' : '#000000';
    };

    // Amino acid or nucleotide? Judged on the actual residues, never on any
    // declared type: the fraction of non-gap characters that are plausible
    // bases (ACGTUN) decides.
    forester.msaIsNucleotide = function (seq) {
        if (!seq) {
            return false;
        }
        let bases = 0;
        let residues = 0;
        for (let i = 0; i < seq.length; ++i) {
            let ch = seq.charAt(i);
            if (forester.isMsaGap(ch)) {
                continue;
            }
            ++residues;
            if ('ACGTUNacgtun'.indexOf(ch) >= 0) {
                ++bases;
            }
        }
        return residues > 0 && (bases / residues) > 0.9;
    };

    // Per-column conservation over the given rows (gapped strings; a short
    // row's missing tail counts as gaps). Two measures, both in [0,1]:
    // 'identity' -- the fraction of ROWS carrying the column's most common
    // residue (gaps stay in the denominator); 'information' -- the Schneider
    // & Stephens sequence-logo information content, normalized by log2(K)
    // (K = 4 or 20) and scaled by the column's non-gap fraction. Consensus is
    // the most common NON-gap residue, ties broken alphabetically so figures
    // are reproducible.
    forester.msaConservation = function (rows, length, measure, nucleotide) {
        if (!(length >= 0) || !isFinite(length)) {
            return {scores: [], consensus: []};
        }
        length = Math.floor(length);
        let n = rows.length;
        let scores = new Array(length);
        let consensus = new Array(length);
        let K = nucleotide ? 4 : 20;
        let log2K = Math.log(K) / Math.LN2;
        for (let c = 0; c < length; ++c) {
            let counts = {};
            let nonGap = 0;
            for (let r = 0; r < n; ++r) {
                let row = rows[r];
                let ch = (row && c < row.length) ? row.charAt(c) : '-';
                if (forester.isMsaGap(ch)) {
                    continue;
                }
                ch = ch.toUpperCase();
                ++nonGap;
                counts[ch] = (counts[ch] || 0) + 1;
            }
            let best = null;
            let bestCount = 0;
            Object.keys(counts).sort().forEach(function (ch) {
                if (counts[ch] > bestCount) {
                    bestCount = counts[ch];
                    best = ch;
                }
            });
            consensus[c] = best;
            if (n < 1 || nonGap < 1) {
                scores[c] = 0;
            } else if (measure === 'information') {
                let H = 0;
                Object.keys(counts).forEach(function (ch) {
                    let p = counts[ch] / nonGap;
                    H -= p * (Math.log(p) / Math.LN2);
                });
                let info = (log2K - H) / log2K;
                scores[c] = Math.max(0, info) * (nonGap / n);
            } else {
                scores[c] = bestCount / n;
            }
        }
        return {scores: scores, consensus: consensus};
    };

    // Per-column sequence logo over the given rows -- the stack of letters
    // whose total height is the column's information content in BITS and whose
    // shares are the residue frequencies (Schneider & Stephens 1990), which is
    // the display the MEME Suite and WebLogo draw.
    //
    // No small-sample correction. Schneider's e_n = (K-1)/(2 ln2 n) is meant
    // for a motif sampled from many sequences; here the rows are the tips
    // currently on screen, so n is routinely 3 or 4 -- and for protein at n=3
    // the correction (4.57 bits) EXCEEDS the maximum (4.32), so a perfectly
    // conserved column of a small clade would draw nothing at all, exactly
    // when the reader has asked about that clade. The caller names n instead.
    //
    // Gaps are not a letter: frequencies are taken over the non-gap residues,
    // and the stack is then scaled by the column's occupancy, so a column held
    // up by two residues out of fifty is short rather than perfectly conserved.
    // `bits` and `occupancy` are returned separately so a caller can say which
    // of the two made a column short.
    //
    // Letters come back most frequent FIRST, ties broken alphabetically, so a
    // figure is reproducible rather than at the mercy of key order.
    forester.msaLogo = function (rows, length, nucleotide) {
        let maxBits = Math.log(nucleotide ? 4 : 20) / Math.LN2;
        if (!(length >= 0) || !isFinite(length)) {
            return {columns: [], maxBits: maxBits};
        }
        length = Math.floor(length);
        let n = rows.length;
        let columns = new Array(length);
        for (let c = 0; c < length; ++c) {
            let counts = {};
            let nonGap = 0;
            for (let r = 0; r < n; ++r) {
                let row = rows[r];
                let ch = (row && c < row.length) ? row.charAt(c) : '-';
                if (forester.isMsaGap(ch)) {
                    continue;
                }
                ch = ch.toUpperCase();
                ++nonGap;
                counts[ch] = (counts[ch] || 0) + 1;
            }
            if (n < 1 || nonGap < 1) {
                columns[c] = {bits: 0, occupancy: 0, height: 0, letters: []};
                continue;
            }
            let H = 0;
            Object.keys(counts).forEach(function (ch) {
                let p = counts[ch] / nonGap;
                H -= p * (Math.log(p) / Math.LN2);
            });
            let bits = Math.max(0, maxBits - H);
            let occupancy = nonGap / n;
            let height = bits * occupancy;
            let letters = Object.keys(counts).sort().sort(function (a, b) {
                return counts[b] - counts[a];   // count first, alphabetical within a tie
            }).map(function (ch) {
                let p = counts[ch] / nonGap;
                return {ch: ch, p: p, h: p * height};
            });
            columns[c] = {bits: bits, occupancy: occupancy, height: height, letters: letters};
        }
        return {columns: columns, maxBits: maxBits};
    };

    // The hover readout's description of one residue: full name, class (amino
    // acids), Kyte-Doolittle hydropathy. Returns null for a gap.
    forester.msaResidueInfo = function (ch, nucleotide) {
        if (ch === undefined || ch === null || forester.isMsaGap(ch)) {
            return null;
        }
        let u = ch.toUpperCase();
        if (nucleotide) {
            return {
                name: MSA_NT_NAMES[u] || 'ambiguity code',
                clazz: MSA_NT_CLASS[u] || null,
                hydropathy: null
            };
        }
        let clazz = MSA_AA_CLASS[u] || 'non-standard / ambiguity code';
        if (u === 'C') {
            clazz = 'cysteine (disulphide-forming)';
        }
        return {
            name: MSA_AA_NAMES[u] || 'ambiguity code',
            clazz: clazz,
            hydropathy: (MSA_HYDROPATHY[u] !== undefined) ? MSA_HYDROPATHY[u] : null
        };
    };

    // The residue's 1-based position within its own UNGAPPED sequence -- the
    // coordinate that maps back onto the real molecule -- or null on a gap.
    forester.msaUngappedPosition = function (row, col) {
        if (!row || col < 0 || col >= row.length || forester.isMsaGap(row.charAt(col))) {
            return null;
        }
        let pos = 0;
        for (let i = 0; i <= col; ++i) {
            if (!forester.isMsaGap(row.charAt(i))) {
                ++pos;
            }
        }
        return pos;
    };


    // --------------------------------------------------------------
    // Unrooted (equal-angle) layout
    // --------------------------------------------------------------
    // The desktop's unrooted display: Meacham's equal-angle algorithm, one
    // pass, no daylight iterations. The root sits at (0,0) and owns the full
    // circle [startAngle, startAngle + 2*pi); each child receives a wedge of
    // its parent's proportional to the external nodes it encloses and sits at
    // its wedge's mid-angle, at the distance lengthOf(child) gives (branch
    // length times the caller's scale factor, or a constant for a cladogram).
    // Angles are ABSOLUTE screen angles (radians, y down), inherited and
    // subdivided -- never re-referenced to the incoming branch, whose
    // direction is implicitly each wedge's midpoint.
    //
    // Writes ux, uy (position) and uangle (the incoming spoke's screen angle)
    // onto every node; returns {maxRad}, the largest distance from the root.
    forester.equalAngleLayout = function (root, startAngle, lengthOf) {
        let counts = new Map();
        let countExt = function (n) {
            let c;
            if (!n.children || n.children.length < 1) {
                c = 1;
            } else {
                c = 0;
                for (let i = 0; i < n.children.length; ++i) {
                    c += countExt(n.children[i]);
                }
            }
            counts.set(n, c);
            return c;
        };
        countExt(root);
        let maxRad = 0;
        root.ux = 0;
        root.uy = 0;
        root.uangle = startAngle;
        let recurse = function (n, low, high) {
            if (!n.children || n.children.length < 1) {
                return;
            }
            let total = counts.get(n);
            let current = low;
            for (let i = 0; i < n.children.length; ++i) {
                let desc = n.children[i];
                let arc = (counts.get(desc) / total) * (high - low);
                let mid = current + (arc / 2);
                let len = lengthOf(desc);
                desc.ux = n.ux + (Math.cos(mid) * len);
                desc.uy = n.uy + (Math.sin(mid) * len);
                desc.uangle = mid;
                let r = Math.sqrt((desc.ux * desc.ux) + (desc.uy * desc.uy));
                if (r > maxRad) {
                    maxRad = r;
                }
                recurse(desc, current, current + arc);
                current += arc;
            }
        };
        recurse(root, startAngle, startAngle + (2 * Math.PI));
        return {maxRad: maxRad};
    };


    // --------------------------------------------------------------
    // Label-field suggestion
    // --------------------------------------------------------------
    // Decides which of the three label groups (node name, taxonomy, sequence)
    // should START checked when a tree is loaded. Two rules, judged on the
    // external nodes' actual label text (supplied by the caller's extractors,
    // so the decision is made on exactly what would be printed):
    //
    // 1. Redundancy: a field whose text is CONTAINED in another checked
    //    field's text (case-insensitively, ignoring spaces and underscores)
    //    for at least 90% of the nodes carrying both adds nothing and starts
    //    unchecked. On mutual containment the earlier field in the priority
    //    order name > taxonomy > sequence is kept.
    //
    // 2. Length budget: if the median combined label of the remaining fields
    //    still exceeds 50 characters, only the single most IDENTIFYING field
    //    stays: the one with the highest ratio of distinct values to ALL
    //    external nodes (a label's job is telling nodes apart -- and a field
    //    carried by only a few tips cannot identify the rest, however unique
    //    its few values are). Within a tie (0.05) the priority order wins,
    //    unless a later field is SUBSTANTIALLY more economical (median length
    //    under 60% of the leader's).
    //
    // A field with no values at all is never checked. This only decides the
    // INITIAL state; the caller's UI stays free to override.
    //
    // extractors: {name, taxonomy, sequence}, each a function(externalNode)
    // returning the text that field would contribute to the node's label, or
    // null / '' when it contributes nothing.
    /**
     * Reads bare numeric internal node labels as confidence values -- the
     * dialect nearly every tree writer emits (RAxML, IQ-TREE, FastTree, MEGA,
     * BV-BRC) writes branch support as `)100:0.05`, which the New Hampshire
     * grammar makes an internal node NAME.
     *
     * The rule is shared with the desktop Java Archaeopteryx; change it only
     * together with that implementation.
     *
     * mode 'auto' (the default) is ALL-OR-NOTHING: every candidate label must
     * look like support, so a tree of real clade names is never touched.
     * mode 'confidence' is PER-NODE and unbounded: every numeric label is
     * promoted whatever its value, which is how a user forces a mixed tree
     * (some clade names, some support) or an out-of-range scale. mode 'label'
     * does nothing -- the labels stay names.
     *
     * The ROOT is never promoted in any mode: a confidence belongs to the
     * branch ABOVE a node and the root has none, so a numeric label there is
     * almost always the tree's name -- `)99;` would otherwise lose it while
     * `)MyTree;` kept it.
     *
     * Promotion MOVES: the label is cleared as the confidence is added, or the
     * value would be drawn twice.
     *
     * Pure apart from the mutation it is asked to make, and returns the number
     * of labels promoted so the CALLER can react (the viewer switches its
     * confidence display on, so support values appear rather than names
     * silently vanishing). Deliberately does not touch display state itself.
     *
     * @param phy - the phylogeny
     * @param mode - 'auto' | 'confidence' | 'label'
     * @returns {number} how many internal labels became confidences
     */
    forester.promoteInternalLabelsToConfidence = function (phy, mode) {
        if (mode === 'label') {
            return 0;
        }
        if (mode !== 'confidence') {
            mode = 'auto';
        }
        let root = forester.getTreeRoot(phy);
        let candidates = [];
        let anyConfidence = false;
        forester.preOrderTraversalAll(root, function (n) {
            if (n === root || !(n.children)) {
                return;
            }
            if (n.confidences && n.confidences.length > 0) {
                anyConfidence = true;
            }
            if (typeof n.name === 'string' && n.name.length > 0) {
                candidates.push(n);
            }
        });
        if (candidates.length < 1) {
            return 0;
        }
        let numeric = candidates.map(function (n) {
            let v = Number(n.name);
            return (n.name.trim().length > 0 && isFinite(v)) ? v : null;
        });
        let chosen;
        if (mode === 'confidence') {
            // Per node: promote what parses, leave the rest alone.
            chosen = candidates.filter(function (n, i) {
                return numeric[i] !== null;
            });
        } else {
            // A file mixing `)[100]:` and `)95:` leaves the bracketed nodes
            // with no label at all, so one bare numeric label beside real
            // confidences is corroborated rather than lonely. Without this a
            // mixed-dialect tree ships some confidences and one stray name.
            let need = anyConfidence ? 1 : 2;
            if (candidates.length < need) {
                return 0;
            }
            if (numeric.some(function (v) {
                // All-or-nothing: one real clade name and the tree is names.
                // The range covers every scale in use -- 0-1 posteriors,
                // 0-100 bootstrap, 0-1000 MrBayes -- and excludes identifiers
                // such as NCBI taxids that would read as "support 9606".
                return v === null || v < 0 || v > 1000;
            })) {
                return 0;
            }
            // Clade numbering is overwhelmingly 1..N, each exactly once. A
            // real support tree containing every integer 1..N once and nothing
            // else is essentially impossible at n >= 3, and without this guard
            // a clade-numbered tree silently becomes "1%, 2%, 3% support".
            let distinct = new Set(numeric);
            let min = Math.min.apply(null, numeric);
            let max = Math.max.apply(null, numeric);
            if (distinct.size === numeric.length && min === 1 && max === numeric.length) {
                return 0;
            }
            chosen = candidates;
        }
        chosen.forEach(function (n) {
            let v = Number(n.name);
            // Type deliberately left unset: the value cannot distinguish
            // bootstrap from posterior from aLRT from TBE, and a wrong type
            // printed beside a number in a published figure is worse than none.
            pushConfidence(n, v, '');
            n.name = '';
        });
        return chosen.length;
    };

    forester.suggestLabelFields = function (root, extractors) {
        const FIELDS = ['name', 'taxonomy', 'sequence'];
        const CONTAINMENT_MIN = 0.9;
        const BUDGET_CHARS = 50;
        const MIN_PAIRS = 2;
        const TIE_EPSILON = 0.05;
        const LENGTH_ADVANTAGE = 0.6;
        let ext = forester.getAllExternalNodes(root);
        let norm = function (s) {
            return s.toLowerCase().replace(/[\s_]+/g, '');
        };
        let median = function (arr) {
            if (arr.length < 1) {
                return 0;
            }
            let a = arr.slice().sort(function (x, y) {
                return x - y;
            });
            let m = a.length >> 1;
            return (a.length % 2) ? a[m] : (a[m - 1] + a[m]) / 2;
        };
        let vals = {};
        FIELDS.forEach(function (f) {
            let get = extractors ? extractors[f] : null;
            vals[f] = ext.map(function (n) {
                let v = get ? get(n) : null;
                v = (v === undefined || v === null) ? '' : String(v).trim();
                return v.length > 0 ? v : null;
            });
        });
        let stats = {};
        FIELDS.forEach(function (f) {
            let present = vals[f].filter(Boolean);
            stats[f] = {
                count: present.length,
                medianLength: median(present.map(function (s) {
                    return s.length;
                })),
                distinctRatio: (present.length > 0 && ext.length > 0)
                    ? (new Set(present.map(norm)).size / ext.length) : 0
            };
        });
        let checked = {};
        FIELDS.forEach(function (f) {
            checked[f] = stats[f].count > 0;
        });

        let containedFrac = function (inner, outer) {
            let both = 0;
            let contained = 0;
            for (let i = 0; i < ext.length; ++i) {
                let a = vals[outer][i];
                let b = vals[inner][i];
                if (a && b) {
                    ++both;
                    if (norm(a).indexOf(norm(b)) >= 0) {
                        ++contained;
                    }
                }
            }
            return both >= MIN_PAIRS ? contained / both : 0;
        };
        for (let i = 0; i < FIELDS.length; ++i) {
            for (let j = i + 1; j < FIELDS.length; ++j) {
                let a = FIELDS[i];
                let b = FIELDS[j];
                if (!checked[a] || !checked[b]) {
                    continue;
                }
                // on mutual containment this still drops b: a is earlier in
                // the keep-priority order
                if (containedFrac(b, a) >= CONTAINMENT_MIN) {
                    checked[b] = false;
                } else if (containedFrac(a, b) >= CONTAINMENT_MIN) {
                    checked[a] = false;
                }
            }
        }

        let combined = [];
        for (let i = 0; i < ext.length; ++i) {
            let parts = [];
            FIELDS.forEach(function (f) {
                if (checked[f] && vals[f][i]) {
                    parts.push(vals[f][i]);
                }
            });
            if (parts.length > 0) {
                combined.push(parts.join(' | ').length);
            }
        }
        let medianCombined = median(combined);
        let kept = FIELDS.filter(function (f) {
            return checked[f];
        });
        if (medianCombined > BUDGET_CHARS && kept.length > 1) {
            // top distinct-ratio first (a greedy pairwise chain is not
            // transitive within the tie window); everything within the tie
            // window then competes by priority order, with the economy rule
            // as the only override
            let maxDr = 0;
            kept.forEach(function (f) {
                if (stats[f].distinctRatio > maxDr) {
                    maxDr = stats[f].distinctRatio;
                }
            });
            let contenders = kept.filter(function (f) {
                return stats[f].distinctRatio >= maxDr - TIE_EPSILON;
            });
            let best = contenders[0];
            for (let k = 1; k < contenders.length; ++k) {
                if (stats[contenders[k]].medianLength < LENGTH_ADVANTAGE * stats[best].medianLength) {
                    best = contenders[k];
                }
            }
            FIELDS.forEach(function (f) {
                checked[f] = (f === best);
            });
        }
        return {
            showNodeName: checked.name,
            showTaxonomy: checked.taxonomy,
            showSequence: checked.sequence,
            stats: {fields: stats, medianCombinedLength: medianCombined}
        };
    };


    // --------------------------------------------------------------
    // For exporting
    // --------------------------------------------------------------
    if (typeof module !== 'undefined' && module.exports && !global.xmldocAssumeBrowser) module.exports.forester = forester; else if (typeof window !== "undefined") window.forester = forester; else this.forester = forester;
})();
    