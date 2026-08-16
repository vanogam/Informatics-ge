import Typography from "@mui/material/Typography";
import Box from "@mui/material/Box";
import Paper from "@mui/material/Paper";
import {useState} from "react";
import getMessage from "../Components/lang";
import {testColor, testSeverity} from "../styles/testResultColors";
import {roundScore} from "../utils/subtasks";

/**
 * @param subtaskPoints what the enclosing subtask is worth, so a partial score reads in the
 *        statement's own units - "13 / 25" rather than the raw "0.52 / 1" fraction. Absent for
 *        tasks without subtasks, where the test has no point value of its own.
 */
const SubmissionTestResult = ({testcase, subtaskPoints}) => {
    const [expanded, setExpanded] = useState(false);
    return (<Paper
        elevation={4}
        sx={{
            padding: '1rem',
            marginBottom: '1rem',
            userSelect: 'contain',
            WebkitUserSelect: 'contain',
            backgroundColor: testColor(testcase),
        }}
    >
        <Typography sx={{fontSize: '15px'}}>
            #{testcase.testKey}
        </Typography>
        <Box
            sx={{display: 'flex', cursor: 'pointer', justifyContent: 'space-between'}}
            onClick={() => setExpanded(!expanded)}
        >
            <Typography sx={{fontSize: '15px'}}>
                სტატუსი:{' '}
                <span style={{fontWeight: 700}}>
                    {testcase.testStatus}
                </span>
            </Typography>

            {testSeverity(testcase) === 'partial' && (
                <Typography sx={{fontSize: '15px'}}>
                    <span style={{fontWeight: 700}}>
                        {roundScore(testcase.score * (subtaskPoints ?? 1))}
                    </span> / {subtaskPoints ?? 1}
                </Typography>
            )}

            <Typography sx={{fontSize: '15px'}}>
                დრო:{' '}
                <span style={{fontWeight: 700}}>{testcase.time}</span>
            </Typography>
            <Typography sx={{fontSize: '15px'}}>
                მეხსიერება:{' '}
                <span style={{fontWeight: 700}}>
                    {parseInt(testcase.memory / 1000) + 'KB'}
                </span>
            </Typography>
        </Box>
        {expanded && (
            <Box sx={{marginTop: '1rem', userSelect: 'contain', WebkitUserSelect: 'contain'}}>
                <Typography sx={{fontSize: '15px', fontWeight: 700}}>{getMessage('ka', 'inputContent')}:</Typography>
                <Typography
                    component="pre"
                    sx={{
                        fontSize: '14px',
                        backgroundColor: '#f2f2f2',
                        padding: '0.5rem',
                        borderRadius: '4px',
                        overflowX: 'auto',
                    }}
                >
                    {testcase.input}
                </Typography>
                <Typography sx={{
                    fontSize: '15px',
                    fontWeight: 700,
                    marginTop: '1rem'
                }}>{getMessage('ka', 'outputContent')}:</Typography>
                <Typography
                    component="pre"
                    sx={{
                        fontSize: '14px',
                        backgroundColor: '#f2f2f2',
                        padding: '0.5rem',
                        borderRadius: '4px',
                        overflowX: 'auto',
                    }}
                >
                    {testcase.outcome}
                </Typography>
                <Typography sx={{
                    fontSize: '15px',
                    fontWeight: 700,
                    marginTop: '1rem'
                }}>{getMessage('ka', 'correctOutput')}:</Typography>
                <Typography
                    component="pre"
                    sx={{
                        fontSize: '14px',
                        backgroundColor: '#f2f2f2',
                        padding: '0.5rem',
                        borderRadius: '4px',
                        overflowX: 'auto',
                    }}
                >
                    {testcase.correctOutput}
                </Typography>
                {!!testcase.message && (<>
                    <Typography sx={{
                        fontSize: '15px',
                        fontWeight: 700,
                        marginTop: '1rem'
                    }}>{getMessage('ka', 'compilerMessage')}:</Typography>
                    <Typography
                        component="pre"
                        sx={{
                            fontSize: '14px',
                            backgroundColor: '#f2f2f2',
                            padding: '0.5rem',
                            borderRadius: '4px',
                            overflowX: 'auto',
                        }}
                    >
                        {testcase.message}
                    </Typography>
                </>)}
            </Box>
        )}
    </Paper>)
};

export default SubmissionTestResult;