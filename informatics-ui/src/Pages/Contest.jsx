import { useParams } from 'react-router-dom'
import {
	Container,
	Typography,
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableRow,
} from '@mui/material'
import { NavLink } from "react-router-dom";
import axios from 'axios';
import { useState, useEffect } from 'react';

function handleContestResponse(response, setProblems){
	var curTasks = []
	const tasks = response.data.tasks
	var curCategory = 1
	for(const task of tasks){
		
		const taskId = task.task.id
		const taskName = task.task.title.KA
		const taskItem = {
			id: taskId, 
			name: taskName,
			category: curCategory
		}
		curCategory += 1
		curTasks.push(taskItem)
	}
	setProblems(curTasks)
}
export default function Contest(){
    const {contest_id} = useParams()
	const [problems , setProblems] = useState([])
	useEffect(() => {
		axios
			.get(`http://localhost:8080/contest/${contest_id}/tasks`, {
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
			<Typography variant="h6"
				fontWeight="bold"
				mt="1rem"
				align="center"
				sx={{ color: '#452c54', fontWeight: 'bold' }}>
				კონტესტის ამოცანები
			</Typography>
			<Typography
				paragraph
				align="center"
				pt="0.5rem"
				pb="1rem"
				borderBottom="2px dashed #aaa"
			>
				
			</Typography>
			<Container maxWidth="lg">
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
								<TableCell ><NavLink to={`${problem.id}`}exact>{problem.name} </NavLink></TableCell>

							</TableRow>
						))}
					</TableBody>
				</Table>
			</Container>
		</main>
	)
    
    

};