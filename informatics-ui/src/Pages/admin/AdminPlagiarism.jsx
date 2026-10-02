import {useContext, useEffect, useState} from 'react'
import {
	Box,
	Container,
	Typography,
	Paper,
	Stack,
	ToggleButton,
	ToggleButtonGroup,
	Autocomplete,
	TextField,
	Button,
	CircularProgress,
	Table,
	TableHead,
	TableRow,
	TableCell,
	TableBody,
	TableContainer,
	Chip,
} from '@mui/material'
import {useNavigate} from 'react-router-dom'
import AdminNavigationBar from '../../Components/AdminNavigationBar'
import {useRequireAdmin} from '../../utils/useRequireAdmin'
import {AxiosContext} from '../../utils/axiosInstance'
import {formatDateTime} from '../../utils/dateUtils'
import {usePagination} from '../../utils/usePagination'
import PaginationControls from '../../Components/PaginationControls'
import getMessage from '../../Components/lang'

const SEARCH_DEBOUNCE_MS = 300
const COMBOBOX_RESULT_LIMIT = 10

const STATUS_COLORS = {
	PENDING: 'default',
	RUNNING: 'warning',
	COMPLETED: 'success',
	FAILED: 'error',
}

export default function AdminPlagiarism() {
	const {ready} = useRequireAdmin()
	const axiosInstance = useContext(AxiosContext)
	const navigate = useNavigate()

	const [scope, setScope] = useState('CONTEST')

	const [contestInput, setContestInput] = useState('')
	const [contestOptions, setContestOptions] = useState([])
	const [contestLoading, setContestLoading] = useState(false)
	const [selectedContest, setSelectedContest] = useState(null)

	const [taskInput, setTaskInput] = useState('')
	const [taskOptions, setTaskOptions] = useState([])
	const [taskLoading, setTaskLoading] = useState(false)
	const [selectedTask, setSelectedTask] = useState(null)

	const [eligibleUsers, setEligibleUsers] = useState([])
	const [selectedUsers, setSelectedUsers] = useState([])

	const [starting, setStarting] = useState(false)
	const [startError, setStartError] = useState(null)

	const [jobs, setJobs] = useState([])
	const [loadingJobs, setLoadingJobs] = useState(true)
	const pagination = usePagination(20)

	useEffect(() => {
		if (scope !== 'CONTEST') {
			setContestOptions([])
			return
		}
		setContestLoading(true)
		const timeout = setTimeout(() => {
			axiosInstance
				.get('/contests', {params: {name: contestInput || undefined, offset: 0, limit: COMBOBOX_RESULT_LIMIT}})
				.then((response) => setContestOptions(response.data?.contests || []))
				.catch(() => setContestOptions([]))
				.finally(() => setContestLoading(false))
		}, SEARCH_DEBOUNCE_MS)
		return () => clearTimeout(timeout)
	}, [axiosInstance, scope, contestInput])

	useEffect(() => {
		if (scope !== 'TASK') {
			setTaskOptions([])
			return
		}
		setTaskLoading(true)
		const timeout = setTimeout(() => {
			axiosInstance
				.get('/admin/tasks', {params: {title: taskInput || undefined, offset: 0, limit: COMBOBOX_RESULT_LIMIT}})
				.then((response) => setTaskOptions(response.data?.tasks || []))
				.catch(() => setTaskOptions([]))
				.finally(() => setTaskLoading(false))
		}, SEARCH_DEBOUNCE_MS)
		return () => clearTimeout(timeout)
	}, [axiosInstance, scope, taskInput])

	// The user filter is scoped to whichever contest/task is currently chosen - re-fetch whenever
	// that changes, and drop any selection that no longer applies.
	useEffect(() => {
		setSelectedUsers([])
		const contestId = scope === 'CONTEST' ? selectedContest?.id : null
		const taskId = scope === 'TASK' ? selectedTask?.task?.id : null
		if (!contestId && !taskId) {
			setEligibleUsers([])
			return
		}
		axiosInstance
			.get('/admin/plagiarism/eligible-users', {params: {contestId: contestId || undefined, taskId: taskId || undefined}})
			.then((response) => setEligibleUsers(response.data || []))
			.catch(() => setEligibleUsers([]))
	}, [axiosInstance, scope, selectedContest, selectedTask])

	const fetchJobs = () => {
		axiosInstance
			.get('/admin/plagiarism/jobs', {params: {offset: pagination.offset, limit: pagination.pageSize}})
			.then((response) => {
				setJobs(response.data?.jobs || [])
				pagination.setTotalCount(response.data?.totalCount || 0)
			})
			.catch(() => setJobs([]))
			.finally(() => setLoadingJobs(false))
	}

	useEffect(() => {
		if (!ready) {
			return
		}
		fetchJobs()
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [ready, pagination.offset, pagination.pageSize])

	// Keeps the list live while anything is still queued/running, without polling forever once
	// everything has settled.
	useEffect(() => {
		if (!ready) {
			return
		}
		const hasPending = jobs.some((job) => job.status === 'PENDING' || job.status === 'RUNNING')
		if (!hasPending) {
			return
		}
		const interval = setInterval(fetchJobs, 5000)
		return () => clearInterval(interval)
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [ready, jobs])

	const canStart = (scope === 'CONTEST' ? !!selectedContest : !!selectedTask) && !starting

	const handleStart = () => {
		setStarting(true)
		setStartError(null)
		axiosInstance
			.post('/admin/plagiarism/jobs', {
				contestId: scope === 'CONTEST' ? selectedContest?.id : null,
				taskId: scope === 'TASK' ? selectedTask?.task?.id : null,
				usernames: selectedUsers,
			})
			.then(() => {
				setSelectedContest(null)
				setSelectedTask(null)
				setSelectedUsers([])
				fetchJobs()
			})
			.catch((error) => setStartError(error.response?.data?.message || 'plagiarismStartError'))
			.finally(() => setStarting(false))
	}

	if (!ready) {
		return (
			<>
				<AdminNavigationBar />
				<Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
					<CircularProgress />
				</Box>
			</>
		)
	}

	return (
		<main>
			<AdminNavigationBar />
			<Container maxWidth="lg">
				<Paper elevation={3} sx={{padding: '1.5rem', marginTop: '1rem', marginBottom: '1.5rem'}}>
					<Typography variant="h6" sx={{marginBottom: '1rem', color: '#452c54', fontWeight: 'bold'}}>
						{getMessage('ka', 'plagiarismNewCheck')}
					</Typography>
					<Stack direction="row" flexWrap="wrap" gap={2} alignItems="center">
						<ToggleButtonGroup
							size="small"
							exclusive
							value={scope}
							onChange={(event, value) => {
								if (!value) {
									return
								}
								setScope(value)
								setSelectedContest(null)
								setSelectedTask(null)
							}}
						>
							<ToggleButton value="CONTEST">{getMessage('ka', 'plagiarismScopeContest')}</ToggleButton>
							<ToggleButton value="TASK">{getMessage('ka', 'plagiarismScopeTask')}</ToggleButton>
						</ToggleButtonGroup>

						{scope === 'CONTEST' ? (
							<Autocomplete
								size="small"
								sx={{minWidth: 240}}
								options={contestOptions}
								loading={contestLoading}
								value={selectedContest}
								onChange={(event, newValue) => setSelectedContest(newValue)}
								inputValue={contestInput}
								onInputChange={(event, newInputValue) => setContestInput(newInputValue)}
								getOptionLabel={(contest) => contest?.name || ''}
								isOptionEqualToValue={(contest, selected) => contest.id === selected.id}
								filterOptions={(options) => options}
								noOptionsText={getMessage('ka', 'noResults')}
								renderInput={(params) => (
									<TextField {...params} label={getMessage('ka', 'plagiarismSelectContest')} />
								)}
							/>
						) : (
							<Autocomplete
								size="small"
								sx={{minWidth: 240}}
								options={taskOptions}
								loading={taskLoading}
								value={selectedTask}
								onChange={(event, newValue) => setSelectedTask(newValue)}
								inputValue={taskInput}
								onInputChange={(event, newInputValue) => setTaskInput(newInputValue)}
								getOptionLabel={(taskOption) => taskOption?.task?.title || ''}
								isOptionEqualToValue={(taskOption, selected) => taskOption.task.id === selected.task.id}
								filterOptions={(options) => options}
								noOptionsText={getMessage('ka', 'noResults')}
								renderInput={(params) => (
									<TextField {...params} label={getMessage('ka', 'plagiarismSelectTask')} />
								)}
							/>
						)}

						<Autocomplete
							multiple
							size="small"
							sx={{minWidth: 280}}
							options={eligibleUsers}
							value={selectedUsers}
							onChange={(event, newValue) => setSelectedUsers(newValue)}
							disabled={eligibleUsers.length === 0}
							noOptionsText={getMessage('ka', 'noResults')}
							renderInput={(params) => (
								<TextField
									{...params}
									label={getMessage('ka', 'plagiarismSelectUsers')}
									placeholder={eligibleUsers.length > 0 ? getMessage('ka', 'plagiarismUsersHint') : undefined}
								/>
							)}
						/>

						<Button
							variant="contained"
							disabled={!canStart}
							onClick={handleStart}
							sx={{backgroundColor: '#2f2d47'}}
						>
							{getMessage('ka', 'plagiarismStartCheck')}
						</Button>
					</Stack>
					{startError && (
						<Typography color="error" variant="body2" sx={{marginTop: '0.5rem'}}>
							{getMessage('ka', startError)}
						</Typography>
					)}
				</Paper>

				<Typography variant="h6" sx={{marginBottom: '1rem', color: '#452c54', fontWeight: 'bold'}}>
					{getMessage('ka', 'plagiarismJobsTitle')}
				</Typography>
				{loadingJobs ? (
					<Box display="flex" justifyContent="center" padding="2rem">
						<CircularProgress />
					</Box>
				) : (
					<>
						<TableContainer component={Paper}>
							<Table>
								<TableHead>
									<TableRow>
										<TableCell>{getMessage('ka', 'plagiarismScopeColumn')}</TableCell>
										<TableCell>{getMessage('ka', 'plagiarismStatusColumn')}</TableCell>
										<TableCell>{getMessage('ka', 'plagiarismCreatedColumn')}</TableCell>
										<TableCell>{getMessage('ka', 'plagiarismFinishedColumn')}</TableCell>
										<TableCell align="right">{getMessage('ka', 'plagiarismRunsColumn')}</TableCell>
										<TableCell align="right">{getMessage('ka', 'plagiarismComparisonsColumn')}</TableCell>
										<TableCell align="right">{getMessage('ka', 'plagiarismView')}</TableCell>
									</TableRow>
								</TableHead>
								<TableBody>
									{jobs.length === 0 ? (
										<TableRow>
											<TableCell colSpan={7} align="center">
												<Typography variant="body2" color="text.secondary">
													{getMessage('ka', 'plagiarismNoJobs')}
												</Typography>
											</TableCell>
										</TableRow>
									) : (
										jobs.map((job) => (
											<TableRow
												key={job.id}
												hover
												sx={{cursor: 'pointer', '&:hover': {backgroundColor: '#eee'}}}
												onClick={() => navigate(`/admin/plagiarism/${job.id}`)}
											>
												<TableCell>
													{getMessage('ka', job.scopeType === 'CONTEST' ? 'plagiarismScopeContest' : 'plagiarismScopeTask')}
													: {job.scopeName}
												</TableCell>
												<TableCell>
													<Chip
														size="small"
														label={getMessage('ka', `PLAGIARISM_STATUS_${job.status}`)}
														color={STATUS_COLORS[job.status] || 'default'}
													/>
												</TableCell>
												<TableCell>{formatDateTime(job.createdAt)}</TableCell>
												<TableCell>{job.finishedAt ? formatDateTime(job.finishedAt) : '—'}</TableCell>
												<TableCell align="right">{job.runCount}</TableCell>
												<TableCell align="right">{job.totalComparisons}</TableCell>
												<TableCell align="right">
													<Button
														size="small"
														onClick={(event) => {
															event.stopPropagation()
															navigate(`/admin/plagiarism/${job.id}`)
														}}
													>
														{getMessage('ka', 'plagiarismView')}
													</Button>
												</TableCell>
											</TableRow>
										))
									)}
								</TableBody>
							</Table>
						</TableContainer>
						<PaginationControls pagination={pagination} />
					</>
				)}
			</Container>
		</main>
	)
}
