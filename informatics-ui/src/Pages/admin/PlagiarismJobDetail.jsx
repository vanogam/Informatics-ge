import {useContext, useEffect, useState} from 'react'
import {
	Box,
	Container,
	Typography,
	Paper,
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
import {useNavigate, useParams} from 'react-router-dom'
import AdminNavigationBar from '../../Components/AdminNavigationBar'
import {useRequireAdmin} from '../../utils/useRequireAdmin'
import {AxiosContext} from '../../utils/axiosInstance'
import {formatDateTime} from '../../utils/dateUtils'
import {usePagination} from '../../utils/usePagination'
import PaginationControls from '../../Components/PaginationControls'
import getMessage from '../../Components/lang'

const STATUS_COLORS = {
	PENDING: 'default',
	RUNNING: 'warning',
	COMPLETED: 'success',
	FAILED: 'error',
}

export default function PlagiarismJobDetail() {
	const {ready} = useRequireAdmin()
	const {jobId} = useParams()
	const axiosInstance = useContext(AxiosContext)
	const navigate = useNavigate()

	const [detail, setDetail] = useState(null)
	const [loading, setLoading] = useState(true)

	const [selectedRunId, setSelectedRunId] = useState(null)
	const [comparisons, setComparisons] = useState([])
	const [loadingComparisons, setLoadingComparisons] = useState(false)
	const pagination = usePagination(20)

	const fetchDetail = () => {
		axiosInstance
			.get(`/admin/plagiarism/jobs/${jobId}`)
			.then((response) => setDetail(response.data))
			.catch(() => setDetail(null))
			.finally(() => setLoading(false))
	}

	useEffect(() => {
		if (!ready) {
			return
		}
		fetchDetail()
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [ready, jobId])

	useEffect(() => {
		if (!ready || !detail) {
			return
		}
		if (detail.job.status !== 'PENDING' && detail.job.status !== 'RUNNING') {
			return
		}
		const interval = setInterval(fetchDetail, 5000)
		return () => clearInterval(interval)
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [ready, detail, jobId])

	useEffect(() => {
		if (!selectedRunId) {
			setComparisons([])
			return
		}
		setLoadingComparisons(true)
		axiosInstance
			.get(`/admin/plagiarism/runs/${selectedRunId}/comparisons`, {
				params: {offset: pagination.offset, limit: pagination.pageSize},
			})
			.then((response) => {
				setComparisons(response.data?.comparisons || [])
				pagination.setTotalCount(response.data?.totalCount || 0)
			})
			.catch(() => setComparisons([]))
			.finally(() => setLoadingComparisons(false))
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [axiosInstance, selectedRunId, pagination.offset, pagination.pageSize])

	const selectRun = (runId) => {
		pagination.setPage(0)
		setSelectedRunId(runId === selectedRunId ? null : runId)
	}

	if (!ready || loading) {
		return (
			<>
				<AdminNavigationBar />
				<Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
					<CircularProgress />
				</Box>
			</>
		)
	}

	if (!detail) {
		return (
			<>
				<AdminNavigationBar />
				<Container maxWidth="lg">
					<Typography color="error" align="center" mt="2rem">
						{getMessage('ka', 'plagiarismJobNotFound')}
					</Typography>
				</Container>
			</>
		)
	}

	const {job, runs} = detail

	return (
		<main>
			<AdminNavigationBar />
			<Container maxWidth="lg">
				<Button sx={{marginTop: '1rem'}} onClick={() => navigate('/admin/plagiarism')}>
					&larr; {getMessage('ka', 'plagiarismBackToJobs')}
				</Button>

				<Paper elevation={3} sx={{padding: '1.5rem', marginTop: '1rem', marginBottom: '1.5rem'}}>
					<Typography variant="h6" sx={{color: '#452c54', fontWeight: 'bold'}}>
						{getMessage('ka', job.scopeType === 'CONTEST' ? 'plagiarismScopeContest' : 'plagiarismScopeTask')}
						: {job.scopeName}
					</Typography>
					<Box sx={{display: 'flex', gap: 2, alignItems: 'center', marginTop: '0.5rem', flexWrap: 'wrap'}}>
						<Chip
							size="small"
							label={getMessage('ka', `PLAGIARISM_STATUS_${job.status}`)}
							color={STATUS_COLORS[job.status] || 'default'}
						/>
						<Typography variant="body2" color="text.secondary">
							{getMessage('ka', 'plagiarismCreatedColumn')}: {formatDateTime(job.createdAt)}
						</Typography>
						{job.finishedAt && (
							<Typography variant="body2" color="text.secondary">
								{getMessage('ka', 'plagiarismFinishedColumn')}: {formatDateTime(job.finishedAt)}
							</Typography>
						)}
					</Box>
					{job.errorMessage && (
						<Typography color="error" variant="body2" sx={{marginTop: '0.5rem'}}>
							{job.errorMessage}
						</Typography>
					)}
				</Paper>

				<TableContainer component={Paper} sx={{marginBottom: '1.5rem'}}>
					<Table>
						<TableHead>
							<TableRow>
								<TableCell>{getMessage('ka', 'plagiarismTaskColumn')}</TableCell>
								<TableCell>{getMessage('ka', 'plagiarismLanguageColumn')}</TableCell>
								<TableCell>{getMessage('ka', 'plagiarismStatusColumn')}</TableCell>
								<TableCell align="right">{getMessage('ka', 'plagiarismSubmissionCountColumn')}</TableCell>
								<TableCell align="right">{getMessage('ka', 'plagiarismComparisonsColumn')}</TableCell>
								<TableCell align="right">{getMessage('ka', 'plagiarismViewReport')}</TableCell>
							</TableRow>
						</TableHead>
						<TableBody>
							{runs.length === 0 ? (
								<TableRow>
									<TableCell colSpan={6} align="center">
										<Typography variant="body2" color="text.secondary">
											{getMessage('ka', 'plagiarismNoRuns')}
										</Typography>
									</TableCell>
								</TableRow>
							) : (
								runs.map((run) => (
									<TableRow
										key={run.id}
										hover
										selected={run.id === selectedRunId}
										sx={{cursor: run.status === 'COMPLETED' ? 'pointer' : 'default'}}
										onClick={() => run.status === 'COMPLETED' && selectRun(run.id)}
									>
										<TableCell>{run.taskTitle}</TableCell>
										<TableCell>{getMessage('ka', `LANG_${run.language}`)}</TableCell>
										<TableCell>
											<Chip
												size="small"
												label={getMessage('ka', `PLAGIARISM_STATUS_${run.status}`)}
												color={STATUS_COLORS[run.status] || 'default'}
											/>
											{run.errorMessage && (
												<Typography variant="caption" color="error" sx={{display: 'block'}}>
													{run.errorMessage}
												</Typography>
											)}
										</TableCell>
										<TableCell align="right">{run.submissionCount ?? '—'}</TableCell>
										<TableCell align="right">{run.comparisonCount ?? '—'}</TableCell>
										<TableCell align="right">
											{run.status === 'COMPLETED' && (
												<Button size="small" onClick={() => selectRun(run.id)}>
													{getMessage('ka', 'plagiarismViewReport')}
												</Button>
											)}
										</TableCell>
									</TableRow>
								))
							)}
						</TableBody>
					</Table>
				</TableContainer>

				{selectedRunId && (
					<>
						<Typography variant="h6" sx={{marginBottom: '1rem', color: '#452c54', fontWeight: 'bold'}}>
							{getMessage('ka', 'plagiarismComparisonsTitle')}
						</Typography>
						{loadingComparisons ? (
							<Box display="flex" justifyContent="center" padding="2rem">
								<CircularProgress />
							</Box>
						) : (
							<>
								<TableContainer component={Paper}>
									<Table>
										<TableHead>
											<TableRow>
												<TableCell>{getMessage('ka', 'plagiarismUserAColumn')}</TableCell>
												<TableCell>{getMessage('ka', 'plagiarismUserBColumn')}</TableCell>
												<TableCell align="right">{getMessage('ka', 'plagiarismSimilarityColumn')}</TableCell>
												<TableCell align="right">{getMessage('ka', 'plagiarismCompareAction')}</TableCell>
											</TableRow>
										</TableHead>
										<TableBody>
											{comparisons.length === 0 ? (
												<TableRow>
													<TableCell colSpan={4} align="center">
														<Typography variant="body2" color="text.secondary">
															{getMessage('ka', 'plagiarismNoComparisons')}
														</Typography>
													</TableCell>
												</TableRow>
											) : (
												comparisons.map((comparison) => (
													<TableRow
														key={comparison.id}
														hover
														sx={{cursor: 'pointer', '&:hover': {backgroundColor: '#eee'}}}
														onClick={() => navigate(`/admin/plagiarism/compare/${comparison.id}`)}
													>
														<TableCell>{comparison.usernameA}</TableCell>
														<TableCell>{comparison.usernameB}</TableCell>
														<TableCell align="right">{(comparison.similarity * 100).toFixed(1)}%</TableCell>
														<TableCell align="right">
															<Button
																size="small"
																onClick={(event) => {
																	event.stopPropagation()
																	navigate(`/admin/plagiarism/compare/${comparison.id}`)
																}}
															>
																{getMessage('ka', 'plagiarismCompareAction')}
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
					</>
				)}
			</Container>
		</main>
	)
}
