import {Button, Paper, Stack, Tooltip, Typography} from "@mui/material";
import {Delete, DeleteForever, Download, DownloadTwoTone} from "@mui/icons-material";
import getMessage from "../lang";
import {useContext, useState} from "react";
import {AxiosContext} from "../../utils/axiosInstance";
import {toast} from "react-toastify";
import {useConfirmDialog} from "../../utils/ConfirmDialogContext";

export default function TestcasesEditor({taskId, loadTask, testcases, setTestcases, changePublic}) {
    const [inputFile, setInputFile] = useState(null);
    const [outputFile, setOutputFile] = useState(null);
    const [multipleTestcasesFile, setMultipleTestcasesFile] = useState(null);
    const [selectedTestcases, setSelectedTestcases] = useState([]);

    const axiosInstance = useContext(AxiosContext)
    const { showConfirmDialog } = useConfirmDialog()

    const downloadTestcases = () => {
        axiosInstance.get(`/task/${taskId}/testcases`, {responseType: 'blob'})
            .then(downloadFile)
    }

    const downloadTestcase = (key) => {
        axiosInstance.get(`/task/${taskId}/testcase/${key}`, {responseType: 'blob'})
            .then(downloadFile)
    }

    const downloadFile = (response) => {
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
    }

    const handleDeleteTestcase = (key) => {
        axiosInstance.delete(`/task/${taskId}/testcase/${key}`)
            .then((response) => {
                if (response.status === 200) {
                    toast.success(getMessage('ka', 'testcaseDeleted'));
                    setTestcases(testcases.filter((testcase) => testcase.key !== key));
                }
            })
    }

    const handleRemoveTestcases = (testKeys) => {
        if (!testKeys || testKeys.length === 0) {
            return;
        }
        
        axiosInstance.delete(`/task/${taskId}/testcases`, {
            data: { testKeys: testKeys }
        })
            .then((response) => {
                if (response.status === 200) {
                    const count = testKeys.length;
                    toast.success(getMessage('ka', 'testcaseDeleted') + (count > 1 ? ` (${count})` : ''));
                    setTestcases(testcases.filter((testcase) => !testKeys.includes(testcase.key)));
                    setSelectedTestcases([]);
                    loadTask();
                }
            })
            .catch((error) => {
                toast.error(getMessage('ka', 'error') || 'Failed to delete testcases');
            })
    }

    const handleToggleSelection = (key) => {
        setSelectedTestcases(prev => {
            if (prev.includes(key)) {
                return prev.filter(k => k !== key);
            } else {
                return [...prev, key];
            }
        });
    }

    const handleAddSingleTestcase = () => {
        axiosInstance.post(
            `/task/${taskId}/testcase`,
            getSingleTestcasePostData(),
        ).then((response) => {
            if (response.status === 200) {
                toast.success(getMessage('ka', 'addedTestcase')
                    + ': '
                    + response.data.result.success.reduce((acc, cur) => acc + (acc === '' ? '' : ', ') + cur, ''));
                if (response.data.result.unmatched.length > 0) {
                    toast.warn(getMessage('ka', 'failedToAddTestcase')
                        + ': '
                        + response.data.result.unmatched.reduce((acc, cur) => acc + (acc === '' ? '' : ', ') + cur, ''));
                }
                setMultipleTestcasesFile(null)
                loadTask()
            }
        })
    }

    const getSingleTestcasePostData = () => {
        const formData = new FormData();
        formData.append('taskId', taskId);
        formData.append('inputFile', inputFile);
        formData.append('outputFile', outputFile);
        return formData;
    }

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
                setMultipleTestcasesFile(null)
                loadTask()
            }
        })
    }

    const getMultipleTestcasesPostData = () => {
        const formData = new FormData();
        formData.append('taskId', taskId);
        formData.append('file', multipleTestcasesFile);
        return formData;
    }

    const [expandedIndex, setExpandedIndex] = useState(null);

    const handleRowClick = (e, idx) => {
        if (
            e.target.tagName === "BUTTON" ||
            e.target.tagName === "INPUT" ||
            e.target.closest("button") ||
            e.target.closest("input")
        ) return;
        setExpandedIndex(expandedIndex === idx ? null : idx);
    };

    if (!!taskId) {
        return (<Stack gap="1rem">
            <Typography align="center" variant="h6">
                {getMessage("ka", "testcases")}
            </Typography>
            <Stack direction="row" justifyContent="space-between" alignItems="center"
                   sx={{px: '1rem', py: '0.5rem', backgroundColor: '#f5f5f5', borderRadius: '4px'}}>
                <Stack sx={{display: 'flex', alignItems: 'center', width: '5%'}}>
                    <input 
                        type="checkbox" 
                        checked={testcases.length > 0 && selectedTestcases.length === testcases.length}
                        onChange={(e) => {
                            if (e.target.checked) {
                                setSelectedTestcases(testcases.map(tc => tc.key));
                            } else {
                                setSelectedTestcases([]);
                            }
                        }}
                    />
                </Stack>
                <Typography sx={{width: '5%'}}>#</Typography>
                <Typography sx={{width: '55%'}}>{getMessage("ka", "name")}</Typography>
                <Typography sx={{width: '10%'}}>{getMessage("ka", "public")}</Typography>
                <Stack direction="row" gap="0.5rem" sx={{width: '25%', justifyContent: 'flex-end'}}>
                    <Tooltip title={getMessage('ka', 'downloadTestcases')}>
                        <Button
                            variant="contained"
                            color="primary"
                            onClick={downloadTestcases}
                        >
                            <DownloadTwoTone/>
                        </Button>
                    </Tooltip>
                    <Tooltip title={getMessage('ka', 'deleteTestcases')}>
                        <Button
                            variant="contained"
                            color="error"
                            onClick={() => {
                                if (testcases.length > 0) {
                                    showConfirmDialog({
                                        message: getMessage('ka', 'confirmDeleteAll') || 'Are you sure you want to delete all testcases?',
                                        type: 'warning',
                                        onConfirm: () => {
                                            handleRemoveTestcases(testcases.map(tc => tc.key));
                                        }
                                    });
                                }
                            }}
                        >
                            <DeleteForever/>
                        </Button>
                    </Tooltip>
                </Stack>
            </Stack>
            <Paper elevation={4} sx={{py: '1rem', marginBottom: '0.5rem'}} key={`testcases`}>
                {testcases?.map((testcase, index) => (
                    <Stack direction={"column"}>
                        <Stack key={testcase.key}
                               onClick={(e) => handleRowClick(e, index)}
                               direction="row"
                               justifyContent="space-between"
                               alignItems="center"
                               sx={{
                                   py: 1,
                                   '&:hover': {
                                       backgroundColor: 'rgba(0, 0, 0, 0.1)', // Adjust the color as needed
                                       borderRadius: '4px',
                                   }
                               }}>
                            <Stack sx={{display: 'flex', alignItems: 'center', width: '5%', ml: '1rem'}}>
                                <input 
                                    type="checkbox" 
                                    checked={selectedTestcases.includes(testcase.key)}
                                    onChange={(e) => {
                                        e.stopPropagation();
                                        handleToggleSelection(testcase.key);
                                    }}
                                    onClick={(e) => e.stopPropagation()}
                                />
                            </Stack>
                            <Typography sx={{display: 'flex', alignItems: 'center', width: '75%'}}>
                        <span style={{
                            width: '20%',
                            fontWeight: 700,
                            display: 'inline-block',
                            whiteSpace: 'nowrap'
                        }}>#{index + 1} test:</span>
                                <span
                                    style={{
                                        width: '80%',
                                        display: 'inline-block',
                                        whiteSpace: 'nowrap'
                                    }}>{testcase.key}</span>
                            </Typography>
                            <Stack direction="row" gap="0.5rem" sx={{width: '20%', justifyContent: 'flex-end'}}>
                                <Stack sx={{display: 'flex', alignItems: 'center', width: '40%', ml: '1rem'}}>
                                    <input type="checkbox" checked={testcase.isPublic}
                                           onChange={() => changePublic(testcase.key, !testcase.isPublic)}
                                           onClick={(e) => e.stopPropagation()}/>
                                </Stack>

                                <Button
                                    variant="contained"
                                    color="info"
                                    onClick={() => downloadTestcase(testcase.key)}
                                >
                                    <Download/>
                                </Button>
                                <Button
                                    variant="contained"
                                    color="error"
                                    onClick={() => handleDeleteTestcase(testcase.key)}
                                    sx={{mr: '1rem'}}
                                >
                                    <Delete/>
                                </Button>
                            </Stack>
                        </Stack>
                        {expandedIndex === index && (
                            <Stack sx={{px: '2rem', py: '0.5rem', background: '#f9f9f9'}}>
                                <Typography variant="body2" sx={{mb: 1}}>{getMessage('ka', 'input')}</Typography>
                                <textarea
                                    value={testcase.inputSnippet || ""}
                                    readOnly
                                    style={{width: '100%', minHeight: '60px', marginBottom: '1rem'}}
                                />
                                <Typography variant="body2" sx={{mb: 1}}>{getMessage('ka', 'output')}</Typography>
                                <textarea
                                    value={testcase.outputSnippet || ""}
                                    readOnly
                                    style={{width: '100%', minHeight: '60px'}}
                                />
                            </Stack>
                        )}
                    </Stack>
                ))}
                <Button
                    variant="contained"
                    color="error"
                    onClick={() => {
                        if (selectedTestcases.length > 0) {
                            handleRemoveTestcases(selectedTestcases);
                        }
                    }}
                    disabled={selectedTestcases.length === 0}
                    sx={{ml: '1rem'}}
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
                        {inputFile !== null && (
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
                        {outputFile !== null && (
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
                    <Stack direction="column">
                        <Button fullWidth variant="contained" component="label">
                            {getMessage('ka', 'uploadArchive')}
                            <input type="file" hidden
                                   onChange={(e) => setMultipleTestcasesFile(e.target.files[0])}/>
                        </Button>
                        {multipleTestcasesFile !== null && (
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
}