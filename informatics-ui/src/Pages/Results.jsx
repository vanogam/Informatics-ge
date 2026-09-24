import { useParams } from 'react-router-dom'
import {
	Container,
	Typography,
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableRow,
	TableContainer,
	Paper,
	Box,
	Checkbox,
	FormControlLabel,
} from '@mui/material'
import { useState, useEffect, useContext } from 'react'
import { AxiosContext } from '../utils/axiosInstance'
import ContestNavigationBar from '../Components/ContestNavigationBar'
import { getScoreCellBackground } from '../styles/scoreColors'
import { usePagination } from '../utils/usePagination'
import PaginationControls from '../Components/PaginationControls'

const INCLUDE_UPSOLVING_STORAGE_PREFIX = 'informatics.includeUpsolving.'

// Upsolving-only rows (contestants with no live standings entry) get this tint instead of a
// place number, so they read as clearly separate from the ranked live standings.
const UPSOLVING_ONLY_ROW_COLOR = '#fdf3d7'

const readStoredIncludeUpsolving = (contestId) => {
	try {
		const raw = localStorage.getItem(INCLUDE_UPSOLVING_STORAGE_PREFIX + contestId)
		return raw === null ? null : raw === 'true'
	} catch {
		return null
	}
}

const writeStoredIncludeUpsolving = (contestId, value) => {
	try {
		localStorage.setItem(INCLUDE_UPSOLVING_STORAGE_PREFIX + contestId, String(value))
	} catch {
		// private browsing / storage disabled - the checkbox just won't persist
	}
}

const buildResult = (contestantId, username, liveTaskResults, upTaskResults, includeUpsolving) => {
	const taskScores = {}
	const upsolvingTasks = {}

	const allTaskCodes = new Set([
		...Object.keys(liveTaskResults || {}),
		...Object.keys(upTaskResults || {}),
	])

	let totalScore = 0

	allTaskCodes.forEach((taskCode) => {
		const liveScore =
			liveTaskResults[taskCode] && liveTaskResults[taskCode].score != null
				? liveTaskResults[taskCode].score
				: 0

		let finalScore = liveScore
		let isUpsolvingUsed = false

		if (includeUpsolving && upTaskResults && upTaskResults[taskCode]) {
			const upScore =
				upTaskResults[taskCode].score != null ? upTaskResults[taskCode].score : 0
			if (upScore > liveScore) {
				finalScore = upScore
				isUpsolvingUsed = true
			}
		}

		taskScores[taskCode] = finalScore
		if (isUpsolvingUsed) {
			upsolvingTasks[taskCode] = true
		}
		totalScore += finalScore
	})

	return { contestantId, username, totalScore, taskScores, upsolvingTasks }
}

