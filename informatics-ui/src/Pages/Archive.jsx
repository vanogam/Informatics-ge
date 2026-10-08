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
	Autocomplete,
	TextField,
	Box,
	Stack,
} from '@mui/material'
import { useNavigate } from "react-router-dom";
import { useState, useEffect, useContext } from 'react'
import { AxiosContext } from '../utils/axiosInstance'
import { AuthContext } from '../store/authentication'
import { getScoreRowBackground, getScoreRowHoverBackground } from '../styles/scoreColors'
import ContestNavigationBar from '../Components/ContestNavigationBar'
import TaskTags from '../Components/TaskTags'
import getMessage from '../Components/lang'
import { usePagination } from '../utils/usePagination'
import PaginationControls from '../Components/PaginationControls'

function handleContestResponse(response, setProblems){
	var curTasks = []
	const tasks = response.data.tasks
	if (!tasks) {
		setProblems([])
		return
	}
	// Sort tasks by order
	const sortedTasks = [...tasks].sort((a, b) => {
		const orderA = a.task?.order || 0
		const orderB = b.task?.order || 0
		return orderA - orderB
	})
	
	for(const task of sortedTasks){
		const taskId = task.task.id
		const taskName = task.task.title
		const contestId = task.task.contestId
		const contestName = task.contestName
		const score = task.score
		const taskItem = {
			id: taskId,
			name: taskName,
			contestId: contestId,
			contestName: contestName,
			score: score,
			tags: task.tags || []
		}
		curTasks.push(taskItem)
	}
	setProblems(curTasks)
}
const baseTransparency = 0.2
const hoverTransparency = 0.3

export default function Archive(){
	const axiosInstance = useContext(AxiosContext)
	const authContext = useContext(AuthContext)
	const isStaff = (authContext.role || '').includes('ADMIN') || (authContext.role || '').includes('TEACHER')
	const navigate = useNavigate()
	const [problems , setProblems] = useState([])
	const [tagFilter, setTagFilter] = useState('')
	const [allTags, setAllTags] = useState([])
	const pagination = usePagination()
	const {offset, pageSize, setTotalCount, resetPage} = pagination

	useEffect(() => {
		axiosInstance.get('/tags')
			.then((response) => setAllTags(response.data.tags || []))
			.catch(_ => {})
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [axiosInstance])

	useEffect(() => {
		axiosInstance
			.get('/room/1/tasks', {
				params:{
					offset: offset,
					limit: pageSize,
					tag: tagFilter || undefined
				}
			})
			.then((response) => {
				handleContestResponse(response, setProblems)
				setTotalCount(response.data.totalCount || 0)
			})
			.catch((error) => {
				console.error('Error loading archive tasks:', error)
				setProblems([])
				setTotalCount(0)
			})
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [axiosInstance, offset, pageSize, tagFilter])

	const setRowTags = (taskId) => (newTags) => {
		setProblems((prev) => prev.map((p) => (p.id === taskId ? {...p, tags: newTags} : p)))
	}

    return (
       <main>
			<ContestNavigationBar />
			<Typography  variant="h6"
				fontWeight="bold"
				mt="1rem"
				align="center"
				sx={{ color: '#452c54', fontWeight: 'bold' }}>
				არქივი
			</Typography>
			<Typography
				paragraph
				align="center"
				pt="0.5rem"
				pb="1rem"
				borderBottom="2px dashed #aaa"
			>
				ამ გვერდზე შეგიძლიათ იხილოთ დაარქივებული ამოცანები
			</Typography>
			<Container maxWidth="lg">
			<Box sx={{ marginBottom: '1rem', maxWidth: '20rem' }}>
				<Autocomplete
					options={allTags}
					value={tagFilter || null}
					onChange={(_, value) => {
						setTagFilter(value || '')
						resetPage()
					}}
					renderInput={(params) => (
						<TextField {...params} label={getMessage('ka', 'filterByTag')} size="small"/>
					)}
				/>
			</Box>
			<TableContainer component={Paper} sx={{ marginInline: 'auto' }}>
				<Table sx={{ marginX: 'auto' }}>
					<TableHead>
						<TableRow>
							<TableCell sx={{width: '60%'}}>{getMessage('ka', 'name')}</TableCell>
							<TableCell>{getMessage('ka', 'contest')}</TableCell>
							<TableCell>{getMessage('ka', 'score')}</TableCell>
						</TableRow>
					</TableHead>
					<TableBody>
						{problems.length === 0 ? (
							<TableRow>
								<TableCell colSpan={3} align="center">
									<Typography variant="body2" color="text.secondary">
										არ არის ხელმისაწვდომი ამოცანები
									</Typography>
								</TableCell>
							</TableRow>
						) : (
							problems.map((problem) => {
								const rowColor = getScoreRowBackground(problem.score, baseTransparency)
								const hoverColor = getScoreRowHoverBackground(
									problem.score,
									baseTransparency,
									hoverTransparency
								)
								return (
									<TableRow
										key={`${problem.contestId}-${problem.id}`}
										onClick={() => navigate(`/contest/${problem.contestId}/problem/${problem.id}`)}
										sx={{ 
											'&:last-child td, &:last-child th': { border: 0 }, 
											cursor: 'pointer',
											backgroundColor: rowColor,
											'&:hover': {
												backgroundColor: hoverColor,
											}
										}}
									>
										<TableCell>
											<Stack direction="row" alignItems="center" spacing={1} flexWrap="wrap" sx={{width: '100%'}}>
												<span>{problem.name}</span>
												<TaskTags
													taskId={problem.id}
													tags={problem.tags}
													setTags={setRowTags(problem.id)}
													editable={isStaff}
													allTags={allTags}
												/>
											</Stack>
										</TableCell>
										<TableCell>{problem.contestName}</TableCell>
										<TableCell>{problem.score !== null && problem.score !== undefined && problem.score.toFixed(1)}</TableCell>
									</TableRow>
								)
							})
						)}
					</TableBody>
				</Table>
				</TableContainer>
				<PaginationControls pagination={pagination} />
			</Container>
		</main>
	)
    
    

};