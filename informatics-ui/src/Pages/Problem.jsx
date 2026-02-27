import {useParams} from 'react-router-dom'
import {Page, pdfjs} from 'react-pdf';
import {
    Box, TextField, MenuItem
} from '@mui/material'
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
        // Fetch task details to get order
        axiosInstance.get(`/task/${problem_id}`)
            .then((response) => {
                setTaskOrder(response.data.order)
            })
            .catch(_ => {})

        axiosInstance.get(`/task/${problem_id}/statement/KA`,
        )
            .then((response) => {
                setStatement(response.data)
            })
            .catch(_ => {})

    }, [problem_id])
    const statementTitle = !!statement.statement 
        ? (taskOrder ? `${taskOrder}. ${statement.statement.title}` : statement.statement.title)
        : '';
    const statementText = !!statement.statement ? `**${statementTitle}**` + '\n\n'
        + statement.statement.statement + '\n\n' +
        `**${getMessage('ka', 'inputContent')}:**\n\n${statement.statement.inputInfo}\n\n` +
        `**${getMessage('ka', 'outputContent')}:**\n\n${statement.statement.outputInfo}\n\n` : '';
    return (
        <Box>
            <ContestNavigationBar />
            <Box sx={{
                display: 'flex',
                flexDirection: 'row',
                marginLeft: '10%'
            }}>
            <Box sx={{marginLeft: '2%', marginTop: '5%', width: '60%'}}>
                <ReactMarkdown
                    children={statementText}
                    remarkPlugins={[remarkMath]}
                    rehypePlugins={[rehypeMathjax]}
                    urlTransform={url => `/api/task/${problem_id}/image/${url}`}
                />
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


            </Box>
        </Box>
        </Box>
    );


};