export default function Results() {
	const axiosInstance = useContext(AxiosContext)
	const { contest_id } = useParams()
	const [results, setResults] = useState([])
	const [upsolvingOnlyResults, setUpsolvingOnlyResults] = useState([])
	const [standings, setStandings] = useState([])
	const [upsolvingStandings, setUpsolvingStandings] = useState([])
	const [taskOrder, setTaskOrder] = useState([])
	const [taskNameMap, setTaskNameMap] = useState({})
	const pagination = usePagination()
	const {page, pageSize, offset, setTotalCount} = pagination
	const [loading, setLoading] = useState(false)
	const [includeUpsolving, setIncludeUpsolving] = useState(() => readStoredIncludeUpsolving(contest_id) ?? false)

	useEffect(() => {
		loadStandings()
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [contest_id, offset, pageSize, axiosInstance])

	// Re-sync the checkbox when navigating between contests without a full remount.
	useEffect(() => {
		const stored = readStoredIncludeUpsolving(contest_id)
		if (stored !== null) {
			setIncludeUpsolving(stored)
		}
		// If nothing is stored yet, loadUpsolvingStandings (below) picks the default once the
		// contest's end date is known.
	}, [contest_id])

	const loadUpsolvingStandings = () => {
		if (!contest_id) return
		axiosInstance
			.get(`/contest/${contest_id}`)
			.then((response) => {
				const upStandings = response.data.upsolvingStandings || []
				setUpsolvingStandings(upStandings)

				if (readStoredIncludeUpsolving(contest_id) === null) {
					const endDate = response.data.endDate
					const isPastContest = endDate ? new Date(endDate).getTime() < Date.now() : false
					writeStoredIncludeUpsolving(contest_id, isPastContest)
					setIncludeUpsolving(isPastContest)
				}
			})
			.catch((error) => {
				console.error('Error fetching upsolving standings:', error)
				setUpsolvingStandings([])
			})
	}

	useEffect(() => {
		loadUpsolvingStandings()
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [contest_id, axiosInstance])

	const handleIncludeUpsolvingChange = (checked) => {
		setIncludeUpsolving(checked)
		writeStoredIncludeUpsolving(contest_id, checked)
		if (checked) {
			loadUpsolvingStandings()
		}
	}

	const loadStandings = () => {
		setLoading(true)

		axiosInstance
			.get(`/contest/${contest_id}/standings`, {
				params: {
					offset: offset,
					limit: pageSize,
				},
			})
			.then((response) => {
				const fetchedStandings = response.data.standings || []
				const taskNameMapData = response.data.taskNameMap || {}

				const taskCodesFromBackend = Object.keys(taskNameMapData)
				const backendTaskCodesSet = new Set(taskCodesFromBackend)

				// Collect any task codes from standings that aren't in the backend map
				const additionalTaskCodes = []
				fetchedStandings.forEach((standing) => {
					if (standing.taskResults) {
						Object.keys(standing.taskResults).forEach((taskCode) => {
							if (!backendTaskCodesSet.has(taskCode) && !additionalTaskCodes.includes(taskCode)) {
								additionalTaskCodes.push(taskCode)
							}
						})
					}
				})

				// Combine: backend order first, then any additional ones
				const taskOrderArray = [...taskCodesFromBackend, ...additionalTaskCodes]

				setTaskOrder(taskOrderArray)
				setTaskNameMap(taskNameMapData)
				setStandings(fetchedStandings)
				setTotalCount(response.data.totalCount || 0)

				setLoading(false)
			})
			.catch((error) => {
				console.error('Error fetching standings:', error)
				setLoading(false)
			})
	}

	useEffect(() => {
		const upMap = new Map()
		upsolvingStandings.forEach((up) => {
			if (up && up.contestantId !== undefined && up.taskResults) {
				upMap.set(up.contestantId, up)
			}
		})

		const liveContestantIds = new Set(standings.map((s) => s.contestantId))

		const processedResults = standings.map((standing) => {
			const upEntry = upMap.get(standing.contestantId)
			return buildResult(
				standing.contestantId,
				standing.username || 'deleted',
				standing.taskResults || {},
				(upEntry && upEntry.taskResults) || {},
				includeUpsolving
			)
		})

		const upsolvingOnly = includeUpsolving
			? upsolvingStandings
					.filter((up) => up && up.contestantId !== undefined && !liveContestantIds.has(up.contestantId))
					.map((up) =>
						buildResult(up.contestantId, up.username || 'deleted', {}, up.taskResults || {}, true)
					)
					.sort((a, b) => b.totalScore - a.totalScore)
			: []

		setResults(processedResults)
		setUpsolvingOnlyResults(upsolvingOnly)
	}, [standings, upsolvingStandings, includeUpsolving])

	const getTaskName = (taskCode) => {
		return taskNameMap[taskCode] || taskCode
	}

	const getTaskScore = (result, taskCode) => {
		const score = result.taskScores[taskCode]
		if (score === undefined) {
			return '-'
		}
		const formatted = score.toFixed(2)
		const isUpsolving =
			result.upsolvingTasks &&
			Object.prototype.hasOwnProperty.call(result.upsolvingTasks, taskCode)
		return isUpsolving ? `${formatted}*` : formatted
	}

	const columnCount = taskOrder.length + 3 // place + user + total

	return (
		<main>
			<ContestNavigationBar />
			<Typography
				variant='h6'
				fontWeight='bold'
				mt='1rem'
				align='center'
				sx={{ color: '#452c54', fontWeight: 'bold' }}
			>
				კონტესტის შედეგები
			</Typography>
			<Typography
				paragraph
				align='center'
				pt='0.4rem'
				pb='1rem'
				borderBottom='2px dashed #aaa'
			></Typography>
			<Container maxWidth='lg'>
				<Box
					sx={{
						display: 'flex',
						justifyContent: 'space-between',
						alignItems: 'center',
						mb: 2,
					}}
				>
					<FormControlLabel
						control={
							<Checkbox
								checked={includeUpsolving}
								onChange={(e) => handleIncludeUpsolvingChange(e.target.checked)}
								color='primary'
							/>
						}
						label='include upsolving'
					/>
				</Box>

				<TableContainer component={Paper} sx={{ marginInline: 'auto' }}>
					<Table sx={{ marginX: 'auto' }}>
						<TableHead>
							<TableRow>
								<TableCell sx={{ fontWeight: 'bold' }}>ადგილი</TableCell>
								<TableCell sx={{ fontWeight: 'bold' }}>მომხმარებელი</TableCell>
								<TableCell
									sx={{
										fontWeight: 'bold',
										borderRight: '2px solid #ccc',
										whiteSpace: 'nowrap',
									}}
								>
									საბოლოო შედეგი
								</TableCell>
								{taskOrder.map((taskCode) => (
									<TableCell key={taskCode} sx={{ fontWeight: 'bold' }}>
										{getTaskName(taskCode)}
									</TableCell>
								))}
							</TableRow>
						</TableHead>
						<TableBody>
							{loading ? (
								<TableRow>
									<TableCell colSpan={columnCount} align='center'>
										იტვირთება...
									</TableCell>
								</TableRow>
							) : results.length === 0 && upsolvingOnlyResults.length === 0 ? (
								<TableRow>
									<TableCell colSpan={columnCount} align='center'>
										შედეგები ჯერ არ არის
									</TableCell>
								</TableRow>
							) : (
								<>
									{results.map((result, index) => (
										<TableRow
											key={`live-${result.contestantId}`}
											sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
										>
											<TableCell align='center'>{page * pageSize + index + 1}</TableCell>
											<TableCell component='th' scope='row'>
												{result.username}
											</TableCell>
											<TableCell
												component='th'
												scope='row'
												sx={{
													fontWeight: 'bold',
													borderRight: '2px solid #ccc',
													whiteSpace: 'nowrap',
												}}
											>
												{result.totalScore.toFixed(2)}
											</TableCell>
											{taskOrder.map((taskCode) => (
												<TableCell
													key={taskCode}
													align='center'
													sx={{
														backgroundColor: getScoreCellBackground(
															result.taskScores[taskCode]
														),
													}}
												>
													{getTaskScore(result, taskCode)}
												</TableCell>
											))}
										</TableRow>
									))}
									{upsolvingOnlyResults.map((result) => (
										<TableRow
											key={`up-${result.contestantId}`}
											sx={{
												backgroundColor: UPSOLVING_ONLY_ROW_COLOR,
												'&:last-child td, &:last-child th': { border: 0 },
											}}
										>
											<TableCell align='center'>—</TableCell>
											<TableCell component='th' scope='row'>
												{result.username}
											</TableCell>
											<TableCell
												component='th'
												scope='row'
												sx={{
													fontWeight: 'bold',
													borderRight: '2px solid #ccc',
													whiteSpace: 'nowrap',
												}}
											>
												{result.totalScore.toFixed(2)}
											</TableCell>
											{taskOrder.map((taskCode) => (
												<TableCell
													key={taskCode}
													align='center'
													sx={{
														backgroundColor: getScoreCellBackground(
															result.taskScores[taskCode]
														),
													}}
												>
													{getTaskScore(result, taskCode)}
												</TableCell>
											))}
										</TableRow>
									))}
								</>
							)}
						</TableBody>
					</Table>
				</TableContainer>

				<PaginationControls pagination={pagination} />
			</Container>
		</main>
	)
}
