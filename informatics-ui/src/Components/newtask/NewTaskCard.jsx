import {Button, Container, MenuItem, Paper, Stack, TextField, Typography} from '@mui/material'
import {useContext, useEffect, useState} from 'react'
import {AxiosContext} from '../../utils/axiosInstance'
import getMessage from '../lang'
import {toast} from 'react-toastify'
import {useNavigate, useParams} from 'react-router-dom'
import StatementEditor from "./StatementEditor";
import TestcasesEditor from "./TestcasesEditor";

export default function NewTaskCard() {
    const navigate = useNavigate();
    const {contest_id, taskId} = useParams()
    const pageParams = useParams();
    const [code, setCode] = useState('');
    const [initialTitle, setInitialTitle] = useState('');
    const [title, setTitle] = useState('');
    const [contestId, setContestId] = useState(contest_id);
    const [taskType, setTaskType] = useState('BATCH');
    const [evaluatorType, setEvaluatorType] = useState('TOKEN');
    const [taskScoreType, setTaskScoreType] = useState('SUM');
    const [taskScoreParameter, setTaskScoreParameter] = useState('');
    const [timeLimitMillis, setTimeLimitMillis] = useState('');
    const [memoryLimitMB, setMemoryLimitMB] = useState('');
    const [inputTemplate, setInputTemplate] = useState('');
    const [outputTemplate, setOutputTemplate] = useState('');
    const [kaStatement, setKaStatement] = useState({});
    const [initStatement, setInitStatement] = useState({});
    const [testcases, setTestcases] = useState([])

    const [fieldValidations, setFieldValidations] = useState({
        code: true,
        title: true,
        timeLimitMillis: true,
        memoryLimitMB: true,
        inputTemplate: true,
        outputTemplate: true,
    });

    const axiosInstance = useContext(AxiosContext)
    const taskScoreTypes = ['SUM', 'GROUP_MIN']
    const evaluatorTypes = ['TOKEN', 'DOUBLE_E6', 'DOUBLE_E9', 'LINES', 'YES_NO', 'CUSTOM']
    const taskTypes = ['BATCH']
    useEffect(() => {
        if (taskId) {
            loadTask()
        }
        loadStatement()
    }, [])

    useEffect(() => {
        const handleBeforeUnload = (event) => {
            // Check if `kaStatement` has been modified
            if (JSON.stringify(kaStatement) !== JSON.stringify(initStatement)) {
                event.preventDefault();
                event.returnValue = 'You have unsaved changes. Are you sure you want to leave?'; // Custom warning text
            }
        };

        window.addEventListener('beforeunload', handleBeforeUnload);

        return () => {
            window.removeEventListener('beforeunload', handleBeforeUnload);
        };
    }, [kaStatement]);


    const loadTask = () => {
        axiosInstance.get(`/task/${taskId}`).then((response) => {
            const task = response.data
            setTitle(task.title);
            setInitialTitle(task.title);
            setCode(task.code);
            setContestId(task.contestId);
            setTaskType(task.taskType);
            setTaskScoreType(task.taskScoreType);
            setTaskScoreParameter(task.taskScoreParameter);
            setTimeLimitMillis(task.timeLimitMillis);
            setEvaluatorType(task.checkerType);
            setMemoryLimitMB(task.memoryLimitMB);
            setInputTemplate(task.inputTemplate);
            setOutputTemplate(task.outputTemplate);
            setTestcases(task.testcases);
        })
            .catch(_ => {})
    }
    const validateFields = () => {
        const requiredFields = {
            code,
            title,
            timeLimitMillis,
            memoryLimitMB,
            inputTemplate,
            outputTemplate,
        };

        const validations = {};
        let isValid = true;

        for (const [fieldName, field] of Object.entries(requiredFields)) {
            validations[fieldName] = !!field;
            isValid = isValid && validations[fieldName];
        }

        setFieldValidations(validations);
        return isValid;
    };

    const handleNewTask = () => {
        if (!validateFields()) {
            toast.warn(getMessage('ka', 'missingRequiredFields'));
            return;
        }

        const params = {
            taskId: taskId,
            contestId: contestId,
            code: code.toString(),
            title: title.toString(),
            taskType: taskType.toString(),
            taskScoreType: taskScoreType.toString(),
            taskScoreParameter: parseFloat(taskScoreParameter),
            timeLimitMillis: parseInt(timeLimitMillis),
            checkerType: evaluatorType.toString(),
            memoryLimitMB: parseInt(memoryLimitMB),
            inputTemplate: inputTemplate.toString(),
            outputTemplate: outputTemplate.toString(),
        };

        if (kaStatement !== initStatement) {
            submitStatement(kaStatement);
        }

        axiosInstance
            .post('/task', params)
            .then((res) => {
                toast.success(getMessage('ka', taskId ? 'taskSuccessfullyEdited' : 'taskSuccessfullyAdded'))
                navigate(`/task/${res.data.id}`);
            })
    }

    const loadStatement = () => {
        axiosInstance.get(`/task/${taskId}/statement/KA`)
            .then(response => {
                if (response.status === 200) {
                    setKaStatement(response.data.statement || "");
                    setInitStatement(response.data.statement || "");
                }
            })
            .catch(_ => {});
    }

    const submitStatement = (markdown) => {
        axiosInstance.post(`/task/${taskId}/statement`, {
            statement: JSON.stringify(markdown),
            language: 'KA'
        }).then(response => {
            if (response.status === 200) {
                toast.success(getMessage('ka', 'statementSaved'));
                loadStatement()
            }
        })
    }

    const changePublic = (testKey, status) => {
        axiosInstance.put(`/task/${taskId}/testcases/${testKey}/public`, {
            status: status
        })
            .then(response => {
                if (response.status === 200) {
                    setTestcases(testcases.map(
                        tc => tc.key === testKey ? {...tc, isPublic: !tc.isPublic} : tc
                    ));
                    toast.success(getMessage('ka', 'saved'));
                }
            })
            .catch(_ => {
                toast.error(getMessage('ka', 'unexpectedException'));
            })

    }

    return (
        <Container maxWidth='md'>
            <Paper elevation={4} sx={{padding: '1rem'}}>
                <Typography align='center' variant='h6' mb='1rem'>
                    {taskId ? initialTitle : 'ახალი ამოცანა'}
                </Typography>
                <Stack gap='1rem' maxWidth='md' mx='auto' mb='1rem'>
                    <StatementEditor taskId={taskId}
                                     statement={kaStatement}
                                     setStatement={setKaStatement}
                                     loadStatement={loadStatement}
                                     saveStatement={submitStatement}
                    />
                    <TextField
                        label={getMessage('ka', 'title')}
                        value={title}
                        onChange={(e) => {
                            setTitle(e.target.value);
                            setFieldValidations({...fieldValidations, title: true});
                        }}
                        variant='outlined'
                        size='small'
                        required
                        error={!fieldValidations.title}
                    />
                    <TextField
                        size='small'
                        label={getMessage('ka', 'taskCode')}
                        value={code}
                        onChange={(e) => {
                            setCode(e.target.value);
                            setFieldValidations({...fieldValidations, code: true});
                        }}
                        required
                        variant='outlined'
                        error={!fieldValidations.code}
                    />
                    <TextField
                        select
                        size='small'
                        label={getMessage('ka', 'taskType')}
                        value={taskType}
                        onChange={(e) => setTaskType(e.target.value)}
                        variant='outlined'
                    >
                        {taskTypes.map((option) => (
                            <MenuItem key={option} value={option}>
                                {getMessage('ka', 'TASK_TYPE_' + option)}
                            </MenuItem>
                        ))}
                    </TextField>
                    <TextField
                        select
                        label={getMessage('ka', 'taskScoreType')}
                        value={taskScoreType}
                        onChange={(e) => setTaskScoreType(e.target.value)}
                        variant='outlined'
                        size='small'
                        sx={{minWidth: 'max-content'}}
                    >
                        {taskScoreTypes.map((option) => (
                            <MenuItem key={option} value={option}>
                                {getMessage('ka', 'TASK_SCORE_TYPE_' + option)}
                            </MenuItem>
                        ))}
                    </TextField>
                    <TextField
                        label={getMessage('ka', 'taskScoreParameter')}
                        value={taskScoreParameter}
                        onChange={(e) => setTaskScoreParameter(e.target.value)}
                        variant='outlined'
                        size='small'
                    />
                    <TextField
                        select
                        label={getMessage('ka', 'evaluatorType')}
                        value={evaluatorType}
                        onChange={(e) => setEvaluatorType(e.target.value)}
                        variant='outlined'
                        size='small'
                        sx={{minWidth: 'max-content'}}
                    >
                        {evaluatorTypes.map((option) => (
                            <MenuItem key={option} value={option}>
                                {getMessage('ka', 'EVALUATOR_TYPE_' + option)}
                            </MenuItem>
                        ))}
                    </TextField>
                    <TextField
                        label={getMessage('ka', 'timeLimitMillis')}
                        value={timeLimitMillis}
                        onChange={(e) => {
                            setTimeLimitMillis(e.target.value);
                            setFieldValidations({...fieldValidations, timeLimitMillis: true});
                        }}
                        variant='outlined'
                        size='small'
                        required
                        error={!fieldValidations.timeLimitMillis}
                    />
                    <TextField
                        label={getMessage('ka', 'memoryLimitMB')}
                        value={memoryLimitMB}
                        onChange={(e) => {
                            setMemoryLimitMB(e.target.value);
                            setFieldValidations({...fieldValidations, memoryLimitMB: true});
                        }}
                        variant='outlined'
                        size='small'
                        required
                        error={!fieldValidations.memoryLimitMB}
                    />
                    <TextField
                        label={getMessage('ka', 'inputTemplate')}
                        value={inputTemplate}
                        onChange={(e) => {
                            setInputTemplate(e.target.value);
                            setFieldValidations({...fieldValidations, inputTemplate: true});
                        }}
                        variant='outlined'
                        size='small'
                        required
                        error={!fieldValidations.inputTemplate}
                    />
                    <TextField
                        label={getMessage('ka', 'outputTemplate')}
                        value={outputTemplate}
                        onChange={(e) => {
                            setOutputTemplate(e.target.value);
                            setFieldValidations({...fieldValidations, outputTemplate: true});
                        }}
                        variant='outlined'
                        size='small'
                        required
                        error={!fieldValidations.outputTemplate}
                    />
                </Stack>
                <TestcasesEditor taskId={taskId}
                                 loadTask={loadTask}
                                 testcases={testcases}
                                 setTestcases={setTestcases}
                                 changePublic={changePublic}/>
                <Button
                    fullWidth
                    variant='contained'
                    size='large'
                    sx={{marginTop: '1rem', background: '#3c324e'}}
                    onClick={() => {
                        handleNewTask()
                    }}
                >
                    {getMessage('ka', taskId ? 'edit' : 'addProblem')}
                </Button>
            </Paper>
        </Container>
    )
}
