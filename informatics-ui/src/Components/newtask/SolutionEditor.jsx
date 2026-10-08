import getMessage from "../lang";
import {
    Box, Button, FormControlLabel, Stack, Switch, Tab, Tabs, TextField, Typography
} from "@mui/material";
import {Delete} from "@mui/icons-material";
import {useContext, useEffect, useState} from "react";
import {highlight, languages} from 'prismjs/components/prism-core'
import 'prismjs/components/prism-clike'
import 'prismjs/components/prism-c'
import 'prismjs/components/prism-cpp'
import 'prismjs/components/prism-python'
import 'prismjs/components/prism-java'
import 'prismjs/themes/prism.css'
import {AxiosContext} from "../../utils/axiosInstance";
import {toast} from "react-toastify";
import {STANDARD_SOLUTION_LANGUAGES, prismGrammarName, solutionLanguageLabel} from "../../utils/solutionLanguages";

/**
 * Teacher-facing editor for a task's per-language solution code. Standard languages (C++, Java,
 * Python) are one click away; anything else is a custom language name the teacher types in.
 * Mirrors EditorialEditor's collapsible shell and visibility switch.
 */
const SolutionEditor = ({taskId, visible, setVisible}) => {
    const [showSolutionEditor, setShowSolutionEditor] = useState(false);
    const [languageKeys, setLanguageKeys] = useState([]);
    const [activeLanguage, setActiveLanguage] = useState(null);
    const [codeByLanguage, setCodeByLanguage] = useState({});
    const [customLanguage, setCustomLanguage] = useState('');
    const axiosInstance = useContext(AxiosContext);

    const loadLanguages = () => {
        axiosInstance.get(`/task/${taskId}/solution/languages`)
            .then(response => {
                if (response.status === 200) {
                    setLanguageKeys(response.data.languages || []);
                }
            });
    };

    useEffect(() => {
        if (taskId && showSolutionEditor) {
            loadLanguages();
        }
    }, [taskId, showSolutionEditor]);

    const openLanguage = (language) => {
        setActiveLanguage(language);
        if (codeByLanguage[language] === undefined) {
            axiosInstance.get(`/task/${taskId}/solution/${language}`)
                .then(response => {
                    setCodeByLanguage(prev => ({...prev, [language]: response.data.code || ''}));
                });
        }
    };

    const addLanguage = (language) => {
        const key = language.trim();
        if (!key || languageKeys.includes(key)) {
            return;
        }
        setLanguageKeys(prev => [...prev, key]);
        setCodeByLanguage(prev => ({...prev, [key]: ''}));
        setActiveLanguage(key);
        setCustomLanguage('');
    };

    const removeLanguage = (language) => {
        axiosInstance.delete(`/task/${taskId}/solution/${language}`)
            .then(response => {
                if (response.status === 200) {
                    setLanguageKeys(prev => prev.filter(key => key !== language));
                    if (activeLanguage === language) {
                        setActiveLanguage(null);
                    }
                    toast.success(getMessage('ka', 'saved'));
                }
            });
    };

    const saveSolution = () => {
        axiosInstance.post(`/task/${taskId}/solution`, {
            code: codeByLanguage[activeLanguage] || '',
            language: activeLanguage,
        }).then(response => {
            if (response.status === 200) {
                toast.success(getMessage('ka', 'solutionSaved'));
                loadLanguages();
            }
        });
    };

    const toggleVisible = (checked) => {
        axiosInstance.put(`/task/${taskId}/solution/visible`, {visible: checked})
            .then(response => {
                if (response.status === 200) {
                    setVisible(checked);
                    toast.success(getMessage('ka', 'saved'));
                }
            });
    };

    const addableStandardLanguages = STANDARD_SOLUTION_LANGUAGES.filter(l => !languageKeys.includes(l));
    const grammar = activeLanguage ? languages[prismGrammarName(activeLanguage)] : null;

    return <Stack gap='1rem' width='100%' mx='auto' mb='1rem'>
        <Button
            fullWidth
            variant='contained'
            component='label'
            onClick={() => setShowSolutionEditor((prev) => !prev)}
        >
            {getMessage("ka", 'solution')}
        </Button>
        {showSolutionEditor && (
            <div style={{marginTop: '1rem'}}>
                <FormControlLabel
                    control={<Switch checked={!!visible} onChange={(e) => toggleVisible(e.target.checked)}/>}
                    label={getMessage('ka', 'showSolution')}
                />
                <Stack direction='row' gap='0.5rem' flexWrap='wrap' sx={{marginBottom: '1rem'}}>
                    {addableStandardLanguages.map(language => (
                        <Button key={language} size='small' variant='outlined' onClick={() => addLanguage(language)}>
                            + {solutionLanguageLabel(language)}
                        </Button>
                    ))}
                    <TextField
                        size='small'
                        placeholder={getMessage('ka', 'customLanguageName')}
                        value={customLanguage}
                        onChange={(e) => setCustomLanguage(e.target.value)}
                    />
                    <Button size='small' variant='outlined' onClick={() => addLanguage(customLanguage)}>
                        + {getMessage('ka', 'customLanguage')}
                    </Button>
                </Stack>

                {languageKeys.length > 0 && (
                    <Tabs
                        value={languageKeys.includes(activeLanguage) ? activeLanguage : false}
                        onChange={(_, value) => openLanguage(value)}
                        sx={{marginBottom: '1rem'}}
                    >
                        {languageKeys.map(language => (
                            <Tab key={language} value={language} label={
                                <Stack direction='row' alignItems='center' gap='4px'>
                                    {solutionLanguageLabel(language)}
                                    <Delete
                                        fontSize='small'
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            removeLanguage(language);
                                        }}
                                    />
                                </Stack>
                            }/>
                        ))}
                    </Tabs>
                )}

                {activeLanguage && (
                    <Box>
                        <textarea
                            style={{width: "100%", height: "16rem", fontSize: "14px", fontFamily: '"Fira code", "Fira Mono", monospace'}}
                            value={codeByLanguage[activeLanguage] || ''}
                            onChange={(e) => setCodeByLanguage(prev => ({...prev, [activeLanguage]: e.target.value}))}
                        />
                        {grammar && codeByLanguage[activeLanguage] && (
                            <Box sx={{marginTop: '0.5rem'}}>
                                <Typography variant='caption' color='textSecondary'>{getMessage('ka', 'preview')}</Typography>
                                <pre
                                    className="language-none"
                                    style={{margin: 0, padding: '8px', background: '#f5f2f0', fontSize: 12, overflow: 'auto'}}
                                    dangerouslySetInnerHTML={{
                                        __html: highlight(codeByLanguage[activeLanguage], grammar, prismGrammarName(activeLanguage))
                                    }}
                                />
                            </Box>
                        )}
                        <Button
                            variant="contained"
                            color="secondary"
                            sx={{backgroundColor: '#2f2d47', marginTop: '0.5rem'}}
                            onClick={saveSolution}
                        >
                            {getMessage('ka', 'save')}
                        </Button>
                    </Box>
                )}
            </div>
        )}
    </Stack>;
}

export default SolutionEditor;
