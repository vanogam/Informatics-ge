import React from 'react'

/**
 * Optional display size appended to an image's alt text, as `![caption|400](url)`.
 *
 * Pasted figures carry no intended size: a screenshot copied out of a PDF viewer is
 * rasterized at that viewer's zoom, so a figure two inches wide in the source arrives as a
 * bitmap several times larger and renders that big. The author is the only one who knows how
 * large it should read, so the statement carries the size.
 *
 * A bare number is pixels, a trailing `%` is a share of the statement column. Anything else
 * is left alone and the image keeps its natural size.
 */
const SIZE_HINT = /^(.*)\|\s*(\d+)(%?)\s*$/;

function parseAlt(alt) {
    const match = SIZE_HINT.exec(alt || '');
    if (!match) {
        return {alt, width: undefined};
    }
    return {alt: match[1].trim(), width: match[2] + (match[3] || 'px')};
}

/**
 * Renders a markdown image, honouring the size hint in its alt text. max-width keeps a hint
 * wider than the column - or an unhinted oversized paste - from overflowing.
 */
// `node` is react-markdown's own AST handle and is not a DOM attribute, so it is dropped here
// rather than spread onto the element.
function MarkdownImage({alt, node, ...props}) {
    const size = parseAlt(alt);
    return <img {...props} alt={size.alt} style={size.width ? {width: size.width} : undefined}/>;
}

/**
 * Shared `components` for every ReactMarkdown in the app, so a statement renders the same in
 * the editor preview as it does on the problem page.
 */
const markdownComponents = {
    img: MarkdownImage,
};

export default markdownComponents;