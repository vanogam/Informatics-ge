import {Button, Paper, Stack, Tooltip, Typography} from "@mui/material";
import {Delete, Download} from "@mui/icons-material";
import getMessage from "../lang";
import {useCallback, useContext, useEffect, useState} from "react";
import {AxiosContext} from "../../utils/axiosInstance";
import {toast} from "react-toastify";

/** Must stay aligned with ge.freeuni.informatics.maxSingleTestcaseFileMb on the server (default 32). */
const MAX_TASK_FILE_BYTES = 32 * 1024 * 1024;

/**
 * Grader sources linked into every submission, and the manager that judges communication
 * tasks. Both are uploaded per file or as a ZIP.
 */
export default function GradersEditor({taskId, isCommunication, isCustomChecker}) {
    const [files, setFiles] = useState([]);
    const [graderFiles, setGraderFiles] = useState([]);
    const [managerFiles, setManagerFiles] = useState([]);
    const [checkerFiles, setCheckerFiles] = useState([]);

    const axiosInstance = useContext(AxiosContext);

    const loadFiles = useCallback(() => {
        if (!taskId) {
            return;
        }
        axiosInstance.get(`/task/${taskId}/files`)
            .then(response => {
                if (response.status === 200) {
                    setFiles(response.data.files || []);
                }
            })
            .catch(_ => {});
    }, [axiosInstance, taskId]);

    useEffect(() => {
        loadFiles();
    }, [loadFiles]);

    /**
     * Uploads each selected file on its own. Source files are small, so a plain multi-select
     * beats making the teacher zip them; a .zip is still accepted and unpacked server-side.
     */
    const upload = async (endpoint, selected, reset) => {
        if (selected.some(file => file.size > MAX_TASK_FILE_BYTES)) {
            toast.error(getMessage('ka', 'uploadFileSizeExceeded'));
            return;
        }
        const added = [];
        const rejected = [];
        for (const file of selected) {
            const formData = new FormData();
            formData.append('file', file);
            try {
                // Errors are summarised below, so the global interceptor stays quiet.
                const response = await axiosInstance.post(`/task/${taskId}/${endpoint}`, formData,
                    {ignoreErrors: true});
                added.push(...(response.data.result?.success || []));
                rejected.push(...(response.data.result?.rejected || []).map(name => ({name})));
            } catch (error) {
                // The server explains why it turned the file away - a misplaced manager, a
                // name it cannot store, an unsupported type. Without the code the teacher only
                // sees "failed" and has nothing to act on.
                rejected.push({name: file.name, reason: error.response?.data?.message});
            }
        }
        if (added.length > 0) {
            toast.success(getMessage('ka', 'graderAdded') + ': ' + added.join(', '));
        }
        if (rejected.length > 0) {
            const details = rejected
                .map(entry => entry.reason
                    ? `${entry.name} — ${getMessage('ka', entry.reason)}`
                    : entry.name)
                .join('; ');
            toast.warn(getMessage('ka', 'failedToAddFiles') + ': ' + details);
        }
        reset([]);
        loadFiles();
    };

    const removeFile = (kind, fileName) => {
        axiosInstance.delete(`/task/${taskId}/file/${kind}/${fileName}`)
            .then(response => {
                if (response.status === 200) {
                    toast.success(getMessage('ka', 'graderDeleted'));
                    setFiles(files.filter(f => !(f.kind === kind && f.fileName === fileName)));
                }
            })
            .catch(_ => {
                toast.error(getMessage('ka', 'unexpectedException'));
            });
    };

    const downloadFile = (kind, fileName) => {
        axiosInstance.get(`/task/${taskId}/file/${kind}/${fileName}`, {responseType: 'blob'})
            .then(response => {
                if (response.status === 200) {
                    const url = window.URL.createObjectURL(new Blob([response.data]));
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = fileName;
                    document.body.appendChild(a);
                    a.click();
                    a.remove();
                    window.URL.revokeObjectURL(url);
                }
            });
    };

    const changeVisibility = (kind, fileName, status) => {
        axiosInstance.put(`/task/${taskId}/file/${kind}/${fileName}/public`, {status})
            .then(response => {
                if (response.status === 200) {
                    setFiles(files.map(f => f.kind === kind && f.fileName === fileName
                        ? {...f, visibleToContestants: status}
                        : f));
                    toast.success(getMessage('ka', 'saved'));
                }
            })
            .catch(error => {
                // A 400 carries a reason the interceptor already toasted; a generic message on
                // top of it would only contradict the specific one.
                if (error.response?.status !== 400) {
                    toast.error(getMessage('ka', 'unexpectedException'));
                }
            });
    };

    if (!taskId) {
        return null;
    }

    const graders = files.filter(f => f.kind === 'GRADER');
    const managers = files.filter(f => f.kind === 'MANAGER');
    const checkers = files.filter(f => f.kind === 'CHECKER');

    const fileRow = (file) => (
        <Stack key={`${file.kind}-${file.fileName}`}
               direction="row"
               justifyContent="space-between"
               alignItems="center"
               sx={{py: 1, px: '1rem', '&:hover': {backgroundColor: 'rgba(0, 0, 0, 0.1)', borderRadius: '4px'}}}>
            <Typography sx={{width: '55%'}}>{file.fileName}</Typography>
            <Typography variant="body2" color="textSecondary" sx={{width: '15%'}}>
                {Math.max(1, Math.round((file.sizeBytes || 0) / 1024))} KB
            </Typography>
            <Stack direction="row" gap="0.5rem" sx={{width: '30%', justifyContent: 'flex-end'}} alignItems="center">
                {file.kind === 'GRADER' && (
                    <Tooltip title={getMessage('ka', 'visibleToContestants')}>
                        <input type="checkbox"
                               checked={file.visibleToContestants}
                               onChange={() => changeVisibility(file.kind, file.fileName, !file.visibleToContestants)}/>
                    </Tooltip>
                )}
                <Button variant="contained" color="info" onClick={() => downloadFile(file.kind, file.fileName)}>
                    <Download/>
                </Button>
                <Button variant="contained" color="error" onClick={() => removeFile(file.kind, file.fileName)}>
                    <Delete/>
                </Button>
            </Stack>
        </Stack>
    );

    const uploadRow = (label, hint, selected, setSelected, endpoint) => (
        <Stack direction="row" gap="1rem" alignItems="center">
            <Stack>
                <Typography align="left" variant="h8">{getMessage('ka', label)}</Typography>
                <Typography variant="body2" color="textSecondary">{getMessage('ka', hint)}</Typography>
            </Stack>
            <Stack direction="row" gap="1rem" sx={{marginLeft: 'auto'}}>
                <Stack direction="column">
                    <Button variant="contained" component="label">
                        {getMessage('ka', 'uploadFiles')}
                        <input type="file" hidden multiple
                               accept=".cpp,.cc,.cxx,.c,.h,.hpp,.hxx,.py,.java,.txt,.md,.zip"
                               onClick={(e) => e.target.value = ''}
                               onChange={(e) => setSelected(Array.from(e.target.files))}/>
                    </Button>
                    {selected.length > 0 && (
                        <Typography variant="body2" color="textSecondary">
                            {getMessage('ka', 'uploadedFile')}: {selected.map(f => f.name).join(', ')}
                        </Typography>
                    )}
                </Stack>
                <Button variant="contained"
                        sx={{background: '#3c324e'}}
                        disabled={selected.length === 0}
                        onClick={() => upload(endpoint, selected, setSelected)}>
                    {getMessage('ka', 'add')}
                </Button>
            </Stack>
        </Stack>
    );

    return (
        <Stack gap="1rem" sx={{mt: '1rem'}}>
            <Typography align="center" variant="h6">{getMessage('ka', 'graders')}</Typography>
            <Typography align="center" variant="body2" color="textSecondary">
                {getMessage('ka', 'multiFileHint')}
            </Typography>

            {isCommunication && managers.length === 0 && (
                <Typography variant="body2" color="error">
                    {getMessage('ka', 'noManagerWarning')}
                </Typography>
            )}
            {isCustomChecker && checkers.length === 0 && (
                <Typography variant="body2" color="error">
                    {getMessage('ka', 'noCheckerWarning')}
                </Typography>
            )}

            {files.length > 0 && (
                <Paper elevation={4} sx={{py: '0.5rem'}}>
                    {graders.map(fileRow)}
                    {managers.map(fileRow)}
                    {checkers.map(fileRow)}
                </Paper>
            )}

            {uploadRow('graderFiles', 'graderHint', graderFiles, setGraderFiles, 'graders')}
            {/* Only the evaluator the task actually uses is offered, so a teacher cannot
                upload a checker to a manager-scored task and wonder why it is ignored. */}
            {(isCommunication || managers.length > 0)
                && uploadRow('managerFile', 'managerHint', managerFiles, setManagerFiles, 'manager')}
            {(isCustomChecker || checkers.length > 0)
                && uploadRow('checkerFile', 'checkerHint', checkerFiles, setCheckerFiles, 'checker')}
        </Stack>
    );
}
