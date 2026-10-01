import {useContext, useEffect, useState} from 'react'
import Box from '@mui/material/Box'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Select from '@mui/material/Select'
import MenuItem from '@mui/material/MenuItem'
import FormControl from '@mui/material/FormControl'
import InputLabel from '@mui/material/InputLabel'
import Autocomplete from '@mui/material/Autocomplete'
import CircularProgress from '@mui/material/CircularProgress'
import Button from '@mui/material/Button'
import {AxiosContext} from '../utils/axiosInstance'
import getMessage from './lang'

const LANGUAGE_OPTIONS = ['CPP', 'PYTHON', 'OUTPUT']

const STATUS_OPTIONS = [
    'IN_QUEUE', 'COMPILING', 'RUNNING', 'SYSTEM_ERROR', 'COMPILATION_ERROR',
    'TIME_LIMIT_EXCEEDED', 'MEMORY_LIMIT_EXCEEDED', 'RUNTIME_ERROR',
    'WRONG_ANSWER', 'FAILED', 'PARTIAL', 'CORRECT',
]

// How many rows a search-as-you-type combobox pulls per keystroke - the list behind it (every
// contest, or every task) can be far bigger than what's useful to scroll through in a dropdown.
const COMBOBOX_RESULT_LIMIT = 10
const SEARCH_DEBOUNCE_MS = 300

const EMPTY_FILTERS = {username: '', taskId: '', contestId: '', language: '', status: ''}

/**
 * The filter bar shared by every submissions list. Which controls actually render is decided by
 * the page that owns the list, not by this component: a page whose data is already scoped to one
 * contest or one user must not offer a control that would just re-select the same scope, so
 * `showUsername`/`showContest` default to off and `fixedContestId` skips the contest control
 * entirely while still scoping the problem dropdown to it.
 *
 * @param value current filter values: {username, taskId, contestId, language, status}
 * @param onChange called with the next filter object whenever a control changes
 * @param showUsername whether to offer the exact-match username field
 * @param showContest whether to offer a contest combobox (ignored when fixedContestId is set)
 * @param fixedContestId when set, the problem combobox is scoped to this contest and no contest
 *                        control is shown at all
 * @param contestsRoomId restricts the contest search to contests in this room; omitted for a
 *                        global (all rooms) search, as on the admin and profile pages
 * @param adminTasks whether the problem combobox may search every task in the system (via the
 *                    admin-only /admin/tasks) when no contest is selected, instead of staying
 *                    unusable until one is - only valid where the viewer is known to be an admin
 */
