import {useParams} from 'react-router-dom'
import {Page, pdfjs} from 'react-pdf';
import {
    Box, TextField, MenuItem, Stack, Typography
} from '@mui/material'
import {Download} from '@mui/icons-material'
import React, {useContext, useState} from 'react'
import Editor from 'react-simple-code-editor'
import {highlight, languages} from 'prismjs/components/prism-core'
import 'prismjs/components/prism-clike'
import 'prismjs/components/prism-c'
import 'prismjs/components/prism-cpp'
import 'prismjs/components/prism-python'
import 'prismjs/components/prism-javascript'
import 'prismjs/themes/prism.css'
import '../styles/numbers.css'
import {Button} from '@mui/material'
import {useEffect} from 'react';
import {AxiosContext} from '../utils/axiosInstance'
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import "../styles/markdown.css";
import remarkMath from "remark-math";
import rehypeMathjax from "rehype-mathjax";
import getMessage from "../Components/lang";
import TableContainer from "@mui/material/TableContainer";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TableCell from "@mui/material/TableCell";
import {Paper} from "@mui/material";
import ContestNavigationBar from "../Components/ContestNavigationBar";
import markdownComponents from "../utils/markdownComponents";

pdfjs.GlobalWorkerOptions.workerSrc = `https://cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjs.version}/pdf.worker.js`;

const boxStyle = {
    display: 'flex',
    flexDirection: 'column',
    '& .MuiTextField-root': {m: 1, width: '25ch'},
    paddingLeft: '30%',
    marginLeft: '10%',
    paddingTop: '5%',
    color: 'purple',
    fontWeight: 'bold',
    fontSize: '20',
}

const highlightWithLineNumbers = (input, grammar, language) =>
    highlight(input, grammar, language)
        .split('\n')
        .map((line, i) => `<span class='editorLineNumber'>${i + 1}</span>${line}`)
        .join('\n')

