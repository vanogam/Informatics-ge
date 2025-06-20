import {Button, Container, MenuItem, Paper, Stack, TextField, Typography} from '@mui/material'
import {useContext, useEffect, useState} from 'react'
import {AxiosContext} from '../utils/axiosInstance'
import getMessage from './lang'
import {toast} from 'react-toastify'
import {useNavigate, useParams} from 'react-router-dom'
import MarkdownEditor from './markdownEditor'
import {Delete, Download} from "@mui/icons-material";

export default function NewTaskCard() {
    const navigate = useNavigate();
    const {contest_id, taskId} = useParams()
    const pageParams = useParams();
    const [code, setCode] = useState('');
    const [initialTitle, setInitialTitle] = useState('');
    const [title, setTitle] = useState('');
    const [contestId, setContestId] = useState(contest_id);
    const [taskType, setTaskType] = useState('BATCH');
    const [taskScoreType, setTaskScoreType] = useState('SUM');
    const [taskScoreParameter, setTaskScoreParameter] = useState('');
    const [timeLimitMillis, setTimeLimitMillis] = useState('');
    const [memoryLimitMB, setMemoryLimitMB] = useState('');
    const [inputTemplate, setInputTemplate] = useState('');
    const [outputTemplate, setOutputTemplate] = useState('');
    const [kaStatement, setKaStatement] = useState(null);
    const [fieldValidations, setFieldValidations] = useState({
        code: true,
        title: true,
        timeLimitMillis: true,
        memoryLimitMB: true,
        inputTemplate: true,
        outputTemplate: true,
    });

    const [inputFile, setInputFile] = useState(false);
    const [outputFile, setOutputFile] = useState(false);
    const [multipleTestcasesFile, setMultipleTestcasesFile] = useState(null);

    const [testCases, setTestCases] = useState([])
    const [showStatementEditor, setShowStatementEditor] = useState(false);
    const handleAddSingleTestcase = () => {

    }
    const getMultipleTestcasesPostData = () => {
        const formData = new FormData();
        formData.append('taskId', taskId);
        formData.append('file', multipleTestcasesFile);
        return formData;
    }
    console.log(taskId)
    const handleAddMultipleTestcases = () => {
        axiosInstance.post(
            `/task/${taskId}/testcases`,
            getMultipleTestcasesPostData(),
        ).then((response) => {
            if (response.status === 200) {
                toast.success(getMessage('ka', 'addedTestcases')
                    + ': '
                    + response.data.result.success.reduce((acc, cur) => acc + (acc === '' ? '' : ', ') + cur, ''));
                if (response.data.result.unmatched.length > 0) {
                    toast.warn(getMessage('ka', 'failedToAddTestcases')
                        + ': '
                        + response.data.result.unmatched.reduce((acc, cur) => acc + (acc === '' ? '' : ', ') + cur, ''));
                }
            }
        })
    }
    const axiosInstance = useContext(AxiosContext)
    const taskScoreTypes = ['SUM', 'GROUP_MIN']
    const taskTypes = ['BATCH']
    useEffect(() => {
        if (taskId) {
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
                setMemoryLimitMB(task.memoryLimitMB);
                setInputTemplate(task.inputTemplate);
                setOutputTemplate(task.outputTemplate);
                console.log(task.testCases)
                setTestCases(task.testCases.map((testCase) => testCase.toString()));
            })
        }
        loadStatement()
    }, [])
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
            memoryLimitMB: parseInt(memoryLimitMB),
            inputTemplate: inputTemplate.toString(),
            outputTemplate: outputTemplate.toString(),
        };

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
                    setKaStatement(response.data || "");
                }
            });
    }
    const submitStatement = (markdown) => {
        axiosInstance.post(`/task/${taskId}/statement`, {
            statement: markdown,
            language: 'KA'
        }).then(response => {
            if (response.status === 200) {
                toast.success(getMessage('ka', 'statementSaved'));
                loadStatement()
            }
        })
    }

    const downloadTestcases = () => {
        axiosInstance.get(`/task/${taskId}/testcases`, { responseType: 'blob' })
            .then((response) => {
                if (response.status === 200) {
                    const blob = new Blob([response.data], {type: 'application/octet-stream'});
                    const url = window.URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = response.headers['content-disposition']?.split('filename=')[1].slice(1, -1);
                    document.body.appendChild(a);
                    a.click();
                    a.remove();
                    window.URL.revokeObjectURL(url);
                }
            })
    }

    const downloadTestcase = (key) => {
        axiosInstance.get(`/task/${taskId}/testcase/${key}`, { responseType: 'blob' })
            .then((response) => {
                if (response.status === 200) {
                    const blob = new Blob([response.data], {type: 'application/octet-stream'});
                    const url = window.URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = response.headers['content-disposition']?.split('filename=')[1].slice(1, -1);
                    document.body.appendChild(a);
                    a.click();
                    a.remove();
                    window.URL.revokeObjectURL(url);
                }
            })
    }

    const handleDeleteTestcase = (key) => {
        axiosInstance.delete(`/task/${taskId}/testcase/${key}`)
            .then((response) => {
                if (response.status === 200) {
                    toast.success(getMessage('ka', 'testcaseDeleted'));
                    setTestCases(testCases.filter((testCase) => testCase !== key));
                }
            })
    }

    return (
        <Container maxWidth='md'>
            <Paper elevation={4} sx={{padding: '1rem'}}>
                <Typography align='center' variant='h6' mb='1rem'>
                    {taskId ? initialTitle : 'ახალი ამოცანა'}
                </Typography>
                <Stack gap='1rem' maxWidth='md' mx='auto' mb='1rem'>
                    <Button
                        fullWidth
                        variant='contained'
                        component='label'
                        onClick={() => setShowStatementEditor((prev) => !prev)}
                    >
                        {getMessage('ka', 'kaStatement')}
                    </Button>
                    {showStatementEditor && (
                        <div style={{marginTop: '1rem'}}>
                            <MarkdownEditor value={kaStatement}
                                            onChange={(value) => setKaStatement(value)}
                                            imageUploadAddress={`/task/${taskId}/image`}
                                            imageDownloadFunc={url => `/api/task/${taskId}/image/${url}`}
                                            submitFunc={submitStatement}
                            />
                        </div>
                    )}
                    {/*</Stack>*/}
                    {/*<Stack flexDirection='row' gap='1rem'>*/}
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

                {!!taskId &&
                    (<Stack gap="1rem">
                        <Typography align="center" variant="h6">
                            {getMessage("ka", "testcases")}
                        </Typography>
                        <Paper elevation={4} sx={{py: '1rem', marginBottom: '0.5rem'}} key={`testcases`}>
                            {testCases?.map((testCase, index) => (
                                <Stack direction="row" justifyContent="space-between" alignItems="center"  sx={{ py: 1,
                                    '&:hover': {
                                        backgroundColor: 'rgba(0, 0, 0, 0.1)', // Adjust the color as needed
                                        borderRadius: '4px',
                                    }
                                }}>
                                    <Typography sx={{ml: '1rem'}}>
                                        <span style={{fontWeight: 700}}>#{index + 1} test: </span>
                                        {testCase}
                                    </Typography>
                                    <Stack direction="row" gap="0.5rem" alignItems="center">
                                        <input type="checkbox"/>
                                        <Button
                                            variant="contained"
                                            color="info"
                                            onClick={() => downloadTestcase(testCase)}
                                        >
                                            <Download/>
                                        </Button>
                                        <Button
                                            variant="contained"
                                            color="error"
                                            onClick={() => handleDeleteTestcase(testCase)}
                                            sx = {{mr: '1rem'}}
                                        >
                                            <Delete/>
                                        </Button>
                                    </Stack>
                                </Stack>
                            ))}
                            <Button
                                variant="contained"
                                color="primary"
                                onClick={downloadTestcases}
                                sx={{margin: '1rem'}}
                                >
                                {getMessage('ka', 'downloadTestcases')}
                            </Button>
                            <Button
                                variant="contained"
                                color="error"
                                onClick={() => {}}
                                >
                                {getMessage('ka', 'deleteSelected')}
                            </Button>

                        </Paper>
                        <Stack direction="row" gap="1rem">
                            <Typography align="left" variant="h8">
                                {getMessage("ka", "addSingleTestcase")}
                            </Typography>
                            <Stack direction="row" gap="1rem" sx={{marginLeft: 'auto'}}>
                                <Stack direction="column" gap="0.5rem">
                                    <Button align="right" variant="contained" component="label">
                                        {getMessage('ka', 'uploadInputFile')}
                                        <input type="file" hidden onChange={(e) => setInputFile(e.target.files[0])}/>
                                    </Button>
                                    {inputFile && (
                                        <Typography variant="body2" color="textSecondary">
                                            {getMessage('ka', 'uploadedFile')}: {inputFile.name}
                                        </Typography>
                                    )}
                                </Stack>
                                <Stack direction="column" gap="0.5rem">
                                    <Button fullWidth variant="contained" component="label">
                                        {getMessage('ka', 'uploadOutputFile')}
                                        <input type="file" hidden onChange={(e) => setOutputFile(e.target.files[0])}/>
                                    </Button>
                                    {outputFile && (
                                        <Typography variant="body2" color="textSecondary">
                                            {getMessage('ka', 'uploadedFile')}: {outputFile.name}
                                        </Typography>
                                    )}
                                </Stack>
                                <Stack align="right">
                                    <Button
                                        fullWidth
                                        variant="contained"
                                        sx={{background: '#3c324e'}}
                                        disabled={!inputFile || !outputFile}
                                        onClick={() => handleAddSingleTestcase()}
                                    >
                                        {getMessage('ka', 'add')}
                                    </Button>
                                </Stack>
                            </Stack>
                        </Stack>

                        <Stack direction="row" gap="1rem">
                            <Typography align="center" variant="h8" mb="1rem">
                                {getMessage("ka", "addMultipleTestcases")}
                            </Typography>
                            <Stack direction="row" gap="1rem" sx={{marginLeft: 'auto'}}>
                                <Stack direction={"column"}>
                                    <Button fullWidth variant="contained" component="label">
                                        {getMessage('ka', 'uploadArchive')}
                                        <input type="file" hidden
                                               onChange={(e) => setMultipleTestcasesFile(e.target.files[0])}/>
                                    </Button>
                                    {multipleTestcasesFile && (
                                        <Typography variant="body2" color="textSecondary">
                                            {getMessage('ka', 'uploadedFile')}: {multipleTestcasesFile.name}
                                        </Typography>
                                    )}
                                </Stack>
                                <Stack>
                                    <Button
                                        fullWidth
                                        variant="contained"
                                        sx={{background: '#3c324e'}}
                                        disabled={!multipleTestcasesFile}
                                        onClick={() => handleAddMultipleTestcases()}
                                    >
                                        {getMessage('ka', 'add')}
                                    </Button>
                                </Stack>
                            </Stack>
                        </Stack>
                    </Stack>)
                }
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
