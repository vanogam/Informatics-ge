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
} from '@mui/material'
import BarChartIcon from '@mui/icons-material/BarChart';
import { NavLink } from "react-router-dom";
import { useState, useEffect, useContext } from 'react'
import { AxiosContext, getAxiosInstance } from '../utils/axiosInstance'
import ContestNavigationBar from '../Components/ContestNavigationBar'

function handleContestResponse(response, setProblems){
	var curTasks = []
	const tasks = response.data.tasks
	for(const task of tasks){
		const taskId = task.task.id
		const taskName = task.task.title.KA
		const taskItem = {
			id: taskId, 
			name: taskName,
			category: "A"
		}
		curTasks.push(taskItem)
	}
	setProblems(curTasks)
}
export default function Archive(){
	const axiosInstance = useContext(AxiosContext)
	const {contest_id} = useParams()
	const [problems , setProblems] = useState([])
	useEffect(() => {
		axiosInstance
			.get('/room/1/tasks?offset=0&limit=20', {
				params:{
					offset : 0 , 
					limit: 20
				}
			})
			.then((response) =>  handleContestResponse(response, setProblems))
			.catch((error) => console.log(error))
	}, [])

    // const rows = [
	// 	{
	// 		id: 1,
    //         category: "a",
	// 		name: 'Problem1',
			
	// 	},
    //     {
	// 		id: 2,
    //         category: "b",
	// 		name: 'Problem2',
	// 	},
    // ]
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
							
							<TableCell>კატეგორია</TableCell>
							<TableCell >სახელი</TableCell>
					
						</TableRow>
					</TableHead>
					<TableBody>
						{problems.map((problem) => (
							<TableRow
							
								key={problem.name}
								sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
							>
								
                                <TableCell component="th" scope="row">
									{problem.category}

								</TableCell>
								<TableCell ><NavLink  style={{ color: 'black', textDecorationLine: 'none' }} to={`/contest/${contest_id}/${problem.id}`}exact={"true"}>{problem.name} </NavLink></TableCell>

							</TableRow>
						))}
					</TableBody>
				</Table>
				</TableContainer>
			</Container>
		</main>
	)
    
    

};