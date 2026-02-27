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
	Pagination,
	Select,
	MenuItem,
	FormControl,
	InputLabel,
	Box,
	Checkbox,
	FormControlLabel,
} from '@mui/material'
import { useState, useEffect, useContext } from 'react'
import { AxiosContext } from '../utils/axiosInstance'
import ContestNavigationBar from '../Components/ContestNavigationBar'
import { getScoreCellBackground } from '../styles/scoreColors'

export default function Results() {
	const axiosInstance = useContext(AxiosContext)
	const { contest_id } = useParams()
	const [results, setResults] = useState([])
	const [standings, setStandings] = useState([])
	const [upsolvingStandings, setUpsolvingStandings] = useState([])
	const [taskOrder, setTaskOrder] = useState([])
	const [taskNameMap, setTaskNameMap] = useState({})
	const [page, setPage] = useState(0) // 0-indexed for offset calculation
	const [pageSize, setPageSize] = useState(20)
	const [totalCount, setTotalCount] = useState(0)
	const [loading, setLoading] = useState(false)
	const [includeUpsolving, setIncludeUpsolving] = useState(false)

	const pageSizeOptions = [20, 50, 100, 200]

	useEffect(() => {
		loadStandings()
	}, [contest_id, page, pageSize, axiosInstance])

	useEffect(() => {
		const loadUpsolvingStandings = () => {
			axiosInstance
				.get(`/contest/${contest_id}`)
				.then((response) => {
					const upStandings = response.data.upsolvingStandings || []
					setUpsolvingStandings(upStandings)
				})
				.catch((error) => {
					console.error('Error fetching upsolving standings:', error)
					setUpsolvingStandings([])
				})
		}

		if (contest_id) {
			loadUpsolvingStandings()
		}
	}, [contest_id, axiosInstance])

	const loadStandings = () => {
		setLoading(true)
		const offset = page * pageSize

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

				if (fetchedStandings.length === pageSize) {
					setTotalCount((page + 1) * pageSize + 1)
				} else {
					setTotalCount(offset + fetchedStandings.length)
				}

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

		const processedResults = standings.map((standing) => {
			const displayName = standing.username || `deleted`

			const taskScores = {}
			const upsolvingTasks = {}

			const liveTaskResults = standing.taskResults || {}
			const upEntry = upMap.get(standing.contestantId)
			const upTaskResults = (upEntry && upEntry.taskResults) || {}

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

			return {
				contestantId: standing.contestantId,
				username: displayName,
				totalScore,
				taskScores,
				upsolvingTasks,
			}
		})

		setResults(processedResults)
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

	const handlePageChange = (event, newPage) => {
		setPage(newPage - 1) // Material-UI Pagination is 1-indexed
	}

	const handlePageSizeChange = (event) => {
		const newPageSize = event.target.value
		setPageSize(newPageSize)
		setPage(0) // Reset to first page when changing page size
	}

	const totalPages = Math.ceil(totalCount / pageSize)

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
								onChange={(e) => setIncludeUpsolving(e.target.checked)}
								color='primary'
							/>
						}
						label='include upsolving'
					/>
					<FormControl size='small' sx={{ minWidth: 120 }}>
						<InputLabel>გვერდის ზომა</InputLabel>
						<Select
							value={pageSize}
							label='გვერდის ზომა'
							onChange={handlePageSizeChange}
						>
							{pageSizeOptions.map((size) => (
								<MenuItem key={size} value={size}>
									{size}
								</MenuItem>
							))}
						</Select>
					</FormControl>
				</Box>

				<TableContainer component={Paper} sx={{ marginInline: 'auto' }}>
					<Table sx={{ marginX: 'auto' }}>
						<TableHead>
							<TableRow>
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
									<TableCell colSpan={taskOrder.length + 2} align='center'>
										იტვირთება...
									</TableCell>
								</TableRow>
							) : results.length === 0 ? (
								<TableRow>
									<TableCell colSpan={taskOrder.length + 2} align='center'>
										შედეგები ჯერ არ არის
									</TableCell>
								</TableRow>
							) : (
								results.map((result) => (
									<TableRow
										key={result.contestantId}
										sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
									>
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
								))
							)}
						</TableBody>
					</Table>
				</TableContainer>

				{/* Pagination */}
				{totalCount > 0 && (
					<Box sx={{ display: 'flex', justifyContent: 'center', mt: 3, mb: 3 }}>
						<Pagination
							count={totalPages}
							page={page + 1} // Material-UI Pagination is 1-indexed
							onChange={handlePageChange}
							color='primary'
							showFirstButton
							showLastButton
						/>
					</Box>
				)}
			</Container>
		</main>
	)
}
