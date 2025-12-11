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
} from '@mui/material'
import { useState, useEffect, useContext } from 'react'
import { AxiosContext } from '../utils/axiosInstance'
import ContestNavigationBar from '../Components/ContestNavigationBar'

export default function Results() {
	const axiosInstance = useContext(AxiosContext)
	const { contest_id } = useParams()
	const [results, setResults] = useState([])
	const [taskOrder, setTaskOrder] = useState([])
	const [taskNameMap, setTaskNameMap] = useState({})
	const [page, setPage] = useState(0) // 0-indexed for offset calculation
	const [pageSize, setPageSize] = useState(20)
	const [totalCount, setTotalCount] = useState(0)
	const [loading, setLoading] = useState(false)

	const pageSizeOptions = [20, 50, 100, 200]

	useEffect(() => {
		loadStandings()
	}, [contest_id, page, pageSize, axiosInstance])

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
				const standings = response.data.standings || []
				const taskNameMapData = response.data.taskNameMap || {}

				const taskCodesFromBackend = Object.keys(taskNameMapData)
				const backendTaskCodesSet = new Set(taskCodesFromBackend)

				// Collect any task codes from standings that aren't in the backend map
				const additionalTaskCodes = []
				standings.forEach((standing) => {
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

				const processedResults = standings.map((standing) => {
					const displayName = standing.username || `User ${standing.contestantId}`

					const taskScores = {}
					if (standing.taskResults) {
						Object.entries(standing.taskResults).forEach(([taskCode, taskResult]) => {
							taskScores[taskCode] = taskResult.score || 0
						})
					}

					return {
						contestantId: standing.contestantId,
						username: displayName,
						totalScore: standing.totalScore || 0,
						taskScores: taskScores,
					}
				})

				setResults(processedResults)

				if (standings.length === pageSize) {
					setTotalCount((page + 1) * pageSize + 1)
				} else {
					setTotalCount(offset + standings.length)
				}

				setLoading(false)
			})
			.catch((error) => {
				console.error('Error fetching standings:', error)
				setLoading(false)
			})
	}

	const getTaskName = (taskCode) => {
		return taskNameMap[taskCode] || taskCode
	}

	const getTaskScore = (result, taskCode) => {
		return result.taskScores[taskCode] !== undefined
			? result.taskScores[taskCode].toFixed(2)
			: '-'
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
				<Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
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
								{taskOrder.map((taskCode) => (
									<TableCell key={taskCode} sx={{ fontWeight: 'bold' }}>
										{getTaskName(taskCode)}
									</TableCell>
								))}
								<TableCell sx={{ fontWeight: 'bold' }}>საბოლოო შედეგი</TableCell>
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
										{taskOrder.map((taskCode) => (
											<TableCell key={taskCode} align='center'>
												{getTaskScore(result, taskCode)}
											</TableCell>
										))}
										<TableCell component='th' scope='row' sx={{ fontWeight: 'bold' }}>
											{result.totalScore.toFixed(2)}
										</TableCell>
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
