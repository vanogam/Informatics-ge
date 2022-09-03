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

function handleResults(response, setResults){
    console.log(response)
	// var curTasks = []
	// const tasks = response.data.tasks
	// for(const task of tasks){
	// 	const taskId = task.task.id
	// 	const taskName = task.task.title.KA
	// 	const taskItem = {
	// 		id: taskId, 
	// 		name: taskName,
	// 		category: "A"
	// 	}
	// 	curTasks.push(taskItem)
	// }
	// setProblems(curTasks)
}
export default function Results(){
    const {contest_id} = useParams()
    console.log("YAAAY", contest_id)
	const [results, setResults] = useState([])
	useEffect(() => {
		axios
			.get(`http://localhost:8080/contest/${contest_id}/submissions`)
			.then((response) =>  handleResults(response, setResults))
			.catch((error) => console.log(error))
	}, [])

    return (
       <main>
			<Typography  variant="h6"
				fontWeight="bold"
				mt="1rem"
				align="center"
				sx={{ color: '#452c54', fontWeight: 'bold' }}>
				კონტესტის შედეგები
			</Typography>
			<Typography
				paragraph
				align="center"
				pt="0.4rem"
				pb="1rem"
				borderBottom="2px dashed #aaa"
			>
			</Typography>
			<Container maxWidth="lg">
				<Table sx={{ marginX: 'auto' }}>
					<TableHead>
						<TableRow>
							
							<TableCell>მომხმარებელი</TableCell>
							<TableCell >ამოცანა</TableCell>
                            <TableCell>ამოცანა</TableCell>
							<TableCell >სტატუსი</TableCell>
                            <TableCell >შედეგი</TableCell>
						</TableRow>
					</TableHead>
					<TableBody>
						{results.map((result) => (
							<TableRow
							
								key={result.name}
								sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
							>
								
                                <TableCell component="th" scope="row">
									{result.category}

								</TableCell>
								<TableCell ><NavLink to={`${result.id}`}exact>{result.name} </NavLink></TableCell>

							</TableRow>
						))}
					</TableBody>
				</Table>
			</Container>
		</main>
	)

};