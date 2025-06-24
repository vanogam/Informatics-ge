import {useParams} from 'react-router-dom'
import {Document, Page, pdfjs} from 'react-pdf';
import {
    Typography,
    Box
} from '@mui/material'
import React, {useContext, useState} from 'react'
import Editor from 'react-simple-code-editor'
import {highlight, languages} from 'prismjs/components/prism-core'
import 'prismjs/components/prism-clike'
import 'prismjs/components/prism-c'
import 'prismjs/components/prism-cpp'
import 'prismjs/components/prism-javascript'
import 'prismjs/themes/prism.css'
import '../styles/numbers.css'
import {Button} from '@mui/material'
import {useNavigate} from "react-router-dom";
import {useEffect} from 'react';
import {AxiosContext} from '../utils/axiosInstance'
import ReactMarkdown from "react-markdown";
import remarkMath from "remark-math";
import rehypeMathjax from "rehype-mathjax";

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
    let navigate = useNavigate();

    function submitProblem(code, contest_id, task_id) {
        const body = {
            "contestId": contest_id,
            "taskId": task_id,
            "submissionText": code,
            "language": "CPP"
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
    const [code, setCode] = React.useState(
        `#include <iostream>\nusing namespace std;\nint main()\n{\ncout << "Hello, World!";\nreturn 0; \n}\n`
    )
    const [statement, setStatement] = useState("")
    const [submission, setSubmission] = useState("")
    useEffect(() => {


        axiosInstance.get(`/task/${problem_id}/statement/KA`,
        )
            .then((response) => {
                setStatement(response.data)
            })

    }, [])
    return (
        <Box sx={{
            display: 'flex',
            flexDirection: 'row',
            marginLeft: '10%'
        }}>
            <Box sx={{marginLeft: '2%', marginTop: '5%', width: '80%', maxWidth: 500}}>
                <ReactMarkdown
                    children={statement}
                    remarkPlugins={[remarkMath]}
                    rehypePlugins={[rehypeMathjax]}
                    urlTransform={url => `/api/task/${problem_id}/image/${url}`}
                />
            </Box>

            <Box
                sx={{
                    paddingTop: '50px',
                    '& .MuiTextField-root': {m: 1, width: '50ch'},

                    paddingLeft: '10%',
                    display: 'flex',
                    flexDirection: 'column',

                    fontSize: '20',
                }}
            >
                <p sx={{color: 'purple'}}>შეიყვანე კოდი: </p>
                <Editor
                    value={code}
                    onValueChange={(code) => setCode(code)}
                    highlight={(code) =>
                        highlightWithLineNumbers(code, languages.cpp, 'cpp')
                    }
                    className="editor"
                    textareaId="codeArea"
                    style={{
                        overflowY: 'auto',
                        height: '55vh',
                        fontFamily: '"Fira code", "Fira Mono", monospace',
                        fontSize: 12,
                    }}
                />
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
                    ამოხსნის გაგზავნა
                </Button>


            </Box>

        </Box>
    );


};