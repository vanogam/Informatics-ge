import {useParams} from 'react-router-dom'
import {Box, MenuItem, TextField, Typography} from '@mui/material'
import React, {useContext, useEffect, useState} from 'react'
import {highlight, languages} from 'prismjs/components/prism-core'
import 'prismjs/components/prism-clike'
import 'prismjs/components/prism-c'
import 'prismjs/components/prism-cpp'
import 'prismjs/components/prism-python'
import 'prismjs/components/prism-java'
import 'prismjs/themes/prism.css'
import '../styles/numbers.css'
import {AxiosContext} from '../utils/axiosInstance'
import getMessage from "../Components/lang";
import ContestNavigationBar from "../Components/ContestNavigationBar";
import {prismGrammarName, solutionLanguageLabel} from "../utils/solutionLanguages";

/**
 * Read-only solution view: a contestant picks the language from a combobox that only ever lists
 * languages a teacher actually uploaded code for. Mirrors ProblemEditorial's "nothing to show"
 * fallback for a direct URL visit when the tab should have been greyed out.
 */
export default function ProblemSolution() {
    const {problem_id} = useParams()
    const axiosInstance = useContext(AxiosContext);
    const [taskOrder, setTaskOrder] = useState(null)
    const [taskTitle, setTaskTitle] = useState('')
    const [solutionLanguages, setSolutionLanguages] = useState(null)
    const [language, setLanguage] = useState('')
    const [code, setCode] = useState('')

    useEffect(() => {
        axiosInstance.get(`/task/${problem_id}`)
            .then((response) => {
                setTaskOrder(response.data.order)
                setTaskTitle(response.data.title)
            })
            .catch(_ => {})

        axiosInstance.get(`/task/${problem_id}/solution/languages`)
            .then((response) => {
                const availableLanguages = response.data.languages || []
                setSolutionLanguages(availableLanguages)
                if (availableLanguages.length > 0) {
                    setLanguage(availableLanguages[0])
                }
            })
            .catch(_ => {})
    }, [problem_id])

    useEffect(() => {
        if (!language) {
            return
        }
        axiosInstance.get(`/task/${problem_id}/solution/${language}`)
            .then((response) => {
                setCode(response.data.code || '')
            })
            .catch(_ => {})
    }, [problem_id, language])

    const heading = [taskOrder ? `${taskOrder}.` : '', taskTitle].filter(Boolean).join(' ').trim();
    const grammar = language ? languages[prismGrammarName(language)] : null;

    return (
        <Box>
            <ContestNavigationBar/>
            <Box sx={{marginLeft: '10%', marginTop: '5%', width: '60%'}}>
                {heading && (
                    <Typography variant='h6' sx={{marginBottom: '1rem'}}>
                        {heading} - {getMessage('ka', 'solution')}
                    </Typography>
                )}
                {solutionLanguages && solutionLanguages.length > 0 ? (
                    <>
                        <TextField
                            select
                            label={getMessage('ka', 'chooseSolutionLanguage')}
                            value={language}
                            onChange={(e) => setLanguage(e.target.value)}
                            variant='outlined'
                            size='small'
                            sx={{minWidth: '12rem', marginBottom: '1rem'}}
                        >
                            {solutionLanguages.map((option) => (
                                <MenuItem key={option} value={option}>
                                    {solutionLanguageLabel(option)}
                                </MenuItem>
                            ))}
                        </TextField>
                        {grammar ? (
                            <pre
                                className="editor"
                                style={{
                                    margin: 0,
                                    padding: '12px',
                                    overflow: 'auto',
                                    fontFamily: '"Fira code", "Fira Mono", monospace',
                                    fontSize: 13,
                                }}
                                dangerouslySetInnerHTML={{__html: highlight(code, grammar, prismGrammarName(language))}}
                            />
                        ) : (
                            <pre style={{margin: 0, padding: '12px', overflow: 'auto', fontSize: 13}}>{code}</pre>
                        )}
                    </>
                ) : (
                    solutionLanguages !== null && (
                        <Typography color='textSecondary'>{getMessage('ka', 'noSolutionAvailable')}</Typography>
                    )
                )}
            </Box>
        </Box>
    );
}