export default function Problem() {
    const axiosInstance = useContext(AxiosContext);

    function submitProblem(code, contest_id, task_id) {
        const body = {
            "contestId": contest_id,
            "taskId": task_id,
            "submissionText": code[language].code,
            "language": language
        }
        console.log(body)
        axiosInstance
            .post(`/submit`, body)
            .then((response) => {
                if (response.status == 200) {
                    window.location = `/contest/${contest_id}/mySubmissions`
                }
            })
    }

    const {contest_id, problem_id} = useParams()
    const [code, setCode] = React.useState({
        "CPP": {
                      grammar: 'cpp',
            code: `#include <iostream>\nusing namespace std;\nint main()\n{\ncout << "Hello, World!";\nreturn 0; \n}\n`
                },
        "PYTHON": {
            grammar: 'python',
            code: `print("Hello, World!")`
        }
    })
    const [language, setLanguage] = useState("CPP")
    const [statement, setStatement] = useState("")
    const [taskOrder, setTaskOrder] = useState(null)
    const [attachments, setAttachments] = useState([])
    const [limits, setLimits] = useState(null)
    const editorWrapperRef = React.useRef(null)

    useEffect(() => {
        const wrapper = editorWrapperRef.current
        if (!wrapper) return
        const textarea = wrapper.querySelector('#codeArea')
        if (!textarea) return
        const scrollToCursor = () => {
            const lineIndex = textarea.value.substring(0, textarea.selectionStart).split('\n').length - 1
            const computed = getComputedStyle(textarea)
            const lineHeightStr = computed.lineHeight
            const lineHeight = lineHeightStr === 'normal'
                ? parseFloat(computed.fontSize) * 1.2
                : parseFloat(lineHeightStr)
            const cursorTop = lineIndex * lineHeight
            const cursorBottom = cursorTop + lineHeight
            if (cursorTop < wrapper.scrollTop) {
                wrapper.scrollTop = cursorTop
            } else if (cursorBottom > wrapper.scrollTop + wrapper.clientHeight) {
                wrapper.scrollTop = cursorBottom - wrapper.clientHeight
            }
        }
        textarea.addEventListener('keyup', scrollToCursor)
        textarea.addEventListener('click', scrollToCursor)
        return () => {
            textarea.removeEventListener('keyup', scrollToCursor)
            textarea.removeEventListener('click', scrollToCursor)
        }
    }, [])

    const lang = ['CPP', 'PYTHON']
    useEffect(() => {
        // Fetch task details to get the order and the limits shown under the title
        axiosInstance.get(`/task/${problem_id}`)
            .then((response) => {
                setTaskOrder(response.data.order)
                setLimits({
                    timeLimitMillis: response.data.timeLimitMillis,
                    memoryLimitMB: response.data.memoryLimitMB
                })
            })
            .catch(_ => {})

        axiosInstance.get(`/task/${problem_id}/statement/KA`,
        )
            .then((response) => {
                setStatement(response.data)
            })
            .catch(_ => {})

        // Only the files a teacher marked visible come back here; a task with none
        // answers with an empty list and the section stays hidden.
        axiosInstance.get(`/task/${problem_id}/attachments`)
            .then((response) => {
                setAttachments(response.data.files || [])
            })
            .catch(_ => {})

    }, [problem_id])

    // Fetched through axios rather than linked directly: the download is session
    // authenticated and the API may sit on a different host than the UI.
    const downloadAttachment = (fileName) => {
        axiosInstance.get(`/task/${problem_id}/attachment/${fileName}`, {responseType: 'blob'})
            .then((response) => {
                if (response.status !== 200) {
                    return
                }
                const url = window.URL.createObjectURL(new Blob([response.data]))
                const a = document.createElement('a')
                a.href = url
                a.download = fileName
                document.body.appendChild(a)
                a.click()
                a.remove()
                window.URL.revokeObjectURL(url)
            })
    }
    // Built from the parts that exist, so a missing title cannot render as "1. null".
    const statementTitle = !!statement.statement
        ? [taskOrder ? `${taskOrder}.` : '', statement.statement.title || '']
            .filter(Boolean).join(' ').trim()
        : '';
    // A section is only rendered when it has content: an empty field would otherwise leave a
    // heading with nothing under it, and a null one would print the word "null".
    const section = (label, body) => body && body.trim()
        ? `**${getMessage('ka', label)}:**\n\n${body.trim()}\n\n`
        : '';
    // Title and body render separately so the limits can sit between them. Both go through
    // markdown - a title can carry math or formatting just like the body does.
    const statementTitleText = statementTitle ? `**${statementTitle}**` : '';
    const statementText = !!statement.statement
        ? (statement.statement.statement ? `${statement.statement.statement}\n\n` : '')
        + section('inputContent', statement.statement.inputInfo)
        + section('outputContent', statement.statement.outputInfo)
        : '';
    const renderMarkdown = (text) => (
        <ReactMarkdown
            children={text}
            remarkPlugins={[remarkMath, remarkGfm]}
            rehypePlugins={[rehypeMathjax]}
            components={markdownComponents}
            urlTransform={url => `/api/task/${problem_id}/image/${url}`}
        />
    );
    // Seconds read better than milliseconds at contest limits, and trailing zeros are dropped
    // so a 1000ms limit shows as "1" rather than "1.0".
    const formatTimeLimit = (millis) => `${parseFloat((millis / 1000).toFixed(3))}`;
    return (
        <Box>
            <ContestNavigationBar />
            <Box sx={{
                display: 'flex',
                flexDirection: 'row',
                marginLeft: '10%'
            }}>
            <Box sx={{marginLeft: '2%', marginTop: '5%', width: '60%'}}>
                <div className="markdown-body">
                    {statementTitleText && renderMarkdown(statementTitleText)}
                </div>
                {limits && (limits.timeLimitMillis != null || limits.memoryLimitMB != null) && (
                    <Stack direction="row"
                           gap="1.5rem"
                           flexWrap="wrap"
                           sx={{my: '1rem', color: 'text.secondary'}}>
                        {limits.timeLimitMillis != null && (
                            <Typography variant="body2">
                                <b>{getMessage('ka', 'timeLimit')}:</b>{' '}
                                {getMessage('ka', 'seconds', formatTimeLimit(limits.timeLimitMillis))}
                            </Typography>
                        )}
                        {limits.memoryLimitMB != null && (
                            <Typography variant="body2">
                                <b>{getMessage('ka', 'memoryLimit')}:</b> {limits.memoryLimitMB} MB
                            </Typography>
                        )}
                    </Stack>
                )}
                <div className="markdown-body">
                    {renderMarkdown(statementText)}
                </div>
                {statement.publicTestcases && statement.publicTestcases.length > 0 && (
                    <TableContainer component={Paper} sx={{ marginTop: 2, maxWidth: 600 }}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>{getMessage('ka', 'input')}</TableCell>
                                    <TableCell>{getMessage('ka', 'output')}</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {statement.publicTestcases.map((tc, idx) => (
                                    <TableRow key={idx}>
                                        <TableCell sx={{ userSelect: 'contain', WebkitUserSelect: 'contain' }}>
                                            <pre style={{ margin: 0 }}>{tc.inputSnippet}</pre>
                                        </TableCell>
                                        <TableCell sx={{ userSelect: 'contain', WebkitUserSelect: 'contain' }}>
                                            <pre style={{ margin: 0 }}>{tc.outputSnippet}</pre>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )}
            </Box>
            <Box
                sx={{
                    paddingTop: '50px',
                    paddingRight: '64px',
                    paddingLeft: '16px',
                    display: 'flex',
                    flexDirection: 'column',
                    flex: 1,
                    minWidth: 0,
                    fontSize: '20',
                }}
            >
                <p sx={{color: 'purple'}}>შეიყვანე კოდი: </p>
                <div ref={editorWrapperRef} style={{ minHeight: '55vh', maxHeight: '55vh', overflow: 'auto', width: '100%' }}>
                    <Editor
                        value={code[language].code}
                        onValueChange={(change) => setCode({...code, [language]: {...code[language], code: change}})}
                        highlight={(text) =>
                            highlightWithLineNumbers(text, languages[code[language].grammar], code[language].grammar)
                        }
                        className="editor"
                        textareaId="codeArea"
                        style={{
                            width: '100%',
                            minHeight: '55vh',
                            fontFamily: '"Fira code", "Fira Mono", monospace',
                            fontSize: 12,
                        }}
                    />
                </div>
                <TextField
                    select
                    label={getMessage('ka', 'language')}
                    value={language}
                    onChange={(e) => setLanguage(e.target.value)}
                    variant='outlined'
                    size='small'
                    fullWidth
                    sx={{
                        minWidth: 'max-content',
                        marginTop: '10px',
                        marginLeft: 0
                    }}
                >
                    {lang.map((option) => (
                        <MenuItem key={option} value={option}>
                            {getMessage('ka', 'LANG_' + option)}
                        </MenuItem>
                    ))}
                </TextField>
                <Button
                    sx={{
                        marginInline: '2px',
                        alignSelf: 'right',
                        marginLeft: '60px',
                        width: '50%',
                        marginTop: '5%',
                        background: '#3c324e',
                    }}
                    onClick={() => submitProblem(code, contest_id, problem_id)}
                    variant="contained"
                >
                    {getMessage('ka', 'submit')}
                </Button>

                {attachments.length > 0 && (
                    <Box sx={{marginTop: '5%'}}>
                        <Typography variant="subtitle1" sx={{fontWeight: 'bold'}}>
                            {getMessage('ka', 'attachments')}
                        </Typography>
                        <Stack sx={{marginTop: '8px'}}>
                            {attachments.map((file) => (
                                <Stack key={file.fileName}
                                       direction="row"
                                       justifyContent="space-between"
                                       alignItems="center"
                                       gap="8px"
                                       sx={{py: 0.5}}>
                                    <Typography sx={{wordBreak: 'break-all'}}>{file.fileName}</Typography>
                                    <Stack direction="row" alignItems="center" gap="8px">
                                        <Typography variant="body2" color="textSecondary" noWrap>
                                            {Math.max(1, Math.round((file.sizeBytes || 0) / 1024))} KB
                                        </Typography>
                                        <Button size="small"
                                                variant="outlined"
                                                startIcon={<Download/>}
                                                onClick={() => downloadAttachment(file.fileName)}>
                                            {getMessage('ka', 'download')}
                                        </Button>
                                    </Stack>
                                </Stack>
                            ))}
                        </Stack>
                    </Box>
                )}

            </Box>
        </Box>
        </Box>
    );


};