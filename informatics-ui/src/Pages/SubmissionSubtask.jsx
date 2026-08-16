import Typography from "@mui/material/Typography";
import Box from "@mui/material/Box";
import Paper from "@mui/material/Paper";
import {useState} from "react";
import getMessage from "../Components/lang";
import {worstColor} from "../styles/testResultColors";
import {roundScore} from "../utils/subtasks";

/**
 * One subtask in a submission's results: a header shaped like a test row, wrapping its tests.
 *
 * <p>The header reports the subtask the way GROUP_MIN scores it - the weakest test decides the
 * points, so the score is the minimum, and the colour is the worst test's. Time and memory are
 * the maxima, since those are the figures that would breach a limit. No status is shown: a
 * subtask has no single verdict, which is the whole reason the score is a minimum.
 */
const SubmissionSubtask = ({index, points, testcases, children}) => {
    const [expanded, setExpanded] = useState(false);

    const scores = testcases.map(t => (typeof t.score === 'number' ? t.score : 0));
    const minScore = scores.length > 0 ? Math.min(...scores) : 0;
    const earned = roundScore(minScore * points);
    const maxTime = testcases.reduce((acc, t) => Math.max(acc, t.time || 0), 0);
    const maxMemory = testcases.reduce((acc, t) => Math.max(acc, t.memory || 0), 0);

    return (
        <Paper
            elevation={4}
            sx={{
                padding: '1rem',
                marginBottom: '1rem',
                backgroundColor: worstColor(testcases),
                borderLeft: '4px solid #3c324e',
            }}
        >
            <Box
                sx={{display: 'flex', cursor: 'pointer', justifyContent: 'space-between', alignItems: 'center'}}
                onClick={() => setExpanded(!expanded)}
            >
                <Typography sx={{fontSize: '15px', fontWeight: 700, minWidth: '11rem'}}>
                    {expanded ? '▾' : '▸'} {getMessage('ka', 'subtask')} {index}
                </Typography>
                <Typography sx={{fontSize: '15px'}}>
                    <span style={{fontWeight: 700}}>{earned}</span> / {points}
                </Typography>
                <Typography sx={{fontSize: '15px'}}>
                    დრო: <span style={{fontWeight: 700}}>{maxTime}</span>
                </Typography>
                <Typography sx={{fontSize: '15px'}}>
                    მეხსიერება: <span style={{fontWeight: 700}}>{parseInt(maxMemory / 1000) + 'KB'}</span>
                </Typography>
            </Box>
            {expanded && (
                <Box sx={{marginTop: '1rem'}}>
                    {children}
                </Box>
            )}
        </Paper>
    );
};

export default SubmissionSubtask;