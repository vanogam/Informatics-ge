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
} from '@mui/material'
import { useNavigate } from "react-router-dom";
import { useState, useEffect, useContext } from 'react'
import { AxiosContext } from '../utils/axiosInstance'
import ContestNavigationBar from '../Components/ContestNavigationBar'
import getMessage from '../Components/lang'

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
			score: score
		}
		curTasks.push(taskItem)
	}
	setProblems(curTasks)
}
const baseTransparency = 0.2
const hoverTransparency = 0.3
function getRowColor(score) {
	if (score === null || score === undefined) {
		return 'transparent' // uncolored
	}
	if (score < 0.1) {
		return `rgba(244, 67, 54, ${baseTransparency})` // red (light)
	}
	if (score > 0.1 && score < 99.9) {
		return `rgba(255, 193, 7, ${baseTransparency})` // yellow (light)
	}
	if (score > 99.9) {
		return `rgba(76, 175, 80, ${baseTransparency})` // green (light)
	}
	return 'transparent' // default
}

export default function Archive(){
	const axiosInstance = useContext(AxiosContext)
	const navigate = useNavigate()
	const [problems , setProblems] = useState([])
	useEffect(() => {
		axiosInstance
			.get('/room/1/tasks', {
				params:{
					offset : 0 , 
					limit: 100
				}
			})
			.then((response) =>  handleContestResponse(response, setProblems))
			.catch((error) => {
				console.error('Error loading archive tasks:', error)
				setProblems([])
			})
	}, [axiosInstance])

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
			<TableContainer component={Paper} sx={{ marginInline: 'auto' }}>
				<Table sx={{ marginX: 'auto' }}>
					<TableHead>
						<TableRow>
							<TableCell>{getMessage('ka', 'name')}</TableCell>
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
								const rowColor = getRowColor(problem.score)
								return (
									<TableRow
										key={`${problem.contestId}-${problem.id}`}
										onClick={() => navigate(`/contest/${problem.contestId}/problem/${problem.id}`)}
										sx={{ 
											'&:last-child td, &:last-child th': { border: 0 }, 
											cursor: 'pointer',
											backgroundColor: rowColor,
											'&:hover': {
												backgroundColor: rowColor === 'transparent' 
													? 'rgba(0, 0, 0, 0.04)' 
													: rowColor.replace(`, ${baseTransparency}`, `, ${hoverTransparency}`) // Darker on hover
											}
										}}
									>
										<TableCell>{problem.name}</TableCell>
										<TableCell>{problem.contestName}</TableCell>
										<TableCell>{problem.score !== null && problem.score !== undefined && problem.score.toFixed(1)}</TableCell>
									</TableRow>
								)
							})
						)}
					</TableBody>
				</Table>
				</TableContainer>
			</Container>
		</main>
	)
    
    

};