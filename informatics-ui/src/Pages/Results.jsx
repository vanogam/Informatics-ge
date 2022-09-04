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

function handleResults(response, setResults, response2){
    console.log(response)
	var curResults =  []
	const standings = response.data.standings
	const taskNameMap = response.data.taskNameMap
	const taskNames = response2.data.taskNames
	for(const standing of standings){
		const scores = standing.scores
		var standingItem = {
			name: standing.username,
			score: standing.totalScore,
			a: "-",
			b: "-"
		}
		console.log("TaskNames", taskNames)
		console.log("Scoes", scores)
		console.log("Map", taskNameMap)
		for (const [key, value] of Object.entries(scores)) {
			console.log(key, value);
			for (const [key2, value2] of Object.entries(taskNameMap)){
				if(key + ":KA" == key2) {
				  if (taskNames[0] && (taskNames[0] == value2)){
					  standingItem["a"] = value
				  }else if( taskNames[1] && (taskNames[1] == value2)){
					  standingItem["b"]= value
				  }
				}
			}
		  }
		  console.log(standingItem)
		curResults.push(standingItem)
	}
	setResults(curResults)
}
export default function Results(){
    const {contest_id} = useParams()
	const [results, setResults] = useState([])
	const [taskNames, setTaskNames] = useState([])
	useEffect(() => {
		axios
			.get(`http://localhost:8080/contest/${contest_id}/standings`,
			{params: {
				offset : 0 , 
				limit: 20
			}})
			.then((response1) =>  
			{
				axios
			.get(`http://localhost:8080/contest/${contest_id}/task-names`)
			.then((response2) =>  {setTaskNames(response2.data.taskNames); handleResults(response1, setResults, response2)})
			})
			
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
							
							<TableCell >მომხმარებელი 👨‍💻</TableCell>
                            <TableCell>ამოცანა "{taskNames[0]? taskNames[0]: "-"}" </TableCell>
							<TableCell> ამოცანა "{taskNames[1]? taskNames[0]: "-"}"</TableCell>
							<TableCell>საბოლოო შედეგი 🏆</TableCell>
			
						</TableRow>
					</TableHead>
					<TableBody>
						{results.map((result) => (
							<TableRow
							
								key={result.name}
								sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
							>
								
                                <TableCell component="th" scope="row">
									{result.name}

								</TableCell>
								<TableCell component="th" scope="row">
									{result.a}

								</TableCell>
								<TableCell component="th" scope="row">
									{result.b}

								</TableCell>
								<TableCell component="th" scope="row">
									{result.score}

								</TableCell>

							</TableRow>
						))}
					</TableBody>
				</Table>
			</Container>
		</main>
	)

};