export default function SubmissionFilters({
    value,
    onChange,
    showUsername = false,
    showContest = false,
    fixedContestId = null,
    contestsRoomId = null,
    adminTasks = false,
}) {
    const axiosInstance = useContext(AxiosContext)
    const [usernameDraft, setUsernameDraft] = useState(value.username || '')

    const [contestInput, setContestInput] = useState('')
    const [contestOptions, setContestOptions] = useState([])
    const [contestLoading, setContestLoading] = useState(false)
    const [selectedContest, setSelectedContest] = useState(null)

    const [taskInput, setTaskInput] = useState('')
    const [taskOptions, setTaskOptions] = useState([])
    const [taskLoading, setTaskLoading] = useState(false)
    const [selectedTask, setSelectedTask] = useState(null)

    const effectiveContestId = fixedContestId || value.contestId || null
    const problemUsable = !!(fixedContestId || effectiveContestId || adminTasks)

    // The username field is free text (an exact match, but still typed character by character), so
    // it is debounced rather than firing a request per keystroke; every other control is a
    // discrete choice and applies immediately.
    useEffect(() => {
        setUsernameDraft(value.username || '')
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [value.username])

    useEffect(() => {
        if (usernameDraft === (value.username || '')) {
            return
        }
        const timeout = setTimeout(() => {
            onChange({...value, username: usernameDraft})
        }, 400)
        return () => clearTimeout(timeout)
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [usernameDraft])

    // A filter cleared from outside (the "Clear filters" button, or a contest change resetting the
    // problem choice) drops the id from `value` - drop the matching combobox selection to match.
    useEffect(() => {
        if (!value.contestId) {
            setSelectedContest(null)
        }
    }, [value.contestId])

    useEffect(() => {
        if (!value.taskId) {
            setSelectedTask(null)
        }
    }, [value.taskId])

    useEffect(() => {
        if (!showContest || fixedContestId) {
            return
        }
        setContestLoading(true)
        const timeout = setTimeout(() => {
            axiosInstance
                .get('/contests', {
                    params: {
                        roomId: contestsRoomId || undefined,
                        name: contestInput || undefined,
                        offset: 0,
                        limit: COMBOBOX_RESULT_LIMIT,
                    },
                })
                .then((response) => setContestOptions(response.data?.contests || []))
                .catch(() => setContestOptions([]))
                .finally(() => setContestLoading(false))
        }, SEARCH_DEBOUNCE_MS)
        return () => clearTimeout(timeout)
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [axiosInstance, showContest, fixedContestId, contestsRoomId, contestInput])

    useEffect(() => {
        if (!problemUsable) {
            setTaskOptions([])
            return
        }
        setTaskLoading(true)
        const timeout = setTimeout(() => {
            const request = effectiveContestId
                ? axiosInstance.get(`/contest/${effectiveContestId}/tasks`, {
                    params: {title: taskInput || undefined, offset: 0, limit: COMBOBOX_RESULT_LIMIT},
                })
                // No contest chosen: search every task in the system instead of leaving the
                // control unusable, since an admin isn't necessarily narrowing by contest first.
                : axiosInstance.get('/admin/tasks', {
                    params: {title: taskInput || undefined, offset: 0, limit: COMBOBOX_RESULT_LIMIT},
                })
            request
                .then((response) => setTaskOptions(response.data?.tasks || []))
                .catch(() => setTaskOptions([]))
                .finally(() => setTaskLoading(false))
        }, SEARCH_DEBOUNCE_MS)
        return () => clearTimeout(timeout)
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [axiosInstance, effectiveContestId, adminTasks, problemUsable, taskInput])

    const hasActiveFilter = Object.keys(EMPTY_FILTERS).some((key) => value[key])

    const handleContestSelect = (contest) => {
        setSelectedContest(contest)
        // A problem picked under the previous contest almost never belongs to the new one.
        onChange({...value, contestId: contest ? contest.id : '', taskId: ''})
    }

    const handleTaskSelect = (taskOption) => {
        setSelectedTask(taskOption)
        onChange({...value, taskId: taskOption ? taskOption.task.id : ''})
    }

    const handleFieldChange = (field) => (event) => {
        onChange({...value, [field]: event.target.value})
    }

    const searchEndAdornment = (loading, params) => (
        <>
            {loading ? <CircularProgress color="inherit" size={16} /> : null}
            {params.InputProps.endAdornment}
        </>
    )

    return (
        <Box sx={{marginBottom: '1rem'}}>
            <Stack direction="row" flexWrap="wrap" gap={2} alignItems="center">
                {showUsername && (
                    <TextField
                        size="small"
                        label={getMessage('ka', 'filterUsername')}
                        value={usernameDraft}
                        onChange={(event) => setUsernameDraft(event.target.value)}
                        sx={{minWidth: 160}}
                    />
                )}

                {showContest && !fixedContestId && (
                    <Autocomplete
                        size="small"
                        sx={{minWidth: 220}}
                        options={contestOptions}
                        loading={contestLoading}
                        value={selectedContest}
                        onChange={(event, newValue) => handleContestSelect(newValue)}
                        inputValue={contestInput}
                        onInputChange={(event, newInputValue) => setContestInput(newInputValue)}
                        getOptionLabel={(contest) => contest?.name || ''}
                        isOptionEqualToValue={(contest, selected) => contest.id === selected.id}
                        filterOptions={(options) => options}
                        noOptionsText={getMessage('ka', 'noResults')}
                        renderInput={(params) => (
                            <TextField
                                {...params}
                                label={getMessage('ka', 'filterContest')}
                                InputProps={{...params.InputProps, endAdornment: searchEndAdornment(contestLoading, params)}}
                            />
                        )}
                    />
                )}

                {problemUsable && (
                    <Autocomplete
                        size="small"
                        sx={{minWidth: 220}}
                        options={taskOptions}
                        loading={taskLoading}
                        value={selectedTask}
                        onChange={(event, newValue) => handleTaskSelect(newValue)}
                        inputValue={taskInput}
                        onInputChange={(event, newInputValue) => setTaskInput(newInputValue)}
                        getOptionLabel={(taskOption) => taskOption?.task?.title || ''}
                        isOptionEqualToValue={(taskOption, selected) => taskOption.task.id === selected.task.id}
                        filterOptions={(options) => options}
                        noOptionsText={getMessage('ka', 'noResults')}
                        renderInput={(params) => (
                            <TextField
                                {...params}
                                label={getMessage('ka', 'filterProblem')}
                                InputProps={{...params.InputProps, endAdornment: searchEndAdornment(taskLoading, params)}}
                            />
                        )}
                    />
                )}

                <FormControl size="small" sx={{minWidth: 140}}>
                    <InputLabel>{getMessage('ka', 'filterLanguage')}</InputLabel>
                    <Select
                        value={value.language || ''}
                        label={getMessage('ka', 'filterLanguage')}
                        onChange={handleFieldChange('language')}
                    >
                        <MenuItem value=""><em>{getMessage('ka', 'filterAll')}</em></MenuItem>
                        {LANGUAGE_OPTIONS.map((language) => (
                            <MenuItem key={language} value={language}>{getMessage('ka', `LANG_${language}`)}</MenuItem>
                        ))}
                    </Select>
                </FormControl>

                <FormControl size="small" sx={{minWidth: 160}}>
                    <InputLabel>{getMessage('ka', 'filterStatus')}</InputLabel>
                    <Select
                        value={value.status || ''}
                        label={getMessage('ka', 'filterStatus')}
                        onChange={handleFieldChange('status')}
                    >
                        <MenuItem value=""><em>{getMessage('ka', 'filterAll')}</em></MenuItem>
                        {STATUS_OPTIONS.map((status) => (
                            <MenuItem key={status} value={status}>{getMessage('ka', `STATUS_FILTER_${status}`)}</MenuItem>
                        ))}
                    </Select>
                </FormControl>

                {hasActiveFilter && (
                    <Button size="small" onClick={() => onChange({...EMPTY_FILTERS})}>
                        {getMessage('ka', 'clearFilters')}
                    </Button>
                )}
            </Stack>
        </Box>
    )
}

export {EMPTY_FILTERS}
