import {useContext, useState} from 'react'
import Editor from 'react-simple-code-editor'
import {highlight, languages} from 'prismjs/components/prism-core'

import Box from '@mui/material/Box'
import Modal from '@mui/material/Modal'
import Table from '@mui/material/Table'
import Paper from '@mui/material/Paper'
import TableRow from '@mui/material/TableRow'
import TableHead from '@mui/material/TableHead'
import TableCell from '@mui/material/TableCell'
import TableBody from '@mui/material/TableBody'
import Typography from '@mui/material/Typography'
import TableContainer from '@mui/material/TableContainer'
import {useParams} from 'react-router-dom'
import {useEffect} from 'react'
import {Chip} from '@mui/material'
import {AxiosContext, getAxiosInstance} from '../utils/axiosInstance'
import getMessage from "../Components/lang";
import SubmissionTestResult from "./SubmissionTestResult";

export default function MySubmissions() {
    const [submissions, setSubmissions] = useState([])
    const [selectedSubmission, setSelectedSubmission] = useState({})
    const [popUp, setPopUp] = useState(false)
    const {contest_id} = useParams()
    const axiosInstance = useContext(AxiosContext)

    useEffect(() => {
        axiosInstance
            .get(`/contest/${contest_id}/submissions`)
            .then((response) => {
                if (response.status === 200)
                    setSubmissions(response.data.submissions)
                else return <>NO SUBMISSIONS FOUND</>
            }, [])
        const interval = setInterval(() => {
            axiosInstance
                .get(`/contest/${contest_id}/submissions`)
                .then((response) => {
                    if (response.status === 200)
                        setSubmissions(response.data.submissions)
                    else return <>NO SUBMISSIONS FOUND</>
                })
        }, 5000)

        return () => {
            clearInterval(interval)
        }
    }, [])

    const highlightWithLineNumbers = (input, grammar, language) =>
        highlight(input, grammar, language)
            .split('\n')
            .map((line, i) => `${line}`)
            .join('\n')

    const loadSubmission = (id) => {
        axiosInstance.get(`/submission/${id}`)
            .then((response) => {
                setSelectedSubmission(response.data)
            })
    }

    const isFinished = (status) => {
        return status === 'COMPILATION_ERROR' ||
               status === 'TIME_LIMIT_EXCEEDED' ||
               status === 'MEMORY_LIMIT_EXCEEDED' ||
               status === 'RUNTIME_ERROR' ||
               status === 'WRONG_ANSWER' ||
               status === 'FAILED' ||
               status === 'PARTIAL' ||
               status === 'CORRECT'
    }
    return (
        <>
            <Typography
                sx={{color: '#452c54', fontWeight: 'bold'}}
                align="center"
                variant="h6"
                mb="1rem"
                mt="1rem"
            >
                ჩემი მცდელობები
            </Typography>
            <TableContainer
                component={Paper}
                sx={{maxWidth: '80%', marginInline: 'auto'}}
            >
                <Table sx={{minWidth: 650}} aria-label="simple table">
                    <TableHead>
                        <TableRow>
                            <TableCell>ამოცანა</TableCell>
                            <TableCell align="right">მომხარებელი</TableCell>
                            <TableCell align="right">გაშვების დრო</TableCell>
                            <TableCell align="right">ენა</TableCell>
                            <TableCell align="right">ქულა</TableCell>
                            <TableCell align="right">სტატუსი</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {submissions.map((submission) => (
                            <TableRow
                                onClick={() => {
                                    setSelectedSubmission(null)
                                    loadSubmission(submission.id)
                                    setPopUp(true)
                                }}
                                key={submission.id}
                                sx={{
                                    '&:last-child td, &:last-child th': {border: 0},
                                    cursor: 'pointer',
                                    '&:hover': {backgroundColor: '#eee'},
                                }}
                            >
                                <TableCell component="th" scope="row">
                                    {submission.taskName}
                                </TableCell>
                                <TableCell align="right">{submission.username}</TableCell>
                                <TableCell align="right">{submission.submissionTime}</TableCell>
                                <TableCell align="right">{submission.language}</TableCell>
                                <TableCell align="right">
                                    {submission.score}
                                </TableCell>
                                <TableCell align="right">
                                    {getMessage('ka', `SUBMISSION_STATUS_${submission.status}`, submission.currentTest)}
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
            <Modal open={popUp} onClose={() => setPopUp(false)}>
                {!selectedSubmission ? (
                    <Box
                        sx={{
                            display: 'flex',
                            justifyContent: 'center',
                            alignItems: 'center',
                            height: '100%',
                        }}
                    >
                        <Typography variant="h6">Loading...</Typography>
                    </Box>
                ) : (<Box
                    sx={{
                        position: 'absolute',
                        top: '50%',
                        left: '50%',
                        transform: 'translate(-50%, -50%)',
                        maxHeight: '80%',
                        overflowY: 'auto',
                        width: "55%",
                        bgcolor: 'white',
                        border: `2px solid ;`,
                        borderRadius: '0.5rem',
                        boxShadow: 24,
                        p: 4,
                    }}
                >
                    <Paper elevation={4} sx={{padding: '1rem', marginBottom: '1rem'}}>
                        <Typography sx={{fontSize: '10px', fontWeight: '400'}}>
                            მომხმარებელი: {selectedSubmission.username}
                        </Typography>
                        <Typography sx={{fontSize: '10px', fontWeight: '400'}}>
                            ენა: {selectedSubmission.language}
                        </Typography>
                        <Typography sx={{fontSize: '10px', fontWeight: '400'}}>
                            გაშვების დრო: {selectedSubmission.submissionTime}
                        </Typography>
                    </Paper>
                    <Paper elevation={4} sx={{padding: '1rem', marginBottom: '1rem'}}>
                        <Editor
                            value={selectedSubmission.text}
                            highlight={(code) =>
                                highlightWithLineNumbers(code, languages.cpp, 'cpp')
                            }
                            textareaId="codeArea"
                            style={{
                                overflowY: 'auto',
                                maxHeight: '20rem',
                                fontFamily: '"Fira code", "Fira Mono", monospace',
                                fontSize: 12,
                            }}
                        />
                    </Paper>
                    <Paper elevation={4} sx={{padding: '1rem', marginBottom: '1rem'}}>
                        <Typography sx={{fontSize: '13px', fontWeight: '400'}}>
                            კომპილაციის მესიჯი: {selectedSubmission.compilationMessage}
                        </Typography>
                    </Paper>
                    {(
                        isFinished(selectedSubmission.status) ? (
                            <Paper elevation={4} sx={{padding: '1rem'}}>
                                <Typography align="center" variant="h6" mb="1rem">
                                    {getMessage('ka', 'tests')}
                                </Typography>
                                {selectedSubmission?.results?.map((testcase) => (
                                    <SubmissionTestResult testcase={testcase} />
                                ))}
                            </Paper>
                        ) : (
                            <Paper elevation={4} sx={{padding: '1rem'}}>
                                <Typography>
                                    გაშვებულია: {selectedSubmission.currentTest} ტესტზე
                                </Typography>
                            </Paper>
                        )
                    )}
                </Box>)}
            </Modal>
        </>
    )
}
