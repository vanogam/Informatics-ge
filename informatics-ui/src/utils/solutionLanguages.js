import getMessage from '../Components/lang'

/**
 * The languages a teacher always has on hand, in the order they should list - any language a
 * teacher types in beyond these is a custom one, named freely and stored as-is.
 */
export const STANDARD_SOLUTION_LANGUAGES = ['CPP', 'JAVA', 'PYTHON']

const PRISM_GRAMMAR_NAMES = {
    CPP: 'cpp',
    JAVA: 'java',
    PYTHON: 'python',
}

/** A standard language gets its proper display name; a custom one is shown exactly as typed. */
export function solutionLanguageLabel(language) {
    return STANDARD_SOLUTION_LANGUAGES.includes(language)
        ? getMessage('ka', `SOLUTION_LANG_${language}`)
        : language
}

/** The prismjs grammar name to try highlighting a language's code with. */
export function prismGrammarName(language) {
    return PRISM_GRAMMAR_NAMES[language] || language.toLowerCase()
}
