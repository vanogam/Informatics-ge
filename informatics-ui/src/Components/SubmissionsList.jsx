import {useContext, useRef, useState} from 'react'
import Editor from 'react-simple-code-editor'
import {highlight, languages} from 'prismjs/components/prism-core'
import {useNavigate} from 'react-router-dom'

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
import {useEffect} from 'react'
import {AxiosContext} from '../utils/axiosInstance'
import {formatDateTime} from '../utils/dateUtils'
import getMessage from "./lang";
import SubmissionTestResult from "../Pages/SubmissionTestResult";
import SubmissionSubtask from "../Pages/SubmissionSubtask";
import {groupTestcases, roundScore} from "../utils/subtasks";
import RejudgeActions, {useCanRejudge} from "./RejudgeActions";
import {usePagination} from "../utils/usePagination";
import PaginationControls from "./PaginationControls";

const truncateSx = {
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
}

const clickableCellSx = {
    ...truncateSx,
    cursor: 'pointer',
    '&:hover': {textDecoration: 'underline'},
}

export default function SubmissionsList({getEndpoint, title, autoRefresh = true, filters = {}}) {
    const [submissions, setSubmissions] = useState([])
    const [selectedSubmission, setSelectedSubmission] = useState({})
    const [popUp, setPopUp] = useState(false)
    const pagination = usePagination()
    const {pageSize, offset, setTotalCount, resetPage} = pagination
    const axiosInstance = useContext(AxiosContext)
    const canRejudge = useCanRejudge()
    const navigate = useNavigate()
    // Lets an action refresh the list immediately instead of waiting out the poll - and is the
    // only refresh at all on the profile tab, which runs with polling switched off.
    const refetch = useRef(() => {})

    // getEndpoint is recreated every render by the caller, so resolve it to a plain string here -
    // that's what actually identifies "which list", and switching it should jump back to page 1.
    const endpoint = getEndpoint()
    // filters is likewise recreated every render by the caller; serialize it so effects only react
    // to an actual change in its values, not to a new object with the same content.
    const filtersKey = JSON.stringify(filters)

    useEffect(() => {
        resetPage()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [endpoint, filtersKey])

    useEffect(() => {
        const fetchSubmissions = () => {
            // An unset filter is an empty string (so a controlled <Select> always has a value) -
            // sending it as-is would bind to e.g. a Long taskId param and fail with a 400, so only
            // the filters actually chosen are ever forwarded.
            const activeFilters = Object.fromEntries(
                Object.entries(filters).filter(([, filterValue]) => filterValue !== '' && filterValue != null)
            )
            axiosInstance
                .get(endpoint, {
                    params: {
                        offset: offset,
                        limit: pageSize,
                        ...activeFilters,
                    },
                })
                .then((response) => {
                    if (response.status === 200) {
                        const submissionsList = Array.isArray(response.data.submissions)
                            ? response.data.submissions
                            : []
                        setSubmissions(submissionsList)
                        setTotalCount(response.data.totalCount || 0)
                    } else {
                        setSubmissions([])
                        setTotalCount(0)
                    }
                })
        }

        refetch.current = fetchSubmissions
        fetchSubmissions()

        if (autoRefresh) {
            const interval = setInterval(fetchSubmissions, 5000)
            return () => {
                clearInterval(interval)
            }
        }

        return;
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [endpoint, axiosInstance, autoRefresh, offset, pageSize, filtersKey])

    const highlightWithLineNumbers = (input, grammar, language) =>
        highlight(input, grammar, language)
            .split('\n')
            .map((line, i) => `${line}`)
            .join('\n')

    const loadSubmission = (id) => {
        axiosInstance.get(`/submission/${id}`)
            .then((response) => {
                // A 401 is resolved rather than rejected (see axiosInstance), with a null body -
                // treat that the same as a failed load instead of leaving the modal stuck.
                if (!response.data) {
                    setPopUp(false)
                    return
                }
                setSelectedSubmission(response.data)
            })
            .catch(() => {
                setPopUp(false)
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
               status === 'CORRECT' ||
               status === 'SYSTEM_ERROR'
    }
    

    /**
     * Wraps tests in their subtasks when the task is scored that way, and falls back to a plain
     * list otherwise - including when the score parameter no longer matches the number of tests,
     * where a grouping would misrepresent how the submission was actually scored.
     */
    const renderResults = (submission) => {
        const results = submission?.results || [];
        const {grouped, groups} = groupTestcases(
            results, submission?.taskScoreType, submission?.taskScoreParameter, (t) => t.testKey);

        if (!grouped) {
            return results.map((testcase) => (
                <SubmissionTestResult key={testcase.testKey} testcase={testcase} />
            ));
        }
        return groups.map((group, index) => (
            <SubmissionSubtask key={`subtask-${index}`}
                               index={index}
                               points={group.score}
                               testcases={group.testcases}>
                {group.testcases.map((testcase) => (
                    <SubmissionTestResult key={testcase.testKey}
                                          testcase={testcase}
                                          subtaskPoints={group.score} />
                ))}
            </SubmissionSubtask>
        ));
    };

    return (
        <>
            {title ? (
                <Typography
                    sx={{color: '#452c54', fontWeight: 'bold'}}
                    align="center"
                    variant="h6"
                    mb="1rem"
                    mt="1rem"
                >
                    {title}
                </Typography>
            ) : null}
            <Box sx={{width: '100%', minWidth: 0, boxSizing: 'border-box'}}>
                <TableContainer
                    component={Paper}
                    sx={{width: '100%', overflowX: 'visible'}}
                >
                    <Table
                        sx={{width: '100%', tableLayout: 'fixed'}}
                        aria-label="simple table"
                    >
                        <TableHead>
                            <TableRow>
                                <TableCell sx={truncateSx}>ამოცანა</TableCell>
                                <TableCell align="right" sx={{...truncateSx, width: '12%'}}>მომხარებელი</TableCell>
                                <TableCell align="right" sx={{...truncateSx, width: '14%'}}>გაშვების დრო</TableCell>
                                <TableCell align="right" sx={{...truncateSx, width: '8%'}}>ენა</TableCell>
                                <TableCell align="right" sx={{...truncateSx, width: '7%'}}>ქულა</TableCell>
                                <TableCell align="right" sx={{...truncateSx, width: '28%'}}>სტატუსი</TableCell>
                                {canRejudge && <TableCell align="right" sx={{width: '10%'}}/>}
                            </TableRow>
                        </TableHead>

                        <TableBody>
                            {submissions.map((submission) => {
                                const openDetails = () => {
                                    setSelectedSubmission(null)
                                    loadSubmission(submission.id)
                                    setPopUp(true)
                                }
                                return (
                                    <TableRow
                                        key={submission.id}
                                        sx={{
                                            '&:last-child td, &:last-child th': {border: 0},
                                            '&:hover': {backgroundColor: '#eee'},
                                        }}
                                    >
                                        <TableCell
                                            component="th"
                                            scope="row"
                                            title={submission.taskName}
                                            sx={clickableCellSx}
                                            onClick={() => navigate(`/contest/${submission.contestId}/problem/${submission.taskId}`)}
                                        >
                                            {submission.taskName}
                                        </TableCell>
                                        <TableCell
                                            align="right"
                                            title={submission.username}
                                            sx={clickableCellSx}
                                            onClick={() => navigate(`/user/${submission.username}`)}
                                        >
                                            {submission.username}
                                        </TableCell>
                                        <TableCell align="right" sx={clickableCellSx} onClick={openDetails}>
                                            {formatDateTime(submission.submissionTime)}
                                        </TableCell>
                                        <TableCell align="right" sx={clickableCellSx} onClick={openDetails}>
                                            {submission.language}
                                        </TableCell>
                                        <TableCell align="right" sx={clickableCellSx} onClick={openDetails}>
                                            {roundScore(submission.score)}
                                        </TableCell>
                                        <TableCell
                                            align="right"
                                            title={getMessage('ka', `SUBMISSION_STATUS_${submission.status}`, submission.currentTest)}
                                            sx={truncateSx}
                                        >
                                            {getMessage('ka', `SUBMISSION_STATUS_${submission.status}`, submission.currentTest)}
                                        </TableCell>
                                        {canRejudge && (
                                            <TableCell align="right">
                                                <RejudgeActions submission={submission}
                                                                onDone={() => refetch.current()}/>
                                            </TableCell>
                                        )}
                                    </TableRow>
                                )
                            })}
                        </TableBody>
                    </Table>
                </TableContainer>
                <PaginationControls pagination={pagination} />
            </Box>
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
                            გაშვების დრო: {formatDateTime(selectedSubmission.submissionTime)}
                        </Typography>
                    </Paper>
                    <Paper elevation={4} sx={{padding: '1rem', marginBottom: '1rem', userSelect: 'contain', WebkitUserSelect: 'contain'}}>
                        {selectedSubmission.kind === 'OUTPUT' ? (
                            // There is no source to show: the submission is a set of uploaded
                            // answers, and their names are what identifies what was sent.
                            <>
                                <Typography sx={{fontSize: '13px', fontWeight: '400', marginBottom: '0.5rem'}}>
                                    {getMessage('ka', 'submittedOutputs')}
                                </Typography>
                                <pre style={{margin: 0, maxHeight: '20rem', overflowY: 'auto', fontSize: 12}}>
                                    {selectedSubmission.text}
                                </pre>
                            </>
                        ) : (
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
                        )}
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
                                {renderResults(selectedSubmission)}
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

